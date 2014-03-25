package com.essaid.owlcl.module.builder.simple;

import com.essaid.owlcl.core.IsftModuleBuilder;
import com.essaid.owlcl.module.Module;

public class UninferredSimpleModuleBuilder extends AbstractSimpleModuleBuilder {

	public UninferredSimpleModuleBuilder(Module simpleModule) {
		super(simpleModule);
	}

	@Override
	public void build(Module module) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildFinished(Module module) {
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
	public IsftModuleBuilder createBuilder(Module module) {

		return new UninferredSimpleModuleBuilder(module);
	}

}
