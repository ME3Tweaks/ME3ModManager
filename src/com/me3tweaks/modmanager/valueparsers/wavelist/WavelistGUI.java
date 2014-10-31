package com.me3tweaks.modmanager.valueparsers.wavelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class WavelistGUI extends JFrame implements ActionListener {
	JTextArea input, output;
	JButton submit;

	public static void main(String[] args) throws IOException {
		new WavelistGUI();
	}

	public WavelistGUI() {
		this.setTitle("ME3CMM Wavelist Parser Tool");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/icon32.png")));
		this.setPreferredSize(new Dimension(400, 500));
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		JPanel wavelistGUI = new JPanel(new BorderLayout());
		JLabel instructionsLabel = new JLabel(
				"<html>ME3CMM Wavelist Parser<br>Enter the wavelist text below and press parse to view easily readable information.</html>");
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

		inputPanel.add(input, BorderLayout.CENTER);
		inputPanel.add(submit, BorderLayout.SOUTH);
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.PAGE_AXIS));
		JLabel outputLabel = new JLabel("Output");
		output = new JTextArea(20, 30);
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(false);
		outputPanel.add(outputLabel);
		outputPanel.add(output);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				inputPanel, outputPanel);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == submit) {
			// parse it.
			try {
				String input_text = input.getText();
				Wave wave = new Wave(input_text);
				output.setText(wave.toString());
			} catch (Exception ex) {
				output.setText(ex.toString());
			}
		}
	}
}
