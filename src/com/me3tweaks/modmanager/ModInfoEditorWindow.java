package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModInfoEditorWindow extends JDialog implements ActionListener {
	private Mod mod;
	private JTextField modNameField;
	private JTextArea modDescriptionField;
	private JButton saveButton;
	private JCheckBox keepUpdateCode;

	public ModInfoEditorWindow(JFrame frame, Mod mod) {
		this.mod = mod;
		setupWindow();
		this.setLocationRelativeTo(frame);
		this.setVisible(true);
	}

	private void setupWindow() {
		// TODO Auto-generated method stub
		this.setTitle(mod.getModName());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 365));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel namePanel = new JPanel(new BorderLayout());
		TitledBorder nameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Name");
		namePanel.setBorder(nameBorder);
		modNameField = new JTextField(mod.getModName());
		namePanel.add(modNameField, BorderLayout.NORTH);

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description");
		descriptionPanel.setBorder(descriptionBorder);
		modDescriptionField = new JTextArea(ResourceUtils.convertBrToNewline(mod.getModDescription()));
		JScrollPane scrollPane = new JScrollPane(modDescriptionField);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		modDescriptionField.setWrapStyleWord(true);
		modDescriptionField.setLineWrap(true);
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel savePanel = new JPanel(new BorderLayout());
		keepUpdateCode = new JCheckBox("Keep updater information for this mod");
		keepUpdateCode.setSelected(true);
		keepUpdateCode.setToolTipText("Unchecking this box will remove the ability for this mod to use the ME3Tweaks updater service.");

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		if (mod.isME3TweaksUpdatable()) {
			savePanel.add(keepUpdateCode, BorderLayout.NORTH);
		}
		savePanel.add(saveButton, BorderLayout.SOUTH);

		contentPanel.add(namePanel, BorderLayout.NORTH);
		contentPanel.add(descriptionPanel, BorderLayout.CENTER);
		contentPanel.add(savePanel, BorderLayout.SOUTH);
		add(contentPanel);

		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			ModManager.debugLogger.writeMessage("Saving new mod description and name...");
			mod.setModName(modNameField.getText());
			mod.setModDescription(ResourceUtils.convertNewlineToBr(modDescriptionField.getText()));
			File file = new File(ModManager.appendSlash(mod.getModPath()) + "moddesc.ini");
			try {
				FileUtils.writeStringToFile(file, mod.createModDescIni(keepUpdateCode.isSelected(), mod.modCMMVer));
			} catch (IOException e1) {
				ModManager.debugLogger.writeException(e1);
			}
			dispose();
			ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
		}
	}
}
