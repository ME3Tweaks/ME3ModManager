package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class StarterKitWindow extends JDialog {
	JTextField modName, internalName, internalTLKId, mountPriority;
	private JTextField modDeveloper;
	private JTextField modSite;
	private JTextArea modDescription;

	public StarterKitWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow() {
		setTitle("Custom DLC Starter Kit Builder");
		setPreferredSize(new Dimension(500, 500));
		setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new EmptyBorder(5,5,5,5));
		GridBagConstraints c = new GridBagConstraints();

		int labelColumn = 0;
		int fieldColumn = 1;
		c.weightx =  1;
		c.gridx = labelColumn;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Mod Name"), c);
		c.gridy++;
		panel.add(new JLabel("Internal Name"), c);
		c.gridy++;
		panel.add(new JLabel("Internal Name TLK ID"), c);
		c.gridy++;
		panel.add(new JLabel("Mount Priority"), c);
		c.gridy++;
		panel.add(new JLabel("Mount Flag"), c);
		c.gridy++;
		panel.add(new JLabel("Mod Description"), c);

		modName = new JTextField();
		modDeveloper = new JTextField();
		modSite = new JTextField();
		internalName = new JTextField();
		internalTLKId = new JTextField();
		mountPriority = new JTextField();
		modDescription = new JTextArea();

		c.gridy = 0;
		c.gridx = fieldColumn;
		panel.add(modName, c);
		c.gridy++;
		panel.add(modDeveloper, c);
		c.gridy++;
		panel.add(modSite, c);
		c.gridy++;
		panel.add(internalName, c);
		c.gridy++;
		panel.add(internalTLKId, c);
		c.gridy++;
		panel.add(mountPriority, c);
		c.gridy++;
		//panel.add(modName, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		panel.add(modDescription, c);
		c.gridy++;
		c.gridy = 0;
		c.gridx = fieldColumn;
		add(panel);
		pack();
	}
}
