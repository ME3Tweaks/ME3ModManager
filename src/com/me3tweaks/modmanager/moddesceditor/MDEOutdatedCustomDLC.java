package com.me3tweaks.modmanager.moddesceditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;

import com.me3tweaks.modmanager.objects.AlternateFile;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.SwingLink;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

import javafx.application.Platform;
import javafx.stage.FileChooser;

public class MDEOutdatedCustomDLC {
	private JXCollapsiblePane collapsablePanel;

	public JXCollapsiblePane getPanel() {
		return collapsablePanel;
	}

	public JTextField getUserReasonField() {
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

	}
}
