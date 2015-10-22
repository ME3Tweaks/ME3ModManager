package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DLCFailedWindow extends JDialog {

	public DLCFailedWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow() {
		this.setTitle("DLC Verification Failure Messages");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setResizable(false);
		this.setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JLabel infoLabel = new JLabel(
				"<html><div style=\"width: 470px\">After installing a mod, if you get a screen similar to the one below at the main menu, the DLC listed (the list may be blank if the developer did not give their DLC a name) will not load. You must start the game from Mod Manager using Start Game (to use LauncherWV) or install a permanent bypass (Install binkw32 from the tools menu) to load modified or custom DLC.</div></html>");
		infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(infoLabel);

		BufferedImage wPic;
		JLabel wIcon = new JLabel("Failed to load image");

		try {
			wPic = ImageIO.read(this.getClass().getResource("/resource/DLC_AUTH_FAIL.png"));
			wIcon = new JLabel(new ImageIcon(wPic));
		} catch (IOException e1) {
			ModManager.debugLogger.writeError("Failed to load INVALID DLC IMAGE!");
		}
		wIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(10, 10)));

		panel.add(wIcon);

		JButton ok = new JButton("OK");
		ok.setAlignmentX(Component.CENTER_ALIGNMENT);
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel.add(Box.createRigidArea(new Dimension(10, 10)));
		panel.add(ok);
		panel.setAlignmentX(CENTER_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);
		this.pack();
	}
}
