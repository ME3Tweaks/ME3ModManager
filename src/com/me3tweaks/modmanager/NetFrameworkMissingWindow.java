package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class NetFrameworkMissingWindow extends JDialog {
	JLabel introLabel;
	JButton downloadButton;
	private static final String netPage = "https://www.microsoft.com/en-us/download/details.aspx?id=48130";

	public NetFrameworkMissingWindow() {
		this.setTitle(".NET Framework 4.5 not installed");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel updatePanel = new JPanel();
		updatePanel.setBorder(new EmptyBorder(5,5,5,5));
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("<html><div style=\"width:330px;\">Mod Manager requires .NET Framework 4.5 or higher. It does not appear to be installed.<br>"
				+ "You can download and install it from here:</div></html>");
		downloadButton = new JButton("Download .NET 4.6 from Microsoft");
		downloadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ModManager.openWebpage(new URL(netPage));
				} catch (MalformedURLException e1) {
					ModManager.debugLogger.writeError("Invalid URL for .NET! This shouldn't happen...");
				}
			}
		});

		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(5,5)));
		updatePanel.add(downloadButton);
		this.getContentPane().add(updatePanel);
	}
}
