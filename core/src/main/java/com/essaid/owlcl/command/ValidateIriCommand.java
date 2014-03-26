package com.essaid.owlcl.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.essaid.owlcl.command.cli.CanonicalFileConverter;
import com.essaid.owlcl.command.cli.DirectoryExistsValueValidator;
import com.essaid.owlcl.util.OntologyFiles;
import com.essaid.owlcl.util.Report;

@Parameters(
		commandNames = "validate",
		commandDescription = "This command validates IRIs in the *.owl files. "
				+ "It first loads all *.owl files and gives an error if there is an IRI that is used in more "
				+ "than one file. Then, after finding all the IRIs from the first step, it tries to auto-load "
				+ "those OWL files by their IRI. This will fail if ther are certain XML attributes not correct "
				+ "according to the OWL-API (and Protege) convensions. An error here means that OWL-API based "
				+ "tools could have problems resolving those IRIs. The last check is to check that all imports"
				+ " can be resolved locally and reports which ones can't. For the ones not locally resolvable, it"
				+ "report the URL they will be actually resolved from.")
public class ValidateIriCommand extends AbstractCommand {

	// ================================================================================
	// the directory to validate
	// ================================================================================
	// public File directory = new
	// File(ISFUtil.getTrunkDirectory().getAbsolutePath(), "src/ontology");
	public File directory = null;
	public boolean directorySet;

	@Parameter(names = "-directory", converter = CanonicalFileConverter.class,
			validateValueWith = DirectoryExistsValueValidator.class,
			description = "The starting directory to validate ontologies and thier IRIs.")
	public void setDirectory(File directory) {
		this.directory = directory;
		this.directorySet = true;
	}

	public File getDirectory() {
		return directory;
	}

	// ================================================================================
	// Report path
	// ================================================================================

	@Parameter(names = "-report",
			description = "Relative path/name for the report file without suffix")
	public String reportPath = "validateReport";

	// ================================================================================
	// Implementation
	// ================================================================================

	public boolean problemsFound;
	Map<IRI, String> iriToDocMap = new HashMap<IRI, String>();
	public String[] extensions = { "owl" };

	public ValidateIriCommand(MainCommand main) {
		super(main);
		configure();
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {
		actionsList.add(Action.duplicates.name());
		actionsList.add(Action.autoload.name());
		actionsList.add(Action.resolve.name());
	}

	OntologyFiles of;
	Report report;

	public void run() {
		List<File> files = new ArrayList<File>();
		files.add(directory);
		of = new OntologyFiles(files, true);

		report = new Report(this, reportPath);

		for (String action : getAllActions())
		{
			Action.valueOf(action).execute(this);
		}

		if (problemsFound)
		{
			report.info("");
			report.info("===========  Possible problems, see above. =============");
		} else
		{
			report.info("");
			report.info("============ ALL GOOD, SEE LOG ABOVE  ==============");

		}

		report.finish();
	}

	enum Action {
		duplicates {

			@Override
			public void execute(ValidateIriCommand command) {
				command.report.info("");
				command.report.info("===========================================");
				command.report.info("=====   Checking for duplicate IRIs.  =====");
				command.report.info("===========================================");
				command.report.info("");

				for (Entry<IRI, List<File>> entry : command.of.getDuplicateIris(null).entrySet())
				{
					command.problemsFound = true;
					command.report.info("");
					command.report.info("IRI: " + entry.getKey());

					for (File file : entry.getValue())
					{
						command.report.info("\tIn file: " + file.getAbsolutePath());
					}
				}

			}
		},
		autoload {

			@SuppressWarnings("deprecation")
			@Override
			public void execute(ValidateIriCommand command) {
				command.report.info("");
				command.report.info("==================================================");
				command.report.info("==========   Checking for autoloading IRIs.  =====");
				command.report.info("==================================================");
				command.report.info("");

				for (Entry<File, IRI> entry : command.of.getLocalOntologyFiles(null).entrySet())
				{
					OWLOntologyManager man = OWLManager.createOWLOntologyManager();
					man.setSilentMissingImportsHandling(true);
					man.clearIRIMappers();
					AutoIRIMapper mapper = new AutoIRIMapper(command.getDirectory(), true);
					man.addIRIMapper(mapper);

					try
					{
						OWLOntology o = man.loadOntology(entry.getValue());

						boolean samePath = new File(man.getOntologyDocumentIRI(o).toURI())
								.getCanonicalPath().equals(entry.getKey().getCanonicalPath());
						if (!samePath)
						{
							command.report.warn("IRI: " + entry.getValue()
									+ " should have been loaded from: "
									+ entry.getKey().getCanonicalPath());
							command.report.warn("\tBut it was loaded from: "
									+ man.getOntologyDocumentIRI(o));
							command.problemsFound = true;
						} else
						{
							command.report.info("IRI loaded correctly. IRI: " + entry.getValue()
									+ "  <---  " + entry.getKey().getCanonicalPath());
						}
					} catch (OWLOntologyCreationException e)
					{
						command.report.warn("Error creating ontology during auto loading IRI: "
								+ entry.getValue());
						command.report.warn("\tException: " + e.getMessage());
						command.report.warn("\tCause: " + e.getCause());
						command.problemsFound = true;

					} catch (IOException e)
					{
						command.report.warn("IOException during auto loading IRI: "
								+ entry.getValue());
						command.report.warn("\tException: " + e.getMessage());
						command.report.warn("\tCause: " + e.getCause());
						command.problemsFound = true;
					}
				}

			}
		},
		resolve {

			@Override
			public void execute(ValidateIriCommand command) {

				command.report.info("");
				command.report.info("==================================================");
				command.report.info("==========   Checking for non-local imports  =====");
				command.report.info("==================================================");
				command.report.info("");

				for (Entry<File, Set<IRI>> entry : command.of.getLocallyUnresolvableIris(null)
						.entrySet())
				{
					try
					{
						command.report.warn("File: " + entry.getKey().getCanonicalPath());
					} catch (IOException e)
					{
						throw new RuntimeException("Failed to get canonical path for file: "
								+ entry.getKey(), e);
					}
					for (IRI iri : entry.getValue())
					{
						command.report.warn("\tHas unresolved import of IRI: " + iri);
						command.problemsFound = true;
					}
				}

			}
		};

		public abstract void execute(ValidateIriCommand command);
	}

	protected void configure() {
		// TODO Auto-generated method stub

	}

	protected void init() {
		// TODO Auto-generated method stub

	}

  @Override
  public Object call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
