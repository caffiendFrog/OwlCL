package com.essaid.owlcl.core.util;

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import com.essaid.owlcl.core.IOwlclManager;
import com.google.inject.Inject;

public class DefaultLogConfigurator implements ILogConfigurator {

  private LoggerContext context;
  private Logger rootLogger;
  private Appender<ILoggingEvent> consoleAppender;
  private IOwlclManager manager;

  private PatternLayoutEncoder encoder;
  private FileAppender<ILoggingEvent> appender;
  private PatternLayoutEncoder debugEncoder;
  private FileAppender<ILoggingEvent> debugAppender;

  @Inject
  public DefaultLogConfigurator(IOwlclManager manager) {
    this.manager = manager;

    context = (LoggerContext) LoggerFactory.getILoggerFactory();

    if (context != null)
    {
      rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
      consoleAppender = rootLogger.getAppender("console");

      // console, if not quiet, only shows warn level messages
      consoleAppender.addFilter(new Filter<ILoggingEvent>() {

        @Override
        public FilterReply decide(ILoggingEvent event) {
          if (event.getLevel().isGreaterOrEqual(Level.WARN))
          {
            return FilterReply.ACCEPT;
          }
          return FilterReply.DENY;
        }
      });

      // default is quiet console
      disableConsole();

      encoder = new PatternLayoutEncoder();
      encoder.setContext(context);
      encoder.setPattern("%r %c %level - %msg%n");
      encoder.start();

      debugEncoder = new PatternLayoutEncoder();
      debugEncoder.setContext(context);
      debugEncoder.setPattern("%r %c %level - %msg%n");
      debugEncoder.start();

      setDirectory(this.manager.getOutputDirectory());

    }
  }

  @Override
  public void enableConsole() {
    rootLogger.addAppender(consoleAppender);
  }

  @Override
  public void disableConsole() {
    rootLogger.detachAppender("console");
  }

  @Override
  public void setLogLevel(String level) {
    if (level.equals(ILogConfigurator.DEBUG))
    {
      rootLogger.setLevel(Level.DEBUG);
    } else if (level.equals(ILogConfigurator.INFO))
    {
      rootLogger.setLevel(Level.INFO);
    } else if (level.equals(ILogConfigurator.WARN))
    {
      rootLogger.setLevel(Level.WARN);
    }

  }

  @Override
  public void setDirectory(File directory) {

    if (context != null)
    {

      // info
      if (appender != null)
      {
        appender.stop();
        rootLogger.detachAppender(appender);
      }
      appender = new FileAppender<ILoggingEvent>();
      appender.setFile(new File(directory, "owlcl-log.txt").getAbsolutePath());
      appender.setContext(context);
      appender.setEncoder(encoder);
      appender.addFilter(new Filter<ILoggingEvent>() {

        @Override
        public FilterReply decide(ILoggingEvent event) {
          if (event.getLevel().isGreaterOrEqual(Level.INFO))
          {
            return FilterReply.ACCEPT;
          } else
          {
            return FilterReply.DENY;
          }
        }

      });
      appender.start();
      rootLogger.addAppender(appender);

      // debug
      if (debugAppender != null)
      {
        debugAppender.stop();
        rootLogger.detachAppender(debugAppender);
      }
      debugAppender = new FileAppender<ILoggingEvent>();
      debugAppender.setFile(new File(directory, "owlcl-log-debug.txt").getAbsolutePath());
      debugAppender.setContext(context);
      debugAppender.setEncoder(debugEncoder);
      debugAppender.start();
      rootLogger.addAppender(debugAppender);

    }

  }
}
