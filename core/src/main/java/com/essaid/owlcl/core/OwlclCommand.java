package com.essaid.owlcl.core;

import com.beust.jcommander.Command;

public abstract class OwlclCommand extends Command {

  public static final String CORE_MAIN = "owlcl.command.core.main";
  public static final String CORE_CATALOG = "owlcl.command.core.catalog";
  public static final String CORE_NEW_MODULE = "owlcl.command.core.newModule";

  protected OwlclCommand parent;

  public OwlclCommand(OwlclCommand parentCommand) {
    this.parent = parentCommand;
  }
}
