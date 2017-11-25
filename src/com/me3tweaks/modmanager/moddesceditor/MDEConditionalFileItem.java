package com.me3tweaks.modmanager.moddesceditor;

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
	private static String[] conditionsHuman = { "is not installed", "is installed", "is selected by user"};

	private JComboBox<String> operationBox;
	private static String[] operations = { AlternateFile.OPERATION_INSTALL, AlternateFile.OPERATION_NOINSTALL, AlternateFile.OPERATION_SUBSTITUTE };
	private JButton minusButton;
	private JTextField descriptionField;

	public MDEConditionalFileItem(AlternateFile af) {
		this.af = af;
		setupPanel();
	}

	private void setupPanel() {
		panel = new JPanel(new HorizontalLayout());
		
		minusButton = new JButton("-");
		panel.add(minusButton);

		conditionBox = new JComboBox<String>(conditionsHuman);
		operationBox = new JComboBox<String>(operations);

		JLabel ifLabel = new JLabel("If");
		panel.add(ifLabel);
		JTextField conditionalDLC = new JTextField(af.getConditionalDLC());
		panel.add(conditionalDLC);
		panel.add(operationBox);
		panel.add(conditionBox);
		
		JLabel thenLabel = new JLabel("then");
		panel.add(thenLabel);

		panel.add(operationBox);
		
		JLabel srcLabel = new JLabel(af.getAltFile());
		panel.add(srcLabel);
		
		JLabel forLabel = new JLabel("for");
		panel.add(forLabel);
		
		JLabel destLabel = new JLabel(af.getModFile());
		panel.add(destLabel);

	}
}
