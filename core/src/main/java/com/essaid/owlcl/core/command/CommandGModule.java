package com.essaid.owlcl.core.command;

import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.OwlclGModule;
import com.essaid.owlcl.core.util.GuiceUtils;
import com.google.inject.AbstractModule;

public class CommandGModule extends AbstractModule implements OwlclGModule {

  @Override
  protected void configure() {

    GuiceUtils.installCommandFactory(binder(), MainCommand.class, OwlclCommand.CORE_MAIN);
  }

}
