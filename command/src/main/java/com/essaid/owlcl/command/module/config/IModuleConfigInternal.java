package com.essaid.owlcl.command.module.config;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public interface IModuleConfigInternal extends IModuleConfig {

  void update();

  IRI getUnclassifiedIriStated();

  void setUnclassifiedIri(IRI iri);

  IRI getClassifiedIriStated();

  void setClassifiedIri(IRI iri);
  
  IRI getVersionIriStated();
  
  void setVersionIri(IRI iri);

  String getClassifiedFilenameStated();

  void setClassifiedFilename(String name);

  String getUnclassifiedFilenameStated();

  void setUnclassifiedFilename(String name);

  void setSourceIris(Set<IRI> iris);

  void setSourceExcludedIris(Set<IRI> iris);

  void saveConfiguration();

  Boolean isClassifiedStated();

  void setClassified(boolean classified);

  Boolean isUnclassifiedStated();

  void setUnclassified(boolean unclassified);

  Boolean isUnclassifiedCleanlegacyStated();

  void setUnclassifiedCleanlegacy(boolean cleanlegacy);

  Boolean isClassifiedCleanlegacyStated();

  void setClassifiedCleanlegacy(boolean cleanlegacy);

  Boolean isUnclassifiedAddlegacyStated();

  void setUnclassifiedAddlegacy(boolean addlegacy);

  Boolean isClassifiedAddlegacyStated();

  void setClassifiedAddlegacy(boolean addlegacy);

  public List<String> getClassifiedBuilderNamesStated();

  public List<String> getUnclassifiedBuilderNamesStated();

}
