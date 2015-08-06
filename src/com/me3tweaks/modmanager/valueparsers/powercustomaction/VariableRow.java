package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.ref.WeakReference;
import java.util.Map;

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
	private String sqlVarName;
	boolean configured = false;
	protected static int OPEN_RANK = -2;
	protected static int LOCKED_BASE_RANK = -1;
	int lockedRank = OPEN_RANK;
	public static String ENTRY_TEMPLATE = "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">HUMANNAME</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: PREFIX<\\?=\\$defaultsmod->powers->mod_powers_TABLENAME_VARNAME;?>POSTFIX</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"VARNAME\" class=\"short_input\" type=\"text\" name=\"VARNAME\" placeholder=\"HINTTEXT\" value=\"<?=\\$mod->powers->mod_powers_TABLENAME_VARNAME;?>\">\n"
			+ "\t\t\t\t\t</div>\n";
	public static String PHPVALIDATION_TEMPLATE = "\t\t//VARNAME\n"
			+ "\t\t$shouldadd = validate_greater_than_or_equal_to_zero_DATATYPE($_POST['VARNAME']);\n" + "\t\tif (is_null($shouldadd)){\n"
			+ "\t\t\t$updateinfo['VARNAME'] = $_POST['VARNAME'];\n" + "\t\t} else {\n" + "\t\t\tarray_push($status, \"HUMANNAME \".$shouldadd);\n"
			+ "\t\t}\n\n";

	public VariableRow() {

	}

	public VariableRow(int i) {
		System.out.println("Row locked to rank " + i);
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
			sqlVarName = variable.getSqlVarName() + "_rankbonus_" + lockedRank;
			labelText = "[" + variable.getDlcPackage() + "] " + sqlVarName;
		} else {
			sqlVarName = variable.getSqlVarName();
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
		if (lockedRank > LOCKED_BASE_RANK && !variable.getSqlVarName().contains("evolve")) {
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
		} else if (lockedRank == LOCKED_BASE_RANK && !variable.getSqlVarName().contains("evolve")) {
			rankComboBox.setSelectedItem("NONE");
			rankComboBox.setEnabled(false);
			String hintText = "BASEVALUE";
			switch (PowerCustomActionGUI2.getVarApplicationAreas(variable)) {
			case PowerCustomActionGUI2.SP_VAR_ONLY:
				hintText += " SP";
				break;
			case PowerCustomActionGUI2.MP_VAR_ONLY:
				hintText += " MP";
				break;
			}
			hint.setText(hintText);
		} else if (variable.getSqlVarName().contains("evolve")) {

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

		if (variable.getDataType() == PowerVariable.DataType.FLOAT || variable.getDataType() == PowerVariable.DataType.INTEGER || variable.getDataType() == PowerVariable.DataType.BASERANKUPGRADE) {
			// allow change
			dataTypeComboBox = new JComboBox<String>(new String[] { "INT", "FLOAT" });
			dataTypeComboBox.setToolTipText("Specify this value must be an integer (2,3,4), or allows decimals (2.5, 1.2)");
			dataTypeComboBox.setSelectedItem((variable.getDataType() == PowerVariable.DataType.INTEGER) ? "INT" : "FLOAT");
			add(dataTypeComboBox);
		}

		configured = true;
	}

	/*
	 * public void configure(PowerVariable owningVar, int lockedRank) {
	 * this.owningVar = new WeakReference<PowerVariable>(owningVar); forcedRank
	 * = true;
	 * 
	 * setBorder(new EtchedBorder()); setLayout(new
	 * FlowLayout(FlowLayout.LEFT)); //
	 * label.setAlignmentX(Component.LEFT_ALIGNMENT); //add(label);
	 * 
	 * humanName = new JTextField(20); humanName.setUI(new
	 * HintTextFieldUI("Human Name"));
	 * humanName.setToolTipText("Human-readable name describing the property");
	 * add(humanName);
	 * 
	 * hint = new JTextField(10); hint.setUI(new HintTextFieldUI("Hint"));
	 * hint.setToolTipText("Shadow text to show when the entry field is empty");
	 * 
	 * rankComboBox = new JComboBox<String>(ranks);
	 * rankComboBox.setPreferredSize(new Dimension(60, 20)); rankComboBox
	 * .setToolTipText(
	 * "Upgrade rank this variable applies to. Select NONE if this power has the variable take effect if you simply spec into it."
	 * ); if (lockedRank > 0 && !owningVar.getSqlVarName().contains("evolve")) {
	 * rankComboBox.setSelectedItem(Integer.toString(lockedRank)); String
	 * hintText = "Rank " + lockedRank + " Bonus"; switch
	 * (PowerCustomActionGUI2.getVarApplicationAreas(owningVar)) { case
	 * PowerCustomActionGUI2.SP_VAR_ONLY: hintText += " (SP)"; break; case
	 * PowerCustomActionGUI2.MP_VAR_ONLY: hintText += " (MP)"; break; }
	 * hint.setText(hintText); rankComboBox.setEnabled(false); } else if
	 * (lockedRank == 0 && !owningVar.getSqlVarName().contains("evolve")) {
	 * rankComboBox.setSelectedItem("NONE"); rankComboBox.setEnabled(false); }
	 * else { rankComboBox.setSelectedItem("NONE"); } add(rankComboBox);
	 * 
	 * prefix = new JTextField(3); prefix.setUI(new HintTextFieldUI("Pre"));
	 * prefix.setToolTipText(
	 * "Prefix part of Default: PREFIX[VAL]POSTFIX label on an input");
	 * add(prefix);
	 * 
	 * postfix = new JTextField(20); postfix.setUI(new
	 * HintTextFieldUI("Default Postfix")); postfix.setToolTipText(
	 * "Postfix part of Default: PREFIX[VAL]POSTFIX label on an input");
	 * add(postfix);
	 * 
	 * add(hint);
	 * 
	 * containerModel = new DefaultComboBoxModel<ContainerRow>();
	 * containerComboBox = new JComboBox<ContainerRow>();
	 * containerComboBox.setModel(containerModel);
	 * containerComboBox.setPreferredSize(new Dimension(100, 20));
	 * containerComboBox
	 * .setToolTipText("Container this property will be placed into on the page"
	 * ); add(containerComboBox);
	 * 
	 * configured = true; }
	 */

	public JComboBox<String> getDataTypeComboBox() {
		return dataTypeComboBox;
	}

	public JComboBox<ContainerRow> getContainerComboBox() {
		return containerComboBox;
	}

	public JTextField getPrefix() {
		return prefix;
	}

	public JTextField getPostfix() {
		return postfix;
	}

	public JComboBox<String> getRankComboBox() {
		return rankComboBox;
	}

	public JTextField getHint() {
		return hint;
	}

	public WeakReference<PowerVariable> getOwningVar() {
		return owningVar;
	}

	public void setContainerComboBox(JComboBox<ContainerRow> containerComboBox) {
		this.containerComboBox = containerComboBox;
	}

	public DefaultComboBoxModel<ContainerRow> getContainerModel() {
		return containerModel;
	}

	public String getSqlVarName() {
		return sqlVarName;
	}

	public String getHumanName() {
		return humanName.getText();
	}

	public String convertToPHPValidation() {
		String validationTemplate = PHPVALIDATION_TEMPLATE;
		validationTemplate = validationTemplate.replaceAll("VARNAME", sqlVarName);
		validationTemplate = validationTemplate.replaceAll("HUMANNAME", humanName.getText());
		if (dataTypeComboBox != null) {
			validationTemplate = validationTemplate.replaceAll("DATATYPE", (((String) dataTypeComboBox.getSelectedItem()).equals("INT") ? "int"
					: "float"));
		} else {
			validationTemplate = validationTemplate.replaceAll("DATATYPE", "float");
		}

		return validationTemplate;
	}

	public String convertToJSValidation() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t");
		sb.append(sqlVarName);
		sb.append(": {\n");
		sb.append("\t\t\t\trequired: true,\n");
		sb.append("\t\t\t\tmin: 0");
		if (dataTypeComboBox != null && dataTypeComboBox.getSelectedItem().equals("INT")) {
			sb.append(",\n\t\t\t\tdigits: true\n");
		} else {
			sb.append("\n");
		}
		sb.append("\t\t\t},\n");
		return sb.toString();
	}

}
