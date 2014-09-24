package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Used in Mod Manager 2.0 and above as a way to write console messages to a file when the jar file is wrapped in the exe.
 * @author FemShep
 *
 */
public class DebugLogger {
	File logFile;
	FileWriter fw;
	
	public DebugLogger(){
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
	
	protected void writeMessage(String message){
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
}
