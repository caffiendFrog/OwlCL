package com.essaid.owlcl.command.module.config;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public interface IModuleConfig {
  
  void loadConfiguration();

  String getModuleName();

  IRI getConfigurationIri();

  OWLOntology getConfigurationOntology();

  IRI getIncludeIri();

  OWLOntology getIncludeOntology();

  IRI getExcludeIri();

  OWLOntology getExcludeOntology();

  IRI getLegacyIri();

  OWLOntology getLegacyOntology();

  IRI getLegacyRemovedIri();

  OWLOntology getLegacyRemovedOntology();

  OWLOntologyManager getSourceManager();

  OWLOntology getSourceOntology();

  OWLReasoner getSourceReasoner();

  List<String> getBuildersNames();

  List<String> getBuildersInferredNames();

  Set<IRI> getExcludeSourceIris();

  boolean isGenerate();

  boolean isGenerateInferred();
  
  boolean isAddLegacy();
  
  boolean isCleanLegacy();

  Set<OWLAnnotation> getAnnotations();

  IRI getGeneratedModuleIri();

  String getGenerateModuleFileName();

  IRI getGeneratedInferredModuleIri();

  String getGenerateInferredModuleFileName();
}
