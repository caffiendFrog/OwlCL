package com.beust.jcommander;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public abstract class Command extends JCommander implements Callable<Object>, CommandContext {

  @DynamicParameter(names = { "-D" }, description = "The dynamic parameters that can be "
      + "specified on the command line in the form of \"-Dparam=value\". Multiple params "
      + "can be specified and this map will perserve the parsing/setting order.")
  private Map<String, String> dynamicParameters = new LinkedHashMap<String, String>();

  @Parameter(description = "The list of \"main\" parameters in the order specified.")
  private List<String> mainParameters = new LinkedList<String>();

  @ParametersDelegate
  Map<ParameterGroup, ParameterGroup> parameterGroups = new HashMap<ParameterGroup, ParameterGroup>();

  public Command() {
    super.addObject(this);
  }

  public List<String> getMainParameters() {
    return mainParameters;
  }

  public void setMainParameters(List<String> mainParameters) {
    this.mainParameters = mainParameters;
  }

  public Map<String, String> getDynamicParameters() {
    return dynamicParameters;
  }

  public void setDynamicParameters(Map<String, String> dynamicParameters) {
    this.dynamicParameters = dynamicParameters;
  }

  public boolean addParameterGroup(ParameterGroup group) {
    if (!parameterGroups.containsKey(group))
    {
      parameterGroups.put(group, group);
      return true;
    }
    return false;
  }

  public ParameterGroup getParameterGroup(String groupName) {
    ParameterGroup group = new ParameterGroup(groupName) {
    };
    return parameterGroups.get(group);
  }

  public Collection<ParameterGroup> getParameterGroups() {
    Set<ParameterGroup> groups = new HashSet<ParameterGroup>(parameterGroups.keySet());
    return groups;
  }

  @Override
  public abstract Object call() throws Exception;

  public final void initialize() {
    doInitialize();

    for (JCommander jcommand : nameCommandMap.values())
    {

      if (jcommand instanceof Command)
      {
        Command command = (Command) jcommand;
        command.initialize();
      }
    }

  }

  protected abstract void doInitialize();

  protected void preParse() {

  }

  protected void postParse() {

  }

  protected void preCall() {

  }

  protected void postCall() {

  }

  public void dispose() {

  }

  public void addCommand(Command command) {

    addCommand(command, new String[0]);

  }

  public void addCommand(Command command, String... aliases) {
    Parameters p = command.getClass().getAnnotation(Parameters.class);
    if (p != null && p.commandNames().length > 0)
    {
      for (String commandName : p.commandNames())
      {
        addCommand(commandName, command, aliases);
      }
    } else
    {
      throw new ParameterException("Trying to add command " + command.getClass().getName()
          + " without specifying its names in @Parameters");
    }

  }

  /**
   * Adds a sub command to this command. The added command will inherit the same
   * command settings as this command.
   */
  public void addCommand(String name, Command command, String... aliases) {
    command.setProgramName(name);
    command.setAcceptUnknownOptions(this.allowUnknownArgs);
    command.setAllowAbbreviatedOptions(this.allowAbbreviations);
    command.setCaseSensitiveOptions(this.caseSensitive);
    command.setDefaultProvider(this.defaultProvider);
    command.setVerbose(this.verbose);

    this.nameCommandMap.put(command.getProgramName(), command);
    this.registerAliases(name, command.getProgramName(), aliases);

  }

  public void addConfiguredCommand(Command command) {

    addConfiguredCommand(command, new String[0]);

  }

  public void addConfiguredCommand(Command command, String... aliases) {
    Parameters p = command.getClass().getAnnotation(Parameters.class);
    if (p != null && p.commandNames().length > 0)
    {
      for (String commandName : p.commandNames())
      {
        addConfiguredCommand(commandName, command, aliases);
      }
    } else
    {
      throw new ParameterException("Trying to add command " + command.getClass().getName()
          + " without specifying its names in @Parameters");
    }

  }

  /**
   * Adds a sub command to this command. The added command will not inherit any
   * configuration from this command
   */
  public void addConfiguredCommand(String name, Command command, String... aliases) {
    command.setProgramName(name);

    this.nameCommandMap.put(command.getProgramName(), command);
    this.registerAliases(name, command.getProgramName(), aliases);

  }

  @Override
  @Deprecated
  public void addCommand(Object object) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  @Deprecated
  public void addCommand(String name, Object object) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  @Deprecated
  public void addCommand(String name, Object object, String... aliases) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  // ================================================================================
  // main demo
  // ================================================================================

  public static void main(String[] args) throws Exception {
    Command command = new Command() {

      @Override
      protected void doInitialize() {
        System.out.println("Doing initialization.");

      }

      @Override
      public Object call() throws Exception {
        System.out.println("Doing some useful work and calling all sub commands "
            + "as identified by the arguments.");
        return null;
      }
    };

    DefaultUsage.usage(command);

    command.parse(args);
    System.out.println("\nDynamic parameters:");
    command.getDynamicParameters().entrySet();
    for (Entry<String, String> dparam : command.getDynamicParameters().entrySet())

    {
      System.out.println("\t" + dparam.getKey() + "=" + dparam.getValue());
    }

    System.out.println("\nMain parameters:");
    for (String main : command.getMainParameters())
    {
      System.out.println("\t" + main);
    }

    command.call();

    System.out.println("\nFinished!");
  }

  public Command getSubCommand(String name) {
    return (Command) super.findCommandByAlias(name);
  }
}
