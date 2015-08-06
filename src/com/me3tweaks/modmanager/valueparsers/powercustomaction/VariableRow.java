package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.me3tweaks.modmanager.cellrenderers.HintTextFieldUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerVariable.DLCPackage;

public class VariableRow extends JPanel {
	private JTextField humanName, prefix, postfix;
	private JComboBox<ContainerRow> containerComboBox;
	private DefaultComboBoxModel<ContainerRow> containerModel;
	private JComboBox<String> rankComboBox = new JComboBox<String>();
	private static String[] ranks = new String[] { "NONE", "1", "2", "3", "4A", "4B", "5A", "5B", "6A", "6B" };
	private boolean forcedRank = false;
	private JComboBox<String> dataTypeComboBox;

	/**
	 * Loads the variable and instantiates the interface
	 */
	public void configure(PowerVariable variable) {
		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("[" + variable.getDlcPackage() + "] " + variable.getSqlVarName());
		//label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);

		humanName = new JTextField(20);
		humanName.setUI(new HintTextFieldUI("Human Name"));
		humanName.setToolTipText("Human-readable name describing the property");
		add(humanName);

		rankComboBox = new JComboBox<String>(ranks);
		rankComboBox.setPreferredSize(new Dimension(60, 20));
		rankComboBox.setToolTipText("Upgrade rank this variable applies to");
		add(rankComboBox);

		prefix = new JTextField(3);
		prefix.setUI(new HintTextFieldUI("Pre"));
		prefix.setToolTipText("Prefix part of Default: PREFIX[VAL]POSTFIX label on an input");
		add(prefix);

		postfix = new JTextField(20);
		postfix.setUI(new HintTextFieldUI("Default Postfix"));
		postfix.setToolTipText("Postfix part of Default: PREFIX[VAL]POSTFIX label on an input");
		add(postfix);

		containerModel = new DefaultComboBoxModel<ContainerRow>();
		containerComboBox = new JComboBox<ContainerRow>();
		containerComboBox.setModel(containerModel);
		containerComboBox.setPreferredSize(new Dimension(100, 20));
		containerComboBox.setToolTipText("Container this property will be placed into on the page");
		add(containerComboBox);
		
		if (variable.getDataType() == PowerVariable.DataType.FLOAT || variable.getDataType() == PowerVariable.DataType.INTEGER){
			//allow change
			dataTypeComboBox = new JComboBox<String>(new String[]{"INT", "FLOAT"});
			dataTypeComboBox.setToolTipText("Specify this value must be an integer (2,3,4), or allows decimals (2.5, 1.2)");
			dataTypeComboBox.setSelectedItem((variable.getDataType() == PowerVariable.DataType.INTEGER) ? "INT" : "FLOAT");
			add(dataTypeComboBox);
		}
	}

	public void configure(DLCPackage dlcPackage, String variableName, int lockedRank) {
		forcedRank = true;

		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("[" + dlcPackage + "] " + variableName);
		//label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);

		humanName = new JTextField(20);
		humanName.setUI(new HintTextFieldUI("Human Name"));
		humanName.setToolTipText("Human-readable name describing the property");
		add(humanName);

		rankComboBox = new JComboBox<String>(ranks);
		rankComboBox.setPreferredSize(new Dimension(60, 20));
		rankComboBox.setToolTipText("Upgrade rank this variable applies to. Select NONE if this power has the variable take effect if you simply spec into it.");
		if (lockedRank > 0 && !variableName.contains("evolve")) {
			rankComboBox.setSelectedItem(Integer.toString(lockedRank));
			rankComboBox.setEnabled(false);
		} else if (lockedRank == 0 && !variableName.contains("evolve")){
			rankComboBox.setSelectedItem("NONE");
			rankComboBox.setEnabled(false);
		} else {
			rankComboBox.setSelectedItem("NONE");
		}
		add(rankComboBox);

		prefix = new JTextField(3);
		prefix.setUI(new HintTextFieldUI("Pre"));
		prefix.setToolTipText("Prefix part of Default: PREFIX[VAL]POSTFIX label on an input");
		add(prefix);

		postfix = new JTextField(20);
		postfix.setUI(new HintTextFieldUI("Default Postfix"));
		postfix.setToolTipText("Postfix part of Default: PREFIX[VAL]POSTFIX label on an input");
		add(postfix);

		containerModel = new DefaultComboBoxModel<ContainerRow>();
		containerComboBox = new JComboBox<ContainerRow>();
		containerComboBox.setModel(containerModel);
		containerComboBox.setPreferredSize(new Dimension(100, 20));
		containerComboBox.setToolTipText("Container this property will be placed into on the page");
		add(containerComboBox);
	}

	public JComboBox<ContainerRow> getContainerComboBox() {
		return containerComboBox;
	}

	public void setContainerComboBox(JComboBox<ContainerRow> containerComboBox) {
		this.containerComboBox = containerComboBox;
	}

	public DefaultComboBoxModel<ContainerRow> getContainerModel() {
		return containerModel;
	}

}
