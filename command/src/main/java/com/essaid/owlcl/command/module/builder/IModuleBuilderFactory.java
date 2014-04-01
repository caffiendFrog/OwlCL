package com.essaid.owlcl.command.module.builder;

import com.essaid.owlcl.command.module.IModule;


public interface IModuleBuilderFactory {

	String getName();

	String getDescription();

	IModuleBuilder createBuilder(IModule module);

}
