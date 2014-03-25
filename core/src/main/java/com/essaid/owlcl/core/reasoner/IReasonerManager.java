package com.essaid.owlcl.core.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface IReasonerManager {

  OWLReasonerFactory getFactFactory();

  OWLReasonerFactory getHermitFactory();

  OWLReasonerFactory getPelletFactory();

  OWLReasoner getReasonedOntology(OWLOntology  ontologyIri);

}
