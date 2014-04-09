package com.essaid.owlcl.command;

import static com.essaid.owlcl.command.EroCommand.Action.generate;
import static com.essaid.owlcl.command.EroCommand.Action.save;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.module.DefaultModule;
import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.ModuleNames;
import com.essaid.owlcl.command.module.config.IModuleConfig;
import com.essaid.owlcl.command.module.config.ModuleConfigurationV1;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.cli.util.CanonicalFileConverter;
import com.essaid.owlcl.core.cli.util.DirectoryExistsValueValidator;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.OwlclUtil;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

@Parameters(commandNames = { "ero" }, commandDescription = "Creates the ERO modules.")
public class EroCommand extends AbstractCommand {

  // ================================================================================
  // If any legacy files should be cleaned from the axioms the module is now
  // generating.
  // ================================================================================
  @Parameter(names = "-cleanLegacy",
      description = "Will clean the legacy ERO files from any axioms the module is "
          + "generating. To enable this, simply add the option without a value "
          + "(i.e. -cleanLegacy without \"true\" as an option value)")
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
  private boolean cleanLegacySet = false;

  // ================================================================================
  // If legacy content should be added to the module.
  // ================================================================================
  @Parameter(names = "-addLegacy",
      description = "Will add the legacy ERO content to the generated module. ")
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
  // Directory of reference run for diff report.
  // ================================================================================

  @Parameter(names = "-previous",
      description = "The previous version's directory for the diff report",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setPreviousDirectory(File previousDirectory) {
    this.previousDirectory = previousDirectory;
    this.previousDirectorySet = true;
  }

  public File getPreviousDirectory() {
    return previousDirectory;
  }

  public boolean isPreviousDirectorySet() {
    return previousDirectorySet;
  }

  private File previousDirectory;
  private boolean previousDirectorySet;

  // ================================================================================
  // Output directory unclassified
  // ================================================================================

  @Parameter(names = "-outputUnclassified",
      description = "The directory where the unclassified modules will " + "be saved.")
  public void setEroOutputUnclassified(File eroOutput) {
    this.eroOutputUnclassified = eroOutput;
    this.eroOutputUnclassifiedSet = true;
  }

  public File getEroOutputUnclassified() {
    return eroOutputUnclassified;
  }

  public boolean isEroOutputUnclassifiedSet() {
    return eroOutputUnclassifiedSet;
  }

  private File eroOutputUnclassified;
  private boolean eroOutputUnclassifiedSet;

  // ================================================================================
  // Output directory classified
  // ================================================================================

  @Parameter(names = "-outputClassified",
      description = "The directory where the generate modules will " + "be saved.")
  public void setEroOutputClassified(File eroOutput) {
    this.eroOutputClassified = eroOutput;
    this.eroOutputClassifiedSet = true;
  }

  public File getEroOutputClassified() {
    return eroOutputClassified;
  }

  public boolean isEroOutputClassifiedSet() {
    return eroOutputClassifiedSet;
  }

  private File eroOutputClassified;
  private boolean eroOutputClassifiedSet;

  // ================================================================================
  // Implementation
  // ================================================================================

  OWLOntology isfOntology = null;
  OWLOntologyManager man = null;
  OWLReasoner reasoner = null;
  File outputDirectory = null;

  @Inject
  IReasonerManager reasonerManager;

  @Inject
  Injector injector;
  public static final IRI ISF_DEV_IRI = IRI
      .create("http://purl.obolibrary.org/obo/arg/isf-dev.owl");

  @Inject
  public EroCommand(@Assisted OwlclCommand main) {
    super(main);
  }

  @Override
  protected void doInitialize() {
    configure();

  }

  protected void configure() {
    if (!isEroOutputClassifiedSet())
    {
      this.eroOutputClassified = new File(getMain().getJobDirectory(), "classified");
    }

    if (!isEroOutputUnclassifiedSet())
    {
      this.eroOutputUnclassified = new File(getMain().getJobDirectory(), "unclassified");
    }

  }

  @Override
  public Object call() throws Exception {
    configure();
    File moduleDirecotry;
    DefaultModule module;
    IModuleConfig mc;
    Path classifiedOutput;
    Path unclassifiedOutput;

    man = getMain().getSharedBaseManager();
    isfOntology = OwlclUtil.getOrLoadOntology(EroCommand.ISF_DEV_IRI, man);
    reasoner = reasonerManager.getReasonedOntology(isfOntology);

    // ================================================================================
    // eaglei
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "core").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "core").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eaglei = module;

    // ModuleCommand eaglei = new ModuleCommand(getMain());
    // eaglei.setModuleName(ModuleNames.EAGLEI);
    // eaglei.setOutput(new File(outputDirectory, "core"));
    // // eaglei.sourceOntology = isfOntology;
    // // eaglei.sourceReasoner = reasoner;
    // eaglei.setAddLegacy(addLegacy);
    // eaglei.setCleanLegacy(cleanLegacy);
    // eaglei.setReasoned(true);
    // eaglei.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "core"));
    // eaglei.module = module;

    // ================================================================================
    // extended GO
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED_GO);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "imports").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "imports").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);

    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule exgtendedGo = module;

    // //
    // ModuleCommand eagleiExtendedGo = new ModuleCommand(getMain());
    // eagleiExtendedGo.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO);
    // eagleiExtendedGo.setOutput(new File(outputDirectory, "imports"));
    // // eagleiExtendedGo.sourceOntology = isfOntology;
    // // eagleiExtendedGo.sourceReasoner = reasoner;
    // eagleiExtendedGo.setAddLegacy(addLegacy);
    // eagleiExtendedGo.setCleanLegacy(cleanLegacy);
    // eagleiExtendedGo.setReasoned(true);
    // eagleiExtendedGo.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI_EXTENDED_GO);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "imports"));
    // eagleiExtendedGo.module = module;
    //

    // ================================================================================
    // extended mesh
    // ================================================================================
    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED_MESH);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "imports").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "imports").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule exgtendedMesh = module;

    // //
    // ModuleCommand eagleiExtendedMesh = new ModuleCommand(getMain());
    // eagleiExtendedMesh.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH);
    // eagleiExtendedMesh.setOutput(new File(outputDirectory, "imports"));
    // // eagleiExtendedMesh.sourceOntology = isfOntology;
    // // eagleiExtendedMesh.sourceReasoner = reasoner;
    // eagleiExtendedMesh.setAddLegacy(addLegacy);
    // eagleiExtendedMesh.setCleanLegacy(cleanLegacy);
    // eagleiExtendedMesh.setReasoned(true);
    // eagleiExtendedMesh.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI_EXTENDED_MESH);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "imports"));
    // eagleiExtendedMesh.module = module;
    //

    // ================================================================================
    // extended MP
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED_MP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "imports").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "imports").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule exgtendedMP = module;

    // //
    // ModuleCommand eagleiExtendedMp = new ModuleCommand(getMain());
    // eagleiExtendedMp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP);
    // eagleiExtendedMp.setOutput(new File(outputDirectory, "imports"));
    // // eagleiExtendedMp.sourceOntology = isfOntology;
    // // eagleiExtendedMp.sourceReasoner = reasoner;
    // eagleiExtendedMp.setAddLegacy(addLegacy);
    // eagleiExtendedMp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedMp.setReasoned(true);
    // eagleiExtendedMp.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI_EXTENDED_MP);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "imports"));
    // eagleiExtendedMp.module = module;
    //

    // ================================================================================
    //
    // ================================================================================

    // ================================================================================
    // extended Pato
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED_PATO);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "imports").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "imports").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule exgtendedPato = module;

    // //
    // ModuleCommand eagleiExtendedPato = new ModuleCommand(getMain());
    // eagleiExtendedPato.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO);
    // eagleiExtendedPato.setOutput(new File(outputDirectory, "imports"));
    // // eagleiExtendedPato.sourceOntology = isfOntology;
    // // eagleiExtendedPato.sourceReasoner = reasoner;
    // eagleiExtendedPato.setAddLegacy(addLegacy);
    // eagleiExtendedPato.setCleanLegacy(cleanLegacy);
    // eagleiExtendedPato.setReasoned(true);
    // eagleiExtendedPato.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI_EXTENDED_PATO);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "imports"));
    // eagleiExtendedPato.module = module;
    //

    // ================================================================================
    // Extended uberon
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_UBERON);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "imports").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "imports").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule exgtendedUberon = module;

    // //
    // ModuleCommand eagleiExtendedUberon = new ModuleCommand(getMain());
    // eagleiExtendedUberon.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON);
    // eagleiExtendedUberon.setOutput(new File(outputDirectory, "imports"));
    // // eagleiExtendedUberon.sourceOntology = isfOntology;
    // // eagleiExtendedUberon.sourceReasoner = reasoner;
    // eagleiExtendedUberon.setAddLegacy(addLegacy);
    // eagleiExtendedUberon.setCleanLegacy(cleanLegacy);
    // eagleiExtendedUberon.setReasoned(true);
    // eagleiExtendedUberon.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/"
    // + ModuleNames.EAGLEI_EXTENDED_UBERON);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.loadConfiguration();
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // module = new DefaultModule(mc, new File(outputDirectory, "imports"));
    // eagleiExtendedUberon.module = module;
    //

    // ================================================================================
    // eaglei extended
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED);
    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "core").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "core").toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtended = module;

    eagleiExtended.importModuleIntoUnclassified(eaglei, false);
    eagleiExtended.importModuleIntoClassified(eaglei, true);

    eagleiExtended.importModuleIntoUnclassified(exgtendedGo, false);
    eagleiExtended.importModuleIntoClassified(exgtendedGo, true);

    eagleiExtended.importModuleIntoUnclassified(exgtendedMesh, false);
    eagleiExtended.importModuleIntoClassified(exgtendedMesh, true);

    eagleiExtended.importModuleIntoUnclassified(exgtendedMP, false);
    eagleiExtended.importModuleIntoClassified(exgtendedMP, true);

    eagleiExtended.importModuleIntoUnclassified(exgtendedPato, false);
    eagleiExtended.importModuleIntoClassified(exgtendedPato, true);

    eagleiExtended.importModuleIntoUnclassified(exgtendedUberon, false);
    eagleiExtended.importModuleIntoClassified(exgtendedUberon, true);

    //
    // //
    // ModuleCommand eagleiExtended = new ModuleCommand(getMain());
    // eagleiExtended.setModuleName(ModuleNames.EAGLEI_EXTENDED);
    // eagleiExtended.setOutput(new File(outputDirectory, "core"));
    // // eagleiExtended.sourceOntology = isfOntology;
    // // eagleiExtended.sourceReasoner = reasoner;
    // eagleiExtended.setAddLegacy(addLegacy);
    // eagleiExtended.setCleanLegacy(cleanLegacy);
    // eagleiExtended.setReasoned(true);
    // eagleiExtended.setUnReasoned(true);
    // moduleDirecotry = new File(getMain().getProject(), "module/" +
    // ModuleNames.EAGLEI_EXTENDED);
    // mc = new ModuleConfigurationV1(moduleDirecotry, man, man);
    // mc.setSourceOntology(isfOntology);
    // mc.setSourceReasoner(reasoner);
    // mc.loadConfiguration();
    // module = new DefaultModule(mc, new File(outputDirectory, "core"));
    // eagleiExtended.module = module;
    //

    // eagleiExtended.module.importModuleIntoBoth(eaglei.module, null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedGo.module,
    // null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedMesh.module,
    // null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedMp.module,
    // null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedPato.module,
    // null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedUberon.module,
    // null);
    // eagleiExtended.module.importModuleIntoBoth(eagleiExtendedGo.module,
    // null);

    // ================================================================================
    //
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiApp = module;

    // //
    // ModuleCommand eagleiApp = new ModuleCommand(getMain());
    // eagleiApp.setModuleName(ModuleNames.EAGLEI_APP);
    // eagleiApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiApp.sourceOntology = isfOntology;
    // eagleiApp.sourceReasoner = reasoner;
    // eagleiApp.setAddLegacy(addLegacy);
    // eagleiApp.setCleanLegacy(cleanLegacy);
    // eagleiApp.setReasoned(true);
    // eagleiApp.setUnReasoned(true);
    //
    // //

    // ================================================================================
    // eageli app def
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_APP_DEF);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiAppDef = module;

    eagleiApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiApp.importModuleIntoClassified(eagleiAppDef, true);

    // ModuleCommand eagleiAppDef = new ModuleCommand(getMain());
    // eagleiAppDef.setModuleName(ModuleNames.EAGLEI_APP_DEF);
    // eagleiAppDef.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiAppDef.sourceOntology = isfOntology;
    // eagleiAppDef.sourceReasoner = reasoner;
    // eagleiAppDef.setAddLegacy(addLegacy);
    // eagleiAppDef.setCleanLegacy(cleanLegacy);
    // eagleiAppDef.setReasoned(true);
    // eagleiAppDef.setUnReasoned(true);
    //
    // eagleiApp.module.importModuleIntoBoth(eagleiAppDef.module, null);
    //
    // //

    // ================================================================================
    // eaglei app extended
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/" + ModuleNames.EAGLEI_EXTENDED_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedApp = module;

    // ModuleCommand eagleiExtendedApp = new ModuleCommand(getMain());
    // eagleiExtendedApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_APP);
    // eagleiExtendedApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedApp.sourceOntology = isfOntology;
    // eagleiExtendedApp.sourceReasoner = reasoner;
    // eagleiExtendedApp.setAddLegacy(addLegacy);
    // eagleiExtendedApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedApp.setReasoned(true);
    // eagleiExtendedApp.setUnReasoned(true);
    //

    // ================================================================================
    // extended go app
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_GO_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedGoApp = module;

    eagleiExtendedGoApp.importModuleIntoUnclassified(exgtendedGo, false);
    eagleiExtendedGoApp.importModuleIntoClassified(exgtendedGo, true);

    eagleiExtendedGoApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedGoApp.importModuleIntoClassified(eagleiAppDef, true);

    // //
    // ModuleCommand eagleiExtendedGoApp = new ModuleCommand(getMain());
    // eagleiExtendedGoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO_APP);
    // eagleiExtendedGoApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedGoApp.sourceOntology = isfOntology;
    // eagleiExtendedGoApp.sourceReasoner = reasoner;
    // eagleiExtendedGoApp.setAddLegacy(addLegacy);
    // eagleiExtendedGoApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedGoApp.setReasoned(true);
    // eagleiExtendedGoApp.setUnReasoned(true);
    // eagleiExtendedGoApp.module.importModuleIntoBoth(eagleiExtendedGo.module,
    // null);
    // eagleiExtendedGoApp.module.importModuleIntoBoth(eagleiAppDef.module,
    // null);
    //

    // ================================================================================
    // extended mesh app
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_MESH_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);
    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedMeshApp = module;

    eagleiExtendedMeshApp.importModuleIntoUnclassified(exgtendedMesh, false);
    eagleiExtendedMeshApp.importModuleIntoClassified(exgtendedMesh, true);

    eagleiExtendedMeshApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedMeshApp.importModuleIntoClassified(eagleiAppDef, true);

    // //
    // ModuleCommand eagleiExtendedMeshApp = new ModuleCommand(getMain());
    // eagleiExtendedMeshApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH_APP);
    // eagleiExtendedMeshApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedMeshApp.sourceOntology = isfOntology;
    // eagleiExtendedMeshApp.sourceReasoner = reasoner;
    // eagleiExtendedMeshApp.setAddLegacy(addLegacy);
    // eagleiExtendedMeshApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedMeshApp.setReasoned(true);
    // eagleiExtendedMeshApp.setUnReasoned(true);
    // eagleiExtendedMeshApp.module.importModuleIntoBoth(eagleiExtendedMesh.module,
    // null);
    // eagleiExtendedMeshApp.module.importModuleIntoBoth(eagleiAppDef.module,
    // null);

    // ================================================================================
    // extended MP app
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_MP_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedMpApp = module;

    eagleiExtendedMpApp.importModuleIntoUnclassified(exgtendedMP, false);
    eagleiExtendedMpApp.importModuleIntoClassified(exgtendedMP, true);

    eagleiExtendedMpApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedMpApp.importModuleIntoClassified(eagleiAppDef, true);

    //
    // //
    // ModuleCommand eagleiExtendedMpApp = new ModuleCommand(getMain());
    // eagleiExtendedMpApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP_APP);
    // eagleiExtendedMpApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedMpApp.sourceOntology = isfOntology;
    // eagleiExtendedMpApp.sourceReasoner = reasoner;
    // eagleiExtendedMpApp.setAddLegacy(addLegacy);
    // eagleiExtendedMpApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedMpApp.setReasoned(true);
    // eagleiExtendedMpApp.setUnReasoned(true);
    // eagleiExtendedMpApp.module.importModuleIntoBoth(eagleiExtendedMp.module,
    // null);
    // eagleiExtendedMpApp.module.importModuleIntoBoth(eagleiAppDef.module,
    // null);
    //

    // ================================================================================
    // extended pato app
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_PATO_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedPatoApp = module;

    eagleiExtendedPatoApp.importModuleIntoUnclassified(exgtendedPato, false);
    eagleiExtendedPatoApp.importModuleIntoClassified(exgtendedPato, true);

    eagleiExtendedPatoApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedPatoApp.importModuleIntoClassified(eagleiAppDef, true);

    // //
    // ModuleCommand eagleiExtendedPatoApp = new ModuleCommand(getMain());
    // eagleiExtendedPatoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO_APP);
    // eagleiExtendedPatoApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedPatoApp.sourceOntology = isfOntology;
    // eagleiExtendedPatoApp.sourceReasoner = reasoner;
    // eagleiExtendedPatoApp.setAddLegacy(addLegacy);
    // eagleiExtendedPatoApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedPatoApp.setReasoned(true);
    // eagleiExtendedPatoApp.setUnReasoned(true);
    // eagleiExtendedPatoApp.module.importModuleIntoBoth(eagleiExtendedPato.module,
    // null);
    // eagleiExtendedPatoApp.module.importModuleIntoBoth(eagleiAppDef.module,
    // null);
    //

    // ================================================================================
    // uberon app
    // ================================================================================

    moduleDirecotry = new File(getMain().getProject(), "module/"
        + ModuleNames.EAGLEI_EXTENDED_UBERON_APP);

    mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(), getMain()
        .getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner, false);
    injector.injectMembers(mc);

    classifiedOutput = new File(eroOutputClassified, "application-specific-files").toPath();
    unclassifiedOutput = new File(eroOutputUnclassified, "unclassified/application-specific-files")
        .toPath();
    module = new DefaultModule(mc, unclassifiedOutput, classifiedOutput);
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }
    DefaultModule eagleiExtendedUberonApp = module;

    eagleiExtendedUberonApp.importModuleIntoUnclassified(exgtendedUberon, false);
    eagleiExtendedUberonApp.importModuleIntoClassified(exgtendedUberon, true);

    eagleiExtendedUberonApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedUberonApp.importModuleIntoClassified(eagleiAppDef, true);

    // //
    // ModuleCommand eagleiExtendedUberonApp = new ModuleCommand(getMain());
    // eagleiExtendedUberonApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON_APP);
    // eagleiExtendedUberonApp.setOutput(new File(outputDirectory,
    // "application-specific-files"));
    // eagleiExtendedUberonApp.sourceOntology = isfOntology;
    // eagleiExtendedUberonApp.sourceReasoner = reasoner;
    // eagleiExtendedUberonApp.setAddLegacy(addLegacy);
    // eagleiExtendedUberonApp.setCleanLegacy(cleanLegacy);
    // eagleiExtendedUberonApp.setReasoned(true);
    // eagleiExtendedUberonApp.setUnReasoned(true);
    // eagleiExtendedUberonApp.module.importModuleIntoBoth(eagleiExtendedUberon.module,
    // null);
    // eagleiExtendedUberonApp.module.importModuleIntoBoth(eagleiAppDef.module,
    // null);
    //

    // ================================================================================
    //
    // ================================================================================

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtended, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtended, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiApp, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtendedGoApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtendedGoApp, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtendedMeshApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtendedMeshApp, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtendedMpApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtendedMpApp, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtendedPatoApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtendedPatoApp, true);

    eagleiExtendedApp.importModuleIntoUnclassified(eagleiExtendedUberonApp, false);
    eagleiExtendedApp.importModuleIntoClassified(eagleiExtendedUberonApp, true);

    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtended.module,
    // null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiApp.module, null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedGoApp.module,
    // null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedMeshApp.module,
    // null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedMpApp.module,
    // null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedPatoApp.module,
    // null);
    // eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedUberonApp.module,
    // null);

    // ================================================================================
    //
    // ================================================================================

    eagleiExtendedApp.saveModule();

    CatalogCommand catalog = new CatalogCommand(getMain());
    injector.injectMembers(catalog);
    catalog.setDirectory(eroOutputClassified);
    try
    {
      catalog.call();
    } catch (Exception e)
    {
      throw new RuntimeException("Error creating ERO catalog files.", e);
    }

    catalog = new CatalogCommand(getMain());
    injector.injectMembers(catalog);
    catalog.setDirectory(eroOutputUnclassified);
    try
    {
      catalog.call();
    } catch (Exception e)
    {
      throw new RuntimeException("Error creating ERO catalog files.", e);
    }

    // topModule = eagleiExtended.module;

    //
    // topModule = eagleiExtendedApp.module;
    return null;
  }

  public void run() {
    for (String action : getAllActions())
    {
      Action.valueOf(action.toLowerCase()).execute(this);
    }
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    actionsList.add(generate.name());
    actionsList.add(save.name());
    actionsList.add(Action.catalog.name());
    if (previousDirectory != null)
    {
      actionsList.add(Action.compare.name());
    }
  }

  enum Action {
    generate {

      @Override
      public void execute(EroCommand command) {
        System.out.println("Saving top module to: ");
        // command.topModule.generateModule();
      }
    },

    save {

      @Override
      public void execute(EroCommand command) {
        // command.topModule.saveGeneratedModule();
      }
    },
    catalog {

      @Override
      public void execute(EroCommand command) {
        CatalogCommand catalog = new CatalogCommand(command.getMain());
        catalog.setDirectory(command.outputDirectory);
        try
        {
          catalog.call();
        } catch (Exception e)
        {
          throw new RuntimeException("Error creating ERO catalog files.", e);
        }
      }
    },
    compare {

      @Override
      public void execute(EroCommand command) {
//        CompareCommand cc = new CompareCommand(command.getMain());
//
//        cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));
//        cc.getFromFiles().add(command.previousDirectory);
//
//        cc.setToIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));
//        cc.getToFiles().add(command.eroOutput);
//        cc.setReportDirectory(command.eroOutput);
//        try
//        {
//          cc.call();
//        } catch (Exception e)
//        {
//          throw new RuntimeException("Failed to compare the ero versions.", e);
//        }
      }
    };

    public abstract void execute(EroCommand command);
  }

}
