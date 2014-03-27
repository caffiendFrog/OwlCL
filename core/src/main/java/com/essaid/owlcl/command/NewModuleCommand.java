package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.essaid.owlcl.command.cli.IriConverter;
import com.essaid.owlcl.core.command.MainCommand;
import com.essaid.owlcl.module.ModuleVocab;
import com.essaid.owlcl.util.Owlcl;
import com.essaid.owlcl.util.OwlclUtil;
import com.essaid.owlcl.util.RuntimeOntologyLoadingException;

@Parameters(commandNames = "newModule", commandDescription = "The command to create a new module.")
public class NewModuleCommand extends AbstractCommand {

	// ================================================================================
	// The module name
	// ================================================================================
	private String moduleName;
	private boolean moduleNameSet;

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

	private File directory;
	private boolean directorySet;

	@Parameter(names = "-directory",
			description = "The directory where the module will be created.",
			converter = FileConverter.class)
	public void setDirectory(File directory) {
		this.directory = directory;
		this.directorySet = true;
	}

	public File getDirectory() {
		return directory;
	}

	// ================================================================================
	// The source IRIs for the module
	// ================================================================================
	private List<IRI> sourceIris;
	private boolean sourceIrisSet;

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
	private IRI iri;
	private boolean iriSet;

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
	// The final IRI of the module inferred
	// ================================================================================
	private IRI iriInferred;
	private boolean iriInferredSet;

	@Parameter(names = "-iriInferred", description = "The generated inferred module's IRI",
			converter = IriConverter.class)
	public void setIriInferred(IRI iri) {
		this.iriInferred = iri;
		this.iriInferredSet = true;
	}

	public IRI getIriInferred() {
		return iriInferred;
	}

	// ================================================================================
	// The final filename of the module
	// ================================================================================
	private String fileName;
	private boolean fileNameSet;

	@Parameter(names = "-fileName", description = "The generated module's file name.")
	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.fileNameSet = true;
	}

	public String getFileName() {
		return fileName;
	}

	// ================================================================================
	// The final filename of the module inferred
	// ================================================================================
	private String fileInferredName;
	private boolean fileInferredNameSet;

	@Parameter(names = "-fileNameInferred",
			description = "The generated inferred module's file name.")
	public void setFileInferredName(String fileName) {
		this.fileInferredName = fileName;
		this.fileInferredNameSet = true;
	}

	public String getFileInferredName() {
		return fileInferredName;
	}

	// ================================================================================
	// The IRI prefix of the module's files
	// ================================================================================
	private String iriPrefix;
	private boolean iriPrefixSet;

	@Parameter(
			names = "-iriPrefix",
			description = "The IRI prefix for the module's various owl files. "
					+ "It should end with a forward slash. There is a default but this could be useful. "
					+ "However, if you are migrating an exising module, make sure that the same prefix is used "
					+ "as before. Otherwise, files will be overwritting with new ones. Make sure you have a copy or a committed version before running this on an existing module in case there is a bug or you don't like the results.")
	public void setIriPrefix(String iriPrefix) {
		this.iriPrefix = iriPrefix;
		this.iriPrefixSet = true;
	}

	public String getIriPrefix() {
		return iriPrefix;
	}

	// ================================================================================
	// Initialization
	// ================================================================================
	
	private boolean inited;

	protected void configure() {
		moduleName = "_unnamed";
		if (getMain().getProject() == null)
		{
			directory = new File(getMain().getOutputDirectory(), "module/" + moduleName);
		} else
		{
			directory = new File(getMain().getProject(), "module/" + moduleName);
		}
		sourceIris = new ArrayList<IRI>();
		sourceIris.add(Owlcl.ISF_DEV_IRI);
		iri = IRI.create(Owlcl.ISF_ONTOLOGY_IRI_PREFIX + moduleName + Owlcl.MODULE_IRI_SUFFIX);
		iriInferred = IRI.create(Owlcl.ISF_ONTOLOGY_IRI_PREFIX + moduleName
				+ Owlcl.MODULE_IRI_INRERRED_SUFFIX);
		fileName = moduleName + Owlcl.MODULE_IRI_SUFFIX;
		fileInferredName = moduleName + Owlcl.MODULE_IRI_INRERRED_SUFFIX;
		iriPrefix = Owlcl.ISF_ONTOLOGY_IRI_PREFIX;
	}

	@SuppressWarnings("deprecation")
	protected void init() {
		if (inited)
		{
			return;
		}
		inited = true;

		if (getMain().getProject() == null)
		{
			directory = new File(getMain().getOutputDirectory(), "module/" + moduleName);
		} else
		{
			directory = new File(getMain().getProject(), "module/" + moduleName);
		}

		directory.mkdirs();

		iri = IRI.create(Owlcl.ISF_ONTOLOGY_IRI_PREFIX + moduleName + Owlcl.MODULE_IRI_SUFFIX);
		iriInferred = IRI.create(Owlcl.ISF_ONTOLOGY_IRI_PREFIX + moduleName
				+ Owlcl.MODULE_IRI_INRERRED_SUFFIX);
		fileName = moduleName + Owlcl.MODULE_IRI_SUFFIX;
		fileInferredName = moduleName + Owlcl.MODULE_IRI_INRERRED_SUFFIX;

		man = OWLManager.createOWLOntologyManager();
		man.clearIRIMappers();
		man.addIRIMapper(new AutoIRIMapper(directory, false));
		man.setSilentMissingImportsHandling(true);

		df = man.getOWLDataFactory();

		topIri = IRI.create(iriPrefix + moduleName + Owlcl.TOP_IRI_SUFFIX);
		configurationIri = IRI.create(iriPrefix + moduleName + Owlcl.CONFIGURATION_IRI_SUFFIX);
		includeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
		excludeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
		legacyIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
		legacyRemovedIri = IRI.create(iriPrefix + moduleName
				+ Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
	}

	// ================================================================================
	// Implementation
	// ================================================================================

	public NewModuleCommand(MainCommand main) {
		super(main);
		configure();
	}

	OWLOntologyManager man;
	OWLDataFactory df;
	IRI topIri;
	IRI configurationIri;
	IRI includeIri;
	IRI excludeIri;
	IRI legacyIri;
	IRI legacyRemovedIri;

	public void run() {
		init();

		// include
		OWLOntology includeOntology = OwlclUtil.getOrLoadOrCreateOntology(includeIri, man);

		// exclude
		OWLOntology excludeOntology = OwlclUtil.getOrLoadOrCreateOntology(excludeIri, man);

		// legacy
		OWLOntology legacyOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyIri, man);

		// legacy removed
		OWLOntology legacyRemovedOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyRemovedIri,
				man);

		// configuration
		OWLOntology configurationOntology = null;
		try
		{
			configurationOntology = OwlclUtil.getOrLoadOntology(configurationIri, man);
		} catch (RuntimeOntologyLoadingException e1)
		{
			if (!e1.isIriMapping())
			{
				throw e1;
			}
		}

		// if null we might need to migrate the -annotation file
		OWLOntology annotationOntology = null;
		if (configurationOntology == null)
		{
			annotationOntology = OwlclUtil.getOrLoadOntology(
					IRI.create(iriPrefix + moduleName + "-module-annotation.owl"), man);
		}

		// create a new one if needed.
		if (configurationOntology == null)
		{
			configurationOntology = OwlclUtil.getOrLoadOrCreateOntology(configurationIri, man);
			if (annotationOntology != null)
			{
				// until I see how to change xmlns and xml:base, otherwise those
				// will not be updated.
				// TODO: fix
				man.addAxioms(configurationOntology, annotationOntology.getAxioms());
				for (OWLAnnotation a : annotationOntology.getAnnotations())
				{
					man.applyChange(new AddOntologyAnnotation(configurationOntology, a));
				}

				for (OWLImportsDeclaration id : annotationOntology.getImportsDeclarations())
				{
					man.applyChange(new AddImport(configurationOntology, id));
				}
			}
		}

		// remove the old imports
		man.applyChange(new RemoveImport(configurationOntology, df
				.getOWLImportsDeclaration(includeIri)));
		man.applyChange(new RemoveImport(configurationOntology, df
				.getOWLImportsDeclaration(excludeIri)));
		man.applyChange(new RemoveImport(configurationOntology, df
				.getOWLImportsDeclaration(legacyIri)));
		man.applyChange(new RemoveImport(configurationOntology, df
				.getOWLImportsDeclaration(legacyRemovedIri)));
		for (IRI source : sourceIris)
		{
			man.applyChange(new AddImport(configurationOntology, df
					.getOWLImportsDeclaration(source)));
		}
		// ifs-tools.owl import
		man.applyChange(new AddImport(configurationOntology, df
				.getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));

		// check/add the module IRI annotation
		Set<String> axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
				ModuleVocab.module_iri.getAP(), configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module IRI annotations for module: " + getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_iri.getAP(),
					df.getOWLLiteral(iriPrefix + getModuleName() + Owlcl.MODULE_IRI_SUFFIX))));
		}

		// check/add the module inferred IRI annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
				ModuleVocab.module_iri_inferred.getAP(), configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module inferreed IRI annotations for module: "
					+ getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology,
					df.getOWLAnnotation(
							ModuleVocab.module_iri_inferred.getAP(),
							df.getOWLLiteral(iriPrefix + getModuleName()
									+ Owlcl.MODULE_IRI_INRERRED_SUFFIX))));
		}

		// check/add the module file name annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_file_name.getAP(),
				configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module file name annotations for module: "
					+ getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_file_name.getAP(), df.getOWLLiteral(fileName))));
		}

		// check/add the module inferred file name annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
				ModuleVocab.module_file_name_inferred.getAP(), configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module inferred file name annotations for module: "
					+ getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_file_name_inferred.getAP(),
					df.getOWLLiteral(fileInferredName))));
		}

		// check/add the module generate annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_generate.getAP(),
				configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module generate annotations for module: " + getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_generate.getAP(), df.getOWLLiteral(""))));
		}

		// check/add the module generate inferred annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
				ModuleVocab.module_generate_inferred.getAP(), configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module generate annotations for module: " + getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_generate_inferred.getAP(), df.getOWLLiteral(""))));
		}

		// check/add the builders annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_builders.getAP(),
				configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module builders annotations for module: " + getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_builders.getAP(), df.getOWLLiteral(""))));
		}

		// check/add the inferred builders annotation
		axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
				ModuleVocab.module_inferred_builders.getAP(), configurationOntology, false);
		if (axioms.size() > 1)
		{
			logger.warn("Found multiple module inferred builders annotations for module: "
					+ getModuleName());
		} else if (axioms.size() == 0)
		{
			man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
					ModuleVocab.module_inferred_builders.getAP(), df.getOWLLiteral(""))));
		}

		// top
		OWLOntology topOntology = OwlclUtil.getOrLoadOrCreateOntology(topIri, man);
		man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(configurationIri)));
		man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(includeIri)));
		man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(excludeIri)));
		man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(legacyIri)));
		man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(legacyRemovedIri)));

		try
		{
			man.saveOntology(includeOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.MODULE_INCLUDE_IRI_SUFFIX)));
			man.saveOntology(excludeOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX)));
			man.saveOntology(legacyOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.MODULE_LEGACY_IRI_SUFFIX)));
			man.saveOntology(legacyRemovedOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX)));
			man.saveOntology(configurationOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.CONFIGURATION_IRI_SUFFIX)));
			man.saveOntology(topOntology, new FileOutputStream(new File(getDirectory(),
					getModuleName() + Owlcl.MODULE_TOP_IRI_SUFFIX)));
		} catch (OWLOntologyStorageException | FileNotFoundException e)
		{
			throw new RuntimeException(
					"Failed while saving files for new module" + getModuleName(), e);
		}
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {

	}

  @Override
  public Object call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
