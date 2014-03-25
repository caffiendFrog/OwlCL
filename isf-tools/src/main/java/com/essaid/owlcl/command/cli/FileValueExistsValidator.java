package com.essaid.owlcl.command.cli;

import java.io.File;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class FileValueExistsValidator implements IValueValidator<File> {

	@Override
	public void validate(String name, File value) throws ParameterException {
		if (!value.exists())
		{
			throw new ParameterException("File " + value.getAbsolutePath()
					+ " does not exist. Parameter: " + name);
		}

	}

}
