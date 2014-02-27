package isf.command;

import static isf.command.EroCommand.Action.*;
import isf.ISFUtil;
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

	@Parameter(names = "-cleanLegacy",
			description = "Will clean the legacy ERO files from any axioms the module is "
					+ "generating. To enable this, simply add the option without a value "
					+ "(i.e. -cleanLegacy without \"true\" as an option value)")
	boolean cleanLegacy = false;

	@Parameter(names = "-addLegacy",
			description = "Will add the legacy ERO content to the generated module. Use "
					+ "the option with a value (i.e. -addLegacy false) to disable this.", arity = 1)
	boolean addLegacy = true;

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

		try {
			isfOntology = ISFUtil.setupAndLoadIsfOntology(man);
		} catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Failed to load ISF ontology due to: ", e);
		}
		reasoner = ISFUtil.getDefaultReasoner(isfOntology);

		GenerateModuleCommand eaglei = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI);
		eaglei.outputDirectory = outputDirectory;
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtended = new GenerateModuleCommand(super.main);
		eagleiExtended.setModuleName(ModuleNames.EAGLEI_EXTENDED);
		eagleiExtended.outputDirectory = outputDirectory;
		eagleiExtended.init();
		eagleiExtended.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGo = new GenerateModuleCommand(super.main);
		eagleiExtendedGo.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO);
		eagleiExtendedGo.outputDirectory = outputDirectory;
		eagleiExtendedGo.init();
		eagleiExtendedGo.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMesh = new GenerateModuleCommand(super.main);
		eagleiExtendedMesh.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH);
		eagleiExtendedMesh.outputDirectory = outputDirectory;
		eagleiExtendedMesh.init();
		eagleiExtendedMesh.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMp = new GenerateModuleCommand(super.main);
		eagleiExtendedMp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP);
		eagleiExtendedMp.outputDirectory = outputDirectory;
		eagleiExtendedMp.init();
		eagleiExtendedMp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedPato = new GenerateModuleCommand(super.main);
		eagleiExtendedPato.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO);
		eagleiExtendedPato.outputDirectory = outputDirectory;
		eagleiExtendedPato.init();
		eagleiExtendedPato.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedUberon = new GenerateModuleCommand(super.main);
		eagleiExtendedUberon.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON);
		eagleiExtendedUberon.outputDirectory = outputDirectory;
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
		eagleiApp.outputDirectory = outputDirectory;
		eagleiApp.init();
		eagleiApp.module.setReasoner(reasoner);
		eagleiApp.module.addImport(eaglei.module);

		GenerateModuleCommand eagleiAppDef = new GenerateModuleCommand(super.main);
		eagleiAppDef.setModuleName(ModuleNames.EAGLEI_APP_DEF);
		eagleiAppDef.outputDirectory = outputDirectory;
		eagleiAppDef.init();
		eagleiAppDef.module.setReasoner(reasoner);

		eagleiApp.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedApp = new GenerateModuleCommand(super.main);
		eagleiExtendedApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_APP);
		eagleiExtendedApp.outputDirectory = outputDirectory;
		eagleiExtendedApp.init();
		eagleiExtendedApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGoApp = new GenerateModuleCommand(super.main);
		eagleiExtendedGoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO_APP);
		eagleiExtendedGoApp.outputDirectory = outputDirectory;
		eagleiExtendedGoApp.init();
		eagleiExtendedGoApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMeshApp = new GenerateModuleCommand(super.main);
		eagleiExtendedMeshApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH_APP);
		eagleiExtendedMeshApp.outputDirectory = outputDirectory;
		eagleiExtendedMeshApp.init();
		eagleiExtendedMeshApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMpApp = new GenerateModuleCommand(super.main);
		eagleiExtendedMpApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP_APP);
		eagleiExtendedMpApp.outputDirectory = outputDirectory;
		eagleiExtendedMpApp.init();
		eagleiExtendedMpApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedPatoApp = new GenerateModuleCommand(super.main);
		eagleiExtendedPatoApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO_APP);
		eagleiExtendedPatoApp.outputDirectory = outputDirectory;
		eagleiExtendedPatoApp.init();
		eagleiExtendedPatoApp.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedUberonApp = new GenerateModuleCommand(super.main);
		eagleiExtendedUberonApp.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON_APP);
		eagleiExtendedUberonApp.outputDirectory = outputDirectory;
		eagleiExtendedUberonApp.init();
		eagleiExtendedUberonApp.module.setReasoner(reasoner);

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
		for (String action : getAllActions()) {
			Action.valueOf(action.toLowerCase()).execute(this);
		}
	}

	@Override
	protected List<String> getDefaultActions(List<String> actionsList) {
		actionsList.add(generate.name());
		actionsList.add(addlegacy.name());
		actionsList.add(cleanlegacy.name());
		actionsList.add(savelegacy.name());
		actionsList.add(save.name());
		return actionsList;
	}

	enum Action {
		generate {
			@Override
			public void execute(EroCommand command) {
				try {
					command.topModule.generateModuleTransitive();
				} catch (Exception e) {
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
				try {
					command.topModule.saveLegacyOntologiesTransitive();
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException("Error saving legacy ontologies in ero command.", e);
				}
			}
		},
		save {
			@Override
			public void execute(EroCommand command) {
				try {
					command.topModule.saveGeneratedModuleTransitive();
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException("Error saving ero module in command.", e);
				}
			}
		};

		public abstract void execute(EroCommand command);
	}

}
