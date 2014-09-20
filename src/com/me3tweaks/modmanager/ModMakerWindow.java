package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ModMakerWindow extends JDialog implements ActionListener{
	JLabel infoLabel;
	JButton downloadButton;
	JTextField codeField;
	ModManagerWindow callingWindow;

	public ModMakerWindow(JFrame callingWindow) {
		this.setTitle("ME3Tweaks Mod Maker");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(420, 228));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.callingWindow = (ModManagerWindow) callingWindow;
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel modMakerPanel = new JPanel();
		modMakerPanel.setLayout(new BoxLayout(modMakerPanel, BoxLayout.PAGE_AXIS));
		infoLabel = new JLabel("<html>Mod Maker allows you to create a mod using the ME3Tweaks Mod Maker utility. Enter your download code below to begin the mod compiler.</html>");
		modMakerPanel.add(infoLabel);
		
		JPanel codeDownloadPanel = new JPanel(new FlowLayout());
		
		
		codeField = new JTextField(6);
		codeDownloadPanel.add(codeField);
		downloadButton = new JButton("Download & Compile");
		codeDownloadPanel.add(downloadButton);
		modMakerPanel.add(codeDownloadPanel);
		
		downloadButton.addActionListener(this);
		this.getContentPane().add(modMakerPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == downloadButton) {
			dispose();
			callingWindow.startModMaker(codeField.getText().toString());
		}
		
	}
}
