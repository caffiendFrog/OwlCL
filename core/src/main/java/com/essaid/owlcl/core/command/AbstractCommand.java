package com.essaid.owlcl.core.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.essaid.owlcl.core.OwlclCommand;

public abstract class AbstractCommand extends OwlclCommand {

  // ================================================================================
  // Actions
  // ================================================================================
  @Parameter(names = "-actions",
      description = "The exact sub-actions to execute the command. If this is "
          + "specified, the default execution actions of the command are " + "overridden "
          + "with the specified actions. An action might require some "
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

  public MainCommand getMain() {
    return (MainCommand) parent;
  }

  public AbstractCommand(OwlclCommand main) {
    super(main);
    if (main == null || !(main instanceof MainCommand))
    {
      throw new IllegalStateException(
          "AbstractCommand needs an OwlclCommand of type MainCommand but was passed " + main == null ? null
              : main.getClass().getName());
    }
  }

  /**
   * Returns a list of string values that represent the execution steps with any
   * required input in the form of param1=value1. Warning, this method is called
   * before the constructor is finished, be careful.
   * 
   * @return
   */
  protected abstract void addCommandActions(List<String> actionsList);

  private List<String> getCommandDefaultActions() {
    List<String> actions = new ArrayList<String>();
    addCommandActions(actions);
    return actions;
  }

  protected List<String> getAllActions() {
    List<String> allActions = new ArrayList<String>();
    allActions.addAll(preActions);
    addCommandActions(allActions);
    allActions.addAll(postActions);
    return allActions;
  }

}
