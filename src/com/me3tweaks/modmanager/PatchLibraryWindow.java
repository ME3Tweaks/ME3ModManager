package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.me3tweaks.modmanager.cellrenderers.ModCellRenderer;
import com.me3tweaks.modmanager.cellrenderers.PatchCellRenderer;
import com.me3tweaks.modmanager.objects.ME3TweaksPatchPackage;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.ThreadCommand;

/**
 * Patch Window shows the list of patches in the patch library and things you
 * can do with it
 * 
 * @author Mgamerz
 *
 */
public class PatchLibraryWindow extends JDialog implements ListSelectionListener, ActionListener {
	DefaultListModel<Patch> patchModel;
	private JList<Patch> patchList;
	private JTextArea patchDesc;
	private JButton buttonApplyPatch;
	private JComboBox<Mod> modComboBox;
	DefaultComboBoxModel<Mod> modModel;
	private ArrayList<Integer> automated_requiredMixinIds;
	private Mod automated_mod;
	private boolean downloaded;

	public PatchLibraryWindow() {
		ModManager.debugLogger.writeMessage("Loading patch library interface");
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		new ME3TweaksLibraryUpdater(ModManagerWindow.ACTIVE_WINDOW.getPatchList(), false).execute();
		setVisible(true);
	}

	/**
	 * Starts PatchLibraryWindow in automatic mode. The IDs specified are
	 * checked for being in the local library. If they aren't in the library, it
	 * attempts to download them from me3tweaks.com's library.
	 * 
	 * @param mixinIds
	 */
	public PatchLibraryWindow(JDialog callingDialog, ArrayList<Integer> mixinIds, Mod mod) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		this.automated_requiredMixinIds = mixinIds;
		this.automated_mod = mod;
		ModManager.debugLogger.writeMessage("Loading patch library in automated mode");
		boolean hasMissingMixIn = false;
		for (int requiredID : mixinIds) {
			boolean foundId = false;
			for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
				if (patch.getMe3tweaksid() == requiredID) {
					foundId = true;
					break;
				}
			}
			if (!foundId) {
				hasMissingMixIn = true;
				break;
			}
		}

		if (hasMissingMixIn) {
			new ME3TweaksLibraryUpdater(ModManagerWindow.ACTIVE_WINDOW.getPatchList(), true).execute();
			setupAutomatedWindow(callingDialog);
			setVisible(true);
		} else {
			advertiseInstalls(automated_requiredMixinIds, automated_mod);
			dispose();
		}
	}

	private void setupAutomatedWindow(JDialog callingDialog) {
		this.setTitle("MixIn Library");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(250, 70));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel(new BorderLayout());

		JLabel operationLabel = new JLabel("Getting latest MixIns from ME3Tweaks");

		panel.add(operationLabel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);
		pack();
		this.setLocationRelativeTo(callingDialog);
	}

	private void advertiseInstalls(ArrayList<Integer> mixinIds, Mod mod) {
		//Build patch array, build prompt text
		String str = "This mod wants to apply the following MixIns from ME3Tweaks:";
		ArrayList<Patch> patches = new ArrayList<Patch>();
		for (int mixinid : mixinIds) {
			for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
				if (mixinid == patch.getMe3tweaksid()) {
					str += "\n - " + patch.getPatchName();
					patches.add(patch);
					break;
				}
			}
		}
		str += "\n\nApply these MixIns to the mod?";
		//show prompt
		ModManager.debugLogger.writeMessage("Prompting user for install of mixins");

		int response = JOptionPane.NO_OPTION;
		if (!ModManager.AUTO_APPLY_MODMAKER_MIXINS) {
			response = JOptionPane.showConfirmDialog(null, str, "Recommended MixIns", JOptionPane.YES_NO_OPTION);
			
		} else {
			response = JOptionPane.YES_OPTION;
			ModManager.debugLogger.writeMessage("User has auto install mixins option set.");
		}
		if (response == JOptionPane.YES_OPTION) {
			ModManager.debugLogger.writeMessage("User has accepted mixins");
			//Apply
			ModManager.debugLogger.writeMessage("Applying patches in automated mode");
			if (!mod.isValidMod()) {
				return; // this shouldn't be reachable anyways
			}
			PatchApplicationWindow paw = new PatchApplicationWindow(this, patches, mod);
			ArrayList<Patch> failedpatches = paw.getFailedPatches();
			if (failedpatches.size() > 0) {
				String rstr = "The following MixIns failed to apply:\n";
				for (Patch p : failedpatches) {
					rstr += " - " + p.getPatchName() + "\n";
				}
				rstr += "Check the debugging log to see why.";
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, rstr, "MixIns Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("MixIn Library");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(600, 480));
		this.setIconImages(ModManager.ICONS);

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
			//System.out.println(patch.convertToME3TweaksSQLInsert());
		}
		lrSplitPane.setLeftComponent(new JScrollPane(patchList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		patchDesc = new JTextArea("Select one or more mixins (ctrl+click) on the left to see their descriptions.");

		patchDesc.setLineWrap(true);
		patchDesc.setWrapStyleWord(true);
		patchDesc.setEditable(false);
		lrSplitPane.setRightComponent(new JScrollPane(patchDesc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		contentPanel.add(lrSplitPane, c);

		// statusbar
		// ButtonPanel
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints bc = new GridBagConstraints();

		modModel = new DefaultComboBoxModel<Mod>();
		modComboBox = new JComboBox<Mod>();
		modComboBox.setModel(modModel);
		modComboBox.setRenderer(new ModCellRenderer());

		Mod mod = new Mod(null); // invalid
		mod.setModName("Select a mod to add mixins to");

		modModel.addElement(mod);
		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.getSize(); i++) {
			Mod loadedMod = ModManagerWindow.ACTIVE_WINDOW.modModel.get(i);
			modModel.addElement(loadedMod);
		}

		buttonApplyPatch = new JButton("Apply MixIns");
		buttonApplyPatch.addActionListener(this);
		buttonApplyPatch.setEnabled(false);
		buttonApplyPatch.setToolTipText("Applies the selected MixIns to a mod");

		// buttonStartGame = new JButton("Start Game");
		// buttonStartGame.addActionListener(this);
		// buttonStartGame.setToolTipText("Starts the game. If LauncherWV DLC
		// bypass is installed, it will that to launch the game instead");
		bc.anchor = GridBagConstraints.WEST;
		bc.weightx = 1;
		buttonPanel.add(modComboBox, bc);

		bc.anchor = GridBagConstraints.EAST;
		// bc.fill = GridBagConstraints.HORIZONTAL;
		bc.weightx = 0;
		buttonPanel.add(buttonApplyPatch, bc);
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		// buttonPanel.add(buttonStartGame);
		c.gridy = 10;
		c.weighty = 0;
		contentPanel.add(buttonPanel, c);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPanel);

		// listeners
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
		System.out.println("values have changed.");
		if (e.getValueIsAdjusting() == false) {
			if (patchList.getSelectedIndex() == -1) {
				patchDesc.setText("Select one or more mixins (ctrl+click) on the left to see their descriptions.");
				buttonApplyPatch.setEnabled(false);
			} else {
				updateDescription();
			}
		} else {
			updateDescription();
		}
	}

	private void updateDescription() {
		String description = "";
		List<Patch> selectedPatches = patchList.getSelectedValuesList();
		boolean isFirst = true;
		for (Patch patch : selectedPatches) {
			System.out.println(patch.convertToME3TweaksSQLInsert());
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
			if (patch.getPatchAuthor() != null) {
				description += "Created by " + patch.getPatchAuthor() + "\n";
			}
		}
		patchDesc.setText(description);
		if (modComboBox.getSelectedIndex() > 0) {
			buttonApplyPatch.setEnabled(true);
		} else {
			buttonApplyPatch.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonApplyPatch) {
			ModManager.debugLogger.writeMessage("Applying patches");
			List<Patch> selectedPatches = patchList.getSelectedValuesList();
			Mod mod = modModel.getElementAt(modComboBox.getSelectedIndex());
			if (!mod.isValidMod()) {
				return; // this shouldn't be reachable anyways
			}
			PatchApplicationWindow paw = new PatchApplicationWindow(this, new ArrayList<Patch>(selectedPatches), mod);
			new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE);
			ArrayList<Patch> failedpatches = paw.getFailedPatches();
			if (failedpatches.size() <= 0) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "All mixins were applied.", "MixIns applied", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String str = "The following MixIns failed to apply:\n";
				for (Patch p : failedpatches) {
					str += " - " + p.getPatchName() + "\n";
				}
				str += "Check the debugging log to see why.";
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, str, "MixIns Error", JOptionPane.ERROR_MESSAGE);
			}
			dispose();
			new ModManagerWindow(false);
		}
	}

	class ME3TweaksLibraryUpdater extends SwingWorker<Void, ThreadCommand> {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc;
		boolean modmakerMode;
		boolean atLeast1New = false;
		ArrayList<Patch> patches;

		public ME3TweaksLibraryUpdater(ArrayList<Patch> patches, boolean isInModMakerMode) {
			this.modmakerMode = isInModMakerMode;
			this.patches = patches;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// Download XML from server
			ModManager.debugLogger.writeMessage("================DOWNLOADING MIXIN LIBRARY INFORMATION==============");
			String link;
			if (ModManager.IS_DEBUG) {
				link = "http://webdev-mgamerz.c9.io/mixins/libraryinfo";
			} else {
				link = "http://me3tweaks.com/mixins/libraryinfo";
			}
			ModManager.debugLogger.writeMessage("Fetching mixin info from " + link);
			try {
				String modDelta = IOUtils.toString(new URL(link));
				ModManager.debugLogger.writeMessage("Mixin listing xml downloaded to memory");
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				ModManager.debugLogger.writeMessage("Loading mixin info into memory (as DOM).");
				doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(modDelta.getBytes("utf-8")))); // http://stackoverflow.com/questions/1706493/java-net-malformedurlexception-no-protocol
				ModManager.debugLogger.writeMessage("Mixin info from server loaded into memory.");
				doc.getDocumentElement().normalize();

				// Parse XML into objects
				ArrayList<ME3TweaksPatchPackage> serverpacks = new ArrayList<ME3TweaksPatchPackage>();
				NodeList patchList = doc.getElementsByTagName("MixIn");
				for (int i = 0; i < patchList.getLength(); i++) {
					Element mixinElement = (Element) patchList.item(i);
					ME3TweaksPatchPackage pack = new ME3TweaksPatchPackage();
					pack.setPatchname(mixinElement.getElementsByTagName("patchname").item(0).getTextContent());
					pack.setPatchdesc(mixinElement.getElementsByTagName("patchdesc").item(0).getTextContent());
					pack.setPatchdev(mixinElement.getElementsByTagName("patchdev").item(0).getTextContent());
					pack.setPatchver(Double.parseDouble(mixinElement.getElementsByTagName("patchver").item(0).getTextContent()));
					pack.setTargetversion(Double.parseDouble(mixinElement.getElementsByTagName("targetversion").item(0).getTextContent()));
					pack.setTargetmodule(mixinElement.getElementsByTagName("targetmodule").item(0).getTextContent());
					pack.setTargetfile(mixinElement.getElementsByTagName("targetfile").item(0).getTextContent());
					pack.setTargetsize(Long.parseLong(mixinElement.getElementsByTagName("targetsize").item(0).getTextContent()));
					pack.setFinalizer((mixinElement.getElementsByTagName("finalizer").item(0).getTextContent().equals(1)) ? true : false);
					pack.setPatchurl(mixinElement.getElementsByTagName("patchurl").item(0).getTextContent());
					pack.setFolder(mixinElement.getElementsByTagName("folder").item(0).getTextContent());
					pack.setMe3tweaksid(Integer.parseInt(mixinElement.getElementsByTagName("me3tweaksid").item(0).getTextContent()));
					serverpacks.add(pack);
				}

				// find local packs that need updated, or are missing
				ArrayList<ME3TweaksPatchPackage> packsToDownload = new ArrayList<ME3TweaksPatchPackage>();
				ArrayList<Patch> patchesToDelete = new ArrayList<Patch>(); //will remove in like 2 builds or so so users don't have duplicates

				for (ME3TweaksPatchPackage serverpack : serverpacks) {
					boolean needsDownloaded = true;

					for (int i = 0; i < patches.size(); i++) {
						Patch localpatch = patches.get(i);
						if (localpatch.getMe3tweaksid() <= 0) {
							patchesToDelete.add(localpatch);
							continue;
						}

						if (localpatch.getMe3tweaksid() != serverpack.getMe3tweaksid()) {
							continue;
						}
						/*
						 * if (!localpatch.getPatchName().equals(serverpack.
						 * getPatchname())){ continue; }
						 * 
						 * //same name if
						 * (!localpatch.getTargetModule().equals(serverpack.
						 * getTargetmodule())){ continue; }
						 * 
						 * //same module if
						 * (!localpatch.getTargetPath().equals(serverpack.
						 * getTargetfile())){ continue; }
						 */
						// same target

						// same name, same module, same target, likely the same
						// - just check if version is newer.

						if (localpatch.getPatchVersion() < serverpack.getPatchver()) {
							continue;
						}

						// patch does not need downloaded.
						needsDownloaded = false;
						ModManager.debugLogger.writeMessage("Local MixIn " + localpatch.getPatchName() + " is up to date.");
						break;
					}
					if (needsDownloaded) {
						ModManager.debugLogger.writeMessage("Server MixIn " + serverpack.getPatchname() + " is not present locally (or out of date), adding to download queue");
						packsToDownload.add(serverpack);
					}
				}

				//delete old packs from build 40/41/42
				for (Patch localPatch : patchesToDelete) {
					File patchFolder = new File(localPatch.getPatchFolderPath());
					FileUtils.deleteQuietly(patchFolder);
					publish(new ThreadCommand("REMOVE_PATCH", null, localPatch));
				}

				// download new packs
				for (ME3TweaksPatchPackage pack : packsToDownload) {
					atLeast1New = true;
					ModManager.debugLogger.writeMessage("Downloading MixIn patch: " + pack.getPatchurl());

					// clear old directories, make new ones
					String targetFolderStr = ModManager.appendSlash(ModManager.getPatchLibraryDir() + pack.getFolder());
					ModManager.debugLogger.writeMessage("Patch directory: " + targetFolderStr);
					File targetFolder = new File(targetFolderStr);
					FileUtils.deleteDirectory(targetFolder);
					targetFolder.mkdirs();
					File jsfFile = new File(targetFolderStr + "patch.jsf");
					FileUtils.copyURLToFile(new URL(pack.getPatchurl()), jsfFile);
					String patchDesc = Patch.generatePatchDesc(pack);
					FileUtils.writeStringToFile(new File(targetFolderStr + "patchdesc.ini"), patchDesc);

					Patch p = new Patch(targetFolderStr + "patchdesc.ini");
					publish(new ThreadCommand("ADD_PATCH", null, p));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public void process(List<ThreadCommand> chunks) {

			for (ThreadCommand tc : chunks) {
				if (!modmakerMode) {
					Patch p = (Patch) tc.getData();
					if (tc.getCommand().equals("ADD_PATCH")) {
						patchModel.addElement(p);
					}
					if (tc.getCommand().equals("REMOVE_PATCH")) {
						patchModel.removeElement(p);
					}
				}
			}
		}

		public void done() {
			if (atLeast1New) {
				ModManager.debugLogger.writeMessage("Patch Library updated from ME3Tweaks, reloading patch list");
				ModManagerWindow.ACTIVE_WINDOW.setPatchList(ModManager.getPatchesFromDirectory());
			}
			//downloaded = true;
			if (modmakerMode) {
				//reload patch list
				//check requirements are met
				ArrayList<Integer> missingIds = new ArrayList<Integer>();
				for (int requiredID : automated_requiredMixinIds) {
					boolean foundId = false;
					for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
						if (patch.getMe3tweaksid() == requiredID) {
							foundId = true;
							break;
						}
					}
					if (!foundId) {
						missingIds.add(requiredID);
					}
				}

				if (missingIds.size() > 0) {
					String errorStr = "This mod requests the additional MixIn IDs that don't exist on ME3Tweaks:\n";
					for (int miss : missingIds) {
						errorStr += miss + " ";
						automated_requiredMixinIds.remove(new Integer(miss));
					}
					errorStr += "\nThese MixIns can't be added to this mod.";
					JOptionPane.showMessageDialog(null, errorStr, "Missing MixIns", JOptionPane.WARNING_MESSAGE);
				}
				dispose();
				advertiseInstalls(automated_requiredMixinIds, automated_mod);
			}
		}

	}

}
