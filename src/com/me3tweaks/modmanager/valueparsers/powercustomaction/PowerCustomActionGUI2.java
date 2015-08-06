package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.cellrenderers.HintTextAreaUI;
import com.me3tweaks.modmanager.cellrenderers.HintTextFieldUI;
import com.me3tweaks.modmanager.valueparsers.powercustomaction.PowerVariable.DLCPackage;

@SuppressWarnings("serial")
public class PowerCustomActionGUI2 extends JFrame implements ActionListener {
	protected static final int MP_VAR_ONLY = 2;
	protected static final int SP_VAR_ONLY = 1;
	protected static final int BOTH_SPMP_VAR = 0;
	private static boolean isRunningAsMain = false;
	JTextArea input1, input2, input3, input4, inputBalance, inputDescription;
	JTextField inputHTAccessURL;
	JTextField inputHumanName;
	JComboBox<PowerVariable.DLCPackage> package1, package2, package3, package4;
	JCheckBox isMP2, isMP3, isMP4;
	JButton load, generate;
	DefaultComboBoxModel<DLCPackage> packageModel1 = new DefaultComboBoxModel<DLCPackage>(PowerVariable.DLCPackage.values());
	DefaultComboBoxModel<DLCPackage> packageModel2 = new DefaultComboBoxModel<DLCPackage>(PowerVariable.DLCPackage.values());
	DefaultComboBoxModel<DLCPackage> packageModel3 = new DefaultComboBoxModel<DLCPackage>(PowerVariable.DLCPackage.values());
	DefaultComboBoxModel<DLCPackage> packageModel4 = new DefaultComboBoxModel<DLCPackage>(PowerVariable.DLCPackage.values());

	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;
	protected static ArrayList<PowerVariable> loadedVariables;
	protected static ArrayList<PowerVariable> balancedPowers;
	protected static ArrayList<ContainerRow> loadedContainers;
	private JPanel rowsPanel;
	private JTabbedPane editor = new JTabbedPane();
	private JPanel containersPanel;
	private JButton addContainer, finalizeButton;
	protected static ArrayList<PowerVariable> loadedMPVariables;
	private String basePath, mpPath;

	public static void main(String[] args) throws IOException {
		isRunningAsMain = true;
		ToolTipManager.sharedInstance().setDismissDelay(20000);
		new PowerCustomActionGUI2();
	}

	public PowerCustomActionGUI2() {
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {

		this.setTitle("ME3CMM SFXCustomAction ModMaker Generation Tool");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		// this.setMinimumSize(new Dimension(1080, 800));
		this.setPreferredSize(new Dimension(1280, 720));

		JPanel powerGUI2 = new JPanel();
		powerGUI2.setMaximumSize(new Dimension(1980, 1200));
		powerGUI2.setLayout(new BoxLayout(powerGUI2, BoxLayout.PAGE_AXIS));

		JPanel sourceInputsPanel = new JPanel(new GridBagLayout());
		sourceInputsPanel.setMaximumSize(new Dimension(1980, 1200));
		sourceInputsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Section Inputs"));
		GridBagConstraints c = new GridBagConstraints();

		input1 = new JTextArea(4, 10);
		input1.setUI(new HintTextAreaUI("Lowest Priority Section", true));
		package1 = new JComboBox<DLCPackage>();
		package1.setModel(packageModel1);

		input2 = new JTextArea(4, 10);
		input2.setUI(new HintTextAreaUI("Mid Priority Section", true));
		input2.setMinimumSize(new Dimension(100, 100));
		package2 = new JComboBox<DLCPackage>();
		package2.setModel(packageModel2);
		isMP2 = new JCheckBox("BG MP Section");
		isMP2.setToolTipText("<html>Checking this box indicates this section applies only to the MP variant of the original basegame power.<br>Variables will have _mp appended to the SQL names.</html>");

		input3 = new JTextArea(4, 10);
		input3.setUI(new HintTextAreaUI("Mid-High Priority Section", true));
		package3 = new JComboBox<DLCPackage>();
		package3.setModel(packageModel3);
		isMP3 = new JCheckBox("BG MP Section");
		isMP3.setToolTipText("<html>Checking this box indicates this section applies only to the MP variant of the original basegame power.<br>Variables will have _mp appended to the SQL names.</html>");

		input4 = new JTextArea(4, 10);
		input4.setUI(new HintTextAreaUI("High Priority Section", true));
		package4 = new JComboBox<DLCPackage>();
		package4.setModel(packageModel4);
		isMP4 = new JCheckBox("BG MP Section");
		isMP4.setToolTipText("<html>Checking this box indicates this section applies only to the MP variant of the original basegame power.<br>Variables will have _mp appended to the SQL names.</html>");

		inputBalance = new JTextArea(4, 10);
		inputBalance.setUI(new HintTextAreaUI("Balance Changes (Highest Priority) Section", true));

		input1.setLineWrap(true);
		input1.setColumns(10);
		input1.setWrapStyleWord(false); //default

		input2.setLineWrap(true);
		input3.setLineWrap(true);
		input4.setLineWrap(true);
		inputBalance.setLineWrap(true);
		int sourceRow = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		Insets topInsets = new Insets(5, 0, 0, 0);
		Insets noInsets = new Insets(0, 0, 0, 0);

		// input1
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		sourceInputsPanel.add(input1, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 1;
		sourceInputsPanel.add(package1, c);

		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(5, 1));
		c.weightx = 1;
		c.gridy = ++sourceRow;
		c.gridwidth = 2;
		sourceInputsPanel.add(sep, c);

		// input2
		c.gridy = ++sourceRow;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.insets = topInsets;
		c.fill = GridBagConstraints.BOTH;
		sourceInputsPanel.add(input2, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = noInsets;
		c.gridy = ++sourceRow;
		c.weighty = 0;
		c.gridwidth = 1;
		sourceInputsPanel.add(package2, c);
		c.gridx = 1;
		sourceInputsPanel.add(isMP2, c);

		sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(5, 1));
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 2;
		sourceInputsPanel.add(sep, c);

		// input3
		c.insets = topInsets;
		c.gridy = ++sourceRow;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		sourceInputsPanel.add(input3, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = noInsets;
		c.gridy = ++sourceRow;
		c.weighty = 0;
		c.gridwidth = 1;
		sourceInputsPanel.add(package3, c);
		c.gridx = 1;
		sourceInputsPanel.add(isMP3, c);

		sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(5, 1));
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 2;
		sourceInputsPanel.add(sep, c);

		// input4
		c.gridy = ++sourceRow;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		sourceInputsPanel.add(input4, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = ++sourceRow;
		c.weighty = 0;
		c.gridwidth = 1;
		sourceInputsPanel.add(package4, c);
		c.gridx = 1;
		sourceInputsPanel.add(isMP4, c);

		sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(5, 1));
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 2;
		sourceInputsPanel.add(sep, c);

		// inputbalance
		c.gridy = ++sourceRow;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		sourceInputsPanel.add(inputBalance, c);
		c.fill = GridBagConstraints.HORIZONTAL;

		/*
		 * c.gridx = 4; sourceInputsPanel.add(new JScrollPane(input3), c);
		 * c.gridx = 6; sourceInputsPanel.add(new JScrollPane(input4), c);
		 * c.gridx = 8; sourceInputsPanel.add(new JScrollPane(inputBalance), c);
		 * 
		 * sourceInputsPanel.add(package2, c); sourceInputsPanel.add(isMP2, c);
		 * 
		 * sourceInputsPanel.add(package3, c); sourceInputsPanel.add(isMP3, c);
		 * 
		 * sourceInputsPanel.add(package4, c); sourceInputsPanel.add(isMP4, c);
		 */

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = ++sourceRow;
		load = new JButton("Load sections into editor");
		load.addActionListener(this);
		sourceInputsPanel.add(load, c);

		rowsPanel = new JPanel();
		rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.PAGE_AXIS));
		rowsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Power Variables"));

		containersPanel = new JPanel();
		containersPanel.setLayout(new BoxLayout(containersPanel, BoxLayout.PAGE_AXIS));
		containersPanel.setBorder(new TitledBorder(new EtchedBorder(), "Variable Containers"));
		addContainer = new JButton("Add a new container");
		addContainer.addActionListener(this);
		containersPanel.add(addContainer);

		// MISC
		JPanel miscPanel = new JPanel();
		miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.PAGE_AXIS));
		miscPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		inputHumanName = new JTextField(20);
		inputHumanName.setMaximumSize(new Dimension(100000, 30));
		inputHumanName.setUI(new HintTextFieldUI("Display Name in ModMaker", true));
		inputHumanName.setBorder(new TitledBorder(new EtchedBorder(), "Display Name"));
		miscPanel.add(inputHumanName);
		miscPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		inputDescription = new JTextArea(5, 80);
		inputDescription.setMaximumSize(new Dimension(100000, 60));
		inputDescription.setUI(new HintTextFieldUI("Below-title description", true));
		inputDescription.setBorder(new TitledBorder(new EtchedBorder(), "Power Description"));
		miscPanel.add(inputDescription);
		miscPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		inputHTAccessURL = new JTextField(20);
		inputHTAccessURL.setMaximumSize(new Dimension(100000, 30));
		inputHTAccessURL.setUI(new HintTextFieldUI("Page URL (/modmaker/edit/x/powers/PAGEURL", true));
		inputHTAccessURL.setBorder(new TitledBorder(new EtchedBorder(), "Ending URL"));
		miscPanel.add(inputHTAccessURL);
		miscPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		miscPanel.add(Box.createVerticalGlue());
		finalizeButton = new JButton("Finalize ModMaker Attributes");
		finalizeButton.setToolTipText("Generates output for inputing into ModMaker based on the items you have input into the editor");
		finalizeButton.addActionListener(this);
		miscPanel.add(finalizeButton);
		JScrollPane inputPane = new JScrollPane(sourceInputsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane varPane = new JScrollPane(rowsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane containersPane = new JScrollPane(containersPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		inputPane.getVerticalScrollBar().setUnitIncrement(16);
		containersPane.getVerticalScrollBar().setUnitIncrement(16);
		varPane.getVerticalScrollBar().setUnitIncrement(16);

		editor.add("Input Data", inputPane);
		editor.addTab("Variables", varPane);
		editor.addTab("Containers", containersPane);
		editor.addTab("Misc", miscPanel);

		editor.setEnabledAt(1, false);
		editor.setEnabledAt(2, false);
		editor.setEnabledAt(3, false);
		add(editor);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (isRunningAsMain) {
					System.exit(0);
				}
			}
		});
		String in1, in2, in3, inBal;
		try {
			in1 = FileUtils.readFileToString(new File("b.xml"));
			in2 = FileUtils.readFileToString(new File("mp.xml"));
			in3 = FileUtils.readFileToString(new File("p2.xml"));
			inBal = FileUtils.readFileToString(new File("bal.xml"));
			input1.setText(in1);
			input2.setText(in2);
			input3.setText(in3);
			isMP2.setSelected(true);
			isMP3.setSelected(true);
			package3.setSelectedItem(PowerVariable.DLCPackage.PATCH2);
			//inputBalance.setText(inBal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pack();
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
		if (e.getSource() == load) {
			loadData();
		} else if (e.getSource() == addContainer) {
			addNewContainer();
		} else if (e.getSource() == finalizeButton) {
			finalizeChanges();
		}
	}

	private void addNewContainer() {
		editor.setEnabledAt(1, true);
		ContainerRow cr = new ContainerRow();
		cr.configure();
		cr.getDeleteContainerButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				removeContainer(cr);
			}
		});
		loadedContainers.add(cr);
		containersPanel.add(cr);

		for (PowerVariable pv : loadedVariables) {
			pv.addContainerOption(cr);
		}
		for (PowerVariable pv : loadedMPVariables) {
			pv.addContainerOption(cr);
		}
		pack();
	}

	private void removeContainer(ContainerRow cr) {
		for (PowerVariable pv : loadedVariables) {
			pv.removeContainerOption(cr);
		}
		containersPanel.remove(cr);
		loadedContainers.remove(cr);
		if (loadedContainers.size() <= 0) {
			editor.setEnabledAt(1, false);
		}
		pack();
	}

	private void loadData() {
		loadedVariables = new ArrayList<PowerVariable>();
		loadedMPVariables = new ArrayList<PowerVariable>();
		loadedContainers = new ArrayList<ContainerRow>();
		balancedPowers = new ArrayList<PowerVariable>();

		rowsPanel.removeAll();
		ArrayList<JTextArea> inputs = new ArrayList<JTextArea>();
		if (input1.getText() != null && !input1.getText().equals("")) {
			inputs.add(input1);
		}
		if (input2.getText() != null && !input2.getText().equals("")) {
			inputs.add(input2);
		}
		if (input3.getText() != null && !input3.getText().equals("")) {
			inputs.add(input3);
		}
		if (input4.getText() != null && !input4.getText().equals("")) {
			inputs.add(input4);
		}
		if (inputBalance.getText() != null && !inputBalance.getText().equals("")) {
			inputs.add(inputBalance);
		}
		// input 1->2->3->4->balance.
		try {
			for (JTextArea input : inputs) {
				// get DLC Package this input belongs to
				DLCPackage dlcPackage;
				boolean isMPOnly = false;
				if (input == input1) {
					dlcPackage = (DLCPackage) package1.getSelectedItem();
				} else if (input == input2) {
					dlcPackage = (DLCPackage) package2.getSelectedItem();
					isMPOnly = isMP2.isSelected();
				} else if (input == input3) {
					dlcPackage = (DLCPackage) package3.getSelectedItem();
					isMPOnly = isMP3.isSelected();
				} else if (input == input4) {
					dlcPackage = (DLCPackage) package4.getSelectedItem();
					isMPOnly = isMP3.isSelected();
				} else {
					// balance
					dlcPackage = null;
				}

				String input_text = getInput(input);
				DocumentBuilder dBuilder;

				dBuilder = dbFactory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(input_text));
				doc = dBuilder.parse(is);
				doc.getDocumentElement().normalize();
				NodeList sections = doc.getElementsByTagName("Section");
				Element sectionElement = (Element) sections.item(0);
				String sectionName = sectionElement.getAttribute("name");
				String tableName = getTableName(sectionName);
				if (inputHTAccessURL.getText() != null && inputHTAccessURL.getText().equals("")) {
					inputHTAccessURL.setText(tableName);
				}
				if (dlcPackage != null) {
					if (isMPOnly) {
						if (mpPath == null) {
							mpPath = sectionName;
						} else {
							if (!mpPath.equals(sectionName)) {
								JOptionPane.showMessageDialog(null,
										"Two or more sections are marked as MP only, but their section names don't match.", "Section name mismatch",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					} else {
						if (basePath == null) {
							basePath = sectionName;
						} else {
							if (!basePath.equals(sectionName)) {
								JOptionPane.showMessageDialog(null,
										"Two or more sections are not marked as MP only, but their section names don't match.",
										"Section name mismatch", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
				}

				if (dlcPackage == null && !sectionName.equals(basePath) && !sectionName.equals(mpPath)) {
					JOptionPane.showMessageDialog(null,
							"The section in the balance changes does not match either the basegame section or the MP variant sections.",
							"Section name mismatch", JOptionPane.ERROR_MESSAGE);
					return;
				}

				NodeList propertyList = sectionElement.getChildNodes();
				// We are now at at the "sections" array.
				// We now need to iterate over the dataElement list of
				// properties's path attribute, and drill into this one so we
				// know where to replace.
				for (int k = 0; k < propertyList.getLength(); k++) {
					// for every property in this filenode (of the data to
					// merge)...
					Node scannednode = propertyList.item(k);
					if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
						Element prop = (Element) scannednode;
						if (prop.getAttribute("type") == null || prop.getAttribute("type").equals("")) {
							continue; // skip items without types
						}
						PowerVariable var = new PowerVariable(dlcPackage, isMPOnly, basePath, sectionElement.getAttribute("name"), prop);
						if (isMPOnly) {
							loadedMPVariables.remove(var);
						} else {
							loadedVariables.remove(var); // remove updated or balanced
						}
						if (dlcPackage != null) {
							if (isMPOnly) {
								loadedMPVariables.add(var);
							} else {
								loadedVariables.add(var);
							}
						} else {
							balancedPowers.add(var);
						}
					}
				}
			} // end xml loading

			for (PowerVariable var : loadedVariables) {
				var.configureRows();
				for (VariableRow row : var.getVariableRows()) {
					rowsPanel.add(row);
				}
			}
			for (PowerVariable var : loadedMPVariables) {
				var.configureRows();
				for (VariableRow row : var.getVariableRows()) {
					rowsPanel.add(row);
				}
			}

			// editor.setEnabledAt(1, true); //vars
			editor.setEnabledAt(2, true); // containers
			editor.setEnabledAt(3, true); // misc
			pack();
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private String getInput(JTextArea input) {
		String wrappedXML = input.getText();
		wrappedXML = "<custompowers>" + wrappedXML + "</custompowers>";
		return wrappedXML;
	}

	private String getTableName(String sectionName) {
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

	private String getLoadName(String tableName) {
		return tableName.replaceAll("_", "");
	}

	private JPanel createRow(PowerVariable var) {
		JPanel panel = new JPanel();
		// JLabel varLabel = new JLabel(power.)
		return panel;
	}

	/**
	 * ITS A BIG ONE
	 */
	private void finalizeChanges() {
		generateHTML();

		generateSQLTable();
		generateSQLInsert();
		generateFork();
		generateVars();
		generateLoad();
		generatePHPValidation();
		generateJSValidation();
		generatePublish();
	}

	private void generateHTML() {
		//get balance list
		StringBuilder balanceList = new StringBuilder();
		for (PowerVariable var : balancedPowers) {
			balanceList.append(var.convertToBalance());
		}

		//Build hashmap of contatiner -> list<powers>
		HashMap<ContainerRow, ArrayList<VariableRow>> containerList = new HashMap<ContainerRow, ArrayList<VariableRow>>();
		for (ContainerRow cr : loadedContainers) {
			containerList.put(cr, new ArrayList<VariableRow>());
		}

		for (Map.Entry<ContainerRow, ArrayList<VariableRow>> container : containerList.entrySet()) {
			ContainerRow key = container.getKey();
			ArrayList<VariableRow> items = container.getValue();
			for (PowerVariable pv : loadedVariables) {
				for (VariableRow vr : pv.getVariableRows()) {
					if (vr.getContainerComboBox().getSelectedItem() == key) {
						items.add(vr);
					}
				}
			}
		}

		ArrayList<String> containerHTMLs = new ArrayList<String>();
		//generate detparams first
		for (PowerVariable var : loadedVariables) {
			if (var.getDataType() == PowerVariable.DataType.DETONATIONPARAMETERS) {
				containerHTMLs.add(var.convertToPHPEntryBox());
			}
		}
		for (PowerVariable var : loadedMPVariables) {
			if (var.getDataType() == PowerVariable.DataType.DETONATIONPARAMETERS) {
				containerHTMLs.add(var.convertToPHPEntryBox());
			}
		}

		//generate standard containers
		for (Map.Entry<ContainerRow, ArrayList<VariableRow>> container : containerList.entrySet()) {
			ContainerRow cr = container.getKey();
			ArrayList<VariableRow> children = container.getValue();

			StringBuilder innerText = new StringBuilder();
			for (VariableRow vr : children) {
				String entry = VariableRow.ENTRY_TEMPLATE;
				entry = entry.replaceAll("HINTTEXT", vr.getHint().getText());
				entry = entry.replaceAll("PREFIX", vr.getPrefix().getText());
				entry = entry.replaceAll("POSTFIX", vr.getPostfix().getText());
				entry = entry.replaceAll("VARNAME", vr.getSqlVarName());
				entry = entry.replaceAll("HUMANNAME", vr.getHumanName());
				entry = entry.replaceAll("TABLENAME", vr.getOwningVar().get().getBaseTableName());
				entry = entry.replaceAll("HINTTEXT", vr.getHint().getText());
				innerText.append(entry);
			}
			String containerBlock = ContainerRow.CONTAINER_TEMPLATE;
			containerBlock = containerBlock.replaceAll("CONTAINERNAME", cr.getContainerTitle().getText());
			containerBlock = containerBlock.replaceAll("CONTAINERDESCRIPTION", cr.getContainerText().getText());
			containerBlock = containerBlock.replaceAll("BALANCECHANGES_PLACEHOLDER", balanceList.toString());
			containerBlock = containerBlock.replaceAll("INPUTS_PLACEHOLDER", innerText.toString());
			containerHTMLs.add(containerBlock);
		}

		//Build HTML
		StringBuilder sb = new StringBuilder();
		for (String block : containerHTMLs) {
			sb.append(block);
		}
		try {
			String pageTemplate = FileUtils.readFileToString(new File("PCA-Generator/template.php"));
			pageTemplate = pageTemplate.replaceAll("LOADNAME", getLoadName(getTableName(basePath)));
			pageTemplate = pageTemplate.replaceAll("VARNAME", getTableName(basePath));
			pageTemplate = pageTemplate.replaceAll("HUMANNAME", inputHumanName.getText());
			pageTemplate = pageTemplate.replaceAll("PAGEDESCRIPTION", inputDescription.getText());
			pageTemplate = pageTemplate.replaceAll("CONTAINERS_PLACEHOLDER", Matcher.quoteReplacement(sb.toString()));
			//page built
			FileUtils.writeStringToFile(new File("PCA-Generator/" + getTableName(basePath) + ".php"), pageTemplate);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateSQLTable() {
		String tableName = getTableName(basePath); // base is always used in SQL
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(tableName);
		sb.append("*/\n");
		sb.append("CREATE TABLE modmaker_powers_");
		sb.append(tableName);
		sb.append(" (\n");
		sb.append("\tmod_id INT NOT NULL,\n");

		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToSQLTable());
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToSQLTable());
		}

		sb.append("\tmodified boolean NOT NULL,\n");
		sb.append("\tmodified_genesis boolean NOT NULL,\n");
		sb.append("\tFOREIGN KEY (mod_id) REFERENCES modmaker_mods(mod_id) ON DELETE CASCADE,\n"); // end
																									// of
																									// SQL
																									// statement
		sb.append("\tPRIMARY KEY(mod_id)\n");
		sb.append(") ENGINE=INNODB;\n");
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/SQL_CREATE_TABLE.sql"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateSQLInsert() {
		String tableName = getTableName(basePath); // base is always used in SQL
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(tableName);
		sb.append(" data*/\n");
		sb.append("INSERT INTO modmaker_powers_");
		sb.append(tableName);
		sb.append(" VALUES(\n");
		sb.append("\t1, /*GENESIS MOD ID*/\n");
		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToSQLInsert());
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToSQLInsert());
		}

		sb.append("\tfalse, /*modified*/\n");
		sb.append("\tfalse /*genesis modified*/\n");
		sb.append(");"); // end of SQL statement
		sb.append("\n");
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/SQL_INSERT.sql"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateFork() {
		String tableName = getTableName(basePath); // base is always used in SQL
		StringBuilder sb = new StringBuilder();
		sb.append("\t//");
		sb.append(tableName.toUpperCase());
		sb.append("\n");
		// echo "<br>Beginning TABLENAME fork.";
		sb.append("\t//echo \"<br>Beginning ");
		sb.append(tableName);
		sb.append(" fork.\";\n");

		sb.append("\t$stmt = $dbh->prepare(\"SELECT * FROM modmaker_powers_");
		sb.append(tableName);
		sb.append(" WHERE mod_id=:fork_parent\");\n");
		sb.append("\t$stmt->bindValue(\":fork_parent\", $original_id);\n");
		sb.append("\t$stmt->execute();\n");
		sb.append("\t$");
		sb.append(tableName);
		sb.append("row = $stmt->fetch();\n");
		sb.append("\t$stmt = $dbh->prepare(\"INSERT INTO modmaker_powers_");
		sb.append(tableName);
		sb.append(" VALUES(:mod_id, ");

		// build values list
		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToForkStage1());
			sb.append(",");
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToForkStage1());
			sb.append(",");
		}

		sb.append("false, :modified_genesis)\");\n");

		// bind values
		sb.append("\t$stmt->bindValue(\":mod_id\", $mod_id);\n");
		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToForkStage2());
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToForkStage2());
		}

		// bind modified_genesis
		sb.append("\t$stmt->bindValue(\":modified_genesis\", $");
		sb.append(tableName);
		sb.append("row['");
		sb.append("modified_genesis");
		sb.append("']);\n");

		// if (!$stmt->execute()) {
		sb.append("\tif (!$stmt->execute()) {\n");
		// echo "NAME FORK FAIL."
		sb.append("\t\techo \"");
		sb.append(tableName);
		sb.append(" fork failed:<br>\";\n");
		sb.append("\t\tprint_r($stmt->errorInfo());\n");
		sb.append("\t\treturn ob_get_clean();\n");
		// } else {
		sb.append("\t} else {\n");
		// echo "<br>Finished NAME fork
		sb.append("\t\t//echo \"<br>Finished ");
		sb.append(tableName);
		sb.append(" fork.\";\n");
		// closing brackets
		sb.append("\t}\n\n");
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/fork.php"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateVars() {
		String tableName = getTableName(basePath); // base is always used in SQL
		StringBuilder sb = new StringBuilder();
		sb.append("\t/*");
		sb.append(tableName);
		sb.append("*/\n");
		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToModVar());
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToModVar());
		}

		sb.append("\tpublic $mod_powers_");
		sb.append(tableName);
		sb.append("_modified = null;\n");

		sb.append("\tpublic $mod_powers_");
		sb.append(tableName);
		sb.append("_modified_genesis = null;\n");

		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/mod-vars.php"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateLoad() {
		String tableName = getTableName(basePath); // base is always used in SQL
		String loadName = getLoadName(tableName);
		StringBuilder sb = new StringBuilder();

		sb.append("\tpublic function loadPower");
		sb.append(Character.toUpperCase(loadName.charAt(0)) + loadName.toLowerCase().substring(1)); // have
																									// only
																									// first
																									// letter
																									// capitalized.
		sb.append("(){\n");
		sb.append("\t\trequire($_SERVER['DOCUMENT_ROOT'].\"/db-middleman.php\");\n");
		sb.append("\t\t//Load values from DB\n");
		sb.append("\t\t$sql = \"SELECT * FROM modmaker_powers_");
		sb.append(tableName);
		sb.append(" WHERE mod_id=:mod_id\";\n");

		sb.append("\t\t$stmt = $dbh->prepare($sql);\n");
		sb.append("\t\t$stmt->bindValue(\":mod_id\", $this->mod_id);\n");
		sb.append("\t\t$stmt->execute();\n");
		sb.append("\t\t$row = $stmt->fetch();\n");

		// build values list
		for (PowerVariable pv : loadedVariables) {
			sb.append(pv.convertToModLoad());
		}
		for (PowerVariable pv : loadedMPVariables) {
			sb.append(pv.convertToModLoad());
		}
		// load modified, genesis
		// modified, genesis
		sb.append("\t\t$this->mod_powers_");
		sb.append(tableName);
		sb.append("_modified = $row['modified'];\n");
		sb.append("\t\t$this->mod_powers_");
		sb.append(tableName);
		sb.append("_modified_genesis = $row['modified_genesis'];\n");
		sb.append("\t}\n");
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/mod-load.php"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generatePHPValidation() {
		String tableName = getTableName(basePath);
		StringBuilder sb = new StringBuilder();
		sb.append("\tcase \"");
		sb.append(tableName);
		sb.append("\":\n");
		sb.append("\t\t$status = array();\n");
		sb.append("\t\t$updateinfo = array();\n");

		ArrayList<PowerVariable> varList = new ArrayList<PowerVariable>();
		for (PowerVariable pv : loadedVariables) {
			varList.add(pv);
		}
		for (PowerVariable pv : loadedMPVariables) {
			varList.add(pv);
		}
		
		for (PowerVariable pv : varList) {
			sb.append(pv.convertToPHPValidation());
		}
		
		//append final
		sb.append("\t\t$mod->loadPowerFunctions();\n");
		sb.append("\t\t$result = $mod->powers->updatePower('");
		sb.append(tableName);
		sb.append("', $updateinfo);\n");

		sb.append("\t\tif (is_null($result) and count($status)<=0) {\n");
		sb.append("\t\t\t$_SESSION['powers_update'] = \"");
		sb.append(tableName);
		sb.append(" updated.\";\n");

		sb.append("\t\t\theader('Location: /modmaker/edit/'.$id.'/powers');\n");
		;
		sb.append("\t\t\tdie();\n");
		sb.append("\t\t} else {\n");
		sb.append("\t\t\tarray_push($status, $result);\n");
		sb.append("\t\t\t$_SESSION['");
		sb.append(tableName);
		sb.append("_status'] = $status;\n");
		sb.append("\t\t\theader('Location: /modmaker/edit/'.$id.'/powers/");
		sb.append(inputHTAccessURL.getText());
		sb.append("');\n");
		sb.append("\t\t\tdie();\n");
		sb.append("\t\t}\n");
		sb.append("\t\tbreak;\n\n");
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/powers-validation.php"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateJSValidation() {
		StringBuilder sb = new StringBuilder();
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
		
		//build rules
		ArrayList<PowerVariable> varList = new ArrayList<PowerVariable>();
		for (PowerVariable pv : loadedVariables) {
			varList.add(pv);
		}
		for (PowerVariable pv : loadedMPVariables) {
			varList.add(pv);
		}
		
		for (PowerVariable pv : varList) {
			sb.append(pv.convertToJSValidation());
		}
		
		sb.append("\t\t}\n");
		sb.append("\t});\n");
		sb.append("});\n");
		
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/"+getTableName(basePath)+".js"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generatePublish() {
		StringBuilder sb = new StringBuilder();
		String tableName = getTableName(basePath);

		// get list of where all defs go
		HashSet<DLCPackage> allPackages = new HashSet<DLCPackage>();
		for (PowerVariable pv : loadedVariables) {
			allPackages.add(pv.getDlcPackage());
		}
		for (PowerVariable pv : loadedMPVariables) {
			allPackages.add(pv.getDlcPackage());
		}

		// generate publish for things in each package
		for (DLCPackage d : allPackages) {
			sb.append("//");
			sb.append(tableName);
			sb.append("\n");
			sb.append("if ($this->mod->powers->mod_powers_");
			sb.append(tableName);
			sb.append("_modified_genesis) {");
			sb.append("\n");

			ArrayList<PowerVariable> varsToPublish = new ArrayList<PowerVariable>();
			for (PowerVariable pv : loadedVariables) {
				if (pv.getDlcPackage() == d) {
					varsToPublish.add(pv);
				}
			}
			for (PowerVariable pv : loadedMPVariables) {
				if (pv.getDlcPackage() == d) {
					varsToPublish.add(pv);
				}
			}

			for (PowerVariable pv : varsToPublish) {
				//publishing code
				sb.append(pv.convertToPublisherLine());
			}
			sb.append("}\n\n");
		}
		try {
			FileUtils.writeStringToFile(new File("PCA-Generator/publisher.php"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static int getVarApplicationAreas(PowerVariable var) {
		//Check for var in MP
		if (loadedMPVariables.contains(var)) {
			return MP_VAR_ONLY;
		}
		//not in MP
		if (loadedVariables.contains(var)) {
			for (PowerVariable pv : loadedMPVariables) {
				//check MP names for a match on varname
				if (pv.getVarName().equals(var.getVarName())) {
					return SP_VAR_ONLY;
				}
			}
			for (PowerVariable pv : balancedPowers) {
				//check MP names for a match on varname
				if (pv.getVarName().equals(var.getVarName())) {
					return SP_VAR_ONLY;
				}
			}
		}
		return BOTH_SPMP_VAR;
	}
}
