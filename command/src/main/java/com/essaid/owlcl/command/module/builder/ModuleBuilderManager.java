package com.essaid.owlcl.command.module.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.essaid.owlcl.command.module.IModule;
import com.essaid.owlcl.core.annotation.InjectLogger;
import com.essaid.owlcl.core.util.IInitializable;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;

public class ModuleBuilderManager implements IInitializable {

  @InjectLogger
  private Logger logger;

  @Inject
  Injector injector;

  private Map<String, IModuleBuilderFactory> factoriesMap = new HashMap<String, IModuleBuilderFactory>();

  ModuleBuilderManager() {
  }

  public IModuleBuilder getBuilder(String builderName, IModule module) {

    return factoriesMap.get(builderName).createBuilder(module);
  }

  @Override
  public void initialize() {
    for (Entry<Key<?>, Binding<?>> e : injector.getBindings().entrySet())
    {
      if (IModuleBuilderFactory.class.isAssignableFrom(e.getKey().getTypeLiteral().getRawType()))
      {
        Named named = (Named) e.getKey().getAnnotation();
        IModuleBuilderFactory f = (IModuleBuilderFactory) e.getValue().getProvider().get();
        factoriesMap.put(named.value(), f);
        logger.info("Adding factory: {}", named.value());
      }
    }

  }
}
