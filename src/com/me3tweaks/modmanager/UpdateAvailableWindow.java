package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class UpdateAvailableWindow extends JDialog {
	JLabel introLabel, versionsLabel, changelogLabel;
	JButton updateButton;
	JSONObject updateInfo;

	public UpdateAvailableWindow(JSONObject updateInfo, JFrame callingWindow) {
		this.updateInfo = updateInfo;
		this.setTitle("Update Available");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		long width = (long) updateInfo.get("dialog_width"); //dialog size is determined by the latest build information. This is because it might have a long changelog.
		long height = (long) updateInfo.get("dialog_height");
		this.setPreferredSize(new Dimension((int)width, (int)height));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("An update to Mod Manager is available from ME3Tweaks.");
		String latest_version_hr = (String) updateInfo.get("latest_version_hr");
		long latest_build_number = (long) updateInfo.get("latest_build_number");

		versionsLabel = new JLabel("<html>Local Version: "+ModManager.VERSION+" (Build "+ModManager.BUILD_NUMBER+")<br>"
				+ "Latest Version: "+latest_version_hr+" (Build "+latest_build_number+")</html>");
		String release_notes = (String) updateInfo.get("release_notes");
		changelogLabel = new JLabel(release_notes);
		updateButton = new JButton("Download Update");
		
		
		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		updatePanel.add(versionsLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));

		updatePanel.add(changelogLabel);
		updatePanel.add(updateButton);
		
		//aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		updatePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(updatePanel);
	}
}
