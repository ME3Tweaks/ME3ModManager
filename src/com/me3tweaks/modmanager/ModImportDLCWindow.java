package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.MetaCMM;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;

public class ModImportDLCWindow extends JDialog implements ListSelectionListener {

	DefaultListModel<String> model = new DefaultListModel<>();
	JList<String> dlcModlist = new JList<String>(model);
	private String biogameDir;
	private JButton importButton;
	protected boolean reloadWhenClosed;

	public ModImportDLCWindow(JFrame callingWindow, String biogameDir) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening ModImportWindow (DLC Import)");
		this.biogameDir = biogameDir;
		setupWindow(callingWindow);
		setVisible(true);
	}

	private void setupWindow(JFrame callingWindow) {
		setTitle("Import installed mods into Mod Manager");
		setMinimumSize(new Dimension(300, 300));
		setIconImages(ModManager.ICONS);

		File mainDlcDir = new File(ModManager.appendSlash(biogameDir) + "DLC" + File.separator);
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		for (String dir : directories) {
			if (ModTypeConstants.isKnownDLCFolder(dir)) {
				continue;
			}
			//add to list
			File metacmm = new File(mainDlcDir + File.separator + dir + File.separator + "_metacmm.txt");
			if (metacmm.exists()) {
				//read it so we find our guid
				MetaCMM metafile = new MetaCMM(metacmm);
				if (metafile.getInstallationGUID().equals(ModManager.getGUID())) {
					continue; //installed by us
				}
			}
			
			
			File mountfile = new File(mainDlcDir + File.separator + dir + File.separator + "CookedPCConsole" + File.separator + "mount.dlc");
			if (dir.toUpperCase().startsWith("DLC_") && !metacmm.exists() && mountfile.exists()) {
				ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(dir);
				if (tpmi != null) {
					model.addElement(dir + " (" + tpmi.getModname() + ")");
				} else {
					model.addElement(dir);
				}
			}
		}

		dlcModlist.addListSelectionListener(this);
		dlcModlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel panel = new JPanel(new BorderLayout());

		JLabel infoHeader = new JLabel(
				"<html><center>Import already-installed DLC mods into Mod Manager to<br>install or uninstall them quickly and easily.<br>Only mods that have not been installed by Mod Manager are listed.</center></html>");
		panel.add(infoHeader, BorderLayout.NORTH);

		importButton = new JButton("Import");
		importButton.setEnabled(false);
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String dlcfoldername = model.get(dlcModlist.getSelectedIndex());
				if (dlcfoldername.contains(" ")) {
					dlcfoldername = dlcfoldername.substring(0, dlcfoldername.indexOf(' '));
				}
				String folder = mainDlcDir.getAbsolutePath() + File.separator + dlcfoldername;
				dispose();
				ImportEntryWindow iew = new ImportEntryWindow(ModImportDLCWindow.this, model.get(dlcModlist.getSelectedIndex()), folder);
				if (iew.getResult() == ImportEntryWindow.OK) {
					reloadWhenClosed = true;
				}
			}
		});
		JPanel cPanel = new JPanel(new BorderLayout());
		if (model.getSize() > 0) {
			JScrollPane scroll = new JScrollPane(dlcModlist, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			cPanel.add(scroll, BorderLayout.CENTER);
		} else {
			cPanel.add(new JLabel("<html><center>No active Non-Mod Manager<br>Custom DLC mods are installed</center></html>", SwingConstants.CENTER));
		}

		cPanel.setBorder(new TitledBorder(new EtchedBorder(), "Installed Custom DLC mods"));
		panel.add(cPanel, BorderLayout.CENTER);

		panel.add(importButton, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		setLocationRelativeTo(callingWindow);
	}

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (dlcModlist.getSelectedIndex() == -1) {
				importButton.setEnabled(false);
			} else {
				importButton.setEnabled(true);
			}
		}
	}
}
