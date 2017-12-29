package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.ui.ModCellRenderer;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

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
	private JButton restoreButton;
	private JButton websiteButton;
	private JButton deleteButton;

	public FailedModsWindow() {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Loading failed mods window");
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Invalid Mods");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(600, 480));
		setIconImages(ModManager.ICONS);

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

		JPanel leftPanel = new JPanel(new BorderLayout());
		JScrollPane leftPane = new JScrollPane(failedModList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftPanel.add(leftPane, BorderLayout.CENTER);
		TitledBorder invalidBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Invalid Mods");
		leftPanel.setBorder(invalidBorder);

		lrSplitPane.setLeftComponent(leftPanel);

		failedModDesc = new JTextArea("Select a mod on the left to see why it failed to load.");

		failedModDesc.setLineWrap(true);
		failedModDesc.setWrapStyleWord(true);
		failedModDesc.setEditable(false);

		JPanel rightPanel = new JPanel(new BorderLayout());
		JScrollPane rightPane = new JScrollPane(failedModDesc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightPanel.add(rightPane, BorderLayout.CENTER);
		TitledBorder failureBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Failure reason");
		rightPanel.setBorder(failureBorder);

		lrSplitPane.setRightComponent(rightPanel);

		c.weighty = 1;
		c.weightx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		contentPanel.add(lrSplitPane, c);

		// statusbar
		// ButtonPanel
		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.LINE_AXIS));
		websiteButton = new JButton("Visit Mod Website");
		restoreButton = new JButton("Restore online");
		deleteButton = new JButton("Delete Mod");
		websiteButton.setEnabled(false);
		restoreButton.setEnabled(false);
		deleteButton.setEnabled(false);

		websiteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ResourceUtils.openWebpage(new URL(failedModsModel.get(failedModList.getSelectedIndex()).getModSite()));
				} catch (MalformedURLException e) {
					ModManager.debugLogger.writeErrorWithException("Malformed URL:", e);
					JOptionPane.showMessageDialog(FailedModsWindow.this,
							"The listed website is an invalid URL:\n" + failedModsModel.get(failedModList.getSelectedIndex()).getModSite(), "Invalid URL",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		restoreButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int modelIndex = failedModList.getSelectedIndex();
				if (modelIndex > -1) {
					Mod mod = failedModsModel.get(modelIndex);
					mod = new Mod(mod); //make clone
					if (mod.getModMakerCode() <= 0 || ModManagerWindow.validateBIOGameDir()) {
						if (mod.getModMakerCode() <= 0 || ModManager.validateNETFrameworkIsInstalled()) {
							ModManager.debugLogger.writeMessage("Running (restore mode) for failed mod " + mod.getModName());
							mod.setVersion(0.001);
							ModManagerWindow.ACTIVE_WINDOW.startSingleModUpdate(mod);
							dispose();
						} else {
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(".NET Framework 4.5 or higher is missing");
							ModManager.debugLogger.writeMessage("Fail Mod Restore: Missing .NET Framework");
							new NetFrameworkMissingWindow("You must install .NET Framework 4.5 to restore ModMaker mods.");
						}
					} else {
						ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Restoring ModMaker mods requires valid BIOGame");
						JOptionPane.showMessageDialog(FailedModsWindow.this,
								"The BIOGame directory is not valid.\nCannot restore ModMaker mods without a valid BIOGame directory.\nFix the BIOGame directory before continuing.",
								"Invalid BioGame Directory", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int modelIndex = failedModList.getSelectedIndex();
				if (modelIndex > -1) {
					Mod mod = failedModsModel.get(modelIndex);
					mod = new Mod(mod); //make clone
					String modfolder = mod.getModPath();
					int result = JOptionPane.showConfirmDialog(FailedModsWindow.this,
							"Delete " + mod.getModName() + " from Mod Manager?\nThis will not remove the mod if it is installed.", "Confirm mod deletion",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						ModManager.debugLogger.writeMessage("Deleting invalid mod folder at user request: " + modfolder);
						boolean deleted = FileUtils.deleteQuietly(new File(modfolder));
						if (deleted) {
							ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
							failedModsModel.remove(modelIndex);
							if (failedModsModel.isEmpty()) {
								ModManager.debugLogger.writeMessage("No invalid mods remaining - closing Invalid Mods window.");
								dispose();
							}
						}
					}
				}
			}

		});

		actionsPanel.add(Box.createHorizontalGlue());
		actionsPanel.add(websiteButton);
		actionsPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		actionsPanel.add(restoreButton);
		actionsPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		actionsPanel.add(deleteButton);
		c.gridy++;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 0, 0);
		contentPanel.add(actionsPanel, c);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		add(contentPanel);

		pack();
		lrSplitPane.setDividerLocation(150 + lrSplitPane.getInsets().top);
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if (failedModList.getSelectedIndex() == -1) {
				failedModDesc.setText("Select a mod on the left to see why it failed to load.");
				websiteButton.setEnabled(false);
				restoreButton.setEnabled(false);
				deleteButton.setEnabled(false);
				deleteButton.setToolTipText("Select a mod on the left");
			} else {
				updateDescription();
			}
		} else {
			updateDescription();
		}
	}

	private void updateDescription() {
		String description = "";
		Mod mod = failedModsModel.get(failedModList.getSelectedIndex());
		description += mod.getModName();
		description += "\n" + mod.getModPath();
		description += "\n\n";
		if (mod.getFailedReason() == null) {
			description += "Mod has no failed reason set or it encountered a null pointer exception while loading.";
		} else {
			description += mod.getFailedReason();
		}
		if (mod.getModSite() != null && !mod.getModSite().equals("")) {
			description += "\n\nThe mod lists a website: " + mod.getModSite()
					+ ". Click the Visit Website button to go to it, you may be able to find additional assistance there.";
			websiteButton.setEnabled(true);
			websiteButton.setToolTipText(mod.getModSite());
		} else {
			websiteButton.setEnabled(false);
			websiteButton.setToolTipText("This mod does not list a website for end-users to go to for support");
		}
		if (mod.isME3TweaksUpdatable()) {
			description += "\n\nThis mod can attempt a restore via the ME3Tweaks updater service. Select Restore Online to force this mod to perform an update and match the version on the server.\nIf the issue is from additional files, the service won't be able to fix it.";
			restoreButton.setEnabled(true);
			restoreButton.setToolTipText("Attempt online recovery of this mod by forcing an update");
		} else {
			restoreButton.setEnabled(false);
			restoreButton.setToolTipText("This mod cannot attempt a current restore in its current state");
		}
		deleteButton.setEnabled(true);
		deleteButton.setToolTipText("Delete this mod from Mod Manager's library");
		failedModDesc.setText(description);
		failedModDesc.setCaretPosition(0);
	}
}
