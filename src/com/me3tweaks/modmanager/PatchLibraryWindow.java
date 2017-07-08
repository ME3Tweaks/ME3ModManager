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
import java.io.IOException;
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

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.modmaker.DynamicPatch;
import com.me3tweaks.modmanager.objects.ME3TweaksPatchPackage;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.ModCellRenderer;
import com.me3tweaks.modmanager.ui.PatchCellRenderer;

/**
 * Patch Window shows the list of patches in the patch library and things you
 * can do with it
 *
 * @author Mgamerz
 *
 */
public class PatchLibraryWindow extends JDialog implements ListSelectionListener, ActionListener {
	public static final int AUTOUPDATE_MODE = 2;
	public static final int MODMAKER_MODE = 1;
	public static final int MANUAL_MODE = 0;
	DefaultListModel<Patch> patchModel;
	private JList<Patch> patchList;
	private JTextArea patchDesc;
	private JButton buttonApplyPatch;
	private JComboBox<Mod> modComboBox;
	DefaultComboBoxModel<Mod> modModel;
	private ArrayList<Integer> automated_requiredMixinIds;
	private Mod automated_mod;
	private boolean downloaded;
	private ArrayList<DynamicPatch> dynamicMixIns;
	private final Object lock = new Lock(); //threading wait() and notifyall();
	private int numberofupdatedmixins = -1;
	private Mod newmod;

	public PatchLibraryWindow(int mode) {
		if (mode == MANUAL_MODE) {
			ModManager.debugLogger.writeMessage("Loading mixin library interface");
			setupWindow();
			new ME3TweaksLibraryUpdater(null, ModManagerWindow.ACTIVE_WINDOW.getPatchList(), PatchLibraryWindow.MANUAL_MODE).execute();
			setVisible(true);
		} else if (mode == AUTOUPDATE_MODE) {
			synchronized (lock) {
				while (numberofupdatedmixins == -1) {
					try {
						ModManager.debugLogger.writeMessage("Loading mixin library in automated mode, waiting for it to finish");
						new ME3TweaksLibraryUpdater(null, ModManagerWindow.ACTIVE_WINDOW.getPatchList(), PatchLibraryWindow.AUTOUPDATE_MODE).execute();
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						ModManager.debugLogger.writeErrorWithException("Unable to wait for MixIns update lock:", e);
					}
				}
			}

		}
	}

	/**
	 * Starts PatchLibraryWindow in automatic mode. The IDs specified are
	 * checked for being in the local library. If they aren't in the library, it
	 * attempts to download them from me3tweaks.com's library.
	 *
	 * @param requiredMixinIds
	 *            me3tweaks patch ids
	 */
	public PatchLibraryWindow(JDialog callingDialog, ArrayList<String> requiredMixinIds, ArrayList<DynamicPatch> dynamicMixIns, Mod mod) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		ArrayList<ModmakerMixinIdentifier> modmakerIds = new ArrayList<ModmakerMixinIdentifier>();
		ArrayList<Integer> requiredIds = new ArrayList<Integer>();

		for (String requiredID : requiredMixinIds) {
			modmakerIds.add(new ModmakerMixinIdentifier(requiredID));
		}

		ModManager.debugLogger.writeMessage("Loading mixin library in automated mode");
		boolean hasMissingMixIn = false;
		for (ModmakerMixinIdentifier modmakermixin : modmakerIds) {
			boolean foundId = false;
			for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
				if (patch.getMe3tweaksid() == modmakermixin.mixinid && patch.getPatchVersion() >= modmakermixin.minVersion) {
					foundId = true;
					break;
				}
			}
			if (!foundId) {
				hasMissingMixIn = true;
				break;
			}
		}
		for (ModmakerMixinIdentifier mixin : modmakerIds) {
			requiredIds.add(new Integer(mixin.mixinid));
		}
		this.automated_requiredMixinIds = requiredIds;
		this.automated_mod = mod;
		this.dynamicMixIns = dynamicMixIns;
		if (hasMissingMixIn) {
			new ME3TweaksLibraryUpdater(callingDialog, ModManagerWindow.ACTIVE_WINDOW.getPatchList(), PatchLibraryWindow.MODMAKER_MODE).execute();
			setupAutomatedWindow(callingDialog);
			setVisible(true);
		} else {
			advertiseInstalls(callingDialog, automated_requiredMixinIds, automated_mod);
			dispose();
		}
	}

	private void setupAutomatedWindow(JDialog callingDialog) {
		setTitle("MixIn Library");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(250, 70));
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel(new BorderLayout());

		JLabel operationLabel = new JLabel("Getting latest MixIns from ME3Tweaks");

		panel.add(operationLabel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(callingDialog);
	}

	private void advertiseInstalls(JDialog callingDialog, ArrayList<Integer> mixinIds, Mod mod) {
		//Build patch array, build prompt text
		String str = "This mod wants to apply the following MixIns from ME3Tweaks:";
		ArrayList<Patch> patchesToApply = new ArrayList<Patch>();
		for (int mixinid : mixinIds) {
			for (Patch patch : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
				if (mixinid == patch.getMe3tweaksid()) {
					str += "\n - " + patch.getPatchName();
					patchesToApply.add(patch);
					break;
				}
			}
		}

		for (DynamicPatch p : dynamicMixIns) {
			str += "\n - " + p.getFinalPatch().getPatchName();
			patchesToApply.add(p.getFinalPatch());
		}

		if (ModManager.MODMAKER_CONTROLLER_MOD_ADDINS) {
			ModManager.debugLogger.writeMessage("User has enabled controller mod addins for modmaker mods");

			Patch cameraTurningPatch = null;
			Patch vibrationPlatformCheckPatch = null;
			boolean modifiesSFXgame = false;
			boolean modifiesPatchBioPlayerController = false;

			//Get Patches
			for (Patch p : ModManagerWindow.ACTIVE_WINDOW.getPatchList()) {
				if (p.getMe3tweaksid() == 1533) {
					cameraTurningPatch = p;
					continue;
				}
				if (p.getMe3tweaksid() == 1557) {
					vibrationPlatformCheckPatch = p;
					continue;
				}
			}

			//Build List of controller patches to apply
			ArrayList<Patch> patchesToAdd = new ArrayList<>();
			for (Patch p : patchesToApply) {
				if (!modifiesSFXgame && p.getTargetPath().equals("/BIOGame/CookedPCConsole/SFXGame.pcc")) {
					patchesToAdd.add(cameraTurningPatch);
					ModManager.debugLogger.writeMessage("Added patch 1533 (CAMERA TURNING) to compilation - ModMaker Controller Addins");
					modifiesSFXgame = true;
					continue;
				}

				if (!modifiesPatchBioPlayerController && p.getTargetPath().equals("/BIOGame/DLC/DLC_TestPatch/CookedPCConsole/Patch_BioPlayerController.pcc")) {
					patchesToAdd.add(vibrationPlatformCheckPatch);
					ModManager.debugLogger.writeMessage("Added patch 1557 (VIBRATION PATCH) to compilation - ModMaker Controller Addins");
					modifiesPatchBioPlayerController = true;
					continue;
				}
			}

			if (patchesToAdd.size() == 0) {
				ModManager.debugLogger.writeMessage("No controller add-ins required as no mixins create conflicting files");
			} else {
				patchesToApply.addAll(patchesToAdd);
				ModManager.debugLogger.writeMessage("Added " + patchesToAdd.size() + " controller fixing mixins.");
			}
		}

		str += "\n\nMixIns are patches that are applied to game files that alter them, but may considerably increase the size of the mod. Mods that are designed with MixIns are typically balanced around them, so it is encouraged to apply them. Your game should be in a unmodded state during this step, so files not in the MixIn cache can be cached and correctly patched.\n\nApply these MixIns to the mod?\n\nYou can turn on automatic MixIn acceptance in the Options screen from the Actions menu.";
		//show prompt
		ModManager.debugLogger.writeMessage("Prompting user for install of mixins");

		int response = JOptionPane.NO_OPTION;
		if (!ModManager.AUTO_APPLY_MODMAKER_MIXINS) {
			JTextArea textArea = new JTextArea(str);
			JScrollPane scrollPane = new JScrollPane(textArea);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			scrollPane.setPreferredSize(new Dimension(500, 350));
			textArea.setCaretPosition(0);
			textArea.setEditable(false);
			response = JOptionPane.showConfirmDialog(null, scrollPane, "Recommended MixIns", JOptionPane.YES_NO_OPTION);

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
			PatchApplicationWindow paw = new PatchApplicationWindow(callingDialog, patchesToApply, mod);
			ArrayList<Patch> failedpatches = paw.getFailedPatches();
			if (failedpatches.size() > 0) {
				String rstr = "The following MixIns failed to apply:\n";
				for (Patch p : failedpatches) {
					rstr += " - " + p.getPatchName() + "\n";
				}
				rstr += "Check the Mod Manager log to see why.";
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, rstr, "MixIns Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void setupWindow() {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle("MixIn Library");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(600, 480));
		setIconImages(ModManager.ICONS);

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

		Mod selectamod = new Mod(); // invalid
		selectamod.setModName("Select a mod to add mixins to");

		newmod = new Mod();
		newmod.setModName("Create new mod from MixIns");

		modModel.addElement(selectamod);
		modModel.addElement(newmod);
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
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
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
			description += patch.getPatchName() + " v" + patch.getPatchVersion();
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
			Mod mod = modModel.getElementAt(modComboBox.getSelectedIndex());
			List<Patch> selectedPatches = patchList.getSelectedValuesList();
			if (mod == newmod) {
				boolean nameIsBad = true;
				String errormsg = "";
				String input = "";
				while (nameIsBad) {
					input = JOptionPane.showInputDialog(PatchLibraryWindow.this,
							"Input a name for the mod that will be created from these MixIns.\nAlphanumberic only, must be less than 20 characters." + errormsg,
							"Create a new mod from MixIns", JOptionPane.QUESTION_MESSAGE);
					if (input == null) {
						return;
					}
					input = input.trim();

					boolean asciionly = input.chars().allMatch(c -> c == 0x20 || c == 0x5F || (c > 0x30 && c < 0x3A) || (c > 0x40 && c < 0x5B) || (c > 0x60 && c < 0x7B)); //what the f is this?
					if (!asciionly) {
						ModManager.debugLogger.writeError("Name is not ascii alphanumeric only: " + input);
						errormsg = "\nMod name must be alphanumberic.";
						continue;
					}
					if (input.length() > 20 || input.length() < 1) {
						ModManager.debugLogger.writeError("Name is empty or too long: " + input);
						errormsg = "\nMod name must be less than 20 characters.";
						continue;
					}

					if (input.equals("")) {
						errormsg = "\nMod name must be at least 1 character.";
						continue;
					}

					nameIsBad = false;
				}

				ModManager.debugLogger.writeMessage("User entered mod name: " + input);

				mod = new Mod();
				String modfolder = ModManager.getModsDir() + input;
				File modfolderfile = new File(modfolder);
				modfolderfile.mkdirs();
				mod.setModPath(ModManager.getModsDir() + input);
				mod.setModName(input);
				mod.setVersion(1);
				String desc = "This mod was created from the following MixIns:<br>";
				for (Patch p : selectedPatches) {
					desc += " - " + p.getPatchName() + " v" + p.getPatchVersion() + "<br>";
				}
				mod.setModDescription(desc);
				mod.setAuthor("Mod Manager " + ModManager.VERSION);
				String descini = mod.createModDescIni(true, ModManager.MODDESC_VERSION_SUPPORT);
				ModManager.debugLogger.writeMessage("Creating blank moddesc.ini file for new mod " + input);
				try {
					FileUtils.writeStringToFile(new File(modfolder + "/moddesc.ini"), descini);
					mod = new Mod();
					mod.setEmptyModIsOK(true);
					mod.loadMod(modfolder + "/moddesc.ini");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
			}

			ModManager.debugLogger.writeMessage("Applying patches");
			if (!mod.isValidMod()) {
				return; // this shouldn't be reachable anyways
			}
			PatchApplicationWindow paw = new PatchApplicationWindow(this, new ArrayList<Patch>(selectedPatches), mod);
			new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.GetBioGameDir());
			ArrayList<Patch> failedpatches = paw.getFailedPatches();
			if (failedpatches.size() <= 0) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "All mixins were applied.", "MixIns applied", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String str = "The following MixIns failed to apply:\n";
				for (Patch p : failedpatches) {
					str += " - " + p.getPatchName() + "\n";
				}
				str += "Check the Mod Manager log to see why.";
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, str, "MixIns Error", JOptionPane.ERROR_MESSAGE);
			}
			dispose();
			ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
		}
	}

	class ME3TweaksLibraryUpdater extends SwingWorker<Void, ThreadCommand> {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc;
		boolean modmakerMode;
		boolean autoupdateMode;
		boolean atLeast1New = false;
		ArrayList<Patch> patches;
		private JDialog callingDialog;
		private int numChanged = 0;

		/**
		 *
		 * @param callingDialog
		 *            calling dialog. Can be null.
		 * @param patches
		 *            list of current patches
		 * @param mode
		 *            autoupdateMode = 1, modmakerMode = 0
		 */
		public ME3TweaksLibraryUpdater(JDialog callingDialog, ArrayList<Patch> patches, int mode) {
			this.modmakerMode = (mode == MODMAKER_MODE);
			this.autoupdateMode = (mode == AUTOUPDATE_MODE);
			//if neither are true it will default to manual mode
			this.patches = patches;
			this.callingDialog = callingDialog;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// Download XML from server
			ModManager.debugLogger.writeMessage("================DOWNLOADING MIXIN LIBRARY INFORMATION==============");
			String link = "https://me3tweaks.com/mixins/libraryinfo";

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
					pack.setFinalizer((mixinElement.getElementsByTagName("finalizer").item(0).getTextContent().equals("1")) ? true : false);
					pack.setPatchurl(mixinElement.getElementsByTagName("patchurl").item(0).getTextContent());
					pack.setFolder(mixinElement.getElementsByTagName("folder").item(0).getTextContent());
					pack.setMe3tweaksid(Integer.parseInt(mixinElement.getElementsByTagName("me3tweaksid").item(0).getTextContent()));
					serverpacks.add(pack);
				}

				// find local packs that need updated, or are missing
				ArrayList<ME3TweaksPatchPackage> packsToDownload = new ArrayList<ME3TweaksPatchPackage>();

				for (ME3TweaksPatchPackage serverpack : serverpacks) {
					boolean needsDownloaded = true;

					for (int i = 0; i < patches.size(); i++) {
						Patch localpatch = patches.get(i);

						if (localpatch.getMe3tweaksid() != serverpack.getMe3tweaksid()) {
							continue;
						}

						if (localpatch.getPatchVersion() < serverpack.getPatchver()) {
							ModManager.debugLogger.writeMessage("Local MixIn " + serverpack.getPatchname() + " is out of date, adding to download queue");
							continue;
						}

						// patch does not need downloaded.
						needsDownloaded = false;
						ModManager.debugLogger.writeMessage("Local MixIn " + localpatch.getPatchName() + " is up to date.");
						break;
					}
					if (needsDownloaded) {
						ModManager.debugLogger.writeMessage("Server MixIn " + serverpack.getPatchname() + " is not present locally, adding to download queue");
						packsToDownload.add(serverpack);
					}
				}

				// download new packs
				for (ME3TweaksPatchPackage pack : packsToDownload) {
					numChanged = packsToDownload.size();
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

					Patch p = new Patch(targetFolderStr + "patchdesc.ini", targetFolderStr + "patch.jsf");
					publish(new ThreadCommand("REMOVE_LOCAL_LIST_PATCH", null, p));
					publish(new ThreadCommand("ADD_PATCH", null, p));
				}
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Error downloading mixins:", e);
			}
			return null;
		}

		public void process(List<ThreadCommand> chunks) {

			for (ThreadCommand tc : chunks) {
				if (!modmakerMode && !autoupdateMode) {
					Patch p = (Patch) tc.getData();
					//Add new patch to list
					if (tc.getCommand().equals("ADD_PATCH")) {
						patchModel.addElement(p);
					}
					//Remove same-object patch from list
					if (tc.getCommand().equals("REMOVE_PATCH")) {
						patchModel.removeElement(p);
					}
					//Remove all existing patches with ID being updated
					if (tc.getCommand().equals("REMOVE_LOCAL_LIST_PATCH")) {
						for (int i = 0; i < patchModel.getSize(); i++) {
							if (patchModel.getElementAt(i).getMe3tweaksid() == p.getMe3tweaksid()) {
								patchModel.remove(i);
							}
						}
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
				advertiseInstalls(callingDialog, automated_requiredMixinIds, automated_mod);
			} else if (autoupdateMode) {
				synchronized (lock) {
					numberofupdatedmixins = numChanged;
					lock.notifyAll(); //wake up thread
				}
			}
		}
	}

	private class ModmakerMixinIdentifier {
		public int getMinVersion() {
			return minVersion;
		}

		public void setMinVersion(int minVersion) {
			this.minVersion = minVersion;
		}

		public int getMixinid() {
			return mixinid;
		}

		public void setMixinid(int mixinid) {
			this.mixinid = mixinid;
		}

		private int minVersion;
		private int mixinid;

		public ModmakerMixinIdentifier(String modmakerString) {
			if (modmakerString.contains("v")) {
				int endIDIndex = modmakerString.indexOf('v');
				String id = modmakerString.substring(0, endIDIndex);
				String version = modmakerString.substring(endIDIndex + 1, modmakerString.length());
				this.mixinid = Integer.parseInt(id);
				this.minVersion = Integer.parseInt(version);
			} else {
				this.mixinid = Integer.parseInt(modmakerString);
				this.minVersion = 1;
			}
		}
	}

	public Object getApplyLock() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getLatestMixIns() {
		PatchLibraryWindow plw = new PatchLibraryWindow(AUTOUPDATE_MODE);
		return "Updated " + plw.numberofupdatedmixins + " MixIn" + (plw.numberofupdatedmixins != 1 ? "s" : "");
	}

}
