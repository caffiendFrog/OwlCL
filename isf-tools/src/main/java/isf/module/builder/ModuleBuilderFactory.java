package isf.module.builder;

import isf.module.Module;


public interface ModuleBuilderFactory {

	String getName();

	String getDescription();

	ModuleBuilder createBuilder(Module module);

}
