package isf.command;

import static isf.command.EroCommand.Action.addlegacy;
import static isf.command.EroCommand.Action.catalog;
import static isf.command.EroCommand.Action.generate;
import static isf.command.EroCommand.Action.save;
import isf.ISFUtil;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.DirectoryExistsValidator;
import isf.command.cli.Main;
import isf.module.Module;
import isf.module.ModuleNames;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
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
			validateWith = DirectoryExistsValidator.class)
	public File previousDirectory;

	// ================================================================================
	// Implementation
	// ================================================================================

	Module topModule = null;

	OWLOntology isfOntology = null;
	OWLOntologyManager man = null;
	OWLReasoner reasoner = null;
	File outputDirectory = null;

	public EroCommand(Main main) {
		super(main);
	}

	private void init() {
		man = ISFUtil.getIsfManagerSingleton();
		outputDirectory = new File(super.main.getOutputDirectory(), "ero-release");

		try
		{
			isfOntology = ISFUtil.setupAndLoadIsfOntology(man);
		} catch (OWLOntologyCreationException e)
		{
			throw new IllegalStateException("Failed to load ISF ontology due to: ", e);
		}
		reasoner = ISFUtil.getDefaultReasoner(isfOntology);

		GenerateModuleCommand eaglei = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI);
		eaglei.outputDirectory = new File(outputDirectory, "core");
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtended = new GenerateModuleCommand(super.main);
		eagleiExtended.setModuleName(ModuleNames.EAGLEI_EXTENDED);
		eagleiExtended.outputDirectory = new File(outputDirectory, "core");
		eagleiExtended.init();
		eagleiExtended.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGo = new GenerateModuleCommand(super.main);
		eagleiExtendedGo.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO);
		eagleiExtendedGo.outputDirectory = new File(outputDirectory, "imports");
		eagleiExtendedGo.init();
		eagleiExtendedGo.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMesh = new GenerateModuleCommand(super.main);
		eagleiExtendedMesh.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH);
		eagleiExtendedMesh.outputDirectory = new File(outputDirectory, "imports");
		eagleiExtendedMesh.init();
		eagleiExtendedMesh.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMp = new GenerateModuleCommand(super.main);
		eagleiExtendedMp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP);
		eagleiExtendedMp.outputDirectory = new File(outputDirectory, "imports");
		eagleiExtendedMp.init();
		eagleiExtendedMp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedPato = new GenerateModuleCommand(super.main);
		eagleiExtendedPato.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO);
		eagleiExtendedPato.outputDirectory = new File(outputDirectory, "imports");
		eagleiExtendedPato.init();
		eagleiExtendedPato.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedUberon = new GenerateModuleCommand(super.main);
		eagleiExtendedUberon.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON);
		eagleiExtendedUberon.outputDirectory = new File(outputDirectory, "imports");
		eagleiExtendedUberon.init();
		eagleiExtendedUberon.module.setReasoner(reasoner);

		eagleiExtended.module.addImport(eaglei.module);
		eagleiExtended.module.addImport(eagleiExtendedGo.module);
		eagleiExtended.module.addImport(eagleiExtendedMesh.module);
		eagleiExtended.module.addImport(eagleiExtendedMp.module);
		eagleiExtended.module.addImport(eagleiExtendedPato.module);
		eagleiExtended.module.addImport(eagleiExtendedUberon.module);
		eagleiExtended.module.addImport(eagleiExtendedGo.module);

		GenerateModuleCommand eagleiApp = new GenerateModuleCommand(super.main);
		eagleiApp.setModuleName(ModuleNames.EAGLEI_APP);
		eagleiApp.outputDirectory = new File(outputDirectory, "application-specific-files");
		eagleiApp.init();
		eagleiApp.module.setReasoner(reasoner);
		eagleiApp.module.addImport(eaglei.module);

		GenerateModuleCommand eagleiAppDef = new GenerateModuleCommand(super.main);
		eagleiAppDef.setModuleName(ModuleNames.EAGLEI_APP_DEF);
		eagleiAppDef.outputDirectory = new File(outputDirectory, "application-specific-files");
		eagleiAppDef.init();
		eagleiAppDef.module.setReasoner(reasoner);

		eagleiApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedApp = new GenerateModuleCommand(super.main);
		eagleiExtendedApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_APP);
		eagleiExtendedApp.outputDirectory = new File(outputDirectory, "application-specific-files");
		eagleiExtendedApp.init();
		eagleiExtendedApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGoApp = new GenerateModuleCommand(super.main);
		eagleiExtendedGoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO_APP);
		eagleiExtendedGoApp.outputDirectory = new File(outputDirectory,
				"application-specific-files");
		eagleiExtendedGoApp.init();
		eagleiExtendedGoApp.module.setReasoner(reasoner);
		eagleiExtendedGoApp.module.addImport(eagleiExtendedGo.module);
		eagleiExtendedGoApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedMeshApp = new GenerateModuleCommand(super.main);
		eagleiExtendedMeshApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH_APP);
		eagleiExtendedMeshApp.outputDirectory = new File(outputDirectory,
				"application-specific-files");
		eagleiExtendedMeshApp.init();
		eagleiExtendedMeshApp.module.setReasoner(reasoner);
		eagleiExtendedMeshApp.module.addImport(eagleiExtendedMesh.module);
		eagleiExtendedMeshApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedMpApp = new GenerateModuleCommand(super.main);
		eagleiExtendedMpApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP_APP);
		eagleiExtendedMpApp.outputDirectory = new File(outputDirectory,
				"application-specific-files");
		eagleiExtendedMpApp.init();
		eagleiExtendedMpApp.module.setReasoner(reasoner);
		eagleiExtendedMpApp.module.addImport(eagleiExtendedMp.module);
		eagleiExtendedMpApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedPatoApp = new GenerateModuleCommand(super.main);
		eagleiExtendedPatoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO_APP);
		eagleiExtendedPatoApp.outputDirectory = new File(outputDirectory,
				"application-specific-files");
		eagleiExtendedPatoApp.init();
		eagleiExtendedPatoApp.module.setReasoner(reasoner);
		eagleiExtendedPatoApp.module.addImport(eagleiExtendedPato.module);
		eagleiExtendedPatoApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedUberonApp = new GenerateModuleCommand(super.main);
		eagleiExtendedUberonApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON_APP);
		eagleiExtendedUberonApp.outputDirectory = new File(outputDirectory,
				"application-specific-files");
		eagleiExtendedUberonApp.init();
		eagleiExtendedUberonApp.module.setReasoner(reasoner);
		eagleiExtendedUberonApp.module.addImport(eagleiExtendedUberon.module);
		eagleiExtendedUberonApp.module.addImport(eagleiAppDef.module);

		eagleiExtendedApp.module.addImport(eagleiExtended.module);
		eagleiExtendedApp.module.addImport(eagleiApp.module);
		eagleiExtendedApp.module.addImport(eagleiExtendedGoApp.module);
		eagleiExtendedApp.module.addImport(eagleiExtendedMeshApp.module);
		eagleiExtendedApp.module.addImport(eagleiExtendedMpApp.module);
		eagleiExtendedApp.module.addImport(eagleiExtendedPatoApp.module);
		eagleiExtendedApp.module.addImport(eagleiExtendedUberonApp.module);

		topModule = eagleiExtendedApp.module;
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
		actionsList.add(addlegacy.name());
		// actionsList.add(cleanlegacy.name());
		// actionsList.add(savelegacy.name());
		actionsList.add(save.name());
		actionsList.add(catalog.name());
		if (previousDirectory != null)
		{
			actionsList.add(Action.compare.name());
		}
	}

	enum Action {
		generate {

			@Override
			public void execute(EroCommand command) {
				try
				{
					command.topModule.generateModuleTransitive();
				} catch (Exception e)
				{
					throw new RuntimeException("Error generating ero module in command", e);
				}
			}
		},
		addlegacy {

			@Override
			public void execute(EroCommand command) {
				command.topModule.addLegacyOntologiesTransitive();
			}
		},
		cleanlegacy {

			@Override
			public void execute(EroCommand command) {
				command.topModule.cleanLegacyOntologiesTransitive();

			}
		},
		savelegacy {

			@Override
			public void execute(EroCommand command) {
				try
				{
					command.topModule.saveLegacyOntologiesTransitive();
				} catch (OWLOntologyStorageException e)
				{
					throw new RuntimeException("Error saving legacy ontologies in ero command.", e);
				}
			}
		},
		save {

			@Override
			public void execute(EroCommand command) {
				try
				{
					command.topModule.saveModuleTransitive();
				} catch (OWLOntologyStorageException e)
				{
					throw new RuntimeException("Error saving ero module in command.", e);
				}
			}
		},
		catalog {

			@Override
			public void execute(EroCommand command) {
				// TODO Auto-generated method stub
				CatalogCommand catalog = new CatalogCommand(command.main);
				catalog.setDirectory(command.outputDirectory.getAbsolutePath());
				catalog.run();
			}
		},
		compare {

			@Override
			public void execute(EroCommand command) {
				CompareCommand cc = new CompareCommand(command.main);

				cc.fromFiles.add(command.previousDirectory);
				cc.toFiles.add(command.outputDirectory);
				cc.reportPath = "ero-diff-with-last-original.txt";
				cc.run();
			}
		};

		public abstract void execute(EroCommand command);
	}

}
