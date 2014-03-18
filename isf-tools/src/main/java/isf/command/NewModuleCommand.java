package isf.command;

import isf.command.cli.IriConverter;
import isf.command.cli.Main;
import isf.util.ISF;
import isf.util.ISFTVocab;
import isf.util.ISFUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "newModule", commandDescription = "The command to create a new module.")
public class NewModuleCommand extends AbstractCommand {

	// ================================================================================
	// The module name
	// ================================================================================
	public String moduleName;
	public boolean moduleNameSet;

	@Parameter(
			names = "-name",
			description = "The module name. This will be used to create default IRIs, files, and folders.")
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
		this.moduleNameSet = true;
	}

	public String getModuleName() {
		return moduleName;
	}

	// ================================================================================
	// The output directory
	// ================================================================================

	public File directory;

	public File getDirectory() {
		return directory;
	}

	@Parameter(names = "-directory",
			description = "The directory where the module will be created.")
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	// ================================================================================
	// The source IRIs for the module
	// ================================================================================
	public List<IRI> sourceIris;
	public boolean sourceIrisSet;

	@Parameter(names = "-sourceIris", converter = IriConverter.class,
			description = "The source IRIs that will be used for this module.")
	public void setSourceIris(List<IRI> sourceIris) {
		this.sourceIris = sourceIris;
		this.sourceIrisSet = true;
	}

	public List<IRI> getSourceIris() {
		return sourceIris;
	}

	// ================================================================================
	// The final IRI of the module
	// ================================================================================
	public IRI iri;
	public boolean iriSet;

	@Parameter(names = "-iri", description = "The generated module's IRI",
			converter = IriConverter.class)
	public void setIri(IRI iri) {
		this.iri = iri;
		this.iriSet = true;
	}

	public IRI getIri() {
		return iri;
	}

	// ================================================================================
	// The final IRI of the module
	// ================================================================================
	public String fileName;
	public boolean fileNameSet;

	@Parameter(names = "-fileName", description = "The generated module's file name.")
	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.fileNameSet = true;
	}

	public String getFileName() {
		return fileName;
	}

	// ================================================================================
	// The IRI prefix of the module's files
	// ================================================================================
	public String iriPrefix;
	public boolean iriPrefixSet;

	@Parameter(names = "-iriPrefix",
			description = "The IRI prefix for the module's various owl files. "
					+ "It should end with a forward slash.", converter = IriConverter.class)
	public void setIriPrefix(String iriPrefix) {
		this.iriPrefix = iriPrefix;
		this.iriPrefixSet = true;
	}

	public String getIriPrefix() {
		return iriPrefix;
	}

	// ================================================================================
	// Implementation
	// ================================================================================

	public NewModuleCommand(Main main) {
		super(main);
		preConfigure();
	}

	@Override
	protected void preConfigure() {
		moduleName = "new-module";
		directory = new File(main.getJobDirectory(), "module");

		sourceIris = new ArrayList<IRI>();
		sourceIris.add(ISF.ISF_DEV_IRI);

		iri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + moduleName + ISF.MODULE_IRI_SUFFIX);
		fileName = moduleName + ISF.MODULE_IRI_SUFFIX;
		iriPrefix = ISF.ISF_ONTOLOGY_IRI_PREFIX;
	}

	@Override
	protected void init() {
		directory.mkdirs();

		man = OWLManager.createOWLOntologyManager();
		man.clearIRIMappers();
		man.addIRIMapper(new AutoIRIMapper(directory, false));
		man.setSilentMissingImportsHandling(true);

		df = man.getOWLDataFactory();

		topIri = IRI.create(iriPrefix + moduleName + ISF.TOP_IRI_SUFFIX);
		annotationIri = IRI.create(iriPrefix + moduleName + ISF.ANNOTATION_IRI_SUFFIX);
		includeIri = IRI.create(iriPrefix + moduleName + ISF.MODULE_INCLUDE_IRI_SUFFIX);
		excludeIri = IRI.create(iriPrefix + moduleName + ISF.MODULE_EXCLUDE_IRI_SUFFIX);
		legacyIri = IRI.create(iriPrefix + moduleName + ISF.MODULE_LEGACY_IRI_SUFFIX);
		isfToolsIri = IRI.create(ISF.ISF_ONTOLOGY_IRI_PREFIX + ISF.ISF_TOOLS_IRI);

	}

	OWLOntologyManager man;
	OWLDataFactory df;
	IRI topIri;
	IRI annotationIri;
	IRI includeIri;
	IRI excludeIri;
	IRI legacyIri;
	IRI legacyRemovedIri;
	IRI isfToolsIri;

	@Override
	public void run() {
		init();

		try
		{
			// include
			OWLOntology includeOntology = getOrLoadOrCreateOntology(includeIri, man);
			man.saveOntology(includeOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + ISF.MODULE_INCLUDE_IRI_SUFFIX)));

			// exclude
			OWLOntology excludeOntology = getOrLoadOrCreateOntology(excludeIri, man);
			man.saveOntology(excludeOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + ISF.MODULE_EXCLUDE_IRI_SUFFIX)));

			// legacy
			OWLOntology legacyOntology = getOrLoadOrCreateOntology(legacyIri, man);
			man.saveOntology(legacyOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + ISF.MODULE_LEGACY_IRI_SUFFIX)));

			// legacy removed
			OWLOntology legacyRemovedOntology = getOrLoadOrCreateOntology(legacyRemovedIri, man);
			man.saveOntology(legacyRemovedOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + ISF.MODULE_LEGACY_REMOVED_IRI_SUFFIX)));

			// annotation
			OWLOntology annotationOntology = getOrLoadOrCreateOntology(annotationIri, man);
			// remove the old imports
			man.applyChange(new RemoveImport(annotationOntology, df
					.getOWLImportsDeclaration(includeIri)));
			man.applyChange(new RemoveImport(annotationOntology, df
					.getOWLImportsDeclaration(excludeIri)));
			man.applyChange(new RemoveImport(annotationOntology, df
					.getOWLImportsDeclaration(legacyIri)));
			man.applyChange(new RemoveImport(annotationOntology, df
					.getOWLImportsDeclaration(legacyRemovedIri)));
			for (IRI source : sourceIris)
			{
				man.applyChange(new AddImport(annotationOntology, df
						.getOWLImportsDeclaration(source)));
			}
			// ifs-tools.owl import
			man.applyChange(new AddImport(annotationOntology, df
					.getOWLImportsDeclaration(isfToolsIri)));

			// check/add the module IRI annotation
			Set<OWLAnnotationAssertionAxiom> axioms = ISFUtil.getAnnotationAssertionAxioms(
					annotationOntology, ISFTVocab.module_iri.getAP(), false);
			if (axioms.size() > 1)
			{
				logger.warn("Found multiple module IRI annotations for module: " + getModuleName());
			} else if (axioms.size() == 0)
			{
				man.applyChange(new AddOntologyAnnotation(annotationOntology, df.getOWLAnnotation(
						ISFTVocab.module_iri.getAP(),
						df.getOWLLiteral(iriPrefix + ISF.MODULE_IRI_SUFFIX))));
			}

			// check/add the module file name annotation
			axioms = ISFUtil.getAnnotationAssertionAxioms(annotationOntology,
					ISFTVocab.module_file_name.getAP(), false);
			if (axioms.size() > 1)
			{
				logger.warn("Found multiple module file name annotations for module: "
						+ getModuleName());
			} else if (axioms.size() == 0)
			{
				man.applyChange(new AddOntologyAnnotation(annotationOntology, df.getOWLAnnotation(
						ISFTVocab.module_file_name.getAP(), df.getOWLLiteral(fileName))));
			}

			man.saveOntology(annotationOntology, new FileOutputStream(new File(getDirectory(),
					fileName)));

			// top
			OWLOntology topOntology = getOrLoadOrCreateOntology(topIri, man);
			man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(annotationIri)));
			man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(includeIri)));
			man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(excludeIri)));
			man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(legacyIri)));
			man.applyChange(new AddImport(topOntology, df
					.getOWLImportsDeclaration(legacyRemovedIri)));
			man.saveOntology(topOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + ISF.MODULE_TOP_IRI_SUFFIX)));

		} catch (Exception e)
		{
			throw new RuntimeException("Failed to save module file", e);
		}

	}

	@Override
	protected void addCommandActions(List<String> actionsList) {

	}

}
