package com.essaid.jcommander.example;

import com.beust.jcommander.DefaultUsage;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.essaid.jcommander.example.command.CommandWithLongDescription;

public class TestLongCommandDescriptions {

  @Parameter(names = "-main")
  String main;

  public static void main(String[] args) {
    TestLongCommandDescriptions mainObject = new TestLongCommandDescriptions();
    JCommander jc = new JCommander(mainObject);
    jc.addCommand(new CommandWithLongDescription());

    jc.parse("longdesc");
    DefaultUsage.usage(jc);

  }

}
