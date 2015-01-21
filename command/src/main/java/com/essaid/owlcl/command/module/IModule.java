package com.essaid.owlcl.command.module;

import java.nio.file.Path;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.essaid.owlcl.command.module.builder.IModuleBuilder;
import com.essaid.owlcl.command.module.config.IModuleConfig;

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
public interface IModule {

  IModuleConfig getModuleConfiguration();

  /**
   * This returns the generated ontology with only the builders' output. If the
   * "final" version is already called, this ontology will be the same as the
   * final.
   * 
   * @return
   */
  OWLOntology getBuildersUnclassified();

  OWLOntology getFinalUnclassified();

  OWLOntology getBuildersClassified();

  OWLOntology getFinalClassified();

  // ================================================================================
  // properties
  // ================================================================================

  boolean isUnclassified();

  void setUnclassified(Boolean generate);

  boolean isClassified();

  void setClassified(Boolean generateInferred);

  // ================================================================================
  // module imports
  // ================================================================================

  /**
   * Adds and import into this "classified" module and the specific OWL import
   * will be based on the boolean option. If false, the unclassified module will
   * be imported into this unclassified module. If true, the classified module
   * from the imported module will be imported into this "unclassified" file. If
   * null, both versions of the imported module will be imported to this
   * unclassified module (not sure if this is useful but the null option is used
   * for this third possible case. The fourth case is not needed because not
   * calling this method is the fourth case).
   * 
   * The idea is that one might want to mix classified and unclassified when
   * composing modules.
   * 
   * 
   * @param module
   * @param inferred
   */
  void importModuleIntoUnclassified(IModule module, Boolean inferred);

  /**
   * See importModuleIntoUnclassified
   * 
   * @param module
   * @param inferred
   */
  void importModuleIntoClassified(IModule module, Boolean inferred);

  // ================================================================================
  // Legacy related
  // ================================================================================
  /**
   * This will remove all axioms from all legacy ontologies based on what is
   * currently in the module ontology. The idea is that after the module
   * ontology is populated, the legacy ontology files can be cleaned from any
   * module axiom since the module now generates those axioms. It is a way to
   * simplify migrating legacy ontologies to being ISF modules (i.e. being an
   * ISF module based on the ISF ontology).
   */
  void cleanLegacyUnclassified();

  void cleanLegacyClassified();

  boolean isAddLegacyUnclassified();

  void setAddLegacyUnclassified(Boolean generate);

  boolean isAddLegacyClassified();

  void setAddLegacyClassified(Boolean generate);

  boolean isCleanLegacyClassified();

  void setCleanLegacyClassified(Boolean generate);

  boolean isCleanLegacyUnclassified();

  void setCleanLegacyUnclassified(Boolean generate);

  // ================================================================================
  // Utility
  // ================================================================================

  void saveModule() throws OWLOntologyStorageException;

  void saveUnclassifiedModule();

  void saveClassifiedModule();

//  void saveUnclassifiedModule(Path fielOrDirectory);

//  void saveClassifiedModule(Path fielOrDirectory);

  com.essaid.owlcl.core.util.Report getReportUnclassified();

  com.essaid.owlcl.core.util.Report getReportClassified();

  void dispose();

  OWLDataFactory getDataFactory();

  Path getOutputUnclassified();

  Path getOutputClassified();

  // ================================================================================
  // for builders
  // ================================================================================

  void addAnnotationUnclassified(OWLAnnotation annotation, IModuleBuilder builder);

  void removeAnnotationUnclassified(OWLAnnotation annotation, IModuleBuilder builder);

  void addAnnotationsUnclassified(Set<OWLAnnotation> annotations, IModuleBuilder builder);

  void removeAnnotationsUnclassified(Set<OWLAnnotation> annotations, IModuleBuilder builder);

  void addAnnotationClassified(OWLAnnotation annotation, IModuleBuilder builder);

  void removeAnnotationClassified(OWLAnnotation annotation, IModuleBuilder builder);

  void addAnnotationsClassified(Set<OWLAnnotation> annotations, IModuleBuilder builder);

  void removeAnnotationsClassified(Set<OWLAnnotation> annotations, IModuleBuilder builder);

  void addAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder);

  void removeAxiomUnclassified(OWLAxiom axiom, IModuleBuilder builder);

  void addAxiomsUnclassified(Set<OWLAxiom> axioms, IModuleBuilder builder);

  void removeAxiomsUnclassified(Set<OWLAxiom> axioms, IModuleBuilder builder);

  void addAxiomClassified(OWLAxiom axiom, IModuleBuilder builder);

  void removeAxiomClassified(OWLAxiom axiom, IModuleBuilder builder);

  void addAxiomsClassified(Set<OWLAxiom> axioms, IModuleBuilder builder);

  void removeAxiomsClassified(Set<OWLAxiom> axioms, IModuleBuilder builder);

  // ================================================================================
  //
  // ================================================================================

}
