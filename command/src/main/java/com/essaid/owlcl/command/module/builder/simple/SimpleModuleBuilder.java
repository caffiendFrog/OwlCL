package com.essaid.owlcl.command.module.builder.simple;

import com.essaid.owlcl.command.module.IModule;

public class SimpleModuleBuilder extends AbstractSimpleModuleBuilder {

  public SimpleModuleBuilder(IModule simpleModule) {
    super(simpleModule);
  }

  @Override
  public void build(IModule module, boolean inferred) {
    System.out.println("Is inferred: "+ inferred);
    System.out.println("building from simple module builder");
  }

  @Override
  public void buildFinished(IModule module) {
    System.out.println("finished from simple module builder");
  }

}
