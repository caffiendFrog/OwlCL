package com.essaid.owlcl.core.guice;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.essaid.owlcl.core.OwlclGuiceModule;
import com.essaid.owlcl.core.reasoner.DefaultReasonerManager;
import com.essaid.owlcl.core.reasoner.IReasonerManager;
import com.essaid.owlcl.core.util.IInitializable;
import com.essaid.owlcl.core.util.IReportFactory;
import com.essaid.owlcl.core.util.Report;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * 
 * 
 */
public final class OwlclCoreGModule extends AbstractModule implements OwlclGuiceModule {

  @Override
  protected void configure() {

    // ================================================================================
    // Custom logger injection
    // ================================================================================
    Slf4jTypeListener ll = new Slf4jTypeListener();
    bindListener(Matchers.any(), ll);

    // ================================================================================
    // Post injection initialization for IInitializable
    // ================================================================================
    final InjectionListener<IInitializable> il = new InjectionListener<IInitializable>() {

      @Override
      public void afterInjection(IInitializable injectee) {
        injectee.initialize();
      }
    };

    TypeListener tl = new TypeListener() {

      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        @SuppressWarnings("unchecked")
        TypeEncounter<IInitializable> iie = (TypeEncounter<IInitializable>) encounter;
        iie.register(il);
      }
    };

    bindListener(new AbstractMatcher<TypeLiteral<?>>() {

      public boolean matches(TypeLiteral<?> typeLiteral) {
        return IInitializable.class.isAssignableFrom(typeLiteral.getRawType());
      }
    }, tl);

    // ================================================================================
    // report factory
    // ================================================================================
    binder().install(
        new FactoryModuleBuilder().implement(Report.class, Report.class).build(
            Key.get(IReportFactory.class)));

    //================================================================================
    // Reasoner manager
    //================================================================================
    
    bind(IReasonerManager.class).to(DefaultReasonerManager.class);
    
    // ================================================================================
    // Dynamic modules
    // ================================================================================
    Iterator<OwlclGuiceModule> moduleIterator = ServiceLoader.load(OwlclGuiceModule.class)
        .iterator();
    while (moduleIterator.hasNext())
    {
      Module module = moduleIterator.next();
      install(module);
    }

  }

}
