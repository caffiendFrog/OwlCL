package isf.command;

import static isf.command.GenerateModuleCommand.Action.*;
import isf.ISFUtil;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.Main;
import isf.module.SimpleModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "module",
		commandDescription = "Generate the named module. The module has to be already created.")
public class GenerateModuleCommand extends AbstractCommand {

	private static Logger log = LoggerFactory
			.getLogger(GenerateModuleCommand.class.getSimpleName());

	// ================================================================================
	// The module name
	// ================================================================================
	public String moduleName = "_default-new-module";
	public boolean moduleNameSet;

	@Parameter(
			names = "-name",
			description = "The module name. This will be used to create default IRIs, files, and folders.")
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
		this.moduleNameSet = true;
		if (!directorySet) {
			// don't use the setter.
			directory = getDefaultDirectory();
		}
	}

	public String getModuleName() {
		return moduleName;
	}

	// ================================================================================
	// The directory where the module files are located (not the generated
	// files)
	// ================================================================================
	public File directory = getDefaultDirectory();
	public boolean directorySet;

	@Parameter(names = "-directory", converter = CanonicalFileConverter.class,
			description = "The location where the module defining files will be created"
					+ " (not the output).")
	public void setDirectory(File directory) {
		this.directory = directory;
		this.directorySet = true;
	}

	public File getDirectory() {
		return directory;
	}

	private File getDefaultDirectory() {
		return new File(ISFUtil.getDefaultModuleDirectory(), getModuleName());
	}

	// ================================================================================
	// The legacy indicator
	// ================================================================================
	public boolean legacy = false;
	public boolean legacySet;

	@Parameter(
			names = "-legacy",
			description = "If this option is set, legacy mode will be used to execute or "
					+ "skip legacy related actions even if they are specified in the *action options.")
	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
		this.legacySet = true;
	}

	public boolean isLegacy() {
		return legacy;
	}

	// ================================================================================
	// Implementation
	// ================================================================================
	public SimpleModule module = null;

	public GenerateModuleCommand(Main main) {
		super(main);

		outputDirectory = new File(main.getOutputDirectory(), "module/" + moduleName);
	}

	public File outputDirectory;

	public void init() {
		module = new SimpleModule(moduleName, ISFUtil.getIsfManagerSingleton(), directory,
				outputDirectory);
	}

	@Override
	public void run() {

		for (String action : getAllActions()) {
			Action.valueOf(action).execute(this);
		}
	}

	@Override
	protected List<String> getDefaultActions(List<String> actionsList) {
		actionsList.add(Action.load.name());
		actionsList.add(Action.generate.name());
		actionsList.add(Action.cleanLegacy.name());
		actionsList.add(Action.addLegacy.name());
		actionsList.add(Action.saveLegacy.name());
		actionsList.add(Action.save.name());
		return actionsList;
	}

	enum Action {
		load {
			@Override
			public void execute(GenerateModuleCommand command) {
				command.module.load();

			}
		},
		generate {
			@Override
			public void execute(GenerateModuleCommand command) {
				try {
					command.module.generateModule();
				} catch (Exception e) {
					// TODO fix this exception in the module implementation
					throw new RuntimeException("Error caught while generating module in command", e);
				}
			}
		},
		cleanLegacy {
			@Override
			public void execute(GenerateModuleCommand command) {
				command.module.cleanLegacyOntologies();

			}
		},
		addLegacy {
			@Override
			public void execute(GenerateModuleCommand command) {
				command.module.addLegacyOntologies();

			}
		},
		save {
			@Override
			public void execute(GenerateModuleCommand command) {
				try {
					command.module.saveGeneratedModule();
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException("Error caught while saving module in command.", e);
				}

			}
		},
		saveLegacy {
			@Override
			public void execute(GenerateModuleCommand command) {
				try {
					command.module.saveLegacyOntologies();
				} catch (OWLOntologyStorageException e) {
					throw new RuntimeException(
							"Error caught while saving module legacy ontologies in command.", e);
				}

			}
		};

		public abstract void execute(GenerateModuleCommand command);
	}

}
