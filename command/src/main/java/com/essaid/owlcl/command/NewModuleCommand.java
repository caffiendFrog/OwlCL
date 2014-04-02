package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Owlcl;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "newModule", commandDescription = "The command to create a new module.")
public class NewModuleCommand extends AbstractCommand {

  // ================================================================================
  // The module name
  // ================================================================================
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

  public boolean isModuleNameSet() {
    return moduleNameSet;
  }

  private String moduleName;
  private boolean moduleNameSet;

  // ================================================================================
  // The output directory
  // ================================================================================

  @Parameter(names = "-directory", description = "The directory where the module will be created.",
      converter = FileConverter.class)
  public void setDirectory(File directory) {
    this.directory = directory;
    this.directorySet = true;
  }

  public File getDirectory() {
    return directory;
  }

  public boolean isDirectorySet() {
    return directorySet;
  }

  private File directory;
  private boolean directorySet;

  // ================================================================================
  // The source IRIs for the module
  // ================================================================================

  @Parameter(names = "-sources", converter = IriConverter.class,
      description = "The source IRIs that will be used for this module.")
  public void setSourceIris(List<IRI> sourceIris) {
    this.sourceIris = sourceIris;
    this.sourceIrisSet = true;
  }

  public List<IRI> getSourceIris() {
    return sourceIris;
  }

  public boolean isSourceIrisSet() {
    return sourceIrisSet;
  }

  private List<IRI> sourceIris;
  private boolean sourceIrisSet;

  // ================================================================================
  // The source exclude IRIs for the module
  // ================================================================================

  @Parameter(names = "-sourceExcludes", converter = IriConverter.class,
      description = "The excluded source IRIs that will be used for this module.")
  public void setSourceExcludeIris(List<IRI> sourceExcludesIris) {
    this.sourceExcludeIris = sourceExcludesIris;
    this.sourceExcludeIrisSet = true;
  }

  public List<IRI> getSourceExcludeIris() {
    return sourceExcludeIris;
  }

  public boolean isSourceExcludeIrisSet() {
    return sourceExcludeIrisSet;
  }

  private List<IRI> sourceExcludeIris;
  private boolean sourceExcludeIrisSet;

  // ================================================================================
  // The final IRI of the module
  // ================================================================================

  @Parameter(names = "-iri", description = "The generated module's IRI",
      converter = IriConverter.class)
  public void setIri(IRI iri) {
    this.iri = iri;
    this.iriSet = true;
  }

  public IRI getIri() {
    return iri;
  }

  public boolean isIriSet() {
    return iriSet;
  }

  private IRI iri;
  private boolean iriSet;

  // ================================================================================
  // The final IRI of the module inferred
  // ================================================================================

  @Parameter(names = "-iriInferred", description = "The generated inferred module's IRI",
      converter = IriConverter.class)
  public void setIriInferred(IRI iri) {
    this.iriInferred = iri;
    this.iriInferredSet = true;
  }

  public IRI getIriInferred() {
    return iriInferred;
  }

  public boolean isIriInferredSet() {
    return iriInferredSet;
  }

  private IRI iriInferred;
  private boolean iriInferredSet;

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

  public boolean isFileNameSet() {
    return fileNameSet;
  }

  // ================================================================================
  // The final filename of the module inferred
  // ================================================================================

  @Parameter(names = "-fileNameInferred",
      description = "The generated inferred module's file name.")
  public void setFileInferredName(String fileName) {
    this.fileInferredName = fileName;
    this.fileInferredNameSet = true;
  }

  public String getFileInferredName() {
    return fileInferredName;
  }

  public boolean isFileInferredNameSet() {
    return fileInferredNameSet;
  }

  private String fileInferredName;
  private boolean fileInferredNameSet;

  // ================================================================================
  // The IRI prefix of the module's files
  // ================================================================================

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

  public boolean isIriPrefixSet() {
    return iriPrefixSet;
  }

  private String iriPrefix;
  private boolean iriPrefixSet;

  // ================================================================================
  // Initialization
  // ================================================================================

  protected void configure() {
    if (!isModuleNameSet())
    {
      moduleName = "_unnamed";
    }

    if (!isIriPrefixSet())
    {
      iriPrefix = "http://owlcl/";
    }

    if (!isDirectorySet())
    {
      directory = new File(getMain().getJobDirectory(), "module/" + moduleName);
    }

    if (!isSourceIrisSet())
    {
      sourceIris = new ArrayList<IRI>();
      sourceIris.add(Owlcl.ISF_DEV_IRI);
    }

    if (!isSourceExcludeIrisSet())
    {
      sourceExcludeIris = new ArrayList<IRI>();
      sourceExcludeIris.add(Owlcl.ISF_TOOLS_IRI);
    }

    if (!isIriSet())
    {
      iri = IRI.create(iriPrefix + "module/" + moduleName + Owlcl.MODULE_IRI_SUFFIX);
    }

    if (!isIriInferredSet())
    {
      iriInferred = IRI.create(iriPrefix + "module/" + moduleName
          + Owlcl.MODULE_IRI_INRERRED_SUFFIX);
    }

    if (!isFileNameSet())
    {
      fileName = moduleName + Owlcl.MODULE_IRI_SUFFIX;
    }

    if (!isFileInferredNameSet())
    {
      fileInferredName = moduleName + Owlcl.MODULE_IRI_INRERRED_SUFFIX;
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  public NewModuleCommand(@Assisted OwlclCommand main) {
    super(main);
  }

  protected void doInitialize() {
    getLogger().debug("Doing initialization.");
    configure();
  };

  OWLOntologyManager man;
  OWLDataFactory df;
  IRI topIri;
  IRI configurationIri;
  IRI includeIri;
  IRI excludeIri;
  IRI legacyIri;
  IRI legacyRemovedIri;

  private void preconditions() {

    if (directory.exists())
    {
      throw new IllegalStateException("New module's directory already exists. Aborting so that I "
          + "don't overwrite an existing module");
    }

  }

  @Override
  public Object call() throws Exception {
    configure();
    preconditions();
    run();
    return null;
  }

  public void run() {

    directory.mkdirs();

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
    legacyRemovedIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

    // include ontology
    OWLOntology includeOntology = OwlclUtil.getOrLoadOrCreateOntology(includeIri, man);

    // exclude ontology
    OWLOntology excludeOntology = OwlclUtil.getOrLoadOrCreateOntology(excludeIri, man);

    // legacy ontology
    OWLOntology legacyOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyIri, man);

    // legacy removed ontology
    OWLOntology legacyRemovedOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyRemovedIri, man);

    // configuration ontology
    OWLOntology configurationOntology = OwlclUtil.getOrLoadOrCreateOntology(configurationIri, man);

    // add source imports
    for (IRI source : sourceIris)
    {
      man.applyChange(new AddImport(configurationOntology, df.getOWLImportsDeclaration(source)));
    }

    // add source excludes
    for (IRI source : sourceExcludeIris)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_source_exclude.getAP(), df.getOWLLiteral(source.toString()))));
    }

    // ifs-tools.owl import
    man.applyChange(new AddImport(configurationOntology, df
        .getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));

    // module IRI
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_iri.getAP(),
        df.getOWLLiteral(iriPrefix + getModuleName() + Owlcl.MODULE_IRI_SUFFIX))));

    // module inferred IRI
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_iri_inferred.getAP(),
        df.getOWLLiteral(iriPrefix + getModuleName() + Owlcl.MODULE_IRI_INRERRED_SUFFIX))));

    // module file name
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_file_name.getAP(), df.getOWLLiteral(fileName))));

    // module inferred name
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_file_name_inferred.getAP(), df.getOWLLiteral(fileInferredName))));

    // module generate true/false
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_generate.getAP(), df.getOWLLiteral("true"))));

    // module generate inferred true/false
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_generate_inferred.getAP(), df.getOWLLiteral("true"))));

    // builders
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_builders.getAP(), df.getOWLLiteral("no-builder"))));
    // }

    // builders inferred
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_inferred_builders.getAP(), df.getOWLLiteral("no-builders"))));
    // }

    // add legacy
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_add_legacy.getAP(), df.getOWLLiteral("false"))));
    // }

    // clean legacy
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_clean_legacy.getAP(), df.getOWLLiteral("false"))));
    // }

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
      man.saveOntology(topOntology, new FileOutputStream(new File(getDirectory(), getModuleName()
          + Owlcl.MODULE_TOP_IRI_SUFFIX)));

      File versionFile = new File(directory, "V-" + IModule.VERSION);
      versionFile.createNewFile();

    } catch (OWLOntologyStorageException | IOException e)
    {
      throw new RuntimeException("Failed while saving files for new module" + getModuleName(), e);
    }
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

}
