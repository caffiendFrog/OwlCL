package com.essaid.owlcl.core.cli.util;

import java.io.File;
import java.util.List;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class FileListValueValidator implements IValueValidator<List<File>> {

	@Override
	public void validate(String name, List<File> value) throws ParameterException {

		for (File file : value)
		{

			if (!file.exists())
			{
				throw new ParameterException("File: " + file.getAbsolutePath()
						+ " does not exist. Parameter name: " + name);
			}
		}

	}

}
