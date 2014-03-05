package isf.command;

import isf.ISFUtil;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.Main;
import isf.util.OntologyFilesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "typeCheck", commandDescription = "Checks if an IRI has more than "
		+ "one type in the files, directories, and any of their imports."
		+ " Imports are loaded from local files when possible, otherwise, loaded online. "
		+ "All ontologies are loaded from/into a single manager.")
public class TypecheckCommand extends AbstractCommand {

	// ================================================================================
	// Files and directories to check types
	// ================================================================================

	public List<File> files;
	public boolean fileSet;

	@Parameter(names = "-files",
			description = "One or more files or directories to check IRI types.",
			converter = CanonicalFileConverter.class)
	public void setFiles(List<File> files) {
		this.files = files;
		this.fileSet = true;
	}

	public List<File> getFiles() {
		return files;
	}

	// ================================================================================
	// Subdirectories
	// ================================================================================

	public boolean subDir = true;
	public boolean subDirSet;

	public boolean isSubDir() {
		return subDir;
	}

	@Parameter(names = "-subs", arity = 1, description = "Also check sub directories.")
	public void setSubDir(boolean subDir) {
		this.subDir = subDir;
		this.subDirSet = true;
	}

	// ================================================================================
	// Add typing axioms where needed?
	// ================================================================================

	@Parameter(
			names = "-addTypes",
			description = "Will add type axioms (declartions) where an IRI is used "
					+ "but its type is not asserted. This helps resolve certain reasoning issues. If an IRI "
					+ "has multiple types as reported by thsi tool, all types will be asserted by this action so "
					+ "the files should be cleaned up before doing this if needed.")
	public boolean addTypes;
	public boolean addTypesSet;

	// ================================================================================
	// Implementation
	// ================================================================================

	// Map<File, OWLOntology> ontologies = new HashMap<File, OWLOntology>();

	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	OntologyFilesUtil ontologyFiles;
	Map<IRI, Set<OWLEntity>> iriToEntityMap = new HashMap<IRI, Set<OWLEntity>>();

	Set<OWLOntology> ontologies = new HashSet<OWLOntology>();

	public TypecheckCommand(Main main) {
		super(main);
	}

	@Override
	public void run() {
		ontologyFiles = new OntologyFilesUtil(files);

		Map<File, Exception> exceptions = new HashMap<File, Exception>();
		//
		man = OWLManager.createOWLOntologyManager();
		// setup base files
		OntologyFilesUtil baseFiles = new OntologyFilesUtil(main.localOwlFiles);
		baseFiles.setupManager(man, main.subLocalOwlFiles, exceptions);
		// setup job files
		ontologyFiles.setupManager(man, subDir, exceptions);
		for (Entry<File, Exception> entry : exceptions.entrySet())
		{
			System.out.println("Found error while loading manger for file:" + entry.getKey()
					+ " with exception: " + entry.getValue());
		}
		exceptions.clear();

		for (Entry<File, IRI> entry : ontologyFiles.getLocalOntologyFiles(subDir, exceptions)
				.entrySet())
		{
			ontologies.add(ISFUtil.getOrLoadOntology(entry.getValue(), man));
		}

		for (String action : getAllActions())
		{
			Action.valueOf(action).execute(this);
		}

	}

	@Override
	protected List<String> getCommandActions(List<String> actionsList) {
		actionsList.add(Action.duplicatIris.name());
		actionsList.add(Action.duplicateTypes.name());
		if (addTypes)
		{
			actionsList.add(Action.addTypes.name());
		}
		return actionsList;
	}

	enum Action {
		duplicatIris {

			@Override
			public void execute(TypecheckCommand command) {
				// show duplicates
				Map<File, Exception> exceptions = new HashMap<File, Exception>();
				Map<IRI, List<File>> duplicates = command.ontologyFiles.getDuplicateIris(
						command.subDir, exceptions);

				for (Entry<File, Exception> entry : exceptions.entrySet())
				{
					command.warn("Errors getting duplicate IRIs. File: "
							+ entry.getKey().getAbsolutePath(), entry.getValue());
				}

				// first warn about duplicate IRIs that will hide files from the
				// type
				// checking
				for (Entry<IRI, List<File>> entry : duplicates.entrySet())
				{
					command.warn("Duplicate IRI: " + entry.getKey(), null);
					command.indent++;
					for (File file : entry.getValue())
					{
						command.info("in file: " + file.getAbsolutePath());
					}
					command.indent--;
				}
			}
		},
		duplicateTypes {

			@Override
			public void execute(TypecheckCommand command) {
				for (OWLOntology o : command.ontologies)
				{
					for (OWLEntity entity : o.getSignature(true))
					{
						Set<OWLEntity> entities = command.iriToEntityMap.get(entity.getIRI());
						if (entities == null)
						{
							entities = new HashSet<OWLEntity>();
							command.iriToEntityMap.put(entity.getIRI(), entities);

						}
						entities.add(entity);
					}

				}

				// now iterate over all IRIs and their entities
				for (Entry<IRI, Set<OWLEntity>> entry : command.iriToEntityMap.entrySet())
				{
					if (entry.getValue().size() > 1)
					{
						command.warn("Multiple types: " + entry.getKey(), null);
						command.indent++;
						String types = "";
						for (OWLEntity entity : entry.getValue())
						{
							types += entity.getEntityType() + " ";
						}
						command.info(types);
						command.indent--;
					}
				}
			}

		},
		addTypes {

			@Override
			public void execute(TypecheckCommand command) {
				Map<IRI, Set<OWLOntology>> iriToOntologiesMap = new HashMap<IRI, Set<OWLOntology>>();

				// we want to intentionally ignore imports
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				man.clearIRIMappers();
				man.setSilentMissingImportsHandling(true);

				OntologyFilesUtil files = new OntologyFilesUtil(command.files);
				files.setupManager(man, command.subDir, new HashMap<File, Exception>());

				for (Entry<File, IRI> entry : files.getLocalOntologyFiles(command.subDir,
						new HashMap<File, Exception>()).entrySet())
				{
					OWLOntology o = ISFUtil.getOrLoadOntology(entry.getValue(), man);

					for (OWLAxiom axiom : o.getAxioms())
					{
						for (OWLEntity entity : axiom.getSignature())
						{
							Set<OWLOntology> entityOntologies = iriToOntologiesMap.get(entity
									.getIRI());
							if (entityOntologies == null)
							{
								entityOntologies = new HashSet<OWLOntology>();
								iriToOntologiesMap.put(entity.getIRI(), entityOntologies);
							}
							entityOntologies.add(o);
						}

						if (axiom instanceof OWLAnnotationAssertionAxiom)
						{
							OWLAnnotationSubject subject = ((OWLAnnotationAssertionAxiom) axiom)
									.getSubject();
							if (subject instanceof IRI)
							{
								IRI subjectIri = (IRI) subject;
								Set<OWLOntology> entityOntologies = iriToOntologiesMap
										.get(subjectIri);
								if (entityOntologies == null)
								{
									entityOntologies = new HashSet<OWLOntology>();
									iriToOntologiesMap.put(subjectIri, entityOntologies);
								}
								entityOntologies.add(o);

							}
						}
					}
				}

				Set<OWLOntology> changedOntologies = new HashSet<OWLOntology>();
				for (Entry<IRI, Set<OWLOntology>> entry : iriToOntologiesMap.entrySet())
				{
					Set<OWLEntity> entities = command.iriToEntityMap.get(entry.getKey());
					if (entities != null)
					{
						for (OWLEntity entity : entities)
						{

							OWLDeclarationAxiom da = man.getOWLDataFactory()
									.getOWLDeclarationAxiom(entity);
							for (OWLOntology o : entry.getValue())
							{
								if (man.addAxiom(o, da).size() > 0)
								{
									changedOntologies.add(o);
									command.info("Add declaration: " + da + " to ontology: "
											+ o.getOntologyID().getOntologyIRI());
								}
							}
						}

					}

				}

				for (OWLOntology o : changedOntologies)
				{
					try
					{
						man.saveOntology(o);
					} catch (OWLOntologyStorageException e)
					{
						throw new RuntimeException("Failed to save ontology after adding types. "
								+ o.getOntologyID().getOntologyIRI(), e);
					}
				}
			}
		};

		public abstract void execute(TypecheckCommand command);
	}

}
