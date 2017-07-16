package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class OptionsWindow extends JDialog {
	private JCheckBox autoInjectKeybindsModMaker;
	private JCheckBox enforceDotNetRequirement;
	private JCheckBox autoUpdateModManager;
	private JCheckBox autoUpdateMods;
	private JCheckBox autoApplyMixins;
	private JCheckBox skipUpdate;
	private JCheckBox useWindowsUI;
	private JCheckBox compressCompatibilityGeneratorOutput;
	private JCheckBox logModmaker;
	private AbstractButton logModInit;
	private JCheckBox logPatchInit;
	private JCheckBox autoFixControllerModMaker;

	public OptionsWindow(JFrame callingWindow) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening Options Window");
		setupWindow();
		setLocationRelativeTo(callingWindow);
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Mod Manager Options");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);
		setResizable(false);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

		enforceDotNetRequirement = new JCheckBox("Perform .NET requirements check");
		enforceDotNetRequirement.setToolTipText(
				"<html>.NET 4.5.2 or higher is required for Mod Manager to complete most tasks.<br>Due to a bug in one of the libraries Mod Manager uses, this isn't always detected.<br>Turn this off if you have .NET 4.5.2 or higher, but Mod Manager can't detect it.</html>");
		enforceDotNetRequirement.setSelected(ModManager.PERFORM_DOT_NET_CHECK);
		enforceDotNetRequirement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ModManager.PERFORM_DOT_NET_CHECK = enforceDotNetRequirement.isSelected();
					if (enforceDotNetRequirement.isSelected()) {
						ModManager.debugLogger.writeMessage("Enabling .NET framework check");
						ini.put("Settings", "enforcedotnetrequirement", "1");
						ModManager.validateNETFrameworkIsInstalled();
						ModManagerWindow.ACTIVE_WINDOW.updateApplyButton();
					} else {
						ModManager.debugLogger.writeMessage("Disabling .NET framework check");
						ini.put("Settings", "enforcedotnetrequirement", "0");
						ModManager.NET_FRAMEWORK_IS_INSTALLED = true;
						ModManagerWindow.ACTIVE_WINDOW.updateApplyButton();
					}
					ini.store();
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
			}
		});

		useWindowsUI = new JCheckBox("Use Windows style UI");
		useWindowsUI.setToolTipText("<html>Make Mod Manager use a Microsoft Windows style interface rather than the default cross-platform style</html>");
		useWindowsUI.setSelected(ModManager.USE_WINDOWS_UI);
		useWindowsUI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Wini ini = ModManager.LoadSettingsINI();
				ModManager.USE_WINDOWS_UI = useWindowsUI.isSelected();
				if (useWindowsUI.isSelected()) {
					ModManager.debugLogger.writeMessage("Enabling Windows UI");
					ini.put("Settings", "usewindowsui", "1");
				} else {
					ModManager.debugLogger.writeMessage("Disabling Windows UI");
					ini.put("Settings", "usewindowsui", "0");
				}
				try {
					ini.store();
					JOptionPane.showMessageDialog(OptionsWindow.this, "Restart Mod Manager for this setting to take effect.", "Restart required", JOptionPane.WARNING_MESSAGE);
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
			}
		});

		compressCompatibilityGeneratorOutput = new JCheckBox("Compress compatibility generator output files");
		compressCompatibilityGeneratorOutput.setToolTipText("<html>Compresses the output compatibilibility generator files. This may save some space, but is experimental.</html>");
		compressCompatibilityGeneratorOutput.setSelected(ModManager.COMPRESS_COMPAT_OUTPUT);
		compressCompatibilityGeneratorOutput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Wini ini = ModManager.LoadSettingsINI();
				ModManager.COMPRESS_COMPAT_OUTPUT = compressCompatibilityGeneratorOutput.isSelected();
				if (compressCompatibilityGeneratorOutput.isSelected()) {
					ModManager.debugLogger.writeMessage("Setting compat generator output to compressed");
					ini.put("Settings", "compresscompatibilitygeneratoroutput", "1");
					ModManager.COMPRESS_COMPAT_OUTPUT = true;
				} else {
					ModManager.debugLogger.writeMessage("Setting compat generator output to decompressed");
					ini.put("Settings", "compresscompatibilitygeneratoroutput", "0");
					ModManager.COMPRESS_COMPAT_OUTPUT = false;
				}
				try {
					ini.store();
				} catch (InvalidFileFormatException error) {
					error.printStackTrace();
				} catch (IOException error) {
					ModManager.debugLogger.writeMessage("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
				}
			}
		});
		if (!ResourceUtils.is64BitWindows()){
			compressCompatibilityGeneratorOutput.setEnabled(false);
			compressCompatibilityGeneratorOutput.setToolTipText("Compressing output requires 64-bit version of Windows");
		}

		autoInjectKeybindsModMaker = new JCheckBox("Auto-inject custom keybinds into ModMaker mods");
		autoInjectKeybindsModMaker.setToolTipText(
				"<html>If you use a custom keybinds file (BioInput.xml) and place it in the data/override directory,<br>at the end of compiling ModMaker mods Mod Manager will auto-inject them for you.</html>");
		autoInjectKeybindsModMaker.setSelected(ModManager.AUTO_INJECT_KEYBINDS);
		autoInjectKeybindsModMaker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
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

		autoFixControllerModMaker = new JCheckBox("Auto-inject controller mod fixes into ModMaker mods");
		autoFixControllerModMaker.setToolTipText(
				"<html>A few mixins will cause a file conflict with the controller mods if two mods are installed over each other.<br>Turn this option on if you are a controller mod user and it will apply the camera turning mixins to any ModMaker mod that modifies SFXGame.</html>");
		autoFixControllerModMaker.setSelected(ModManager.MODMAKER_CONTROLLER_MOD_ADDINS);
		autoFixControllerModMaker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					if (autoFixControllerModMaker.isSelected()) {
						ini.put("Settings", "controllermoduser", "1");
					} else {
						ini.put("Settings", "controllermoduser", "0");
					}
					ModManager.MODMAKER_CONTROLLER_MOD_ADDINS = autoFixControllerModMaker.isSelected();
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
					File settings = new File(ModManager.SETTINGS_FILENAME);
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

		autoUpdateMods = new JCheckBox("Keep Mod Manager content up to date from ME3Tweaks.com");
		autoUpdateMods.setToolTipText("<html>Checks every " + ModManager.AUTO_CHECK_INTERVAL_DAYS
				+ " days for updates to mods, online asi manifest, help contents, 3rd party mod info and more.<br>It is highly recommended you keep this on as several parts of Mod Manager make use of this feature.</html>");
		autoUpdateMods.setSelected(ModManager.AUTO_UPDATE_CONTENT);
		autoUpdateMods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "autoupdatemods", autoUpdateMods.isSelected() ? "true" : "false");
					ini.put("Settings", "declinedautoupdate", autoUpdateMods.isSelected() ? "false" : "true");
					ini.store();
					ModManager.AUTO_UPDATE_CONTENT = autoUpdateMods.isSelected();
				} catch (InvalidFileFormatException x) {
					x.printStackTrace();
				} catch (IOException x) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", x);
				}
			}
		});

		autoApplyMixins = new JCheckBox("<html>Automatically apply recommended MixIns to ModMaker mods</html>");
		autoApplyMixins.setToolTipText("<html>Automatically accepts recommended MixIns when compiling a ModMaker mod</html>");
		autoApplyMixins.setSelected(ModManager.AUTO_APPLY_MODMAKER_MIXINS);
		autoApplyMixins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "autoinstallmixins", autoApplyMixins.isSelected() ? "1" : "0");
					ini.store();
					ModManager.AUTO_APPLY_MODMAKER_MIXINS = autoApplyMixins.isSelected();
				} catch (InvalidFileFormatException x) {
					x.printStackTrace();
				} catch (IOException x) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", x);
				}
			}
		});

		if (ModManager.SKIP_UPDATES_UNTIL_BUILD > ModManager.BUILD_NUMBER) {
			skipUpdate = new JCheckBox("Only show update if it is build " + ModManager.SKIP_UPDATES_UNTIL_BUILD + " or higher");
			skipUpdate.setToolTipText("<html>Suppresses update prompts until a new version is releases past the current known one</html>");
			skipUpdate.setSelected(ModManager.SKIP_UPDATES_UNTIL_BUILD > ModManager.BUILD_NUMBER);
			skipUpdate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Wini ini;
					try {
						File settings = new File(ModManager.SETTINGS_FILENAME);
						if (!settings.exists())
							settings.createNewFile();
						ini = new Wini(settings);
						if (skipUpdate.isSelected()) {
							ModManager.debugLogger
									.writeMessage("OPTIONS: User is skipping (has already skipped to see this checkbox) the next update, build " + (ModManager.BUILD_NUMBER + 1));
							ini.put("Settings", "nextupdatedialogbuild", ModManager.BUILD_NUMBER + 1);
							ModManager.SKIP_UPDATES_UNTIL_BUILD = ModManager.BUILD_NUMBER + 1;
						} else {
							ModManager.debugLogger.writeMessage("OPTIONS: User is turning off the next skipped update");
							ini.remove("Settings", "nextupdatedialogbuild");
							ModManager.SKIP_UPDATES_UNTIL_BUILD = 0;
						}
						ini.store();
					} catch (InvalidFileFormatException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", ex);
					}
				}
			});
		}

		logModmaker = new JCheckBox("<html><div style=\"width: 300px\">Log ModMaker Coalesced Compiler</div></html>");
		logModmaker.setToolTipText(
				"<html>ModMaker generates a large set of logs while compiling the coalesced files for ModMaker mods, which can slow down the process and generate large log files.<br>Turn this on only if you need to debug ModMaker.</html>");
		logModmaker.setSelected(ModManager.LOG_MODMAKER);
		logModmaker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini = ModManager.LoadSettingsINI();
				ModManager.debugLogger.writeMessage("User changing run log modmaker to " + logModmaker.isSelected());
				ini.put("Settings", "logmodmaker", logModmaker.isSelected() ? "1" : "0");
				ModManager.LOG_MODMAKER = logModmaker.isSelected();
				try {
					ini.store();
				} catch (InvalidFileFormatException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", ex);
				}
			}
		});

		logModInit = new JCheckBox("<html><div style=\"width: 300px\">Log Mod startup</div></html>");
		logModInit.setToolTipText("<html>Writes mod debugging information generated while parsing mod info to the log file.<br>This is not needed unless debugging a mod.</html>");
		logModInit.setSelected(ModManager.LOG_MOD_INIT);
		logModInit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ModManager.debugLogger.writeMessage("User changing run log mod init to " + logModInit.isSelected());
					ini.put("Settings", "logmodinit", logModInit.isSelected() ? "1" : "0");
					ModManager.LOG_MOD_INIT = logModInit.isSelected();
					ini.store();
				} catch (InvalidFileFormatException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", ex);
				}
			}
		});

		logPatchInit = new JCheckBox("<html><div style=\"width: 300px\">Log MixIn startup</div></html>");
		logPatchInit.setToolTipText(
				"<html>Writes MixIn debugging information generated while parsing MixIn info to the log file.<br>This is not needed unless debugging a MixIn.</html>");
		logPatchInit.setSelected(ModManager.LOG_PATCH_INIT);
		logPatchInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ModManager.debugLogger.writeMessage("User changing run log mixin init to " + logPatchInit.isSelected());
					ini.put("Settings", "logpatchinit", logPatchInit.isSelected() ? "1" : "0");
					ModManager.LOG_PATCH_INIT = logPatchInit.isSelected();
					ini.store();
				} catch (InvalidFileFormatException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", ex);
				}
			}
		});

		optionsPanel.add(autoApplyMixins);
		optionsPanel.add(autoInjectKeybindsModMaker);
		optionsPanel.add(autoFixControllerModMaker);
		optionsPanel.add(compressCompatibilityGeneratorOutput);
		optionsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		optionsPanel.add(autoUpdateModManager);
		if (skipUpdate != null) {
			optionsPanel.add(skipUpdate);
		}
		optionsPanel.add(autoUpdateMods);
		optionsPanel.add(useWindowsUI);
		optionsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		optionsPanel.add(enforceDotNetRequirement);
		optionsPanel.add(logModmaker);
		optionsPanel.add(logModInit);
		optionsPanel.add(logPatchInit);

		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(optionsPanel);
		pack();
	}
}
