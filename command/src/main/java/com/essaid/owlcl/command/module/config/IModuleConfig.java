package com.essaid.owlcl.command.module.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public interface IModuleConfig {

  int CURRENT_VERSION = 1;

  String getName();

  String getIriPrefix();

  Path getDirectory();

  IRI getConfigurationIri();

  OWLOntology getConfigurationOntology();

  IRI getTopIri();

  OWLOntology getTopOntology();

  Set<IRI> getExcludedSourceIris();

  IRI getIncludeIri();

  OWLOntology getIncludeOntology();

  IRI getExcludeIri();

  OWLOntology getExcludeOntology();

  IRI getSourceConfigurationIri();

  OWLOntology getSourceConfigurationOntology();

  IRI getLegacyIri();

  OWLOntology getLegacyOntology();

  IRI getLegacyRemovedIri();

  OWLOntology getLegacyRemovedOntology();

  OWLOntology getSourceOntology();

  OWLReasoner getSourceReasoner();

  List<String> getUnclassifiedBuilderNames();

  List<String> getClassifiedBuilderNames();

  boolean isUnclassified();

  boolean isClassified();

  boolean isClassifiedAddlegacy();

  boolean isUnclassifiedAddlegacy();

  boolean isUnclassifiedCleanLegacy();

  boolean isClassifiedCleanLegacy();

  IRI getUnclassifiedIri();

  String getUnclassifiedFileName();

  IRI getClassifiedIri();

  String getClassifiedFileName();

}
