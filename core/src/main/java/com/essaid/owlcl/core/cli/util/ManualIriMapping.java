package com.essaid.owlcl.core.cli.util;

import org.semanticweb.owlapi.model.IRI;

public class ManualIriMapping {

	public ManualIriMapping(IRI fromIri, IRI toIri) {
		this.fromIri = fromIri;
		this.toIri = toIri;
	}

	public IRI fromIri;
	public IRI toIri;
}
