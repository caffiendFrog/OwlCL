package com.essaid.owlcl.util;

import static com.essaid.owlcl.util.OwlclVocab.Vocab.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public enum OwlclVocab {

	prefix(Owlcl.ISFT_PREFIX), iri_mapps_to(ISFT_IRI_MAPPES_TO);

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

	// ================================================================================
	// The previous strings in the code before using the enum.
	// ================================================================================
	public static class Vocab {

		static final String ISFT_IRI_MAPPES_TO = Owlcl.ISFT_PREFIX + "iri-mappes-to";

	}

}
