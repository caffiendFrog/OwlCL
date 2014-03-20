package isf.module.builder.simple;

import isf.module.Module;
import isf.module.builder.ModuleBuilder;

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
	public ModuleBuilder createBuilder(Module module) {

		return new UninferredSimpleModuleBuilder(module);
	}

}
