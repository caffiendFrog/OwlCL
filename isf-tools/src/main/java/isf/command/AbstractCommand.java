package isf.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public abstract class AbstractCommand {

	// ================================================================================
	// Actions
	// ================================================================================
	@Parameter(names = "-actions",
			description = "The exact sub-actions to execute the command. If this is "
					+ "specified, the default execution actions of the command are "
					+ "overridden " + "with the specified actions. An action might require some "
					+ "options and each command should document this.")
	public void setActions(List<String> actions) {
		this.actions = actions;
		this.actionsSet = true;
	}

	public List<String> getActions() {
		return actions;
	}

	public boolean isActionsSet() {
		return actionsSet;
	}

	private List<String> actions = getCommandDefaultActions();
	private boolean actionsSet;

	// ================================================================================
	//
	// ================================================================================
	@Parameter(names = "-preActions",
			description = "Additional actions that could be taken before the execution "
					+ "of the command .")
	public List<String> preActions = new ArrayList<String>();

	// ================================================================================
	//
	// ================================================================================
	@Parameter(names = "-postActions",
			description = "Additional actions that could be taken after the execution of "
					+ "the command.")
	public List<String> postActions = new ArrayList<String>();

	// ================================================================================
	//
	// ================================================================================

	public final Logger logger = LoggerFactory.getLogger(this.getClass());


	private Main main;

	public Main getMain() {
		return main;
	}

	public AbstractCommand(Main main) {
		this.main = main;
	}

	protected abstract void configure();

	/**
	 * Returns a list of string values that represent the execution steps with
	 * any required input in the form of param1=value1. Warning, this method is
	 * called before the constructor is finished, be careful.
	 * 
	 * @return
	 */
	protected abstract void addCommandActions(List<String> actionsList);

	private List<String> getCommandDefaultActions() {
		List<String> actions = new ArrayList<String>();
		addCommandActions(actions);
		return actions;
	}

	// ================================================================================
	//
	// ================================================================================

	public abstract void run();

	protected List<String> getAllActions() {
		List<String> allActions = new ArrayList<String>();
		allActions.addAll(preActions);
		addCommandActions(allActions);
		allActions.addAll(postActions);
		return allActions;
	}

}
