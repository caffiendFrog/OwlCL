package com.essaid.owlcl.core.util;

import java.nio.file.Path;

public interface IReportFactory {

  Report createReport(String name, Path directory, ILoggerOwner loggerOwner);

}
