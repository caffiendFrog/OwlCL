package com.essaid.owlcl.core.cli.util;

import org.semanticweb.owlapi.model.IRI;

import com.beust.jcommander.IStringConverter;

public class IriConverter implements IStringConverter<IRI> {

	@Override
	public IRI convert(String value) {
		return IRI.create(value);
	}

}
