package com.me3tweaks.modmanager;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
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
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.w3c.dom.Document;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModList;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.DebugLogger;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.me3tweaks.modmanager.utilities.Version;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.W32APIOptions;

import javafx.embed.swing.JFXPanel;

public class ModManager {
	public static boolean IS_DEBUG = false;
	public final static boolean FORCE_32BIT_MODE = false; //set to true to force it to think it is running 32-bit for (most things)

	public static final String VERSION = "5.0.7 MR1";
	public static long BUILD_NUMBER = 82L;
	public static final String BUILD_DATE = "12/27/2017";
	public static final String SETTINGS_FILENAME = "me3cmm.ini";
	public static DebugLogger debugLogger;
	public static boolean logging = false;
	public static final double MODMAKER_VERSION_SUPPORT = 2.2; // max modmaker
																// version
	public static final double MODDESC_VERSION_SUPPORT = 4.5; // max supported
																// cmmver in
																// moddesc
	public static boolean MOD_MANAGER_UPDATE_READY = false; //if true, don't delete temp
	public static boolean AUTO_APPLY_MODMAKER_MIXINS = false;
	public static boolean AUTO_UPDATE_CONTENT = true;
	public static boolean CHECKED_FOR_UPDATE_THIS_SESSION = false;
	public static long LAST_AUTOUPDATE_CHECK;
	public static final int MIN_REQUIRED_CMDLINE_MAIN = 1;
	public static final int MIN_REQUIRED_CMDLINE_MINOR = 0;
	public final static int MIN_REQUIRED_CMDLINE_BUILD = 0;
	public final static int MIN_REQUIRED_CMDLINE_REV = 27;

	private final static int MIN_REQUIRED_NET_FRAMEWORK_RELNUM = 379893; //4.5.2
	public static ArrayList<Image> ICONS;
	public static boolean AUTO_INJECT_KEYBINDS = false;
	public static boolean AUTO_UPDATE_MOD_MANAGER = true;
	public static boolean NET_FRAMEWORK_IS_INSTALLED = false;
	public static long SKIP_UPDATES_UNTIL_BUILD = 0;
	public static int AUTO_CHECK_INTERVAL_DAYS = 2;
	public static long AUTO_CHECK_INTERVAL_MS = TimeUnit.DAYS.toMillis(AUTO_CHECK_INTERVAL_DAYS);
	public static boolean LOG_MOD_INIT = false;
	public static boolean LOG_PATCH_INIT = false;
	public static boolean PERFORM_DOT_NET_CHECK = true;
	public static boolean MODMAKER_CONTROLLER_MOD_ADDINS = false;
	public static String THIRD_PARTY_MOD_JSON;
	public static boolean LOG_MODMAKER = false;
	public static boolean CHECK_FOR_ALOT_INSTALL = true;

	public static String COMMANDLINETOOLS_URL;
	public static String LATEST_ME3EXPLORER_URL;
	public static String LATEST_ME3EXPLORER_VERSION;
	public static boolean USE_WINDOWS_UI;
	protected static boolean COMPRESS_COMPAT_OUTPUT = false;

	public static String ALOTINSTALLER_DOWNLOADLINK;
	public static Version ALOTINSTALLER_LATESTVERSION;

	public static String TIPS_SERVICE_JSON;
	protected final static int COALESCED_MAGIC_NUMBER = 1836215654;
	public final static String[] KNOWN_GUI_CUSTOMDLC_MODS = { "DLC_CON_XBX", "DLC_CON_UIScaling", "DLC_CON_UIScaling_Shared" };
	public static final String[] SUPPORTED_GAME_LANGUAGES = { "INT", "ESN", "DEU", "ITA", "FRA", "RUS", "POL", "JPN" };
	public static ImageIcon ACTIVITY_ICON;

	public static final class Lock {
	} //threading wait() and notifyall();

	public static void main(String[] args) {
		loadLogger();
		boolean emergencyMode = false;
		boolean isUpdate = false;
		try {
			System.out.println("Starting Mod Manager " + ModManager.VERSION);
			System.out.println("Debugging mode is " + (ModManager.IS_DEBUG ? "enabled" : "disabled"));

			ICONS = new ArrayList<Image>();
			ICONS.add(Toolkit.getDefaultToolkit().getImage(ModManager.class.getResource("/resource/icon32.png")));
			ICONS.add(Toolkit.getDefaultToolkit().getImage(ModManager.class.getResource("/resource/icon64.png")));
			ICONS.add(Toolkit.getDefaultToolkit().getImage(ModManager.class.getResource("/resource/icon128.png")));
			ACTIVITY_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ModManagerWindow.class.getResource("/resource/network.gif")));

			ToolTipManager.sharedInstance().setDismissDelay(15000);

			System.setProperty("derby.system.home", new File(ModManager.getDatabaseDir()).getAbsolutePath()); //move derby.log

			File settings = new File(ModManager.SETTINGS_FILENAME);
			if (!settings.exists()) {
				settings.createNewFile();
				Wini settingsini = new Wini(new File(ModManager.SETTINGS_FILENAME));
				settingsini.put("Settings", "initialmodmanagerversionbuild", ModManager.VERSION + "-b" + ModManager.BUILD_NUMBER);
				settingsini.put("Settings", "usewindowsui", "1"); //Default to Windows UI
				settingsini.store();
			}

			try {
				Wini settingsini = ModManager.LoadSettingsINI();
				debugLogger.initialize();
				logging = true;
				debugLogger.writeMessage("Starting logger. Logger was able to start up with no issues.");
				debugLogger.writeMessage("Mod Manager version " + ModManager.VERSION + "; Build " + ModManager.BUILD_NUMBER + "; Build Date " + BUILD_DATE);
				debugLogger.writeMessage("JVM can use a maximum of " + ResourceUtils.humanReadableByteCount(Runtime.getRuntime().maxMemory(), true) + " of memory");
				if (ModManager.isUsingBundledJRE()) {
					debugLogger.writeMessage("Using bundled 64-bit JRE.");
				} else {
					debugLogger.writeMessage("Using system JRE, not the bundled version.");
				}
				debugLogger.writeMessage("--------Mod Manager Main Startup--------");

				String verString = settingsini.get("Settings", "initialmodmanagerversionbuild");
				if (verString == null || verString.equals("")) {
					settingsini.put("Settings", "initialmodmanagerversionbuild", "Before " + ModManager.VERSION + "-b" + ModManager.BUILD_NUMBER);
					settingsini.store();
					debugLogger.writeMessage("me3cmm.ini was created before " + ModManager.VERSION + "-b" + ModManager.BUILD_NUMBER);
				} else {
					debugLogger.writeMessage("me3cmm.ini was created by Mod Manager " + verString);
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

				// GUI Compatibility Pack - Compress output
				if (ResourceUtils.is64BitWindows()) {
					String compressCompatOutput = settingsini.get("Settings", "compresscompatibilitygeneratoroutput");
					int compressCompatOutputInt = 0;
					if (compressCompatOutput != null && !compressCompatOutput.equals("")) {
						try {
							compressCompatOutputInt = Integer.parseInt(compressCompatOutput);
							if (compressCompatOutputInt > 0) {
								// logging is on
								debugLogger.writeMessage("Compressing GUI Compatibility Generator output is ON");
								COMPRESS_COMPAT_OUTPUT = true;
							} else {
								debugLogger.writeMessage("Compressing GUI Compatibility Generator output is OFF");
								COMPRESS_COMPAT_OUTPUT = false;
							}
						} catch (NumberFormatException e) {
							ModManager.debugLogger.writeError("Number format exception reading the compress output compat generator check flag, defaulting to false");
						}
					}
				} else {
					debugLogger.writeMessage("32-bit Windows - forcing gui compatibility output to decompressed.");
					COMPRESS_COMPAT_OUTPUT = false;
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

				String windowsUIStr = settingsini.get("Settings", "usewindowsui");
				int windowsUIint = 0;
				if (windowsUIStr != null && !windowsUIStr.equals("")) {
					try {
						windowsUIint = Integer.parseInt(windowsUIStr);
						if (windowsUIint > 0) {
							// logging is on
							debugLogger.writeMessage("Windows UI L&F is enabled");
							ModManager.USE_WINDOWS_UI = true;
						} else {
							debugLogger.writeMessage("Using default UI");
							ModManager.USE_WINDOWS_UI = false;
						}
					} catch (NumberFormatException e) {
						ModManager.debugLogger.writeError("Number format exception reading the UI flag, defaulting to cross platform (Default)");
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
					AUTO_UPDATE_CONTENT = false;
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
						debugLogger.writeError("Number format exception reading the auto install of modmaker mixins mode - autoinstall mode disabled");
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
						ModManager.debugLogger.writeError("Number format exception reading the build number to skip to in settings. Defaulting to 0.");
						SKIP_UPDATES_UNTIL_BUILD = 0;
					}
				}

				// Controller mod fixes
				String controllerModUserStr = settingsini.get("Settings", "controllermoduser");
				int controllerModUserInt = 0;
				if (controllerModUserStr != null && !controllerModUserStr.equals("")) {
					try {
						controllerModUserInt = Integer.parseInt(controllerModUserStr);
						if (controllerModUserInt > 0) {
							// logging is on
							debugLogger.writeMessage("ModMaker Controller Mod add-ins enabled");
							MODMAKER_CONTROLLER_MOD_ADDINS = true;
						} else {
							debugLogger.writeMessage("ModMaker Controller Mod add-ins disabled");
							MODMAKER_CONTROLLER_MOD_ADDINS = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the controller mod user flag - defaulting to disabled");
						MODMAKER_CONTROLLER_MOD_ADDINS = false;
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

				// Log Mod Startup
				{
					String modmakerlogStr = settingsini.get("Settings", "logmodmaker");
					int modmakerlogStartupInt = 0;
					if (modmakerlogStr != null && !modmakerlogStr.equals("")) {
						try {
							modmakerlogStartupInt = Integer.parseInt(modmakerlogStr);
							if (modmakerlogStartupInt > 0) {
								// logging is on
								debugLogger.writeMessage("Modmaker Compiler logging is enabled");
								LOG_MODMAKER = true;
							} else {
								debugLogger.writeMessage("Modmaker Compiler logging is disabled");
								LOG_MODMAKER = false;
							}
						} catch (NumberFormatException e) {
							debugLogger.writeError("Number format exception reading the log modmaker setting - setting to disabled");
							LOG_MODMAKER = false;
						}
					}
				}

				// CHECK FOR ALOT INSTALLATION

				// Log Mod Startup
				String alotCheckLogStr = settingsini.get("Settings", "checkforalotinstall");
				int alotCheckInt = 0;
				if (alotCheckLogStr != null && !alotCheckLogStr.equals("")) {
					try {
						alotCheckInt = Integer.parseInt(alotCheckLogStr);
						if (alotCheckInt > 0) {
							// logging is on
							debugLogger.writeMessage("Checking for ALOT installation is enabled");
							CHECK_FOR_ALOT_INSTALL = true;
						} else {
							debugLogger.writeMessage("Checking for ALOT installation is disabled");
							CHECK_FOR_ALOT_INSTALL = false;
						}
					} catch (NumberFormatException e) {
						debugLogger.writeError("Number format exception reading the check for alot install - setting to enabled");
						CHECK_FOR_ALOT_INSTALL = true;
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
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Update failed! Still using Build " + ModManager.BUILD_NUMBER + ".", "Update Failed",
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

			if (args.length > 2 && args[0].equals("--jre-update-from")) {
				// This is being run as an update
				String javaJRE = System.getProperty("java.version");
				if (javaJRE.equals(args[1])) {
					if (args[2].equals("system") && !ModManager.isUsingBundledJRE()) {
						ModManager.debugLogger.writeError("JRE update failed - same version, not using bundled!");
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "JRE update (might have) failed!\nStill using " + javaJRE + ".", "JRE Update Failed",
								JOptionPane.ERROR_MESSAGE);
					} else {
						ModManager.debugLogger.writeMessage("JRE update succeeded - same version, but now using bundled");
						String message = "JRE update successful.\nMod Manager is now using a bundled JRE - it no longer needs the system one\nRunning Java " + args[1];
						JOptionPane.showMessageDialog(null, message, "JRE Update Complete", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					ModManager.debugLogger.writeMessage("JRE update succeeded - updated to " + args[1]);
					String message = "JRE update successful.\nOld Version: " + args[1] + "\nCurrent Version: " + javaJRE;
					JOptionPane.showMessageDialog(null, message, "JRE Update Complete", JOptionPane.INFORMATION_MESSAGE);
				}
			}

			if (args.length > 1 && args[0].equals("--minor-update-from")) {
				// This is being run as a minor update
				try {
					long oldbuild = Long.parseLong(args[1]);
					if (oldbuild == ModManager.BUILD_NUMBER) {
						// SOMETHING WAS WRONG!
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Minor update was applied.", "Update OK", JOptionPane.INFORMATION_MESSAGE);
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

			if (checkIfCMMPatchIsTooLong()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"Mod Manager has detected that it running from a location with a long filepath.\nMod Manager caches files using their relative game directory path.\nYou may consider moving Mod Manager higher up this file system's hierarchy\nto avoid issues with Windows path limitations.",
						"Windows Path Limitation Warning", JOptionPane.WARNING_MESSAGE);
			}

			if (ModManager.getThirdPartyModDBFile().exists()) {
				ModManager.debugLogger.writeMessage("Loading third party identification service JSON into memory");
				ModManager.THIRD_PARTY_MOD_JSON = FileUtils.readFileToString(ModManager.getThirdPartyModDBFile(), StandardCharsets.UTF_8);
			} else {
				ModManager.debugLogger.writeMessage("No third party identification service JSON found. May not have been downloaded yet...");
			}

			if (ModManager.getTipsServiceFile().exists()) {
				ModManager.debugLogger.writeMessage("Loading ME3Tweaks Tips Service JSON into memory");
				ModManager.TIPS_SERVICE_JSON = FileUtils.readFileToString(ModManager.getTipsServiceFile(), StandardCharsets.UTF_8);
			} else {
				ModManager.debugLogger.writeMessage("No tips service JSON found. May not have been downloaded yet...");
			}

			ModManager.debugLogger.writeMessage("========End of startup=========");
		} catch (

		Throwable e) {
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
				debugLogger.writeMessage("Logger starting in emergency mode. Startup failed as well as logging settings, but logger was able to initialize.");
			} else {
				debugLogger.writeMessage("Logger starting in limited mode. Startup failed but logger was able to initialize.");
			}
			debugLogger.writeMessage("Mod Manager version " + ModManager.VERSION + " Build " + ModManager.BUILD_NUMBER);
			if (emergencyMode) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"<html>An unknown error occured during Mod Manager startup:<br>" + e.getMessage() + "<br>"
								+ "Logging mode was attempted to be turned on, but failed. Logging for this session has been enabled.<br>"
								+ "Mod Manager will attempt to continue startup with limited resources and defaults.<br>"
								+ "Something is very wrong and Mod Manager will likely not function properly.</html>",
						"Critical Startup Error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"<html>An unknown error occured during Mod Manager startup:<br>" + e.getMessage() + "<br>"
								+ "Mod Manager will attempt to continue startup with limited resources and defaults.<br>" + "Logging mode has been automatically turned on.</html>",
						"Startup Error", JOptionPane.WARNING_MESSAGE);
			}
		}
		// SETUI LOOK
		try {
			if (ModManager.USE_WINDOWS_UI) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
		} catch (Exception e) {
			System.err.println("Couldn't set the UI interface style");
		}
		
		ModManager.debugLogger.writeMessage("Loading JavaFX");
		new JFXPanel(); // used for initializing javafx thread (ideally called once)
		ModManager.debugLogger.writeMessage("Loaded JavaFX");

		
		try {
			new ModManagerWindow(isUpdate);
		} catch (Throwable e) {
			ModManager.debugLogger.writeErrorWithException("Uncaught throwable during runtime:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
					"Mod Manager had an uncaught exception during runtime:\n" + e.getMessage() + "\nPlease report this to FemShep.", "Mod Manager has crashed",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Gets the path where the tips service json is located.
	 * 
	 * @return data/<me3tweaksservicescache>/tipservice.json
	 */
	public static File getTipsServiceFile() {
		return new File(getME3TweaksServicesCache() + "tipsservice.json");
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

		// move update folder
		ModManager.debugLogger.writeMessage("Checking if using old update dir");
		File oldupdatedir = new File(ModManager.appendSlash(System.getProperty("user.dir")) + "update/");
		if (oldupdatedir.exists()) {
			ModManager.debugLogger.writeMessage("Moving update to data/");
			try {
				FileUtils.moveDirectory(oldupdatedir, new File(ModManager.getToolsDir()));
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("FAILED TO MOVE update TO data/ DIRECTORY! Deleting the update/ folder instead (will auto download the new 7z)");
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

		// move coalesced.original
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
		File mod_info = new File("mod_info");
		mod_info.delete();
		File derbylog = new File("derby.log");
		derbylog.delete();
		File tlk = new File("tlk");
		File toc = new File("toc");
		File coalesceds = new File("coalesceds");
		try {
			FileUtils.deleteDirectory(toc);
			FileUtils.deleteDirectory(tlk);
			FileUtils.deleteDirectory(coalesceds);
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Unable to cleanup old stuff.", e);
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
				//System.out.println("Searching for file: " + searchSubDirDesc);
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
			stream = ModManager.class.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			jarFolder = new File(ModManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
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
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
					"The BioGame directory is not valid.\nMod Manager cannot install the DLC bypass.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File gamedir = bgdir.getParentFile();

		File launcherWV = new File(gamedir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
		ModManager.debugLogger.writeMessage("Using binary win32 folder: " + launcherWV.getAbsolutePath());

		try {
			ModManager.ExportResource("/Launcher_WV.exe", launcherWV.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			if (isAdmin()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "An error occured extracting Launcher_WV.exe out of ME3CMM.exe.\nPlease report this to FemShep.",
						"Launcher_WV.exe error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"An error occured extracting Launcher_WV.exe out of ME3CMM.exe.\nYou may need to run ME3CMM.exe as an administrator.", "Launcher_WV.exe error",
						JOptionPane.ERROR_MESSAGE);
			}
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			return false;
		}

		return true;
	}

	public static int checkforME3105(String biogamedir) {
		//Check to make sure ME3 1.05
		File executable = new File(new File(biogamedir).getParent() + "\\Binaries\\Win32\\MassEffect3.exe");
		if (!executable.exists()) {
			ModManager.debugLogger.writeError("Unable to find game EXE at " + executable);
			return -1;
		}
		return EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());
	}

	public static boolean installBinkw32Bypass(String biogamedir, boolean asi) {
		ModManager.debugLogger.writeMessage("Installing binkw32.dll DLC authorizer. Using the ASI version: " + asi);

		// extract and install binkw32.dll
		// from
		// http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a

		File bgdir = new File(biogamedir);
		int exebuild = checkforME3105(biogamedir);
		if (exebuild <= -1) {
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Unable to detect game executable version.\nInstall aborted.", "Mass Effect 3 EXE not detected",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (exebuild != 5) {
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Binkw32 bypass does not support any version of Mass Effect 3 except 1.05.\n" + (exebuild == 6
					? "Downgrade to Mass Effect 3 1.05 to use it, or continue using LauncherWV through Mod Manager.\nThe ME3Tweaks forums has instructions on how to do this."
					: "Upgrade your game to use 1.05. Pirated editions of the game are not supported."), "Unsupported ME3 version", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");

		try {
			ModManager.ExportResource("/binkw23.dll", bink32_orig.toString());
			if (asi) {
				ModManager.ExportResource("/binkw32_asi.dll", bink32.toString());
				File zlib = new File(gamedir.toString() + "\\Binaries\\Win32\\zlib1.dll");
				ModManager.ExportResource("/zlib1.dll", zlib.toString());
			} else {
				ModManager.ExportResource("/binkw32.dll", bink32.toString());
			}
		} catch (Exception e1) {
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e1));
			if (isAdmin()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"An error occured extracting binkw32" + (asi ? "_asi" : "") + ".dll out of ME3CMM.exe.\nPlease report this to FemShep.",
						"binkw32" + (asi ? "_asi" : "") + ".dll error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"An error occured extracting binkw32" + (asi ? "_asi" : "")
								+ ".dll out of ME3CMM.exe.\nYou may need to run ME3CMM.exe as an administrator or grant yourself write permissions from the tools menu.",
						"binkw32" + (asi ? "_asi" : "") + ".dll error", JOptionPane.ERROR_MESSAGE);
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
		return appendSlash(System.getProperty("user.dir")) + "mods\\";
	}

	/**
	 * Gets the data/ folder, returning with an appended slash
	 *
	 * @return
	 */
	public static String getDataDir() {
		return appendSlash(System.getProperty("user.dir")) + "data\\";
	}

	public static String getGUITransplanterDir() {
		return getCommandLineToolsDir();
	}

	/**
	 * Returns the path to the Mod Manager Command Line Tools directory.
	 * 
	 * @return data/tools/ModMangerCommandLine/(x64/x86)/
	 */
	public static String getCommandLineToolsDir() {
		return getToolsDir() + "ModManagerCommandLine\\" + (ResourceUtils.is64BitWindows() ? "x64" : "x86") + "\\";
	}

	/**
	 * Downloads Transplanter if not already downloaded. Returns path if
	 * downloaded, null if not found locally after download attempt.
	 *
	 * @param download
	 *            Download transplanter from ME3Tweaks if not available locally
	 * @return path to transplanter, null if none can be acquired
	 */
	public static String getGUITransplanterCLI(boolean download) {
		File transplanterexe = new File(getGUITransplanterDir() + "Transplanter-CLI.exe");
		if (!transplanterexe.exists() && download) {
			if (!downloadGUITransplanter()) {
				return null;
			}
		}
		if (!transplanterexe.exists() && download) {
			//still hasn't downloaded...
			return null;
		}
		return transplanterexe.getAbsolutePath();
	}

	private static boolean downloadGUITransplanter() {
		String url = "https://me3tweaks.com/modmanager/tools/GUITRANSPLANTER-MM5.7z";
		ModManager.debugLogger.writeMessage("Downloading GUI Transplanter: " + url);
		try {
			File updateDir = new File(ModManager.getTempDir());
			updateDir.mkdirs();
			FileUtils.copyURLToFile(new URL(url), new File(ModManager.getTempDir() + "guitransplanter.7z"));
			ModManager.debugLogger.writeMessage("library 7z downloaded.");

			//run 7za on it
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.get7zExePath());
			commandBuilder.add("-y"); //overwrite
			commandBuilder.add("x"); //extract
			commandBuilder.add(ModManager.getTempDir() + "guitransplanter.7z");//7z file
			commandBuilder.add("-o" + getGUITransplanterDir()); //extraction path
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			ModManager.debugLogger.writeMessage("Extracting GUI library...");
			int returncode = 1;
			ProcessBuilder pb = new ProcessBuilder(command);
			returncode = ModManager.runProcess(pb).getReturnCode();
			ModManager.debugLogger.writeMessage("Unzip completed successfully (code 0): " + (returncode == 0));
			FileUtils.deleteQuietly(new File(ModManager.getTempDir() + "guitransplanter.7z"));
			return true;
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Error downloading GUI Transplanter:", e);
		}
		return false;
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
	 * Gets the list of saved BIOGAme directories from the BIOGAME_DIRECTORIES
	 * file. If none are found, it looks up the registry key and returns that.
	 * ModMAnagerWindow will also look up the registry key to set the default
	 * value.
	 * 
	 * @return list of found biogame directories
	 */
	public static ArrayList<String> getSavedBIOGameDirectories() {
		ArrayList<String> directories = new ArrayList<>();
		File file = new File(ModManager.getSavedBIOGameDirectoriesFile());
		if (file.exists()) {
			Scanner scanner;
			try {
				scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String directory = scanner.nextLine();
					directory = ResourceUtils.removeTrailingSlashes(directory);
					if (!(new File(directory).exists())) {
						continue; //skip
					}
					if (!directories.contains(directory)) {
						directories.add(directory);
					}
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				ModManager.debugLogger.writeError("BIOGAME_DIRECTORIES file does not exist.");
			}
		}
		if (directories.size() == 0) {
			Wini ini = ModManager.LoadSettingsINI();
			String setDir = ini.get("Settings", "biogame_dir");
			ModManager.debugLogger.writeMessage("FALLBACK: ME3CMM.ini has saved the biogame directory to (blank/null if doesn't exist): " + setDir);
			if (setDir != null) {
				File dir = new File(setDir);
				if (dir.exists()) {
					setDir = ResourceUtils.removeTrailingSlashes(setDir);
					directories.add(setDir);
					try {
						ModManager.debugLogger.writeMessage("Upgrading biogame directory to BIOGAME_DIRECTORIES file.");
						FileUtils.writeLines(file, directories);
						ini.remove("Settings", "biogame_dir");
						ini.store();
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("ERROR UPGRADING ME3CMM.ini bigame_dir to BIOGAME_DIRECTORIES:", e);
					}
					return directories;
				}
			}
			//Haven't found any yet... look it up
			String regpath = ModManager.LookupGamePathViaRegistryKey(true);
			if (regpath != null && new File(regpath).exists()) {
				regpath = ResourceUtils.removeTrailingSlashes(regpath);
				directories.add(regpath);
			}
		}
		return directories;

	}

	/**
	 * Returns data/me3tweaksservicescache/
	 *
	 * @return
	 */
	public static String getME3TweaksServicesCache() {
		File file = new File(getDataDir() + "me3tweaksservicescache/");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Gets ME3Explorer directory, with slash on the end
	 *
	 * @return
	 */
	public static String getME3ExplorerEXEDirectory() {
		File me3expdir = new File(getDataDir() + "ME3Explorer/");
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
	 * Returns the mixinlibrary folder.
	 *
	 * @return ME3CMM/mixinlibrary/
	 */
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
				ModManager.debugLogger.writeError(key + " TOC in pristine directory has failed hash check: " + hash + " vs known good value: " + tocHashes.get(key));
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
	 *            Path inside of a module. E.g.
	 *            /DLC/DLC_CON_MP4/CookedPCConsole/Test.pcc
	 * @param targetModule
	 *            MP4
	 * @return
	 */
	public static String getPatchSource(String targetPath, String targetModule) {
		ModManager.debugLogger.writeMessage("Looking for patch source: " + targetPath + " in module " + targetModule);
		File sourceDestination = new File(getPatchesDir() + "source/" + ME3TweaksUtils.headerNameToInternalName(targetModule) + File.separator + targetPath);
		String bioGameDir = ModManager.appendSlash(ModManagerWindow.GetBioGameDir());
		if (sourceDestination.exists()) {
			ModManager.debugLogger.writeMessage("Patch source is already in library.");
			return sourceDestination.getAbsolutePath();
		} else {
			ModManager.debugLogger.writeMessage("Patch source is not in library (would be at: " + sourceDestination.getAbsolutePath() + "), fetching from game directory.");
		}
		if (targetModule.equals(ModTypeConstants.BASEGAME)) {
			// we must use PCCEditor2 to decompress the file using the
			// -decompresspcc command line arg
			//get source directory via relative path chaining
			File sourceSource = new File(ModManager.appendSlash(new File(bioGameDir).getParent()) + targetPath);
			if (sourceSource.exists()) {
				sourceDestination.getParentFile().mkdirs();

				// run ME3EXPLORER --decompress-pcc
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching " + sourceDestination.getName());
				ProcessResult pr = ModManager.decompressPCC(sourceSource, sourceDestination);
				if (pr.getReturnCode() == 0) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Cached " + sourceDestination.getName());
				} else {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching failed for file " + sourceDestination.getName());
				}
				ModManager.debugLogger.writeMessage("File decompressed to location, and ready? : " + sourceDestination.exists());
			}
			return sourceDestination.exists() ? sourceDestination.getAbsolutePath() : null;

			// END OF
			// BASEGAME======================================================
		} else if (targetModule.equals(ModTypeConstants.CUSTOMDLC)) {
			System.err.println("CUSTOMDLC IS NOT SUPPORTED RIGHT NOW");
			return null;
		} else {
			// DLC===============================================================
			// Check if its unpacked
			String gamedir = appendSlash(new File(ModManagerWindow.GetBioGameDir()).getParent());
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
			if (targetModule.equals(ModTypeConstants.TESTPATCH)) {
				sfarName = "Patch_001.sfar";
			}
			String sfarPath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(targetModule)) + sfarName;
			if (new File(sfarPath).exists()) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching " + sourceDestination.getName());
				ProcessResult pr = ModManager.ExtractFileFromSFAR(sfarPath, targetPath, sourceDestination.getParent());
				// patchProcessBuilder.redirectErrorStream(true);
				// patchProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				if (pr.getReturnCode() == 0) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Cached " + sourceDestination.getName());
					ModManager.debugLogger.writeMessage("Caching complete for file " + sourceDestination.getName());
					return sourceDestination.getAbsolutePath();

				} else {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching failed for file " + sourceDestination.getName());
					ModManager.debugLogger.writeError("Caching failed (non 0 return code) for file " + sourceDestination.getName());
					return null;
				}
			} else {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching failed for file " + sourceDestination.getName());
				ModManager.debugLogger.writeError("Caching failed (DLC not installed) for file " + sourceDestination.getName());
				return null;
			}
		}
	}

	public static ProcessResult ExtractFileFromSFAR(String sfarPath, String targetPath, String extractionDirectory) {
		// TODO Auto-generated method stub
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getCommandLineToolsDir() + "SFARTools-Extract.exe");
		commandBuilder.add("--SFARPath");
		commandBuilder.add(sfarPath);
		commandBuilder.add("--ExtractFilenames");
		commandBuilder.add(targetPath);
		commandBuilder.add("--OutputPath");
		commandBuilder.add(extractionDirectory);
		commandBuilder.add("--FlatFolderExtraction");

		ProcessBuilder extractionProcessBuilder = new ProcessBuilder(commandBuilder);
		return ModManager.runProcess(extractionProcessBuilder);
	}

	/**
	 * Decompresses the PCC to the listed destination, location can be the same.
	 * If the PCC file is already decompressed, the file is copied instead.
	 *
	 * @param sourceSource
	 *            PCC to decompress
	 * @param sourceDestination
	 *            Place to put decompressed pcc
	 * @return result of the decomrpession process
	 */
	public static ProcessResult decompressPCC(File sourceSource, File sourceDestination) {
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getCommandLineToolsDir() + "PCCDecompress.exe");
		commandBuilder.add("--inputfile");
		commandBuilder.add(sourceSource.getAbsolutePath());
		commandBuilder.add("--outputfile");
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
		commandBuilder.add(ModManager.getCommandLineToolsDir() + "PCCDecompress.exe");
		commandBuilder.add("--inputfile");
		commandBuilder.add(sourceSource.getAbsolutePath());
		commandBuilder.add("--outputfile");
		commandBuilder.add(sourceDestination.getAbsolutePath());
		commandBuilder.add("--compress");

		ProcessBuilder decompressProcessBuilder = new ProcessBuilder(commandBuilder);
		return ModManager.runProcess(decompressProcessBuilder);
	}

	/**
	 * Dumps the pcc using the specified options package.
	 *
	 * @param pcc
	 *            PCC to dump
	 * @param options
	 *            Options package that is used to set the exe switches
	 * 
	 * @return Process result of dump
	 */
	public static ProcessResult dumpPCC(String pcc, PCCDumpOptions options) {
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getGUITransplanterCLI(false));
		commandBuilder.add("--inputfile");
		commandBuilder.add(pcc);
		commandBuilder.add("--gamedir");
		commandBuilder.add(options.gamePath);
		commandBuilder.add("--extract");
		if (options.names) {
			commandBuilder.add("--names");
		}
		if (options.imports) {
			commandBuilder.add("--imports");
		}
		if (options.exports) {
			commandBuilder.add("--exports");
		}
		if (options.coalesced) {
			commandBuilder.add("--coalesced");
		}
		if (options.scripts) {
			commandBuilder.add("--scripts");
		}
		if (options.properties) {
			commandBuilder.add("--properties");
			commandBuilder.add("--tlkcache");
			commandBuilder.add(ModManager.getPCCDumpTLKCacheFolder());
		}

		if (options.swfs) {
			commandBuilder.add("--swfs");
		}

		String outputfolder = options.outputFolder;
		//Calculate output folder
		if (pcc.contains("BIOGame")) {
			//Try to extract what is is.
			if (pcc.toLowerCase().contains("BIOGame\\CookedPCConsole\\".toLowerCase())) {
				//Likely basegame.
				outputfolder += "BASEGAME\\";
				commandBuilder.add("--outputfolder");
				commandBuilder.add(outputfolder);
			} else if (pcc.contains("BIOGame\\DLC\\")) {
				try {
					int startOfDLCName = pcc.indexOf("BIOGame\\DLC\\") + 12;
					String dlcname = pcc.substring(startOfDLCName, pcc.length());
					int endOfDLCName = dlcname.indexOf("\\");
					dlcname = dlcname.substring(0, endOfDLCName);
					commandBuilder.add("--outputfolder");
					commandBuilder.add(outputfolder + dlcname + "\\");
				} catch (Exception e) {
					ModManager.debugLogger.writeMessage("Unable to parse out the dlc directory name. Just dumping to the top level pcc dump folder.");
					//Just dump to the top level folder
					commandBuilder.add("--outputfolder");
					commandBuilder.add(options.outputFolder);
				}
			}

		} else {
			//Just dump to the top level folder
			commandBuilder.add("--outputfolder");
			commandBuilder.add(options.outputFolder);
		}
		ProcessBuilder decompressProcessBuilder = new ProcessBuilder(commandBuilder);
		return ModManager.runProcess(decompressProcessBuilder, FilenameUtils.getName(pcc));
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
		ModManager.debugLogger.writeMessage("Getting game file (will use unpacked if possible) from " + targetModule + ", with relative path " + targetPath);
		String bioGameDir = ModManager.appendSlash(ModManagerWindow.GetBioGameDir());
		File destFile = new File(copyToLocation);
		FileUtils.deleteQuietly(destFile);
		new File(destFile.getParent()).mkdirs();

		if (targetModule.equals(ModTypeConstants.BASEGAME)) {
			if (targetPath.endsWith(".pcc")) {
				File sourceSource = new File(ModManager.appendSlash(new File(bioGameDir).getParent()) + targetPath);
				File destinationFile = new File(copyToLocation);
				ModManager.decompressPCC(sourceSource, destinationFile);
				if (!destinationFile.exists()) {
					ModManager.debugLogger.writeError("PCC Decompressor did not successfully place the file into the library.");
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
		} else if (targetModule.equals(ModTypeConstants.CUSTOMDLC)) {
			ModManager.debugLogger.writeError("Fetching files from CustomDLC is not supported.");
			return null;
		} else {
			// DLC===============================================================
			// Check if its unpacked
			String gamedir = appendSlash(new File(ModManagerWindow.GetBioGameDir()).getParent());
			File unpackedFile = new File(gamedir + targetPath);
			if (unpackedFile.exists()) {
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
			if (targetModule.equals(ModTypeConstants.TESTPATCH)) {
				sfarName = "Patch_001.sfar";
			}
			String sfarPath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(targetModule)) + sfarName;

			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.getCommandLineToolsDir() + "SFARTools-Extract.exe");
			commandBuilder.add("--sfarpath");
			commandBuilder.add(sfarPath);
			commandBuilder.add("--sfarpath");

			commandBuilder.add(targetPath);
			commandBuilder.add(copyToLocation);
			ProcessBuilder extractionProcessBuilder = new ProcessBuilder(commandBuilder);
			ModManager.runProcess(extractionProcessBuilder);
			return copyToLocation;
		}
	}

	/**
	 * Loads patch objects from the patchlibrary/patches directory
	 *
	 * @return
	 */
	public static ArrayList<Patch> getPatchesFromDirectory() {
		ModManager.debugLogger.writeMessage("Loading MixIns from data directory.");
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
			// Got a list of subdirs. Now loop them to find all patchdesc.ini
			// files
			ModManager.debugLogger.writeMessage("Loading MixIns...");
			for (int i = 0; i < subdirs.length; i++) {
				File searchSubDirDesc = new File(ModManager.appendSlash(subdirs[i].toString()) + "patchdesc.ini");
				if (searchSubDirDesc.exists()) {
					Patch validatingPatch = new Patch(searchSubDirDesc.getAbsolutePath(), ModManager.appendSlash(subdirs[i].toString()) + "patch.jsf");
					if (validatingPatch.isValid()) {
						validPatches.add(validatingPatch);
					}
				}
			}
			ModManager.debugLogger.writeMessage("Loaded " + validPatches.size() + " MixIns.");

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
		if (biogameDir == null) {
			return false;
		}
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

	/**
	 * Checks for the ASI binkw32 bypass.
	 *
	 * @return true if bink23 exists and bink32 hash matches known ASI version,
	 *         false otherwise
	 */
	public static boolean checkIfASIBinkBypassIsInstalled(String biogameDir) {
		if (biogameDir == null) {
			return false;
		}
		ModManager.debugLogger.writeMessage("Checking for ASI Binkw32 ASI with biogame location: " + biogameDir);
		File bgdir = new File(biogameDir);
		if (!bgdir.exists()) {
			ModManager.debugLogger.writeMessage("Biogame dir does not exist: " + biogameDir);
			return false;
		}
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Game directory: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		File bink23 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw23.dll");
		try {
			// Original ASI hash, July 8 2017 v3 hash, July 23 2017 v4 Hash
			String[] asiBinkHashes = { "65eb0d2e5c3ccb1cdab5e48d1a9d598d", "bc37adee806059822c972b71df36775d", "1acccbdae34e29ca7a50951999ed80d5" };
			ArrayList<String> asihashlist = new ArrayList<>(Arrays.asList(asiBinkHashes));
			String binkhash = MD5Checksum.getMD5Checksum(bink32.toString());
			if (asihashlist.contains(binkhash) && bink23.exists()) {
				ModManager.debugLogger.writeMessage("Binkw32 ASI is installed");
				//Add zlib if not present to bring old installs up to date
				File zlib = new File(gamedir.toString() + "\\Binaries\\Win32\\zlib1.dll");
				if (!zlib.exists()) {
					ModManager.debugLogger.writeMessage("Installing zlib.dll to bring old ASI bypass installation up to date");
					ModManager.ExportResource("/zlib1.dll", zlib.toString());
				}
				return true;
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Exception while attempting to find DLC bypass (Binkw32).", e);
		}
		ModManager.debugLogger.writeMessage("Binkw32 ASI is not installed");

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

	/**
	 * Returns directory that contains folders of patches
	 *
	 * @return /mixinlibrary/patches/
	 */
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
					ModManager.debugLogger.writeMessage("This version (" + releaseNum + ") satisfies the current requirements (" + MIN_REQUIRED_NET_FRAMEWORK_RELNUM + ")");
					NET_FRAMEWORK_IS_INSTALLED = true;
					return true;
				} else {
					ModManager.debugLogger.writeError("This version (" + releaseNum + ") DOES NOT satisfy the current requirements (" + MIN_REQUIRED_NET_FRAMEWORK_RELNUM + ")");
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
	 * Runs a process already built via processbuilder. This method simply runs
	 * the EXE and does not wait for it, nor does it print any of its output.
	 *
	 * @param p
	 *            Process to build and run
	 */
	public static void runProcessDetached(ProcessBuilder p) {
		try {
			StringBuilder sb = new StringBuilder();
			List<String> list = p.command();
			for (String arg : list) {
				sb.append(arg);
				sb.append(" ");
			}
			ModManager.debugLogger.writeMessage("runProcessDetached(): " + sb.toString());
			p.start();
			return;
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Process exception occured:", e);
			return;
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
		final StringWriter writer = new StringWriter();
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
			new Thread(new Runnable() {
				public void run() {
					try {
						IOUtils.copy(process.getInputStream(), writer); //don't know which charset to use.
						IOUtils.copy(process.getErrorStream(), writer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			int returncode = process.waitFor();
			long endTime = System.currentTimeMillis();
			ModManager.debugLogger.writeMessage("Process finished with code " + returncode + ", took " + (endTime - startTime) + " ms.");
			ModManager.debugLogger.writeMessage("Process output: " + writer.toString());
			String output = writer.toString();
			writer.close();
			return new ProcessResult(returncode, output, null);
		} catch (IOException | InterruptedException e) {
			ModManager.debugLogger.writeErrorWithException("Process exception occured:", e);
			String output = writer.toString();
			try {
				writer.close();
			} catch (IOException e1) {
				//don't care.
			}
			return new ProcessResult(-1, output, e);
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
	 * Returns map of Custom DLC files mapped to a list of custom dlc they
	 * appear in. If the list is longer than 1 element it means there is a
	 * conflict. Only scans for PCC files. The returned values are sorted in
	 * order of lowest to highest priority.
	 *
	 * @param customDLCs
	 *            list of Custom DLCs to scan against
	 * @param dlcdirectory
	 *            DLC directory of ME3
	 * @return map of conflicts, or null if exception occurs.
	 */
	public static HashMap<String, ArrayList<CustomDLC>> getCustomDLCConflicts(ArrayList<CustomDLC> customDLCs, String dlcdirectory) {
		try {
			Collections.sort(customDLCs);

			//prepare list of folders
			HashMap<String, ArrayList<CustomDLC>> filesMap = new HashMap<>();
			ArrayList<String> customDLCFolders = new ArrayList<String>();
			for (CustomDLC dlc : customDLCs) {
				customDLCFolders.add(dlcdirectory + dlc.getDlcName());
			}

			//construct lists of what custom dlc each file appears in
			for (CustomDLC custDLC : customDLCs) {
				String dlcFolder = ResourceUtils.normalizeFilePath(dlcdirectory + custDLC.getDlcName() + File.separator, true);
				Collection<File> files = FileUtils.listFiles(new File(dlcFolder), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
				for (File file : files) {
					if (!FilenameUtils.getExtension(file.getAbsolutePath().toLowerCase()).equals("pcc")) {
						continue;
					}
					String filename = FilenameUtils.getName(file.getAbsolutePath());
					ArrayList<CustomDLC> dlcFileAppearsIn = null;
					boolean keyExists = filesMap.containsKey(filename);
					if (keyExists) {
						//conflict
						dlcFileAppearsIn = filesMap.get(filename);

					} else {
						dlcFileAppearsIn = new ArrayList<>();
					}
					dlcFileAppearsIn.add(custDLC);
					filesMap.put(filename, dlcFileAppearsIn);
				}
			}
			return filesMap;
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error getting DLC conflict list:", e);
		}
		return null;
	}

	/**
	 * Gets a list of DLC that begin with the name DLC_. The values are
	 * converted to uppercase.
	 *
	 * @param biogamedir
	 * @return list of foldernames that are considered by the game to be real
	 *         DLC.
	 */
	public static ArrayList<String> getInstalledDLC(String biogamedir) {
		File mainDlcDir = new File(ModManager.appendSlash(biogamedir) + "DLC/");
		ModManager.debugLogger.writeMessage("Getting list of installed active DLC from " + mainDlcDir);
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				File f = new File(current, name);
				return f.isDirectory() && f.getName().toUpperCase().startsWith("DLC_");
			}
		});
		ArrayList<String> foldernames = new ArrayList<String>();
		if (directories != null) {
			for (String folder : directories) {
				foldernames.add(folder.toUpperCase());
			}
		} else {
			ModManager.debugLogger.writeError("CRITICAL ERROR: UNABLE TO ENUMERATE DLCS!");
		}
		return foldernames;
	}

	/**
	 * Gets the path to the GUI library specified by the DLC name
	 *
	 * @param dlcname
	 *            DLC to get library for
	 * @param download
	 *            Download library if library not found. If set to false, this
	 *            will return the library path, if true it will return null if
	 *            the library can't be downloaded.
	 * @return library path or null if download is true and library can't be
	 *         downloaded
	 */
	public static String getGUILibraryFor(String dlcname, boolean download) {
		String libraryPath = getUILibraryPath();
		switch (dlcname) {
		case "DLC_CON_XBX":
			libraryPath += "XBX/";
			break;
		case "DLC_CON_UIScaling":
			libraryPath += "UIScaling/";
			break;
		default:
			ModManager.debugLogger.writeError("Unknown GUI Library: " + dlcname);
			return null;
		}

		File libraryFolder = new File(libraryPath);
		if (!libraryFolder.exists() && download || !libraryFolder.isDirectory() && download) {
			downloadGUILibrary(dlcname);
		}
		if (!libraryFolder.exists() && download) {
			return null;
		}
		return libraryPath;
	}

	/**
	 * Downloads an extract a GUI library from ME3Tweaks using the provided
	 * dlcname as a filename.
	 *
	 * @param dlcname
	 *            library to download
	 * @return true if extraction is OK, false if something went wrong
	 */
	private static boolean downloadGUILibrary(String dlcname) {
		String url = "https://me3tweaks.com/modmanager/tools/uilibrary/" + dlcname.toUpperCase() + ".7z";
		ModManager.debugLogger.writeMessage("Downloading GUI library: " + url);
		try {
			File updateDir = new File(ModManager.getTempDir());
			updateDir.mkdirs();
			FileUtils.copyURLToFile(new URL(url), new File(ModManager.getTempDir() + "guilibrary.7z"));
			ModManager.debugLogger.writeMessage("library 7z downloaded.");

			//run 7za on it
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.get7zExePath());
			commandBuilder.add("-y"); //overwrite
			commandBuilder.add("x"); //extract
			commandBuilder.add(ModManager.getTempDir() + "guilibrary.7z");//7z file
			commandBuilder.add("-o" + ModManager.getUILibraryPath()); //extraction path

			// System.out.println("Building command");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			// Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			ModManager.debugLogger.writeMessage("Extracting GUI library...");
			int returncode = 1;
			ProcessBuilder pb = new ProcessBuilder(command);
			returncode = ModManager.runProcess(pb).getReturnCode();
			ModManager.debugLogger.writeMessage("Unzip completed successfully (code 0): " + (returncode == 0));
			//FileUtils.deleteQuietly(new File(ModManager.getTempDir() + "guilibrary.7z"));
			return true;
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Error downloading GUI library:", e);
		}
		return false;
	}

	/**
	 * Returns the path of the UI Library
	 *
	 * @return
	 */
	private static String getUILibraryPath() {
		return getDataDir() + "UILibrary" + File.separator;
	}

	/**
	 * Returns true if DLC_CON_XBX, DLC_CON_UIScaling, or
	 * DLC_CON_UIScaling_Shared is present in the DLC directory.
	 *
	 * @param biogameDirectory
	 *            biogame dir
	 * @return true if folder exists, false otherwise
	 */
	public static boolean isGUIModInstalled(String biogameDirectory) {
		String dlcfolder = appendSlash(biogameDirectory) + "DLC/";
		for (String dlc : KNOWN_GUI_CUSTOMDLC_MODS) {
			File f = new File(dlcfolder + dlc);
			ModManager.debugLogger.writeMessage("Scanning for UI mod: " + f);
			if (f.exists() && f.isDirectory()) {
				return true;
			}
		}
		return false;
	}

	public static File getASIManifestFile() {
		return new File(getME3TweaksServicesCache() + "asimanifest.xml");
	}

	public static File getThirdPartyModDBFile() {
		return new File(getME3TweaksServicesCache() + "thirdpartymoddb.json");
	}

	public static boolean checkIfCMMPatchIsTooLong() {
		return System.getProperty("user.dir").length() > 120;
	}

	public static boolean areBalanceChangesInstalled(String bioGameDir) {
		File bcf = new File((new File(bioGameDir).getParent()) + "/Binaries/win32/asi/ServerCoalesced.bin");
		return bcf.exists();
	}

	/**
	 * Returns a file object taht points to a new log file that will be written
	 * to disk
	 *
	 * @return
	 */
	public static File getNewLogFile(String fname) {
		if (fname.equals("")) {
			fname = "Log";
		} else {
			fname = fname.replaceAll("\\\\", "-");
			fname = fname.replaceAll("..", "_");
		}
		// TODO Auto-generated method stub
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm");
		String datestr = dateFormat.format(date);
		String filepath = getLogsDir() + fname + "-ModManager" + ModManager.VERSION + "_b" + ModManager.BUILD_NUMBER + " " + datestr + ".txt";
		return new File(filepath);
	}

	private static String getLogsDir() {
		File file = new File(getDataDir() + "logs/");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Checks if MassEffect3.exe is currently running. Uses native code.
	 * 
	 * @return
	 */
	public static boolean isMassEffect3Running() {
		try {
			Kernel32 kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
			Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
			boolean result = false;
			WinNT.HANDLE snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
			try {
				while (kernel32.Process32Next(snapshot, processEntry)) {
					if ("MassEffect3.exe".toUpperCase().equals(Native.toString(processEntry.szExeFile).toUpperCase())) {
						result = true;
						break;
					}
				}
			} finally {
				kernel32.CloseHandle(snapshot);
			}
			ModManager.debugLogger.writeMessage("Mass Effect 3 is " + (result ? "" : "not ") + "currently running.");
			return result;
		} catch (Throwable t) {
			ModManager.debugLogger.writeErrorWithException("Critical native access exception: ", t);
			ModManager.debugLogger.writeError("Mod Manager will report that the game is not running to continue normal operations.");
			return false;
		}
	}

	/**
	 * Runs the process specified by p, and logs output with the specified
	 * prefix. Returns the process result.
	 * 
	 * @param p
	 *            ProcessBuilder object to run
	 * @param prefix
	 *            Prefix for logs
	 * @return ProcessResult of the process
	 */
	public static ProcessResult runProcess(ProcessBuilder p, String prefix) {
		final StringWriter writer = new StringWriter();

		try {
			StringBuilder sb = new StringBuilder();
			List<String> list = p.command();
			for (String arg : list) {
				sb.append(arg);
				sb.append(" ");
			}
			ModManager.debugLogger.writeMessage("[" + prefix + "]runProcess(): " + sb.toString());
			long startTime = System.currentTimeMillis();
			Process process = p.start();
			//handle stdout
			new Thread(new Runnable() {
				public void run() {
					try {
						IOUtils.copy(process.getInputStream(), writer); //don't know which charset to use.
						IOUtils.copy(process.getErrorStream(), writer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			int returncode = process.waitFor();
			long endTime = System.currentTimeMillis();
			ModManager.debugLogger.writeMessage("[" + prefix + "]Process finished with code " + returncode + ", took " + (endTime - startTime) + " ms.");
			ModManager.debugLogger.writeMessage("[" + prefix + "]Process output: " + writer.toString());
			String output = writer.toString();
			writer.close();
			return new ProcessResult(returncode, output, null);
		} catch (IOException | InterruptedException e) {
			ModManager.debugLogger.writeErrorWithException("[" + prefix + "]Process exception occured:", e);
			String output = writer.toString();
			try {
				writer.close();
			} catch (IOException e1) {
				//don't care.
			}
			return new ProcessResult(-1, output, e);
		}
	}

	/**
	 * Reads me3cmm.ini from disk. If it does not exist, it is created. In
	 * almost all instances this should work - if on read only media it won't,
	 * but that's not really my concern.
	 * 
	 * @return Wini object for ME3CMM.ini
	 */
	public static Wini LoadSettingsINI() {
		File settings = new File(ModManager.SETTINGS_FILENAME);
		if (!settings.exists()) {
			try {
				settings.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ModManager.debugLogger.writeErrorWithException("ERROR WRITING NEW SETTINGS FILE:", e);
			}
		}
		Wini ini = null;
		try {
			ini = new Wini(settings);
			ini.load(settings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("ERROR READING CONFIG FILE... WE'RE PROBABLY GOING TO CRASH.", e);
		}

		return ini;
	}

	/**
	 * Looks up the game's installation directory using the windows registry
	 * 
	 * @param appendBiogame
	 *            appends BIOGame to the end, if found.
	 * @return
	 */
	public static String LookupGamePathViaRegistryKey(boolean appendBiogame) {
		String installDir = null;
		String _32bitpath = "SOFTWARE\\BioWare\\Mass Effect 3";
		String _64bitpath = "SOFTWARE\\Wow6432Node\\BioWare\\Mass Effect 3";
		ModManager.debugLogger.writeMessage("Looking up location of game using registry...");
		try {
			installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _64bitpath, "Install Dir");
			ModManager.debugLogger.writeMessage("Game location found - via 64bit key.");
		} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
			ModManager.debugLogger.writeMessage("Exception looking at 64 registry key: " + _64bitpath);
		}

		if (installDir == null) {
			// try 32bit key
			try {
				ModManager.debugLogger.writeMessage("64-bit registry key not found. Attemping to find via 32-bit registy key");
				installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _32bitpath, "Install Dir");
				ModManager.debugLogger.writeMessage("Game location found - via 32bit key.");
			} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
				ModManager.debugLogger.writeMessage("Exception looking at 32bit registry key: " + _32bitpath);
			}
		}
		if (installDir != null) {
			installDir = ModManager.appendSlash(installDir);
			ModManager.debugLogger.writeMessage("Found mass effect 3 location in registry: " + installDir);
			if (appendBiogame) {
				installDir += "BIOGame";
			}
		} else {
			ModManager.debugLogger.writeError("Could not find Mass Effect 3 registry key in both 32 and 64 bit locations.");
		}

		return installDir;
	}

	/**
	 * Looks up the game's language through the registry.
	 * 
	 * @return language code if found, null if not found
	 */
	public static String LookupGameLanguageViaRegistryKey() {
		String locale = null;
		String _32bitpath = "SOFTWARE\\BioWare\\Mass Effect 3";
		String _64bitpath = "SOFTWARE\\Wow6432Node\\BioWare\\Mass Effect 3";
		ModManager.debugLogger.writeMessage("Looking up location of game using registry...");
		try {
			locale = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _64bitpath, "Locale");
			ModManager.debugLogger.writeMessage("Locale found - via 64bit key.");
		} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
			ModManager.debugLogger.writeMessage("Exception looking at 64 registry key: " + _64bitpath);
		}

		if (locale == null) {
			// try 32bit key
			try {
				ModManager.debugLogger.writeMessage("64-bit registry key not found. Attemping to find via 32-bit registy key");
				locale = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _32bitpath, "Locale");
				ModManager.debugLogger.writeMessage("Game location found - via 32bit key.");
			} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
				ModManager.debugLogger.writeMessage("Exception looking at 32bit registry key: " + _32bitpath);
			}
		}
		if (locale != null) {
			ModManager.debugLogger.writeMessage("Found mass effect 3 locale in registry: " + locale);
		} else {
			ModManager.debugLogger.writeError("Could not find Mass Effect 3 registry key in both 32 and 64 bit locations.");
		}

		return locale;
	}

	/**
	 * Runs autotoc on the listed folders.
	 * 
	 * @param folders
	 *            Folders to generate PCConsoleTOC files for
	 * @return AutoTOC'd folders
	 */
	public static ProcessResult runAutoTOCOnFolders(ArrayList<String> folders) {
		ArrayList<String> commandBuilder = new ArrayList<String>();
		commandBuilder.add(ModManager.getCommandLineToolsDir() + "FullAutoTOC.exe");
		commandBuilder.add("--tocfolders");
		for (String folder : folders) {
			commandBuilder.add(folder);
		}

		String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
		ProcessBuilder pb = new ProcessBuilder(command);
		return ModManager.runProcess(pb);
	}

	/**
	 * Returns data/BIOGAME_DIRECTORIES.
	 * 
	 * @return Path to the BIOGAME_DIRECTORIES file.
	 */
	public static String getSavedBIOGameDirectoriesFile() {
		return ModManager.getDataDir() + "BIOGAME_DIRECTORIES";
	}

	public static boolean GrantPermissionsToDirectory(String directory, String username) {
		if (directory.endsWith("\\") || directory.endsWith("/")) {
			directory = directory.substring(0, directory.length() - 1);
		}
		ArrayList<String> command = new ArrayList<String>();
		command.add(ModManager.getCommandLineToolsDir() + "elevate.exe");
		command.add("-c");
		command.add("-n");
		command.add("-w");
		command.add("icacls");
		command.add(directory);
		command.add("/t");
		command.add("/grant");
		command.add(username + ":(OI)(CI)F");
		ModManager.debugLogger.writeMessage("Granting permissions to the current user for selected directory: " + directory);
		ProcessBuilder pb = new ProcessBuilder(command);
		ProcessResult pr = ModManager.runProcess(pb);
		return pr.getReturnCode() == 0;
	}

	public static boolean checkWritePermissions(String selectedGamePath) {
		File testfile = new File(selectedGamePath + "\\MODMANAGER_PERMISSIONSTEST");
		if (testfile.exists()) {
			boolean deleted = testfile.delete();
			if (deleted) {
				ModManager.debugLogger.writeMessage("User has write permissions to file: " + testfile);
				return true;
			} else {
				ModManager.debugLogger.writeMessage("User does not have write permissions to file: " + testfile);
				return false;
			}
		} else {
			try {
				boolean created = testfile.createNewFile();
				if (created) {
					testfile.delete();
					ModManager.debugLogger.writeMessage("User has write permissions (create) to file: " + testfile);
					return true;
				} else {
					ModManager.debugLogger.writeMessage("User does not have write permissions (create) to file: " + testfile);
					return false;
				}
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("User does not have write permissions (create) to file: " + testfile);
				ModManager.debugLogger.writeException(e);
				return false;
			}
		}
	}

	public static String getCommandLineToolsRequiredVersion() {
		return ModManager.MIN_REQUIRED_CMDLINE_MAIN + "." + ModManager.MIN_REQUIRED_CMDLINE_MINOR + "." + ModManager.MIN_REQUIRED_CMDLINE_BUILD + "."
				+ ModManager.MIN_REQUIRED_CMDLINE_REV;
	}

	/**
	 * Returns the PCC dumping folder.
	 * 
	 * @return data\pccdumps\
	 */
	public static String getPCCDumpFolder() {
		File file = new File(getDataDir() + "PCCdumps\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Returns the PCC dumping folder's TLK cache
	 * 
	 * @return data\pccdumps\TLKCache
	 */
	public static String getPCCDumpTLKCacheFolder() {
		File file = new File(getDataDir() + "PCCdumps\\TLKCache\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Gets the directory that contains the batch installation groups.
	 * 
	 * @return data\modgroups\
	 */
	public static String getModGroupsFolder() {
		File file = new File(getDataDir() + "modgroups\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Gets the directory that modmaker xml files are cached to, with a \ on the
	 * end.
	 * 
	 * @return cache directory path
	 */
	public static String getModmakerCacheDir() {
		File file = new File(getDataDir() + "modmaker\\cache\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Gets the directory for running tests in Mod Manager.
	 * 
	 * @return data\testing\ with a slash on the end.
	 */
	public static String getTestingDir() {
		File file = new File(getDataDir() + "testing\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * 
	 * @return data\Patch_001_Extracted\
	 */
	public static String getTestpatchUnpackFolder() {
		File file = new File(getDataDir() + "Patch_001_Extracted\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * 
	 * @return data\ALOTInstaller\
	 */
	public static String getALOTInstallerDirectory() {
		File file = new File(getDataDir() + "ALOTInstaller\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * 
	 * @return data\Deployed Mods\
	 */
	public static String getDeploymentDirectory() {
		File file = new File(getDataDir() + "Deployed Mods\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * 
	 * @return data\tools\ModManagerCommandLine\<arch>\7z.exe
	 */
	public static String get7zExePath() {
		return getCommandLineToolsDir() + "7z.exe";
	}

	/**
	 * 
	 * @return data\tools\ModManagerCommandLine\<arch>\7z.dll
	 */
	public static String get7zDllPath() {
		return getCommandLineToolsDir() + "7z.dll";
	}

	/**
	 * Compresses the selected mod for deployment by staging the mod and then
	 * compressing the staged mod - this prevents additional files from being
	 * included in the mod. More ram you have = more compressed the mod will be.
	 * 
	 * @param mod
	 *            Mod to stage
	 * @return Path to output file
	 */
	public static String compressModForDeployment(Mod mod) {
		String outputpath = getDeploymentDirectory() + mod.getModName() + "_" + mod.getVersion() + ".7z";
		ModManager.debugLogger.writeMessage("Deploying " + mod.getModName());

		//Get amount of memory
		int dictsize = 64; //Default size - definitely not the best, but we'll default to 32-bit.

		if (ResourceUtils.is64BitWindows()) {
			ModManager.debugLogger.writeMessage("64-bit Windows - calculating dictionary size that *should* work on this computer...");
			String[] memorycommand = new String[] { "wmic", "ComputerSystem", "get", "TotalPhysicalMemory" };
			ProcessBuilder pb = new ProcessBuilder(memorycommand);
			ProcessResult pr = ModManager.runProcess(pb);
			String output = pr.getOutput();

			String[] lines = output.split("\\n");
			if (lines.length == 3) {
				String memsizebytes = lines[1].replaceAll("[^0-9]", "");
				long bytecount = Long.parseLong(memsizebytes);
				String memsize = ResourceUtils.humanReadableByteCount(bytecount, false);
				ModManager.debugLogger.writeMessage("Total memory (including virtual): " + memsize);
				dictsize = (int) (bytecount / 43 / 1024 / 1024); //MB
				dictsize = Math.max(dictsize, 256); //avoid out of memory error on 32-bit java
				ModManager.debugLogger.writeMessage("Chosen dictionary size: " + dictsize + "MB");
			}
		}

		ArrayList<String> commandBuilder = new ArrayList<String>();

		commandBuilder.add("cmd");
		commandBuilder.add("/c");
		commandBuilder.add("start");
		commandBuilder.add("Mod Manager Mod Compressor");
		commandBuilder.add("/wait");
		commandBuilder.add(ModManager.get7zExePath());
		commandBuilder.add("a"); //add
		commandBuilder.add(outputpath); //destfile
		commandBuilder.add(mod.getModPath());//inputfile

		//commandBuilder.add("-mmt" + numcores); //let it multithread itself.
		commandBuilder.add("-mx=9"); //max compression
		commandBuilder.add("-aoa"); //overwrite/update
		commandBuilder.add("-md" + dictsize + "m");
		ModManager.debugLogger.writeMessage("Compressing mod - output to " + outputpath);
		ProcessBuilder pb = new ProcessBuilder(commandBuilder);
		FileUtils.deleteQuietly(new File(outputpath));
		ModManager.runProcess(pb).getReturnCode();
		return outputpath;
	}

	/**
	 * Tries to find a resource for a target path inside of a target module.
	 * This method searches inside the cmmbackup directory. Returns path to the
	 * found item or null if none could be found.
	 *
	 * @param targetPath
	 *            Path inside of a module. E.g.
	 *            /DLC/DLC_CON_MP4/CookedPCConsole/Test.pcc
	 * @param targetModule
	 *            Headername, e.g. MP4
	 * @return
	 */
	public static String getBackupPatchSource(String targetPath, String targetModule) {
		String cmmbackup = new File(ModManagerWindow.GetBioGameDir()).getParent() + "\\cmmbackup";
		System.out.println(cmmbackup);

		ModManager.debugLogger.writeMessage("Looking for backup patch source: " + targetPath + " in module " + targetModule);
		File sourceDestination = new File(getPatchesDir() + "source/" + ME3TweaksUtils.headerNameToInternalName(targetModule) + File.separator + targetPath);
		if (sourceDestination.exists()) {
			ModManager.debugLogger.writeMessage("Patch source is already in library.");
			return sourceDestination.getAbsolutePath();
		}

		String backupDir = ModManager.appendSlash(cmmbackup);

		if (targetModule.equals(ModTypeConstants.BASEGAME)) {
			// we must decompress the file
			//get source directory via relative path chaining
			File sourceSource = new File(backupDir + targetPath);
			if (sourceSource.exists()) {
				sourceDestination.getParentFile().mkdirs();

				// run PCC Decompressor
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching " + sourceDestination.getName());
				ProcessResult pr = ModManager.decompressPCC(sourceSource, sourceDestination);
				if (pr.getReturnCode() == 0) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Cached " + sourceDestination.getName());
				} else {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching failed for file " + sourceDestination.getName());
				}
				ModManager.debugLogger.writeMessage("File decompressed to location, and ready? : " + sourceDestination.exists());
				return sourceDestination.exists() ? sourceDestination.getAbsolutePath() : null;
			} else {
				ModManager.debugLogger.writeMessage("Could not find file in cmmbackup for patch fetch : " + sourceSource.getAbsolutePath());
				return null;
			}
			// END OF
			// BASEGAME======================================================
		} else if (targetModule.equals(ModTypeConstants.CUSTOMDLC)) {
			System.err.println("CUSTOMDLC IS NOT SUPPORTED RIGHT NOW");
			return null;
		} else {
			// DLC===============================================================
			// Pull from SFAR.bak
			// Check if its unpacked
			String gamedir = appendSlash(new File(ModManagerWindow.GetBioGameDir()).getParent());

			// use the sfar
			// get .sfar path
			String sfarName = "Default.sfar.bak";
			if (targetModule.equals(ModTypeConstants.TESTPATCH)) {
				sfarName = "Patch_001.sfar.bak";
			}
			String sfarPath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(targetModule)) + sfarName;
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching " + sourceDestination.getName());
			ProcessResult pr = ModManager.ExtractFileFromSFAR(sfarPath, targetPath, sourceDestination.getParent());
			if (pr.getReturnCode() == 0) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Cached " + sourceDestination.getName());
				ModManager.debugLogger.writeMessage("Caching complete for file " + sourceDestination.getName());
				return sourceDestination.getAbsolutePath();

			} else {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Caching failed for file " + sourceDestination.getName());
				ModManager.debugLogger.writeError("Caching failed (non 0 return code) for file " + sourceDestination.getName());
				return null;

			}
		}
	}

	/**
	 * Checks if Mod Manager is running from the bundled JRE, or an installed
	 * system one.
	 * 
	 * @return true if using the JRE from data/jre-x64. false if using any other
	 *         JRE.
	 */
	public static boolean isUsingBundledJRE() {
		String jrepath = System.getProperty("java.home");
		String modmanpath = System.getProperty("user.dir");
		String relpath = "data\\jre-x64";
		try {
			String calculatedRelativePath = ResourceUtils.getRelativePath(jrepath, modmanpath, File.separator);
			if (calculatedRelativePath.equals(relpath)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static String getBundledJREPath() {
		return getDataDir() + "jre-x64\\";
	}

	public static String getME3TweaksUpdaterServiceFolder() {
		// TODO Auto-generated method stub
		File file = new File(getDataDir() + "ME3TweaksUpdaterService\\");
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	public static boolean isALOTInstalled(String biogamePath) {
		biogamePath += "/CookedPCConsole/adv_combat_tutorial_xbox_D_Int.afc";
		File markerfile = new File(biogamePath);
		if (markerfile.exists()) {
			Path path = Paths.get(markerfile.getAbsolutePath());
			try {
				byte[] markerbytes = new byte[] { 0x4D, 0x45, 0x4D, 0x49 };
				byte[] data = Files.readAllBytes(path);
				byte[] final4bytes = Arrays.copyOfRange(data, data.length - 4, data.length);
				if (Arrays.equals(final4bytes, markerbytes)) {
					return true;
				}
				return false;
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Error checking if ALOT is installed.", e);
			}
		}
		return false;
	}
}