package isf.command;

import isf.command.cli.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public abstract class AbstractCommand {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public Main main;

	public AbstractCommand(Main main) {
		this.main = main;
	}

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

	/**
	 * Returns a list of string values that represent the execution steps with
	 * any required input in the form of param1=value1. Warning, this method is
	 * called before the constructor is finished, be careful.
	 * 
	 * @return
	 */
	protected abstract void addCommandActions(List<String> actionsList);

	public abstract void run();

	protected List<String> getAllActions() {
		List<String> allActions = new ArrayList<String>();
		allActions.addAll(preActions);
		addCommandActions(allActions);
		allActions.addAll(postActions);
		return allActions;
	}

	// public PrintWriter pw = new PrintWriter(System.out);

//	protected int indent;

//	protected String indent(String string) {
//		return new String(new char[indent]).replace('\0', ' ') + string;
//	}

	// protected void warn(String message, Exception e) {
	// pw.println(indent("Warn: " + message));
	// if (e != null)
	// {
	// ++indent;
	// pw.print(indent("E: " + e.getClass().getSimpleName() + " -> " +
	// e.getMessage()));
	// --indent;
	// }
	// pw.flush();
	// }
	//
	// protected void info(String message) {
	// pw.println(indent(message));
	// pw.flush();
	// }
	//
	// protected void infoDetail(String message) {
	// pw.println(indent(message));
	// pw.flush();
	// }
	//
	// protected void debug(String message, Exception e) {
	// pw.println(indent("Debug: " + message));
	// if (e != null)
	// {
	// ++indent;
	// pw.print("E: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
	// --indent;
	// }
	// pw.flush();
	//
	// }

	public class Report {

		PrintWriter pw;

		public Report(String relativeFilePath) throws FileNotFoundException {
			pw = new PrintWriter(new File(AbstractCommand.this.main.getOutputDirectory(),
					relativeFilePath));
		}

		void error(String value) {
			AbstractCommand.this.logger.error(value);
			pw.append(value);
			doConsole(value);
		}

		void warn(String value) {
			AbstractCommand.this.logger.warn(value);
			pw.append(value);
			doConsole(value);
		}

		void info(String value) {
			AbstractCommand.this.logger.info(value);
			pw.append(value);
			doConsole(value);

		}

		void detail(String value) {
			AbstractCommand.this.logger.debug(value);
			if (AbstractCommand.this.main.detailedReport)
			{
				pw.append(value);
			}
			doConsole(value);
		}

		void finish() {
			pw.close();
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
