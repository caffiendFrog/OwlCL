package com.essaid.owlcl.module.builder.simple;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import com.essaid.owlcl.core.IsftModuleBuilder;
import com.essaid.owlcl.module.Module;
import com.essaid.owlcl.module.builder.ModuleBuilderFactory;

public abstract class AbstractSimpleModuleBuilder implements IsftModuleBuilder, ModuleBuilderFactory {

	Module module;
	com.essaid.owlcl.util.Report report;
	OWLDataFactory df;

	public AbstractSimpleModuleBuilder(Module simpleModule) {
		this.module = simpleModule;
		this.report = module.getReport();
		this.df = module.getDataFactory();
	}

	protected void addOntologyAnnotations() {
		for (OWLAnnotation a : module.getModuleConfiguration().getAnnotations())
		{
			if (!a.getProperty().getIRI().toString().contains("isftools-"))
			{
				module.addModuleAnnotation(a);
			}
		}
	}

	public static Set<OWLAxiom> getDefiningAxioms(final OWLEntity entity,
			Set<OWLOntology> ontologies, boolean includeImports) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLOntology o : ontologies)
		{
			axioms.addAll(AbstractSimpleModuleBuilder.getDefiningAxioms(entity, o, includeImports));
		}
		return axioms;
	}

	public static Set<OWLAxiom> getDefiningAxioms(final OWLEntity entity, OWLOntology ontology,
			boolean includeImports) {
		final Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLOntology> ontologies;
		if (includeImports)
		{
			ontologies = ontology.getImportsClosure();
		} else
		{
			ontologies = Collections.singleton(ontology);
		}

		for (final OWLOntology o : ontologies)
		{
			entity.accept(new OWLEntityVisitor() {

				@Override
				public void visit(OWLAnnotationProperty property) {
					axioms.addAll(o.getAxioms(property));
				}

				@Override
				public void visit(OWLDatatype datatype) {
					axioms.addAll(o.getAxioms(datatype));

				}

				@Override
				public void visit(OWLNamedIndividual individual) {
					axioms.addAll(o.getAxioms(individual));

				}

				@Override
				public void visit(OWLDataProperty property) {
					axioms.addAll(o.getAxioms(property));

				}

				@Override
				public void visit(OWLObjectProperty property) {
					axioms.addAll(o.getAxioms(property));

				}

				@Override
				public void visit(OWLClass cls) {
					axioms.addAll(o.getAxioms(cls));

				}
			});
		}

		return axioms;
	}

}
