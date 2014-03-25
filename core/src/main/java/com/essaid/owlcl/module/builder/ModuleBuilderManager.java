package com.essaid.owlcl.module.builder;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.essaid.owlcl.core.IsftModuleBuilder;
import com.essaid.owlcl.module.Module;

public class ModuleBuilderManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final ModuleBuilderManager instance;
	static
	{
		instance = new ModuleBuilderManager();
	}

	Map<String, ModuleBuilderFactory> factories = new HashMap<String, ModuleBuilderFactory>();

	private ModuleBuilderManager() {
		Iterator<ModuleBuilderFactory> i = ServiceLoader.load(ModuleBuilderFactory.class)
				.iterator();
		while (i.hasNext())
		{
			ModuleBuilderFactory factory = i.next();
			if (factories.keySet().contains(factory.getName()))
			{
				logger.error("BuilderFactory with name " + factory.getName() + " and class "
						+ factory.getClass().getName()
						+ " is being ignored becuase name is already assigned.");
			} else
			{
				factories.put(factory.getName(), factory);
			}
		}

	}

	public IsftModuleBuilder getBuilder(String builderName, Module module) {
		return factories.get(builderName).createBuilder(module);
	}
}
