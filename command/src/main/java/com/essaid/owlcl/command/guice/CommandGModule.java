package com.essaid.owlcl.command.guice;

import static com.essaid.owlcl.command.AbstractCommand.*;

import com.essaid.owlcl.command.AnnotateCommand;
import com.essaid.owlcl.command.CatalogCommand;
import com.essaid.owlcl.command.CompareCommand;
import com.essaid.owlcl.command.EroCommand;
import com.essaid.owlcl.command.MergeCommand;
import com.essaid.owlcl.command.ModuleCommand;
import com.essaid.owlcl.command.MainCommand;
import com.essaid.owlcl.command.MapperCommand;
import com.essaid.owlcl.command.NewModuleCommand;
import com.essaid.owlcl.command.RewriteCommand;
import com.essaid.owlcl.command.TypecheckCommand;
import com.essaid.owlcl.command.UpdateModuleCommand;
import com.essaid.owlcl.command.ValidateIriCommand;
import com.essaid.owlcl.command.module.builder.IModuleBuilderFactory;
import com.essaid.owlcl.command.module.builder.ModuleBuilderManager;
import com.essaid.owlcl.command.module.builder.simple.SimpleInferredModuleBuilderFactory;
import com.essaid.owlcl.command.module.builder.simple.SimpleModuleBuilderFactory;
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

    GuiceUtils.installCommandFactory(binder(), ModuleCommand.class, GENERATE_MODULE);
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

    GuiceUtils.installCommandFactory(binder(), AnnotateCommand.class, ANNOTATE);
    GuiceUtils.installTopCommand(topCommandFactories, ANNOTATE);

    GuiceUtils.installCommandFactory(binder(), MergeCommand.class, MERGE);
    GuiceUtils.installTopCommand(topCommandFactories, MERGE);

    // module related

    bind(ModuleBuilderManager.class).in(Scopes.SINGLETON);
    ;

    Multibinder<IModuleBuilderFactory> builderFactories = Multibinder
        .<IModuleBuilderFactory> newSetBinder(binder(), IModuleBuilderFactory.class);

    builderFactories.addBinding().to(SimpleInferredModuleBuilderFactory.class);
    builderFactories.addBinding().to(SimpleModuleBuilderFactory.class);

  }
}
