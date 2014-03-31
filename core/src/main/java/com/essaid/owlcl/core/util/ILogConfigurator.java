package com.essaid.owlcl.core.util;

import java.io.File;

public interface ILogConfigurator {

  String DEBUG = "debug";
  String INFO = "info";
  String WARN = "warn";

  void setDirectory(File directory);

  void setLogLevel(String level);
  
  void enableConsole();

  void disableConsole();

}
