package com.essaid.owlcl.command.module.config;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public interface IModuleConfigInternal extends IModuleConfig {

  void load();

  void update();

  void loadAndUpdate();

  void setUnclassifiedIri(IRI iri);

  void setClassifiedIri(IRI iri);

  void setClassifiedFilename(String name);

  void setUnclassifiedFilename(String name);

  void setSourceIris(Set<IRI> iris);

  void setSourceExcludedIris(Set<IRI> iris);

  void saveConfiguration();

  void setClassified(boolean classified);

  void setUnclassified(boolean unclassified);

  void setUnclassifiedCleanlegacy(boolean cleanlegacy);

  void setClassifiedCleanlegacy(boolean cleanlegacy);

  void setUnclassifiedAddlegacy(boolean addlegacy);

  void setClassifiedAddlegacy(boolean addlegacy);

}
