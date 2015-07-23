package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me3tweaks.modmanager.basegamedb.BasegameHashDB;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModXMLTools;
import com.me3tweaks.modmanager.modupdater.UpdatePackage;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.valueparsers.bioai.BioAIGUI;
import com.me3tweaks.modmanager.valueparsers.biodifficulty.DifficultyGUI;
import com.me3tweaks.modmanager.valueparsers.consumable.ConsumableGUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerCustomActionGUI;
import com.me3tweaks.modmanager.valueparsers.wavelist.WavelistGUI;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

@SuppressWarnings("serial")
public class ModManagerWindow extends JFrame implements ActionListener, ListSelectionListener {
	public static ModManagerWindow ACTIVE_WINDOW;
	boolean isUpdate;
	public JTextField fieldBiogameDir;
	JTextArea fieldDescription;
	JScrollPane scrollDescription;
	JButton buttonBioGameDir, buttonApplyMod, buttonStartGame;
	JFileChooser dirChooser;
	JMenuBar menuBar;
	JMenu actionMenu, modMenu, toolsMenu, backupMenu, restoreMenu, sqlMenu, helpMenu;
	JMenuItem actionModMaker, actionVisitMe, actionOpenME3Exp, actionReload, actionExit;
	JMenuItem modutilsHeader, modutilsInfoEditor, modutilsInstallCustomKeybinds, modutilsAutoTOC, modutilsUninstallCustomDLC, modutilsCheckforupdate;
	JMenuItem backupBackupDLC, backupBasegame;
	JMenuItem toolsModMaker, toolsMergeMod, toolsOpenME3Dir, toolsInstallLauncherWV, toolsInstallBinkw32, toolsUninstallBinkw32;
	JMenuItem restoreRevertEverything, restoreRevertBasegame, restoreRevertAllDLC, restoreRevertSPDLC, restoreRevertMPDLC, restoreRevertMPBaseDLC,
			restoreRevertSPBaseDLC, restoreRevertCoal;
	JMenuItem sqlWavelistParser, sqlDifficultyParser, sqlAIWeaponParser, sqlPowerCustomActionParser, sqlConsumableParser, sqlGearParser;
	JMenuItem helpPost, helpForums, helpAbout;
	JList<Mod> modList;
	JProgressBar progressBar;
	ListSelectionModel listSelectionModel;
	JSplitPane splitPane;
	public JLabel labelStatus;
	final String selectAModDescription = "Select a mod on the left to view its description and apply it.";
	DefaultListModel<Mod> modModel;
	// static HashMap<String, Mod> listDescriptors;
	private JMenuItem modutilsUpdateXMLGenerator;
	private JMenuItem toolsCheckallmodsforupdate;

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
		ModManager.debugLogger.writeMessage("Starting ModManagerWindow init");
		initializeWindow();
		ACTIVE_WINDOW = this;
		this.setVisible(true);
	}

	private void initializeWindow() {
		this.setTitle("Mass Effect 3 Coalesced Mod Manager");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		setupWindow(this);

		Dimension minSize = new Dimension(560, 520);
		this.setPreferredSize(minSize);
		this.setMinimumSize(minSize);
		this.pack();
		setLocationRelativeTo(null);
		if (isUpdate) {
			JOptionPane.showMessageDialog(this, "Update successful: Updated to Mod Manager " + ModManager.VERSION + " (Build "
					+ ModManager.BUILD_NUMBER + ").", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
		}

		if (ModManager.AUTO_UPDATE_MODS == false && !ModManager.ASKED_FOR_AUTO_UPDATE) {
			//ask user
			int result = JOptionPane
					.showConfirmDialog(
							this,
							"Mod Manager can automatically keep your mods up to date with ME3Tweaks checking once every three days.\nWould you like to turn this feature on?",
							"Mod Auto-Update", JOptionPane.YES_NO_CANCEL_OPTION);
			if (result != JOptionPane.CANCEL_OPTION) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "autoupdatemods", result == JOptionPane.YES_OPTION ? "true" : "false");
					ini.put("Settings", "declinedautoupdate", result == JOptionPane.YES_OPTION ? "false" : "true");
					ini.store();
				} catch (InvalidFileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
			}
		}
		new UpdateCheckThread().execute();
	}

	class UpdateCheckThread extends SwingWorker<Void, Object> {

		@Override
		public Void doInBackground() {
			File f7za = new File(ModManager.getToolsDir() + "7za.exe");
			if (!f7za.exists()) {
				ModManager.debugLogger.writeMessage("7za.exe does not exist at the following path, downloading new copy: " + f7za.getAbsolutePath());
				new Get7ZipThread().execute();
			}

			checkForUpdates();
			checkForModUpdates();
			return null;
		}

		private void checkForModUpdates() {
			// TODO Auto-generated method stub
			long threeDaysMs = 259200000L;
			if (ModManager.AUTO_UPDATE_MODS) {
				if (System.currentTimeMillis() - ModManager.LAST_AUTOUPDATE_CHECK > threeDaysMs) {
					ModManager.debugLogger.writeMessage("Running auto-updater, it has been "
							+ ModManager.getDurationBreakdown(System.currentTimeMillis() - ModManager.LAST_AUTOUPDATE_CHECK)
							+ " since the last update check.");
					publish("Auto Updater: Checking for mod updates");
					checkAllModsForUpdates(false);
					publish("Checked for updates to mods on ME3Tweaks");
				}
			}
		}

		@Override
		protected void process(List<Object> chunks) {
			Object latest = chunks.get(chunks.size() - 1);
			if (latest instanceof String) {
				String latestUpdate = (String) latest;
				labelStatus.setText(latestUpdate);
			}
		}

		@Override
		protected void done() {

		}
	}

	class Get7ZipThread extends SwingWorker<Void, Object> {

		@Override
		public Void doInBackground() {
			String url = "http://me3tweaks.com/modmanager/tools/7za.exe";
			try {
				File updateDir = new File(ModManager.getToolsDir());
				updateDir.mkdirs();
				FileUtils.copyURLToFile(new URL(url), new File(ModManager.getToolsDir() + "7za.exe"));
				publish("Downloaded Update Unzipper into tools directory");
				ModManager.debugLogger.writeMessage("Downloaded missing 7za.exe file for updating Mod Manager");

			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("Error downloading 7za into update folder");
				publish("Error downloading 7za for updating");
				ModManager.debugLogger.writeException(e);
			}
			return null;
		}

		@Override
		protected void process(List<Object> chunks) {
			Object latest = chunks.get(chunks.size() - 1);
			if (latest instanceof String) {
				String latestUpdate = (String) latest;
				labelStatus.setText(latestUpdate);
			}
		}

		@Override
		protected void done() {

		}
	}

	private Void checkForUpdates() {
		labelStatus.setText("Checking for updates...");
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
			} catch (Exception e) {
				ModManager.debugLogger.writeError("Error checking for updates:");
				ModManager.debugLogger.writeException(e);
				labelStatus.setText("Error checking for update (check logs)");
				return null;
			}
			if (serverJSON == null) {
				ModManager.debugLogger.writeError("No response from server");
				labelStatus.setText("Updater: No response from server");
				return null;
			}

			JSONParser parser = new JSONParser();
			JSONObject latest_object = (JSONObject) parser.parse(serverJSON);

			String buildHash = (String) latest_object.get("build_md5");
			boolean hashMismatch = false;
			try {
				String currentHash = MD5Checksum.getMD5Checksum("ME3CMM.exe");
				if (buildHash != null && !buildHash.equals("") && !currentHash.equals(buildHash)) {
					//hash mismatch
					hashMismatch = true;
				}
			} catch (Exception e1) {
				ModManager.debugLogger.writeErrorWithException("Unable to hash ME3CMM.exe:", e1);
			}

			long latest_build = (long) latest_object.get("latest_build_number");
			if (latest_build < ModManager.BUILD_NUMBER) {
				//build is newer than current
				labelStatus.setVisible(true);
				ModManager.debugLogger.writeMessage("No updates, at latest version. (or could not contact update server.)");
				labelStatus.setText("No Mod Manager updates available");
				return null;
			}

			if (latest_build == ModManager.BUILD_NUMBER && !hashMismatch) {
				//build is same as server version
				labelStatus.setVisible(true);
				ModManager.debugLogger.writeMessage("No updates, at latest version.");
				labelStatus.setText("No Mod Manager updates available");
				return null;
			}

			ModManager.debugLogger.writeMessage("Update check: Local:" + ModManager.BUILD_NUMBER + " Latest: " + latest_build + ", is less? "
					+ (ModManager.BUILD_NUMBER < latest_build));

			boolean showUpdate = true;
			// make sure the user hasn't declined this one.
			Wini settingsini;
			try {
				settingsini = new Wini(new File(ModManager.settingsFilename));
				String showIfHigherThan = settingsini.get("Settings", "nextupdatedialogbuild");
				long build_check = ModManager.BUILD_NUMBER;
				if (showIfHigherThan != null && !showIfHigherThan.equals("")) {
					try {
						build_check = Integer.parseInt(showIfHigherThan);
						if (latest_build > build_check) {
							System.out.println("Update params: " + latest_build + " > " + build_check);
							// update is newer than one stored in ini, show the
							// dialog.
							showUpdate = true;
						} else {
							// don't show it.
							showUpdate = false;
						}
					} catch (NumberFormatException e) {
						ModManager.debugLogger
								.writeMessage("Number format exception reading the build number updateon in the ini. Showing the dialog.");
					}
				}
			} catch (InvalidFileFormatException e) {
				ModManager.debugLogger.writeMessage("Invalid INI! Did the user modify it by hand?");
				e.printStackTrace();
			} catch (IOException e) {
				ModManager.debugLogger
						.writeMessage("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
			}

			if (showUpdate) {
				// An update is available!
				labelStatus.setVisible(true);
				labelStatus.setText("Update available");
				new UpdateAvailableWindow(latest_object, this);
			} else {
				labelStatus.setVisible(true);
				labelStatus.setText("No updates available");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

	private JFrame setupWindow(JFrame frame) {
		// Menubar
		menuBar = makeMenu();
		// Main Panel
		JPanel contentPanel = new JPanel(new BorderLayout());

		// North Panel
		JPanel northPanel = new JPanel(new BorderLayout());

		// Title Panel
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel("Mass Effect 3 Coalesced Mod Manager " + ModManager.VERSION, SwingConstants.LEFT), BorderLayout.WEST);

		// BioGameDir Panel
		JPanel cookedDirPanel = new JPanel(new BorderLayout());
		TitledBorder cookedDirTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Mass Effect 3 BIOGame Directory");
		fieldBiogameDir = new JTextField();
		fieldBiogameDir.setText(getLocationText(fieldBiogameDir));
		buttonBioGameDir = new JButton("Browse...");
		buttonBioGameDir
				.setToolTipText("<html>Browse and set the BIOGame directory.<br>This is located in the installation directory for Mass Effect 3.<br>Typically this is in the Origin Games folder.</html>");
		fieldBiogameDir.setColumns(37);
		buttonBioGameDir.setPreferredSize(new Dimension(90, 14));

		buttonBioGameDir.addActionListener(this);
		cookedDirPanel.setBorder(cookedDirTitle);
		cookedDirPanel.add(fieldBiogameDir, BorderLayout.CENTER);
		cookedDirPanel.add(buttonBioGameDir, BorderLayout.EAST);

		northPanel.add(titlePanel, BorderLayout.NORTH);
		northPanel.add(cookedDirPanel, BorderLayout.CENTER);

		// ModsList
		JPanel modsListPanel = new JPanel(new BorderLayout());
		// JLabel availableModsLabel = new JLabel("  Available Mods:");
		TitledBorder modListsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Available Mods");

		modList = new JList<Mod>();
		modList.addListSelectionListener(this);
		modList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(modList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// modsListPanel.add(availableModsLabel, BorderLayout.NORTH);
		modsListPanel.setBorder(modListsBorder);
		modsListPanel.add(listScroller, BorderLayout.CENTER);

		modModel = new DefaultListModel<Mod>();
		modList.setModel(modModel);
		ArrayList<Mod> modList = ModManager.getModsFromDirectory();
		for (Mod mod : modList) {
			modModel.addElement(mod);
		}

		// DescriptionField
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		// JLabel descriptionLabel = new JLabel("Mod Description:");
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description");
		descriptionPanel.setBorder(descriptionBorder);
		fieldDescription = new JTextArea(selectAModDescription);
		scrollDescription = new JScrollPane(fieldDescription);

		fieldDescription.setLineWrap(true);
		fieldDescription.setWrapStyleWord(true);

		fieldDescription.setEditable(false);
		scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(scrollDescription, BorderLayout.CENTER);
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
		progressBar = new JProgressBar();
		progressBar.setVisible(false);

		// ButtonPanel
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonApplyMod = new JButton("Apply Mod");
		buttonApplyMod.addActionListener(this);
		buttonApplyMod.setEnabled(false);
		buttonApplyMod.setToolTipText("Applies this mod to the game");

		buttonStartGame = new JButton("Start Game");
		buttonStartGame.addActionListener(this);
		buttonStartGame.setToolTipText("Starts the game. If LauncherWV DLC bypass is installed, it will that to launch the game instead");

		buttonPanel.add(buttonApplyMod);
		buttonPanel.add(buttonStartGame);
		applyPanel.add(labelStatus, BorderLayout.WEST);
		applyPanel.add(progressBar, BorderLayout.CENTER);
		applyPanel.add(buttonPanel, BorderLayout.EAST);

		southPanel.add(applyPanel, BorderLayout.SOUTH);

		// add all panels
		contentPanel.add(northPanel, BorderLayout.NORTH);
		contentPanel.add(splitPane, BorderLayout.CENTER);
		contentPanel.add(southPanel, BorderLayout.SOUTH);
		this.setJMenuBar(menuBar);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.add(contentPanel);
		verifyBackupCoalesced();
		return this;
	}

	private JMenuBar makeMenu() {
		menuBar = new JMenuBar();
		// Actions
		actionMenu = new JMenu("Actions");
		actionModMaker = new JMenuItem("Create a mod");
		actionModMaker.setToolTipText("Opens ME3Tweaks ModMaker");
		actionVisitMe = new JMenuItem("Open ME3Tweaks.com");
		actionVisitMe.setToolTipText("Opens ME3Tweaks.com");
		actionOpenME3Exp = new JMenuItem("Run ME3Explorer");
		actionOpenME3Exp.setToolTipText("Runs the bundled ME3Explorer program");
		actionReload = new JMenuItem("Reload Mods");
		actionReload.setToolTipText("Refreshes the list of mods and their descriptions");
		actionExit = new JMenuItem("Exit");
		actionExit.setToolTipText("Closes Mod Manager");

		actionMenu.add(actionModMaker);
		actionMenu.add(actionVisitMe);
		actionMenu.addSeparator();
		actionMenu.add(actionOpenME3Exp);
		actionMenu.add(actionReload);
		actionMenu.add(actionExit);

		actionModMaker.addActionListener(this);
		actionVisitMe.addActionListener(this);
		actionOpenME3Exp.addActionListener(this);
		actionReload.addActionListener(this);
		actionExit.addActionListener(this);
		menuBar.add(actionMenu);

		// MOD TOOLS
		modMenu = new JMenu("Mod Utils");
		modutilsHeader = new JMenuItem("No mod selected");
		modutilsHeader.setEnabled(false);
		modutilsInstallCustomKeybinds = new JMenuItem("Install custom keybinds into this mod");
		modutilsInstallCustomKeybinds.addActionListener(this);
		//check if BioInput.xml exists.
		if (checkForKeybindsOverride()) {
			modutilsInstallCustomKeybinds.setEnabled(false);
			modutilsInstallCustomKeybinds
					.setToolTipText("<html>To enable installing custom keybinds put a<br>BioInput.xml file in the data/override/ directory.</html>");
		} else {
			modutilsInstallCustomKeybinds.setToolTipText("<html>Replace BioInput.xml in the BASEGAME Coalesced file</html>");
		}

		modutilsUpdateXMLGenerator = new JMenuItem("Generate Mod XML");
		modutilsUpdateXMLGenerator.addActionListener(this);
		modutilsInfoEditor = new JMenuItem("Edit name/description");
		modutilsInfoEditor.addActionListener(this);
		modutilsInfoEditor.setToolTipText("Rename this mod and change the description shown in the descriptions window");
		modutilsAutoTOC = new JMenuItem("Run AutoTOC on this mod");
		modutilsAutoTOC.addActionListener(this);
		modutilsAutoTOC.setToolTipText("Automatically update all TOC files this mod uses with proper sizes to prevent crashes");
		modutilsUninstallCustomDLC = new JMenuItem("Uninstall this mod's custom DLC");
		modutilsUninstallCustomDLC.addActionListener(this);

		modutilsCheckforupdate = new JMenuItem("Check for newer version (ME3Tweaks)");
		modutilsCheckforupdate.addActionListener(this);

		modMenu.add(modutilsHeader);
		if (ModManager.IS_DEBUG) {
			modMenu.add(modutilsUpdateXMLGenerator);
		}
		modMenu.addSeparator();
		modMenu.add(modutilsInstallCustomKeybinds);
		modMenu.add(modutilsInfoEditor);
		modMenu.add(modutilsAutoTOC);
		modMenu.add(modutilsUninstallCustomDLC);
		modMenu.add(modutilsCheckforupdate);
		modMenu.setEnabled(false);
		menuBar.add(modMenu);

		// Tools
		toolsMenu = new JMenu("Tools");
		toolsModMaker = new JMenuItem("Enter ME3Tweaks ModMaker code");
		toolsModMaker.setToolTipText("Allows you to download and compile mods with ease");

		toolsMergeMod = new JMenuItem("Merge mods...");
		toolsMergeMod.setToolTipText("Allows you to merge CMM3+ mods together and resolve conflicts between mods");

		toolsOpenME3Dir = new JMenuItem("Open BIOGame directory");
		toolsOpenME3Dir.addActionListener(this);
		toolsOpenME3Dir.setToolTipText("Opens a Windows Explorer window in the BIOGame Directory");

		toolsInstallLauncherWV = new JMenuItem("Install LauncherWV DLC Bypass");
		toolsInstallLauncherWV
				.setToolTipText("<html>Installs an in-memory patcher giving you console and allowing modified DLC.<br>This does not does not modify files</html>");
		toolsInstallBinkw32 = new JMenuItem("Install Binkw32 DLC Bypass");
		toolsInstallBinkw32
				.setToolTipText("<html>Installs a startup patcher giving you console and allowing modified DLC.<br>This modifies your game and is erased when doing an Origin Repair</html>");
		toolsUninstallBinkw32 = new JMenuItem("Uninstall Binkw32 DLC Bypass");
		toolsUninstallBinkw32.setToolTipText("<html>Removes the Binkw32.dll startup patcher, reverting the original file</html>");

		toolsCheckallmodsforupdate = new JMenuItem("Check eligible mods for updates");
		toolsCheckallmodsforupdate
				.setToolTipText("Checks this mod version against the one on ME3Tweaks and prompts to download an update if one is available");

		toolsModMaker.addActionListener(this);
		toolsMergeMod.addActionListener(this);
		toolsCheckallmodsforupdate.addActionListener(this);
		toolsInstallLauncherWV.addActionListener(this);
		toolsInstallBinkw32.addActionListener(this);
		toolsUninstallBinkw32.addActionListener(this);

		toolsMenu.add(toolsModMaker);
		toolsMenu.addSeparator();
		toolsMenu.add(toolsMergeMod);
		toolsMenu.add(toolsCheckallmodsforupdate);
		toolsMenu.add(toolsOpenME3Dir);
		toolsMenu.addSeparator();

		toolsMenu.add(toolsInstallLauncherWV);
		toolsMenu.add(toolsInstallBinkw32);
		toolsMenu.add(toolsUninstallBinkw32);
		menuBar.add(toolsMenu);

		// BACKUP
		backupMenu = new JMenu("Backup");

		backupBackupDLC = new JMenuItem("Backup DLCs");
		backupBackupDLC
				.setToolTipText("Backs up your DLC to .bak files. When installing a mod it will ask if a .bak files does not exist if you want to backup");

		backupBasegame = new JMenuItem("Update basegame file database");
		backupBasegame.setToolTipText("Creates a database of checksums for basegame files so you can restore basegame files properly");

		backupBackupDLC.addActionListener(this);
		backupBasegame.addActionListener(this);

		backupMenu.add(backupBackupDLC);
		backupMenu.add(backupBasegame);
		menuBar.add(backupMenu);

		// RESTORE
		restoreMenu = new JMenu("Restore");
		restoreRevertEverything = new JMenuItem("Restore everything");
		restoreRevertEverything
				.setToolTipText("<html>Restores all basegame files, and checks all DLC.<br>This does not remove custom DLC modules.</html>");
		restoreRevertBasegame = new JMenuItem("Restore basegame files");
		restoreRevertBasegame.setToolTipText("<html>Restores all basegame files that have been modified by installing mods</html>");

		restoreRevertAllDLC = new JMenuItem("Restore all DLCs");
		restoreRevertAllDLC.setToolTipText("<html>Restores all DLC files.<br>This does not remove custom DLC modules.</html>");

		restoreRevertSPDLC = new JMenuItem("Restore SP DLCs");
		restoreRevertSPDLC.setToolTipText("<html>Restores all SP DLCs.<br>This does not remove custom DLC modules.</html>");

		restoreRevertSPBaseDLC = new JMenuItem("Restore SP DLC + Basegame");
		restoreRevertSPBaseDLC
				.setToolTipText("<html>Restores all basegame files, and checks all SP DLC files.<br>This does not remove custom DLC modules.</html>");

		restoreRevertMPDLC = new JMenuItem("Restore MP DLCs");
		restoreRevertMPDLC.setToolTipText("<html>Restores all MP DLCs.<br>This does not remove custom DLC modules</html>");

		restoreRevertMPBaseDLC = new JMenuItem("Restore MP DLC + Basegame");
		restoreRevertMPBaseDLC
				.setToolTipText("<html>Restores all basegame files, and checks all Multiplayer DLC files.<br>This does not remove custom DLC modules.<br>If you are doing multiplayer mods, you should use this to restore</html>");
		restoreRevertCoal = new JMenuItem("Restore vanilla Coalesced.bin");
		restoreRevertCoal.setToolTipText("<html>Restores the basegame coalesced file</html>");

		restoreRevertEverything.addActionListener(this);
		restoreRevertBasegame.addActionListener(this);
		restoreRevertAllDLC.addActionListener(this);
		restoreRevertSPDLC.addActionListener(this);
		restoreRevertSPBaseDLC.addActionListener(this);
		restoreRevertMPDLC.addActionListener(this);
		restoreRevertMPBaseDLC.addActionListener(this);
		restoreRevertCoal.addActionListener(this);

		restoreMenu.add(restoreRevertEverything);
		restoreMenu.add(restoreRevertBasegame);
		restoreMenu.add(restoreRevertAllDLC);
		restoreMenu.addSeparator();
		restoreMenu.add(restoreRevertMPDLC);
		restoreMenu.add(restoreRevertMPBaseDLC);
		restoreMenu.add(restoreRevertSPDLC);
		restoreMenu.add(restoreRevertSPBaseDLC);
		restoreMenu.addSeparator();
		restoreMenu.add(restoreRevertCoal);
		menuBar.add(restoreMenu);

		sqlMenu = new JMenu("SQL");
		sqlWavelistParser = new JMenuItem("Wavelist Parser");
		sqlDifficultyParser = new JMenuItem("Biodifficulty Parser");
		sqlAIWeaponParser = new JMenuItem("BioAI Parser");
		sqlPowerCustomActionParser = new JMenuItem("CustomAction Parser");
		sqlConsumableParser = new JMenuItem("Consumable Parser");
		sqlGearParser = new JMenuItem("Gear Parser");

		sqlWavelistParser.addActionListener(this);
		sqlDifficultyParser.addActionListener(this);
		sqlAIWeaponParser.addActionListener(this);
		sqlPowerCustomActionParser.addActionListener(this);
		sqlConsumableParser.addActionListener(this);
		sqlGearParser.addActionListener(this);

		sqlMenu.add(sqlWavelistParser);
		sqlMenu.add(sqlDifficultyParser);
		sqlMenu.add(sqlAIWeaponParser);
		sqlMenu.add(sqlPowerCustomActionParser);
		sqlMenu.add(sqlConsumableParser);
		sqlMenu.add(sqlGearParser);
		if (ModManager.IS_DEBUG) {
			menuBar.add(sqlMenu);
		}

		// Help
		helpMenu = new JMenu("Help");
		helpPost = new JMenuItem("View Instructions");
		helpPost.setToolTipText("Opens the Mod Manager FAQ");
		helpForums = new JMenuItem("Forums");
		helpForums.setToolTipText("Opens the ME3Tweaks forum on ME3Explorer Forums");
		helpAbout = new JMenuItem("About...");
		helpAbout.setToolTipText("<html>Opens the Mod Manager About page.<br>Contains the mod debugging switch</html>");

		helpForums.addActionListener(this);
		helpPost.addActionListener(this);
		helpAbout.addActionListener(this);

		helpMenu.add(helpPost);
		helpMenu.add(helpForums);
		helpMenu.add(helpAbout);
		menuBar.add(helpMenu);

		return menuBar;
	}

	private void verifyBackupCoalesced() {
		File restoreTest = new File(ModManager.getDataDir() + "Coalesced.original");
		if (!restoreTest.exists()) {
			ModManager.debugLogger.writeMessage("Didn't find Coalesced.original - checking existing installed one, will copy if verified.");
			// try to copy the current one
			String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e";
			File coalesced = new File(ModManager.appendSlash(fieldBiogameDir.getText().toString()) + "CookedPCConsole\\Coalesced.bin");
			// Take the MD5 first to verify it.
			if (coalesced.exists()) {
				try {
					if (patch3CoalescedHash.equals(MD5Checksum.getMD5Checksum(coalesced.toString()))) {
						// back it up
						Files.copy(coalesced.toPath(), restoreTest.toPath());
						restoreRevertCoal.setEnabled(true);
						ModManager.debugLogger.writeMessage("Backed up Coalesced.");
					} else {
						ModManager.debugLogger.writeMessage("Didn't back up coalecsed, hash mismatch.");
						restoreRevertCoal.setEnabled(false);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				ModManager.debugLogger.writeMessage("Coalesced.bin was not found - unable to back up automatically");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// too bad we can't do a switch statement on the object :(
		if (e.getSource() == buttonBioGameDir) {
			dirChooser = new JFileChooser();
			File tryDir = new File(fieldBiogameDir.getText());
			if (tryDir.exists()) {
				dirChooser.setCurrentDirectory(new File(fieldBiogameDir.getText()));
			} else {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Directory " + fieldBiogameDir.getText()
							+ " does not exist, defaulting to working directory.");
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
				checkForValidBioGame();
			} else {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("No directory selected...");
				}
			}
		} else if (e.getSource() == toolsModMaker) {
			if (validateBIOGameDir()) {
				ModManager.debugLogger.writeMessage("Opening ModMaker Entry Window");
				new ModMakerEntryWindow(this, fieldBiogameDir.getText());
			} else {
				labelStatus.setText("ModMaker requires valid BIOGame directory to start");
				labelStatus.setVisible(true);
				JOptionPane.showMessageDialog(null, "The BIOGame directory is not valid.\nFix the BIOGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == backupBackupDLC) {
			backupDLC(fieldBiogameDir.getText());
		} else

		if (e.getSource() == backupBasegame) {
			if (validateBIOGameDir()) {
				createBasegameDB(fieldBiogameDir.getText());
			} else {
				JOptionPane
						.showMessageDialog(
								null,
								"The BioGame directory is not valid.\nMod Manager cannot update or create the Basegame Database.\nFix the BioGame directory before continuing.",
								"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertCoal) {
			if (validateBIOGameDir()) {
				restoreCoalesced(fieldBiogameDir.getText());
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertAllDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.ALL);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertBasegame) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.BASEGAME);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertSPDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.SP);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertMPDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.MP);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertSPBaseDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.SPBASE);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == restoreRevertMPBaseDLC) {
			if (validateBIOGameDir()) {
				restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.MPBASE);
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == restoreRevertEverything) {
			if (validateBIOGameDir()) {
				restoreEverything(fieldBiogameDir.getText());
			} else {
				JOptionPane.showMessageDialog(null,
						"The BioGame directory is not valid.\nMod Manager cannot do any restorations.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			}
		} else

		if (e.getSource() == actionModMaker) {
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
		} else

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
		} else if (e.getSource() == actionReload) {
			// Reload this jframe
			new ModManagerWindow(false);
		} else

		if (e.getSource() == actionExit) {
			System.exit(0);
		} else

		if (e.getSource() == helpPost) {
			URI theURI;
			try {
				theURI = new URI("http://me3tweaks.com/tools/modmanager/faq");
				java.awt.Desktop.getDesktop().browse(theURI);
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		} else if (e.getSource() == helpForums) {
			URI theURI;
			try {
				theURI = new URI("http://me3explorer.freeforums.org/me3tweaks-f33.html");
				java.awt.Desktop.getDesktop().browse(theURI);
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		if (e.getSource() == helpAbout) {
			new AboutWindow(this);
		} else if (e.getSource() == buttonApplyMod) {
			applyMod();
		} else

		if (e.getSource() == buttonStartGame) {
			startGame(ModManager.appendSlash(fieldBiogameDir.getText()));
		} else

		if (e.getSource() == actionOpenME3Exp) {
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.appendSlash(ModManager.getME3ExplorerEXEDirectory(true)) + "ME3Explorer.exe");
			// System.out.println("Building command");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			// Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			ModManager.debugLogger.writeMessage("Executing me3explorer command: " + sb.toString());
			try {
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.start();
			} catch (IOException ex) {
				ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(ex));
			}
		} else if (e.getSource() == toolsMergeMod) {
			new MergeModWindow(this);
		} else if (e.getSource() == toolsOpenME3Dir) {
			openME3Dir();
		} else if (e.getSource() == modutilsAutoTOC) {
			autoTOC();
		} else if (e.getSource() == modutilsInfoEditor) {
			showInfoEditor();
		} else if (e.getSource() == modutilsUninstallCustomDLC) {
			uninstallCustomDLC();
		} else if (e.getSource() == sqlWavelistParser) {
			new WavelistGUI();
		} else if (e.getSource() == modutilsCheckforupdate) {
			checkForModUpdate();
		} else if (e.getSource() == modutilsInstallCustomKeybinds) {
			new KeybindsInjectionWindow(this, modModel.getElementAt(modList.getSelectedIndex()));
		} else if (e.getSource() == toolsCheckallmodsforupdate) {
			checkAllModsForUpdates(true);
		} else if (e.getSource() == modutilsUpdateXMLGenerator) {
			ModManager.debugLogger.writeMessage(ModXMLTools.generateXMLList(modModel.getElementAt(modList.getSelectedIndex())));
		} else if (e.getSource() == sqlDifficultyParser) {
			new DifficultyGUI();
		} else

		if (e.getSource() == sqlAIWeaponParser) {
			new BioAIGUI();
		} else

		if (e.getSource() == sqlPowerCustomActionParser) {
			new PowerCustomActionGUI();
		} else if (e.getSource() == sqlConsumableParser) {
			new ConsumableGUI();
		} else if (e.getSource() == sqlGearParser) {
			// new GearGUI();
		} else if (e.getSource() == toolsInstallLauncherWV) {
			installBypass();
		} else if (e.getSource() == toolsInstallBinkw32) {
			installBinkw32Bypass();
		} else if (e.getSource() == toolsUninstallBinkw32) {
			uninstallBinkw32Bypass();
		}
	}

	private void checkAllModsForUpdates(boolean isManualCheck) {
		//ArrayList<Mod> updateDescs = new ArrayList<Mod>();
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
				ModManager.debugLogger.writeMessage(mod.getModName() + " is not me3tweaks updatable");
			}
		}

		if (updatableMods.size() > 0) {
			new AllModsUpdateWindow(this, isManualCheck, updatableMods);
		} else {
			if (isManualCheck) {
				JOptionPane
						.showMessageDialog(
								null,
								"No mods are eligible for the Mod Manager update service.\nEligible mods include ModMaker mods and ones hosted on ME3Tweaks.com.",
								"No updatable mods", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Downloads from ME3Tweaks a manifest of the latest version of a mod. It
	 * then checks the version of the local mod against it. If the other one is
	 * higher a list of files to update and delete are constructed and then a
	 * user can elect to update
	 */
	private void checkForModUpdate() {
		Mod mod = modModel.get(modList.getSelectedIndex());
		UpdatePackage upackage = ModXMLTools.validateLatestAgainstServer(mod);
		if (upackage != null) {
			// an update is available
			int result = JOptionPane.showConfirmDialog(this, "An update to " + mod.getModName() + " is available from ME3Tweaks.\nLocal Version: "
					+ mod.getVersion() + "\nServer Version: " + upackage.getVersion() + "\nUpgrade this mod?", "Update available",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				ModUpdateWindow muw = new ModUpdateWindow(upackage);
				muw.startUpdate(this);
			}
		} else {
			labelStatus.setText(mod.getModName() + " is up to date with ME3Tweaks");
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
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the Custom DLC of this mod selected
	 */
	private void uninstallCustomDLC() {
		if (validateBIOGameDir()) {
			String dlcdir = ModManager.appendSlash(fieldBiogameDir.getText()) + "DLC" + File.separator;
			Mod mod = modModel.get(modList.getSelectedIndex());
			ModManager.debugLogger.writeMessage("Deleting custom content for mod: " + mod.getModName());
			for (ModJob job : mod.jobs) {
				if (job.getModType() != ModJob.CUSTOMDLC) {
					continue; // skip
				}

				for (String customDLCfolder : job.getDestFolders()) {
					File custDLC = new File(dlcdir + customDLCfolder);
					ModManager.debugLogger.writeMessage("Checking for custom content installed: " + custDLC.getAbsolutePath());

					if (custDLC.exists()) {
						try {
							FileUtils.forceDelete(custDLC);
							ModManager.debugLogger.writeMessage("Deleted custom DLC content " + custDLC.getAbsolutePath());
						} catch (IOException e) {
							ModManager.debugLogger.writeError("Couldn't delete custom DLC content " + custDLC.getAbsolutePath());
							ModManager.debugLogger.writeException(e);
						}
					}
				}
			}
			labelStatus.setText("Deleted custom DLC content");
		}
	}

	/**
	 * Shows the name/descip editor
	 */
	private void showInfoEditor() {
		// TODO Auto-generated method stub
		int selectedIndex = modList.getSelectedIndex();
		if (selectedIndex < 0) {
			return; // shouldn't be able to toc an unselected mod eh?
		}
		// System.out.println("SELECTED VALUE: " + selectedValue);
		Mod mod = modModel.get(selectedIndex);
		new ModInfoEditor(this, mod);
	}

	private void createBasegameDB(String biogameDir) {
		File file = new File(biogameDir);
		new BasegameHashDB(this, file.getParent(), true);
	}

	private void backupDLC(String bioGameDir) {
		// Check that biogame is valid
		if (validateBIOGameDir()) {
			new BackupWindow(this, bioGameDir);
		} else {
			// Biogame is invalid
			JOptionPane.showMessageDialog(null,
					"The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
					"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			labelStatus.setText(" DLC backup failed");
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
			JOptionPane.showMessageDialog(null,
					"The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.",
					"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			labelStatus.setText(" DLC backup failed");
			labelStatus.setVisible(true);
		}
		return;
	}

	/**
	 * Checks that the user has chosen a correct biogame directory.
	 */
	private void checkForValidBioGame() {
		File coalesced = new File(ModManager.appendSlash(dirChooser.getSelectedFile().toString()) + "CookedPCConsole\\Coalesced.bin");
		if (coalesced.exists()) {
			String YesNo[] = { "Yes", "No" };
			int saveDir = JOptionPane.showOptionDialog(null, "BioGame directory set to: " + dirChooser.getSelectedFile().toString()
					+ "\nSave this path?", "Save BIOGame path?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
			if (saveDir == 0) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "biogame_dir", ModManager.appendSlash(dirChooser.getSelectedFile().toString()));
					ModManager.debugLogger.writeMessage(ModManager.appendSlash(dirChooser.getSelectedFile().toString()));
					ini.store();
				} catch (InvalidFileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
				labelStatus.setText("Saved BioGame directory to me3cmm.ini");
				labelStatus.setVisible(true);
			}
			fieldBiogameDir.setText(dirChooser.getSelectedFile().toString());
		} else {
			JOptionPane.showMessageDialog(null, "Coalesced.bin not found in " + dirChooser.getSelectedFile().toString(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Checks if the string in the biogamedir is a valid biogame directory.
	 * 
	 * @return True if valid, false otherwise
	 */
	private boolean validateBIOGameDir() {
		File coalesced = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + "CookedPCConsole\\Coalesced.bin");
		if (coalesced.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private String getLocationText(JTextField locationSet) {
		Wini settingsini;
		String setDir = "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BIOGame\\";
		String os = System.getProperty("os.name");
		try {
			settingsini = new Wini(new File(ModManager.settingsFilename));
			setDir = settingsini.get("Settings", "biogame_dir");
			if (setDir == null || setDir.equals("")) {
				// Try to detect it via the registry
				if (os.contains("Windows")) {
					String installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
							"SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{534A31BD-20F4-46b0-85CE-09778379663C}",
							"InstallLocation");
					ModManager.debugLogger.writeMessage("Found mass effect 3 in registry: " + installDir);
					setDir = installDir + "\\BIOGame";
				}
			}
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			try {
				System.err.println("I/O Error reading settings file. It may not exist yet.");
				// Try to make one
				if (os.contains("Windows")) {
					String installDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
							"SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{534A31BD-20F4-46b0-85CE-09778379663C}",
							"InstallLocation");
					File bgdir = new File(installDir + "\\BIOGame");
					if (bgdir.exists()) {
						// its correct
						Wini ini;
						try {
							File settings = new File(ModManager.settingsFilename);
							if (!settings.exists())
								settings.createNewFile();
							ini = new Wini(settings);
							ini.put("Settings", "biogame_dir", bgdir.toString());
							if (ModManager.logging) {
								ModManager.debugLogger.writeMessage(bgdir.toString() + " was detected via the registry to be the biogame dir.");
							}
							ini.store();
							setDir = bgdir.toString();
							// fieldBiogameDir.setText(bgdir.toString());
						} catch (InvalidFileFormatException ex) {
							e.printStackTrace();
						} catch (IOException ex) {
							System.err.println("Could not automatically save detected biogame dir.");
						}
					} else {
						System.out.println("BGDIR DOESNT EXIST!");
					}
					System.out.println(installDir);
				}
			} catch (Exception ex) {
				ModManager.debugLogger.writeMessage("Exception occured!");
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeMessage("Exception occured!");
			return "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BIOGame";
		}

		return setDir;
	}

	/**
	 * Installs the mod.
	 * 
	 * @return True if the file copied, otherwise false
	 */
	private boolean applyMod() {
		// Prepare
		labelStatus.setText(" Installing mod...");
		labelStatus.setVisible(true);

		// Validate BioGame Dir
		File coalesced = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + "CookedPCConsole\\" + "Coalesced.bin");
		if (ModManager.logging) {
			ModManager.debugLogger.writeMessage("Validating BioGame dir: " + coalesced);
		}
		if (!coalesced.exists()) {
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);

			labelStatus.setText(" Mod not installed");
			labelStatus.setVisible(true);
			return false;
		}

		Mod mod = modModel.get(modList.getSelectedIndex());
		// Read mod
		// Apply The Mod
		// Update Coalesced (main game)
		if (mod.modsCoal()) {
			if (ModManager.checkDoOriginal(fieldBiogameDir.getText())) {
				// check source file
				File source = new File(ModManager.appendSlash(mod.getModPath()) + "Coalesced.bin");

				if (!source.exists()) {
					labelStatus.setText("Mod not installed");
					labelStatus.setVisible(true);
					JOptionPane
							.showMessageDialog(
									null,
									"Coalesced.bin was not found in the Mod file's directory.\nIt might have moved, been deleted, or renamed.\nPlease check this mod's directory.",
									"Coalesced not found", JOptionPane.ERROR_MESSAGE);

					return false;
				}

				String destFile = coalesced.toString();
				String sourceFile = ModManager.appendSlash(mod.getModPath()) + "Coalesced.bin";
				try {
					Files.copy(Paths.get(sourceFile), Paths.get(destFile), StandardCopyOption.REPLACE_EXISTING);
					if (mod.getJobs().length == 0) {
						labelStatus.setText(" " + mod.getModName() + " installed");
					} else {
						labelStatus.setText("Injecting files into DLC modules...");
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Copying Coalesced.bin failed. Stack trace:\n" + e.getMessage(),
							"Error copying Coalesced.bin", JOptionPane.ERROR_MESSAGE);
					labelStatus.setText("Mod failed to install");
				}
			} else {

				labelStatus.setText(" Mod not installed");
				labelStatus.setVisible(true);
				return false;
			}
		}

		if (mod.getJobs().length > 0) {
			checkBackedUp(mod);
			new PatchWindow(this, mod.getJobs(), fieldBiogameDir.getText(), mod);
		} else {
			ModManager.debugLogger.writeMessage("No dlc mod job, finishing mod installation");
		}
		return true;
	}

	private void checkBackedUp(Mod mod) {
		ModJob[] jobs = mod.getJobs();
		for (ModJob job : jobs) {
			if (job.getModType() == ModJob.BASEGAME || job.getModType() == ModJob.CUSTOMDLC) {
				continue; // we can't really check for a .bak of Coalesced.
			}
			// Default.sfar
			File backFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Default.sfar.bak");
			System.out.println("Checking for backup file: " + backFile.getAbsolutePath());
			if (!backFile.exists()) {
				// Patch_001.sfar
				backFile = new File(ModManager.appendSlash(fieldBiogameDir.getText()) + job.getDLCFilePath() + "\\Patch_001.sfar.bak");
				System.out.println("Checking for backup file: " + backFile.getAbsolutePath());

				if (!backFile.exists()) {
					String YesNo[] = { "Yes", "No" }; // Yes/no buttons
					int showDLCBackup = JOptionPane.showOptionDialog(null, "<html>" + job.getJobName()
							+ " DLC has not been backed up.<br>Back it up now?</hmtl>", "Backup DLC", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
					if (showDLCBackup == 0) {
						autoBackupDLC(fieldBiogameDir.getText(), job.getJobName());
					}
				}
			}
			// Patch001.sfar
		}

		/*
		 * Wini ini; boolean answer = true; try { File settings = new
		 * File(ModManager.settingsFilename); if (!settings.exists()) {
		 * ModManager.debugLogger.writeMessage(
		 * "Creating settings file, it did not previously exist.");
		 * settings.createNewFile(); } ini = new Wini(settings); String
		 * backupFlag = ini.get("Settings", "dlc_backed_up"); if (backupFlag ==
		 * null || !backupFlag.equals("1")) { // backup flag not set, lets ask
		 * user if (ModManager.logging) { ModManager.debugLogger .writeMessage(
		 * "Did not read the backup flag from settings or flag was not set to 1"
		 * ); } String YesNo[] = { "Yes", "No" }; // Yes/no buttons int
		 * showDLCBackup = JOptionPane .showOptionDialog( null,
		 * "This instance of Mod Manager hasn't backed up your DLC's yet [this dialog is bugged].\nIf you have previously backed up using Mod Manager, you can ignore this message.\nYou really should back up your DLC so restoring them is faster than using Origin's repair game service.\n\n\nOpen the backup manager window?"
		 * , "Backup DLC before mod installation?", JOptionPane.YES_NO_OPTION,
		 * JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]); //
		 * System.out.println("User chose: "+showDLCBackup); if (showDLCBackup
		 * == 0) { backupDLC(fieldBiogameDir.getText()); } } // shown only once.
		 * Backup complete, set to settings file // ini.put("Settings",
		 * "dlc_backup_flag", "1"); //ini.store(); } catch
		 * (InvalidFileFormatException e) {
		 * ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e));
		 * } catch (IOException e) { System.err .println(
		 * "Settings file encountered an I/O error while attempting to write it. Settings not saved."
		 * ); } return answer;
		 */
	}

	/**
	 * Handles looking up the name of a mod from the mod object that it comes
	 * from. Uses a hash map.
	 * 
	 * @param modName
	 *            Name of the mod from the list (display name)
	 * @return File that describes the selected mod
	 * 
	 *         private Mod lookupModByFileName(String modName) { if
	 *         (listDescriptors.containsKey(modName) == false) {
	 *         ModManager.debugLogger.writeMessage(modName +
	 *         " doesn't exist in the mod hashmap."); return null; }
	 *         ModManager.debugLogger.writeMessage(
	 *         "Hashmap contains location of mod " + modName + ": " +
	 *         listDescriptors.containsKey(modName));
	 * 
	 *         return listDescriptors.get(modName); }
	 */

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (modList.getSelectedIndex() == -1) {
				buttonApplyMod.setEnabled(false);
				fieldDescription.setText(selectAModDescription);
				modMenu.setEnabled(false);
				modMenu.setToolTipText("Select a mod to enable this menu");
			} else {
				Mod selectedMod = modModel.get(modList.getSelectedIndex());
				modMenu.setToolTipText(null);

				// Update mod description
				fieldDescription.setText(selectedMod.getModDisplayDescription());
				fieldDescription.setCaretPosition(0);
				buttonApplyMod.setEnabled(checkIfNone(modList.getSelectedValue().toString()));
				modutilsHeader.setText(modModel.get(modList.getSelectedIndex()).getModName());
				modMenu.setEnabled(true);
				if (selectedMod.isME3TweaksUpdatable()) {
					modutilsCheckforupdate.setEnabled(true);
					modutilsCheckforupdate.setText("Update mod from ME3Tweaks");
					modutilsCheckforupdate.setToolTipText("Checks for updates on ME3Tweaks.com and upgrades this mod");
				} else {
					modutilsCheckforupdate.setEnabled(false);
					modutilsCheckforupdate.setText("Mod not eligible for ME3Tweaks updating");
					modutilsCheckforupdate
							.setToolTipText("<html>Mod update eligibility requires a floating point version number<br>and an update code from ME3Tweaks</html>");
					modutilsUninstallCustomDLC.setToolTipText("Deletes this mod's custom DLC modules");
				}

				if (selectedMod.containsCustomDLCJob()) {
					modutilsUninstallCustomDLC.setEnabled(true);
					modutilsUninstallCustomDLC.setText("Uninstall this mod's custom DLC content");
				} else {
					modutilsUninstallCustomDLC.setEnabled(false);
					modutilsUninstallCustomDLC.setText("Mod does not use custom DLC content");
					modutilsUninstallCustomDLC.setToolTipText("This mod does not install any custom DLC modules");
				}
			}
		}
	}

	/**
	 * Checks if the mod in the list is "No Mods Available" to see if the Apply
	 * Button should be disabled.
	 * 
	 * @param modName
	 *            Name of the mod in the list to be checked
	 * @return False if it is, otherwise true
	 */
	private boolean checkIfNone(String modName) {
		if (modName.equals("No Mods Available")) {
			return false;
		}
		return true;
	}

	private boolean restoreEverything(String bioGameDir) {
		if (restoreCoalesced(bioGameDir)) { // attempt to restore coalesced. if
											// it fails, don't bother with the
											// rest.
			return restoreDataFiles(bioGameDir, RestoreMode.ALL);
		} else {
			// something failed
			JOptionPane.showMessageDialog(null, "Your DLC has not been restored.", "DLC restoration error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	private boolean restoreCoalesced(String bioGameDir) {
		String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e"; // This
																			// is
																			// Patch
																			// 3
																			// Coalesced's
																			// hash
																			// -
																			// the
																			// final
																			// one
		File cOriginal = new File(ModManager.getDataDir() + "Coalesced.original");
		if (cOriginal.exists()) {
			// Take the MD5 first to verify it.
			try {
				if (patch3CoalescedHash.equals(MD5Checksum.getMD5Checksum(cOriginal.toString()))) {
					// file is indeed the original
					// Copy
					String destFile = ModManager.appendSlash(bioGameDir) + "CookedPCConsole\\Coalesced.bin";
					if (new File(destFile).exists() == false) {
						JOptionPane
								.showMessageDialog(
										null,
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
					JOptionPane
							.showMessageDialog(
									null,
									"Your backed up original Coalesced.bin file does not match the known original from Mass Effect 3.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.",
									"Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (Exception e) {
				ModManager.debugLogger.writeException(e);
				labelStatus.setText("Coalesced.bin not restored");
				labelStatus.setVisible(true);
				e.printStackTrace();
				return false;
			}
		} else {
			labelStatus.setText("Coalesced.bin not restored");
			labelStatus.setVisible(true);
			JOptionPane
					.showMessageDialog(
							null,
							"The backed up Coalesced.bin file (data/Coalesced.original) does not exist.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.\n\nThis error should have been caught but can be thrown due to file system changes \nwhile the program is open.",
							"Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);

		}
		return false;
	}

	/**
	 * Restores all DLCs to their original state if it can, using hashes to
	 * validate authenticity.
	 * 
	 * @param bioGameDir
	 *            Directory to biogame folder
	 * @return True if all were restored, false otherwise
	 */
	private boolean restoreDataFiles(String bioGameDir, int restoreMode) {
		// Check to make sure biogame is correct
		if (validateBIOGameDir()) {
			new RestoreFilesWindow(this, bioGameDir, restoreMode);
			return true;
		} else {
			labelStatus.setText("Restore Failed");
			labelStatus.setVisible(true);
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid. Files cannot be restored.", "Invalid BioGame Directory",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void startGame(String CookedDir) {
		File startingDir = new File(CookedDir);
		/*
		 * for (int i = 0; i<2; i++){ if (!startingDir.exists()){
		 * JOptionPane.showMessageDialog(null,
		 * "Unable to find the following game executable:\n" +CookedDir+
		 * "\nMake sure your BIOGame directory is correct.",
		 * "Unable to Launch Game", JOptionPane.ERROR_MESSAGE); return; } }
		 */
		boolean binkw32bypass = checkForBinkBypass(); // favor bink over WV
		startingDir = new File(startingDir.getParent());
		File executable = new File(startingDir.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
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
			JOptionPane.showMessageDialog(null, "Unable to find game executable in the specified directory:\n" + executable.getAbsolutePath()
					+ "\nMake sure your BIOGame directory is correct.", "Unable to Launch Game", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Executable exists.
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", executable.getAbsolutePath() };
		try {
			labelStatus.setText("Launched Mass Effect 3");
			this.setExtendedState(JFrame.ICONIFIED);
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ModManager.debugLogger.writeMessage("Path: " + executable.getAbsolutePath() + " - Exists? " + executable.exists());
	}

	/**
	 * Checks for the binkw32 bypass. If its there it will skip the LauncherWV
	 * check.
	 * 
	 * @return
	 */
	private boolean checkForBinkBypass() {
		String wvdlcBink32MD5 = "5a826dd66ad28f0099909d84b3b51ea4"; // Binkw32.dll
																	// that
																	// bypasses
																	// DLC check
																	// (WV) -
																	// from
																	// Private
																	// Server
																	// SVN
		String wvdlcBink32MD5_2 = "05540bee10d5e3985608c81e8b6c481a"; // Binkw32.dll
																		// that
																		// bypasses
																		// DLC
																		// check
																		// (WV)
																		// -
																		// from
																		// DLC
																		// Patcher
																		// thread

		File bgdir = new File(fieldBiogameDir.getText());
		if (!bgdir.exists()) {
			return false;
		}
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Game directory: " + gamedir.toString());
		File bink32 = new File(gamedir.toString() + "\\Binaries\\Win32\\binkw32.dll");
		try {
			String binkhash;
			binkhash = MD5Checksum.getMD5Checksum(bink32.toString());
			if (binkhash.equals(wvdlcBink32MD5) || binkhash.equals(wvdlcBink32MD5_2)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ModManager.debugLogger.writeMessage("Bink bypass was not found.");
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e));
		}
		return false;
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

	private void autoTOC() {
		// update the PCConsoleTOC's of a specific mod.
		int selectedIndex = modList.getSelectedIndex();
		if (selectedIndex < 0) {
			return; // shouldn't be able to toc an unselected mod eh?
		}
		// System.out.println("SELECTED VALUE: " + selectedValue);
		Mod mod = modModel.getElementAt(selectedIndex);
		new AutoTocWindow(mod);
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
		JOptionPane
				.showMessageDialog(
						null,
						"The BioGame directory is not valid.\nMod Manager cannot install the LauncherWV DLC bypass.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private boolean installBinkw32Bypass() {
		if (validateBIOGameDir()) {
			boolean result = ModManager.installBinkw32Bypass(fieldBiogameDir.getText());
			if (result) {
				// ok
				labelStatus.setText("Binkw32 installed. DLC will always authorize.");
			} else {
				labelStatus.setText("FAILURE: Binkw32 bypass not installed!");
			}
			return result;
		}
		JOptionPane
				.showMessageDialog(
						null,
						"The BioGame directory is not valid.\nMod Manager cannot install Binkw32.dll DLC bypass.\nFix the BioGame directory before continuing.",
						"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	private boolean uninstallBinkw32Bypass() {
		if (validateBIOGameDir()) {
			boolean result = ModManager.uninstallBinkw32Bypass(fieldBiogameDir.getText());
			if (result) {
				// ok
				labelStatus.setText("Binkw32 bypass uninstalled.");
			} else {
				labelStatus.setText("FAILURE: Binkw32 bypass not uninstalled!");
			}
			return result;
		}
		JOptionPane
				.showMessageDialog(
						null,
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

}