package com.essaid.owlcl.command.module.builder.simple;

import static com.essaid.owlcl.command.module.OwlclVocab.Vocab.*;
import static com.essaid.owlcl.command.module.builder.simple.MBSimpleVocab.Constant.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import com.essaid.owlcl.command.module.Owlcl;

public enum MBSimpleVocab {
	exclude(ISFT_EXCLUDE), exclude_subs(ISFT_EXCLUDE_SUBS), include(ISFT_INCLUDE), include_subs(
			ISFT_INCLUDE_SUBS), include_instances(ISFT_INCLUDE_INSTANCES);

	private String vocab;

	private MBSimpleVocab(String value) {
		this.vocab = value;
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

	public static class Constant {

		static final String ISFT_PREFIX = Owlcl.ISF_ONTOLOGY_IRI_PREFIX + "isftools-";
		static final String ISFT_EXCLUDE = ISFT_PREFIX + "module-exclude";
		static final String ISFT_EXCLUDE_SUBS = ISFT_PREFIX + "module-exclude-subs";
		static final String ISFT_INCLUDE = ISFT_PREFIX + "module-include";
		static final String ISFT_INCLUDE_INSTANCES = ISFT_PREFIX + "module-include-instances";
		static final String ISFT_INCLUDE_SUBS = ISFT_PREFIX + "module-include-subs";
	}
}
