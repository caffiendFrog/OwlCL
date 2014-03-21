package isf.util;

import isf.command.AbstractCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Report {

	private PrintWriter pw;
	private PrintWriter pwDetailed;
	private int counter = 0;
	private AbstractCommand command;

	public Report(AbstractCommand command, String relativeFilePath) {

		this.command = command;
		try
		{
			pw = new PrintWriter(new File(command.getMain().getOutputDirectory(), relativeFilePath
					+ ".txt"));
			pwDetailed = new PrintWriter(new File(command.getMain().getOutputDirectory(),
					relativeFilePath + "-detailed.txt"));
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
		command.logger.error(value);
		pw.println(number + value);
		pwDetailed.println(number + value);
		doConsole(number + value);
	}

	public void warn(String value) {
		String number = getNextLineNumber();
		command.logger.warn(value);
		pw.println(number + value);
		pwDetailed.println(number + value);
		doConsole(number + value);
	}

	public void info(String value) {
		String number = getNextLineNumber();
		command.logger.info(value);
		pw.println(number + value);
		pwDetailed.println(number + value);
		doConsole(number + value);

	}

	public void detail(String value) {
		String number = getNextLineNumber();
		command.logger.debug(value);
		pwDetailed.println(number + value);
		// doConsole(value);
	}

	public void finish() {
		pw.println("\n=====  Finished report!  ======");
		pw.close();
		pwDetailed.println("\n=====  Finished report!  ======");
		pwDetailed.close();
		doConsole("\n=====  Finished report!  ======");
	}

	private void doConsole(String value) {
		if (!command.getMain().isQuiet())
		{
			System.out.println(value);
		}
	}
}