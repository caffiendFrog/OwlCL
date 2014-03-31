package com.essaid.owlcl.core.reasoner;

import javax.inject.Named;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.inject.Inject;

public class DefaultReasonerManager implements IReasonerManager {

  @Inject(optional = true)
  @Named("Fact++")
  private OWLReasonerFactory factFactory;

  @Override
  public OWLReasonerFactory getFactFactory() {
    return null;
  }

  @Override
  public OWLReasonerFactory getHermitFactory() {
    return null;
  }

  @Override
  public OWLReasonerFactory getPelletFactory() {
    return null;
  }

  @Override
  public OWLReasoner getReasonedOntology(OWLOntology ontology) {
    return null;
  }

  @Override
  public OWLReasoner getReasonedOntology(IRI iri) {
    return null;
  }

}
