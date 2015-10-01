package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

@SuppressWarnings("serial")
public class OptionsWindow extends JDialog {
	JCheckBox loggingMode;
	private JCheckBox autoInjectKeybindsModMaker;
	private JCheckBox autoUpdateModManager;
	private JCheckBox autoUpdateMods;

	public OptionsWindow(JFrame callingWindow) {
		setupWindow();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("Mod Manager Options");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 365));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));

		loggingMode = new JCheckBox("Write debugging log to file");
		loggingMode.setToolTipText("<html>Turning this on will write a session log to me3cmm_last_run_log.txt next to ME3CMM.exe.<br>This log can be used by FemShep to help diagnose issues with Mod Manager.<br>It will also tell you why mods aren't loading and other things.</html>");
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
						JOptionPane.showMessageDialog(null, "A log file will be generated in the ME3CMM.exe directory with the filename 'me3cmm_last_run_log.txt'.\nUse this to debug Mod Manager.\nClose ME3CMM before opening your log file.\nYou must restart Mod Manager for logging to take effect.\nNote: Logs will continue to be made every time the program is run.", "Logging Mode", JOptionPane.INFORMATION_MESSAGE);
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
		
		autoInjectKeybindsModMaker = new JCheckBox("Auto-inject keybinds into ModMaker mods");
		autoInjectKeybindsModMaker.setToolTipText("<html>If you use a custom keybinds file (BioInput.xml) and place it in the data/override directory,<br>at the end of compiling ModMaker mods Mod Manager will auto-inject them for you.</html>");
		autoInjectKeybindsModMaker.setSelected(ModManager.AUTO_INJECT_KEYBINDS);
		autoInjectKeybindsModMaker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					if (autoInjectKeybindsModMaker.isSelected()) {
						ini.put("Settings", "autoinjectkeybinds", "1");
					} else {
						ini.put("Settings", "autoinjectkeybinds", "0");
					}
					ModManager.AUTO_INJECT_KEYBINDS = autoInjectKeybindsModMaker.isSelected();
					ini.store();
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");	
				}
			}
		});
		
		autoUpdateModManager = new JCheckBox("Check for updates at startup");
		autoUpdateModManager.setToolTipText("<html>Keep Mod Manager up to date by checking for updates at startup</html>");
		autoUpdateModManager.setSelected(ModManager.AUTO_UPDATE_MOD_MANAGER);
		autoUpdateModManager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					if (autoUpdateModManager.isSelected()) {
						ini.put("Settings", "checkforupdates", "1");
					} else {
						ini.put("Settings", "checkforupdates", "0");
					}
					ModManager.AUTO_UPDATE_MOD_MANAGER = autoUpdateModManager.isSelected();
					ini.store();
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");	
				}
			}
		});
		
		autoUpdateMods = new JCheckBox("Keep mods up to date from ME3Tweaks.com");
		autoUpdateMods.setToolTipText("<html>Checks every 3 days for updates to mods from ME3Tweaks.com</html>");
		autoUpdateMods.setSelected(ModManager.AUTO_UPDATE_MOD_MANAGER);
		autoUpdateMods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "autoupdatemods", autoUpdateMods.isSelected() ? "true" : "false");
					ini.put("Settings", "declinedautoupdate", autoUpdateMods.isSelected() ? "false" : "true");
					ini.store();
					ModManager.ASKED_FOR_AUTO_UPDATE = true;
					ModManager.AUTO_UPDATE_MODS = autoUpdateMods.isSelected();
				} catch (InvalidFileFormatException x) {
					x.printStackTrace();
				} catch (IOException x) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", x);
				}
			}
		});
		
		
		aboutPanel.add(loggingMode);
		aboutPanel.add(autoInjectKeybindsModMaker);
		aboutPanel.add(autoUpdateModManager);
		aboutPanel.add(autoUpdateMods);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(aboutPanel);
		this.pack();
	}
}
