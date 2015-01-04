package com.essaid.owlcl.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.beust.jcommander.Parameter;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.util.ILoggerOwner;
import com.google.inject.Inject;

public abstract class AbstractCommand extends OwlclCommand implements ILoggerOwner {

  public static final String CATALOG = "owlcl.command.catalog";
  public static final String COMPARE = "owlcl.command.compare";
  public static final String ERO = "owlcl.command.ero";
  public static final String MAIN = OwlclCommand.CORE_MAIN;
  public static final String NEW_MODULE = "owlcl.command.newModule";
  public static final String GENERATE_MODULE = "owlcl.command.generateModule";
  public static final String MAPPER = "owlcl.command.map";
  public static final String REWRITE = "owlcl.command.rewrite";
  public static final String TYPECHECK = "owlcl.command.typecheck";
  public static final String UPDATE_MODULE = "owlcl.command.updateModule";
  public static final String VALIDATE = "owlcl.command.validate";
  public static final String ANNOTATE = "owlcl.command.annotate";
  public static final String MERGE = "owlcl.command.merge";

  // ================================================================================
  // Actions
  // ================================================================================
  @Parameter(names = "-actions",
      description = "The exact sub-actions to execute the command. If this is "
          + "specified, the default execution actions of the command are " + "overridden "
          + "with the specified actions. An action might require some "
          + "options and each command should document this.", hidden = true)
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
          + "of the command .", hidden = true)
  public List<String> preActions = new ArrayList<String>();

  // ================================================================================
  //
  // ================================================================================
  @Parameter(names = "-postActions",
      description = "Additional actions that could be taken after the execution of "
          + "the command.", hidden = true)
  public List<String> postActions = new ArrayList<String>();

  // ================================================================================
  // implementation
  // ================================================================================

  @InjectLogger
  private Logger logger;

  public AbstractCommand(OwlclCommand main) {
    super(main);
    if (main != null && !(main instanceof MainCommand))
    {
      throw new IllegalStateException(
          "AbstractCommand needs an OwlclCommand of type MainCommand but was passed " + main == null ? null
              : main.getClass().getName());
    }
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  public MainCommand getMain() {
    return (MainCommand) parent;
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
