package com.essaid.owlcl.command;

import com.essaid.owlcl.core.OwlclCommand;

/**
 * This was mostly needed for Guice's assisted injection but it might be useful
 * for other situations. It should be used when a parent OwlclCommand is needed
 * for the topmost command's constructor instead of passing null.
 * 
 * @author Shahim Essaid
 * 
 */
public final class NullCommand extends OwlclCommand {

  public NullCommand() {
    super(null);
  }

  @Override
  public Object call() throws Exception {
    throw new IllegalStateException("The NullCommand was called.");
  }

}
