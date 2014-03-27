package com.essaid.owlcl.module.builder.simple;

import com.essaid.owlcl.core.IsftModuleBuilder;
import com.essaid.owlcl.core.IModule;

public class UninferredSimpleModuleBuilder extends AbstractSimpleModuleBuilder {

	public UninferredSimpleModuleBuilder(IModule simpleModule) {
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
	public IsftModuleBuilder createBuilder(IModule module) {

		return new UninferredSimpleModuleBuilder(module);
	}

}
