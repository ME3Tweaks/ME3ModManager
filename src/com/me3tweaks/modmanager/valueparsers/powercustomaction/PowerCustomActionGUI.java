package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PowerCustomActionGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JButton parse, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generatePublish, copy;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;


	public static void main(String[] args) throws IOException {
		new PowerCustomActionGUI();
	}

	public PowerCustomActionGUI() {
		this.setTitle("ME3CMM BioAI Parser Tool");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/icon32.png")));
		this.setMinimumSize(new Dimension(490, 500));
		this.setPreferredSize(new Dimension(490, 500));
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		JPanel bioaiGUI = new JPanel(new BorderLayout());
		JLabel instructionsLabel = new JLabel(
				"<html>ME3CMM SFXCustomAction Parser<br>Enter the SFXCustomACtion text below, as XML, starting with a &lt;Section&gt; tag, including all powers you wish to parse, and end with the final power's closing section tag.</html>");
		bioaiGUI.add(instructionsLabel, BorderLayout.NORTH);
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
		
		parse = new JButton("Parse");
		parse.addActionListener(this);
		
		
		// sql stuff
		JPanel SQLPanel = new JPanel(new BorderLayout());
		generateTable = new JButton("Generate TBL");
		generateTable.addActionListener(this);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		SQLPanel.add(generateTable, BorderLayout.CENTER);
		SQLPanel.add(generateInsert, BorderLayout.SOUTH);
		
		//PHP stuff
		JPanel PHPPanel = new JPanel(new BorderLayout());
		generateFork = new JButton("Generate Fork");
		generateFork.addActionListener(this);
		generatePublish = new JButton("Generate Publish");
		generatePublish.addActionListener(this);
		generateLoad = new JButton("Generate Load");
		generateLoad.addActionListener(this);
		
		//enerateInsert = new JButton("Generate SQL");
		//generateInsert.addActionListener(this);
		PHPPanel.add(generateFork, BorderLayout.NORTH);
		PHPPanel.add(generateLoad, BorderLayout.CENTER);
		PHPPanel.add(generatePublish, BorderLayout.SOUTH);
		
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
		buttonPanel.add(parse, BorderLayout.WEST);
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
		
		bioaiGUI.add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(bioaiGUI);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == parse) {
			parse();
		} else if (e.getSource() == generateInsert) {
			generateSQL();
		} else if (e.getSource() == generateTable) {
			generateTable();
		} else if (e.getSource() == generateFork){
			generateForkPHP();
		} else if (e.getSource() == generatePublish) {
			generatePublish();
		} else if (e.getSource() == generateLoad){
			generateLoad();
		} else if (e.getSource() == generateVariables) {
			generateVariables();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection (myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
			clpbrd.setContents (stringSelection, null);
		}
	}
	
	private void parse() {
		// TODO Auto-generated method stub
		// parse it.
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try {
			
			
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableName = sectionElement.getAttribute("name");
				System.out.println("Parsing section "+tableName);
				tableName = tableName.substring(tableName.indexOf('.')+1);
				tableName = tableName.replace("sfxcustomaction", "");
				tableName = tableName.replace("powercustomaction", "");
				if (tableName.charAt(tableName.length()-1) == '_') {
					tableName = tableName.substring(0, tableName.length()-2);
				}
				if (tableName.charAt(0) == '_') {
					tableName = tableName.substring(1);
				}
				String varPrefix = "modmaker_powers_"+tableName+"_";
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						String propName = prop.getAttribute("name");
						String data = prop.getTextContent();
						System.out.println("Found property: "+prop.getAttribute("name")+"");
						if (data.equals("true") || data.equals("false")){
							//its a boolean.
							System.out.println("BOOLEAN: "+propName);
							sb.append(varPrefix);
							sb.append(propName);
							sb.append(" = ");
							sb.append(data);
							sb.append("\n");
							continue;
						}
						if (DetonationParameters.isDetonationParameters(data)) {
							DetonationParameters dp = new DetonationParameters(varPrefix, data);
						}
						
						if (BaseRankUpgrade.isRankBonusUpgrade(data)) {
							BaseRankUpgrade bru = new BaseRankUpgrade(varPrefix, data);
						}
						/*try {
							Range test = new Range(prop.getTextContent());
							sb.append(prop.getAttribute("name"));
							sb.append(": ");
							sb.append(test.X);
							sb.append(" - ");
							sb.append(test.Y);
							sb.append("\n");
						} catch (StringIndexOutOfBoundsException strException){
							//its a direct str
							sb.append(prop.getAttribute("name"));
							sb.append(": ");
							sb.append(prop.getTextContent());
							sb.append("\n");
						} */
					}
				}
			}
			output.setText(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			output.setText(ex.toString());
		}
	}

	private void generateSQL(){
		// g
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		//sb.append(tableNameField.getText());
		sb.append(" data*/\n");
		sb.append("INSERT INTO modmaker_aiweapon_");
		//sb.append(tableNameField.getText());
		sb.append(" VALUES(\n");
		sb.append("\t1, /*GENESIS MOD ID*/\n");
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					sb.append("\t");
					
				}
			}
			sb.append("\tfalse, /*modified*/\n");
			sb.append("\tfalse /*genesis modified*/\n");
			sb.append(");"); //end of SQL statement
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}		
	}

	private void generateTable() {
		// g
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		//sb.append(tableNameField.getText());
		sb.append("*/\n");
		sb.append("CREATE TABLE modmaker_aiweapon_");
		//sb.append(tableNameField.getText());
		sb.append(" (\n");
		sb.append("\t mod_id INT NOT NULL,\n");
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
			sb.append("\tmodified boolean NOT NULL,\n");
			sb.append("\tmodified_genesis boolean NOT NULL,\n");
			sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
			sb.append("\tPRIMARY KEY(mod_id)\n");
			sb.append(") ENGINE=INNODB;");
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generatePublish() {
		// g
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		sb.append("//");
		//sb.append(tableNameField.getText());
		sb.append("\n");
		sb.append("if ($this->mod->mod_aiweapon_");
		//sb.append(tableNameField.getText());
		sb.append("_modified_genesis) {");
		sb.append("\n");
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
			sb.append("}");
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generateLoad(){
		// get name of enemy
		// Parse the enemy
		String input_text = getInput();
		String weaponName = "placeholder";
		String tableName = "placeholder";
		
		StringBuilder sb = new StringBuilder();
		
		//public function loadNAME(){
		sb.append("\tpublic function loadAIWeapon");
		sb.append(Character.toUpperCase(weaponName.charAt(0)) + weaponName.toLowerCase().substring(1)); //have only first letter capitalized.
		sb.append("(){\n");
		//doubletab from here on
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
			//modified, genesis
			sb.append("\t\t$this->mod_aiweapon_");
			sb.append(weaponName);
			sb.append("_modified = null;\n");
			sb.append("\t\t$this->mod_aiweapon_");
			sb.append(weaponName);
			sb.append("_modified_genesis = null;\n");
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		
		sb.append("\t\trequire($_SERVER['DOCUMENT_ROOT'].\"/db-middleman.php\");\n");
		
		//load values from DB
		sb.append("\t\t//Load values from DB\n");
		
		//select * from modmaker_enemies_NAME where mod_id=:mod
		sb.append("\t\t$sql = \"SELECT * FROM ");
		sb.append(tableName);
		sb.append(" WHERE mod_id=:mod_id\";\n");
		
		sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
		sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
		sb.append("\t\t$stmt->execute();\n");
		sb.append("\t\t$row = $stmt->fetch();\n");
		//generate variables, load from row
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		//modified, genesis
		sb.append("\t\t$this->mod_aiweapon_");
		sb.append(weaponName);
		sb.append("_modified = $row['modified'];\n");
		sb.append("\t\t$this->mod_aiweapon_");
		sb.append(weaponName);
		sb.append("_modified_genesis = $row['modified_genesis'];\n");
        sb.append("\t}\n");
		
		output.setText(sb.toString());
	}
	
	/**
	 * Generates fork code for ModMaker's fork handler
	 */
	public void generateForkPHP(){
		String input_text = getInput();
		String weaponName = "placeholder";
		String tableName = "placeholder";
		StringBuilder sb = new StringBuilder();
		sb.append("//");
		sb.append(weaponName.toUpperCase());
		sb.append("\n"); 
		//echo "<br>Beginning TABLENAME fork.";
		sb.append("//echo \"<br>Beginning ");
		sb.append(weaponName);
		sb.append(" fork.\";\n");
		
		//$stmt = $dbh->prepare("SELECT * FROM modmaker_enemies_centurion WHERE mod_id=:fork_parent");
		sb.append("$stmt = $dbh->prepare(\"SELECT * FROM ");
		sb.append(tableName);
		sb.append(" WHERE mod_id=:fork_parent\");\n");
		//$stmt->bindValue(":fork_parent", $original_id);
		sb.append("$stmt->bindValue(\":fork_parent\", $original_id);\n");
		//$stmt->execute();
		sb.append("$stmt->execute();\n");
		//$NAMEs = $stmt->fetchAll();
		sb.append("$");
		sb.append(weaponName);
		sb.append("s = $stmt->fetchAll();\n");
		//foreach ($NAMEs as $NAMErow) {
		sb.append("foreach($");
		sb.append(weaponName);
		sb.append("s as $");
		sb.append(weaponName);
		sb.append("row) {\n");
		sb.append("\t$stmt = $dbh->prepare(\"INSERT INTO ");
		sb.append(tableName);
		sb.append(" VALUES(:mod_id, ");
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
			//output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		sb.append("false, :modified_genesis)\");\n");
		sb.append("\t$stmt->bindValue(\":mod_id\", $mod_id);\n");
		
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
			//output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		//bind modified_genesis
		sb.append("\t$stmt->bindValue(\":modified_genesis\", $");
		sb.append(weaponName);
		sb.append("row['");
		sb.append("modified_genesis");
		sb.append("']);\n");
		
		//if (!$stmt->execute()) {
		sb.append("\tif (!$stmt->execute()) {\n");
		//echo "NAME FORK FAIL."
		sb.append("\t\t//echo \"");
		sb.append(weaponName);
		sb.append(" FORK FAIL: \".print_r($stmt->errorInfo());\n");
		sb.append("\t\treturn ERROR_SQL_GENERIC;\n");
		// } else {
		sb.append("\t} else {\n");
		//echo "<br>Finished NAME fork
		sb.append("\t\t//echo \"<br>Finished ");
		sb.append(weaponName);
		sb.append(" fork.\";\n");
		//closing brackets
		sb.append("\t}\n");
		sb.append("}");
		output.setText(sb.toString());
	}
	
	private void generateVariables() {		
		String input_text = getInput();
		String weaponName = "placeholder";
		StringBuilder sb = new StringBuilder();
		sb.append("\t//");
		sb.append(weaponName);
		sb.append("\n");
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			Element sectionElement = (Element) section.item(0);
			NodeList propertyList = sectionElement.getChildNodes();
			//We are now at at the "sections" array.
			//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
			for (int k = 0; k < propertyList.getLength(); k++){
				//for every property in this filenode (of the data to merge)...
				Node scannednode = propertyList.item(k);
				if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
					Element prop = (Element) scannednode;
					
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		// modified
		sb.append("\tpublic $mod_aiweapon_");
		sb.append(weaponName);
		sb.append("_");
		sb.append("modified = null;\n");
		//modified_genesis
		sb.append("\tpublic $mod_aiweapon_");
		sb.append(weaponName);
		sb.append("_");
		sb.append("modified_genesis = null;\n");
		
		output.setText(sb.toString());
	}
	
	private String getInput(){
		String wrappedXML = input.getText();
		wrappedXML = "<custompowers>"+wrappedXML+"</custompowers>";
		return wrappedXML;
	}
}