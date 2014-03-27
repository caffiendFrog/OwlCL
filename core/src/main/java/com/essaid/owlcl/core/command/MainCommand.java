package com.essaid.owlcl.core.command;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
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

import com.beust.jcommander.Parameter;
import com.essaid.owlcl.command.cli.CanonicalFileConverter;
import com.essaid.owlcl.command.cli.DirectoryExistsValueValidator;
import com.essaid.owlcl.command.cli.FileListValueValidator;
import com.essaid.owlcl.command.cli.FileValueExistsValidator;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.util.IOwlclManager;
import com.essaid.owlcl.util.OntologyFiles;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
public class MainCommand extends OwlclCommand {

  public static final String PROGRAM_DESC = "This is the ISF Tools program with various "
      + "(in development) commands and options.\n";

  public static final String PROJECT_PROPERTY = "project";
  /**
   * A comma seperated list of files or directories that are considered to be
   * the ontology files under development. The order does matter. When resolving
   * and IRI, the files and directories will be searched in order and the first
   * one found will be used. These files have a higher priority than the
   * "import files".
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
   * specific command. Currently, the tool looks for main configuration in this
   * directory, in a isft-main.properties file. If there is a similar file in
   * the user's home directory (as specified by user.home), the home file will
   * provide default values that can be overridden by the file in the working
   * directory.
   * 
   * It defaults to the current directory as specified by the user.dir system
   * property.
   * 
   * The working directory is not related to the output directory but by default
   * the output directory will be a sub directory named "isft-output". This
   * default output location can be overridden.
   */
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

  public boolean isWorkingDirectorySet() {
    return workingDirectorySet;
  }

  private File workingDirectory = null;
  private boolean workingDirectorySet = false;

  // ================================================================================
  // Ontology project directory
  // ================================================================================

  @Parameter(names = "-project", description = "The location of the ontology project. "
      + "If this is set, certain commands will will work with files under this "
      + "project location based on an assumed project file structure.",
      converter = CanonicalFileConverter.class,
      validateValueWith = DirectoryExistsValueValidator.class)
  public void setProject(File project) {
    this.project = project;
    this.projectSet = true;
  }

  public File getProject() {
    return project;
  }

  public boolean isProjectSet() {
    return projectSet;
  }

  private File project = null;
  private boolean projectSet = false;

  // ================================================================================
  // Ontology files and directories
  // ================================================================================

  @Parameter(names = "-ontologyFiles", converter = CanonicalFileConverter.class,
      validateValueWith = FileListValueValidator.class, description = "")
  public void setOntologyFiles(List<File> ontologyFiles) {
    this.ontologyFiles = ontologyFiles;
    this.ontologyFilesSet = true;
  }

  public List<File> getOntologyFiles() {
    return ontologyFiles;
  }

  public boolean isOntologyFilesSet() {
    return ontologyFilesSet;
  }

  private List<File> ontologyFiles = null;
  private boolean ontologyFilesSet = false;

  // ================================================================================
  // ontology subs?
  // ================================================================================

  @Parameter(names = "-ontologySubs", arity = 1,
      description = "If ontology subfolders should be considered. " + "True by default.")
  public void setOntologySubs(boolean ontologySubs) {
    this.ontologySubs = ontologySubs;
    this.ontologySubsSet = true;
  }

  public boolean isOntologySubs() {
    return ontologySubs;
  }

  public boolean isOntologySubsSet() {
    return ontologySubsSet;
  }

  private boolean ontologySubs = true;
  private boolean ontologySubsSet = false;

  // ================================================================================
  // Import files.
  // ================================================================================

  @Parameter(names = "-importFiles", converter = CanonicalFileConverter.class,
      validateValueWith = FileListValueValidator.class, description = "")
  public void setImportFiles(List<File> importFiles) {
    this.importFiles = importFiles;
    this.importFilesSet = true;
  }

  public List<File> getImportFiles() {
    return importFiles;
  }

  public boolean isImportFilesSet() {
    return importFilesSet;
  }

  private List<File> importFiles = null;
  private boolean importFilesSet = false;

  // ================================================================================
  // import subs?
  // ================================================================================

  @Parameter(names = "-importSubs", arity = 1,
      description = "If import subfolders should be considered. " + "True by default.")
  public void setImportSubs(boolean importSubs) {
    this.importSubs = importSubs;
    this.importSubsSet = true;
  }

  public boolean isImportSubs() {
    return importSubs;
  }

  public boolean isImportSubsSet() {
    return importSubsSet;
  }

  private boolean importSubs = true;
  private boolean importSubsSet = false;

  // ================================================================================
  // The output directory location
  // ================================================================================

  @Parameter(names = "-output", converter = CanonicalFileConverter.class,
      description = "The top directory for output.")
  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
    this.outputDirectorySet = true;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isOutputDirectorySet() {
    return outputDirectorySet;
  }

  private File outputDirectory = null;
  private boolean outputDirectorySet = false;

  // ================================================================================
  // Logging level for this run
  // ================================================================================

  @Parameter(names = "-loglevel",
      description = "The logging level. Valid values include warn, info, and debug.")
  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
    this.logLevelSet = true;
  }

  public String getLogLevel() {
    return this.logLevel;
  }

  public boolean isLogLevelSet() {
    return logLevelSet;
  }

  private String logLevel = null;
  private boolean logLevelSet = false;

  // ================================================================================
  // Detailed reports
  // ================================================================================

  @Parameter(names = "-details", arity = 1,
      description = "If any generated reports should be detailed.")
  public void setDetailedReport(boolean detailedReport) {
    this.detailedReport = detailedReport;
    this.detailedReportSet = true;
  }

  public boolean isDetailedReport() {
    return detailedReport;
  }

  public boolean isDetailedReportSet() {
    return detailedReportSet;
  }

  private boolean detailedReport = true;
  private boolean detailedReportSet = false;

  // ================================================================================
  // A job name that will be prefix the job's folder name.
  // ================================================================================

  @Parameter(names = "-jobName", description = "A very short name that will be the prefix "
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

  public boolean isJobNameSet() {
    return jobNameSet;
  }

  private String jobName = null;
  private boolean jobNameSet = false;

  // ================================================================================
  // A job name qualifier that will qualify the job's folder name.
  // ================================================================================

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

  public boolean isJobQualifierSet() {
    return jobQualifierSet;
  }

  private String jobQualifier = null;
  private boolean jobQualifierSet = false;

  // ================================================================================
  // Offline mode?
  // ================================================================================

  @Parameter(names = "-offline", arity = 1,
      description = "Avoids loading ontologies online for the base/main "
          + "ontology manager. Specific commands might or might not use the base/main manager.")
  public void setOffline(boolean offline) {
    this.offline = offline;
    this.offlineSet = true;
  }

  public boolean isOffline() {
    return offline;
  }

  public boolean isOfflineSet() {
    return offlineSet;
  }

  private boolean offline = true;
  private boolean offlineSet = false;

  // ================================================================================
  // Quiet?
  // ================================================================================

  @Parameter(names = "-quiet", arity = 1,
      description = "Suppress default console output. By default "
          + "warn or higher level logging, and generate reports are alos shown on console.")
  public void setQuiet(boolean quiet) {
    this.quiet = quiet;
    this.quietSet = true;
  }

  public boolean isQuiet() {
    return quiet;
  }

  public boolean isQuietSet() {
    return quietSet;
  }

  private boolean quiet = false;
  private boolean quietSet = false;

  // ================================================================================
  // Overwrite output
  // ================================================================================

  @Parameter(names = "-overwrite", arity = 1,
      description = "Should the job's output directory be overwritten if it exists?")
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public boolean isOverwriteSet() {
    return overwriteSet;
  }

  public boolean overwrite = false;
  private boolean overwriteSet = false;

  // ================================================================================
  // Initialization
  // ================================================================================

  private Properties configProperties;

  /**
   * This preconfigures the Main object before JCommander. The working directory
   * (which should be set if needed) and home directory are used to look for
   * isft-main.properties as initial configuration. The home file provides
   * defaults to the working file. If this loaded should be avoided, set the
   * configProperties field to a custom (possibly empty) instance. If
   * configProperties is not null, the file loading will be skipped.
   * <p>
   * 
   * The values specified in the property files are used to pre-configure this
   * instance.
   */
  public void configure() {

    if (!workingDirectorySet)
    {
      workingDirectory = mananger.getWorkDirectory();
    }

    // load properties (after the working directory is set if needed.)
    if (configProperties == null || workingDirectorySet)
    {
      Properties homeProperties = getProperties(new File(System.getProperty("user.home")), null);
      Properties workingProperties = getProperties(getWorkingDirectory(), homeProperties);

      if (workingProperties != null)
      {
        configProperties = workingProperties;
      } else
      {
        configProperties = new Properties();
      }
    }

    // job name
    if (!jobNameSet)
    {
      String jobName = configProperties.getProperty(JOB_NAME_PROPERTY);
      if (jobName != null)
      {
        this.jobName = jobName.trim().replace(' ', '_');
      } else
      {
        this.jobName = "_isft_job";
      }
    }

    // job qualifier
    if (!jobQualifierSet)
    {
      String jobQualifier = configProperties.getProperty(JOB_QUALIFIER_PROPERTY);
      if (jobQualifier != null)
      {
        this.jobQualifier = jobQualifier.trim().replace(' ', '_');
      } else
      {
        SimpleDateFormat df = new SimpleDateFormat("yy.MM.dd-HH.mm.ss");
        this.jobQualifier = df.format(new Date());
      }
    }

    // project directory
    if (!projectSet)
    {
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
      } else
      {
        this.project = getJobDirectory();
      }
    }

    // ontology files
    if (!ontologyFilesSet)
    {
      String ontologyFileNames = configProperties.getProperty(ONTOLOGY_FILES_PROPERTY);
      if (ontologyFileNames != null)
      {
        this.ontologyFiles = new ArrayList<File>();
        for (String fileName : ontologyFileNames.split(","))
        {
          File file = new File(fileName.trim());
          if (file.exists())
          {
            ontologyFiles.add(file);
          } else
          {
            throw new IllegalStateException("File: " + file + " listed as ontolog file in Main "
                + "configuration properties but it does not exist.");
          }
        }
      }
    }

    // ontology subs
    if (!ontologySubsSet)
    {
      String ontologyFileSubs = configProperties.getProperty(ONTOLOGY_SUBS_PROPERTY);
      if (ontologyFileSubs != null)
      {
        ontologySubs = Boolean.valueOf(ontologyFileSubs.trim());
      } else
      {
        ontologySubs = true;
      }
    }

    // import files
    if (!importFilesSet)
    {
      String importFileNames = configProperties.getProperty(IMPORT_FILES_PROPERTY);
      if (importFileNames != null)
      {
        this.importFiles = new ArrayList<File>();
        for (String fileName : importFileNames.split(","))
        {
          File file = new File(fileName.trim());
          if (file.exists())
          {
            importFiles.add(file);
          } else
          {
            throw new IllegalStateException("File: " + file + " listed as import file in Main "
                + "configuration properties but it does not exist.");
          }
        }
      }
    }

    // import subs
    if (!importSubsSet)
    {
      String importFileSubs = configProperties.getProperty(IMPORT_SUBS_PROPERTY);
      if (importFileSubs != null)
      {
        importSubs = Boolean.valueOf(importFileSubs.trim());
      } else
      {
        importSubs = true;
      }
    }

    // output directory
    if (!outputDirectorySet)
    {
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
    }

    // quiet
    if (!quietSet)
    {
      String quiet = configProperties.getProperty(QUIET_PROPERTY);
      if (quiet != null)
      {
        this.quiet = Boolean.valueOf(quiet.trim());
      } else
      {
        this.quiet = false;
      }
    }

    // offline
    if (!offlineSet)
    {
      String offline = configProperties.getProperty(OFFLINE_PROPERTY);
      if (offline != null)
      {
        this.offline = Boolean.valueOf(offline.trim());
      } else
      {
        this.offline = true;
      }
    }

    // overwrite
    if (!overwriteSet)
    {
      String overwrite = configProperties.getProperty(OVERWRITE_PROPERTY);
      if (overwrite != null)
      {
        this.overwrite = Boolean.valueOf(overwrite.trim());
      } else
      {
        this.overwrite = false;
      }
    }

    // loglevel
    if (!logLevelSet)
    {

      String loglevel = configProperties.getProperty(LOG_LEVEL_PROPERTY);
      if (loglevel != null)
      {
        this.logLevel = loglevel.trim().toLowerCase();
      } else
      {
        this.logLevel = "debug";
      }
    }

  }

  private LoggerContext context;

  private boolean initWithLogging;

  /**
   * To setup logging backend. SLF4J is the API and logback is the backend.
   */
  public void initWithLogging() {
    configure();
    getJobDirectory().mkdirs();

    this.initWithLogging = true;
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

  private Logger logger = null;

  public void init() {
    if (!initWithLogging)
    {
      configure();
      getJobDirectory().mkdirs();
    }
    logger = LoggerFactory.getLogger(this.getClass());

    logger.info("Started job at " + new Date());
    logger.info("Working directory: " + getWorkingDirectory().getAbsolutePath());
    logger.info("Job's directory: " + getJobDirectory().getAbsolutePath());

    // ifiles = new OntologyFiles(importFiles, importSubs);
    logger.info("Use import sub directories: " + importSubs);
    for (File file : importFiles)
    {
      logger.info("Import file: " + file);
    }

  }

  // ================================================================================
  // Implementation
  // ================================================================================

  @Inject
  IOwlclManager mananger;

  @Inject
  public MainCommand(@Assisted OwlclCommand parent) {
    super(parent);
  }

  @Override
  public void initialize() {
    configure();
  }

  @Override
  public Object call() throws Exception {

    System.out.println("\nDynamic parameters:");
    this.getDynamicParameters().entrySet();
    for (Entry<String, String> dparam : this.getDynamicParameters().entrySet())

    {
      System.out.println("\t" + dparam.getKey() + "=" + dparam.getValue());
    }

    System.out.println("\nMain parameters:");
    for (String mainParameter : this.getMainParameters())
    {
      System.out.println("\t" + mainParameter);
    }

    System.out.println("\nFinished!");

    return null;
  }

  /**
   * Pass in a working directory that possibly has configuration properties.
   * This constructor is to force a client to make a choise instead of just
   * getting the current directory.
   * 
   * @param workingDirectory
   */
  // public Main(File workingDirectory) {
  // this.workingDirectory = workingDirectory;
  // configure();
  // }

  private File jobDirectory = null;

  public File getJobDirectory() {
    if (jobDirectory == null)
    {
      jobDirectory = new File(getOutputDirectory(), jobName + "_" + jobQualifier);
      jobDirectory.mkdirs();
    }
    return jobDirectory;
  }

  private OntologyFiles ifiles = null;
  private OntologyFiles ofiles = null;

  public OWLOntologyManager getNewBaseManager() {
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    if (offline)
    {
      man.clearIRIMappers();
    }

    // add the imports first, they have lower priority
    if (ifiles == null)
    {
      logger.info("Loading base import files");
      for (File file : importFiles)
      {
        logger.info("\tFile: " + file.getAbsolutePath());
      }

      ifiles = new OntologyFiles(importFiles, importSubs);
      logger.debug("Found base imports: ");
      for (Entry<File, IRI> entry : ifiles.getLocalOntologyFiles(null).entrySet())
      {
        logger.debug("\tIRI: " + entry.getValue() + "  <--  " + entry.getKey().getAbsolutePath());
      }

    }
    ifiles.setupManager(man, null);

    if (ofiles == null)
    {
      logger.info("Loading base ontology files");
      for (File file : ontologyFiles)
      {
        logger.info("\tFile: " + file.getAbsolutePath());
      }

      ofiles = new OntologyFiles(ontologyFiles, ontologySubs);
      logger.debug("Found base ontologies: ");
      for (Entry<File, IRI> entry : ofiles.getLocalOntologyFiles(null).entrySet())
      {
        logger.debug("\tIRI: " + entry.getValue() + "  <--  " + entry.getKey().getAbsolutePath());
      }
    }
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
        throw new RuntimeException("Error while loading isf-tools.properties file from directory: "
            + directory, e);
      }
    }
    return null;
  }

  // private IJCommander jc = new JCommander();
  // private NewModuleCommand newModule;
  // private GenerateModuleCommand module;
  // private EroCommand ero;
  // private CatalogCommand catalog;
  // private ValidateIriCommand validate;
  // private CompareCommand cc;
  // private TypecheckCommand typescheck;
  // private RewriteCommand rw;
  // private MapperCommand mc;
  //
  // private UpdateModuleCommand um;
  //
  // public void parseArgs(String[] args) {
  // jc.setAllowAbbreviatedOptions(true);
  // jc.setCaseSensitiveOptions(false);
  // jc.setProgramName("java -jar isf-tools-*.jar");
  //
  // jc.addObject(this);
  //
  // newModule = new NewModuleCommand(this);
  // jc.addCommand("newModule", newModule);
  //
  // module = new GenerateModuleCommand(this);
  // jc.addCommand("module", module);
  //
  // ero = new EroCommand(this);
  // jc.addCommand("ero", ero);
  //
  // catalog = new CatalogCommand(this);
  // jc.addCommand("catalog", catalog);
  //
  // validate = new ValidateIriCommand(this);
  // jc.addCommand("validate", validate);
  //
  // cc = new CompareCommand(this);
  // jc.addCommand("compare", cc);
  //
  // typescheck = new TypecheckCommand(this);
  // jc.addCommand("typecheck", typescheck);
  //
  // rw = new RewriteCommand(this);
  // jc.addCommand("rewrite", rw);
  //
  // mc = new MapperCommand(this);
  // jc.addCommand("map", mc);
  //
  // um = new UpdateModuleCommand(this);
  // jc.addCommand("updateModule", um);
  //
  // if (args.length == 0)
  // {
  // System.out.println(PROGRAM_DESC);
  // jc.usage();
  // return;
  // }
  //
  // try
  // {
  // jc.parse(args);
  // } catch (ParameterException e)
  // {
  // System.err.println(e.getMessage());
  // System.out.println(PROGRAM_DESC);
  // jc.usage();
  // }
  //
  // }

  // public static void main(String[] args) {
  // Main main = new Main(new File(System.getProperty("user.dir")));
  // main.parseArgs(args);
  // main.initWithLogging();
  //
  // }

}
