package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModInfoEditorWindow extends JDialog implements ActionListener {
	private Mod mod;
	private JTextField modNameField;
	private JTextField modSiteField;
	private JTextArea modDescriptionField;
	private JButton saveButton;
	private JCheckBox keepUpdateCode;
	private JComboBox<String> cmmTargeComboBox;

	public ModInfoEditorWindow(Mod mod) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening ModInfoEditorWindow.");
		this.mod = mod;
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Editing metadata for " + mod.getModName());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(380, 365));
		//setResizable(false);
		setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel namePanel = new JPanel(new BorderLayout());
		TitledBorder nameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Name (Required)");
		namePanel.setBorder(nameBorder);
		modNameField = new JTextField(mod.getModName());
		modNameField.setToolTipText("Display name for your mod in Mod Manager. This field is required for all mods.");
		namePanel.add(modNameField, BorderLayout.NORTH);

		JPanel sitePanel = new JPanel(new BorderLayout());
		TitledBorder siteBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Website (Optional)");
		sitePanel.setBorder(siteBorder);
		modSiteField = new JTextField(mod.getModSite());
		modSiteField.setToolTipText(
				"Website URL for your mod. This URL will appear at the bottom of the description panel and provides an easy way for the user to find your mod online for updates, support, etc.");
		sitePanel.add(modSiteField, BorderLayout.NORTH);

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description (Required)");
		descriptionPanel.setBorder(descriptionBorder);
		modDescriptionField = new JTextArea(ResourceUtils.convertBrToNewline(mod.getModDescription()));
		modDescriptionField.setToolTipText(
				"Description of the mod, which appears in the right pane of the main Mod Manager window. Be brief but include important information. This field is required for all mods.");

		JScrollPane scrollPane = new JScrollPane(modDescriptionField);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		modDescriptionField.setWrapStyleWord(true);
		modDescriptionField.setLineWrap(true);
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel savePanel = new JPanel(new BorderLayout());
		keepUpdateCode = new JCheckBox("Keep updater information for this mod");
		keepUpdateCode.setSelected(true);
		keepUpdateCode.setToolTipText("Unchecking this box will remove the ability for this mod to use the ME3Tweaks Update Service.");

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		if (mod.isME3TweaksUpdatable()) {
			savePanel.add(keepUpdateCode, BorderLayout.NORTH);
		}
		savePanel.add(saveButton, BorderLayout.SOUTH);

		JPanel metadataInfo = new JPanel();
		metadataInfo.setLayout(new BoxLayout(metadataInfo, BoxLayout.PAGE_AXIS));

		metadataInfo.add(namePanel);
		metadataInfo.add(sitePanel);

		contentPanel.add(metadataInfo, BorderLayout.NORTH);
		contentPanel.add(descriptionPanel, BorderLayout.CENTER);
		contentPanel.add(savePanel, BorderLayout.SOUTH);
		add(contentPanel);

		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			String validationString = validateFields();
			if (validationString == null) {
				String modname = modNameField.getText().trim();
				String moddesc = modDescriptionField.getText().trim();
				String modsite = modSiteField.getText().trim();
				try {
					Wini moddescini = new Wini(new File(mod.getDescFile()));
					moddescini.put("ModInfo", "modname", modname);
					moddescini.put("ModInfo", "moddesc", ResourceUtils.convertNewlineToBr(moddesc));
					if (modsite.length() > 0) {
						moddescini.put("ModInfo", "modname", modname);
					} else {
						moddescini.remove("ModInfo", "modsite");
					}
					if (!keepUpdateCode.isSelected()) {
						moddescini.remove("ModInfo", "updatecode");
						moddescini.remove("ModInfo", "me3tweaksid");
					}
					ModManager.debugLogger.writeMessage("Saving updated moddesc.ini file.");
					moddescini.store();
				} catch (InvalidFileFormatException e1) {
					ModManager.debugLogger.writeErrorWithException("Invalid moddesc.ini file format! Cannot save changes.", e1);
					JOptionPane.showMessageDialog(this, "An error occured saving your changes: "+e1.getMessage()+"\nCheck the Mod Manager log for more detailed info.", "Error saving updated info", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("I/O Exception saving moddesc.ini file! Cannot save changes.", e1);
					JOptionPane.showMessageDialog(this, "An error occured saving your changes: "+e1.getMessage()+"\nCheck the Mod Manager log for more detailed info.", "Error saving updated info", JOptionPane.ERROR_MESSAGE);
				}
				ModManager.debugLogger.writeMessage("Commited moddesc.ini updates to disk");
				dispose();
				ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
				ModManagerWindow.ACTIVE_WINDOW.highlightMod(mod);
			} else {
				JOptionPane.showMessageDialog(this, validationString, "Error validating mod info", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	/**
	 * Validates input for the mod info window
	 * 
	 * @return
	 */
	private String validateFields() {
		ModManager.debugLogger.writeMessage("Validating modinfoeditor fields");
		String modname = modNameField.getText().trim();
		String moddesc = modDescriptionField.getText().trim();
		String modsite = modSiteField.getText().trim();

		//Name - cannot be null or empty
		if (modname.length() <= 0) {
			return "Mod Name cannot be empty.";
		}

		//description
		if (moddesc.length() <= 0) {
			return "Mod Description cannot be empty.";
		}

		if (modsite.length() > 0) {
			try {
				URL url = new URL(modsite);
			} catch (MalformedURLException e) {
				return modsite + "\nis not a valid URL.";
			}
		}
		return null;
	}
}
