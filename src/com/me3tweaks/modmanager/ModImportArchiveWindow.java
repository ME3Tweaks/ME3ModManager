package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.SevenZipCompressedModInspector;

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

	/**
	 * Standard, user triggered opening
	 */
	public ModImportArchiveWindow() {
		try {
			SevenZip.initSevenZipFromPlatformJAR();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error loading 7zip binding, it may be open in another instance of Mod Manager:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
					"Unable to load the 7-zip library.\nDo you have another instance of Mod Manager open?", "Cannot load 7zip library",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
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
		try {
			SevenZip.initSevenZipFromPlatformJAR();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error loading 7zip binding, it may be open in another instance of Mod Manager:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
					"Unable to load the 7-zip library.\nDo you have another instance of Mod Manager open?", "Cannot load 7zip library",
					JOptionPane.ERROR_MESSAGE);
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
		ScanWorker worker = new ScanWorker(archivePathField.getText());
		worker.execute();
	}

	private void setupWindow() {
		setTitle("Import compressed mods into Mod Manager");
		setIconImages(ModManager.ICONS);
		setPreferredSize(new Dimension(650, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
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
		modTable = new JTable(compressedModModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};
		modTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent lse) {
				if (!lse.getValueIsAdjusting()) {

					int selectedModIndex = modTable.getSelectedRow();
					if (selectedModIndex == -1) {
						descriptionArea.setText("Select a mod on the left to view its description.");
					} else {
						CompressedMod mod = compressedMods.get(selectedModIndex);
						descriptionArea.setText(mod.getModDescription());
					}

				}
			}
		});

		modTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		compressedModModel.addColumn("Import");
		compressedModModel.addColumn("Mod Name");
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
		JScrollPane descScroller = new JScrollPane(descriptionArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftsidePanel, descScroller);
		leftsidePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(splitPane);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		splitPane.setDividerLocation(0.5);

		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser archiveChooser = new JFileChooser();
				File tryDir = new File(archivePathField.getText());
				if (tryDir.exists() && tryDir.isFile()) {
					archiveChooser.setCurrentDirectory(tryDir.getParentFile());
				} else {
					archiveChooser.setCurrentDirectory(new File("."));
				}
				archiveChooser.setDialogTitle("Select mod archive to import");
				archiveChooser.setAcceptAllFileFilterUsed(false);
				archiveChooser.setFileFilter(new FileNameExtensionFilter("Compressed Archive Files (.7z/.rar/.zip)", "7z", "rar", "zip"));

				if (archiveChooser.showOpenDialog(ModImportArchiveWindow.this) == JFileChooser.APPROVE_OPTION) {

					loadArchive(archiveChooser.getSelectedFile().getAbsolutePath());
				}
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
	}

	public class ScanWorker extends SwingWorker<ArrayList<CompressedMod>, ThreadCommand> {

		private String archiveFile;

		public ScanWorker(String archiveFile) {
			this.archiveFile = archiveFile;
			checkMap.clear();
			compressedModModel.setRowCount(0);
			compressedMods.clear();
			progressBar.setIndeterminate(true);
			descriptionArea.setText("Scanning archive for Mod Manager mods...\nFor large mods this may take some time.");
			browseButton.setEnabled(false);
		}

		@Override
		protected ArrayList<CompressedMod> doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return SevenZipCompressedModInspector.getCompressedModsInArchive(archiveFile);
		}

		protected void process(List<ThreadCommand> commands) {
			for (ThreadCommand command : commands) {
				switch (command.getCommand()) {
				case "PROGRESS_UPDATE":
					progressBar.setValue(Integer.parseInt((String) command.getMessage()));
					break;
				}
			}
		}

		public void setProgressValue(int progress) {
			publish(new ThreadCommand("PROGRESS_UPDATE", Integer.toString(progress)));
		}

		protected void done() {
			checkMap.clear();
			compressedModModel.setRowCount(0);
			compressedMods = null;
			try {
				compressedMods = get();
			} catch (ExecutionException | InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("Unable to get compressed mod info from archive:", e);
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "An error occured while reading this archive file.",
						"Error reaching archive file", JOptionPane.ERROR_MESSAGE);
			}
			browseButton.setEnabled(true);

			for (CompressedMod cm : compressedMods) {
				JCheckBox importBox = new JCheckBox(cm.getModName());
				importBox.setSelected(true);
				checkMap.put(importBox, cm);
				compressedModModel.addRow(new Object[] { true, cm.getModName() });
			}
			if (compressedMods.size() > 0) {
				importButton.setEnabled(true); //will stay false if no mods loaded
				importButton.setText("Import Selected Mods");
			} else {
				importButton.setEnabled(true); //will stay false if no mods loaded
				importButton.setText("No mods in this archive");
			}
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
			importButton.setVisible(true);
			descriptionArea.setText("Select a mod in the list to view its description.");
		}

	}

	public class ImportWorker extends SwingWorker<Boolean, ThreadCommand> {

		private ArrayList<CompressedMod> modsToImport;
		private String archiveFilePath;

		public ImportWorker(String archiveFilePath, ArrayList<CompressedMod> modsToImport) {
			// TODO Auto-generated constructor stub
			this.archiveFilePath = archiveFilePath;
			this.modsToImport = modsToImport;
			descriptionArea.setText("Importing mods into Mod Manager...");
			browseButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return SevenZipCompressedModInspector.extractCompressedModsFromArchive(archiveFilePath, modsToImport, this);
		}

		protected void process(List<ThreadCommand> commands) {
			for (ThreadCommand command : commands) {
				switch (command.getCommand()) {
				case "PROGRESS_UPDATE":
					progressBar.setValue(Integer.parseInt((String) command.getMessage()));
					break;
				case "SIDELOAD_OR_NEW_PROMPT":
					Object[] choices = { "SIDELOAD as update", "Import as NEW", "Cancel importing" };
					String message = "You are importing "
							+ command.getMessage()
							+ ", which is already in imported into Mod Manager.\nPlease choose from one of the following options:\n\nSIDELOAD: Import mod as an update, overwriting local files with ones from this archive\nNEW: Delete local imported mod, and import mod from archive as a new mod\n\nSelect what you'd like to do.";

					synchronized (lock) {
						sideloadresult = JOptionPane.showOptionDialog(ModImportArchiveWindow.this, message, "Mod to import already exists",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[1]);
						lock.notifyAll(); //wake up thread
					}
					break;
				}
			}
		}

		public void setProgressValue(int progress) {
			publish(new ThreadCommand("PROGRESS_UPDATE", Integer.toString(progress)));
		}

		public void done() {
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
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "Mods have been imported.", "Import Successful",
						JOptionPane.INFORMATION_MESSAGE);
				dispose();
				new ModManagerWindow(false);
			} else {
				JOptionPane
						.showMessageDialog(
								ModImportArchiveWindow.this,
								"Error occured during mod import.\nSome mods may have successfully imported.\nReload Mod Manager from the actions menu\nto see new mods that may have imported.",
								"Import Unsuccessful", JOptionPane.ERROR_MESSAGE);
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
	}
}
