package com.essaid.owlcl.command.module;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.essaid.owlcl.command.module.builder.simple.MBSimpleVocab;
import com.essaid.owlcl.command.module.config.IModuleConfigInternal;
import com.essaid.owlcl.command.module.config.ModuleConfigurationV1;
import com.essaid.owlcl.core.util.OwlclUtil;

public class Util {

  public static Set<OWLAnnotationAssertionAxiom> getIncludeAxioms(
      OWLOntology configurationOntology, boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(configurationOntology,
        MBSimpleVocab.include.getAP(), includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getIncludeInstancesAxioms(
      OWLOntology configurationOntology, boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(configurationOntology,
        MBSimpleVocab.include_instances.getAP(), includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getIncludeSubsAxioms(
      OWLOntology configurationOntology, boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(configurationOntology,
        MBSimpleVocab.include_subs.getAP(), includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getExcludeAxioms(
      OWLOntology configurationOntology, boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(configurationOntology,
        MBSimpleVocab.exclude.getAP(), includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getExcludeSubsAxioms(
      OWLOntology configurationOntology, boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(configurationOntology,
        MBSimpleVocab.exclude_subs.getAP(), includeImports);
  }

  public static Set<OWLEntity> getIncludeEntities(OWLOntology configurationOntology,
      OWLOntology sourceOntology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeAxioms(configurationOntology,
        includeImports);
    return getSubjectEntities(configurationOntology, sourceOntology, includeImports, axioms);
  }

  public static Set<OWLEntity> getIncludeInstances(OWLOntology configurationOntology,
      OWLOntology sourceOntology, boolean includeImports) {

    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeInstancesAxioms(configurationOntology,
        includeImports);

    return getSubjectEntities(configurationOntology, sourceOntology, includeImports, axioms);

  }

  public static Set<OWLEntity> getIncludeSubsEntities(OWLOntology configurationOntology,
      OWLOntology sourceOntology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeSubsAxioms(configurationOntology,
        includeImports);
    return getSubjectEntities(configurationOntology, sourceOntology, includeImports, axioms);
  }

  public static Set<OWLEntity> getExcludeEntities(OWLOntology configurationOntology,
      OWLOntology sourceOntology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getExcludeAxioms(configurationOntology,
        includeImports);
    return getSubjectEntities(configurationOntology, sourceOntology, includeImports, axioms);
  }

  private static Set<OWLEntity> getSubjectEntities(OWLOntology configurationOntology,
      OWLOntology sourceOntology, boolean includeImports, Set<OWLAnnotationAssertionAxiom> axioms) {
    Set<OWLEntity> entities = new HashSet<OWLEntity>();
    IRI subject;
    for (OWLAnnotationAssertionAxiom a : axioms)
    {
      if (a.getSubject() instanceof IRI)
      {
        subject = (IRI) a.getSubject();
        entities.addAll(sourceOntology.getEntitiesInSignature(subject, includeImports));
      }
    }
    return entities;
  }

  public static Set<OWLEntity> getExcludeSubsEntities(OWLOntology ontology,
      OWLOntology sourceOntology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getExcludeSubsAxioms(ontology, includeImports);
    return getSubjectEntities(ontology, sourceOntology, includeImports, axioms);
  }

  public static int getModuleVersion(File directory) {

    Collection<File> versionFiles = FileUtils
        .listFiles(directory, new PrefixFileFilter("V-"), null);

    if (versionFiles.size() == 0)
    {
      return -1;
    } else if (versionFiles.size() > 1)
    {
      return -2;
    } else
    {
      String fileName = versionFiles.iterator().next().getName();
      int i = fileName.indexOf('-');
      return Integer.valueOf(fileName.substring(i + 1));
    }
  }

  public static IModuleConfigInternal getExistingConfigurationByVersion(int version,
      Path directory, OWLOntologyManager configMan, OWLOntologyManager sourceMan,
      OWLOntology sourceOntology, OWLReasoner sourceReasoner) {

    IModuleConfigInternal config = null;
    if (version == 1)
    {
      config = ModuleConfigurationV1.getExistingInstance(directory, configMan, sourceMan,
          sourceOntology, sourceReasoner);

    }
    return config;

  }
}
