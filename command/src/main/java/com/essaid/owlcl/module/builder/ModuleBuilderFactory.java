package com.essaid.owlcl.module.builder;

import com.essaid.owlcl.core.IsftModuleBuilder;
import com.essaid.owlcl.core.IModule;


public interface ModuleBuilderFactory {

	String getName();

	String getDescription();

	IsftModuleBuilder createBuilder(IModule module);

}
