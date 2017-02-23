package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.ui.ConflictResolutionRadioButton;

public class MergeConflictResolutionWindow extends JDialog implements ActionListener {
	JButton mergeButton, favorLeft, favorRight;
	HashMap<String, ArrayList<String>> conflictReplaceFiles, conflictAddRemoveFiles, conflictReplaceRemoveFiles, conflictAddFiles;
	HashMap<String, ArrayList<ButtonGroup>> buttonGroups;
	private Mod mod1;
	private Mod mod2;
	private MergeModWindow callingWindow;

	public MergeConflictResolutionWindow(MergeModWindow callingWindow, Mod mod1, Mod mod2) {
		ModManager.debugLogger.writeMessage("===Opening MergeConflictResolutionWindow===");
		this.callingWindow = callingWindow;
		this.mod1 = mod1;
		this.mod2 = mod2;
		buttonGroups = new HashMap<String, ArrayList<ButtonGroup>>();
		ModManager.debugLogger.writeMessage("Opening Merge Conflict Resolution Window");
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Merge Conflicts");
		setModal(true);
		setIconImages(ModManager.ICONS);

		JPanel contentPanel = new JPanel(new BorderLayout());
		JScrollPane listScroller = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel topPanel = new JPanel(new BorderLayout());

		JLabel info = new JLabel(
				"<html>The mods you have chosen to merge both attempt to modify or remove the same files.<br>Since mods are full file replacement, you must choose which file or operation you will use for the merged mod.</html>");
		topPanel.add(info, BorderLayout.NORTH);

		JPanel favorPanel = new JPanel(new BorderLayout());
		favorLeft = new JButton("Select all from " + mod1.getModName());
		favorRight = new JButton("Select all from " + mod2.getModName());
		favorLeft.addActionListener(this);
		favorRight.addActionListener(this);

		favorPanel.add(favorLeft, BorderLayout.WEST);
		favorPanel.add(favorRight, BorderLayout.EAST);
		topPanel.add(favorPanel, BorderLayout.CENTER);

		JPanel conflictPanel = new JPanel();
		conflictPanel.setLayout(new BoxLayout(conflictPanel, BoxLayout.PAGE_AXIS));
		conflictReplaceFiles = mod1.getReplaceConflictsWithMod(mod2);
		conflictAddFiles = mod1.getAddConflictsWithMod(mod2);
		conflictAddRemoveFiles = mod1.getAddRemoveConflictsWithMod(mod2);
		conflictReplaceRemoveFiles = mod1.getReplaceRemoveConflictsWithMod(mod2);

		HashMap<String, JPanel> modulePanelMap = new HashMap<String, JPanel>();

		if (conflictReplaceFiles != null) {
			//REPLACE CONFLICTS
			Iterator<Map.Entry<String, ArrayList<String>>> it = conflictReplaceFiles.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>) it.next();
				String module = pairs.getKey();
				JPanel moduleConflictPanel = new JPanel();
				if (modulePanelMap.get(module) != null) {
					moduleConflictPanel = modulePanelMap.get(module);
				} else {
					modulePanelMap.put(module, moduleConflictPanel); //won't have conflicts as this is the first
					moduleConflictPanel.setLayout(new BoxLayout(moduleConflictPanel, BoxLayout.PAGE_AXIS));
					TitledBorder moduleBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Conflicts in " + module);
					moduleConflictPanel.setBorder(moduleBorder);
				}

				//moduleConflictPanel.add(moduleLabel);

				for (String conflictFile : pairs.getValue()) {
					JPanel singleConflictPanel = new JPanel(new BorderLayout());
					TitledBorder conflictBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Replaced by mods: " + FilenameUtils.getName(conflictFile));
					conflictBorder.setTitleJustification(TitledBorder.CENTER);
					singleConflictPanel.setBorder(conflictBorder);
					ButtonGroup bg = new ButtonGroup();
					//JLabel fileLabel = new JLabel(FilenameUtils.getName(conflictFile));

					ConflictResolutionRadioButton mod1Button = new ConflictResolutionRadioButton("Use " + mod1.getModName());
					mod1Button.setActionCommand("left");
					mod1Button.addActionListener(this);

					ConflictResolutionRadioButton mod2Button = new ConflictResolutionRadioButton("Use " + mod2.getModName());
					mod2Button.setActionCommand("right");
					mod2Button.addActionListener(this);
					bg.add(mod1Button);
					bg.add(mod2Button);
					if (buttonGroups.containsKey(module)) {
						buttonGroups.get(module).add(bg);
					} else {
						ArrayList<ButtonGroup> moduleGroup = new ArrayList<ButtonGroup>();
						moduleGroup.add(bg);
						buttonGroups.put(module, moduleGroup);
					}
					//singleConflictPanel.add(fileLabel, BorderLayout.NORTH);
					singleConflictPanel.add(mod1Button, BorderLayout.WEST);
					singleConflictPanel.add(mod2Button, BorderLayout.EAST);
					moduleConflictPanel.add(singleConflictPanel);
				}
				//it.remove(); // avoids a ConcurrentModificationException
			}
		}
		//ADDCONFLICTS
		if (conflictAddFiles != null) {
			Iterator<Map.Entry<String, ArrayList<String>>> addIterator = conflictAddFiles.entrySet().iterator();
			while (addIterator.hasNext()) {
				Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>) addIterator.next();
				String module = pairs.getKey();
				JPanel moduleConflictPanel = new JPanel();
				if (modulePanelMap.get(module) != null) {
					moduleConflictPanel = modulePanelMap.get(module);
				} else {
					modulePanelMap.put(module, moduleConflictPanel); //won't have conflicts as this is the first
					moduleConflictPanel.setLayout(new BoxLayout(moduleConflictPanel, BoxLayout.PAGE_AXIS));
					TitledBorder moduleBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Conflicts in " + module);
					moduleConflictPanel.setBorder(moduleBorder);
				}
				for (String conflictFile : pairs.getValue()) {
					JPanel singleConflictPanel = new JPanel(new BorderLayout());
					TitledBorder conflictBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Added by mods: " + FilenameUtils.getName(conflictFile));
					conflictBorder.setTitleJustification(TitledBorder.CENTER);
					singleConflictPanel.setBorder(conflictBorder);
					ButtonGroup bg = new ButtonGroup();

					ConflictResolutionRadioButton mod1Button = new ConflictResolutionRadioButton("Use " + mod1.getModName());
					mod1Button.setActionCommand("left");
					mod1Button.addActionListener(this);

					ConflictResolutionRadioButton mod2Button = new ConflictResolutionRadioButton("Use " + mod2.getModName());
					mod2Button.setActionCommand("right");
					mod2Button.addActionListener(this);
					bg.add(mod1Button);
					bg.add(mod2Button);
					if (buttonGroups.containsKey(module)) {
						buttonGroups.get(module).add(bg);
					} else {
						ArrayList<ButtonGroup> moduleGroup = new ArrayList<ButtonGroup>();
						moduleGroup.add(bg);
						buttonGroups.put(module, moduleGroup);
					}
					//singleConflictPanel.add(fileLabel, BorderLayout.NORTH);
					singleConflictPanel.add(mod1Button, BorderLayout.WEST);
					singleConflictPanel.add(mod2Button, BorderLayout.EAST);
					moduleConflictPanel.add(singleConflictPanel);
				}
				conflictPanel.add(moduleConflictPanel);
			}
		}

		//ADDREMOVECONFLICTS
		if (conflictAddRemoveFiles != null) {
			Iterator<Map.Entry<String, ArrayList<String>>> addIterator = conflictAddRemoveFiles.entrySet().iterator();
			while (addIterator.hasNext()) {
				Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>) addIterator.next();
				String module = pairs.getKey();
				JPanel moduleConflictPanel = new JPanel();
				if (modulePanelMap.get(module) != null) {
					moduleConflictPanel = modulePanelMap.get(module);
				} else {
					modulePanelMap.put(module, moduleConflictPanel); //won't have conflicts as this is the first
					moduleConflictPanel.setLayout(new BoxLayout(moduleConflictPanel, BoxLayout.PAGE_AXIS));
					TitledBorder moduleBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Conflicts in " + module);
					moduleConflictPanel.setBorder(moduleBorder);
				}
				for (String conflictFile : pairs.getValue()) {
					JPanel singleConflictPanel = new JPanel(new BorderLayout());
					TitledBorder conflictBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Added/Removed by mods: " + FilenameUtils.getName(conflictFile));
					conflictBorder.setTitleJustification(TitledBorder.CENTER);
					singleConflictPanel.setBorder(conflictBorder);
					ButtonGroup bg = new ButtonGroup();

					//Determine if mod1 adds or removes this file
					boolean mod1IsAdd = false;
					for (ModJob job : mod1.getJobs()) {
						ArrayList<String> addFileTargets = job.getFilesToAddTargets();
						for (String addTarget : addFileTargets) {
							if (addTarget.equals(conflictFile)) {
								mod1IsAdd = true;
								break;
							}
						}
						if (mod1IsAdd) {
							break;
						}
					}

					ConflictResolutionRadioButton mod1Button = new ConflictResolutionRadioButton((mod1IsAdd ? "Add file " : "Remove file ")
							+ mod1.getModName());
					ConflictResolutionRadioButton mod2Button = new ConflictResolutionRadioButton((mod1IsAdd ? "Remove file " : "Add file ")
							+ mod2.getModName());

					mod1Button.setActionCommand("left");
					mod1Button.setModule(module);
					mod1Button.setConflictType(mod1IsAdd ? ConflictResolutionRadioButton.ADD : ConflictResolutionRadioButton.REMOVE);
					mod1Button.setConflictTarget(conflictFile);
					mod1Button.setSourcePath(mod1IsAdd ? mod1.findTargetSourceFileFromJob(false, mod1.getJobByModuleName(module), conflictFile)
							: null);
					mod1Button.addActionListener(this);

					mod2Button.setActionCommand("right");
					mod2Button.setModule(module);
					mod2Button.setConflictType(mod1IsAdd ? ConflictResolutionRadioButton.REMOVE : ConflictResolutionRadioButton.ADD);
					mod2Button.setConflictTarget(conflictFile);
					mod2Button.setSourcePath(mod1IsAdd ? null
							: mod2.findTargetSourceFileFromJob(false, mod2.getJobByModuleName(module), conflictFile));
					mod2Button.addActionListener(this);

					bg.add(mod1Button);
					bg.add(mod2Button);
					if (buttonGroups.containsKey(module)) {
						buttonGroups.get(module).add(bg);
					} else {
						ArrayList<ButtonGroup> moduleGroup = new ArrayList<ButtonGroup>();
						moduleGroup.add(bg);
						buttonGroups.put(module, moduleGroup);
					}
					//singleConflictPanel.add(fileLabel, BorderLayout.NORTH);
					singleConflictPanel.add(mod1Button, BorderLayout.WEST);
					singleConflictPanel.add(mod2Button, BorderLayout.EAST);
					moduleConflictPanel.add(singleConflictPanel);
				}
				conflictPanel.add(moduleConflictPanel);
			}
		}

		//REPLACEREMOVECONFLICTS
		if (conflictReplaceRemoveFiles != null) {
			Iterator<Map.Entry<String, ArrayList<String>>> addIterator = conflictReplaceRemoveFiles.entrySet().iterator();
			while (addIterator.hasNext()) {
				Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>) addIterator.next();
				String module = pairs.getKey();
				JPanel moduleConflictPanel = new JPanel();
				if (modulePanelMap.get(module) != null) {
					moduleConflictPanel = modulePanelMap.get(module);
				} else {
					modulePanelMap.put(module, moduleConflictPanel); //won't have conflicts as this is the first
					moduleConflictPanel.setLayout(new BoxLayout(moduleConflictPanel, BoxLayout.PAGE_AXIS));
					TitledBorder moduleBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Conflicts in " + module);
					moduleConflictPanel.setBorder(moduleBorder);
				}
				for (String conflictFile : pairs.getValue()) {
					JPanel singleConflictPanel = new JPanel(new BorderLayout());
					TitledBorder conflictBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							"Replaced/Removed by mods: " + FilenameUtils.getName(conflictFile));
					conflictBorder.setTitleJustification(TitledBorder.CENTER);
					singleConflictPanel.setBorder(conflictBorder);
					ButtonGroup bg = new ButtonGroup();

					//Determine if mod1 adds or removes this file
					boolean mod1IsReplace = false;
					for (ModJob job : mod1.getJobs()) {
						ArrayList<String> replaceTargets = job.getFilesToReplaceTargets();
						for (String replaceTarget : replaceTargets) {
							if (replaceTarget.equals(conflictFile)) {
								mod1IsReplace = true;
								break;
							}
						}
						if (mod1IsReplace) {
							break;
						}
					}

					ConflictResolutionRadioButton mod1Button = new ConflictResolutionRadioButton((mod1IsReplace ? "Replace file " : "Remove file ")
							+ mod1.getModName());
					mod1Button.setActionCommand("left");
					mod1Button.setModule(module);
					mod1Button.setConflictType(mod1IsReplace ? ConflictResolutionRadioButton.REPLACE : ConflictResolutionRadioButton.REMOVE);
					mod1Button.setConflictTarget(conflictFile);
					mod1Button.setSourcePath(mod1IsReplace ? mod1.findTargetSourceFileFromJob(false, mod1.getJobByModuleName(module), conflictFile)
							: null);
					mod1Button.addActionListener(this);

					ConflictResolutionRadioButton mod2Button = new ConflictResolutionRadioButton((mod1IsReplace ? "Remove file " : "Replace file ")
							+ mod2.getModName());
					mod2Button.setActionCommand("right");
					mod2Button.setModule(module);
					mod2Button.setConflictType(mod1IsReplace ? ConflictResolutionRadioButton.REMOVE : ConflictResolutionRadioButton.REPLACE);
					mod2Button.setConflictTarget(conflictFile);
					mod2Button.setSourcePath(mod1IsReplace ? null : mod2.findTargetSourceFileFromJob(false, mod2.getJobByModuleName(module),
							conflictFile));
					mod2Button.addActionListener(this);

					bg.add(mod1Button);
					bg.add(mod2Button);
					if (buttonGroups.containsKey(module)) {
						buttonGroups.get(module).add(bg);
					} else {
						ArrayList<ButtonGroup> moduleGroup = new ArrayList<ButtonGroup>();
						moduleGroup.add(bg);
						buttonGroups.put(module, moduleGroup);
					}
					//singleConflictPanel.add(fileLabel, BorderLayout.NORTH);
					singleConflictPanel.add(mod1Button, BorderLayout.WEST);
					singleConflictPanel.add(mod2Button, BorderLayout.EAST);
					moduleConflictPanel.add(singleConflictPanel);
				}
				conflictPanel.add(moduleConflictPanel);
			}
		}

		for (Map.Entry<String, JPanel> entry : modulePanelMap.entrySet()) {
			String key = entry.getKey();
			JPanel value = entry.getValue();
			conflictPanel.add(value);
		}

		JPanel bottomPanel = new JPanel(new BorderLayout());
		mergeButton = new JButton("Merge Mods");
		mergeButton.addActionListener(this);
		mergeButton.setEnabled(false);
		bottomPanel.add(mergeButton);
		contentPanel.add(topPanel, BorderLayout.NORTH);
		contentPanel.add(conflictPanel, BorderLayout.CENTER);
		contentPanel.add(bottomPanel, BorderLayout.SOUTH);

		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		add(listScroller);
		pack();
		setLocationRelativeTo(callingWindow);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("left") || e.getActionCommand().equals("right")) {
			//radiobutton click
			if (canSubmit()) {
				mergeButton.setEnabled(true);
			}
			return;
		}
		if (e.getSource() == mergeButton) {
			resolveConflicts();
			return;
		}
		if (e.getSource() == favorLeft) {
			favorAll(false);
			return;
		}
		if (e.getSource() == favorRight) {
			favorAll(true);
		}
	}

	private boolean canSubmit() {
		for (Map.Entry<String, ArrayList<ButtonGroup>> entry : buttonGroups.entrySet()) {
			ArrayList<ButtonGroup> groups = entry.getValue();
			for (int i = 0; i < groups.size(); i++) { //for all groups
				ButtonGroup bg = groups.get(i);
				Enumeration<AbstractButton> enumer = bg.getElements();
				int X = MergeModWindow.LEFT;
				while (enumer.hasMoreElements()) { //for buttons in the group
					ConflictResolutionRadioButton button = (ConflictResolutionRadioButton) enumer.nextElement();
					if (button.isSelected()) {
						break;
					}
					if (X == MergeModWindow.LEFT) {
						X = MergeModWindow.RIGHT; //
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void favorAll(boolean right) {
		for (Map.Entry<String, ArrayList<ButtonGroup>> entry : buttonGroups.entrySet()) {
			String key = entry.getKey();
			ArrayList<ButtonGroup> groups = entry.getValue();
			for (int i = 0; i < groups.size(); i++) { //for all groups
				ButtonGroup bg = groups.get(i);
				Enumeration<AbstractButton> enumer = bg.getElements();
				boolean isFirst = true;
				while (enumer.hasMoreElements()) { //for buttons in the group
					ConflictResolutionRadioButton button = (ConflictResolutionRadioButton) enumer.nextElement();
					if (!right) {
						button.setSelected(true);
						break;
					} else if (isFirst) {
						isFirst = false;
						continue;
					} else {
						button.setSelected(true);
						break;
					}
				}
			}
		}
		if (canSubmit()) {
			mergeButton.setEnabled(true);
		}
	}

	/**
	 * Merges the mod using the chosen conflict resolution options.
	 * 
	 * 1. Merges mod from left into right with only non-conflict files being
	 * added to mod1. 2. For conflict files, reads if the right side radiobutton
	 * is checked. (If it's the left one, then it was already in mod1 and
	 * nothing is changed) 3. Saves new mod to new folder
	 */
	private void resolveConflicts() {
		String newName = (String) JOptionPane
				.showInputDialog(
						this,
						"<html>Enter a new name for this mod. The new mod's files will be placed in this folder.<br>If a mod folder with this name exists it will be deleted first.</html>",
						"Merged Mod Name", JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (newName != null && !newName.equals("")) {
			newName = newName.trim();
			ModManager.debugLogger.writeMessage("===Start of conflict resolution==");

			//HashMap<String, ModFile> resolvedFiles = new HashMap<String, ModFile>();
			Mod merged = mod1.mergeWith(mod2, newName);
			for (Map.Entry<String, ArrayList<ButtonGroup>> entry : buttonGroups.entrySet()) {
				String key = entry.getKey();
				ArrayList<ButtonGroup> groups = entry.getValue();
				for (int i = 0; i < groups.size(); i++) { //for all groups
					ButtonGroup bg = groups.get(i);
					Enumeration<AbstractButton> enumer = bg.getElements();
					boolean parsingIsLeftButton = true;
					//int X = MergeModWindow.LEFT;
					boolean resolved = false;
					while (enumer.hasMoreElements()) { //for buttons in the group
						ConflictResolutionRadioButton button = (ConflictResolutionRadioButton) enumer.nextElement();
						if (parsingIsLeftButton) {
							if (button.isSelected()) {
								resolved = true;
								break;
							} else {
								parsingIsLeftButton = false;
							}
						} else {
							//if (X == MergeModWindow.RIGHT && button.isSelected()) {
							for (ModJob job : merged.jobs) { //merging into mod1
								if (job.getJobName().equals(key)) {
									//figure out what conflict it is

									//Replace Conflicts
									if (conflictReplaceFiles != null) {
										ArrayList<String> conflictingFilesInModule = conflictReplaceFiles.get(job.getJobName());
										for (String conflictFile : conflictingFilesInModule) {
											int updateIndex = -1;
											//for every conflict file...
											for (int x = 0; x < job.getFilesToReplaceTargets().size(); x++) {
												//get index so we can update the newFiles that correspodn to it.
												if (job.getFilesToReplaceTargets().get(x).equals(conflictFile)) {
													updateIndex = x;
													break;
												}
											}

											//got the index for mod 1.
											//get the index for mod 2 so we can look up new path
											String conflictFilePath = null;
											for (ModJob mod2job : mod2.jobs) { //find job in mod2
												if (mod2job.getJobName().equals(key)) {
													String newSourceFile = mod2.findTargetSourceFileFromJob(true, mod2job, conflictFile);
													if (newSourceFile == null) {
														ModManager.debugLogger
																.writeError("FIND TARGET SOURCE FILE FROM JOB RETURNED NULL, IMPENDING CRASH! Via ResolveConflicts REPLACE");
													} else {
														conflictFilePath = newSourceFile;
														break;
													}
												}
											}
											//got new path, now to update it...
											job.getFilesToReplace().set(updateIndex, conflictFilePath);
											resolved = true;
										}
									}

									if (resolved) {
										break;
									}
									//Add Conflicts
									if (conflictAddFiles != null) {
										ArrayList<String> conflictingFilesInModule = conflictAddFiles.get(job.getJobName());
										for (String conflictFile : conflictingFilesInModule) {
											int updateIndex = -1;
											//for every conflict file...
											for (int x = 0; x < job.getFilesToAddTargets().size(); x++) {
												//get index so we can update the newFiles that correspodn to it.
												if (job.getFilesToAddTargets().get(x).equals(conflictFile)) {
													updateIndex = x;
													break;
												}
											}

											//got the index for mod 1.
											//get the index for mod 2 so we can look up new path
											String conflictFilePath = null;
											for (ModJob mod2job : mod2.jobs) { //find job in mod2
												if (mod2job.getJobName().equals(key)) {
													String newSourceFile = mod2.findTargetSourceFileFromJob(true, mod2job, conflictFile);
													if (newSourceFile == null) {
														ModManager.debugLogger
																.writeError("FIND TARGET SOURCE FILE FROM JOB RETURNED NULL, IMPENDING CRASH! Via ResolveConflicts REPLACE");
													} else {
														conflictFilePath = newSourceFile;
														break;
													}
												}
											}
											System.out.println("MOD 2 FILE: " + conflictFilePath);

											//got new path, now to update it...
											job.getFilesToAdd().set(updateIndex, conflictFilePath);
											resolved = true;
										}
									}
									if (resolved) {
										break;
									}
									//ADD/REMOVE Conflicts
									//Remove from one list.
									if (conflictAddRemoveFiles != null) {
										String conflictingFile = button.getConflictTarget();
										String addFileSource = button.getSourcePath();
										int selectedButtonOperation = button.getConflictType();
										ArrayList<String> listToRemoveFrom = null;
										ArrayList<String> listTargetsToRemoveFrom = null;
										ArrayList<String> listToAddTo = null;
										ArrayList<String> listTargetsToAddTo = null;

										switch (selectedButtonOperation) {
										case ConflictResolutionRadioButton.ADD:
											//remove remove operation from left (existing). Add new add operation from right.
											ModManager.debugLogger.writeMessage("Removing [REMOVE] operation from mod 1 for file conflict: "
													+ conflictingFile);
											listTargetsToRemoveFrom = job.getFilesToRemoveTargets(); //if we want to add we should delete other jobs item
											listToAddTo = job.getFilesToAdd();
											listTargetsToAddTo = job.getFilesToAddTargets();
											break;
										case ConflictResolutionRadioButton.REMOVE:
											//Remove ADD operation from left, and add remove operation from right.
											ModManager.debugLogger.writeMessage("Removing [ADD] operation from mod 1 for file conflict: "
													+ conflictingFile);
											listToRemoveFrom = job.getFilesToAdd();
											listTargetsToRemoveFrom = job.getFilesToAddTargets(); //if we want to add we should delete other jobs item
											listTargetsToAddTo = job.getFilesToRemoveTargets();
											break;
										default:
											ModManager.debugLogger.writeError("Unknown button operation constant: " + selectedButtonOperation);
										}
										
										//CODE HERE
										if (!resolveConflictWithChoices(conflictingFile,addFileSource, listToRemoveFrom,listTargetsToRemoveFrom,listToAddTo,listTargetsToAddTo)){
											ModManager.debugLogger.writeError("Could not resolve conflict for file: "
													+ conflictingFile);
										} else {
											resolved = true;
										}
									}
									if (resolved) {
										break;
									}

									//REPLACE/REMOVE CONFLICTS
									if (conflictReplaceRemoveFiles != null) {
										String conflictingFile = button.getConflictTarget();
										int selectedButtonOperation = button.getConflictType();
										String replaceFileSource = button.getSourcePath();
										ArrayList<String> listToRemoveFrom = null;
										ArrayList<String> listTargetsToRemoveFrom = null;
										ArrayList<String> listToAddTo = null;
										ArrayList<String> listTargetsToAddTo = null;
										
										switch (selectedButtonOperation) {
										case ConflictResolutionRadioButton.REPLACE:
											//remove remove operation from left (existing). Add new add operation from right.
											ModManager.debugLogger.writeMessage("Removing [REMOVE] operation from mod 1 for file conflict: "
													+ conflictingFile);
											listTargetsToRemoveFrom = job.getFilesToRemoveTargets(); //if we want to add we should delete other jobs item
											listToAddTo = job.getFilesToAdd();
											listTargetsToAddTo = job.getFilesToAddTargets();
											break;
										case ConflictResolutionRadioButton.REMOVE:
											//Remove ADD operation from left, and add remove operation from right.
											ModManager.debugLogger.writeMessage("Removing [ADD] operation from mod 1 for file conflict: "
													+ conflictingFile);
											listToRemoveFrom = job.getFilesToAdd();
											listTargetsToRemoveFrom = job.getFilesToAddTargets(); //if we want to add we should delete other jobs item
											listTargetsToAddTo = job.getFilesToRemoveTargets();
											break;
										default:
											ModManager.debugLogger.writeError("Unknown button operation constant: " + selectedButtonOperation);
										}
										
										//CODE HERE
										if (!resolveConflictWithChoices(conflictingFile,replaceFileSource, listToRemoveFrom,listTargetsToRemoveFrom,listToAddTo,listTargetsToAddTo)){
											ModManager.debugLogger.writeError("Could not resolve conflict for file: "
													+ conflictingFile);
										} else {
											resolved = true;
										}
									}

									if (resolved) {
										break;
									}
								}
							}
							if (!resolved) {
								ModManager.debugLogger.writeError("Unable to resolve conflicts in mod.");
							}
						}
					}
				}
			}
			ModManager.debugLogger.writeMessage("===End of conflict resolution==");

			//create new mod
			Mod newMod = merged.createNewMod(mod2);
			if (newMod.isValidMod()) {
				JOptionPane.showMessageDialog(this, "<html>Merge successful.<br>Mod Manager will now reload mods.</html>", "Mods merged",
						JOptionPane.INFORMATION_MESSAGE);
				dispose();
				callingWindow.dispose();
			} else {
				JOptionPane
						.showMessageDialog(
								this,
								"<html>Merge was unsuccessful.<br>The produced mod is not valid.<br>Check the Mod Manager log or contact femshep (with both mods)<br>for assistance.</html>",
								"Mods not merged", JOptionPane.ERROR_MESSAGE);
				dispose();
				callingWindow.dispose();
			}
			dispose();
			callingWindow.dispose();
			new ModManagerWindow(false);
		}
	}

	private boolean resolveConflictWithChoices(String conflictingTargetFile, String addFileSource, ArrayList<String> listToRemoveFrom,
			ArrayList<String> listTargetsToRemoveFrom, ArrayList<String> listToAddTo, ArrayList<String> listTargetsToAddTo) {
		String conflictingSourceFilename = FilenameUtils.getName(conflictingTargetFile);
		boolean result = true;
		boolean oneOpRan = false;
		
		if (listToRemoveFrom != null) {
			boolean resolved = false;
			for (int i = 0; i < listToRemoveFrom.size(); i++) {
				String listFilename = FilenameUtils.getName(listToRemoveFrom.get(i));
				if (listFilename.equals(conflictingSourceFilename)){
					listToRemoveFrom.remove(i);
					resolved = true;
					break;
				}
			}
			result = result && resolved;
			oneOpRan = true;
		}
		if (listTargetsToRemoveFrom != null) {
			boolean resolved = false;
			for (int i = 0; i < listTargetsToRemoveFrom.size(); i++) {
				String listFilename = listTargetsToRemoveFrom.get(i);
				if (listFilename.equals(conflictingTargetFile)){
					listTargetsToRemoveFrom.remove(i);
					resolved = true;
					break;
				}
			}
			result = result && resolved;
			oneOpRan = true;
		}
		
		if (listToAddTo != null) {
			listToAddTo.add(conflictingSourceFilename);
			oneOpRan = true;
		}
		
		if (listTargetsToAddTo != null) {
			listTargetsToAddTo.add(conflictingTargetFile);
			oneOpRan = true;
		}
		if (!oneOpRan) {
			ModManager.debugLogger.writeError("Conflict was not resolved, no operations ran.");
		}
		return result && oneOpRan;
	}
}
