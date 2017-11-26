package com.me3tweaks.modmanager.moddesceditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.HorizontalLayout;

import com.me3tweaks.modmanager.objects.AlternateFile;

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
	private static int itemSpacing = 5;

	public MDEConditionalFileItem(AlternateFile af) {
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

		JLabel srcLabel = new JLabel(af.getAltFile());
		Component srcSpace = Box.createRigidArea(new Dimension(itemSpacing, 3));
		panel.add(srcSpace);
		panel.add(srcLabel);

		JLabel forLabel = new JLabel("for");
		Component forSpace = Box.createRigidArea(new Dimension(itemSpacing, 3));
		panel.add(forSpace);
		panel.add(forLabel);

		JLabel destLabel = new JLabel(af.getModFile());
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, 3)));
		panel.add(destLabel);

		operationBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = operationBox.getSelectedIndex();
					switch (index) {
					case 0: //add
						srcSpace.setVisible(true);
						forSpace.setVisible(true);
						srcLabel.setVisible(true);
						forLabel.setVisible(true);
						break;
					case 1: //dont install
						srcSpace.setVisible(false);
						forSpace.setVisible(false);
						srcLabel.setVisible(false);
						forLabel.setVisible(false);
						break;
					case 2: //substitute
						srcSpace.setVisible(true);
						forSpace.setVisible(true);
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
