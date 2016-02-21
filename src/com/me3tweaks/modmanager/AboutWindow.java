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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

@SuppressWarnings("serial")
public class AboutWindow extends JDialog {
	JLabel infoLabel;

	public AboutWindow(JFrame callingWindow) {
		setupWindow();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("About Mod Manager");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		
		
		JPanel aboutPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel(
				"<html>Mass Effect 3 Coalesced Mod Manager<br> Version "
						+ ModManager.VERSION + " Build "+ModManager.BUILD_NUMBER
						+ "- "+ModManager.BUILD_DATE+"<br>Developed by FemShep"
								+ "<br>"
								+ "Source code available at http://github.com/mgamerz/me3modmanager"
								+ "<br>"
								+ "<br>Uses ini4j: http://ini4j.sourceforge.net"
								+ "<br>Uses json-simple: https://code.google.com/p/json-simple/"
								+ "<br>Uses Apache Commons-io: http://commons.apache.org/proper/commons-io/"
								+ "<br>Uses Apache Commons-Lang: https://commons.apache.org/lang"
								+ "<br>Uses Apache Commons-Validator: https://commons.apache.org/proper/commons-validator/"
								+ "<br>Uses Apache Derby: https://db.apache.org/derby/"
								+ "<br>Uses Apache HttpComponents: https://hc.apache.org/"
								+ "<br>Uses JNA: https://github.com/java-native-access/jna"
								+ "<br>Uses ME3Explorer: http://github.com/me3explorer/me3explorer"
								+ "<br>Uses LauncherWV: http://github.com/me3explorer/launcherwv"
								+ "<br>ModMaker uses TLK and Coalesce tools from TankMaster"
								+ "<br>ModMaker includes the binkw32.dll bypass by WarrantyVoider"
								+ "<br>Packaged with Launch4j: http://launch4j.sourceforge.net/"
								+ "<br>"
								+ "<br>Mass Effect 3 is a registered trademark of Electronic Arts."
								+ "<br>"
								+ "<br>FemShep is not liable for any end-user actions done with this software."
								+ "<br>Thanks for using my software.</html>");
		aboutPanel.add(infoLabel, BorderLayout.NORTH);

		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(aboutPanel);
		this.pack();
	}
}
