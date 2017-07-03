package com.me3tweaks.modmanager.valueparsers.bioweapon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.me3tweaks.modmanager.ModManagerWindow;

public class BioWeaponGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JButton parse, generateInsert, generateTable, generateFork, generateLoad, generateVariables, generatePublish, copy;
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;
	String[] blacklistedProperties = { "adhesionrot", "aimcorrectionamount", "aimmodes", "ammoprettyname", "allowableweaponmods", "badhesionduringcam", "badhesionenabled",
			"beaminterpspeed", "beaminterptime", "bfrictiondistancescalingenabled", "bfrictionenabled", "bnotregularweapongui", "busesnipercam", "bzoomsnapenabled",
			"caminputadhesiondamping", "clientsidehitleeway", "clientsidehitmaxangle", "clientsidehitmaxangleclose", "clientsidehitmaxdistclose", "clientsidehitmaxdistreallyclose",
			"coverleanexitdelay", "coverleanpositions", "coverpartialleanexitdelay", "damagehench", "ejectshellcasingtimeratio", "fadingparameters", "frictionmultiplierrange",
			"frictiontargetoffset", "generaldescription", "guiclassname", "guiimage", "hearnoisetimeout", "iconref", "idealmaxrange", "idealminrange", "idealtargetdistance",
			"impactforcemodifier", "impactrelevancedistance", "magneticcorrectionthresholdangle", "maxadhesiondistance", "maxfrictiondistance", "maxlateraladhesiondist",
			"maxmagneticcorrectionangle", "maxzoomaimerror", "maxzoomcrosshairrange", "maxzoomsnapdistance", "meleedamagemodifier", "minadhesiondistance", "minadhesionvelocity",
			"mincrosshairrange", "minfrictiondistance", "modcrosshairmultiplier", "movementspeedoverrides", "needreloadnotify", "noammofiresounddelay", "notificationimage",
			"peakfrictiondistance", "peakfrictionheightscale", "peakfrictionradiusscale", "protheandamagemultiplier", "prettyname", "rateoffireai", "reactionchancemodifier",
			"readoutoffset", "recoilcap", "recoilfadespeed", "recoilinterpspeed", "recoilminfadepitch", "recoilminfadeyaw", "recoilpitchfrequncy", "recoilpitchoscillation",
			"recoilyawfrequency", "recoilyawnoise", "recoilyawscale", "shortdescription", "shortprettyname", "steamsoundthreshold", "tracerspawnoffset", "timetoheatupai",
			"weaponacquiredid_ngp", "weaponacquiredid", "weaponmodbodycolours", "weaponmodemissivecolours", "weaponmodgripcolours", "weaponmodmeshoverrides",
			"zoomaccfireinterpspeed", "zoomaccfirepenalty", "zoomfov", "zoomsnaplist", "zoomrecoilcap" };

	String[] alwaysIncludedProperties = { "binfiniteammo" };

	public static void main(String[] args) throws IOException {
		new BioWeaponGUI();
	}

	public BioWeaponGUI() {
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("ME3CMM BioWeapon Parser Tool");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		setMinimumSize(new Dimension(490, 500));
		setPreferredSize(new Dimension(490, 500));
		JPanel bioweaponGUI = new JPanel(new BorderLayout());
		JLabel instructionsLabel = new JLabel(
				"<html>ME3CMM BioWeapon Parser<br>Enter the BioWeapon block XML text below, starting with a &lt;Section&gt; tag and continuing through all weapons you wish to parse, and the closing tag of the final weapon as the end.</html>");
		bioweaponGUI.add(instructionsLabel, BorderLayout.NORTH);
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

		parse = new JButton("Parse");
		parse.addActionListener(this);

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
		SQLPanel.add(generateFork, BorderLayout.SOUTH);

		//PHP stuff
		JPanel PHPPanel = new JPanel(new BorderLayout());
		generateVariables = new JButton("Generate Vars");
		generateVariables.addActionListener(this);
		generateLoad = new JButton("Generate Load");
		generateLoad.addActionListener(this);
		generatePublish = new JButton("Generate Publish");
		generatePublish.addActionListener(this);

		PHPPanel.add(generateVariables, BorderLayout.NORTH);
		PHPPanel.add(generateLoad, BorderLayout.CENTER);
		PHPPanel.add(generatePublish, BorderLayout.SOUTH);

		JPanel modmakerPanel = new JPanel(new BorderLayout());
		modmakerPanel.add(SQLPanel, BorderLayout.WEST);
		modmakerPanel.add(PHPPanel, BorderLayout.CENTER);
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
		inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputScrollPane.setPreferredSize(new Dimension(250, 250));

		copy = new JButton("Copy");
		copy.addActionListener(this);
		outputPanel.add(outputLabel, BorderLayout.NORTH);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);
		outputPanel.add(copy, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
		splitPane.setDividerLocation(150 + splitPane.getInsets().top);

		bioweaponGUI.add(splitPane, BorderLayout.CENTER);
		getContentPane().add(bioweaponGUI);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		generateFork.setEnabled(false);
		generateLoad.setEnabled(false);
		generatePublish.setEnabled(false);
		generateTable.setEnabled(false);
		generateVariables.setEnabled(false);
		generateInsert.setEnabled(false);

		generateFork.setToolTipText("SQL output is disabled");
		generateLoad.setToolTipText("SQL output is disabled");
		generatePublish.setToolTipText("SQL output is disabled");
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == parse) {
			parse();
		} else if (e.getSource() == generateInsert) {
			generateSQL();
		} else if (e.getSource() == generateTable) {
			generateTable();
		} else if (e.getSource() == generateFork) {
			generateForkPHP();
		} else if (e.getSource() == generatePublish) {
			generatePublish();
		} else if (e.getSource() == generateLoad) {
			generateLoad();
		} else if (e.getSource() == generateVariables) {
			generateVariables();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection(myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	private void parse() {
		// parse it.
		HashMap<String, Integer> variableCount = new HashMap<String, Integer>();
		String input_text = getInput();
		StringBuilder sb = new StringBuilder();
		try {

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList section = doc.getElementsByTagName("Section");
			System.out.println("START");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					//We are now at at the "sections" array.

					NodeList propertyList = sectionElement.getChildNodes();
					for (int k = 0; k < propertyList.getLength(); k++) {

						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							if (isIgnoredProperty(prop.getAttribute("name"))) {
								continue;
							}
							if (variableCount.get(prop.getAttribute("name")) != null) {
								Integer currentCount = variableCount.get(prop.getAttribute("name"));
								currentCount++;
								variableCount.put(prop.getAttribute("name"), currentCount);
							} else {
								variableCount.put(prop.getAttribute("name"), 1);
							}
						}
					}
				}
			}
			List<String> keys = new ArrayList<String>(variableCount.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				sb.append(key);
				sb.append(": ");
				sb.append(variableCount.get(key));
				sb.append("\n");
			}
			output.setText(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			output.setText(ex.toString());
		}
	}

	private void generateSQL() {
		// g
		String input_text = getInput();
		System.out.println(input_text);
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
					//We are now at at a section element. this is a table
					String tableName = getTableName(sectionElement.getAttribute("name"));
					sb.append("/*");
					sb.append(tableName);
					sb.append("*/\n");
					sb.append("INSERT INTO modmaker_weapon");
					sb.append(tableName);
					sb.append(" VALUES (\n");
					sb.append("\t1, /*GENESIS MOD ID*/\n");
					NodeList propertyList = sectionElement.getChildNodes();
					for (int k = 0; k < propertyList.getLength(); k++) {
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = prop.getAttribute("name");
							if (isIgnoredProperty(name)) {
								continue;
							}
							boolean propertyParsed = false;
							try {
								System.out.println("Trying to parse as range: " + data);
								Range r = new Range(data); //check if its a range. discard variable
								//min
								sb.append("\t");
								sb.append(r.doubleX);
								sb.append(", /*");
								sb.append(name);
								sb.append("_min*/\n");

								//max
								sb.append("\t");
								sb.append(r.doubleY);
								sb.append(", /*");
								sb.append(name);
								sb.append("_max*/\n");
								propertyParsed = true;
							} catch (StringIndexOutOfBoundsException strException) {

							}
							//BOOLEAN
							if (!propertyParsed) {
								if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")) {
									System.out.println("Trying to parse as boolean: " + data);
									sb.append("\t");
									sb.append(data);
									sb.append(", /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								}
							}
							//INTEGER
							if (!propertyParsed) {
								System.out.println("Trying to parse as INT: " + data);
								try {
									Integer.parseInt(data);
									sb.append("\t");
									sb.append(data);
									sb.append(", /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}

							//FLOAT
							if (!propertyParsed) {
								System.out.println("Trying to parse as float: " + data);
								try {
									double d = Double.parseDouble(data);
									sb.append("\t");
									sb.append(d);
									sb.append(", /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}
							if (!propertyParsed) {
								System.err.println("Did not parse " + name + ":" + data);
							}

						}
					}
				}
				sb.append("\tfalse, /*modified*/\n");
				sb.append("\tfalse /*modified_genesis*/\n");
				sb.append(");\n\n");
			}
			/*
			 * sb.append("\tmodified boolean NOT NULL,\n");
			 * sb.append("\tmodified_genesis boolean NOT NULL,\n"); sb.
			 * append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"
			 * ); //end of SQL statement sb.append("\tPRIMARY KEY(mod_id)\n");
			 * sb.append(") ENGINE=INNODB;");
			 */
			output.setText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	private void generateTable() {
		// g
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
					//We are now at at a section element. this is a table
					String tableName = getTableName(sectionElement.getAttribute("name"));
					sb.append("/*");
					sb.append(tableName);
					sb.append("*/\n");
					sb.append("CREATE TABLE modmaker_weapon");
					sb.append(tableName);
					sb.append(" (\n");
					sb.append("\tmod_id INT NOT NULL,\n");

					NodeList propertyList = sectionElement.getChildNodes();
					for (int k = 0; k < propertyList.getLength(); k++) {
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = prop.getAttribute("name");
							if (isIgnoredProperty(name)) {
								continue;
							}
							boolean propertyParsed = false;
							try {
								System.out.println("Trying to parse as range: " + data);
								Range r = new Range(data); //check if its a range. discard variable
								sb.append("\t");
								sb.append(name);
								sb.append("_min ");
								sb.append((r.isInt) ? "INT" : "FLOAT");
								sb.append(" NOT NULL, /*");
								sb.append(name);
								sb.append(" X VAL*/\n");
								sb.append("\t");
								sb.append(name);
								sb.append("_max ");
								sb.append((r.isInt) ? "INT" : "FLOAT");
								sb.append(" NOT NULL, /*");
								sb.append(name);
								sb.append(" Y VAL*/\n");
								propertyParsed = true;
							} catch (StringIndexOutOfBoundsException strException) {

							}
							//BOOLEAN
							if (!propertyParsed) {
								if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")) {
									System.out.println("Trying to parse as boolean: " + data);
									sb.append("\t");
									sb.append(name);
									sb.append(" BOOLEAN NOT NULL, /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								}
							}
							//INTEGER
							if (!propertyParsed) {
								System.out.println("Trying to parse as INT: " + data);
								try {
									Integer.parseInt(data);
									sb.append("\t");
									sb.append(name);
									sb.append(" INT NOT NULL, /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}

							//FLOAT
							if (!propertyParsed) {
								System.out.println("Trying to parse as float: " + data);
								try {
									Double.parseDouble(data);
									sb.append("\t");
									sb.append(name);
									sb.append(" FLOAT NOT NULL, /*");
									sb.append(name);
									sb.append("*/\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}
							if (!propertyParsed) {
								System.err.println("Did not parse " + name + ":" + data);
							}

						}
					}
				}
				sb.append("\tmodified boolean NOT NULL,\n");
				sb.append("\tmodified_genesis boolean NOT NULL,\n");
				sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); //end of SQL statement
				sb.append("\tPRIMARY KEY(mod_id)\n");
				sb.append(") ENGINE=INNODB;\n\n");
			}
			/*
			 * sb.append("\tmodified boolean NOT NULL,\n");
			 * sb.append("\tmodified_genesis boolean NOT NULL,\n"); sb.
			 * append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"
			 * ); //end of SQL statement sb.append("\tPRIMARY KEY(mod_id)\n");
			 * sb.append(") ENGINE=INNODB;");
			 */
			output.setText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	private String getTableName(String sectionName) {
		if (sectionName.equals("sfxgame.sfxweapon")) {
			return "_sfxweapon";
		}
		sectionName = sectionName.substring(sectionName.indexOf("."));
		sectionName = sectionName.substring(sectionName.indexOf("_"));
		return sectionName;
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
					String weaponName = getWeaponName(tableName);
					String dlcElement = "$basegameBioWeaponElements";
					if (path.contains("mp1")) {
						dlcElement = "$mp1BioWeaponElements";
					}
					if (path.contains("mp2")) {
						dlcElement = "$mp2BioWeaponElements";
					}
					if (path.contains("mp3")) {
						dlcElement = "$mp3BioWeaponElements";
					}
					if (path.contains("mp4")) {
						dlcElement = "$mp4BioWeaponElements";
					}
					if (path.contains("mp5")) {
						dlcElement = "$mp5BioWeaponElements";
					}

					sb.append("//");
					sb.append(weaponName);
					sb.append("\n");
					sb.append("if ($this->mod->weapon->mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified_genesis) {");
					sb.append("\n");

					NodeList propertyList = sectionElement.getChildNodes();
					//We are now at at the attribute list.
					for (int k = 0; k < propertyList.getLength(); k++) {
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = prop.getAttribute("name");
							System.out.println("Getting type for: " + path + " " + name);
							if (isIgnoredProperty(name)) {
								continue;
							}
							int type = Integer.parseInt(prop.getAttribute("type"));
							boolean propertyParsed = false;
							try {
								new Range(prop.getTextContent()); //check if its a range. discard variable
								sb.append("\t");
								sb.append("array_push(");
								sb.append(dlcElement);
								sb.append(", $this->createProperty(\"");
								sb.append(path);
								sb.append("\", \"");
								sb.append(prop.getAttribute("name"));
								sb.append("\", $this->createRange(");
								//min
								sb.append("$this->mod->weapon->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(name);
								sb.append("_min,");
								//max
								sb.append("$this->mod->weapon->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(name);
								sb.append("_max,");
								if (data.contains("f")) {
									//its a float
									sb.append(" true, true), ");
								} else {
									//int
									sb.append(" false, false), ");
								}
								sb.append(type);
								sb.append("));");
								sb.append("\n");
								propertyParsed = true;
							} catch (StringIndexOutOfBoundsException strException) {

							}
							//BOOLEAN
							if (!propertyParsed) {
								if (data.toLowerCase().equals("true") || data.toLowerCase().equals("false")) {
									sb.append("\t");
									sb.append("array_push(");
									sb.append(dlcElement);
									sb.append(", $this->createProperty(\"");
									sb.append(path);
									sb.append("\", \"");
									sb.append(name);
									sb.append("\", ($this->mod->weapon->mod_weapon_");
									sb.append(weaponName);
									sb.append("_");
									sb.append(name);
									sb.append(" == 1) ? \"true\" : \"false\",");
									sb.append(type);
									sb.append("));");
									sb.append("\n");
									propertyParsed = true;
								}
							}
							//INTEGER
							if (!propertyParsed) {
								try {
									Integer.parseInt(data);
									sb.append("\t");
									sb.append("array_push(");
									sb.append(dlcElement);
									sb.append(", $this->createProperty(\"");
									sb.append(path);
									sb.append("\", \"");
									sb.append(name);
									sb.append("\", $this->mod->weapon->mod_weapon_");
									sb.append(weaponName);
									sb.append("_");
									sb.append(name);
									sb.append(",");
									sb.append(type);
									sb.append("));");
									sb.append("\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}

							//FLOAT
							if (!propertyParsed) {
								try {
									Double.parseDouble(data);
									sb.append("\t");
									sb.append("array_push(");
									sb.append(dlcElement);
									sb.append(", $this->createProperty(\"");
									sb.append(path);
									sb.append("\", \"");
									sb.append(name);
									sb.append("\", $this->mod->weapon->mod_weapon_");
									sb.append(weaponName);
									sb.append("_");
									sb.append(name);
									sb.append(".\"f\", ");
									sb.append(type);
									sb.append("));");
									sb.append("\n");
									propertyParsed = true;
								} catch (NumberFormatException nfe) {

								}
							}
							if (!propertyParsed) {
								System.err.println("Did not parse " + name + ":" + data);
							}
						} //end if property element
					} //end property loop
					sb.append("}\n\n");
				} //end section element
			} //end section loop

			output.setText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	private void generateLoad() {
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
					String weaponName = getWeaponName(tableName);
					//public function loadWeaponX() {
					sb.append("\tpublic function loadWeapon");
					sb.append(Character.toUpperCase(weaponName.charAt(0)) + weaponName.toLowerCase().substring(1)); //have only first letter capitalized.
					sb.append("(){\n");
					//inner vars
					NodeList propertyList = sectionElement.getChildNodes();
					for (int k = 0; k < propertyList.getLength(); k++) {
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = prop.getAttribute("name");

							if (isIgnoredProperty(name)) {
								continue;
							}

							try {
								new Range(data);
								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_min = null;\n");

								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_max = null;\n");
							} catch (StringIndexOutOfBoundsException strException) {
								//its not a range, just set the val to null
								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append(" = null;\n");
							}
						} //end scanned element node
					} //end property loop

					//clear modified, genesis
					sb.append("\t\t$this->mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified = null;\n");
					sb.append("\t\t$this->mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified_genesis = null;\n\n");

					sb.append("\t\trequire($_SERVER['DOCUMENT_ROOT'].\"/db-middleman.php\");\n");

					//load values from DB
					sb.append("\t\t//Load values from DB\n");

					//select * from modmaker_enemies_NAME where mod_id=:mod
					sb.append("\t\t$sql = \"SELECT * FROM modmaker_weapon");
					sb.append(tableName);
					sb.append(" WHERE mod_id=:mod_id\";\n");

					sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
					sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
					sb.append("\t\t$stmt->execute();\n");
					sb.append("\t\t$row = $stmt->fetch();\n");

					//iterate over properties again, for loading
					for (int k = 0; k < propertyList.getLength(); k++) {
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;

							if (isIgnoredProperty(prop.getAttribute("name"))) {
								continue;
							}
							try {
								new Range(prop.getTextContent());
								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_min = $row['");
								sb.append(prop.getAttribute("name"));
								sb.append("_min'];\n");

								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_max = $row['");
								sb.append(prop.getAttribute("name"));
								sb.append("_max'];\n");
							} catch (StringIndexOutOfBoundsException strException) {
								//its a direct str
								sb.append("\t\t$this->mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append(" = $row['");
								sb.append(prop.getAttribute("name"));
								sb.append("'];\n");
							}
						}
					}
					//load modified, genesis
					//modified, genesis
					sb.append("\t\t$this->mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified = $row['modified'];\n");
					sb.append("\t\t$this->mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified_genesis = $row['modified_genesis'];\n");
					sb.append("\t}\n");

				} //end section element loop

			} //end section loop

			//generate variables, load from row

			output.setText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	private String getWeaponName(String tableName) {
		return tableName.replaceAll("_", "");
	}

	/**
	 * Generates fork code for ModMaker's fork handler
	 */
	public void generateForkPHP() {
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
					String weaponName = getWeaponName(tableName);

					sb.append("\t//");
					sb.append(weaponName.toUpperCase());
					sb.append("\n");
					//echo "<br>Beginning TABLENAME fork.";
					sb.append("\t//echo \"<br>Beginning ");
					sb.append(weaponName);
					sb.append(" fork.\";\n");

					//$stmt = $dbh->prepare("SELECT * FROM modmaker_enemies_centurion WHERE mod_id=:fork_parent");
					sb.append("\t$stmt = $dbh->prepare(\"SELECT * FROM modmaker_weapon");
					sb.append(tableName);
					sb.append(" WHERE mod_id=:fork_parent\");\n");
					//$stmt->bindValue(":fork_parent", $original_id);
					sb.append("\t$stmt->bindValue(\":fork_parent\", $original_id);\n");
					//$stmt->execute();
					sb.append("\t$stmt->execute();\n");
					//$NAMEs = $stmt->fetchAll();
					sb.append("\t$");
					sb.append(weaponName);
					sb.append("row = $stmt->fetch();\n");
					//foreach ($NAMEs as $NAMErow) {
					sb.append("\t$stmt = $dbh->prepare(\"INSERT INTO modmaker_weapon");
					sb.append(tableName);
					sb.append(" VALUES(:mod_id, ");

					NodeList propertyList = sectionElement.getChildNodes();
					//We are now at at the "sections" array.
					//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
					for (int k = 0; k < propertyList.getLength(); k++) {
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String name = prop.getAttribute("name");
							if (isIgnoredProperty(name)) {
								continue;
							}
							try {
								//sb.append("\t$");
								new Range(prop.getTextContent());
								sb.append(":");
								sb.append(name);
								sb.append("_min, :");
								sb.append(name);
								sb.append("_max");
							} catch (StringIndexOutOfBoundsException strException) {
								sb.append(":");
								sb.append(name);
							}
							sb.append(", ");
						}
					}
					sb.append("false, :modified_genesis)\");\n");
					sb.append("\t$stmt->bindValue(\":mod_id\", $mod_id);\n");

					for (int k = 0; k < propertyList.getLength(); k++) {
						//for every property in this filenode (of the data to merge)...
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String name = prop.getAttribute("name");
							if (isIgnoredProperty(name)) {
								continue;
							}
							try {
								//min
								new Range(prop.getTextContent());
								sb.append("\t$stmt->bindValue(\":");
								sb.append(name);
								sb.append("_min\", $");
								sb.append(weaponName);
								sb.append("row['");
								sb.append(name);
								sb.append("_min']);\n");
								//max
								sb.append("\t$stmt->bindValue(\":");
								sb.append(name);
								sb.append("_max\", $");
								sb.append(weaponName);
								sb.append("row['");
								sb.append(name);
								sb.append("_max']);\n");
							} catch (StringIndexOutOfBoundsException strException) {
								sb.append("\t$stmt->bindValue(\":");
								sb.append(name);
								sb.append("\", $");
								sb.append(weaponName);
								sb.append("row['");
								sb.append(name);
								sb.append("']);\n");
							}
						}
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
					sb.append("\t\techo \"");
					sb.append(weaponName);
					sb.append(" FORK FAIL: \".print_r($stmt->errorInfo());\n");
					sb.append("\t\treturn ob_get_clean();\n");
					// } else {
					sb.append("\t} else {\n");
					//echo "<br>Finished NAME fork
					sb.append("\t\t//echo \"<br>Finished ");
					sb.append(weaponName);
					sb.append(" fork.\";\n");
					//closing brackets
					sb.append("\t}\n\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
		output.setText(sb.toString());
	}

	private void generateVariables() {
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
					String weaponName = getWeaponName(tableName);
					NodeList propertyList = sectionElement.getChildNodes();
					for (int k = 0; k < propertyList.getLength(); k++) {
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String data = prop.getTextContent();
							String name = prop.getAttribute("name");

							if (isIgnoredProperty(name)) {
								continue;
							}

							try {
								new Range(data);
								sb.append("\t\tpublic $mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_min = null;\n");

								sb.append("\t\tpublic $mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append("_max = null;\n");
							} catch (StringIndexOutOfBoundsException strException) {
								//its not a range, just set the val to null
								sb.append("\t\tpublic $mod_weapon_");
								sb.append(weaponName);
								sb.append("_");
								sb.append(prop.getAttribute("name"));
								sb.append(" = null;\n");
							}
						} //end scanned element node
					} //end property loop

					//clear modified, genesis
					sb.append("\t\tpublic $mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified = null;\n");
					sb.append("\t\tpublic $mod_weapon_");
					sb.append(weaponName);
					sb.append("_modified_genesis = null;\n\n");

				} //end section element loop
			} //end section loop
			output.setText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			output.setText(e.getMessage());
		}
	}

	private boolean isIgnoredProperty(String property) {
		if (Arrays.asList(blacklistedProperties).contains(property)) {
			return true;
		}
		return false;
	}

	private String getInput() {
		String wrappedXML = input.getText();
		wrappedXML = "<bioweapon>" + wrappedXML + "</bioweapon>";
		return wrappedXML;
	}
}
