package com.me3tweaks.modmanager;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.w3c.dom.Document;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModList;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.utilities.DebugLogger;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class ModManager {

	public static final String VERSION = "4.2.3";
	public static long BUILD_NUMBER = 56L;
	public static final String BUILD_DATE = "4/29/2016";
	public static DebugLogger debugLogger;
	public static boolean IS_DEBUG = false;
	public static final String SETTINGS_FILENAME = "me3cmm.ini";
	public static boolean logging = false;
	public static final double MODMAKER_VERSION_SUPPORT = 2.0; // max modmaker
																// version
	public static final double MODDESC_VERSION_SUPPORT = 4.2; // max supported
																// cmmver in
																// moddesc
	public static boolean AUTO_APPLY_MODMAKER_MIXINS = false;
	public static boolean AUTO_UPDATE_MODS = true;
	public static boolean CHECKED_FOR_UPDATE_THIS_SESSION = false;
	public static long LAST_AUTOUPDATE_CHECK;
	public static final int MIN_REQUIRED_ME3EXPLORER_MAIN = 2;
	public static final int MIN_REQUIRED_ME3EXPLORER_MINOR = 0;
	public final static int MIN_REQUIRED_ME3EXPLORER_REV = 3;
	private final static int MIN_REQUIRED_NET_FRAMEWORK_RELNUM = 378389; //4.5.0
	public static boolean USE_GAME_TOCFILES_INSTEAD = false;
	public static ArrayList<Image> ICONS;
	public static boolean AUTO_INJECT_KEYBINDS = false;
	public static boolean AUTO_UPDATE_MOD_MANAGER = true;
	public static boolean AUTO_UPDATE_ME3EXPLORER = true;
	public static boolean NET_FRAMEWORK_IS_INSTALLED = false;
	public static long SKIP_UPDATES_UNTIL_BUILD = 0;
	public static int AUTO_CHECK_INTERVAL_DAYS = 2;
	public static long AUTO_CHECK_INTERVAL_MS = TimeUnit.DAYS.toMillis(AUTO_CHECK_INTERVAL_DAYS);
	public static boolean LOG_MOD_INIT = false;
	public static boolean LOG_PATCH_INIT = false;
	public static boolean PERFORM_DOT_NET_CHECK = true;
	protected final static int COALESCED_MAGIC_NUMBER = 1836215654;
	public final static String[] KNOWN_GUI_CUSTOMDLC_MODS = {"DLC_CON_XBX", "DLC_CON_UIScaling", "DLC_CON_UIScaling_Shared"};
	public static final class Lock { } //threading wait() and notifyall();
	public static void main(String[] args) {
		loadLogger();
		boolean emergencyMode = false;
		boolean isUpdate = false;
		try {
			System.out.println("Starting Mod Manager");
			// SETUI LOOK
			try {
				// Set cross-platform Java L&F (also called "Metal")
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e) {
				System.err.println("Couldn't set the UI interface style");
			}

			ICONS = new ArrayList<Image>();
			ICONS.add(Toolkit.getDefaultToolkit().getImage(ModManager.class.getResource("/resource/icon32.png")));
			ICONS.add(Toolkit.getDefaultToolkit().getImage(ModManager.class.getResource("/resource/icon64.png")));

			ToolTipManager.sharedInstance().setDismissDelay(15000);

			File settings = new File(ModManager.SETTINGS_FILENAME);
			if (!settings.exists()) {
				settings.createNewFile();
			}

			// Set and get debugging mode from wini
			if (ModManager.IS_DEBUG) {
				debugLogger.initialize();
				logging = true;
				debugLogger.writeMessage("Starting logger, this is a debugging build.");
			}

			Wini settingsini;
			try {
				settingsini = new Wini(new File(ModManager.SETTINGS_FILENAME));
				{
					if (!ModManager.IS_DEBUG) {
						String logStr = settingsini.get("Settings", "logging_mode");
						int logInt = 0;
						if (logStr != null && !logStr.equals("")) {
							try {
								logInt = Integer.parseInt(logStr);
								if (logInt > 0) {
									// logging is on
									debugLogger.initialize();
									logging = true;
									debugLogger.writeMessage("Starting logger. Logger was able to start up with no issues.");
									debugLogger.writeMessage("Mod Manager version " + ModManager.VERSION + "; Build " + ModManager.BUILD_NUMBER
											+ "; Build Date " + BUILD_DATE);
								} else {
									debugLogger.writeMessage("Logging mode disabled");
								}
							} catch (NumberFormatException e) {
								debugLogger.writeMessage("Number format exception reading the log mode - log mode disabled");
							}
						} else {
							debugLogger.initialize();
							logging = true;
							debugLogger.writeMessage("Logging variable not set, defaulting to true. Starting logger. Mod Manager version "
									+ ModManager.VERSION + "; Build " + ModManager.BUILD_NUMBER + "; Build date " + BUILD_DATE);
						}
					}
				}
				// .NET encforcement check
				String netEnforcementStr = settingsini.get("Settings", "enforcedotnetrequirement");
				int netEnforcementInt = 0;
				if (netEnforcementStr != null && !netEnforcementStr.equals("")) {
					try {
						netEnforcementInt = Integer.parseInt(netEnforcementStr);
						if (netEnforcementInt > 0) {
							// logging is on
							debugLogger.writeMessage(".NET enforcement check is ON");
							PERFORM_DOT_NET_CHECK = true;
						} else {
							debugLogger.writeMessage(".NET enforcement check is OFF");
							PERFORM_DOT_NET_CHECK = false;
						}
					} catch (NumberFormatException e) {
						ModManager.debugLogger.writeError("Number format exception reading the .NET enforcement check flag, defaulting to enabled");
					}
				}
				// Auto Update Check
				String updateStr = settingsini.get("Settings", "checkforupdates");
				int updateInt = 0;
				if (updateStr != null && !updateStr.equals("")) {
					try {
						updateInt = Integer.parseInt(updateStr);
						if (updateInt > 0) {
							// logging is on
							debugLogger.writeMessage("Auto check for mod manager updates is enabled");
							AUTO_UPDATE_MOD_MANAGER = true;
						} else {
							debugLogger.writeMessage("Auto check for mod manager updates is disabled");
							AUTO_UPDATE_MOD_MANAGER = false;
						}
					} catch (NumberFormatException e) {
						ModManager.debugLogger.writeError("Number format exception reading the update check flag, defaulting to enabled");
					}
				}
				String superDebugStr = settingsini.get("Settings", "superdebug");
				if (superDebugStr != null && superDebugStr.equals("SUPERDEBUG")) {
					debugLogger.writeMessage("Forcing SUPERDEBUG mode on");
					IS_DEBUG = true;
					debugLogger.initialize();
					logging = true;
					debugLogger.writeMessage("Starting logger. Mod Manager version" + ModManager.VERSION + " Build " + ModManager.BUILD_NUMBER);
				}
				String forcedVersion = settingsini.get("Settings", "forceversion");
				if (forcedVersion != null && !forcedVersion.equals("")) {
					debugLogger.writeMessage("Forcing Mod Manager to think it is build number " + forcedVersion);
					BUILD_NUMBER = Long.parseLong(forcedVersion);
				}
				String autoupdate = settingsini.get("Settings", "autoupdatemods");
				if (autoupdate != null && autoupdate.toLowerCase().equals("false")) {
					debugLogger.writeMessage("Disabling mod auto-updates");
					AUTO_UPDATE_MODS = false;
				}

				// Autodownload ME3Explorer updates
				String autoupdateme3explorerStr = settingsini.get("Settings", "autodownloadme3explorer");
				int autoupdateme3explorerInt = 0;
				if (autoupdateme3explorerStr != null && !autoupdateme3explorerStr.equals("")) {
					try {
						autoupdateme3explorerInt = Integer.parseInt(autoupdateme3explorerStr);
						if (autoupdateme3explorerInt > 0) {
							// logging is on
							debugLogger.writeMessage("ME3Explorer updates are auto enabled");
							AUTO_UPDATE_ME3EXPLORER = true;
						} else {
							debugLogger.writeError("ME3Explorer updates are disabled - errors related to ME3EXplorer out of date ARE NOT SUPPORTED!");
							AUTO_UPDATE_ME3EXPLORER = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the me3explorer update preference - turning on by default");
						AUTO_UPDATE_ME3EXPLORER = true;
					}
				}

				// Autoinject keybinds
				String keybindsStr = settingsini.get("Settings", "autoinjectkeybinds");
				int keybindsInt = 0;
				if (keybindsStr != null && !keybindsStr.equals("")) {
					try {
						keybindsInt = Integer.parseInt(keybindsStr);
						if (keybindsInt > 0) {
							// logging is on
							debugLogger.writeMessage("Auto-Inject Keybinds are enabled");
							AUTO_INJECT_KEYBINDS = true;
						} else {
							debugLogger.writeMessage("Auto-Inject Keybinds is disabled");
							AUTO_INJECT_KEYBINDS = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the keybinds injection mode - autoinject mode disabled");
						AUTO_INJECT_KEYBINDS = false;
					}
				}
				// Autoinject modmaker mixins
				String autoinstallmixinsStr = settingsini.get("Settings", "autoinstallmixins");
				int autoinstallmixinsInt = 0;
				if (autoinstallmixinsStr != null && !autoinstallmixinsStr.equals("")) {
					try {
						autoinstallmixinsInt = Integer.parseInt(autoinstallmixinsStr);
						if (autoinstallmixinsInt > 0) {
							// logging is on
							debugLogger.writeMessage("Auto-Install of Modmaker Mixins is enabled");
							AUTO_APPLY_MODMAKER_MIXINS = true;
						} else {
							debugLogger.writeMessage("Auto-Install of Modmaker Mixins is disabled");
							AUTO_APPLY_MODMAKER_MIXINS = false;
						}
					} catch (NumberFormatException e) {
						debugLogger
								.writeError("Number format exception reading the auto install of modmaker mixins mode - autoinstall mode disabled");
						AUTO_APPLY_MODMAKER_MIXINS = false;
					}
				}

				// last check date
				String lastAutoCheck = settingsini.get("Settings", "lastautocheck");
				if (lastAutoCheck != null) {
					try {
						LAST_AUTOUPDATE_CHECK = Long.parseLong(lastAutoCheck);
					} catch (NumberFormatException e) {
						debugLogger.writeError("Error: Number Format Exception in LAST_AUTOUPDATE_CHECK, skipping");
					}
				}

				//update skip
				String showIfHigherThan = settingsini.get("Settings", "nextupdatedialogbuild");
				if (showIfHigherThan != null && !showIfHigherThan.equals("")) {
					try {
						SKIP_UPDATES_UNTIL_BUILD = Integer.parseInt(showIfHigherThan);
					} catch (NumberFormatException e) {
						ModManager.debugLogger
								.writeError("Number format exception reading the build number to skip to in settings. Defaulting to 0.");
						SKIP_UPDATES_UNTIL_BUILD = 0;
					}
				}

				// AutoTOC game files after install
				String autotocPostInstallStr = settingsini.get("Settings", "runautotocpostinstall");
				int autotocPostInstallInt = 0;
				if (autotocPostInstallStr != null && !autotocPostInstallStr.equals("")) {
					try {
						autotocPostInstallInt = Integer.parseInt(autotocPostInstallStr);
						if (autotocPostInstallInt > 0) {
							// logging is on
							debugLogger.writeMessage("AutoTOC post install is enabled");
							USE_GAME_TOCFILES_INSTEAD = true;
						} else {
							debugLogger.writeMessage("AutoTOC post install is disabled");
							USE_GAME_TOCFILES_INSTEAD = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the autotoc post install flag - defaulting to disabled");
						USE_GAME_TOCFILES_INSTEAD = false;
					}
				}

				// Log Mod Startup
				String modstartupStr = settingsini.get("Settings", "logmodinit");
				int modstartupInt = 0;
				if (modstartupStr != null && !modstartupStr.equals("")) {
					try {
						modstartupInt = Integer.parseInt(modstartupStr);
						if (modstartupInt > 0) {
							// logging is on
							debugLogger.writeMessage("Mod startup logging is enabled");
							LOG_MOD_INIT = true;
						} else {
							debugLogger.writeMessage("Mod startup logging is disabled");
							LOG_MOD_INIT = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the log mod init - setting to disabled");
						LOG_MOD_INIT = false;
					}
				}

			} catch (InvalidFileFormatException e) {
				ModManager.debugLogger.writeErrorWithException("Invalid file format exception. Settings in this file will be ignored", e);
			} catch (IOException e) {
				System.err.println("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
			}

			if (args.length > 1 && args[0].equals("--update-from")) {
				// This is being run as an update
				try {
					long oldbuild = Long.parseLong(args[1]);
					if (oldbuild >= ModManager.BUILD_NUMBER) {
						// SOMETHING WAS WRONG!
						JOptionPane.showMessageDialog(null, "Update failed! Still using Build " + ModManager.BUILD_NUMBER + ".", "Update Failed",
								JOptionPane.ERROR_MESSAGE);
						ModManager.debugLogger.writeMessage("UPDATE FAILED!");
					} else {
						// update ok
						ModManager.debugLogger.writeMessage("UPDATE SUCCEEDED!");
						File file = new File("update"); // Delete the update
														// directory
						file.delete();
						isUpdate = true;
					}

				} catch (NumberFormatException e) {
					ModManager.debugLogger.writeMessage("--update-from number format exception.");
				}
			}
			if (args.length > 1 && args[0].equals("--minor-update-from")) {
				// This is being run as a minor update
				try {
					long oldbuild = Long.parseLong(args[1]);
					if (oldbuild == ModManager.BUILD_NUMBER) {
						// SOMETHING WAS WRONG!
						JOptionPane.showMessageDialog(null, "Minor update was applied.", "Update OK", JOptionPane.INFORMATION_MESSAGE);
						ModManager.debugLogger.writeMessage("MINOR UPDATE OK!");
					}
				} catch (NumberFormatException e) {
					ModManager.debugLogger.writeMessage("--minor-update-from number format exception.");
				}
			}
			ModManager.debugLogger.writeMessage("ME3CMM is running from: " + System.getProperty("user.dir"));
			ModManager.debugLogger.writeMessage("System information:");
			ModManager.debugLogger.writeMessage(getSystemInfo());
			doFileSystemUpdate();
			if (!validateNETFrameworkIsInstalled()) {
				new NetFrameworkMissingWindow(
						"Mod Manager was unable to detect a usable .NET Framework. Mod Manager requires Microsoft .NET Framework 4.5 or higher in order to function properly. ");
			}
			ModManager.debugLogger.writeMessage("========End of startup=========");
		} catch (Throwable e) {
			Wini ini;
			try {
				File settings = new File(ModManager.SETTINGS_FILENAME);
				if (!settings.exists()) {
					settings.createNewFile();
				}
				ini = new Wini(settings);
				ini.put("Settings", "logging_mode", "1");
				ini.store();
			} catch (Exception error) {
				emergencyMode = true;
				ModManager.debugLogger.writeErrorWithException("Unable to save settings. We are now in emergency startup mode.", e);
			}
			debugLogger.initialize();
			logging = true;
			debugLogger.writeErrorWithException("A throwable was thrown during Mod Manager Startup.", e);
			if (emergencyMode) {
				debugLogger
						.writeMessage("Logger starting in emergency mode. Startup failed as well as logging settings, but logger was able to initialize.");
			} else {
				debugLogger.writeMessage("Logger starting in limited mode. Startup failed but logger was able to initialize.");
			}
			debugLogger.writeMessage("Mod Manager version " + ModManager.VERSION + " Build " + ModManager.BUILD_NUMBER);
			if (emergencyMode) {
				JOptionPane.showMessageDialog(null, "<html>An unknown error occured during Mod Manager startup:<br>" + e.getMessage() + "<br>"
						+ "Logging mode was attempted to be turned on, but failed. Logging for this session has been enabled.<br>"
						+ "Mod Manager will attempt to continue startup with limited resources and defaults.<br>"
						+ "Something is very wrong and Mod Manager will likely not function properly.</html>", "Critical Startup Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "<html>An unknown error occured during Mod Manager startup:<br>" + e.getMessage() + "<br>"
						+ "Mod Manager will attempt to continue startup with limited resources and defaults.<br>"
						+ "Logging mode has been automatically turned on.</html>", "Startup Error", JOptionPane.WARNING_MESSAGE);
			}
		}
		try {
			new ModManagerWindow(isUpdate);
		} catch (Throwable e) {
			ModManager.debugLogger.writeErrorWithException("Uncaught throwable during runtime:", e);
			JOptionPane.showMessageDialog(null, "Mod Manager had an uncaught exception during runtime:\n" + e.getMessage()
					+ "\nThis error has been logged if logging was on.\nPlease report this to FemShep.");
		}
	}

	private static void deferred() throws Exception {
		//put code here and deferred() in main to pre-execute testing values
	}

	/**
	 * Moves folders to data/ and mods/ from configurations prior to Build 40
	 */
	private static void doFileSystemUpdate() {
		// check classic folders (same as me3cmm.exe)
		// move to new mods/ directory
		File modsDir = new File(ModManager.getModsDir());
		if (!modsDir.exists()) {
			modsDir.mkdirs();
		}

		ModManager.debugLogger.writeMessage("==Looking for mods in running directory, will move valid ones to mods/==");
		ModList modList = getMods(System.getProperty("user.dir"));
		ArrayList<Mod> modsToMove = modList.getValidMods();
		for (Mod mod : modsToMove) {
			try {
				FileUtils.moveDirectory(new File(mod.getModPath()), new File(ModManager.getModsDir() + mod.getModName()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE MOD TO mods/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}

		// Move ME3Explorer
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

		// Move TankMaster Compiler
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
		// Move TankMaster TLK
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

		// move update folder
		ModManager.debugLogger.writeMessage("Checking if using old update dir");
		File oldupdatedir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "update/");
		if (oldupdatedir.exists()) {
			ModManager.debugLogger.writeMessage("Moving update to data/");
			try {
				FileUtils.moveDirectory(oldupdatedir, new File(ModManager.getToolsDir()));
			} catch (IOException e) {
				ModManager.debugLogger
						.writeMessage("FAILED TO MOVE update TO data/ DIRECTORY! Deleting the update/ folder instead (will auto download the new 7za)");
				ModManager.debugLogger.writeException(e);
				FileUtils.deleteQuietly(oldupdatedir);
			}
		}

		// move databases folder
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

		// move coalesced.original folder
		ModManager.debugLogger.writeMessage("Checking if should move coalesced.original");
		File coalOrig = new File("Coalesced.original");
		if (coalOrig.exists()) {
			ModManager.debugLogger.writeMessage("Moving Coalesced.original to data/");
			try {
				FileUtils.moveFile(coalOrig, new File(ModManager.getDataDir() + "Coalesced.original"));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE Coalesced.original TO data/ DIRECTORY!");
				ModManager.debugLogger.writeException(e);
			}
		}

		// cleanup
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

	public static ModList getModsFromDirectory() {
		ModManager.debugLogger.writeMessage("==Getting list of mods in mods directory==");
		File modsDir = new File(ModManager.getModsDir());
		ModList mods = getMods(modsDir.getAbsolutePath());
		Collections.sort(mods.getValidMods());
		Collections.sort(mods.getInvalidMods());
		return mods;
	}

	/**
	 * Gets valid mods from the given directory by looking for subfolders with
	 * moddesc.ini files
	 * 
	 * @return
	 */
	private static ModList getMods(String path) {
		File modsDir = new File(path);
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] subdirs = modsDir.listFiles(fileFilter);
		ArrayList<Mod> availableMods = new ArrayList<Mod>();
		ArrayList<Mod> failedMods = new ArrayList<Mod>();

		if (subdirs != null && subdirs.length > 0) {
			// Got a list of subdirs. Now loop them to find all moddesc.ini
			// files
			for (int i = 0; i < subdirs.length; i++) {
				File searchSubDirDesc = new File(ModManager.appendSlash(subdirs[i].toString()) + "moddesc.ini");
				System.out.println("Searching for file: " + searchSubDirDesc);
				if (searchSubDirDesc.exists()) {
					Mod validatingMod = new Mod(ModManager.appendSlash(subdirs[i].getAbsolutePath()) + "moddesc.ini");
					if (validatingMod.isValidMod()) {
						availableMods.add(validatingMod);
					} else {
						failedMods.add(validatingMod);
					}
				}
			}
		}

		return new ModList(availableMods, failedMods);
	}

	/*
	 * public static ArrayList<Mod> getCMM3ModsFromDirectory() { File fileDir =
	 * new File(getModsDir()); // This filter only returns directories
	 * FileFilter fileFilter = new FileFilter() { public boolean accept(File
	 * file) { return file.isDirectory(); } }; File[] subdirs =
	 * fileDir.listFiles(fileFilter);
	 * 
	 * //Got a list of subdirs. Now loop them to find all moddesc.ini files
	 * ArrayList<Mod> availableMod = new ArrayList<Mod>(); for (int i = 0; i <
	 * subdirs.length; i++) { File searchSubDirDesc = new
	 * File(ModManager.appendSlash(subdirs[i].toString()) + "moddesc.ini"); if
	 * (searchSubDirDesc.exists()) { Mod validatingMod = new
	 * Mod(ModManager.appendSlash(subdirs[i].getAbsolutePath()) +
	 * "moddesc.ini"); if (validatingMod.isValidMod() && validatingMod.modCMMVer
	 * >= 3) { availableMod.add(validatingMod); } } }
	 * 
	 * 
	 * for (Mod i:availableMod){
	 * ModManagerWindow.listDescriptors.put(i.getModName(),i); }
	 * 
	 * return availableMod; }
	 */

	/**
	 * Checks for a file called Coalesced.original. If it exists, it will exit
	 * this method, otherwise it will backup the current Coalesced and check
	 * it's MD5 again the known original Coalesced.
	 * 
	 */
	/*
	 * public static boolean checkDoOriginal(String origDir) { String
	 * patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e"; // This // is
	 * // Patch // 3 // Coalesced's // hash File cOriginal = new
	 * File(ModManager.getDataDir() + "Coalesced.original"); if
	 * (cOriginal.exists() == false) { // Attempt to copy an original try {
	 * String coalDirHash =
	 * MD5Checksum.getMD5Checksum(ModManager.appendSlash(origDir) +
	 * "CookedPCConsole\\Coalesced.bin");
	 * ModManager.debugLogger.writeMessage("Patch 3 Coalesced Original Hash: " +
	 * coalDirHash);
	 * ModManager.debugLogger.writeMessage("Current Patch 3 Coalesced Hash: " +
	 * patch3CoalescedHash);
	 * 
	 * if (!coalDirHash.equals(patch3CoalescedHash)) { String[] YesNo = { "Yes",
	 * "No" }; int keepInstalling = JOptionPane .showOptionDialog( null,
	 * "There is no backup of your original Coalesced yet.\nThe hash of the Coalesced in the directory you specified does not match the known hash for Patch 3's Coalesced.bin.\nYour Coalesced.bin's hash: "
	 * + coalDirHash + "\nPatch 3 Coalesced.bin's hash: " + patch3CoalescedHash
	 * +
	 * "\nYou can continue, but you might lose access to your original Coalesced.\nYou can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq if you need to restore your original.\nContinue installing this mod? "
	 * , "Coalesced Backup Error", JOptionPane.YES_NO_OPTION,
	 * JOptionPane.WARNING_MESSAGE, null, YesNo, YesNo[1]); if (keepInstalling
	 * == 0) return true; return false; } else { // Make a backup of it String
	 * destFile = ModManager.getDataDir() + "Coalesced.original"; String
	 * sourceFile = ModManager.appendSlash(origDir) + "Coalesced.bin"; String[]
	 * command = { "cmd.exe", "/c", "copy", "/Y", sourceFile, destFile }; try {
	 * Process p = Runtime.getRuntime().exec(command);
	 * 
	 * // The InputStream we get from the Process reads from // the standard
	 * output // of the process (and also the standard error, by // virtue of
	 * the line // copyFiles.redirectErrorStream(true) ). BufferedReader reader
	 * = new BufferedReader(new InputStreamReader(p.getInputStream())); String
	 * line; do { line = reader.readLine(); if (line != null) {
	 * ModManager.debugLogger.writeMessage(line); } } while (line != null);
	 * reader.close();
	 * 
	 * p.waitFor(); } catch (IOException e) { ModManager.debugLogger
	 * .writeMessage(
	 * "Error backing up the original Coalesced. Hash matched but we had an I/O exception. Aborting install."
	 * ); ModManager.debugLogger.writeMessage(e.getMessage()); return false; }
	 * catch (InterruptedException e) { ModManager.debugLogger.writeMessage(
	 * "Backup of the original Coalesced was interupted. Aborting install.");
	 * ModManager.debugLogger.writeMessage(e.getMessage()); return false; }
	 * return true; } } catch (Exception e) {
	 * ModManager.debugLogger.writeMessage
	 * ("Error occured while attempting to backup or hash the original Coalesced."
	 * ); ModManager.debugLogger.writeMessage(e.getMessage()); return false; } }
	 * // Backup exists return true; }
	 */

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
			stream = ModManager.class.getResourceAsStream(resourceName);// note
																		// that
																		// each
																		// / is
																		// a
																		// directory
																		// down
																		// in
																		// the
																		// "jar
																		// tree"
																		// been
																		// the
																		// jar
																		// the
																		// root
																		// of
																		// the
																		// tree
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			jarFolder = new File(ModManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()
					.replace('\\', '/');
			// resStreamOut = new FileOutputStream(jarFolder + resourceName);
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
		ModManager.debugLogger.writeMessage("Using binary win32 folder: " + launcherWV.getAbsolutePath());

		// File bink32_orig = new
		// File(gamedir.toString()+"\\Binaries\\Win32\\binkw32_orig.dll");

		// File bink32 = new File("dlcpatcher/binkw32.dll");

		try {
			ModManager.ExportResource("/Launcher_WV.exe", launcherWV.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			if (isAdmin()) {
				JOptionPane.showMessageDialog(null, "An error occured extracting Launcher_WV.exe out of ME3CMM.exe.\nPlease report this to FemShep.",
						"Launcher_WV.exe error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null,
						"An error occured extracting Launcher_WV.exe out of ME3CMM.exe.\nYou may need to run ME3CMM.exe as an administrator.",
						"Launcher_WV.exe error", JOptionPane.ERROR_MESSAGE);
			}
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			return false;
		}

		return true;
	}

	public static boolean installBinkw32Bypass(String biogamedir) {
		ModManager.debugLogger.writeMessage("Installing binkw32.dll DLC authorizer.");

		//Check to make sure ME3 1.05
		File executable = new File(new File(biogamedir).getParent() + "\\Binaries\\Win32\\MassEffect3.exe");
		if (!executable.exists()) {
			ModManager.debugLogger.writeError("Unable to find game EXE at " + executable);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Unable to detect game executable version.\nInstall aborted.",
					"Mass Effect 3 1.06 detected", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		int minorBuildNum = EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());

		if (minorBuildNum != 5) {
			ModManager.debugLogger.writeError("Binkw32 does not work with 1.06 version of ME3, aborting.");
			JOptionPane
					.showMessageDialog(
							ModManagerWindow.ACTIVE_WINDOW,
							"The included binkw32.dll file does not support Mass Effect 3 1.06.\nDowngrade to Mass Effect 3 1.05 to use it, or continue using LauncherWV through Mod Manager.\nThe ME3Tweaks forums has instructions on how to do this.",
							"Mass Effect 3 1.06 detected", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// extract and install binkw32.dll
		// from
		// http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
		// ClassLoader cl = ModManager.class.getClassLoader();

		File bgdir = new File(biogamedir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");

		// File bink32 = new File("dlcpatcher/binkw32.dll");
		/*
		 * if (bink32.exists()) { // if we got here binkw32.dll should have
		 * failed the hash check Path source = Paths.get(bink32.toString());
		 * Path destination = Paths.get(bink32_orig.toString()); // create
		 * backup of original try { Files.copy(source, destination,
		 * StandardCopyOption.REPLACE_EXISTING); } catch (IOException ex) {
		 * ex.printStackTrace(); return false; } }
		 */
		try {
			ModManager.ExportResource("/binkw23.dll", bink32_orig.toString());
			ModManager.ExportResource("/binkw32.dll", bink32.toString());
		} catch (Exception e1) {
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			if (isAdmin()) {
				JOptionPane.showMessageDialog(null, "An error occured extracting binkw32.dll out of ME3CMM.exe.\nPlease report this to FemShep.",
						"binkw32.dll error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null,
						"An error occured extracting binkw32.dll out of ME3CMM.exe.\nYou may need to run ME3CMM.exe as an administrator.",
						"binkw32.dll error", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}

		return true;
	}

	public static boolean uninstallBinkw32Bypass(String biogamedir) {
		ModManager.debugLogger.writeMessage("Uninstalling binkw32.dll DLC authorizer. Will restore original from binkw23.dll.");
		// extract and install binkw32.dll
		// from
		// http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
		// ClassLoader cl = ModManager.class.getClassLoader();

		File bgdir = new File(biogamedir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");

		// File bink32 = new File("dlcpatcher/binkw32.dll");
		if (bink32_orig.exists()) {
			// safe binkw32 exists. copy it over the roiginal.
			Path source = Paths.get(bink32_orig.toString());
			Path destination = Paths.get(bink32.toString());
			// create backup of original
			try {
				Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
				bink32_orig.delete();
				return true;
			} catch (IOException ex) {
				ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(ex));
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
		if (string == null)
			return null;
		if (string.charAt(string.length() - 1) == File.separatorChar) {
			return string;
		} else {
			return string + File.separator;
		}
	}

	/**
	 * Convert a millisecond duration to a string format
	 * 
	 * @param millis
	 *            A duration to convert to a string form
	 * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
	 */
	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
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

		return (sb.toString());
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

	public static String getGUITransplanterDir() {
		return getDataDir() + "guitransplanter/";
	}

	public static String getGUITransplanterCLI() {
		return "E:\\Documents\\GitHubVisualStudio\\ME3-GUI-Transplanter\\ME3 GUI Transplanter\\Build\\Release\\Transplanter-CLI.exe";
		//return getGUITransplanterDir() + "Transplanter-CLI.exe";
	}

	public static String getTankMasterCompilerDir() {
		File file = new File(getDataDir() + "tankmaster_coalesce/");
		// file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static String getTankMasterTLKDir() {
		File file = new File(getDataDir() + "tankmaster_tlk/");
		// file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static String getToolsDir() {
		File file = new File(getDataDir() + "tools/");
		// file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static String getHelpDir() {
		File file = new File(getDataDir() + "help/");
		file.mkdirs();
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
		File me3expdir = new File(getDataDir() + "ME3Explorer/");
		if (!me3expdir.exists() && showDialog) {
			JOptionPane.showMessageDialog(null,
					"Unable to find ME3Explorer in the data directory.\nME3Explorer is required for Mod Manager to work properly.",
					"ME3Explorer Error", JOptionPane.ERROR_MESSAGE);
			return "";
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

	public static String getPatchesDir() {
		File file = new File(getDataDir() + "mixinlibrary/");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Returns if the specified coalesced (job) is in the pristine folder and
	 * the hash matches the known good value for it
	 * 
	 * @param name
	 *            value to use to see if has pristine coalesced
	 * @param mode
	 *            mode to parse name as
	 * @return true if pristine, false if doesn't exist (or otherwise)
	 */
	public static boolean hasPristineCoalesced(String name, int mode) {
		File coal = new File(getPristineCoalesced(name, mode));
		if (!coal.exists()) {
			return false;
		}
		// check hash
		try {
			String hash = MD5Checksum.getMD5Checksum(coal.getAbsolutePath());
			HashMap<String, String> coalHashes = ME3TweaksUtils.getCoalHashesMap();
			// convert to header so we can check MODTYPE in hashmap
			String key = "error";
			switch (mode) {
			case ME3TweaksUtils.FILENAME:
				key = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
				break;
			case ME3TweaksUtils.HEADER:
				key = name;
				break;
			case ME3TweaksUtils.INTERNAL:
				key = ME3TweaksUtils.internalNameToHeaderName(name);
				break;
			}

			if (hash.equals(coalHashes.get(key))) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeError("ERROR GENERATING HASH FOR PRISTING COALESCED: " + coal.getAbsolutePath());
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
	 * 
	 * @param mode
	 *            ME3TweaksUtils mode indicating what the name variable is
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
			// do nothing
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
	 * 
	 * @param mode
	 *            ME3TweaksUtils mode indicating what the name variable is
	 * 
	 * @param mode
	 *            Mode indicating what name is as a constant type
	 * @return
	 */
	public static String getPristineTOC(String name, int mode) {
		switch (mode) {
		case ME3TweaksUtils.FILENAME:
			name = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
			break;
		case ME3TweaksUtils.HEADER:
			// do nothing
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
	 * @param name
	 *            value to use to see if has pristine coalesced
	 * @param mode
	 *            mode to parse name as
	 * @return true if pristine, false if doesn't exist (or otherwise)
	 */
	public static boolean hasPristineTOC(String name, int mode) {
		File toc = new File(getPristineTOC(name, mode));
		if (!toc.exists()) {
			return false;
		}

		// check hash
		try {
			String hash = MD5Checksum.getMD5Checksum(toc.getAbsolutePath());
			HashMap<String, String> tocHashes = ME3TweaksUtils.getTOCHashesMap();
			// convert to header so we can check MODTYPE in hashmap
			String key = "error";
			switch (mode) {
			case ME3TweaksUtils.FILENAME:
				key = ME3TweaksUtils.internalNameToHeaderName(ME3TweaksUtils.coalFilenameToInternalName(name));
				break;
			case ME3TweaksUtils.HEADER:
				key = name;
				break;
			case ME3TweaksUtils.INTERNAL:
				key = ME3TweaksUtils.internalNameToHeaderName(name);
				break;
			}

			if (hash.equals(tocHashes.get(key))) {
				return true;
			} else {
				ModManager.debugLogger.writeError(key + " TOC in pristine directory has failed hash check: " + hash + " vs known good value: "
						+ tocHashes.get(key));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeError("ERROR GENERATING HASH FOR PRISTING TOC: " + toc.getAbsolutePath());
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
		// file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static boolean hasPristinePatchSource(String targetPath, String targetModule) {
		File file = new File(getPatchesDir() + "source/" + targetModule + File.separator + targetPath);
		ModManager.debugLogger.writeMessage("Checking for library patch source: " + file.getAbsolutePath() + ", exists? " + file.exists());
		if (!file.exists()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Tries to find a resource for a target path inside of a target module.
	 * Returns path to the found item or null if none could be found.
	 * 
	 * @param targetPath
	 * @param targetModule
	 * @return
	 */
	public static String getPatchSource(String targetPath, String targetModule) {
		ModManager.debugLogger.writeMessage("Looking for patch source: " + targetPath + " in module " + targetModule);
		File sourceDestination = new File(getPatchesDir() + "source/" + ME3TweaksUtils.headerNameToInternalName(targetModule) + File.separator
				+ targetPath);
		String bioGameDir = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		if (sourceDestination.exists()) {
			ModManager.debugLogger.writeMessage("Patch source is already in library.");
			return sourceDestination.getAbsolutePath();
		} else {
			ModManager.debugLogger.writeMessage("Patch source is not in library (would be at: " + sourceDestination.getAbsolutePath()
					+ "), fetching from original location.");
		}
		if (targetModule.equals(ModType.BASEGAME)) {
			// we must use PCCEditor2 to decompress the file using the
			// -decompresspcc command line arg
			//get source directory via relative path chaining
			File sourceSource = new File(ModManager.appendSlash(new File(bioGameDir).getParent()) + targetPath);
			sourceDestination.getParentFile().mkdirs();

			// run ME3EXPLORER --decompress-pcc
			ProcessResult pr = ModManager.decompressPCC(sourceSource, sourceDestination);
			ModManager.debugLogger.writeMessage("File decompressed to location, and ready? : " + sourceDestination.exists());
			return sourceDestination.getAbsolutePath();
			// END OF
			// BASEGAME======================================================
		} else if (targetModule.equals(ModType.CUSTOMDLC)) {
			System.err.println("CUSTOMDLC IS NOT SUPPORTED RIGHT NOW");
			return null;
		} else {
			// DLC===============================================================
			// Check if its unpacked
			String gamedir = appendSlash(new File(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText()).getParent());
			File unpackedFile = new File(gamedir + targetPath);
			if (unpackedFile.exists()) {
				try {
					FileUtils.copyFile(unpackedFile, sourceDestination);
					ModManager.debugLogger.writeMessage("Copied unpacked file into patch library");
					return sourceDestination.getAbsolutePath();
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Unable to copy unpacked file into patch source library:", e);
					return null;
				}
			}

			// use the sfar
			// get .sfar path
			String sfarName = "Default.sfar";
			if (targetModule.equals(ModType.TESTPATCH)) {
				sfarName = "Patch_001.sfar";
			}
			String sfarPath = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText())
					+ ModManager.appendSlash(ModType.getDLCPath(targetModule)) + sfarName;

			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
			commandBuilder.add("-dlcextract");
			commandBuilder.add(sfarPath);
			commandBuilder.add(targetPath);
			commandBuilder.add(sourceDestination.getAbsolutePath());
			StringBuilder sb = new StringBuilder();
			for (String arg : commandBuilder) {
				if (arg.contains(" ")) {
					sb.append("\"" + arg + "\" ");
				} else {
					sb.append(arg + " ");
				}
			}
			sourceDestination.getParentFile().mkdirs();
			ModManager.debugLogger.writeMessage("Executing ME3EXPLORER DLCEditor2 Extraction command: " + sb.toString());

			ProcessBuilder extractionProcessBuilder = new ProcessBuilder(commandBuilder);
			// patchProcessBuilder.redirectErrorStream(true);
			// patchProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process extractionProcess;
			try {
				extractionProcess = extractionProcessBuilder.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(extractionProcess.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					System.out.println(line);
				int result = extractionProcess.waitFor();
				ModManager.debugLogger.writeMessage("ME3Explorer process finished, return code: " + result);
				return sourceDestination.getAbsolutePath();
			} catch (IOException e) {
				ModManager.debugLogger.writeException(e);
			} catch (InterruptedException e) {
				ModManager.debugLogger.writeException(e);
			}
		}
		return null;
	}

	/**
	 * Decompresses the PCC to the listed destination, location can be the same.
	 * 
	 * @param sourceSource
	 * @param sourceDestination
	 * @return
	 */
	public static ProcessResult decompressPCC(File sourceSource, File sourceDestination) {
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
		commandBuilder.add("-decompresspcc");
		commandBuilder.add(sourceSource.getAbsolutePath());
		commandBuilder.add(sourceDestination.getAbsolutePath());
		ProcessBuilder decompressProcessBuilder = new ProcessBuilder(commandBuilder);
		return ModManager.runProcess(decompressProcessBuilder);
	}

	/**
	 * Compresses the listed PCC to the listed destination, both can be the
	 * same.
	 * 
	 * @param sourceSource
	 * @param sourceDestination
	 * @return
	 */
	public static ProcessResult compressPCC(File sourceSource, File sourceDestination) {
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
		commandBuilder.add("-compresspcc");
		commandBuilder.add(sourceSource.getAbsolutePath());
		commandBuilder.add(sourceDestination.getAbsolutePath());
		ProcessBuilder decompressProcessBuilder = new ProcessBuilder(commandBuilder);
		return ModManager.runProcess(decompressProcessBuilder);
	}

	/**
	 * Copies a file from the game to the specified location. Decompresses a
	 * basegame PCC if one is specified.
	 * 
	 * @param targetPath
	 *            Path inside of module
	 * @param targetModule
	 *            Module to pull file from
	 * @param copyToLocation
	 *            Location to put copy of file
	 * @return null if could not get file, otherwise copyToLocation.
	 */
	public static String getGameFile(String targetPath, String targetModule, String copyToLocation) {
		ModManager.debugLogger.writeMessage("Getting game file (will use unpacked if possible) from " + targetModule + ", with relative path "
				+ targetPath);
		String bioGameDir = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		File destFile = new File(copyToLocation);
		FileUtils.deleteQuietly(destFile);
		new File(destFile.getParent()).mkdirs();

		if (targetModule.equals(ModType.BASEGAME)) {
			if (targetPath.endsWith(".pcc")) {
				// we must use PCCEditor2 to decompress the file using the
				// --decompress-pcc command line arg, and specify where it will be decompressed to
				//get source directory via relative path chaining
				File sourceSource = new File(ModManager.appendSlash(new File(bioGameDir).getParent()) + targetPath);
				// run ME3EXPLORER --decompress-pcc
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
				commandBuilder.add("-decompresspcc");
				commandBuilder.add(sourceSource.getAbsolutePath());
				commandBuilder.add(copyToLocation);
				StringBuilder sb = new StringBuilder();
				for (String arg : commandBuilder) {
					sb.append("\"" + arg + "\" ");
				}
				ModManager.debugLogger.writeMessage("Executing ME3EXPLORER Decompressor command (into library): " + sb.toString());

				ProcessBuilder decompressProcessBuilder = new ProcessBuilder(commandBuilder);
				// patchProcessBuilder.redirectErrorStream(true);
				// patchProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				Process decompressProcess;
				try {
					ModManager.debugLogger.writeMessage("===ME3EXPLORER PCC REPACKER===");
					decompressProcess = decompressProcessBuilder.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(decompressProcess.getInputStream()));
					String line;
					while ((line = reader.readLine()) != null)
						ModManager.debugLogger.writeMessage(line);
					int result = decompressProcess.waitFor();
					ModManager.debugLogger.writeMessage("===END OF PCC REPACKER===");

					ModManager.debugLogger.writeMessage("ME3Explorer process finished, return code: " + result);
				} catch (IOException e) {
					ModManager.debugLogger.writeException(e);
					return null;
				} catch (InterruptedException e) {
					ModManager.debugLogger.writeException(e);
					return null;
				}
				return copyToLocation;
			} else {
				//not a pcc file.
				String gamedir = ModManager.appendSlash(new File(bioGameDir).getParent());
				String fileToGetPath = gamedir + targetPath;
				File fileToGet = new File(fileToGetPath);
				ModManager.debugLogger.writeMessage("Getting game file: " + fileToGet + ", exists? " + fileToGet.exists());
				if (fileToGet.exists()) {
					try {
						ModManager.debugLogger.writeMessage("Copying to destination: " + copyToLocation);
						FileUtils.copyFile(fileToGet, destFile);
						ModManager.debugLogger.writeMessage("Copied to destination.");
						return copyToLocation;
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Unable to get game file from basegame:", e);
						return null;
					}
				} else {
					ModManager.debugLogger.writeError("File to get from basegame does not exist.");
					return null;
				}
			}
			// END OF
			// BASEGAME======================================================
		} else if (targetModule.equals(ModType.CUSTOMDLC)) {
			System.err.println("CUSTOMDLC IS NOT SUPPORTED RIGHT NOW");
			return null;
		} else {
			// DLC===============================================================
			// Check if its unpacked
			String gamedir = appendSlash(new File(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText()).getParent());
			File unpackedFile = new File(gamedir + targetPath);
			if (unpackedFile.exists()) {
				//check if PCConsoleTOC, as we probably want the one in the SFAR (or this one, provided DLC is unpacked)
				/*
				 * if
				 * (unpackedFile.getAbsolutePath().endsWith("PCConsoleTOC.bin"
				 * )){
				 * 
				 * //if (inPlaceToc) }
				 */
				try {
					new File(destFile.getParent()).mkdirs();
					ModManager.debugLogger.writeMessage("Copying unpacked file to destination: " + copyToLocation);
					FileUtils.copyFile(unpackedFile, destFile);
					ModManager.debugLogger.writeMessage("Copied unpacked file to destination");
					return copyToLocation;
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Unable to copy unpacked file to destination:", e);
					return null;
				}
			}

			// use the sfar
			// get .sfar path
			String sfarName = "Default.sfar";
			if (targetModule.equals(ModType.TESTPATCH)) {
				sfarName = "Patch_001.sfar";
			}
			String sfarPath = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText())
					+ ModManager.appendSlash(ModType.getDLCPath(targetModule)) + sfarName;

			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
			commandBuilder.add("-dlcextract");
			commandBuilder.add(sfarPath);
			commandBuilder.add(targetPath);
			commandBuilder.add(copyToLocation);
			StringBuilder sb = new StringBuilder();
			for (String arg : commandBuilder) {
				if (arg.contains(" ")) {
					sb.append("\"" + arg + "\" ");
				} else {
					sb.append(arg + " ");
				}
			}
			ModManager.debugLogger.writeMessage("Executing ME3EXPLORER DLCEditor2 Extraction command: " + sb.toString());

			ProcessBuilder extractionProcessBuilder = new ProcessBuilder(commandBuilder);
			// patchProcessBuilder.redirectErrorStream(true);
			// patchProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process extractionProcess;
			try {
				extractionProcess = extractionProcessBuilder.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(extractionProcess.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					System.out.println(line);
				int result = extractionProcess.waitFor();
				ModManager.debugLogger.writeMessage("ME3Explorer process finished, return code: " + result);
				return copyToLocation;
			} catch (IOException e) {
				ModManager.debugLogger.writeException(e);
			} catch (InterruptedException e) {
				ModManager.debugLogger.writeException(e);
			}
		}
		return null;
	}

	/**
	 * Loads patch objects from the patchlibrary/patches directory
	 * 
	 * @return
	 */
	public static ArrayList<Patch> getPatchesFromDirectory() {
		ModManager.debugLogger.writeMessage("Loading Patches from patchlibrary");
		File modsDir = new File(getPatchesDir() + "patches/");
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] subdirs = modsDir.listFiles(fileFilter);
		ArrayList<Patch> validPatches = new ArrayList<Patch>();

		if (subdirs != null && subdirs.length > 0) {
			// Got a list of subdirs. Now loop them to find all moddesc.ini
			// files
			for (int i = 0; i < subdirs.length; i++) {
				File searchSubDirDesc = new File(ModManager.appendSlash(subdirs[i].toString()) + "patchdesc.ini");
				// System.out.println("Searching for file: " +
				// searchSubDirDesc);
				if (searchSubDirDesc.exists()) {
					Patch validatingPatch = new Patch(searchSubDirDesc.getAbsolutePath());
					if (validatingPatch.isValid()) {
						validPatches.add(validatingPatch);
					}
				}
			}
		}
		Collections.sort(validPatches);
		return validPatches;
	}

	/**
	 * Checks for the binkw32 bypass.
	 * 
	 * @return true if bink23 exists and bink32 hash fails, false otherwise
	 */
	public static boolean checkIfBinkBypassIsInstalled(String biogameDir) {
		File bgdir = new File(biogameDir);
		if (!bgdir.exists()) {
			return false;
		}
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Game directory: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink23 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");
		try {
			String originalBink32MD5 = "128b560ef70e8085c507368da6f26fe6";
			String binkhash = MD5Checksum.getMD5Checksum(bink32.toString());
			if (!binkhash.equals(originalBink32MD5) && bink23.exists()) {
				return true;
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Exception while attempting to find DLC bypass (Binkw32).", e);
		}
		return false;
	}

	public static boolean hasKnownDLCBypass(String biogameDir) {
		try {
			String originalBink32MD5 = "128b560ef70e8085c507368da6f26fe6";

			File bgdir = new File(biogameDir);
			File gamedir = bgdir.getParentFile();
			ModManager.debugLogger.writeMessage("Game directory: " + gamedir.toString());
			File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
			File bink23 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");
			try {
				String binkhash = MD5Checksum.getMD5Checksum(bink32.toString());
				if (!binkhash.equals(originalBink32MD5) && bink23.exists()) {
					ModManager.debugLogger.writeMessage("Binkw32 DLC bypass probably installed (hash is wrong and bink23 exists)");
					return true;
				} else {

					// OLD CODE
					// if (binkhash.equals(wvdlcBink32MD5) ||
					// binkhash.equals(wvdlcBink32MD5_2)) {
					// ModManager.debugLogger.writeMessage("Binkw32 DLC bypass
					// installed");
					// return true;
					// } else {
					// Check for LauncherWV.
					File Launcher_WV = new File(gamedir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
					File LauncherWV = new File(gamedir.toString() + "\\Binaries\\Win32\\LauncherWV.exe");
					if (Launcher_WV.exists() || LauncherWV.exists()) {
						// does exist
						ModManager.debugLogger.writeMessage("Launcher WV DLC bypass installed");
						return true;
					} else {
						// No DLC Bypass installed
						ModManager.debugLogger.writeMessage("Binkw32.dll's hash indicates it is the original, binkw32 bypass not installed.");
						ModManager.debugLogger.writeMessage("LauncherWV was not found in Win32 as Launcher_WV or LauncherWV.");
						ModManager.debugLogger.writeMessage("DLC bypass is not installed.");
						return false; // we will install launcherwv
					}
				}
			} catch (Exception e) {
				ModManager.debugLogger.writeMessage("Exception attempting to verify binkw32.dll");
				ModManager.debugLogger.writeException(e);
				return false;
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Exception checking for known DLC bypass:", e);
			return false;
		}
	}

	private static String getSystemInfo() {
		StringBuilder sb = new StringBuilder();
		ModManager.debugLogger.writeMessage("----Java System Properties----");
		Properties props = System.getProperties();
		Enumeration e = props.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			ModManager.debugLogger.writeMessage(key + " = " + props.getProperty(key));
		}

		ModManager.debugLogger.writeMessage("----System Environment Variables----");
		Map<String, String> env = System.getenv();
		Set<String> keys = env.keySet();
		for (String key : keys) {
			ModManager.debugLogger.writeMessage(key + "=" + env.get(key));
		}
		return sb.toString();
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static String getPatchLibraryDir() {
		return getPatchesDir() + "patches/";
	}

	/**
	 * Attemps to read what version of .NET framework is installed and if the
	 * release version meets the minimum ME3Explorer requirements
	 * 
	 * @return true if satisfied, false otherwise
	 */
	public static boolean validateNETFrameworkIsInstalled() {
		if (!PERFORM_DOT_NET_CHECK) {
			NET_FRAMEWORK_IS_INSTALLED = true;
			return true;
		}

		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			int releaseNum = 0;
			String netFrameWork4Key = "SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full";
			ModManager.debugLogger.writeMessage("Checking for .NET Framework 4.5 or higher registry key");
			try {
				releaseNum = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, netFrameWork4Key, "Release");
				ModManager.debugLogger.writeMessage(".NET Framework release detected: " + releaseNum);
				if (releaseNum >= MIN_REQUIRED_NET_FRAMEWORK_RELNUM) {
					ModManager.debugLogger.writeMessage("This version (" + releaseNum + ") satisfies the current requirements ("
							+ MIN_REQUIRED_NET_FRAMEWORK_RELNUM + ")");
					NET_FRAMEWORK_IS_INSTALLED = true;
					return true;
				} else {
					ModManager.debugLogger.writeError("This version (" + releaseNum + ") DOES NOT satisfy the current requirements ("
							+ MIN_REQUIRED_NET_FRAMEWORK_RELNUM + ")");
					NET_FRAMEWORK_IS_INSTALLED = false;
					return false;
				}
			} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
				ModManager.debugLogger.writeError(".NET Framework 4.5 registry key was not found: " + netFrameWork4Key);
				NET_FRAMEWORK_IS_INSTALLED = false;
				return false;
			} catch (Throwable e) {
				ModManager.debugLogger.writeErrorWithException(".NET Framework 4.5 detection exception:", e);
				NET_FRAMEWORK_IS_INSTALLED = false;
				return false;
			}
		}
		ModManager.debugLogger.writeError("This is not a windows OS. So obviously there is no registry. .NET is not installed");
		NET_FRAMEWORK_IS_INSTALLED = false;
		return false;
	}

	public static boolean isAdmin() {
		Preferences prefs = Preferences.systemRoot();
		PrintStream systemErr = System.err;
		synchronized (systemErr) { // better synchroize to avoid problems with other threads that access System.err
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int i) throws IOException {
				}
			}));
			try {
				prefs.put("me3cmm", "test"); // SecurityException on Windows
				prefs.remove("me3cmm");
				prefs.flush(); // BackingStoreException on Linux
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				System.setErr(systemErr);
			}
		}
	}

	/**
	 * Runs a process already build via processbuilder, prints timing info and
	 * returns the result
	 * 
	 * @param p
	 *            Process to build and run
	 * @return ProcessResult, with code if successful, or exception as not-null
	 *         if one occured
	 */
	public static ProcessResult runProcess(ProcessBuilder p) {
		try {
			StringBuilder sb = new StringBuilder();
			List<String> list = p.command();
			for (String arg : list) {
				sb.append(arg);
				sb.append(" ");
			}
			ModManager.debugLogger.writeMessage("runProcess(): " + sb.toString());
			long startTime = System.currentTimeMillis();
			Process process = p.start();
			//handle stdout
			final StringWriter writer = new StringWriter();
			new Thread(new Runnable() {
				public void run() {
					try {
						IOUtils.copy(process.getInputStream(), writer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			int returncode = process.waitFor();
			long endTime = System.currentTimeMillis();
			ModManager.debugLogger.writeMessage("Process finished with code " + returncode + ", took " + (endTime - startTime) + " ms.");
			return new ProcessResult(returncode, null);
		} catch (IOException | InterruptedException e) {
			ModManager.debugLogger.writeErrorWithException("Process exception occured:", e);
			return new ProcessResult(0, e);
		}
	}

	public static File getHelpFile() {
		return new File(getHelpDir() + "localhelp.xml");
	}

	/**
	 * Gets the GUI Transplant Directory (Transplanter-CLI, Transplanter-GUI)
	 * 
	 * @return
	 */
	public static String getTransplantDir() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void loadLogger() {
		debugLogger = new DebugLogger();
	}

	/**
	 * Gets information about the game and puts it into a string
	 * 
	 * @param biogameDir
	 * @return
	 */
	public static String getGameEnvironmentInfo(String biogameDir) {
		StringBuilder sb = new StringBuilder();
		File BIOGAMEFILE = new File(biogameDir);
		if (!BIOGAMEFILE.exists()) {
			return "INVALID BIOGAME DIRECTORY, CANNOT GET GAME INFORMATION.\n";
		}
		File GAMEDIR = BIOGAMEFILE.getParentFile();
		sb.append("============== MASS EFFECT 3 GAME INFORMATION ==================\n");
		sb.append("BIOGame Directory: " + biogameDir + "\n");
		File executable = new File(GAMEDIR.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
		int minorBuildNum = EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());

		sb.append("Executable version: 1.0" + minorBuildNum + "\n");
		sb.append("DLC Bypass installed: " + hasKnownDLCBypass(biogameDir) + "\n");
		sb.append("Preferred bypass method: "
				+ (checkIfBinkBypassIsInstalled(biogameDir) ? "Binkw32 Virtual Function Redirection" : "LauncherWV Process Thread Injection") + "\n");

		sb.append("DLC Status:\n");
		//get dlc status info
		//add testpatch
		HashMap<String, Long> sizesMap = ModType.getSizesMap();
		File testpatchSfar = new File(ModManager.appendSlash(biogameDir) + File.separator + "Patches" + File.separator + "PCConsole" + File.separator
				+ "Patch_001.sfar");
		if (testpatchSfar.exists()) {
			if (testpatchSfar.length() == sizesMap.get(ModType.TESTPATCH)) {
				sb.append("TESTPATCH: Unmodified (1.05)\n");
			} else if (testpatchSfar.length() == ModType.TESTPATCH_16_SIZE) {
				sb.append("TESTPATCH: Unmodified (1.06)\n");
			} else {
				sb.append("TESTPATCH: Modified (unable to determine version)\n");
			}
		} else {
			sb.append("TESTPATCH: Not Installed (!!)\n");
		}

		//iterate over DLC.
		File mainDlcDir = new File(ModManager.appendSlash(biogameDir) + "DLC" + File.separator);
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		HashMap<String, String> nameMap = ModType.getHeaderFolderMap();
		ArrayList<String> foundHeaders = new ArrayList<String>();
		foundHeaders.add(ModType.BASEGAME);
		foundHeaders.add(ModType.TESTPATCH);

		for (String dir : directories) {
			String dlcDirPath = ModManager.appendSlash(ModManager.appendSlash(biogameDir) + "DLC" + File.separator + dir);
			ModManager.debugLogger.writeMessage("Scanning " + dlcDirPath);
			if (ModType.isKnownDLCFolder(dir)) {
				File mainSfar = new File(dlcDirPath + "CookedPCConsole\\Default.sfar");
				if (mainSfar.exists()) {
					//find the header (the lazy way)
					String header = null;
					for (Map.Entry<String, String> entry : nameMap.entrySet()) {
						String localHeader = entry.getKey();
						String foldername = entry.getValue();
						if (FilenameUtils.getBaseName(dir).equalsIgnoreCase(foldername)) {
							header = localHeader;
							foundHeaders.add(header);
							break;
						}

					}
					assert header != null;

					if (mainSfar.length() == sizesMap.get(header)) {
						//vanilla
						sb.append(header + ": Unmodified (SFAR)\n");
						continue;
					}
					File externalTOC = new File(dlcDirPath + "PCConsoleTOC.bin");
					if (externalTOC.exists()) {
						//its unpacked
						sb.append(header + ": Unpacked\n");
						continue;
					} else {
						//its a modified SFAR
						sb.append(header + ": Modified (SFAR)\n");
						continue;
					}
				} else {
					sb.append(dir + ": Invalid DLC\n");
					continue; //not valid DLC
				}
			} else {
				//unnofficial DLC
				File externalTOC = new File(dlcDirPath + "PCConsoleTOC.bin");
				if (externalTOC.exists()) {
					sb.append(dir + ": INSTALLED AS CUSTOM DLC\n");
					continue;
				} else {
					sb.append(dir + ": No SFAR or PCConsoleTOC files present in directory\n");
				}
			}
		}
		String[] officialHeaders = ModType.getHeaderNameArray();
		for (String h : officialHeaders) {
			if (!foundHeaders.contains(h)) {
				sb.append(h + ": Not installed\n");
			}
		}

		sb.append("=========== END OF MASS EFFECT 3 GAME INFORMATION============\n");
		return sb.toString();
	}

	public static HashMap<String, String> getCustomDLCConflicts(String biogameDir) {
		try {
			//Iterate over DLC folders and find Mount.dlc files. Only DLC folders with these files will be considered.
			File mainDlcDir = new File(ModManager.appendSlash(biogameDir) + "DLC" + File.separator);
			String[] directories = mainDlcDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});

			ArrayList<String> unpackedDLCFolders = new ArrayList<String>(); //need to sort, lowest to highest priority

			for (String dir : directories) {
				File mountfile = new File(ModManager.appendSlash(ModManager.appendSlash(biogameDir) + "DLC" + File.separator + dir) + File.separator
						+ "CookedPCConsole" + File.separator + "Mount.dlc");
				if (mountfile.exists()) {
					unpackedDLCFolders.add(ModManager.appendSlash(ModManager.appendSlash(biogameDir) + "DLC" + File.separator + dir));
				}
			}

			//Enumerate all files
			HashMap<String, String> filemap = new HashMap<>();
			HashMap<String, String> conflictmap = new HashMap<>();

			for (String unpackedPath : unpackedDLCFolders) {
				Collection<File> files = FileUtils.listFiles(new File(unpackedPath), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
				for (File file : files) {
					if (!FilenameUtils.getExtension(file.getAbsolutePath()).equals("pcc")) {
						continue;
					}
					boolean keyExists = filemap.containsKey(FilenameUtils.getName(file.getAbsolutePath()));
					if (keyExists) {
						//conflict
						conflictmap.put(FilenameUtils.getName(file.getAbsolutePath()), unpackedPath);
					} else {
						filemap.put(FilenameUtils.getName(file.getAbsolutePath()), unpackedPath);
					}
				}
			}
			return conflictmap;
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error getting DLC conflict list:", e);
		}
		return new HashMap<>();
	}
}