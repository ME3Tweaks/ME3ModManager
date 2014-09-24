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
	JCheckBox loggingMode;

	public AboutWindow(JFrame callingWindow) {
		this.setTitle("About Coalesced Mod Manager");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 338));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel aboutPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel(
				"<html>Mass Effect 3 - Coalesced Mod Manager<br> Version "
						+ ModManager.VERSION
						+ "- "+ModManager.BUILD_DATE+"<br>Developed by \"FemShep\""
								+ "<br>"
								+ "Source code available at http://sourceforge.net/projects/me3cmm/"
								+ "<br>"
								+ "<br>Uses ini4j: http://ini4j.sourceforge.net"
								+ "<br>Uses json-simple: https://code.google.com/p/json-simple/"
								+ "<br>Uses Apache Commons-io: http://commons.apache.org/proper/commons-io/"
								+ "<br>Uses JNA: https://github.com/twall/jnaJNA"
								+ "<br>Packaged with Launch4j: http://launch4j.sourceforge.net/"
								+ "<br>"
								+ "<br>Mass Effect 3 is a registered trademark of Electronic Arts."
								+ "<br>"
								+ "<br>Fem Shep is not liable for any end-user actions done with this software.</html>");
		aboutPanel.add(infoLabel, BorderLayout.NORTH);

		loggingMode = new JCheckBox("Mod debugging mode");
		loggingMode.setSelected(ModManager.logging);
		loggingMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);

					if (loggingMode.isSelected()) {
						ini.put("Settings", "logging_mode", "1");
						JOptionPane.showMessageDialog(null, "A log file will be generated in the ME3CMM.exe directory with the filename 'me3cmm_last_run_log.txt'.\nUse this to debug your moddesc files.\nClose ME3CMM before opening your log file.\nYou must restart Mod Manager for logging to take effect.\nNote: Logs will continue to be made every time the program is run.", "Logging Mode", JOptionPane.INFORMATION_MESSAGE);
					} else {
						ini.put("Settings", "logging_mode", "0");
						ModManager.logging = false;
					}
					ini.store();
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");	
				}
			}
		});
		aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(aboutPanel);
	}
}
