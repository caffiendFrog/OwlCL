package com.essaid.owlcl.core.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public enum OwlclVocab {
  iri_mapps_to(com.essaid.owlcl.core.util.OwlclConstants.IRI_MAPPES_TO);
  ;

  private String vocab;

  private OwlclVocab(String vocab) {
    this.vocab = vocab;
  }

  public IRI iri() {
    return IRI.create(vocab);
  }

  public OWLAnnotationProperty getAP() {
    return OWLManager.getOWLDataFactory().getOWLAnnotationProperty(iri());
  }

  public String vocab() {
    return vocab;
  }

}
