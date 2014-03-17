package isf.module.builder;

import java.util.List;

import org.semanticweb.owlapi.model.OWLDataFactory;

import isf.command.AbstractCommand.Report;
import isf.module.Module;
import isf.module.SimpleModule;

public abstract class AbstractSimpleModuleBuilder implements ModuleBuilder {

	SimpleModule module;
	Report report;
	OWLDataFactory df;

	public AbstractSimpleModuleBuilder(SimpleModule simpleModule) {
		this.module = simpleModule;
		this.report = module.getReport();
		this.df = module.getDataFactory();
	}

	public static void addBuilders(SimpleModule module, List<String> builders) {

		for (String builder : builders)
		{
			Builders.valueOf(builder).addBuilder(module);
		}
	}

	enum Builders {
		inferred {

			@Override
			void addBuilder(SimpleModule module) {
				module.addBuilder(new UninferredSimpleModuleBuilder(module));

			}
		},
		uninferred {

			@Override
			void addBuilder(SimpleModule module) {
				module.addBuilder(new InferredSimpleModuleBuilder(module));

			}
		};

		abstract void addBuilder(SimpleModule module);
	}
}
