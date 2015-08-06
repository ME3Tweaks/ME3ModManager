package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.me3tweaks.modmanager.cellrenderers.HintTextFieldUI;

public class ContainerRow extends JPanel {
	private JTextField containerTitle, containerText;
	private JButton deleteContainerButton, updateContainer;
	/**
	 * Loads the variable and instantiates the interface
	 */
	public void configure() {
		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		deleteContainerButton = new JButton("Delete Container");
		add(deleteContainerButton);
		
		containerTitle = new JTextField(20);
		containerTitle.setUI(new HintTextFieldUI("Container Title"));
		add(containerTitle);
		
		containerText = new JTextField(50);
		containerText.setUI(new HintTextFieldUI("Container Text"));
		add(containerText);
		
		updateContainer = new JButton("Update Container");
		add(updateContainer);
	}
	
	public String toString(){
		return containerTitle.getText();
	}

	public JButton getDeleteContainerButton() {
		return deleteContainerButton;
	}

}
