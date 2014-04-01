package com.essaid.owlcl.command.module;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.essaid.owlcl.command.module.builder.simple.MBSimpleVocab;
import com.essaid.owlcl.core.util.OwlclUtil;

public class Util {

  public static Set<OWLAnnotationAssertionAxiom> getIncludeAxioms(OWLOntology ontology,
      boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.include.getAP(),
        includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getIncludeInstancesAxioms(OWLOntology ontology,
      boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(ontology,
        MBSimpleVocab.include_instances.getAP(), includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getIncludeSubsAxioms(OWLOntology ontology,
      boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.include_subs.getAP(),
        includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getExcludeAxioms(OWLOntology ontology,
      boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.exclude.getAP(),
        includeImports);
  }

  public static Set<OWLAnnotationAssertionAxiom> getExcludeSubsAxioms(OWLOntology ontology,
      boolean includeImports) {

    return OwlclUtil.getAnnotationAssertionAxioms(ontology, MBSimpleVocab.exclude_subs.getAP(),
        includeImports);
  }

  public static Set<OWLEntity> getIncludeEntities(OWLOntology ontology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeAxioms(ontology, includeImports);
    return getSubjectEntities(ontology, includeImports, axioms);
  }

  public static Set<OWLEntity> getIncludeInstances(OWLOntology ontology, boolean includeImports) {

    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeInstancesAxioms(ontology, includeImports);

    return getSubjectEntities(ontology, includeImports, axioms);

  }

  public static Set<OWLEntity> getIncludeSubsEntities(OWLOntology ontology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getIncludeSubsAxioms(ontology, includeImports);
    return getSubjectEntities(ontology, includeImports, axioms);
  }

  public static Set<OWLEntity> getExcludeEntities(OWLOntology ontology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getExcludeAxioms(ontology, includeImports);
    return getSubjectEntities(ontology, includeImports, axioms);
  }

  private static Set<OWLEntity> getSubjectEntities(OWLOntology ontology, boolean includeImports,
      Set<OWLAnnotationAssertionAxiom> axioms) {
    Set<OWLEntity> entities = new HashSet<OWLEntity>();
    IRI subject;
    for (OWLAnnotationAssertionAxiom a : axioms)
    {
      if (a.getSubject() instanceof IRI)
      {
        subject = (IRI) a.getSubject();
        entities.addAll(ontology.getEntitiesInSignature(subject, includeImports));
      }
    }
    return entities;
  }

  public static Set<OWLEntity> getExcludeSubsEntities(OWLOntology ontology, boolean includeImports) {
    Set<OWLAnnotationAssertionAxiom> axioms = getExcludeSubsAxioms(ontology, includeImports);
    return getSubjectEntities(ontology, includeImports, axioms);
  }

}
