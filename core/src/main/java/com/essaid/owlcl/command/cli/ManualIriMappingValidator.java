package com.essaid.owlcl.command.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ManualIriMappingValidator implements IParameterValidator {

	@Override
	public void validate(String name, String value) throws ParameterException {

		String[] values = value.split("\\s+");
		if (values.length != 3 || !values[1].equals("=>"))
		{
			throw new ParameterException("A mannual mapping was specified but appears to be "
					+ "incorrectly formatted. Value: " + value);
		}

	}

}
