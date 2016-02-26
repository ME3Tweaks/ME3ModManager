package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.JCheckBoxList;
import com.me3tweaks.modmanager.utilities.SevenZipCompressedModInspector;

public class ModImportArchiveWindow extends JDialog {

	private JCheckBoxList compressedModList;
	private DefaultListModel<JCheckBox> compressedModModel;
	HashMap<JCheckBox, CompressedMod> checkMap = new HashMap<JCheckBox, CompressedMod>(); //crazy, I know...
	private JButton importButton;
	private JProgressBar progressBar;
	private JPanel leftsidePanel;
	private JTextArea descriptionArea;
	private JButton browseButton;

	public ModImportArchiveWindow() {
		try {
			SevenZip.initSevenZipFromPlatformJAR();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error loading 7zip binding, it may be open in another instance of Mod Manager:", e);
			JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Unable to load the 7-zip library.\nDo you have another instance of Mod Manager open?", "Cannot load 7zip library", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
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
		JTextField archivePathField = new JTextField(55);
		archivePathField.setUI(new HintTextFieldUI("Select an archive (.7z, .zip, .rar)"));
		archivePathField.setEnabled(false);
		browseButton = new JButton("Browse...");
		browseButton.setPreferredSize(new Dimension(100, 19));
		browsePanel.add(archivePathField, BorderLayout.CENTER);
		browsePanel.add(browseButton, BorderLayout.EAST);

		compressedModList = new JCheckBoxList();
		compressedModModel = new DefaultListModel<>();
		compressedModList.setModel(compressedModModel);
		compressedModList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		compressedModList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				System.out.println("Item is changing");
				if (e.getValueIsAdjusting() == false) {
					System.out.println("Item not adjusting");
					if (compressedModList.getSelectedIndex() == -1) {
						descriptionArea.setText("Select a mod on the left to view its description.");
					} else {
						descriptionArea.setText(checkMap.get(compressedModModel.get(compressedModList.getSelectedIndex())).getModDescription());
						descriptionArea.setCaretPosition(0);
					}
				}
			}
		});

		JPanel actionPanel = new JPanel(new BorderLayout());
		
		importButton = new JButton("Import Selected");
		importButton.setEnabled(false);
		importButton.setPreferredSize(new Dimension(100, 23));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		actionPanel.add(importButton,BorderLayout.NORTH);
		actionPanel.add(progressBar,BorderLayout.SOUTH);
		
		JScrollPane importScroller = new JScrollPane(compressedModList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
				JFileChooser binChooser = new JFileChooser();
				File tryDir = new File(archivePathField.getText());
				if (tryDir.exists() && tryDir.isFile()) {
					binChooser.setCurrentDirectory(tryDir.getParentFile());
				} else {
					binChooser.setCurrentDirectory(new File("."));
				}
				binChooser.setDialogTitle("Select .bin to decompile");
				//binChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				//
				// disable the "All files" option.
				//
				binChooser.setAcceptAllFileFilterUsed(false);
				binChooser.setFileFilter(new FileNameExtensionFilter("Compressed Archive Files (.7z/.rar/.zip)", "7z", "rar", "zip"));

				if (binChooser.showOpenDialog(ModImportArchiveWindow.this) == JFileChooser.APPROVE_OPTION) {
					compressedModModel.clear();
					String chosenFile = binChooser.getSelectedFile().getAbsolutePath();
					archivePathField.setText(chosenFile);
					importButton.setEnabled(false);
					importButton.setVisible(false);
					progressBar.setVisible(true);
					ScanWorker worker = new ScanWorker(archivePathField.getText());
					worker.execute();
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
			compressedModModel.clear();
			progressBar.setIndeterminate(true);
			descriptionArea.setText("Scanning archive for Mod Manager mods...");
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
			compressedModModel.clear();
			ArrayList<CompressedMod> compressedMods = null;
			try {
				compressedMods = get();
			} catch (ExecutionException | InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("Unable to get compressed mod info from archive:", e);
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "An error occured while reading this archive file.", "Error reaching archive file",
						JOptionPane.ERROR_MESSAGE);
			}
			browseButton.setEnabled(true);
			
			for (CompressedMod cm : compressedMods) {
				JCheckBox importBox = new JCheckBox(cm.getModName());
				importBox.setSelected(true);
				checkMap.put(importBox, cm);
				compressedModModel.addElement(importBox);
			}
			if (compressedMods.size() > 0 ) {
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

			for (int i = 0; i < compressedModModel.size(); i++) {
				JCheckBox box = compressedModModel.getElementAt(i);
				box.setSelected(false);
			}

			importButton.setText("Import Finished");
			leftsidePanel.remove(progressBar);
			leftsidePanel.add(importButton, BorderLayout.SOUTH);
			pack();
			repaint();
			if (!error) {
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "Mods have been imported.", "Import Successful", JOptionPane.INFORMATION_MESSAGE);
				dispose();
				new ModManagerWindow(false);
			} else {
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this,
						"Error occured during mod import.\nSome mods may have successfully imported.\nReload Mod Manager from the actions menu\nto see new mods that may have imported.",
						"Import Unsuccessful", JOptionPane.ERROR_MESSAGE);
			}
			descriptionArea.setText("Select an archive file to scan contents for Mod Manager mods.");
			browseButton.setEnabled(true);

		}
	}
}
