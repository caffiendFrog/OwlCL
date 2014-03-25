package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.CommandResult;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.cli.CanonicalFileConverter;
import com.essaid.owlcl.command.cli.DirectoryExistsValueValidator;
import com.essaid.owlcl.core.annotation.MainCommandQualifier;
import com.essaid.owlcl.module.ModuleVocab;
import com.essaid.owlcl.util.Owlcl;
import com.essaid.owlcl.util.OwlclUtil;
import com.essaid.owlcl.util.RuntimeOntologyLoadingException;
import com.google.inject.Inject;

@Parameters(commandNames = "updateModule",
    commandDescription = "Updates one or more module directories.")
public class UpdateModuleCommand extends AbstractCommand<CommandResult> {

  // ================================================================================
  // name
  // ================================================================================
  @Parameter(names = "-name", description = "The module name for the one module update. If "
      + "not supplied, or if module root update, the directory name will be used as "
      + "module name.")
  public String moduleName = null;

  // ================================================================================
  // directory
  // ================================================================================
  public File directory;
  public boolean directorySet;

  public File getDirectory() {
    return directory;
  }

  @Parameter(names = "-directory",
      description = "The directory to update. If it is not a module root,"
          + " only that directory will be updated. If root, will search for "
          + " *module* files in subdirectories and assume to be a module if present.",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setDirectory(File directory) {
    this.directory = directory;
    this.directorySet = true;
  }

  // ================================================================================
  // root
  // ================================================================================

  public boolean root = false;

  public boolean isRoot() {
    return root;
  }

  @Parameter(names = "-root")
  public void setRoot(boolean root) {
    this.root = root;
  }

  // ================================================================================
  // Initialization
  // ================================================================================
  protected void configure() {

    this.moduleName = "_unnamed";

    if (main.getProject() != null)
    {
      this.directory = new File(main.getProject(), "module");
      this.root = true;
    } else
    {
      this.directory = new File(main.getJobDirectory() + "module/" + this.moduleName);
    }
  }

  protected void init() {
    // if main had a command line parameter that sets custom project (as
    // opposed to getting it from properties) and this command didn't have a
    // custom directory, we need to update the default with the new
    // main.project.
    if (main.isProjectSet() && this.directorySet == false)
    {
      this.directory = new File(main.getProject(), "module");
    }

  }

  // ================================================================================
  //
  // ================================================================================

  @Inject
  @MainCommandQualifier
  private MainCommand main;

  Logger logger = LoggerFactory.getLogger(this.getClass());

  public UpdateModuleCommand(MainCommand main) {
    super(main);
    configure();
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // TODO Auto-generated method stub

  }

  public void run() {
    init();
    if (!root)
    {
      if (containsModuleFiles(directory))
      {
        if (moduleName == null)
        {
          moduleName = directory.getName();
        }
        updateModule(moduleName, directory);
      }
    } else
    {

      for (File file : getModuleDirectories(directory))
      {
        updateModule(file.getName(), file);
      }
    }

  }

  private void updateModule(String moduleName, File moduleDirectory) {
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    man.clearIRIMappers();
    man.setSilentMissingImportsHandling(true);
    man.addIRIMapper(new AutoIRIMapper(moduleDirectory, false));

    OWLDataFactory df = man.getOWLDataFactory();

    OWLOntology configurationOntology = null;
    OWLOntology annotationOntology = null;
    boolean configurationConfiguration = false;
    boolean annotationConfiguration = false;

    // try all the possible configurations from recent to old
    if (new File(moduleDirectory, moduleName + "-module-configuration.owl").exists())
    {
      try
      {
        configurationOntology = OwlclUtil.loadOntology(new File(moduleDirectory, moduleName
            + "-module-configuration.owl"), man);
        configurationConfiguration = true;
      } catch (RuntimeOntologyLoadingException e)
      {
        logger.warn("Faild to load " + moduleName
            + "-module-configuration.owl while looking for possible configurations.");
        if (!e.isIriMapping())
        {
          throw e;
        }
      }

    } else if (new File(moduleDirectory, moduleName + "-module-annotation.owl").exists())
    {
      try
      {
        annotationOntology = OwlclUtil.loadOntology(new File(moduleDirectory, moduleName
            + "-module-annotation.owl"), man);
        annotationConfiguration = true;
      } catch (RuntimeOntologyLoadingException e)
      {
        logger.warn("Faild to load " + moduleName
            + "-module-annotation.owl while looking for possible configurations.");
        if (!e.isIriMapping())
        {
          throw e;
        }
      }
    }

    if (configurationOntology == null && annotationOntology == null)
    {
      logger.warn("Updating module " + moduleName
          + " failed. Couldn't file a configuration file to start from.");
      return;
    }

    // get current prefix since we don't know what it is
    String iriPrefix = null;
    if (annotationConfiguration)
    {
      int index = annotationOntology.getOntologyID().getOntologyIRI().toString().lastIndexOf('/');
      iriPrefix = annotationOntology.getOntologyID().getOntologyIRI().toString()
          .substring(0, index + 1);
    } else if (configurationConfiguration)
    {
      int index = configurationOntology.getOntologyID().getOntologyIRI().toString()
          .lastIndexOf('/');
      iriPrefix = configurationOntology.getOntologyID().getOntologyIRI().toString()
          .substring(0, index);
    }

    // build iris
    IRI topIri = IRI.create(iriPrefix + moduleName + Owlcl.TOP_IRI_SUFFIX);
    IRI configurationIri = IRI.create(iriPrefix + moduleName + Owlcl.CONFIGURATION_IRI_SUFFIX);
    IRI includeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
    IRI excludeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
    IRI legacyIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
    IRI legacyRemovedIri = IRI.create(iriPrefix + moduleName
        + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

    // we now have IRIs and will continue updating

    if (annotationConfiguration)
    {
      configurationOntology = OwlclUtil.createOntology(configurationIri, man);
      // until I see how to change xmlns and xml:base, otherwise
      // those
      // will not be updated.
      // TODO: fix
      for (OWLAnnotation a : annotationOntology.getAnnotations())
      {
        man.applyChange(new AddOntologyAnnotation(configurationOntology, a));
      }

      for (OWLImportsDeclaration id : annotationOntology.getImportsDeclarations())
      {
        man.applyChange(new AddImport(configurationOntology, id));
      }
      man.addAxioms(configurationOntology, annotationOntology.getAxioms());

    }

    // include
    OWLOntology includeOntology = OwlclUtil.getOrLoadOrCreateOntology(includeIri, man);

    // exclude
    OWLOntology excludeOntology = OwlclUtil.getOrLoadOrCreateOntology(excludeIri, man);

    // legacy
    OWLOntology legacyOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyIri, man);

    // legacy removed
    OWLOntology legacyRemovedOntology = OwlclUtil.getOrLoadOrCreateOntology(legacyRemovedIri, man);

    // remove the old imports. these would have been copied (above) with all
    // imports.
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(includeIri)));
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(excludeIri)));
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(legacyIri)));
    man.applyChange(new RemoveImport(configurationOntology, df
        .getOWLImportsDeclaration(legacyRemovedIri)));

    // ifs-tools.owl import
    man.applyChange(new AddImport(configurationOntology, df
        .getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));

    // check/add the module IRI annotation
    Set<String> axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_iri.getAP(), configurationOntology, false);
    if (axioms.size() > 1)
    {
      logger.warn("Found multiple module IRI annotations for module: " + moduleName);
    } else if (axioms.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_iri.getAP(),
          df.getOWLLiteral(iriPrefix + moduleName + Owlcl.MODULE_IRI_SUFFIX))));
    }

    // check/add the module inferred IRI annotation
    axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_iri_inferred.getAP(),
        configurationOntology, false);
    if (axioms.size() > 1)
    {
      logger.warn("Found multiple module inferreed IRI annotations for module: " + moduleName);
    } else if (axioms.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_iri_inferred.getAP(),
          df.getOWLLiteral(iriPrefix + moduleName + Owlcl.MODULE_IRI_INRERRED_SUFFIX))));
    }

    // check/add the module file name annotation
    axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_file_name.getAP(),
        configurationOntology, false);
    if (axioms.size() > 1)
    {
      logger.warn("Found multiple module file name annotations for module: " + moduleName);
    } else if (axioms.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_file_name.getAP(), df.getOWLLiteral(moduleName + "-module.owl"))));
    }

    // check/add the module inferred file name annotation
    axioms = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_file_name_inferred.getAP(), configurationOntology, false);
    if (axioms.size() > 1)
    {
      logger.warn("Found multiple module inferred file name annotations for module: " + moduleName);
    } else if (axioms.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_file_name_inferred.getAP(),
          df.getOWLLiteral(moduleName + "-module-inferred.owl"))));
    }

    // check/add the module generate annotation
    axioms = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_generate.getAP(),
        configurationOntology, false);
    if (axioms.size() > 1)
    {
      logger.warn("Found multiple module generate annotations for module: " + moduleName);
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
      logger.warn("Found multiple module generate annotations for module: " + moduleName);
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
      logger.warn("Found multiple module builders annotations for module: " + moduleName);
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
      logger.warn("Found multiple module inferred builders annotations for module: " + moduleName);
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
      man.saveOntology(includeOntology, new FileOutputStream(new File(getDirectory(), moduleName
          + Owlcl.MODULE_INCLUDE_IRI_SUFFIX)));
      man.saveOntology(excludeOntology, new FileOutputStream(new File(getDirectory(), moduleName
          + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX)));
      man.saveOntology(legacyOntology, new FileOutputStream(new File(getDirectory(), moduleName
          + Owlcl.MODULE_LEGACY_IRI_SUFFIX)));
      man.saveOntology(legacyRemovedOntology, new FileOutputStream(new File(getDirectory(),
          moduleName + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX)));
      man.saveOntology(configurationOntology, new FileOutputStream(new File(getDirectory(),
          moduleName + Owlcl.CONFIGURATION_IRI_SUFFIX)));
      man.saveOntology(topOntology, new FileOutputStream(new File(getDirectory(), moduleName
          + Owlcl.MODULE_TOP_IRI_SUFFIX)));
    } catch (OWLOntologyStorageException | FileNotFoundException e)
    {
      throw new RuntimeException("Failed while saving files for new module" + moduleName, e);
    }

  }

  private Set<File> getModuleDirectories(File rootDirectory) {
    Set<File> directories = new HashSet<File>();
    for (File file : rootDirectory.listFiles())
    {
      if (file.isDirectory())
      {
        if (containsModuleFiles(file))
        {
          directories.add(file);
        }
      }
    }
    return directories;
  }

  private boolean containsModuleFiles(File directory) {
    return FileUtils.listFiles(directory, new RegexFileFilter(".*module.*"), null).size() > 0;
  }

  @Override
  public CommandResult call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
