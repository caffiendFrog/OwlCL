package com.essaid.owlcl.command;

import static com.essaid.owlcl.command.AbstractCommand.*;

import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.OwlclGModule;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.essaid.owlcl.core.util.GuiceUtils;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class CommandGModule extends AbstractModule implements OwlclGModule {

  @Override
  protected void configure() {

    GuiceUtils.installCommandFactory(binder(), MainCommand.class, MAIN);

    Multibinder<IOwlclCommandFactory> topCommandFactories = Multibinder.newSetBinder(binder(),
        IOwlclCommandFactory.class, TopCommandQualifier.class);

    GuiceUtils.installCommandFactory(binder(), CatalogCommand.class, CATALOG);
    GuiceUtils.installTopCommand(topCommandFactories, CATALOG);

    GuiceUtils.installCommandFactory(binder(), CompareCommand.class, COMPARE);
    GuiceUtils.installTopCommand(topCommandFactories, COMPARE);

    GuiceUtils.installCommandFactory(binder(), EroCommand.class, ERO);
    GuiceUtils.installTopCommand(topCommandFactories, ERO);

    GuiceUtils.installCommandFactory(binder(), GenerateModuleCommand.class, GENERATE_MODULE);
    GuiceUtils.installTopCommand(topCommandFactories, GENERATE_MODULE);

    GuiceUtils.installCommandFactory(binder(), MapperCommand.class, MAPPER);
    GuiceUtils.installTopCommand(topCommandFactories, MAPPER);

    GuiceUtils.installCommandFactory(binder(), NewModuleCommand.class, NEW_MODULE);
    GuiceUtils.installTopCommand(topCommandFactories, NEW_MODULE);

    GuiceUtils.installCommandFactory(binder(), RewriteCommand.class, REWRITE);
    GuiceUtils.installTopCommand(topCommandFactories, REWRITE);

    GuiceUtils.installCommandFactory(binder(), TypecheckCommand.class, TYPECHECK);
    GuiceUtils.installTopCommand(topCommandFactories, TYPECHECK);

    GuiceUtils.installCommandFactory(binder(), UpdateModuleCommand.class, UPDATE_MODULE);
    GuiceUtils.installTopCommand(topCommandFactories, UPDATE_MODULE);

    GuiceUtils.installCommandFactory(binder(), ValidateIriCommand.class, VALIDATE);
    GuiceUtils.installTopCommand(topCommandFactories, NEW_MODULE);

  }

}
