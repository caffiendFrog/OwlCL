package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Owlcl;
import com.essaid.owlcl.command.module.config.IModuleConfig;
import com.essaid.owlcl.command.module.config.IModuleConfigInternal;
import com.essaid.owlcl.command.module.config.ModuleConfigurationV1;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "newModule", commandDescription = "The command to create a new module.")
public class NewModuleCommand extends AbstractCommand {

  // ================================================================================
  // The module name
  // ================================================================================
  @Parameter(
      names = "-name",
      description = "The module name. This will be used to create default IRIs, files, or folders.")
  public void setName(String moduleName) {
    this.moduleName = moduleName;
    this.moduleNameSet = true;
  }

  public String getName() {
    return moduleName;
  }

  public boolean isNameSet() {
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

  @Parameter(names = "-sources", converter = IriConverter.class, variableArity = true,
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

  @Parameter(names = "-sourceExcludes", converter = IriConverter.class, variableArity = true,
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

  @Parameter(names = "-unclassifiedIri", description = "The IRI for the unclassified module.",
      converter = IriConverter.class)
  public void setUnclassifiedIri(IRI iri) {
    this.unclassifiedIri = iri;
    this.unclassifiedIriSet = true;
  }

  public IRI getUnclassifiedIri() {
    return unclassifiedIri;
  }

  public boolean isUnclassifiedIriSet() {
    return unclassifiedIriSet;
  }

  private IRI unclassifiedIri;
  private boolean unclassifiedIriSet;

  // ================================================================================
  // The final IRI of the module inferred
  // ================================================================================

  @Parameter(names = "-classifiedIri", description = "The IRI for the classified module.",
      converter = IriConverter.class)
  public void setClassifiedIri(IRI iri) {
    this.classifiedIri = iri;
    this.classifiedIriSet = true;
  }

  public IRI getClassifiedIri() {
    return classifiedIri;
  }

  public boolean isClassifiedIriSet() {
    return classifiedIriSet;
  }

  private IRI classifiedIri;
  private boolean classifiedIriSet;

  // ================================================================================
  // The final filename of the module
  // ================================================================================
  private String unclassifiedFileName;
  private boolean unclassifiedFileNameSet;

  @Parameter(names = "-unclassifiedFileName",
      description = "The file name for the classified module.")
  public void setUnclassifiedFileName(String fileName) {
    this.unclassifiedFileName = fileName;
    this.unclassifiedFileNameSet = true;
  }

  public String getUnclassifiedFileName() {
    return unclassifiedFileName;
  }

  public boolean isUnclassifiedFileNameSet() {
    return unclassifiedFileNameSet;
  }

  // ================================================================================
  // The final filename of the module inferred
  // ================================================================================

  @Parameter(names = "-classifiedFileName",
      description = "The file name for the classified module.")
  public void setClassifiedFileName(String fileName) {
    this.classifiedFileName = fileName;
    this.classifiedFileNameSet = true;
  }

  public String getClassifiedFileName() {
    return classifiedFileName;
  }

  public boolean isClassifiedFileNameSet() {
    return classifiedFileNameSet;
  }

  private String classifiedFileName;
  private boolean classifiedFileNameSet;

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
    if (!isNameSet())
    {
      moduleName = "_unnamed";
    }

    if (!isIriPrefixSet())
    {
      iriPrefix = "http://owl.essaid.com/owlcl/default/";
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

    if (!isUnclassifiedIriSet())
    {
      unclassifiedIri = IRI.create(iriPrefix + "module/" + moduleName
          + Owlcl.MODULE_UNCLASSIFIED_SUFFIX);
    }

    if (!isClassifiedIriSet())
    {
      classifiedIri = IRI.create(iriPrefix + "module/" + moduleName
          + Owlcl.MODULE_CLASSIFIED_SUFFIX);
    }

    if (!isUnclassifiedFileNameSet())
    {
      unclassifiedFileName = moduleName + Owlcl.MODULE_UNCLASSIFIED_SUFFIX;
    }

    if (!isClassifiedFileNameSet())
    {
      classifiedFileName = moduleName + Owlcl.MODULE_CLASSIFIED_SUFFIX;
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  Injector injector;

  @Inject
  public NewModuleCommand(@Assisted OwlclCommand main) {
    super(main);
  }

  protected void doInitialize() {
    getLogger().debug("Doing initialization.");
    configure();
  };

//  OWLOntologyManager man;
//  OWLDataFactory df;
//  IRI topIri;
//  IRI configurationIri;
//  IRI includeIri;
//  IRI excludeIri;
//  IRI legacyIri;
//  IRI legacyRemovedIri;

  private void preconditions() {

    // if (directory.exists())
    // {
    // throw new
    // IllegalStateException("New module's directory already exists. Aborting so that I "
    // + "don't overwrite an existing module");
    // }

  }

  @Override
  public Object call() throws Exception {
    configure();
    preconditions();
    run();
    return null;
  }

  public void run() {

    OWLOntologyManager man = OWLManager.createOWLOntologyManager();

    IModuleConfigInternal ci = ModuleConfigurationV1.getNewInstance(moduleName, iriPrefix,
        directory.toPath(), man, man, null, null);
    injector.injectMembers(ci);

    ci.loadAndUpdate();

    ci.setUnclassifiedIri(unclassifiedIri);
    ci.setUnclassifiedFilename(unclassifiedFileName);

    ci.setClassifiedIri(classifiedIri);
    ci.setClassifiedFilename(classifiedFileName);

    ci.setSourceIris(new HashSet<IRI>(sourceIris));
    ci.setSourceExcludedIris(new HashSet<IRI>(sourceExcludeIris));

    ci.saveConfiguration();

    System.exit(0);
//
//    directory.mkdirs();
//
//    man = OWLManager.createOWLOntologyManager();
//    man.clearIRIMappers();
//    man.addIRIMapper(new AutoIRIMapper(directory, false));
//    man.setSilentMissingImportsHandling(true);
//
//    df = man.getOWLDataFactory();
//
//    topIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_TOP_IRI_SUFFIX);
//    configurationIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX);
//    includeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
//    excludeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
//    legacyIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
//    legacyRemovedIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);
//
//    // include ontology
//    OWLOntology includeOntology = OwlclUtil.getOrLoadOrCreateOntology(includeIri, man);
//
//    // exclude ontology
//    OWLOntology excludeOntology = OwlclUtil.getOrLoadOrCreateOntology(excludeIri, man);
//
//    // legacy ontology
//    OWLOntology legacyOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyIri, man);
//
//    // legacy removed ontology
//    OWLOntology legacyRemovedOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyRemovedIri, man);
//
//    // configuration ontology
//    OWLOntology configurationOntology = OwlclUtil.getOrLoadOrCreateOntology(configurationIri, man);
//
//    // add source imports
//    for (IRI source : sourceIris)
//    {
//      man.applyChange(new AddImport(configurationOntology, df.getOWLImportsDeclaration(source)));
//    }
//
//    // add source excludes
//    for (IRI source : sourceExcludeIris)
//    {
//      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//          ModuleVocab.module_source_exclude.getAP(), df.getOWLLiteral(source.toString()))));
//    }
//
//    // ifs-tools.owl import
//    man.applyChange(new AddImport(configurationOntology, df
//        .getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));
//
//    // module IRI
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_unclassified_iri.getAP(),
//        df.getOWLLiteral(iriPrefix + getName() + Owlcl.MODULE_UNCLASSIFIED_SUFFIX))));
//
//    // module inferred IRI
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_classified_iri.getAP(),
//        df.getOWLLiteral(iriPrefix + getName() + Owlcl.MODULE_CLASSIFIED_SUFFIX))));
//
//    // module file name
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_unclassified_filename.getAP(), df.getOWLLiteral(unclassifiedFileName))));
//
//    // module inferred name
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_classified_filename.getAP(), df.getOWLLiteral(classifiedFileName))));
//
//    // module generate true/false
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_is_unclassified.getAP(), df.getOWLLiteral("true"))));
//
//    // module generate inferred true/false
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_is_classified.getAP(), df.getOWLLiteral("true"))));
//
//    // builders
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_unclassified_builders.getAP(), df.getOWLLiteral("no-builder"))));
//    // }
//
//    // builders inferred
//    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
//        ModuleVocab.module_classified_builders.getAP(), df.getOWLLiteral("no-builders"))));
//    // }
//
//    // // add legacy
//    // man.applyChange(new AddOntologyAnnotation(configurationOntology,
//    // df.getOWLAnnotation(
//    // ModuleVocab.module_add_legacy.getAP(), df.getOWLLiteral("false"))));
//    // // }
//    //
//    // // clean legacy
//    // man.applyChange(new AddOntologyAnnotation(configurationOntology,
//    // df.getOWLAnnotation(
//    // ModuleVocab.module_clean_legacy.getAP(), df.getOWLLiteral("false"))));
//    // // }
//
//    // top
//    OWLOntology topOntology = OwlclUtil.getOrLoadOrCreateOntology(topIri, man);
//    man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(configurationIri)));
//    man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(includeIri)));
//    man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(excludeIri)));
//    man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(legacyIri)));
//    man.applyChange(new AddImport(topOntology, df.getOWLImportsDeclaration(legacyRemovedIri)));
//
//    try
//    {
//      man.saveOntology(includeOntology, new FileOutputStream(new File(getDirectory(), getName()
//          + Owlcl.MODULE_INCLUDE_IRI_SUFFIX)));
//      man.saveOntology(excludeOntology, new FileOutputStream(new File(getDirectory(), getName()
//          + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX)));
//      man.saveOntology(legacyOntology, new FileOutputStream(new File(getDirectory(), getName()
//          + Owlcl.MODULE_LEGACY_IRI_SUFFIX)));
//      man.saveOntology(legacyRemovedOntology, new FileOutputStream(new File(getDirectory(),
//          getName() + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX)));
//      man.saveOntology(configurationOntology, new FileOutputStream(new File(getDirectory(),
//          getName() + Owlcl.MODULE_CONFIGURATION_IRI_SUFFIX)));
//      man.saveOntology(topOntology, new FileOutputStream(new File(getDirectory(), getName()
//          + Owlcl.MODULE_TOP_IRI_SUFFIX)));
//
//      File versionFile = new File(directory, "V-" + IModuleConfig.CURRENT_VERSION);
//      versionFile.createNewFile();
//
//    } catch (OWLOntologyStorageException | IOException e)
//    {
//      throw new RuntimeException("Failed while saving files for new module" + getName(), e);
//    }
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

}
