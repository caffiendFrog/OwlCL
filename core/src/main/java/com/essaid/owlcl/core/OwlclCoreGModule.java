package com.essaid.owlcl.core;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * 
 * 
 */
public final class OwlclCoreGModule extends AbstractModule implements OwlclGModule {

  @Override
  protected void configure() {

    
    
    Iterator<OwlclGModule> moduleIterator = ServiceLoader.load(OwlclGModule.class).iterator();

    System.out.println("=============  loading modules ===============");
    while (moduleIterator.hasNext())
    {
      Module module = moduleIterator.next();
      System.out.println("=============  loading " + module.getClass().getName());
      install(module);
    }

  }

}
