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
		BASEGAME("$basegameBioGameElements"), MP1("$mp1BioGameElements"), MP2("$mp12BioGameElements"), MP3("$mp3BioGameElements"), 
		MP4("$mp4BioGameElements"), MP5("$mp5BioGameElements"), PATCH1("$patch1BioGameElements"), PATCH2("$patch2BioGameElements"), 
		TESTPATCH("$testpatchBioGameElements"), HEN_PR("$fromashesBioGameElements"), END("$extendedcutBiogameElements"), EXP1("$leviathanBioGameElements"), 
		EXP2("$omegaGameElements"), EXP3("$citadelBioGameElements"), EXP3B("$citadelbaseBioGameElements"), APP01("$appearanceBioGameElements"), 
		GUN01("$firefightBioGameElements"), GUN02("$groundsideBioGameElements");
	    
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

	    public String getPHPVar(){
	    	return text;
	    }
	}

	public static enum ValidationRule {
		GREATER_THAN_X, GREATER_THAN_OR_EQUAL_TO_X, GREATER_THAN_0, GREATER_THAN_OR_EQUAL_TO_0, ANY, LESS_THAN_X, LESS_THAN_OR_EQUAL_TO_X, LESS_THAN_0, LESS_THAN_OR_EQUAL_TO_0,
	}

	public static String ENTRY_TEMPLATE = "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">VARNAME</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: PREFIX<\\?=\\$defaultsmod->powers->mod_powers_TABLENAME_VARNAME;?>POSTFIX</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"VARNAME\" class=\"short_input\" type=\"text\" name=\"VARNAME\" placeholder=\"VARNAME\" value=\"<?=\\$mod->powers->mod_powers_TABLENAME_VARNAME;?>\">\n"
			+ "\t\t\t\t\t</div>";
	public static String CONTAINER_TEMPLATE = "\t\t\t\t<!-- CONTAINERNAME -->\n" + "\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"
			+ "\t\t\t\t\t<img class=\"guide purple_card\" src=\"/images/common/no_image.png\">\n"
			+ "\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">CONTAINERNAME</h2>\n"
			+ "\t\t\t\t\t<p>These properties need to be moved to their proper boxes.</p>\n" + "INPUTS_PLACEHOLDER" + "\t\t\t\t</div>\n";
	public static String DETONATION_CONTAINER_TEMPLATE = "\t\t\t\t<!-- DETONATIONVARNAME PARAMETERS  -->\n"
			+ "\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"
			+ "\t\t\t\t\t<img class=\"guide hard\" src=\"/images/modmaker/powers/TABLENAME/explosion.jpg\">\n"
			+ "\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">Detonation Parameters</h2>\n"
			+ "\t\t\t\t\t<p>Detonation paramaters determine what gets hit when TABLENAME detonate.</p>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Blocked By Objects</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: BLOCKED_BY_OBJECTS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_blockedbyobjects\" type=\"checkbox\" name=\"DETONATIONVARNAME_blockedbyobjects\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_blockedbyobjects) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Distance Sorted</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: DISTANCE_SORTED</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_distancesorted\" type=\"checkbox\" name=\"DETONATIONVARNAME_distancesorted\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_distancesorted) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Dead Characters</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_DEAD_CHARS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactdeadpawns\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactdeadpawns\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactdeadpawns) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Friendlies</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_FRIENDS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactfriends\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactfriends\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactfriends) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Placeables</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_PLACEABLES</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactplaceables\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactplaceables\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactplaceables) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n" + "CONEANGLE" + "\t\t\t\t</div>";
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
	private boolean balanced = false;

	private ArrayList<VariableRow> variableRows = new ArrayList<VariableRow>();
	private String humanVarName;
	private String defaultPrefix;
	private String defaultPostfix;
	private String inputHint;
	private String baseTableName;

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

		if (dlcPackage == null) {
			balanced = true;
		}

		String data = element.getTextContent();
		if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")) {
			//its a boolean.
			dataType = DataType.BOOLEAN;
			value = data;
			VariableRow r = new VariableRow();
			r.configure(this);
			variableRows.add(r);
		} else if (DetonationParameters.isDetonationParameters(data)) {
			//detonation parameters
			dataType = DataType.DETONATIONPARAMETERS;
			dp = new DetonationParameters(tableName, data);
		} else if (BaseRankUpgrade.isRankBonusUpgrade(data)) {
			//BRU
			dataType = DataType.BASERANKUPGRADE;
			bru = new BaseRankUpgrade(tableName, data);

			VariableRow r = new VariableRow();
			r.configure(dlcPackage, sqlVarName, 0);
			variableRows.add(r);

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey() + 1; //human readable rank
				VariableRow brur = new VariableRow();
				brur.configure(dlcPackage, sqlVarName + "_rankbonus_" + (rank - 1), rank);
				variableRows.add(brur);
			}
		} else {
			try {
				Integer.parseInt(data);
				dataType = DataType.INTEGER;
				value = data;
				VariableRow r = new VariableRow();
				r.configure(this);
				variableRows.add(r);
			} catch (NumberFormatException e) {

			}
			try {
				Double.parseDouble(data);
				dataType = DataType.FLOAT;
				value = data;
				VariableRow r = new VariableRow();
				r.configure(this);
				variableRows.add(r);
			} catch (NumberFormatException e) {

			}

			if (bru == null && value == null && dp == null) {
				JOptionPane.showMessageDialog(null, "A variable failed to parse into one of the four data types: " + sqlVarName);
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
		//look for MPs
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
		//SPs EXP_003, etc

		//Base, but check for type, as it might be Patch2.
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
			sb.append(varName);
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
				//double upgrade = entry.getValue();
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
			//its a boolean.
			return "\t" + varName + " BOOLEAN NOT NULL,\n";
		}
		if (dp != null) {
			//add detonation params
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
			if (bru.isDouble) {
				sb.append("FLOAT");
			} else {
				sb.append("INT");
			}
			sb.append(" NOT NULL,\n");
			if (bru.formula != null) {
				//put in formula
				sb.append("\t");
				sb.append(sqlVarName);
				sb.append("_formula VARCHAR(");
				sb.append(bru.formula.length());
				sb.append(") NOT NULL,\n");
			}
			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				//double upgrade = entry.getValue();
				sb.append("\t");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(" FLOAT NOT NULL, \n");
			}
			return sb.toString();
		}
		try {
			Integer.parseInt(value);
			return "\t" + varName + " INT NOT NULL,\n";
		} catch (NumberFormatException e) {

		}

		try {
			Double.parseDouble(value);
			return "\t" + varName + " FLOAT NOT NULL,\n";
		} catch (NumberFormatException e) {

		}
		return "Unknown data type (TABLE) for power variable: " + varName;

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
			//createDetonationParameters
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
			sb.append(varName); //keep this, as its the proper one to publish to.
			sb.append("\", \"(BaseValue=\".$this->mod->powers->mod_powers_");
			sb.append(baseTableName);
			sb.append("_");
			sb.append(sqlVarName);
			sb.append(".\"");
			if (bru.isDouble) {
				sb.append("f");
			}
			//add formula
			if (bru.formula != null) {
				sb.append(",Formula=");
				sb.append(bru.formula);
			}

			//add ranks
			//,RankBonuses[0]=0,RankBonuses[1]=0.25f,RankBonuses[2]=0.25f,RankBonuses[3]=0
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
		String inputBox = ENTRY_TEMPLATE;
		inputBox = inputBox.replaceAll("VARNAME", sqlVarName);
		inputBox = inputBox.replaceAll("TABLENAME", tableName);
		if (defaultPrefix != null) {
			inputBox = inputBox.replaceAll("PREFIX", defaultPrefix);
		}
		if (defaultPostfix != null) {
			inputBox = inputBox.replaceAll("POSTFIX", defaultPostfix);
		}
		return inputBox;
	}

	public String convertToPHPValidation() {
		if (dp != null) {
			StringBuilder sb = new StringBuilder();
			//add detonation params
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

			//CONEANGLE
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
		if (bru != null) {
			StringBuilder sb = new StringBuilder();
			//BASE VALUE
			sb.append("\t\t//");
			sb.append(sqlVarName);
			sb.append("\n");

			sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['");
			sb.append(sqlVarName);
			sb.append("']);\n");

			sb.append("\t\tif (is_null($shouldadd)){\n");
			sb.append("\t\t\t$updateinfo['");
			sb.append(sqlVarName);
			sb.append("'] = $_POST['");
			sb.append(sqlVarName);
			sb.append("'];\n");

			sb.append("\t\t} else {\n");
			sb.append("\t\t\tarray_push($status, \"");
			sb.append(sqlVarName);
			sb.append(" \".$shouldadd);\n");
			sb.append("\t\t}\n\n");

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				//RANKBONUSES
				sb.append("\t\t//");
				sb.append(sqlVarName);
				sb.append("\n");

				sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("']);\n");

				sb.append("\t\tif (is_null($shouldadd)){\n");
				sb.append("\t\t\t$updateinfo['");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("'] = $_POST['");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append("'];\n");

				sb.append("\t\t} else {\n");
				sb.append("\t\t\tarray_push($status, \"");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(" \".$shouldadd);\n");
				sb.append("\t\t}\n\n");
			}
			return sb.toString();
		}
		//ints, dubs
		try {
			Integer.parseInt(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t\t//");
			sb.append(sqlVarName);
			sb.append("\n");

			sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_int($_POST['");
			sb.append(sqlVarName);
			sb.append("']);\n");

			sb.append("\t\tif (is_null($shouldadd)){\n");
			sb.append("\t\t\t$updateinfo['");
			sb.append(sqlVarName);

			sb.append("'] = $_POST['");
			sb.append(sqlVarName);
			sb.append("'];\n");

			sb.append("\t\t} else {\n");
			sb.append("\t\t\tarray_push($status, \"");
			sb.append(sqlVarName);
			sb.append(" \".$shouldadd);\n");
			sb.append("\t\t}\n\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			Double.parseDouble(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t\t//");
			sb.append(sqlVarName);
			sb.append("\n");

			sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['");
			sb.append(sqlVarName);
			sb.append("']);\n");

			sb.append("\t\tif (is_null($shouldadd)){\n");
			sb.append("\t\t\t$updateinfo['");
			sb.append(sqlVarName);
			sb.append("'] = $_POST['");
			sb.append(sqlVarName);
			sb.append("'];\n");

			sb.append("\t\t} else {\n");
			sb.append("\t\t\tarray_push($status, \"");
			sb.append(sqlVarName);
			sb.append(" \".$shouldadd);\n");
			sb.append("\t\t}\n\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}
		return "UNKNOWN PHP VALIDATION PROP VAR TYPE: " + varName;
	}

	/**
	 * Creates the JS Validation rule for this property
	 * 
	 * @return
	 */
	public String convertToJSValidation() {
		if (dp != null) {
			StringBuilder sb = new StringBuilder();
			//DP is checkboxes
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
		if (bru != null) {
			StringBuilder sb = new StringBuilder();
			//BASE VALUE
			sb.append("\t\t\t");
			sb.append(sqlVarName);
			sb.append(": {\n");
			sb.append("\t\t\t\trequired: true,\n");
			sb.append("\t\t\t\tmin: 0\n");
			sb.append("\t\t\t},\n");

			for (Map.Entry<Integer, Double> entry : bru.rankBonuses.entrySet()) {
				int rank = entry.getKey();
				//RANKBONUSES
				sb.append("\t\t\t");
				sb.append(sqlVarName);
				sb.append("_rankbonus_");
				sb.append(rank);
				sb.append(": {\n");
				sb.append("\t\t\t\trequired: true,\n");
				sb.append("\t\t\t\tmin: 0\n");
				sb.append("\t\t\t},\n");
			}
			return sb.toString();
		}
		//ints, dubs
		try {
			int ints = Integer.parseInt(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t\t\t");
			sb.append(sqlVarName);
			sb.append(": {\n");
			sb.append("\t\t\t\trequired: true,\n");
			sb.append("\t\t\t\tdigits: true,\n");
			sb.append("\t\t\t\tmin: 0\n");
			sb.append("\t\t\t},\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		try {
			Double.parseDouble(value);
			StringBuilder sb = new StringBuilder();
			sb.append("\t\t\t");
			sb.append(sqlVarName);
			sb.append(": {\n");
			sb.append("\t\t\t\trequired: true,\n");
			sb.append("\t\t\t\tmin: 0\n");
			sb.append("\t\t\t},\n");
			return sb.toString();
		} catch (NumberFormatException e) {

		}

		return "JS VALIDATION: UNKNOWN PROP VAR TYPE: " + sqlVarName;
	}

	/**
	 * Part of INSERT (binding vars)
	 * 
	 * @return
	 */
	public String convertToForkStage1() {
		return ":" + sqlVarName;
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
				//double upgrade = entry.getValue();

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
		
		System.err.println("Failed to parse value for forking: "+sqlVarName);
		return "";
	}

	public String getTableName() {
		return tableName;
	}
	
	public String convertToModVar(){
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			//add detonation params

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

			//TODO: FORMULA
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
				//double upgrade = entry.getValue();
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
	
	public String convertToModLoad(){
		StringBuilder sb = new StringBuilder();
		if (dp != null) {
			//add detonation params
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

			//formula
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
				//double upgrade = entry.getValue();

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
		System.err.println("Unknown property data type: "+sqlVarName+" when generating mod load");
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
			//dp has its own containers, always
			for (VariableRow vr : variableRows) {
				vr.getContainerComboBox().addItem(cr);
			}
		}
	}

	public void removeContainerOption(ContainerRow cr) {
		if (dp == null) {
			//dp has its own containers, always
			for (VariableRow vr : variableRows) {
				vr.getContainerComboBox().removeItem(cr);
			}
		}
	}

	public ArrayList<VariableRow> getVariableRows() {
		return variableRows;
	}

}
