package com.essaid.owlcl.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.coode.owlapi.rdfxml.parser.IRIProvider;
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
import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.ModuleVocab;
import com.essaid.owlcl.command.module.Owlcl;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.essaid.owlcl.core.util.Report;
import com.essaid.owlcl.core.util.RuntimeOntologyLoadingException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "updateModule",
    commandDescription = "Updates one or more module directories.")
public class UpdateModuleCommand extends AbstractCommand {

  // ================================================================================
  // directory
  // ================================================================================
  @Parameter(names = "-directory",
      description = "The directory to update. If it is not a module root,"
          + " only that directory will be updated. If root, will search for "
          + " *module* files in subdirectories and assume to be a module if present.",
      converter = CanonicalFileConverter.class)
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
  // root
  // ================================================================================

  @Parameter(names = "-root")
  public void setRoot(boolean root) {
    this.root = root;
    this.rootSet = true;
  }

  public boolean isRoot() {
    return root;
  }

  public boolean isRootSet() {
    return rootSet;
  }

  private boolean root;
  private boolean rootSet;

  @Override
  protected void doInitialize() {
    configure();

  }

  // ================================================================================
  // Initialization
  // ================================================================================
  protected void configure() {

    if (!isDirectorySet())
    {
      directory = new File(getMain().getProject(), "module");
      root = true;
    }
  }

  // ================================================================================
  // implementation
  // ================================================================================

  @Inject
  public UpdateModuleCommand(@Assisted OwlclCommand main) {
    super(main);
  }

  @Inject
  private IReportFactory reportFactory;

  private Report report;

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // TODO Auto-generated method stub

  }

  private int moduleVersion = 0;

  @Override
  public Object call() throws Exception {
    getLogger().info("Starting module update");
    getLogger().info("\tdirectory: " + directory.getAbsolutePath());
    getLogger().info("\troot: " + root);
    report = reportFactory.createReport("ModuleUpdates.txt", getMain().getJobDirectory(), this);

    if (!root)
    {
      if (containsModuleFiles(directory))
      {
        updateModule(directory);
      } else
      {
        getLogger().error("Directory {} does not appear to be a module directory, skipping. ",
            getDirectory().getAbsolutePath());
      }
    } else
    {
      for (File file : getModuleDirectories(directory))
      {
        updateModule(file);
      }
    }
    report.finish();
    return null;
  }

  private boolean preconditions(File directory) {
    if (!directory.exists())
    {
      getLogger().error("Module directory {} does not exist while attempting update.",
          directory.getAbsolutePath());
      return false;
    }

    moduleVersion = Util.getModuleVersion(directory);
    if (moduleVersion == -1)
    {
      getLogger().error("Module does not have a version file while updating directory {} ",
          directory);
      return false;
    }
    if (moduleVersion == -2)
    {
      getLogger()
          .error("Module has multiple version files while updating directory {} ", directory);
      return false;
    }
    if (moduleVersion > IModule.VERSION)
    {
      getLogger().error("Module has higher version that this tool while updating directory {}",
          directory);
      return false;
    }

    return true;
  }

  private void updateModule(File moduleDirectory) {

    if (!preconditions(moduleDirectory))
    {
      getLogger().error(
          "Failed preconditions for updating module located at {} , skipping update.",
          moduleDirectory);
      return;
    }

    // extract any needed info from old versions, or return if version is
    // current
    switch (moduleVersion)
    {
    case 0:
      report.info("Updating module version 0 located at: " + moduleDirectory.getAbsolutePath());
      migrate0(moduleDirectory);
      break;
    case 1:
      report.info("Module version 1 up to date, ,located at: " + moduleDirectory.getAbsolutePath());
      // up to date, just return. No updates are done without bumping the
      // version.
      return;
    default:
      // should not get here
      report.error("Module with unknown version " + moduleVersion + " skipped,located at: "
          + moduleDirectory.getAbsolutePath());
      // getLogger().error("Unknown version {} for module located at {}, skipping.",
      // moduleVersion,
      // getDirectory());
      return;
    }

  }

  private void migrate0(File directory) {
    File file = FileUtils
        .listFiles(directory, new SuffixFileFilter("-module-annotation.owl"), null).iterator()
        .next();
    String name = file.getName();
    String moduleName = name.substring(0, name.lastIndexOf("-module-annotation.owl"));
    getLogger().info("Found module version 0 with name {}", moduleName);

    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    m.clearIRIMappers();
    m.setSilentMissingImportsHandling(true);
    m.addIRIMapper(new AutoIRIMapper(directory, false));

    OWLOntology o = OwlclUtil.loadOntology(file, m);
    String iri = o.getOntologyID().getOntologyIRI().toString();
    String iriPrefix = iri.substring(0, iri.lastIndexOf('/') + 1);

    // setup the current version module's ontologies
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();
    man.clearIRIMappers();
    man.setSilentMissingImportsHandling(true);
    man.addIRIMapper(new AutoIRIMapper(directory, false));

    IRI annotationIri = IRI.create(iriPrefix + moduleName + "-module-annotation.owl");
    IRI topIri = IRI.create(iriPrefix + moduleName + Owlcl.TOP_IRI_SUFFIX);
    IRI configurationIri = IRI.create(iriPrefix + moduleName + Owlcl.CONFIGURATION_IRI_SUFFIX);
    IRI includeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_INCLUDE_IRI_SUFFIX);
    IRI excludeIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_EXCLUDE_IRI_SUFFIX);
    IRI legacyIri = IRI.create(iriPrefix + moduleName + Owlcl.MODULE_LEGACY_IRI_SUFFIX);
    IRI legacyRemovedIri = IRI.create(iriPrefix + moduleName
        + Owlcl.MODULE_LEGACY_REMOVED_IRI_SUFFIX);

    OWLOntology annotationOntology = OwlclUtil.getOrLoadOntology(annotationIri, man);
    OWLOntology configurationOntology = OwlclUtil.createOntology(configurationIri, man);

    // move content of old annotation ontology
    for (OWLAnnotation a : annotationOntology.getAnnotations())
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, a));
      getLogger().debug("Copying ontology annotation from module-annotation.owl. {}", a);
    }

    for (OWLImportsDeclaration id : annotationOntology.getImportsDeclarations())
    {
      man.applyChange(new AddImport(configurationOntology, id));
      getLogger().debug("Copying ontology import from module-annotation.owl. {}", id);
    }
    man.addAxioms(configurationOntology, annotationOntology.getAxioms());

    // remove old imports
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(includeIri)));
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(excludeIri)));
    man.applyChange(new RemoveImport(configurationOntology, df.getOWLImportsDeclaration(legacyIri)));
    man.applyChange(new RemoveImport(configurationOntology, df
        .getOWLImportsDeclaration(legacyRemovedIri)));

    // ifs-tools.owl import
    man.applyChange(new AddImport(configurationOntology, df
        .getOWLImportsDeclaration(Owlcl.ISF_TOOLS_IRI)));

    // ifs-tools.owl source exclude
    man.applyChange(new AddOntologyAnnotation(configurationOntology,
        df.getOWLAnnotation(ModuleVocab.module_source_exclude.getAP(),
            df.getOWLLiteral(Owlcl.ISF_TOOLS_IRI.toString()))));

    // check/add the module IRI annotation
    Set<String> values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_iri.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module IRI annotations for module: {}", moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_iri.getAP(),
          df.getOWLLiteral(iriPrefix + moduleName + Owlcl.MODULE_IRI_SUFFIX))));
    }

    // check/add the module inferred IRI annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_iri_inferred.getAP(),
        configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module inferreed IRI annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_iri_inferred.getAP(),
          df.getOWLLiteral(iriPrefix + moduleName + Owlcl.MODULE_IRI_INRERRED_SUFFIX))));
    }

    // check/add the module file name annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_file_name.getAP(),
        configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module file name annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_file_name.getAP(), df.getOWLLiteral(moduleName + "-module.owl"))));
    }

    // check/add the module inferred file name annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_file_name_inferred.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn(
          "Found multiple module inferred file name annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_file_name_inferred.getAP(),
          df.getOWLLiteral(moduleName + "-module-inferred.owl"))));
    }

    // check/add the module generate annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_generate.getAP(),
        configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module generate annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_generate.getAP(), df.getOWLLiteral("true"))));
    }

    // check/add the module generate inferred annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_generate_inferred.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module generate annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_generate_inferred.getAP(), df.getOWLLiteral("true"))));
    }

    // check/add the builders annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(ModuleVocab.module_builders.getAP(),
        configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn("Found multiple module builders annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_builders.getAP(), df.getOWLLiteral("no-builders"))));
    }

    // check/add the inferred builders annotation
    values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_inferred_builders.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn(
          "Found multiple module inferred builders annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_inferred_builders.getAP(), df.getOWLLiteral("no-builders"))));
    }

    
    // check/add add legacy
    values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_add_legacy.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn(
          "Found multiple add legacy annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_add_legacy.getAP(), df.getOWLLiteral("false"))));
    }
    
    // check/add clean legacy
    values = OwlclUtil.getOntologyAnnotationLiteralValues(
        ModuleVocab.module_clean_legacy.getAP(), configurationOntology, false);
    if (values.size() > 1)
    {
      getLogger().warn(
          "Found multiple clean legacy annotations for module: " + moduleName);
    } else if (values.size() == 0)
    {
      man.applyChange(new AddOntologyAnnotation(configurationOntology, df.getOWLAnnotation(
          ModuleVocab.module_clean_legacy.getAP(), df.getOWLLiteral("false"))));
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
      man.saveOntology(configurationOntology, new FileOutputStream(new File(directory, moduleName
          + Owlcl.CONFIGURATION_IRI_SUFFIX)));
      man.saveOntology(topOntology, new FileOutputStream(new File(directory, moduleName
          + Owlcl.MODULE_TOP_IRI_SUFFIX)));

      // update files
      File versionFile = new File(directory, "V-" + moduleVersion);
      versionFile.delete();
      versionFile = new File(directory, "V-" + IModule.VERSION);
      versionFile.createNewFile();

      File annotationFile = new File(directory, moduleName + "-module-annotation.owl");
      annotationFile.renameTo(new File(directory, "_old_" + annotationFile.getName()));
    } catch (OWLOntologyStorageException | IOException e)
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

}
