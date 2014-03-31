package com.essaid.owlcl.core.guice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.essaid.owlcl.core.annotation.InjectLogger;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * http://glauche.de/2009/08/24/logging-with-slf4j-and-guice/
 * 
 * http://stackoverflow
 * .com/questions/6604071/guice-log4j-custom-injection-does-not
 * -support-logging-within-the-constructor
 * 
 * @author Shahim Essaid
 * 
 */
public class Slf4jTypeListener implements TypeListener {

  public <I> void hear(TypeLiteral<I> aTypeLiteral, TypeEncounter<I> aTypeEncounter) {

    for (Field field : aTypeLiteral.getRawType().getDeclaredFields())
    {
      if (field.getType() == Logger.class && field.isAnnotationPresent(InjectLogger.class))
      {
        // static case
        if (Modifier.isStatic(field.getModifiers()))
        {
          // use reflection
          try
          {
            field.setAccessible(true);
            Logger logger = LoggerFactory.getLogger(field.getDeclaringClass());
            field.set(null, logger);
          } catch (IllegalAccessException iae)
          {
            throw new RuntimeException("Error setting logger on static field: " + field.getName()
                + " in class: " + field.getDeclaringClass().getName());
          }
        } else
        {
          aTypeEncounter.register(new Slf4jMembersInjector<I>(field));
        }

      }
    }
  }
}