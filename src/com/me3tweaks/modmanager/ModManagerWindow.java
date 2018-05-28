package com.me3tweaks.modmanager;

import com.me3tweaks.modmanager.help.HelpMenu;
import com.me3tweaks.modmanager.moddesceditor.ModDescEditorWindow;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModXMLTools;
import com.me3tweaks.modmanager.modupdater.UpdatePackage;
import com.me3tweaks.modmanager.objects.*;
import com.me3tweaks.modmanager.repairdb.BasegameHashDB;
import com.me3tweaks.modmanager.ui.StayOpenJCheckboxMenuItem;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.me3tweaks.modmanager.utilities.Version;
import com.me3tweaks.modmanager.valueparsers.bioai.BioAIGUI;
import com.me3tweaks.modmanager.valueparsers.biodifficulty.DifficultyGUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerCustomActionGUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerCustomActionGUI2;
import com.me3tweaks.modmanager.valueparsers.wavelist.WavelistGUI;
import com.sun.jna.platform.win32.Advapi32Util;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import net.iharder.dnd.FileDrop;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controls the main window for Mass Effect 3 Mod Manager.
 *
 * @author mgamerz
 */
@SuppressWarnings("serial")
public class ModManagerWindow extends JFrame implements ActionListener, ListSelectionListener {
    private static BiogameDirChangeListener BIOGAME_ITEM_LISTENER;
    public static ModManagerWindow ACTIVE_WINDOW;
    public static ArrayList<Integer> forceUpdateOnReloadList = new ArrayList<Integer>();
    private static String PRELOADED_BIOGAME_DIR;
    boolean isUpdate;
    public JComboBox<String> comboboxBiogameDir;
    JTextArea fieldDescription;
    JScrollPane scrollDescription;
    JButton buttonBioGameDir, buttonApplyMod, buttonStartGame;
    JMenuBar menuBar;
    JMenu actionMenu, modUtilsMenu, modManagementMenu, devMenu, toolsMenu, backupMenu, restoreMenu, parsersMenu, helpMenu, openToolMenu, modDeveloperMenu;
    JPopupMenu modUtilsPopupMenu;
    JMenuItem actionCheckForContentUpdates, actionModMaker, actionVisitMe, actionOptions, actionReload, actionExit;
    JMenuItem modManagementImportFromArchive, modManagementImportAlreadyInstalled, modManagementConflictDetector, modManagementModMaker, modManagementASI, modManagementFailedMods,
            modManagementPatchLibary, modManagementClearPatchLibraryCache, modManagementModGroupsManager;
    JMenuItem toolME3Explorer, toolsGrantWriteAccess, toolsOpenME3Dir, toolsUninstallBinkw32, toolMountdlcEditor,
    /* toolsMergeMod */ toolME3Config, toolsPCCDataDumper;
    JCheckBoxMenuItem toolsInstallBinkw32asi;
    JMenuItem backupBackupDLC, backupCreateGDB;
    JCheckBoxMenuItem backupCreateVanillaCopy;
    JMenuItem restoreSelective, restoreRevertEverything, restoreDeleteUnpacked, restoreRevertBasegame, restoreRevertAllDLC, restoreRevertSPDLC, restoreRevertMPDLC,
            restoreRevertMPBaseDLC, restoreRevertSPBaseDLC, restoreRevertCoal, restoreVanillifyDLC, restoreVanillaCopy;

    JMenuItem modDevStarterKit, moddevOfficialDLCManager;
    JMenuItem sqlWavelistParser, sqlDifficultyParser, sqlAIWeaponParser, sqlPowerCustomActionParser, sqlPowerCustomActionParser2;
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
    private boolean loadedFirstTime = false;
    private JMenuItem toolAlotInstaller;
    private ArrayList<MainUIBackgroundJob> backgroundJobs;
    private JXCollapsiblePane activityPanel;

    /**
     * Opens a new Mod Manager window. Disposes of old ones if one is open.
     * Automatically makes window visible so it will block the UI thread.
     *
     * @param isUpdate If this is an upgrade from a previous version of mod manager
     */
    public ModManagerWindow(boolean isUpdate) {
        if (ACTIVE_WINDOW != null) {
            backgroundJobs = ACTIVE_WINDOW.backgroundJobs; // carry background job indicators.
            ACTIVE_WINDOW.dispose();
            ACTIVE_WINDOW = null;
        } else {
            backgroundJobs = new ArrayList<MainUIBackgroundJob>();
        }
        this.isUpdate = isUpdate;
        ModManager.debugLogger.writeMessage("Setting up Mod Manager Window");
        try {

            initializeWindow();
            ACTIVE_WINDOW = this;
        } catch (Exception e) {
            ModManager.debugLogger.writeErrorWithException("UNKNOWN CRITICAL STARTUP EXCEPTION FOR MOD MANAGER WINDOW:", e);
            ModManager.debugLogger.writeError("Mod Manager has crashed!");
            String emessage = e.getMessage();
            if (emessage == null) {
                emessage = "An uncaught NullPointerException has been thrown while preparing the interface.";
            }
            JOptionPane.showMessageDialog(ModManagerWindow.this,
                    "<html><div style=\"width:330px;\">Mod Manager's interface (post-startup) encountered a critical unknown error and was unable to start:<br>" + emessage + "<br>"
                            + "<br>This has been logged to the me3cmm_last_run_log.txt file next to ME3CMM.exe.<br>Please report this to FemShep.</div></html>",
                    "Critical Error", JOptionPane.ERROR_MESSAGE);
            ModManager.debugLogger.writeMessage("Shutting down...");
            System.exit(1);
            return;
        }
        validateBIOGameDir();
        ModManager.debugLogger.writeMessage("Mod Manager Window UI: Now setting visible.");
        try {
			/*
			//DEBUG ONLY
			if (!modModel.isEmpty()) {
				modList.setSelectedIndex(0);
				new ModDescEditorWindow(modModel.firstElement());
			}*/
            setVisible(true);
        } catch (Exception e) {
            ModManager.debugLogger.writeErrorWithException("Uncaught runtime exception:", e);
            JOptionPane.showMessageDialog(ModManagerWindow.this,
                    "<html><div style=\"width:330px;\">Mod Manager's interface has just encountered an error:<br>" + e.getMessage() + "<br>"
                            + "<br>This has been logged to the me3cmm_last_run_log.txt file.<br>The application will attempt to ignore this error.</div></html>",
                    "Mod Manager Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Submits a background job indicator. When the task is complete you then
     * submit a task completion and when the list is empty the activity
     * indicator will stop.
     *
     * @param taskname Name of the task.
     * @return hashcode of the task that requires submission to end the task.
     */
    public int submitBackgroundJob(String taskname) {
        MainUIBackgroundJob bg = new MainUIBackgroundJob(taskname);
        backgroundJobs.add(bg);
        setActivityIcon(true);
        return bg.hashCode();
    }

    /**
     * Submits a completion request to the main interface using the originally
     * returned code. When all jobs are cleared, the activity indicator is
     * hidden.
     *
     * @param jobHash hash of previously submitted job.
     */
    public void submitJobCompletion(int jobHash) {
        MainUIBackgroundJob bg = null;
        for (MainUIBackgroundJob job : backgroundJobs) {
            if (job.hashCode() == jobHash) {
                bg = job;
                break;
            }
        }
        if (bg != null) {
            backgroundJobs.remove(bg);
            ModManager.debugLogger.writeMessage("Released background activity job for " + bg.getTaskName());
            if (backgroundJobs.size() <= 0) {
                setActivityIcon(false);
            }
        }
    }

    private void initializeWindow() {
        setupWindow();
        pack();
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

        ModManager.debugLogger.writeMessage("Running startup NetworkThread.");
        new NetworkThread(false).execute();
        ModManager.debugLogger.writeMessage("Mod Manager Window UI: InitializeWindow() has completed.");
    }

    /**
     * This thread executes multiple items: Check For Mod Manager Updates Check
     * for Mod Updates Check for 7z file + download Check for ME3Explorer
     * Updates (if needed)
     *
     * @author Michael
     */
    class NetworkThread extends SwingWorker<Void, ThreadCommand> {

        private boolean force;
        private int networkThreadCode;

        public NetworkThread(boolean force) {
            this.force = force;
            networkThreadCode = submitBackgroundJob("NetworkThread");
        }

        @Override
        public Void doInBackground() {

            if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
                ModManager.debugLogger.writeMessage("Mod Manager on 32-bit windows is no longer supported. Networking support was disabled May 15th, 2018.");
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Network support for 32-bit Mod Manager ended May 15, 2018"));
                return null;
            }

            File f7z = new File(ModManager.get7zExePath());
            boolean needsUpdate = false;
            if (f7z.exists()) {
                try {
                    int mainVersion = EXEFileInfo.getMajorVersionOfProgram((f7z.getAbsolutePath()));
                    int minorVersion = EXEFileInfo.getMinorVersionOfProgram((f7z.getAbsolutePath()));
                    if (mainVersion < 18 || mainVersion == 18 && minorVersion < 5) {
                        needsUpdate = true;
                        ModManager.debugLogger.writeMessage("7z is outdated - updating to at least 18.5");
                    }
                } catch (Exception e) {
                    needsUpdate = true;
                    ModManager.debugLogger.writeErrorWithException("7z exe version information could not be read - downloading new copy", e);
                }
            }
            if (!f7z.exists() || needsUpdate) {
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 7z"));
                ModManager.debugLogger.writeMessage("7z.exe does not exist at the following path, downloading new copy: " + f7z.getAbsolutePath());
                String url = "https://me3tweaks.com/modmanager/tools/7z";
                url += ".exe";
                try {
                    File updateDir = new File(ModManager.getToolsDir());
                    updateDir.mkdirs();
                    FileUtils.copyURLToFile(new URL(url), new File(ModManager.get7zExePath()));
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded 7z dll"));
                    ModManager.debugLogger.writeMessage("Environment Check: Downloaded missing 7z.exe file for updating Mod Manager");

                } catch (IOException e) {
                    ModManager.debugLogger.writeErrorWithException("Environment Check: Error downloading 7z.exe into tools folder", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading 7z"));
                }
            } else {
                ModManager.debugLogger.writeMessage("Environment Check: 7z.exe is present in tools/ directory");
            }

            File f7zdll = new File(ModManager.get7zDllPath());
            needsUpdate = false;
            if (f7zdll.exists()) {
                try {
                    int mainVersion = EXEFileInfo.getMajorVersionOfProgram((f7zdll.getAbsolutePath()));
                    int minorVersion = EXEFileInfo.getMinorVersionOfProgram((f7zdll.getAbsolutePath()));
                    if (mainVersion < 18 || mainVersion == 18 && minorVersion < 5) {
                        needsUpdate = true;
                        ModManager.debugLogger.writeMessage("7z dll is outdated - updating to at least 18.5");
                    }
                } catch (Exception e) {
                    needsUpdate = true;
                    ModManager.debugLogger.writeErrorWithException("7z dll version information could not be read - downloading new copy", e);
                }
            }
            if (!f7zdll.exists() || needsUpdate) {
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 7z library"));
                ModManager.debugLogger.writeMessage("Environment Check: 7z.dll does not exist at the following path, downloading new copy: " + f7zdll.getAbsolutePath());
                String url = "https://me3tweaks.com/modmanager/tools/7z";
                url += ".dll";
                try {
                    File updateDir = new File(ModManager.getToolsDir());
                    updateDir.mkdirs();
                    FileUtils.copyURLToFile(new URL(url), new File(ModManager.get7zDllPath()));
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded 7z Unzipper dll into tools directory"));
                    ModManager.debugLogger.writeMessage("Downloaded missing 7z.dll file for updating Mod Manager");

                } catch (IOException e) {
                    ModManager.debugLogger.writeErrorWithException("Environment Check: Error downloading 7z dll into tools folder", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading 7z dll"));
                }
            } else {
                ModManager.debugLogger.writeMessage("Environment Check: 7z.dll is present in tools/ directory");
            }

            File lzma = new File(ModManager.getToolsDir() + "lzma.exe");
            if (!lzma.exists()) {
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading LZMA tool"));
                ModManager.debugLogger.writeMessage("Environment Check: lzma.exe does not exist at the following path, downloading new copy: " + lzma.getAbsolutePath());
                String url = "https://me3tweaks.com/modmanager/tools/lzma.exe";
                try {
                    File updateDir = new File(ModManager.getToolsDir());
                    updateDir.mkdirs();
                    FileUtils.copyURLToFile(new URL(url), new File(ModManager.getToolsDir() + "lzma.exe"));
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded lzma.exe into tools directory"));
                    ModManager.debugLogger.writeMessage("Environment Check: Downloaded missing lzma.exe file for preparing mod updates");

                } catch (IOException e) {
                    ModManager.debugLogger.writeErrorWithException("Error downloading lzma into tools folder", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading lzma"));
                }
            } else {
                ModManager.debugLogger.writeMessage("Environment Check: lzma.exe is present in tools/ directory");
            }

            // Tankmaster TLK, Coalesce
            File tmc = new File(ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe");
            File tmtlk = new File(ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe");

            if (!tmtlk.exists() || !tmc.exists()) {
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading Tankmaster Tools"));
                ModManager.debugLogger.writeMessage("Environment Check: Tankmaster's TLK/COALESCE tools are missing, downloading new copy: " + tmtlk.getAbsolutePath());
                String url = "https://me3tweaks.com/modmanager/tools/tankmastertools.7z";
                try {
                    File updateDir = new File(ModManager.getTempDir());
                    updateDir.mkdirs();
                    FileUtils.copyURLToFile(new URL(url), new File(ModManager.getTempDir() + "tankmastertools.7z"));
                    ModManager.debugLogger.writeMessage("7z downloaded.");

                    // run 7za on it
                    ArrayList<String> commandBuilder = new ArrayList<String>();
                    commandBuilder.add(ModManager.get7zExePath());
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
                    ModManager.debugLogger.writeMessage("Environment Check: Extracting Tankmaster Tools.");
                    ProcessBuilder pb = new ProcessBuilder(command);
                    ProcessResult pr = ModManager.runProcess(pb);
                    int returncode = pr.getReturnCode();

                    ModManager.debugLogger.writeMessage("Environment Check: Unzip completed successfully (code 0): " + (returncode == 0));
                    if (returncode == 0) {
                        publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded Tankmaster Tools into data directory"));
                    } else {
                        publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Unknown error downloading Tankmaster tools"));
                    }
                    FileUtils.deleteQuietly(new File(ModManager.getTempDir() + "tankmastertools.7z"));
                } catch (IOException e) {
                    ModManager.debugLogger.writeErrorWithException("Environment Check: Error downloading 7z into tools folder", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Error downloading 7z for updating"));
                }
            } else {
                ModManager.debugLogger.writeMessage("Environment Check: Tankmaster tools exist locally already.");
            }

            File jpatch = new File(ModManager.getToolsDir() + "jptch.exe");
            if (!jpatch.exists()) {
                ModManager.debugLogger.writeMessage("Environment Check: Jptch.exe doesn't exist - downloading mixin tools.");
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading MixIn Patching Tools"));
                ME3TweaksUtils.downloadJDiffTools();
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded MixIn Patching Tools"));
                ModManager.debugLogger.writeMessage("Environment Check: Downloaded MixIn tools");
            }

            if (ModManager.AUTO_UPDATE_MOD_MANAGER && !ModManager.CHECKED_FOR_UPDATE_THIS_SESSION) {
                if (ResourceUtils.is64BitWindows()) {
                    JSONObject latestinfo = checkForModManagerUpdates();
                    checkForJREUpdates(latestinfo);
                } else {
                    labelStatus.setText("Mod Manager 32-bit updates have ended");
                    ModManager.debugLogger.writeMessage("Skipping update check - Windows 32 bit support has ended.");
                }
            }

            // checkForME3ExplorerUpdates();
            checkForCommandLineToolUpdates();
            if (isUpdate || ModManager.AUTO_UPDATE_CONTENT || forceUpdateOnReloadList.size() > 0 || force)

            {
                boolean forcedByUpdate = isUpdate;
                isUpdate = false; // no mas
                checkForContentUpdates(force || forcedByUpdate == true);
            }

            checkForALOTInstallerUpdates();
            return null;
        }

        /**
         * Checks for Command Line Tools and the correct version of them.
         */
        private void checkForCommandLineToolUpdates() {
            if (ModManager.COMMANDLINETOOLS_URL != null) {
                String fullautotoc = ModManager.getCommandLineToolsDir() + "FullAutoTOC.exe";
                File f = new File(fullautotoc);
                if (!f.exists()) {
                    ModManager.debugLogger.writeMessage("Environment Check: Mod Manager Command Line Tools are missing. Downloading from GitHub.");
                    new CommandLineToolsUpdaterWindow();
                } else {
                    int main = EXEFileInfo.getMajorVersionOfProgram(fullautotoc);
                    int minor = EXEFileInfo.getMinorVersionOfProgram(fullautotoc);
                    int build = EXEFileInfo.getBuildOfProgram(fullautotoc);
                    int rev = EXEFileInfo.getRevisionOfProgram(fullautotoc);
                    ModManager.debugLogger.writeMessage("Environment Check: Command Line Tools Version: " + main + "." + minor + "." + build + "." + rev);

                    Version installedVersion = new Version(main + "." + minor + "." + build + "." + rev);
                    Version minVersion = new Version(ModManager.MIN_REQUIRED_CMDLINE_MAIN + "." + ModManager.MIN_REQUIRED_CMDLINE_MINOR + "."
                            + ModManager.MIN_REQUIRED_CMDLINE_BUILD + "." + ModManager.MIN_REQUIRED_CMDLINE_REV);

                    if (installedVersion.compareTo(minVersion) < 0) {
                        // we must update it
                        ModManager.debugLogger.writeMessage("Environment Check: Command Line Tools out of date - required version: " + minVersion.toString());
                        new CommandLineToolsUpdaterWindow();
                    } else {
                        ModManager.debugLogger.writeMessage("Environment Check: Current Command Line Tools version satisfies requirements for Mod Manager");
                    }
                }
            } else {
                ModManager.debugLogger.writeMessage("Environment Check: No command line update link - no point checking for updates.");
            }
        }

        private void checkForContentUpdates(boolean force) {
            if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
                ModManager.debugLogger.writeMessage("Mod Manager on 32-bit windows is no longer supported. Networking support was disabled May 15th, 2018.");
                publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Network support for 32-bit Mod Manager ended May 15, 2018"));
                return;
            }

            // Check for updates
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

                // THIRD PARTY MOD IDENTIFICATION SERVICE
                try {
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 3rd party mod identification info"));
                    ModManager.debugLogger.writeMessage("Downloading third party mod data from identification service");
                    FileUtils.copyURLToFile(new URL("https://me3tweaks.com/mods/dlc_mods/thirdpartyidentificationservice?highprioritysupport=true"),
                            ModManager.getThirdPartyModDBFile());
                    ModManager.THIRD_PARTY_MOD_JSON = FileUtils.readFileToString(ModManager.getThirdPartyModDBFile(), StandardCharsets.UTF_8);
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


                //THIRD PARTY MOD IMPORTING SERVICE
                try {
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading 3rd party mod importing info"));
                    ModManager.debugLogger.writeMessage("Downloading third party importing data from importing service");
                    FileUtils.copyURLToFile(new URL("https://me3tweaks.com/mods/dlc_mods/thirdpartyimportingservice"), ModManager.getThirdPartyModImportingDBFile());
                    ModManager.THIRD_PARTY_IMPORTING_JSON = FileUtils.readFileToString(ModManager.getThirdPartyModImportingDBFile(), StandardCharsets.UTF_8);
                    ModManager.debugLogger.writeMessage("Downloaded third party importing data from importing service");
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded 3rd party mod importing info"));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    ModManager.debugLogger.writeErrorWithException("Failed to download third party importing data: ", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to get 3rd party importing info"));
                }

                //TIPS SERVICE
                try {
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloading tips service data"));
                    ModManager.debugLogger.writeMessage("Downloading tips from ME3Tweaks Tips Service");
                    FileUtils.copyURLToFile(new URL("https://me3tweaks.com/modmanager/tools/tipsservice"), ModManager.getTipsServiceFile());
                    ModManager.TIPS_SERVICE_JSON = FileUtils.readFileToString(ModManager.getTipsServiceFile(), StandardCharsets.UTF_8);
                    ModManager.debugLogger.writeMessage("Downloaded tips service json");
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Downloaded tips service data"));
                    publish(new ThreadCommand("SET_TIP"));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    ModManager.debugLogger.writeErrorWithException("Failed to download tips service data: ", e);
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Failed to get tips service data"));
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
         * Checks for the latest github release of MEM and stores the values in
         * memory. The values are used when attempting to start MEM.
         */
        private void checkForALOTInstallerUpdates() {
            try {
                String alotInstallerReleaseAPIEndpoint = "https://api.github.com/repos/Mgamerz/ALOTAddonGUI/releases/latest";
                String response = IOUtils.toString(new URL(alotInstallerReleaseAPIEndpoint), StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONObject latestRelease = (JSONObject) parser.parse(response);
                String version = (String) latestRelease.get("tag_name");
                try {
                    ModManager.ALOTINSTALLER_LATESTVERSION = new Version(version);
                    ModManager.debugLogger.writeMessage("Latest ALOT Installer version on github: v" + ModManager.ALOTINSTALLER_LATESTVERSION);
                } catch (NumberFormatException e) {
                    return;
                }

                JSONArray assets = (JSONArray) latestRelease.get("assets");
                if (assets.size() > 0) {
                    JSONObject releaseAsset = (JSONObject) assets.get(0);
                    ModManager.ALOTINSTALLER_DOWNLOADLINK = (String) releaseAsset.get("browser_download_url");
                }
            } catch (IOException e) {
                ModManager.debugLogger.writeErrorWithException("I/O Exception when checking Github for ALOT Installer update check:", e);
            } catch (ParseException e1) {
                ModManager.debugLogger.writeErrorWithException("Error in JSON returned from Github for ALOT Installer update check:", e1);
            }
        }

        @Override
        protected void process(List<ThreadCommand> chunks) {
            for (ThreadCommand latest : chunks) {
                switch (latest.getCommand()) {
                    case "UPDATE_HELP_MENU":
                        menuBar.remove(helpMenu);
                        helpMenu = HelpMenu.constructHelpMenu();
                        menuBar.add(helpMenu);
                        break;
                    case "SHOW_UPDATE_WINDOW":
                        new UpdateAvailableWindow((JSONObject) latest.getData());
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
                    case "SET_TIP":
                        if (modList.isSelectionEmpty() && fieldDescription.getText().equals(selectAModDescription)) {
                            fieldDescription.setText(getNoSelectedModDescription());
                        }
                        break;
                }
            }
        }

        @Override
        protected void done() {
            submitJobCompletion(networkThreadCode);
            forceUpdateOnReloadList.clear(); // remove pending updates
            try {
                get();
            } catch (Exception e) {
                ModManager.debugLogger.writeErrorWithException("Exception in the network thread: ", e);
            }
        }

        private void checkForJREUpdates(JSONObject latest_object) {
            if (latest_object == null) {
                return;
            }

            String latestjavaexehash = (String) latest_object.get("jre_latest_version_v2");
            if (!ModManager.IS_DEBUG && latestjavaexehash != null) {
                boolean hashMismatch = false;
                File f = new File(ModManager.getBundledJREPath() + "bin\\java.exe");
                if (f.exists() && ModManager.isUsingBundledJRE()) {
                    // get hash
                    String bundledHash = "";
                    try {
                        bundledHash = MD5Checksum.getMD5Checksum(f.getAbsolutePath());
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Unable to hash java.exe. Due to the significance of this issue, we are not going to attempt a JRE update.",
                                e);
                        return;
                    }
                    if (!bundledHash.equals(latestjavaexehash)) {
                        hashMismatch = true;
                        ModManager.debugLogger.writeMessage("Bundled JRE hash does not match server - likely out of date.");
                    }
                } else {
                    // doesn't exist - failed hash check
                    hashMismatch = true;
                    ModManager.debugLogger.writeMessage("Bundled JRE does not exist, but we should be using one.");
                }

                if (hashMismatch) {
                    new UpdateJREAvailableWindow(latest_object);
                } else {
                    ModManager.debugLogger.writeMessage("No JRE upgrade being advertised");
                }
            }
        }

        private JSONObject checkForModManagerUpdates() {
            labelStatus.setText("Checking for Mod Manager updates");
            ModManager.debugLogger.writeMessage("Checking for update...");
            JSONObject latest_object = null;
            // Check for update
            try {
                String update_check_link = "https://me3tweaks.com/modmanager/updatecheck?currentversion=" + ModManager.BUILD_NUMBER;
                String serverJSON = null;
                try {
                    serverJSON = IOUtils.toString(new URL(update_check_link), StandardCharsets.UTF_8);
                    // System.out.println(update_check_link);
                } catch (Exception e) {
                    ModManager.debugLogger.writeError("Error checking for updates:");
                    ModManager.debugLogger.writeException(e);
                    labelStatus.setText("Error checking for update (check logs)");
                    ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
                    return latest_object;
                }
                if (serverJSON == null) {
                    ModManager.debugLogger.writeError("No response from server");
                    labelStatus.setText("Updater: No response from server");
                    ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
                    return latest_object;
                }

                JSONParser parser = new JSONParser();
                latest_object = (JSONObject) parser.parse(serverJSON);
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
                            if (buildHash != null) {
                                ModManager.debugLogger.writeMessage("Server build hash: " + buildHash);
                            } else {
                                ModManager.debugLogger.writeMessage("ME3Tweaks does not list a hash for this build. Skipping minor update check.");
                            }
                        }
                    } catch (Exception e1) {
                        ModManager.debugLogger.writeErrorWithException("Unable to hash ME3CMM.exe:", e1);
                    }
                } else {
                    ModManager.debugLogger.writeMessage("Skipping hash check for updates. Likely running in IDE or some other wizardry");
                }

                long latest_build = (long) latest_object.get("latest_build_number");

                String arch = ResourceUtils.is64BitWindows() ? "_x64" : "_x86";

                String latestCommandLineToolsLink = (String) latest_object.get("latest_commandlinetools_link" + arch);
                ModManager.LATEST_ME3EXPLORER_VERSION = (String) latest_object.get("latest_me3explorer_version");
                ModManager.LATEST_ME3EXPLORER_URL = (String) latest_object.get("latest_me3explorer_download_link");
                String commandLineToolsRequiredVersion = (String) latest_object.get("latest_commandlinetools_version");
                if (commandLineToolsRequiredVersion != null) {
                    try {
                        ModManager.MIN_REQUIRED_CMDLINE_REV = Integer.parseInt(commandLineToolsRequiredVersion);
                    } catch (NumberFormatException e) {
                        ModManager.debugLogger.writeMessage("Could not parse a command line tools requiredversion from online manifest. We will default to the built in value of "
                                + ModManager.MIN_REQUIRED_CMDLINE_REV);
                    }
                }

                if (latest_build < ModManager.BUILD_NUMBER) {
                    // build is newer than current
                    labelStatus.setVisible(true);
                    ModManager.debugLogger.writeMessage("No updates, at latest version. (or could not contact update server.)");
                    labelStatus.setText("No Mod Manager updates available");
                    ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
                    checkForGUIupdates(latest_object);
                    if (latestCommandLineToolsLink != null) {
                        ModManager.COMMANDLINETOOLS_URL = latestCommandLineToolsLink;
                    }
                    return latest_object;
                }
                if (latest_build == ModManager.BUILD_NUMBER && !hashMismatch) {
                    // build is same as server version
                    labelStatus.setVisible(true);
                    ModManager.debugLogger.writeMessage("No updates, at latest version.");
                    labelStatus.setText("Mod Manager is up to date");
                    ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
                    checkForGUIupdates(latest_object);
                    if (latestCommandLineToolsLink != null) {
                        ModManager.COMMANDLINETOOLS_URL = latestCommandLineToolsLink;
                    }
                    return latest_object;
                }

                ModManager.debugLogger
                        .writeMessage("Update check: Local:" + ModManager.BUILD_NUMBER + " Latest: " + latest_build + ", is less? " + (ModManager.BUILD_NUMBER < latest_build));

                boolean showUpdate = true;
                // make sure the user hasn't declined this one.
                Wini settingsini = ModManager.LoadSettingsINI();
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
                            try {
                                settingsini.store();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                ModManager.debugLogger.writeErrorWithException("Unable to save settings:", e);
                            }
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

                if (showUpdate) {
                    // An update is available!
                    publish(new ThreadCommand("SET_STATUSBAR_TEXT", "Mod Manager update available"));
                    publish(new ThreadCommand("SHOW_UPDATE_WINDOW", null, latest_object));
                    ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
                } else {
                    labelStatus.setVisible(true);
                    labelStatus.setText("Update notification suppressed until next build");
                }
                if (latestCommandLineToolsLink != null) {
                    ModManager.COMMANDLINETOOLS_URL = latestCommandLineToolsLink;
                }
                checkForGUIupdates(latest_object);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                ModManager.debugLogger.writeErrorWithException("Error parsing server response:", e);
            }
            ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = true;
            return latest_object;
        }

        /**
         * Parses additional info out of the latest server json related to GUI
         * library info
         *
         * @param serversJSON
         */
        private void checkForGUIupdates(JSONObject serversJSON) {
            Object obj = serversJSON.get("latest_guitransplanter");
            String uiLibPath = ModManager.getGUILibraryFor("DLC_CON_UIScaling", false);
            {
                File uiLibVersionFile = new File(uiLibPath + "libraryversion.txt");
                ModManager.debugLogger.writeMessage("Checking for UISCALING GUI library version file at " + uiLibVersionFile);
                if (uiLibVersionFile.exists()) {
                    try {
                        String uilibverstr = FileUtils.readFileToString(uiLibVersionFile, StandardCharsets.UTF_8);
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
                                    String newlibverstr = FileUtils.readFileToString(uiLibVersionFile, StandardCharsets.UTF_8);
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
        int jobCode;

        public SingleModUpdateCheckThread(Mod mod) {
            this.mod = mod;
            labelStatus.setText("Checking for " + mod.getModName() + " updates");
            jobCode = submitBackgroundJob("Checking for " + mod.getModName() + " updates");
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
                        submitJobCompletion(jobCode);
                        JOptionPane.showMessageDialog(ModManagerWindow.this, upackage.getMod().getModName()
                                + " has an update available from ME3Tweaks, but requires a sideloaded update first.\nAfter this dialog is closed, a browser window will open where you can download this sideload update.\nDrag and drop this downloaded file onto Mod Manager to install it.\nAfter the sideloaded update is complete, Mod Manager will download the rest of the update.\n\nThis is to save on bandwidth costs for both ME3Tweaks and the developer of "
                                + upackage.getMod().getModName() + ".", "Sideload update required", JOptionPane.WARNING_MESSAGE);
                        try {
                            ResourceUtils.openWebpage(new URL(upackage.getSideloadURL()));
                        } catch (MalformedURLException e) {
                            ModManager.debugLogger.writeError("Invalid sideload URL: " + upackage.getSideloadURL());
                            JOptionPane.showMessageDialog(ModManagerWindow.this,
                                    upackage.getMod().getModName() + " specified an invalid URL for it's sideload upload:\n" + upackage.getSideloadURL(), "Invalid Sideload URL",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        String updatetext = mod.getModName() + " has an update available from ME3Tweaks:\n";
                        updatetext += AllModsUpdateWindow.getVersionUpdateString(upackage);
                        if (upackage.getChangeLog() != null && !upackage.getChangeLog().equals("")) {
                            updatetext += "    Changelog: ";
                            updatetext += upackage.getChangeLog();
                            updatetext += "\n";
                        }
                        updatetext += "\nUpdate this mod?";
                        submitJobCompletion(jobCode);
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
            submitJobCompletion(jobCode);
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
        setTitle("Mass Effect 3 Mod Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImages(ModManager.ICONS);
        Dimension minSize = new Dimension(560, 520);
        setPreferredSize(minSize);
        setMinimumSize(minSize);
        ArrayList<String> directories = ModManager.getSavedBIOGameDirectories();
        ArrayList<String> displayedDirectories = new ArrayList<String>();
        DefaultComboBoxModel<String> biogameDirectoriesModel = new DefaultComboBoxModel<>();

        comboboxBiogameDir = new JComboBox<String>();
        comboboxBiogameDir.setModel(biogameDirectoriesModel);

        for (String dir : directories) {
            if (internalValidateBIOGameDir(dir)) {
                biogameDirectoriesModel.addElement(dir);
                displayedDirectories.add(dir);
            } else {
                ModManager.debugLogger.writeError("Saved biogame directory failed validation: " + dir);
            }
        }

        ModManager.debugLogger.writeMessage("Setting active biogame directory from registry...");
        String registrykey = ModManager.LookupGamePathViaRegistryKey(true);
        boolean useAddInstead = comboboxBiogameDir.getModel().getSize() > 0;
        if (registrykey != null) {
            registrykey = ResourceUtils.removeTrailingSlashes(registrykey);
            int index = displayedDirectories.indexOf(registrykey);
            String gamedir = new File(registrykey).getParent();
            if (index >= 0) {
                comboboxBiogameDir.setSelectedIndex(index);

            } else {
                biogameDirectoriesModel.insertElementAt(registrykey, 0);
                comboboxBiogameDir.setSelectedIndex(0);
                directories.add(registrykey);
                saveBiogamePath(directories);
            }
            boolean hasWritePermissions = ModManager.checkWritePermissions(gamedir) && ModManager.checkWritePermissions(gamedir + "\\BIOGame")
                    && ModManager.checkWritePermissions(gamedir + "\\BIOGame\\CookedPCConsole");
            if (!hasWritePermissions) {
                showFolderPermissionsGrantDialog(registrykey);
            }
        }

        if (comboboxBiogameDir.getSelectedItem() != null) {
            PRELOADED_BIOGAME_DIR = (String) comboboxBiogameDir.getSelectedItem();
        }
        BIOGAME_ITEM_LISTENER = new BiogameDirChangeListener();

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
                        // Verify all file types are the same.
                        String extension = FilenameUtils.getExtension(files[0].toString()).toLowerCase();
                        ArrayList<Path> paths = new ArrayList<>();
                        for (File file : files) {
                            String cextension = FilenameUtils.getExtension(file.getAbsolutePath());
                            if (!cextension.equals(extension)) {
                                labelStatus.setText("All dropped files must have same extension");
                                return;
                            }
                            paths.add(file.toPath());
                        }

                        String logMessage = "Files dropped onto Mod Manager window:";
                        for (Path p : paths) {
                            logMessage += "\n" + p.toString();
                        }
                        ModManager.debugLogger.writeMessage(logMessage);

                        File f = paths.get(0).toFile();

                        if (paths.get(0).toFile().isDirectory()) {
                            // prompt
                            new FileDropWindow(ModManagerWindow.this, paths);
                        } else {
                            switch (extension) {
                                case "7z":
                                case "zip":
                                case "rar":
                                    new ModImportArchiveWindow(ModManagerWindow.this, files[0].toString());
                                    break;
                                case "asi":
                                    ModManager.debugLogger.writeMessage("ASI was dropped onto ModManagerWindow - doing requirements check...");
                                    int exebuild = ModManager.checkforME3105(GetBioGameDir());
                                    if (exebuild != 5) {
                                        labelStatus.setText("ASI mods don't work with Mass Effect 3 1.0" + exebuild);
                                        break;
                                    }
                                    if (!ModManager.checkIfASIBinkBypassIsInstalled(GetBioGameDir())) {
                                        labelStatus.setText("Binkw32 ASI loader not installed, see tools menu");
                                        break;
                                    }
                                    String copyDest = ModManager.appendSlash(new File(ModManagerWindow.GetBioGameDir()).getParent()) + "Binaries\\win32\\asi\\" + files[0].getName();
                                    ModManager.debugLogger.writeMessage("ASI Source: " + files[0].getAbsolutePath());
                                    ModManager.debugLogger.writeMessage("ASI Dest:   " + copyDest);
                                    if (copyDest.equalsIgnoreCase(files[0].getAbsolutePath())) {
                                        ModManager.debugLogger.writeMessage("Source and destination files are the same.");
                                        ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Source and destination files are the same.");
                                        return;
                                    }
                                    ArrayList<Path> singlePath = new ArrayList<>();
                                    singlePath.add(paths.get(0));
                                    new FileDropWindow(ModManagerWindow.this, singlePath);
                                    break;
                                case "txt":
                                case "pcc":
                                case "xml":
                                    new FileDropWindow(ModManagerWindow.this, paths);
                                    break;
                                case "dlc":
                                    new MountFileEditorWindow(files[0].toString());
                                    break;
                                case "tlk":
                                    if (files.length == 1) {
                                        ModManager.debugLogger.writeMessage("Decompiling " + files[0]);
                                        TLKTool.decompileTLK(files[0]);
                                        labelStatus.setText("Decompiled " + files[0].getName());
                                    } else {
                                        new FileDropWindow(ModManagerWindow.this, paths);
                                    }
                                    break;
                                case "bin": // read magic at beginning to find out
                                    // what type // of // file this is
                                    if (files.length == 1) {
                                        byte[] buffer = new byte[4];

                                        try (InputStream is = new FileInputStream(files[0])) {
                                            if (is.read(buffer) != buffer.length) { // do something

                                                ModManager.debugLogger.writeMessage("Dropped file not a coalesced file.");
                                                labelStatus.setText("Dropped file is not a coalesced file");
                                                break;
                                            }
                                            int magic = ResourceUtils.byteArrayToInt(buffer);
                                            switch (magic) {
                                                case ModManager.COALESCED_MAGIC_NUMBER:
                                                    new CoalescedWindow(files[0], false);
                                                    break;
                                            }

                                            is.close();
                                        } catch (IOException e) {
                                            ModManager.debugLogger.writeErrorWithException("I/O Exception reading coalesced magic number:", e);
                                            labelStatus.setText("Dropped file is not a coalesced file");
                                        }
                                    } else {
                                        new FileDropWindow(ModManagerWindow.this, paths);
                                    }
                                    break;
                                default:
                                    labelStatus.setText("Extension not supported for Drag & Drop: " + extension);
                                    break;
                            }
                        }
                    }
                } else {
                    labelStatus.setText("Drag and Drop requires a valid BioGame directory");
                    JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory to use drag and drop features.",
                            "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // North Panel
        JPanel northPanel = new JPanel(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        String titleVal = "Mass Effect 3 Mod Manager " + ModManager.VERSION + (ModManager.IS_DEBUG ? " [DEBUG MODE]" : "");
        if (!ResourceUtils.is64BitWindows()) {
            titleVal += " (final 32-bit windows version)";
        }
        titlePanel.add(new JLabel(titleVal, SwingConstants.LEFT), BorderLayout.WEST);

        // BioGameDir Panel
        cookedDirPanel = new JPanel(new BorderLayout());
        TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Mass Effect 3 BIOGame Directory (Installation Target)");

        buttonBioGameDir = new JButton(useAddInstead ? "Add Target" : "Browse...");
        buttonBioGameDir.setToolTipText("<html>Add a new game installation for installing mods to.</html>");
        buttonBioGameDir.setPreferredSize(new Dimension(useAddInstead ? 105 : 90, 14));

        buttonBioGameDir.addActionListener(this);
        cookedDirPanel.setBorder(cookedDirTitle);
        cookedDirPanel.add(comboboxBiogameDir, BorderLayout.CENTER);
        cookedDirPanel.add(buttonBioGameDir, BorderLayout.EAST);

        northPanel.add(titlePanel, BorderLayout.NORTH);
        northPanel.add(cookedDirPanel, BorderLayout.CENTER);

        ModManager.debugLogger.writeMessage("Configuring modList and modModel");
        // ModsList
        JPanel modsListPanel = new JPanel(new BorderLayout());
        // JLabel availableModsLabel = new JLabel(" Available Mods:");
        TitledBorder modListsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Available Mods");

        modList = new JList<Mod>();
        modList.addListSelectionListener(this);
        modList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modList.setLayoutOrientation(JList.VERTICAL);
        modList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    modList.setSelectedIndex(modList.locationToIndex(e.getPoint()));
                    int selectedIndex = modList.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        Mod mod = modModel.get(selectedIndex);

                        JPopupMenu menu = new JPopupMenu();
                        ArrayList<Component> menuItems = buildModUtilsMenu(mod);
                        for (Component item : menuItems) {
                            menu.add(item);
                        }
                        menu.show(modList, e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });
        modList.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (modList.getSelectedIndex() >= 0) {
                        deleteMod(modModel.getElementAt(modList.getSelectedIndex()), true);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        JScrollPane listScroller = new JScrollPane(modList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        modlistFailedIndicatorLink = new JButton();
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

        // modsListPanel.add(availableModsLabel, BorderLayout.NORTH);
        modsListPanel.setBorder(modListsBorder);
        modsListPanel.add(listScroller, BorderLayout.CENTER);
        modsListPanel.add(modlistFailedIndicatorLink, BorderLayout.SOUTH);
        modModel = new DefaultListModel<Mod>();
        modList.setModel(modModel);

        // load patches
        setPatchList(ModManager.getPatchesFromDirectory());
        ModManager.debugLogger.writeMessage("Mixins have loaded.");

        // DescriptionField
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        // JLabel descriptionLabel = new JLabel("Mod Description:");
        TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description");
        descriptionPanel.setBorder(descriptionBorder);
        fieldDescription = new JTextArea(getNoSelectedModDescription());
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
            buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.7.1 or higher in order to install mods");
        }
        buttonStartGame = new JButton("Start Game");
        buttonStartGame.addActionListener(this);
        buttonStartGame.setToolTipText("<html>Starts the game and minimizes Mod Manager.</html>");

        buttonPanel.add(buttonApplyMod);
        buttonPanel.add(buttonStartGame);

        JPanel statusPanel = new JPanel(new BorderLayout());
        activityPanel = new JXCollapsiblePane(Direction.LEFT);
        activityPanel.add(new JLabel(ModManager.ACTIVITY_ICON));
        labelStatus.setBorder(new EmptyBorder(3, 3, 3, 3));
        statusPanel.add(activityPanel, BorderLayout.WEST);
        statusPanel.add(labelStatus, BorderLayout.CENTER);

        applyPanel.add(statusPanel, BorderLayout.WEST);
        applyPanel.add(buttonPanel, BorderLayout.EAST);

        southPanel.add(applyPanel, BorderLayout.SOUTH);

        // add all panels
        contentPanel.add(northPanel, BorderLayout.NORTH);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(southPanel, BorderLayout.SOUTH);
        this.setJMenuBar(menuBar);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        this.add(contentPanel);
        reloadModlist();
        if (modModel.size() == 0) {
            fieldDescription
                    .setText("No mods are available in the Mod Manager library. Download some ModMaker mods or import mods through the Mod Management menu to get started.");
        }

        updateBinkBypassStatus();

        comboboxBiogameDir.addItemListener(new BiogameDirChangeListener());
        ModManager.debugLogger.writeMessage("Mod Manager GUI: SetupWindow() has completed.");
    }

    /**
     * Reloads mods from disk and updates the UI.
     */
    public void reloadModlist() {
        modModel.clear();
        ModManager.debugLogger.writeMessage("Reloading mods...");
        ModList ml = ModManager.getModsFromDirectory();
        ArrayList<Mod> validMods = ml.getValidMods();
        invalidMods = ml.getInvalidMods();
        ModManager.debugLogger
                .writeMessage(validMods.size() + invalidMods.size() + " mods have loaded. " + validMods.size() + " are valid, " + invalidMods.size() + " are invalid.");

        for (Mod mod : validMods) {
            modModel.addElement(mod);
        }

        modlistFailedIndicatorLink
                .setText("<HTML><font color=#ff2020><u>" + invalidMods.size() + " mod" + (invalidMods.size() != 1 ? "s" : "") + " failed to load</u></font></HTML>");
        modlistFailedIndicatorLink.setVisible(invalidMods.size() > 0);
        if (loadedFirstTime) {
            labelStatus.setText("Reloaded mods");
        } else {
            loadedFirstTime = true;
        }
    }

    protected void updateApplyButton() {
        if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
            buttonApplyMod.setText("Apply Mod");
            if (modList.getSelectedIndex() == -1) {
                buttonApplyMod.setToolTipText("Select a mod on the left");
                buttonApplyMod.setEnabled(false);
            } else {
                buttonApplyMod.setToolTipText("<html>Applies this mod to the game.<br>Not all mods are compatible with each other.</html>");
                buttonApplyMod.setEnabled(true);

            }
        } else {
            buttonApplyMod.setText(".NET Missing");
            buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.7.1 or higher in order to install mods");
            buttonApplyMod.setEnabled(false);
        }
    }

    private JMenuBar makeMenu() {
        menuBar = new JMenuBar();
        // Actions
        actionMenu = new JMenu("Actions");
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

        toolAlotInstaller = new JMenuItem("ALOT Installer");
        if (ResourceUtils.is64BitWindows()) {
            toolAlotInstaller.setToolTipText(
                    "<html>Runs the ALOT installer.<br>The ALOT installer allows you to install high resolution texture packs for all three Mass Effect Trilogy games. It also includes ALOT Installer, a texturing program.<br>Note: This is an external program.</html>");
        } else {
            toolAlotInstaller.setToolTipText("<html>ALOT Installer requires 64-bit Windows</html>");
            toolAlotInstaller.setEnabled(false);
        }
        toolME3Explorer = new JMenuItem("ME3Explorer (ME3Tweaks Fork)");
        toolME3Explorer.setToolTipText(
                "<html>Runs ME3Explorer.<br>ME3Explorer is the primary tool for creating and editing Mass Effect 3 content.<br>Note: This is an external program and only the ME3Tweaks additions are supported by ME3Tweaks.</html>");
        toolTankmasterCoalUI = new JMenuItem("TankMaster Coalesce Interface");
        toolTankmasterCoalUI.setToolTipText("Opens interface for Tankmaster's Coalesced compiler");
        toolTankmasterTLK = new JMenuItem("TankMaster ME2/ME3 TLK Tool");
        toolTankmasterTLK.setToolTipText("Runs the bundled TLK tool provided by TankMaster");
        toolsAutoTOCGame = new JMenuItem("Run AutoTOC on game");
        toolsAutoTOCGame.setToolTipText("<html>Updates TOC files for basegame, DLC (unpacked and modified SFAR), and custom DLC.<br>May help fix infinite loading issues.</html>");

        actionReload = new JMenuItem("Reload Mod Manager");
        actionReload.setToolTipText("Reloads Mod Manager to refresh mods, mixins, and help documentation");
        actionExit = new JMenuItem("Exit");
        actionExit.setToolTipText("Closes Mod Manager");

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
        modManagementModGroupsManager = new JMenuItem("Batch Mod Installer");
        modManagementModGroupsManager.setToolTipText("<html>Installs mods in batch mod using mod groups.</html>");

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
        modManagementMenu.add(modManagementModGroupsManager);
        modManagementMenu.add(modManagementOpenModsFolder);
        modManagementMenu.addSeparator();
        modManagementMenu.add(modManagementClearPatchLibraryCache);
        menuBar.add(modManagementMenu);

        modManagementClearPatchLibraryCache.addActionListener(this);
        modManagementOpenModsFolder.addActionListener(this);
        modManagementFailedMods.addActionListener(this);
        modManagementImportFromArchive.addActionListener(this);
        modManagementImportAlreadyInstalled.addActionListener(this);
        modManagementModGroupsManager.addActionListener(this);

        modUtilsMenu = new JMenu("Mod Utils");
        modUtilsMenu.setEnabled(false);
        menuBar.add(modUtilsMenu);
        // Tools
        toolsMenu = new JMenu("Tools");

        // toolsMergeMod = new JMenuItem("Mod Merging Utility");
        // toolsMergeMod.setToolTipText(
        // "<html>Allows you to merge Mod Manager mods together and resolve conflicts
        // between mods<br>This tool is deprecated and may be removed in the
        // future</html>");

        toolsOpenME3Dir = new JMenuItem("Open BIOGame directory");
        toolsOpenME3Dir.setToolTipText("Opens a Windows Explorer window in the BIOGame Directory");

        toolsGrantWriteAccess = new JMenuItem("Grant write access to selected game directory");
        toolsGrantWriteAccess.setToolTipText("Attempts to grant your user account write access to the listed Mass Effect 3 game directory");

        toolsInstallBinkw32asi = new JCheckBoxMenuItem("Install Binkw32 ASI DLC Bypass");
        toolsInstallBinkw32asi.setToolTipText(
                "<html>Installs a startup patcher giving you console and allowing modified DLC.<br>This version allows loading of advanced ASI mods that allow 3rd party game plugins to run on your machine.<br>This modifies your game and is erased when doing an Origin Repair</html>");

        toolsUninstallBinkw32 = new JMenuItem("Uninstall Binkw32 DLC Bypass");
        toolsUninstallBinkw32.setToolTipText("<html>Removes the Binkw32.dll DLC bypass, reverting to the original file</html>");

        toolsUnpackDLC = new JMenuItem("DLC Unpacker");
        toolsUnpackDLC.setToolTipText("Unpack DLC SFARs for quick and easy modding");

        toolMountdlcEditor = new JMenuItem("Mount.dlc Editor");
        toolMountdlcEditor.setToolTipText("Allows you to modify or create new Mount.dlc files");

        toolME3Config = new JMenuItem("ME3 Config Tool");
        toolME3Config.setToolTipText("<html>Opens the ME3 Configuration Utility that comes packaged with the game.<br>Lets you configure graphics and audio settings.</html>");

        toolsPCCDataDumper = new JMenuItem("PCC Data Dumper");
        toolsPCCDataDumper
                .setToolTipText("<html>Parses PCC informtation (such as scripts, properties, coalesced, etc)<br>and dumps it into an easily searchable text format.</html>");

        mountMenu = new JMenu("Manage [PLACEHOLDER] Mount files");
        mountMenu.setVisible(false);

        toolsAutoTOCGame.addActionListener(this);
        modManagementModMaker.addActionListener(this);
        modManagementASI.addActionListener(this);
        modManagementConflictDetector.addActionListener(this);
        // toolsMergeMod.addActionListener(this);
        modManagementCheckallmodsforupdate.addActionListener(this);
        toolsUnpackDLC.addActionListener(this);
        toolsGrantWriteAccess.addActionListener(this);
        modManagementPatchLibary.addActionListener(this);
        toolMountdlcEditor.addActionListener(this);

        // DEV
        devMenu = new JMenu("Mod Development");
        modDevStarterKit = new JMenuItem("Generate a Custom DLC Starter Kit");
        modDevStarterKit.addActionListener(this);
        modDevStarterKit.setToolTipText("Generate a barebones Custom DLC mod that is immediately ready to use");
        moddevOfficialDLCManager = new JMenuItem("Official DLC Toggler");
        moddevOfficialDLCManager.addActionListener(this);
        moddevOfficialDLCManager.setToolTipText("Allows you to quickly enable or disable official BioWare DLC for testing");
        moddevUpdateXMLGenerator = new JMenuItem("Prepare mod for ME3Tweaks Updater Service");
        moddevUpdateXMLGenerator.setToolTipText("No mod is currently selected");
        moddevUpdateXMLGenerator.setEnabled(false);
        moddevUpdateXMLGenerator.addActionListener(this);
        devMenu.add(mountMenu);
        devMenu.add(toolMountdlcEditor);
        devMenu.add(modDevStarterKit);
        devMenu.add(moddevOfficialDLCManager);
        devMenu.add(moddevUpdateXMLGenerator);
        devMenu.add(toolTankmasterCoalUI);
        devMenu.add(toolTankmasterTLK);

        toolsOpenME3Dir.addActionListener(this);
        toolsInstallBinkw32asi.addActionListener(this);
        toolsUninstallBinkw32.addActionListener(this);
        toolME3Config.addActionListener(this);
        toolsPCCDataDumper.addActionListener(this);
        toolME3Explorer.addActionListener(this);
        toolAlotInstaller.addActionListener(this);
        toolTankmasterTLK.addActionListener(this);
        toolTankmasterCoalUI.addActionListener(this);

        parsersMenu = new JMenu("Coalesced Parsers");
        sqlWavelistParser = new JMenuItem("Wavelist Parser");
        sqlDifficultyParser = new JMenuItem("Biodifficulty Parser");
        sqlAIWeaponParser = new JMenuItem("BioAI Parser");
        sqlPowerCustomActionParser = new JMenuItem("CustomAction Parser");
        sqlPowerCustomActionParser2 = new JMenuItem("CustomAction Editor");

        sqlWavelistParser.addActionListener(this);
        sqlDifficultyParser.addActionListener(this);
        sqlAIWeaponParser.addActionListener(this);
        sqlPowerCustomActionParser.addActionListener(this);
        // sqlPowerCustomActionParser2.addActionListener(this); //Outputs SQL only.
        // Disabled.

        parsersMenu.add(sqlWavelistParser);
        parsersMenu.add(sqlDifficultyParser);
        parsersMenu.add(sqlAIWeaponParser);
        parsersMenu.add(sqlPowerCustomActionParser);

        toolsMenu.add(toolsUnpackDLC);
        toolsMenu.add(toolsAutoTOCGame);
        toolsMenu.add(toolsOpenME3Dir);
        toolsMenu.add(toolsGrantWriteAccess);
        toolsMenu.addSeparator();
        toolsMenu.add(toolME3Config);
        toolsMenu.add(toolAlotInstaller);
        toolsMenu.add(toolME3Explorer);
        toolsMenu.add(toolsPCCDataDumper);
        // toolsMenu.add(parsersMenu); not enough content for this to be useful.
        toolsMenu.add(devMenu);
        toolsMenu.addSeparator();
        toolsMenu.add(toolsInstallBinkw32asi);
        toolsMenu.add(toolsUninstallBinkw32);
        menuBar.add(toolsMenu);

        // BACKUP
        backupMenu = new JMenu("Backup");

        backupBackupDLC = new JMenuItem("Backup DLCs");
        backupBackupDLC.setToolTipText("Backs up your DLC to .bak files. When installing a mod it will ask if a .bak files does not exist if you want to backup");

        backupBasegameUnpacked = new JMenuItem("Backup basegame/unpacked files");
        backupBasegameUnpacked.setToolTipText("An Unpacked and basegame file will be automatically backed up when Mod Manager replaces or removes that file");

        backupCreateVanillaCopy = new JCheckBoxMenuItem("Create complete game backup (Unmodified)");
        backupCreateVanillaCopy
                .setToolTipText("<html>Create an entire copy of the game so you can do a complete restore in the future.<br>Useful if you are doing texture modding.</html>");
        if (VanillaBackupWindow.GetFullBackupPath(false) != null) {
            backupCreateVanillaCopy.setSelected(true);
            backupCreateVanillaCopy.setText("Game has been fully backed up");
            backupCreateVanillaCopy
                    .setToolTipText("<html>The game has been fully backed up in an unmodified state.<br>You can restore the entire game using the Restore Menu.</html>");
        }

        backupCreateGDB = new JMenuItem("Update game repair database");
        backupCreateGDB.setToolTipText(
                "Creates/updates a database of checksums for basegame and unpacked DLC files.\nMod Manager uses this database for verifying restoring and backing up");

        backupBackupDLC.addActionListener(this);
        backupBasegameUnpacked.addActionListener(this);
        backupCreateGDB.addActionListener(this);
        backupCreateVanillaCopy.addActionListener(this);

        backupMenu.add(backupBackupDLC);
        backupMenu.add(backupCreateVanillaCopy);
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

        restoreVanillaCopy = new JMenuItem("Restore game to vanilla");
        restoreVanillaCopy.setToolTipText("<html>Restore your game from a previously created vanilla backup</html>");

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
        restoreVanillaCopy.addActionListener(this);

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
        restoreMenu.add(restoreVanillaCopy);
        restoreMenu.addSeparator();
        restoreMenu.add(restoreMenuAdvanced);
        menuBar.add(restoreMenu);

        helpMenu = HelpMenu.constructHelpMenu();
        menuBar.add(helpMenu);

        updateBinkBypassStatus();

        return menuBar;
    }

    private void updateBinkBypassStatus() {
        if (ModManager.checkIfASIBinkBypassIsInstalled(GetBioGameDir())) {
            toolsInstallBinkw32asi.setSelected(true);
            toolsInstallBinkw32asi.setText("Binkw32 ASI DLC bypass is installed");
        } else {
            toolsInstallBinkw32asi.setSelected(false);
            toolsInstallBinkw32asi.setText("Install Binkw32 ASI DLC Bypass");
        }
        // no bypass installed
        toolsUninstallBinkw32.setVisible(ModManager.checkIfASIBinkBypassIsInstalled(GetBioGameDir()));
    }

    private ArrayList<Component> buildModUtilsMenu(final Mod mod) {
        JMenuItem modutilsHeader = new JMenuItem(mod.getModName());
        modutilsHeader.setEnabled(false);
        JMenuItem modutilsInstallCustomKeybinds = new JMenuItem("Install custom keybinds into this mod");
        // check if BioInput.xml exists.
        if (!checkForKeybindsOverride()) {
            // ModManager.debugLogger.writeMessage("No keybinds file in the override
            // directory (bioinput.xml)");
            modutilsInstallCustomKeybinds.setEnabled(false);
            modutilsInstallCustomKeybinds.setToolTipText("<html>To enable installing custom keybinds put a<br>BioInput.xml file in the data/override/ directory.</html>");
        } else {
            // ModManager.debugLogger.writeMessage("Found keybinds file in the override
            // directory (bioinput.xml)");
            modutilsInstallCustomKeybinds.setToolTipText("<html>Replace BioInput.xml in the BASEGAME Coalesced file</html>");
        }

        JMenuItem modutilsInfoEditor = new JMenuItem("Edit name/description/site");
        modutilsInfoEditor.setToolTipText("Rename this mod and change the description/site shown in the description pane");

        // Variants
        JMenuItem modNoDeltas = new JMenuItem("No included variants");
        modNoDeltas.setToolTipText("<html>Variants are Coalesced patches that can make small changes like turning on motion blur.<br>See the FAQ on how to create them.</html>");
        modNoDeltas.setEnabled(false);

        JMenuItem modutilsVerifyDeltas = new JMenuItem("Verify variants");
        modutilsVerifyDeltas.setToolTipText("<html>Verifies all parts of deltas are applicable to mod</html>");

        JMenuItem modDeltaRevert = new JMenuItem("Revert to original version");
        modDeltaRevert.setToolTipText("<html>Restores the mod to the original version, without variants applied</html>");

        int numVariablesAvailable = mod.getModDeltas().size();
        JMenu modDeltaMenu = new JMenu(numVariablesAvailable + " available variant" + ((numVariablesAvailable != 1) ? "s" : ""));
        modDeltaMenu.setToolTipText(
                "<html>This mod has variants that allow quick changes to the mod without shipping a full new version.<br>Variants are Coalesced patches that can make small changes like turning on motion blur.<br>See the FAQ on how to create them.</html>");
        modDeltaMenu.setVisible(false);
        if (numVariablesAvailable > 0) {
            modDeltaMenu.add(modDeltaRevert);
            modDeltaMenu.add(modutilsVerifyDeltas);
            File originalVariantFolder = new File(mod.getModPath() + Mod.VARIANT_FOLDER + File.separator + Mod.ORIGINAL_FOLDER);
            modDeltaRevert.setEnabled(originalVariantFolder.exists());
            if (originalVariantFolder.exists()) {
                modDeltaRevert.setToolTipText("Revert to the original version of this mod");
            } else {
                modDeltaRevert.setToolTipText("No variants have been applied yet, so this is the original");
            }

            modDeltaMenu.addSeparator();
            modDeltaMenu.setText(mod.getModDeltas().size() + " available variant" + (mod.getModDeltas().size() != 1 ? "s" : ""));
            modDeltaMenu.setVisible(true);
            modNoDeltas.setVisible(false);
            for (ModDelta delta : mod.getModDeltas()) {
                JMenuItem deltaItem = new JMenuItem(delta.getDeltaName());
                deltaItem.setToolTipText("<html>" + delta.getDeltaDescription() + "</html>");
                deltaItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ModManager.debugLogger.writeMessage("Applying delta " + delta.getDeltaName() + " to " + mod.getModName());
                        new DeltaWindow(mod, delta, false, false);
                        modDeltaRevert.setEnabled(true);
                        modDeltaRevert.setText("Revert to original version");
                        modDeltaRevert.setToolTipText("<html>Restores the mod to the original version, without variants applied</html>");
                    }

                });
                modDeltaMenu.add(deltaItem);
            }
        }

        // ALTERNATES
        JMenu modAlternatesMenu = new JMenu();
        modAlternatesMenu.removeAll();

        // Count number of alternates
        ArrayList<AlternateFile> alts = mod.getAlternateFiles();
        ArrayList<AlternateCustomDLC> altdlcs = mod.getAlternateCustomDLC();
        int numoptions = altdlcs.size() + alts.size();

        for (ModJob job : mod.getJobs()) {
            if (job.getJobType() == ModJob.CUSTOMDLC) {
                continue; // don't parse these
            }
            numoptions += job.getAlternateFiles().size();
        }

        if (numoptions > 0) {
            modAlternatesMenu.setEnabled(true);
            modAlternatesMenu.setText(numoptions + " alternate installation option" + (numoptions != 1 ? "s" : ""));
            if (numoptions > 0) {
                modAlternatesMenu.setToolTipText("<html>This mod has " + numoptions + " additional installation configuration" + (numoptions != 1 ? "s" : "") + "</html>");
            }

            ArrayList<AlternateFile> autoAlts = mod.getApplicableAutomaticAlternates(GetBioGameDir());
            for (AlternateFile af : alts) { //alts not autoalts.
                String friendlyname = af.getOperation() + " due to " + af.getCondition() + " for " + af.getConditionalDLC();
                if (af.getFriendlyName() != null) {
                    friendlyname = af.getFriendlyName();
                }
                StayOpenJCheckboxMenuItem item = new StayOpenJCheckboxMenuItem(friendlyname);
                item.setToolTipText(af.getDescription());
                item.setEnabled(af.getCondition().equals(AlternateFile.CONDITION_MANUAL));
                if (autoAlts.contains(af)) {
                    item.setSelected(true);
                }
                item.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        af.setHasBeenChosen(item.isSelected());
                        ModManager.debugLogger.writeMessage(
                                "[" + mod.getModName() + "] User has toggled an optional CUSTOMDLC ALTFILE addin " + item.getText() + " to " + (item.isSelected() ? "on" : "off") + ".");
                        labelStatus.setText(item.getText() + " set to " + (item.isSelected() ? "enabled" : "disabled"));
                    }
                });
                modAlternatesMenu.add(item);
            }

            // Populate Manual Alternate Files for Official DLC
            for (ModJob job : mod.getJobs()) {
                if (job.getJobType() == ModJob.CUSTOMDLC) {
                    continue; // don't parse these
                }
                for (AlternateFile af : job.getAlternateFiles()) {
                    String friendlyname = af.getOperation() + " due to " + af.getCondition() + " for " + job.getJobName();
                    if (af.getFriendlyName() != null) {
                        friendlyname = af.getFriendlyName();
                    }
                    StayOpenJCheckboxMenuItem item = new StayOpenJCheckboxMenuItem(friendlyname);
                    item.setToolTipText(af.getDescription());
                    item.setEnabled(true); // Can only do
                    // CONDITION_MANUAL
                    item.setSelected(af.isEnabled());
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            af.setHasBeenChosen(item.isSelected());
                            ModManager.debugLogger.writeMessage(
                                    "[" + mod.getModName() + "] User has toggled an optional ALTFILE addin " + item.getText() + " to " + (item.isSelected() ? "on" : "off") + ".");
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
                    StayOpenJCheckboxMenuItem item = new StayOpenJCheckboxMenuItem(friendlyname);
                    item.setToolTipText(altdlc.getDescription());
                    if (!altdlc.getCondition().equals(AlternateCustomDLC.CONDITION_MANUAL)) {
                        item.setEnabled(false);
                    } else {
                        item.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ModManager.debugLogger.writeMessage("[" + mod.getModName() + "] User has toggled an optional ALTDLC addin " + item.getText() + " to "
                                        + (item.isSelected() ? "on" : "off") + ".");
                                altdlc.setHasBeenChosen(item.isSelected());
                            }
                        });
                    }
                    if (mod.getAppliedAutomaticAlternateCustomDLC().contains(altdlc)) {
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
            modAlternatesMenu.setToolTipText("This mod has no altnate installation options defined for it");
        }

        JMenuItem modutilsAutoTOC = new JMenuItem("Run AutoTOC on this mod");
        modutilsAutoTOC.setToolTipText("Automatically update all TOC files this mod uses with proper sizes to prevent crashes");

        // UPDATES CHECK
        JMenuItem modutilsRestoreMod = new JMenuItem("Restore mod from ME3Tweaks");
        modutilsRestoreMod.setToolTipText("Forces mod to update (even if on latest version) which restores mod to vanilla state (from ME3Tweaks)");
        JMenuItem modutilsCheckforupdate = new JMenuItem();
        if (mod.isME3TweaksUpdatable()) {
            modutilsCheckforupdate.setEnabled(true);
            modutilsCheckforupdate.setText("Check for updates");
            modutilsCheckforupdate.setToolTipText("Checks for updates to this mod from ME3Tweaks");
            modutilsRestoreMod.setVisible(true);
            if (mod.getModMakerCode() > 0) {
                moddevUpdateXMLGenerator.setEnabled(false);
                moddevUpdateXMLGenerator.setToolTipText("ModMaker mods will update when a new revision is published on ME3Tweaks ModMaker");
                moddevUpdateXMLGenerator.setText("Prepare " + mod.getModName() + " for ME3Tweaks Updater Service");
            } else {
                moddevUpdateXMLGenerator.setEnabled(true);
                moddevUpdateXMLGenerator.setText("Prepare " + mod.getModName() + " for ME3Tweaks Updater Service");
                moddevUpdateXMLGenerator.setToolTipText("Compresses mod files for storage on ME3Tweaks and generates a mod manifest. Copies to clipboard when complete.");
            }
        } else {
            modutilsRestoreMod.setVisible(false);
            modutilsCheckforupdate.setEnabled(false);
            modutilsCheckforupdate.setText("Mod not eligible for updates");
            moddevUpdateXMLGenerator.setEnabled(false);
            moddevUpdateXMLGenerator.setToolTipText(mod.getModName() + " does not have a ME3Tweaks update code");
            moddevUpdateXMLGenerator.setText("Cannot prepare " + mod.getModName() + " for ME3Tweaks Updater Service");
            modutilsCheckforupdate.setToolTipText("<html>Mod update eligibility requires a floating point version number<br>and an update code from ME3Tweaks</html>");
        }

        // DEVELOPER MENU
        JMenu modDeveloperMenu = new JMenu("Developer options");

        // MODDESC EDITOR
        JMenuItem modutilsDeploy = new JMenuItem("Deploy Mod");
        modutilsDeploy.setToolTipText("<html>Prepares the mod for deployment.<br>Stages only files used by the mod, AutoTOC's, then compresses the mod.</html>");

        // DEPLOYMENT
        JMenuItem modutilsModdescEditor = new JMenuItem("Installation editor (moddesc)");
        modutilsDeploy.setToolTipText("<html>Prepares the mod for deployment.<br>Stages only files used by the mod, AutoTOC's, then compresses the mod.</html>");

        // OPEN MOD FOLDER
        JMenuItem modutilsOpenFolder = new JMenuItem("Open mod folder");
        modutilsOpenFolder.setToolTipText("<html>Opens this mod's folder in File Explorer.<br>" + mod.getModPath() + "</html>");

        modDeveloperMenu.add(modutilsModdescEditor);
        modDeveloperMenu.add(modutilsDeploy);

        // DELETE MOD
        JMenuItem modutilsDeleteMod = new JMenuItem("Delete mod from library");
        modutilsDeleteMod.setToolTipText("<html>Delete this mod from Mod Manager.<br>This does not remove this mod if it is installed</html>");

        ArrayList<Component> menuItems = new ArrayList<>();
        menuItems.add(modutilsHeader);
        menuItems.add(modutilsCheckforupdate);
        menuItems.add(modutilsRestoreMod);
        menuItems.add(new JSeparator());
        menuItems.add(modDeltaMenu);
        menuItems.add(modNoDeltas);
        menuItems.add(modAlternatesMenu);
        menuItems.add(new JSeparator());
        menuItems.add(modutilsInstallCustomKeybinds);
        menuItems.add(modutilsInfoEditor);
        menuItems.add(modutilsAutoTOC);
        menuItems.add(modDeveloperMenu);
        menuItems.add(new JSeparator());
        menuItems.add(modutilsOpenFolder);
        menuItems.add(modutilsDeleteMod);

        modutilsModdescEditor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(ModManagerWindow.this, "This tool is under construction and is not fully functional\nor stable in this build yet. Use at your own risk!");
                new ModDescEditorWindow(mod);
            }
        });

        modutilsCheckforupdate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mod.getModMakerCode() <= 0 || validateBIOGameDir()) {
                    if (mod.getModMakerCode() <= 0 || ModManager.validateNETFrameworkIsInstalled()) {
                        ModManager.debugLogger.writeMessage("Running single mod update check on " + mod.getModName());
                        new SingleModUpdateCheckThread(mod).execute();
                    } else {
                        updateApplyButton();
                        labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                        ModManager.debugLogger.writeMessage("Single mode updater: Missing .NET Framework");
                        new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 to update ModMaker mods.");
                    }
                } else {
                    labelStatus.setText("Updating ModMaker mods requires valid BIOGame");
                    labelStatus.setVisible(true);
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
                            "The BIOGame directory is not valid.\nCannot update ModMaker mods without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
                            "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        modutilsRestoreMod.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mod.getModMakerCode() <= 0 || validateBIOGameDir()) {
                    if (mod.getModMakerCode() <= 0 || ModManager.validateNETFrameworkIsInstalled()) {
                        ModManager.debugLogger.writeMessage("Running (restore mode) single mod update check on " + mod.getModName());
                        Mod cloneMod = new Mod(mod); // create clone
                        cloneMod.setVersion(0.001);
                        new SingleModUpdateCheckThread(cloneMod).execute();
                    } else {
                        updateApplyButton();
                        labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                        ModManager.debugLogger.writeMessage("Single mode updater: Missing .NET Framework");
                        new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 to update ModMaker mods.");
                    }
                } else {
                    labelStatus.setText("Updating ModMaker mods requires valid BIOGame");
                    labelStatus.setVisible(true);
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
                            "The BIOGame directory is not valid.\nCannot update ModMaker mods without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
                            "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        modutilsVerifyDeltas.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Verifying deltas");
                    for (ModDelta delta : mod.getModDeltas()) {
                        new DeltaWindow(mod, delta, true, false);
                    }
                } else {
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Patch Library: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher to switch mod variants.");
                }
            }
        });
        modDeltaRevert.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Reverting a delta.");
                    new DeltaWindow(mod);
                } else {
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Revert Delta: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher to switch mod variants.");
                }
            }
        });

        modutilsInstallCustomKeybinds.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    updateApplyButton();
                    new KeybindsInjectionWindow(ModManagerWindow.this, mod, false);
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Keybinds Injector: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to use the Keybinds Injector.");
                }
            }
        });
        modutilsInfoEditor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ModInfoEditorWindow(mod);
            }
        });
        modutilsAutoTOC.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Running AutoTOC.");
                    updateApplyButton();
                    autoTOC(AutoTocWindow.LOCALMOD_MODE);
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("AutoTOC: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to use the AutoTOC feature.");
                }
            }
        });
        modutilsDeleteMod.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ModManager.debugLogger.writeMessage("User clicked Delete Mod on " + mod.getModName());
                deleteMod(mod,true);
            }
        });

        modutilsDeploy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ModManager.debugLogger.writeMessage("Running autotoc before compressing mod");
                new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, GetBioGameDir());

                ModManager.debugLogger.writeMessage("Compressing mod for deployment: " + mod.getModPath());
                new ModDeploymentThread(mod).execute();

            }
        });

        modutilsOpenFolder.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceUtils.openFolderInExplorer(mod.getModPath());
            }
        });

        return menuItems;
    }

    private void deleteMod(Mod mod, boolean prompt) {
        int result = JOptionPane.OK_OPTION;
        if (prompt) {
            result = JOptionPane.showConfirmDialog(ModManagerWindow.this,
                    "Deleting this mod will remove it from Mod Manager's library.\nThis does not remove the mod if it is installed.\nThis operation cannot be reversed.\nDelete "
                            + mod.getModName() + "?",
                    "Confirm Mod Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (result == JOptionPane.OK_OPTION) {
            ModManager.debugLogger.writeMessage("Deleting mod: " + mod.getModPath());
            if (FileUtils.deleteQuietly(new File(mod.getModPath()))) {
                modWebsiteLink.setVisible(false);
                reloadModlist();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        // too bad we can't do a switch statement on the object :(
        if (e.getSource() == buttonBioGameDir) {
            Platform.runLater(() -> {
                FileChooser exeChooser = new FileChooser();
                exeChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Mass Effect 3 Executable", "MassEffect3.exe"));
                String biogamedir = GetBioGameDir();
                if (biogamedir != null) {
                    File tryDir = new File(biogamedir);
                    if (tryDir.exists()) {
                        exeChooser.setInitialDirectory(new File(GetBioGameDir()));
                    } else {
                        ModManager.debugLogger.writeMessage("Directory " + GetBioGameDir() + " does not exist, defaulting to working directory.");
                    }
                } else {
                    exeChooser.setInitialDirectory(new java.io.File("."));
                }
                exeChooser.setTitle("Select Mass Effect 3 Executable");
                File chosenFile = exeChooser.showOpenDialog(null);
                if (chosenFile != null) {
                    checkForValidBioGame(chosenFile);
                } else {
                    ModManager.debugLogger.writeMessage("No executable selected...");
                }
            });
        } else if (e.getSource() == modManagementModMaker) {
            if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
                labelStatus.setText("ModMaker support on 32-bit windows ended May 15, 2018");
                return;
            }
            if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Opening ModMaker Entry Window");
                    updateApplyButton();
                    new ModMakerEntryWindow();
                } else {

                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("ModMaker: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to use ModMaker.");
                }
            } else {
                labelStatus.setText("ModMaker requires valid BIOGame directory to start");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);

            }
        } else if (e.getSource() == modManagementASI) {
            if (validateBIOGameDir()) {
                int exebuild = ModManager.checkforME3105(GetBioGameDir());
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
                if (ModManager.checkIfASIBinkBypassIsInstalled(GetBioGameDir()) == false) {
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
                            "ASI loader not installed.\nASI mods won't load without using the ASI version of binkw32.\nYou can install this from the tools menu or the ASI Mod Management window.",
                            "ASI loader not installed", JOptionPane.WARNING_MESSAGE);
                }

                new ASIModWindow(new File(GetBioGameDir()).getParent(), false);
            } else {
                updateApplyButton();
                labelStatus.setText("Can't manage ASI mods without valid BioGame");
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
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
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeError("Custom DLC Conflict Window: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to fully use the conflict detection tool.");
                }
            } else {
                labelStatus.setText("Conflict detector requires valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == modManagementFailedMods) {
            new FailedModsWindow();
        } else if (e.getSource() == backupBackupDLC) {
            if (validateBIOGameDir()) {
                backupDLC(GetBioGameDir());
            } else {
                labelStatus.setText("Backing up DLC requires valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == backupBasegameUnpacked) {
            if (validateBIOGameDir()) {
                String me3dir = (new File(GetBioGameDir())).getParent();
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
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == backupCreateVanillaCopy || e.getSource() == restoreVanillaCopy) {
            new VanillaBackupWindow(e.getSource() == backupCreateVanillaCopy);
            ModManager.debugLogger.writeMessage("Exit backup window - updating button status");
            backupCreateVanillaCopy.setSelected(VanillaBackupWindow.GetFullBackupPath(false) != null);
        } else if (e.getSource() == backupCreateGDB) {
            if (validateBIOGameDir()) {
                createBasegameDB(GetBioGameDir());
            } else {
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot update or create the game repair database.\nFix the BioGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertCoal) {
            if (ModManager.isMassEffect3Running()) {
                JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can restore game files.", "MassEffect3.exe is running",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (validateBIOGameDir()) {
                restoreCoalesced(GetBioGameDir());
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == modDevStarterKit) {
            new StarterKitWindow();
        } else if (e.getSource() == moddevOfficialDLCManager) {
            if (validateBIOGameDir()) {
                new OfficialDLCWindow(ModManagerWindow.GetBioGameDir());
            } else {
                labelStatus.setText("Official DLC Toggler requires valid BIOGame");
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BioGame directory to use the Official DLC toggler.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertAllDLC) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.ALLDLC);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoredeleteAllCustomDLC) {
            if (validateBIOGameDir()) {
                if (JOptionPane.showConfirmDialog(this, "This will delete all folders in the BIOGame/DLC folder that aren't known to be official.\nDelete all custom DLC?",
                        "Delete all Custom DLC", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

                    restoreDataFiles(GetBioGameDir(), RestoreMode.REMOVECUSTOMDLC);
                }
            } else {
                labelStatus.setText("Can't remove custom DLC with invalid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreCustomDLCManager) {
            if (validateBIOGameDir()) {
                new CustomDLCWindow(GetBioGameDir());
            } else {
                labelStatus.setText("Custom DLC Manager requires valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nCustom DLC Manager requires a valid directory.\nFix the BioGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreSelective) {
            if (validateBIOGameDir()) {
                new SelectiveRestoreWindow(GetBioGameDir());
            } else {
                labelStatus.setText("Custom Restore requires valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nCustom Restore requires a valid directory.\nFix the BioGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertBasegame) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.BASEGAME);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertUnpacked) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.UNPACKED);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertBasegameUnpacked) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.UNPACKEDBASEGAME);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreVanillifyDLC) {
            if (validateBIOGameDir()) {
                if (JOptionPane.showConfirmDialog(this,
                        "This will delete all unpacked DLC items, including backups of those files.\nThe backup files are deleted because you shouldn't restore unpacked files if your DLC isn't set up for unpacked files.\nMake sure you have your *original* SFARs backed up! Otherwise you will have to use Origin to download them again.\nAre you sure you want to continue?",
                        "Delete unpacked DLC files", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

                    restoreDataFiles(GetBioGameDir(), RestoreMode.VANILLIFYDLC);
                }
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertSPDLC) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.SP);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertMPDLC) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.MP);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertSPBaseDLC) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.SPBASE);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertMPBaseDLC) {
            if (validateBIOGameDir()) {
                restoreDataFiles(GetBioGameDir(), RestoreMode.MPBASE);
            } else {
                labelStatus.setText("Cannot restore files without valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == restoreRevertEverything) {
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

                        restoreCoalesced(GetBioGameDir());
                        restoreDataFiles(GetBioGameDir(), RestoreMode.ALL);
                    }
                } else {
                    labelStatus.setText("Cannot restore files without valid BIOGame directory");
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
                            "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
                            "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == restoreDeleteUnpacked) {
            if (validateBIOGameDir()) {
                if (JOptionPane.showConfirmDialog(this,
                        "This will delete all unpacked DLC items, including backups of those files.\nThe backup files are deleted because you shouldn't restore unpacked files if your DLC isn't set up for unpacked files.\nMake sure you have your *original* SFARs backed up! Otherwise you will have to use Origin to download them again.\nAre you sure you want to continue?",
                        "Delete unpacked DLC files", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    restoreDataFiles(GetBioGameDir(), RestoreMode.REMOVEUNPACKEDITEMS);
                }
            } else {
                labelStatus.setText("Cannot delete files with invalid BIOGame directory");

                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory",
                        JOptionPane.ERROR_MESSAGE);
            }
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
                ResourceUtils.openWebpage(new URI(mod.getModSite()));
            } catch (URISyntaxException e1) {
                // TODO Auto-generated catch block
                ModManager.debugLogger.writeErrorWithException("Unable to open this mod's web site:", e1);
            }
        } else if (e.getSource() == modlistFailedIndicatorLink) {
            new FailedModsWindow();
        } else if (e.getSource() == modManagementImportAlreadyInstalled) {
            if (validateBIOGameDir()) {
                new ModImportDLCWindow(this, GetBioGameDir());
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
        } else if (e.getSource() == modManagementCheckallmodsforupdate) {

            if (!validateBIOGameDir()) {
                JOptionPane.showMessageDialog(this, "Your BIOGame directory is not correctly set.\nOnly non-ModMaker mods will be checked for updates.",
                        "Invalid BIOGame Directory", JOptionPane.WARNING_MESSAGE);
            }
            checkAllModsForUpdates(true);
        } else if (e.getSource() == actionExit) {
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
                        labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                        ModManager.debugLogger.writeMessage("Applying selected mod: .NET is not installed");
                        new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to install mods.");
                    }
                } else {
                    labelStatus.setText("Installing a mod requires valid BIOGame path");
                    labelStatus.setVisible(true);
                    JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                            "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(ModManagerWindow.this, "Mass Effect 3 must be closed before you can install a mod.", "MassEffect3.exe is running",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == buttonStartGame) {
            if (validateBIOGameDir()) {
                ModManager.debugLogger.writeMessage("Starting game.");
                startGame(ModManager.appendSlash(GetBioGameDir()));
            } else {
                labelStatus.setText("Starting the game requires a valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BIOGame directory is not valid.\nMod Manager does not know where to launch the game executable.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == toolsPCCDataDumper) {
            if (ModManager.validateNETFrameworkIsInstalled()) {
                updateApplyButton();
                new PCCDataDumperWindow();
            } else {
                updateApplyButton();
                labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                ModManager.debugLogger.writeMessage("Run PCC Data Dumper: .NET is not installed");
                new NetFrameworkMissingWindow("The PCC Data Dumper tool requires .NET 4.7.1 or higher to be installed.");
            }

        } else if (e.getSource() == toolME3Explorer) {
            if (ModManager.validateNETFrameworkIsInstalled()) {
                updateApplyButton();
                boolean prompt = false;
                String extVersionStr = null;
                File me3exp = new File(ModManager.getME3ExplorerEXEDirectory() + "ME3Explorer.exe");
                String promptMessage = "Placeholder";
                String promptTitle = "Placeholder";
                int promptIcon = JOptionPane.ERROR_MESSAGE;

                if (!me3exp.exists()) {
                    if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
                        labelStatus.setText("Network support for 32-bit Mod Manager ended May 15, 2018");
                        return;
                    }

                    promptMessage = "ME3Explorer is not included in Mod Manager and must be downloaded.\nMod Manager can download version " + ModManager.LATEST_ME3EXPLORER_VERSION
                            + " for you.\nDownload?";
                    promptIcon = JOptionPane.WARNING_MESSAGE;
                    promptTitle = "Download required";
                    prompt = true;
                } else {
                    if (ModManager.LATEST_ME3EXPLORER_VERSION != null) {
                        int[] existingVersionInfo = EXEFileInfo.getVersionInfo(me3exp.getAbsolutePath());
                        extVersionStr = existingVersionInfo[0] + "." + existingVersionInfo[1] + "." + existingVersionInfo[2] + "." + existingVersionInfo[3];
                        if (EXEFileInfo.versionCompare(ModManager.LATEST_ME3EXPLORER_VERSION, extVersionStr) > 0) {
                            if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
                                labelStatus.setText("Network support for 32-bit Mod Manager ended May 15, 2018");
                                return;
                            }
                            promptMessage = "Your local copy of ME3Explorer is out of date.\nLocal version: " + extVersionStr + "\nLatest version: "
                                    + ModManager.LATEST_ME3EXPLORER_VERSION + "\nDownload latest version?";
                            promptIcon = JOptionPane.WARNING_MESSAGE;
                            promptTitle = "Update Available";
                            prompt = true;
                        }
                    }
                }
                if (prompt) {
                    // Show update
                    int update = JOptionPane.showConfirmDialog(ModManagerWindow.ACTIVE_WINDOW, promptMessage, promptTitle, JOptionPane.YES_NO_OPTION, promptIcon);
                    if (update == JOptionPane.YES_OPTION) {
                        new ME3ExplorerUpdaterWindow(ModManager.LATEST_ME3EXPLORER_VERSION, true);
                    } else {
                        if (me3exp.exists()) {
                            ProcessBuilder pb = new ProcessBuilder(me3exp.getAbsolutePath());
                            File workingdir = new File(me3exp.getAbsolutePath()).getParentFile();
                            pb.directory(workingdir);
                            ModManager.debugLogger.writeMessage("Launching ME3Explorer. Working directory for process: " + workingdir);
                            ModManager.runProcessDetached(pb);
                            labelStatus.setText("Launched ME3Explorer");
                        } else {
                            ModManager.debugLogger.writeMessage("Aborting ME3Explorer launch - does not exist locally");
                            labelStatus.setText("ME3Explorer not available");
                        }
                    }
                } else {
                    if (me3exp.exists()) {
                        // run it
                        ProcessBuilder pb = new ProcessBuilder(me3exp.getAbsolutePath());
                        File workingdir = new File(me3exp.getAbsolutePath()).getParentFile();
                        pb.directory(workingdir);
                        ModManager.debugLogger.writeMessage("Launching ME3Explorer. Working directory for process: " + workingdir);
                        ModManager.runProcessDetached(pb);
                        labelStatus.setText("Launched ME3Explorer");
                    }
                }
            } else {
                updateApplyButton();
                labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                ModManager.debugLogger.writeMessage("Run ME3Explorer: .NET is not installed");
                new NetFrameworkMissingWindow("ME3Explorer requires .NET 4.7.1 or higher in order to run.");
            }
        } else if (e.getSource() == toolAlotInstaller) {
            if (ModManager.validateNETFrameworkIsInstalled()) {
                updateApplyButton();
                boolean prompt = false;
                File alotInstallerEXE = new File(ModManager.getALOTInstallerDirectory() + "ALOTInstaller.exe");
                String promptMessage = "Placeholder";
                String promptTitle = "Placeholder";
                int promptIcon = JOptionPane.ERROR_MESSAGE;

                if (!alotInstallerEXE.exists() && ModManager.ALOTINSTALLER_LATESTVERSION != null && ModManager.ALOTINSTALLER_DOWNLOADLINK != null) {
                    String latestVersion = ModManager.ALOTINSTALLER_LATESTVERSION.toString();
                    promptMessage = "ALOT Installer is not included in Mod Manager and must be downloaded.\nMod Manager can download version " + latestVersion
                            + " for you.\nDownload?";
                    promptIcon = JOptionPane.WARNING_MESSAGE;
                    promptTitle = "Download required";
                    prompt = true;
                } else if (ModManager.ALOTINSTALLER_LATESTVERSION != null && ModManager.ALOTINSTALLER_DOWNLOADLINK != null) {
                    Version existingVersion = EXEFileInfo.getVersion(alotInstallerEXE.getAbsolutePath());
                    if (existingVersion.compareTo(ModManager.ALOTINSTALLER_LATESTVERSION) < 0) {
                        promptMessage = "Your local copy of ALOT Installer is out of date.\nLocal version: " + existingVersion + "\nLatest version: "
                                + ModManager.ALOTINSTALLER_LATESTVERSION + "\nDownload latest version?";
                        promptIcon = JOptionPane.WARNING_MESSAGE;
                        promptTitle = "Update available";
                        prompt = true;
                    }
                } else {
                    ModManager.debugLogger.writeMessage("No network connection at startup - no known way to download ALOT Installer.");
                    labelStatus.setText("Mod Manager needs network connection at startup");
                }
                if (prompt) {
                    // Show update
                    int update = JOptionPane.showConfirmDialog(ModManagerWindow.ACTIVE_WINDOW, promptMessage, promptTitle, JOptionPane.YES_NO_OPTION, promptIcon);
                    if (update == JOptionPane.YES_OPTION) {
                        new ALOTInstallerUpdaterWindow(ModManager.ALOTINSTALLER_LATESTVERSION, true);
                    } else {
                        if (alotInstallerEXE.exists()) {
                            ModManager.debugLogger.writeMessage("Launching ALOT Installer");
                            ProcessBuilder pb = new ProcessBuilder(alotInstallerEXE.getAbsolutePath());
                            pb.directory(new File(ModManager.getALOTInstallerDirectory()));
                            ModManager.runProcessDetached(pb);
                            labelStatus.setText("Launched ALOT Installer");

                        } else {
                            ModManager.debugLogger.writeMessage("Aboring ALOT Installer launch - does not exist locally");
                            labelStatus.setText("ALOT Installer not available");
                        }
                    }
                } else {
                    if (alotInstallerEXE.exists()) {
                        // run it
                        ModManager.debugLogger.writeMessage("Launching ALOT Installer");
                        ProcessBuilder pb = new ProcessBuilder(alotInstallerEXE.getAbsolutePath());
                        ModManager.runProcessDetached(pb);
                        labelStatus.setText("Launched ALOT Installer");
                    }
                }

            } else {
                updateApplyButton();
                labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                ModManager.debugLogger.writeMessage("Run ALOT Installer: .NET is not installed");
                new NetFrameworkMissingWindow("ALOT Installer requires .NET 4.7.1 or higher in order to run.");
            }

        } else if (e.getSource() == modManagementOpenModsFolder)

        {
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
                labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                ModManager.debugLogger.writeMessage("Run TLK: .NET is not installed");
                new NetFrameworkMissingWindow("Tankmaster's TLK Tool requires .NET 4.7.1 or higher in order to run.");
            }
        } else if (e.getSource() == toolTankmasterCoalUI) {
            if (ModManager.validateNETFrameworkIsInstalled()) {
                new CoalescedWindow();
            } else {
                updateApplyButton();
                labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                ModManager.debugLogger.writeMessage("Run ME3Explorer: .NET is not installed");
                new NetFrameworkMissingWindow("ME3Explorer requires .NET 4.7.1 or higher in order to run.");
            }
        } else if (e.getSource() == actionOptions) {
            new OptionsWindow(this);
            /*
             * } else if (e.getSource() == toolsMergeMod) {
             * JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
             * "The mod merging tool has been deprecated.\nIt is no longer tested, and may break. To merge mods, install both and then run AutoTOC on the game.\nThe final applied mod will take precedence if there are conflicts."
             * , "Deprecated Tool", JOptionPane.WARNING_MESSAGE); if
             * (ModManager.validateNETFrameworkIsInstalled()) {
             * ModManager.debugLogger.writeMessage("Opening Mod Merging utility"
             * ); updateApplyButton(); new MergeModWindow(this); } else {
             * updateApplyButton();
             * labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
             * ModManager.debugLogger.
             * writeMessage("Merge Tool: Missing .NET Framework"); new
             * NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to merge mods."
             * ); }
             */
        } else if (e.getSource() == toolME3Config) {
            if (validateBIOGameDir()) {
                ModManager.debugLogger.writeMessage("Opening ME3 Config tool");
                File path = new File(GetBioGameDir());
                path = path.getParentFile();
                String command = path.getAbsolutePath() + "\\Binaries\\MassEffect3Config.exe";
                if (new File(command).exists()) {
                    ProcessBuilder p = new ProcessBuilder(command);
                    ModManager.debugLogger.writeMessage("Launching ME3 Config tool: " + command);
                    ModManager.runProcessDetached(p);
                } else {
                    ModManager.debugLogger.writeError("Config tool is missing! Not found at " + path);
                    JOptionPane.showMessageDialog(ModManagerWindow.this, "The config tool executable doesn't exist where it should:\n" + path, "Missing Config Tool",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                labelStatus.setText("ME3 Config tool requires a valid BIOGame directory");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BIOGame directory is not valid.\nCannot open the ME3 Config tool if Mod Manager doesn't know where Mass Effect 3 is.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == toolsUnpackDLC) {
            if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Opening Unpack DLC window");
                    updateApplyButton();
                    new UnpackWindow(this);
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Unpack DLC Tool: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to unpack DLC.");
                }
            } else {
                labelStatus.setText("Unpacking DLC requires a valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BIOGame directory is not valid.\nCannot unpack DLC without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == toolMountdlcEditor) {
            new MountFileEditorWindow();
        } else if (e.getSource() == toolsOpenME3Dir) {
            openME3Dir();
        } else if (e.getSource() == toolsGrantWriteAccess) {
            File f = new File((String) comboboxBiogameDir.getSelectedItem());
            f = f.getParentFile();
            String username = Advapi32Util.getUserName();
            ModManager.GrantPermissionsToDirectory(f.getAbsolutePath() + "\\", username);
        } else if (e.getSource() == toolsAutoTOCGame) {
            if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Running Game-wide AutoTOC.");
                    updateApplyButton();
                    new AutoTocWindow(ModManagerWindow.GetBioGameDir());
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("AutoTOC: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to use the AutoTOC feature.");
                }
            } else {
                labelStatus.setText("Game AutoTOC requires a valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The BIOGame directory is not valid.\nCannot update TOC files without a valid directory.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == modManagementPatchLibary) {
            if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Opening patch library window.");
                    updateApplyButton();
                    new PatchLibraryWindow(PatchLibraryWindow.MANUAL_MODE);
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Patch Library: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to use MixIns.");
                }
            } else {
                labelStatus.setText("Use of the patch library requires a valid BIOGame folder");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == modManagementModGroupsManager) {
            if (validateBIOGameDir()) {
                if (ModManager.validateNETFrameworkIsInstalled()) {
                    ModManager.debugLogger.writeMessage("Opening Mod Groups window.");
                    updateApplyButton();
                    new ModGroupWindow();
                } else {
                    updateApplyButton();
                    labelStatus.setText(".NET Framework 4.7.1 or higher is missing");
                    ModManager.debugLogger.writeMessage("Batch Mod Installer: Missing .NET Framework");
                    new NetFrameworkMissingWindow("You must install .NET Framework 4.7.1 or higher in order to batch install mods.");
                }
            } else {
                labelStatus.setText("Use of the patch library requires a valid BIOGame folder");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == moddevUpdateXMLGenerator) {
            ModManager.debugLogger.writeMessage("Generating Manifest...");
            ModXMLTools.generateXMLFileList(modModel.getElementAt(modList.getSelectedIndex()));
        } else if (e.getSource() == sqlDifficultyParser) {
            new DifficultyGUI();
        } else if (e.getSource() == sqlWavelistParser) {
            new WavelistGUI();
        } else if (e.getSource() == sqlAIWeaponParser) {
            new BioAIGUI();
        } else if (e.getSource() == sqlPowerCustomActionParser) {
            new PowerCustomActionGUI();
        } else if (e.getSource() == sqlPowerCustomActionParser2) {
            new PowerCustomActionGUI2();
        } else if (e.getSource() == toolsInstallBinkw32asi) {
            if (ModManager.isMassEffect3Running()) {
                JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can install binkw32 ASI DLC bypass.",
                        "MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (validateBIOGameDir()) {
                ModManager.debugLogger.writeMessage("Installing manual Binkw32 (ASI) bypass.");
                installBinkw32Bypass();
                updateBinkBypassStatus();
            } else {
                labelStatus.setText("Installing DLC bypass requires valid BIOGame directory");
                labelStatus.setVisible(true);
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
                        "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void checkAllModsForUpdates(boolean isManualCheck) {
        if (!ResourceUtils.is64BitWindows() && LocalDateTime.now().toLocalDate().isAfter(LocalDate.parse("2018-05-15"))) {
            ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Mod updating support on 32-bit windows ended May 15, 2018");
            return;
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
            labelStatus.setText("Checking mods for updates");
            new AllModsUpdateWindow(this, isManualCheck, updatableMods);
        } else {
            if (isManualCheck) {
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "No mods are eligible for the Mod Manager update service.\nEligible mods include ModMaker mods and ones hosted on ME3Tweaks.com.", "No updatable mods",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Opens a explorer.exe window with ME3 directory
     */
    private void openME3Dir() {
        File file = new File(GetBioGameDir());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(ModManagerWindow.this, "The BioGame directory does not exist.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(new File(GetBioGameDir()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            ModManager.debugLogger.writeErrorWithException("I/O Exception while opening ME3Dir.", e);
        }
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
            new BackupWindow(this);
        } else {
            // Biogame is invalid
            JOptionPane.showMessageDialog(ModManagerWindow.this, "The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
                    "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            labelStatus.setText("DLC backup failed");
            labelStatus.setVisible(true);
        }
        return;
    }

    private void autoBackupDLC(String bioGameDir, String dlcName) {
        // Check that biogame is valid
        if (validateBIOGameDir()) {
            new BackupWindow(this, dlcName);
        } else {
            // Biogame is invalid
            JOptionPane.showMessageDialog(ModManagerWindow.this, "The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
                    "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            labelStatus.setText("DLC backup failed");
            labelStatus.setVisible(true);
        }
        return;
    }

    /**
     * Checks that the user has chosen a correct biogame directory. If the
     * selection is valid it is added to the list of BIOGAME_DIRECTORIES file.
     */
    private void checkForValidBioGame(File chosenFile) {
        String chosenPath = chosenFile.getAbsolutePath();

        if (FilenameUtils.getName(chosenPath).equals("MassEffect3.exe")) {
            //adjust to biogame
            File f = new File(chosenPath);
            boolean showBadMessage = false;
            if (f != null) {
                f = f.getParentFile(); //Win32
            }
            if (f != null) {
                f = f.getParentFile(); //Binaries
            } else {
                showBadMessage = true;
            }

            System.out.println(f);

            if (f != null) {
                f = f.getParentFile(); // Mass Effect 3
            } else {
                showBadMessage = true;
            }

            System.out.println(f);

            if (f == null) {
                showBadMessage = true;
            }

            if (showBadMessage) {
                ModManager.debugLogger.writeMessage("Selected EXE is not part of a game installation because not enough parent directories exist.");
                JOptionPane.showMessageDialog(ModManagerWindow.this,
                        "The selected EXE is not in a valid game installation,\nnot enough parent folders exist.\nMod Manager cannot use this as a installation target.",
                        "Protected Directory Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String gameDir = f.toString() + "\\BIOGame";
            chosenPath = gameDir;
        }

        ModManager.debugLogger.writeMessage("User chose EXE - Game directory being checked is " + chosenPath);

        if (internalValidateBIOGameDir(chosenPath)) {
            String biogamePath = chosenPath;
            chosenPath = new File(chosenPath).getParent(); // Game Directory
            ModManager.debugLogger.writeMessage("Parent (after internal validation passed):" + chosenPath);

            // Check to make sure path is not a backup path
            File cmm_vanilla = new File(chosenPath + File.separator + "cmm_vanilla");
            ModManager.debugLogger.writeMessage("Checking if biogame directory is a vanilla backup: " + cmm_vanilla);
            if (cmm_vanilla.exists()) {
                ModManager.debugLogger.writeMessage("Selected directory is a vanilla backup, rejecting biogame choice.");
                JOptionPane.showMessageDialog(ModManagerWindow.this, "The selected directory is marked as a vanilla backup.\nMod Manager cannot use this as a installation target.",
                        "Protected Directory Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check to make sure mod manager folder is not a subset
            String localpath = System.getProperty("user.dir");
            ModManager.debugLogger.writeMessage("Checking if mod manager directory is a subdirectory of the game: " + chosenPath);
            ModManager.debugLogger.writeMessage("CMM Directory: " + localpath);
            try {
                String relativePath = ResourceUtils.getRelativePath(localpath, chosenPath, File.separator);
                if (!relativePath.contains("..")) {
                    ModManager.debugLogger.writeMessage("Relative path detected: " + relativePath);
                    // common path
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
                            "Mod Manager will not work if it is run from the Mass Effect 3 game directory, or any of its subdirectories.\nMove the Mod Manager folder out of the game directory, as Mod Manager will not work until you do this.",
                            "Invalid Mod Manager Location", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (ResourceUtils.PathResolutionException e) {
                // we're OK
            }

            ComboBoxModel<String> model = comboboxBiogameDir.getModel();
            ArrayList<String> biogameDirectories = new ArrayList<String>();
            int size = model.getSize();
            for (int i = 0; i < size; i++) {
                String element = model.getElementAt(i);
                if (element.equalsIgnoreCase(biogamePath)) {
                    continue; // we'll put ours at the top of the list
                }
                biogameDirectories.add(element);
                // System.out.println("Element at " + i + " = " + element);
            }
            comboboxBiogameDir.removeItemListener(BIOGAME_ITEM_LISTENER);
            comboboxBiogameDir.removeAllItems();
            comboboxBiogameDir.addItem(biogamePath); // our selected item

            for (String biodir : biogameDirectories) {
                comboboxBiogameDir.addItem(biodir); // all the others
            }

            biogameDirectories.add(0, biogamePath);

            saveBiogamePath(biogameDirectories);
            labelStatus.setText("Set game target directory");
            labelStatus.setVisible(true);
            comboboxBiogameDir.setSelectedItem(chosenPath);
            comboboxBiogameDir.addItemListener(BIOGAME_ITEM_LISTENER);
            validateBIOGameDir();
        } else {
            JOptionPane.showMessageDialog(ModManagerWindow.this, "Invalid Mass Effect 3 BIOGame folder selected:\n" + chosenPath, "Invalid ME3 BIOGame Directory",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks if the string in the biogamedir is a valid biogame directory.
     * Checks for Coalesced.bin and DLC folder existence.
     *
     * @return True if valid, false otherwise
     */
    public static boolean validateBIOGameDir() {
        if (ModManagerWindow.ACTIVE_WINDOW != null && ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir != null) {
            ModManagerWindow.PRELOADED_BIOGAME_DIR = ModManagerWindow.GetBioGameDir();
        }

        if (internalValidateBIOGameDir(PRELOADED_BIOGAME_DIR)) {
            setBioDirHighlight(false);
            setBottomButtonState(true);
            return true;
        } else {
            setBioDirHighlight(true);
            setBottomButtonState(false);
            return false;
        }
    }

    /**
     * Internal validation rule for validating biogame dir.
     *
     * @param path
     * @return
     */
    private static boolean internalValidateBIOGameDir(String path) {
        if (path == null) {
            ModManager.debugLogger.writeError("BIOGame Directory is invalid (null path)");
            return false;
        }
        File coalesced = new File(ModManager.appendSlash(path) + "CookedPCConsole\\Coalesced.bin");
        File dlcFolder = new File(ModManager.appendSlash(path) + "DLC\\");
        File parentPath = new File(path).getParentFile();
        if (!coalesced.exists()) {
            ModManager.debugLogger.writeError("Validating BIOGame directory failed: Coalesced.bin doesn't exist at " + coalesced.getAbsolutePath());
        }

        if (!dlcFolder.exists()) {
            ModManager.debugLogger.writeError("Validating BIOGame directory failed: DLC Folder doesn't exist at " + dlcFolder.getAbsolutePath());
        }

        if (coalesced.exists() && dlcFolder.exists() && parentPath != null) {
            // It exists - testing for subdirectory.
            String localpath = ModManager.appendSlash(System.getProperty("user.dir"));
            try {
                if (!localpath.equalsIgnoreCase(ModManager.appendSlash(parentPath.getAbsolutePath()))) {
                    String relative = ResourceUtils.getRelativePath(localpath, ModManager.appendSlash(parentPath.getAbsolutePath()), File.separator);
                    if (relative.startsWith("..")) {
                        return true;
                    }
                }
                // common path
                ModManager.debugLogger.writeError("Mod Manager is located in the game directory! Shutting down to avoid issues.");
                JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
                        "Mod Manager will not work when running from inside a Mass Effect 3 game directory.\nMove Mod Manager out of the game directory and restart Mod Manager.",
                        "Invalid Mod Manager Location", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } catch (ResourceUtils.PathResolutionException e) {
                // we're OK
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the state of the bottom two buttons in Mod Manager main UI
     *
     * @param b true to on, false to off
     */
    private static void setBottomButtonState(boolean b) {
        if (ModManagerWindow.ACTIVE_WINDOW != null && ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir != null) {
            ModManagerWindow.ACTIVE_WINDOW.buttonApplyMod.setEnabled(ACTIVE_WINDOW.modList.getSelectedIndex() >= 0 ? b : false);
            ModManagerWindow.ACTIVE_WINDOW.buttonStartGame.setEnabled(b);
        }
    }

    /**
     * Changes the border around the Biogame Directory panel to indicate
     * something is wrong.
     *
     * @param highlight
     */
    private static void setBioDirHighlight(boolean highlight) {
        if (ModManagerWindow.ACTIVE_WINDOW != null && ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir != null) {
            if (highlight) {
                TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mass Effect 3 BIOGame Directory (INVALID)",
                        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, UIManager.getDefaults().getFont("titledBorder.font"), Color.RED);
                ModManagerWindow.ACTIVE_WINDOW.cookedDirPanel.setBorder(cookedDirTitle);
            } else {
                TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                        "Mass Effect 3 BIOGame Directory (Installation Target)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        UIManager.getDefaults().getFont("titledBorder.font"), new Color(0, 150, 0));
                ModManagerWindow.ACTIVE_WINDOW.cookedDirPanel.setBorder(cookedDirTitle);
            }
        }
    }

    /**
     * Saves the biogame directory path to me3cmm.ini
     *
     * @param installDir Directory of ME3 BIOGame folder
     */
    private void saveBiogamePath(ArrayList<String> installDir) {
        ModManager.debugLogger.writeMessage("Saving list of BIOGame paths.");
        try {
            FileWriter writer = new FileWriter(ModManager.getSavedBIOGameDirectoriesFile());

            for (String str : installDir) {
                ModManager.debugLogger.writeMessage(" - " + str);
                writer.write(ResourceUtils.removeTrailingSlashes(str));
                writer.write("\n");
            }
            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            ModManager.debugLogger.writeErrorWithException("Failed to save biogame directories!", e);
        }
    }

    /**
     * Installs the mod after doing some prechecks
     *
     * @return True if the mod installed, false otherwise
     */
    private boolean applyMod() {
        ModManager.debugLogger.writeMessage("applyMod() method is executing.");
        // Precheck for ALOT
        int index = modList.getSelectedIndex();
        if (index >= 0) {
            Mod mod = modModel.get(index);
            if (ModManager.isALOTInstalled(GetBioGameDir())) {
                boolean hasPCCInstall = false;
                ModManager.debugLogger.writeMessage("ALOT is installed, checking for installation of non-testpatch PCC files...");

                for (ModJob job : mod.getJobs()) {
                    if (job.getJobName().equals(ModTypeConstants.TESTPATCH)) {
                        continue; // we don't are about this
                    }
                    for (String destFile : job.getFilesToReplaceTargets()) {
                        String extension = FilenameUtils.getExtension(destFile);
                        if (FilenameUtils.getExtension(destFile).toLowerCase().equals("pcc")) {
                            hasPCCInstall = true;
                            ModManager.debugLogger.writeMessage("Detected PCC file attempting to install over ALOT installation: " + destFile);
                            break;
                        }
                    }
                    if (hasPCCInstall) {
                        break;
                    }
                }

                if (hasPCCInstall) {
                    if (ModManager.CHECK_FOR_ALOT_INSTALL) {
                        installBlockedByALOT(false);
                        return false;
                    } else {
                        ModManager.debugLogger.writeMessage("ALOT is installed, found conflicts. User has allow install check off, but we are going to warn anyways.");
                        int result = JOptionPane.showOptionDialog(this,
                                "ALOT is installed and this mod installs PCC files.\nYou should only install this if you really know what you are doing as you WILL break the game.\nSeriously - if you don't know what you are actually doing, do not continue.\n\nInstall anyways?",
                                "Warning: ALOT is installed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Yes", "No"}, "No");
                        if (result == JOptionPane.NO_OPTION) {
                            return false;
                        }
                        ModManager.debugLogger.writeMessage("User says they know what they are doing. Hope you don't regret this!");
                    }

                } else {
                    ModManager.debugLogger.writeMessage("ALOT is installed, did not detect any potential issues for this mod install.");
                }
            }

            int jobCode = submitBackgroundJob("ModInstall");
            labelStatus.setText("Installing " + mod.getModName() + "...");
            if (mod.getJobs().length > 0) {
                checkDLCIsBackedUp(mod);
                new ModInstallWindow(this, mod, null);
            }
            submitJobCompletion(jobCode);
            return true;
        }
        return false;
    }

    /**
     * Shows a message to the user that installation of mods is blocked due to
     * ALOT.
     *
     * @param multi Indicates if this is due to single mod installer or multimod
     *              installer (batch mode)
     */
    public void installBlockedByALOT(boolean multi) {
        ModManager.debugLogger.writeMessage("Installation of mod has been blocked due to detection of ALOT.");
        ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Installation blocked due to detection of ALOT");
        if (multi) {
            JOptionPane.showMessageDialog(this,
                    "Batch Mod Installer is disabled while ALOT is installed.\nInstallation of mods is blocked while ALOT is installed, as this will almost always cause problems.\nIf you know what you are doing you can turn this check off in the options menu.",
                    "Installation Blocked", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "ALOT is installed and this mod installs PCC files.\nInstallation of mods is blocked while ALOT is installed, as this will almost always cause problems.\nIf you know what you are doing you can turn this check off in the options menu.",
                    "Installation Blocked", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks to make sure DLC has been backed up before mod installation
     *
     * @param mod Mod to check backups for
     */
    private void checkDLCIsBackedUp(Mod mod) {
        ModJob[] jobs = mod.getJobs();
        for (ModJob job : jobs) {
            if (job.getJobType() == ModJob.BASEGAME || job.getJobType() == ModJob.CUSTOMDLC) {
                continue; // we can't really check for a .bak of Coalesced.
            }
            // Default.sfar
            File mainFile = new File(ModManager.appendSlash(GetBioGameDir()) + job.getDLCFilePath() + "\\Default.sfar");
            boolean defaultsfarMainFileExists = mainFile.exists();
            File backFile = new File(ModManager.appendSlash(GetBioGameDir()) + job.getDLCFilePath() + "\\Default.sfar.bak");
            ModManager.debugLogger.writeMessage("Checking for backup file: " + backFile.getAbsolutePath());
            if (!backFile.exists()) {
                // Patch_001.sfar
                mainFile = new File(ModManager.appendSlash(GetBioGameDir()) + job.getDLCFilePath() + "\\Patch_001.sfar");
                boolean patch001farMainFileExists = mainFile.exists();
                backFile = new File(ModManager.appendSlash(GetBioGameDir()) + job.getDLCFilePath() + "\\Patch_001.sfar.bak");
                ModManager.debugLogger.writeMessage("Checking for TESTPATCH file: " + backFile.getAbsolutePath());

                if ((defaultsfarMainFileExists || patch001farMainFileExists) && !backFile.exists()) {
                    String YesNo[] = {"Yes", "No"}; // Yes/no buttons
                    int showDLCBackup = JOptionPane.showOptionDialog(ModManagerWindow.ACTIVE_WINDOW,
                            "<html>" + job.getJobName() + " DLC has not been backed up.<br>Back it up now?</hmtl>", "Backup DLC", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
                    if (showDLCBackup == 0) {
                        autoBackupDLC(GetBioGameDir(), job.getJobName());
                    }
                }
            }
        }
    }


    /**
     * Handles changes in the modlist selction.
     *
     * @param listChange List change event information
     */
    @Override
    public void valueChanged(ListSelectionEvent listChange) {
        if (listChange.getValueIsAdjusting() == false) {
            if (modList.getSelectedIndex() < 0) {
                buttonApplyMod.setEnabled(false);
                modUtilsMenu.setEnabled(false);

                if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
                    buttonApplyMod.setToolTipText("Select a mod on the left");
                } else {
                    buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.7.1 or higher in order to install mods");
                }
                fieldDescription.setText(getNoSelectedModDescription());
                modWebsiteLink.setVisible(false);
                modUtilsMenu.setEnabled(false);
                modUtilsMenu.setToolTipText("Select a mod to enable this menu");
                moddevUpdateXMLGenerator.setText("Prepare mod for ME3Tweaks Updater Service");
                moddevUpdateXMLGenerator.setToolTipText("No mod is currently selected");
                moddevUpdateXMLGenerator.setEnabled(false);
            } else {
                Mod selectedMod = modModel.get(modList.getSelectedIndex());
                modUtilsMenu.setToolTipText(null);
                modUtilsMenu.setEnabled(true);
                modUtilsMenu.removeAll();
                ArrayList<Component> menuItems = buildModUtilsMenu(selectedMod);
                for (Component item : menuItems) {
                    modUtilsMenu.add(item);
                }

                // Update mod description
                fieldDescription.setText(selectedMod.getModDisplayDescription());
                fieldDescription.setCaretPosition(0);

                if (ModManagerWindow.validateBIOGameDir()) {
                    if (ModManager.NET_FRAMEWORK_IS_INSTALLED) {
                        buttonApplyMod.setToolTipText(
                                "<html>Apply this mod to the game.<br>If other mods are installed, you should consider uninstalling them by<br>using the Restore Menu if they are known to not work together.</html>");
                    } else {
                        buttonApplyMod.setToolTipText("Mod Manager requires .NET Framework 4.7.1 or higher in order to install mods");
                        buttonApplyMod.setText("Missing .NET");
                        buttonApplyMod.setEnabled(false);
                    }

                    ArrayList<String> requiredHeaders = selectedMod.getRequiredDLCHeaders();
                    if (requiredHeaders.size() > 0) {
                        ArrayList<String> installedDLC = ModManager.getInstalledDLC(GetBioGameDir());
                        if (installedDLC.containsAll(requiredHeaders)) {
                            buttonApplyMod.setEnabled(true);
                            buttonApplyMod.setText("Apply Mod");
                        } else {
                            buttonApplyMod.setEnabled(false);
                            String toolTip = "<html>This mod is missing required DLC.<br>Missing required DLC:";
                            for (String str : requiredHeaders) {
                                if (!installedDLC.contains(str)) {
                                    toolTip += "<br> - " + ME3TweaksUtils.getThirdPartyModName(str, true);
                                }
                            }
                            toolTip += "</html>";
                            buttonApplyMod.setToolTipText(toolTip);
                            buttonApplyMod.setText("Missing Required DLC");
                        }
                    } else {
                        buttonApplyMod.setEnabled(true);
                        buttonApplyMod.setText("Apply Mod");
                    }
                } else {
                    buttonApplyMod.setEnabled(false);
                    buttonApplyMod.setToolTipText("Invalid BIOGame Directory.");
                }

                UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
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
                    mountMenu.setText(selectedMod.getModName() + " Mount.dlc files");
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

            }
        }
    }

    /**
     * Gets selected mod description, with a tip.
     *
     * @return description with tip if tips service is loaded
     */
    private String getNoSelectedModDescription() {
        String description = selectAModDescription;
        String tip = ME3TweaksUtils.getME3TweaksTip();
        if (tip.equals("")) {
            return description;
        } else {
            return description + "\n\n-------------------------\n" + tip;
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
                        JOptionPane.showMessageDialog(ModManagerWindow.this,
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
                    JOptionPane.showMessageDialog(ModManagerWindow.this,
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
            JOptionPane.showMessageDialog(ModManagerWindow.this,
                    "The backed up Coalesced.bin file (data/Coalesced.original) does not exist.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.\n\nThis error should have been caught but can be thrown due to file system changes \nwhile the program is open.",
                    "Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);

        }
        return false;
    }

    /**
     * Initiates a restore procedure using the specified directory and restore
     * mode
     *
     * @param bioGameDir  Directory to biogame folder
     * @param restoreMode constant defining the restore procedure
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
            JOptionPane.showMessageDialog(ModManagerWindow.this, "The BioGame directory is not valid. Files cannot be restored.\nFix the directory and try again.",
                    "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Starts the MassEffect3.exe executable.
     *
     * @param CookedDir biogamedir
     */
    private void startGame(String CookedDir) {
        File startingDir = new File(CookedDir);
        ModManager.debugLogger.writeMessage("Starting game.");
        startingDir = new File(startingDir.getParent());
        File executable = new File(startingDir.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
        // check ME3 version for 1.6
        try {
            int minorBuildNum = EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());
            if (minorBuildNum > 5) {
                ModManager.debugLogger.writeMessage("!!!!This user has >1.5 version of Mass Effect 3!!!!");
                JOptionPane.showMessageDialog(this, "<html><div style='width: 300px'>You have a version of Mass Effect 3 higher than 1.5 (1." + minorBuildNum
                                + "), which is the main version most of the world uses.<br>"
                                + "It seems BioWare has been slowly pushing this version out to some users starting in September 2013. Not all users will get this version.<br><br>If you encounter issues you may consider downgrading to the 1.5 EXE. If you play Multiplayer, you will only be able to connect to other 1.6 users, which are very few.<br>Check the ME3Tweaks forums for info on how to downgrade.</div></html>",
                        "Mass Effect 3 Rare Version Detected", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            // do nothing. Continue like the old mod manager did.
            ModManager.debugLogger.writeErrorWithException("Error getting Mass Effect 3 EXE version. This error will be ignored.", e);
        }

        ModManager.debugLogger.writeMessage("Launching: " + executable.getAbsolutePath());

        // check if the new one exists
        if (!executable.exists()) {
            JOptionPane.showMessageDialog(ModManagerWindow.this,
                    "Unable to find game executable in the specified directory:\n" + executable.getAbsolutePath() + "\nMake sure your BIOGame directory is correct.",
                    "Unable to Launch Game", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Executable exists.
        String[] command = {"cmd.exe", "/c", "start", "cmd.exe", "/c", executable.getAbsolutePath()};
        try {
            labelStatus.setText("Launched Mass Effect 3");
            this.setExtendedState(JFrame.ICONIFIED);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            ModManager.debugLogger.writeErrorWithException("I/O Exception while launching ME3.", e);

        }
        ModManager.debugLogger.writeMessage("Path: " + executable.getAbsolutePath() + " - Exists? " + executable.exists());
    }

    /**
     * Creates a new Mod Maker Compiler dialog with the specified code. Called
     * from the code entry dialog.
     *
     * @param code Code to use for downloading the mod.
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
        new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.GetBioGameDir());
    }

    private boolean installBinkw32Bypass() {
        if (validateBIOGameDir()) {
            boolean result = ModManager.installBinkw32Bypass(GetBioGameDir());
            if (result) {
                // ok
                labelStatus.setText("Binkw32 ASI installed. DLC will always authorize.");
            } else {
                labelStatus.setText("FAILURE: Binkw32 ASI not installed!");
            }
            return result;
        }
        JOptionPane.showMessageDialog(ModManagerWindow.this, "The BioGame directory is not valid.\nMod Manager cannot install Binkw32 ASI DLC bypass.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private boolean uninstallBinkw32Bypass() {
        if (validateBIOGameDir()) {
            boolean result = ModManager.uninstallBinkw32Bypass(GetBioGameDir());
            if (result) {
                // ok
                labelStatus.setText("Binkw32 bypass uninstalled");
            } else {
                labelStatus.setText("FAILURE: Binkw32 bypass not uninstalled");
            }
            updateBinkBypassStatus();
            return result;
        }
        JOptionPane.showMessageDialog(ModManagerWindow.this,
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
        if (invalidMods == null) {
            return new ArrayList<Mod>();
        }
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

    /**
     * Fetches the current active BioGame target. Does not guarantee the end of
     * the string has a /.
     *
     * @return Current target
     */
    public static String GetBioGameDir() {
        if (ModManagerWindow.ACTIVE_WINDOW == null || ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir == null
                || ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir.getSelectedItem() == null) {
            return ModManagerWindow.PRELOADED_BIOGAME_DIR;
        } else {
            return ModManagerWindow.ACTIVE_WINDOW.comboboxBiogameDir.getSelectedItem().toString();
        }
    }

    class BiogameDirChangeListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {

                String selectedPath = (String) event.getItem();
                ModManager.debugLogger.writeMessage("Switching game targets to " + selectedPath);
                String selectedGamePath = new File(selectedPath).getParent(); //Game directory
                //UPDATE REGISTRY KEY
                String currentpath = ModManager.LookupGamePathViaRegistryKey(false);
                if (currentpath != null) {
                    if (currentpath.endsWith("\\")) {
                        currentpath = currentpath.substring(0, currentpath.length() - 1);
                    }

                    ModManager.debugLogger.writeMessage("Current registry path: " + currentpath);
                    ModManager.debugLogger.writeMessage("Selected path: " + selectedGamePath);
                    if (currentpath != null && !currentpath.equalsIgnoreCase(selectedGamePath)) {
                        // Update registry key.
                        boolean is64bit = ResourceUtils.is64BitWindows();
                        String keypath = is64bit ? "HKLM\\SOFTWARE\\Wow6432Node\\BioWare\\Mass Effect 3" : "HKLM\\SOFTWARE\\BioWare\\Mass Effect 3";
                        ArrayList<String> command = new ArrayList<String>();
                        command.add(ModManager.getCommandLineToolsDir() + "elevate.exe");
                        command.add("-c");
                        command.add("reg");
                        command.add("add");
                        command.add(keypath);
                        command.add("/v");
                        command.add("Install Dir");
                        command.add("/t");
                        command.add("REG_SZ");
                        command.add("/d");
                        command.add(selectedGamePath);
                        command.add("/f");
                        ModManager.debugLogger.writeMessage("Changing registry key to point to new biogame directory: " + selectedGamePath);
                        ProcessBuilder pb = new ProcessBuilder(command);
                        ProcessResult pr = ModManager.runProcess(pb);
                        if (pr.getReturnCode() == 0) {
                            ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updated registry key to point to selected installation");
                        }
                    }
                } else {
                    ModManager.debugLogger.writeMessage("REGISTRY PATH NOT FOUND. There's a likely reason...");
                }

                // CHECK WRITE PERMISSIONS
                File biogame = new File(selectedGamePath + "\\BIOGame");
                File selectedGamePathF = new File(selectedGamePath);
                File cookedPCConsole = new File(selectedGamePath + "\\BIOGame\\CookedPCConsole");

                if (biogame.exists() && selectedGamePathF.exists() && cookedPCConsole.exists()) { //prevents it from thinking it is unable to write due to non-existence
                    if (biogame.isDirectory() && selectedGamePathF.isDirectory() && cookedPCConsole.isDirectory()) { //make sure it is a folder so we can write into sub
                        boolean hasWritePermissions = ModManager.checkWritePermissions(selectedGamePath) && ModManager.checkWritePermissions(selectedGamePath + "\\BIOGame")
                                && ModManager.checkWritePermissions(selectedGamePath + "\\BIOGame\\CookedPCConsole");
                        if (!hasWritePermissions) {
                            showFolderPermissionsGrantDialog(selectedGamePath);
                        }
                    }
                }
                validateBIOGameDir(); // reset upper state
            }
        }
    }

    /**
     * Shows the write permissions dialog and will execute the windows command
     * to grant permissions to the current user if accepted.
     *
     * @param folder Folder to grant write permissions to.
     */
    private void showFolderPermissionsGrantDialog(String folder) {
        String username = Advapi32Util.getUserName();
        String message = "Your user account (" + username + ") does not have write permissions to the game directory:\n";
        message += folder + "\n\n";
        message += "Mod Manager can grant write permissions to this folder to you.\nDo you want to grant permissions?";

        int result = JOptionPane.showConfirmDialog(this, message, "Write Permissions Required", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            if (ModManager.GrantPermissionsToDirectory(folder, username)) {
                ModManager.debugLogger.writeMessage("Granted permissions to folder: " + folder);
                if (labelStatus != null) { // Can be null if permissions check is running at startup.
                    labelStatus.setText("Granted write permissions to game directory");
                }
            }
        }
    }

    /**
     * Attempts to highlight the mod in the list from information about the
     * passed in mod.
     *
     * @param searchmod Mod to highlight. Will use properties from this object to find
     *                  the correct one
     */
    public void highlightMod(Mod searchmod) {
        if (searchmod != null) {
            for (int i = 0; i < modModel.size(); i++) {
                Mod mod = modModel.getElementAt(i);
                if (mod.getDescFile().equals(searchmod.getDescFile()) && searchmod.getDescFile() != null) {
                    modList.setSelectedIndex(i);
                    modList.ensureIndexIsVisible(i);
                    return;
                }
            }
        }
    }

    private void setActivityIcon(boolean visbiility) {
        // labelStatus.setIcon(visbiility ? ModManager.ACTIVITY_ICON : null);
        activityPanel.setCollapsed(!visbiility);
    }

    class ModDeploymentThread extends SwingWorker<Boolean, ThreadCommand> {
        Mod mod;
        private File outfile;
        int jobCode;

        public ModDeploymentThread(Mod mod) {
            this.mod = mod;
            labelStatus.setText("Staging mod for deployment...");
            jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("Deploying " + mod.getModName());
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            String stagingdir = ModManager.getTempDir() + "Deployment Staging\\" + new File(mod.getModPath()).getName() + "\\";
            File stagingdirfile = new File(stagingdir);
            FileUtils.deleteQuietly(stagingdirfile);
            stagingdirfile.mkdirs();
            // System.out.println("Staging dir: " + stagingdir);

            // Identify files the mod uses.
            String modbasepath = mod.getModPath();
            int stagedfilecount = 0;
            for (ModJob job : mod.jobs) {
                // Files to replace
                for (String str : job.getFilesToReplace()) {
                    String relativepath = ResourceUtils.getRelativePath(str, modbasepath, File.separator);
                    String outputpath = stagingdir + relativepath;
                    ModManager.debugLogger.writeMessage("Copying mod file to staging: " + str + " -> " + outputpath);
                    FileUtils.copyFile(new File(str), new File(outputpath));
                    stagedfilecount++;
                    publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                }

                // Files to add
                for (String str : job.getFilesToAdd()) {
                    String relativepath = ResourceUtils.getRelativePath(str, modbasepath, File.separator);
                    String outputpath = stagingdir + relativepath;
                    ModManager.debugLogger.writeMessage("Copying mod file to staging: " + relativepath);
                    FileUtils.copyFile(new File(str), new File(outputpath));
                    stagedfilecount++;
                    publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                }

                // Manual Alternates
                for (AlternateFile af : job.getAlternateFiles()) {
                    String substitutefile = af.getSubtituteFile();
                    if (substitutefile != null) {
                        String outputpath = stagingdir + substitutefile;
                        ModManager.debugLogger.writeMessage("Copying [MANUAL SUB] mod file to staging: " + substitutefile + " -> " + outputpath);
                        FileUtils.copyFile(new File(mod.getModPath() + substitutefile), new File(outputpath));
                        stagedfilecount++;
                        publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                    }
                    String altfile = af.getAltFile();
                    if (altfile != null) {
                        String outputpath = stagingdir + altfile;
                        ModManager.debugLogger.writeMessage("Copying [MANUAL ALT] mod file to staging: " + altfile + " -> " + outputpath);
                        FileUtils.copyFile(new File(mod.getModPath() + altfile), new File(outputpath));
                        stagedfilecount++;
                        publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                    }
                }
            }

            // Automatically applied alternates
            for (AlternateFile af : mod.getAlternateFiles()) {
                String substitutefile = af.getSubtituteFile();
                if (substitutefile != null) {
                    String outputpath = stagingdir + substitutefile;
                    ModManager.debugLogger.writeMessage("Copying [AUTO SUB] mod file to staging: " + substitutefile + " -> " + outputpath);
                    FileUtils.copyFile(new File(mod.getModPath() + substitutefile), new File(outputpath));
                    stagedfilecount++;
                    publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                }
                String altfile = af.getAltFile();
                if (altfile != null) {
                    String outputpath = stagingdir + altfile;
                    ModManager.debugLogger.writeMessage("Copying [AUTO ALT] mod file to staging: " + altfile + " -> " + outputpath);
                    FileUtils.copyFile(new File(mod.getModPath() + altfile), new File(outputpath));
                    stagedfilecount++;
                    publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                }
            }

            // Alternate Custom DLC
            ArrayList<String> altDLCFoldersToStage = new ArrayList<>();
            for (AlternateCustomDLC altdlc : mod.getAlternateCustomDLC()) {
                String altDLCRelativePath = altdlc.getAltDLC();
                if (!altDLCFoldersToStage.contains(altDLCRelativePath)) {
                    altDLCFoldersToStage.add(altDLCRelativePath);
                }
            }

            for (String folder : altDLCFoldersToStage) {
                Predicate<Path> predicate = p -> Files.isRegularFile(p);
                String altdlcpath = mod.getModPath() + folder;
                ArrayList<Path> files = (ArrayList<Path>) Files.walk(Paths.get(altdlcpath)).filter(predicate).collect(Collectors.toList());
                for (Path p : files) {
                    File altfile = p.toFile();
                    String relativePath = ResourceUtils.getRelativePath(altfile.getAbsolutePath(), mod.getModPath(), File.separator);
                    String outputpath = stagingdir + relativePath;
                    ModManager.debugLogger.writeMessage("Copying [ALTERNATE DLC] mod file to staging: " + altfile + " -> " + outputpath);
                    FileUtils.copyFile(altfile, new File(outputpath));
                    stagedfilecount++;
                    publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
                }
            }

            // Variants/Deltas
            for (ModDelta d : mod.getModDeltas()) {
                String relativepath = ResourceUtils.getRelativePath(d.getDeltaFilepath(), modbasepath, File.separator);
                String outputpath = stagingdir + relativepath;
                ModManager.debugLogger.writeMessage("Copying [DELTA] mod file to staging: " + d.getDeltaFilepath() + " -> " + outputpath);
                FileUtils.copyFile(new File(d.getDeltaFilepath()), new File(outputpath));
                stagedfilecount++;
                publish(new ThreadCommand("UPDATE_STATUS", "Staged " + stagedfilecount + " files..."));
            }

            String stagingini = stagingdir + "moddesc.ini";
            FileUtils.copyFile(new File(mod.getDescFile()), new File(stagingini));

            ModManager.debugLogger.writeMessage("Testing staged mod");
            Mod testmod = new Mod(stagingini);
            if (!testmod.isValidMod()) {
                ModManager.debugLogger.writeError("Staged mod is not valid. Cannot compress.");
                return false;
            } else {
                ModManager.debugLogger.writeMessage("Staged mod is valid.");
            }

            publish(new ThreadCommand("UPDATE_STATUS", "Deploying - memory usage will be high temporarily"));

            String outputfile = ModManager.compressModForDeployment(testmod);
            outfile = new File(outputfile);
            ModManager.debugLogger.writeMessage("Thread exiting - result of compression method: " + outfile.exists());
            FileUtils.deleteDirectory(stagingdirfile);
            return outfile.exists();
        }

        @Override
        protected void process(List<ThreadCommand> chunks) {
            for (ThreadCommand latest : chunks) {
                switch (latest.getCommand()) {
                    case "UPDATE_STATUS":
                        labelStatus.setText(latest.getMessage());
                        break;

                }
            }
        }

        protected void done() {
            ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);

            try {
                boolean result = get();
                if (result && outfile != null) {
                    ModManager.debugLogger.writeMessage("SUCCESS COMPRESSING MOD.");
                    ArrayList<String> showInExplorerProcess = new ArrayList<String>();
                    labelStatus.setText("Mod deployment succeeded");
                    showInExplorerProcess.add("explorer.exe");
                    showInExplorerProcess.add("/select,");
                    showInExplorerProcess.add("\"" + outfile.getAbsolutePath() + "\"");
                    ProcessBuilder pb = new ProcessBuilder(showInExplorerProcess);
                    ModManager.runProcessDetached(pb);
                } else {
                    labelStatus.setText("Mod failed deployment - see logs");
                    ModManager.debugLogger.writeError("Mod failed to compress (output file does not exist!)");
                    ModManager.debugLogger.writeError("FAILURE COMPRESSING MOD.");
                }
            } catch (InterruptedException |

                    ExecutionException e) {
                ModManager.debugLogger.writeErrorWithException("Exception in ModDeploymentThread:", e);
            }
        }
    }

}