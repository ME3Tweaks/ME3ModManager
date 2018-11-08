package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.SevenZipCompressedModInspector;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.sf.sevenzipjbinding.SevenZip;

/**
 * Window that handles importing a mod into mod manager from zip 7z rar
 * 
 * @author mgamerz
 *
 */
public class ModImportArchiveWindow extends JDialog {
	private final Object lock = new Lock(); //threading wait() and notifyall();
	public final static int IMPORT_AS_NEW_OPTION = 1;
	public static final int IMPORT_AS_SIDELOAD_OPTION = 0;
	private int sideloadresult;
	HashMap<JCheckBox, CompressedMod> checkMap = new HashMap<JCheckBox, CompressedMod>(); //crazy, I know...
	private JButton importButton;
	private JProgressBar progressBar;
	private JPanel leftsidePanel;
	private JTextArea descriptionArea;
	private JButton browseButton;
	private JTextField archivePathField;
	private JTable modTable;
	private DefaultTableModel compressedModModel;
	private ArrayList<CompressedMod> compressedMods = new ArrayList<CompressedMod>();
	private JScrollPane descScroller;

	/**
	 * Standard, user triggered opening
	 */
	public ModImportArchiveWindow() {
		super(null, Dialog.ModalityType.MODELESS);
		ModManager.debugLogger.writeMessage("Opening Mod Import Window - Archive (manual mode)");
		try {
			ModManager.debugLogger.writeMessage("Loading 7-zip library");
			SevenZip.initSevenZipFromPlatformJAR();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error loading 7zip binding, it may be open in another instance of Mod Manager:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Unable to load the 7-zip library.\nDo you have another instance of Mod Manager open?",
					"Cannot load 7zip library", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setupWindow();
		setVisible(true);
	}

	/**
	 * Autoscan, user dropped file on mod manager ui
	 * 
	 * @param modManagerWindow
	 *            calling window
	 * @param file
	 *            file dropped
	 */
	public ModImportArchiveWindow(ModManagerWindow modManagerWindow, String file) {
		super(null, Dialog.ModalityType.MODELESS);
		ModManager.debugLogger.writeMessage("Opening Mod Import Window - Archive (filedrop mode)");
		ModManager.debugLogger.writeMessage("Automating load of archive file: " + file);

		try {
			ModManager.debugLogger.writeMessage("Loading 7-zip library");
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error loading 7zip binding, it may be open in another instance of Mod Manager:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Unable to load the 7-zip library.\nDo you have another instance of Mod Manager open?",
					"Cannot load 7zip library", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		loadArchive(file);
		setVisible(true);
	}

	private void loadArchive(String file) {
		compressedModModel.setRowCount(0); //clears
		compressedMods.clear();
		archivePathField.setText(file);
		importButton.setEnabled(false);
		importButton.setVisible(false);
		progressBar.setVisible(true);
		ModManager.debugLogger.writeMessage("Starting archive loading thread.");
		ScanWorker worker = new ScanWorker(archivePathField.getText());
		worker.execute();
	}

	private void setupWindow() {
		setTitle("Import compressed mods into Mod Manager");
		setIconImages(ModManager.ICONS);
		setPreferredSize(new Dimension(650, 400));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setEditable(false);
		descriptionArea.setText("Select an archive file to scan contents for Mod Manager mods.");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		//Browse Panel
		leftsidePanel = new JPanel(new BorderLayout());
		leftsidePanel.setBorder(new TitledBorder(new EtchedBorder(), "Select archive to import"));

		JPanel browsePanel = new JPanel(new BorderLayout());
		archivePathField = new JTextField(55);
		archivePathField.setUI(new HintTextFieldUI("Select an archive (.7z, .zip, .rar)"));
		archivePathField.setEnabled(false);
		browseButton = new JButton("Browse...");
		browseButton.setPreferredSize(new Dimension(100, 19));
		browsePanel.add(archivePathField, BorderLayout.CENTER);
		browsePanel.add(browseButton, BorderLayout.EAST);

		compressedModModel = new DefaultTableModel() {
			public Class<?> getColumnClass(int colIndex) {
				switch (colIndex) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0) {
					return true;
				} else {
					return false;
				}
			}
		};

		modTable = new JTable(compressedModModel);
		modTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent lse) {
				if (!lse.getValueIsAdjusting()) {

					int selectedModIndex = modTable.getSelectedRow();
					if (selectedModIndex == -1) {
						descriptionArea.setText("Select a mod on the left to view its description.");
					} else {
						CompressedMod mod = compressedMods.get(selectedModIndex);
						String modDescToDisplay = mod.getModDescription();
						if (!mod.isOfficiallySupported()) {
							modDescToDisplay = "This mod archive was not built for use with Mod Manager in mind - importing it this way is experimental. If you encounter issues with this mod, it may be an issue due to importing and not from the mod developer.\n\n"
									+ modDescToDisplay;
						}
						descriptionArea.setText(modDescToDisplay);
						descriptionArea.setCaretPosition(0);
					}

				}
			}
		});

		compressedModModel.addColumn("Import");
		compressedModModel.addColumn("Mod Name");
		modTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		modTable.getColumnModel().getColumn(0).setMaxWidth(60);
		modTable.getColumnModel().getColumn(1).setMinWidth(130);
		//modTable.getColumnModel().getColumn(1).setPreferredWidth(Integer.MAX_VALUE);

		JPanel actionPanel = new JPanel(new BorderLayout());

		importButton = new JButton("Import Selected");
		importButton.setEnabled(false);
		importButton.setPreferredSize(new Dimension(100, 23));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		actionPanel.add(importButton, BorderLayout.NORTH);
		actionPanel.add(progressBar, BorderLayout.SOUTH);

		JScrollPane importScroller = new JScrollPane(modTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		importScroller.setEnabled(false);

		leftsidePanel.add(browsePanel, BorderLayout.NORTH);
		leftsidePanel.add(importScroller, BorderLayout.CENTER);
		leftsidePanel.add(actionPanel, BorderLayout.SOUTH);

		//RIGHT SIDE
		descScroller = new JScrollPane(descriptionArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftsidePanel, descScroller);
		leftsidePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(splitPane);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);

		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Platform.runLater(() -> {
					FileChooser archiveChooser = new FileChooser();
					File tryDir = new File(archivePathField.getText());
					if (tryDir.exists() && tryDir.isFile()) {
						archiveChooser.setInitialDirectory(tryDir.getParentFile());
					} else {
						//Downloads
						File downloadsFolder = new File(System.getProperty("user.home") + "/Downloads/");
						if (downloadsFolder.exists()) {
							archiveChooser.setInitialDirectory(downloadsFolder);
						} else {
							archiveChooser.setInitialDirectory(new File("."));
						}
					}
					archiveChooser.setTitle("Select mod archive to import");
					archiveChooser.getExtensionFilters().addAll(new ExtensionFilter("Compressed Mods", "*.7z", "*.rar", "*.zip"));

					File selectedFile = archiveChooser.showOpenDialog(null);
					if (selectedFile != null) {

						loadArchive(selectedFile.getAbsolutePath());
					}
				});
			}
		});
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<CompressedMod> modsToImport = new ArrayList<>();
				for (Map.Entry<JCheckBox, CompressedMod> entry : checkMap.entrySet()) {
					JCheckBox key = entry.getKey();
					CompressedMod value = entry.getValue();

					if (key.isSelected()) {
						modsToImport.add(value);
					}
				}
				if (modsToImport.size() > 0) {
					importButton.setText("Importing");
					importButton.setEnabled(false);
					importButton.setVisible(false);
					progressBar.setVisible(true);
					ImportWorker worker = new ImportWorker(archivePathField.getText(), modsToImport);
					worker.execute();
					invalidate();
					validate();
					repaint();
				}
			}
		});
		pack();
		splitPane.setDividerLocation(0.5);
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	public class ScanWorker extends SwingWorker<ArrayList<CompressedMod>, ThreadCommand> {

		private String archiveFile;
		private int jobCode;
		private boolean is7zfile = false;

		public ScanWorker(String archiveFile) {
			jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("ArchiveScan","Scanning archive for mods");
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Scanning " + FilenameUtils.getName(archiveFile));
			this.archiveFile = archiveFile;
			is7zfile = archiveFile.toLowerCase().endsWith(".7z");
			checkMap.clear();
			compressedModModel.setRowCount(0);
			compressedMods.clear();
			progressBar.setIndeterminate(true);
			descriptionArea.setText("Scanning archive for Mod Manager mods...\nFor large mods this may take some time.");
			browseButton.setEnabled(false);
		}

		@Override
		protected ArrayList<CompressedMod> doInBackground() throws Exception {
			ModManager.debugLogger.writeMessage("[ScanWorker]: Reading archive...");
			// TODO Auto-generated method stub
			return SevenZipCompressedModInspector.getCompressedModsInArchive(archiveFile, this);
		}

		protected void process(List<ThreadCommand> commands) {

			for (ThreadCommand command : commands) {
				switch (command.getCommand()) {
				case "PROGRESS_UPDATE":
					setModalityType(ModalityType.DOCUMENT_MODAL);
					progressBar.setIndeterminate(false);
					progressBar.setValue(Integer.parseInt((String) command.getMessage()));
					break;
				case "FOUND_MODFILE":
					int data = (int) command.getData();
					descriptionArea.setText("Scanning archive for Mod Manager mods...\nFound " + data + " potential mod" + (data != 1 ? "s" : ""));
					if (is7zfile) {
						descriptionArea.setText(descriptionArea.getText()+ "\n\nThe progress bar may not move for a while for large mods or mods compressed as a 7z file.");
					}

					break;
				case "POST_SUBTEXT":
					String rside = descriptionArea.getText();
					rside += "\n\nReading mod data from archive, please wait...";
					descriptionArea.setText(rside);
					break;
				case "SET_SUBTEXT_SCANNING":
					String dside = "Scanning archive for Mod Manager mods...\n\n" + command.getMessage();
					descriptionArea.setText(dside);
					break;
				case "RELEASE_WINDOW":
					setModalityType(ModalityType.MODELESS);
					break;
				}
			}
		}

		public void setProgressValue(int progress) {
			publish(new ThreadCommand("PROGRESS_UPDATE", Integer.toString(progress)));
		}

		protected void done() {
			ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Archive scan finished");
			ModManager.debugLogger.writeMessage("[SCANWORKER] Background thread finished.");
			checkMap.clear();
			compressedModModel.setRowCount(0);
			compressedMods = null;
			try {
				compressedMods = get();
			} catch (ExecutionException | InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("Unable to get compressed mod info from archive:", e);
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "An error occured while reading this archive file.", "Error reaching archive file",
						JOptionPane.ERROR_MESSAGE);
			}
			ModManager.debugLogger.writeMessage("[SCANWORKER] Found " + compressedMods.size() + " in archive. Displaying to user.");

			browseButton.setEnabled(true);

			for (CompressedMod cm : compressedMods) {
				if (!cm.isOfficiallySupported()) {
					//Check for blacklist
                    ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(cm.getCustomDLCFolderName(), true);
					if (tpmi != null && tpmi.isBlacklistedForModImport()) {
						progressBar.setIndeterminate(false);
						progressBar.setVisible(false);
						importButton.setVisible(true);
						importButton.setEnabled(false);
						importButton.setText("Mod cannot be imported");
						descriptionArea.setText(tpmi.getModname()
								+ " cannot be imported into Mod Manager - the mod setup is too complex for the Third Party Importing Service to build installation information for.");
						return;

					}
				}

				JCheckBox importBox = new JCheckBox(cm.getModName());
				importBox.setSelected(true);
				checkMap.put(importBox, cm);
				compressedModModel.addRow(new Object[] { true, cm.getModName() });
			}
			if (compressedMods.size() > 0) {
				importButton.setEnabled(true); //will stay false if no mods loaded
				importButton.setText("Import Selected Mods");
				descriptionArea.setText("Select a mod in the list to view its description.");
			} else {
				importButton.setEnabled(false); //will stay false if no mods loaded
				descriptionArea.setText("The selected archive does not contain any Mod Manager mods.");
				importButton.setText("No mods in this archive");
			}
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
			importButton.setVisible(true);
		}

		public void publishUpdate(ThreadCommand threadCommand) {
			publish(threadCommand);
		}

	}

	public class ImportWorker extends SwingWorker<Boolean, ThreadCommand> {
		private ArrayList<CompressedMod> modsToImport;
		private String archiveFilePath;
		private int jobCode;
		private String CURRENT_MOD_NAME = "";

		public ImportWorker(String archiveFilePath, ArrayList<CompressedMod> modsToImport) {
			// TODO Auto-generated constructor stub
			this.archiveFilePath = archiveFilePath;
			this.modsToImport = modsToImport;
			jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("Importing Mods", "Importing mod(s)...");
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Importing " + (modsToImport.size() != 1 ? "mods" : modsToImport.get(0).getModName()));
			descriptionArea.setText("Importing mods into Mod Manager...");
			progressBar.setIndeterminate(true);
			browseButton.setEnabled(false);
			ModManager.debugLogger.writeMessage("[IMPORTWORKER] Starting ImportWorker. The following mods will be extracted: ");
			for (CompressedMod cm : modsToImport) {
				ModManager.debugLogger.writeMessage(" -- " + cm.modName);
			}
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			// TODO Auto-generated method stub
			ModManager.debugLogger.writeMessage("[IMPORTWORKER] Starting to extract compressed mods.");
			return SevenZipCompressedModInspector.extractCompressedModsFromArchive(archiveFilePath, modsToImport, this);
		}

		protected void process(List<ThreadCommand> commands) {
			for (ThreadCommand command : commands) {
				switch (command.getCommand()) {
				case "PROGRESS_UPDATE":
					progressBar.setIndeterminate(false);
					progressBar.setValue(Integer.parseInt((String) command.getMessage()));
					break;
				case "SIDELOAD_OR_NEW_PROMPT":
					Object[] choices = { "SIDELOAD as update", "Import as NEW", "Cancel importing" };
					String message = "You are importing " + command.getMessage()
							+ ", which is already imported into Mod Manager.\nPlease choose from one of the following options:\n\nSIDELOAD: Import mod as an update, overwriting local files with ones from this archive\nNEW: Delete local imported mod, and import mod from archive as a new mod\n\nIf you weren't instructed to select 'SIDELOAD as update' you should do 'Import as NEW'.\n\nSelect what you'd like to do.";

					synchronized (lock) {
						sideloadresult = JOptionPane.showOptionDialog(ModImportArchiveWindow.this, message, "Mod to import already exists", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, choices, choices[1]);
						lock.notifyAll(); //wake up thread
					}
					if (sideloadresult == IMPORT_AS_SIDELOAD_OPTION) {
						//when mod manager reloads, it will check for updates
						ModManager.CHECKED_FOR_UPDATE_THIS_SESSION = false;
					}
					break;
				case "EXTRACTING_MOD":
					CURRENT_MOD_NAME = command.getMessage();
					break;
				case "EXTRACTING_FILE":
					descriptionArea.setText("Importing mods into Mod Manager...\n\nExtracting " + CURRENT_MOD_NAME + "\n - " + command.getMessage());
					break;
				case "SET_MAINWINDOW_TEXT":
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(command.getMessage());
					break;
				}
			}
		}

		public void setProgressValue(int progress) {
			publish(new ThreadCommand("PROGRESS_UPDATE", Integer.toString(progress)));
		}

		public void done() {
			ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
			ModManager.debugLogger.writeMessage("[IMPORTWORKER] Finished background thread.");
			boolean error = false;
			try {
				error = !get(); //returns true for OK
			} catch (ExecutionException | InterruptedException e) {
				error = true;
				ModManager.debugLogger.writeErrorWithException("Error extracting mod archive:", e);
			}

			for (int i = 0; i < compressedModModel.getRowCount(); i++) {
				compressedModModel.setValueAt(false, i, 0);
			}

			importButton.setText("Import Finished");
			leftsidePanel.remove(progressBar);
			leftsidePanel.add(importButton, BorderLayout.SOUTH);
			pack();
			repaint();
			if (!error) {
				ModManager.debugLogger.writeMessage("[IMPORTWORKER] Import successful.");
				try {
					ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
					boolean checkForUpdates = false;
					for (CompressedMod m : modsToImport) {
						if (m.getModDescMod().isME3TweaksUpdatable()) {
							checkForUpdates = true;
							break;
						}
					}

					if (checkForUpdates) {
						ModManagerWindow.ACTIVE_WINDOW.checkAllModsForUpdates(false); //should force an update check on new mod.
					}
					if (modsToImport.size() == 1) {
						//Highlight it
						ModManagerWindow.ACTIVE_WINDOW.highlightMod(modsToImport.get(0).getModDescMod());
					}
					dispose();
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mods have been imported.", "Import Successful", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "Mod import succeeded, but one of the mods was unable to load.", "Import Failed",
							JOptionPane.ERROR_MESSAGE);
				}
				dispose();
				return;
			} else {
				ModManager.debugLogger.writeError("[IMPORTWORKER] Import was not fully successful");
				ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
				progressBar.setVisible(false);
				importButton.setVisible(true);
				importButton.setEnabled(true);
				importButton.setText("Import Selected");
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "Error occured during mod import.\nSome mods may have successfully imported.", "Import Unsuccessful",
						JOptionPane.ERROR_MESSAGE);
			}
			descriptionArea.setText("Select an archive file to scan contents for Mod Manager mods.");
			browseButton.setEnabled(true);

		}

		/**
		 * Opens a JOptionPane confirm dialog, asking if the mod being imported
		 * is an update (aka sideloading) or a new mod to replace the old one.
		 * 
		 * This method will cause the UI thread to prompt, and it will stall the
		 * calling thread until a response is done.
		 * 
		 * @param modName
		 * @return
		 */
		public int askIfSideloadOrNew(String modName) {
			sideloadresult = -1;
			publish(new ThreadCommand("SIDELOAD_OR_NEW_PROMPT", modName));
			synchronized (lock) {
				while (sideloadresult == -1) {
					try {
						ModManager.debugLogger.writeMessage("Waiting for user to select import type, new or sideload.");
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						ModManager.debugLogger.writeErrorWithException("Unable to wait for sideload prompt lock:", e);
					}
				}
			}
			return sideloadresult;
		}

		public void publishUpdate(ThreadCommand threadcommand) {
			publish(threadcommand);
		}
	}
}
