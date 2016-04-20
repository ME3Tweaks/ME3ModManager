package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.ui.ModCellRenderer;

/**
 * Patch Window shows the list of patches in the patch library and things you
 * can do with it
 * 
 * @author Mgamerz
 *
 */
public class FailedModsWindow extends JDialog implements ListSelectionListener {
	DefaultListModel<Mod> failedModsModel;
	private JList<Mod> failedModList;
	private JTextArea failedModDesc;

	public FailedModsWindow() {
		ModManager.debugLogger.writeMessage("Loading failed mods window");
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Invalid Mods List");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(600, 480));
		this.setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JSplitPane lrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		failedModList = new JList<Mod>();
		failedModsModel = new DefaultListModel<Mod>();
		failedModList.setModel(failedModsModel);
		failedModList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		failedModList.addListSelectionListener(this);
		failedModList.setCellRenderer(new ModCellRenderer());
		for (Mod mod : ModManagerWindow.ACTIVE_WINDOW.getInvalidMods()) {
			failedModsModel.addElement(mod);
		}
		lrSplitPane.setLeftComponent(new JScrollPane(failedModList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		failedModDesc = new JTextArea("Select a mod on the left to see why it failed to load.");

		failedModDesc.setLineWrap(true);
		failedModDesc.setWrapStyleWord(true);
		failedModDesc.setEditable(false);
		lrSplitPane.setRightComponent(new JScrollPane(failedModDesc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		contentPanel.add(lrSplitPane, c);

		// statusbar
		// ButtonPanel
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);

		// listeners
		pack();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		System.out.println("values have changed.");
		if (e.getValueIsAdjusting() == false) {
			if (failedModList.getSelectedIndex() == -1) {
				failedModDesc.setText("Select a mod on the left to see why it failed to load.");
			} else {
				updateDescription();
			}
		} else {
			updateDescription();
		}
	}

	private void updateDescription() {
		String description = "";
		List<Mod> selectedMod = failedModList.getSelectedValuesList();
		boolean isFirst = true;
		for (Mod mod : selectedMod) {
			if (isFirst) {
				isFirst = false;
			} else {
				description += "---------------------------------\n";
			}
			description += mod.getModName();
			description += "    "+mod.getModPath();
			description += "\n\n";
			description += mod.getFailedReason();
		}
		failedModDesc.setText(description);
	}
}
