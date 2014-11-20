package com.me3tweaks.modmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class ModManager {
	
	public static final String VERSION = "3.0 Public Beta";
	public static long BUILD_NUMBER = 21L;
	public static final String BUILD_DATE = "11/19/2014";
	public static DebugLogger debugLogger;
	public static boolean IS_DEBUG = true;
	public static String settingsFilename = "me3cmm.ini";
	public static boolean logging = false;
	public static double MODMAKER_VERSION_SUPPORT = 1.0; //max modmaker version
	
	public static void main(String[] args) {		
		//Set and get debugging mode from wini
		debugLogger = new DebugLogger();
		if (ModManager.IS_DEBUG) {
			debugLogger.initialize();
			logging = true;
			debugLogger.writeMessage("Starting logger due to Debug flag");
		} else {
			Wini settingsini;
			try {
				settingsini = new Wini(new File(ModManager.settingsFilename));
				String logStr  = settingsini.get("Settings", "logging_mode");
				int logInt = 0;
				if (logStr!= null && !logStr.equals("")) {
					try {
						logInt = Integer.parseInt(logStr);
						if (logInt>0){
							//logging is on
							System.out.println("Logging mode is enabled");
							debugLogger.initialize();
							logging = true;
							debugLogger.writeMessage("Starting logger. Mod Manager version"+ModManager.VERSION+" Build "+ModManager.BUILD_NUMBER);
						} else {
							System.out.println("Logging mode disabled");
						}
					} catch (NumberFormatException e){
						System.out.println("Number format exception reading the log mode - log mod disabled");
					}
				}
			} catch (InvalidFileFormatException e) {
				System.out.println("Invalid file format exception. Logging mode disabled");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
			}
		}
		boolean isUpdate = false;
		if (args.length > 1 && args[0].equals("--update-from")){
			//This is being run as an update
			try {
				long oldbuild = Long.parseLong(args[1]); 
				if (oldbuild >= ModManager.BUILD_NUMBER) {
					//SOMETHING WAS WRONG!
					JOptionPane.showMessageDialog(null,
							"Update failed! Still using Build "+ModManager.BUILD_NUMBER+".",
							"Update Failed", JOptionPane.ERROR_MESSAGE);
					ModManager.debugLogger.writeMessage("UPDATE FAILED!");
				} else {
					//update ok
					ModManager.debugLogger.writeMessage("UPDATE SUCCEEDED!");
					File file = new File("update"); //Delete the update directory
					file.delete();
					isUpdate = true;
				}
			
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage("--update-from number format exception.");
			}

		}
		new ModManagerWindow(isUpdate);
	}
	
	public static String[] getModsFromDirectory(){
		File fileDir = new File(System.getProperty("user.dir"));
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		};
		File[] subdirs = fileDir.listFiles(fileFilter);
		
		//Got a list of subdirs. Now loop them to find all coalesced/patch/ini files
		ArrayList<Mod> availableMod = new ArrayList<Mod>();
		for(int i = 0; i<subdirs.length;i++){
			File searchSubDirDesc = new File(ModManagerWindow.appendSlash(subdirs[i].toString())+"moddesc.ini");
			if (searchSubDirDesc.exists()){
				Mod validatingMod = new Mod(ModManagerWindow.appendSlash(subdirs[i].getAbsolutePath())+"moddesc.ini");
				if (validatingMod.isValidMod()){
					availableMod.add(validatingMod);
				}
			}
		}
		
		for (Mod i:availableMod){
			ModManagerWindow.listDescriptors.put(i.getModName(),i);
		}
		if (availableMod.size()==0){
			return new String[]{"No Mods Available"};
		}
		String[] returnMods = new String[availableMod.size()];
		for(int i = 0; i<availableMod.size();i++){
			ModManager.debugLogger.writeMessage("Adding mod "+availableMod.get(i).getModName());
			returnMods[i]=availableMod.get(i).getModName();
		}
		Arrays.sort(returnMods,java.text.Collator.getInstance());
		return returnMods;
	}


	/** Checks for a file called Coalesced.original. If it exists, it will exit this method, otherwise it will backup the current Coalesced and check it's MD5 again the known original Coalesced.
	 * 
	 */
	public static boolean checkDoOriginal(String origDir) {
		String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e"; //This is Patch 3 Coalesced's hash
		File cOriginal = new File("Coalesced.original");
		if (cOriginal.exists() == false){
			//Attempt to copy an original
			try {
				String coalDirHash = MD5Checksum.getMD5Checksum(ModManagerWindow.appendSlash(origDir)+"CookedPCConsole\\Coalesced.bin");
				ModManager.debugLogger.writeMessage("Patch 3 Coalesced Original Hash: "+coalDirHash);
				ModManager.debugLogger.writeMessage("Current Patch 3 Coalesced Hash: "+patch3CoalescedHash);
				
				if (!coalDirHash.equals(patch3CoalescedHash)){
					String[] YesNo = {"Yes", "No"};
					int keepInstalling = JOptionPane.showOptionDialog(null,"There is no backup of your original Coalesced yet.\nThe hash of the Coalesced in the directory you specified does not match the known hash for Patch 3's Coalesced.bin.\nYour Coalesced.bin's hash: "+coalDirHash+"\nPatch 3 Coalesced.bin's hash: "+patch3CoalescedHash+"\nYou can continue, but you might lose access to your original Coalesced.\nYou can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq if you need to restore your original.\nContinue installing this mod? ", "Coalesced Backup Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, YesNo, YesNo[1]);
					if (keepInstalling == 0) return true;
					return false;
				} else {
					//Make a backup of it
					String destFile = "Coalesced.original";
					String sourceFile = ModManagerWindow.appendSlash(origDir)+"Coalesced.bin";
					String[] command = { "cmd.exe", "/c", "copy", "/Y", sourceFile, destFile };
							try {
								Process p = Runtime.getRuntime().exec(command);
								
								// The InputStream we get from the Process reads from the standard output
								// of the process (and also the standard error, by virtue of the line
								// copyFiles.redirectErrorStream(true) ).
								BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
								String line;
								do {
								    line = reader.readLine();
								    if (line != null) { 
										ModManager.debugLogger.writeMessage(line); 
									}
								} while (line != null);
								reader.close();
								
								p.waitFor();
							} catch (IOException e) {
								ModManager.debugLogger.writeMessage("Error backing up the original Coalesced. Hash matched but we had an I/O exception. Aborting install.");
								ModManager.debugLogger.writeMessage(e.getMessage());
								return false;
							} catch (InterruptedException e) {
								ModManager.debugLogger.writeMessage("Backup of the original Coalesced was interupted. Aborting install.");
								ModManager.debugLogger.writeMessage(e.getMessage());
								return false;
							}
							return true;
					}
			} catch (Exception e) {
				ModManager.debugLogger.writeMessage("Error occured while attempting to backup or hash the original Coalesced.");
				ModManager.debugLogger.writeMessage(e.getMessage());
				return false;
			}
		}
		//Backup exists
		return true;
	}
	
	public static String getME3ExplorerEXEDirectory(boolean showDialog) {
		File executable = new File(ModManagerWindow.appendSlash(System.getProperty("user.dir"))+"ME3Explorer.exe");
		ModManager.debugLogger.writeMessage("Searching for ME3Explorer exe: "+executable.getAbsolutePath());
		
		if (!executable.exists()){
			//try another file
			executable = new File("ME3Explorer\\ME3Explorer.exe");
			ModManager.debugLogger.writeMessage("Searching for ME3Explorer exe: "+executable.getAbsolutePath());
			if (!executable.exists()){
				ModManager.debugLogger.writeMessage("Could not find ME3Explorer.");
				if (showDialog) {
					StringBuilder sb = new StringBuilder();
					sb.append("Failed to find ME3Explorer.exe in the following directories:\n");
					sb.append(" - "+System.getProperty("user.dir")+"\n");
					sb.append(" - "+System.getProperty("user.dir")+"\\ME3Explorer\\"+"\n");
					JOptionPane.showMessageDialog(null, sb.toString(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
		}
		ModManager.debugLogger.writeMessage("Found ME3Explorer: "+executable.getAbsolutePath());
		return ModManagerWindow.appendSlash(executable.getParent());//ModManagerWindow.appendSlash("ME3Explorer_0102w_beta");
	}
	
	  /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
	 * @param exportPath 
     * @return The path to the exported resource
     * @throws Exception
     */
    public static String ExportResource(String resourceName, String exportPath) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        try {
            stream = ModManager.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(ModManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            //resStreamOut = new FileOutputStream(jarFolder + resourceName);
            resStreamOut = new FileOutputStream(exportPath);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            stream.close();
            resStreamOut.close();
        }

        return jarFolder + resourceName;
    }
}