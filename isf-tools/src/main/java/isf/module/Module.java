package isf.module;

import isf.command.AbstractCommand.Report;
import isf.module.builder.ModuleBuilder;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * A "module", in the most general sense, is a subset of the axioms of one or
 * more "source" ontologies. The axioms are copied from the source ontologies
 * into the module by one or more "builder(s)". A "builder" finds its
 * configuration from the "module" and reports back to the module the axioms it
 * selects based on the configuration.
 * 
 * A module provides two source views. One is an OWLOntology that imports all
 * the sources and the other is a reasoned version in the form of an
 * OWLReasoner. The builders use the view they need and report axioms by the
 * view they used.
 * 
 * The source ontolgies are the import closure of the direct imports of the
 * *-module-annotation.owl files. If for some reason, one or more of the
 * ontologies in this closure should be excluded, an annotion can be used to
 * capture this and the builders will not see these excluded ontologies.
 * 
 * After a module is built, it is saved in two ontolgies based on the view that
 * was used and the ontology IRI and file name are configurable. The axioms in
 * the unclassified ontology should be a subset of the ones in the classified.
 * 
 * Composite modules are modules that aggregate other simple modules but the
 * idea is the same.
 * 
 * @author Shahim Essaid
 * 
 */
public interface Module {

	String getName();

	void loadConfiguration();

	void addBuilder(ModuleBuilder builder);

	Set<ModuleBuilder> getBuilders();

	Report getReport();

	void generateModule();

	/**
	 * This will cause any axiom in the legacy ontologies to be also included in
	 * the module. This method, and the cleanLegacyOntologies() can help with
	 * migrating a legacy ontology to become an ISF module. This call will allow
	 * the module to include legacy content that is not yet in the ISF, or
	 * content that will not be in the ISF but is still needed in the generated
	 * module.
	 */
	// void addLegacyOntologies();
	//
	// void addLegacyOntologiesTransitive();

	/**
	 * This will remove all axioms from all legacy ontologies based on what is
	 * currently in the module ontology. The idea is that after the module
	 * ontology is populated, the legacy ontology files can be cleaned from any
	 * module axiom since the module now generates those axioms. It is a way to
	 * simplify migrating legacy ontologies to being ISF modules (i.e. being an
	 * ISF module based on the ISF ontology).
	 */
	// void cleanLegacyOntologies();
	//
	// void cleanLegacyOntologiesTransitive();

	IRI getModuleIri();

	IRI getModuleIriInferred();

	OWLOntology getGeneratedModule();

	OWLOntology getGeneratedModuleInferred();

	OWLOntology getSource();

	/**
	 * This allows a client to set the source ontology (which includes the
	 * imports) in case there are multiple modules being generated and they all
	 * share the same sources. Otherwise, the module will build its own source
	 * ontology based on its configuration.
	 * 
	 * this has to be set after constructing the module and before calling any
	 * other methods.
	 * 
	 * @param source
	 */
	// void setSource(OWLOntology source);

	OWLReasoner getSourceReasoned();

	/**
	 * This allows a client to set the reasoned source ontology instead of
	 * having the module reason its source ontology. This would be useful when
	 * multiple modules are being generated with the same reasoned sources and
	 * the reasoning is lengthy. Otherwise, the module will reason its sources.
	 * 
	 * this has to be set after constructing the module and before calling any
	 * other methods.
	 * 
	 * @param sourceReasoner
	 */
	// void setSourceReasoned(OWLReasoner sourceReasoner);

	/**
	 * Adds and import into this "generated" module and the specific OWL import
	 * will be based on the boolean option. If true, the inferred module from
	 * the imported module will be imprted into this "generated" file.
	 * 
	 * The idea is that one might want to mix inferred with un-inferred when
	 * composing modules.
	 * 
	 * A null inferred parameter means import the same type. i.e. generate to
	 * generate, inferred to inferred.
	 * 
	 * @param module
	 * @param inferred
	 */
	void importModuleIntoGenerated(Module module, Boolean inferred);

	/**
	 * See importModuleIntoGenerated
	 * 
	 * @param module
	 * @param inferred
	 */
	void importModuleIntoGeneratedInferred(Module module, Boolean inferred);

	/**
	 * See importModuleIntoGenerated
	 * 
	 * If the Boolean is null, both versions of the imported module will be
	 * imported into this module, by matching type. Otherwise, false means the
	 * "generated" will be imported to both and true means that the "inferred"
	 * will be imported into both.
	 * 
	 * @param module
	 * @param inferred
	 */
	void importModuleIntoBoth(Module module, Boolean inferred);

	void addModuleAnnotation(OWLAnnotation annotation);

	void removeModuleAnnotation(OWLAnnotation annotation);

	void addModuleAnnotations(Set<OWLAnnotation> annotations);

	void removeModuleAnnotations(Set<OWLAnnotation> annotations);

	void addModuleAnnotationInferred(OWLAnnotation annotation);

	void removeModuleAnnotationInferred(OWLAnnotation annotation);

	void addModuleAnnotationsInferred(Set<OWLAnnotation> annotations);

	void removeModuleAnnotationsInferred(Set<OWLAnnotation> annotations);

	void addAxiom(OWLAxiom axiom);

	void removeAxiom(OWLAxiom axiom);

	void addAxioms(Set<OWLAxiom> axioms);

	void removeAxioms(Set<OWLAxiom> axioms);

	void addAxiomInferred(OWLAxiom axiom);

	void removeAxiomInferred(OWLAxiom axiom);

	void addAxiomsInferred(Set<OWLAxiom> axioms);

	void removeAxiomsInferred(Set<OWLAxiom> axioms);

	void saveModuleConfiguration();

	void saveGeneratedModule();

	void dispose();

	void setGenerate(boolean generate);

	boolean isGenerate();

	void setGenerateInferred(boolean generateInferred);

	boolean isGenerateInferred();

	OWLDataFactory getDataFactory();

	OWLOntology getModuleConfiguration();

}
