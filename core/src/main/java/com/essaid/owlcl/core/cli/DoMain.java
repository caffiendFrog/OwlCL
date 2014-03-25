package com.essaid.owlcl.core.cli;

import com.beust.jcommander.CommandResult;
import com.beust.jcommander.DefaultUsage;
import com.essaid.owlcl.core.IsftCommand;
import com.essaid.owlcl.core.IsftMainGM;
import com.essaid.owlcl.core.annotation.MainCommandQualifier;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class DoMain {

  public void run(String[] args) throws Exception {

    Injector injector = Guice.createInjector(new IsftMainGM());
    TypeLiteral<IsftCommand<CommandResult>> commandType = new TypeLiteral<IsftCommand<CommandResult>>() {
    };
    IsftCommand<CommandResult> main = injector.getInstance(Key.get(commandType,
        MainCommandQualifier.class));
    main.initialize();
    DefaultUsage.usage(main);
    main.parse(args);
    main.call();
  }

}
