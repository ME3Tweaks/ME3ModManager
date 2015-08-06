package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.ref.WeakReference;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
	private JTextField hint;
	private WeakReference<PowerVariable> owningVar;
	boolean configured = false;
	int lockedRank = -1;

	public VariableRow() {

	}

	public VariableRow(int i) {System.out.println("Row locked to rank "+i);
		this.lockedRank = i;
	}

	/**
	 * Loads the variable and instantiates the interface
	 */
	public void configure(PowerVariable variable) {
		this.owningVar = new WeakReference<PowerVariable>(variable);
		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		String labelText = null;
		if (lockedRank >= 0) {
			labelText = "[" + variable.getDlcPackage() + "] " + variable.getSqlVarName() + "_rankbonus_" + lockedRank;
		} else {
			labelText = "[" + variable.getDlcPackage() + "] " + variable.getSqlVarName();
		}
		// label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(new JLabel(labelText));

		humanName = new JTextField(20);
		humanName.setUI(new HintTextFieldUI("Human Name"));
		humanName.setToolTipText("Human-readable name describing the property");
		add(humanName);

		hint = new JTextField(10);
		hint.setUI(new HintTextFieldUI("Hint"));
		hint.setToolTipText("Shadow text to show when the entry field is empty");
		
		rankComboBox = new JComboBox<String>(ranks);
		rankComboBox.setPreferredSize(new Dimension(60, 20));
		rankComboBox
				.setToolTipText("Upgrade rank this variable applies to. Select NONE if this power has the variable take effect if you simply spec into it.");
		if (lockedRank > 0 && !variable.getSqlVarName().contains("evolve")) {
			rankComboBox.setSelectedItem(Integer.toString(lockedRank + 1));
			String hintText = "Rank " + (lockedRank + 1) + " Bonus";
			switch (PowerCustomActionGUI2.getVarApplicationAreas(variable)) {
			case PowerCustomActionGUI2.SP_VAR_ONLY:
				hintText += " SP";
				break;
			case PowerCustomActionGUI2.MP_VAR_ONLY:
				hintText += " MP";
				break;
			}
			hint.setText(hintText);
			rankComboBox.setEnabled(false);
		} else if (lockedRank == 0 && !variable.getSqlVarName().contains("evolve")) {
			rankComboBox.setSelectedItem("NONE");
			rankComboBox.setEnabled(false);
			String hintText = "BASEVALUE " + (lockedRank + 1) + " ";
			switch (PowerCustomActionGUI2.getVarApplicationAreas(variable)) {
			case PowerCustomActionGUI2.SP_VAR_ONLY:
				hintText += " (SP)";
				break;
			case PowerCustomActionGUI2.MP_VAR_ONLY:
				hintText += " (MP)";
				break;
			}
			hint.setText(hintText);
			
			
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

		add(hint);

		containerModel = new DefaultComboBoxModel<ContainerRow>();
		containerComboBox = new JComboBox<ContainerRow>();
		containerComboBox.setModel(containerModel);
		containerComboBox.setPreferredSize(new Dimension(100, 20));
		containerComboBox.setToolTipText("Container this property will be placed into on the page");
		add(containerComboBox);

		if (variable.getDataType() == PowerVariable.DataType.FLOAT || variable.getDataType() == PowerVariable.DataType.INTEGER) {
			// allow change
			dataTypeComboBox = new JComboBox<String>(new String[] { "INT", "FLOAT" });
			dataTypeComboBox.setToolTipText("Specify this value must be an integer (2,3,4), or allows decimals (2.5, 1.2)");
			dataTypeComboBox.setSelectedItem((variable.getDataType() == PowerVariable.DataType.INTEGER) ? "INT" : "FLOAT");
			add(dataTypeComboBox);
		}

		configured = true;
	}

/*	public void configure(PowerVariable owningVar, int lockedRank) {
		this.owningVar = new WeakReference<PowerVariable>(owningVar);
		forcedRank = true;

		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		// label.setAlignmentX(Component.LEFT_ALIGNMENT);
		//add(label);

		humanName = new JTextField(20);
		humanName.setUI(new HintTextFieldUI("Human Name"));
		humanName.setToolTipText("Human-readable name describing the property");
		add(humanName);

		hint = new JTextField(10);
		hint.setUI(new HintTextFieldUI("Hint"));
		hint.setToolTipText("Shadow text to show when the entry field is empty");

		rankComboBox = new JComboBox<String>(ranks);
		rankComboBox.setPreferredSize(new Dimension(60, 20));
		rankComboBox
				.setToolTipText("Upgrade rank this variable applies to. Select NONE if this power has the variable take effect if you simply spec into it.");
		if (lockedRank > 0 && !owningVar.getSqlVarName().contains("evolve")) {
			rankComboBox.setSelectedItem(Integer.toString(lockedRank));
			String hintText = "Rank " + lockedRank + " Bonus";
			switch (PowerCustomActionGUI2.getVarApplicationAreas(owningVar)) {
			case PowerCustomActionGUI2.SP_VAR_ONLY:
				hintText += " (SP)";
				break;
			case PowerCustomActionGUI2.MP_VAR_ONLY:
				hintText += " (MP)";
				break;
			}
			hint.setText(hintText);
			rankComboBox.setEnabled(false);
		} else if (lockedRank == 0 && !owningVar.getSqlVarName().contains("evolve")) {
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

		add(hint);

		containerModel = new DefaultComboBoxModel<ContainerRow>();
		containerComboBox = new JComboBox<ContainerRow>();
		containerComboBox.setModel(containerModel);
		containerComboBox.setPreferredSize(new Dimension(100, 20));
		containerComboBox.setToolTipText("Container this property will be placed into on the page");
		add(containerComboBox);

		configured = true;
	}*/

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
