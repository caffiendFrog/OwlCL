package com.beust.jcommander;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.beust.jcommander.converters.IParameterSplitter;
import com.beust.jcommander.converters.NoConverter;
import com.beust.jcommander.converters.StringConverter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.beust.jcommander.internal.JDK6Console;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.validators.NoValidator;
import com.beust.jcommander.validators.NoValueValidator;

public class Util {

  private static Console m_console;

  public static Console getConsole() {
    if (m_console == null)
    {
      try
      {
        Method consoleMethod = System.class.getDeclaredMethod("console", new Class<?>[0]);
        Object console = consoleMethod.invoke(null, new Object[0]);
        m_console = new JDK6Console(console);
      } catch (Throwable t)
      {
        m_console = new DefaultConsole();
      }
    }
    return m_console;
  }

  /**
   * @return n spaces
   */
  public static String getSpaces(int count) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++)
    {
      result.append(" ");
    }
    return result.toString();
  }

  public static final String DEBUG_PROPERTY = "jcommander.debug";

  public static StringBuilder join(Object[] args) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < args.length; i++)
    {
      if (i > 0)
        result.append(" ");
      result.append(args[i]);
    }
    return result;
  }

  /**
   * Reads the file specified by filename and returns the file content as a
   * string. End of lines are replaced by a space.
   * 
   * @param fileName
   *          the command line filename
   * @return the file content as a string.
   */
  static List<String> readFile(String fileName) {
    List<String> result = Lists.newArrayList();

    try
    {
      BufferedReader bufRead = new BufferedReader(new FileReader(fileName));

      String line;

      // Read through file one line at time. Print line # and line
      while ((line = bufRead.readLine()) != null)
      {
        // Allow empty lines in these at files
        if (line.length() > 0)
          result.add(line.trim());
      }

      bufRead.close();
    } catch (IOException e)
    {
      throw new ParameterException("Could not read file " + fileName + ": " + e);
    }

    return result;
  }

  /**
   * Remove spaces at both ends and handle double quotes.
   */
  static String trim(String string) {
    String result = string.trim();
    if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1)
    {
      result = result.substring(1, result.length() - 1);
    }
    return result;
  }

  /**
   * @return The internationalized version of the string if available, otherwise
   *         return def.
   */
  static String getI18nString(ResourceBundle bundle, String key, String def) {
    String s = bundle != null ? bundle.getString(key) : null;
    return s != null ? s : def;
  }

  static void wrapDescription(StringBuilder out, int indent, String description, int columnSize) {
    int max = columnSize;
    String[] words = description.split(" ");
    int current = indent;
    int i = 0;
    while (i < words.length)
    {
      String word = words[i];
      if (word.length() > max || current + word.length() <= max)
      {
        out.append(" ").append(word);
        current += word.length() + 1;
      } else
      {
        out.append("\n").append(getSpaces(indent + 1)).append(word);
        current = indent;
      }
      i++;
    }
  }

  static IStringConverter<?> instantiateConverter(String optionName,
      Class<? extends IStringConverter<?>> converterClass) throws IllegalArgumentException,
      InstantiationException, IllegalAccessException, InvocationTargetException {
    Constructor<IStringConverter<?>> ctor = null;
    Constructor<IStringConverter<?>> stringCtor = null;
    Constructor<IStringConverter<?>>[] ctors = (Constructor<IStringConverter<?>>[]) converterClass
        .getDeclaredConstructors();
    for (Constructor<IStringConverter<?>> c : ctors)
    {
      Class<?>[] types = c.getParameterTypes();
      if (types.length == 1 && types[0].equals(String.class))
      {
        stringCtor = c;
      } else if (types.length == 0)
      {
        ctor = c;
      }
    }

    IStringConverter<?> result = stringCtor != null ? stringCtor.newInstance(optionName)
        : (ctor != null ? ctor.newInstance() : null);

    return result;
  }

  static boolean isEmpty(String s) {
    return s == null || "".equals(s);
  }

  /**
   * Find the resource bundle in the annotations.
   * 
   * @return
   */
  @SuppressWarnings("deprecation")
  static ResourceBundle findResourceBundle(Object o) {
    ResourceBundle result = null;

    Parameters p = o.getClass().getAnnotation(Parameters.class);
    if (p != null && !isEmpty(p.resourceBundle()))
    {
      result = ResourceBundle.getBundle(p.resourceBundle(), Locale.getDefault());
    } else
    {
      com.beust.jcommander.ResourceBundle a = o.getClass().getAnnotation(
          com.beust.jcommander.ResourceBundle.class);
      if (a != null && !isEmpty(a.value()))
      {
        result = ResourceBundle.getBundle(a.value(), Locale.getDefault());
      }
    }

    return result;
  }

  /*
   * Creates a new collection for the field's type.
   * 
   * Currently only List and Set are supported. Support for Queues and Stacks
   * could be useful.
   */
  @SuppressWarnings("unchecked")
  static Collection<Object> newCollection(Class<?> type) {
    if (SortedSet.class.isAssignableFrom(type))
      return new TreeSet();
    else if (LinkedHashSet.class.isAssignableFrom(type))
      return new LinkedHashSet();
    else if (Set.class.isAssignableFrom(type))
      return new HashSet();
    else if (List.class.isAssignableFrom(type))
      return new ArrayList();
    else
    {
      throw new ParameterException("Parameters of Collection type '" + type.getSimpleName()
          + "' are not supported. Please use List or Set instead.");
    }
  }

  public static List<ParameterDescription> getConfigObjectDescriptions(JCommander jCommander,
      Object configObject, ResourceBundle resourceBundle) {
    List<ParameterDescription> descriptions = Lists.newArrayList();

    Class<? extends Object> cls = configObject.getClass();
    while (!Object.class.equals(cls))
    {
      for (Field f : cls.getDeclaredFields())
      {
        Annotation annotation = f.getAnnotation(Parameter.class);
        Annotation delegateAnnotation = f.getAnnotation(ParametersDelegate.class);
        Annotation dynamicParameter = f.getAnnotation(DynamicParameter.class);
        if (annotation != null)
        {
          descriptions.add(new ParameterDescription(configObject, (Parameter) annotation, f, null,
              resourceBundle, jCommander));
        } else if (dynamicParameter != null)
        {
          descriptions.add(new ParameterDescription(configObject,
              (DynamicParameter) dynamicParameter, f, null, resourceBundle, jCommander));
        } else if (delegateAnnotation != null)
        {
          Object delegate = Util.getValue(configObject, null, f);
          if (delegate == null)
          {
            throw new ParameterException("Delegate annotation on field " + f.getName()
                + " with delegate annotation " + delegateAnnotation + " is not allowed to be null.");
          }
          if (Map.class.isAssignableFrom(delegate.getClass()))
          {
            for (Object key : ((Map) delegate).keySet())
            {
              Object value = ((Map) delegate).get(key);
              if (key != value)
              {
                continue;
              }
              if (key.getClass().isAssignableFrom(ParameterGroup.class))
              {
                descriptions.addAll(getConfigObjectDescriptions(jCommander, value, resourceBundle));
              }
            }
          } else
          {
            descriptions.addAll(getConfigObjectDescriptions(jCommander, delegate, resourceBundle));
          }

        }
      }
      cls = cls.getSuperclass();
    }

    // Reassigning
    cls = configObject.getClass();
    while (!Object.class.equals(cls))
    {
      for (Method m : cls.getDeclaredMethods())
      {
        Annotation annotation = m.getAnnotation(Parameter.class);
        Annotation delegateAnnotation = m.getAnnotation(ParametersDelegate.class);
        Annotation dynamicParameter = m.getAnnotation(DynamicParameter.class);
        if (annotation != null)
        {
          descriptions.add(new ParameterDescription(configObject, (Parameter) annotation, null, m,
              resourceBundle, jCommander));
        } else if (dynamicParameter != null)
        {
          descriptions.add(new ParameterDescription(configObject,
              (DynamicParameter) dynamicParameter, null, m, resourceBundle, jCommander));
        } else if (delegateAnnotation != null)
        {
          Object delegate = Util.getValue(configObject, m, null);
          if (delegate == null)
          {
            throw new ParameterException("Delegate annotation on method " + m.getName()
                + " with delegate annotation " + delegateAnnotation + " is not allowed to be null.");
          }
          descriptions.addAll(getConfigObjectDescriptions(jCommander, delegate, resourceBundle));
        }
      }
      cls = cls.getSuperclass();
    }
    return descriptions;
  }

  public static String getName(Method setter, Field field) {
    if (setter != null)
    {
      return setter.getName();
    } else
    {
      return field.getName();
    }
  }

  public static Object getValue(Object object, Method setter, Field field) {
    Method getter = null;
    try
    {
      if (setter != null)
      {
        if (getter == null)
        {
          getter = setter.getDeclaringClass().getMethod("g" + setter.getName().substring(1),
              new Class[0]);
        }
        return getter.invoke(object);
      } else
      {
        field.setAccessible(true);
        return field.get(object);
      }
    } catch (SecurityException e)
    {
      throw new ParameterException(e);
    } catch (NoSuchMethodException e)
    {
      // Try to find a field
      String name = setter.getName();
      String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
      Object result = null;
      try
      {
        Field field2 = setter.getDeclaringClass().getDeclaredField(fieldName);
        if (field2 != null)
        {
          field2.setAccessible(true);
          result = field2.get(object);
        }
      } catch (NoSuchFieldException ex)
      {
        // ignore
      } catch (IllegalAccessException ex)
      {
        // ignore
      }
      return result;
    } catch (IllegalArgumentException e)
    {
      throw new ParameterException(e);
    } catch (IllegalAccessException e)
    {
      throw new ParameterException(e);
    } catch (InvocationTargetException e)
    {
      throw new ParameterException(e);
    }
  }

  public static void validateValueParameter(Class<? extends IValueValidator> validator,
      String name, Object value) {
    try
    {
      if (validator != NoValueValidator.class)
      {
        Util.print("Validating value parameter:" + name + " value:" + value + " validator:"
            + validator);
      }
      validator.newInstance().validate(name, value);
    } catch (InstantiationException e)
    {
      throw new ParameterException("Can't instantiate validator:" + e);
    } catch (IllegalAccessException e)
    {
      throw new ParameterException("Can't instantiate validator:" + e);
    }
  }

  public static void validateParameter(ParameterDescription pd,
      Class<? extends IParameterValidator> validator, String name, String value) {
    try
    {
      if (validator != NoValidator.class)
      {
        Util.print("Validating parameter:" + name + " value:" + value + " validator:" + validator);
      }
      validator.newInstance().validate(name, value);
      if (IParameterValidator2.class.isAssignableFrom(validator))
      {
        IParameterValidator2 instance = (IParameterValidator2) validator.newInstance();
        instance.validate(name, value, pd);
      }
    } catch (InstantiationException e)
    {
      throw new ParameterException("Can't instantiate validator:" + e);
    } catch (IllegalAccessException e)
    {
      throw new ParameterException("Can't instantiate validator:" + e);
    } catch (ParameterException ex)
    {
      throw ex;
    } catch (Exception ex)
    {
      throw new ParameterException(ex);
    }
  }

  static void print(String string) {
    if (System.getProperty(DEBUG_PROPERTY) != null)
    {
      getConsole().println("[ParameterDescription] " + string);
    }
  }

  public static <V> V findInMap(Map<? extends IKey, V> map, IKey name, boolean caseSensitive,
      boolean allowAbbreviations) {
    if (allowAbbreviations)
    {
      return Util.findAbbreviatedValue(map, name, caseSensitive);
    } else
    {
      if (caseSensitive)
      {
        return map.get(name);
      } else
      {
        for (IKey c : map.keySet())
        {
          if (c.getName().equalsIgnoreCase(name.getName()))
          {
            return map.get(c);
          }
        }
      }
    }
    return null;
  }

  static <V> V findAbbreviatedValue(Map<? extends IKey, V> map, IKey name, boolean caseSensitive) {
    String string = name.getName();
    Map<String, V> results = Maps.newHashMap();
    for (IKey c : map.keySet())
    {
      String n = c.getName();
      boolean match = (caseSensitive && n.startsWith(string))
          || ((!caseSensitive) && n.toLowerCase().startsWith(string.toLowerCase()));
      if (match)
      {
        results.put(n, map.get(c));
      }
    }

    V result;
    if (results.size() > 1)
    {
      throw new ParameterException("Ambiguous option: " + name + " matches " + results.keySet());
    } else if (results.size() == 1)
    {
      result = results.values().iterator().next();
    } else
    {
      result = null;
    }

    return result;
  }

  public static boolean isStringEmpty(String s) {
    return s == null || "".equals(s);
  }

  /**
   * Use the splitter to split the value into multiple values and then convert
   * each of them individually.
   */
  static Object convertToList(String value, IStringConverter<?> converter,
      Class<? extends IParameterSplitter> splitterClass) throws InstantiationException,
      IllegalAccessException {
    IParameterSplitter splitter = splitterClass.newInstance();
    List<Object> result = Lists.newArrayList();
    for (String param : splitter.split(value))
    {
      result.add(converter.convert(param));
    }
    return result;
  }

  public static Object convertValue(ParameterDescription pd, String value) {
    return convertValue(pd, pd.getType(), value);
  }

  /**
   * @param type
   *          The type of the actual parameter
   * @param value
   *          The value to convert
   */
  public static Object convertValue(ParameterDescription description, Class type, String value) {
    Parameter annotation = description.getParameter();

    // Do nothing if it's a @DynamicParameter
    if (annotation == null)
      return value;

    Class<? extends IStringConverter<?>> converterClass = annotation.converter();
    boolean listConverterWasSpecified = annotation.listConverter() != NoConverter.class;

    //
    // Try to find a converter on the annotation
    //
    if (converterClass == null || converterClass == NoConverter.class)
    {
      // If no converter specified and type is enum, used enum values to convert
      if (type.isEnum())
      {
        converterClass = type;
      } else
      {
        converterClass = description.getJCommander().findConverter(type);
      }
    }

    if (converterClass == null)
    {
      Type elementType = description.findFieldGenericType();
      converterClass = elementType != null ? description.getJCommander().findConverter(
          (Class<? extends IStringConverter<?>>) elementType) : StringConverter.class;
      // Check for enum type parameter
      if (converterClass == null && Enum.class.isAssignableFrom((Class) elementType))
      {
        converterClass = (Class<? extends IStringConverter<?>>) elementType;
      }
    }

    IStringConverter<?> converter;
    Object result = null;
    try
    {
      String[] names = annotation.names();
      String optionName = names.length > 0 ? names[0] : "[Main class]";
      if (converterClass != null && converterClass.isEnum())
      {
        try
        {
          result = Enum.valueOf((Class<? extends Enum>) converterClass, value);
        } catch (IllegalArgumentException e)
        {

          try
          {
            result = Enum.valueOf((Class<? extends Enum>) converterClass, value.toUpperCase());
          } catch (Exception e2)
          {
            throw new ParameterException("Invalid value for " + optionName
                + " parameter. Allowed values:"
                + EnumSet.allOf((Class<? extends Enum>) converterClass));
          }
        }
      } else
      {
        converter = instantiateConverter(optionName, converterClass);
        if (type.isAssignableFrom(List.class)
            && description.getGenericType() instanceof ParameterizedType)
        {

          // The field is a List
          if (listConverterWasSpecified)
          {
            // If a list converter was specified, pass the value to it
            // for direct conversion
            IStringConverter<?> listConverter = instantiateConverter(optionName,
                annotation.listConverter());
            result = listConverter.convert(value);
          } else
          {
            // No list converter: use the single value converter and pass each
            // parsed value to it individually
            result = convertToList(value, converter, annotation.splitter());
          }
        } else
        {
          result = converter.convert(value);
        }
      }
    } catch (InstantiationException e)
    {
      throw new ParameterException(e);
    } catch (IllegalAccessException e)
    {
      throw new ParameterException(e);
    } catch (InvocationTargetException e)
    {
      throw new ParameterException(e);
    }

    return result;
  }

}
