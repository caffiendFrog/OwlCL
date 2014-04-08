package com.essaid.owlcl.command;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.module.DefaultModule;
import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.Util;
import com.essaid.owlcl.command.module.config.IModuleConfig;
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
  // The directory where the module output will go
  // ================================================================================

  @Parameter(names = "-output", converter = CanonicalFileConverter.class,
      description = "The location where the module output will go.")
  public void setOutput(File directory) {
    this.output = directory;
    this.outputSet = true;
  }

  public File getOutput() {
    return output;
  }

  public boolean isOutputSet() {
    return outputSet;
  }

  private File output = null;
  private boolean outputSet;

  // ================================================================================
  // do unreasoned
  // ================================================================================

  @Parameter(names = "-unInferred", arity = 1,
      description = "Set the module to generate the un-reasoned version. "
          + "Use it to overrides the default module configuration if needed."
          + "Ignore the shown default on the command line, the default is what "
          + "is set in the module configuration file.")
  public void setUnReasoned(boolean unReasoned) {
    this.unReasoned = unReasoned;
    this.unReasonedSet = true;
  }

  public boolean isUnReasoned() {
    return unReasoned;
  }

  public boolean isUnReasonedSet() {
    return unReasonedSet;
  }

  private boolean unReasoned;
  private boolean unReasonedSet;

  // ================================================================================
  // do reasoned
  // ================================================================================

  @Parameter(names = "-inferred", arity = 1,
      description = "Set the module to generate the reasoned version. "
          + "Use it to overrides the default module configuration if needed."
          + "Ignore the shown default on the command line, the default is what "
          + "is set in the module configuration file.")
  public void setReasoned(boolean reasoned) {
    this.reasoned = reasoned;
    this.reasonedSet = true;
  }

  public boolean isReasoned() {
    return reasoned;
  }

  public boolean isReasonedSet() {
    return reasonedSet;
  }

  private boolean reasoned;
  private boolean reasonedSet;

  // ================================================================================
  // Add legacy
  // ================================================================================

  @Parameter(names = "-addLegacy",
      description = "If this option is set, legacy content will be added.")
  public void setAddLegacy(boolean addLegacy) {
    this.addLegacy = addLegacy;
    this.addLegacySet = true;
  }

  public boolean isAddLegacy() {
    return addLegacy;
  }

  public boolean isAddLegacySet() {
    return addLegacySet;
  }

  private boolean addLegacy;
  private boolean addLegacySet;

  // ================================================================================
  // clean legacy
  // ================================================================================

  @Parameter(names = "-cleanLegacy",
      description = "If this option is set, legacy content will be cleaned.")
  public void setCleanLegacy(boolean cleanLegacy) {
    this.cleanLegacy = cleanLegacy;
    this.cleanLegacySet = true;
  }

  public boolean isCleanLegacy() {
    return cleanLegacy;
  }

  public boolean isCleanLegacySet() {
    return cleanLegacySet;
  }

  private boolean cleanLegacy;
  private boolean cleanLegacySet;

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

    if (!outputSet)
    {
      output = new File(getMain().getJobDirectory(), "module-output/" + moduleName);
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

  OWLReasoner sourceReasoner;
  OWLOntology sourceOntology;
  IModule module = null;

  Set<ModuleCommand> imports = new HashSet<ModuleCommand>();

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

    output.mkdirs();
    if (module == null)
    {

      IModuleConfig moduleComfig = new ModuleConfigurationV1(directory, getMain()
          .getSharedBaseManager(), getMain().getSharedBaseManager());

      injector.injectMembers(moduleComfig);

      moduleComfig.loadConfiguration();

      module = new DefaultModule(moduleComfig, output);

      injector.injectMembers(module);
    }

    // override the configuration from command line
    if (reasonedSet)
    {
      module.setClassified(reasoned);
    }

    if (unReasonedSet)
    {
      module.setClassified(unReasoned);
    }

    if (addLegacySet)
    {
      module.setAddLegacy(addLegacy);
    }

    if (cleanLegacySet)
    {
      module.setCleanLegacy(cleanLegacy);
    }

    if (sourceOntology != null)
    {
      module.getModuleConfiguration().setSourceOntology(sourceOntology);
    }

    if (sourceReasoner != null)
    {
      module.getModuleConfiguration().setSourceReasoner(sourceReasoner);
    }

    module.generateModule();
    module.saveGeneratedModule();

    return null;
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

}
