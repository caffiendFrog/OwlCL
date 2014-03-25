package com.essaid.owlcl.core.cli;

import java.lang.reflect.Field;

import com.beust.jcommander.CommandResult;
import com.beust.jcommander.DefaultUsage;
import com.essaid.owlcl.core.IsftCommand;
import com.essaid.owlcl.core.IsftMainGM;
import com.essaid.owlcl.core.annotation.MainCommandQualifier;
import com.essaid.owlcl.util.OwlclUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class Main {

  public static void main(String[] args) throws Exception {

    OwlclUtil.instance().init();
    Class c = Main.class.getClassLoader().loadClass("Hello");
    System.out.println(c.getName());
    Field f = c.getField("i");
    f.setAccessible(true);
    System.out.println("Field value: " + f.getInt(null));
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
