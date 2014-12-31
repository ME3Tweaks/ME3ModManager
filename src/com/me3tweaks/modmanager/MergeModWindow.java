package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class MergeModWindow extends JDialog implements ListSelectionListener, ActionListener {
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	JLabel infoLabel;
	// CheckBoxList dlcList;
	String consoleQueue[];
	boolean windowOpen = true, forceAuthentic = true;
	HashMap<String, JCheckBox> checkboxMap;
	ArrayList<String> failedBackups;
	String currentText;
	JPanel checkBoxPanel, mod1Panel, mod2Panel;
	JList<String> leftMods, rightMods;
	JProgressBar progressBar;
	JButton mergeButton;
	ArrayList<Mod> mods;
	JCheckBox checkboxForceOriginal;
	ModManagerWindow callingWindow;
	HashMap<String, Mod> listDescriptors;
	/**
	 * Manually invoked backup window
	 * @param callingWindow
	 * @param BioGameDir
	 */
	public MergeModWindow(ModManagerWindow callingWindow) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		listDescriptors = new HashMap<String, Mod>();
		mods = ModManager.getCMM3ModsFromDirectory();
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Mod Merger");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(900, 363));
		this.setResizable(false);
		
		JPanel contentPanel = new JPanel(new BorderLayout());

		// Title Panel
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.add(new JLabel("Select mods to merge. Only mods using CMM3 can be merged.", SwingConstants.CENTER), BorderLayout.NORTH);
		
		// ModsLists
		JPanel modsListPanel = new JPanel(new BorderLayout());
		
		//listDescriptors = new HashMap<String, Mod>();
		leftMods = new JList<String>(getModTitles());
		leftMods.addListSelectionListener(this);
		leftMods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftMods.setLayoutOrientation(JList.VERTICAL);
		JScrollPane leftListScroller = new JScrollPane(leftMods,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		rightMods = new JList<String>(getModTitles());
		rightMods.addListSelectionListener(this);
		rightMods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rightMods.setLayoutOrientation(JList.VERTICAL);
		JScrollPane rightListScroller = new JScrollPane(rightMods,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		modsListPanel.add(leftListScroller, BorderLayout.WEST);
		modsListPanel.add(rightListScroller, BorderLayout.EAST);
		
		mod1Panel = new JPanel();
		mod1Panel.setLayout(new BoxLayout(mod1Panel, BoxLayout.PAGE_AXIS));
		mod2Panel = new JPanel();
		mod2Panel.setLayout(new BoxLayout(mod2Panel, BoxLayout.PAGE_AXIS));
		JScrollPane midRightListScroller = new JScrollPane(mod2Panel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane midLeftListScroller = new JScrollPane(mod1Panel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		midLeftListScroller.setBorder(BorderFactory.createMatteBorder(3, 0, 3, 3, Color.BLUE));
		midRightListScroller.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 0, Color.ORANGE));

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		centerPanel.add(midLeftListScroller);
		centerPanel.add(midRightListScroller);
		
		modsListPanel.add(centerPanel, BorderLayout.CENTER);
		
		mergeButton = new JButton("Select a mod from both sides to merge");
		mergeButton.setEnabled(false);
		mergeButton.addActionListener(this);
		contentPanel.add(titlePanel, BorderLayout.NORTH);
		contentPanel.add(modsListPanel, BorderLayout.CENTER);
		contentPanel.add(mergeButton, BorderLayout.SOUTH);
		add(contentPanel);
	}

	@Override
	public void valueChanged(ListSelectionEvent listChange) {
		if (listChange.getValueIsAdjusting() == false) {
			if (listChange.getSource() == leftMods) {
				if (leftMods.getSelectedIndex() == -1) {
					updateFilesPanel(mod1Panel, null);
				} else {
					updateFilesPanel(mod1Panel, mods.get(leftMods.getSelectedIndex()));
				}
			}
			if (listChange.getSource() == rightMods) {
				if (rightMods.getSelectedIndex() == -1) {
					updateFilesPanel(mod2Panel, null);
				} else {
					updateFilesPanel(mod2Panel, mods.get(rightMods.getSelectedIndex()));
				}
			}
			
			//validate merge button
			if (leftMods.getSelectedIndex() < 0 || rightMods.getSelectedIndex() < 0) {
				mergeButton.setText("Select a mod from both sides to merge");
				mergeButton.setEnabled(false);
				return;
			}
			
			if (leftMods.getSelectedIndex() == rightMods.getSelectedIndex()) {
				mergeButton.setText("Cannot merge mod with itself");
				mergeButton.setEnabled(false);
				return;
			}
			
			if (leftMods.getSelectedIndex() >= 0 && rightMods.getSelectedIndex() >= 0) {
				mergeButton.setEnabled(true);
				if (mods.get(leftMods.getSelectedIndex()).canMergeWith(mods.get(rightMods.getSelectedIndex()))) {
					mergeButton.setText("Merge mods");
				} else {
					mergeButton.setText("Resolve Merge Conflicts");
				}
			} else {
				mergeButton.setEnabled(false);
			}
		}
	}
	
	
	
	private void updateFilesPanel(JPanel panel, Mod mod) {
		if (mod == null) {
			System.out.println("REMOVING ALL");
			panel.removeAll();
			panel.add(new JLabel("Select a mod for merging."));
			
		} else {
			panel.removeAll();
			ModJob[] jobs = mod.getJobs();
			for (ModJob job : jobs){
				panel.add(new JLabel(job.jobName));
				for (String file : job.getFilesToReplace()) {
					//String display = ResourceUtils.getRelativePath(System.out.printlnSystem.getProperty("user.dir"),File.separator);
					panel.add(new JLabel("  "+file));
				}
			}
		}
		panel.revalidate();
	}
	
	private String[] getModTitles() {
		String[] titles = new String[mods.size()];
		for (int i = 0; i < mods.size(); i++) {
			titles[i] = mods.get(i).getModName();
		}
		return titles;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mergeButton) {
			Mod mod1 = mods.get(leftMods.getSelectedIndex());
			Mod mod2 = mods.get(rightMods.getSelectedIndex());
			System.out.println("merge window: merge clicked");
			if (mod1.canMergeWith(mod2)) {
				String s = (String) JOptionPane.showInputDialog(this, "Enter a new name for this mod. The new mod's files will be placed in this folder.","Merged Mod Name", JOptionPane.PLAIN_MESSAGE, null, null, null);
				if (s!=null && !s.equals("")) {
					s = s.trim();
					Mod merged = mod1.mergeWith(mod2,s);
					merged.createNewMod();
					dispose();
					callingWindow.dispose();
					new ModManagerWindow(false);
				}
				
			} else {
				new MergeConflictResolutionWindow(this, mod1, mod2);
			}
		}
	}
}
