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
import java.util.AbstractMap;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

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
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModDescEditorWindow extends JXFrame {

	private Mod mod;
	private static int SUBPANEL_INSET_LEFT = 30;
	private ArrayList<MDECustomDLC> customDLCSelections = new ArrayList<>();
	private ArrayList<MDEConditionalFileItem> conditionalFileItems = new ArrayList<MDEConditionalFileItem>();
	private JLabel noAltFilesLabel;

	public ModDescEditorWindow(Mod mod) {
		ModManager.debugLogger.writeMessage("Reloading " + mod.getModName() + " without automatic alternates applied.");
		Mod noAutoParse = new Mod();
		noAutoParse.setShouldApplyAutos(false);
		noAutoParse.loadMod(mod.getDescFile());
		this.mod = noAutoParse;
		setupWindow(mod);
		setVisible(true);
	}

	private void setupWindow(Mod mod) {
		this.setTitle(mod.getModName() + " - ModDesc Editor");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);

		JXPanel optionsPanel = new JXPanel(new VerticalLayout());
		optionsPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);
		//METADATA PANEL==================================================
		//Mod Name & ModDesc version
		JXPanel metadataPanel = new JXPanel();
		metadataPanel.setLayout(new VerticalLayout());

		JXPanel namePanel = new JXPanel();
		JXPanel nameFieldPanel = new JXPanel();
		nameFieldPanel.setLayout(new BoxLayout(nameFieldPanel, BoxLayout.X_AXIS));
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));

		TitledBorder nameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Name (Required)");
		nameFieldPanel.setBorder(nameBorder);
		JTextField modNameField = new JTextField(mod.getModName());
		modNameField.setToolTipText("Display name for your mod in Mod Manager.");
		nameFieldPanel.add(modNameField);

		namePanel.add(nameFieldPanel);
		//RIGHT SIDE NAME PANEL
		JXPanel versionPanel = new JXPanel();
		TitledBorder versionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Version Info (Required)");
		versionPanel.setBorder(versionBorder);
		versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.X_AXIS));

		JXPanel devPanel = new JXPanel();
		devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.X_AXIS));
		TitledBorder devBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Developer Name (Required)");
		devPanel.setBorder(devBorder);
		JTextField modDevField = new JTextField(mod.getAuthor());
		modDevField.setToolTipText("<html>Name of the mod developer. This should be your username/alias you go by in the modding scene.</html>");
		devPanel.add(modDevField, BorderLayout.NORTH);
		namePanel.add(devPanel);

		String modVerTooltip = "<html>Specifies the version of the mod. This must be a decimal number.<br>Typically you use integers to indicate large releases and fractional components to indicate minor updates.<br>e.g. 1.04 is major version 1 revision 4.</html>";
		JLabel modVer = new JLabel("Mod Version:");
		modVer.setToolTipText(modVerTooltip);
		versionPanel.add(modVer);
		versionPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		JTextField modVerField = new JTextField(Double.toString(mod.getVersion()), 8);
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

		JComboBox<String> cmmverCombobox = new JComboBox<String>();
		cmmverCombobox.addItem(Double.toString(mod.modCMMVer));
		if (mod.modCMMVer != ModManager.MODDESC_VERSION_SUPPORT) {
			cmmverCombobox.addItem(Double.toString(ModManager.MODDESC_VERSION_SUPPORT));
		}
		cmmverCombobox.setEditable(false);
		cmmverCombobox.setToolTipText(moddescversionTooltip);

		versionPanel.add(cmmverCombobox);

		namePanel.add(versionPanel);

		//Mod Site Panel row
		JXPanel secondRowPanel = new JXPanel();
		secondRowPanel.setLayout(new BoxLayout(secondRowPanel, BoxLayout.X_AXIS));

		JXPanel sitePanel = new JXPanel(new BorderLayout());
		TitledBorder siteBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Website/URL (Recommended)");
		sitePanel.setBorder(siteBorder);
		JTextField modSiteField = new JTextField(mod.getModSite());
		modSiteField.setToolTipText(
				"<html>Website URL for your mod.<br>This URL will appear at the bottom of the description panel and provides an easy way for the user to find your mod online for updates, support, etc.</html>");
		sitePanel.add(modSiteField, BorderLayout.NORTH);

		secondRowPanel.add(sitePanel);

		//Mod Description Panel
		JXPanel descriptionPanel = new JXPanel(new BorderLayout());
		TitledBorder descriptionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod Description (Required)");
		descriptionPanel.setBorder(descriptionBorder);
		JTextArea modDescriptionField = new JTextArea(ResourceUtils.convertBrToNewline(mod.getModDescription()));
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

		metadataPanel.add(namePanel);
		metadataPanel.add(secondRowPanel);
		metadataPanel.add(descriptionPanel);

		//
		boolean expandBaseDLC = false, expandCustomDLC = false, expandCondFiles = false, expandCondDLC = false;

		//BASEGAME + OFFICIAL DLC MODIFICATIONS PANEL
		JXPanel baseOfficialPanel = new JXPanel();
		baseOfficialPanel.setLayout(new VerticalLayout());
		baseOfficialPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		baseOfficialPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel baseOfficialIntro = new JLabel(
				"<html>Basegame and official DLC modifiers should be used in circumstances where DLC cannot be added (e.g. in MP for gameplay mods, but not additional MP content mods).<br>You should also use them for items in TESTPATCH or files that must be modified as they load before the main menu (e.g. SFXGame).<br>There is very rarely a need to modify SP DLC using this method, use Custom DLC instead.</html>");
		baseOfficialPanel.add(baseOfficialIntro);
		for (MDEOfficialJob mdeJob : mod.rawOfficialJobs) {
			expandBaseDLC = true;
			//Task Header Panel
			JButton button = new JButton();

			JLabel taskHeaderLabel = new JLabel(mdeJob.getRawHeader() + " (in " + mdeJob.getRawFolder() + ")", SwingConstants.LEFT);
			taskHeaderLabel.setFont(taskHeaderLabel.getFont().deriveFont(16f));
			taskHeaderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			taskHeaderLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					button.doClick();
				}
			});

			JXPanel jobHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
			jobHeaderPanel.add(button);
			jobHeaderPanel.add(taskHeaderLabel);

			//Task Details (Collapsable)
			JXPanel jobPanel = new JXPanel(new GridBagLayout());
			jobPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;

			//TASK DETAILS
			Insets columnRightSideInsets = new Insets(0, 0, 0, 10);
			//REPLACEMENTS
			{
				JXPanel replacementsListPanel = new JXPanel(new GridBagLayout());
				replacementsListPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
				GridBagConstraints gridC = new GridBagConstraints();

				JLabel replacementsHeader = new JLabel("File Replacements");
				replacementsHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
				jobPanel.add(replacementsHeader, gbc);
				gbc.gridy++;

				if (mdeJob.getRawNewFiles() != null && mdeJob.getRawReplaceFiles() != null) {
					StringTokenizer newStrok = new StringTokenizer(mdeJob.getRawNewFiles(), ";");
					StringTokenizer oldStrok = new StringTokenizer(mdeJob.getRawReplaceFiles(), ";");

					JLabel sourceHeader = new JLabel("New file");
					JLabel replaceHeader = new JLabel("In-game path to replace");
					sourceHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
					replaceHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
					gridC.fill = GridBagConstraints.HORIZONTAL;
					gridC.gridx = 1;
					gridC.insets = columnRightSideInsets;
					gridC.weightx = 0;
					gridC.anchor = GridBagConstraints.WEST;
					replacementsListPanel.add(sourceHeader, gridC);
					gridC.gridx = 2;
					gridC.weightx = 1;
					replacementsListPanel.add(replaceHeader, gridC);
					gridC.gridy++;

					while (newStrok.hasMoreTokens()) {
						String newFile = newStrok.nextToken();
						String oldFile = oldStrok.nextToken();

						JLabel fileReplaceLabel = new JLabel(newFile);
						JLabel replacePathLabel = new JLabel(oldFile);

						JButton minusButton = new JButton("-");

						gridC.fill = GridBagConstraints.NONE;
						gridC.gridy++;
						gridC.gridx = 0;
						gridC.weightx = 0;
						replacementsListPanel.add(minusButton, gridC);

						gridC.gridx = 1;
						replacementsListPanel.add(fileReplaceLabel, gridC);

						gridC.gridx = 2;
						gridC.weightx = 1;
						gridC.fill = GridBagConstraints.HORIZONTAL;
						replacementsListPanel.add(replacePathLabel, gridC);
					}

				} else {
					JLabel noReplacements = new JLabel("No files are replaced in this job.");
					replacementsListPanel.add(noReplacements, gbc);
					gbc.gridy++;
				}

				if (!mdeJob.getRawHeader().equals(ModTypeConstants.BINI)) {
					gridC.gridy++;
					gridC.gridx = 0;
					gridC.weightx = 0;
					gridC.anchor = GridBagConstraints.WEST;
					gridC.gridwidth = 3;
					gridC.fill = GridBagConstraints.NONE;
					JButton addReplacementFile = new JButton("Add replacement file to " + mdeJob.getRawHeader());
					replacementsListPanel.add(addReplacementFile, gridC);
				}

				jobPanel.add(replacementsListPanel, gbc);
			}
			if (!mdeJob.getRawHeader().equals(ModTypeConstants.BINI)) {
				//ADD FILES
				{
					JXPanel additionsListPanel = new JXPanel(new GridBagLayout());
					additionsListPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
					GridBagConstraints gridC = new GridBagConstraints();
					gbc.gridy++;
					JLabel newFilesHeader = new JLabel("New Additional Files");
					newFilesHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
					jobPanel.add(newFilesHeader, gbc);
					gbc.gridy++;

					if (mdeJob.getRawAddFiles() != null && mdeJob.getRawAddTargetFiles() != null) {

						//Get Raad-only
						ArrayList<String> readOnlyFiles = new ArrayList<String>();
						if (mdeJob.getRawAddReadOnlyTargetFiles() != null) {
							StringTokenizer addTargetReadOnlyStrok = new StringTokenizer(mdeJob.getRawAddReadOnlyTargetFiles(), ";");

							while (addTargetReadOnlyStrok.hasMoreTokens()) {
								String readonlytarget = addTargetReadOnlyStrok.nextToken();
								if (mdeJob.getRawHeader().equals(ModTypeConstants.BASEGAME)) {
									readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, false);
								} else {
									readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, true);
								}
								readOnlyFiles.add(readonlytarget);
							}
						}
						StringTokenizer addStrok = new StringTokenizer(mdeJob.getRawAddFiles(), ";");
						StringTokenizer addTargetsStrok = new StringTokenizer(mdeJob.getRawAddTargetFiles(), ";");

						/*
						 * gbc.gridy++;
						 */

						JLabel sourceHeader = new JLabel("New file");
						JLabel replaceHeader = new JLabel("In-game path to add to");
						sourceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
						replaceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
						gridC.insets = columnRightSideInsets;
						gridC.fill = GridBagConstraints.HORIZONTAL;
						gridC.gridx = 1;
						additionsListPanel.add(sourceHeader, gridC);
						gridC.gridx = 2;
						additionsListPanel.add(replaceHeader, gridC);
						gridC.gridy++;
						gridC.anchor = GridBagConstraints.WEST;

						while (addStrok.hasMoreTokens()) {

							JButton minusButton = new JButton("-");

							gridC.fill = GridBagConstraints.NONE;
							gridC.gridy++;
							gridC.gridx = 0;
							gridC.weightx = 0;
							additionsListPanel.add(minusButton, gridC);

							String newFile = addStrok.nextToken();
							String oldFile = addTargetsStrok.nextToken();

							JLabel fileReplaceLabel = new JLabel(newFile);
							JLabel replacePathLabel = new JLabel(oldFile);

							gridC.gridx = 1;

							additionsListPanel.add(fileReplaceLabel, gridC);
							gridC.gridx = 2;
							gridC.weightx = 0;

							additionsListPanel.add(replacePathLabel, gridC);
							gridC.fill = GridBagConstraints.HORIZONTAL;

							gridC.gridx = 3;
							gridC.weightx = 1;

							JCheckBox readOnly = new JCheckBox("Read only");
							if (readOnlyFiles.contains(oldFile)) {
								readOnly.setSelected(true);
							}
							additionsListPanel.add(readOnly, gridC);
						}
					} else {
						JLabel noAdditions = new JLabel("No files are added to the game by this job.", SwingConstants.LEFT);
						gridC.gridy++;
						gridC.gridx = 0;
						gridC.weightx = 1;
						gridC.anchor = GridBagConstraints.WEST;
						gridC.gridwidth = 3;
						gridC.fill = GridBagConstraints.NONE;
						additionsListPanel.add(noAdditions, gridC);
					}

					gridC.gridy++;
					gridC.gridx = 0;
					gridC.weightx = 0;
					gridC.anchor = GridBagConstraints.WEST;
					gridC.gridwidth = 3;
					gridC.fill = GridBagConstraints.NONE;
					JButton addNewFile = new JButton("Add additional file to " + mdeJob.getRawHeader());
					additionsListPanel.add(addNewFile, gridC);

					//end add panel
					gbc.gridy++;
					jobPanel.add(additionsListPanel, gbc);
				}

				//MANUAL ALTFILES
				//ADD FILES
				{
					JXPanel additionsListPanel = new JXPanel(new GridBagLayout());
					additionsListPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
					GridBagConstraints gridC = new GridBagConstraints();
					gbc.gridy++;
					JLabel newFilesHeader = new JLabel("User selectable options");
					newFilesHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
					jobPanel.add(newFilesHeader, gbc);
					gbc.gridy++;

					if (mdeJob.getRawAddFiles() != null && mdeJob.getRawAddTargetFiles() != null) {

						//Get Raad-only
						ArrayList<String> readOnlyFiles = new ArrayList<String>();
						if (mdeJob.getRawAddReadOnlyTargetFiles() != null) {
							StringTokenizer addTargetReadOnlyStrok = new StringTokenizer(mdeJob.getRawAddReadOnlyTargetFiles(), ";");

							while (addTargetReadOnlyStrok.hasMoreTokens()) {
								String readonlytarget = addTargetReadOnlyStrok.nextToken();
								if (mdeJob.getRawHeader().equals(ModTypeConstants.BASEGAME)) {
									readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, false);
								} else {
									readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, true);
								}
								readOnlyFiles.add(readonlytarget);
							}
						}
						StringTokenizer addStrok = new StringTokenizer(mdeJob.getRawAddFiles(), ";");
						StringTokenizer addTargetsStrok = new StringTokenizer(mdeJob.getRawAddTargetFiles(), ";");

						/*
						 * gbc.gridy++;
						 */

						JLabel sourceHeader = new JLabel("New file");
						JLabel replaceHeader = new JLabel("In-game path to add to");
						sourceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
						replaceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
						gridC.insets = columnRightSideInsets;
						gridC.fill = GridBagConstraints.HORIZONTAL;
						gridC.gridx = 1;
						additionsListPanel.add(sourceHeader, gridC);
						gridC.gridx = 2;
						additionsListPanel.add(replaceHeader, gridC);
						gridC.gridy++;
						gridC.anchor = GridBagConstraints.WEST;

						while (addStrok.hasMoreTokens()) {

							JButton minusButton = new JButton("-");

							gridC.fill = GridBagConstraints.NONE;
							gridC.gridy++;
							gridC.gridx = 0;
							gridC.weightx = 0;
							additionsListPanel.add(minusButton, gridC);

							String newFile = addStrok.nextToken();
							String oldFile = addTargetsStrok.nextToken();

							JLabel fileReplaceLabel = new JLabel(newFile);
							JLabel replacePathLabel = new JLabel(oldFile);

							gridC.gridx = 1;

							additionsListPanel.add(fileReplaceLabel, gridC);
							gridC.gridx = 2;
							gridC.weightx = 0;

							additionsListPanel.add(replacePathLabel, gridC);
							gridC.fill = GridBagConstraints.HORIZONTAL;

							gridC.gridx = 3;
							gridC.weightx = 1;

							JCheckBox readOnly = new JCheckBox("Read only");
							if (readOnlyFiles.contains(oldFile)) {
								readOnly.setSelected(true);
							}
							additionsListPanel.add(readOnly, gridC);
						}
					} else {
						JLabel noAdditions = new JLabel("No user selection options are available for this job.", SwingConstants.LEFT);
						gridC.gridy++;
						gridC.gridx = 0;
						gridC.weightx = 1;
						gridC.anchor = GridBagConstraints.WEST;
						gridC.gridwidth = 3;
						gridC.fill = GridBagConstraints.NONE;
						additionsListPanel.add(noAdditions, gridC);
					}

					gridC.gridy++;
					gridC.gridx = 0;
					gridC.weightx = 0;
					gridC.anchor = GridBagConstraints.WEST;
					gridC.gridwidth = 3;
					gridC.fill = GridBagConstraints.NONE;
					JButton addNewFile = new JButton("Add user selectable option to " + mdeJob.getRawHeader());
					additionsListPanel.add(addNewFile, gridC);

					//end add panel
					gbc.gridy++;
					jobPanel.add(additionsListPanel, gbc);
				}
			}

			//REQUIREMENTS
			if (!mdeJob.getRawHeader().equals(ModTypeConstants.BASEGAME)) {
				JLabel requirementLabel = new JLabel("Reason for this task: " + mdeJob.getRawRequirementText());
				gbc.gridy++;
				jobPanel.add(requirementLabel, gbc);
			}

			JXCollapsiblePane jobPane = new JXCollapsiblePane();
			jobPane.add(jobPanel);
			jobPane.setCollapsed(true);

			Action toggleAction = jobPane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
			toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
			toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
			button.setAction(toggleAction);
			button.setText("");

			baseOfficialPanel.add(jobHeaderPanel);
			baseOfficialPanel.add(jobPane);

		}

		JButton addNewHeader = new JButton("Add new official files task");

		JPanel addNewTaskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addNewTaskPanel.add(addNewHeader);
		baseOfficialPanel.add(addNewTaskPanel);

		//CUSTOM DLC PANEL ======================================================================
		JXPanel customDLCPanel = new JXPanel();
		customDLCPanel.setLayout(new BoxLayout(customDLCPanel, BoxLayout.Y_AXIS));
		customDLCPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		JLabel customDLCIntroText = new JLabel(
				"<html>Custom DLC are additional DLC folders that will be added to the game DLC directory.<br>Files with the same name as in other DLC or basegame will be overriden if the DLC has a higher mount priority.<br>Files that are loaded before the main menu can't be overriden this way.</html>");

		customDLCIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(customDLCIntroText);

		JLabel labelNoCustomDLC = new JLabel("No Custom DLCs will install when this mod is installed.");
		labelNoCustomDLC.setVisible(false);
		labelNoCustomDLC.setAlignmentX(Component.LEFT_ALIGNMENT);
		customDLCPanel.add(labelNoCustomDLC);
		if (mod.rawcustomDLCDestDirs != null && mod.rawCustomDLCSourceDirs != null) {
			expandCustomDLC = true;

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
					//there are folders that can be added.
					//Prsent the list
					ModDescEditorDirectoryChooser chooser = new ModDescEditorDirectoryChooser(directoryList);
					if (chooser.getChosenFile() != null) {
						String src = chooser.getChosenFile().getName();
						final MDECustomDLC mde = generateMDECustomDLC(src, src); //same input, dest
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
						customDLCPanel.add(addCustomDLC); //add to bottom.
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

		//altfiles
		JXPanel altFilesPanel = new JXPanel();
		altFilesPanel.setLayout(new VerticalLayout());//(altFilesPanel, BoxLayout.Y_AXIS));
		altFilesPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		JPanel altFileRowsPanel = new JPanel(new VerticalLayout(3));
		altFilesPanel.add(altFileRowsPanel);
		{
			JLabel altFilesIntroText = new JLabel(
					"<html>You can specify that specific files are to be substituted, added, or removed from a Custom DLC folder you are installing if another Official or Custom DLC is present.<br>These options allow you to automatically include compatibility fixes as well as add options for users to configure the mod in an officially developer sanctioned way.</html>",
					SwingConstants.LEFT);

			altFilesIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
			altFileRowsPanel.add(altFilesIntroText);

			noAltFilesLabel = new JLabel("No alternate files specified.");
			altFilesPanel.add(noAltFilesLabel);
			if (mod.rawAltFilesText != null) {
				expandCondFiles = true;

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
			JPanel addAltFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			addAltFilePanel.add(addAltFile);
			altFilesPanel.add(addAltFilePanel);
		}
		//altdlc in customdlc header.
		JXPanel altDLCPanel = new JXPanel();
		altDLCPanel.setLayout(new BoxLayout(altDLCPanel, BoxLayout.Y_AXIS));
		altDLCPanel.setBorder(new EmptyBorder(3, SUBPANEL_INSET_LEFT, 3, 3));
		JLabel altDLCIntroText = new JLabel(
				"<html>You can specify that specific CustomDLC are to be substituted, added, or removed from a Custom DLC folder you are installing if another Official or Custom DLC is present.<br>These options allow you to automatically include compatibility fixes as well as add options for users to configure the mod in an officially developer sanctioned way.</html>");

		altDLCIntroText.setAlignmentX(Component.LEFT_ALIGNMENT);
		altDLCPanel.add(altDLCIntroText);
		{
			if (mod.rawAltDlcText != null) {
				expandCondDLC = true;
				for (AlternateCustomDLC ad : mod.getAlternateCustomDLC()) {
					altDLCPanel.add(new JLabel(ad.getFriendlyName()));
				}
			} else {

				altDLCPanel.add(new JLabel("No alc specified."));
			}
		}
		//Sections Top Level =================================
		JXCollapsiblePane metadataPane = new JXCollapsiblePane();
		metadataPane.add(metadataPanel);

		JXCollapsiblePane baseDLCPane = new JXCollapsiblePane();
		baseDLCPane.add(baseOfficialPanel);
		baseDLCPane.setCollapsed(!expandBaseDLC);

		JXCollapsiblePane customDLCPane = new JXCollapsiblePane();
		customDLCPane.add(customDLCPanel);
		customDLCPane.setCollapsed(!expandCustomDLC);

		JXCollapsiblePane condFilesPane = new JXCollapsiblePane();
		condFilesPane.add(altFilesPanel);
		condFilesPane.setCollapsed(!expandCondFiles);

		JXCollapsiblePane condDLCPane = new JXCollapsiblePane();
		condDLCPane.add(altDLCPanel);
		condDLCPane.setCollapsed(!expandCondDLC);

		JLabel metaPanelTitle = new JLabel("Mod Metadata");
		JLabel baseOfficialPanelTitle = new JLabel("Basegame + Official DLC + Balance Changes Modifications");
		JLabel customDLCPanelTitle = new JLabel("Always-installed Custom DLC");
		JLabel conditionalFilesPanelTitle = new JLabel("Compatibility + Manual options files");
		JLabel conditionalCustomDLCPanelTitle = new JLabel("Compatibility + Manual options DLC");

		JButton metaPanelButton = new JButton();
		JButton basePanelButton = new JButton();
		JButton customPanelButton = new JButton();
		JButton condFilePanelButton = new JButton();
		JButton condDLCPanelButton = new JButton();

		//Set fonts
		metaPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(18f));
		baseOfficialPanelTitle.setFont(baseOfficialPanelTitle.getFont().deriveFont(18f));
		customDLCPanelTitle.setFont(customDLCPanelTitle.getFont().deriveFont(18f));
		conditionalFilesPanelTitle.setFont(conditionalFilesPanelTitle.getFont().deriveFont(18f));
		conditionalCustomDLCPanelTitle.setFont(conditionalCustomDLCPanelTitle.getFont().deriveFont(18f));

		//Button Clicks

		JLabel[] labels = { metaPanelTitle, baseOfficialPanelTitle, customDLCPanelTitle, conditionalFilesPanelTitle, conditionalCustomDLCPanelTitle };
		JButton[] expanders = { metaPanelButton, basePanelButton, customPanelButton, condFilePanelButton, condDLCPanelButton };
		JXCollapsiblePane[] expanderPanes = { metadataPane, baseDLCPane, customDLCPane, condFilesPane, condDLCPane };

		for (int i = 0; i < expanders.length; i++) {
			JButton button = expanders[i];
			JXCollapsiblePane pane = expanderPanes[i];
			JLabel label = labels[i];
			Action toggleAction = pane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
			toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
			toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
			button.setAction(toggleAction);
			button.setText("");

			//make label clickable
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					button.doClick();
				}
			});
		}

		//Header Panels
		JXPanel metaHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
		JXPanel baseHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
		JXPanel customHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
		JXPanel condFileHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
		JXPanel condDLCHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));

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

		JScrollPane topScrollPane = new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//JComponent cont = (JComponent) getContentPane();
		//cont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//this.setPreferredSize(new Dimension(800, 600));
		this.getContentPane().add(topScrollPane);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	private MDECustomDLC generateMDECustomDLC(String src, String dest) {
		JXPanel custDLCLineItem = new JXPanel(new HorizontalLayout());
		JButton subtractButton = new JButton("-");
		custDLCLineItem.add(subtractButton);
		String custDLCLine = src + " -> " + dest + " [mod folder -> installed folder]";
		custDLCLineItem.add(Box.createRigidArea(new Dimension(5,5)));
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
}
