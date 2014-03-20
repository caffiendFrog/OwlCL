package isf.command;

import isf.command.cli.CanonicalFileConverter;
import isf.module.DefaultModule;
import isf.module.Module;

import java.io.File;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "module",
		commandDescription = "Generate the named module. The module has to be already created.")
public class GenerateModuleCommand extends AbstractCommand {

	private static Logger logger = LoggerFactory.getLogger(GenerateModuleCommand.class
			.getSimpleName());

	// ================================================================================
	// The module name
	// ================================================================================
	public String moduleName = null;
	public boolean moduleNameSet;

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

	// ================================================================================
	// The directory where the module files are located (not the generated
	// files)
	// ================================================================================
	public File directory = null;
	public boolean directorySet;

	@Parameter(names = "-directory", converter = CanonicalFileConverter.class,
			description = "The location where the module defining files.")
	public void setDirectory(File directory) {
		this.directory = directory;
		this.directorySet = true;
	}

	public File getDirectory() {
		return directory;
	}

	// ================================================================================
	// The directory where the module output will go
	// ================================================================================
	public File output = null;
	public boolean outputSet;

	@Parameter(names = "-output", converter = CanonicalFileConverter.class,
			description = "The location where the module output will go.")
	public void setOutput(File directory) {
		this.output = directory;
		this.outputSet = true;
	}

	public File getOutput() {
		return output;
	}

	// ================================================================================
	// do unreasoned
	// ================================================================================
	public boolean unReasoned = false;
	public boolean unReasonedSet = false;

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

	// ================================================================================
	// do reasoned
	// ================================================================================
	public boolean reasoned = false;
	public boolean reasonedSet = false;

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

	// ================================================================================
	// Add legacy
	// ================================================================================
	public boolean addLegacy = false;
	public boolean addLegacySet;

	@Parameter(names = "-addLegacy",
			description = "If this option is set, legacy content will be added.")
	public void setAddLegacy(boolean addLegacy) {
		this.addLegacy = addLegacy;
		this.addLegacySet = true;
	}

	public boolean isAddLegacy() {
		return addLegacy;
	}

	// ================================================================================
	// clean legacy
	// ================================================================================
	public boolean cleanLegacy = false;
	public boolean cleanLegacySet;

	private boolean inited;

	@Parameter(names = "-cleanLegacy",
			description = "If this option is set, legacy content will be cleaned.")
	public void setCleanLegacy(boolean cleanLegacy) {
		this.cleanLegacy = cleanLegacy;
		this.cleanLegacySet = true;
	}

	public boolean isCleanLegacy() {
		return cleanLegacy;
	}

	// ================================================================================
	// Initialization
	// ================================================================================

	@Override
	protected void preConfigure() {
		this.moduleName = "_unnamed";
		if (main.project == null)
		{
			directory = new File(main.outputDirectory, "module/" + moduleName);
			output = new File(main.outputDirectory + "module-output/" + moduleName);
		} else
		{
			directory = new File(main.project, "module/" + moduleName);
			output = new File(main.project, "module-output/" + moduleName);
		}
	}

	@Override
	public void init() {
		if (inited)
		{
			return;
		}

		this.inited = true;

		if (main.project == null)
		{
			if (!directorySet)
			{
				directory = new File(main.outputDirectory, "module/" + moduleName);
			}
			if (!outputSet)
			{
				output = new File(main.outputDirectory + "module-output/" + moduleName);
			}
		} else
		{
			if (!directorySet)
			{
				directory = new File(main.project, "module/" + moduleName);
			}
			if (!outputSet)
			{
				output = new File(main.project, "module-output/" + moduleName);
			}
		}

		output.mkdirs();

		if (sourceOntology != null && sourceReasoner != null)
		{
			module = new DefaultModule(moduleName, directory, sourceOntology, sourceReasoner,
					output, addLegacy, cleanLegacy);
		} else
		{
			module = new DefaultModule(this.moduleName, this.directory,
					main.getSharedBaseManager(), this.output, addLegacy, cleanLegacy);
		}
		module.loadConfiguration();

		if (reasonedSet)
		{
			module.setGenerateInferred(reasoned);
		}

		if (unReasonedSet)
		{
			module.setGenerateInferred(unReasoned);
		}

	}

	// ================================================================================
	// Implementation
	// ================================================================================

	public GenerateModuleCommand(Main main) {
		super(main);
		preConfigure();
	}

	OWLReasoner sourceReasoner;
	OWLOntology sourceOntology;
	Module module = null;

	@Override
	public void run() {
		init();
		module.generateModule();
		module.saveGeneratedModule();
		module.saveModuleConfiguration();

	}

	@Override
	protected void addCommandActions(List<String> actionsList) {

	}

}
