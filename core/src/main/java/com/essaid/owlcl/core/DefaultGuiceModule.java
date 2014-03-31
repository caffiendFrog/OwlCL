package com.essaid.owlcl.core;

import com.essaid.owlcl.core.util.DefaultLogConfigurator;
import com.essaid.owlcl.core.util.ILogConfigurator;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DefaultGuiceModule extends AbstractModule implements OwlclGuiceModule {

  @Override
  protected void configure() {
    bind(ILogConfigurator.class).to(DefaultLogConfigurator.class).in(Scopes.SINGLETON);

  }

}
