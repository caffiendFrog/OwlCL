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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class ParameterDescription {

  private Object configObject;
  private Parameter parameter;
  private DynamicParameter dynamicParameter;
  /** Keep track of whether a value was added to flag an error */
  private boolean assigned = false;
  private ResourceBundle resourceBundle;
  private String description;
  private JCommander jCommander;
  private Object defaultValue;
  /** Longest of the names(), used to present usage() alphabetically */
  private String longestName = "";
  private Field field;
  private Method setter;

  public ParameterDescription(Object object, DynamicParameter dynamicParameter, Field field,
      Method method, ResourceBundle bundle, JCommander jc) {
    this.setter = method;
    this.field = field;
    if (this.field != null)
    {
      this.field.setAccessible(true);
    }
    if (!Map.class.isAssignableFrom(getType()))
    {
      throw new ParameterException("@DynamicParameter " + getSetterOrFieldName()
          + " should be of type " + "Map but is " + getType().getName());
    }
    this.dynamicParameter = dynamicParameter;
    init(object, bundle, jc);
  }

  public ParameterDescription(Object object, Parameter annotation, Field field, Method method,
      ResourceBundle bundle, JCommander jc) {
    this.setter = method;
    this.field = field;
    if (this.field != null)
    {
      this.field.setAccessible(true);
    }
    this.parameter = annotation;
    init(object, bundle, jc);

  }

  private void initDescription(String description, String descriptionKey, String[] names) {
    this.description = description;
    if (!"".equals(descriptionKey))
    {
      if (resourceBundle != null)
      {
        this.description = resourceBundle.getString(descriptionKey);
      }
    }

    for (String name : names)
    {
      if (name.length() > longestName.length())
        longestName = name;
    }
  }

  @SuppressWarnings("unchecked")
  private void init(Object object, ResourceBundle bundle, JCommander jCommander) {
    configObject = object;
    resourceBundle = bundle;
    if (resourceBundle == null)
    {
      resourceBundle = Util.findResourceBundle(object);
    }
    this.jCommander = jCommander;

    if (parameter != null)
    {
      String description;
      if (Enum.class.isAssignableFrom(getType()) && parameter.description().isEmpty())
      {
        description = "Options: " + EnumSet.allOf((Class<? extends Enum>) getType());
      } else
      {
        description = parameter.description();
      }
      initDescription(description, parameter.descriptionKey(), parameter.names());
    } else if (dynamicParameter != null)
    {
      initDescription(dynamicParameter.description(), dynamicParameter.descriptionKey(),
          dynamicParameter.names());
    } else
    {
      throw new AssertionError("Shound never happen");
    }

    try
    {
      defaultValue = Util.getValue(object, this.setter, this.field);
    } catch (Exception e)
    {
    }

    //
    // Validate default values, if any and if applicable
    //
    if (defaultValue != null)
    {
      if (parameter != null)
      {
       // System.out.println("Parameter: "+parameter.getClass()+" "+jCommander.getProgramName());
        validateDefaultValues(parameter.names());
      }
    }
  }

  private void validateDefaultValues(String[] names) {
    String name = names.length > 0 ? names[0] : "";
    validateValueParameter(name, defaultValue);
  }

  public String getLongestName() {
    return longestName;
  }

  public Object getDefault() {
    return defaultValue;
  }

  public String getDescription() {
    return this.description;
  }

  public JCommander getJCommander() {
    return jCommander;
  }

  public Object getConfigObject() {
    return configObject;
  }

  public String getNames() {
    StringBuilder sb = new StringBuilder();
    String[] names = names();
    for (int i = 0; i < names.length; i++)
    {
      if (i > 0)
        sb.append(", ");
      if (names.length == 1 && names[i].startsWith("--"))
        sb.append("    ");
      sb.append(names[i]);
    }
    return sb.toString();
  }

  private boolean isMultiOption() {
    Class<?> fieldType = this.getType();
    return fieldType.equals(List.class) || fieldType.equals(Set.class) || this.isDynamicParameter();
  }

  public void addValue(String value) {
    addValue(value, false /* not default */);
  }

  /**
   * @return true if this parameter received a value during the parsing phase.
   */
  public boolean isAssigned() {
    return assigned;
  }

  public void setAssigned(boolean b) {
    assigned = b;
  }

  /**
   * Add the specified value to the field. First, validate the value if a
   * validator was specified. Then look up any field converter, then any type
   * converter, and if we can't find any, throw an exception.
   */
  public void addValue(String value, boolean isDefault) {
    Util.print("Adding " + (isDefault ? "default " : "") + "value:" + value + " to parameter:"
        + this.getSetterOrFieldName());
    String name = names()[0];
    if (assigned && !isMultiOption())
    {
      throw new ParameterException("Can only specify option " + name + " once.");
    }

    validateParameter(name, value);

    Class<?> type = this.getType();

    Object convertedValue = Util.convertValue(this, value);
    validateValueParameter(name, convertedValue);
    boolean isCollection = Collection.class.isAssignableFrom(type);

    if (isCollection)
    {
      @SuppressWarnings("unchecked")
      Collection<Object> l = (Collection<Object>) this.getValue();
      if (l == null || fieldIsSetForTheFirstTime(isDefault))
      {
        l = Util.newCollection(type);
        this.setValue(l);
      }
      if (convertedValue instanceof Collection)
      {
        l.addAll((Collection) convertedValue);
      } else
      { // if (isMainParameter || m_parameterAnnotation.arity() > 1) {
        l.add(convertedValue);
        // } else {
        // l.
      }
    } else
    {
      addValue(convertedValue);
    }
    if (!isDefault)
      assigned = true;
  }

  private void validateParameter(String name, String value) {
    Class<? extends IParameterValidator> validator = validateWith();
    if (validator != null)
    {
      Util.validateParameter(this, validator, name, value);
    }
  }

  private void validateValueParameter(String name, Object value) {
    Class<? extends IValueValidator> validator = validateValueWith();
    if (validator != null)
    {
      Util.validateValueParameter(validator, name, value);
    }
  }

  /*
   * Tests if its the first time a non-default value is being added to the
   * field.
   */
  private boolean fieldIsSetForTheFirstTime(boolean isDefault) {
    return (!isDefault && !assigned);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder().append("[PD:");
    builder.append(getParameter() != null ? "Parameter" : "Dynamic").append(":");
    builder.append(configObject.getClass().getSimpleName()).append(".");
    builder.append(getSetterOrFieldName()).append(":");
    if (getParameter() != null && getParameter().names().length > 0)
    {
      if (getParameter().names().length > 0)
      {
        builder.append(getParameter().names()[0]).append("]");
      } else
      {
        builder.append("_UN_NAMED_MAIN_PARAMETER_]");
      }
    }
    return builder.toString();
  }

  public boolean isDynamicParameter() {
    return dynamicParameter != null;
  }

  public boolean isHelp() {
    return parameter != null && parameter.help();
  }

  public String[] names() {
    return parameter != null ? parameter.names() : dynamicParameter.names();
  }

  public boolean required() {
    return parameter != null ? parameter.required() : dynamicParameter.required();
  }

  public boolean password() {
    return parameter != null ? parameter.password() : false;
  }

  public boolean echoInput() {
    return parameter != null ? parameter.echoInput() : false;
  }

  public boolean variableArity() {
    return parameter != null ? parameter.variableArity() : false;
  }

  public int arity() {
    return parameter != null ? parameter.arity() : 1;
  }

  public boolean hidden() {
    return parameter != null ? parameter.hidden() : dynamicParameter.hidden();
  }

  public String getAssignment() {
    return dynamicParameter != null ? dynamicParameter.assignment() : "";
  }

  public void addValue(Object value) {
    if (parameter != null)
    {
      this.setValue(value);
    } else
    {
      String a = dynamicParameter.assignment();
      String sv = value.toString();

      int aInd = sv.indexOf(a);
      if (aInd == -1)
      {
        throw new ParameterException("Dynamic parameter expected a value of the form a" + a + "b"
            + " but got:" + sv);
      }
      callPut(sv.substring(0, aInd), sv.substring(aInd + 1));
    }
  }

  private void callPut(String key, String value) {
    try
    {
      Method m;
      m = findPut(this.getType());
      m.invoke(Util.getValue(configObject, this.setter, this.field), key, value);
    } catch (SecurityException e)
    {
      e.printStackTrace();
    } catch (IllegalAccessException e)
    {
      e.printStackTrace();
    } catch (InvocationTargetException e)
    {
      e.printStackTrace();
    } catch (NoSuchMethodException e)
    {
      e.printStackTrace();
    }
  }

  private Method findPut(Class<?> cls) throws SecurityException, NoSuchMethodException {
    return cls.getMethod("put", Object.class, Object.class);
  }

  public Class<? extends IParameterValidator> validateWith() {
    return parameter != null ? parameter.validateWith() : dynamicParameter.validateWith();
  }

  public Class<? extends IValueValidator> validateValueWith() {
    return parameter != null ? parameter.validateValueWith() : dynamicParameter.validateValueWith();
  }

  public Type getGenericType() {
    if (setter != null)
    {
      return setter.getGenericParameterTypes()[0];
    } else
    {
      return field.getGenericType();
    }
  }

  public Parameter getParameter() {

    return parameter;
  }

  public DynamicParameter getDynamicParameter() {
    return dynamicParameter;
  }

  public Class<?> getType() {
    if (setter != null)
    {
//      System.out.println("=============== GETTING TYPE FOR: "+ setter.getName());
      return setter.getParameterTypes()[0];
    } else
    {
      return field.getType();
    }
  }

  public void setValue(Object value) {
    try
    {
      if (setter != null)
      {
        setter.invoke(configObject, value);
      } else
      {
        field.set(configObject, value);
      }
    } catch (IllegalArgumentException ex)
    {
      throw new ParameterException(ex);
    } catch (IllegalAccessException ex)
    {
      throw new ParameterException(ex);
    } catch (InvocationTargetException ex)
    {
      // If a ParameterException was thrown, don't wrap it into another one
      if (ex.getTargetException() instanceof ParameterException)
      {
        throw (ParameterException) ex.getTargetException();
      } else
      {
        throw new ParameterException(ex);
      }
    }
  }

  public Type findFieldGenericType() {
    if (setter != null)
    {
      return null;
    } else
    {
      if (field.getGenericType() instanceof ParameterizedType)
      {
        ParameterizedType p = (ParameterizedType) field.getGenericType();
        Type cls = p.getActualTypeArguments()[0];
        if (cls instanceof Class)
        {
          return cls;
        }
      }
    }

    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ParameterDescription other = (ParameterDescription) obj;
    if (field == null)
    {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if (setter == null)
    {
      if (other.setter != null)
        return false;
    } else if (!setter.equals(other.setter))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + ((setter == null) ? 0 : setter.hashCode());
    return result;
  }

  public String getSetterOrFieldName() {
    if (setter != null)
    {
      return setter.getName();
    } else
    {
      return field.getName();
    }
  }

  public Object getValue() {
    return Util.getValue(configObject, setter, field);
  }

}
