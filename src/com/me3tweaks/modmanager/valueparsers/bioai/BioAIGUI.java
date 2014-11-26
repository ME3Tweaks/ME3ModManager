package com.me3tweaks.modmanager.valueparsers.bioai;

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

public class BioAIGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JButton parse, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generateUpdate, copy;
	JTextField tableNameField;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;


	public static void main(String[] args) throws IOException {
		new BioAIGUI();
	}

	public BioAIGUI() {
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
				"<html>ME3CMM BioAI Parser<br>Enter the BioAI text below, as XML, starting with the &lt;Section&gt; and the closing tag as the end.</html>");
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
		tableNameField = new JTextField("tablesuffix");
		generateTable = new JButton("Generate TBL");
		generateTable.addActionListener(this);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		SQLPanel.add(tableNameField, BorderLayout.NORTH);
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
			// parse it.
			String input_text = input.getText();
			try {
				StringBuilder sb = new StringBuilder();
				
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
						System.out.println("Found property: "+prop.getAttribute("name"));
						try {
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
						}
					}
				}
				output.setText(sb.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
				output.setText(ex.toString());
			}
		} else if (e.getSource() == generateInsert) {
			generateSQL();
		 }else if (e.getSource() == generateTable) {
			generateTable();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection (myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
			clpbrd.setContents (stringSelection, null);
		}
	}
	
	private void generateSQL(){
		String[] tables = new String[] {
				"powers_aihacking",
				"powers_supplyturret",
				"startupmovies",
	            "wavebudgets",
	            "wavelists",
	            "wavecosts",
	            "possessionwaves"
	           };
		StringBuilder sb = new StringBuilder();
		String stockstr = "DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'3\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'4\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'5\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'6\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'7\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'10\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'11\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'12\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'13\';\n"+
				"DELETE FROM `me3tweaks`.`modmaker_PLACEHOLDER` WHERE `mod_id`=\'19\';\n"+

				"ALTER TABLE `me3tweaks`.`modmaker_PLACEHOLDER` \n"+
				"ADD FOREIGN KEY (`mod_id`)\n"+
				"  REFERENCES `me3tweaks`.`modmaker_mods` (`mod_id`)\n"+
				"  ON DELETE CASCADE\n"+
				"  ON UPDATE NO ACTION;";
		for (String table : tables) {
			String appendStr = stockstr.replaceAll("PLACEHOLDER", table);
			sb.append(appendStr);
			sb.append("\n\n");
		}
		output.setText(sb.toString());
		
	}

	private void generateTable() {
		// g
		String input_text = input.getText();
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(tableNameField.getText());
		sb.append("*/\n");
		sb.append("CREATE TABLE modmaker_aiweapon_");
		sb.append(tableNameField.getText());
		sb.append(" (\n");
		sb.append("\t mod_id INT NOT NULL\n");
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
					try {
						new Range(prop.getTextContent()); //check if its a range. discard variable
						sb.append("\t");
						sb.append(prop.getAttribute("name"));
						sb.append("_min FLOAT NOT NULL, /*");
						sb.append(prop.getAttribute("name"));
						sb.append(" X VAL*/\n");
						sb.append("\t");
						sb.append(prop.getAttribute("name"));
						sb.append("_max FLOAT NOT NULL, /*");
						sb.append(prop.getAttribute("name"));
						sb.append(" Y VAL*/\n");
					} catch (StringIndexOutOfBoundsException strException){
						//its a direct str
						sb.append("\t");
						sb.append(prop.getAttribute("name"));
						sb.append(" FLOAT NOT NULL, /*");
						sb.append(prop.getAttribute("name"));
						sb.append("*/\n");
					}
				}
			}
			sb.append("\tmodified boolean NOT NULL,\n");
			sb.append("\tmodified_genesis boolean NOT NULL,\n");
			sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
			sb.append("\tPRIMARY KEY(mod_id)\n");
			sb.append(");");
			output.setText(sb.toString());
		} catch (Exception e){
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}
}
