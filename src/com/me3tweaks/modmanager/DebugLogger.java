package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;

/** Used in Mod Manager 2.0 and above as a way to write console messages to a file when the jar file is wrapped in the exe.
 * @author FemShep
 *
 */
public class DebugLogger {
	File logFile;
	FileWriter fw;
	
	public DebugLogger(){

	}
	
	/**
	 * Called if you want to use the debug logger.
	 */
	public void initialize(){
		logFile = new File("me3cmm_last_run_log.txt");
		try {
			if (logFile.exists()){
				logFile.delete();
			}
			logFile.createNewFile();
			//we now have write permissions
			fw = new FileWriter(logFile);
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			        try {
			        	System.out.println("Executing shutdown task");
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Cannot close filewriter. Giving up.");
						e.printStackTrace();
					}
			    }
			});	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logFile = null;
			ModManager.logging = false;
			System.out.println("Log failed to write! Cannot write log due to IOException");
			e.printStackTrace();
		}
	}
	
	public synchronized void writeMessage(String message){
		if (ModManager.logging){
			try {
				System.out.println("[L]: "+message);
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				e.printStackTrace();
			}
		}
	}

	public synchronized void writeException(Exception e) {
		if (ModManager.logging){
			try {
				System.err.println("[L-E]: "+ ExceptionUtils.getStackTrace(e));
				fw.write(ExceptionUtils.getStackTrace(e));
				fw.write(System.getProperty("line.separator"));
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				ex.printStackTrace();
			}
		}
	}

	public synchronized void writeError(String message) {
		if (ModManager.logging){
			try {
				System.err.println("[L:E]: "+message);
				fw.write("WARN/ERROR: ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("cannot write to log file! IOException");
				e.printStackTrace();
			}
		}
	}

	public void writeErrorWithException(String error, Exception e) {
		writeError(error);
		writeException(e);
	}
}
