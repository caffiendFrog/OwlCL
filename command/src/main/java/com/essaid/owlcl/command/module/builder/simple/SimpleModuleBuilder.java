package com.essaid.owlcl.command.module.builder.simple;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SimpleModuleBuilder extends AbstractSimpleModuleBuilder {

  @Inject
  public SimpleModuleBuilder(@Assisted IModule simpleModule) {
    super(simpleModule);
  }

  @Override
  public void build(IModule module) {
    // TODO Auto-generated method stub

  }

  @Override
  public void buildFinished(IModule module) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getName() {
    return "simple";
  }

  @Override
  public String getDescription() {
    return "A simple builder factory";
  }

  @Override
  public IModuleBuilder createBuilder(IModule module) {

    return new SimpleModuleBuilder(module);
  }

}
