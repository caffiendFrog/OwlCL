package com.essaid.owlcl.core;

import com.google.inject.AbstractModule;

public class DefaultGuiceModule extends AbstractModule implements OwlclGuiceModule {

  @Override
  protected void configure() {
//    bind(ILogConfigurator.class).to(DefaultLogConfigurator.class).in(Scopes.SINGLETON);

  }

}
