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
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.IriConverter;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.RuntimeOntologyLoadingException;
import com.essaid.owlcl.module.ModuleVocab;
import com.essaid.owlcl.module.Owlcl;
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

  @Parameter(names = "-sourceIris", converter = IriConverter.class,
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

  public void run() {
    configure();

    if (directory.exists())
    {
      throw new IllegalStateException("New module's directory already exists. Aborting so that I "
          + "don't overwrite an existing module");
    }

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

    // include
    OWLOntology includeOntology = OwlclUtil.getOrLoadOrCreateOntology(includeIri, man);

    // exclude
    OWLOntology excludeOntology = OwlclUtil.getOrLoadOrCreateOntology(excludeIri, man);

    // legacy
    OWLOntology legacyOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyIri, man);

    // legacy removed
    OWLOntology legacyRemovedOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyRemovedIri, man);

    // configuration
    OWLOntology configurationOntology = null;

    configurationOntology = OwlclUtil.getOrLoadOrCreateOntology(configurationIri, man);

    // // remove the old imports
    // man.applyChange(new RemoveImport(configurationOntology,
    // df.getOWLImportsDeclaration(includeIri)));
    // man.applyChange(new RemoveImport(configurationOntology,
    // df.getOWLImportsDeclaration(excludeIri)));
    // man.applyChange(new RemoveImport(configurationOntology,
    // df.getOWLImportsDeclaration(legacyIri)));
    // man.applyChange(new RemoveImport(configurationOntology, df
    // .getOWLImportsDeclaration(legacyRemovedIri)));

    // add source imports
    for (IRI source : sourceIris)
    {
      man.applyChange(new AddImport(configurationOntology, df.getOWLImportsDeclaration(source)));
    }

    // ifs-tools.owl import
    man.applyChange(new AddImport(configurationOntology, df
        .getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));

    // // check/add the module IRI annotation
    // Set<String> axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
    // ModuleVocab.module_iri.getAP(), configurationOntology, false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn("Found multiple module IRI annotations for module: " +
    // getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_iri.getAP(),
        df.getOWLLiteral(iriPrefix + getModuleName() + Owlcl.MODULE_IRI_SUFFIX))));
    // }

    // // check/add the module inferred IRI annotation
    // axioms =
    // OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_iri_inferred.getAP(),
    // configurationOntology, false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn(
    // "Found multiple module inferreed IRI annotations for module: " +
    // getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_iri_inferred.getAP(),
        df.getOWLLiteral(iriPrefix + getModuleName() + Owlcl.MODULE_IRI_INRERRED_SUFFIX))));
    // }

    // check/add the module file name annotation
    // axioms =
    // OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_file_name.getAP(),
    // configurationOntology, false);
    // if (axioms.size() > 1)
    // {
    // getLogger()
    // .warn("Found multiple module file name annotations for module: " +
    // getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_file_name.getAP(), df.getOWLLiteral(fileName))));
    // }

    // check/add the module inferred file name annotation
    // axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
    // ModuleVocab.module_file_name_inferred.getAP(), configurationOntology,
    // false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn(
    // "Found multiple module inferred file name annotations for module: " +
    // getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_file_name_inferred.getAP(), df.getOWLLiteral(fileInferredName))));
    // }

    // check/add the module generate annotation
    // axioms =
    // OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_generate.getAP(),
    // configurationOntology, false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn("Found multiple module generate annotations for module: "
    // + getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_generate.getAP(), df.getOWLLiteral("true"))));
    // }

    // // check/add the module generate inferred annotation
    // axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
    // ModuleVocab.module_generate_inferred.getAP(), configurationOntology,
    // false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn("Found multiple module generate annotations for module: "
    // + getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_generate_inferred.getAP(), df.getOWLLiteral("true"))));
    // }

    // // check/add the builders annotation
    // axioms =
    // OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_builders.getAP(),
    // configurationOntology, false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn("Found multiple module builders annotations for module: "
    // + getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_builders.getAP(), df.getOWLLiteral(""))));
    // }

    // // check/add the inferred builders annotation
    // axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
    // ModuleVocab.module_inferred_builders.getAP(), configurationOntology,
    // false);
    // if (axioms.size() > 1)
    // {
    // getLogger().warn(
    // "Found multiple module inferred builders annotations for module: " +
    // getModuleName());
    // } else if (axioms.size() == 0)
    // {
    man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
        ModuleVocab.module_inferred_builders.getAP(), df.getOWLLiteral(""))));
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
    } catch (OWLOntologyStorageException | FileNotFoundException e)
    {
      throw new RuntimeException("Failed while saving files for new module" + getModuleName(), e);
    }
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

  @Override
  public Object call() throws Exception {
    run();
    return null;
  }

}
