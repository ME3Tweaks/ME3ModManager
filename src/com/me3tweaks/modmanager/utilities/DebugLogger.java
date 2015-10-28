package com.me3tweaks.modmanager.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.me3tweaks.modmanager.ModManager;

/**
 * Used in Mod Manager 2.0 and above as a way to write console messages to a
 * file when the jar file is wrapped in the exe.
 * 
 * @author FemShep
 *
 */
public class DebugLogger {
	File logFile;
	FileWriter fw;
	int messagesBeforeFlush = 10;
	int currentMessages = 0;
	public final static String LOGGING_FILENAME = "me3cmm_last_run_log.txt";
	private boolean initialized = false;

	public DebugLogger() {

	}

	/**
	 * Called if you want to use the debug logger.
	 */
	public void initialize() {
		if (initialized) {
			return;
		}
		logFile = new File(LOGGING_FILENAME);
		try {
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();
			//we now have write permissions
			fw = new FileWriter(logFile);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			fw.write("Logger init, time: " + dateFormat.format(date));
			fw.write(System.getProperty("line.separator"));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date date = new Date();
						writeMessage("Logger shutting down. Time: " + dateFormat.format(date));
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Cannot close filewriter. Giving up.");
						e.printStackTrace();
					}
				}
			});
			initialized = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logFile = null;
			ModManager.logging = false;
			System.out.println("Log failed to write! Cannot write log due to IOException");
			e.printStackTrace();
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	public synchronized void writeMessage(String message) {
		if (ModManager.logging) {
			try {
				System.out.println("[L]: " + message);
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
				currentMessages++;
				checkIfFlushNeeded();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Something crazy is preventing logs from being written!");
				e.printStackTrace();
			}
		}
	}

	public synchronized void writeException(Throwable e) {
		if (ModManager.logging) {
			try {
				System.err.println("[L-E]: " + ExceptionUtils.getStackTrace(e));
				fw.write(ExceptionUtils.getStackTrace(e));
				fw.write(System.getProperty("line.separator"));
				currentMessages++;
				checkIfFlushNeeded();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				ex.printStackTrace();
			} catch (Exception ex) {
				System.err.println("Something crazy is preventing logs from being written!");
				ex.printStackTrace();
			}
		}
	}

	public synchronized void writeError(String message) {
		if (ModManager.logging) {
			try {
				System.err.println("[L:E]: " + message);
				fw.write("WARN/ERROR: ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
				currentMessages++;
				checkIfFlushNeeded();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				e.printStackTrace();
			} catch (Exception e) {
				System.err.print("Something crazy is preventing logs from being written!");
				e.printStackTrace();
			}
		}
	}

	private void checkIfFlushNeeded() {
		if (ModManager.logging && currentMessages > messagesBeforeFlush) {
			try {
				fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			currentMessages = 0;
		}
	}

	public void writeErrorWithException(String error, Throwable e) {
		writeError(error);
		writeException(e);
	}

	/**
	 * Returns the log, assuming logging is enabled. If not, it will return the
	 * string "Logging is not enabled...". The log is flushed to disk and then
	 * read in from the file.
	 * 
	 * @return Log text
	 */
	public String getLog() {
		if (ModManager.logging) {
			try {
				ModManager.debugLogger.writeMessage("Flushing log to disk and returning log contents");
				fw.flush();
				return FileUtils.readFileToString(new File(LOGGING_FILENAME));
			} catch (IOException e) {
				return "Unable to flush log to disk (or other IOException): " + ExceptionUtils.getStackTrace(e);
			}
		} else {
			return "Mod Manager logging is not enabled. To enable it, please go to File>Options and turn it on.";
		}
	}

	public void writeMessageConditionally(String string, boolean condition) {
		if (condition) {
			writeMessage(string);
		}
	}
	
	public void writeErrorConditionally(String string, boolean condition) {
		if (condition) {
			writeError(string);
		}
	}
	
	
}
