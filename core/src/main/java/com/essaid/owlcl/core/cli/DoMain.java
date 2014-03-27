package com.essaid.owlcl.core.cli;

import java.util.Set;

import com.beust.jcommander.DefaultUsage;
import com.essaid.owlcl.command.NullCommand;
import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.OwlclCoreGModule;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.essaid.owlcl.core.util.DefaultOwlclManager;
import com.essaid.owlcl.core.util.IOwlclManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class DoMain {

  public DoMain(DefaultOwlclManager dm, String[] args) {
    run(dm, args);
  }

  private void run(final DefaultOwlclManager dm, String[] args) {

    Injector injector = Guice.createInjector(new OwlclCoreGModule(), new AbstractModule() {

      @Override
      protected void configure() {
        bind(IOwlclManager.class).toInstance(dm);

      }
    });

    IOwlclCommandFactory main = null;

    main = injector.getInstance(Key.get(IOwlclCommandFactory.class,
        Names.named(OwlclCommand.CORE_MAIN)));

    OwlclCommand mainCommand = main.getCommand(new NullCommand());

    Key<Set<IOwlclCommandFactory>> fkey = new Key<Set<IOwlclCommandFactory>>(
        TopCommandQualifier.class) {
    };

    for (IOwlclCommandFactory f : injector.getInstance(fkey))
    {
      mainCommand.addCommand(f.getCommand(mainCommand));

    }

    mainCommand.initialize();
    DefaultUsage.usage(mainCommand);
    mainCommand.parse(args);
    try
    {
      mainCommand.call();
    } catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
