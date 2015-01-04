package com.beust.jcommander;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;

public class DefaultUsage {

  /**
   * Display the usage for this command.
   */
  public static void usage(JCommander jc, String commandName) {
    StringBuilder sb = new StringBuilder();
    usage(jc, commandName, sb);
    Util.getConsole().println(sb.toString());
  }

  /**
   * Store the help for the command in the passed string builder.
   */
  public static void usage(JCommander jc, String commandName, StringBuilder out) {
    usage(jc, commandName, out, "");
  }

  /**
   * Store the help for the command in the passed string builder, indenting
   * every line with "indent".
   */
  public static void usage(JCommander jc, String commandName, StringBuilder out, String indent) {
    String description = jc.getCommandDescription(commandName);
    JCommander jc2 = jc.findCommandByAlias(commandName);
    if (description != null)
    {
      out.append(indent).append(description);
      out.append("\n");
    }
    DefaultUsage.usage(jc2, out, indent);
  }

  /**
   * Display the help on System.out.
   */
  public static void usage(JCommander jc) {
    StringBuilder sb = new StringBuilder();
    usage(jc, sb);
    Util.getConsole().println(sb.toString());
  }

  /**
   * Store the help in the passed string builder.
   */
  public static void usage(JCommander jc, StringBuilder out) {
    usage(jc, out, "");
  }

  public static void usage(JCommander jc, StringBuilder out, String indent) {
    if (jc.getKeyDescriptionMap() == null)
      jc.createDescriptions();
    boolean hasCommands = !jc.getCommands().isEmpty();

    //
    // First line of the usage
    //
    String programName = jc.getProgramName() != null ? jc.getProgramName().getDisplayName()
        : "<main class>";
    out.append(indent).append("Usage: " + programName + " [options]");
    if (hasCommands)
      out.append(indent).append(" [command] [command options]");
    if (jc.getMainDescription() != null)
    {
      out.append(" " + jc.getMainDescription().getDescription());
    }
    out.append("\n");

    //
    // Align the descriptions at the "longestName" column
    //
    int longestName = 0;
    List<ParameterDescription> sorted = Lists.newArrayList();
    for (ParameterDescription pd : jc.getParameterDescriptions())
    {
      if (!pd.hidden())
      {
        sorted.add(pd);
        // + to have an extra space between the name and the description
        int length = pd.getNames().length() + 2;
        if (length > longestName)
        {
          longestName = length;
        }
      }
    }

    //
    // Sort the options
    //
    Collections.sort(sorted, jc.getParameterDescriptionComparator());

    //
    // Display all the names and descriptions
    //
    int descriptionIndent = 6;
    if (sorted.size() > 0)
      out.append(indent).append("  Options:\n");
    for (ParameterDescription pd : sorted)
    {
      out.append(indent).append(
          "  " + (pd.required() ? "* " : "  ") + pd.getNames() + "\n" + indent
              + Util.getSpaces(descriptionIndent));
      int indentCount = indent.length() + descriptionIndent;
      Util.wrapDescription(out, indentCount, pd.getDescription(), jc.getColumnSize());
      Object def = pd.getDefault();
      if (pd.isDynamicParameter())
      {
        out.append("\n" + Util.getSpaces(indentCount + 1)).append(
            "Syntax: " + pd.names()[0] + "key" + pd.getAssignment() + "value");
      }
      if (def != null)
      {
        String displayedDef = Util.isStringEmpty(def.toString()) ? "<empty string>" : def
            .toString();
        out.append("\n" + Util.getSpaces(indentCount + 1)).append(
            "Default: " + (pd.password() ? "********" : displayedDef));
      }
      out.append("\n");
    }

    //
    // If commands were specified, show them as well
    //
    if (hasCommands)
    {
      out.append("  Commands:\n");
      // The magic value 3 is the number of spaces between the name of the
      // option
      // and its description
      for (Map.Entry<ProgramName, JCommander> commands : jc.getNameCommandMap().entrySet())
      {
        ProgramName progName = commands.getKey();
        String dispName = progName.getDisplayName();
        out.append(indent).append("    " + dispName); // + s(spaceCount) +
                                                      // getCommandDescription(progName.name)
                                                      // + "\n");

        // Options for this command
        usage(jc, progName.getName(), out, "      ");
        out.append("\n");
      }
    }
  }

}
