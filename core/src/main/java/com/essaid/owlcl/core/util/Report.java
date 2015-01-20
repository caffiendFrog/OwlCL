package com.essaid.owlcl.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class Report {

  private PrintWriter pw;
  private PrintWriter pwDetailed;
  private int counter = 0;
  private Logger logger;

  @Inject
	public Report(@Assisted String name, @Assisted Path directory,
			@Assisted ILoggerOwner loggerOwner) throws IOException {
    this.logger = loggerOwner.getLogger();

    if(!Files.exists(directory)){
    	Files.createDirectories(directory);
    }
    
    try
    {
      pw = new PrintWriter(new File(directory.toFile(), name + ".txt"));
      pwDetailed = new PrintWriter(new File(directory.toFile(), name + "-detailed.txt"));
    } catch (FileNotFoundException e)
    {
      throw new RuntimeException("Failed to create report files", e);
    }

  }

  private String getNextLineNumber() {
    String lineNumber = "00000000000" + ++counter;
    int len = lineNumber.length();
    return lineNumber.substring(len - 6, len) + ") ";
  }

  public void error(String value) {
    String number = getNextLineNumber();
    logger.error(value);
    pw.println(number + value);
    pwDetailed.println(number + value);
  }

  public void warn(String value) {
    String number = getNextLineNumber();
    logger.warn(value);
    pw.println(number + value);
    pwDetailed.println(number + value);
  }

  public void info(String value) {
    String number = getNextLineNumber();
    logger.info(value);
    pw.println(number + value);
    pwDetailed.println(number + value);

  }

  public void detail(String value) {
    String number = getNextLineNumber();
    logger.debug(value);
    pwDetailed.println(number + value);
    // doConsole(value);
  }

  public void finish() {
    pw.println("\n=====  Finished report!  ======");
    pw.close();
    pwDetailed.println("\n=====  Finished report!  ======");
    pwDetailed.close();
  }
}