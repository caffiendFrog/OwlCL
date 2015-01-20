package com.essaid.owlcl.command;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.module.DefaultModule;
import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.command.module.config.IModuleConfig;
import com.essaid.owlcl.command.module.config.IModuleConfigInternal;
import com.essaid.owlcl.command.module.config.ModuleConfigurationV1;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "module",
    commandDescription = "Generate the named module. The module has to be already created.")
public class ModuleCommand extends AbstractCommand {

  // ================================================================================
  // The module name
  // ================================================================================

  @Parameter(
      names = "-name",
      description = "The module name. This will be used to create default IRIs, files, and folders if needed.")
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
  // The directory where the module files are located (not the generated
  // files)
  // ================================================================================

  @Parameter(names = "-directory", converter = CanonicalFileConverter.class,
      description = "The directory location of the module defining files.")
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
  // The directory where the module's classified output will go
  // ================================================================================

  @Parameter(names = "-outputClassified", converter = CanonicalFileConverter.class,
      description = "The location where the module's classified output will go.")
  public void setOutputClassified(File directoryClassified) {
    this.outputClassified = directoryClassified;
    this.outputClassifiedSet = true;
  }

  public File getOutputClassified() {
    return outputClassified;
  }

  public boolean isOutputClassifiedSet() {
    return outputClassifiedSet;
  }

  private File outputClassified;
  private boolean outputClassifiedSet;

  // ================================================================================
  // The directory where the module's classified output will go
  // ================================================================================

  @Parameter(names = "-outputUnclassified", converter = CanonicalFileConverter.class,
      description = "The location where the module's unclassified output will go.")
  public void setOutputUnclassified(File directoryUnclassified) {
    this.outputUnclassified = directoryUnclassified;
    this.outputUnclassifiedSet = true;
  }

  public File getOutputUnclassified() {
    return outputUnclassified;
  }

  public boolean isOutputUnclassifiedSet() {
    return outputUnclassifiedSet;
  }

  private File outputUnclassified;
  private boolean outputUnclassifiedSet;

  // ================================================================================
  // do unreasoned
  // ================================================================================

  @Parameter(names = "-unclassified", arity = 1,
      description = "Set the module to generate the unclassified version. "
          + "Use it to overrides the default module configuration if needed."
          + "Ignore the shown default on the command line, the default is what "
          + "is set in the module configuration file.")
  public void setUnclassified(boolean unclassified) {
    this.unclassified = unclassified;
    this.unclassifiedSet = true;
  }

  public boolean isUnclassified() {
    return unclassified;
  }

  public boolean isUnclassifiedSet() {
    return unclassifiedSet;
  }

  private boolean unclassified;
  private boolean unclassifiedSet;

  // ================================================================================
  // do reasoned
  // ================================================================================

  @Parameter(names = "-classified",
      description = "Set the module to generate the classified version. "
          + "Use it to overrides the default module configuration if needed."
          + "Ignore the shown default on the command line, the default is what "
          + "is set in the module configuration file.")
  public void setClassified(boolean classified) {
    this.classified = classified;
    this.classifiedSet = true;
  }

  public boolean isClassified() {
    return classified;
  }

  public boolean isClassifiedSet() {
    return classifiedSet;
  }

  private boolean classified;
  private boolean classifiedSet;

  // ================================================================================
  // Add legacy unclassified
  // ================================================================================

  @Parameter(
      names = "-addLegacyUnclassified",
      description = "If this option is set, legacy content will be added to the unclassified version.")
  public void setAddLegacyUnclassified(boolean addLegacy) {
    this.addLegacyUnclassified = addLegacy;
    this.addLegacyUnclassifiedSet = true;
  }

  public boolean isAddLegacyUnclassified() {
    return addLegacyUnclassified;
  }

  public boolean isAddLegacyUnclassifiedSet() {
    return addLegacyUnclassifiedSet;
  }

  private boolean addLegacyUnclassified;
  private boolean addLegacyUnclassifiedSet;

  // ================================================================================
  // Add legacy classified
  // ================================================================================

  @Parameter(
      names = "-addLegacyClassified",
      description = "If this option is set, legacy content will be added to the classified version.")
  public void setAddLegacyClassified(boolean addLegacy) {
    this.addLegacyClassified = addLegacy;
    this.addLegacyClassifiedSet = true;
  }

  public boolean isAddLegacyClassified() {
    return addLegacyClassified;
  }

  public boolean isAddLegacyClassifiedSet() {
    return addLegacyClassifiedSet;
  }

  private boolean addLegacyClassified;
  private boolean addLegacyClassifiedSet;

  // ================================================================================
  // clean legacy unclassified
  // ================================================================================

  @Parameter(
      names = "-cleanLegacyUnclassified",
      description = "If this option is set, legacy content will be cleaned based on the content of the classified"
          + "version.")
  public void setCleanLegacyUnclassified(boolean cleanLegacy) {
    this.cleanLegacyUnclassified = cleanLegacy;
    this.cleanLegacyUnclassifiedSet = true;
  }

  public boolean isCleanLegacyUnclassified() {
    return cleanLegacyUnclassified;
  }

  public boolean isCleanLegacyUnclassifiedSet() {
    return cleanLegacyUnclassifiedSet;
  }

  private boolean cleanLegacyUnclassified;
  private boolean cleanLegacyUnclassifiedSet;

  // ================================================================================
  // clean legacy classified
  // ================================================================================

  @Parameter(
      names = "-cleanLegacyClassified",
      description = "If this option is set, legacy content will be cleaned based on the content of the classified"
          + "version.")
  public void setCleanLegacyClassified(boolean cleanLegacy) {
    this.cleanLegacyClassified = cleanLegacy;
    this.cleanLegacyClassifiedSet = true;
  }

  public boolean isCleanLegacyClassified() {
    return cleanLegacyClassified;
  }

  public boolean isCleanLegacyClassifiedSet() {
    return cleanLegacyClassifiedSet;
  }

  private boolean cleanLegacyClassified;
  private boolean cleanLegacyClassifiedSet;

  // ================================================================================
  // Initialization
  // ================================================================================

  protected void configure() {

    if (!moduleNameSet)
    {
      this.moduleName = "_unnamed";
    }
    if (!directorySet)
    {
      directory = new File(getMain().getProject(), "module/" + moduleName);
    }

    if (!outputClassifiedSet)
    {
      outputClassified = new File(getMain().getJobDirectory(), "module-output/" + moduleName
          + "/classified");
    }

    if (!outputUnclassifiedSet)
    {
      outputUnclassified = new File(getMain().getJobDirectory(), "module-output/" + moduleName
          + "/unclassified");
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  Injector injector;

  @Inject
  public ModuleCommand(@Assisted OwlclCommand main) {
    super(main);
  }

//  Set<ModuleCommand> imports = new HashSet<ModuleCommand>();

  protected void doInitialize() {
    configure();
  };

  private boolean preconditions() {
    if (!getDirectory().exists())
    {
      getLogger().error("Module {} with directory {}  does not exist. Skipping.", moduleName,
          getDirectory().getAbsolutePath());
      return false;
    }

    int version = Util.getModuleVersion(getDirectory());
    getLogger().info("Module version is: {}", version);
    if (version != IModuleConfig.CURRENT_VERSION)
    {
      getLogger()
          .error(
              "Module version {} does not match this tools's version {} , skipping. Please update module {} at: {}",
              version, IModuleConfig.CURRENT_VERSION, moduleName, getDirectory().getAbsolutePath());
      return false;
    }
    return true;
  }

  @Override
  public Object call() throws Exception {
    configure();
    if (!preconditions())
    {
      getLogger().error("Module {} located at {} failed preconditions.", moduleName,
          getDirectory().getAbsolutePath());
      return null;
    }

    // IModuleConfig moduleComfig = new ModuleConfigurationV1(directory,
    // getMain()
    // .getSharedBaseManager(), getMain().getSharedBaseManager());

    IModuleConfigInternal moduleComfig = ModuleConfigurationV1.getExistingInstance(
        directory.toPath(), getMain().getSharedBaseManager(), getMain().getSharedBaseManager(),
        null, null, false);

    injector.injectMembers(moduleComfig);

    IModule module = new DefaultModule(moduleComfig, outputUnclassified.toPath(),
        outputClassified.toPath());

    injector.injectMembers(module);

    // override the configuration from command line
    if (classifiedSet)
    {
      module.setClassified(classified);
    }

    if (unclassifiedSet)
    {
      module.setUnclassified(unclassified);
    }

    if (addLegacyUnclassifiedSet)
    {
      module.setAddLegacyClassified(addLegacyUnclassified);
    }

    if (addLegacyClassifiedSet)
    {
      module.setAddLegacyClassified(addLegacyClassified);
    }

    module.saveModule();
    
    // if (addLegacySet)
    // {
    // module.setAddLegacyClassified(addLegacy);
    //
    // if (cleanLegacySet)
    // {
    // module.setCleanLegacy(cleanLegacy);
    // }
    //
    // if (sourceOntology != null)
    // {
    // module.getModuleConfiguration().setSourceOntology(sourceOntology);
    // }
    //
    // if (sourceReasoner != null)
    // {
    // module.getModuleConfiguration().setSourceReasoner(sourceReasoner);
    // }
    //
    // module.generateModule();
    // module.saveGeneratedModule();
    //
    // }
    return null;
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

}
