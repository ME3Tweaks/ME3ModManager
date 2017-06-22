package com.me3tweaks.modmanager.valueparsers.biodifficulty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.me3tweaks.modmanager.valueparsers.powercustomaction.ContainerRow;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerVariable;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.VariableRow;

public class DifficultyGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	boolean closeOnExit = false;
	JTextField enemy;
	JButton submit, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generateUpdate, copy;
	JComboBox<String> difficultylist;
	String[] difficulties = { "Narrative", "Casual", "Normal", "Hardcore", "Insanity", "Bronze", "Silver", "Gold", "Platinum" };
	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private JButton generatePublisherStatsList, generateHTML;

	public static void main(String[] args) throws IOException {
		new DifficultyGUI();
	}

	public DifficultyGUI() {
		this.setTitle("ME3CMM Biodifficulty Parser Tool");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.setMinimumSize(new Dimension(490, 500));
		this.setPreferredSize(new Dimension(490, 500));
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		JPanel wavelistGUI = new JPanel(new BorderLayout());
		JLabel instructionsLabel = new JLabel(
				"<html>ME3CMM Biodifficulty Parser<br>Enter the Biodifficulty text below and press parse to view easily readable information.</html>");
		wavelistGUI.add(instructionsLabel, BorderLayout.NORTH);
		instructionsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel inputPanel = new JPanel(new BorderLayout());
		input = new JTextArea(6, 45);
		input.setMinimumSize(new Dimension(50, 120));
		input.setLineWrap(true);
		input.setWrapStyleWord(false);
		//inputscroll
		JScrollPane inputScrollPane = new JScrollPane(input);
		inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		inputScrollPane.setPreferredSize(new Dimension(250, 250));

		submit = new JButton("Parse");
		submit.addActionListener(this);

		// sql stuff
		JPanel SQLPanel = new JPanel(new BorderLayout());
		difficultylist = new JComboBox<String>(difficulties);
		generateTable = new JButton("Generate TBL");
		generateTable.addActionListener(this);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		SQLPanel.add(difficultylist, BorderLayout.NORTH);
		SQLPanel.add(generateTable, BorderLayout.CENTER);
		SQLPanel.add(generateInsert, BorderLayout.SOUTH);

		//PHP stuff
		JPanel PHPPanel = new JPanel(new BorderLayout());
		generateFork = new JButton("Generate Fork");
		generateFork.addActionListener(this);
		generateUpdate = new JButton("Generate Update");
		generateUpdate.addActionListener(this);
		generateLoad = new JButton("Generate Load");
		generateLoad.addActionListener(this);
		generateHTML = new JButton("Generate HTML");
		generateHTML.addActionListener(this);
		generatePublisherStatsList = new JButton("Generate Category");
		generatePublisherStatsList.addActionListener(this);
		
		
		//enerateInsert = new JButton("Generate SQL");
		//generateInsert.addActionListener(this);
		PHPPanel.add(generateFork, BorderLayout.NORTH);
		PHPPanel.add(generateLoad, BorderLayout.CENTER);
		PHPPanel.add(generateUpdate, BorderLayout.SOUTH);

		//PHP Panel 2 (rightside)
		JPanel PHPPanel2 = new JPanel(new BorderLayout());
		generateVariables = new JButton("Generate Vars");
		generateVariables.addActionListener(this);
		PHPPanel2.add(generateVariables, BorderLayout.NORTH);
		PHPPanel2.add(generatePublisherStatsList, BorderLayout.CENTER);
		PHPPanel2.add(generateHTML, BorderLayout.SOUTH);

		JPanel modmakerPanel = new JPanel(new BorderLayout());
		modmakerPanel.add(SQLPanel, BorderLayout.WEST);
		modmakerPanel.add(PHPPanel, BorderLayout.CENTER);
		modmakerPanel.add(PHPPanel2, BorderLayout.EAST);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(submit, BorderLayout.WEST);
		buttonPanel.add(modmakerPanel, BorderLayout.EAST);

		inputPanel.add(inputScrollPane, BorderLayout.CENTER);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		JPanel outputPanel = new JPanel(new BorderLayout());
		JLabel outputLabel = new JLabel("Output");
		output = new JTextArea(20, 30);
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(false);
		//outputscroll
		JScrollPane outputScrollPane = new JScrollPane(output);
		inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputScrollPane.setPreferredSize(new Dimension(250, 250));

		copy = new JButton("Copy");
		copy.addActionListener(this);
		outputPanel.add(outputLabel, BorderLayout.NORTH);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);
		outputPanel.add(copy, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
		splitPane.setDividerLocation(150 + splitPane.getInsets().top);

		wavelistGUI.add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(wavelistGUI);
		pack();
		
		generateFork.setEnabled(false);
		generateUpdate.setEnabled(false);
		generateLoad.setEnabled(false);
		generateHTML.setEnabled(false);
		generatePublisherStatsList.setEnabled(false);
		generateTable.setEnabled(false);
		generateVariables.setEnabled(false);
		generateInsert.setEnabled(false);
		
		generateFork.setToolTipText("SQL output is disabled");
		generateUpdate.setToolTipText("SQL output is disabled");
		generateLoad.setToolTipText("SQL output is disabled");
		generateHTML.setToolTipText("SQL output is disabled");
		generatePublisherStatsList.setToolTipText("SQL output is disabled");
		generateTable.setToolTipText("SQL output is disabled");
		generateVariables.setToolTipText("SQL output is disabled");
		generateInsert.setToolTipText("SQL output is disabled");
	}

	public static void diffString(String str1, String str2) {
		if (str1.length() != str2.length()) {
			System.out.println("Strings are not the same length: " + str1.length() + " vs " + str2.length());

			return;
		}
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) == str2.charAt(i)) {
				continue;
			} else {
				System.out.println("Difference at index " + i + ", str1: " + str1.charAt(i) + ", str2: " + str2.charAt(i));
			}
		}
	}

	/**
	 * Generates fork code for ModMaker's PHP page
	 */
	public void generateForkPHP() {
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		Category cat;
		try {
			cat = new Category(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			return;
		}
		StringBuilder sb = new StringBuilder();
		//ENEMYNAME
		sb.append("//");
		sb.append(cat.categoryname.toUpperCase());
		sb.append("\n");
		//echo "<br>Beginning Centurion fork.";
		sb.append("//echo \"<br>Beginning ");
		sb.append(cat.categoryname);
		sb.append(" fork.\";\n");
		//$stmt = $dbh->prepare("SELECT * FROM modmaker_enemies_centurion WHERE mod_id=:fork_parent");
		sb.append("$stmt = $dbh->prepare(\"SELECT * FROM modmaker_enemies_");
		sb.append(cat.categoryname.toLowerCase());
		sb.append(" WHERE mod_id=:fork_parent\");\n");
		//$stmt->bindValue(":fork_parent", $original_id);
		sb.append("$stmt->bindValue(\":fork_parent\", $original_id);\n");
		//$stmt->execute();
		sb.append("$stmt->execute();\n");
		//$NAMEs = $stmt->fetchAll();
		sb.append("$");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("s = $stmt->fetchAll();\n");
		//foreach ($NAMEs as $NAMErow) {
		sb.append("foreach($");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("s as $");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("row) {\n");
		//WE NEED TO INDENT FROM HERE.
		//first we grab the difficulty - not defined in this parser.
		//$NAME_difficulty = $NAMErow['difficulty'];

		sb.append("\t$");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("_difficulty = $");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("row['difficulty'];\n");

		//Generate NAMEs_STAT = NAMErow['STAT'];
		for (Stat stat : cat.stats) {
			sb.append("\t$");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_");
			sb.append(stat.statname.toLowerCase());
			sb.append(" = $");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("row['");
			sb.append(stat.statname.toLowerCase());
			sb.append("'];\n");
		}
		//get genesis modifed in the loop
		sb.append("\t$");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("_modified_genesis = $");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("row['modified_genesis'];\n");

		//$stmt = $dbh->prepare("INSERT INTO modmaker_enemies_centurion VALUES(:mod_id, :difficulty, :minhealth, :maxhealth, :minshields, :maxshields, :minsmokefrequency, :maxsmokefrequency, :grenadeinterval,:maxenemyshieldrecharge, :aishieldregendelay, :aishieldregenpct,  false, :modified_genesis)");

		sb.append("\t$stmt = $dbh->prepare(\"INSERT INTO modmaker_enemies_");
		sb.append(cat.categoryname.toLowerCase());
		sb.append(" VALUES(:mod_id, :difficulty,");
		for (Stat stat : cat.stats) {
			sb.append(" :");
			sb.append(stat.statname.toLowerCase());
			sb.append(",");
		}
		sb.append(" false, :modified_genesis)\");\n");
		//bind mod_id and difficulty.
		sb.append("\t$stmt->bindValue(\":mod_id\", $mod_id);\n");
		sb.append("\t$stmt->bindValue(\":difficulty\", $");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("_difficulty);\n");

		//$stmt->bindValue(":STAT", $NAME_STAT);
		for (Stat stat : cat.stats) {
			sb.append("\t$stmt->bindValue(\":");
			sb.append(stat.statname.toLowerCase());
			sb.append("\", $");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_");
			sb.append(stat.statname.toLowerCase());
			sb.append(");\n");
		}
		//bind modified_genesis
		sb.append("\t$stmt->bindValue(\":modified_genesis\", $");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("_modified_genesis);\n");

		//if (!$stmt->execute()) {
		sb.append("\tif (!$stmt->execute()) {\n");
		//echo "NAME FORK FAIL."
		sb.append("\t\techo \"");
		sb.append(cat.categoryname);
		sb.append(" FORK FAIL: \".print_r($stmt->errorInfo());\n");
		sb.append("\t\treturn ERROR_SQL_GENERIC;\n");
		// } else {
		sb.append("\t}\n");
		sb.append("}");
		output.setText(sb.toString());
	}

	private void generateHTMLBox() {
		//generate standard containers
		String input_text = input.getText();
		Category cat;
		try {
			cat = new Category(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			return;
		}
		StringBuilder innerText = new StringBuilder();
		for (Stat stat : cat.stats) {
			String entry = Category.ENTRY_TEMPLATE;
			entry = entry.replaceAll("HINTTEXT", "MISSINGHINT");
			entry = entry.replaceAll("PREFIX", "");
			entry = entry.replaceAll("POSTFIX", "");
			entry = entry.replaceAll("VARNAME", stat.statname.toLowerCase());
			entry = entry.replaceAll("HUMANNAME", stat.statname);
			entry = entry.replaceAll("TABLENAME", "mod_" + cat.categoryname.toLowerCase());
			innerText.append(entry);
		}
		String containerBlock = ContainerRow.CONTAINER_TEMPLATE;
		containerBlock = containerBlock.replaceAll("CONTAINERNAME", cat.categoryname);
		containerBlock = containerBlock.replaceAll("CONTAINERDESCRIPTION",
				"These values were autogenerated by Mod Manager. Move them or delete unnecessary ones.");
		containerBlock = containerBlock.replaceAll("INPUTS_PLACEHOLDER", innerText.toString());
		output.setText(containerBlock);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == submit) {
			// parse it.
			String input_text = input.getText();
			try {
				Category cat = new Category(input_text);
				output.setText(cat.toString());
			} catch (Exception ex) {
				output.setText(ex.toString());
			}
		} else if (e.getSource() == generateInsert) {
			generateSQL();
		} else if (e.getSource() == generateTable) {
			generateTable2();
		} else if (e.getSource() == generateFork) {
			generateForkPHP();
		} else if (e.getSource() == generateLoad) {
			generateLoadPHP();
		} else if (e.getSource() == generateVariables) {
			generateVariablesPHP();
		} else if (e.getSource() == generateHTML) {
			generateHTMLBox();
		} else if (e.getSource() == generatePublisherStatsList) {
			generateCategory();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection(myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}

	}

	private void generateCategory() {
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		
		Category cat = new Category(input_text);
		sb.append("\tcase \""+cat.categoryname+"\":\n");
		for (Stat stat : cat.stats) {
			//$this->addStat("MeleeAttackInterval", $mod->mod_praetorian_meleeattackinterval[$this->difficulty]);
			sb.append("\t\t$this->addStat(\"");
			sb.append(stat.statname);
			sb.append("\", $mod->mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_");
			sb.append(stat.statname.toLowerCase());
			sb.append("[$this->difficulty]);\n");
		}
		sb.append("\t\tbreak;\n");
		this.output.setText(sb.toString());
	}

	private String spLevelToHumanName(String section) {
		switch (section) {
		case "level1difficultydata":
			return "narrative";
		case "level2difficultydata":
			return "casual";
		case "level3difficultydata":
			return "normal";
		case "level4difficultydata":
			return "hardcore";
		case "level5difficultydata":
			return "insanity";
		case "level6difficultydata":
			return "debug";
		}
		return ";;"; //will cause SQL error
	}

	private void generateTable2() {
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			String wrappedText = "<biodiff>" + input_text + "</biodiff>";
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(wrappedText));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList properties = doc.getElementsByTagName("Property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node scannednode = properties.item(i);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element difficultyElem = (Element) scannednode;
					//iterate over values
					NodeList valueList = difficultyElem.getChildNodes();
					for (int k = 0; k < valueList.getLength(); k++) {
						Node valueNode = valueList.item(k);
						if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
							Element valueElement = (Element) valueNode;
							Category cat;
							try {
								cat = new Category(valueElement.getTextContent());
							} catch (Exception ex) {
								ex.printStackTrace();
								output.setText(ex.toString());
								return;
							}
							sb.append("/*-------");
							sb.append(cat.categoryname);
							sb.append("-------*/\n");
							sb.append("DROP TABLE IF EXISTS modmaker_enemies_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append(";\n");

							//CREATE TABLE STATEMENT
							sb.append("CREATE TABLE modmaker_enemies_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("(\n");
							sb.append("\tmod_id INT NOT NULL, /*mod this enemy belongs to*/\n");
							sb.append("\tdifficulty VARCHAR(9) NOT NULL, /*difficulty this applies to*/\n");
							for (Stat stat : cat.stats) {
								sb.append("\t");
								sb.append(stat.statname.toLowerCase());
								sb.append(" FLOAT NOT NULL,\n");
							}

							sb.append("\tmodified boolean NOT NULL,\n");
							sb.append("\tmodified_genesis boolean NOT NULL,\n");
							sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
							sb.append("\tPRIMARY KEY(mod_id, difficulty)\n");
							sb.append(");\n");
						}
					}
				}
			}

			output.setText(sb.toString());
		} catch (Exception e) {
			//not xml
			sb = new StringBuilder();
			Category cat;
			try {
				cat = new Category(input_text);
			} catch (Exception ex) {
				ex.printStackTrace();
				output.setText(ex.toString());
				return;
			}
			sb.append("/*-------");
			sb.append(cat.categoryname);
			sb.append("-------*/\n");
			sb.append("DROP TABLE IF EXISTS modmaker_enemies_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append(";\n");

			//CREATE TABLE STATEMENT
			sb.append("CREATE TABLE modmaker_enemies_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("(\n");
			sb.append("\tmod_id INT NOT NULL, /*mod this enemy belongs to*/\n");
			sb.append("\tdifficulty VARCHAR(9), /*difficulty this applies to*/\n");
			for (Stat stat : cat.stats) {
				sb.append("\t");
				sb.append(stat.statname.toLowerCase());
				sb.append(" FLOAT NOT NULL,\n");
			}

			sb.append("\tmodified boolean NOT NULL,\n");
			sb.append("\tmodified_genesis boolean NOT NULL,\n");
			sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
			sb.append("\tPRIMARY KEY(mod_id, difficulty)\n");
			sb.append(");");
			output.setText(sb.toString());
		}
	}

	private void generateSQL() {
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			String wrappedText = "<biodiff>" + input_text + "</biodiff>";
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(wrappedText));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList properties = doc.getElementsByTagName("Property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node scannednode = properties.item(i);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element difficultyElem = (Element) scannednode;
					String difficulty = spLevelToHumanName(scannednode.getAttributes().getNamedItem("name").getTextContent());
					//iterate over values
					NodeList valueList = difficultyElem.getChildNodes();
					for (int k = 0; k < valueList.getLength(); k++) {
						Node valueNode = valueList.item(k);
						if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
							Element valueElement = (Element) valueNode;
							Category cat;
							try {
								cat = new Category(valueElement.getTextContent());
							} catch (Exception ex) {
								ex.printStackTrace();
								output.setText(ex.toString());
								return;
							}
							sb.append("/*");
							sb.append(difficulty);
							sb.append(" ");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("*/\n");
							/*
							 * sb.append("DELETE FROM modmaker_enemies_");
							 * sb.append(cat.categoryname.toLowerCase());
							 * sb.append(
							 * " WHERE difficulty = 'narrative' or difficulty = 'casual' or difficulty = 'normal' or difficulty = 'hardcore' or difficulty = 'insanity' or difficulty='narrativ';\n"
							 * );
							 */
							sb.append("INSERT INTO modmaker_enemies_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append(" VALUES(\n");
							sb.append("\t1, /*GENESIS MOD ID*/\n");
							sb.append("\t\"");
							sb.append(difficulty);
							sb.append("\", /*difficulty*/\n");
							for (Stat stat : cat.stats) {
								sb.append("\t");
								sb.append(stat.statrange.floaty);
								sb.append(", /*");
								sb.append(stat.statname.toLowerCase());
								sb.append("*/\n");
							}
							sb.append("\tfalse, /*modified*/\n");
							sb.append("\tfalse /*genesis modified*/\n");
							sb.append(");"); //end of SQL statement
						}
					}
				}
			}

			output.setText(sb.toString());
		} catch (Exception e) {
			//not xml
			sb = new StringBuilder();
			Category cat;
			try {
				cat = new Category(input_text);
			} catch (Exception ex) {
				ex.printStackTrace();
				output.setText(ex.toString());
				return;
			}
			sb.append("/*");
			sb.append(((String) difficultylist.getSelectedItem()).toLowerCase());
			sb.append(" ");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("*/\n");
			sb.append("INSERT INTO modmaker_enemies_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append(" VALUES(\n");
			sb.append("\t1, /*GENESIS MOD ID*/\n");
			sb.append("\t\"");
			sb.append(((String) difficultylist.getSelectedItem()).toLowerCase());
			sb.append("\", /*difficulty*/\n");
			for (Stat stat : cat.stats) {
				sb.append("\t");
				sb.append(stat.statrange.floaty);
				sb.append(", /*");
				sb.append(stat.statname.toLowerCase());
				sb.append("*/\n");
			}
			sb.append("\tfalse, /*modified*/\n");
			sb.append("\tfalse /*genesis modified*/\n");
			sb.append(");"); //end of SQL statement
			output.setText(sb.toString());
		}
	}

	private void generateTable() {
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		Category cat;
		try {
			cat = new Category(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			ex.printStackTrace();
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/*-------");
		sb.append(cat.categoryname);
		sb.append("-------*/\n");
		sb.append("DROP TABLE IF EXISTS modmaker_enemies_");
		sb.append(cat.categoryname.toLowerCase());
		sb.append(";\n");

		//CREATE TABLE STATEMENT
		sb.append("CREATE TABLE modmaker_enemies_");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("(\n");
		sb.append("\tmod_id INT NOT NULL, /*mod this enemy belongs to*/\n");
		sb.append("\tdifficulty VARCHAR(9), /*difficulty this applies to*/\n");
		for (Stat stat : cat.stats) {
			sb.append("\t");
			sb.append(stat.statname.toLowerCase());
			sb.append(" FLOAT NOT NULL,\n");
		}

		sb.append("\tmodified boolean NOT NULL,\n");
		sb.append("\tmodified_genesis boolean NOT NULL,\n");
		sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
		sb.append("\tPRIMARY KEY(mod_id, difficulty)\n");
		sb.append(");");

		output.setText(sb.toString());
	}

	private void generateVariablesPHP() {
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			String wrappedText = "<biodiff>" + input_text + "</biodiff>";
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(wrappedText));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList properties = doc.getElementsByTagName("Property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node scannednode = properties.item(i);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element difficultyElem = (Element) scannednode;
					//iterate over values
					NodeList valueList = difficultyElem.getChildNodes();
					for (int k = 0; k < valueList.getLength(); k++) {
						Node valueNode = valueList.item(k);
						if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
							Element valueElement = (Element) valueNode;
							Category cat;
							try {
								cat = new Category(valueElement.getTextContent());
							} catch (Exception ex) {
								ex.printStackTrace();
								output.setText(ex.toString());
								return;
							}
							sb.append("\t//");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("\n");
							for (Stat stat : cat.stats) {
								sb.append("\tpublic $mod_");
								sb.append(cat.categoryname.toLowerCase());
								sb.append("_");
								sb.append(stat.statname.toLowerCase());
								sb.append(" = null;\n");
							}
							// modified
							sb.append("\tpublic $mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_");
							sb.append("modified = null;\n");
							//modified_genesis
							sb.append("\tpublic $mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_");
							sb.append("modified_genesis = null;\n");
						}
					}
				}
			}
			output.setText(sb.toString());
		} catch (Exception e) {
			// get name of enemy
			// Parse the enemy
			Category cat;
			try {
				cat = new Category(input_text);
			} catch (Exception ex) {
				output.setText(ex.toString());
				ex.printStackTrace();
				return;
			}

			//centurion
			/*
			 * public $mod_centurion_health_min; //array ny name (difficulties)
			 * public $mod_centurion_health_max; //array by name(difficulties)
			 * public $mod_centurion_shields_min = null; public
			 * $mod_centurion_shields_max = null; public
			 * $mod_centurion_maxenemyshieldrecharge = null; public
			 * $mod_centurion_aishieldregendelay = null; public
			 * $mod_centurion_aishieldregenpct= null; public
			 * $mod_centurion_smokefrequency_min = null; public
			 * $mod_centurion_smokefrequency_max = null; public
			 * $mod_centurion_grenadeinterval = null; public
			 * $mod_centurion_modified = false; public
			 * $mod_centurion_modified_genesis = false;
			 */

			sb = new StringBuilder();
			sb.append("\t//");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("\n");
			for (Stat stat : cat.stats) {
				sb.append("\tpublic $mod_");
				sb.append(cat.categoryname.toLowerCase());
				sb.append("_");
				sb.append(stat.statname.toLowerCase());
				sb.append(" = null;\n");
			}
			// modified
			sb.append("\tpublic $mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_");
			sb.append("modified = null;\n");
			//modified_genesis
			sb.append("\tpublic $mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_");
			sb.append("modified_genesis = null;\n");

			output.setText(sb.toString());
		}
	}

	private void generateLoadPHP() {
		System.out.println("Generating load");
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			String wrappedText = "<biodiff>" + input_text + "</biodiff>";
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(wrappedText));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList properties = doc.getElementsByTagName("Property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node scannednode = properties.item(i);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element difficultyElem = (Element) scannednode;
					//iterate over values
					NodeList valueList = difficultyElem.getChildNodes();
					for (int k = 0; k < valueList.getLength(); k++) {
						Node valueNode = valueList.item(k);
						if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
							Element valueElement = (Element) valueNode;
							Category cat;
							try {
								cat = new Category(valueElement.getTextContent());
							} catch (Exception ex) {
								ex.printStackTrace();
								output.setText(ex.toString());
								return;
							}
							sb.append("\t//");
							sb.append(cat.categoryname.toUpperCase());
							sb.append("\n");

							//public function loadNAME(){
							sb.append("\tpublic function load");
							sb.append(Character.toUpperCase(cat.categoryname.charAt(0)) + cat.categoryname.toLowerCase().substring(1)); //have only first letter capitalized.
							sb.append("(){\n");
							//doubletab from here on
							for (Stat stat : cat.stats) {
								sb.append("\t\t$this->mod_");
								sb.append(cat.categoryname.toLowerCase());
								sb.append("_");
								sb.append(stat.statname.toLowerCase());
								sb.append(" = array();\n");
							}
							sb.append("\t\t$this->mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_modified = array();\n");
							//modified genesis
							sb.append("\t\t$this->mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_modified_genesis = array();\n");
							//$dbh = new PDO('mysql:host=0.0.0.0;port=3306;dbname=me3tweaks', 'mgamerz');
							sb.append("\t\t$dbh = new PDO('mysql:host=0.0.0.0;port=3306;dbname=me3tweaks', 'mgamerz');\n");

							//load values from DB
							sb.append("\t\t//Load values from DB\n");

							//select * from modmaker_enemies_NAME where mod_id=:mod
							sb.append("\t\t$sql = \"SELECT * FROM modmaker_enemies_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append(" WHERE mod_id=:mod_id\";\n");

							sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
							sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
							sb.append("\t\t$stmt->execute();\n");
							sb.append("\t\twhile ($row = $stmt->fetch()){\n");
							//triple tab, stat assignemnt
							for (Stat stat : cat.stats) {
								sb.append("\t\t\t$this->mod_");
								sb.append(cat.categoryname.toLowerCase());
								sb.append("_");
								sb.append(stat.statname.toLowerCase());
								sb.append("[$row['difficulty']] = $row['");
								sb.append(stat.statname.toLowerCase());
								sb.append("'];\n");
							}
							//modified, genesis
							sb.append("\t\t\t$this->mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_modified[$row['difficulty']] = $row['modified'];\n");
							sb.append("\t\t\t$this->mod_");
							sb.append(cat.categoryname.toLowerCase());
							sb.append("_modified_genesis[$row['difficulty']] = $row['modified_genesis'];\n");
							sb.append("\t\t}\n");
							sb.append("\t}\n");
						}
					}
				}
			}
			output.setText(sb.toString());
		} catch (Exception e) {
			Category cat;
			System.out.println("ERR");
			try {
				cat = new Category(input_text);
			} catch (Exception ex) {
				output.setText(ex.toString());
				return;
			}

			sb = new StringBuilder();
			//NAME
			sb.append("\t//");
			sb.append(cat.categoryname.toUpperCase());
			sb.append("\n");

			//public function loadNAME(){
			sb.append("\tpublic function load");
			sb.append(Character.toUpperCase(cat.categoryname.charAt(0)) + cat.categoryname.toLowerCase().substring(1)); //have only first letter capitalized.
			sb.append("(){\n");
			//doubletab from here on
			for (Stat stat : cat.stats) {
				sb.append("\t\t$this->mod_");
				sb.append(cat.categoryname.toLowerCase());
				sb.append("_");
				sb.append(stat.statname.toLowerCase());
				sb.append(" = array();\n");
			}
			sb.append("\t\t$this->mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_modified = array();\n");
			//modified genesis
			sb.append("\t\t$this->mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_modified_genesis = array();\n");
			//$dbh = new PDO('mysql:host=0.0.0.0;port=3306;dbname=me3tweaks', 'mgamerz');
			sb.append("\t\t$dbh = new PDO('mysql:host=0.0.0.0;port=3306;dbname=me3tweaks', 'mgamerz');\n");

			//load values from DB
			sb.append("\t\t//Load values from DB\n");

			//select * from modmaker_enemies_NAME where mod_id=:mod
			sb.append("\t\t$sql = \"SELECT * FROM modmaker_enemies_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append(" WHERE mod_id=:mod_id\";\n");

			sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
			sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
			sb.append("\t\t$stmt->execute();\n");
			sb.append("\t\twhile ($row = $stmt->fetch()){\n");
			//triple tab, stat assignemnt
			for (Stat stat : cat.stats) {
				sb.append("\t\t\t$this->mod_");
				sb.append(cat.categoryname.toLowerCase());
				sb.append("_");
				sb.append(stat.statname.toLowerCase());
				sb.append("[$row['difficulty']] = $row['");
				sb.append(stat.statname.toLowerCase());
				sb.append("'];\n");
			}
			//modified, genesis
			sb.append("\t\t\t$this->mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_modified[$row['difficulty']] = $row['modified'];\n");
			sb.append("\t\t\t$this->mod_");
			sb.append(cat.categoryname.toLowerCase());
			sb.append("_modified_genesis[$row['difficulty']] = $row['modified_genesis'];\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n");

			output.setText(sb.toString());
		}
	}
}
