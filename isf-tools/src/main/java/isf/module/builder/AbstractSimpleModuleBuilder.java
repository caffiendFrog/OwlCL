package isf.module.builder;

import org.semanticweb.owlapi.model.OWLDataFactory;

import isf.command.AbstractCommand.Report;
import isf.module.SimpleModule;

public abstract class AbstractSimpleModuleBuilder implements ModuleBuilder {

	SimpleModule module;
	Report report ;
	OWLDataFactory df;

	public AbstractSimpleModuleBuilder(SimpleModule simpleModule) {
		this.module = simpleModule;
		this.report = module.getReport();
		this.df = module.getDataFactory();
	}

}
