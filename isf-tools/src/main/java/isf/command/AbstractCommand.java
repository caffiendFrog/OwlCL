package isf.command;

import isf.command.cli.Main;
import isf.util.RuntimeOntologyLoadingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public abstract class AbstractCommand {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public Main main;

	public AbstractCommand(Main main) {
		this.main = main;
	}

	protected abstract void preConfigure();

	protected abstract void init();

	/**
	 * Returns a list of string values that represent the execution steps with
	 * any required input in the form of param1=value1. Warning, this method is
	 * called before the constructor is finished, be careful.
	 * 
	 * @return
	 */
	protected abstract void addCommandActions(List<String> actionsList);

	public List<String> actions = getCommandDefaultActions();

	@Parameter(names = "-actions",
			description = "The exact sub-actions to execute the command. If this is "
					+ "specified, the default execution actions of the command are "
					+ "overridden " + "with the specified actions. An action might require some "
					+ "options and each command should document this.")
	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getActions() {
		return actions;
	}

	private List<String> getCommandDefaultActions() {
		List<String> actions = new ArrayList<String>();
		addCommandActions(actions);
		return actions;
	}

	@Parameter(names = "-preActions",
			description = "Additional actions that could be taken before the execution "
					+ "of the command .")
	public List<String> preActions = new ArrayList<String>();

	@Parameter(names = "-postActions",
			description = "Additional actions that could be taken after the execution of "
					+ "the command.")
	public List<String> postActions = new ArrayList<String>();

	public abstract void run();

	protected List<String> getAllActions() {
		List<String> allActions = new ArrayList<String>();
		allActions.addAll(preActions);
		addCommandActions(allActions);
		allActions.addAll(postActions);
		return allActions;
	}

	protected OWLOntology getOrLoadOntology(IRI iri, OWLOntologyManager man) {
		OWLOntology o = man.getOntology(iri);
		if (o == null)
		{
			try
			{
				o = man.loadOntology(iri);
			} catch (OWLOntologyCreationException e)
			{
				throw new RuntimeOntologyLoadingException("Failed while getOrLoadOntology IRI: "
						+ iri, e);
			}
		}
		return o;
	}

	protected OWLOntology createOntology(IRI iri, OWLOntologyManager man) {

		OWLOntology o = null;

		try
		{
			o = man.createOntology(iri);
		} catch (OWLOntologyCreationException e)
		{
			throw new RuntimeOntologyLoadingException("Faild to createOntology for IRI: " + iri, e);
		}

		return o;

	}

	protected OWLOntology getOrLoadOrCreateOntology(IRI iri, OWLOntologyManager man) {
		OWLOntology o = null;
		try
		{
			o = getOrLoadOntology(iri, man);
		} catch (RuntimeOntologyLoadingException e1)
		{
			if (e1.isIriMapping())
			{
				o = createOntology(iri, man);
			} else
			{
				throw e1;
			}
		}
		return o;
	}

	public class Report {

		PrintWriter pw;
		PrintWriter pwDetailed;
		int counter = 0;

		public Report(String relativeFilePath) {
			try
			{
				pw = new PrintWriter(new File(AbstractCommand.this.main.getOutputDirectory(),
						relativeFilePath + ".txt"));
				pwDetailed = new PrintWriter(new File(
						AbstractCommand.this.main.getOutputDirectory(), relativeFilePath
								+ "-detailed.txt"));
			} catch (FileNotFoundException e)
			{
				throw new RuntimeException("Failed to create report files", e);
			}

		}

		private String getNextLineNumber() {
			String lineNumber = "00000000000" + ++counter;
			int len = lineNumber.length();
			return lineNumber.substring(len - 6, len) + ") ";
		}

		public void error(String value) {
			String number = getNextLineNumber();
			AbstractCommand.this.logger.error(value);
			pw.println(number + value);
			pwDetailed.println(number + value);
			doConsole(number + value);
		}

		public void warn(String value) {
			String number = getNextLineNumber();
			AbstractCommand.this.logger.warn(value);
			pw.println(number + value);
			pwDetailed.println(number + value);
			doConsole(number + value);
		}

		public void info(String value) {
			String number = getNextLineNumber();
			AbstractCommand.this.logger.info(value);
			pw.println(number + value);
			pwDetailed.println(number + value);
			doConsole(number + value);

		}

		public void detail(String value) {
			String number = getNextLineNumber();
			AbstractCommand.this.logger.debug(value);
			pwDetailed.println(number + value);
			// doConsole(value);
		}

		public void finish() {
			pw.println("\n=====  Finished report!  ======");
			pw.close();
			pwDetailed.println("\n=====  Finished report!  ======");
			pwDetailed.close();
			doConsole("\n=====  Finished report!  ======");
		}

		private void doConsole(String value) {
			if (!AbstractCommand.this.main.quiet)
			{
				System.out.println(value);
			}
		}
	}

}
