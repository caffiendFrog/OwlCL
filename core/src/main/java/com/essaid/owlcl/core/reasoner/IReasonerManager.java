package com.essaid.owlcl.core.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface IReasonerManager {

  String FACT_PLUS_PLUS_REASONER_FACTORY = "owlcl.reasoner.factory.fact++";

  OWLReasonerFactory getFactFactory();

  OWLReasonerFactory getHermitFactory();

  OWLReasonerFactory getPelletFactory();

  OWLReasoner getReasonedOntology(OWLOntology ontologyIri);

}
