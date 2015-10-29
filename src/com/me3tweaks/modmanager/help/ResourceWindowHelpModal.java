package com.me3tweaks.modmanager.help;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
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

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;

public class ResourceWindowHelpModal extends JDialog {

	public ResourceWindowHelpModal(HelpItemPackage pack) {
		setupWindow(pack);
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow(HelpItemPackage pack) {
		this.setTitle(pack.getModalTitle());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JLabel infoLabel = new JLabel("<html>" + pack.getModalMessage() + "</html>");
		infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(infoLabel);

		BufferedImage wPic;
		JLabel wIcon = new JLabel("Additional help item resource located at:\n" + pack.getResource());

		if (pack.getResource().toLowerCase().endsWith(".jpg") || pack.getResource().toLowerCase().endsWith(".png")) {
			try {
				wPic = ImageIO.read(new File(pack.getResource()));
				wIcon = new JLabel(new ImageIcon(wPic));
			} catch (IOException | NullPointerException e1) {
				wIcon = new JLabel("Failed to load image resource");
				ModManager.debugLogger.writeErrorWithException("Failed to load IMAGE!", e1);
			}
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
