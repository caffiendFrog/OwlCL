package isf.command;

import isf.ISFUtil;
import isf.command.cli.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(
		commandNames = "iri",
		commandDescription = "This command validates IRIs in the *.owl files. "
				+ "It first loads all *.owl files and gives an error if there is an IRI that is used in more "
				+ "than one file. Then, after finding all the IRIs from the first step, it tries to auto-load "
				+ "those OWL files by their IRI. This will fail if ther are certain XML attributes not correct "
				+ "according to the OWL-API (and Protege) convensions. An error here means that OWL-API based "
				+ "tools could have problems resolving those IRIs. The last check is to check that all imports"
				+ " can be resolved locally and reports which ones can't. For the ones not locally resolvable, it"
				+ "report the URL they will be actually resolved from.")
public class ValidateIriCommand extends AbstractCommand {

	public String directory = ISFUtil.getTrunkDirectory().getAbsolutePath() + "/src/ontology";
	public boolean directorySet;

	public boolean problemsFound;

	@Parameter(names = "-directory",
			description = "The starting directory to validate ontologies and thier IRIs.")
	public void setDirectory(String directory) {
		this.directory = directory;
		this.directorySet = true;
	}

	public String getDirectory() {
		return directory;
	}

	// ================================================================================
	// Implementation
	// ================================================================================

	Map<IRI, String> iriToDocMap = new HashMap<IRI, String>();
	public String[] extensions = { "owl" };

	public ValidateIriCommand(Main main) {
		super(main);
	}

	@Override
	protected void addCommandActions(List<String> actionsList) {
		actionsList.add(Action.duplicates.name());
		actionsList.add(Action.autoload.name());
		actionsList.add(Action.resolve.name());
	}

	@Override
	public void run() {

		for (String action : getAllActions())
		{
			Action.valueOf(action).execute(this);
		}

		if (problemsFound)
		{
			System.out.println("\n============ POSSIBLE PROBLEMS, SEE LOG ABOVE  ===============");
		} else
		{
			System.out.println("\n============ ALL GOOD, SEE LOG ABOVE  ===============");

		}

	}

	enum Action {
		duplicates {

			@Override
			public void execute(ValidateIriCommand command) {
				System.out.println("=====   Checking for duplicate IRIs.  =====\n");
				int counter = 0;
				for (File file : FileUtils.listFiles(new File(command.getDirectory()),
						command.extensions, true))
				{
					++counter;

					try
					{

						OWLOntologyManager man = OWLManager.createOWLOntologyManager();
						man.setSilentMissingImportsHandling(true);
						IRI iri = man.loadOntologyFromOntologyDocument(file).getOntologyID()
								.getOntologyIRI();

						if (command.iriToDocMap.keySet().contains(iri))
						{
							System.err.println(counter + "\tFound duplicate IRI of: " + iri
									+ "\n in file: " + file.getAbsolutePath());
							System.err.println("\tPrevious file with same IRI was: "
									+ command.iriToDocMap.get(iri));
							command.problemsFound = true;
						} else
						{
							command.iriToDocMap.put(iri, file.getAbsolutePath());
							System.out.print(counter + " - Found IRI: " + iri);
							System.out.println("\t In file: " + file.getAbsolutePath());
						}

					} catch (OWLOntologyCreationException e)
					{
						throw new RuntimeException(
								"Error during loading all *.owl files to check for duplicates."
										+ "\n\tSee below exception for details"
										+ "\n\tFile beind loaded was: " + file.getAbsolutePath(), e);
					}
				}

			}
		},
		autoload {

			@Override
			public void execute(ValidateIriCommand command) {
				System.out.println("\n\n=====   Checking for autoloading IRIs.  =====\n");
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				man.setSilentMissingImportsHandling(true);
				man.clearIRIMappers();
				AutoIRIMapper mapper = new AutoIRIMapper(new File(command.getDirectory()), true);
				man.addIRIMapper(mapper);

				int counter = 0;
				for (Entry<IRI, String> entry : command.iriToDocMap.entrySet())
				{
					++counter;
					try
					{
						OWLOntology o = man.loadOntology(entry.getKey());

						boolean samePath = new File(man.getOntologyDocumentIRI(o).toURI())
								.getCanonicalPath().equals(entry.getValue());
						if (!samePath)
						{
							System.err.println(counter + "\tIRI: " + entry.getKey()
									+ " should have been loaded from: " + entry.getValue());
							System.err.println("\tBut it was loaded from: "
									+ man.getOntologyDocumentIRI(o));
							command.problemsFound = true;
						} else
						{
							System.out.println(counter + " - IRI loaded correctly. IRI: "
									+ entry.getKey());
						}
					} catch (OWLOntologyCreationException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		},
		resolve {

			@Override
			public void execute(ValidateIriCommand command) {
				System.out.println("\n\n=====   Checking for non-local imports  =====\n");

				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				AutoIRIMapper mapper = new AutoIRIMapper(new File(command.getDirectory()), true);
				man.addIRIMapper(mapper);
				int counter = 0;
				for (Entry<IRI, String> entry : command.iriToDocMap.entrySet())
				{
					++counter;
					try
					{
						OWLOntology o = man.loadOntology(entry.getKey());
						System.out.println();
						System.out.println(counter + " - Ontology: "
								+ o.getOntologyID().getOntologyIRI());
						for (OWLOntology imprt : o.getImports())
						{
							IRI docIri = man.getOntologyDocumentIRI(imprt);
							if (!docIri.toString().startsWith("file:/"))
							{
								System.out.println(counter
										+ "\t============= Remote import of IRI:"
										+ imprt.getOntologyID().getOntologyIRI());
								System.out.println("\t============= From location: " + docIri);
								command.problemsFound = true;
							} else
							{
								System.out.println("\tLocal import: "
										+ imprt.getOntologyID().getOntologyIRI());
							}
						}

					} catch (OWLOntologyCreationException e)
					{
						System.err.println(counter + "\tError loading ontology with IRI: "
								+ entry.getKey());
						System.err.println("\tWhile checking imports.");
						throw new RuntimeException("", e);
					}
				}

			}
		};

		public abstract void execute(ValidateIriCommand command);
	}

}
