package com.me3tweaks.modmanager.objects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;

public class HelpMenuItem {
	private String menuItemTitle;
	private String tooltipText;
	private String url;
	private String popupText;
	private String popupTitle;
	private JLabel picture;
	private int messageType;

	public HelpMenuItem(String menuItemTitle, String popupTitle, String popupText) {
		this.menuItemTitle = menuItemTitle;
		this.popupTitle = popupTitle;
		this.popupText = popupText;
		this.messageType = JOptionPane.INFORMATION_MESSAGE;
	}

	public HelpMenuItem(String menuItemTitle, String popupTitle, String popupText, JLabel picture) {
		this.menuItemTitle = menuItemTitle;
		this.popupTitle = popupTitle;
		this.popupText = popupText;
		this.picture = picture;
		this.messageType = JOptionPane.INFORMATION_MESSAGE;	}

	public JMenuItem createMenuItem() {
		JMenuItem item = new JMenuItem(menuItemTitle);
		if (tooltipText != null) {
			item.setToolTipText(tooltipText);
		}

		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (url != null) {
					try {
						ModManager.openWebpage(new URL(url));
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "<html><div style=\"width: 300px\">" + popupText + "</div></html>",
							popupTitle, messageType);
				}
			}
		});

		return item;
	}
}
