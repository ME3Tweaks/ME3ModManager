package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
 * Manages mod groups
 * 
 * @author Mgamerz
 *
 */
public class ModGroupWindow extends JDialog implements ActionListener, ListSelectionListener {

	private JList<ModGroup> modGroupsList;
	private JList<Mod> modsInGroupList;
	private JButton mergeButton;
	private JSplitPane splitPane;
	private boolean painted = false;
	private JLabel groupDescription;
	private DefaultListModel<ModGroup> modGroupModel;
	private DefaultListModel<Mod> groupContentsModel;

	public ModGroupWindow() {
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Batch Mod Installer");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(400, 363));
		this.setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new BorderLayout());

		// Title Panel - TOP
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel("Create a mod group so you can quickly batch install a specific set of mods.", SwingConstants.CENTER), BorderLayout.NORTH);

		//GROUPS - WEST
		JPanel groupsPanel = new JPanel(new BorderLayout());
		TitledBorder modGroupsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mod groups");
		modGroupsList = new JList<ModGroup>();
		modGroupsList.addListSelectionListener(this);
		modGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modGroupsList.setLayoutOrientation(JList.VERTICAL);

		modGroupModel = new DefaultListModel<ModGroup>();
		modGroupsList.setModel(modGroupModel);
		for (ModGroup mod : getModGroups()) {
			modGroupModel.addElement(mod);
		}
		JScrollPane leftListScroller = new JScrollPane(modGroupsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		groupsPanel.setBorder(modGroupsBorder);
		groupsPanel.add(leftListScroller, BorderLayout.CENTER);

		//GROUP CONTENTS - EAST
		JPanel groupContentPanel = new JPanel(new BorderLayout());
		TitledBorder modsInGroupBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mods in group");
		modsInGroupList = new JList<Mod>();
		modsInGroupList.addListSelectionListener(this);
		modsInGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modsInGroupList.setLayoutOrientation(JList.VERTICAL);
		
		groupContentsModel = new DefaultListModel<Mod>();
		modsInGroupList.setModel(groupContentsModel);

		JScrollPane rightListScroller = new JScrollPane(modsInGroupList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel bottomRightPanel = new JPanel(new BorderLayout());
		bottomRightPanel.setBorder(modsInGroupBorder);
		bottomRightPanel.add(rightListScroller, BorderLayout.CENTER);

		JPanel topRightPanel = new JPanel();
		groupDescription = new JLabel("Select a mod group");
		topRightPanel.add(groupDescription);

		groupContentPanel.add(topRightPanel, BorderLayout.NORTH);
		groupContentPanel.add(bottomRightPanel, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupsPanel, groupContentPanel);
		splitPane.setResizeWeight(.5d);
		mergeButton = new JButton("Select a mod from both sides to merge");
		//mergeButton.setEnabled(false);
		//mergeButton.addActionListener(this);

		contentPanel.add(titlePanel, BorderLayout.NORTH);
		contentPanel.add(splitPane, BorderLayout.CENTER);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (listChange.getSource() == modGroupsList) {
				int index = modGroupsList.getSelectedIndex();
				if (index >= 0) {
					ModGroup mg = modGroupModel.getElementAt(index);
					groupDescription.setText(mg.getModGroupDescription());
					groupContentsModel.clear();
					for (String descini : mg.getDescPaths()) {
						for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.size(); i++) {
							Mod mod = ModManagerWindow.ACTIVE_WINDOW.modModel.getElementAt(i);
							String lookingfor = ResourceUtils.normalizeFilePath(ModManager.getModsDir()+descini,true);
							System.out.println("Looking for: "+lookingfor);

							if (mod.getDescFile().equalsIgnoreCase(lookingfor)) {
								groupContentsModel.addElement(mod);
							} else {
								System.out.println("Not matched: "+mod.getDescFile());

							}

						}
					}
				}
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (!painted) {
			painted = true;
			splitPane.setDividerLocation(0.4);
		}
	}

	public ArrayList<ModGroup> getModGroups() {
		ArrayList<ModGroup> groups = new ArrayList<>();
		String modGroupFolder = ModManager.getModGroupsFolder();
		String[] extensions = new String[] { "txt" };
		List<File> files = (List<File>) FileUtils.listFiles(new File(modGroupFolder), extensions, false);
		for (File file : files) {
			System.out.println("file: " + file.getAbsolutePath());
			ModGroup mg = new ModGroup(file.getAbsolutePath());
			groups.add(mg);
		}

		return groups;

	}
}
