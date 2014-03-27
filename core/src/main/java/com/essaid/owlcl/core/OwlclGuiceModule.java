package com.essaid.owlcl.core;

import com.essaid.owlcl.command.CatalogCommand;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.essaid.owlcl.core.command.MainCommand;
import com.essaid.owlcl.core.util.CoreUtils;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Based on
 * http://stackoverflow.com/questions/902639/has-anyone-used-serviceloader-
 * together-with-guice
 * 
 * 
 */
public class OwlclGuiceModule extends AbstractModule implements IOwlclGuiceModule {

  @Override
  protected void configure() {

    CoreUtils.installCommandFactory(binder(), MainCommand.class, OwlclCommand.CORE_MAIN);

    Multibinder<IOwlclCommandFactory> topCommandFactories = Multibinder.newSetBinder(binder(),
        IOwlclCommandFactory.class, TopCommandQualifier.class);

    CoreUtils.installCommandFactory(binder(), CatalogCommand.class, OwlclCommand.CORE_CATALOG);
    CoreUtils.installTopCommand(topCommandFactories, OwlclCommand.CORE_CATALOG);

  }

}
