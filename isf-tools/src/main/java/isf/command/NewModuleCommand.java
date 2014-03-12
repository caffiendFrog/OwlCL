package isf.command;

import static isf.command.NewModuleCommand.Action.create;
import isf.ISF;
import isf.ISFUtil;
import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.IriConverter;
import isf.command.cli.Main;
import isf.module.SimpleModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "newModule", commandDescription = "The command to create a new module.")
public class NewModuleCommand extends AbstractCommand {

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
		if (!iriSet) {
			// don't use the setter.
			iri = getDefaultIri();
		}
	}

	public String getModuleName() {
		return moduleName;
	}

	// ================================================================================
	// The source IRIs for the module, ISF by default
	// ================================================================================
	public List<IRI> sourceIris = getDefaultSources();
	public boolean sourceIrisSet;

	@Parameter(
			names = "-sourceIris",
			converter = IriConverter.class,
			description = "The source IRIs that will be used for this module. The OWL documents for the "
					+ "IRIs will first be looked for under the ISF trunk/src/ontology directory and then "
					+ "an attempt will be made to resolve online.")
	public void setSourceIris(List<IRI> sourceIris) {
		this.sourceIris = sourceIris;
		this.sourceIrisSet = true;
	}

	public List<IRI> getSourceIris() {
		return sourceIris;
	}

	private List<IRI> getDefaultSources() {
		List<IRI> iris = new ArrayList<IRI>();
		iris.add(ISF.ISF_DEV_IRI);
		return iris;
	}

	// ================================================================================
	// The final IRI of the module
	// ================================================================================
	public IRI iri = getDefaultIri();
	public boolean iriSet;

	@Parameter(names = "-iri", description = "The generated module's IRI",
			converter = IriConverter.class)
	public void setIri(IRI iri) {
		this.iri = iri;
		this.iriSet = true;
	}

	public IRI getIri() {
		return iri;
	}

	private IRI getDefaultIri() {
		return IRI.create("http://isf-tools/default-" + getModuleName() + "-module.owl");
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

	SimpleModule module = null;

	public NewModuleCommand(Main main) {
		super(main);
	}

	@Override
	public void run() {
		module = new SimpleModule(moduleName, ISFUtil.getIsfManagerSingleton(), directory,
				new File(ISFUtil.getGeneratedDirectory(), "module/" + moduleName));

		for (String action : getAllActions()) {
			Action.valueOf(action).execute(this);
		}
	}

	@Override
	protected List<String> getCommandActions(List<String> actionsList) {
		actionsList.add(Action.create.name());
		return actionsList;
	}

	public enum Action {
		create {
			@Override
			public void execute(NewModuleCommand command) {
				log.debug("Running newModule create action.");
				command.module.create(command.iri, command.sourceIris, command.legacy);
			}
		};

		Logger log = LoggerFactory.getLogger(Action.class);

		public abstract void execute(NewModuleCommand command);
	}

}
