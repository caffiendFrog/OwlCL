package com.essaid.owlcl.core.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

public interface OWLOntologyIDMapper extends OWLOntologyIRIMapper {

  IRI getDocumentIRI(OWLOntologyID ontologyId);

}
