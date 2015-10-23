package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.me3tweaks.modmanager.cellrenderers.HintTextFieldUI;
import com.me3tweaks.modmanager.objects.ProcessResult;

public class CoalescedWindow extends JFrame {

	public CoalescedWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Coalesced Compiler/Decompiler");
		setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		GridBagConstraints c = new GridBagConstraints();

		//Decompile Panel
		JPanel decompilePanel = new JPanel(new GridBagLayout());
		decompilePanel.setBorder(new TitledBorder(new EtchedBorder(), "Decompile a .bin file to .xml files"));
		JTextField dInputField = new JTextField(55);
		dInputField.setUI(new HintTextFieldUI("Select a Coalesced .bin file"));
		JButton dBrowse = new JButton("Browse...");
		dBrowse.setPreferredSize(new Dimension(100, 19));
		JButton dCompile = new JButton("Decompile");
		dCompile.setEnabled(false);
		dCompile.setPreferredSize(new Dimension(100, 23));
		JLabel dStatus = new JLabel(" ");

		c = new GridBagConstraints();
		c.gridx = 0;
		decompilePanel.add(dInputField, c);

		c.gridx = 1;
		decompilePanel.add(dBrowse, c);

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		decompilePanel.add(dStatus, c);

		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		decompilePanel.add(dCompile, c);

		//Compile Panel
		JPanel compilePanel = new JPanel(new GridBagLayout());
		compilePanel.setBorder(new TitledBorder(new EtchedBorder(), "Compile a .bin file from .xml files"));
		JTextField cInputField = new JTextField(55);
		cInputField.setUI(new HintTextFieldUI("Select a .xml manifest file"));
		JButton cBrowse = new JButton("Browse...");
		cBrowse.setPreferredSize(new Dimension(100, 19));
		JButton cCompile = new JButton("Compile");
		cCompile.setPreferredSize(new Dimension(100, 23));
		cCompile.setEnabled(false);
		JLabel cStatus = new JLabel("Manifest file is the .xml file with the same name as the file that was originally decompiled");

		c = new GridBagConstraints();
		c.gridx = 0;
		compilePanel.add(cInputField, c);

		c.gridx = 1;
		compilePanel.add(cBrowse, c);

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		compilePanel.add(cStatus, c);

		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		compilePanel.add(cCompile, c);

		JLabel label = new JLabel("<html>You can compile or decompile Coalesced files using this tool, for both basegame and DLC.</html>");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		dStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		cStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		decompilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		compilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(label);
		panel.add(decompilePanel);
		panel.add(compilePanel);

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();

		dBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser binChooser = new JFileChooser();
				File tryDir = new File(dInputField.getText());
				if (tryDir.exists() && tryDir.isFile()) {
					binChooser.setCurrentDirectory(tryDir.getParentFile());
				} else {
					binChooser.setCurrentDirectory(new File("."));
				}
				binChooser.setDialogTitle("Select .bin to decompile");
				//binChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				//
				// disable the "All files" option.
				//
				binChooser.setAcceptAllFileFilterUsed(false);
				binChooser.setFileFilter(new FileNameExtensionFilter("Coalesced files (.bin)", "bin"));

				if (binChooser.showOpenDialog(CoalescedWindow.this) == JFileChooser.APPROVE_OPTION) {
					String chosenFile = binChooser.getSelectedFile().getAbsolutePath();
					if (chosenFile.toLowerCase().endsWith("PCConsoleTOC.bin".toLowerCase())) {
						dStatus.setText("Selected PCConsoleTOC file, not a Coalesced file.");
					} else {
						dInputField.setText(chosenFile);
						dStatus.setText(" ");
						dCompile.setEnabled(true);
					}
				}
			}
		});

		cBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser xmlChooser = new JFileChooser();
				File tryFile = new File(cInputField.getText());
				if (tryFile.exists() && tryFile.isFile()) {
					xmlChooser.setCurrentDirectory(tryFile.getParentFile());
				} else {
					xmlChooser.setCurrentDirectory(new File("."));
				}
				xmlChooser.setDialogTitle("Select .xml manifest to compile");
				//binChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				//
				// disable the "All files" option.
				//
				xmlChooser.setAcceptAllFileFilterUsed(false);
				xmlChooser.setFileFilter(new FileNameExtensionFilter("Coalesced Manifest File (.xml)", "xml"));

				if (xmlChooser.showOpenDialog(CoalescedWindow.this) == JFileChooser.APPROVE_OPTION) {
					String chosenFile = xmlChooser.getSelectedFile().getAbsolutePath();
					cInputField.setText(chosenFile);
					cCompile.setEnabled(true);
				} else {

				}
			}
		});

		dCompile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (new File(dInputField.getText()).exists()) {
					String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";

					ArrayList<String> commandBuilder = new ArrayList<String>();
					commandBuilder.add(compilerPath);
					commandBuilder.add(dInputField.getText());
					//System.out.println("Building command");
					String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
					//Debug stuff
					StringBuilder sb = new StringBuilder();
					for (String arg : command) {
						sb.append(arg + " ");
					}
					ProcessBuilder decompileProcessBuilder = new ProcessBuilder(command);
					decompileProcessBuilder.redirectErrorStream(true);
					decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					ProcessResult res = ModManager.runProcess(decompileProcessBuilder);
					if (!res.hadError() && res.getReturnCode() == 0) {
						dStatus.setText("Decompiled .bin file. Files are located in a folder of the same name.");
					}
				}
			}
		});

		cCompile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (new File(cInputField.getText()).exists()) {
					String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";

					ArrayList<String> commandBuilder = new ArrayList<String>();
					commandBuilder.add(compilerPath);
					commandBuilder.add(cInputField.getText());
					//System.out.println("Building command");
					String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
					//Debug stuff
					StringBuilder sb = new StringBuilder();
					for (String arg : command) {
						sb.append(arg + " ");
					}
					ProcessBuilder compileProcessBuilder = new ProcessBuilder(command);
					compileProcessBuilder.redirectErrorStream(true);
					compileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					ProcessResult res = ModManager.runProcess(compileProcessBuilder);
					if (!res.hadError() && res.getReturnCode() == 0) {
						cStatus.setText("Compiled .bin file. The new file is located in the same folder as the manifest.");
					}
				}
			}
		});
	}
}
