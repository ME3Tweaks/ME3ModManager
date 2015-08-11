package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;

public class PowerVariable {
	public static enum DataType {
		BOOLEAN, FLOAT, INTEGER, BASERANKUPGRADE, DETONATIONPARAMETERS
	}

	public static enum DetonationParams {
		_blockedbyobjects, _distancesorted, _impactdeadpawns, _impactfriends, _impactplaceables
	}

	public static enum DLCPackage {
		BASEGAME("$basegameBioGameElements"), MP1("$mp1BioGameElements"), MP2("$mp12BioGameElements"), MP3("$mp3BioGameElements"), MP4(
				"$mp4BioGameElements"), MP5("$mp5BioGameElements"), PATCH1("$patch1BioGameElements"), PATCH2("$patch2BioGameElements"), TESTPATCH(
				"$testpatchBioGameElements"), HEN_PR("$fromashesBioGameElements"), END("$extendedcutBiogameElements"), EXP1(
				"$leviathanBioGameElements"), EXP2("$omegaGameElements"), EXP3("$citadelBioGameElements"), EXP3B("$citadelbaseBioGameElements"), APP01(
				"$appearanceBioGameElements"), GUN01("$firefightBioGameElements"), GUN02("$groundsideBioGameElements");

		private final String text;

		/**
		 * @param text
		 */
		private DLCPackage(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return name();
		}

		public String getPHPVar() {
			return text;
		}
	}

	public static enum ValidationRule {
		GREATER_THAN_X, GREATER_THAN_OR_EQUAL_TO_X, GREATER_THAN_0, GREATER_THAN_OR_EQUAL_TO_0, ANY, LESS_THAN_X, LESS_THAN_OR_EQUAL_TO_X, LESS_THAN_0, LESS_THAN_OR_EQUAL_TO_0,
	}

	private String tableName;
	private DataType dataType;
	private String varName;
	private String sqlVarName;
	private int type;
	private DLCPackage dlcPackage;
	private String value;
	private BaseRankUpgrade bru;
	private DetonationParameters dp;
	private String sectionName;
	private String baseSectionName;

	private ArrayList<VariableRow> variableRows = new ArrayList<VariableRow>();
	private String humanVarName;
	private String defaultPrefix;
	private String defaultPostfix;
	private String inputHint;
	private String baseTableName;
	private boolean isNewVariable = false;

	public PowerVariable(DLCPackage dlcPackage, DetonationParameters dp, String baseSectionName) {
		this.sectionName = baseSectionName;
		this.baseSectionName = baseSectionName;
		this.baseTableName = GetTableName(baseSectionName);
		this.tableName = GetTableName(sectionName);
		if (dlcPackage == PowerVariable.DLCPackage.BASEGAME) {
			this.type = 2;
		} else {
			this.type = 3;
		}
		this.dataType = DataType.DETONATIONPARAMETERS;
		this.dlcPackage = dlcPackage;
		this.dp = dp;
		this.varName = "detonationparameters";
		this.sqlVarName = "detonationparameters";
		this.isNewVariable  = true;
	}
	
	/**
	 * Constructs a object representing the variable
	 * 
	 * @param dlcPackage
	 * @param isMPOnly
	 * 
	 * @param element
	 *            Element from the DOM
	 */
	public PowerVariable(DLCPackage dlcPackage, boolean isMPOnly, String baseSectionName, String sectionName, Element element) {
		this.sectionName = sectionName;
		this.baseSectionName = baseSectionName;
		this.baseTableName = GetTableName(baseSectionName);
		this.tableName = GetTableName(sectionName);
		this.type = Integer.parseInt(element.getAttribute("type"));
		this.dlcPackage = dlcPackage;
		varName = element.getAttribute("name");

		if (varName.equals("force")) {
			sqlVarName = "vforce";
		} else if (varName.equals("range")) {
			sqlVarName = "vrange";
		} else {
			sqlVarName = varName;
		}

		if (isMPOnly) {
			sqlVarName += "_mp";
		}

		String data = element.getTextContent();
		if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")) {
			// its a boolean.
			dataType = DataType.BOOLEAN;
			value = data;
			VariableRow r = new VariableRow();
			variableRows.add(r);
			return;
		} else if (DetonationParameters.isDetonationParameters(data)) {
			// detonation parameters
			dataType = DataType.DETONATIONPARAMETERS;
			dp = new DetonationParameters(tableName, data);
			// don't configure
			return;
		} else if (BaseRankUpgrade.isRankBonusUpgrade(data)) {
			// BRU
			dataType = DataType.BASERANKUPGRADE;
			bru = new BaseRankUpgrade(tableName, data);

			VariableRow r = new VariableRow(VariableRow.LOCKED_BASE_RANK);
			// r.configure(this, 0);
			variableRows.add(r);

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey() + 1; // human readable rank
				VariableRow brur = new VariableRow(rank - 1);
				// brur.configure(this, rank);
				variableRows.add(brur);
			}
			return;
		} else {
			try {
				Integer.parseInt(data);
				dataType = DataType.INTEGER;
				value = data;
				VariableRow r = new VariableRow();
				variableRows.add(r);
				return;
			} catch (NumberFormatException e) {

			}
			
			try {
				Double.parseDouble(data);
				dataType = DataType.FLOAT;
				value = data;
				VariableRow r = new VariableRow();
				variableRows.add(r);
				return;
			} catch (NumberFormatException e) {

			}

			if (bru == null && value == null && dp == null) {
				JOptionPane.showMessageDialog(null, "A variable failed to parse into one of the four data types: " + sqlVarName);
			}
		}
	}

	protected void configureRows() {
		for (VariableRow r : variableRows) {
			if (r.configured == false) {
				switch (dataType) {
				case FLOAT:
				case INTEGER:
					r.configure(this);
					break;
				case DETONATIONPARAMETERS:
					// does not need configuring
					break;
				case BASERANKUPGRADE:
					// should already be configured
					r.configure(this);
					break;
				case BOOLEAN:
					break;
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sectionName == null) ? 0 : sectionName.hashCode());
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PowerVariable other = (PowerVariable) obj;
		if (sectionName == null) {
			if (other.sectionName != null)
				return false;
		} else if (!sectionName.equals(other.sectionName))
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		return true;
	}

	public static DLCPackage BestGuessPackage(int type, String sectionName) {
		// look for MPs
		if (sectionName.contains("mp1")) {
			return DLCPackage.MP1;
		}
		if (sectionName.contains("mp2")) {
			return DLCPackage.MP2;
		}
		if (sectionName.contains("mp3")) {
			return DLCPackage.MP3;
		}
		if (sectionName.contains("mp4")) {
			return DLCPackage.MP4;
		}
		if (sectionName.contains("mp5")) {
			return DLCPackage.MP5;
		}
		// SPs EXP_003, etc

		// Base, but check for type, as it might be Patch2.
		if (type != 2) {
			return DLCPackage.PATCH2;
		}

		return DLCPackage.BASEGAME;
	}

	public static String GetTableName(String sectionName) {
		String str = sectionName.substring(sectionName.indexOf('.') + 1);
		if (str.equals("sfxpowercustomaction")) {
			return "sfxpowercustomaction_base";
		}

		str = str.replaceAll("sfxpowercustomaction", "");
		if (str.charAt(0) == '_') {
			return str.substring(1);
		} else {
			return str;
		}
	}

	/**
	 * Converts this property to a line of the SQL insert statement. Can
	 * generate multiple is property is a multi-sql property (bru, dp).
	 * 
	 * @return String to append to SQL INSERT
	 */
	public String convertToSQLInsert() {

		if (value != null) {
			String sqlVal = value;
			if (sqlVal.endsWith("f")) {
				sqlVal = sqlVal.substring(0, sqlVal.length() - 1);
			}
			if (sqlVal.endsWith(".")) {
				sqlVal = sqlVal.substring(0, sqlVal.length() - 1);
			}
			return "\t" + sqlVal + ", /*" + sqlVarName + "*/\n";
		}
		if (bru != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			if (bru.isDouble) {
				sb.append(bru.doubleBaseValue);
			} else {
				sb.append(bru.intBaseValue);
			}
			sb.append(", /*BASEVALUE of ");
			sb.append(sqlVarName);
			sb.append("*/\n");

			if (bru.formula != null) {
				sb.append("\t\"");
				sb.append(bru.formula);
				sb.append("\", /*");
				sb.append(varName);
				sb.append("_formula");
				sb.append("*/\n");
			}

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				double bonus = entry.getValue();
				int rank = entry.getKey();
				// double upgrade = entry.getValue();
				sb.append("\t");
				sb.append(bonus);
				sb.append(", /*");
				sb.append(varName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("*/\n");
			}
			return sb.toString();
		}
		if (dp != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append((dp.blockedByObjects) ? "1" : "0");
			sb.append(", /*");
			sb.append(varName);
			sb.append("_blockedbyobjects*/\n");
			sb.append("\t");
			sb.append((dp.distancedSorted) ? "1" : "0");
			sb.append(", /*");
			sb.append(varName);
			sb.append("_distancesorted*/\n");
			sb.append("\t");
			sb.append((dp.impactDeadPawns) ? "1" : "0");
			sb.append(", /*");
			sb.append(varName);
			sb.append("_impactdeadpawns*/\n");
			sb.append("\t");
			sb.append((dp.impactFriends) ? "1" : "0");
			sb.append(", /*");
			sb.append(varName);
			sb.append("_impactfriends*/\n");
			sb.append("\t");
			sb.append((dp.impactPlaceables) ? "1" : "0");
			sb.append(", /*");
			sb.append(varName);
			sb.append("_impactplaceables*/\n");
			if (dp.coneAngle >= 0) {
				sb.append("\t");
				sb.append(dp.coneAngle);
				sb.append(", /*");
				sb.append(varName);
				sb.append("_coneangle*/\n");
			}
			return sb.toString();
		}
		System.err.println("INVALID VALUES IN SQLINSERT GEN");
		return "\tALL DATA VALUES ARE NULL FOR SQL INSERT OF PROP " + varName + ".\n";
	}

	public String convertToSQLTable() {

		if (dataType == DataType.BOOLEAN) {
			// its a boolean.
			return "\t" + varName + " BOOLEAN NOT NULL,\n";
		}
		if (dp != null) {
			// add detonation params
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects BOOLEAN NOT NULL,\n");
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append("_distancesorted BOOLEAN NOT NULL,\n");
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns BOOLEAN NOT NULL,\n");
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append("_impactfriends BOOLEAN NOT NULL,\n");
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append("_impactplaceables BOOLEAN NOT NULL,\n");
			if (dp.coneAngle >= 0) {
				sb.append("\t");
				sb.append(sqlVarName);
				sb.append("_coneangle FLOAT NOT NULL,\n");
			}
			return sb.toString();
		}
		if (bru != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append(" ");
			for (VariableRow vr : variableRows) {
				if (vr.getSqlVarName().equals(sqlVarName)) {
					sb.append(vr.getDataTypeComboBox().getSelectedItem());
					break;
				}
			}
			sb.append(" NOT NULL,\n");
			if (bru.formula != null) {
				// put in formula
				sb.append("\t");
				sb.append(sqlVarName);
				sb.append("_formula VARCHAR(");
				sb.append(bru.formula.length());
				sb.append(") NOT NULL,\n");
			}
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				// double upgrade = entry.getValue();
				sb.append("\t");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);

				for (VariableRow vr : variableRows) {
					if (vr.getSqlVarName().equals(sqlVarName + "_rankbonus_" + rank)) {
						sb.append(" ");
						sb.append(vr.getDataTypeComboBox().getSelectedItem());
						break;
					}
				}
				sb.append(" NOT NULL, \n");
			}
			return sb.toString();
		}

		//int/dubs
		StringBuilder sb = new StringBuilder();
		for (VariableRow vr : variableRows) {
			sb.append("\t");
			sb.append(sqlVarName);
			sb.append(" ");
			sb.append(vr.getDataTypeComboBox().getSelectedItem());
			sb.append(" NOT NULL, \n");
			
		}
		return sb.toString();
	}

	public String convertToPublisherLine() {
		if (dp != null) {
			StringBuilder sb = new StringBuilder();

			sb.append("\t");
			sb.append("array_push(");
			sb.append(dlcPackage.getPHPVar());
			sb.append(", $this->createProperty(\"");
			sb.append(sectionName);
			sb.append("\", \"");
			sb.append(varName);
			// createDetonationParameters
			sb.append("\", $this->createDetonationParameters(");

			sb.append("$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects,");

			sb.append("$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_distancesorted,");

			sb.append("$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns,");

			sb.append("$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactfriends,");

			sb.append("$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactplaceables");

			if (dp.coneAngle >= 0) {
				sb.append(",$this->mod->powers->mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_coneangle.\"f\"");
			}
			sb.append("),");
			sb.append(type);
			if (isNewVariable) {
				sb.append(", \"addition\"");
			}
			sb.append("));\n");
			return sb.toString();
		}
		if (bru != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append("array_push(");
			sb.append(dlcPackage.getPHPVar());
			sb.append(", $this->createProperty(\"");
			sb.append(sectionName);
			sb.append("\", \"");
			sb.append(varName); // keep this, as its the proper one to publish
								// to.
			sb.append("\", \"(BaseValue=\".$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(".\"");
			if (bru.isDouble) {
				sb.append("f");
			}
			// add formula
			if (bru.formula != null) {
				sb.append(",Formula=");
				sb.append(bru.formula);
			}

			// add ranks
			// ,RankBonuses[0]=0,RankBonuses[1]=0.25f,RankBonuses[2]=0.25f,RankBonuses[3]=0
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				sb.append(",RankBonuses[");
				sb.append(rank);
				sb.append("]=\".$this->mod->powers->mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(".\"f");
			}

			sb.append(")\", ");
			sb.append(type);
			sb.append("));\n");
			return sb.toString();
		}

		try {
			Integer.parseInt(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append("array_push(");
			sb.append(dlcPackage.getPHPVar());
			sb.append(", $this->createProperty(\"");
			sb.append(sectionName);
			sb.append("\", \"");
			sb.append(varName);
			sb.append("\", $this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(", ");
			sb.append(type);
			sb.append("));\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			Double.parseDouble(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t");
			sb.append("array_push(");
			sb.append(dlcPackage.getPHPVar());
			sb.append(", $this->createProperty(\"");
			sb.append(sectionName);
			sb.append("\", \"");
			sb.append(varName);
			sb.append("\", $this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(".\"f\", ");
			sb.append(type);
			sb.append("));\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}
		System.err.println("Not publishing property: " + varName);

		return "";
	}

	public String convertToPHPEntryBox() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			String detblock = ContainerRow.DETONATION_CONTAINER_TEMPLATE;
			detblock = detblock.replaceAll("DETONATIONVARNAME", sqlVarName);
			detblock = detblock.replaceAll("TABLENAME", baseTableName);
			//add detonation params
			for (DetonationParams param : DetonationParams.values()) {
				String dparam = param.toString();
				switch (dparam) {
				case "_blockedbyobjects":
					detblock = detblock.replaceAll("BLOCKED_BY_OBJECTS", (dp.blockedByObjects) ? "True" : "False");
					break;
				case "_distancesorted":
					detblock = detblock.replaceAll("DISTANCE_SORTED", (dp.distancedSorted) ? "True" : "False");
					break;
				case "_impactdeadpawns":
					detblock = detblock.replaceAll("IMPACTS_DEAD_CHARS", (dp.impactDeadPawns) ? "True" : "False");
					break;
				case "_impactfriends":
					detblock = detblock.replaceAll("IMPACTS_FRIENDS", (dp.impactFriends) ? "True" : "False");
					break;
				case "_impactplaceables":
					detblock = detblock.replaceAll("IMPACTS_PLACEABLES", (dp.impactPlaceables) ? "True" : "False");
					break;
				default:
					System.err.println("UNKNOWN DETONATION PARAM: " + dparam);
				}
			}
			if (dp.coneAngle >= 0) {
				String inputTmp = VariableRow.ENTRY_TEMPLATE;
				inputTmp = inputTmp.replaceAll("VARNAME", sqlVarName + "_coneangle").replaceAll("TABLENAME", baseTableName);
				inputTmp = inputTmp + "\n";
				detblock = detblock.replaceAll("CONEANGLE", inputTmp);
			} else {
				detblock = detblock.replaceAll("CONEANGLE", "");
			}
			return detblock;
		}
		if (bru != null) {
			//BASE VALUE
			sb.append(VariableRow.ENTRY_TEMPLATE.replaceAll("VARNAME", sqlVarName).replaceAll("TABLENAME", baseTableName));
			//dont need to do formula
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				//RANKBONUSES
				sb.append(VariableRow.ENTRY_TEMPLATE.replaceAll("VARNAME", sqlVarName + "_rankbonus_" + rank).replaceAll("TABLENAME", baseTableName));
			}
			return sb.toString();
		}

		//ints, dubs
		try {
			int ints = Integer.parseInt(value);
			sb.append(VariableRow.ENTRY_TEMPLATE.replaceAll("VARNAME", sqlVarName).replaceAll("TABLENAME", baseTableName));
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			double dubs = Double.parseDouble(value);
			sb.append(VariableRow.ENTRY_TEMPLATE.replaceAll("VARNAME", sqlVarName).replaceAll("TABLENAME", baseTableName));
			return sb.toString();
		} catch (NumberFormatException e) {

		}
		System.err.println("Not generating input for: " + sqlVarName);
		return "";
	}

	public String convertToPHPValidation() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			// add detonation params
			for (DetonationParams param : DetonationParams.values()) {
				String dparam = param.toString();
				sb.append("\t\t//");
				sb.append(sqlVarName);
				sb.append(dparam);
				sb.append("\n");
				sb.append("\t\tif (isset($_POST['");
				sb.append(sqlVarName);
				sb.append(dparam);
				sb.append("'])) {\n");
				sb.append("\t\t\t$updateinfo['");
				sb.append(sqlVarName);
				sb.append(dparam);
				sb.append("'] = true;\n");
				sb.append("\t\t} else {\n");
				sb.append("\t\t\t$updateinfo['");
				sb.append(sqlVarName);
				sb.append(dparam);
				sb.append("'] = false;\n");
				sb.append("\t\t}\n\n");
			}

			// CONEANGLE
			if (dp.coneAngle >= 0) {
				sb.append("\t\t//");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("\n");

				sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("']);\n");

				sb.append("\t\tif (is_null($shouldadd)){\n");
				sb.append("\t\t\t$updateinfo['");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("'] = $_POST['");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("'];\n");

				sb.append("\t\t} else {\n");
				sb.append("\t\t\tarray_push($status, \"");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append(" \".$shouldadd);\n");
				sb.append("\t\t}\n\n");
			}
			return sb.toString();
		}

		for (VariableRow vr : variableRows) {
			sb.append(vr.convertToPHPValidation());
		}
		return sb.toString();
		/*
		 * if (bru != null) { StringBuilder sb = new StringBuilder(); for
		 * (VariableRow vr : variableRows) {
		 * sb.append(vr.convertToPHPValidation()); } // BASE VALUE
		 * sb.append("\t\t//"); sb.append(sqlVarName); sb.append("\n");
		 * 
		 * sb.append(
		 * "\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['"
		 * ); sb.append(sqlVarName); sb.append("']);\n");
		 * 
		 * sb.append("\t\tif (is_null($shouldadd)){\n");
		 * sb.append("\t\t\t$updateinfo['"); sb.append(sqlVarName);
		 * sb.append("'] = $_POST['"); sb.append(sqlVarName);
		 * sb.append("'];\n");
		 * 
		 * sb.append("\t\t} else {\n");
		 * sb.append("\t\t\tarray_push($status, \""); sb.append(sqlVarName);
		 * sb.append(" \".$shouldadd);\n"); sb.append("\t\t}\n\n");
		 * 
		 * for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
		 * int rank = entry.getKey(); // RANKBONUSES sb.append("\t\t//");
		 * sb.append(sqlVarName); sb.append("\n");
		 * 
		 * sb.append(
		 * "\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['"
		 * ); sb.append(sqlVarName); sb.append("_rankbonus_"); sb.append(rank);
		 * sb.append("']);\n");
		 * 
		 * sb.append("\t\tif (is_null($shouldadd)){\n");
		 * sb.append("\t\t\t$updateinfo['"); sb.append(sqlVarName);
		 * sb.append("_rankbonus_"); sb.append(rank);
		 * sb.append("'] = $_POST['"); sb.append(sqlVarName);
		 * sb.append("_rankbonus_"); sb.append(rank); sb.append("'];\n");
		 * 
		 * sb.append("\t\t} else {\n");
		 * sb.append("\t\t\tarray_push($status, \""); sb.append(sqlVarName);
		 * sb.append("_rankbonus_"); sb.append(rank);
		 * sb.append(" \".$shouldadd);\n"); sb.append("\t\t}\n\n"); } return
		 * sb.toString(); } // ints, dubs try { Integer.parseInt(value);
		 * StringBuilder sb = new StringBuilder(); sb.append("\t\t//");
		 * sb.append(sqlVarName); sb.append("\n");
		 * 
		 * sb.append(
		 * "\t\t$shouldadd = validate_greater_than_or_equal_to_zero_int($_POST['"
		 * ); sb.append(sqlVarName); sb.append("']);\n");
		 * 
		 * sb.append("\t\tif (is_null($shouldadd)){\n");
		 * sb.append("\t\t\t$updateinfo['"); sb.append(sqlVarName);
		 * 
		 * sb.append("'] = $_POST['"); sb.append(sqlVarName);
		 * sb.append("'];\n");
		 * 
		 * sb.append("\t\t} else {\n");
		 * sb.append("\t\t\tarray_push($status, \""); sb.append(sqlVarName);
		 * sb.append(" \".$shouldadd);\n"); sb.append("\t\t}\n\n"); return
		 * sb.toString(); } catch (NumberFormatException e) {
		 * 
		 * }
		 * 
		 * try { Double.parseDouble(value); StringBuilder sb = new
		 * StringBuilder(); sb.append("\t\t//"); sb.append(sqlVarName);
		 * sb.append("\n");
		 * 
		 * sb.append(
		 * "\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['"
		 * ); sb.append(sqlVarName); sb.append("']);\n");
		 * 
		 * sb.append("\t\tif (is_null($shouldadd)){\n");
		 * sb.append("\t\t\t$updateinfo['"); sb.append(sqlVarName);
		 * sb.append("'] = $_POST['"); sb.append(sqlVarName);
		 * sb.append("'];\n");
		 * 
		 * sb.append("\t\t} else {\n");
		 * sb.append("\t\t\tarray_push($status, \""); sb.append(sqlVarName);
		 * sb.append(" \".$shouldadd);\n"); sb.append("\t\t}\n\n"); return
		 * sb.toString(); } catch (NumberFormatException e) {
		 * 
		 * }
		 */
	}

	/**
	 * Creates the JS Validation rule for this property
	 * 
	 * @return
	 */
	public String convertToJSValidation() {
		if (dp != null) {
			StringBuilder sb = new StringBuilder();
			// DP is checkboxes
			if (dp.coneAngle >= 0) {
				sb.append("\t\t\t");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append(": {\n");
				sb.append("\t\t\t\trequired: true,\n");
				sb.append("\t\t\t\tmin: 0\n");
				sb.append("\t\t\t},\n");
			}
			return sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		for (VariableRow vr : variableRows) {
			sb.append(vr.convertToJSValidation());
		}
		return sb.toString();
	}

	/**
	 * Part of INSERT (binding vars)
	 * 
	 * @return
	 */
	public String convertToForkStage1() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			sb.append(":");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects, ");
			sb.append(":");
			sb.append(sqlVarName);
			sb.append("_distancesorted, ");
			sb.append(":");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns, ");
			sb.append(":");
			sb.append(sqlVarName);
			sb.append("_impactfriends, ");
			sb.append(":");
			sb.append(sqlVarName);
			sb.append("_impactplaceables, ");
			if (dp.coneAngle >= 0) {
				sb.append(":");
				sb.append(sqlVarName);
				sb.append("_coneangle, ");
			}
			return sb.toString();
		}
		if (bru != null) {
			sb.append(":");
			sb.append(sqlVarName);
			sb.append(", ");
			if (bru.formula != null) {
				sb.append(":");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append(", ");
			}
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
			    int rank = entry.getKey();
			    //double upgrade = entry.getValue();
			    sb.append(":");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(", ");
			}
			return sb.toString();
		}
		try {
			int ints = Integer.parseInt(value);
			sb.append(":");
			sb.append(sqlVarName);
			sb.append(", ");
			return sb.toString();
		} catch (NumberFormatException e) {
			
		}
		
		try {
			double dubs = Double.parseDouble(value);
			sb.append(":");
			sb.append(sqlVarName);
			sb.append(", ");
			return sb.toString();
		} catch (NumberFormatException e) {
			
		}
		System.err.println("not generating fork code for value: "+sqlVarName);
		return sb.toString();
	}

	/**
	 * PHP PDO Binding (var to var)
	 * 
	 * @return
	 */
	public String convertToForkStage2() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects");
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects']);\n");

			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("_distancesorted");
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("_distancesorted']);\n");

			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns");
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns']);\n");

			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("_impactfriends");
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("_impactfriends']);\n");

			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("_impactplaceables");
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("_impactplaceables']);\n");

			if (dp.coneAngle >= 0) {
				sb.append("\t$stmt->bindValue(\":");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("\", $");
				sb.append(baseTableName);
				sb.append("row['");
				sb.append(sqlVarName);
				sb.append("_coneangle']);\n");
			}
			return sb.toString();
		}
		if (bru != null) {
			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("']);\n");

			if (bru.formula != null) {
				sb.append("\t$stmt->bindValue(\":");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append("\", $");
				sb.append(baseTableName);
				sb.append("row['");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append("']);\n");
			}

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				// double upgrade = entry.getValue();

				sb.append("\t$stmt->bindValue(\":");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("\", $");
				sb.append(baseTableName);
				sb.append("row['");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("']);\n");
			}
			return sb.toString();
		}
		try {
			int ints = Integer.parseInt(value);
			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("']);\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			double dubs = Double.parseDouble(value);
			sb.append("\t$stmt->bindValue(\":");
			sb.append(sqlVarName);
			sb.append("\", $");
			sb.append(baseTableName);
			sb.append("row['");
			sb.append(sqlVarName);
			sb.append("']);\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		System.err.println("Failed to parse value for forking: " + sqlVarName);
		return "";
	}

	public String getTableName() {
		return tableName;
	}

	public String convertToModVar() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			// add detonation params

			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects = null;\n");

			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_distancesorted = null;\n");

			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns = null;\n");

			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactfriends = null;\n");

			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactplaceables = null;\n");

			if (dp.coneAngle >= 0) {
				sb.append("\tpublic $mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_coneangle = null;\n");
			}
			return sb.toString();
		}
		if (bru != null) {
			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = null;\n");

			// TODO: FORMULA
			if (bru.formula != null) {
				sb.append("\tpublic $mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append(" = null;\n");
			}
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				// double upgrade = entry.getValue();
				sb.append("\tpublic $mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(" = null;\n");
			}
			return sb.toString();

		}
		try {
			int ints = Integer.parseInt(value);
			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = null;\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			double dubs = Double.parseDouble(value);
			sb.append("\tpublic $mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = null;\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}
		System.out.println("Not generating variable for property: " + sqlVarName);
		return "";
	}

	public String convertToModLoad() {
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			// add detonation params
			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects");
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("_blockedbyobjects");
			sb.append("'];\n");

			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_distancesorted");
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("_distancesorted");
			sb.append("'];\n");

			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns");
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("_impactdeadpawns");
			sb.append("'];\n");

			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactfriends");
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("_impactfriends");
			sb.append("'];\n");

			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append("_impactplaceables");
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("_impactplaceables");
			sb.append("'];\n");

			if (dp.coneAngle >= 0) {
				sb.append("\t\t$this->mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append(" = $row['");
				sb.append(sqlVarName);
				sb.append("_coneangle");
				sb.append("'];\n");
			}
			return sb.toString();
		}
		if (bru != null) {
			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("'];\n");

			// formula
			if (bru.formula != null) {
				sb.append("\t\t$this->mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append(" = $row['");
				sb.append(sqlVarName);
				sb.append("_formula");
				sb.append("'];\n");
			}
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				// double upgrade = entry.getValue();

				sb.append("\t\t$this->mod_powers_");
				sb.append(baseTableName);
				sb.append("_");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(" = $row['");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("'];\n");
			}
			return sb.toString();
		}
		try {
			int ints = Integer.parseInt(value);
			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("'];\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			double dubs = Double.parseDouble(value);
			sb.append("\t\t$this->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(" = $row['");
			sb.append(sqlVarName);
			sb.append("'];\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}
		System.err.println("Unknown property data type: " + sqlVarName + " when generating mod load");
		return "";
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getSqlVarName() {
		return sqlVarName;
	}

	public void setSqlVarName(String sqlVarName) {
		this.sqlVarName = sqlVarName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public DLCPackage getDlcPackage() {
		return dlcPackage;
	}

	public void setDlcPackage(DLCPackage dlcPackage) {
		this.dlcPackage = dlcPackage;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public BaseRankUpgrade getBru() {
		return bru;
	}

	public void setBru(BaseRankUpgrade bru) {
		this.bru = bru;
	}

	public DetonationParameters getDp() {
		return dp;
	}

	public void setDp(DetonationParameters dp) {
		this.dp = dp;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getHumanVarName() {
		return humanVarName;
	}

	public void setHumanVarName(String humanVarName) {
		this.humanVarName = humanVarName;
	}

	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	public void setDefaultPrefix(String defaultPrefix) {
		this.defaultPrefix = defaultPrefix;
	}

	public String getDefaultPostfix() {
		return defaultPostfix;
	}

	public void setDefaultPostfix(String defaultPostfix) {
		this.defaultPostfix = defaultPostfix;
	}

	public String getInputHint() {
		return inputHint;
	}

	public void setInputHint(String inputHint) {
		this.inputHint = inputHint;
	}

	public void addContainerOption(ContainerRow cr) {
		if (dp == null) {
			// dp has its own containers, always
			for (VariableRow vr : variableRows) {
				if (vr.getContainerComboBox() != null) {
					vr.getContainerComboBox().addItem(cr);
				}
			}
		}
	}

	public void removeContainerOption(ContainerRow cr) {
		if (dp == null) {
			// dp has its own containers, always
			for (VariableRow vr : variableRows) {
				vr.getContainerComboBox().removeItem(cr);
			}
		}
	}

	public ArrayList<VariableRow> getVariableRows() {
		return variableRows;
	}

	public String getBaseTableName() {
		return baseTableName;
	}

	public String convertToBalance() {
		if (bru != null) {
			String retStr = "";
			retStr += "\t\t\t\t\t<li>" + sqlVarName + "</li>\n";
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				retStr += "\t\t\t\t\t<li>" + sqlVarName + "_rankbonus_" + rank + "</li>\n";
			}
			return retStr;
		} else {
			return "\t\t\t\t\t<li>" + sqlVarName + "</li>\n";
		}
	}

}
