package com.essaid.owlcl.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.module.DefaultModule;
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
  // Module base directory to allow module files to be outside the project
  // ================================================================================

  @Parameter(names = "-modulesBaseDir", description = "The base directory where module directories"
      + " are located", converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setModuleBaseDirectory(File moduleBaseDirectory) {
    this.moduleBaseDirectory = moduleBaseDirectory;
    this.moduleBaseDirectorySet = true;
  }

  public File getModuleBaseDirectory() {
    return moduleBaseDirectory;
  }

  public boolean isModuleBaseDirectorySet() {
    return moduleBaseDirectorySet;
  }

  private File moduleBaseDirectory;
  private boolean moduleBaseDirectorySet;

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
  // Directory of reference run for diff report.
  // ================================================================================

  @Parameter(names = "-previousUnclassified",
      description = "The previous unclassified version's directory for the diff report",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setPreviousUnclassifiedDirectory(File previousDirectory) {
    this.previousUnclassifiedDirectory = previousDirectory;
    this.previousUnclassifiedDirectorySet = true;
  }

  public File getPreviousUnclassifiedDirectory() {
    return previousUnclassifiedDirectory;
  }

  public boolean isPreviousUnclassifiedDirectorySet() {
    return previousUnclassifiedDirectorySet;
  }

  private File previousUnclassifiedDirectory;
  private boolean previousUnclassifiedDirectorySet;

  // ================================================================================
  // Directory of reference run for diff report.
  // ================================================================================

  @Parameter(names = "-previousClassified",
      description = "The previous classified version's directory for the diff report",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setPreviousClassifiedDirectory(File previousDirectory) {
    this.previousClassifiedDirectory = previousDirectory;
    this.previousClassifiedDirectorySet = true;
  }

  public File getPreviousClassifiedDirectory() {
    return previousClassifiedDirectory;
  }

  public boolean isPreviousClassifiedDirectorySet() {
    return previousClassifiedDirectorySet;
  }

  private File previousClassifiedDirectory;
  private boolean previousClassifiedDirectorySet;

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
    if (!isModuleBaseDirectorySet())
    {
      this.moduleBaseDirectory = new File(getMain().getProject(), "module");
    }

    if (!isEroOutputClassifiedSet())
    {
      this.eroOutputClassified = new File(getMain().getJobDirectory(), "classified");
    }

    if (!isEroOutputUnclassifiedSet())
    {
      this.eroOutputUnclassified = new File(getMain().getJobDirectory(), "unclassified");
    }

  }

  private DefaultModule configModule(String moduleName, String outputPath) {
    File moduleDirecotry = new File(moduleBaseDirectory, moduleName);
    IModuleConfig mc = ModuleConfigurationV1.getExistingInstance(moduleDirecotry.toPath(),
        getMain().getSharedBaseManager(), getMain().getSharedBaseManager(), isfOntology, reasoner,
        false);
    injector.injectMembers(mc);

    File classifiedOutput = null;
    if (this.eroOutputClassified != null)
    {
      classifiedOutput = new File(eroOutputClassified, outputPath);
    }

    File unclassifiedOutput = null;
    if (this.eroOutputUnclassified != null)
    {
      unclassifiedOutput = new File(eroOutputUnclassified, outputPath);
    }

    DefaultModule module = new DefaultModule(mc, unclassifiedOutput.toPath(),
        classifiedOutput.toPath());
    injector.injectMembers(module);
    if (addLegacySet)
    {
      module.setAddLegacyClassified(addLegacy);
      module.setAddLegacyUnclassified(addLegacy);
    }

    return module;
  }

  @Override
  public Object call() throws Exception {
    configure();

    man = getMain().getSharedBaseManager();
    isfOntology = OwlclUtil.getOrLoadOntology(EroCommand.ISF_DEV_IRI, man);
    reasoner = reasonerManager.getReasonedOntology(isfOntology);

    // ================================================================================
    // eaglei
    // ================================================================================

    DefaultModule eagleiExtended = configModule(ModuleNames.EAGLEI_EXTENDED, "core");

    DefaultModule eaglei = configModule(ModuleNames.EAGLEI, "core");
    eagleiExtended.importModuleIntoUnclassified(eaglei, false);
    eagleiExtended.importModuleIntoClassified(eaglei, true);

    DefaultModule exgtendedGo = configModule(ModuleNames.EAGLEI_EXTENDED_GO, "imports");
    eagleiExtended.importModuleIntoUnclassified(exgtendedGo, false);
    eagleiExtended.importModuleIntoClassified(exgtendedGo, true);

    DefaultModule exgtendedMesh = configModule(ModuleNames.EAGLEI_EXTENDED_MESH, "imports");
    eagleiExtended.importModuleIntoUnclassified(exgtendedMesh, false);
    eagleiExtended.importModuleIntoClassified(exgtendedMesh, true);

    DefaultModule exgtendedMP = configModule(ModuleNames.EAGLEI_EXTENDED_MP, "imports");
    eagleiExtended.importModuleIntoUnclassified(exgtendedMP, false);
    eagleiExtended.importModuleIntoClassified(exgtendedMP, true);

    DefaultModule exgtendedPato = configModule(ModuleNames.EAGLEI_EXTENDED_PATO, "imports");
    eagleiExtended.importModuleIntoUnclassified(exgtendedPato, false);
    eagleiExtended.importModuleIntoClassified(exgtendedPato, true);

    DefaultModule exgtendedUberon = configModule(ModuleNames.EAGLEI_EXTENDED_UBERON, "imports");
    eagleiExtended.importModuleIntoUnclassified(exgtendedUberon, false);
    eagleiExtended.importModuleIntoClassified(exgtendedUberon, true);

    DefaultModule eagleiApp = configModule(ModuleNames.EAGLEI_APP, "application-specific-files");
    eagleiApp.importModuleIntoUnclassified(eaglei, false);
    eagleiApp.importModuleIntoClassified(eaglei, true);

    DefaultModule eagleiAppDef = configModule(ModuleNames.EAGLEI_APP_DEF,
        "application-specific-files");
    eagleiApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedGoApp = configModule(ModuleNames.EAGLEI_EXTENDED_GO_APP,
        "application-specific-files");
    eagleiExtendedGoApp.importModuleIntoUnclassified(exgtendedGo, false);
    eagleiExtendedGoApp.importModuleIntoClassified(exgtendedGo, true);
    eagleiExtendedGoApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedGoApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedMeshApp = configModule(ModuleNames.EAGLEI_EXTENDED_MESH_APP,
        "application-specific-files");
    eagleiExtendedMeshApp.importModuleIntoUnclassified(exgtendedMesh, false);
    eagleiExtendedMeshApp.importModuleIntoClassified(exgtendedMesh, true);
    eagleiExtendedMeshApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedMeshApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedMpApp = configModule(ModuleNames.EAGLEI_EXTENDED_MP_APP,
        "application-specific-files");
    eagleiExtendedMpApp.importModuleIntoUnclassified(exgtendedMP, false);
    eagleiExtendedMpApp.importModuleIntoClassified(exgtendedMP, true);
    eagleiExtendedMpApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedMpApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedPatoApp = configModule(ModuleNames.EAGLEI_EXTENDED_PATO_APP,
        "application-specific-files");
    eagleiExtendedPatoApp.importModuleIntoUnclassified(exgtendedPato, false);
    eagleiExtendedPatoApp.importModuleIntoClassified(exgtendedPato, true);
    eagleiExtendedPatoApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedPatoApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedUberonApp = configModule(ModuleNames.EAGLEI_EXTENDED_UBERON_APP,
        "application-specific-files");
    eagleiExtendedUberonApp.importModuleIntoUnclassified(exgtendedUberon, false);
    eagleiExtendedUberonApp.importModuleIntoClassified(exgtendedUberon, true);
    eagleiExtendedUberonApp.importModuleIntoUnclassified(eagleiAppDef, false);
    eagleiExtendedUberonApp.importModuleIntoClassified(eagleiAppDef, true);

    DefaultModule eagleiExtendedApp = configModule(ModuleNames.EAGLEI_EXTENDED_APP,
        "application-specific-files");
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

    eagleiExtendedApp.saveModule();

    // catalog
    CatalogCommand catalog;

    if (eroOutputClassified != null)
    {
      catalog = new CatalogCommand(getMain());
      injector.injectMembers(catalog);
      catalog.setTargetDirectory(eroOutputClassified);
      try
      {
        catalog.call();
      } catch (Exception e)
      {
        throw new RuntimeException("Error creating ERO catalog files.", e);
      }
    }

    if (eroOutputUnclassified != null)
    {
      catalog = new CatalogCommand(getMain());
      injector.injectMembers(catalog);
      catalog.setTargetDirectory(eroOutputUnclassified);
      try
      {
        catalog.call();
      } catch (Exception e)
      {
        throw new RuntimeException("Error creating ERO catalog files.", e);
      }
    }

    // compare

    if (previousClassifiedDirectorySet && eroOutputClassified != null)
    {
      // compare to original ERO
      CompareCommand cc = injector.getInstance(CompareCommand.class);
      cc.setReportName("CompareGeneratedToOriginal");

      List<File> files = new ArrayList<File>();
      files.add(eroOutputClassified);
      cc.setFromFiles(files);
      cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      files = new ArrayList<File>();
      files.add(previousClassifiedDirectory);
      cc.setToFiles(files);
      cc.setToIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      cc.setReportDirectory(eroOutputClassified);
      cc.call();

      // compare to ISF
      cc = injector.getInstance(CompareCommand.class);
      cc.setReportName("CompareGeneratedToISF");
      cc.setNoAdded(true);

      files = new ArrayList<File>();
      files.add(eroOutputClassified);
      cc.setFromFiles(files);
      cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      files = new ArrayList<File>();
      files.add(getMain().getProject());
      cc.setToFiles(files);
      cc.setToIri(ISF_DEV_IRI);

      cc.setReportDirectory(eroOutputClassified);
      cc.call();

    }

    if (previousUnclassifiedDirectorySet && eroOutputUnclassified != null)
    {

      // compare to original
      CompareCommand cc = injector.getInstance(CompareCommand.class);
      cc.setReportName("CompareGeneratedToOriginal");

      List<File> files = new ArrayList<File>();
      files.add(eroOutputUnclassified);
      cc.setFromFiles(files);
      cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      files = new ArrayList<File>();
      files.add(previousUnclassifiedDirectory);
      cc.setToFiles(files);
      cc.setToIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      cc.setReportDirectory(eroOutputUnclassified);
      cc.call();

      // compare to ISF
      cc = injector.getInstance(CompareCommand.class);
      cc.setReportName("CompareGeneratedToISF");
      cc.setNoAdded(true);

      files = new ArrayList<File>();
      files.add(eroOutputUnclassified);
      cc.setFromFiles(files);
      cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));

      files = new ArrayList<File>();
      files.add(getMain().getProject());
      cc.setToFiles(files);
      cc.setToIri(ISF_DEV_IRI);

      cc.setReportDirectory(eroOutputUnclassified);
      cc.call();
    }

    return null;
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {

  }

  enum Action {

  }

}
