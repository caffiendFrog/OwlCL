package isf.command;

import isf.command.cli.CanonicalFileConverter;
import isf.command.cli.DirectoryExistsValueValidator;
import isf.command.cli.FileListValueValidator;
import isf.command.cli.FileValueExistsValidator;
import isf.util.OntologyFiles;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

// @formatter:off
/**
 * The initialization in this class is as follows: <p>
 * 
 * 1. The working directory is set to "user.dir" by default. Change if needed.<p>
 * 2. The properties are loaded<p>
 *   2.a if there is a isft-main.properties file, in the user's home directory, it is loaded.<p>
 *   2.b if there is a isft-main.properties file, in the working directory, it is loaded 
 *   with 2.a being the parent properties (i.e. defaults that can be overridden in 2.b).<p>
 * 3. Default field values are set (from the properties, or other hard coded defaults).<p>
 * 4. The instance is passed to JCommander for further configuration from the command line.<p>
 * 5. Other commands are given to JCommander
 * 5. init() is called to setup logging and create directories as needed.
 * 6. Find the 
 * 
 * preInit()  to set defaults and prepare for JCommander
 * use JCommander or programatically to further configure
 * init() to do any post configuration actions before running the command.
 * 
 * 
 * @author Shahim Essaid
 * 
 */
// @formatter:on
public class Main {

	public static final String PROGRAM_DESC = "This is the ISF Tools program with various "
			+ "(in development) commands and options.\n";

	public static final String PROJECT_PROPERTY = "project";
	/**
	 * A comma seperated list of files or directories that are considered to be
	 * the ontology files under development. The order does matter. When
	 * resolving and IRI, the files and directories will be searched in order
	 * and the first one found will be used. These files have a higher priority
	 * than the "import files".
	 */
	public static final String ONTOLOGY_FILES_PROPERTY = "ontology.files";
	public static final String ONTOLOGY_SUBS_PROPERTY = "ontology.subs";

	/**
	 * These are the files and directories that will be searched while resolving
	 * IRIs before attempting online resolution, if enabled.
	 */
	public static final String IMPORT_FILES_PROPERTY = "import.files";
	public static final String IMPORT_SUBS_PROPERTY = "import.subs";

	public static final String OUTPUT_DIRECTORY_PROPERTY = "output.directory";

	public static final String JOB_NAME_PROPERTY = "job.name";
	public static final String JOB_QUALIFIER_PROPERTY = "job.qualifier";

	public static final String OFFLINE_PROPERTY = "offline";
	public static final String QUIET_PROPERTY = "quiet";
	public static final String OVERWRITE_PROPERTY = "overwrite";

	public static final String LOG_LEVEL_PROPERTY = "log.level";
	public static final String DETAILS_PROPERTY = "details";

	// ================================================================================
	// Working directory
	// ================================================================================

	/**
	 * This needs to be set before calling preConfigure(). This location might
	 * have property files that configure the main object, and possibly the
	 * specific command. Currently, the tool looks for main configuration in
	 * this directory, in a isft-main.properties file. If there is a similar
	 * file in the user's home directory (as specified by user.home), the home
	 * file will provide default values that can be overridden by the file in
	 * the working directory.
	 * 
	 * It defaults to the current directory as specified by the user.dir system
	 * property.
	 * 
	 * The working directory is not related to the output directory but by
	 * default the output directory will be a sub directory named "isft-output".
	 * This default output location can be overridden.
	 */
	public File workingDirectory = new File(System.getProperty("user.dir"));
	public boolean workingDirectorySet = false;

	@Parameter(names = "-work", description = "The working directory, where a configuration "
			+ "file might be located", converter = CanonicalFileConverter.class,
			validateValueWith = FileValueExistsValidator.class)
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		this.workingDirectorySet = true;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	// ================================================================================
	// Ontology project directory
	// ================================================================================

	public File project = null;
	public boolean projectSet = false;

	public File getProject() {
		return project;
	}

	@Parameter(names = "-project", description = "The location of the ontology project. "
			+ "If this is set, certain commands will will work with files under this "
			+ "project location based on an assumed project file structure.",
			converter = CanonicalFileConverter.class,
			validateValueWith = DirectoryExistsValueValidator.class)
	public void setProject(File project) {
		this.project = project;
		this.projectSet = true;
	}

	// ================================================================================
	// Ontology files and directories
	// ================================================================================

	public List<File> ontologyFiles = new ArrayList<File>();
	public boolean ontologyFilesSet = false;

	@Parameter(names = "-ontologyFiles", converter = CanonicalFileConverter.class,
			validateValueWith = FileListValueValidator.class, description = "")
	public void setOntologyFiles(List<File> ontologyFiles) {
		this.ontologyFiles = ontologyFiles;
		this.ontologyFilesSet = true;
	}

	public List<File> getOntologyFiles() {
		return ontologyFiles;
	}

	// ================================================================================
	// ontology subs?
	// ================================================================================

	public boolean ontologySubs = true;
	public boolean ontologySubsSet = false;

	@Parameter(names = "-ontologySubs", arity = 1,
			description = "If ontology subfolders should be considered. " + "True by default.")
	public void setOntologySubs(boolean ontologySubs) {
		this.ontologySubs = ontologySubs;
		this.ontologySubsSet = true;
	}

	public boolean isOntologySubs() {
		return ontologySubs;
	}

	// ================================================================================
	// Import files.
	// ================================================================================

	public List<File> importFiles = new ArrayList<File>();
	public boolean importFilesSet = false;

	@Parameter(names = "-importFiles", converter = CanonicalFileConverter.class,
			validateValueWith = FileListValueValidator.class, description = "")
	public void setImportFiles(List<File> importFiles) {
		this.importFiles = importFiles;
		this.importFilesSet = true;
	}

	public List<File> getImportFiles() {
		return importFiles;
	}

	// ================================================================================
	// import subs?
	// ================================================================================

	public boolean importSubs = true;
	public boolean importSubsSet = false;

	@Parameter(names = "-importSubs", arity = 1,
			description = "If import subfolders should be considered. " + "True by default.")
	public void setImportSubs(boolean importSubs) {
		this.importSubs = importSubs;
		this.importSubsSet = true;
	}

	public boolean isImportSubs() {
		return importSubs;
	}

	// ================================================================================
	// The output directory location
	// ================================================================================

	public File outputDirectory = null;
	public boolean outputDirectorySet = false;

	@Parameter(names = "-output", converter = CanonicalFileConverter.class,
			description = "The top directory for output.")
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
		this.outputDirectorySet = true;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	// ================================================================================
	// Logging level for this run
	// ================================================================================

	public String logLevel = null;
	public boolean logLevelSet = false;

	@Parameter(names = "-loglevel",
			description = "The logging level. Valid values include warn, info, and debug.")
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
		this.logLevelSet = true;
	}

	public String getLogLevel() {
		return this.logLevel;
	}

	// ================================================================================
	// Detailed reports
	// ================================================================================

	@Parameter(names = "-details", arity = 1,
			description = "If any generated reports should be detailed.")
	public boolean detailedReport = true;

	// ================================================================================
	// A job name that will be prefix the job's folder name.
	// ================================================================================
	public String jobName = null;
	public boolean jobNameSet = false;

	@Parameter(
			names = "-jobName",
			description = "A very short name that will be the prefix "
					+ "of the directory under the output directory. It should not contain any illegal file name "
					+ "characters and should be short. Spaces are excaped with _ but on the commandline, "
					+ "if the comment contains any spaces, it needs to be double quoted.")
	public void setJobName(String jobName) {
		this.jobName = jobName;
		this.jobNameSet = true;
	}

	public String getJobName() {
		return this.jobName;
	}

	// ================================================================================
	// A job name qualifier that will qualify the job's folder name.
	// ================================================================================
	public String jobQualifier = null;
	public boolean jobQualifierSet = false;

	@Parameter(names = "-jobQualifier",
			description = "A job qualifier that will make the job's directory name unique. "
					+ "By default, the qualifier is a timestamp.")
	public void setJobQualifier(String qualifier) {
		this.jobQualifier = qualifier;
		this.jobQualifierSet = true;
	}

	public String getJobQualifier() {
		return this.jobQualifier;
	}

	// ================================================================================
	// Offline mode?
	// ================================================================================

	public boolean offline = true;
	public boolean offlineSet = false;

	@Parameter(
			names = "-offline",
			arity = 1,
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

	@Parameter(
			names = "-quiet",
			arity = 1,
			description = "Suppress default console output. By default "
					+ "warn or higher level logging, and generate reports are alos shown on console.")
	public boolean quiet = false;

	// ================================================================================
	// Overwrite output
	// ================================================================================

	@Parameter(names = "-overwrite", arity = 1,
			description = "Should the job's output directory be overwritten if it exists?")
	public boolean overwrite = false;

	// ================================================================================
	// Initialization
	// ================================================================================

	public Properties configProperties;

	/**
	 * This preconfigures the Main object before JCommander. The working
	 * directory (which should be set if needed) and home directory are used to
	 * look for isft-main.properties as initial configuration. The home file
	 * provides defaults to the working file. If this loaded should be avoided,
	 * set the configProperties field to a custom (possibly empty) instance. If
	 * configProperties is not null, the file loading will be skipped.
	 * <p>
	 * 
	 * The values specified in the property files are used to pre-configure this
	 * instance.
	 */
	public void preConfigure() {

		// working directory is not set here. It has to be set before calling
		// preConfigure()

		// load properties (after the working directory is set if needed.)
		if (configProperties == null)
		{
			Properties homeProperties = getProperties(new File(System.getProperty("user.home")),
					null);
			Properties workingProperties = getProperties(getWorkingDirectory(), homeProperties);

			if (workingProperties != null)
			{
				configProperties = workingProperties;
			} else
			{
				configProperties = new Properties();
			}
		}

		// project directory
		String projectPath = configProperties.getProperty(PROJECT_PROPERTY);
		if (projectPath != null)
		{
			try
			{
				File project = new File(projectPath).getCanonicalFile();
				if (project.isDirectory())
				{
					this.project = project;
				} else
				{
					throw new IllegalStateException("Project directory does not exist. Directory: "
							+ projectPath);
				}
			} catch (IOException e)
			{
				throw new RuntimeException("Failed to get canonical File to project directory + "
						+ projectPath, e);
			}
		}

		// ontology files
		String ontologyFileNames = configProperties.getProperty(ONTOLOGY_FILES_PROPERTY);
		if (ontologyFileNames != null)
		{
			for (String fileName : ontologyFileNames.split(","))
			{
				File file = new File(fileName.trim());
				if (file.exists())
				{
					ontologyFiles.add(file);
				} else
				{
					throw new IllegalStateException("File: " + file
							+ " listed as ontolog file in Main "
							+ "configuration properties but it does not exist.");
				}
			}
		}

		// ontology subs
		String ontologyFileSubs = configProperties.getProperty(ONTOLOGY_SUBS_PROPERTY);
		if (ontologyFileSubs != null)
		{
			ontologySubs = Boolean.valueOf(ontologyFileSubs.trim());
		} else
		{
			ontologySubs = true;
		}

		// import files
		String importFileNames = configProperties.getProperty(IMPORT_FILES_PROPERTY);
		if (importFileNames != null)
		{
			for (String fileName : importFileNames.split(","))
			{
				File file = new File(fileName.trim());
				if (file.exists())
				{
					importFiles.add(file);
				} else
				{
					throw new IllegalStateException("File: " + file
							+ " listed as import file in Main "
							+ "configuration properties but it does not exist.");
				}
			}
		}

		// ontology subs
		String importFileSubs = configProperties.getProperty(IMPORT_SUBS_PROPERTY);
		if (importFileSubs != null)
		{
			importSubs = Boolean.valueOf(importFileSubs.trim());
		} else
		{
			importSubs = true;
		}

		// output directory
		String output = configProperties.getProperty(OUTPUT_DIRECTORY_PROPERTY);
		if (output != null)
		{
			try
			{
				outputDirectory = new File(output).getCanonicalFile();
			} catch (IOException e)
			{
				throw new RuntimeException("Error while creating output directory.", e);
			}
		} else
		{
			outputDirectory = new File(getWorkingDirectory(), "isft-output");
		}

		// job name
		String jobName = configProperties.getProperty(JOB_NAME_PROPERTY);
		if (jobName != null)
		{
			this.jobName = jobName.trim().replace(' ', '_');
		} else
		{
			this.jobName = "_isft_job";
		}

		// job qualifier
		String jobQualifier = configProperties.getProperty(JOB_QUALIFIER_PROPERTY);
		if (jobQualifier != null)
		{
			this.jobQualifier = jobQualifier.trim().replace(' ', '_');
		} else
		{
			SimpleDateFormat df = new SimpleDateFormat("yy.MM.dd-HH.mm.ss");
			this.jobQualifier = df.format(new Date());
		}

		// quiet
		String quiet = configProperties.getProperty(QUIET_PROPERTY);
		if (quiet != null)
		{
			this.quiet = Boolean.valueOf(quiet.trim());
		} else
		{
			this.quiet = false;
		}

		// offline
		String offline = configProperties.getProperty(OFFLINE_PROPERTY);
		if (offline != null)
		{
			this.offline = Boolean.valueOf(offline.trim());
		} else
		{
			this.offline = true;
		}

		// overwrite
		String overwrite = configProperties.getProperty(OVERWRITE_PROPERTY);
		if (overwrite != null)
		{
			this.overwrite = Boolean.valueOf(overwrite.trim());
		} else
		{
			this.overwrite = false;
		}

		// loglevel
		String loglevel = configProperties.getProperty(LOG_LEVEL_PROPERTY);
		if (loglevel != null)
		{
			this.logLevel = loglevel.trim().toLowerCase();
		} else
		{
			this.logLevel = "debug";
		}

	}

	private LoggerContext context;

	/**
	 * To setup logging backend. SLF4J is the API and logback is the backend.
	 */
	public void initWithLogging() {
		context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%r %c %level - %msg%n");
		encoder.start();

		PatternLayoutEncoder encoderDebug = new PatternLayoutEncoder();
		encoderDebug.setContext(context);
		encoderDebug.setPattern("%r %c %level - %msg%n");
		encoderDebug.start();

		FileAppender<ILoggingEvent> appender = new FileAppender<ILoggingEvent>();
		appender.setFile(new File(getJobDirectory(), "log.txt").getAbsolutePath());
		appender.setContext(context);
		appender.setEncoder(encoder);
		appender.addFilter(new Filter<ILoggingEvent>() {

			@Override
			public FilterReply decide(ILoggingEvent event) {
				if (event.getLevel().isGreaterOrEqual(Level.INFO))
				{
					return FilterReply.ACCEPT;
				} else
				{
					return FilterReply.DENY;
				}
			}

		});
		appender.start();

		FileAppender<ILoggingEvent> appenderDebug = new FileAppender<ILoggingEvent>();
		appenderDebug.setFile(new File(getJobDirectory(), "log-debug.txt").getAbsolutePath());
		appenderDebug.setContext(context);
		appenderDebug.setEncoder(encoderDebug);
		appenderDebug.start();

		ch.qos.logback.classic.Logger rootLogger = context
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

		if (quiet)
		{
			rootLogger.detachAppender("console");
		} else
		{
			rootLogger.getAppender("console").addFilter(new Filter<ILoggingEvent>() {

				@Override
				public FilterReply decide(ILoggingEvent event) {
					if (event.getLevel().isGreaterOrEqual(Level.WARN))
					{
						return FilterReply.ACCEPT;
					}
					return FilterReply.DENY;
				}
			});
		}

		rootLogger.addAppender(appender);
		rootLogger.addAppender(appenderDebug);

		if (logLevel.equals("debug"))
		{
			rootLogger.setLevel(Level.DEBUG);
		} else if (logLevel.equals("info"))
		{
			rootLogger.setLevel(Level.INFO);
		} else if (logLevel.equals("warn"))
		{
			rootLogger.setLevel(Level.WARN);
		}

		init();
	}

	Logger logger = null;

	public void init() {
		logger = LoggerFactory.getLogger(this.getClass());
		getJobDirectory().mkdirs();

		logger.info("Started job at " + new Date());
		logger.info("Working directory: " + getWorkingDirectory().getAbsolutePath());
		logger.info("Job's directory: " + getJobDirectory().getAbsolutePath());

		ifiles = new OntologyFiles(importFiles, importSubs);
		logger.info("Use import sub directories: " + importSubs);
		for (File file : importFiles)
		{
			logger.info("Import file: " + file);
		}
		logger.info("Main import count: " + ifiles.getLocalOntologyFiles(null).size());
		for (java.util.Map.Entry<File, IRI> entry : ifiles.getLocalOntologyFiles(null).entrySet())
		{
			logger.debug("\t" + entry.getValue() + "  <--  " + entry.getKey().getAbsolutePath());
		}

		ofiles = new OntologyFiles(ontologyFiles, ontologySubs);
		logger.info("Use ontology sub directories: " + ontologySubs);
		for (File file : ontologyFiles)
		{
			logger.info("Ontology file: " + file);
		}
		logger.info("Main ontology count: " + ofiles.getLocalOntologyFiles(null).size());
		for (java.util.Map.Entry<File, IRI> entry : ofiles.getLocalOntologyFiles(null).entrySet())
		{
			logger.debug("\t" + entry.getValue() + "  <--  " + entry.getKey().getAbsolutePath());
		}

		// ================================================================================
		// Run the command
		// ================================================================================
		String command = jc.getParsedCommand();

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

	// ================================================================================
	// Implementation
	// ================================================================================

	public File getJobDirectory() {
		return new File(getOutputDirectory(), jobName + "_" + jobQualifier);
	}

	private OntologyFiles ifiles = null;
	private OntologyFiles ofiles = null;

	public OWLOntologyManager getNewBaseManager() {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		if (offline)
		{
			man.clearIRIMappers();
		}
		ifiles.setupManager(man, null);
		ofiles.setupManager(man, null);
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

	private Properties getProperties(File directory, Properties parent) {
		File mainPropertiesFile = new File(directory, "isft-main.properties");
		Properties properties = null;

		if (mainPropertiesFile.isFile())
		{
			if (parent != null)
			{
				properties = new Properties(parent);
			} else
			{
				properties = new Properties();
			}
			try
			{
				properties.load(new FileReader(mainPropertiesFile));
				return properties;
			} catch (IOException e)
			{
				throw new RuntimeException(
						"Error while loading isf-tools.properties file from directory: "
								+ directory, e);
			}
		}
		return null;
	}

	JCommander jc = new JCommander();
	NewModuleCommand newModule;
	GenerateModuleCommand module;
	EroCommand ero;
	CatalogCommand catalog;
	ValidateIriCommand validate;
	CompareCommand cc;
	TypecheckCommand typescheck;
	RewriteCommand rw;
	MapperCommand mc;

	public void parseArgs(String[] args) {
		jc.setAllowAbbreviatedOptions(true);
		jc.setCaseSensitiveOptions(false);
		jc.setProgramName("java -jar isf-tools-*.jar");

		jc.addObject(this);

		newModule = new NewModuleCommand(this);
		jc.addCommand("newModule", newModule);

		module = new GenerateModuleCommand(this);
		jc.addCommand("module", module);

		ero = new EroCommand(this);
		jc.addCommand("ero", ero);

		catalog = new CatalogCommand(this);
		jc.addCommand("catalog", catalog);

		validate = new ValidateIriCommand(this);
		jc.addCommand("validate", validate);

		cc = new CompareCommand(this);
		jc.addCommand("compare", cc);

		typescheck = new TypecheckCommand(this);
		jc.addCommand("typecheck", typescheck);

		rw = new RewriteCommand(this);
		jc.addCommand("rewrite", rw);

		mc = new MapperCommand(this);
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

	}

	public static void main(String[] args) {
		Main main = new Main();
		main.preConfigure();
		main.parseArgs(args);
		main.initWithLogging();

	}

}
