package com.me3tweaks.modmanager.valueparsers.biodifficulty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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

public class DifficultyGUI extends JFrame implements ActionListener {
    JTextArea input, output;
    boolean closeOnExit = false;
	JTextField enemy;
	JButton submit, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generateUpdate, copy;
	JComboBox<String> difficultylist;
	String[] difficulties = { "Bronze", "Silver", "Gold", "Platinum" };

	public static void main(String[] args) throws IOException {
		new DifficultyGUI();
	}

	public DifficultyGUI() {
		this.setTitle("ME3CMM Biodifficulty Parser Tool");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/icon32.png")));
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
		instructionsLabel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel inputPanel = new JPanel(new BorderLayout());
		input = new JTextArea(6, 45);
		input.setMinimumSize(new Dimension(50, 120));
		input.setLineWrap(true);
		input.setWrapStyleWord(false);
		//inputscroll
		JScrollPane inputScrollPane = new JScrollPane(input);
		inputScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
		inputScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputScrollPane.setPreferredSize(new Dimension(250, 250));
		
		
		copy = new JButton("Copy");
		copy.addActionListener(this);
		outputPanel.add(outputLabel, BorderLayout.NORTH);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);
		outputPanel.add(copy, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				inputPanel, outputPanel);
		splitPane.setDividerLocation(150 + splitPane.getInsets().top);

		wavelistGUI.add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(wavelistGUI);
		pack();
	}

	public static void diffString(String str1, String str2) {
		if (str1.length() != str2.length()) {
			System.out.println("Strings are not the same length: "
					+ str1.length() + " vs " + str2.length());

			return;
		}
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) == str2.charAt(i)) {
				continue;
			} else {
				System.out.println("Difference at index " + i + ", str1: "
						+ str1.charAt(i) + ", str2: " + str2.charAt(i));
			}
		}
	}

	/**
	 * Generates fork code for ModMaker's PHP page
	 */
	public void generateForkPHP(){
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		Category cat;
		try {
			cat = new Category(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			return ;
		}
		StringBuilder sb = new StringBuilder();
		//ENEMYNAME
		sb.append("//");
		sb.append(cat.categoryname.toUpperCase());
		sb.append("\n"); 
		//echo "<br>Beginning Centurion fork.";
		sb.append("echo \"<br>Beginning ");
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
		for (Stat stat : cat.stats){
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
		for (Stat stat : cat.stats){
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
		for (Stat stat : cat.stats){
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
		sb.append("\t} else {\n");
		//echo "<br>Finished NAME fork
		sb.append("\t\techo \"<br>Finished ");
		sb.append(cat.categoryname);
		sb.append(" fork.\";\n");
		//closing brackets
		sb.append("\t}\n");
		sb.append("}");
		output.setText(sb.toString());
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
			generateTable();	
		} else if (e.getSource() == generateFork){
			generateForkPHP();
		} else if (e.getSource() == generateLoad){
			generateLoadPHP();
		} else if (e.getSource() == generateVariables){
			generateVariablesPHP();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection (myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
			clpbrd.setContents (stringSelection, null);
		}
	}

	private void generateSQL() {
		// get name of enemy
		// Parse the enemy
		String input_text = input.getText();
		Category cat;
		try {
			cat = new Category(input_text);
		} catch (Exception ex) {
			ex.printStackTrace();
			output.setText(ex.toString());
			return;
		}
		//get difficulty
		String difficulty = (String) difficultylist.getSelectedItem();
		difficulty = difficulty.toLowerCase();
		
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(difficulty);
		sb.append(" ");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("*/\n");
		

		

		sb.append("INSERT INTO modmaker_enemies_");
		sb.append(cat.categoryname.toLowerCase());
		sb.append(" VALUES(\n");
		sb.append("\t1, /*GENESIS MOD ID*/\n");
		sb.append("\t\"");
		sb.append(difficulty);
		sb.append("\", /*difficulty*/\n");
		for (Stat stat : cat.stats){
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
	
	private void generateTable() {
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
		sb.append("\tdifficulty VARCHAR(8), /*difficulty this applies to*/\n");
		for (Stat stat : cat.stats){
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
	
	private void generateVariablesPHP(){
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
		
		 //centurion
		/*public $mod_centurion_health_min; //array ny name (difficulties)
	    public $mod_centurion_health_max; //array by name(difficulties)
	    public $mod_centurion_shields_min = null;
	    public $mod_centurion_shields_max = null;
	    public $mod_centurion_maxenemyshieldrecharge = null;
	    public $mod_centurion_aishieldregendelay = null;
	    public $mod_centurion_aishieldregenpct= null;
	    public $mod_centurion_smokefrequency_min = null;
	    public $mod_centurion_smokefrequency_max = null;
	    public $mod_centurion_grenadeinterval = null;
	    public $mod_centurion_modified = false; 
	    public $mod_centurion_modified_genesis = false; */
		
		StringBuilder sb = new StringBuilder();
		sb.append("\t//");
		sb.append(cat.categoryname.toLowerCase());
		sb.append("\n");
		for (Stat stat : cat.stats){
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

	private void generateLoadPHP() {

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
				//NAME
				sb.append("\t//");
				sb.append(cat.categoryname.toUpperCase());
				sb.append("\n");
				
				//public function loadNAME(){
				sb.append("\tpublic function load");
				sb.append(Character.toUpperCase(cat.categoryname.charAt(0)) + cat.categoryname.toLowerCase().substring(1)); //have only first letter capitalized.
				sb.append("(){\n");
				//doubletab from here on
				for (Stat stat:cat.stats) {
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
				for (Stat stat:cat.stats) {
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
