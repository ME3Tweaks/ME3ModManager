package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.VerticalLayout;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModDescEditorWindow extends JXFrame {

	private Mod mod;

	public ModDescEditorWindow(Mod mod) {
		this.mod = mod;
		setupWindow(mod);
		setVisible(true);
	}

	private void setupWindow(Mod mod) {
		this.setTitle(mod.getModName() + " - ModDesc Editor");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);

		JPanel optionsPanel = new JPanel(new VerticalLayout());

		//METADATA PANEL==================================================
		//Mod Name & ModDesc version
		JPanel metadataPanel = new JPanel();
		metadataPanel.setLayout(new VerticalLayout());

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));

		TitledBorder nameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Name & ModDesc version (Required)");
		namePanel.setBorder(nameBorder);
		JTextField modNameField = new JTextField(mod.getModName());
		modNameField.setToolTipText("Display name for your mod in Mod Manager. This field is required for all mods.");
		namePanel.add(modNameField);
		namePanel.add(Box.createRigidArea(new Dimension(5, 5)));
		namePanel.add(new JSeparator(JSeparator.VERTICAL));
		namePanel.add(Box.createRigidArea(new Dimension(5, 5)));

		String moddescversionTooltip = "<html>Version of moddesc.ini that this mod targets.<br>All versions are forwards compatible, but newer versions offer more features.<br>You should generally always use the latest version ("+ModManager.MODDESC_VERSION_SUPPORT+") unless you know what you are doing.</html>";
		JLabel cmmVer = new JLabel("ModDesc Version:");
		cmmVer.setToolTipText(moddescversionTooltip);
		namePanel.add(cmmVer);
		namePanel.add(Box.createRigidArea(new Dimension(5, 5)));

		JComboBox<String> cmmverCombobox = new JComboBox<String>();
		cmmverCombobox.addItem(Double.toString(mod.modCMMVer));
		if (mod.modCMMVer != ModManager.MODDESC_VERSION_SUPPORT) {
			cmmverCombobox.addItem(Double.toString(ModManager.MODDESC_VERSION_SUPPORT));
		}
		cmmverCombobox.setEditable(false);
		cmmverCombobox.setToolTipText(moddescversionTooltip);

		namePanel.add(cmmverCombobox);

		//Mod Site Panel
		JPanel sitePanel = new JPanel(new BorderLayout());
		TitledBorder siteBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Website/URL (Recommended)");
		sitePanel.setBorder(siteBorder);
		JTextField modSiteField = new JTextField(mod.getModSite());
		modSiteField.setToolTipText(
				"<html>Website URL for your mod.<br>This URL will appear at the bottom of the description panel and provides an easy way for the user to find your mod online for updates, support, etc.</html>");
		sitePanel.add(modSiteField, BorderLayout.NORTH);

		//Mod Description Panel
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description (Required)");
		descriptionPanel.setBorder(descriptionBorder);
		JTextArea modDescriptionField = new JTextArea(ResourceUtils.convertBrToNewline(mod.getModDescription()));
		modDescriptionField.setToolTipText(
				"<html>Description of the mod, which appears in the right pane of the main Mod Manager window.<br>Be brief but include important information.<br>Items at the bottom of the description pane are automatically generated.<br>This field is required for all mods.</html>");
		modDescriptionField.setRows(7);
		JScrollPane scrollPane = new JScrollPane(modDescriptionField);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		modDescriptionField.setWrapStyleWord(true);
		modDescriptionField.setLineWrap(true);
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);

		metadataPanel.add(namePanel);
		metadataPanel.add(sitePanel);
		metadataPanel.add(descriptionPanel);

		//CUSTOM DLC PANEL ======================================================================
		JPanel customDLCPanel = new JPanel();
		customDLCPanel.setLayout(new VerticalLayout());
		
		if (mod.rawcustomDLCDestDirs != null && mod.rawCustomDLCSourceDirs != null) {
			customDLCPanel.add(new JLabel(mod.rawcustomDLCDestDirs));

		} else {
			customDLCPanel.add(new JLabel("No Custom DLCs will install when this mod is installed."));
		}
		
		JButton addCustomDLC = new JButton("Add Custom DLC folder to install");
		customDLCPanel.add(addCustomDLC);
		
		//Sections Top Level =================================
		JXCollapsiblePane metadataPane = new JXCollapsiblePane();
		metadataPane.add(metadataPanel);

		JXCollapsiblePane baseDLCPane = new JXCollapsiblePane();
		//customDLCPane.add(customDLCPanel);

		JXCollapsiblePane customDLCPane = new JXCollapsiblePane();
		customDLCPane.add(customDLCPanel);

		JXCollapsiblePane condFilesPane = new JXCollapsiblePane();
		//customDLCPane.add(customDLCPanel);

		JXCollapsiblePane condDLCPane = new JXCollapsiblePane();
		//customDLCPane.add(customDLCPanel);

		JLabel metaPanelTitle = new JLabel("Mod Metadata");
		JLabel baseOfficialPanelTitle = new JLabel("Basegame + Official DLC Modifications");
		JLabel customDLCPanelTitle = new JLabel("Always-installed Custom DLC");
		JLabel conditionalFilesPanelTitle = new JLabel("Compatibility + Manual options files");
		JLabel conditionalCustomDLCPanelTitle = new JLabel("Compatibility + Manual options DLC");

		//Set fonts
		metaPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(16f));
		baseOfficialPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(16f));
		customDLCPanelTitle.setFont(customDLCPanelTitle.getFont().deriveFont(16f));
		conditionalFilesPanelTitle.setFont(conditionalFilesPanelTitle.getFont().deriveFont(16f));
		conditionalCustomDLCPanelTitle.setFont(conditionalCustomDLCPanelTitle.getFont().deriveFont(16f));

		JButton metaPanelButton = new JButton();
		JButton basePanelButton = new JButton();
		JButton customPanelButton = new JButton();
		JButton condFilePanelButton = new JButton();
		JButton condDLCPanelButton = new JButton();

		JButton[] expanders = { metaPanelButton, basePanelButton, customPanelButton, condFilePanelButton, condDLCPanelButton };
		JXCollapsiblePane[] expanderPanes = { metadataPane, baseDLCPane, customDLCPane, condFilesPane, condDLCPane };

		for (int i = 0; i < expanders.length; i++) {
			JButton button = expanders[i];
			JXCollapsiblePane pane = expanderPanes[i];
			Action toggleAction = pane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
			toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
			toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
			button.setAction(toggleAction);
			button.setText("");
			if (i > 0) {
				pane.setCollapsed(true);
			}
		}

		//Header Panels
		JPanel metaHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel baseHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel customHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel condFileHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel condDLCHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		metaHeaderPanel.add(metaPanelButton);
		metaHeaderPanel.add(metaPanelTitle);

		baseHeaderPanel.add(basePanelButton);
		baseHeaderPanel.add(baseOfficialPanelTitle);

		customHeaderPanel.add(customPanelButton);
		customHeaderPanel.add(customDLCPanelTitle);

		condFileHeaderPanel.add(condFilePanelButton);
		condFileHeaderPanel.add(conditionalFilesPanelTitle);

		condDLCHeaderPanel.add(condDLCPanelButton);
		condDLCHeaderPanel.add(conditionalCustomDLCPanelTitle);

		optionsPanel.add(metaHeaderPanel);
		optionsPanel.add(metadataPane);

		optionsPanel.add(baseHeaderPanel);
		optionsPanel.add(baseDLCPane);

		optionsPanel.add(customHeaderPanel);
		optionsPanel.add(customDLCPane);

		optionsPanel.add(condFileHeaderPanel);
		optionsPanel.add(condFilesPane);

		optionsPanel.add(condDLCHeaderPanel);
		optionsPanel.add(condDLCPane);


		JComponent cont = (JComponent) getContentPane();
		cont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.setPreferredSize(new Dimension(800, 600));
		this.getContentPane().add(optionsPanel);
		this.pack();
	}
}
