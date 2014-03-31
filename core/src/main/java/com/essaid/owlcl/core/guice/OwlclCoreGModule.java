package com.essaid.owlcl.core.guice;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.essaid.owlcl.core.OwlclGuiceModule;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

/**
 * 
 * 
 */
public final class OwlclCoreGModule extends AbstractModule implements OwlclGuiceModule {

  @Override
  protected void configure() {

    // logger injector
    bindListener(Matchers.any(), new Slf4jTypeListener());

    // report factory
    binder().install(
        new FactoryModuleBuilder().implement(Report.class, Report.class).build(
            Key.get(IReportFactory.class)));

    // dynamic modules
    Iterator<OwlclGuiceModule> moduleIterator = ServiceLoader.load(OwlclGuiceModule.class)
        .iterator();
    while (moduleIterator.hasNext())
    {
      Module module = moduleIterator.next();
      install(module);
    }

  }

}
