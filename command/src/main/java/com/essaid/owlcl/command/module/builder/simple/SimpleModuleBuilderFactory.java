package com.essaid.owlcl.command.module.builder.simple;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.builder.IModuleBuilderFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class SimpleModuleBuilderFactory implements IModuleBuilderFactory {

  @Inject
  Injector injector;

  @Override
  public String getName() {
    return "simple";
  }

  @Override
  public String getDescription() {
    return "A simple uninferred module builder.";
  }

  @Override
  public IModuleBuilder createBuilder(IModule module) {
    SimpleModuleBuilder builder = new SimpleModuleBuilder(module);
    injector.injectMembers(builder);
    return builder;
  }

}
