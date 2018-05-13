package com.me3tweaks.modmanager.moddesceditor;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.ModJob;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MDEModFolderChooser extends JDialog {
	private String selectedFolder;

	public String getSelectedFile() {
		return selectedFolder;
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFolder = selectedFile;
	}

	public MDEModFolderChooser(ModDescEditorWindow callingWindow, String currentOption, int optionType, ModJob job) {
		setupWindow(callingWindow, currentOption, optionType, job);
		setVisible(true);
	}

	public void setupWindow(ModDescEditorWindow callingWindow, String currentOption, int optionType, ModJob job) {
		JPanel contentPanel = new JPanel(new BorderLayout());
		DefaultListModel<String> model = new DefaultListModel<String>();
		for (String folder : job.getDestFolders()) {
			model.addElement(folder);
		}
		for (MDEConditionalDLCItem conddlc : callingWindow.getConditionalDLCItems()) {
			if (conddlc.getOperationBox().getSelectedIndex() == 0) {
				model.addElement(conddlc.getDLCDestination());
			}
		}

		JList<String> mainFileList = new JList<String>(model);
		JScrollPane listScroller = new JScrollPane(mainFileList);
		contentPanel.add(listScroller, BorderLayout.CENTER);

		JButton setFile = new JButton("Set File");
		setFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateFields()) {
					selectedFolder = model.get(mainFileList.getSelectedIndex());
					dispose();
				}
			}

			private boolean validateFields() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);

		setMinimumSize(new Dimension(500, 400));
		setIconImages(ModManager.ICONS);
		setTitle("Select folder");
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setLocationRelativeTo(callingWindow);
	}
}
