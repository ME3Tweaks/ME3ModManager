package com.me3tweaks.modmanager.moddesceditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;

import com.me3tweaks.modmanager.objects.AlternateCustomDLC;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.SwingLink;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class MDEConditionalDLCItem {
	private JXCollapsiblePane collapsablePanel;

	public JXCollapsiblePane getPanel() {
		return collapsablePanel;
	}

	public AlternateCustomDLC getAlternateDLC() {
		return altdlc;
	}

	public JComboBox<String> getConditionBox() {
		return conditionBox;
	}

	public static String[] getConditions() {
		return conditions;
	}

	public static String[] getConditionsHuman() {
		return conditionsHuman;
	}

	public JComboBox<String> getOperationBox() {
		return operationBox;
	}

	public static String[] getOperations() {
		return operations;
	}

	public JButton getMinusButton() {
		return minusButton;
	}

	public JTextField getDescriptionField() {
		return descriptionField;
	}

	public JTextField getUserReasonField() {
		return userReasonField;
	}

	public void setUserReasonField(JTextField userReasonField) {
		this.userReasonField = userReasonField;
	}

	private AlternateCustomDLC altdlc;

	private JComboBox<String> conditionBox;
	private static String[] conditions = { AlternateCustomDLC.CONDITION_DLC_NOT_PRESENT, AlternateCustomDLC.CONDITION_DLC_PRESENT, AlternateCustomDLC.CONDITION_ALL_DLC_PRESENT,
			AlternateCustomDLC.CONDITION_ANY_DLC_NOT_PRESENT, AlternateCustomDLC.CONDITION_MANUAL };
	private static String[] conditionsHuman = { "is not installed", "is installed", "are all installed", "any is not installed", "is selected by user" };

	private JComboBox<String> operationBox;
	private static String[] operations = { AlternateCustomDLC.OPERATION_ADD_CUSTOMDLC_JOB, AlternateCustomDLC.OPERATION_ADD_FILES_TO_CUSTOMDLC_FOLDER };
	private static String[] operationsHuman = { "add additional custom DLC", "merge folder" };

	private JButton minusButton;
	private JTextField descriptionField, userReasonField;

	private Mod mod;

	private ModDescEditorWindow owningWindow;

	private SwingLink srcLabel;

	private SwingLink destLabel;
	private static int itemSpacing = 5;

	// private String altFile;
	private String modFile;

	private JTextField conditionalDLC;

	public MDEConditionalDLCItem(ModDescEditorWindow owningWindow, AlternateCustomDLC af2) {
		this.mod = owningWindow.getMod();
		this.owningWindow = owningWindow;
		boolean startCollapsed = false;
		if (af2 == null) {
			af2 = new AlternateCustomDLC();
			startCollapsed = true;
		}
		this.altdlc = af2;
		setupPanel();
		collapsablePanel.setCollapsed(startCollapsed);
	}

	private void setupPanel() {
		collapsablePanel = new JXCollapsiblePane();
		JPanel panel = new JPanel(new HorizontalLayout());
		collapsablePanel.add(panel);
		minusButton = new JButton("-");
		panel.add(minusButton);
		minusButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				collapsablePanel.setCollapsed(true);
				owningWindow.removeConditionalDLCItem(MDEConditionalDLCItem.this);
			}
		});

		conditionBox = new JComboBox<String>(conditionsHuman);
		operationBox = new JComboBox<String>(operationsHuman);
		userReasonField = new JTextField(altdlc.getFriendlyName());
		userReasonField.setUI(new HintTextFieldUI("User friendly string e.g. Fixes X if Y is present"));

		userReasonField.setToolTipText("User friendly name. If one is not entered, a automatically generated one is displayed instead.");
		userReasonField.setColumns(30);
		panel.add(userReasonField);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing * 2, 3)));
		panel.add(new JSeparator(JSeparator.VERTICAL));

		JLabel ifLabel = new JLabel("If");
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(ifLabel);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		String condDLCStr = altdlc.getConditionalDLC().replaceAll("\\(", "").replaceAll("\\)", "");
		conditionalDLC = new JTextField(condDLCStr, 16);
		conditionalDLC.setUI(new HintTextFieldUI("DLC_MOD_Condition"));
		panel.add(conditionalDLC);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(operationBox);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(conditionBox);

		JLabel thenLabel = new JLabel("then");
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(thenLabel);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));

		panel.add(operationBox);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));

		JPanel rightSideVerticalPanel = new JPanel();
		rightSideVerticalPanel.setLayout(new BoxLayout(rightSideVerticalPanel, BoxLayout.Y_AXIS));
		rightSideVerticalPanel.add(Box.createVerticalGlue());
		ActionListener srcChangeAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Platform.runLater(() -> {
					if (operationBox.getSelectedIndex() == 0) {
						// must be top level folder
						File[] directories = new File(mod.getModPath()).listFiles(File::isDirectory);
						ArrayList<File> directoryList = new ArrayList<File>(Arrays.asList(directories));
						for (MDECustomDLC dlc : owningWindow.getCustomDLCSelections()) {
							String name = dlc.getPair().getKey();
							File f = new File(mod.getModPath() + name);
							directoryList.remove(f);
						}

						if (directoryList.size() == 0) {
							JOptionPane.showMessageDialog(owningWindow,
									"Adding a custom DLC folder for installation requires the folder to be in the top level directory.\nAll folders are already automatically installed, so there is no possible valid option right now. Add a new Custom DLC folder to use this option.",
									"No available options", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}

					DirectoryChooser fileChooser = new DirectoryChooser();
					fileChooser.setInitialDirectory(new File(mod.getModPath()));

					// Show open file dialog
					File file = fileChooser.showDialog(null);
					if (file != null) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								// Run on UI thread

								// we need to verify file...
								String relativePath = ResourceUtils.getRelativePath(file.getAbsolutePath(), mod.getModPath(), File.separator);
								if (relativePath.contains("..")) {
									// reject as out of mod folder.

									JOptionPane.showMessageDialog(owningWindow, "This file is not in the mod folder.", "Invalid file", JOptionPane.ERROR_MESSAGE);
								}

								// File is valid
								srcLabel.setText(relativePath);
							}
						});
					}
				});
			}
		};
		srcLabel = new SwingLink(altdlc.getAltDLC(), "Click to change file", srcChangeAction);

		rightSideVerticalPanel.add(srcLabel);

		JLabel intoLabel = new JLabel("into");
		// Component forSpace = Box.createRigidArea(new Dimension(itemSpacing, 3));
		// panel.add(forSpace);
		rightSideVerticalPanel.add(intoLabel);

		ActionListener changeDestAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ModJob job = mod.getJobByModuleName(ModTypeConstants.CUSTOMDLC);
				if (job != null) {
					int index = operationBox.getSelectedIndex();
					if (index > 0) { // if 0, add mode
						index = MDEModFileChooser.OPTIONTYPE_SELECTONLY; // select only
					} else {
						index = MDEModFileChooser.OPTIONTYPE_ADDONLY;
					}
					MDEModFolderChooser fileChooser = new MDEModFolderChooser(owningWindow, modFile, index, job);
					if (fileChooser.getSelectedFile() != null) {
						modFile = fileChooser.getSelectedFile();
						destLabel.setText(modFile);
					}
				}
			}
		};
		modFile = altdlc.getDestDLC();
		destLabel = new SwingLink(modFile, "Click to change file", changeDestAction);
		// panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		rightSideVerticalPanel.add(destLabel);
		rightSideVerticalPanel.add(Box.createVerticalGlue());

		panel.add(rightSideVerticalPanel);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));

		operationBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = operationBox.getSelectedIndex();
					switch (index) {
					case 0: // add
						destLabel.setVisible(false);
						intoLabel.setVisible(false);
						break;
					case 1: // merge
						destLabel.setVisible(true);
						intoLabel.setVisible(true);
						break;
					}
				}
			}
		});

		// hide into/dest for add
		if (operationBox.getSelectedIndex() == 0) {
			destLabel.setVisible(false);
			intoLabel.setVisible(false);
		}

		if (altdlc.getCondition() != null) {
			switch (altdlc.getCondition()) {
			case AlternateCustomDLC.CONDITION_DLC_NOT_PRESENT:
				conditionBox.setSelectedIndex(0);
				break;
			case AlternateCustomDLC.CONDITION_DLC_PRESENT:
				conditionBox.setSelectedIndex(1);
				break;
			case AlternateCustomDLC.CONDITION_ALL_DLC_PRESENT:
				conditionBox.setSelectedIndex(2);
				break;
			case AlternateCustomDLC.CONDITION_ANY_DLC_NOT_PRESENT:
				conditionBox.setSelectedIndex(3);
				break;
			case AlternateCustomDLC.CONDITION_MANUAL:
				conditionBox.setSelectedIndex(4);
				break;
			}
		} else {
			conditionBox.setSelectedIndex(0); // default
		}

		if (altdlc.getOperation() != null) {
			switch (altdlc.getOperation()) {
			case AlternateCustomDLC.OPERATION_ADD_CUSTOMDLC_JOB:
				operationBox.setSelectedIndex(0);
				break;
			case AlternateCustomDLC.OPERATION_ADD_FILES_TO_CUSTOMDLC_FOLDER:
				operationBox.setSelectedIndex(1);
				break;
			}
		} else {
			operationBox.setSelectedIndex(0);
		}
	}

	public String getDLCDestination() {
		return destLabel.getRawText();
	}

	public String generateModDescStr() {
		/*
		 * conditionalDLC = ValueParserLib.getStringProperty(altfileText,
		 * "ConditionalDLC", false); modFile =
		 * ValueParserLib.getStringProperty(altfileText, "ModFile", false); if
		 * (modFile.charAt(0) != '/' && modFile.charAt(0) != '\\') { modFile =
		 * "/" + modFile; } altFile =
		 * ValueParserLib.getStringProperty(altfileText, "ModAltFile", false);
		 * if (altFile == null) { altFile =
		 * ValueParserLib.getStringProperty(altfileText, "AltFile", false); }
		 * condition = ValueParserLib.getStringProperty(altfileText,
		 * "Condition", false); description =
		 * ValueParserLib.getStringProperty(altfileText, "Description", true);
		 * operation = ValueParserLib.getStringProperty(altfileText,
		 * "ModOperation", false); friendlyName =
		 * ValueParserLib.getStringProperty(altfileText, "FriendlyName", true);
		 * substitutefile = ValueParserLib.getStringProperty(altfileText,
		 * "SubstituteFile", false);
		 */

		String str = "(";
		int conditionIndex = getConditionBox().getSelectedIndex();

		//Condition
		switch (conditionIndex) {
		case 0:
			str += ValueParserLib.generateValue("Condition", AlternateCustomDLC.CONDITION_DLC_NOT_PRESENT, false);
			break;
		case 1:
			str += ValueParserLib.generateValue("Condition", AlternateCustomDLC.CONDITION_DLC_PRESENT, false);
			break;
		case 2:
			str += ValueParserLib.generateValue("Condition", AlternateCustomDLC.CONDITION_ALL_DLC_PRESENT, false);
			break;
		case 3:
			str += ValueParserLib.generateValue("Condition", AlternateCustomDLC.CONDITION_ANY_DLC_NOT_PRESENT, false);
			break;
		case 4:
			str += ValueParserLib.generateValue("Condition", AlternateCustomDLC.CONDITION_MANUAL, false);
			break;
		}

		str += ",";

		//ConditionalDLC=
		switch (conditionIndex) {
		case 0:
		case 1:
			str += ValueParserLib.generateValue("ConditionalDLC", conditionalDLC.getText(), false);
			str += ",";
			break;
		case 2:
		case 3:
			ArrayList<String> values = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(conditionalDLC.getText(), ";");
			while (st.hasMoreTokens()) {
				values.add(st.nextToken());
			}
			str += ValueParserLib.generateValueList("ConditionalDLC", values);
			str += ",";
			break;
		}
		
		//ModOperation
		int operationIndex = getOperationBox().getSelectedIndex();

		//Condition
		switch (operationIndex) {
		case 0:
			str += ValueParserLib.generateValue("ModOperation", AlternateCustomDLC.OPERATION_ADD_CUSTOMDLC_JOB, false);
			break;
		case 1:
			str += ValueParserLib.generateValue("ModOperation", AlternateCustomDLC.OPERATION_ADD_FILES_TO_CUSTOMDLC_FOLDER, false);
			break;
		}
		
		str += ",";
		
		//FriendlyName
		str += ValueParserLib.generateValue("FriendlyName", userReasonField.getText(), true);

		str += ")";
		return str;
	}
}
