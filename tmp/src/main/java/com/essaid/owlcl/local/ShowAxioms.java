package com.essaid.owlcl.local;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ShowAxioms {

  public static void main(String[] args) throws OWLOntologyCreationException {

    for (OWLAxiom a : OWLManager.createOWLOntologyManager()
        .loadOntologyFromOntologyDocument(new File("temp/strange-axioms.owl")).getAxioms())
    {
      System.out.println(a.getAxiomType() + " -->  " + a);
     
    }

  }

}
