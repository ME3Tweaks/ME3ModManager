package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me3tweaks.modmanager.help.HelpMenu;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModXMLTools;
import com.me3tweaks.modmanager.modupdater.UpdatePackage;
import com.me3tweaks.modmanager.objects.AlternateCustomDLC;
import com.me3tweaks.modmanager.objects.AlternateFile;
import com.me3tweaks.modmanager.objects.InstalledASIMod;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModDelta;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModList;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.RestoreMode;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.repairdb.BasegameHashDB;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.me3tweaks.modmanager.valueparsers.bioai.BioAIGUI;
import com.me3tweaks.modmanager.valueparsers.biodifficulty.DifficultyGUI;
import com.me3tweaks.modmanager.valueparsers.consumable.ConsumableGUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerCustomActionGUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerCustomActionGUI2;
import com.me3tweaks.modmanager.valueparsers.wavelist.WavelistGUI;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import net.iharder.dnd.FileDrop;

/**
 * Controls the main window for Mass Effect 3 Mod Manager.
 * 
 * @author mgamerz
 *
 */
@SuppressWarnings("serial")
public class ModManagerWindow extends JFrame implements ActionListener, ListSelectionListener {
	public static ModManagerWindow ACTIVE_WINDOW;
	public static ArrayList<Integer> forceUpdateOnReloadList = new ArrayList<Integer>();
	private static String PRELOADED_BIOGAME_DIR;
	boolean isUpdate;
	public JTextField fieldBiogameDir;
	JTextArea fieldDescription;
	JScrollPane scrollDescription;
	JButton buttonBioGameDir, buttonApplyMod, buttonStartGame;
	JMenuBar menuBar;
	JMenu actionMenu, modMenu, modManagementMenu, devMenu, modDeltaMenu, toolsMenu, backupMenu, restoreMenu, sqlMenu, helpMenu, openToolMenu, modAlternatesMenu;
	JMenuItem actionExitDebugMode, actionCheckForContentUpdates, actionModMaker, actionVisitMe, actionOptions, actionReload, actionExit;
	JMenuItem modManagementImportFromArchive, modManagementImportAlreadyInstalled, modManagementConflictDetector, modManagementModMaker, modManagementASI, modManagementFailedMods,
			modManagementPatchLibary, modManagementClearPatchLibraryCache;
	JMenuItem modutilsHeader, modutilsInfoEditor, modNoDeltas, modutilsVerifyDeltas, modutilsInstallCustomKeybinds, modutilsAutoTOC, modutilsCheckforupdate, modutilsRestoreMod,
			modutilsDeleteMod;
	JMenuItem toolME3Explorer, toolsOpenME3Dir, toolsInstallLauncherWV, toolsInstallBinkw32, toolsInstallBinkw32asi, toolsUninstallBinkw32, toolsMountdlcEditor, toolsMergeMod,
			toolME3Config;
	JMenuItem backupBackupDLC, backupCreateGDB;
	JMenuItem restoreSelective, restoreRevertEverything, restoreDeleteUnpacked, restoreRevertBasegame, restoreRevertAllDLC, restoreRevertSPDLC, restoreRevertMPDLC,
			restoreRevertMPBaseDLC, restoreRevertSPBaseDLC, restoreRevertCoal, restoreVanillifyDLC;

	JMenuItem modDevStarterKit;
	JMenuItem sqlWavelistParser, sqlDifficultyParser, sqlAIWeaponParser, sqlPowerCustomActionParser, sqlPowerCustomActionParser2, sqlConsumableParser, sqlGearParser;
	JList<Mod> modList;
	JProgressBar progressBar;
	ListSelectionModel listSelectionModel;
	JSplitPane splitPane;
	public JLabel labelStatus;
	final String selectAModDescription = "Select a mod on the left to view its description.";
	DefaultListModel<Mod> modModel;
	private ArrayList<Patch> patchList;
	private JMenuItem moddevUpdateXMLGenerator;
	private JMenuItem modManagementCheckallmodsforupdate;
	private JMenuItem restoreRevertUnpacked;
	private JMenuItem restoreRevertBasegameUnpacked;
	private JMenuItem restoredeleteAllCustomDLC;
	private JMenuItem restoreCustomDLCManager;
	private JMenuItem backupBasegameUnpacked;
	private JMenuItem toolsUnpackDLC;
	private JMenuItem modDeltaRevert;
	private JMenuItem toolTankmasterCoalFolder;
	private JMenuItem toolTankmasterCoalUI;
	private JMenuItem toolTankmasterTLK;
	private JMenuItem modManagementOpenModsFolder;
	private JMenu mountMenu;
	private JButton modWebsiteLink;
	private ArrayList<Mod> invalidMods;
	private JButton modlistFailedIndicatorLink;
	private JPanel cookedDirPanel;
	private JMenuItem toolsAutoTOCGame;
	private JMenu restoreMenuAdvanced;
	private String preloadedBioGameDir;

	/**
	 * Opens a new Mod Manager window. Disposes of old ones if one is open.
	 * Automatically makes window visible so it will block the UI thread.
	 * 
	 * @param isUpdate
	 *            If this is an upgrade from a previous version of mod manager
	 */
	public ModManagerWindow(boolean isUpdate) {
		if (ACTIVE_WINDOW != null) {
			ACTIVE_WINDOW.dispose();
			ACTIVE_WINDOW = null;
		}
		this.isUpdate = isUpdate;
		ModManager.debugLogger.writeMessage("Starting Mod Manager UI (ModManagerWindow)");
		boolean reload = false;
		try {
			initializeWindow();
			ACTIVE_WINDOW = this;
			reload = processPendingPatches();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("UNKNOWN CRITICAL STARTUP EXCEPTION FOR MOD MANAGER WINDOW:", e);
			ModManager.debugLogger.writeError("Mod Manager has crashed!");
			JOptionPane.showMessageDialog(null,
					"<html><div style=\"width:330px;\">Mod Manager's interface (post-startup) encountered a critical unknown error and was unable to start:<br>" + e.getMessage()
							+ "<br>"
							+ "<br>This has been logged to the me3cmm_last_run_log.txt file if you didn't explicitly turn logging off.<br>Please report this to femshep.</div></html>",
					"Critical Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		validateBIOGameDir();
		if (reload) {
			new ModManagerWindow(false);
		} else {
			ModManager.debugLogger.writeMessage("Mod Manager GUI: Now setting visible.");
			try {
				boolean asiinstalled = ASIModWindow.IsASIModGroupInstalled(5);
				System.out.println("UG5 installed: " + asiinstalled);
				this.setVisible(true);
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Uncaught runtime exception:", e);
				ModManager.debugLogger.writeError("Mod Manager hit exception!");
				JOptionPane.showMessageDialog(null,
						"<html><div style=\"width:330px;\">Mod Manager's interface has just encountered an error<br>" + e.getMessage() + "<br>"
								+ "<br>This has been logged to the me3cmm_last_run_log.txt file if you didn't explicitly turn logging off.<br>The application will attempt to ignore this error.</div></html>",
						"Mod Manager Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * This method scans all mod files and sees if any ones have imported
	 * patches that need to be applied.
	 * 
	 * DEPRECATED.
	 * 
	 * @return should reload mods and patches
	 */
	private boolean processPendingPatches() {
		boolean reload = false;
		ArrayList<String> modsBuild = new ArrayList<String>();
		for (int i = 0; i < modModel.size(); i++) {
			Mod mod = modModel.getElementAt(i);
			if (mod.getRequiredPatches().size() > 0) {
				ModManager.debugLogger.writeMessage("Mod being built with included MixIns:");
				reload = true;
				new PatchApplicationWindow(this, mod.getRequiredPatches(), mod);
				modsBuild.add(mod.getModName());
			}
		}
		if (reload) {
			String dispStr = "The following mods had included mixins and have been built for use:\n";
			for (String str : modsBuild) {
				dispStr += " - " + str + "\n";
			}
			JOptionPane.showMessageDialog(null, dispStr);
			dispose();
		}
		return reload;
	}

	private void initializeWindow() {
		setupWindow();
		this.pack();
		setLocationRelativeTo(null);
		if (isUpdate) {
			JOptionPane.showMessageDialog(this, "Update successful: Updated to Mod Manager " + ModManager.VERSION + " (Build " + ModManager.BUILD_NUMBER
					+ ").\nYou can access the changelog via the Help menu.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
		}

		// clear pending updates (done via sideload update)
		for (Integer code : forceUpdateOnReloadList) {
			for (int i = 0; i < modModel.getSize(); i++) {
				Mod mod = modModel.getElementAt(i);
				if (mod.getClassicUpdateCode() == code) {
					mod.setVersion(0.001);
				}
			}
		}

		new NetworkThread(false).execute();
		ModManager.debugLogger.writeMessage("Mod Manager GUI: Initialize() has completed.");
	}

	/**
	 * This thread executes multiple items: Check For Mod Manager Updates Check
	 * for Mod Updates Check for 7za file + download Check for ME3Explorer
	 * Updates (if needed)
	 * 
	 * @author Michael
	 *
	 */
	class NetworkThread extends SwingWorker<Void, ThreadCommand> {

		private boolean force;

		public NetworkThread(boolean force) {
			this.force = force;
		}

		@Override
		public Void doInBackground() {
			File f7za = new File(ModManager.getToolsDir() + "7za.exe");
			if (!f7za.exists()) {
				publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 7za Unzipper"));
				ModManager.debugLogger.writeMessage("7za.exe does not exist at the following path, downloading new copy: " + f7za.getAbsolutePath());
				String url = "https://me3tweaks.com/modmanager/tools/7za.exe";
				try {
					File updateDir = new File(ModManager.getToolsDir());
					updateDir.mkdirs();
					FileUtils.copyURLToFile(new URL(url), new File(ModManager.getToolsDir() + "7za.exe"));
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded 7za Unzipper into tools directory"));
					ModManager.debugLogger.writeMessage("Downloaded missing 7za.exe file for updating Mod Manager");

				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error downloading 7za into tools folder", e);
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading 7za"));
				}
			} else {
				ModManager.debugLogger.writeMessage("7za.exe is present in tools/ directory");
			}

			File lzma = new File(ModManager.getToolsDir() + "lzma.exe");
			if (!lzma.exists()) {
				publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading LZMA tool"));
				ModManager.debugLogger.writeMessage("lzma.exe does not exist at the following path, downloading new copy: " + lzma.getAbsolutePath());
				String url = "https://me3tweaks.com/modmanager/tools/lzma.exe";
				try {
					File updateDir = new File(ModManager.getToolsDir());
					updateDir.mkdirs();
					FileUtils.copyURLToFile(new URL(url), new File(ModManager.getToolsDir() + "lzma.exe"));
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded lzma.exe into tools directory"));
					ModManager.debugLogger.writeMessage("Downloaded missing lzma.exe file for preparing mod updates");

				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error downloading lzma into tools folder", e);
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading lzma"));
				}
			} else {
				ModManager.debugLogger.writeMessage("lzma.exe is present in tools/ directory");
			}

			// Tankmaster TLK, Coalesce
			File tmc = new File(ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe");
			File tmtlk = new File(ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe");

			if (!tmtlk.exists() || !tmc.exists()) {
				publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading Tankmaster Tools"));
				ModManager.debugLogger.writeMessage("Tankmaster's TLK/COALESCE tools are missing, downloading new copy: " + tmtlk.getAbsolutePath());
				String url = "https://me3tweaks.com/modmanager/tools/tankmastertools.7z";
				try {
					File updateDir = new File(ModManager.getTempDir());
					updateDir.mkdirs();
					FileUtils.copyURLToFile(new URL(url), new File(ModManager.getTempDir() + "tankmastertools.7z"));
					ModManager.debugLogger.writeMessage("7z downloaded.");

					// run 7za on it
					ArrayList<String> commandBuilder = new ArrayList<String>();
					commandBuilder.add(ModManager.getToolsDir() + "7za.exe");
					commandBuilder.add("-y"); // overwrite
					commandBuilder.add("x"); // extract
					commandBuilder.add(ModManager.getTempDir() + "tankmastertools.7z");// 7z
																						// file
					commandBuilder.add("-o" + ModManager.getDataDir()); // extraction
																		// path

					// System.out.println("Building command");
					String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
					// Debug stuff
					StringBuilder sb = new StringBuilder();
					for (String arg : command) {
						sb.append(arg + " ");
					}
					ModManager.debugLogger.writeMessage("Executing 7z extraction command: " + sb.toString());
					Process p = null;
					int returncode = 1;
					try {
						ProcessBuilder pb = new ProcessBuilder(command);
						long timeStart = System.currentTimeMillis();
						p = pb.start();
						ModManager.debugLogger.writeMessage("Executed command, waiting...");
						returncode = p.waitFor();
						long timeEnd = System.currentTimeMillis();
						ModManager.debugLogger.writeMessage("Process has finished. Took " + (timeEnd - timeStart) + " ms.");
					} catch (IOException | InterruptedException e) {
						ModManager.debugLogger.writeException(e);
					}

					ModManager.debugLogger.writeMessage("Unzip completed successfully (code 0): " + (p != null && returncode == 0));
					if (returncode == 0) {
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded Tankmaster Tools into data directory"));
					} else {
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Unknown error downloading Tankmaster tools"));
					}
					FileUtils.deleteQuietly(new File(ModManager.getTempDir() + "tankmastertools.7z"));
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error downloading 7za into tools folder", e);
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading 7za for updating"));
				}
			} else {
				ModManager.debugLogger.writeMessage("7za.exe is present in tools/ directory");
			}

			if (ModManager.AUTO_UPDATE_MOD_MANAGER && !ModManager.CHECKED_FOR_UPDATE_THIS_SESSION) {
				checkForModManagerUpdates();
			}
			checkForME3ExplorerUpdates();
			if (ModManager.AUTO_UPDATE_CONTENT || forceUpdateOnReloadList.size() > 0 || force) {
				checkForContentUpdates(force);
			}
			return null;
		}

		private void checkForME3ExplorerUpdates() {
			String me3explorer = ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe";
			File f = new File(me3explorer);
			if (!f.exists()) {
				ModManager.debugLogger.writeMessage("ME3Explorer is missing. Downloading from ME3Tweaks.");
				if (ModManager.AUTO_UPDATE_ME3EXPLORER) {
					new ME3ExplorerUpdaterWindow(ModManagerWindow.this);
				} else {
					ModManager.debugLogger.writeError("ME3Explorer missing but cannot download due to settings!");
					JOptionPane.showMessageDialog(ModManagerWindow.this,
							"ME3Explorer is missing from data/ME3Explorer.\nMod Manager requires ME3Explorer but you have auto updates for it turned off.\nIf there are errors in this session, they are not supported by FemShep.",
							"Missing ME3Explorer", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				int main = EXEFileInfo.getMajorVersionOfProgram(me3explorer);
				int rev = EXEFileInfo.getBuildOfProgram(me3explorer);
				ModManager.debugLogger.writeMessage("ME3Explorer Version: " + Arrays.toString(EXEFileInfo.getVersionInfo(me3explorer)));
				if (main < ModManager.MIN_REQUIRED_ME3EXPLORER_MAIN || main == ModManager.MIN_REQUIRED_ME3EXPLORER_MAIN && rev < ModManager.MIN_REQUIRED_ME3EXPLORER_REV) {
					// we must update it
					ModManager.debugLogger.writeMessage("ME3Explorer is outdated, local:" + main + "." + rev + ", required" + ModManager.MIN_REQUIRED_ME3EXPLORER_MAIN + "."
							+ ModManager.MIN_REQUIRED_ME3EXPLORER_REV + "+");
					if (ModManager.AUTO_UPDATE_ME3EXPLORER) {
						new ME3ExplorerUpdaterWindow(ModManagerWindow.this);
					} else {
						ModManager.debugLogger.writeError("ME3Explorer outdated but cannot download due to settings!");
						JOptionPane.showMessageDialog(ModManagerWindow.this,
								"Mod Manager requires a newer version of ME3Explorer for this build.\nYou have auto updates for it turned off. You will need to turn them back on to download this update.\nIf there are errors in this session, they are not supported by FemShep.",
								"ME3Explorer Outdated", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					ModManager.debugLogger.writeMessage("Current ME3Explorer version satisfies requirements for Mod Manager");
				}
			}
		}

		private void checkForContentUpdates(boolean force) {
			// Check for updates
			if (ModManager.AUTO_UPDATE_CONTENT || forceUpdateOnReloadList.size() > 0 || force) {
				if (System.currentTimeMillis() - ModManager.LAST_AUTOUPDATE_CHECK > ModManager.AUTO_CHECK_INTERVAL_MS || forceUpdateOnReloadList.size() > 0 || force) {
					ModManager.debugLogger.writeMessage("Running auto-updater, it has been "
							+ ModManager.getDurationBreakdown(System.currentTimeMillis() - ModManager.LAST_AUTOUPDATE_CHECK) + " since the last help/mods update check.");
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading latest help information"));
					HelpMenu.getOnlineHelp();
					publish(new ThreadCommand("UPDATE_HELP_MENU"));
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading ASI list from ME3Tweaks"));
					ASIModWindow.getOnlineASIManifest();
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading latest MixIns"));
					String updateStr = PatchLibraryWindow.getLatestMixIns();
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", updateStr));
					if (modModel.getSize() > 0) {
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Checking for updates to mods"));
						checkAllModsForUpdates(false);
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Mod update check complete"));
					}
					try {
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 3rd party mod identification info"));
						ModManager.debugLogger.writeMessage("Downloading third party mod data from identification service");
						FileUtils.copyURLToFile(new URL("http://me3tweaks.com/mods/dlc_mods/thirdpartyidentificationservice"), ModManager.getThirdPartyModDBFile());
						ModManager.THIRD_PARTY_MOD_JSON = FileUtils.readFileToString(ModManager.getThirdPartyModDBFile());
						ModManager.debugLogger.writeMessage("Downloaded third party mod data from identification service");
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded 3rd party mod identification info"));

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						ModManager.debugLogger.writeErrorWithException("Failed to download third party identification data: ", e);
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to get 3rd party mod identification info"));
					}
					if (validateBIOGameDir()) {
						ArrayList<InstalledASIMod> outdatedASImods = ASIModWindow.getOutdatedASIMods(GetBioGameDir());
						if (outdatedASImods.size() > 0) {
							ModManager.debugLogger.writeMessage("At least one ASI is outdated, advertising update");
							publish(new ThreadCommand("SET_STATUSBAR_TEXT", "ASI mods are outdated"));
							publish(new ThreadCommand("SHOW_OUTDATED_ASI_MODS", null, outdatedASImods));
						} else {
							ModManager.debugLogger.writeMessage("Installed ASIs are up to date (if any are installed)");
						}
					}
				}
			}
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand latest : chunks) {
				switch (latest.getCommand()) {
				case "UPDATE_HELP_MENU":
					menuBar.remove(helpMenu);
					menuBar.add(HelpMenu.constructHelpMenu());
					break;
				case "SHOW_UPDATE_WINDOW":
					new UpdateAvailableWindow((JSONObject) latest.getData(), ModManagerWindow.this);
					break;
				case "SET_STATUSBAR_TEXT":
					labelStatus.setText(latest.getMessage());
					break;
				case "SHOW_OUTDATED_ASI_MODS":
					String message = "The following installed ASIs are out of date:";
					ArrayList<InstalledASIMod> outdated = (ArrayList<InstalledASIMod>) latest.getData();
					for (InstalledASIMod asi : outdated) {
						message += "\n - " + asi.getFilename();
					}
					message += "\n\nOpen the ASI Mod Management window to update them under Mod Management.";
					JOptionPane.showMessageDialog(ModManagerWindow.this, message, "ASI mod update available", JOptionPane.WARNING_MESSAGE);
					break;
				}

			}
		}

		@Override
		protected void done() {
			forceUpdateOnReloadList.clear(); // remove pending updates
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Exception in the network thread: ", e);
			}
		}

		private Void checkForModManagerUpdates() {
			labelStatus.setText("Checking for Mod Manager updates");
			ModManager.debugLogger.writeMessage("Checking for update...");
			// Check for update
			try {
				String update_check_link;
				if (ModManager.IS_DEBUG) {
					update_check_link = "http://webdev-c9-mgamerz.c9.io/modmanager/updatecheck?currentversion=" + ModManager.BUILD_NUMBER;
				} else {
					update_check_link = "https://me3tweaks.com/modmanager/updatecheck?currentversion=" + ModManager.BUILD_NUMBER;
				}
				String serverJSON = null;
				try {
					serverJSON = IOUtils.toString(new URL(update_check_link), StandardCharsets.UTF_8);
					System.out.println(update_check_link);
				} catch (Exception e) {
					ModManager.debugLogger.writeError("Error checking for updates:");
					ModManager.debugLogger.writeException(e);
					labelStatus.setText("Error checking for update (check logs)");
					ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
					return null;
				}
				if (serverJSON == null) {
					ModManager.debugLogger.writeError("No response from server");
					labelStatus.setText("Updater: No response from server");
					ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
					return null;
				}

				JSONParser parser = new JSONParser();
				JSONObject latest_object = (JSONObject) parser.parse(serverJSON);
				String buildHash = (String) latest_object.get("build_md5");
				boolean hashMismatch = false;
				if (new File("ME3CMM.exe").exists()) {
					try {
						String currentHash = MD5Checksum.getMD5Checksum("ME3CMM.exe");
						if (buildHash != null && !buildHash.equals("") && !currentHash.equals(buildHash)) {
							// hash mismatch
							hashMismatch = true;
							ModManager.debugLogger.writeMessage("Local hash (" + currentHash + ") does not match server hash (" + buildHash + ")");
						} else {
							ModManager.debugLogger.writeMessage("Local hash matches server hash: " + buildHash);
						}
					} catch (Exception e1) {
						ModManager.debugLogger.writeErrorWithException("Unable to hash ME3CMM.exe:", e1);
					}
				} else {
					ModManager.debugLogger.writeMessage("Skipping hash check for updates. Likely running in IDE or some other wizardry");
				}

				long latest_build = (long) latest_object.get("latest_build_number");
				if (latest_build < ModManager.BUILD_NUMBER) {
					// build is newer than current
					labelStatus.setVisible(true);
					ModManager.debugLogger.writeMessage("No updates, at latest version. (or could not contact update server.)");
					labelStatus.setText("No Mod Manager updates available");
					ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
					checkForGUIupdates(latest_object);
					return null;
				}

				if (latest_build == ModManager.BUILD_NUMBER && !hashMismatch) {
					// build is same as server version
					labelStatus.setVisible(true);
					ModManager.debugLogger.writeMessage("No updates, at latest version.");
					labelStatus.setText("Mod Manager is up to date");
					ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
					checkForGUIupdates(latest_object);
					return null;
				}

				ModManager.debugLogger
						.writeMessage("Update check: Local:" + ModManager.BUILD_NUMBER + " Latest: " + latest_build + ", is less? " + (ModManager.BUILD_NUMBER < latest_build));

				boolean showUpdate = true;
				// make sure the user hasn't declined this one.
				Wini settingsini;
				try {
					settingsini = new Wini(new File(ModManager.SETTINGS_FILENAME));
					String showIfHigherThan = settingsini.get("Settings", "nextupdatedialogbuild");
					long build_check = ModManager.BUILD_NUMBER;
					if (showIfHigherThan != null && !showIfHigherThan.equals("")) {
						try {
							build_check = Integer.parseInt(showIfHigherThan);
							if (latest_build > build_check) {
								// update is newer than one stored in ini, show
								// the
								// dialog.
								ModManager.debugLogger.writeMessage("Advertising build " + latest_build);
								settingsini.remove("Settings", "nextupdatedialogbuild");
								settingsini.store();
								showUpdate = true;
							} else {
								ModManager.debugLogger.writeMessage("User isn't seeing updates until build " + build_check);
								// don't show it.
								showUpdate = false;
							}
						} catch (NumberFormatException e) {
							ModManager.debugLogger.writeMessage("Number format exception reading the build number updateon in the ini. Showing the dialog.");
						}
					}
				} catch (InvalidFileFormatException e) {
					ModManager.debugLogger.writeErrorWithException("Invalid INI! Did the user modify it by hand?", e);
					e.printStackTrace();
				} catch (IOException e) {
					ModManager.debugLogger.writeMessage("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
				}

				if (showUpdate) {
					// An update is available!
					publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Mod Manager update available"));
					publish(new ThreadCommand("SHOW_UPDATE_WINDOW", null, latest_object));
					ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
				} else {
					labelStatus.setVisible(true);
					labelStatus.setText("Update notification suppressed until next build");
				}
				checkForGUIupdates(latest_object);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				ModManager.debugLogger.writeErrorWithException("Error parsing server response:", e);
			}
			ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
			return null;
		}

		/**
		 * Parses additional info out of the latest server json related to GUI
		 * library info
		 * 
		 * @param serversJSON
		 */
		private void checkForGUIupdates(JSONObject serversJSON) {
			Object obj = serversJSON.get("latest_guitransplanter");
			long latestGUITransplanter = ModManager.MIN_REQUIRED_ME3GUITRANSPLANTER_BUILD;
			if (obj != null) {
				latestGUITransplanter = Long.parseLong((String) obj);
				String cliPath = ModManager.getGUITransplanterCLI(false);
				// update GUI Transplanter
				File cli = new File(cliPath);
				if (cli.exists()) {
					ModManager.debugLogger.writeMessage("Checking for updates for GUI Transplanter.");
					int build = EXEFileInfo.getRevisionOfProgram(cliPath);
					ModManager.debugLogger.writeMessage("The local build of GUI Transplanter is currently 1.0.0." + build + ".");
					if (build < latestGUITransplanter) {
						// download
						publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading update for GUI Transplanter"));
						ModManager.debugLogger.writeMessage("GUI Transplanter is out of date from server. Local " + build + " vs server " + latestGUITransplanter);
						File transplanterFolder = cli.getParentFile();
						FileUtils.deleteQuietly(transplanterFolder);
						ModManager.debugLogger.writeMessage("Downloading new GUI Transplanter");
						String newcli = ModManager.getGUITransplanterCLI(true);
						if (newcli != null) {
							int newbuild = EXEFileInfo.getRevisionOfProgram(cliPath);
							if (newbuild == latestGUITransplanter) {
								ModManager.debugLogger.writeMessage("Downloaded latest GUI Transplanter build: " + newbuild);
								publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Updated GUI Transplanter"));
							} else {
								ModManager.debugLogger.writeError("Failed to download proper updated build. Instead we downloaded: " + newbuild);
								publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to download new GUI Transplanter"));
							}
						} else {
							ModManager.debugLogger.writeError("Failed to download GUI Transplanter");
							publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to download new GUI Transplanter"));
						}
					} else {
						ModManager.debugLogger.writeMessage("GUI Transplanter is up to date (1.0.0." + build + ")");
					}
				} else {
					ModManager.debugLogger.writeMessage("No GUI Transplanter tool downloaded, skipping update check for it");
				}
			}

			String uiLibPath = ModManager.getGUILibraryFor("DLC_CON_UIScaling", false);
			{
				File uiLibVersionFile = new File(uiLibPath + "libraryversion.txt");
				ModManager.debugLogger.writeMessage("Checking for UISCALING GUI library version file at " + uiLibVersionFile);
				if (uiLibVersionFile.exists()) {
					try {
						String uilibverstr = FileUtils.readFileToString(uiLibVersionFile);
						double libver = Double.parseDouble(uilibverstr);
						ModManager.debugLogger.writeMessage("Local UISCALING library version: " + libver);
						obj = serversJSON.get("latest_uiscaling_guilibrary");
						if (obj != null) {
							double serverver = Double.parseDouble((String) obj);
							if (libver < serverver) {
								ModManager.debugLogger.writeMessage("UIScaling Library is out of date, updating...");
								FileUtils.deleteQuietly(new File(uiLibPath));
								publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Updating Interface Scaling Mod GUI library"));
								String newLib = ModManager.getGUILibraryFor("DLC_CON_UIScaling", true);
								if (newLib != null) {
									String newlibverstr = FileUtils.readFileToString(uiLibVersionFile);
									double newlibver = Double.parseDouble(newlibverstr);
									if (newlibver == serverver) {
										ModManager.debugLogger.writeMessage("Downloaded latest Interface Scaling Mod GUI Library: " + newlibver);
										publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Updated Interface Scaling Mod GUI library"));
									} else {
										ModManager.debugLogger.writeError("Failed to download proper updated GUI Library. Instead we downloaded: " + newlibver);
										publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to download new GUI Library"));
									}
								}
							} else {
								ModManager.debugLogger.writeMessage("Local UISCALING library is up to date");
							}
						}
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Error reading local settings:", e);
					} catch (NumberFormatException e) {
						ModManager.debugLogger.writeErrorWithException("Error parsing version numbers:", e);
					}
				} else {
					ModManager.debugLogger.writeMessage("No UIScaling GUI Library, skipping update check for it");
				}
			}

			String xbxLibPath = ModManager.getGUILibraryFor("DLC_CON_XBX", false);
			File xbxLibVersionFile = new File(xbxLibPath + "libraryversion.txt");
			ModManager.debugLogger.writeMessage("Checking for XBX GUI library version file at " + xbxLibVersionFile);
			if (xbxLibVersionFile.exists()) {
				try {
					String xbxlibverstr = FileUtils.readFileToString(xbxLibVersionFile);
					double libver = Double.parseDouble(xbxlibverstr);
					ModManager.debugLogger.writeMessage("Local XBX library version: " + libver);
					obj = serversJSON.get("latest_xbx_guilibrary");
					if (obj != null) {
						double serverver = Double.parseDouble((String) obj);
						ModManager.debugLogger.writeMessage("Server version of XBX library: " + serverver);
						if (libver < serverver) {
							ModManager.debugLogger.writeMessage("XBX Library is out of date, updating...");
							FileUtils.deleteQuietly(new File(xbxLibPath));
							publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Updating SP Controller Support GUI library"));
							String newLib = ModManager.getGUILibraryFor("DLC_CON_XBX", true);
							if (newLib != null) {
								String newlibverstr = FileUtils.readFileToString(xbxLibVersionFile);
								double newlibver = Double.parseDouble(newlibverstr);
								if (newlibver == serverver) {
									ModManager.debugLogger.writeMessage("Downloaded latest SP Controller Support GUI Library: " + newlibver);
									publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Updated SP Controller Support GUI library"));
								} else {
									ModManager.debugLogger.writeError("Failed to download proper updated GUI Library. Instead we downloaded: " + newlibver);
									publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to download new GUI Library"));
								}
							}
						} else {
							ModManager.debugLogger.writeMessage("Local XBX library is up to date");
						}
					}
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error reading local settings:", e);
				} catch (NumberFormatException e) {
					ModManager.debugLogger.writeErrorWithException("Error parsing version numbers:", e);
				}
			} else {
				ModManager.debugLogger.writeMessage("No XBX GUI Library, skipping update check for it");
			}
		}

	}

	class SingleModUpdateCheckThread extends SwingWorker<Void, Object> {
		Mod mod;

		public SingleModUpdateCheckThread(Mod mod) {
			this.mod = mod;
			labelStatus.setText("Checking for " + mod.getModName() + " updates");
		}

		@Override
		public Void doInBackground() {
			ArrayList<Mod> modsToValidate = new ArrayList<Mod>();
			modsToValidate.add(mod);
			ArrayList<UpdatePackage> upackages = ModXMLTools.validateLatestAgainstServer(modsToValidate, null);
			if (upackages != null && upackages.size() > 0) {
				publish("Update available for " + mod.getModName());
				publish(upackages.get(0)); // single update
			} else {
				publish(mod.getModName() + " is up to date");
			}
			return null;
		}

		@Override
		protected void process(List<Object> chunks) {
			for (Object latest : chunks) {
				if (latest instanceof String) {
					String update = (String) latest;
					labelStatus.setText(update);
				}
				if (latest instanceof UpdatePackage) {
					UpdatePackage upackage = (UpdatePackage) latest;
					if (upackage.requiresSideload()) {
						JOptionPane.showMessageDialog(ModManagerWindow.this,
								upackage.getMod().getModName()
										+ " has an update available from ME3Tweaks, but requires a sideloaded update first.\nAfter this dialog is closed, a browser window will open where you can download this sideload update.\nDrag and drop this downloaded file onto Mod Manager to install it.\nAfter the sideloaded update is complete, Mod Manager will download the rest of the update.\n\nThis is to save on bandwidth costs for both ME3Tweaks and the developer of "
										+ upackage.getMod().getModName() + ".",
								"Sideload update required", JOptionPane.WARNING_MESSAGE);
						try {
							ModManager.openWebpage(new URL(upackage.getSideloadURL()));
						} catch (MalformedURLException e) {
							ModManager.debugLogger.writeError("Invalid sideload URL: " + upackage.getSideloadURL());
							JOptionPane.showMessageDialog(ModManagerWindow.this,
									upackage.getMod().getModName() + " specified an invalid URL for it's sideload upload:\n" + upackage.getSideloadURL(), "Invalid Sideload URL",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						String updatetext = mod.getModName() + " has an update available from ME3Tweaks:\n";
						updatetext += AllModsUpdateWindow.getVersionUpdateString(upackage);
						if (upackage.getChangeLog() != null || !upackage.getChangeLog().equals("")) {
							updatetext += "\n - ";
							updatetext += upackage.getChangeLog();
						}
						updatetext += "Update this mod?";
						int result = JOptionPane.showConfirmDialog(ModManagerWindow.this, updatetext, "Mod update available", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							ModManager.debugLogger.writeMessage("Starting manual single-mod updater");
							ModUpdateWindow muw = new ModUpdateWindow(upackage);
							muw.startUpdate(ModManagerWindow.this);
						}
					}
				}
			}
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Exception in the single mod update thread: ", e);
			}
		}

	}

	/**
	 * Checks if the keybindings override file exists.
	 * 
	 * @return true if exists, false otherwise
	 */
	private boolean checkForKeybindsOverride() {
		File bioinputxml = new File(ModManager.getOverrideDir() + "bioinput.xml");
		return bioinputxml.exists();
	}

	private void setupWindow() {
		this.setTitle("Mass Effect 3 Mod Manager");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);
		Dimension minSize = new Dimension(560, 520);
		this.setPreferredSize(minSize);
		this.setMinimumSize(minSize);

		PRELOADED_BIOGAME_DIR = getInitialBiogameDirText();

		// Load mods first
		ModManager.debugLogger.writeMessage("Loading mods...");
		ModList ml = ModManager.getModsFromDirectory();
		ArrayList<Mod> validMods = ml.getValidMods();
		invalidMods = ml.getInvalidMods();
		ModManager.debugLogger.writeMessage("Mods have loaded");

		// Menubar
		menuBar = makeMenu();
		ModManager.debugLogger.writeMessage("Menu system has initialized.");
		// Main Panel
		JPanel contentPanel = new JPanel(new BorderLayout());
		new FileDrop(contentPanel, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				// only works with first file
				if (validateBIOGameDir()) {
					if (files.length > 0 && files[0].exists()) {
						ModManager.debugLogger.writeMessage("File was dropped onto Mod Manager Window: " + files[0]);

						if (files[0].isDirectory()) {
							// prompt
							new FolderBatchWindow(ModManagerWindow.this, files[0]);
						}
						if (files[0].isFile()) {
							String extension = FilenameUtils.getExtension(files[0].toString()).toLowerCase();
							switch (extension) {
							case "7z":
							case "zip":
							case "rar":
								new ModImportArchiveWindow(ModManagerWindow.this, files[0].toString());
								break;
							case "pcc":
							case "asi":
								int exebuild = ModManager.checkforME3105(fieldBiogameDir.getText());
								if (exebuild != 5) {
									labelStatus.setText("ASI mods don't work with Mass Effect 3 1.0" + exebuild);
									break;
								}
								if (!ModManager.checkIfASIBinkBypassIsInstalled(fieldBiogameDir.getText())) {
									labelStatus.setText("Binkw32 ASI loader not installed");
									break;
								}
							case "xml":
								new FolderBatchWindow(ModManagerWindow.this, files[0]);
								break;
							case "dlc":
								new MountFileEditorWindow(files[0].toString());
								break;
							case "tlk":
								TLKTool.decompileTLK(files[0]);
								break;

							case "bin":
								// read magic at beginning to find out what type of
								// file this is
								try {
									byte[] buffer = new byte[4];
									InputStream is = new FileInputStream(files[0]);
									if (is.read(buffer) != buffer.length) {
										// do something
									}
									int magic = ResourceUtils.byteArrayToInt(buffer);
									switch (magic) {
									case ModManager.COALESCED_MAGIC_NUMBER:
										new CoalescedWindow(files[0], false);
										break;
									}

									is.close();

									/*
									 * switch (magic) { default:
									 * System.out.println("uh"); }
									 */
								} catch (IOException e) {
									e.printStackTrace();
									// this shouldn't be possible
								} finally {

								}
								break;
							default:
								labelStatus.setText("Extension not supported for Drag and Drop: " + extension);
								break;
							}
						}
					}
				} else {
					labelStatus.setText("Drag and Drop requires a valid BioGame directory");
					labelStatus.setVisible(true);
					JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory to use drag and drop features.",
							"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// North Panel
		JPanel northPanel = new JPanel(new BorderLayout());

		// Title Panel
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel("Mass Effect 3 Mod Manager " + ModManager.VERSION + (ModManager.IS_DEBUG ? " [DEBUG MODE]" : ""), SwingConstants.LEFT), BorderLayout.WEST);

		// BioGameDir Panel
		cookedDirPanel = new JPanel(new BorderLayout());
		TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mass Effect 3 BIOGame Directory");
		fieldBiogameDir = new JTextField();
		fieldBiogameDir.setText(PRELOADED_BIOGAME_DIR);
		fieldBiogameDir.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				validateBIOGameDir();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		buttonBioGameDir = new JButton("Browse...");
		buttonBioGameDir.setToolTipText(
				"<html>Browse and set the BIOGame directory.<br>This is located in the installation directory for Mass Effect 3.<br>Typically this is in the Origin Games folder.</html>");
		fieldBiogameDir.setColumns(37);
		buttonBioGameDir.setPreferredSize(new Dimension(90, 14));

		buttonBioGameDir.addActionListener(this);
		cookedDirPanel.setBorder(cookedDirTitle);
		cookedDirPanel.add(fieldBiogameDir, BorderLayout.CENTER);
		cookedDirPanel.add(buttonBioGameDir, BorderLayout.EAST);

		northPanel.add(titlePanel, BorderLayout.NORTH);
		northPanel.add(cookedDirPanel, BorderLayout.CENTER);

		ModManager.debugLogger.writeMessage("Preparing ModList UI");
		// ModsList
		JPanel modsListPanel = new JPanel(new BorderLayout());
		// JLabel availableModsLabel = new JLabel(" Available Mods:");
		TitledBorder modListsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Available Mods");

		modList = new JList<Mod>();
		modList.addListSelectionListener(this);
		modList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(modList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		modlistFailedIndicatorLink = new JButton();
		modlistFailedIndicatorLink
				.setText("<HTML><font color=#ff2020><u>" + invalidMods.size() + " mod" + (invalidMods.size() != 1 ? "s" : "") + " failed to load</u></font></HTML>");
		modlistFailedIndicatorLink.setBorderPainted(false);
		modlistFailedIndicatorLink.setBackground(UIManager.getColor("Panel.background"));
		modlistFailedIndicatorLink.setFocusPainted(false);
		modlistFailedIndicatorLink.setMargin(new Insets(0, 0, 0, 0));
		modlistFailedIndicatorLink.setContentAreaFilled(false);
		modlistFailedIndicatorLink.setBorderPainted(false);
		modlistFailedIndicatorLink.setOpaque(false);
		modlistFailedIndicatorLink.setVisible(false);
		modlistFailedIndicatorLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		modlistFailedIndicatorLink.addActionListener(this);
		modlistFailedIndicatorLink.setVisible(invalidMods.size() > 0);

		// modsListPanel.add(availableModsLabel, BorderLayout.NORTH);
		modsListPanel.setBorder(modListsBorder);
		modsListPanel.add(listScroller, BorderLayout.CENTER);
		modsListPanel.add(modlistFailedIndicatorLink, BorderLayout.SOUTH);
		modModel = new DefaultListModel<Mod>();
		modList.setModel(modModel);

		for (Mod mod : validMods) {
			modModel.addElement(mod);
		}
		ModManager.debugLogger.writeMessage("Populated the mod list model");

		// load patches
		ModManager.debugLogger.writeMessage("Loading mixins");

		setPatchList(ModManager.getPatchesFromDirectory());
		ModManager.debugLogger.writeMessage("Mixins have loaded.");

		// DescriptionField
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		// JLabel descriptionLabel = new JLabel("Mod Description:");
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description");
		descriptionPanel.setBorder(descriptionBorder);
		fieldDescription = new JTextArea(selectAModDescription);
		fieldDescription.setDropTarget(null);
		scrollDescription = new JScrollPane(fieldDescription);

		fieldDescription.setLineWrap(true);
		fieldDescription.setWrapStyleWord(true);

		fieldDescription.setEditable(false);
		scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		modWebsiteLink = new JButton();
		modWebsiteLink.setText("<HTML>You fool! You didn't code this to show anything!</HTML>");
		modWebsiteLink.setBorderPainted(false);
		modWebsiteLink.setBackground(UIManager.getColor("Panel.background"));
		modWebsiteLink.setFocusPainted(false);
		modWebsiteLink.setMargin(new Insets(0, 0, 0, 0));
		modWebsiteLink.setContentAreaFilled(false);
		modWebsiteLink.setBorderPainted(false);
		modWebsiteLink.setOpaque(false);
		modWebsiteLink.setVisible(false);
		modWebsiteLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		modWebsiteLink.addActionListener(this);

		// descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(scrollDescription, BorderLayout.CENTER);
		descriptionPanel.add(modWebsiteLink, BorderLayout.SOUTH);

		fieldDescription.setCaretPosition(0);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modsListPanel, descriptionPanel);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(185);

		// SouthPanel
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel applyPanel = new JPanel(new BorderLayout());

		// ApplyPanel
		labelStatus = new JLabel("Loaded mods");
		labelStatus.setVisible(true);

		// ProgressBar

		// ButtonPanel
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonApplyMod = new JButton("Apply Mod");
		updateApplyButton();
		buttonApplyMod.addActionListener(this);
		buttonApplyMod.setEnabled(false);

		if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
			buttonApplyMod.setToolTipText("Select a mod on the left");
		} else {
			buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.5 or higher in order to install mods");
		}
		buttonStartGame = new JButton("Start Game");
		buttonStartGame.addActionListener(this);
		buttonStartGame.setToolTipText(
				"<html>Starts the game.<br>If LauncherWV DLC bypass is installed, it will launch instead to patch out the DLC verifiction test.<br>The game will then start.</html>");

		buttonPanel.add(buttonApplyMod);
		buttonPanel.add(buttonStartGame);
		applyPanel.add(labelStatus, BorderLayout.WEST);
		applyPanel.add(buttonPanel, BorderLayout.EAST);

		southPanel.add(applyPanel, BorderLayout.SOUTH);

		// add all panels
		contentPanel.add(northPanel, BorderLayout.NORTH);
		contentPanel.add(splitPane, BorderLayout.CENTER);
		contentPanel.add(southPanel, BorderLayout.SOUTH);
		this.setJMenuBar(menuBar);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.add(contentPanel);
		ModManager.debugLogger.writeMessage("Mod Manager GUI: SetupWindow() has completed.");

	}

	protected void updateApplyButton() {
		if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
			buttonApplyMod.setText("Apply Mod");
			if (modList.getSelectedIndex() == -1) {
				buttonApplyMod.setToolTipText("Select a mod on the left");
			} else {
				buttonApplyMod.setToolTipText(
						"<html>Apply this mod to the game.<br>If another mod is already installed, restore your game first!<br>You can merge Mod Manager mods in the Tools menu.</html>");

			}
		} else {
			buttonApplyMod.setText(".NET Missing");
			buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.5 or higher in order to install mods");
		}
	}

	private JMenuBar makeMenu() {
		menuBar = new JMenuBar();
		// Actions
		actionMenu = new JMenu("Actions");
		actionExitDebugMode = new JMenuItem("Restart in normal mode");
		actionExitDebugMode.setToolTipText("Restarts Mod Manager in standard mode");
		actionExitDebugMode.addActionListener(this);

		actionModMaker = new JMenuItem("Create a mod on ModMaker");
		actionModMaker.setToolTipText("Opens ME3Tweaks ModMaker");
		actionVisitMe = new JMenuItem("Open ME3Tweaks.com");
		actionVisitMe.setToolTipText("Opens ME3Tweaks.com");

		actionCheckForContentUpdates = new JMenuItem("Check for content updates");
		actionCheckForContentUpdates.setToolTipText("Checks for Mod Manager content updates such as MixIns and ASI mods");

		JMenu actionImportMenu = new JMenu("Import mod");
		modManagementImportAlreadyInstalled = new JMenuItem("Import installed Custom DLC mod");
		modManagementImportAlreadyInstalled.setToolTipText("<html>Import an already installed DLC mod.<br>You can then manage that mod with Mod Manager.</html>");
		modManagementImportFromArchive = new JMenuItem("Import mod from .7z/.rar/.zip");
		modManagementImportFromArchive.setToolTipText(
				"<html>Import a mod that has been packaged for importing into Mod Manager.<br>For directions on how to make mods in this format, please see the Mod Manager moddesc page.</html>");

		actionOptions = new JMenuItem("Options");
		actionOptions.setToolTipText("Configure Mod Manager Options");

		toolME3Explorer = new JMenuItem("ME3Explorer");
		toolME3Explorer.setToolTipText("Runs the bundled ME3Explorer program");
		toolTankmasterCoalFolder = new JMenuItem("TankMaster Coalesce Folder");
		toolTankmasterCoalFolder.setToolTipText("Opens Tankmaster Coalesce folder");
		toolTankmasterCoalUI = new JMenuItem("TankMaster Coalesce Interface");
		toolTankmasterCoalUI.setToolTipText("Opens interface for Tankmaster's Coalesced compiler");
		toolTankmasterTLK = new JMenuItem("TankMaster ME2/ME3 TLK Tool");
		toolTankmasterTLK.setToolTipText("Runs the bundled TLK tool provided by TankMaster");
		toolsAutoTOCGame = new JMenuItem("Run AutoTOC on game");
		toolsAutoTOCGame.setToolTipText("<html>Updates TOC files for basegame, DLC (unpacked and modified SFAR), and custom DLC.<br>May help fix infinite loading issues</html>");

		actionReload = new JMenuItem("Reload Mod Manager");
		actionReload.setToolTipText("Reloads Mod Manager to refresh mods, mixins, and help documentation");
		actionExit = new JMenuItem("Exit");
		actionExit.setToolTipText("Closes Mod Manager");

		if (ModManager.IS_DEBUG) {
			actionMenu.add(actionExitDebugMode);
		}
		actionMenu.add(actionModMaker);
		actionMenu.add(actionVisitMe);
		actionMenu.add(actionOptions);
		actionMenu.addSeparator();
		actionMenu.add(actionCheckForContentUpdates);
		actionMenu.add(actionReload);
		actionMenu.add(actionExit);

		actionModMaker.addActionListener(this);
		actionVisitMe.addActionListener(this);
		actionOptions.addActionListener(this);
		actionCheckForContentUpdates.addActionListener(this);
		actionReload.addActionListener(this);
		actionExit.addActionListener(this);
		menuBar.add(actionMenu);

		// MOD MANAGEMENT
		modManagementMenu = new JMenu("Mod Management");
		modManagementModMaker = new JMenuItem("Download ME3Tweaks ModMaker Mod");
		modManagementModMaker.setToolTipText("Allows you to download and compile ME3Tweaks ModMaker mods");
		modManagementASI = new JMenuItem("ASI Mod Manager");
		modManagementASI.setToolTipText("Manage installed ASI mods that can modify the MassEffect3.exe process while running");
		modManagementConflictDetector = new JMenuItem("Custom DLC Conflict Detector");
		modManagementConflictDetector.setToolTipText("Scans installed custom DLC for file conflicts that may prevent them from working correctly");
		modManagementCheckallmodsforupdate = new JMenuItem("Check eligible mods for updates");
		modManagementCheckallmodsforupdate.setToolTipText("Checks eligible mods for updates on ME3Tweaks and prompts to download an update if one is available");
		modManagementPatchLibary = new JMenuItem("MixIn Library");
		modManagementPatchLibary.setToolTipText("Add premade mixins to mods using patches in your patch library");
		modManagementOpenModsFolder = new JMenuItem("Open mods/ folder");
		modManagementOpenModsFolder.setToolTipText("Opens the mods/ folder so you can inspect and add/remove mods");
		modManagementClearPatchLibraryCache = new JMenuItem("Clear MixIn cache");
		modManagementClearPatchLibraryCache.setToolTipText(
				"<html>Clears the decompressed MixIn library cache.<br>This will make Mod Manager fetch the original files again as MixIns require them.<br>Use this if MixIns are having issues.</html>");

		int numFailedMods = getInvalidMods().size();
		modManagementFailedMods = new JMenuItem(numFailedMods + " mod" + (numFailedMods != 1 ? "s" : "") + " failed to load");
		modManagementFailedMods.setToolTipText("See why mods failed to load");

		actionImportMenu.add(modManagementImportFromArchive);
		actionImportMenu.add(modManagementImportAlreadyInstalled);

		if (numFailedMods > 0) {
			modManagementMenu.add(modManagementFailedMods);
			modManagementMenu.add(new JSeparator());
		}
		modManagementMenu.add(actionImportMenu);
		modManagementMenu.add(modManagementModMaker);
		modManagementMenu.add(modManagementASI);
		modManagementMenu.add(modManagementConflictDetector);
		modManagementMenu.add(modManagementCheckallmodsforupdate);
		modManagementMenu.add(modManagementPatchLibary);
		modManagementMenu.add(modManagementOpenModsFolder);
		modManagementMenu.addSeparator();
		modManagementMenu.add(modManagementClearPatchLibraryCache);
		menuBar.add(modManagementMenu);

		modManagementClearPatchLibraryCache.addActionListener(this);
		modManagementOpenModsFolder.addActionListener(this);
		modManagementFailedMods.addActionListener(this);
		modManagementImportFromArchive.addActionListener(this);
		modManagementImportAlreadyInstalled.addActionListener(this);

		// MOD TOOLS
		modMenu = new JMenu("Mod Utils");
		mountMenu = new JMenu("Manage Custom DLC Mount files");
		mountMenu.setVisible(false);
		modutilsHeader = new JMenuItem("No mod selected");
		modutilsHeader.setEnabled(false);
		modutilsInstallCustomKeybinds = new JMenuItem("Install custom keybinds into this mod");
		modutilsInstallCustomKeybinds.addActionListener(this);
		// check if BioInput.xml exists.
		if (!checkForKeybindsOverride()) {
			ModManager.debugLogger.writeMessage("No keybinds file in the override directory (bioinput.xml)");
			modutilsInstallCustomKeybinds.setEnabled(false);
			modutilsInstallCustomKeybinds.setToolTipText("<html>To enable installing custom keybinds put a<br>BioInput.xml file in the data/override/ directory.</html>");
		} else {
			ModManager.debugLogger.writeMessage("Found keybinds file in the override directory (bioinput.xml)");
			modutilsInstallCustomKeybinds.setToolTipText("<html>Replace BioInput.xml in the BASEGAME Coalesced file</html>");
		}

		moddevUpdateXMLGenerator = new JMenuItem("Prepare mod for ME3Tweaks Updater Service");
		moddevUpdateXMLGenerator.setToolTipText("No mod is currently selected");
		moddevUpdateXMLGenerator.setEnabled(false);
		moddevUpdateXMLGenerator.addActionListener(this);
		modutilsInfoEditor = new JMenuItem("Edit name/description");
		modutilsInfoEditor.addActionListener(this);
		modutilsInfoEditor.setToolTipText("Rename this mod and change the description shown in the descriptions window");

		modDeltaMenu = new JMenu("0 available variants");
		modDeltaMenu.setToolTipText(
				"<html>This mod has variants that allow quick changes to the mod without shipping a full new version.<br>Variants are Coalesced patches that can make small changes like turning on motion blur.<br>See the FAQ on how to create them.</html>");
		modDeltaMenu.setVisible(false);

		modAlternatesMenu = new JMenu("No Alternate Options");
		modAlternatesMenu.setToolTipText("<html>This mod has only one installation configuration</html>");
		modAlternatesMenu.setEnabled(false);

		modNoDeltas = new JMenuItem("No included variants");
		modNoDeltas.setToolTipText("<html>Variants are Coalesced patches that can make small changes like turning on motion blur.<br>See the FAQ on how to create them.</html>");
		modNoDeltas.setEnabled(false);

		modutilsVerifyDeltas = new JMenuItem("Verify variants");
		modutilsVerifyDeltas.setToolTipText("<html>Verifies all parts of deltas are applicable to mod</html>");
		modutilsVerifyDeltas.addActionListener(this);

		modDeltaRevert = new JMenuItem("Revert to original version");
		modDeltaRevert.setToolTipText("<html>Restores the mod to the original version, without variants applied</html>");
		modDeltaRevert.addActionListener(this);

		modutilsAutoTOC = new JMenuItem("Run AutoTOC on this mod");
		modutilsAutoTOC.addActionListener(this);
		modutilsAutoTOC.setToolTipText("Automatically update all TOC files this mod uses with proper sizes to prevent crashes");

		modutilsCheckforupdate = new JMenuItem("Check for newer version (ME3Tweaks)");
		modutilsCheckforupdate.addActionListener(this);

		modutilsRestoreMod = new JMenuItem("Restore mod from ME3Tweaks");
		modutilsRestoreMod.setToolTipText("Forces mod to update (even if on latest version) which restores mod to vanilla state (from ME3Tweaks)");
		modutilsRestoreMod.addActionListener(this);
		modutilsRestoreMod.setVisible(false);

		modutilsDeleteMod = new JMenuItem("Delete mod");
		modutilsDeleteMod.addActionListener(this);
		modutilsDeleteMod.setToolTipText("Delete this mod from Mod Manager");

		modMenu.add(modutilsHeader);
		modMenu.add(modutilsCheckforupdate);
		modMenu.add(modutilsRestoreMod);
		modMenu.addSeparator();
		modMenu.add(modDeltaMenu);
		modMenu.add(modNoDeltas);
		modMenu.add(modAlternatesMenu);
		modMenu.add(mountMenu);
		modMenu.addSeparator();
		modMenu.add(modutilsInstallCustomKeybinds);
		modMenu.add(modutilsInfoEditor);
		modMenu.add(modutilsAutoTOC);
		modMenu.addSeparator();
		modMenu.add(modutilsDeleteMod);
		modMenu.setEnabled(false);
		menuBar.add(modMenu);

		// Tools
		toolsMenu = new JMenu("Tools");

		toolsMergeMod = new JMenuItem("Mod Merging Utility");
		toolsMergeMod.setToolTipText("<html>Allows you to merge Mod Manager mods together and resolve conflicts between mods<br>This tool is deprecated and may be removed in the future</html>");

		toolsOpenME3Dir = new JMenuItem("Open BIOGame directory");
		toolsOpenME3Dir.setToolTipText("Opens a Windows Explorer window in the BIOGame Directory");

		toolsInstallLauncherWV = new JMenuItem("Install LauncherWV DLC Bypass");
		toolsInstallLauncherWV.setToolTipText("<html>Installs an in-memory patcher giving you console and allowing modified DLC.<br>This does not does not modify files.<br>This bypass method has been deprecated. Use binkw32 instead</html>");
		toolsInstallBinkw32 = new JMenuItem("Install Binkw32 DLC Bypass");
		toolsInstallBinkw32.setToolTipText(
				"<html>Installs a startup patcher giving you console and allowing modified DLC.<br>This modifies your game and is erased when doing an Origin Repair</html>");
		toolsInstallBinkw32asi = new JMenuItem("Install Binkw32 DLC Bypass (ASI version)");
		toolsInstallBinkw32asi.setToolTipText(
				"<html>Installs a startup patcher giving you console and allowing modified DLC.<br>This version allows loading of advanced ASI mods that allow 3rd party code to run on your machine.<br>This modifies your game and is erased when doing an Origin Repair</html>");
		toolsUninstallBinkw32 = new JMenuItem("Uninstall Binkw32 DLC Bypass");
		toolsUninstallBinkw32.setToolTipText("<html>Removes the Binkw32.dll DLC bypass (including ASI version), reverting to the original file</html>");

		toolsUnpackDLC = new JMenuItem("DLC Unpacker");
		toolsUnpackDLC.setToolTipText("Opens the Unpack DLC window so you can unpack DLC automatically");

		toolsMountdlcEditor = new JMenuItem("Mount.dlc Editor");
		toolsMountdlcEditor.setToolTipText("Allows you to modify or create new Mount.dlc files easily");

		toolME3Config = new JMenuItem("ME3 Config Tool");
		toolME3Config.setToolTipText("<html>Opens the ME3 Configuration Utility that comes packaged with the game.<br>Lets you configure graphics and audio settings.</html>");

		toolsAutoTOCGame.addActionListener(this);
		modManagementModMaker.addActionListener(this);
		modManagementASI.addActionListener(this);
		modManagementConflictDetector.addActionListener(this);
		toolsMergeMod.addActionListener(this);
		modManagementCheckallmodsforupdate.addActionListener(this);
		toolsUnpackDLC.addActionListener(this);
		toolsInstallLauncherWV.addActionListener(this);
		modManagementPatchLibary.addActionListener(this);
		toolsMountdlcEditor.addActionListener(this);

		// DEV
		devMenu = new JMenu("Mod Development");
		modDevStarterKit = new JMenuItem("Generate a Custom DLC Starter Kit");
		modDevStarterKit.addActionListener(this);
		modDevStarterKit.setToolTipText("Generate a barebones Custom DLC mod that is immediately ready to use");
		devMenu.add(modDevStarterKit);
		devMenu.add(moddevUpdateXMLGenerator);
		devMenu.add(toolTankmasterCoalFolder);
		devMenu.add(toolTankmasterCoalUI);
		devMenu.add(toolTankmasterTLK);

		toolsOpenME3Dir.addActionListener(this);
		toolsInstallBinkw32.addActionListener(this);
		toolsInstallBinkw32asi.addActionListener(this);
		toolsUninstallBinkw32.addActionListener(this);
		toolME3Config.addActionListener(this);
		toolME3Explorer.addActionListener(this);
		toolTankmasterTLK.addActionListener(this);
		toolTankmasterCoalFolder.addActionListener(this);
		toolTankmasterCoalUI.addActionListener(this);
		toolsMenu.add(toolsMergeMod);
		toolsMenu.add(toolsMountdlcEditor);
		// toolsMenu.add(toolGUITransplant);
		toolsMenu.add(toolsUnpackDLC);
		toolsMenu.add(toolsAutoTOCGame);
		toolsMenu.add(toolsOpenME3Dir);
		toolsMenu.addSeparator();
		toolsMenu.add(toolME3Config);
		toolsMenu.add(toolME3Explorer);
		toolsMenu.add(devMenu);
		toolsMenu.addSeparator();
		toolsMenu.add(toolsInstallLauncherWV);
		toolsMenu.add(toolsInstallBinkw32);
		toolsMenu.add(toolsInstallBinkw32asi);
		toolsMenu.add(toolsUninstallBinkw32);
		menuBar.add(toolsMenu);

		// BACKUP
		backupMenu = new JMenu("Backup");

		backupBackupDLC = new JMenuItem("Backup DLCs");
		backupBackupDLC.setToolTipText("Backs up your DLC to .bak files. When installing a mod it will ask if a .bak files does not exist if you want to backup");

		backupBasegameUnpacked = new JMenuItem("Backup basegame/unpacked files");
		backupBasegameUnpacked.setToolTipText("An Unpacked and basegame file will be automatically backed up when Mod Manager replaces or removes that file");

		backupCreateGDB = new JMenuItem("Update game repair database");
		backupCreateGDB.setToolTipText("Creates/updates a database of checksums for basegame and unpacked DLC files for verifying restoring and backing up");

		backupBackupDLC.addActionListener(this);
		backupBasegameUnpacked.addActionListener(this);
		backupCreateGDB.addActionListener(this);

		backupMenu.add(backupBackupDLC);
		backupMenu.add(backupBasegameUnpacked);
		backupMenu.add(backupCreateGDB);
		menuBar.add(backupMenu);

		// RESTORE
		restoreMenu = new JMenu("Restore");
		restoreMenuAdvanced = new JMenu("Advanced Restore");

		restoreSelective = new JMenuItem("Custom Restore");
		restoreSelective.setToolTipText("Allows you to restore specific basegame, DLC, and unpacked files");

		restoreRevertEverything = new JMenuItem("Restore everything");
		restoreRevertEverything.setToolTipText(
				"<html>Restores all basegame files, deletes unpacked DLC files, and restores all SFAR files.<br>This will delete any non standard DLC folders.</html>");

		restoreDeleteUnpacked = new JMenuItem("Delete all unpacked DLC files");
		restoreDeleteUnpacked
				.setToolTipText("<html>Deletes unpacked DLC files, leaving PCConsoleTOC,<br>.sfar and .bak files in the DLC folders.<br>Does not modify Custom DLC.</html>");

		restoreRevertBasegame = new JMenuItem("Restore basegame files");
		restoreRevertBasegame.setToolTipText("<html>Restores all basegame files that have been modified by installing mods</html>");

		restoredeleteAllCustomDLC = new JMenuItem("Remove all Custom DLC");
		restoredeleteAllCustomDLC.setToolTipText("<html>Deletes all non standard DLC folders in the DLC directory</html>");

		restoreCustomDLCManager = new JMenuItem("Custom DLC Manager");
		restoreCustomDLCManager.setToolTipText("<html>Allows selectively removing Custom DLC modules and indicates if MP is affected</html>");

		restoreRevertBasegame = new JMenuItem("Restore basegame files");
		restoreRevertBasegame.setToolTipText("<html>Restores all basegame files that have been modified by installing mods</html>");

		restoreRevertUnpacked = new JMenuItem("Restore unpacked DLC files");
		restoreRevertUnpacked.setToolTipText("<html>Restores all unpacked DLC files that have been modified by installing mods</html>");

		restoreRevertBasegameUnpacked = new JMenuItem("Restore basegame + unpacked files");
		restoreRevertBasegameUnpacked.setToolTipText("<html>Restores all basegame and unpacked DLC files that have been modified by installing mods</html>");

		restoreVanillifyDLC = new JMenuItem("Vanillify game DLC");
		restoreVanillifyDLC.setToolTipText("<html>Removes custom DLC, deletes unpacked files, restores SFARs.</html>");

		restoreRevertAllDLC = new JMenuItem("Restore all DLC SFARs");
		restoreRevertAllDLC.setToolTipText("<html>Restores all DLC SFAR files.<br>This does not remove custom DLC modules.</html>");

		restoreRevertSPDLC = new JMenuItem("Restore SP DLC SFARs");
		restoreRevertSPDLC.setToolTipText("<html>Restores all SP DLCs.<br>This does not remove custom DLC modules.</html>");

		restoreRevertSPBaseDLC = new JMenuItem("Restore SP DLC SFARs + Basegame");
		restoreRevertSPBaseDLC.setToolTipText("<html>Restores all basegame files, and checks all SP DLC SFAR files.<br>This does not remove custom DLC modules.</html>");

		restoreRevertMPDLC = new JMenuItem("Restore MP DLC SFARs");
		restoreRevertMPDLC.setToolTipText("<html>Restores all MP DLC SFARs.<br>This does not remove custom DLC modules.</html>");

		restoreRevertMPBaseDLC = new JMenuItem("Restore MP DLC SFARs + Basegame");
		restoreRevertMPBaseDLC.setToolTipText(
				"<html>Restores all basegame files, and checks all Multiplayer DLC files.<br>This does not remove custom DLC modules.<br>If you are doing multiplayer mods, you should use this to restore</html>");
		restoreRevertCoal = new JMenuItem("Restore vanilla Coalesced.bin");
		restoreRevertCoal.setToolTipText("<html>Restores the basegame coalesced file</html>");

		restoreSelective.addActionListener(this);
		restoreRevertEverything.addActionListener(this);
		restoreDeleteUnpacked.addActionListener(this);
		restoredeleteAllCustomDLC.addActionListener(this);
		restoreCustomDLCManager.addActionListener(this);
		restoreRevertBasegame.addActionListener(this);
		restoreRevertUnpacked.addActionListener(this);
		restoreVanillifyDLC.addActionListener(this);
		restoreRevertBasegameUnpacked.addActionListener(this);
		restoreRevertAllDLC.addActionListener(this);
		restoreRevertSPDLC.addActionListener(this);
		restoreRevertSPBaseDLC.addActionListener(this);
		restoreRevertMPDLC.addActionListener(this);
		restoreRevertMPBaseDLC.addActionListener(this);
		restoreRevertCoal.addActionListener(this);

		restoreMenuAdvanced.add(restoredeleteAllCustomDLC);
		restoreMenuAdvanced.addSeparator();

		restoreMenuAdvanced.add(restoreRevertBasegame);
		restoreMenuAdvanced.add(restoreRevertUnpacked);
		restoreMenuAdvanced.add(restoreRevertBasegameUnpacked);
		restoreMenuAdvanced.add(restoreDeleteUnpacked);
		restoreMenuAdvanced.addSeparator();

		restoreMenuAdvanced.add(restoreVanillifyDLC);
		restoreMenuAdvanced.add(restoreRevertAllDLC);
		restoreMenuAdvanced.add(restoreRevertSPDLC);
		restoreMenuAdvanced.add(restoreRevertMPDLC);

		restoreMenu.add(restoreSelective);
		restoreMenu.add(restoreCustomDLCManager);
		restoreMenu.addSeparator();

		restoreMenu.add(restoreRevertEverything);
		restoreMenu.add(restoreRevertMPBaseDLC);
		restoreMenu.add(restoreRevertSPBaseDLC);
		restoreMenu.addSeparator();
		restoreMenu.add(restoreMenuAdvanced);
		menuBar.add(restoreMenu);

		// DEBUG ONLY - MODMAKER SQL
		sqlMenu = new JMenu("ModMaker SQL");
		sqlWavelistParser = new JMenuItem("Wavelist Parser");
		sqlDifficultyParser = new JMenuItem("Biodifficulty Parser");
		sqlAIWeaponParser = new JMenuItem("BioAI Parser");
		sqlPowerCustomActionParser = new JMenuItem("CustomAction Parser");
		sqlPowerCustomActionParser2 = new JMenuItem("CustomAction Editor");
		sqlConsumableParser = new JMenuItem("Consumable Parser");
		sqlGearParser = new JMenuItem("Gear Parser");

		sqlWavelistParser.addActionListener(this);
		sqlDifficultyParser.addActionListener(this);
		sqlAIWeaponParser.addActionListener(this);
		sqlPowerCustomActionParser.addActionListener(this);
		sqlPowerCustomActionParser2.addActionListener(this);
		sqlConsumableParser.addActionListener(this);
		sqlGearParser.addActionListener(this);

		sqlMenu.add(sqlWavelistParser);
		sqlMenu.add(sqlDifficultyParser);
		sqlMenu.add(sqlAIWeaponParser);
		sqlMenu.add(sqlPowerCustomActionParser);
		sqlMenu.add(sqlPowerCustomActionParser2);
		sqlMenu.add(sqlConsumableParser);
		sqlMenu.add(sqlGearParser);
		if (ModManager.IS_DEBUG) {
			devMenu.add(sqlMenu);
		}

		helpMenu = HelpMenu.constructHelpMenu();
		menuBar.add(helpMenu);

		return menuBar;
	}

	/*
	 * private void verifyBackupCoalesced() { File restoreTest = new
	 * File(ModManager.getDataDir() + "Coalesced.original"); if
	 * (!restoreTest.exists()) { ModManager.debugLogger.writeMessage(
	 * "Didn't find Coalesced.original - checking existing installed one, will copy if verified."
	 * ); // try to copy the current one String patch3CoalescedHash =
	 * "540053c7f6eed78d92099cf37f239e8e"; File coalesced = new
	 * File(ModManager.appendSlash(fieldBiogameDir.getText().toString()) +
	 * "CookedPCConsole\\Coalesced.bin"); // Take the MD5 first to verify it. if
	 * (coalesced.exists()) { try { if
	 * (patch3CoalescedHash.equals(MD5Checksum.getMD5Checksum
	 * (coalesced.toString()))) { // back it up Files.copy(coalesced.toPath(),
	 * restoreTest.toPath()); restoreRevertCoal.setEnabled(true);
	 * ModManager.debugLogger.writeMessage("Backed up Coalesced."); } else {
	 * ModManager .debugLogger.writeMessage(
	 * "Didn't back up coalecsed, hash mismatch.");
	 * restoreRevertCoal.setEnabled(false); } } catch (IOException e) { // TODO
	 * Auto-generated catch block
	 * ModManager.debugLogger.writeErrorWithException(
	 * "I/O Exception while verifying backup coalesced.", e); } catch (Exception
	 * e) { // TODO Auto-generated catch block
	 * ModManager.debugLogger.writeErrorWithException (
	 * "General Exception while verifying backup coalesced.", e); } } else {
	 * ModManager.debugLogger.writeMessage(
	 * "Coalesced.bin was not found - unable to back up automatically"); } } }
	 */

	public void actionPerformed(ActionEvent e) {
		// too bad we can't do a switch statement on the object :(
		if (e.getSource() == buttonBioGameDir) {
			JFileChooser dirChooser = new JFileChooser();
			File tryDir = new File(fieldBiogameDir.getText());
			if (tryDir.exists()) {
				dirChooser.setCurrentDirectory(new File(fieldBiogameDir.getText()));
			} else {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Directory " + fieldBiogameDir.getText() + " does not exist, defaulting to working directory.");
				}
				dirChooser.setCurrentDirectory(new java.io.File("."));
			}
			dirChooser.setDialogTitle("Select BIOGame directory");
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			//
			// disable the "All files" option.
			//
			dirChooser.setAcceptAllFileFilterUsed(false);
			//
			if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				checkForValidBioGame(dirChooser);
			} else {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("No directory selected...");
				}
			}
		} else if (e.getSource() == modManagementModMaker) {
			if (validateBIOGameDir()) {
				if (ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Opening ModMaker Entry Window");
					updateApplyButton();
					new ModMakerEntryWindow(this, fieldBiogameDir.getText());
				} else {

					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("ModMaker: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to use ModMaker.");
				}
			} else {
				labelStatus.setText("ModMaker requires valid BIOGame directory to start");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);

			}
		} else if (e.getSource() == modManagementASI) {
			if (validateBIOGameDir()) {
				int exebuild = ModManager.checkforME3105(fieldBiogameDir.getText());
				if (exebuild != 5) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
							"ASI mods don't work with any version of Mass Effect 3 except 1.05.\n"
									+ (exebuild == 6 ? "Downgrade to Mass Effect 3 1.05 to use them.\nThe ME3Tweaks forums has instructions on how to do this."
											: "Upgrade your game to use 1.05. Pirated editions of the game are not supported."),
							"Unsupported ME3 version", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ModManager.debugLogger.writeMessage("Opening ASI Management Window");
				updateApplyButton();
				if (ModManager.checkIfASIBinkBypassIsInstalled(fieldBiogameDir.getText()) == false) {
					JOptionPane.showMessageDialog(null,
							"ASI loader not installed.\nASI mods won't load without using the ASI version of binkw32.\nYou can install this from the tools menu or the ASI Mod Management window.",
							"ASI loader not installed", JOptionPane.WARNING_MESSAGE);
				}

				new ASIModWindow(new File(fieldBiogameDir.getText()).getParent());
			} else {
				updateApplyButton();
				labelStatus.setText("Can't manage ASI mods without valid BioGame");
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
				ModManager.debugLogger.writeMessage("Invalid biogame dir for asi management");
			}

		} else if (e.getSource() == modManagementConflictDetector) {
			if (validateBIOGameDir()) {
				if (ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Opening Custom DLC Conflict Detection Window");
					updateApplyButton();
					new CustomDLCConflictWindow();
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeError("Custom DLC Conflict Window: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to fully use the conflict detection tool.");
				}
			} else {
				labelStatus.setText("Conflict detector requires valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modManagementFailedMods) {
			new FailedModsWindow();
		} else if (e.getSource() == backupBackupDLC) {
			if (validateBIOGameDir()) {
				backupDLC(fieldBiogameDir.getText());
			} else {
				labelStatus.setText("Backing up DLC requires valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == backupBasegameUnpacked) {
			if (validateBIOGameDir()) {
				String me3dir = (new File(fieldBiogameDir.getText())).getParent();
				String backupLocation = ModManager.appendSlash(me3dir) + "cmmbackup";
				JOptionPane.showMessageDialog(this,
						"<html><div style=\"width:330px;\">Basegame and unpacked DLC files are automatically backed up by Mod Manager when a Mod Manager mod replaces or removes that file while being applied.<br>"
								+ "The game repair database verifies the file being backed up matches the metadata in the database so it can restore back to that version later. When restoring, the backed up file is checked again to make sure it wasn't modified. Otherwise you may restore files of different sizes and the game will crash.<br>"
								+ "This is why modifications outside of Mod Manager are not backed up and are not supported. If you want to use modifications outside of Mod Manager, update the game repair database after you make your changes outside of Mod Manager. Make sure your game is in a working state before you do this or you will restore to a broken snapshot.<br><br>"
								+ "The backup files created by Mod Manager are placed in the following folder:<br>" + backupLocation
								+ "<br><br>MixIns do not support modified files except for those modified by other non-finalizing MixIns.</div></html>",
						"Backup of basegame/unpacked files", JOptionPane.INFORMATION_MESSAGE);
			} else {
				labelStatus.setText("ModMaker requires valid BIOGame directory to start");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == backupCreateGDB) {
			if (validateBIOGameDir()) {
				createBasegameDB(fieldBiogameDir.getText());
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot update or create the game repair database.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertCoal) {
			if (ModManager.isMassEffect3Running()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can restore game files.", "MassEffect3.exe is running",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (validateBIOGameDir()) {
				restoreCoalesced(fieldBiogameDir.getText());
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modDevStarterKit) {
			new StarterKitWindow();
		} else if (e.getSource() == restoreRevertAllDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.ALLDLC);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoredeleteAllCustomDLC) {
			if (validateBIOGameDir()) {
				if (JOptionPane.showConfirmDialog(this, "This will delete all folders in the BIOGame/DLC folder that aren't known to be official.\nDelete all custom DLC?",
						"Delete all Custom DLC", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

					restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.REMOVECUSTOMDLC);
				}
			} else {
				labelStatus.setText("Can't remove custom DLC with invalid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreCustomDLCManager) {
			if (validateBIOGameDir()) {
				new CustomDLCWindow(fieldBiogameDir.getText());
			} else {
				labelStatus.setText("Custom DLC Manager requires valid BIOGame directory");
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nCustom DLC Manager requires a valid directory.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreSelective) {
			if (validateBIOGameDir()) {
				new SelectiveRestoreWindow(fieldBiogameDir.getText());
			} else {
				labelStatus.setText("Custom Restore requires valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nCustom Restore requires a valid directory.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertBasegame) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.BASEGAME);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertUnpacked) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.UNPACKED);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertBasegameUnpacked) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.UNPACKEDBASEGAME);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreVanillifyDLC) {
			if (validateBIOGameDir()) {
				if (JOptionPane.showConfirmDialog(this,
						"This will delete all unpacked DLC items, including backups of those files.\nThe backup files are deleted because you shouldn't restore unpacked files if your DLC isn't set up for unpacked files.\nMake sure you have your *original* SFARs backed up! Otherwise you will have to use Origin to download them again.\nAre you sure you want to continue?",
						"Delete unpacked DLC files", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

					restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.VANILLIFYDLC);
				}
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertSPDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.SP);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}

		} else if (e.getSource() == restoreRevertMPDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.MP);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertSPBaseDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.SPBASE);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertMPBaseDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.MPBASE);
			} else {
				labelStatus.setText("Cannot restore files without valid BIOGame directory");
				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertEverything) {
			if (ModManager.isMassEffect3Running()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can restore game files.", "MassEffect3.exe is running",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (validateBIOGameDir()) {
				if (validateBIOGameDir()) {
					if (JOptionPane.showConfirmDialog(this,
							"This will delete all unpacked DLC items, including backups of those files.\nThe backup files are deleted because you shouldn't restore unpacked files if your DLC isn't set up for unpacked files.\nMake sure you have your *original* SFARs backed up! Otherwise you will have to use Origin to download them again.\nAre you sure you want to continue?",
							"Delete unpacked DLC files", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

						restoreCoalesced(fieldBiogameDir.getText());
						restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.ALL);
					}
				} else {
					labelStatus.setText("Cannot restore files without valid BIOGame directory");
					JOptionPane.showMessageDialog(null,
							"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
							"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == restoreDeleteUnpacked) {
			if (validateBIOGameDir()) {
				if (JOptionPane.showConfirmDialog(this,
						"This will delete all unpacked DLC items, including backups of those files.\nThe backup files are deleted because you shouldn't restore unpacked files if your DLC isn't set up for unpacked files.\nMake sure you have your *original* SFARs backed up! Otherwise you will have to use Origin to download them again.\nAre you sure you want to continue?",
						"Delete unpacked DLC files", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.REMOVEUNPACKEDITEMS);
				}
			} else {
				labelStatus.setText("Cannot delete files with invalid BIOGame directory");

				JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == actionExitDebugMode) {
			ModManager.IS_DEBUG = false;
			new ModManagerWindow(false);
		} else if (e.getSource() == actionModMaker) {
			URI theURI;
			try {
				theURI = new URI("http://me3tweaks.com/modmaker");
				java.awt.Desktop.getDesktop().browse(theURI);
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		} else if (e.getSource() == modWebsiteLink) {
			Mod mod = modModel.get(modList.getSelectedIndex());
			try {
				ModManager.openWebpage(new URI(mod.getModSite()));
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				ModManager.debugLogger.writeErrorWithException("Unable to open this mod's web site:", e1);
			}
		} else if (e.getSource() == modlistFailedIndicatorLink) {
			new FailedModsWindow();
		} else if (e.getSource() == modManagementImportAlreadyInstalled) {
			if (validateBIOGameDir()) {
				new ModImportDLCWindow(this, fieldBiogameDir.getText());
			}
		} else if (e.getSource() == modManagementImportFromArchive) {
			new ModImportArchiveWindow();
		}
		if (e.getSource() == actionVisitMe) {
			URI theURI;
			try {
				theURI = new URI("http://me3tweaks.com");
				java.awt.Desktop.getDesktop().browse(theURI);
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		} else if (e.getSource() == actionCheckForContentUpdates) {
			new NetworkThread(true).execute();
		} else if (e.getSource() == actionReload) {
			// Reload this jframe
			new ModManagerWindow(false);
		} else

		if (e.getSource() == actionExit) {
			ModManager.debugLogger.writeMessage("User selected exit from Actions Menu");
			System.exit(0);
		} else if (e.getSource() == buttonApplyMod) {
			if (!ModManager.isMassEffect3Running()) {
				if (validateBIOGameDir()) {
					ModManager.debugLogger.writeMessage("Applying selected mod: Biogame Dir is valid.");
					if (ModManager.validateNETFrameworkIsInstalled()) {
						updateApplyButton();
						ModManager.debugLogger.writeMessage(".NET is installed");
						applyMod();
					} else {
						updateApplyButton();
						labelStatus.setText(".NET Framework 4.5 or higher is missing");
						ModManager.debugLogger.writeMessage("Applying selected mod: .NET is not installed");
						new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to install mods.");
					}
				} else {
					labelStatus.setText("Installing a mod requires valid BIOGame path");
					labelStatus.setVisible(true);
					JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(ModManagerWindow.this, "Mass Effect 3 must be closed before you can install a mod.", "MassEffect3.exe is running",
						JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == buttonStartGame) {
			if (validateBIOGameDir()) {
				ModManager.debugLogger.writeMessage("Starting game/launcherwv.");
				startGame(ModManager.appendSlash(fieldBiogameDir.getText()));
			} else {
				labelStatus.setText("Starting the game requires a valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nMod Manager does not know where to launch the game executable.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == toolME3Explorer) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				updateApplyButton();
				ModManager.debugLogger.writeMessage(".NET is installed");
				ArrayList<String> commandBuilder = new ArrayList<String>();
				String me3expdir = ModManager.appendSlash(ModManager.getME3ExplorerEXEDirectory(true));
				commandBuilder.add(me3expdir + "ME3Explorer.exe");
				// System.out.println("Building command");
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				// Debug stuff
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing ME3Explorer (via action menu) via command: " + sb.toString());
				try {
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.directory(new File(me3expdir)); // this is where you set
														// the root folder for
														// the executable to run
														// with
					pb.start();
				} catch (IOException ex) {
					ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(ex));
				}
			} else {
				updateApplyButton();
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Run ME3Explorer: .NET is not installed");
				new NetFrameworkMissingWindow("ME3Explorer requires .NET 4.5 or higher in order to run.");
			}
		} else if (e.getSource() == toolTankmasterCoalFolder) {
			ResourceUtils.openDir(ModManager.getTankMasterCompilerDir());
		} else if (e.getSource() == modManagementOpenModsFolder) {
			ResourceUtils.openDir(ModManager.getModsDir());
		} else if (e.getSource() == modManagementClearPatchLibraryCache) {
			File libraryDir = new File(ModManager.getPatchesDir() + "source");
			if (libraryDir.exists()) {
				boolean deleted = FileUtils.deleteQuietly(libraryDir);
				labelStatus.setText("MixIn cache deleted");
				ModManager.debugLogger.writeMessage("Deleted mixin cache " + libraryDir + ": " + deleted);
			} else {
				ModManager.debugLogger.writeMessage("No mixin cache: " + libraryDir);
				labelStatus.setText("No MixIn cache to delete");
			}
		} else if (e.getSource() == toolTankmasterTLK) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				updateApplyButton();
				ArrayList<String> commandBuilder = new ArrayList<String>();
				String tankmasterTLKDir = ModManager.getTankMasterTLKDir();
				commandBuilder.add(tankmasterTLKDir + "MassEffect3.TlkEditor.exe");
				// System.out.println("Building command");
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				// Debug stuff
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing Tankmaster TLK (via action menu) via command: " + sb.toString());
				try {
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.directory(new File(tankmasterTLKDir)); // this is where
																// you set the
																// root folder
																// for the
																// executable to
																// run with
					pb.start();
				} catch (IOException ex) {
					ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(ex));
				}
			} else {
				updateApplyButton();
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Run TLK: .NET is not installed");
				new NetFrameworkMissingWindow("Tankmaster's TLK Tool requires .NET 4.5 or higher in order to run.");
			}
		} else if (e.getSource() == toolTankmasterCoalUI) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				new CoalescedWindow();
			} else {
				updateApplyButton();
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Run ME3Explorer: .NET is not installed");
				new NetFrameworkMissingWindow("ME3Explorer requires .NET 4.5 or higher in order to run.");
			}
		} else if (e.getSource() == actionOptions) {
			new OptionsWindow(this);
		} else if (e.getSource() == toolsMergeMod) {
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
					"The mod merging tool has been deprecated.\nIt is no longer tested, and may break. To merge mods, install both and then run AutoTOC on the game.\nThe final applied mod will take precedence if there are conflicts.",
					"Deprecated Tool", JOptionPane.WARNING_MESSAGE);
			if (ModManager.validateNETFrameworkIsInstalled()) {
				ModManager.debugLogger.writeMessage("Opening Mod Merging utility");
				updateApplyButton();
				new MergeModWindow(this);
			} else {
				updateApplyButton();
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Merge Tool: Missing .NET Framework");
				new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to merge mods.");
			}
		} else if (e.getSource() == toolME3Config) {
			if (validateBIOGameDir()) {
				ModManager.debugLogger.writeMessage("Opening ME3 Config tool");
				File path = new File(fieldBiogameDir.getText());
				path = path.getParentFile();
				String command = path.getAbsolutePath() + "\\Binaries\\MassEffect3Config.exe";
				if (new File(command).exists()) {
					ProcessBuilder p = new ProcessBuilder(command);
					ModManager.debugLogger.writeMessage("Launching ME3 Config tool: " + command);
					try {
						p.start();
					} catch (IOException e1) {
						ModManager.debugLogger.writeErrorWithException("Unable to launch ME3 config tool:", e1);
					}
				} else {
					ModManager.debugLogger.writeError("Config tool is missing! Not found at " + path);
					JOptionPane.showMessageDialog(null, "The config tool executable doesn't exist where it should:\n" + path, "Missing Config Tool exe", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				labelStatus.setText("ME3 Config tool requires a valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nCannot open the ME3 Config tool if Mod Manager doesn't know where Mass Effect 3 is.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == toolsUnpackDLC) {
			if (validateBIOGameDir()) {
				if (ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Opening Unpack DLC window");
					updateApplyButton();
					new UnpackWindow(this, fieldBiogameDir.getText());
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("Unpack DLC Tool: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to unpack DLC.");
				}
			} else {
				labelStatus.setText("Unpacking DLC requires a valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nCannot unpack DLC without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == toolsMountdlcEditor) {
			new MountFileEditorWindow();
		} else if (e.getSource() == toolsOpenME3Dir) {
			openME3Dir();
		} else if (e.getSource() == toolsAutoTOCGame) {
			if (validateBIOGameDir()) {
				if (ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Running Game-wide AutoTOC.");
					updateApplyButton();
					new AutoTocWindow(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("AutoTOC: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to use the AutoTOC feature.");
				}
			} else {
				labelStatus.setText("Game AutoTOC requires a valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nCannot update TOC files without a valid directory.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modutilsAutoTOC) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				ModManager.debugLogger.writeMessage("Running AutoTOC.");
				updateApplyButton();
				autoTOC(AutoTocWindow.LOCALMOD_MODE);
			} else {
				updateApplyButton();
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("AutoTOC: Missing .NET Framework");
				new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to use the AutoTOC feature.");
			}

		} else if (e.getSource() == modutilsInfoEditor) {
			showInfoEditor();
		} else if (e.getSource() == sqlWavelistParser) {
			new WavelistGUI();
		} else if (e.getSource() == modutilsCheckforupdate) {
			Mod mod = modModel.getElementAt(modList.getSelectedIndex());
			if (mod.getModMakerCode() <= 0 || validateBIOGameDir()) {
				if (mod.getModMakerCode() <= 0 || ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Running single mod update check on " + mod.getModName());
					new SingleModUpdateCheckThread(mod).execute();
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("Single mode updater: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 to update ModMaker mods.");
				}
			} else {
				labelStatus.setText("Updating ModMaker mods requires valid BIOGame");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nCannot update ModMaker mods without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modutilsRestoreMod) {

			Mod mod = modModel.getElementAt(modList.getSelectedIndex());
			if (mod.getModMakerCode() <= 0 || validateBIOGameDir()) {
				if (mod.getModMakerCode() <= 0 || ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Running (restore mode) single mod update check on " + mod.getModName());
					mod = new Mod(mod); // create clone
					mod.setVersion(0.001);
					new SingleModUpdateCheckThread(mod).execute();
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("Single mode updater: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 to update ModMaker mods.");
				}
			} else {
				labelStatus.setText("Updating ModMaker mods requires valid BIOGame");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null,
						"The BIOGame directory is not valid.\nCannot update ModMaker mods without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modutilsDeleteMod) {
			ModManager.debugLogger.writeMessage("User clicked Delete Mod on " + modModel.get(modList.getSelectedIndex()).getModName());
			int result = JOptionPane.showConfirmDialog(this, "Deleting this mod will remove it from your filesystem.\nThis operation cannot be reversed.\nDelete "
					+ modModel.get(modList.getSelectedIndex()).getModName() + "?", "Confirm Mod Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				ModManager.debugLogger.writeMessage("Deleting mod: " + modModel.get(modList.getSelectedIndex()).getModPath());
				FileUtils.deleteQuietly(new File(modModel.get(modList.getSelectedIndex()).getModPath()));
				new ModManagerWindow(false);
			}
		} else if (e.getSource() == modutilsInstallCustomKeybinds) {
			new KeybindsInjectionWindow(this, modModel.getElementAt(modList.getSelectedIndex()), false);
		} else if (e.getSource() == modManagementCheckallmodsforupdate) {
			if (!validateBIOGameDir()) {
				JOptionPane.showMessageDialog(this, "Your BIOGame directory is not correctly set.\nOnly non-ModMaker mods will be checked for updates.",
						"Invalid BIOGame Directory", JOptionPane.WARNING_MESSAGE);
			}
			checkAllModsForUpdates(true);
		} else if (e.getSource() == modManagementPatchLibary) {
			if (validateBIOGameDir()) {
				if (ModManager.validateNETFrameworkIsInstalled()) {
					ModManager.debugLogger.writeMessage("Opening patch library window.");
					updateApplyButton();
					new PatchLibraryWindow(PatchLibraryWindow.MANUAL_MODE);
				} else {
					updateApplyButton();
					labelStatus.setText(".NET Framework 4.5 or higher is missing");
					ModManager.debugLogger.writeMessage("Patch Library: Missing .NET Framework");
					new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher in order to use MixIns.");
				}
			} else {
				labelStatus.setText("Use of the patch library requires a valid BIOGame folder");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == modDeltaRevert) {
			// if (validateBIOGameDir()) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				ModManager.debugLogger.writeMessage("Reverting a delta.");
				new DeltaWindow(modModel.get(modList.getSelectedIndex()));
			} else {
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Revert Delta: Missing .NET Framework");
				new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher to switch mod variants.");
			}
		} else if (e.getSource() == modutilsVerifyDeltas) {
			if (ModManager.validateNETFrameworkIsInstalled()) {
				ModManager.debugLogger.writeMessage("Verifying deltas");
				Mod mod = modModel.get(modList.getSelectedIndex());
				for (ModDelta delta : mod.getModDeltas()) {
					new DeltaWindow(mod, delta, true, false);
				}
			} else {
				labelStatus.setText(".NET Framework 4.5 or higher is missing");
				ModManager.debugLogger.writeMessage("Patch Library: Missing .NET Framework");
				new NetFrameworkMissingWindow("You must install .NET Framework 4.5 or higher to switch mod variants.");
			}
		} else if (e.getSource() == moddevUpdateXMLGenerator) {
			ModManager.debugLogger.writeMessage("Generating Manifest...");
			ModXMLTools.generateXMLFileList(modModel.getElementAt(modList.getSelectedIndex()));
		} else if (e.getSource() == sqlDifficultyParser) {
			new DifficultyGUI();
		} else

		if (e.getSource() == sqlAIWeaponParser) {
			new BioAIGUI();
		} else

		if (e.getSource() == sqlPowerCustomActionParser) {
			new PowerCustomActionGUI();
		} else if (e.getSource() == sqlPowerCustomActionParser2) {
			new PowerCustomActionGUI2();
		} else if (e.getSource() == sqlConsumableParser) {
			new ConsumableGUI();
		} else if (e.getSource() == sqlGearParser) {
			// new GearGUI();
		} else if (e.getSource() == toolsInstallLauncherWV) {
			int result = JOptionPane.showConfirmDialog(ModManagerWindow.ACTIVE_WINDOW,
					"LauncherWV has been deprecated.\nYou can install install it, but using the binkw32 bypass methods are far more reliable.\nContinue installing LauncherWV?",
					"Deprecated Bypass", JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				if (validateBIOGameDir()) {
					ModManager.debugLogger.writeMessage("Installing manual LauncherWV bypass.");
					installBypass();
				} else {
					labelStatus.setText("Installing DLC bypass requires valid BIOGame directory");
					labelStatus.setVisible(true);
					JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == toolsInstallBinkw32asi) {
			if (ModManager.isMassEffect3Running()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can install binkw32 ASI DLC bypass.",
						"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (validateBIOGameDir()) {
				if (validateVC2012()) {
					int result = JOptionPane.showConfirmDialog(ModManagerWindow.this,
							"<html><div style='width: 300px'>Installing the ASI version of binkw32.dll bypass will load .asi files and run 3rd party code. Any .asi file in the same folder as MassEffect3.exe and within a subfolder named asi will be loaded at game startup. The code in these asi files will then be run like any program on your computer.<br><br>Ensure you trust the developer you download and install ASI mods from.<br><br>If you have no idea what this means, you should use the default non-asi binkw32.dll bypass option.<br><br>Install the ASI version of binkw32 bypass?</div></html>",
							"Potential security risk", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						ModManager.debugLogger.writeMessage("Installing manual Binkw32 (ASI) bypass.");
						installBinkw32Bypass(true);
					}
				} else {
					labelStatus.setText("Binkw32 ASI Bypass requires Visual C++ 2012 x86");
					labelStatus.setVisible(true);
					JOptionPane.showMessageDialog(null,
							"The Binkw32 ASI Bypass requires Visual C++ 2012 x86 redistributable.\nIf you are sure you have this installed, turn off the .NET version check in Mod Manager options.",
							"ASI loader requires VC2012 x86", JOptionPane.ERROR_MESSAGE);
					try {
						ModManager.openWebpage(new URL("https://www.microsoft.com/en-us/download/details.aspx?id=30679"));
					} catch (MalformedURLException e1) {
						ModManager.debugLogger.writeErrorWithException("Unable to open VC++ download page:", e1);
					}
				}
			} else {
				labelStatus.setText("Installing DLC bypass requires valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == toolsInstallBinkw32) {
			if (ModManager.isMassEffect3Running()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can install binkw32 DLC bypass.",
						"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (validateBIOGameDir()) {
				ModManager.debugLogger.writeMessage("Installing manual Binkw32 bypass (standard).");
				installBinkw32Bypass(false);
			} else {
				labelStatus.setText("Installing DLC bypass requires valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}

		} else if (e.getSource() == toolsUninstallBinkw32) {
			if (ModManager.isMassEffect3Running()) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can uninstall a binkw32 DLC bypass.",
						"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (validateBIOGameDir()) {
				ModManager.debugLogger.writeMessage("Uninstalling manual binkw32 bypass.");
				uninstallBinkw32Bypass();
			} else {
				labelStatus.setText("Installing DLC bypass requires valid BIOGame directory");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.", "Invalid BioGame Directory",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private boolean validateVC2012() {
		if (!ModManager.PERFORM_DOT_NET_CHECK) {
			return true;
		}
		try {
			String x86VC2012 = "SOFTWARE\\Classes\\Installer\\Dependencies\\{33d1fd90-4274-48a1-9bc1-97e33d9c2d6f}";
			String installDir = null;
			ModManager.debugLogger.writeMessage("Scanning registry for Visual C++ 2012 x86");
			installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, x86VC2012, "Version");

			if (installDir == null) {
				return false;
			}

			if (installDir.equals("11.0.61030.0")) {
				return true;
			}
		} catch (Throwable e) {
			ModManager.debugLogger.writeErrorWithException("Error occured while attempting to get VC2012 installation status! Could be the JNA crash.", e);
		}
		return false;
	}

	private void checkAllModsForUpdates(boolean isManualCheck) {
		// Fix for moonshine mod v1
		for (int i = 0; i < modModel.size(); i++) {
			Mod mod = modModel.getElementAt(i);
			if ("MoonShine".equals(mod.getAuthor())) {
				if ("360 Controller Support".equals(mod.getModName())) {
					if (mod.getVersion() == 0) {
						mod.setModUpdateCode(15);
						mod.setVersion(1);
					}
				}
			}
		}

		ArrayList<Mod> updatableMods = new ArrayList<Mod>();
		for (int i = 0; i < modModel.size(); i++) {
			Mod mod = modModel.get(i);
			if (mod.isME3TweaksUpdatable()) {
				updatableMods.add(mod);
			} else {
				ModManager.debugLogger.writeMessage(mod.getModName() + " is not ME3Tweaks updatable");
			}
		}

		if (!ModManagerWindow.validateBIOGameDir()) {
			ArrayList<Mod> modsThatCantUpdate = new ArrayList<>();
			for (Mod mod : updatableMods) {
				if (mod.getModMakerCode() > 1) {
					modsThatCantUpdate.add(mod);
				}
			}
			updatableMods.removeAll(modsThatCantUpdate);
		}

		if (updatableMods.size() > 0) {
			new AllModsUpdateWindow(this, isManualCheck, updatableMods);
		} else {
			if (isManualCheck) {
				JOptionPane.showMessageDialog(null,
						"No mods are eligible for the Mod Manager update service.\nEligible mods include ModMaker mods and ones hosted on ME3Tweaks.com.", "No updatable mods",
						JOptionPane.WARNING_MESSAGE);
			}
			Wini ini;
			try {
				File settings = new File(ModManager.SETTINGS_FILENAME);
				if (!settings.exists())
					settings.createNewFile();
				ini = new Wini(settings);
				ini.put("Settings", "lastautocheck", System.currentTimeMillis());
				ModManager.debugLogger.writeMessage("Updating last-autocheck date in ini");
				ini.store();
				ModManager.LAST_AUTOUPDATE_CHECK = System.currentTimeMillis();
			} catch (InvalidFileFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", e);
			}
		}
	}

	/**
	 * Opens a explorer.exe window with ME3 directory
	 */
	private void openME3Dir() {
		File file = new File(fieldBiogameDir.getText());
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, "The BioGame directory does not exist.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Desktop.getDesktop().open(new File(fieldBiogameDir.getText()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("I/O Exception while opening ME3Dir.", e);
		}
	}

	/**
	 * Shows the name/descip editor
	 */
	private void showInfoEditor() {
		// TODO Auto-generated method stub
		int selectedIndex = modList.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		// System.out.println("SELECTED VALUE: " + selectedValue);
		Mod mod = modModel.get(selectedIndex);
		new ModInfoEditorWindow(this, mod);
	}

	private void createBasegameDB(String biogameDir) {
		File file = new File(biogameDir);
		try {
			new BasegameHashDB(this, file.getParent(), true);
		} catch (SQLException e) {
			// nothing yet...
		}
	}

	private void backupDLC(String bioGameDir) {
		// Check that biogame is valid
		if (validateBIOGameDir()) {
			new BackupWindow(this, bioGameDir);
		} else {
			// Biogame is invalid
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
					"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			labelStatus.setText("DLC backup failed");
			labelStatus.setVisible(true);
		}
		return;
	}

	private void autoBackupDLC(String bioGameDir, String dlcName) {
		// Check that biogame is valid
		if (validateBIOGameDir()) {
			new BackupWindow(this, bioGameDir, dlcName);
		} else {
			// Biogame is invalid
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
					"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			labelStatus.setText("DLC backup failed");
			labelStatus.setVisible(true);
		}
		return;
	}

	/**
	 * Checks that the user has chosen a correct biogame directory.
	 */
	private void checkForValidBioGame(JFileChooser dirChooser) {
		File coalesced = new File(ModManager.appendSlash(dirChooser.getSelectedFile().toString()) + "CookedPCConsole\\Coalesced.bin");
		if (coalesced.exists()) {
			String YesNo[] = { "Yes", "No" };
			int saveDir = JOptionPane.showOptionDialog(null, "BioGame directory set to: " + dirChooser.getSelectedFile().toString() + "\nSave this path?", "Save BIOGame path?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
			if (saveDir == 0) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "biogame_dir", ModManager.appendSlash(dirChooser.getSelectedFile().toString()));
					ModManager.debugLogger.writeMessage(ModManager.appendSlash(dirChooser.getSelectedFile().toString()));
					ini.store();
				} catch (InvalidFileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", e);
				}
				labelStatus.setText("Saved BioGame directory to me3cmm.ini");
				labelStatus.setVisible(true);
			}
			fieldBiogameDir.setText(dirChooser.getSelectedFile().toString());
			validateBIOGameDir();
		} else {
			JOptionPane.showMessageDialog(null, "Coalesced.bin not found in " + dirChooser.getSelectedFile().toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Checks if the string in the biogamedir is a valid biogame directory.
	 * Checks for Coalesced.bin and DLC folder existence.
	 * 
	 * @return True if valid, false otherwise
	 */
	public static boolean validateBIOGameDir() {
		if (ModManagerWindow.ACTIVE_WINDOW != null && ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir != null) {
			ModManagerWindow.PRELOADED_BIOGAME_DIR = ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText();
		}
		
		
		File coalesced = new File(ModManager.appendSlash(PRELOADED_BIOGAME_DIR) + "CookedPCConsole\\Coalesced.bin");
		File dlcFolder = new File(ModManager.appendSlash(PRELOADED_BIOGAME_DIR) + "DLC\\");
		if (coalesced.exists() && dlcFolder.exists()) {
			setBioDirHighlight(false);
			return true;
		} else {
			setBioDirHighlight(true);
			return false;
		}
	}

	/**
	 * Changes the border around the Biogame Directory panel to indicate
	 * something is wrong.
	 * 
	 * @param highlight
	 */
	private static void setBioDirHighlight(boolean highlight) {
		if (ModManagerWindow.ACTIVE_WINDOW != null && ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir != null) {
			if (highlight) {
				TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mass Effect 3 BIOGame Directory (INVALID)",
						TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getDefaults().getFont("titledBorder.font"), Color.RED);
				ModManagerWindow.ACTIVE_WINDOW.cookedDirPanel.setBorder(cookedDirTitle);
			} else {
				TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mass Effect 3 BIOGame Directory",
						TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getDefaults().getFont("titledBorder.font"), new Color(0, 150, 0));
				ModManagerWindow.ACTIVE_WINDOW.cookedDirPanel.setBorder(cookedDirTitle);
			}
		}
	}

	/**
	 * Gets the initial string to place into the biogame dir part of mod
	 * manager. Will default to a reasonably well guessed value if it can't read
	 * from settings or registry.
	 * 
	 * @return
	 */
	private String getInitialBiogameDirText() {
		ModManager.debugLogger.writeMessage("Getting location of Mass Effect 3 directory to populate BioGameDir text field.");
		Wini settingsini;
		String defaultDir = "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BIOGame\\";
		String setDir = "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BIOGame\\";
		String os = System.getProperty("os.name");

		try {
			settingsini = new Wini(new File(ModManager.SETTINGS_FILENAME));
			setDir = settingsini.get("Settings", "biogame_dir");
			ModManager.debugLogger.writeMessage("ME3CMM.ini has saved the biogame directory to (blank/null if doesn't exist): " + setDir);
			if ((setDir == null || setDir.equals("")) && os.contains("Windows")) {
				String installDir = null;
				String _32bitpath = "SOFTWARE\\BioWare\\Mass Effect 3";
				String _64bitpath = "SOFTWARE\\Wow6432Node\\BioWare\\Mass Effect 3";
				ModManager.debugLogger.writeMessage("ME3CMM.ini does not contain the game path, attempting lookup via 64-bit registry key");
				try {
					installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _64bitpath, "Install Dir");
					ModManager.debugLogger.writeMessage("found installdir via 64bit reg key");
				} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
					ModManager.debugLogger.writeMessage("Exception looking at 64 registry key: " + _64bitpath);
				}

				if (installDir == null) {
					// try 32bit key
					try {
						installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, _32bitpath, "Install Dir");
						ModManager.debugLogger.writeMessage("64-bit registry key not found. Attemping to find via 32-bit registy key");
					} catch (com.sun.jna.platform.win32.Win32Exception keynotfoundException) {
						ModManager.debugLogger.writeMessage("Exception looking at 32bit registry key: " + _32bitpath);
					}
				}
				if (installDir != null) {
					ModManager.debugLogger.writeMessage("Found mass effect 3 location in registry: " + installDir);
					setDir = ModManager.appendSlash(installDir) + "BIOGame";
				} else {
					ModManager.debugLogger.writeError("Could not find Mass Effect 3 registry key in both 32 and 64 bit locations.");
				}
			}
		} catch (InvalidFileFormatException e) {
			ModManager.debugLogger.writeError("Invalid file format exception writing to settings ini.");
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("I/O Exception attemping to get ME3 install directory. Could be a settings file not writing: ", e);
		} catch (Throwable e) {
			ModManager.debugLogger.writeErrorWithException("Error occured while attempting to get/set the biogame directory! Could be the JNA crash.", e);
			return "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BIOGame";
		}
		ModManager.debugLogger.writeMessage("Directory that will be set: " + (setDir != null && !setDir.equals("") ? setDir : defaultDir));
		return (setDir != null && !setDir.equals("")) ? setDir : defaultDir;
	}

	/**
	 * Installs the mod.
	 * 
	 * @return True if the file copied, otherwise false
	 */
	private boolean applyMod() {
		// Prepare
		labelStatus.setText("Installing mod...");
		labelStatus.setVisible(true);

		// Validate BioGame Dir
		File coalesced = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + "CookedPCConsole\\" + "Coalesced.bin");
		if (ModManager.logging) {
			ModManager.debugLogger.writeMessage("Validating BioGame dir: " + coalesced);
		}
		if (!coalesced.exists()) {
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);

			labelStatus.setText("Mod not installed");
			labelStatus.setVisible(true);
			return false;
		}

		Mod mod = modModel.get(modList.getSelectedIndex());
		if (mod.getJobs().length > 0) {
			checkBackedUp(mod);
			new ModInstallWindow(this, fieldBiogameDir.getText(), mod);
		} else {
			ModManager.debugLogger.writeMessage("No dlc mod job, finishing mod installation");
		}
		return true;
	}

	private void checkBackedUp(Mod mod) {
		ModJob[] jobs = mod.getJobs();
		for (ModJob job : jobs) {
			if (job.getJobType() == ModJob.BASEGAME || job.getJobType() == ModJob.CUSTOMDLC) {
				continue; // we can't really check for a .bak of Coalesced.
			}
			// Default.sfar
			File mainFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Default.sfar");
			boolean defaultsfarMainFileExists = mainFile.exists();
			File backFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Default.sfar.bak");
			ModManager.debugLogger.writeMessage("Checking for backup file: " + backFile.getAbsolutePath());
			if (!backFile.exists()) {
				// Patch_001.sfar
				mainFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Patch_001.sfar");
				boolean patch001farMainFileExists = mainFile.exists();
				backFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Patch_001.sfar.bak");
				ModManager.debugLogger.writeMessage("Checking for TESTPATCH file: " + backFile.getAbsolutePath());

				if ((defaultsfarMainFileExists || patch001farMainFileExists) && !backFile.exists()) {
					String YesNo[] = { "Yes", "No" }; // Yes/no buttons
					int showDLCBackup = JOptionPane.showOptionDialog(null, "<html>" + job.getJobName() + " DLC has not been backed up.<br>Back it up now?</hmtl>", "Backup DLC",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
					if (showDLCBackup == 0) {
						autoBackupDLC(fieldBiogameDir.getText(), job.getJobName());
					}
				}
			}
		}
	}

	/**
	 * Handles looking up the name of a mod from the mod object that it comes
	 * from. Uses a hash map.
	 * 
	 * @param modName
	 *            Name of the mod from the list (display name)
	 * @return File that describes the selected mod
	 */

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (modList.getSelectedIndex() == -1) {
				buttonApplyMod.setEnabled(false);
				if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
					buttonApplyMod.setToolTipText("Select a mod on the left");
				} else {
					buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.5 or higher in order to install mods");
				}
				fieldDescription.setText(selectAModDescription);
				modMenu.setEnabled(false);
				modMenu.setToolTipText("Select a mod to enable this menu");
				moddevUpdateXMLGenerator.setText("Prepare mod for ME3Tweaks Updater Service");
				moddevUpdateXMLGenerator.setToolTipText("No mod is currently selected");
				moddevUpdateXMLGenerator.setEnabled(false);
			} else {
				Mod selectedMod = modModel.get(modList.getSelectedIndex());
				modMenu.setToolTipText(null);

				// Update mod description
				fieldDescription.setText(selectedMod.getModDisplayDescription());
				fieldDescription.setCaretPosition(0);
				buttonApplyMod.setEnabled(true);
				if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
					buttonApplyMod.setToolTipText(
							"<html>Apply this mod to the game.<br>If other mods are installed, you should consider uninstalling them by<br>using the Restore Menu if they are known to not work together.</html>");
				} else {
					buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.5 or higher in order to install mods");
				}
				modutilsHeader.setText(modModel.get(modList.getSelectedIndex()).getModName());
				modMenu.setEnabled(true);
				if (selectedMod.isME3TweaksUpdatable()) {
					modutilsCheckforupdate.setEnabled(true);
					modutilsCheckforupdate.setText("Check for updates");
					modutilsCheckforupdate.setToolTipText("Checks for updates to this mod from ME3Tweaks");
					modutilsRestoreMod.setVisible(true);
					if (selectedMod.getModMakerCode() > 0) {
						moddevUpdateXMLGenerator.setEnabled(false);
						moddevUpdateXMLGenerator.setToolTipText("ModMaker mods will update when a new revision is published on ME3Tweaks ModMaker");
						moddevUpdateXMLGenerator.setText("Prepare " + selectedMod.getModName() + " for ME3Tweaks Updater Service");
					} else {
						moddevUpdateXMLGenerator.setEnabled(true);
						moddevUpdateXMLGenerator.setText("Prepare " + selectedMod.getModName() + " for ME3Tweaks Updater Service");
						moddevUpdateXMLGenerator.setToolTipText("Compresses mod files for storage on ME3Tweaks and generates a mod manifest. Copies to clipboard when complete.");
					}
				} else {
					modutilsRestoreMod.setVisible(false);
					modutilsCheckforupdate.setEnabled(false);
					modutilsCheckforupdate.setText("Mod not eligible for updates");
					moddevUpdateXMLGenerator.setEnabled(false);
					moddevUpdateXMLGenerator.setToolTipText(selectedMod.getModName() + " does not have a ME3Tweaks update code");
					moddevUpdateXMLGenerator.setText("Cannot prepare " + selectedMod.getModName() + " for ME3Tweaks Updater Service");
					modutilsCheckforupdate.setToolTipText("<html>Mod update eligibility requires a floating point version number<br>and an update code from ME3Tweaks</html>");
				}

				UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
				if (selectedMod.getModSite() != null && urlValidator.isValid(selectedMod.getModSite())) {
					modWebsiteLink.setToolTipText(selectedMod.getModSite());
					modWebsiteLink.setVisible(true);
					modWebsiteLink.setText("<html><u><font color='#000099'>Visit " + selectedMod.getModName() + "'s web site</u></font></html>");
				} else {
					modWebsiteLink.setVisible(false);
				}

				mountMenu.removeAll();

				if (selectedMod.containsCustomDLCJob()) {
					mountMenu.setVisible(true);
					for (ModJob job : selectedMod.getJobs()) {
						if (job.getJobType() == ModJob.CUSTOMDLC) {
							// has custom dlc task
							// for (: job.get
							for (int i = 0; i < job.getFilesToReplace().size(); i++) {
								String target = job.getFilesToReplace().get(i);
								if (FilenameUtils.getName(target).equalsIgnoreCase("mount.dlc")) {
									// mount.dlc file found!
									File mountFile = new File(target);
									String folderName = FilenameUtils.getBaseName(mountFile.getParentFile().getParent());
									JMenuItem mountItem = new JMenuItem("Mount.dlc in " + folderName);
									mountItem.addActionListener(new ActionListener() {

										@Override
										public void actionPerformed(ActionEvent arg0) {
											new MountFileEditorWindow(target);
										}
									});
									mountMenu.add(mountItem);
								}
							}
						}
					}
				} else {
					mountMenu.setVisible(false);
				}

				if (selectedMod.getModDeltas().size() > 0) {
					modDeltaMenu.removeAll();
					modDeltaMenu.add(modDeltaRevert);
					modDeltaMenu.add(modutilsVerifyDeltas);
					File originalVariantFolder = new File(selectedMod.getModPath() + Mod.VARIANT_FOLDER + File.separator + Mod.ORIGINAL_FOLDER);
					modDeltaRevert.setEnabled(originalVariantFolder.exists());
					if (originalVariantFolder.exists()) {
						modDeltaRevert.setToolTipText("Revert to the original version of this mod");
					} else {
						modDeltaRevert.setToolTipText("No variants have been applied yet, so this is the original");
					}

					modDeltaMenu.addSeparator();
					modDeltaMenu.setText(selectedMod.getModDeltas().size() + " available variant" + (selectedMod.getModDeltas().size() != 1 ? "s" : ""));
					modDeltaMenu.setVisible(true);
					modNoDeltas.setVisible(false);
					for (ModDelta delta : selectedMod.getModDeltas()) {
						JMenuItem deltaItem = new JMenuItem(delta.getDeltaName());
						deltaItem.setToolTipText("<html>" + delta.getDeltaDescription() + "</html>");
						deltaItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								ModManager.debugLogger.writeMessage("Applying delta " + delta.getDeltaName() + " to " + selectedMod.getModName());
								new DeltaWindow(selectedMod, delta, false, false);
								modDeltaRevert.setEnabled(true);
								modDeltaRevert.setText("Revert to original version");
								modDeltaRevert.setToolTipText("<html>Restores the mod to the original version, without variants applied</html>");
							}

						});
						modDeltaMenu.add(deltaItem);
					}
				} else {
					modDeltaMenu.setVisible(false);
					modNoDeltas.setVisible(true);
				}

				modAlternatesMenu.removeAll();
				if (selectedMod.getAlternateFiles().size() > 0 || selectedMod.getAlternateCustomDLC().size() > 0) {
					modAlternatesMenu.setEnabled(true);
					ArrayList<AlternateFile> alts = selectedMod.getAlternateFiles();
					ArrayList<AlternateCustomDLC> altdlcs = selectedMod.getAlternateCustomDLC();
					int numoptions = altdlcs.size() + alts.size();
					for (ModJob job : selectedMod.getJobs()) {
						if (job.getJobType() == ModJob.CUSTOMDLC) {
							continue; //don't parse these
						}
						numoptions += job.getAlternateFiles().size();
					}
					modAlternatesMenu.setText(numoptions + " alternate installation option" + (numoptions != 1 ? "s" : ""));
					if (numoptions > 0) {
						modAlternatesMenu.setToolTipText("<html>This mod has " + numoptions + " additional installation configuration" + (numoptions != 1 ? "s" : "") + "</html>");
					}

					ArrayList<AlternateFile> autoAlts = selectedMod.getApplicableAutomaticAlternates(GetBioGameDir());
					for (AlternateFile af : alts) {
						String friendlyname = af.getOperation() + " due to " + af.getCondition() + " for " + af.getConditionalDLC();
						if (af.getFriendlyName() != null) {
							friendlyname = af.getFriendlyName();
						}
						JCheckBoxMenuItem item = new JCheckBoxMenuItem(friendlyname);
						item.setToolTipText(af.getDescription());
						item.setEnabled(false);
						if (autoAlts.contains(af)) {
							item.setSelected(true);
						}
						modAlternatesMenu.add(item);
					}

					//Populate Manual Alternate Files for Official DLC
					for (ModJob job : selectedMod.getJobs()) {
						if (job.getJobType() == ModJob.CUSTOMDLC) {
							continue; //don't parse these
						}
						for (AlternateFile af : job.getAlternateFiles()) {
							String friendlyname = af.getOperation() + " due to " + af.getCondition() + " for " + job.getJobName();
							if (af.getFriendlyName() != null) {
								friendlyname = af.getFriendlyName();
							}
							JCheckBoxMenuItem item = new JCheckBoxMenuItem(friendlyname);
							item.setToolTipText(af.getDescription());
							item.setEnabled(true); //Can only do CONDITION_MANUAL
							item.setSelected(af.isEnabled());
							item.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									af.setHasBeenChosen(item.isSelected());
									ModManager.debugLogger.writeMessage("[" + selectedMod.getModName() + "] User has toggled an optional ALTFILE addin " + item.getText() + " to "
											+ (item.isSelected() ? "on" : "off") + ".");
									labelStatus.setText(item.getText() + " set to " + (item.isSelected() ? "enabled" : "disabled"));
								}
							});
							modAlternatesMenu.add(item);
						}
					}

					if (altdlcs.size() > 0) {
						modAlternatesMenu.addSeparator();
						for (AlternateCustomDLC altdlc : altdlcs) {
							String friendlyname = altdlc.getOperation() + " due to " + altdlc.getCondition() + " for " + altdlc.getConditionalDLC();
							if (altdlc.getFriendlyName() != null) {
								friendlyname = altdlc.getFriendlyName();
							}
							JCheckBoxMenuItem item = new JCheckBoxMenuItem(friendlyname);
							item.setToolTipText(altdlc.getDescription());
							if (!altdlc.getCondition().equals(AlternateCustomDLC.CONDITION_MANUAL)) {
								item.setEnabled(false);
							} else {
								item.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										ModManager.debugLogger.writeMessage("[" + selectedMod.getModName() + "] User has toggled an optional ALTDLC addin " + item.getText()
												+ " to " + (item.isSelected() ? "on" : "off") + ".");
										altdlc.setHasBeenChosen(item.isSelected());
									}
								});
							}
							if (selectedMod.getAppliedAutomaticAlternateCustomDLC().contains(altdlc)) {
								item.setSelected(true);
							} else {
								item.setSelected(altdlc.hasBeenChoosen());
							}
							modAlternatesMenu.add(item);
						}
					}
				} else {
					modAlternatesMenu.setEnabled(false);
					modAlternatesMenu.setText("No alternate installation options");
				}
			}
		}
	}

	/**
	 * Legacy method from Mod Manager 1+2 to restore the original coalesced file
	 * 
	 * @param bioGameDir
	 * @return
	 */
	private boolean restoreCoalesced(String bioGameDir) {
		String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e";
		File cOriginal = new File(ModManager.getDataDir() + "Coalesced.original");
		if (cOriginal.exists()) {
			// Take the MD5 first to verify it.
			try {
				if (patch3CoalescedHash.equals(MD5Checksum.getMD5Checksum(cOriginal.toString()))) {
					// file is indeed the original
					// Copy
					String destFile = ModManager.appendSlash(bioGameDir) + "CookedPCConsole\\Coalesced.bin";
					if (new File(destFile).exists() == false) {
						JOptionPane.showMessageDialog(null,
								"Coalesced.bin to be restored was not found in the specified BIOGame\\CookedPCConsole directory.\nYou must fix the directory before you can restore Coalesced.",
								"Coalesced not found", JOptionPane.ERROR_MESSAGE);
						labelStatus.setText("Coalesced.bin not restored");
						labelStatus.setVisible(true);
						return false;
					}
					String sourceFile = ModManager.getDataDir() + "Coalesced.original";

					Files.copy(new File(sourceFile).toPath(), new File(destFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
					ModManager.debugLogger.writeMessage("Restored Coalesced.bin");
					labelStatus.setText("Coalesced.bin restored");
					labelStatus.setVisible(true);
					return true;
				} else {
					labelStatus.setText("Coalesced.bin not restored.");
					labelStatus.setVisible(true);
					JOptionPane.showMessageDialog(null,
							"Your backed up original Coalesced.bin file does not match the known original from Mass Effect 3.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.",
							"Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Coalesced.bin was unable to be restored due to an error:", e);
				labelStatus.setText("Coalesced.bin not restored");
				labelStatus.setVisible(true);
				return false;
			}
		} else {
			labelStatus.setText("Coalesced.bin not restored");
			labelStatus.setVisible(true);
			JOptionPane.showMessageDialog(null,
					"The backed up Coalesced.bin file (data/Coalesced.original) does not exist.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.\n\nThis error should have been caught but can be thrown due to file system changes \nwhile the program is open.",
					"Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);

		}
		return false;
	}

	/**
	 * Initiates a restore procedure using the specified directory and restore
	 * mode
	 * 
	 * @param bioGameDir
	 *            Directory to biogame folder
	 * @param restoreMode
	 *            constant defining the restore procedure
	 * @return True if all were restored, false otherwise
	 */
	private boolean restoreDataFiles(String bioGameDir, int restoreMode) {
		// Check to make sure biogame is correct
		if (validateBIOGameDir()) {
			new RestoreFilesWindow(bioGameDir, restoreMode);
			return true;
		} else {
			labelStatus.setText("Invalid BioGame Directory");
			labelStatus.setVisible(true);
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid. Files cannot be restored.\nFix the directory and try again.", "Invalid BioGame Directory",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void startGame(String CookedDir) {
		File startingDir = new File(CookedDir);
		ModManager.debugLogger.writeMessage("Starting game.");
		boolean binkw32bypass = ModManager.checkIfBinkBypassIsInstalled(fieldBiogameDir.getText()); // favor
																									// bink
																									// over
																									// WV
		startingDir = new File(startingDir.getParent());
		File executable = new File(startingDir.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
		// check ME3 version for 1.6
		try {
			int minorBuildNum = EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());
			if (minorBuildNum > 5) {
				ModManager.debugLogger.writeMessage("!!!!This user has >1.5 version of Mass Effect 3!!!!");
				JOptionPane.showMessageDialog(this,
						"<html><div style='width: 300px'>You have a version of Mass Effect 3 higher than 1.5 (1." + minorBuildNum
								+ "), which is the main version most of the world uses.<br>"
								+ "It seems BioWare has been slowly pushing this version out to some users starting in September 2013. Not all users will get this version.<br><br>If you encounter issues you may consider downgrading to the 1.5 EXE. If you play Multiplayer, you will only be able to connect to other 1.6 users, which are very few.<br>Check the ME3Tweaks forums for info on how to downgrade.</div></html>",
						"Mass Effect 3 Rare Version Detected", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			// do nothing. Continue like the old mod manager did.
			ModManager.debugLogger.writeErrorWithException("Error getting Mass Effect 3 EXE version. This error will be ignored.", e);
		}

		if (!binkw32bypass) { // try to find Launcher_WV
			executable = new File(startingDir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
			if (!executable.exists()) {
				// Try the other name he uses
				executable = new File(startingDir.toString() + "\\Binaries\\Win32\\LauncherWV.exe");
				if (!executable.exists()) {
					ModManager.debugLogger.writeMessage("Warranty Voider's memory patcher launcher was not found, using the main one.");
					executable = new File(startingDir.toString() + "\\Binaries\\Win32\\MassEffect3.exe"); // use
																											// standard
				}
			}
		} else {
			ModManager.debugLogger.writeMessage("Binkw32 installed. Launching standard ME3.");
		}
		ModManager.debugLogger.writeMessage("Launching: " + executable.getAbsolutePath());

		// check if the new one exists
		if (!executable.exists()) {
			JOptionPane.showMessageDialog(null,
					"Unable to find game executable in the specified directory:\n" + executable.getAbsolutePath() + "\nMake sure your BIOGame directory is correct.",
					"Unable to Launch Game", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Executable exists.
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", executable.getAbsolutePath() };
		try {
			labelStatus.setText("Launched Mass Effect 3");
			this.setExtendedState(JFrame.ICONIFIED);
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("I/O Exception while launching ME3 or LauncherWV.", e);

		}
		ModManager.debugLogger.writeMessage("Path: " + executable.getAbsolutePath() + " - Exists? " + executable.exists());
	}

	/**
	 * Creates a new Mod Maker Compiler dialog with the specified code. Called
	 * from the code entry dialog.
	 * 
	 * @param code
	 *            Code to use for downloading the mod.
	 */
	public void startModMaker(String code, ArrayList<String> languages) {
		new ModMakerCompilerWindow(code, languages);
	}

	private void autoTOC(int mode) {
		// update the PCConsoleTOC's of a specific mod.
		int selectedIndex = modList.getSelectedIndex();
		if (selectedIndex < 0) {
			return; // shouldn't be able to toc an unselected mod eh?
		}
		// System.out.println("SELECTED VALUE: " + selectedValue);
		Mod mod = modModel.getElementAt(selectedIndex);
		new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
	}

	private boolean installBypass() {
		if (validateBIOGameDir()) {
			boolean result = ModManager.installLauncherWV(fieldBiogameDir.getText());
			if (result) {
				// ok
				labelStatus.setText("Launcher WV installed. Start Game will now use it.");
			} else {
				labelStatus.setText("FAILURE: Launcher WV bypass not installed!");
			}
			return result;
		}
		JOptionPane.showMessageDialog(null,
				"The BioGame directory is not valid.\nMod Manager cannot install the LauncherWV DLC bypass.\nFix the BioGame directory before continuing.",
				"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private boolean installBinkw32Bypass(boolean asi) {
		if (validateBIOGameDir()) {
			boolean result = ModManager.installBinkw32Bypass(fieldBiogameDir.getText(), asi);
			if (result) {
				// ok
				labelStatus.setText("Binkw32" + (asi ? " (ASI)" : "") + " installed. DLC will always authorize.");
			} else {
				labelStatus.setText("FAILURE: Binkw32" + (asi ? " (ASI)" : "") + " not installed!");
			}
			return result;
		}
		JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.\nMod Manager cannot install Binkw32" + (asi ? " (ASI)" : "")
				+ ".dll DLC bypass.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private boolean uninstallBinkw32Bypass() {
		if (validateBIOGameDir()) {
			boolean result = ModManager.uninstallBinkw32Bypass(fieldBiogameDir.getText());
			if (result) {
				// ok
				labelStatus.setText("Binkw32 bypass uninstalled");
			} else {
				labelStatus.setText("FAILURE: Binkw32 bypass not uninstalled");
			}
			return result;
		}
		JOptionPane.showMessageDialog(null,
				"The BioGame directory is not valid.\nMod Manager cannot revert the Binkw32.dll DLC bypass.\nFix the BioGame directory before continuing.",
				"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	/**
	 * Gets all mods targeting CMM3 and above
	 * 
	 * @return list of mods targeting CMM3 and above
	 */
	public ArrayList<Mod> getCMM3ModsFromDirectory() {
		ArrayList<Mod> cmm3mods = new ArrayList<Mod>();
		for (int i = 0; i < modModel.size(); i++) {
			Mod mod = modModel.getElementAt(i);
			if (mod.modCMMVer >= 3) {
				cmm3mods.add(mod);
			}
		}
		return cmm3mods;
	}

	public ArrayList<Patch> getPatchesFromLibrary() {

		return null;
	}

	public ArrayList<Patch> getPatchList() {
		return patchList;
	}

	public void setPatchList(ArrayList<Patch> patchList) {
		this.patchList = patchList;
	}

	/**
	 * Returns the list of mods that failed to load when the mod manager window
	 * loaded
	 * 
	 * @return
	 */
	public ArrayList<Mod> getInvalidMods() {
		return invalidMods;
	}

	/**
	 * External API for calling single mod updater
	 * 
	 * @param mod
	 */
	public void startSingleModUpdate(Mod mod) {
		new SingleModUpdateCheckThread(mod).execute();
	}

	public static String GetBioGameDir() {
		if (ModManagerWindow.ACTIVE_WINDOW == null || ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir == null) {
			return ModManagerWindow.PRELOADED_BIOGAME_DIR;
		} else {
			return ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText();
		}
	}
}