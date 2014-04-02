package com.essaid.owlcl.command.module.builder.simple;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.builder.IModuleBuilderFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class SimpleInferredModuleBuilderFactory implements IModuleBuilderFactory {

  @Inject
  Injector injector;

  @Override
  public String getName() {
    return "simple-inferred";
  }

  @Override
  public String getDescription() {
    return "A simple inferred builder factory.";
  }

  @Override
  public IModuleBuilder createBuilder(IModule module) {
    SimpleInferredModuleBuilder builder = new SimpleInferredModuleBuilder(module);
    injector.injectMembers(builder);
    return builder;
  }

}
