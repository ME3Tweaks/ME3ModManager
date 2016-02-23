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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.JCheckBoxList;
import com.me3tweaks.modmanager.utilities.SevenZipCompressedModInspector;

public class ModImportArchiveWindow extends JFrame {

	private JCheckBoxList compressedModList;
	private DefaultListModel<JCheckBox> compressedModModel;
	HashMap<JCheckBox, CompressedMod> checkMap = new HashMap<JCheckBox, CompressedMod>(); //crazy, I know...
	private JButton importButton;
	private JProgressBar progressBar;
	private JPanel leftsidePanel;

	public ModImportArchiveWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Import compressed mods into Mod Manager");
		setIconImages(ModManager.ICONS);
		setPreferredSize(new Dimension(650, 400));

		JTextArea descriptionArea = new JTextArea();
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
		JButton browseButton = new JButton("Browse...");
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
					}
				}
			}
		});

		importButton = new JButton("Import Selected");
		importButton.setEnabled(false);
		importButton.setPreferredSize(new Dimension(100, 23));
		progressBar = new JProgressBar();

		JScrollPane importScroller = new JScrollPane(compressedModList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		importScroller.setEnabled(false);

		leftsidePanel.add(browsePanel, BorderLayout.NORTH);
		leftsidePanel.add(importScroller, BorderLayout.CENTER);
		leftsidePanel.add(importButton, BorderLayout.SOUTH);

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
					importButton.setEnabled(true);
					ArrayList<CompressedMod> compressedMods = SevenZipCompressedModInspector.getCompressedModsInArchive(chosenFile);
					for (CompressedMod cm : compressedMods) {
						JCheckBox importBox = new JCheckBox(cm.getModName());
						importBox.setSelected(true);
						checkMap.put(importBox, cm);
						compressedModModel.addElement(importBox);
					}
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
					ImportWorker worker = new ImportWorker(archivePathField.getText(), modsToImport);
					importButton.setText("Importing");
					importButton.setEnabled(false);
					leftsidePanel.remove(importButton);
					leftsidePanel.add(progressBar, BorderLayout.SOUTH);
					pack();
					worker.execute();
				}
			}
		});
	}

	class ScanWorker extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class ImportWorker extends SwingWorker<Void, ThreadCommand> {

		private ArrayList<CompressedMod> modsToImport;
		private String archiveFilePath;

		public ImportWorker(String archiveFilePath, ArrayList<CompressedMod> modsToImport) {
			// TODO Auto-generated constructor stub
			this.archiveFilePath = archiveFilePath;
			this.modsToImport = modsToImport;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			SevenZipCompressedModInspector.extractCompressedModsFromArchive(archiveFilePath, modsToImport, this);
			return null;
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
				get();
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
				JOptionPane.showMessageDialog(ModImportArchiveWindow.this, "Error occured during mod import.\nSome mods may have successfully imported.\nReload Mod Manager from the actions menu\nto see new mods that may have imported.", "Import Unsuccessful", JOptionPane.ERROR_MESSAGE);
			}
			
		}

	}
}
