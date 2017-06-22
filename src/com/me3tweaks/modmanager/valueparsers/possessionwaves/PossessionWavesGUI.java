package com.me3tweaks.modmanager.valueparsers.possessionwaves;

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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PossessionWavesGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JTextField enemy;
	JButton submit, generateInsert, copy;
	JComboBox<String> enemylist;
	String[] difficulties = { "DO_Level1", "DO_Level2", "DO_Level3", "DO_Level4" };
	String[] enemies = { "COL Trooper", "COL Captain", "Abomination", "Scion", "Praetorian" };


	public static void main(String[] args) throws IOException {
		new PossessionWavesGUI();
	}

	public PossessionWavesGUI() {
		this.setTitle("ME3CMM MP4 PossessionWaves Parser Tool");
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
				"<html>ME3CMM MP4 PossessionWaves Parser<br>Enter the PossessionWaves text below and then select an operation.</html>");
		wavelistGUI.add(instructionsLabel, BorderLayout.NORTH);
		instructionsLabel
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel inputPanel = new JPanel(new BorderLayout());
		input = new JTextArea(6, 45);
		input.setMinimumSize(new Dimension(50, 120));
		input.setLineWrap(true);
		input.setWrapStyleWord(false);

		submit = new JButton("Parse");
		submit.addActionListener(this);
		
		// sql stuff
		JPanel SQLPanel = new JPanel(new BorderLayout());
		//difficultylist = new JComboBox<String>(difficulties);
		enemylist = new JComboBox<String>(enemies);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		SQLPanel.add(enemylist, BorderLayout.NORTH);
		//SQLPanel.add(difficultylist, BorderLayout.CENTER);
		SQLPanel.add(generateInsert, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(submit, BorderLayout.WEST);
		buttonPanel.add(SQLPanel, BorderLayout.EAST);

		inputPanel.add(input, BorderLayout.CENTER);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		JPanel outputPanel = new JPanel(new BorderLayout());
		JLabel outputLabel = new JLabel("Output");
		output = new JTextArea(20, 30);
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(false);
		copy = new JButton("Copy");
		copy.addActionListener(this);
		outputPanel.add(outputLabel, BorderLayout.NORTH);
		outputPanel.add(output, BorderLayout.CENTER);
		outputPanel.add(copy, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				inputPanel, outputPanel);
		wavelistGUI.add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(wavelistGUI);
		pack();
		generateInsert.setEnabled(false);
		generateInsert.setToolTipText("SQL output is disabled");

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
		if (e.getSource() == submit) {
			// parse it.
			String input_text = input.getText();
			try {
				Difficulty cat = new Difficulty(input_text);
				output.setText(cat.toString());
			} catch (Exception ex) {
				output.setText(ex.toString());
			}
		} else if (e.getSource() == generateInsert) {
			generateSQL();
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
		Difficulty diff;
		try {
			diff = new Difficulty(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			return;
		}
		//get enemy
		String enemy = listToSQLName((String) enemylist.getSelectedItem());
		
		StringBuilder sb = new StringBuilder();
		sb.append("/*");
		sb.append(diffToSQLDiff(diff.difficulty));
		sb.append(" ");
		sb.append(enemy);
		sb.append("*/\n");
	
		sb.append("INSERT INTO modmaker_possessionwaves VALUES (\n");
		sb.append("\t1, /*GENESIS MOD ID*/\n");
		sb.append("\t\"");
		sb.append(enemy);
		sb.append("\", /*enemy*/\n");
		sb.append("\t\"");
		sb.append(diffToSQLDiff(diff.difficulty));
		sb.append("\", /*difficulty*/\n");
		for (int i = 0; i < diff.waves.possessionwaves.length; i++){
			sb.append("\t");
			sb.append((diff.waves.possessionwaves[i]) ? "true" : "false");
			sb.append(", /*Wave ");
			sb.append(i+1);
			sb.append("*/\n");
		}
		sb.append("\tfalse, /*modified*/\n");
		sb.append("\tfalse /*genesis modified*/\n");
		sb.append(");"); //end of SQL statement
		
		output.setText(sb.toString());
	}
	
	private String diffToSQLDiff(String difficulty) {
		switch(difficulty){
		case "DO_Level1":
			return "bronze";
		case "DO_Level2":
			return "silver";
		case "DO_Level3":
			return "gold";
		case "DO_Level4":
			return "platinum";
		default:
			return "UNDEFINED_DIFFICULTY";
		}
	}

	private String listToSQLName(String selectedItem) {
		switch(selectedItem){
		case "COL Trooper":
			return "collector_trooper";
		case "COL Captain":
			return "collector_captain";
		case "Abomination":
			return "abomination";
		case "Scion":
			return "scion";
		case "Praetorian":
			return "praetorian";
		default:
			return "UNDEFINED ENEMY";
		}
	}
}
