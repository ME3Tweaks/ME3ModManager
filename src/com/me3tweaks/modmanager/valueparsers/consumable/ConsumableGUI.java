package com.me3tweaks.modmanager.valueparsers.consumable;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SuppressWarnings("serial")
public class ConsumableGUI extends JFrame implements ActionListener {
	private static boolean isRunningAsMain = false;
	JTextArea input, output;
	JButton generateHTMLInput, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generatePublish, generatePHPValidation, generateJSValidation, copy;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;

	public static void main(String[] args) throws IOException {
		isRunningAsMain  = true;
		new ConsumableGUI();
	}
	
	public ConsumableGUI() {
		this.setTitle("ME3CMM Consumable Parser Tool");
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
				"<html>ME3CMM Consumable Parser<br>Enter the Consumables sections below, as XML, starting with a &lt;Section&gt; tag, and end with a closing section tag.</html>");
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
		inputScrollPane.setPreferredSize(new Dimension(250, 270));
		
		generateHTMLInput = new JButton("Generate HTML");
		generateHTMLInput.addActionListener(this);
		
		
		// sql stuff
		JPanel SQLPanel = new JPanel(new BorderLayout());
		generateTable = new JButton("Generate TBL");
		generateTable.addActionListener(this);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		generateFork = new JButton("Generate Fork");
		generateFork.addActionListener(this);
		SQLPanel.add(generateTable, BorderLayout.NORTH);
		SQLPanel.add(generateInsert, BorderLayout.CENTER);
		//extra bottom
		JPanel JSBottom = new JPanel(new BorderLayout());
		generateJSValidation = new JButton("Generate JS Val");
		generateJSValidation.addActionListener(this);
		JSBottom.add(generateFork,BorderLayout.NORTH);
		JSBottom.add(generateJSValidation,BorderLayout.SOUTH);

		SQLPanel.add(JSBottom, BorderLayout.SOUTH);
		//PHP stuff
		JPanel PHPPanel = new JPanel(new BorderLayout());
		
		
		generateVariables = new JButton("Generate Vars");
		generateVariables.addActionListener(this);
		generateLoad = new JButton("Generate Load");
		generateLoad.addActionListener(this);
		generatePublish = new JButton("Generate Publish");
		generatePublish.addActionListener(this);
		
		//enerateInsert = new JButton("Generate SQL");
		//generateInsert.addActionListener(this);
		PHPPanel.add(generateVariables, BorderLayout.NORTH);
		PHPPanel.add(generateLoad, BorderLayout.CENTER);
		
		//extra bottom
		JPanel PHPBottom = new JPanel(new BorderLayout());
		generatePHPValidation = new JButton("Generate PHP Val");
		generatePHPValidation.addActionListener(this);
		PHPBottom.add(generatePublish,BorderLayout.NORTH);
		PHPBottom.add(generatePHPValidation,BorderLayout.SOUTH);

		PHPPanel.add(PHPBottom, BorderLayout.SOUTH);
		
		JPanel modmakerPanel = new JPanel(new BorderLayout());
		modmakerPanel.add(SQLPanel, BorderLayout.WEST);
		modmakerPanel.add(PHPPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(generateHTMLInput, BorderLayout.WEST);
		buttonPanel.add(modmakerPanel, BorderLayout.EAST);
		
		inputPanel.add(inputScrollPane, BorderLayout.CENTER);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		JPanel outputPanel = new JPanel(new BorderLayout());
		JLabel outputLabel = new JLabel("Output");
		output = new JTextArea(20, 30);
		output.setTabSize(4);
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
		
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        	if (isRunningAsMain) {
	        		System.exit(0);
	        	}
		    }
		});
		
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
		if (e.getSource() == generateHTMLInput) {
			//parse();
			generateHTMLInputs();
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
		} else if (e.getSource() == generatePHPValidation) {
			generatePHPValidation();
		} else if (e.getSource() == generateJSValidation) {
			generateJSValidation();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection (myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
			clpbrd.setContents (stringSelection, null);
		}
	}
	
	private void parse() {
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
				String varPrefix = "modmaker_consumable_"+tableName+"_";
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
						if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")){
							//its a boolean.
							System.out.println("BOOLEAN: "+propName);
							sb.append(varPrefix);
							sb.append(propName);
							sb.append(" = ");
							sb.append(data);
							sb.append("\n");
							continue;
						}
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
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			//iterate over sections
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				
				sb.append("/*");
				sb.append(tableSuffix);
				sb.append(" data*/\n");
				sb.append("INSERT INTO modmaker_consumable_");
				sb.append(tableSuffix);
				sb.append(" VALUES(\n");
				sb.append("\t1, /*GENESIS MOD ID*/\n");
				
				
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (name.equals("force")){
							name = "vforce";
						}
						if (name.equals("range")) {
							name = "vrange";
						}
	
						if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")){
							sb.append("\t");
							//its a boolean.
							sb.append(data);
							sb.append(", /*");
							sb.append(name);
							sb.append("*/\n");
							continue;
						}
						
						try {
							int ints = Integer.parseInt(data);
							sb.append("\t");
							sb.append(ints);
							sb.append(", /*");
							sb.append(name);
							sb.append("*/\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							sb.append("\t");
							sb.append(dubs);
							sb.append(", /*");
							sb.append(name);
							sb.append("*/\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.err.println("Not inserting into SQL: "+name);
					}
				}
				sb.append("\tfalse, /*modified*/\n");
				sb.append("\tfalse /*genesis modified*/\n");
				sb.append(");\n"); //end of SQL statement
			}
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}		
	}

	private void generateTable() {
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				sb.append("DROP TABLE IF EXISTS modmaker_consumable_"+tableSuffix+";\n");
				sb.append("/*");
				sb.append(tableSuffix);
				sb.append("*/\n");
				sb.append("CREATE TABLE modmaker_consumable_");
				sb.append(tableSuffix);
				sb.append(" (\n");
				sb.append("\tmod_id INT NOT NULL,\n");
				
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")){
							//its a boolean.
							sb.append("\t");
							sb.append(name);
							sb.append(" BOOLEAN NOT NULL,\n");
							continue;
						}
						
						try {
							int ints = Integer.parseInt(data);
							sb.append("\t");
							sb.append(name);
							sb.append(" INT NOT NULL,\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							sb.append("\t");
							sb.append(name);
							sb.append(" FLOAT NOT NULL,\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.out.println("Ignoring property: "+name);
					}
				}
				sb.append("\tmodified boolean NOT NULL,\n");
				sb.append("\tmodified_genesis boolean NOT NULL,\n");
				sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
				sb.append("\tPRIMARY KEY(mod_id)\n");
				sb.append(") ENGINE=INNODB;\n\n");
			}
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generatePublish() {
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
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					//We are now at at a section element. this is a table/weapon
					String path = sectionElement.getAttribute("name");
					String tableName = getTableName(path);
					String dlcElement = "$basegameBioGameElements";
					if (path.contains("mp1")) {
						dlcElement = "$mp1BioGameElements";
					}
					if (path.contains("mp2")) {
						dlcElement = "$mp2BioGameElements";		
					}
					if (path.contains("mp3")) {
						dlcElement = "$mp3BioGameElements";
					}
					if (path.contains("mp4")) {
						dlcElement = "$mp4BioGameElements";
					}
					if (path.contains("mp5")) {
						dlcElement = "$mp5BioGameElements";
					}
					
					sb.append("//");
					sb.append(tableName);
					sb.append("\n");
					sb.append("if ($this->mod->consumables->mod_consumable_");
					sb.append(tableName);
					sb.append("_modified_genesis) {");
					sb.append("\n");

					NodeList propertyList = sectionElement.getChildNodes();
					//We are now at at the attribute list.
					for (int k = 0; k < propertyList.getLength(); k++){
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String publishName = prop.getAttribute("name");
							String name = propNameToSqlName(publishName);
							if (name.equals("force")){
								name = "vforce";
							}
							if (name.equals("range")) {
								name = "vrange";
							}
							
							System.out.println("Getting type for: "+path+" "+name);
							if (prop.getAttribute("type") == null || prop.getAttribute("type").equals("")) {
								System.err.println("No type for "+name+", skipping.");
								continue;
							}
							int type = Integer.parseInt(prop.getAttribute("type"));
							
														
							try {
								int ints = Integer.parseInt(data);
								sb.append("\t");
								sb.append("array_push(");
								sb.append(dlcElement);
								sb.append(", $this->createProperty(\"");
								sb.append(path);
								sb.append("\", \"");
								sb.append(publishName);
								sb.append("\", $this->mod->consumables->mod_consumable_");
								sb.append(tableName);
								sb.append("_");
								sb.append(name);
								sb.append(", ");
								sb.append(type);
								sb.append("));\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							
							try {
								double dubs = Double.parseDouble(data);
								sb.append("\t");
								sb.append("array_push(");
								sb.append(dlcElement);
								sb.append(", $this->createProperty(\"");
								sb.append(path);
								sb.append("\", \"");
								sb.append(publishName);
								sb.append("\", $this->mod->consumables->mod_consumable_");
								sb.append(tableName);
								sb.append("_");
								sb.append(name);
								sb.append(".\"f\", ");
								sb.append(type);
								sb.append("));\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							System.err.println("Not publishing property: "+publishName);
						} //end if property element
					} //end property loop
					sb.append("}\n\n");
				} //end section element
			} //end section loop
			
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generateLoad(){
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
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					String tableName = getTableName(sectionElement.getAttribute("name"));
					String loadName = getLoadName(tableName);
					//public function loadWeaponX() {
					sb.append("\tpublic function loadConsumable");
					sb.append(Character.toUpperCase(loadName.charAt(0)) + loadName.toLowerCase().substring(1)); //have only first letter capitalized.
					sb.append("(){\n");
					//inner vars
					NodeList propertyList = sectionElement.getChildNodes();
					
					sb.append("\t\trequire($_SERVER['DOCUMENT_ROOT'].\"/db-middleman.php\");\n");
					
					//load values from DB
					sb.append("\t\t//Load values from DB\n");
					
					//select * from modmaker_enemies_NAME where mod_id=:mod
					sb.append("\t\t$sql = \"SELECT * FROM modmaker_consumable_");
					sb.append(tableName);
					sb.append(" WHERE mod_id=:mod_id\";\n");
					
					sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
					sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
					sb.append("\t\t$stmt->execute();\n");
					sb.append("\t\t$row = $stmt->fetch();\n");
					
					//iterate over properties again, for loading via row
					for (int k = 0; k < propertyList.getLength(); k++){
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = propNameToSqlName(prop.getAttribute("name"));
							if (name.equals("force")){
								name = "vforce";
							}
							if (name.equals("range")) {
								name = "vrange";
							}
							
							try {
								int ints = Integer.parseInt(data);
								sb.append("\t\t$this->mod_consumable_");
								sb.append(tableName);
								sb.append("_");
								sb.append(name);
								sb.append(" = $row['");
								sb.append(name);
								sb.append("'];\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							
							try {
								double dubs = Double.parseDouble(data);
								sb.append("\t\t$this->mod_consumable_");
								sb.append(tableName);
								sb.append("_");
								sb.append(name);
								sb.append(" = $row['");
								sb.append(name);
								sb.append("'];\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							System.out.println("Not generating load for property: "+name);
						}
					}
					//load modified, genesis
					//modified, genesis
					sb.append("\t\t$this->mod_consumable_");
					sb.append(tableName);
					sb.append("_modified = $row['modified'];\n");
					sb.append("\t\t$this->mod_consumable_");
					sb.append(tableName);
					sb.append("_modified_genesis = $row['modified_genesis'];\n");
			        sb.append("\t}\n");
					
				} //end section element loop
				
			}//end section loop
			
			//generate variables, load from row
			
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	/**
	 * Generates fork code for ModMaker's fork handler
	 */
	public void generateForkPHP(){
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
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					String tableName = getTableName(sectionElement.getAttribute("name"));
					
					sb.append("\t//");
					sb.append(tableName.toUpperCase());
					sb.append("\n"); 
					//echo "<br>Beginning TABLENAME fork.";
					sb.append("\t//echo \"<br>Beginning ");
					sb.append(tableName);
					sb.append(" fork.\";\n");
					
					//$stmt = $dbh->prepare("SELECT * FROM modmaker_enemies_centurion WHERE mod_id=:fork_parent");
					sb.append("\t$stmt = $dbh->prepare(\"SELECT * FROM modmaker_consumable_");
					sb.append(tableName);
					sb.append(" WHERE mod_id=:fork_parent\");\n");
					//$stmt->bindValue(":fork_parent", $original_id);
					sb.append("\t$stmt->bindValue(\":fork_parent\", $original_id);\n");
					//$stmt->execute();
					sb.append("\t$stmt->execute();\n");
					//$NAMEs = $stmt->fetchAll();
					sb.append("\t$");
					sb.append(tableName);
					sb.append("row = $stmt->fetch();\n");
					//foreach ($NAMEs as $NAMErow) {
					sb.append("\t$stmt = $dbh->prepare(\"INSERT INTO modmaker_consumable_");
					sb.append(tableName);
					sb.append(" VALUES(:mod_id, ");

					NodeList propertyList = sectionElement.getChildNodes();
					//We are now at at the "sections" array.
					//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
					for (int k = 0; k < propertyList.getLength(); k++){
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String name = propNameToSqlName(prop.getAttribute("name"));
							if (name.equals("force")){
								name = "vforce";
							}
							if (name.equals("range")) {
								name = "vrange";
							}
							String data = prop.getTextContent();
							
							try {
								int ints = Integer.parseInt(data);
								sb.append(":");
								sb.append(name);
								sb.append(", ");
								continue;
							} catch (NumberFormatException e) {
								
							}
							
							try {
								double dubs = Double.parseDouble(data);
								sb.append(":");
								sb.append(name);
								sb.append(", ");
								continue;
							} catch (NumberFormatException e) {
								
							}
							System.out.println("Not generating fork substitution (1) for property: "+name);
							
							
						}
					}
					sb.append("false, :modified_genesis)\");\n");
					sb.append("\t$stmt->bindValue(\":mod_id\", $mod_id);\n");
					
					for (int k = 0; k < propertyList.getLength(); k++){
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String name = propNameToSqlName(prop.getAttribute("name"));
							if (name.equals("force")){
								name = "vforce";
							}
							if (name.equals("range")) {
								name = "vrange";
							}
							String data = prop.getTextContent();
							
							try {
								int ints = Integer.parseInt(data);
								sb.append("\t$stmt->bindValue(\":");
								sb.append(name);
								sb.append("\", $");
								sb.append(tableName);
								sb.append("row['");
								sb.append(name);
								sb.append("']);\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							
							try {
								double dubs = Double.parseDouble(data);
								sb.append("\t$stmt->bindValue(\":");
								sb.append(name);
								sb.append("\", $");
								sb.append(tableName);
								sb.append("row['");
								sb.append(name);
								sb.append("']);\n");
								continue;
							} catch (NumberFormatException e) {
								
							}
							System.out.println("Not generating fork substitution (2) for property: "+name);
							
						}
					}
					//bind modified_genesis
					sb.append("\t$stmt->bindValue(\":modified_genesis\", $");
					sb.append(tableName);
					sb.append("row['");
					sb.append("modified_genesis");
					sb.append("']);\n");
					
					//if (!$stmt->execute()) {
					sb.append("\tif (!$stmt->execute()) {\n");
					//echo "NAME FORK FAIL."
					sb.append("\t\techo \"");
					sb.append(tableName);
					sb.append(" fork failed:<br>\";\n");
					sb.append("\t\tprint_r($stmt->errorInfo());\n");
					sb.append("\t\treturn ob_get_clean();\n");
					// } else {
					sb.append("\t} else {\n");
					//echo "<br>Finished NAME fork
					sb.append("\t\t//echo \"<br>Finished ");
					sb.append(tableName);
					sb.append(" fork.\";\n");
					//closing brackets
					sb.append("\t}\n\n");
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		output.setText(sb.toString());
	}
	
	private void generateVariables() {		
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				sb.append("\t/*");
				sb.append(tableSuffix);
				sb.append("*/\n");
				
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (name.equals("force")){
							name = "vforce";
						}
						if (name.equals("range")) {
							name = "vrange";
						}
						
						try {
							int ints = Integer.parseInt(data);
							sb.append("\tpublic $mod_consumable_");
							sb.append(tableSuffix);
							sb.append("_");
							sb.append(name);
							sb.append(" = null;\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							sb.append("\tpublic $mod_consumable_");
							sb.append(tableSuffix);
							sb.append("_");
							sb.append(name);
							sb.append(" = null;\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.out.println("Not generating variable for property: "+name);
					}
				}
				sb.append("\tpublic $mod_consumable_");
				sb.append(tableSuffix);
				sb.append("_modified = null;\n");
				
				sb.append("\tpublic $mod_consumable_");
				sb.append(tableSuffix);
				sb.append("_modified_genesis = null;\n");
			}
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generatePHPValidation() {
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				sb.append("\tcase \"");
				sb.append(tableSuffix);
				sb.append("\":\n");
				sb.append("\t\t$status = array();\n");
				sb.append("\t\t$updateinfo = array();\n");
				
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (name.equals("force")){
							name = "vforce";
						}
						if (name.equals("range")) {
							name = "vrange";
						}
						
						//ints, dubs
						try {
							int ints = Integer.parseInt(data);
							sb.append("\t\t//");
							sb.append(name);
							sb.append("\n");
							
							sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_int($_POST['");
							sb.append(name);
							sb.append("']);\n");

							sb.append("\t\tif (is_null($shouldadd)){\n");
							sb.append("\t\t\t$updateinfo['");
							sb.append(name);

							sb.append("'] = $_POST['");
							sb.append(name);
							sb.append("'];\n");
							
							sb.append("\t\t} else {\n");
							sb.append("\t\t\tarray_push($status, \"");
							sb.append(name);
							sb.append(" \".$shouldadd);\n");
							sb.append("\t\t}\n\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							sb.append("\t\t//");
							sb.append(name);
							sb.append("\n");
							
							sb.append("\t\t$shouldadd = validate_greater_than_or_equal_to_zero_float($_POST['");
							sb.append(name);
							sb.append("']);\n");

							sb.append("\t\tif (is_null($shouldadd)){\n");
							sb.append("\t\t\t$updateinfo['");
							sb.append(name);
							sb.append("'] = $_POST['");
							sb.append(name);
							sb.append("'];\n");
							
							sb.append("\t\t} else {\n");
							sb.append("\t\t\tarray_push($status, \"");
							sb.append(name);
							sb.append(" \".$shouldadd);\n");
							sb.append("\t\t}\n\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.err.println("Not generating validation for: "+name);
					}
				}

				sb.append("\t\t$mod->loadConsumableFunctions();\n");
				sb.append("\t\t$result = $mod->consumables->updateConsumable('");
				sb.append(tableSuffix);
				sb.append("', $updateinfo);\n");
				
				sb.append("\t\tif (is_null($result) and count($status)<=0) {\n");
				sb.append("\t\t\t$_SESSION['consumables_update'] = \"");
				sb.append(tableSuffix);
				sb.append(" updated.\";\n");
				
				sb.append("\t\t\theader('Location: /modmaker/edit/'.$id.'/consumables');\n");;
				sb.append("\t\t\tdie();\n");
				sb.append("\t\t} else {\n");
				sb.append("\t\t\tarray_push($status, $result);\n");
				sb.append("\t\t\t$_SESSION['");
				sb.append(tableSuffix);
				sb.append("_status'] = $status;\n");
				sb.append("\t\t\theader('Location: /modmaker/edit/'.$id.'/consumables/");
				sb.append(tableSuffix);
				sb.append("');\n");
				sb.append("\t\t\tdie();\n");
				sb.append("\t\t}\n");
				sb.append("\t\tbreak;\n\n");
			}
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generateJSValidation(){ 
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try { //Load document
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				if (section.getLength() > 1) {
					sb = new StringBuilder(); //clear it.
				}
				sb.append("$(document).ready(function(){\n");
				sb.append("\t$('#form input[type=\"text\"]').tooltipster({\n");
				sb.append("\t\ttrigger: 'custom', // default is 'hover' which is no good here\n");
				sb.append("\t\tonlyOne: false,    // allow multiple tips to be open at a time\n");
				sb.append("\t\tposition: 'top',\n");
				sb.append("\t\tanimation: 'grow'\n");
				sb.append("\t});\n");
				sb.append("\t//form validation rules\n");
				sb.append("\t$(\"#form\").validate({\n");
				sb.append("\t\terrorPlacement: function (error, element) {\n");
				sb.append("\t\t\t$(element).tooltipster('update', $(error).text());\n");
				sb.append("\t\t\t$(element).tooltipster('show');\n");
				sb.append("\t\t},\n");
				sb.append("\t\tsuccess: function (label, element) {\n");
				sb.append("\t\t\t$(element).tooltipster('hide');\n");
				sb.append("\t\t},\n");
				sb.append("\t\trules: {\n");

				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (name.equals("force")){
							name = "vforce";
						}
						if (name.equals("range")) {
							name = "vrange";
						}
						
						//ints, dubs
						try {
							int ints = Integer.parseInt(data);
							sb.append("\t\t\t");
							sb.append(name);
							sb.append(": {\n");
							sb.append("\t\t\t\trequired: true,\n");
							sb.append("\t\t\t\tdigits: true,\n");
							sb.append("\t\t\t\tmin: 0\n");
							sb.append("\t\t\t},\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							sb.append("\t\t\t");
							sb.append(name);
							sb.append(": {\n");
							sb.append("\t\t\t\trequired: true,\n");
							sb.append("\t\t\t\tmin: 0\n");
							sb.append("\t\t\t},\n");
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.err.println("Not generating JS Validation for "+name);
					}
				}

				sb.append("\t\t}\n");
				sb.append("\t});\n");
				sb.append("});\n");
				if (section.getLength() > 1) {
					//File js = new File("js/");
					//js.mkdirs();
					FileUtils.writeStringToFile(new File("js/"+tableSuffix+".js"), sb.toString());
				}
			}
			if (section.getLength() > 1) {
				output.setText("Files written to js/ folder.");
			} else {
				output.setText(sb.toString());
			}
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private void generateHTMLInputs() {
		String input_text = getInput();
		String wrapper = "\t\t\t\t<!-- AUTO GENERATED -->\n"+
							"\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"+
							"\t\t\t\t\t<img class=\"guide purple_card\" src=\"/images/common/no_image.png\">\n"+
							"\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">Auto Generated Properties</h2>\n"+
							"\t\t\t\t\t<p>These properties need to be moved to their proper boxes.</p>\n"+
							"INPUTS_PLACEHOLDER"+
							"\t\t\t\t</div>";
		String inputTemplate = "\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">VARNAME</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: <\\?=\\$defaultsmod->consumables->mod_consumable_TABLENAME_VARNAME;?></span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"VARNAME\" class=\"short_input\" type=\"text\" name=\"VARNAME\" placeholder=\"VARNAME\" value=\"<?=\\$mod->consumables->mod_consumable_TABLENAME_VARNAME;?>\">\n"+
				"\t\t\t\t\t</div>";
		String detonationParamsTemplate = "<!-- DETONATIONVARNAME PARAMETERS  -->\n"+
				"\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"+
				"\t\t\t\t\t<img class=\"guide hard\" src=\"/images/modmaker/consumables/TABLENAME/explosion.jpg\">\n"+
				"\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">Detonation Parameters</h2>\n"+
				"\t\t\t\t\t<p>Detonation paramters determine what gets hit when TABLENAME detonate.</p>\n"+
				"\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Blocked By Objects</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: BLOCKED_BY_OBJECTS</span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_blockedbyobjects\" type=\"checkbox\" name=\"DETONATIONVARNAME_blockedbyobjects\" <?=($mod->consumables->mod_consumable_TABLENAME_DETONATIONVARNAME_blockedbyobjects) ? \"checked\" : \"\"?>>\n"+
				"\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Distance Sorted</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: DISTANCE_SORTED</span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_distancesorted\" type=\"checkbox\" name=\"DETONATIONVARNAME_distancesorted\" <?=($mod->consumables->mod_consumable_TABLENAME_DETONATIONVARNAME_distancesorted) ? \"checked\" : \"\"?>>\n"+
				"\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Dead Characters</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_DEAD_CHARS</span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactdeadpawns\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactdeadpawns\" <?=($mod->consumables->mod_consumable_TABLENAME_DETONATIONVARNAME_impactdeadpawns) ? \"checked\" : \"\"?>>\n"+
				"\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Friendlies</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_FRIENDS</span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactfriends\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactfriends\" <?=($mod->consumables->mod_consumable_TABLENAME_DETONATIONVARNAME_impactfriends) ? \"checked\" : \"\"?>>\n"+
				"\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t<div class=\"modmaker_entry\">\n"+
				"\t\t\t\t\t\t<div class=\"defaultbox\">\n"+
				"\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Placeables</span>\n"+
				"\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_PLACEABLES</span>\n"+
				"\t\t\t\t\t\t</div>\n"+
				"\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactplaceables\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactplaceables\" <?=($mod->consumables->mod_consumable_TABLENAME_DETONATIONVARNAME_impactplaceables) ? \"checked\" : \"\"?>>\n"+
				"\t\t\t\t\t</div>\n"+
				"CONEANGLE"+
				"\t\t\t\t\n</div>";
		try { //Load document
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			ArrayList<String> inputs = new ArrayList<String>();
			ArrayList<String> detonationBlocks = new ArrayList<String>();
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				String tableSuffix = getTableName(sectionElement.getAttribute("name"));
				NodeList propertyList = sectionElement.getChildNodes();
				//We are now at at the "sections" array.
				//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++){
					//for every property in this filenode (of the data to merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						
						String data = prop.getTextContent();
						String name = propNameToSqlName(prop.getAttribute("name"));
						if (name.equals("force")){
							name = "vforce";
						}
						if (name.equals("range")) {
							name = "vrange";
						}
						
						//ints, dubs
						try {
							int ints = Integer.parseInt(data);
							inputs.add(inputTemplate.replaceAll("VARNAME", name).replaceAll("TABLENAME", tableSuffix));

							continue;
						} catch (NumberFormatException e) {
							
						}
						
						try {
							double dubs = Double.parseDouble(data);
							inputs.add(inputTemplate.replaceAll("VARNAME", name).replaceAll("TABLENAME", tableSuffix));
							continue;
						} catch (NumberFormatException e) {
							
						}
						System.err.println("Not generating input for: "+name);
					}
				}
			}
			
			//build string
			StringBuilder sb = new StringBuilder();
			for (String input : inputs) {
				sb.append(input);
				sb.append("\n");
			}
			String wrap = wrapper.replaceAll("INPUTS_PLACEHOLDER", sb.toString());
			for (String det : detonationBlocks) {
				wrap = wrap+det;
			}
			output.setText(wrap);
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
	
	private String getInput(){
		String wrappedXML = input.getText();
		wrappedXML = "<consumable>"+wrappedXML+"</consumable>";
		return wrappedXML;
	}
	
	private String propNameToSqlName(String propName) {
		propName = propName.replaceAll("\\[", "");
		return propName.replaceAll("\\]", "");
	}
	
	private String getTableName(String sectionName) {
		String str = sectionName.substring(sectionName.indexOf('.')+1);
		
		str = str.replaceAll("sfxgameeffect_matchconsumable_", "");
		if (str.charAt(0) == '_') {
			return str.substring(1);
		} else {
			return str;
		}
	}
	
	private String getLoadName(String tableName) {
		return tableName.replaceAll("_", "");
	}
}
