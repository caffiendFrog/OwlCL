package com.essaid.jcommander.example.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(
    commandNames = "longdesc",
    commandDescription = "long description, long description, "
        + "long description, long description, long description, long description, "
        + "long description, long description, long description, long description, "
        + "long description, long description, long description, long description, long description, long description,"
        + " long description, long description, long description, long description, long description, ")
public class CommandWithLongDescription {
  
  @Parameter(names = "-name")
  String name;


}
