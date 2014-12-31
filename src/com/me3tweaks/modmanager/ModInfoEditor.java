package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;

public class ModInfoEditor extends JDialog implements ActionListener {
	private Mod mod;
	private ModManagerWindow callingWindow;
	private JTextField modNameField;
	private JTextArea modDescriptionField;
	private JButton saveButton;
	
	public ModInfoEditor(ModManagerWindow modManagerWindow, Mod mod) {
		this.callingWindow = modManagerWindow;
		this.mod = mod;
		setupWindow();
		this.setVisible(true);
	}

	private void setupWindow() {
		// TODO Auto-generated method stub
		this.setTitle(mod.getModName());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 365));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5,5,5,5));
		JPanel namePanel = new JPanel(new BorderLayout());
		TitledBorder nameBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Mod Name");
		namePanel.setBorder(nameBorder);
		modNameField = new JTextField(mod.getModName());
		namePanel.add(modNameField, BorderLayout.NORTH);
		
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Mod Description");
		descriptionPanel.setBorder(descriptionBorder);
		modDescriptionField = new JTextArea(Mod.breakFixer(mod.getModDescription()));
		JScrollPane scrollPane = new JScrollPane(modDescriptionField);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		
		modDescriptionField.setWrapStyleWord(true);
		modDescriptionField.setLineWrap(true);
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		
		contentPanel.add(namePanel, BorderLayout.NORTH);
		contentPanel.add(descriptionPanel, BorderLayout.CENTER);
		contentPanel.add(saveButton, BorderLayout.SOUTH);
		add(contentPanel);
		
		this.pack();
		this.setLocationRelativeTo(callingWindow);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			ModManager.debugLogger.writeMessage("Saving new mod description and name...");
			mod.setModName(modNameField.getText());
			mod.setModDescription(Mod.convertNewlineToBr(modDescriptionField.getText()));
			File file = new File(ModManager.appendSlash(mod.getModPath())+"moddesc.ini");
			try {
				FileUtils.writeStringToFile(file, mod.createModDescIni(mod.modCMMVer));
			} catch (IOException e1) {
				ModManager.debugLogger.writeException(e1);
			}
			callingWindow.dispose();
			dispose();
			new ModManagerWindow(false);
		}
	}
}
