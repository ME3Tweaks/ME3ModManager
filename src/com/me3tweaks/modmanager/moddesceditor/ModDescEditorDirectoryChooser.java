package com.me3tweaks.modmanager.moddesceditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.me3tweaks.modmanager.ModManager;

public class ModDescEditorDirectoryChooser extends JDialog {
	private File chosenFile;

	public ModDescEditorDirectoryChooser(ArrayList<File> choices) {
		setupWindow(choices);
		setVisible(true);
	}

	private void setupWindow(ArrayList<File> choices) {
		this.setTitle("Add Custom DLC Folder for installation");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		this.setMinimumSize(new Dimension(300, 300));

		DefaultListModel<String> demoList = new DefaultListModel<String>();
		for (File f : choices) {
			demoList.addElement(f.getName());
		}
		JList<String> choiceList = new JList<String>(demoList);
		choiceList.setVisibleRowCount(8);

		choiceList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {

				JList<String> list = (JList<String>) evt.getSource();
				Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
				if (r != null && r.contains(evt.getPoint())) {
					if (evt.getClickCount() == 2) {
						// Double-click detected
						int index = list.locationToIndex(evt.getPoint());
						chosenFile = choices.get(index);
						dispose();
					} else if (evt.getClickCount() == 3) {
						// Triple-click detected
						int index = list.locationToIndex(evt.getPoint());
						chosenFile = choices.get(index);
						dispose();
					}
				}
			}
		});

		JPanel chooserPanel = new JPanel(new BorderLayout());
		chooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chooserPanel.add(choiceList, BorderLayout.CENTER);

		JButton chooseButton = new JButton("Add DLC folder");
		chooserPanel.add(chooseButton, BorderLayout.SOUTH);

		add(chooserPanel);
		pack();
	}

	public File getChosenFile() {
		return chosenFile;
	}

	public void setChosenFile(File chosenFile) {
		this.chosenFile = chosenFile;
	}
}
