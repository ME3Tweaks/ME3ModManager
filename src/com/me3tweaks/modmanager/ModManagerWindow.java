package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import javax.swing.BorderFactory;
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
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me3tweaks.modmanager.valueparsers.biodifficulty.DifficultyGUI;
import com.me3tweaks.modmanager.valueparsers.wavelist.WavelistGUI;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

@SuppressWarnings("serial")
public class ModManagerWindow extends JFrame implements ActionListener, ListSelectionListener {
	boolean isUpdate;
	JTextField fieldBiogameDir;
	JTextArea fieldDescription;
	JScrollPane scrollDescription;
	JButton buttonBioGameDir, buttonApplyMod, buttonStartGame, buttonStartGameDLC;
	JFileChooser dirChooser;
	JMenuBar menuBar;
	JMenu actionMenu, toolsMenu, helpMenu;
	JMenuItem actionModMaker, actionVisitMe, actionGetME3Exp, actionReload, actionExit;
	JMenuItem toolsBackupDLC;
	JMenuItem toolsModMaker, toolsRevertDLCCoalesced, toolsRevertBasegame, toolsRevertAllDLC, toolsRevertSPDLC, toolsRevertMPDLC, toolsRevertCoal, toolsAutoTOC, toolsWavelistParser, toolsDifficultyParser;
	JMenuItem helpPost, helpAbout;
	JList<String> listMods;
	JProgressBar progressBar;
	ListSelectionModel listSelectionModel;
	JSplitPane splitPane;
	JLabel labelStatus;
	String selectMod = "Select a mod on the left to view its description or to apply it.";
	static HashMap<String, Mod> listDescriptors;

	public ModManagerWindow(boolean isUpdate) {
		this.isUpdate = isUpdate;
		initializeWindow();
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
			JOptionPane.showMessageDialog(this, "Update successful: Updated to Mod Manager "+ModManager.VERSION+" (Build "+ModManager.BUILD_NUMBER+").", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
		}
		new UpdateCheckThread().execute();
		this.setVisible(true);

		
	}
	
	   class UpdateCheckThread extends SwingWorker<Void, Object> {
		   
		   
	       @Override
	       public Void doInBackground() {
	           return checkForUpdates();
	       }

	       @Override
	       protected void done() {
	           try {
	               //label.setText(get());
	           } catch (Exception ignore) {
	           }
	       }
	   }


	private Void checkForUpdates() {
		labelStatus.setText("Checking for update...");
		//Check for update
		try {
			//String update_check_link = "http://me3tweaks.com/modmanager/latest_version.json";
			String update_check_link = "https://webdev-c9-mgamerz.c9.io/modmanager/latest_version.json";
			String latest_version = "latest_version";
			File local_json = new File(latest_version);
			FileUtils.copyURLToFile(new URL(update_check_link), local_json);
			JSONParser parser = new JSONParser();
			JSONObject latest_object = (JSONObject) parser.parse(new FileReader(latest_version));
			local_json.delete();
			long latest_build = (long) latest_object.get("latest_build_number");
			boolean showUpdate = true;
			//make sure the user hasn't declined this one.
			Wini settingsini;
			try {
				settingsini = new Wini(new File(ModManager.settingsFilename));
				String showIfHigherThan  = settingsini.get("Settings", "nextupdatedialogbuild");
				long build_check = ModManager.BUILD_NUMBER;
				if (showIfHigherThan!= null && !showIfHigherThan.equals("")) {
					try {
						build_check = Integer.parseInt(showIfHigherThan);
						if (latest_build>build_check){
							//update is newer than one stored in ini, show the dialog.
							showUpdate = true;
						} else {
							//don't show it.
							showUpdate = false;
						}
					} catch (NumberFormatException e){
						ModManager.debugLogger.writeMessage("Number format exception reading the build number updateon in the ini. Showing the dialog.");
					}
				}
			} catch (InvalidFileFormatException e) {
				ModManager.debugLogger.writeMessage("Invalid INI! Did the user modify it by hand?");
				e.printStackTrace();
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("I/O Error reading settings file. It may not exist yet. It will be created when a setting stored to disk.");
			}
			
			
			if (showUpdate) {
				//An update is available!
				labelStatus.setVisible(true);
				labelStatus.setText("Update available");
				new UpdateAvailableWindow(latest_object, this);
			} else {
				labelStatus.setVisible(true);
				labelStatus.setText("No updates available");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		titlePanel.add(new JLabel("Mass Effect 3 - Coalesced Mod Manager " + ModManager.VERSION, SwingConstants.LEFT), BorderLayout.WEST);

		// BioGameDir Panel
		JPanel cookedDirPanel = new JPanel(new BorderLayout());
		TitledBorder cookedDirTitle = BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mass Effect 3 BIOGame Directory");
		fieldBiogameDir = new JTextField();
		fieldBiogameDir.setText(getLocationText(fieldBiogameDir));
		buttonBioGameDir = new JButton("Browse...");

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
		JLabel availableModsLabel = new JLabel("  Available Mods:");
		listDescriptors = new HashMap<String, Mod>();
		listMods = new JList<String>(ModManager.getModsFromDirectory());
		listMods.addListSelectionListener(this);
		listMods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMods.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(listMods, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		modsListPanel.add(availableModsLabel, BorderLayout.NORTH);
		modsListPanel.add(listScroller, BorderLayout.CENTER);

		// DescriptionField
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		JLabel descriptionLabel = new JLabel("Mod Description:");
		fieldDescription = new JTextArea(selectMod);
		scrollDescription = new JScrollPane(fieldDescription);

		fieldDescription.setLineWrap(true);
		fieldDescription.setWrapStyleWord(true);

		fieldDescription.setEditable(false);
		scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(scrollDescription, BorderLayout.CENTER);
		fieldDescription.setCaretPosition(0);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modsListPanel, descriptionPanel);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(175);

		// SouthPanel
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel applyPanel = new JPanel(new BorderLayout());

		// ApplyPanel
		labelStatus = new JLabel("Status");
		labelStatus.setVisible(false);

		// ProgressBar
		progressBar = new JProgressBar();
		progressBar.setVisible(false);

		// ButtonPanel
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonApplyMod = new JButton("Apply Mod");
		buttonApplyMod.addActionListener(this);
		buttonApplyMod.setEnabled(false);

		buttonStartGame = new JButton("Start Game");
		buttonStartGame.addActionListener(this);

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
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		this.add(contentPanel);
		return this;
	}

	private JMenuBar makeMenu() {
		menuBar = new JMenuBar();
		// Actions
		actionMenu = new JMenu("Actions");
		actionModMaker = new JMenuItem("Create a mod");
		actionVisitMe = new JMenuItem("Open ME3Tweaks.com");
		actionGetME3Exp = new JMenuItem("Get ME3 Explorer");
		actionReload = new JMenuItem("Reload Mods");
		actionExit = new JMenuItem("Exit");

		actionMenu.add(actionModMaker);
		actionMenu.add(actionVisitMe);
		actionMenu.add(actionGetME3Exp);
		actionMenu.addSeparator();
		actionMenu.add(actionReload);
		actionMenu.add(actionExit);

		actionModMaker.addActionListener(this);
		actionVisitMe.addActionListener(this);
		actionReload.addActionListener(this);
		actionGetME3Exp.addActionListener(this);
		actionExit.addActionListener(this);
		menuBar.add(actionMenu);

		// Tools
		toolsMenu = new JMenu("Tools");

		toolsModMaker = new JMenuItem("Enter modmaker code");
		toolsBackupDLC = new JMenuItem("Backup DLCs");
		toolsRevertDLCCoalesced = new JMenuItem("Revert DLC & Coalesced");
		toolsRevertBasegame = new JMenuItem("Restore basegame files");
		toolsRevertAllDLC = new JMenuItem("Restore all DLCs");
		toolsRevertSPDLC = new JMenuItem("Restore SP DLCs");
		toolsRevertMPDLC = new JMenuItem("Restore MP DLCs");
		toolsRevertCoal = new JMenuItem("Restore original Coalesced.bin");
		verifyBackupCoalesced();
		toolsRevertCoal = new JMenuItem("Restore original Coalesced.bin");
		toolsAutoTOC = new JMenuItem("Update TOC of current selected");
		toolsWavelistParser = new JMenuItem("Wavelist Parser");
		toolsDifficultyParser = new JMenuItem("Biodifficulty Parser");


		
		toolsModMaker.addActionListener(this);
		toolsBackupDLC.addActionListener(this);
		toolsRevertDLCCoalesced.addActionListener(this);
		toolsRevertBasegame.addActionListener(this);
		toolsRevertAllDLC.addActionListener(this);
		toolsRevertSPDLC.addActionListener(this);
		toolsRevertMPDLC.addActionListener(this);
		toolsRevertCoal.addActionListener(this);
		toolsAutoTOC.addActionListener(this);
		toolsWavelistParser.addActionListener(this);
		toolsDifficultyParser.addActionListener(this);
		
		toolsMenu.add(toolsModMaker);
		toolsMenu.addSeparator();
		toolsMenu.add(toolsBackupDLC);
		toolsMenu.addSeparator();
		toolsMenu.add(toolsRevertDLCCoalesced);
		toolsMenu.add(toolsRevertBasegame);
		toolsMenu.add(toolsRevertAllDLC);
		toolsMenu.add(toolsRevertSPDLC);
		toolsMenu.add(toolsRevertMPDLC);
		toolsMenu.add(toolsRevertCoal);
		toolsMenu.addSeparator();
		toolsMenu.add(toolsAutoTOC);
		if (ModManager.IS_DEBUG) {
			toolsMenu.add(toolsWavelistParser);
			toolsMenu.add(toolsDifficultyParser);
		}
		menuBar.add(toolsMenu);

		// Help
		helpMenu = new JMenu("Help");
		helpPost = new JMenuItem("View Instructions");
		helpAbout = new JMenuItem("About...");

		helpPost.addActionListener(this);
		helpAbout.addActionListener(this);

		helpMenu.add(helpPost);
		helpMenu.add(helpAbout);
		menuBar.add(helpMenu);

		return menuBar;
	}

	private void verifyBackupCoalesced() {
		File restoreTest = new File("Coalesced.original");
		if (!restoreTest.exists()) {
			toolsRevertCoal.setEnabled(false);
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
		} else
		if (e.getSource() == toolsModMaker) {
			new ModMakerWindow(this, fieldBiogameDir.getText());
		} else 

		if (e.getSource() == toolsBackupDLC) {
			backupDLC(fieldBiogameDir.getText());
		} else

		if (e.getSource() == toolsRevertCoal) {
			restoreCoalesced(fieldBiogameDir.getText());
		} else

		if (e.getSource() == toolsRevertAllDLC) {
			restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.ALL);
		} else
		
		if (e.getSource() == toolsRevertBasegame) {
			restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.BASEGAME);
		} else
		
		if (e.getSource() == toolsRevertSPDLC) {
			restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.SP);
		} else if (e.getSource() == toolsRevertMPDLC) {
			restoreDataFiles(fieldBiogameDir.getText(), RestoreMode.MP);
		} else

		if (e.getSource() == toolsRevertDLCCoalesced) {
			restoreEverything(fieldBiogameDir.getText());
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
		} else
		if (e.getSource() == actionReload) {
			// Reload this jframe
			new ModManagerWindow(false);
			dispose();
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
		}
		if (e.getSource() == helpAbout) {
			new AboutWindow(this);
		} else if (e.getSource() == buttonApplyMod) {
			applyMod();
		} else

		if (e.getSource() == buttonStartGame) {
			startGame(appendSlash(fieldBiogameDir.getText()));
		} else

		if (e.getSource() == actionGetME3Exp) {
			URI theURI;
			try {
				theURI = new URI("http://goo.gl/1zJXp");
				java.awt.Desktop.getDesktop().browse(theURI);
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		} else 
			
		if (e.getSource() == toolsAutoTOC) {
			autoTOC();
		} else 
			
		if (e.getSource() == toolsWavelistParser) {
			new WavelistGUI();
		} else
		
		if (e.getSource() == toolsDifficultyParser) {
			new DifficultyGUI();
		}
	}

	private void backupDLC(String bioGameDir) {
		// Check that biogame is valid
		if (validateBIOGameDir()) {
			new BackupWindow(this, bioGameDir);
		} else {
			// Biogame is invalid
			JOptionPane
					.showMessageDialog(null, "The BioGame directory is not valid.\nDLC cannot be not backed up.\nFix the BioGame directory before continuing.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			labelStatus.setText(" DLC backup failed");
			labelStatus.setVisible(true);
		}
		return;
	}

	/**
	 * Checks that the user has chosen a correct biogame directory.
	 */
	private void checkForValidBioGame() {
		File coalesced = new File(appendSlash(dirChooser.getSelectedFile().toString()) + "CookedPCConsole\\Coalesced.bin");
		if (coalesced.exists()) {
			String YesNo[] = { "Yes", "No" };
			int saveDir = JOptionPane
					.showOptionDialog(null, "BioGame directory set to: " + dirChooser.getSelectedFile().toString()
							+ "\nSave this path for next time?", "Save directory path?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
			if (saveDir == 0) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "biogame_dir", appendSlash(dirChooser.getSelectedFile().toString()));
					ModManager.debugLogger.writeMessage(appendSlash(dirChooser.getSelectedFile().toString()));
					ini.store();
				} catch (InvalidFileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
				labelStatus.setText(" Saved BioGame directory to me3cmm.ini");
				labelStatus.setVisible(true);
			}
			fieldBiogameDir.setText(dirChooser.getSelectedFile().toString());
		} else {
			JOptionPane
					.showMessageDialog(null, "Coalesced.bin not found in " + dirChooser.getSelectedFile().toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Checks if the string in the biogamedir is a valid biogame directory.
	 * 
	 * @return True if valid, false otherwise
	 */
	private boolean validateBIOGameDir() {
		File coalesced = new File(appendSlash(fieldBiogameDir.getText()) + "CookedPCConsole\\Coalesced.bin");
		if (coalesced.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private String getLocationText(JTextField locationSet) {
		Wini settingsini;
		String setDir = "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BioGame\\";
		String os = System.getProperty("os.name");
		try {
			settingsini = new Wini(new File(ModManager.settingsFilename));
			setDir = settingsini.get("Settings", "biogame_dir");
			if (setDir == null || setDir.equals("")) {
				//Try to detect it via the registry
				if (os.contains("Windows")){
			        String installDir = Advapi32Util.registryGetStringValue(
				            WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{534A31BD-20F4-46b0-85CE-09778379663C}", "InstallLocation");	
			        System.out.println(installDir);
				}

		 
				
				
				setDir = "C:\\Program Files (x86)\\Origin Games\\Mass Effect 3\\BioGame\\";
			}
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O Error reading settings file. It may not exist yet.");
			//Try to make one
			if (os.contains("Windows")){
		        String installDir = Advapi32Util.registryGetStringValue(
			            WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{534A31BD-20F4-46b0-85CE-09778379663C}", "InstallLocation");
		        File bgdir = new File(installDir+"\\BIOGame");
		        if (bgdir.exists()) {
		        	//its correct
		        	Wini ini;
					try {
						File settings = new File(ModManager.settingsFilename);
						if (!settings.exists())
							settings.createNewFile();
						ini = new Wini(settings);
						ini.put("Settings", "biogame_dir", bgdir.toString());
						if (ModManager.logging) {
							ModManager.debugLogger.writeMessage(bgdir.toString()+" was detected via the registry to be the biogame dir.");
						}
						ini.store();
						fieldBiogameDir.setText(bgdir.toString());
					} catch (InvalidFileFormatException ex) {
						e.printStackTrace();
					} catch (IOException ex) {
						System.err.println("Could not save automatically detected biogame dir.");
					}
		        }
		        System.out.println(installDir);
			}
			
			
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
		File coalesced = new File(appendSlash(fieldBiogameDir.getText()) + "CookedPCConsole\\" + "Coalesced.bin");
		if (ModManager.logging) {
			ModManager.debugLogger.writeMessage("Validating BioGame dir: " + coalesced);
		}
		if (!coalesced.exists()) {
			JOptionPane.showMessageDialog(null, "The BioGame directory is not valid.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);

			labelStatus.setText(" Mod not installed");
			labelStatus.setVisible(true);
			return false;
		}

		Mod mod = listDescriptors.get(listMods.getSelectedValue().toString());
		// Read mod
		// Apply The Mod
		// Update Coalesced (main game)
		if (mod.modsCoal()) {
			if (ModManager.checkDoOriginal(fieldBiogameDir.getText())) {
				// check source file
				File source = new File(appendSlash(mod.getModPath()) + "Coalesced.bin");

				if (!source.exists()) {
					labelStatus.setText(" Mod not installed");
					labelStatus.setVisible(true);
					JOptionPane
							.showMessageDialog(null, "Coalesced.bin was not found in the Mod file's directory.\nIt might have moved, been deleted, or renamed.\nPlease check this mod's directory.", "Coalesced not found", JOptionPane.ERROR_MESSAGE);
					
					return false;
				}

				String destFile = coalesced.toString();
				String sourceFile = appendSlash(listDescriptors.get(listMods.getSelectedValue().toString()).getModPath()) + "Coalesced.bin";
				try {
					Files.copy(Paths.get(sourceFile), Paths.get(destFile), StandardCopyOption.REPLACE_EXISTING);
					if (mod.getJobs().length == 0) {
						labelStatus.setText(" "+mod.getModName()+" installed");
					} else {
						labelStatus.setText(" Injecting files into DLC modules...");
					}
				} catch (IOException e) {
					JOptionPane
							.showMessageDialog(null, "Copying Coalesced.bin failed. Stack trace:\n" + e.getMessage(), "Error copying Coalesced.bin", JOptionPane.ERROR_MESSAGE);
					labelStatus.setText(" Mod failed to install");
				}
			} else {

				labelStatus.setText(" Mod not installed");
				labelStatus.setVisible(true);
				return false;
			}
		}

		if (mod.getJobs().length > 0) {
			if (checkBackedUp()) {
				new PatchWindow(this, mod.getJobs(), fieldBiogameDir.getText(), mod);
			}
		} else {
			if (ModManager.logging) {
				ModManager.debugLogger.writeMessage("No dlc mod job, finishing mod installation");
			}
		}
		return true;
	}

	private boolean checkBackedUp() {
		Wini ini;
		boolean answer = true;
		try {
			File settings = new File(ModManager.settingsFilename);
			if (!settings.exists())
				settings.createNewFile();
			ini = new Wini(settings);
			String backupFlag = ini.get("Settings", "dlc_backup_flag");
			if (backupFlag == null || !backupFlag.equals("1")) {
				// backup flag not set, lets ask user
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Did not read the backup flag from settings or flag was not set to 1");
				}
				String YesNo[] = { "Yes", "No" }; // Yes/no buttons
				int showDLCBackup = JOptionPane
						.showOptionDialog(null, "This instance of Mod Manager hasn't backed up your DLC's yet.\nIf you have previously backed up using Mod Manager, you can ignore this message.\nYou really should back up your DLC so restoring them is faster than using Origin's repair game service.\n\nNote: This dialog will only be displayed once.\n\nOpen the backup manager window?", "Backup DLC before mod installation?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, YesNo, YesNo[0]);
				// System.out.println("User chose: "+showDLCBackup);
				if (showDLCBackup == 0) {
					backupDLC(fieldBiogameDir.getText());
				}
			}
			// shown only once. Backup complete, set to settings file
			//ini.put("Settings", "dlc_backup_flag", "1");
			ini.store();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
		}
		return answer;
	}

	/**
	 * Appends a slash onto the end of a string if not already there.
	 * 
	 * @param string
	 *            Original string
	 * @return Original string with a slash on the end if it was not there previously.
	 */
	protected static String appendSlash(String string) {
		if (string.charAt(string.length() - 1) == '\\') {
			return string;
		} else {
			return string + "\\";
		}
	}

	/**
	 * Gets a mod's description and formats it to be displayed.
	 * 
	 * @param modName
	 *            Name of the mod in the list on the left.
	 * @return String that is placed in the right side in the mod description box.
	 */
	private String getModDescription(String modName) {
		if (modName.equals("No Mods Available")) {
			buttonApplyMod.setEnabled(false);
			return "No mods are available to apply.";
		}
		Mod selectedMod = lookupModByFileName(modName);

		return selectedMod.getModDescription();
	}

	/**
	 * Handles looking up the name of a mod from the mod object that it comes from. Uses a hash map.
	 * 
	 * @param modName
	 *            Name of the mod from the list (display name)
	 * @return File that describes the selected mod
	 */
	private Mod lookupModByFileName(String modName) {
		if (listDescriptors.containsKey(modName) == false) {
			ModManager.debugLogger.writeMessage(modName + " doesn't exist in the mod hashmap.");
			return null;
		}
		ModManager.debugLogger.writeMessage("Hashmap contains location of mod " + modName + ": " + listDescriptors.containsKey(modName));
		
		return listDescriptors.get(modName);
	}

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		// TODO Auto-generated method stub
		//if(labelStatus.getText().equals(""))
		//labelStatus.setVisible(false);
		if (listChange.getValueIsAdjusting() == false) {
			if (listMods.getSelectedIndex() == -1) {
				buttonApplyMod.setEnabled(false);
				fieldDescription.setText(selectMod);
			} else {
				// Update mod description
				fieldDescription.setText(getModDescription(listMods.getSelectedValue().toString()));
				fieldDescription.setCaretPosition(0);
				buttonApplyMod.setEnabled(checkIfNone(listMods.getSelectedValue().toString()));
			}
		}
	}

	/**
	 * Checks if the mod in the list is "No Mods Available" to see if the Apply Button should be disabled.
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
		if (restoreCoalesced(bioGameDir)) { // attempt to restore coalesced. if it fails, don't bother with the rest.
			return restoreDataFiles(bioGameDir, RestoreMode.ALL);
		} else {
			// something failed
			JOptionPane
					.showMessageDialog(null, "Your DLC has not been restored.", "DLC restoration error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	private boolean restoreCoalesced(String bioGameDir) {
		String patch3CoalescedHash = "540053c7f6eed78d92099cf37f239e8e"; // This is Patch 3 Coalesced's hash - the final
																			// one
		File cOriginal = new File("Coalesced.original");
		if (cOriginal.exists()) {
			// Take the MD5 first to verify it.
			try {
				if (patch3CoalescedHash.equals(MD5Checksum.getMD5Checksum(cOriginal.toString()))) {
					// file is indeed the original
					// Copy
					String destFile = appendSlash(bioGameDir) + "CookedPCConsole\\Coalesced.bin";
					if (new File(destFile).exists() == false) {
						JOptionPane
								.showMessageDialog(null, "Coalesced.bin to be restored was not found in the specified BIOGame\\CookedPCConsole directory.\nYou must fix the directory before you can restore Coalesced.", "Coalesced not found", JOptionPane.ERROR_MESSAGE);
						labelStatus.setText("Coalesced.bin not restored");
						labelStatus.setVisible(true);
						return false;
					}
					String sourceFile = "Coalesced.original";

					Files.copy(new File(appendSlash(System.getProperty("user.dir")) + sourceFile).toPath(), new File(destFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
					labelStatus.setText("Coalesced.bin restored");
					labelStatus.setVisible(true);
					return true;
				} else {
					labelStatus.setText("Coalesced.bin not restored.");
					labelStatus.setVisible(true);
					JOptionPane
							.showMessageDialog(null, "Your backed up original Coalesced.bin file does not match the known original from Mass Effect 3.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.", "Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (Exception e) {
				System.err.println("Unable to restore original Coalesced.");
				labelStatus.setText(" Coalesced.bin not restored");
				labelStatus.setVisible(true);
				e.printStackTrace();
				return false;
			}
		} else {
			labelStatus.setText(" Coalesced.bin not restored");
			labelStatus.setVisible(true);
			JOptionPane
					.showMessageDialog(null, "The backed up Coalesced.bin file (Coalesced.original) does not exist.\nYou'll need to manually restore the original (or what you call your original).\nIf you lost your original you can find a copy of Patch 3's Coalesced on http://me3tweaks.com/tools/modmanager/faq.\nYour current Coalesced has not been changed.\n\nThis error should have been caught but can be thrown due to file system changes \nwhile the program is open.", "Coalesced Backup Error", JOptionPane.ERROR_MESSAGE);

		}
		return false;
	}

	/**
	 * Restores all DLCs to their original state if it can, using hashes to validate authenticity.
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
			labelStatus.setText(" DLC Restoration Failed");
			labelStatus.setVisible(true);
			JOptionPane
			.showMessageDialog(null, "The BioGame directory is not valid. DLC cannot be restored.", "Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void startGame(String CookedDir) {
		File startingDir = new File(CookedDir);
		/*
		 * for (int i = 0; i<2; i++){ if (!startingDir.exists()){ JOptionPane.showMessageDialog(null,
		 * "Unable to find the following game executable:\n"+CookedDir+"\nMake sure your BIOGame directory is correct.",
		 * "Unable to Launch Game", JOptionPane.ERROR_MESSAGE); return; } }
		 */
		startingDir = new File(startingDir.getParent());
		File executable = new File(startingDir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
		if (!executable.exists()) {
			// Try the other name he uses
			executable = new File(startingDir.toString() + "\\Binaries\\Win32\\LauncherWV.exe");
			if (!executable.exists()) {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Warranty Voider's memory patcher launcher was not found, using the main one.");
				}
				executable = new File(startingDir.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
			}
		}
		if (ModManager.logging) {
			ModManager.debugLogger.writeMessage("Launching: " + executable.getAbsolutePath());
		}
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
		if (ModManager.logging) {
			ModManager.debugLogger.writeMessage("Path: " + executable.getAbsolutePath() + " - Exists? " + executable.exists());
		}

	}

	/**
	 * Creates a new Mod Maker Compiler dialog with the specified code. Called from the code entry dialog.
	 * @param code Code to use for downloading the mod.
	 */
	public void startModMaker(String code) {
		new ModMakerCompilerWindow(this, code);
	}
	
	private void autoTOC(){
		//update the PCConsoleTOC's of a specific mod.
		String selectedValue = listMods.getSelectedValue();
		if (selectedValue == null) {
			return; //shouldn't be able to toc an unselected mod eh?
		}
		System.out.println("SELECTED VALUE: "+selectedValue);
		Mod mod = listDescriptors.get(selectedValue);
		new AutoTocWindow(this, mod);
		
	}
}