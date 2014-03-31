package com.essaid.owlcl.core.util;

import java.io.File;

public interface IReportFactory {

  Report createReport(String name, File directory, ILoggerOwner loggerOwner);

}
