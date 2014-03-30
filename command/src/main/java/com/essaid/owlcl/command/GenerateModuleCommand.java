package com.essaid.owlcl.command;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.core.IModule;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.module.DefaultModule;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = "module",
    commandDescription = "Generate the named module. The module has to be already created.")
public class GenerateModuleCommand extends AbstractCommand {

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

  private String moduleName = null;
  private boolean moduleNameSet;

  // ================================================================================
  // The directory where the module files are located (not the generated
  // files)
  // ================================================================================

  @Parameter(names = "-directory", converter = CanonicalFileConverter.class,
      description = "The location where the module defining files.")
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

  private File directory = null;
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

  @Parameter(names = "-unReasoned", arity = 1,
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

  private boolean unReasoned = false;
  private boolean unReasonedSet = false;

  // ================================================================================
  // do reasoned
  // ================================================================================

  @Parameter(names = "-reasoned", arity = 1,
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

  private boolean reasoned = false;
  private boolean reasonedSet = false;

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

  private boolean addLegacy = false;
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

  private boolean cleanLegacy = false;
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
      output = new File(getMain().getProject(), "module-output/" + moduleName);
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  public GenerateModuleCommand(@Assisted OwlclCommand main) {
    super(main);
    configure();
  }

  OWLReasoner sourceReasoner;
  OWLOntology sourceOntology;
  IModule module = null;

  public void run() {
    configure();
    output.mkdirs();

    if (sourceOntology != null && sourceReasoner != null)
    {
      module = new DefaultModule(moduleName, directory, sourceOntology, sourceReasoner, output,
          addLegacy, cleanLegacy);
    } else
    {
      module = new DefaultModule(this.moduleName, this.directory, getMain().getSharedBaseManager(),
          output, addLegacy, cleanLegacy);
    }

    // load the owl configuration
    module.loadConfiguration();

    // override the configuration from command line
    if (reasonedSet)
    {
      module.setGenerateInferred(reasoned);
    }

    if (unReasonedSet)
    {
      module.setGenerateInferred(unReasoned);
    }

    module.generateModule();
    module.saveGeneratedModule();
    module.saveModuleConfiguration();

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
