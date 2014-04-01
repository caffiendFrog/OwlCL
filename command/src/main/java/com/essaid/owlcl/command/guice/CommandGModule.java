package com.essaid.owlcl.command.guice;

import static com.essaid.owlcl.command.AbstractCommand.*;

import com.essaid.owlcl.command.CatalogCommand;
import com.essaid.owlcl.command.CompareCommand;
import com.essaid.owlcl.command.EroCommand;
import com.essaid.owlcl.command.GenerateModuleCommand;
import com.essaid.owlcl.command.MainCommand;
import com.essaid.owlcl.command.MapperCommand;
import com.essaid.owlcl.command.NewModuleCommand;
import com.essaid.owlcl.command.RewriteCommand;
import com.essaid.owlcl.command.TypecheckCommand;
import com.essaid.owlcl.command.UpdateModuleCommand;
import com.essaid.owlcl.command.ValidateIriCommand;
import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.builder.ModuleBuilderManager;
import com.essaid.owlcl.command.module.builder.simple.SimpleInferredModuleBuilder;
import com.essaid.owlcl.command.module.builder.simple.SimpleModuleBuilder;
import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.OwlclGuiceModule;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class CommandGModule extends AbstractModule implements OwlclGuiceModule {

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
    GuiceUtils.installTopCommand(topCommandFactories, VALIDATE);

    // module related

    bind(ModuleBuilderManager.class).in(Scopes.SINGLETON);;

    GuiceUtils.installBuilderFactory(binder(), SimpleInferredModuleBuilder.class,
        IModuleBuilder.SIMPLE_INFERRED);

    GuiceUtils.installBuilderFactory(binder(), SimpleModuleBuilder.class, IModuleBuilder.SIMPLE);

  }

}
