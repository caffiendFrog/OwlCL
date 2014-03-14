package isf.command.cli;

import isf.ISFUtil;
import isf.command.CatalogCommand;
import isf.command.CompareCommand;
import isf.command.EroCommand;
import isf.command.GenerateModuleCommand;
import isf.command.MapperCommand;
import isf.command.NewModuleCommand;
import isf.command.RewriteCommand;
import isf.command.TypecheckCommand;
import isf.command.ValidateIriCommand;
import isf.util.OntologyFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Main {

	private static final String PROGRAM_DESC = "This is the ISF Tools program with various "
			+ "(in development) commands and options.\n";

	// ================================================================================
	// The location of ISF trunk directory if not specified in some other way.
	// ================================================================================
	@Parameter(
			names = "-trunk",
			validateWith = CreateDirectoryValidator.class,
			converter = CanonicalFileConverter.class,
			description = "The directory location of the ISF SVN trunk directory (Git wroking tree) if not"
					+ "specified in some other way (system property, evn, or isf.properties file).")
	public void setISFTrunkDirecotry(File isfTrunkDirectory) {
		ISFUtil.setISFTrunkDirecotry(isfTrunkDirectory);
	}

	public File getISFTrunkDirecotry() {
		return ISFUtil.ISF_TRUNK_DIR;
	}

	// ================================================================================
	// The general/top output directory location
	// ================================================================================
	@Parameter(names = "-output", validateWith = CreateDirectoryValidator.class,
			converter = CanonicalFileConverter.class,
			description = "The top directory for output. By default it will be \"/generated\" "
					+ "under the trunk directory.")
	public void setOutputDirectory(File outputDirectory) {
		ISFUtil.setGeneratedDirectory(outputDirectory);
	}

	public File getOutputDirectory() {
		return ISFUtil.getGeneratedDirectory();
	}

	// ================================================================================
	// Dated output folders.
	// ================================================================================
	@Parameter(names = "-datedOutput", description = "Whether or not to create dated_time "
			+ "sub directories in the output directory to avoid over writing previous results.",
			arity = 1)
	public void setDatedOutput(boolean datedOutput) {
		ISFUtil.datedGenerated = datedOutput;
		// clear the directory so that it is recalculated.
		ISFUtil.setGeneratedDirectory(null);
	}

	public boolean getDatedOutput() {
		return ISFUtil.datedGenerated;
	}

	// ================================================================================
	// Logging level for this run
	// ================================================================================
	@Parameter(names = "-loglevel",
			description = "The logging level. Valid values include warn, info, and debug.")
	public void setLogLevel(String logLevel) {
		ISFUtil.setLoggingLevel(logLevel);
	}

	public String getLogLevel() {
		return ISFUtil.getLoggingLevel();
	}

	// ================================================================================
	// Detailed reports
	// ================================================================================

	@Parameter(names = "-detailedReport",
			description = "If any generated reports should be detailed.")
	public boolean detailedReport = false;

	// ================================================================================
	// A comment that will be appended to the output folder name.
	// ================================================================================
	@Parameter(names = "-comment", description = "A very short comment that will be the suffix "
			+ "of the generated directory. It should not contain any illegal file name "
			+ "characters and should be short. Spaces are excaped with _ but on the commandline "
			+ "if the comment contains any spaces, it needs to be double quoted.")
	public void setComment(String comment) {
		ISFUtil.suffixString = comment.replace(' ', '_');
		ISFUtil.setGeneratedDirectory(null);
	}

	public String getComment() {
		return ISFUtil.suffixString;
	}

	// ================================================================================
	//
	// ================================================================================
	@Parameter(
			names = "-localOwlFiles",
			description = "These files and directories will be used as the base files "
					+ "to resolve OWL imports for certain commands. Certain commands might need to operate on a different"
					+ "set of files/ontologies but the command still needs ontologies resolved from other unrelated directories "
					+ " These files and directories will be added to the bottom of IRI mappers in the manager just "
					+ "before the default IRI mapper that causes online resolution of imports. Add an ontology to this "
					+ "list of files to override online resolution.",
			converter = CanonicalFileConverter.class)
	public List<File> localOwlFiles = new ArrayList<File>();

	// ================================================================================
	//
	// ================================================================================
	@Parameter(names = "-subLocalOwlFiles", arity = 1,
			description = "If subdirectories of -localOwlFiles should be used. "
					+ "Defaults to true.")
	public boolean subLocalOwlFiles = true;

	// ================================================================================
	// Offline mode?
	// ================================================================================

	public boolean offline = true;
	public boolean offlineSet = false;

	@Parameter(
			names = "-offline",
			description = "Avoids loading ontologies online for the base/main "
					+ "ontology manager. Specific commands might or might not use the base/main manager.")
	public void setOffline(boolean offline) {
		this.offline = offline;
		this.offlineSet = true;
	}

	public boolean isOffline() {
		return offline;
	}

	// ================================================================================
	// Quiet?
	// ================================================================================

	@Parameter(names = "-quiet", description = "Suppress default console output. By default "
			+ "warn or higher level logging, and generate reports are alos shown on console.")
	public boolean quiet = false;

	// ================================================================================
	// Implementation
	// ================================================================================

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public OWLOntologyManager getNewBaseManager() {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		if (offline)
		{
			man.clearIRIMappers();
		}

		OntologyFiles of = new OntologyFiles(localOwlFiles, subLocalOwlFiles);

		Map<File, Exception> exceptions = new HashMap<File, Exception>();
		of.setupManager(man, exceptions);

		for (Entry<File, Exception> e : exceptions.entrySet())
		{
			logger.warn("Some error while discovering ontologies/owl files. File " + e.getKey(),
					e.getValue());
		}

		return man;
	}

	private OWLOntologyManager sharedBaseManager = null;

	public OWLOntologyManager getSharedBaseManager() {
		if (sharedBaseManager == null)
		{
			sharedBaseManager = getNewBaseManager();
		}
		return sharedBaseManager;
	}

	public OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager man) {
		return ISFUtil.getOrLoadOntology(iri, man);
	}

	private void run(String[] args) {

		JCommander jc = new JCommander();
		jc.setAllowAbbreviatedOptions(true);
		jc.setCaseSensitiveOptions(false);
		jc.setProgramName("java -jar isf-tools-*.jar");

		Main main = new Main();
		jc.addObject(main);

		EroCommand ero = new EroCommand(main);
		jc.addCommand("ero", ero);

		NewModuleCommand newModule = new NewModuleCommand(main);
		jc.addCommand("newModule", newModule);

		GenerateModuleCommand module = new GenerateModuleCommand(main);
		jc.addCommand("module", module);

		CatalogCommand catalog = new CatalogCommand(main);
		jc.addCommand("catalog", catalog);

		ValidateIriCommand validate = new ValidateIriCommand(main);
		jc.addCommand("validate", validate);

		CompareCommand cc = new CompareCommand(main);
		jc.addCommand("compare", cc);

		TypecheckCommand typescheck = new TypecheckCommand(main);
		jc.addCommand("typecheck", typescheck);

		RewriteCommand rw = new RewriteCommand(main);
		jc.addCommand("rewrite", rw);

		MapperCommand mc = new MapperCommand(this);
		jc.addCommand("map", mc);

		if (args.length == 0)
		{
			System.out.println(PROGRAM_DESC);
			jc.usage();
			return;
		}

		try
		{
			jc.parse(args);
		} catch (ParameterException e)
		{
			System.err.println(e.getMessage());
			System.out.println(PROGRAM_DESC);
			jc.usage();
		}

		String command = jc.getParsedCommand();

		ISFUtil.init();
		ISFUtil.setLoggingLevel("info");

		if (command.equalsIgnoreCase("newModule"))
		{
			newModule.run();
		} else if (command.equalsIgnoreCase("module"))
		{
			module.run();
		} else if (command.equalsIgnoreCase("ero"))
		{
			ero.run();
		} else if (command.equalsIgnoreCase("catalog"))
		{
			catalog.run();
		} else if (command.equalsIgnoreCase("validate"))
		{
			validate.run();
		} else if (command.equalsIgnoreCase("compare"))
		{
			cc.run();
		} else if (command.equalsIgnoreCase("typecheck"))
		{
			typescheck.run();
		} else if (command.equalsIgnoreCase("rewrite"))
		{
			rw.run();
		} else if (command.equalsIgnoreCase("map"))
		{
			mc.run();
		}

	}

	public static void main(String[] args) {
		new Main().run(args);
	}

}
