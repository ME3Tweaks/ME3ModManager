package com.me3tweaks.modmanager.valueparsers.wavelist;

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

import com.me3tweaks.modmanager.ModManagerWindow;

public class WavelistGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JButton submit, generateInsert, copy;
	JComboBox<String> factionlist;
	JComboBox<Integer> wavenumlist;
	String[] difficulties = { "DO_Level1", "DO_Level2", "DO_Level3", "DO_Level4" };
	String[] factions = { "Cerberus", "Cerberus2", "Geth", "Geth2", "Reaper", "Reaper2", "Collector" };
	Integer[] waves = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

	public static void main(String[] args) throws IOException {
		new WavelistGUI();
	}

	public WavelistGUI() {
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("ME3CMM Wavelist Parser Tool");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		setMinimumSize(new Dimension(490, 500));
		setPreferredSize(new Dimension(490, 500));
		JPanel wavelistGUI = new JPanel(new BorderLayout());
		JLabel instructionsLabel = new JLabel("<html>ME3CMM Wavelist Parser<br>Enter the Wavelist XML below and then select an operation.</html>");
		wavelistGUI.add(instructionsLabel, BorderLayout.NORTH);
		instructionsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel inputPanel = new JPanel(new BorderLayout());
		input = new JTextArea(6, 45);
		input.setMinimumSize(new Dimension(50, 120));
		input.setLineWrap(true);
		input.setWrapStyleWord(false);

		submit = new JButton("Parse");
		submit.addActionListener(this);

		// sql stuff
		JPanel SQLPanel = new JPanel(new BorderLayout());
		factionlist = new JComboBox<String>(factions);
		wavenumlist = new JComboBox<Integer>(waves);
		generateInsert = new JButton("Generate SQL");
		generateInsert.addActionListener(this);
		generateInsert.setEnabled(false);
		generateInsert.setToolTipText("SQL output is disabled");
		SQLPanel.add(factionlist, BorderLayout.NORTH);
		SQLPanel.add(wavenumlist, BorderLayout.CENTER);
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

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
		wavelistGUI.add(splitPane, BorderLayout.CENTER);
		getContentPane().add(wavelistGUI);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
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
		if (e.getSource() == submit) {
			// parse it.
			String input_text = input.getText();
			try {
				Wave wave = new Wave(input_text);
				output.setText(wave.toString());
			} catch (Exception ex) {
				output.setText(ex.toString());
			}
		} else if (e.getSource() == generateInsert) {
			generateSQL();
		} else if (e.getSource() == copy) {
			String myString = output.getText();
			StringSelection stringSelection = new StringSelection(myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	private void generateSQL() {
		// g
		String input_text = input.getText();
		Wave wave;
		try {
			wave = new Wave(input_text);
		} catch (Exception ex) {
			output.setText(ex.toString());
			return;
		}
		//get enemy
		String faction = ((String) factionlist.getSelectedItem()).toLowerCase();

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO modmaker_wavelists VALUES (\n");
		sb.append("\t1, /*GENESIS MOD ID*/\n");
		sb.append("\t\"");
		sb.append(diffToSQLDiff(wave.difficulty));
		sb.append("\", /*difficulty*/\n");
		sb.append("\t\"");
		sb.append(faction);
		sb.append("\", /*faction*/\n");
		sb.append("\t");
		sb.append(wavenumlist.getSelectedItem());
		sb.append(", /*wave_num*/\n");

		//baseenemy
		sb.append("\t\"");
		sb.append(biogameNameToSQLName(wave.getBaseEnemy()));
		sb.append("\", /*baseenemy*/\n");

		int enemyindex = 2;
		while (enemyindex <= 5) {
			if (wave.enemies.size() < enemyindex) {
				sb.append("\tnull, /*enemy ");
				sb.append(enemyindex);
				sb.append("*/\n");

				sb.append("\tnull, /*enemy ");
				sb.append(enemyindex);
				sb.append(" min */\n");

				sb.append("\tnull, /*enemy ");
				sb.append(enemyindex);
				sb.append(" max */\n");

				sb.append("\tnull, /*enemy ");
				sb.append(enemyindex);
				sb.append(" maxperwave */\n");
			} else {
				sb.append("\t\"");
				sb.append(biogameNameToSQLName(wave.enemies.get(enemyindex - 1).enemyname));
				sb.append("\", /*enemy ");
				sb.append(enemyindex);
				sb.append("*/\n");

				sb.append("\t");
				sb.append(wave.enemies.get(enemyindex - 1).min);
				sb.append(", /*enemy ");
				sb.append(enemyindex);
				sb.append(" min */\n");

				sb.append("\t");
				sb.append(wave.enemies.get(enemyindex - 1).max);
				sb.append(", /*enemy ");
				sb.append(enemyindex);
				sb.append(" max */\n");

				sb.append("\t");
				sb.append(wave.enemies.get(enemyindex - 1).maxperwave);
				sb.append(", /*enemy ");
				sb.append(enemyindex);
				sb.append(" maxperwave */\n");
			}

			enemyindex++;
		}

		sb.append("\tfalse, /*modified*/\n");
		sb.append("\tfalse /*genesis modified*/\n");
		sb.append(");"); //end of SQL statement

		output.setText(sb.toString());
	}

	private String diffToSQLDiff(String difficulty) {
		switch (difficulty) {
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

	private String biogameNameToSQLName(String biogamename) {
		switch (biogamename) {
		case "WAVE_CER_AssaultTrooper":
			return "assaulttrooper";
		case "WAVE_CER_Centurion":
			return "centurion";
		case "WAVE_CER_Engineer":
			return "engineer";
		case "WAVE_CER_Guardian":
			return "guardian";
		case "WAVE_CER_Nemesis":
			return "nemesis";
		case "WAVE_CER_Phoenix":
			return "dragoon";
		case "WAVE_CER_Atlas":
			return "atlas2";
		case "WAVE_CER_Phantom":
			return "phantom";
		case "WAVE_GTH_GethTrooper":
			return "gethtrooper";
		case "WAVE_GTH_GethRocketTrooper":
			return "gethrockettrooper";
		case "WAVE_GTH_GethHunter":
			return "gethhunter";
		case "WAVE_GTH_GethPyro":
			return "gethpyro";
		case "WAVE_GTH_GethBomber":
			return "gethbomber";
		case "WAVE_GTH_GethPrime":
			return "gethprime";
		case "WAVE_RPR_Cannibal":
			return "cannibal";
		case "WAVE_RPR_Marauder":
			return "marauder";
		case "WAVE_RPR_Brute":
			return "brute";
		case "WAVE_RPR_Husk":
			return "husk";
		case "WAVE_RPR_Ravager":
			return "ravager";
		case "WAVE_RPR_Banshee":
			return "banshee";
		case "WAVE_COL_CollectorTrooper":
			return "collectortrooper";
		case "WAVE_COL_CollectorCaptain":
			return "collectorcaptain";
		case "WAVE_COL_Abomination":
			return "abomination";
		case "WAVE_COL_Scion":
			return "scion";
		case "WAVE_COL_Praetorian":
			return " praetorian";
		default:
			return "UNDEFINED ENEMY";
		}
	}
}
