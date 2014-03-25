package com.essaid.owlcl.command.cli;

import org.semanticweb.owlapi.model.IRI;

import com.beust.jcommander.IStringConverter;

public class ManualIriMappingConverter implements IStringConverter<ManualIriMapping> {

	@Override
	public ManualIriMapping convert(String value) {
		String[] values = value.split("\\s+");
		
		return new ManualIriMapping(IRI.create(values[0]), IRI.create(values[0]));
	}

}
