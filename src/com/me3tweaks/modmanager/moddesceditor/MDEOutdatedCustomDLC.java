package com.me3tweaks.modmanager.moddesceditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;

import com.me3tweaks.modmanager.ui.HintTextFieldUI;

public class MDEOutdatedCustomDLC {
	private JXCollapsiblePane collapsablePanel;

	public JXCollapsiblePane getPanel() {
		return collapsablePanel;
	}

	public JTextField getOutdatedDLCNameField() {
		return userReasonField;
	}

	public void setUserReasonField(JTextField userReasonField) {
		this.userReasonField = userReasonField;
	}

	private JButton minusButton;
	private JTextField userReasonField;
	private ModDescEditorWindow owningWindow;
	private static int itemSpacing = 5;

	public MDEOutdatedCustomDLC(ModDescEditorWindow owningWindow, String outdatedCustomDLC) {
		this.owningWindow = owningWindow;
		setupPanel(outdatedCustomDLC);
	}

	private void setupPanel(String outdatedCustomDLC) {
		collapsablePanel = new JXCollapsiblePane();
		collapsablePanel.setCollapsed(true);
		JPanel panel = new JPanel(new HorizontalLayout());
		collapsablePanel.add(panel);
		minusButton = new JButton("-");
		panel.add(minusButton);
		minusButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				collapsablePanel.setCollapsed(true);
				owningWindow.removeOutdatedCustomDLCItem(MDEOutdatedCustomDLC.this);
			}
		});

		userReasonField = new JTextField(outdatedCustomDLC);
		userReasonField.setUI(new HintTextFieldUI("DLC_MOD_OLDName"));

		userReasonField.setToolTipText("User friendly name. If one is not entered, a automatically generated one is displayed instead.");
		userReasonField.setColumns(30);
		panel.add(Box.createRigidArea(new Dimension(itemSpacing, itemSpacing)));
		panel.add(userReasonField);
		collapsablePanel.add(panel);

	}
}
