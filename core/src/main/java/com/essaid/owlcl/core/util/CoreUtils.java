package com.essaid.owlcl.core.util;

import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class CoreUtils {

  public static void installCommandFactory(Binder binder,
      Class<? extends OwlclCommand> commandClass, String commandName) {
    binder.install(new FactoryModuleBuilder().implement(OwlclCommand.class, commandClass).build(
        Key.get(IOwlclCommandFactory.class, Names.named(commandName))));
  }

  public static void installTopCommand(Binder binder, String commandName) {
    Multibinder<IOwlclCommandFactory> topCommandFactories = Multibinder.newSetBinder(binder,
        IOwlclCommandFactory.class, TopCommandQualifier.class);
    installTopCommand(topCommandFactories, commandName);
  }

  public static void installTopCommand(Multibinder<IOwlclCommandFactory> multiBinder,
      String commandName) {
    multiBinder.addBinding().to(Key.get(IOwlclCommandFactory.class, Names.named(commandName)));

  }

}
