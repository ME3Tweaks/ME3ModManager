package com.me3tweaks.modmanager.moddesceditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.HorizontalLayout;

import com.me3tweaks.modmanager.objects.AlternateFile;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.ui.SwingLink;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

public class MDEConditionalFileItem {
	private JPanel panel;

	public JPanel getPanel() {
		return panel;
	}

	public AlternateFile getAf() {
		return af;
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

	private AlternateFile af;

	private JComboBox<String> conditionBox;
	private static String[] conditions = { AlternateFile.CONDITION_DLC_NOT_PRESENT, AlternateFile.CONDITION_DLC_PRESENT, AlternateFile.CONDITION_MANUAL };
	private static String[] conditionsHuman = { "is not installed", "is installed", "is selected by user" };

	private JComboBox<String> operationBox;
	private static String[] operations = { AlternateFile.OPERATION_INSTALL, AlternateFile.OPERATION_NOINSTALL, AlternateFile.OPERATION_SUBSTITUTE };
	private static String[] operationsHuman = { "add an extra file", "don't install file", "substitute file" };

	private JButton minusButton;
	private JTextField descriptionField;

	private Mod mod;
	private static int itemSpacing = 5;

	public MDEConditionalFileItem(Mod mod, AlternateFile af) {
		this.mod = mod;
		this.af = af;
		setupPanel();
	}

	private void setupPanel() {
		panel = new JPanel(new HorizontalLayout());

		minusButton = new JButton("-");
		panel.add(minusButton);

		conditionBox = new JComboBox<String>(conditionsHuman);
		operationBox = new JComboBox<String>(operationsHuman);

		JLabel ifLabel = new JLabel("If");
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(ifLabel);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		JTextField conditionalDLC = new JTextField(af.getConditionalDLC(), 16);
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
					FileChooser fileChooser = new FileChooser();
					fileChooser.setInitialDirectory(new File(mod.getModPath()));
					//Set extension filter
					FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Mod File", "*.*");
					fileChooser.getExtensionFilters().addAll(extFilterJPG);

					//Show open file dialog
					File file = fileChooser.showOpenDialog(null);
				});
			}
		};
		SwingLink srcLabel = new SwingLink(af.getAltFile(), "Click to change file", srcChangeAction);
		rightSideVerticalPanel.add(srcLabel);

		JLabel forLabel = new JLabel("for");
		//Component forSpace = Box.createRigidArea(new Dimension(itemSpacing, 3));
		//panel.add(forSpace);
		rightSideVerticalPanel.add(forLabel);

		ActionListener changeDestAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

			}
		};
		SwingLink destLabel = new SwingLink(af.getModFile(), "Click to change file", changeDestAction);
		//panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		rightSideVerticalPanel.add(destLabel);
		rightSideVerticalPanel.add(Box.createVerticalGlue());

		panel.add(rightSideVerticalPanel);
		operationBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = operationBox.getSelectedIndex();
					switch (index) {
					case 0: //add
						forLabel.setText("at");
						srcLabel.setVisible(true);
						forLabel.setVisible(true);
						break;
					case 1: //dont install
						srcLabel.setVisible(false);
						forLabel.setVisible(false);
						break;
					case 2: //substitute
						forLabel.setText("for");
						srcLabel.setVisible(true);
						forLabel.setVisible(true);
						break;
					}

				}
			}
		});

		switch (af.getCondition()) {
		case AlternateFile.CONDITION_DLC_NOT_PRESENT:
			conditionBox.setSelectedIndex(0);
			break;
		case AlternateFile.CONDITION_DLC_PRESENT:
			conditionBox.setSelectedIndex(1);
			break;
		case AlternateFile.CONDITION_MANUAL:
			conditionBox.setSelectedIndex(2);
			break;
		}

		switch (af.getOperation()) {
		case AlternateFile.OPERATION_INSTALL:
			operationBox.setSelectedIndex(0);
			break;
		case AlternateFile.OPERATION_NOINSTALL:
			operationBox.setSelectedIndex(1);
			break;
		case AlternateFile.OPERATION_SUBSTITUTE:
			operationBox.setSelectedIndex(2);
			break;
		}

	}
}
