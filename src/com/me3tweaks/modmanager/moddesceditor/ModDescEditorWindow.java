package com.me3tweaks.modmanager.moddesceditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.ini4j.Wini;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.AlternateCustomDLC;
import com.me3tweaks.modmanager.objects.AlternateFile;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModDescEditorWindow extends JXFrame {

	private Mod mod;
	static int SUBPANEL_INSET_LEFT = 6;
	private ArrayList<MDECustomDLC> customDLCSelections = new ArrayList<>();
	private ArrayList<MDEConditionalFileItem> conditionalFileItems = new ArrayList<MDEConditionalFileItem>();
	private ArrayList<MDEConditionalDLCItem> conditionalDLCItems = new ArrayList<MDEConditionalDLCItem>();
	private JLabel noAltFilesLabel;
	private JLabel noAltDLCLabel;
	private JLabel noOutdatedCustomDLCLabel;
	private ArrayList<MDEOutdatedCustomDLC> outdatedCustomDLCItems = new ArrayList<MDEOutdatedCustomDLC>();
	private JButton saveButton;
	private JLabel statusLabel;
	private JTextField modNameField;
	private JTextField modDevField;
	private JTextField modVerField;
	private JComboBox<String> cmmverCombobox;
	private JTextField modSiteField;
	private JTextArea modDescriptionField;
	private JTextField updateFolderField;
	private JTextField updateCodeField;
	private JCheckBox useUpdaterCB;

	public ModDescEditorWindow(Mod mod) {
		ModManager.debugLogger.writeMessage("Opening ModDesc Editor for " + mod.getModName());
		//JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "This tool is under development and is not yet functional.", "Tool not yet functional",
		//		JOptionPane.WARNING_MESSAGE);
		ModManager.debugLogger.writeMessage("Reloading " + mod.getModName() + " without automatic alternates applied.");
		Mod noAutoParse = new Mod();
		noAutoParse.setShouldApplyAutos(false);
		noAutoParse.loadMod(mod.getDescFile());
		this.mod = noAutoParse;
		setupWindow(mod);
		setVisible(true);
	}

	public ArrayList<MDECustomDLC> getCustomDLCSelections() {
		return customDLCSelections;
	}

	public JLabel getNoOutdatedCustomDLCLabel() {
		return noOutdatedCustomDLCLabel;
	}

	public ArrayList<MDEOutdatedCustomDLC> getOutdatedCustomDLCItems() {
		return outdatedCustomDLCItems;
	}

	private void setupWindow(Mod mod) {
		this.setTitle(mod.getModName() + " - ModDesc Editor");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);

		JXPanel optionsPanel = new JXPanel(new VerticalLayout());
		optionsPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		JTabbedPane tabbedPane = new JTabbedPane();
		JLabel metaPanelTitle = new JLabel("Mod Metadata");
		JLabel baseOfficialPanelTitle = new JLabel("Basegame + Official DLC + Balance Changes Modifications");
		JLabel customDLCPanelTitle = new JLabel("Always-installed Custom DLC");
		JLabel conditionalFilesPanelTitle = new JLabel("Compatibility + Manual options files");
		JLabel conditionalCustomDLCPanelTitle = new JLabel("Compatibility + Manual options DLC");
		JLabel outdatedFoldersPanelTitle = new JLabel("Outdated mod DLC folders");

		// Set fonts
		metaPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(18f));
		baseOfficialPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(18f));
		customDLCPanelTitle.setFont(customDLCPanelTitle.getFont().deriveFont(18f));
		conditionalFilesPanelTitle.setFont(conditionalFilesPanelTitle.getFont().deriveFont(18f));
		conditionalCustomDLCPanelTitle.setFont(conditionalCustomDLCPanelTitle.getFont().deriveFont(18f));
		outdatedFoldersPanelTitle.setFont(conditionalCustomDLCPanelTitle.getFont().deriveFont(18f));

		// METADATA PANEL==================================================
		// Mod Name & ModDesc version
		JXPanel metadataPanel = new JXPanel();
		metadataPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		metadataPanel.setLayout(new VerticalLayout());
		JXPanel namePanel = new JXPanel();
		JXPanel nameFieldPanel = new JXPanel();
		nameFieldPanel.setLayout(new BoxLayout(nameFieldPanel, BoxLayout.X_AXIS));
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));

		TitledBorder nameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Name (Required)");
		nameFieldPanel.setBorder(nameBorder);
		modNameField = new JTextField(mod.getModName());
		modNameField.setToolTipText("Display name for your mod in Mod Manager.");
		nameFieldPanel.add(modNameField);

		namePanel.add(nameFieldPanel);
		// RIGHT SIDE NAME PANEL
		JXPanel versionPanel = new JXPanel();
		TitledBorder versionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Version Info (Required)");
		versionPanel.setBorder(versionBorder);
		versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.X_AXIS));

		JXPanel devPanel = new JXPanel();
		devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.X_AXIS));
		TitledBorder devBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Developer Name (Required)");
		devPanel.setBorder(devBorder);
		modDevField = new JTextField(mod.getAuthor());
		modDevField.setToolTipText("<html>Name of the mod developer. This should be your username/alias you go by in the modding scene.</html>");
		devPanel.add(modDevField, BorderLayout.NORTH);
		namePanel.add(devPanel);

		String modVerTooltip = "<html>Specifies the version of the mod. This must be a decimal number.<br>Typically you use integers to indicate large releases and fractional components to indicate minor updates.<br>e.g. 1.04 is major version 1 revision 4.</html>";
		JLabel modVer = new JLabel("Mod Version:");
		modVer.setToolTipText(modVerTooltip);
		versionPanel.add(modVer);
		versionPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		modVerField = new JTextField(Double.toString(mod.getVersion()), 8);
		modVerField.setColumns(8);
		modVerField.setToolTipText(modVerTooltip);
		modVerField.setMaximumSize(modVerField.getPreferredSize());
		versionPanel.add(modVerField);

		versionPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		versionPanel.add(new JSeparator(JSeparator.VERTICAL));
		versionPanel.add(Box.createRigidArea(new Dimension(5, 5)));

		String moddescversionTooltip = "<html>Version of moddesc.ini that this mod targets.<br>All versions are forwards compatible, but newer versions offer more features.<br>You should generally always use the latest version ("
				+ ModManager.MODDESC_VERSION_SUPPORT + ") unless you know what you are doing.</html>";
		JLabel cmmVer = new JLabel("ModDesc Version:");
		cmmVer.setToolTipText(moddescversionTooltip);
		versionPanel.add(cmmVer);
		versionPanel.add(Box.createRigidArea(new Dimension(5, 5)));

		cmmverCombobox = new JComboBox<String>();
		cmmverCombobox.addItem(Double.toString(mod.modCMMVer));
		if (mod.modCMMVer != ModManager.MODDESC_VERSION_SUPPORT) {
			cmmverCombobox.addItem(Double.toString(ModManager.MODDESC_VERSION_SUPPORT));
		}
		cmmverCombobox.setEditable(false);
		cmmverCombobox.setToolTipText(moddescversionTooltip);

		versionPanel.add(cmmverCombobox);

		namePanel.add(versionPanel);

		// Mod Site Panel row
		JXPanel secondRowPanel = new JXPanel();
		secondRowPanel.setLayout(new BoxLayout(secondRowPanel, BoxLayout.X_AXIS));

		JXPanel sitePanel = new JXPanel(new BorderLayout());
		TitledBorder siteBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Website/URL (Recommended)");
		sitePanel.setBorder(siteBorder);
		modSiteField = new JTextField(mod.getModSite());
		modSiteField.setToolTipText(
				"<html>Website URL for your mod.<br>This URL will appear at the bottom of the description panel and provides an easy way for the user to find your mod online for updates, support, etc.</html>");
		sitePanel.add(modSiteField, BorderLayout.NORTH);

		secondRowPanel.add(sitePanel);

		// Mod Description Panel
		JXPanel descriptionPanel = new JXPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description (Required)");
		descriptionPanel.setBorder(descriptionBorder);
		modDescriptionField = new JTextArea(ResourceUtils.convertBrToNewline(mod.getModDescription()));
		modDescriptionField.setToolTipText(
				"<html>Description of the mod, which appears in the right pane of the main Mod Manager window.<br>Be brief but include important information.<br>Items at the bottom of the description pane are automatically generated.<br>This field is required for all mods.</html>");
		modDescriptionField.setRows(7);
		JScrollPane scrollPane = new JScrollPane(modDescriptionField);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(modDescriptionField);
		modDescriptionField.setWrapStyleWord(true);
		modDescriptionField.setLineWrap(true);
		descriptionPanel.add(scrollPane, BorderLayout.CENTER);

		//Updater Panel
		JXPanel updaterPanel = new JXPanel();
		updaterPanel.setLayout(new BoxLayout(updaterPanel, BoxLayout.X_AXIS));
		TitledBorder updaterBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "ME3Tweaks Updater Service Information (Optional)");
		updaterPanel.setBorder(updaterBorder);

		useUpdaterCB = new JCheckBox("Use ME3Tweaks Updater Service");
		useUpdaterCB.setToolTipText(
				"<html>ME3Tweaks Updater Service allows your mod to automatically update in Mod Manager for end users.<br>Using this feature requires an update code which you can request from FemShep.</html>");
		JLabel updateCodeLabel = new JLabel("Update Code:");
		JLabel updateFolderLabel = new JLabel("Server Folder:");
		useUpdaterCB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCodeField.setEnabled(useUpdaterCB.isSelected());
				updateFolderField.setEnabled(useUpdaterCB.isSelected());
				updateCodeLabel.setEnabled(useUpdaterCB.isSelected());
				updateFolderLabel.setEnabled(useUpdaterCB.isSelected());
			}
		});
		updateCodeField = new JTextField();
		updateCodeField.setUI(new HintTextFieldUI("Code"));
		updateCodeField.setColumns(4);
		updateCodeField.setMaximumSize(new Dimension(updateCodeField.getPreferredSize().width, Integer.MAX_VALUE));

		updateFolderField = new JTextField();
		updateFolderField.setUI(new HintTextFieldUI("Server Folder"));

		if (mod.getClassicUpdateCode() > 0 && mod.getServerModFolder() != null) {
			useUpdaterCB.setSelected(true);
			updateCodeField.setText(Integer.toString(mod.getClassicUpdateCode()));
			updateFolderField.setText(mod.getServerModFolder());
		}

		updateCodeField.setEnabled(useUpdaterCB.isSelected());
		updateFolderField.setEnabled(useUpdaterCB.isSelected());
		updateCodeLabel.setEnabled(useUpdaterCB.isSelected());
		updateFolderLabel.setEnabled(useUpdaterCB.isSelected());

		updaterPanel.add(useUpdaterCB);
		updaterPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		updaterPanel.add(new JSeparator(JSeparator.VERTICAL));
		updaterPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		updaterPanel.add(updateCodeLabel);
		updaterPanel.add(Box.createRigidArea(new Dimension(5, 5)));

		updaterPanel.add(updateCodeField);
		updaterPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		updaterPanel.add(updateFolderLabel);
		updaterPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		updaterPanel.add(updateFolderField);
		secondRowPanel.add(updaterPanel);
		metadataPanel.add(metaPanelTitle);
		metadataPanel.add(namePanel);
		metadataPanel.add(secondRowPanel);
		metadataPanel.add(descriptionPanel);

		// BASEGAME + OFFICIAL DLC MODIFICATIONS PANEL
		JXPanel baseOfficialPanel = new JXPanel();
		baseOfficialPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);
		baseOfficialPanel.setLayout(new VerticalLayout());
		baseOfficialPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		baseOfficialPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		baseOfficialPanel.add(baseOfficialPanelTitle);
		baseOfficialPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel baseOfficialIntro = new JLabel(
				"<html>Basegame and official DLC modifiers should be used in circumstances where DLC cannot be added (e.g. in MP for gameplay mods, but not additional MP content mods).<br>You should also use them for items in TESTPATCH or files that must be modified as they load before the main menu (e.g. SFXGame).<br>There is very rarely a need to modify SP DLC using this method, use Custom DLC instead.</html>");
		baseOfficialPanel.add(baseOfficialIntro);
		for (MDEOfficialJob mdeJob : mod.rawOfficialJobs) {
			baseOfficialPanel.add(mdeJob.getPanel());
		}

		JButton addNewHeader = new JButton("Add new official files task");

		JPanel addNewTaskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addNewTaskPanel.add(addNewHeader);
		baseOfficialPanel.add(addNewTaskPanel);
		
		addNewHeader.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MDEOfficialTaskSelector ts = new MDEOfficialTaskSelector(this);
			}
		});

		// CUSTOM DLC PANEL
		// ======================================================================
		JXPanel customDLCPanel = new JXPanel();
		customDLCPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		customDLCPanel.setLayout(new BoxLayout(customDLCPanel, BoxLayout.Y_AXIS));
		customDLCPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		customDLCPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(customDLCPanelTitle);

		JLabel customDLCIntroText = new JLabel(
				"<html>Custom DLC are additional DLC folders that will be added to the game DLC directory.<br>Files with the same name as in other DLC or basegame will be overriden if the DLC has a higher mount priority.<br>Files that are loaded before the main menu can't be overriden this way.</html>");

		customDLCIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(customDLCIntroText);

		JLabel labelNoCustomDLC = new JLabel("No Custom DLCs will install when this mod is installed.");
		labelNoCustomDLC.setVisible(false);
		labelNoCustomDLC.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(labelNoCustomDLC);
		if (mod.rawcustomDLCDestDirs != null && mod.rawCustomDLCSourceDirs != null) {
			StringTokenizer srcStrok = new StringTokenizer(mod.rawcustomDLCDestDirs, ";");
			StringTokenizer destStrok = new StringTokenizer(mod.rawCustomDLCSourceDirs, ";");
			while (srcStrok.hasMoreTokens()) {
				String src = srcStrok.nextToken();
				String dest = destStrok.nextToken();
				final MDECustomDLC mde = generateMDECustomDLC(src, dest);
				mde.getSubtractButton().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						ModManager.debugLogger.writeMessage("MDE Custom DLC Remove button clicked: " + mde.getPair().getKey());
						customDLCPanel.remove(mde.getCustDLCLineItem());
						customDLCSelections.remove(mde);
						if (customDLCSelections.size() == 0) {
							labelNoCustomDLC.setVisible(true);
						}
						revalidate();
						repaint();
					}
				});
				customDLCPanel.add(mde.getCustDLCLineItem());
				customDLCSelections.add(mde);
			}
		} else {
			labelNoCustomDLC.setVisible(true);
		}

		JButton addCustomDLC = new JButton("Add Custom DLC folder to install");
		addCustomDLC.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(addCustomDLC);

		addCustomDLC.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				File[] directories = new File(mod.getModPath()).listFiles(File::isDirectory);
				System.out.println(directories.toString());
				ArrayList<File> directoryList = new ArrayList<File>(Arrays.asList(directories));
				for (MDECustomDLC dlc : customDLCSelections) {
					String name = dlc.getPair().getKey();
					File f = new File(mod.getModPath() + name);
					directoryList.remove(f);
				}

				if (directoryList.size() > 0) {
					// there are folders that can be added.
					// Prsent the list
					ModDescEditorDirectoryChooser chooser = new ModDescEditorDirectoryChooser(directoryList);
					if (chooser.getChosenFile() != null) {
						String src = chooser.getChosenFile().getName();
						final MDECustomDLC mde = generateMDECustomDLC(src, src); // same input, dest
						mde.getSubtractButton().addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								ModManager.debugLogger.writeMessage("MDE Custom DLC Remove button clicked: " + mde.getPair().getKey());
								customDLCPanel.remove(mde.getCustDLCLineItem());
								customDLCSelections.remove(mde);
								if (customDLCSelections.size() == 0) {
									labelNoCustomDLC.setVisible(true);
								}
								revalidate();
								repaint();
							}
						});
						customDLCPanel.add(mde.getCustDLCLineItem());
						customDLCSelections.add(mde);
						customDLCPanel.remove(addCustomDLC);
						customDLCPanel.add(addCustomDLC); // add to bottom.
						labelNoCustomDLC.setVisible(false);
						revalidate();
						repaint();
					}
				} else {
					JOptionPane.showMessageDialog(ModDescEditorWindow.this,
							"No folders in the mod folder are not already in use for installation.\nCustomDLC folders must be in the root of the mod directory.",
							"No folders available", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// altfiles
		JXPanel altFilesPanel = new JXPanel();
		altFilesPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		altFilesPanel.setLayout(new VerticalLayout());// (altFilesPanel, BoxLayout.Y_AXIS));
		altFilesPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		altFilesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		altFilesPanel.add(conditionalFilesPanelTitle);
		JPanel altFileRowsPanel = new JPanel(new VerticalLayout(3));
		altFilesPanel.add(altFileRowsPanel);
		{
			JLabel altFilesIntroText = new JLabel(
					"<html>You can specify that specific files are to be substituted, added, or removed from a Custom DLC folder you are installing if another Official or Custom DLC is present.<br>These options allow you to automatically include compatibility fixes as well as add options for users to configure the mod in an official developer sanctioned way.</html>",
					SwingConstants.LEFT);

			altFilesIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
			altFileRowsPanel.add(altFilesIntroText);

			noAltFilesLabel = new JLabel("No Alternate Files specified.");
			altFilesPanel.add(noAltFilesLabel);
			if (mod.rawAltFilesText != null) {
				for (AlternateFile af : mod.getAlternateFiles()) {
					MDEConditionalFileItem mdecfi = new MDEConditionalFileItem(this, af);
					altFileRowsPanel.add(mdecfi.getPanel());
					conditionalFileItems.add(mdecfi);
					noAltFilesLabel.setVisible(false);
				}
				noAltFilesLabel.setVisible(false);
			}

			JButton addAltFile = new JButton("Add Alternate File");
			addAltFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					MDEConditionalFileItem mdecfi = new MDEConditionalFileItem(ModDescEditorWindow.this, (AlternateFile) null);
					altFileRowsPanel.add(mdecfi.getPanel());
					mdecfi.getPanel().setCollapsed(false);
					conditionalFileItems.add(mdecfi);
					noAltFilesLabel.setVisible(false);
				}
			});
			JPanel addAltFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			addAltFilePanel.add(addAltFile);
			altFilesPanel.add(addAltFilePanel);
		}

		// altdlc in customdlc header.
		JXPanel altDLCPanel = new JXPanel();
		altDLCPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		altDLCPanel.setLayout(new VerticalLayout());
		altDLCPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		altDLCPanel.add(conditionalCustomDLCPanelTitle);

		JPanel altDLCRowsPanel = new JPanel(new VerticalLayout(3));
		altDLCPanel.add(altDLCRowsPanel);
		altDLCPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel altDLCIntroText = new JLabel(
				"<html>You can specify that specific CustomDLC are to be substituted, added, or removed from a Custom DLC folder you are installing if another Official or Custom DLC is present.<br>These options allow you to automatically include compatibility fixes as well as add options for users to configure the mod in an officially developer sanctioned way.</html>");

		altDLCIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
		altDLCRowsPanel.add(altDLCIntroText);
		noAltDLCLabel = new JLabel("No Alternate DLC specified.");
		altDLCPanel.add(noAltDLCLabel);
		if (mod.rawAltDlcText != null) {
			/*
			 * for (AlternateCustomDLC ad : mod.getAlternateCustomDLC()) {
			 * altDLCRowsPanel.add(new JLabel(ad.getFriendlyName())); }
			 */

			for (AlternateCustomDLC af : mod.getAlternateCustomDLC()) {
				MDEConditionalDLCItem mdecfi = new MDEConditionalDLCItem(this, af);
				altDLCRowsPanel.add(mdecfi.getPanel());
				conditionalDLCItems.add(mdecfi);
				noAltDLCLabel.setVisible(false);
			}
			noAltDLCLabel.setVisible(false);
		}

		JButton addAltDLC = new JButton("Add Alternate DLC Task");
		JPanel addAltDLCPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		addAltDLCPanel.add(addAltDLC);
		altDLCPanel.add(addAltDLCPanel);
		addAltDLC.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MDEConditionalDLCItem mdecdlc = new MDEConditionalDLCItem(ModDescEditorWindow.this, (AlternateCustomDLC) null);
				altDLCRowsPanel.add(mdecdlc.getPanel());
				mdecdlc.getPanel().setCollapsed(false);
				conditionalDLCItems.add(mdecdlc);
				noAltDLCLabel.setVisible(false);
			}
		});

		// outdatedcustomdlc
		JXPanel outdatedFoldersPanel = new JXPanel();
		outdatedFoldersPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

		outdatedFoldersPanel.setLayout(new VerticalLayout());
		outdatedFoldersPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		outdatedFoldersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		outdatedFoldersPanel.add(outdatedFoldersPanelTitle);
		JPanel oudatedFolderRowsPanel = new JPanel(new VerticalLayout(3));

		JLabel outdatedDLCIntroText = new JLabel(
				"<html>DLC folders in this list will be prompted for removal if they are found installed after installation of the mod.<br>Using this feature you can remove old versions of your mod (+old compatibility packs) if the DLC folder name changed.</html>");

		outdatedDLCIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
		oudatedFolderRowsPanel.add(outdatedDLCIntroText);

		outdatedFoldersPanel.add(oudatedFolderRowsPanel);
		noOutdatedCustomDLCLabel = new JLabel("No outdated custom DLC folders will be removed when this mod is installed.");
		for (String str : mod.getOutdatedDLCModules()) {
			MDEOutdatedCustomDLC mdeocdlc = new MDEOutdatedCustomDLC(this, str);
			outdatedCustomDLCItems.add(mdeocdlc);
			oudatedFolderRowsPanel.add(mdeocdlc.getPanel());
			mdeocdlc.getPanel().setCollapsed(false);
			noOutdatedCustomDLCLabel.setVisible(false);
		}

		noOutdatedCustomDLCLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		outdatedFoldersPanel.add(noOutdatedCustomDLCLabel);
		JButton addOutdatedFolderButton = new JButton("Add outdated folder");
		JPanel addOutdatedFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		addOutdatedFolderPanel.add(addOutdatedFolderButton);
		outdatedFoldersPanel.add(addOutdatedFolderPanel);
		addOutdatedFolderButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MDEOutdatedCustomDLC mdeexist : outdatedCustomDLCItems) {
					if (mdeexist.getOutdatedDLCNameField().getText().equals("")) {
						return; // do nothing
					}
				}
				MDEOutdatedCustomDLC mdeocdlc = new MDEOutdatedCustomDLC(ModDescEditorWindow.this, "");
				outdatedCustomDLCItems.add(mdeocdlc);
				oudatedFolderRowsPanel.add(mdeocdlc.getPanel());
				noOutdatedCustomDLCLabel.setVisible(false);
				mdeocdlc.getPanel().setCollapsed(false);
				revalidate();
				repaint();
			}
		});

		// Sections Top Level =================================
/*		JXCollapsiblePane metadataPane = new JXCollapsiblePane();
		metadataPane.add(metadataPanel);

		JXCollapsiblePane baseDLCPane = new JXCollapsiblePane();
		baseDLCPane.add(baseOfficialPanel);

		JXCollapsiblePane customDLCPane = new JXCollapsiblePane();
		customDLCPane.add(customDLCPanel);

		JXCollapsiblePane condFilesPane = new JXCollapsiblePane();
		condFilesPane.add(altFilesPanel);

		JXCollapsiblePane condDLCPane = new JXCollapsiblePane();
		condDLCPane.add(altDLCPanel);*/

		JXCollapsiblePane outdatedFoldersPane = new JXCollapsiblePane();
		outdatedFoldersPane.add(outdatedFoldersPanel);

		tabbedPane.addTab("Mod Info", null, new JScrollPane(metadataPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Edit user-visible mod information");
		tabbedPane.addTab("Basegame / Official DLC / Balance Changes", null,
				new JScrollPane(baseOfficialPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Specify modification for the Basegame, Official BioWare DLC and Balance Changes file");
		tabbedPane.addTab("Custom DLC", null, new JScrollPane(customDLCPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Specify custom DLC that will be installed");
		tabbedPane.addTab("Compatibility - File Based", null, new JScrollPane(altFilesPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Specify compatibility conditions and alternative files/folders for installation");
		tabbedPane.addTab("Compatibility - DLC Based", null, new JScrollPane(altDLCPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Specify compatibility conditions and DLC modifications based on game state/user selection");

		tabbedPane.addTab("Outdated DLC", null, new JScrollPane(outdatedFoldersPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"Specify outdated DLC foldernames to delete on install");

		//JScrollPane topScrollPane = new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel mainContentLayout = new JPanel(new BorderLayout());
		mainContentLayout.add(tabbedPane, BorderLayout.CENTER);

		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setLayout(new BoxLayout(statusBarPanel, BoxLayout.X_AXIS));

		statusLabel = new JLabel("");
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean isValid = performValidation();
				if (isValid) {
					String modDescStr = buildModDesc();
				}
			}
		});

		statusBarPanel.add(statusLabel);
		statusBarPanel.add(Box.createHorizontalGlue());
		statusBarPanel.add(saveButton);
		statusBarPanel.setBorder(new EmptyBorder(3, 5, 3, 5));
		mainContentLayout.add(statusBarPanel, BorderLayout.SOUTH);

		// JComponent cont = (JComponent) getContentPane();
		// cont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// this.setPreferredSize(new Dimension(800, 600));
		this.getContentPane().add(mainContentLayout);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	protected String buildModDesc() {
		try {
			Wini moddesc = new Wini();
			moddesc.put("ModManager", "cmmver", cmmverCombobox.getSelectedItem());
			moddesc.put("ModInfo", "modname", modNameField.getText());
			moddesc.put("ModInfo", "moddev", modDevField.getText());
			moddesc.put("ModInfo", "modver", modVerField.getText());
			String site = modSiteField.getText();
			if (!site.equals("")) {
				//will have been validated as url
				moddesc.put("ModInfo", "modsite", site);
			}

			String description = modDescriptionField.getText();
			description = ResourceUtils.convertNewlineToBr(description);
			moddesc.put("ModInfo", "moddesc", description);

			//if (Selections)

			if (customDLCSelections.size() > 0) {
				//has a custom DLC job
				StringBuilder srcdirs = new StringBuilder();
				StringBuilder destdirs = new StringBuilder();
				boolean isFirst = true;
				for (MDECustomDLC mdecdlc : customDLCSelections) {
					if (!isFirst) {
						srcdirs.append(";");
						destdirs.append(";");
					} else {
						isFirst = false;
					}
					SimpleEntry<String, String> pair = mdecdlc.getPair();
					srcdirs.append(pair.getKey());
					destdirs.append(pair.getValue());
				}
				moddesc.put("CUSTOMDLC", "sourcedirs", srcdirs.toString());
				moddesc.put("CUSTOMDLC", "destdirs", destdirs.toString());

				if (conditionalDLCItems.size() > 0) {
					StringBuilder condDLCSB = new StringBuilder();
					boolean isFirst2 = true;
					for (MDEConditionalDLCItem mdecdlc : conditionalDLCItems) {
						String altText = mdecdlc.generateModDescStr();
						if (isFirst2) {
							isFirst2 = false;
						} else {
							condDLCSB.append(",");
						}
						condDLCSB.append(altText);
					}
					moddesc.put("CUSTOMDLC", "altdlc", condDLCSB.toString());
				}
			}

			if (outdatedCustomDLCItems.size() > 0) {
				//has a custom DLC job
				StringBuilder outdatedDLCDirs = new StringBuilder();
				boolean isFirst = true;
				for (MDEOutdatedCustomDLC mdeocdlc : outdatedCustomDLCItems) {
					if (!isFirst) {
						outdatedDLCDirs.append(";");
					} else {
						isFirst = false;
					}
					String dlcFolderName = mdeocdlc.getOutdatedDLCNameField().getText();
					outdatedDLCDirs.append(dlcFolderName);
				}
				moddesc.put("CUSTOMDLC", "outdatedcustomdlc", outdatedDLCDirs.toString());
			}

			if (useUpdaterCB.isSelected()) {
				String updateCodeStr = updateCodeField.getText();
				String updateFolder = updateFolderField.getText();

				moddesc.put("ModInfo", "updatecode", updateCodeStr);
				moddesc.put("UPDATES", "serverfolder", updateFolder);
				moddesc.put("UPDATES", "blacklistedfiles", "Placeholder");
			}

			StringWriter writer = new StringWriter();
			moddesc.store(writer);
			String written = writer.toString();
			System.out.println(written);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * validates the moddesc fields. This is going to be ugly
	 * 
	 * @return true if valid, false otherwise
	 */
	protected boolean performValidation() {
		String modName = modNameField.getText();
		if (modName.length() == 0 || modName.length() > 60) {
			statusLabel.setText("Mod name must be between 1 and 60 characters long");
			return false;
		}
		String modDev = modDevField.getText();
		if (modDev.length() == 0) {
			statusLabel.setText("Mod Developer field cannot be empty");
			return false;
		}
		String modVersion = modVerField.getText();
		if (modVersion.length() == 0) {
			statusLabel.setText("Mod Version field cannot be empty. Floating point value is recommended.");
			return false;
		}
		return true;
	}

	private MDECustomDLC generateMDECustomDLC(String src, String dest) {
		JXPanel custDLCLineItem = new JXPanel(new HorizontalLayout());
		JButton subtractButton = new JButton("-");
		custDLCLineItem.add(subtractButton);
		String custDLCLine = src + " -> " + dest + " [mod folder -> installed folder]";
		custDLCLineItem.add(Box.createRigidArea(new Dimension(5, 5)));
		custDLCLineItem.add(new JLabel(custDLCLine));
		custDLCLineItem.setAlignmentX(Component.LEFT_ALIGNMENT);
		return new MDECustomDLC(custDLCLineItem, subtractButton, new AbstractMap.SimpleEntry<String, String>(src, dest));
	}

	public Mod getMod() {
		return mod;
	}

	public void setMod(Mod mod) {
		this.mod = mod;
	}

	public void removeConditionalFileItem(MDEConditionalFileItem mdeConditionalFileItem) {
		conditionalFileItems.remove(mdeConditionalFileItem);
		noAltFilesLabel.setVisible(conditionalFileItems.size() == 0);
	}

	public void removeConditionalDLCItem(MDEConditionalDLCItem mdeConditionalDLCItem) {
		conditionalDLCItems.remove(mdeConditionalDLCItem);
		noAltDLCLabel.setVisible(conditionalDLCItems.size() == 0);
	}

	public ArrayList<MDEConditionalDLCItem> getConditionalDLCItems() {
		return conditionalDLCItems;
	}

	public void removeOutdatedCustomDLCItem(MDEOutdatedCustomDLC mdeOutdatedCustomDLC) {
		outdatedCustomDLCItems.remove(mdeOutdatedCustomDLC);
		noOutdatedCustomDLCLabel.setVisible(outdatedCustomDLCItems.size() == 0);
	}
}
