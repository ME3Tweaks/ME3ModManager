package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModGroup;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Window for creating new mod groups or editing existing onesO
 * 
 * @author Mgamerz
 *
 */
public class ModGroupCreatorWindow extends JDialog implements ListSelectionListener {
	private ModGroup modGroup;
	private JList<Mod> modsAvailableForSelectionList;
	private JList<Mod> modsInGroupList;
	private JButton saveButton;
	private JSplitPane splitPane;
	private DefaultListModel<Mod> modsAvailableModel;
	private DefaultListModel<Mod> modsInGroupModel;
	private JTextField fieldModGroupName;
	private JTextArea fieldModGroupDescription;
	private JButton moveRightButton, moveLeftButton, moveUpButton, moveDownButton;
	private boolean windowResult = false;

	public ModGroupCreatorWindow() {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		setVisible(true);
	}

	/**
	 * Edit constructor
	 * 
	 * @param mg
	 */
	public ModGroupCreatorWindow(ModGroup mg) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
		this.modGroup = mg;
		setupWindow();
		applyModGroup();
		setVisible(true);
	}

	private void applyModGroup() {
		setTitle("Editing " + modGroup.getModGroupName() + " group");
		for (String descini : modGroup.getDescPaths()) { //for each item in our group...
			String lookingfor = ResourceUtils.normalizeFilePath(ModManager.getModsDir() + descini, true);
			//System.out.println("Looking for: "+lookingfor);
			for (int i = 0; i < modsAvailableModel.size(); i++) { //find the matching model index
				Mod mod = modsAvailableModel.getElementAt(i);
				if (mod.getDescFile().equalsIgnoreCase(lookingfor)) {
					Mod porting = modsAvailableModel.remove(i);
					modsInGroupModel.addElement(porting);
					break;
				}
			}
		}
		fieldModGroupDescription.setText(ResourceUtils.convertBrToNewline(modGroup.getModGroupDescription()));
		fieldModGroupName.setText(modGroup.getModGroupName());
		saveButton.setEnabled(true);
	}

	private void setupWindow() {
		setTitle("Create new mod group");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(500, 375));
		setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new BorderLayout());

		// Title Panel - TOP
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel(
				"<html><center>Move mods to the right side to add them to the group.<br>Organize the installation order so mods that autoconfigure are applied correctly.</center></html>",
				SwingConstants.CENTER), BorderLayout.NORTH);

		//GROUPS - WEST
		JPanel groupsPanel = new JPanel(new BorderLayout());
		TitledBorder modGroupsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Available mods");
		modsAvailableForSelectionList = new JList<Mod>();
		modsAvailableForSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modsAvailableForSelectionList.setLayoutOrientation(JList.VERTICAL);

		modsAvailableModel = new DefaultListModel<Mod>();
		modsAvailableForSelectionList.setModel(modsAvailableModel);

		JScrollPane leftListScroller = new JScrollPane(modsAvailableForSelectionList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		groupsPanel.setBorder(modGroupsBorder);
		groupsPanel.add(leftListScroller, BorderLayout.CENTER);

		moveRightButton = new JButton(">>");
		moveRightButton.setToolTipText("Add selected mods to the group");
		moveLeftButton = new JButton("<<");
		moveLeftButton.setToolTipText("Remove selected mods from the group");
		moveRightButton.setEnabled(false);
		moveLeftButton.setEnabled(false);
		JPanel movementPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		movementPanel.add(moveRightButton);

		groupsPanel.add(movementPanel, BorderLayout.SOUTH);

		//GROUP CONTENTS - EAST
		JPanel groupContentPanel = new JPanel(new BorderLayout());
		TitledBorder modsInGroupBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mods in group (install order)");
		modsInGroupList = new JList<Mod>();
		modsInGroupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modsInGroupList.setLayoutOrientation(JList.VERTICAL);

		modsInGroupModel = new DefaultListModel<Mod>();
		modsInGroupList.setModel(modsInGroupModel);

		JScrollPane rightListScroller = new JScrollPane(modsInGroupList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel modsInGroupPanel = new JPanel(new BorderLayout());
		modsInGroupPanel.setBorder(modsInGroupBorder);
		modsInGroupPanel.add(rightListScroller, BorderLayout.CENTER);

		JPanel orderingPanel = new JPanel();
		orderingPanel.setLayout(new BoxLayout(orderingPanel, BoxLayout.LINE_AXIS));
		moveUpButton = new JButton("Move up");
		moveDownButton = new JButton("Move down");
		moveDownButton.setEnabled(false);
		moveUpButton.setEnabled(false);
		orderingPanel.add(moveLeftButton);
		orderingPanel.add(Box.createHorizontalGlue());
		orderingPanel.add(moveUpButton);
		orderingPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		orderingPanel.add(moveDownButton);
		orderingPanel.add(Box.createHorizontalGlue());
		modsInGroupPanel.add(orderingPanel, BorderLayout.SOUTH);
		groupContentPanel.add(modsInGroupPanel, BorderLayout.CENTER);

		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		saveButton = new JButton("Save group");
		saveButton.setEnabled(false);
		actionsPanel.add(saveButton);

		//panel above buttons
		JPanel textPanel = new JPanel(new BorderLayout());
		fieldModGroupName = new JTextField();
		fieldModGroupDescription = new JTextArea(2, 20);

		JPanel groupnamePanel = new JPanel(new BorderLayout());
		JPanel groupdescriptionPanel = new JPanel(new BorderLayout());
		groupnamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Group name"));
		groupdescriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Group description"));
		groupnamePanel.add(fieldModGroupName, BorderLayout.CENTER);
		groupdescriptionPanel.add(fieldModGroupDescription, BorderLayout.CENTER);

		textPanel.add(groupnamePanel, BorderLayout.NORTH);
		textPanel.add(groupdescriptionPanel, BorderLayout.CENTER);
		textPanel.add(actionsPanel, BorderLayout.SOUTH);
		groupContentPanel.add(textPanel, BorderLayout.SOUTH);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupsPanel, groupContentPanel);
		splitPane.setResizeWeight(.5d);

		contentPanel.add(titlePanel, BorderLayout.NORTH);
		contentPanel.add(splitPane, BorderLayout.CENTER);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);

		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String message = validateSave();
				if (message == null) {
					ModManager.debugLogger.writeMessage("Saving group...");
					if (saveGroup()) {
						windowResult = true;
						dispose();
					}
				} else {
					ModManager.debugLogger.writeMessage("Error validating form: " + message);
					JOptionPane.showMessageDialog(ModGroupCreatorWindow.this, message, "Error saving group", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		moveRightButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				int[] selectedIndices = modsAvailableForSelectionList.getSelectedIndices();
				for (int i = selectedIndices.length - 1; i >= 0; i--) {
					ModManager.debugLogger.writeMessage("Moving right - remove left side index " + selectedIndices[i]);
					Mod porting = modsAvailableModel.remove(selectedIndices[i]);
					modsInGroupModel.addElement(porting);
				}
				saveButton.setEnabled(true);
			}
		});

		moveLeftButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int[] selectedIndices = modsInGroupList.getSelectedIndices();
				for (int i = selectedIndices.length - 1; i >= 0; i--) {
					ModManager.debugLogger.writeMessage("Moving left - remove right side index " + selectedIndices[i]);
					Mod porting = modsInGroupModel.remove(selectedIndices[i]);
					modsAvailableModel.addElement(porting);
				}
				saveButton.setEnabled(true);
			}
		});

		moveUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int[] selectedIndices = modsInGroupList.getSelectedIndices();
				for (int i = 0; i < selectedIndices.length; i++) {
					if (selectedIndices[i] != 0) {
						swap(modsInGroupModel, selectedIndices[i], selectedIndices[i] - 1); //swap in reverse order
						selectedIndices[i] -= 1;
					}
				}
				modsInGroupList.setSelectedIndices(selectedIndices);
			}
		});

		moveDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int[] selectedIndices = modsInGroupList.getSelectedIndices();
				for (int i = selectedIndices.length - 1; i >= 0; i--) {
					if (selectedIndices[i] != modsInGroupModel.size() - 1) {
						swap(modsInGroupModel, selectedIndices[i], selectedIndices[i] + 1); //swap in reverse order
						selectedIndices[i] += 1;
					}
				}
				modsInGroupList.setSelectedIndices(selectedIndices);
			}
		});

		modsInGroupList.addListSelectionListener(this);
		modsAvailableForSelectionList.addListSelectionListener(this);

		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.size(); i++) {
			Mod mod = ModManagerWindow.ACTIVE_WINDOW.modModel.get(i);
			modsAvailableModel.addElement(mod);
		}
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	/**
	 * Swaps two items in a listmodel
	 * 
	 * @param model
	 *            Model to swap in
	 * @param a
	 *            Index 1
	 * @param b
	 *            Index 2
	 */
	private void swap(DefaultListModel<Mod> model, int a, int b) {
		ModManager.debugLogger.writeMessage("Swapping items in list: " + a + " and " + b);
		if (a < 0 || b < 0 || a > model.size() - 1 || b > model.size() - 1) {
			return;
		}
		Mod aObject = model.getElementAt(a);
		Mod bObject = model.getElementAt(b);
		model.set(a, bObject);
		model.set(b, aObject);

	}

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (listChange.getSource() == modsInGroupList) {
				int[] selectedIndicies = modsInGroupList.getSelectedIndices();
				moveUpButton.setEnabled(selectedIndicies.length == 1);
				moveDownButton.setEnabled(selectedIndicies.length == 1);
				moveLeftButton.setEnabled(selectedIndicies.length > 0);
			} else if (listChange.getSource() == modsAvailableForSelectionList) {
				int[] selectedIndicies = modsAvailableForSelectionList.getSelectedIndices();
				moveRightButton.setEnabled(selectedIndicies.length > 0);
			}
		}
	}

	/**
	 * Saves the mod group to disk. Deletes the mod group if there is nothing in
	 * the list.
	 * 
	 * @return true if the dialog should close (saved or deleted), false if it
	 *         shouldn't (user declined deleting, invalid group)
	 */
	private boolean saveGroup() {
		int numModsInGroup = modsInGroupModel.size();
		if (numModsInGroup == 0 && modGroup != null) {
			//delete
			int result = JOptionPane.showConfirmDialog(this, "No mods have been placed into this group!\nDelete this group?", "Delete group", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				ModManager.debugLogger.writeMessage("Deleting empty modgroup: " + modGroup.getLoadFilePath());
				FileUtils.deleteQuietly(new File(modGroup.getLoadFilePath()));
			}
			return true;
		} else if (numModsInGroup == 0) {
			JOptionPane.showMessageDialog(this, "No mods have been placed into this group.", "Empty group", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			String output = fieldModGroupName.getText() + "\n";
			output += ResourceUtils.convertNewlineToBr(fieldModGroupDescription.getText()) + "\n";
			for (int i = 0; i < modsInGroupModel.size(); i++) {
				Mod mod = modsInGroupModel.get(i);
				String relativePath = ResourceUtils.getRelativePath(mod.getDescFile(), ModManager.getModsDir(), File.separator);
				output += relativePath + "\n";
			}
			String outputname = fieldModGroupName.getText().replaceAll("[^a-zA-Z]", "").toLowerCase();
			String outputpath = ModManager.getModGroupsFolder() + outputname + ".txt";
			if (modGroup != null) {
				FileUtils.deleteQuietly(new File(modGroup.getLoadFilePath()));
			}
			try {
				ModManager.debugLogger.writeMessage("Saving mod group file: " + outputpath);
				FileUtils.writeStringToFile(new File(outputpath), output, StandardCharsets.UTF_8);
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Error saving mod group!", e);
			}
			return true;
		}
	}

	/**
	 * Validates the saving form (does not validate the modlist).
	 * 
	 * @return null if OK, string error message if failed.
	 */
	private String validateSave() {
		String name = fieldModGroupName.getText();
		String outputname = fieldModGroupName.getText().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

		if (name.length() == 0 || outputname.length() == 0) {
			return "Mod Group name must be at least 1 alphanumeric character.";
		}

		String desc = fieldModGroupDescription.getText().trim();
		if (desc.length() == 0) {
			return "Mod group description cannot be empty.";
		}

		return null;
	}
	
	/**
	 * Gets the value inidicating if this window made changes.
	 * @return
	 */
	public boolean getWindowResult(){
		return windowResult ;
	}
}
