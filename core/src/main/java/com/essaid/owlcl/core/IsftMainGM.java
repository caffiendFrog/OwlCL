package com.essaid.owlcl.core;

import java.util.ServiceLoader;

import com.beust.jcommander.CommandResult;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * Based on
 * http://stackoverflow.com/questions/902639/has-anyone-used-serviceloader-
 * together-with-guice
 * 
 * 
 */
public class IsftMainGM extends AbstractModule {

  @Override
  protected void configure() {
    for (Module module : ServiceLoader.load(IsftGM.class))
    {
      install(module);
    }

    TypeLiteral<IsftCommand<CommandResult>> commandType = new TypeLiteral<IsftCommand<CommandResult>>() {
    };

    bind(commandType).annotatedWith(com.essaid.owlcl.core.annotation.MainCommandQualifier.class).to(com.essaid.owlcl.command.MainCommand.class);
  }
}
