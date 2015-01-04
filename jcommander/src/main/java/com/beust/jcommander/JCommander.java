/**
 * Copyright (C) 2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beust.jcommander;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import com.beust.jcommander.internal.DefaultConverterFactory;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Nullable;
import com.beust.jcommander.internal.Sets;

/**
 * The main class for JCommander. It's responsible for parsing the object that
 * contains all the annotated fields, parse the command line and assign the
 * fields with the correct values and a few other helper methods, such as
 * usage().
 * 
 * The object(s) you pass in the constructor are expected to have one or more
 * \@Parameter annotations on them. You can pass either a single object, an
 * array of objects or an instance of Iterable. In the case of an array or
 * Iterable, JCommander will collect the \@Parameter annotations from all the
 * objects passed in parameter.
 * 
 * @author Cedric Beust <cedric@beust.com>
 */
public class JCommander {

  private class DefaultVariableArity implements IVariableArity {

    @Override
    public int processVariableArity(String optionName, String[] options) {
      int i = 0;
      while (i < options.length && !isOption(options, options[i]))
      {
        i++;
      }
      return i;
    }
  }

  protected boolean allowAbbreviations = false;

  protected boolean allowUnknownArgs = false;

  protected boolean caseSensitive = true;

  private int columnWidth = 79;

  /**
   * The objects that contain fields annotated with @Parameter.
   */
  private List<Object> configObjects = Lists.newArrayList();

  /**
   * The factories used to look up string converters.
   */
  private LinkedList<IStringConverterFactory> converterFactories = Lists.newLinkedList();

  private final IVariableArity DEFAULT_VARIABLE_ARITY = new DefaultVariableArity();

  /**
   * A default provider returns default values for the parameters.
   */
  protected IDefaultProvider defaultProvider;

  private Comparator<? super ParameterDescription> descriptionComparator = new Comparator<ParameterDescription>() {

    @Override
    public int compare(ParameterDescription p0, ParameterDescription p1) {
      return p0.getLongestName().compareTo(p1.getLongestName());
    }
  };

  private boolean helpSpecified;

  /**
   * A map to look up parameter description per option name.
   */
  private Map<IKey, ParameterDescription> keyDescriptionMap;

  /**
   * Alias database for reverse lookup
   */
  private Map<IKey, ProgramName> keyNameMap = Maps.newLinkedHashMap();

  private ParameterDescription mainDescription;

  private boolean mainInitialized = false;

  /**
   * List of commands and their instance.
   */
  protected Map<ProgramName, JCommander> nameCommandMap = Maps.newLinkedHashMap();
  /**
   * The name of command or alias as it was passed to the command line
   */
  private String parsedAliasString;

  /**
   * The name of the command after the parsing has run.
   */
  private String parsedCommandString;

  private ProgramName programName;

  /**
   * A set of all the parameterizeds that are required. During the reflection
   * phase, this field receives all the fields that are annotated with
   * required=true and during the parsing phase, all the fields that are
   * assigned a value are removed from it. At the end of the parsing phase, if
   * it's not empty, then some required fields did not receive a value and an
   * exception is thrown.
   */
  private Set<ParameterDescription> requiredDescriptions = Sets.newHashSet();

  private ResourceBundle resourceBundle;

  private List<String> unknownArgs = Lists.newArrayList();

  protected int verbose = 0;

  /**
   * Creates a new un-configured JCommander object.
   */
  public JCommander() {
    converterFactories.addFirst(new DefaultConverterFactory());
  }

  /**
   * @param object
   *          The arg object expected to contain {@link Parameter} annotations.
   */
  public JCommander(Object object) {
    this();
    addObject(object);
  }

  /**
   * @param object
   *          The arg object expected to contain {@link Parameter} annotations.
   * @param bundle
   *          The bundle to use for the descriptions. Can be null.
   */
  public JCommander(Object object, @Nullable ResourceBundle bundle) {
    this(object);
    setDescriptionsBundle(bundle);
  }

  /**
   * @param object
   *          The arg object expected to contain {@link Parameter} annotations.
   * @param bundle
   *          The bundle to use for the descriptions. Can be null.
   * @param args
   *          The arguments to parse (optional).
   */
  public JCommander(Object object, ResourceBundle bundle, String... args) {
    this(object, bundle);
    parse(args);
  }

  /**
   * @param object
   *          The arg object expected to contain {@link Parameter} annotations.
   * @param args
   *          The arguments to parse (optional).
   */
  public JCommander(Object object, String... args) {
    this(object);
    parse(args);
  }

  public void addCommand(Object object) {
    Parameters p = object.getClass().getAnnotation(Parameters.class);
    if (p != null && p.commandNames().length > 0)
    {
      for (String commandName : p.commandNames())
      {
        addCommand(commandName, object);
      }
    } else
    {
      throw new ParameterException("Trying to add command " + object.getClass().getName()
          + " without specifying its names in @Parameters");
    }
  }

  /**
   * Add a command object.
   */
  public void addCommand(String name, Object object) {
    addCommand(name, object, new String[0]);
  }

  /**
   * Add a command object and its aliases.
   */
  public void addCommand(String name, Object object, String... aliases) {
    JCommander jc = new JCommander(object);
    jc.setProgramName(name, aliases);
    jc.setDefaultProvider(defaultProvider);
    ProgramName progName = jc.programName;
    nameCommandMap.put(progName, jc);

    registerAliases(name, progName, aliases);
  }

  public void addConverterFactory(IStringConverterFactory converterFactory) {
    converterFactories.addFirst(converterFactory);
  }

  /**
   * Adds the provided arg object to the set of objects that this commander will
   * parse arguments into.
   * 
   * @param object
   *          The arg object expected to contain {@link Parameter} annotations.
   *          If <code>object</code> is an array or is {@link Iterable}, the
   *          child objects will be added instead.
   */
  // declared final since this is invoked from constructors
  public final void addObject(Object object) {
    if (object instanceof Iterable)
    {
      // Iterable
      for (Object o : (Iterable<?>) object)
      {
        configObjects.add(o);
      }
    } else if (object.getClass().isArray())
    {
      // Array
      for (Object o : (Object[]) object)
      {
        configObjects.add(o);
      }
    } else
    {
      // Single object
      configObjects.add(object);
    }
  }

  private void createDescription(Object object) {
    for (ParameterDescription pd : Util.getConfigObjectDescriptions(this, object, resourceBundle))
    {
      if (pd.getParameter() != null)
      {
        // @Parameter
        if (pd.getParameter().names().length == 0)
        {
          // main parameter
          print("Found main parameter:", pd);
          if (mainDescription != null)
          {
            throw new ParameterException("Only one @Parameter with no names attribute is"
                + " allowed, found:" + mainDescription + " and " + pd);
          }

          mainDescription = pd;
        } else
        {
          // named parameter
          for (String name : pd.getParameter().names())
          {
            if (keyDescriptionMap.containsKey(new StringKey(name)))
            {
              throw new ParameterException("Found the option " + name + " multiple times");
            }
            print("Adding description for ", name);
            putKeyDescription(name, pd);

            if (pd.getParameter().required())
              requiredDescriptions.add(pd);
          }
        }

      } else if (pd.getDynamicParameter() != null)
      {
        // @DynamicParameter
        for (String name : pd.getDynamicParameter().names())
        {
          if (keyDescriptionMap.containsKey(name))
          {
            throw new ParameterException("Found the option " + name + " multiple times");
          }
          print("Adding description for ", name);
          putKeyDescription(name, pd);
          if (pd.getDynamicParameter().required())
            requiredDescriptions.add(pd);
        }
      }

    }
  }

  /**
   * Create the ParameterDescriptions for all the \@Parameter found.
   */
  void createDescriptions() {
    keyDescriptionMap = Maps.newHashMap();

    for (Object object : configObjects)
    {
      createDescription(object);
    }
  }

  /**
   * Expand the command line parameters to take @ parameters into account. When @
   * is encountered, the content of the file that follows is inserted in the
   * command line.
   * 
   * @param originalArgv
   *          the original command line parameters
   * @return the new and enriched command line parameters
   */
  private String[] expandArgs(String[] originalArgv) {
    List<String> vResult1 = Lists.newArrayList();

    //
    // Expand @
    //
    for (String arg : originalArgv)
    {

      if (arg.startsWith("@"))
      {
        String fileName = arg.substring(1);
        vResult1.addAll(Util.readFile(fileName));
      } else
      {
        List<String> expanded = expandDynamicArg(arg);
        vResult1.addAll(expanded);
      }
    }

    // Expand separators
    //
    List<String> vResult2 = Lists.newArrayList();
    for (int i = 0; i < vResult1.size(); i++)
    {
      String arg = vResult1.get(i);
      String[] v1 = vResult1.toArray(new String[0]);
      if (isOption(v1, arg))
      {
        String sep = getSeparatorFor(v1, arg);
        if (!" ".equals(sep))
        {
          String[] sp = arg.split("[" + sep + "]", 2);
          for (String ssp : sp)
          {
            vResult2.add(ssp);
          }
        } else
        {
          vResult2.add(arg);
        }
      } else
      {
        vResult2.add(arg);
      }
    }

    return vResult2.toArray(new String[vResult2.size()]);
  }

  private List<String> expandDynamicArg(String arg) {
    for (ParameterDescription pd : keyDescriptionMap.values())
    {
      if (pd.isDynamicParameter())
      {
        for (String name : pd.names())
        {
          if (arg.startsWith(name) && !arg.equals(name))
          {
            return Arrays.asList(name, arg.substring(name.length()));
          }
        }
      }
    }

    return Arrays.asList(arg);
  }

  private JCommander findCommand(ProgramName name) {
    return Util.findInMap(nameCommandMap, name, caseSensitive, allowAbbreviations);
  }

  /*
   * Reverse lookup JCommand object by command's name or its alias
   */
  public JCommander findCommandByAlias(String commandOrAlias) {
    ProgramName progName = findCommandName(commandOrAlias);
    if (progName == null)
    {
      return null;
    }
    JCommander jc = findCommand(progName);
    if (jc == null)
    {
      throw new IllegalStateException(
          "There appears to be inconsistency in the internal command database. "
              + " This is likely a bug. Please report.");
    }
    return jc;
  }

  protected ProgramName findCommandName(String name) {
    return Util.findInMap(keyNameMap, new StringKey(name), caseSensitive, allowAbbreviations);
  }

  public <T> Class<? extends IStringConverter<T>> findConverter(Class<T> cls) {
    for (IStringConverterFactory f : converterFactories)
    {
      Class<? extends IStringConverter<T>> result = f.getConverter(cls);
      if (result != null)
        return result;
    }

    return null;
  }

  /**
   * Finds a description in the key to description map while considering case
   * and abbreviation settings.
   */
  private ParameterDescription findParameterDescription(String arg) {
    return Util.findInMap(keyDescriptionMap, new StringKey(arg), caseSensitive, allowAbbreviations);
  }

  public int getColumnSize() {
    return columnWidth;
  }

  /**
   * @return the description of the command.
   */
  public String getCommandDescription(String commandName) {
    JCommander jc = findCommandByAlias(commandName);
    if (jc == null)
    {
      throw new ParameterException("Asking description for unknown command: " + commandName);
    }

    Object arg = jc.getObjects().get(0);
    Parameters p = arg.getClass().getAnnotation(Parameters.class);
    ResourceBundle bundle = null;
    String result = null;
    if (p != null)
    {
      result = p.commandDescription();
      String bundleName = p.resourceBundle();
      if (!"".equals(bundleName))
      {
        bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
      } else
      {
        bundle = resourceBundle;
      }

      if (bundle != null)
      {
        result = Util.getI18nString(bundle, p.commandDescriptionKey(), p.commandDescription());
      }
    }

    return result;
  }

  public Map<String, JCommander> getCommands() {
    Map<String, JCommander> res = Maps.newLinkedHashMap();
    for (Map.Entry<ProgramName, JCommander> entry : nameCommandMap.entrySet())
    {
      res.put(entry.getKey().m_name, entry.getValue());
    }
    return res;
  }

  /**
   * If arg is an option, we can look it up directly, but if it's a value, we
   * need to find the description for the option that precedes it.
   * 
   * TODO: this doesn't look right. Does it assume that an argument value can
   * only appear once? search backwards instead?
   */
  private ParameterDescription getDescriptionFor(String[] args, String arg) {
    ParameterDescription result = getPrefixDescriptionFor(arg);
    if (result != null)
      return result;

    for (String a : args)
    {
      ParameterDescription pd = getPrefixDescriptionFor(arg);
      if (pd != null)
        result = pd;
      if (a.equals(arg))
        return result;
    }

    // TODO: how can we get here if arg is one of args?
    throw new ParameterException("Unknown parameter: " + arg);
  }

  public Map<IKey, ParameterDescription> getKeyDescriptionMap() {
    return keyDescriptionMap;
  }

  /**
   * @return the main parameter description or null if none is defined.
   */
  public ParameterDescription getMainDescription() {
    return mainDescription;
  }

  /**
   * @return the field that's meant to receive all the parameters that are not
   *         options.
   * 
   * @param arg
   *          the arg that we're about to add (only passed here to output a
   *          meaningful error message).
   */
  private List<?> getMainParameter(String arg) {
    if (mainDescription == null)
    {
      throw new ParameterException("Was passed main parameter '" + arg
          + "' but no main parameter was defined");
    }

    List<?> result = (List<?>) mainDescription.getValue();
    if (result == null)
    {
      result = Lists.newArrayList();
      if (!List.class.isAssignableFrom(mainDescription.getType()))
      {
        throw new ParameterException("Main parameter field " + mainDescription
            + " needs to be of type List, not " + mainDescription.getType());
      }
      mainDescription.setValue(result);
    }
    if (!mainInitialized)
    {
      result.clear();
      mainInitialized = true;
    }
    return result;
  }

  public String getMainParameterDescription() {
    if (keyDescriptionMap == null)
      createDescriptions();
    return mainDescription != null ? mainDescription.getParameter().description() : null;
  }

  public Map<ProgramName, JCommander> getNameCommandMap() {
    return nameCommandMap;
  }

  /**
   * @return the objects that JCommander will fill with the result of parsing
   *         the command line.
   */
  public List<Object> getObjects() {
    return configObjects;
  }

  /**
   * Tries to find a ParameterDescription for the arg and if it has a Parameters
   * annotation it returns its set prefix (which could just be the default).
   * Otherwise, it concatenates all prefixes from the Parameters of all
   * configObjects and if this is still empty, it returns the default "-"
   * prefix. In other words, the specific prefix for this arg's object, or a
   * string of all possible prefixes.
   * 
   * TODO: should this be more strict?
   */
  private String getOptionPrefixes(String[] args, String arg) {
    ParameterDescription pd = getDescriptionFor(args, arg);

    // Could be null if only main parameters were passed
    if (pd != null)
    {
      Parameters p = pd.getConfigObject().getClass().getAnnotation(Parameters.class);
      if (p != null)
        return p.optionPrefixes();
    }
    String result = Parameters.DEFAULT_OPTION_PREFIXES;

    // See if any of the objects contains a @Parameters(optionPrefixes)
    StringBuilder sb = new StringBuilder();
    for (Object o : configObjects)
    {
      Parameters p = o.getClass().getAnnotation(Parameters.class);
      if (p != null && !Parameters.DEFAULT_OPTION_PREFIXES.equals(p.optionPrefixes()))
      {
        sb.append(p.optionPrefixes());
      }
    }

    if (!Util.isStringEmpty(sb.toString()))
    {
      result = sb.toString();
    }

    return result;
  }

  Comparator<? super ParameterDescription> getParameterDescriptionComparator() {
    return descriptionComparator;
  }

  /**
   * @return a Collection of all the \@Parameter annotations found on the target
   *         class. This can be used to display the usage() in a different
   *         format (e.g. HTML).
   */
  public List<ParameterDescription> getParameterDescriptions() {

    ArrayList<ParameterDescription> descriptions = null;
    if (keyDescriptionMap != null)
    {
      descriptions = new ArrayList<ParameterDescription>(new HashSet<ParameterDescription>(
          keyDescriptionMap.values()));
    }
    return descriptions;
  }

  /**
   * The name of the command or the alias in the form it was passed to the
   * command line. <code>null</code> if no command or alias was specified.
   * 
   * @return Name of command or alias passed to command line. If none passed:
   *         <code>null</code>.
   */
  public String getParsedAlias() {
    return parsedAliasString;
  }

  public String getParsedCommand() {
    return parsedCommandString;
  }

  private ParameterDescription getPrefixDescriptionFor(String arg) {
    for (Map.Entry<IKey, ParameterDescription> es : keyDescriptionMap.entrySet())
    {
      // TODO: should this consider the allAbreviations setting?
      if (arg.startsWith(es.getKey().getName()))
        return es.getValue();
    }

    return null;
  }

  public ProgramName getProgramName() {
    return this.programName;
  }

  private String getSeparatorFor(String[] args, String arg) {
    ParameterDescription pd = getDescriptionFor(args, arg);

    // Could be null if only main parameters were passed
    if (pd != null)
    {
      Parameters p = pd.getConfigObject().getClass().getAnnotation(Parameters.class);
      if (p != null)
        return p.separators();
    }

    return " ";
  }

  public List<String> getUnknownOptions() {
    return unknownArgs;
  }

  private void initializeDefaultValue(ParameterDescription pd) {
    for (String optionName : pd.names())
    {
      String def = defaultProvider.getDefaultValueFor(optionName);
      if (def != null)
      {
        print("Initializing " + optionName, " with default value:" + def);
        pd.addValue(def, true /* default */);
        return;
      }
    }
  }

  private void initializeDefaultValues() {
    if (defaultProvider != null)
    {
      for (ParameterDescription pd : keyDescriptionMap.values())
      {
        initializeDefaultValue(pd);
      }

      for (Map.Entry<ProgramName, JCommander> entry : nameCommandMap.entrySet())
      {
        entry.getValue().initializeDefaultValues();
      }
    }
  }

  /**
   * If arg's first character starts with a character that is in the
   * "prefixes string".
   */
  private boolean isOption(String[] args, String arg) {
    String prefixes = getOptionPrefixes(args, arg);
    return arg.length() > 0 && prefixes.indexOf(arg.charAt(0)) >= 0;
  }

  protected void parse(boolean validate, String... args) {
    StringBuilder sb = new StringBuilder("Parsing \"");
    sb.append(Util.join(args).append("\"\n  with:").append(Util.join(configObjects.toArray())));
    print(sb.toString(), "");

    if (keyDescriptionMap == null)
      createDescriptions();
    initializeDefaultValues();
    parseValues(expandArgs(args), validate);
    if (validate)
      validateOptions();
  }

  /**
   * Parse and validate the command line parameters.
   */
  public void parse(String... args) {
    parse(true /* validate */, args);
  }

  /**
   * Main method that parses the values and initializes the fields accordingly.
   */
  protected void parseValues(String[] args, boolean validate) {
    // This boolean becomes true if we encounter a command, which indicates we
    // need
    // to stop parsing (the parsing of the command will be done in a sub
    // JCommander
    // object)
    boolean commandParsed = false;
    int i = 0;
    while (i < args.length && !commandParsed)
    {
      String originalArg = args[i];
      String cleanArg = Util.trim(originalArg);
      args[i] = cleanArg;
      print("Parsing arg: ", cleanArg);

      // commands stop parsing but if the command map is empty, the
      JCommander command = findCommandByAlias(originalArg);
      int increment = 1;
      if (isOption(args, cleanArg) && command == null)
      {
        // Option
        ParameterDescription pd = findParameterDescription(cleanArg);

        if (pd != null)
        {
          if (pd.password())
          {
            // Password option, use the Console to retrieve the password
            char[] password = readPassword(pd.getDescription(), pd.echoInput());
            pd.addValue(new String(password));
            requiredDescriptions.remove(pd);
          } else
          {
            if (pd.variableArity())
            {
              // Variable arity?
              increment = processVariableArity(args, i, pd);
            } else
            {
              // Regular option
              Class<?> fieldType = pd.getType();

              // Boolean, set to true as soon as we see it, unless it specified
              // an arity of 1, in which case we need to read the next value
              if ((fieldType == boolean.class || fieldType == Boolean.class) && pd.arity() == -1)
              {
                pd.addValue("true");
                requiredDescriptions.remove(pd);
              } else
              {
                increment = processFixedArity(args, i, pd, fieldType);
              }
              // If it's a help option, remember for later
              if (pd.isHelp())
              {
                helpSpecified = true;
              }
            }
          }
        } else
        // we get here if the argument looks like an option but it is not a
        // known option because the descriptor was null.
        {
          if (allowUnknownArgs)
          {
            unknownArgs.add(originalArg);
            i++;
            while (i < args.length && !isOption(args, args[i]))
            {
              unknownArgs.add(args[i++]);
            }
            increment = 0;
          } else
          {
            throw new ParameterException("Unknown option: " + originalArg);
          }
        }
      } else
      // we get here if the argument does not look like an option, or, the
      // argument matches a command name.
      {
        // Main parameter
        if (!Util.isStringEmpty(originalArg))
        {
          if (nameCommandMap.isEmpty())
          // one way to exclude a command possibility, if there are not commands
          // at all.
          {
            // Regular (non-command) parsing
            List mp = getMainParameter(originalArg);
            String value = cleanArg; // If there's a non-quoted version, prefer
                                     // that
            // one
            Object convertedValue = value;

            if (mainDescription.getGenericType() instanceof ParameterizedType)
            {
              ParameterizedType p = (ParameterizedType) mainDescription.getGenericType();
              Type cls = p.getActualTypeArguments()[0];
              if (cls instanceof Class)
              {
                convertedValue = Util.convertValue(mainDescription, (Class) cls, value);
              }
            }

            Util.validateParameter(mainDescription, mainDescription.getParameter().validateWith(),
                "Default", value);

            mainDescription.setAssigned(true);
            mp.add(convertedValue);
          } else
          // we'll assume it is a command at this point. Commands win and
          // override
          {
            // Command parsing
            if (command == null && validate)
            {
              throw new MissingCommandException("Expected a command, got " + originalArg);
            } else if (command != null)
            {
              parsedCommandString = command.programName.m_name;
              parsedAliasString = originalArg; // preserve the original form

              // Found a valid command, ask it to parse the remainder of the
              // arguments.
              // Setting the boolean commandParsed to true will force the
              // current
              // loop to end.
              command.parse(subArray(args, i + 1));
              commandParsed = true;
            }
          }
        }
      }
      i += increment;
    }
  }

  /**
   * Parse the command line parameters without validating them.
   */
  public void parseWithoutValidation(String... args) {
    parse(false /* no validation */, args);
  }

  private void print(String string, Object string2) {
    if (this.verbose > 0 || System.getProperty(Util.DEBUG_PROPERTY) != null)
    {
      Util.getConsole().println("[JCommander] " + string + string2);
    }
  }

  private int processFixedArity(String[] args, int index, ParameterDescription pd,
      Class<?> fieldType) {
    // Regular parameter, use the arity to tell use how many values
    // we need to consume
    int arity = pd.arity();
    int n = (arity != -1 ? arity : 1);

    return processFixedArity(args, index, pd, fieldType, n);
  }

  private int processFixedArity(String[] args, int originalIndex, ParameterDescription pd,
      Class<?> fieldType, int arity) {
    int index = originalIndex;
    String arg = args[index];
    // Special case for boolean parameters of arity 0
    if (arity == 0
        && (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)))
    {
      pd.addValue("true");
      requiredDescriptions.remove(pd);
    } else if (index < args.length - 1)
    {
      int offset = "--".equals(args[index + 1]) ? 1 : 0;

      if (index + arity < args.length)
      {
        for (int j = 1; j <= arity; j++)
        {
          pd.addValue(Util.trim(args[index + j + offset]));
          requiredDescriptions.remove(pd);
        }
        index += arity + offset;
      } else
      {
        throw new ParameterException("Expected " + arity + " values after " + arg);
      }
    } else
    {
      throw new ParameterException("Expected a value after parameter " + arg);
    }

    return arity + 1;
  }

  /**
   * @return the number of options that were processed.
   */
  private int processVariableArity(String[] args, int index, ParameterDescription pd) {
    Object configObject = pd.getConfigObject();
    IVariableArity va;
    if (!(configObject instanceof IVariableArity))
    {
      va = DEFAULT_VARIABLE_ARITY;
    } else
    {
      va = (IVariableArity) configObject;
    }

    List<String> currentArgs = Lists.newArrayList();
    for (int j = index + 1; j < args.length; j++)
    {
      currentArgs.add(args[j]);
    }
    int arity = va.processVariableArity(pd.names()[0], currentArgs.toArray(new String[0]));

    int result = processFixedArity(args, index, pd, List.class, arity);
    return result;
  }

  private void putKeyDescription(String name, ParameterDescription pd) {
    if (!caseSensitive)
    {
      name = name.toLowerCase();
    }
    if (allowAbbreviations)
    {
      for (Entry<IKey, ParameterDescription> entry : keyDescriptionMap.entrySet())
      {
        if (entry.getKey().getName().startsWith(name) || name.startsWith(entry.getKey().getName()))
        {
          throw new ParameterException("Option ambiguous with abbreviations. Option: " + name
              + " matches " + entry.getKey().getName());
        }
      }
    }
    keyDescriptionMap.put(new StringKey(name), pd);
  }

  protected void putNameCommand(String name, JCommander jc) {

  }

  /**
   * Invoke Console.readPassword through reflection to avoid depending on Java
   * 6.
   */
  private char[] readPassword(String description, boolean echoInput) {
    Util.getConsole().print(description + ": ");
    return Util.getConsole().readPassword(echoInput);
  }

  protected void registerAliases(String name, ProgramName programName, String... aliases) {
    /*
     * Register aliases
     */
    // register command name as an alias of itself for reverse lookup
    // Note: Name clash check is intentionally omitted to resemble the
    // original behaviour of clashing commands.
    // Aliases are, however, are strictly checked for name clashes.
    keyNameMap.put(new StringKey(name), programName);
    for (String a : aliases)
    {
      IKey alias = new StringKey(a);
      // omit pointless aliases to avoid name clash exception
      if (!alias.equals(name))
      {
        ProgramName mappedName = keyNameMap.get(alias);
        if (mappedName != null && !mappedName.equals(programName))
        {
          throw new ParameterException("Cannot set alias " + alias + " for " + name
              + " command because it has already been defined for " + mappedName.m_name
              + " command");
        }
        keyNameMap.put(alias, programName);
      }
    }
  }

  public void setAcceptUnknownOptions(boolean b) {
    allowUnknownArgs = b;
  }

  public void setAllowAbbreviatedOptions(boolean b) {
    allowAbbreviations = b;
  }

  public void setCaseSensitiveOptions(boolean b) {
    caseSensitive = b;
  }

  public void setColumnSize(int columnSize) {
    columnWidth = columnSize;
  }

  /**
   * Define the default provider for this instance.
   */
  public void setDefaultProvider(IDefaultProvider defaultProvider) {
    this.defaultProvider = defaultProvider;

    for (Map.Entry<ProgramName, JCommander> entry : nameCommandMap.entrySet())
    {
      entry.getValue().setDefaultProvider(defaultProvider);
    }
  }

  /**
   * Sets the {@link ResourceBundle} to use for looking up descriptions. Set
   * this to <code>null</code> to use description text directly.
   */
  // declared final since this is invoked from constructors
  public final void setDescriptionsBundle(ResourceBundle bundle) {
    resourceBundle = bundle;
  }

  public void setParameterDescriptionComparator(Comparator<? super ParameterDescription> c) {
    descriptionComparator = c;
  }

  /**
   * Set the program name (used only in the usage).
   */
  public void setProgramName(String name) {
    setProgramName(name, new String[0]);
  }

  /**
   * 
   * 
   * 
   * Set the program name
   * 
   * @param name
   *          program name
   * @param aliases
   *          aliases to the program name
   */
  public void setProgramName(String name, String... aliases) {
    programName = new ProgramName(name, Arrays.asList(aliases));
  }

  public void setVerbose(int verbose) {
    this.verbose = verbose;
  }

  private String[] subArray(String[] args, int index) {
    int l = args.length - index;
    String[] result = new String[l];
    System.arraycopy(args, index, result, 0, l);

    return result;
  }

  /**
   * Make sure that all the required parameters have received a value.
   */
  private void validateOptions() {
    // No validation if we found a help parameter
    if (helpSpecified)
    {
      return;
    }

    if (!requiredDescriptions.isEmpty())
    {
      StringBuilder missingFields = new StringBuilder();
      for (ParameterDescription pd : requiredDescriptions)
      {
        missingFields.append(pd.getNames()).append(" ");
      }
      throw new ParameterException("The following "
          + (requiredDescriptions.size() == 1 ? "option is required: " : "options are required: ")
          + missingFields);
    }

    if (mainDescription != null)
    {
      if (mainDescription.required() && !mainDescription.isAssigned())
      {
        throw new ParameterException("Main parameters are required (\""
            + mainDescription.getDescription() + "\")");
      }
    }
  }
}
