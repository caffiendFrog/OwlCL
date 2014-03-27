package com.essaid.owlcl.command;

import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.OwlclGModule;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.essaid.owlcl.core.util.CoreUtils;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class CommandGModule extends AbstractModule implements OwlclGModule {

  @Override
  protected void configure() {

    Multibinder<IOwlclCommandFactory> topCommandFactories = Multibinder.newSetBinder(binder(),
        IOwlclCommandFactory.class, TopCommandQualifier.class);

    CoreUtils.installCommandFactory(binder(), CatalogCommand.class, OwlclCommand.CORE_CATALOG);
    CoreUtils.installTopCommand(topCommandFactories, OwlclCommand.CORE_CATALOG);

  }

}
