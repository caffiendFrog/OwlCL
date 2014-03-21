package isf.command;

import static isf.command.EroCommand.Action.generate;
import static isf.command.EroCommand.Action.save;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.DirectoryExistsValueValidator;
import isf.module.Module;
import isf.util.ISFT;
import isf.util.ISFTUtil;
import isf.util.ModuleNames;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

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
	boolean cleanLegacy = false;

	// ================================================================================
	// If legacy content should be added to the module.
	// ================================================================================
	@Parameter(names = "-addLegacy", arity = 1,
			description = "Will add the legacy ERO content to the generated module. Use "
					+ "the option with a value (i.e. -addLegacy false) to disable this.")
	boolean addLegacy = true;

	// ================================================================================
	// Directory of reference run for diff report.
	// ================================================================================

	@Parameter(names = "-previous", converter = CanonicalFileConverter.class,
			validateValueWith = DirectoryExistsValueValidator.class)
	public File previousDirectory;

	// ================================================================================
	// Initialization
	// ================================================================================

	@Override
	protected void configure() {
		// TODO Auto-generated method stub

	}

	// ================================================================================
	// Implementation
	// ================================================================================

	Module topModule = null;

	OWLOntology isfOntology = null;
	OWLOntologyManager man = null;
	OWLReasoner reasoner = null;
	File outputDirectory = null;

	protected void init() {
		man = getMain().getSharedBaseManager();
		outputDirectory = new File(getMain().getJobDirectory(), "ero-release");

		isfOntology = ISFTUtil.getOrLoadOntology(ISFT.ISF_DEV_IRI, man);

		reasoner = ISFTUtil.getReasoner(isfOntology);

		GenerateModuleCommand eaglei = new GenerateModuleCommand(getMain());
		eaglei.setModuleName(ModuleNames.EAGLEI);
		eaglei.setOutput(new File(outputDirectory, "core"));
		eaglei.sourceOntology = isfOntology;
		eaglei.sourceReasoner = reasoner;
		eaglei.setAddLegacy(addLegacy);
		eaglei.setCleanLegacy(cleanLegacy);
		eaglei.setReasoned(true);
		eaglei.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtended = new GenerateModuleCommand(getMain());
		eagleiExtended.setModuleName(ModuleNames.EAGLEI_EXTENDED);
		eagleiExtended.setOutput(new File(outputDirectory, "core"));
		eagleiExtended.sourceOntology = isfOntology;
		eagleiExtended.sourceReasoner = reasoner;
		eagleiExtended.setAddLegacy(addLegacy);
		eagleiExtended.setCleanLegacy(cleanLegacy);
		eagleiExtended.setReasoned(true);
		eagleiExtended.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtendedGo = new GenerateModuleCommand(getMain());
		eagleiExtendedGo.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO);
		eagleiExtendedGo.setOutput(new File(outputDirectory, "imports"));
		eagleiExtendedGo.sourceOntology = isfOntology;
		eagleiExtendedGo.sourceReasoner = reasoner;
		eagleiExtendedGo.setAddLegacy(addLegacy);
		eagleiExtendedGo.setCleanLegacy(cleanLegacy);
		eagleiExtendedGo.setReasoned(true);
		eagleiExtendedGo.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtendedMesh = new GenerateModuleCommand(getMain());
		eagleiExtendedMesh.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH);
		eagleiExtendedMesh.setOutput(new File(outputDirectory, "imports"));
		eagleiExtendedMesh.sourceOntology = isfOntology;
		eagleiExtendedMesh.sourceReasoner = reasoner;
		eagleiExtendedMesh.setAddLegacy(addLegacy);
		eagleiExtendedMesh.setCleanLegacy(cleanLegacy);
		eagleiExtendedMesh.setReasoned(true);
		eagleiExtendedMesh.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtendedMp = new GenerateModuleCommand(getMain());
		eagleiExtendedMp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP);
		eagleiExtendedMp.setOutput(new File(outputDirectory, "imports"));
		eagleiExtendedMp.sourceOntology = isfOntology;
		eagleiExtendedMp.sourceReasoner = reasoner;
		eagleiExtendedMp.setAddLegacy(addLegacy);
		eagleiExtendedMp.setCleanLegacy(cleanLegacy);
		eagleiExtendedMp.setReasoned(true);
		eagleiExtendedMp.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtendedPato = new GenerateModuleCommand(getMain());
		eagleiExtendedPato.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO);
		eagleiExtendedPato.setOutput(new File(outputDirectory, "imports"));
		eagleiExtendedPato.sourceOntology = isfOntology;
		eagleiExtendedPato.sourceReasoner = reasoner;
		eagleiExtendedPato.setAddLegacy(addLegacy);
		eagleiExtendedPato.setCleanLegacy(cleanLegacy);
		eagleiExtendedPato.setReasoned(true);
		eagleiExtendedPato.setUnReasoned(true);
		//
		GenerateModuleCommand eagleiExtendedUberon = new GenerateModuleCommand(getMain());
		eagleiExtendedUberon.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON);
		eagleiExtendedUberon.setOutput(new File(outputDirectory, "imports"));
		eagleiExtendedUberon.sourceOntology = isfOntology;
		eagleiExtendedUberon.sourceReasoner = reasoner;
		eagleiExtendedUberon.setAddLegacy(addLegacy);
		eagleiExtendedUberon.setCleanLegacy(cleanLegacy);
		eagleiExtendedUberon.setReasoned(true);
		eagleiExtendedUberon.setUnReasoned(true);

		eagleiExtended.module.importModuleIntoBoth(eaglei.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedGo.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedMesh.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedMp.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedPato.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedUberon.module, null);
		eagleiExtended.module.importModuleIntoBoth(eagleiExtendedGo.module, null);

		//
		GenerateModuleCommand eagleiApp = new GenerateModuleCommand(getMain());
		eagleiApp.setModuleName(ModuleNames.EAGLEI_APP);
		eagleiApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiApp.sourceOntology = isfOntology;
		eagleiApp.sourceReasoner = reasoner;
		eagleiApp.setAddLegacy(addLegacy);
		eagleiApp.setCleanLegacy(cleanLegacy);
		eagleiApp.setReasoned(true);
		eagleiApp.setUnReasoned(true);

		//
		GenerateModuleCommand eagleiAppDef = new GenerateModuleCommand(getMain());
		eagleiAppDef.setModuleName(ModuleNames.EAGLEI_APP_DEF);
		eagleiAppDef.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiAppDef.sourceOntology = isfOntology;
		eagleiAppDef.sourceReasoner = reasoner;
		eagleiAppDef.setAddLegacy(addLegacy);
		eagleiAppDef.setCleanLegacy(cleanLegacy);
		eagleiAppDef.setReasoned(true);
		eagleiAppDef.setUnReasoned(true);

		eagleiApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		//
		GenerateModuleCommand eagleiExtendedApp = new GenerateModuleCommand(getMain());
		eagleiExtendedApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_APP);
		eagleiExtendedApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedApp.sourceOntology = isfOntology;
		eagleiExtendedApp.sourceReasoner = reasoner;
		eagleiExtendedApp.setAddLegacy(addLegacy);
		eagleiExtendedApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedApp.setReasoned(true);
		eagleiExtendedApp.setUnReasoned(true);

		//
		GenerateModuleCommand eagleiExtendedGoApp = new GenerateModuleCommand(getMain());
		eagleiExtendedGoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO_APP);
		eagleiExtendedGoApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedGoApp.sourceOntology = isfOntology;
		eagleiExtendedGoApp.sourceReasoner = reasoner;
		eagleiExtendedGoApp.setAddLegacy(addLegacy);
		eagleiExtendedGoApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedGoApp.setReasoned(true);
		eagleiExtendedGoApp.setUnReasoned(true);
		eagleiExtendedGoApp.module.importModuleIntoBoth(eagleiExtendedGo.module, null);
		eagleiExtendedGoApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		//
		GenerateModuleCommand eagleiExtendedMeshApp = new GenerateModuleCommand(getMain());
		eagleiExtendedMeshApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH_APP);
		eagleiExtendedMeshApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedMeshApp.sourceOntology = isfOntology;
		eagleiExtendedMeshApp.sourceReasoner = reasoner;
		eagleiExtendedMeshApp.setAddLegacy(addLegacy);
		eagleiExtendedMeshApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedMeshApp.setReasoned(true);
		eagleiExtendedMeshApp.setUnReasoned(true);
		eagleiExtendedMeshApp.module.importModuleIntoBoth(eagleiExtendedMesh.module, null);
		eagleiExtendedMeshApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		//
		GenerateModuleCommand eagleiExtendedMpApp = new GenerateModuleCommand(getMain());
		eagleiExtendedMpApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP_APP);
		eagleiExtendedMpApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedMpApp.sourceOntology = isfOntology;
		eagleiExtendedMpApp.sourceReasoner = reasoner;
		eagleiExtendedMpApp.setAddLegacy(addLegacy);
		eagleiExtendedMpApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedMpApp.setReasoned(true);
		eagleiExtendedMpApp.setUnReasoned(true);
		eagleiExtendedMpApp.module.importModuleIntoBoth(eagleiExtendedMp.module, null);
		eagleiExtendedMpApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		//
		GenerateModuleCommand eagleiExtendedPatoApp = new GenerateModuleCommand(getMain());
		eagleiExtendedPatoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO_APP);
		eagleiExtendedPatoApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedPatoApp.sourceOntology = isfOntology;
		eagleiExtendedPatoApp.sourceReasoner = reasoner;
		eagleiExtendedPatoApp.setAddLegacy(addLegacy);
		eagleiExtendedPatoApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedPatoApp.setReasoned(true);
		eagleiExtendedPatoApp.setUnReasoned(true);
		eagleiExtendedPatoApp.module.importModuleIntoBoth(eagleiExtendedPato.module, null);
		eagleiExtendedPatoApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		//
		GenerateModuleCommand eagleiExtendedUberonApp = new GenerateModuleCommand(getMain());
		eagleiExtendedUberonApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON_APP);
		eagleiExtendedUberonApp.setOutput(new File(outputDirectory, "application-specific-files"));
		eagleiExtendedUberonApp.sourceOntology = isfOntology;
		eagleiExtendedUberonApp.sourceReasoner = reasoner;
		eagleiExtendedUberonApp.setAddLegacy(addLegacy);
		eagleiExtendedUberonApp.setCleanLegacy(cleanLegacy);
		eagleiExtendedUberonApp.setReasoned(true);
		eagleiExtendedUberonApp.setUnReasoned(true);
		eagleiExtendedUberonApp.module.importModuleIntoBoth(eagleiExtendedUberon.module, null);
		eagleiExtendedUberonApp.module.importModuleIntoBoth(eagleiAppDef.module, null);

		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtended.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiApp.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedGoApp.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedMeshApp.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedMpApp.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedPatoApp.module, null);
		eagleiExtendedApp.module.importModuleIntoBoth(eagleiExtendedUberonApp.module, null);

		topModule = eagleiExtendedApp.module;
	}

	public EroCommand(Main main) {
		super(main);
		configure();
	}

	@Override
	public void run() {
		init();
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
				command.topModule.generateModule();
			}
		},

		save {

			@Override
			public void execute(EroCommand command) {
				command.topModule.saveGeneratedModule();
			}
		},
		catalog {

			@Override
			public void execute(EroCommand command) {
				CatalogCommand catalog = new CatalogCommand(command.getMain());
				catalog.setDirectory(command.outputDirectory);
				catalog.run();
			}
		},
		compare {

			@Override
			public void execute(EroCommand command) {
				CompareCommand cc = new CompareCommand(command.getMain());

				cc.setFromIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));
				cc.getFromFiles().add(command.previousDirectory);

				cc.setToIri(IRI.create("http://eagle-i.org/ont/app/1.0/eagle-i-extended-app.owl"));
				cc.getToFiles().add(command.getMain().getJobDirectory());
				cc.setReportPath("ero-diff-with-previous");
				cc.run();
			}
		};

		public abstract void execute(EroCommand command);
	}

}
