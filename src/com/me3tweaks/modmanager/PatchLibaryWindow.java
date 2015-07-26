package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.me3tweaks.modmanager.cellrenderers.ModCellRenderer;
import com.me3tweaks.modmanager.cellrenderers.PatchCellRenderer;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;

/**
 * Patch Window shows the list of patches in the patch library and things you
 * can do with it
 * 
 * @author Mgamerz
 *
 */
public class PatchLibaryWindow extends JDialog implements ListSelectionListener, ActionListener {
	DefaultListModel<Patch> patchModel;
	private JList<Patch> patchList;
	private JTextArea patchDesc;
	private JButton buttonApplyPatch;
	private JComboBox<Mod> modComboBox;
	DefaultComboBoxModel<Mod> modModel;

	public PatchLibaryWindow() {
		ModManager.debugLogger.writeMessage("Loading patch library interface");
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Patch Library");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(600, 480));

		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JSplitPane lrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		patchList = new JList<Patch>();
		patchModel = new DefaultListModel<Patch>();
		patchList.setModel(patchModel);
		patchList.addListSelectionListener(this);
		patchList.setCellRenderer(new PatchCellRenderer());
		for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
			patchModel.addElement(patch);
		}
		lrSplitPane.setLeftComponent(new JScrollPane(patchList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		patchDesc = new JTextArea("PATCH DESCRIPTION");
		patchDesc.setLineWrap(true);
		patchDesc.setWrapStyleWord(true);
		patchDesc.setEditable(false);
		lrSplitPane.setRightComponent(new JScrollPane(patchDesc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		contentPanel.add(lrSplitPane, c);

		//statusbar
		// ButtonPanel
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints bc = new GridBagConstraints();

		modModel = new DefaultComboBoxModel<Mod>();
		modComboBox = new JComboBox<Mod>();
		modComboBox.setModel(modModel);
		modComboBox.setRenderer(new ModCellRenderer());

		Mod mod = new Mod(null); //invalid
		mod.setModName("Select a mod to patch");

		modModel.addElement(mod);
		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.getSize(); i++) {
			Mod loadedMod = ModManagerWindow.ACTIVE_WINDOW.modModel.get(i);
			modModel.addElement(loadedMod);
		}

		buttonApplyPatch = new JButton("Apply Patch");
		buttonApplyPatch.addActionListener(this);
		buttonApplyPatch.setEnabled(false);
		buttonApplyPatch.setToolTipText("Applies this patch to a mod");

		//buttonStartGame = new JButton("Start Game");
		//buttonStartGame.addActionListener(this);
		//buttonStartGame.setToolTipText("Starts the game. If LauncherWV DLC bypass is installed, it will that to launch the game instead");
		bc.anchor = GridBagConstraints.WEST;
		bc.weightx = 1;
		buttonPanel.add(modComboBox, bc);

		bc.anchor = GridBagConstraints.EAST;
		//	bc.fill = GridBagConstraints.HORIZONTAL;
		bc.weightx = 0;
		buttonPanel.add(buttonApplyPatch, bc);
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		//buttonPanel.add(buttonStartGame);
		c.gridy = 10;
		c.weighty = 0;
		contentPanel.add(buttonPanel, c);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);

		//listeners
		modComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (modComboBox.getSelectedIndex() > 0 && patchList.getSelectedIndex() > -1) {
					buttonApplyPatch.setEnabled(true);
				} else {
					buttonApplyPatch.setEnabled(false);
				}
			}
		});

		pack();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if (patchList.getSelectedIndex() == -1) {
				patchDesc.setText("Select one or more patches on the left to see their descriptions");
				buttonApplyPatch.setEnabled(false);
			}
		} else {
			String description = "";
			List<Patch> selectedPatches = patchList.getSelectedValuesList();
			boolean isFirst = true;
			for (Patch patch : selectedPatches) {
				if (isFirst) {
					isFirst = false;
				} else {
					description += "---------------------------------\n";
				}
				description += patch.getPatchName();
				description += "\n\n";
				description += patch.getPatchDescription();
				description += "\n\n";
				description += "Modifies: " + patch.getTargetModule() + " => " + patch.getTargetPath();
				description += "\n";
				if (patch.getPatchAuthor()!=null) {
					description += "Created by "+patch.getPatchAuthor()+"\n";
				}
			}
			patchDesc.setText(description);
			if (modComboBox.getSelectedIndex() > 0) {
				buttonApplyPatch.setEnabled(true);
			} else {
				buttonApplyPatch.setEnabled(false);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonApplyPatch) {
			ModManager.debugLogger.writeMessage("Applying patches");
			List<Patch> selectedPatches = patchList.getSelectedValuesList();
			Mod mod = modModel.getElementAt(modComboBox.getSelectedIndex());
			if (!mod.isValidMod()) {
				return; //this shouldn't be reachable anyways
			}
			boolean patchFailed = false;
			for (Patch patch : selectedPatches) {
				ModManager.debugLogger.writeMessage("Applying patch " + patch.getPatchName() + " to " + mod.getModName());
				if (!patch.applyPatch(mod)) {
					patchFailed = true;
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Patch failed to apply: " + patch.getPatchName(), "Patch not applied", JOptionPane.ERROR_MESSAGE);
				}
			}
			new AutoTocWindow(mod);
			if (!patchFailed) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "All patches were applied.", "Patches applied", JOptionPane.INFORMATION_MESSAGE);
			}
			dispose();
			new ModManagerWindow(false);
		}
	}

}
