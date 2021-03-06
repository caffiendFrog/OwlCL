package com.essaid.owlcl.core.cli.util;

import java.io.File;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class DirectoryExistsValueValidator implements IValueValidator<File> {

	@Override
	public void validate(String name, File value) throws ParameterException {
		if (!value.isDirectory())
		{
			throw new ParameterException("Parameter " + name
					+ " is not set to a valid directory. Value: " + value);

		}

	}

}
