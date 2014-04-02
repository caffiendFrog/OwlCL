package com.essaid.owlcl.command.module.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.util.IInitializable;
import com.google.inject.Inject;

public class ModuleBuilderManager implements IInitializable {

  @InjectLogger
  private Logger logger;

  @Inject
  Set<IModuleBuilderFactory> factories;

  private Map<String, IModuleBuilderFactory> factoriesMap = new HashMap<String, IModuleBuilderFactory>();

  ModuleBuilderManager() {
  }

  public IModuleBuilder getBuilder(String builderName, IModule module) {
    IModuleBuilderFactory f = factoriesMap.get(builderName);
    if (f != null)
    {
      return factoriesMap.get(builderName).createBuilder(module);
    } else
    {
      return null;
    }
  }

  @Override
  public void initialize() {
    for (IModuleBuilderFactory factory : factories)
    {
      factoriesMap.put(factory.getName(), factory);
      logger.info("Adding factory: {}", factory.getName());
    }

  }
}
