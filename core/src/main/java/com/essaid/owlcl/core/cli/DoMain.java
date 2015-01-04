package com.essaid.owlcl.core.cli;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beust.jcommander.DefaultUsage;
import com.essaid.owlcl.core.IOwlclCommandFactory;
import com.essaid.owlcl.core.IOwlclManager;
import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.annotation.TopCommandQualifier;
import com.essaid.owlcl.core.guice.OwlclCoreGModule;
import com.essaid.owlcl.core.util.DefaultLogConfigurator;
import com.essaid.owlcl.core.util.DefaultOwlclManager;
import com.essaid.owlcl.core.util.ILogConfigurator;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Names;

public class DoMain {

  static void run(final DefaultOwlclManager dm, String[] args) {
    
    Logger.getLogger(AssistedInject.class.getName()).setLevel(Level.SEVERE);

    Injector injector = Guice.createInjector(new OwlclCoreGModule(), new AbstractModule() {

      @Override
      protected void configure() {
        bind(IOwlclManager.class).toInstance(dm);

        // this does default initialization that is then further refined from
        // commandline.
        bind(ILogConfigurator.class).toInstance(new DefaultLogConfigurator(dm));
      }
    });

    // look for main command
    Binding<IOwlclCommandFactory> binding = injector.getExistingBinding(Key.get(
        IOwlclCommandFactory.class, Names.named(OwlclCommand.CORE_MAIN)));

    if (binding != null)
    {
      IOwlclCommandFactory mainFactory = binding.getProvider().get();
      // mainFactory = injector.getInstance(Key.get(IOwlclCommandFactory.class,
      // Names.named(OwlclCommand.CORE_MAIN)));

      OwlclCommand mainCommand = mainFactory.getCommand(null);
      mainCommand.setAllowAbbreviatedOptions(false);
      mainCommand.setCaseSensitiveOptions(true);

      Key<Set<IOwlclCommandFactory>> topFactoryKey = new Key<Set<IOwlclCommandFactory>>(
          TopCommandQualifier.class) {
      };

      for (IOwlclCommandFactory topFactory : injector.getInstance(topFactoryKey))
      {
        mainCommand.addCommand(topFactory.getCommand(mainCommand));
      }

      mainCommand.initialize();
      if (args.length == 0)
      {
        DefaultUsage.usage(mainCommand);
      }
      mainCommand.parse(args);
      try
      {
        mainCommand.call();
      } catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } else
    {
      System.out.println("Could not locate main command, probably not installed.");
    }
  }

}
