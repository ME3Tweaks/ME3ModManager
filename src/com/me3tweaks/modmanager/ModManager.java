package com.me3tweaks.modmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.w3c.dom.Document;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;

public class ModManager {

	public static final String VERSION = "3.1";
	public static long BUILD_NUMBER = 40L;

	public static final String BUILD_DATE = "7/22/2015";
	public static DebugLogger debugLogger;
	public static boolean IS_DEBUG = false;
	public static String settingsFilename = "me3cmm.ini";
	public static boolean logging = false; //default to true
	public static double MODMAKER_VERSION_SUPPORT = 1.6; //max modmaker version
	public static boolean AUTO_UPDATE_MODS = false;
	public static boolean ASKED_FOR_AUTO_UPDATE = false;
	public static long LAST_AUTOUPDATE_CHECK;

	public static void main(String[] args) {
		System.out.println("Starting mod manager");

		//SETUI LOOK
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't set the UI interface style");
		}
        ToolTipManager.sharedInstance().setDismissDelay(15000);


		//Set and get debugging mode from wini
		debugLogger = new DebugLogger();
		if (ModManager.IS_DEBUG) {
			debugLogger.initialize();
			logging = true;
			debugLogger.writeMessage("Starting logger due to Debug flag. Auto updates enabled");
			AUTO_UPDATE_MODS = true;
		} else {
			Wini settingsini;
			try {
				settingsini = new Wini(new File(ModManager.settingsFilename));
				String logStr = settingsini.get("Settings", "logging_mode");
				int logInt = 0;
				if (logStr != null && !logStr.equals("")) {
					try {
						logInt = Integer.parseInt(logStr);
						if (logInt > 0) {
							//logging is on
							System.out.println("Logging mode is enabled");
							debugLogger.initialize();
							logging = true;
							debugLogger.writeMessage("Starting logger. Mod Manager version" + ModManager.VERSION + " Build "
									+ ModManager.BUILD_NUMBER);
						} else {
							System.out.println("Logging mode disabled");
						}
					} catch (NumberFormatException e) {
						System.out.println("Number format exception reading the log mode - log mode disabled");
					}
				}
				String superDebugStr = settingsini.get("Settings", "superdebug");
				if (superDebugStr != null && superDebugStr.equals("SUPERDEBUG")) {
					System.out.println("Forcing SUPERDEBUG mode on");
					IS_DEBUG = true;
					debugLogger.initialize();
					logging = true;
					debugLogger.writeMessage("Starting logger. Mod Manager version" + ModManager.VERSION + " Build " + ModManager.BUILD_NUMBER);
				}
				String forcedVersion = settingsini.get("Settings", "forceversion");
				if (forcedVersion != null && !forcedVersion.equals("")) {
					System.out.println("Forcing version id: " + forcedVersion);
					BUILD_NUMBER = Long.parseLong(forcedVersion);
				}
				String autoupdate = settingsini.get("Settings", "autoupdatemods");
				if (autoupdate != null && autoupdate.equals("true")) {
					System.out.println("Enabling mod auto-updates");
					AUTO_UPDATE_MODS = true;
				} else {
					System.out.println("AUTO UPDATE: "+autoupdate);
				}

				if (AUTO_UPDATE_MODS == false) {
					String askedbefore = settingsini.get("Settings", "declinedautoupdate");
					if (askedbefore != null) {
						System.out.println("User answered auto updates before");
						ASKED_FOR_AUTO_UPDATE = true;
					}
				}

				//last check date
				String lastAutoCheck = settingsini.get("Settings", "lastautocheck");
				if (lastAutoCheck != null) {
					try {
						LAST_AUTOUPDATE_CHECK = Long.parseLong(lastAutoCheck);
					} catch (NumberFormatException e) {
						System.err.println("Error: NFE in auto update check, skipping");
					}
				}

			} catch (InvalidFileFormatException e) {
				System.out.println("Invalid file format exception. Settings in this file will be ignored");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
			}
		}

		boolean isUpdate = false;
		if (args.length > 1 && args[0].equals("--update-from")) {
			//This is being run as an update
			try {
				long oldbuild = Long.parseLong(args[1]);
				if (oldbuild >= ModManager.BUILD_NUMBER) {
					//SOMETHING WAS WRONG!
					JOptionPane.showMessageDialog(null, "Update failed! Still using Build " + ModManager.BUILD_NUMBER + ".", "Update Failed",
							JOptionPane.ERROR_MESSAGE);
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
		ModManager.debugLogger.writeMessage("ME3CMM is running from: " + System.getProperty("user.dir"));
		doFileSystemUpdate();
		ModManager.debugLogger.writeMessage("========End of startup=========");
		new ModManagerWindow(isUpdate);
	}

	/**
	 * Moves folders to data/ and mods/ from configurations prior to Build 40
	 */
	private static void doFileSystemUpdate() {
		//check classic folders (same as me3cmm.exe)
		//move to new mods/ directory
		File modsDir = new File(ModManager.getModsDir());
		if (!modsDir.exists()) {
			modsDir.mkdirs();
		}

		ModManager.debugLogger.writeMessage("==Looking for mods in running directory, will move valid ones to mods/==");
		ArrayList<Mod> modsToMove = new ArrayList<Mod>(getValidMods(System.getProperty("user.dir")));
		for (Mod mod : modsToMove) {
			try {
				FileUtils.moveDirectory(new File(mod.getModPath()), new File(ModManager.getModsDir() + mod.getModName()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE MOD TO mods/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}

		//Move ME3Explorer
		ModManager.debugLogger.writeMessage("Checking if using old ME3Explorer dir");
		File me3expDir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "ME3Explorer/");
		if (me3expDir.exists()) {
			ModManager.debugLogger.writeMessage("Moving ME3Explorer to data/");
			try {
				FileUtils.moveDirectory(me3expDir, new File(ModManager.getME3ExplorerEXEDirectory(false)));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE ME3EXPLORER TO /data/ME3EXPLORER!");
				ModManager.debugLogger.writeException(e);
			}
		}

		//Move TankMaster Compiler
		ModManager.debugLogger.writeMessage("Checking if using old tankmaster compiler dir");
		File tcoalDiD = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "Tankmaster Compiler/");
		if (tcoalDiD.exists()) {
			ModManager.debugLogger.writeMessage("Moving TankMaster Compiler to data/");
			try {
				FileUtils.moveDirectory(tcoalDiD, new File(ModManager.getTankMasterCompilerDir()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE TANKMASTER COMPILER TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}
		//Move TankMaster TLK
		ModManager.debugLogger.writeMessage("Checking if using old tankmaster tlk dir");
		File tlkdir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "Tankmaster TLK/");
		if (tlkdir.exists()) {
			ModManager.debugLogger.writeMessage("Moving TankMaster TLK to data/");
			try {
				FileUtils.moveDirectory(tlkdir, new File(ModManager.getTankMasterTLKDir()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE TANKMASTER TLK TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}

		//move update folder
		ModManager.debugLogger.writeMessage("Checking if using old update dir");
		File updatedir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "update/");
		if (updatedir.exists()) {
			ModManager.debugLogger.writeMessage("Moving update to data/");
			try {
				FileUtils.moveDirectory(updatedir, new File(ModManager.getUpdateDir()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE update TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}
		
		//move databases folder
		ModManager.debugLogger.writeMessage("Checking if using old databases dir");
		File databasedir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "databases/");
		if (databasedir.exists()) {
			ModManager.debugLogger.writeMessage("Moving update to databases/");
			try {
				FileUtils.moveDirectory(databasedir, new File(ModManager.getDatabaseDir()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE databases TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}
		
		//move coalesced.original folder
		ModManager.debugLogger.writeMessage("Checking if should move coalesced.original");
		File coalOrig = new File("Coalesced.original");
		if (coalOrig.exists()) {
			ModManager.debugLogger.writeMessage("Moving Coalesced.original to data/");
			try {
				FileUtils.moveFile(coalOrig, new File(ModManager.getDataDir()+"Coalesced.original"));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE Coalesced.original TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}
		
		//cleanup
		File mod_info = new File(ModMakerCompilerWindow.DOWNLOADED_XML_FILENAME);
		mod_info.delete();
		File tlk = new File("tlk");
		File toc = new File("toc");
		File coalesceds = new File("coalesceds");
		try {
			FileUtils.deleteDirectory(toc);
			FileUtils.deleteDirectory(tlk);
			FileUtils.deleteDirectory(coalesceds);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public static ArrayList<Mod> getModsFromDirectory() {
		ModManager.debugLogger.writeMessage("==Getting list of mods in mods directory==");
		File modsDir = new File(ModManager.getModsDir());
		ArrayList<Mod> availableMod = new ArrayList<Mod>(getValidMods(modsDir.getAbsolutePath()));
		Collections.sort(availableMod);
		return availableMod;
	}

	/**
	 * Gets valid mods from the given directory by looking for subfolders with
	 * moddesc.ini files
	 * 
	 * @return
	 */
	private static ArrayList<Mod> getValidMods(String path) {
		File modsDir = new File(path);
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] subdirs = modsDir.listFiles(fileFilter);
		ArrayList<Mod> availableMod = new ArrayList<Mod>();

		if (subdirs != null && subdirs.length > 0) {
			//Got a list of subdirs. Now loop them to find all moddesc.ini files
			for (int i = 0; i < subdirs.length; i++) {
				File searchSubDirDesc = new File(ModManager.appendSlash(subdirs[i].toString()) + "moddesc.ini");
				System.out.println("Searching for file: " + searchSubDirDesc);
				if (searchSubDirDesc.exists()) {
					Mod validatingMod = new Mod(ModManager.appendSlash(subdirs[i].getAbsolutePath()) + "moddesc.ini");
					if (validatingMod.isValidMod()) {
						availableMod.add(validatingMod);
					}
				}
			}
		}

		return availableMod;
	}

/*	public static ArrayList<Mod> getCMM3ModsFromDirectory() {
		File fileDir = new File(getModsDir());
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] subdirs = fileDir.listFiles(fileFilter);

		//Got a list of subdirs. Now loop them to find all moddesc.ini files
		ArrayList<Mod> availableMod = new ArrayList<Mod>();
		for (int i = 0; i < subdirs.length; i++) {
			File searchSubDirDesc = new File(ModManager.appendSlash(subdirs[i].toString()) + "moddesc.ini");
			if (searchSubDirDesc.exists()) {
				Mod validatingMod = new Mod(ModManager.appendSlash(subdirs[i].getAbsolutePath()) + "moddesc.ini");
				if (validatingMod.isValidMod() && validatingMod.modCMMVer >= 3) {
					availableMod.add(validatingMod);
				}
			}
		}

		
		 * for (Mod i:availableMod){
		 * ModManagerWindow.listDescriptors.put(i.getModName(),i); }
		 
		return availableMod;
	}*/

	/**
	 * Checks for a file called Coalesced.original. If it exists, it will exit
	 * this method, otherwise it will backup the current Coalesced and check
	 * it's MD5 again the known original Coalesced.
	 * 
	 */
	public static boolean checkDoOriginal(String origDir) {
		String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e"; //This is Patch 3 Coalesced's hash
		File cOriginal = new File(ModManager.getDataDir()+"Coalesced.original");
		if (cOriginal.exists() == false) {
			//Attempt to copy an original
			try {
				String coalDirHash = MD5Checksum.getMD5Checksum(ModManager.appendSlash(origDir) + "CookedPCConsole\\Coalesced.bin");
				ModManager.debugLogger.writeMessage("Patch 3 Coalesced Original Hash: " + coalDirHash);
				ModManager.debugLogger.writeMessage("Current Patch 3 Coalesced Hash: " + patch3CoalescedHash);

				if (!coalDirHash.equals(patch3CoalescedHash)) {
					String[] YesNo = { "Yes", "No" };
					int keepInstalling = JOptionPane
							.showOptionDialog(
									null,
									"There is no backup of your original Coalesced yet.\nThe hash of the Coalesced in the directory you specified does not match the known hash for Patch 3's Coalesced.bin.\nYour Coalesced.bin's hash: "
											+ coalDirHash
											+ "\nPatch 3 Coalesced.bin's hash: "
											+ patch3CoalescedHash
											+ "\nYou can continue, but you might lose access to your original Coalesced.\nYou can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq if you need to restore your original.\nContinue installing this mod? ",
									"Coalesced Backup Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, YesNo, YesNo[1]);
					if (keepInstalling == 0)
						return true;
					return false;
				} else {
					//Make a backup of it
					String destFile = ModManager.getDataDir()+"Coalesced.original";
					String sourceFile = ModManager.appendSlash(origDir) + "Coalesced.bin";
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
						ModManager.debugLogger
								.writeMessage("Error backing up the original Coalesced. Hash matched but we had an I/O exception. Aborting install.");
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

	/**
	 * Export a resource embedded into a Jar file to the local file path.
	 *
	 * @param resourceName
	 *            ie.: "/SmartLibrary.dll"
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
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			jarFolder = new File(ModManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()
					.replace('\\', '/');
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

	public static boolean installLauncherWV(String biogamedir) {
		ModManager.debugLogger.writeMessage("Installing Launcher_WV.exe bypass");
		File bgdir = new File(biogamedir);
		if (!bgdir.exists()) {
			JOptionPane.showMessageDialog(null,
					"The BioGame directory is not valid.\nMod Manager cannot install the DLC bypass.\nFix the BioGame directory before continuing.",
					"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File gamedir = bgdir.getParentFile();

		File launcherWV = new File(gamedir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
		ModManager.debugLogger.writeMessage("Set binary win32 folder to game folder to: " + launcherWV.getAbsolutePath());

		//File bink32_orig = new File(gamedir.toString()+"\\Binaries\\Win32\\binkw32_orig.dll");

		//File bink32 = new File("dlcpatcher/binkw32.dll");

		try {
			ModManager.ExportResource("/Launcher_WV.exe", launcherWV.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			JOptionPane.showMessageDialog(null, "An error occured extracting Launcher_WV.exe out of the ME3CMM.exe.\nPlease report this to femshep.",
					"Launcher_WV.exe error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public static boolean installBinkw32Bypass(String biogamedir) {
		ModManager.debugLogger.writeMessage("Installing binkw32.dll DLC authorizer. Will backup original to binkw23.dll");
		//extract and install binkw32.dll
		//from http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
		//ClassLoader cl = ModManager.class.getClassLoader();

		File bgdir = new File(biogamedir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");

		//File bink32 = new File("dlcpatcher/binkw32.dll");
		if (bink32.exists()) {
			//if we got here binkw32.dll should have failed the hash check
			Path source = Paths.get(bink32.toString());
			Path destination = Paths.get(bink32_orig.toString());
			//create backup of original
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		try {
			ModManager.ExportResource("/binkw32.dll", bink32.toString());
		} catch (Exception e1) {
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			e1.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean uninstallBinkw32Bypass(String biogamedir) {
		ModManager.debugLogger.writeMessage("Uninstalling binkw32.dll DLC authorizer. Will restore original from binkw23.dll.");
		//extract and install binkw32.dll
		//from http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
		//ClassLoader cl = ModManager.class.getClassLoader();

		File bgdir = new File(biogamedir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");

		//File bink32 = new File("dlcpatcher/binkw32.dll");
		if (bink32_orig.exists()) {
			//safe binkw32 exists. copy it over the roiginal.
			Path source = Paths.get(bink32_orig.toString());
			Path destination = Paths.get(bink32.toString());
			//create backup of original
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
				bink32_orig.delete();
			} catch (IOException ex) {
				ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(ex));
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	/**
	 * Appends a slash onto the end of a string if not already there.
	 * 
	 * @param string
	 *            Original string
	 * @return Original string with a slash on the end if it was not there
	 *         previously.
	 */
	public static String appendSlash(String string) {
		if (string.charAt(string.length() - 1) == File.separatorChar) {
			return string;
		} else {
			return string + File.separator;
		}
	}
	
	/**
     * Convert a millisecond duration to a string format
     * 
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }

	/**
	 * Gets the mods directory, including a final slash
	 * 
	 * @return
	 */
	public static String getModsDir() {
		return appendSlash(System.getProperty("user.dir")) + "mods/";
	}

	/**
	 * Gets the data/ folder, returning with an appended slash
	 * 
	 * @return
	 */
	public static String getDataDir() {
		return appendSlash(System.getProperty("user.dir")) + "data/";
	}

	public static String getTankMasterCompilerDir() {
		File file = new File(getDataDir() + "tankmaster_coalesce/");
		//file.mkdirs();
		return appendSlash(file.getAbsolutePath());	}

	public static String getTankMasterTLKDir() {
		File file = new File(getDataDir() + "tankmaster_tlk/");
		//file.mkdirs();
		return appendSlash(file.getAbsolutePath());	}

	public static String getUpdateDir() {
		File file = new File(getDataDir() + "update/");
		//file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Gets ME3Explorer directory, with slash on the end
	 * 
	 * @param showDialog
	 *            set to true to show dialog if me3explorer is not found
	 * @return
	 */
	public static String getME3ExplorerEXEDirectory(boolean showDialog) {
		File me3expdir = new File(getDataDir()+"ME3Explorer/");
		if (!me3expdir.exists() && showDialog) {
			JOptionPane.showMessageDialog(null,
					"Unable to find ME3Explorer in the data directory.\nME3Explorer is required for Mod Manager to work properly.",
					"ME3Explorer Error", JOptionPane.ERROR_MESSAGE);
		}
		return appendSlash(me3expdir.getAbsolutePath());
	}

	/**
	 * Gets the modmaker compiling directory
	 * 
	 * @return
	 */
	public static String getCompilingDir() {
		File file = new File(getDataDir() + "modmaker/");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static String getPristineDir() {
		File file = new File(getCompilingDir() + "pristine/");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Returns if the specified coalesced (job) is in the pristine folder and
	 * the hash matches the known good value for it
	 * 
	 * @param name value to use to see if has pristine coalesced
	 * @param mode mode to parse name as
	 * @return true if pristine, false if doesn't exist (or otherwise)
	 */
	public static boolean hasPristineCoalesced(String name, int mode) {
		File coal = new File(getPristineCoalesced(name, mode));
		if (!coal.exists()) {
			return false;
		}
		//check hash
		try {
			String hash = MD5Checksum.getMD5Checksum(coal.getAbsolutePath());
			HashMap<String,String> coalHashes = ME3TweaksUtils.getCoalHashesMap();
			//convert to header so we can check MODTYPE in hashmap
			String key = "error";
			switch (mode) {
			case ME3TweaksUtils.FILENAME:
				key = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
				break;
			case ME3TweaksUtils.HEADER:
				//do nothing
				break;
			case ME3TweaksUtils.INTERNAL:
				key = ME3TweaksUtils.internalNameToHeaderName(name);
				break;
			}
			
			if (hash.equals(coalHashes.get(key))){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeError("ERROR GENERATING HASH FOR PRISTING COALESCED: "+coal.getAbsolutePath());
			ModManager.debugLogger.writeException(e);
			return false;
		}
		return false;
	}

	public static String getOverrideDir() {
		File f = new File(getDataDir() + "override/");
		f.mkdirs();
		return appendSlash(f.getAbsolutePath());
	}

	/**
	 * Gets the path of a pristine coalesced with the given filename
	 * @param mode ME3TweaksUtils mode indicating what the name variable is
	 * 
	 * @param basegame
	 * @return
	 */
	public static String getPristineCoalesced(String name, int mode) {
		switch (mode) {
		case ME3TweaksUtils.FILENAME:
			name = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
			break;
		case ME3TweaksUtils.HEADER:
			//do nothing
			break;
		case ME3TweaksUtils.INTERNAL:
			name = ME3TweaksUtils.internalNameToHeaderName(name);
			break;
		}
		
		File f = new File(getPristineDir() + Mod.getStandardFolderName(name));
		f.mkdirs();
		return appendSlash(f.getAbsolutePath()) + ME3TweaksUtils.headerNameToCoalFilename(name);
	}
	
	/**
	 * Gets the path of a pristine TOC with the given filename
	 * @param mode ME3TweaksUtils mode indicating what the name variable is
	 * 
	 * @param mode Mode indicating what name is as a constant type
	 * @return
	 */
	public static String getPristineTOC(String name, int mode) {
		switch (mode) {
		case ME3TweaksUtils.FILENAME:
			name = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
			break;
		case ME3TweaksUtils.HEADER:
			//do nothing
			break;
		case ME3TweaksUtils.INTERNAL:
			name = ME3TweaksUtils.internalNameToHeaderName(name);
			break;
		}
		
		File f = new File(getPristineDir() + Mod.getStandardFolderName(name));
		f.mkdirs();
		return appendSlash(f.getAbsolutePath()) + "PCConsoleTOC.bin";
	}
	
	/**
	 * Returns if the specified coalesced (job) is in the pristine folder and
	 * the hash matches the known good value for it
	 * 
	 * @param name value to use to see if has pristine coalesced
	 * @param mode mode to parse name as
	 * @return true if pristine, false if doesn't exist (or otherwise)
	 */
	public static boolean hasPristineTOC(String name, int mode) {
		File toc = new File(getPristineTOC(name, mode));
		if (!toc.exists()) {
			return false;
		}
		
		//check hash
		try {
			String hash = MD5Checksum.getMD5Checksum(toc.getAbsolutePath());
			HashMap<String,String> tocHashes = ME3TweaksUtils.getTOCHashesMap();
			//convert to header so we can check MODTYPE in hashmap
			String key = "error";
			switch (mode) {
			case ME3TweaksUtils.FILENAME:
				key = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
				break;
			case ME3TweaksUtils.HEADER:
				//do nothing
				break;
			case ME3TweaksUtils.INTERNAL:
				key = ME3TweaksUtils.internalNameToHeaderName(name);
				break;
			}
			
			if (hash.equals(tocHashes.get(key))){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeError("ERROR GENERATING HASH FOR PRISTING TOC: "+toc.getAbsolutePath());
			ModManager.debugLogger.writeException(e);
			return false;
		}
		return false;
	}

	public static String getTempDir() {
		File file = new File(getDataDir() + "temp");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static String getDatabaseDir() {
		File file = new File(getDataDir() + "databases");
		//file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}
}