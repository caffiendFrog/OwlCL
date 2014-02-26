package isf.command;

import static isf.command.EroCommand.Action.*;
import isf.ISFUtil;
import isf.command.cli.Main;
import isf.module.Module;
import isf.module.ModuleNames;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
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
	OWLOntologyManager man = ISFUtil.getIsfManagerSingleton();
	OWLReasoner reasoner = null;

	public EroCommand(Main main) {
		super(main);

		try {
			isfOntology = ISFUtil.setupAndLoadIsfOntology(man);
		} catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Failed to load ISF ontology due to: ", e);
		}
		reasoner = ISFUtil.getDefaultReasoner(isfOntology);

		GenerateModuleCommand eaglei = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtended = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGo = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMesh = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedPato = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedUberson = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		eagleiExtended.module.addImport(eaglei.module);
		eagleiExtended.module.addImport(eagleiExtendedGo.module);
		eagleiExtended.module.addImport(eagleiExtendedMesh.module);
		eagleiExtended.module.addImport(eagleiExtendedMp.module);
		eagleiExtended.module.addImport(eagleiExtendedPato.module);
		eagleiExtended.module.addImport(eagleiExtendedUberson.module);
		eagleiExtended.module.addImport(eagleiExtendedGo.module);

		GenerateModuleCommand eagleiApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);
		eaglei.module.addImport(eaglei.module);

		GenerateModuleCommand eagleiAppDef = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_APP_DEF);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		eaglei.module.addImport(eagleiAppDef.module);

		GenerateModuleCommand eagleiExtendedApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedGoApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_GO_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMeshApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_MESH_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedMpApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_MP_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedPatoApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_PATO_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

		GenerateModuleCommand eagleiExtendedUberonApp = new GenerateModuleCommand(super.main);
		eaglei.setModuleName(ModuleNames.EAGLEI_EXTENDED_UBERON_APP);
		eaglei.init();
		eaglei.module.setReasoner(reasoner);

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
		for (String action : getAllActions()) {
			switch (Action.valueOf(action.toLowerCase())) {
			case generate:
				generate.execute(this);
				break;
			case cleanlegacy:
				if (cleanLegacy) {
					cleanlegacy.execute(this);
				}
				break;
			case addlegacy:
				if (addLegacy) {
					addlegacy.execute(this);
				}
				break;
			case save:
				save.execute(this);
				break;
			case savelegacy:
				if (cleanLegacy) {
					savelegacy.execute(this);
				}
				break;

			default:
				break;
			}
		}

	}

	@Override
	protected List<String> getDefaultActions() {
		List<String> actions = new ArrayList<String>();
		actions.add(generate.name());
		actions.add(addlegacy.name());
		actions.add(cleanlegacy.name());
		actions.add(savelegacy.name());
		actions.add(save.name());
		return actions;
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
