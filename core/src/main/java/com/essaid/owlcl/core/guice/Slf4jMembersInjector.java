package com.essaid.owlcl.core.guice;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.MembersInjector;

/**
 * http://glauche.de/2009/08/24/logging-with-slf4j-and-guice/
 * 
 * @author Shahim Essaid
 * 
 * @param <T>
 */
public class Slf4jMembersInjector<T> implements MembersInjector<T> {

  private final Field field;
  private final Logger logger;

  Slf4jMembersInjector(Field aField) {
    field = aField;
    logger = LoggerFactory.getLogger(field.getDeclaringClass());
    field.setAccessible(true);
  }

  public void injectMembers(T anArg0) {
    try
    {
      field.set(anArg0, logger);
    } catch (IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
  }
}