package com.essaid.owlcl.core;

import com.beust.jcommander.Command;

public abstract class OwlclCommand extends Command {

  public static final String CORE_MAIN = "owlcl.core.command.main";


  protected OwlclCommand parent;

  public OwlclCommand(OwlclCommand parentCommand) {
    this.parent = parentCommand;
  }
}
