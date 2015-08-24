package com.me3tweaks.modmanager;

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

	public PatchLibraryWindow() {
		ModManager.debugLogger.writeMessage("Loading patch library interface");
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		new ME3TweaksLibraryUpdater().execute();
		setVisible(true);
	}

	private void setupWindow() {
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Mix-In Library");
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
			System.out.println(patch.convertToME3TweaksSQLInsert());
		}
		lrSplitPane.setLeftComponent(new JScrollPane(patchList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		patchDesc = new JTextArea("Select one or more mix-ins (ctrl+click) on the left to see their descriptions.");

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
		mod.setModName("Select a mod to add mix-ins to");

		modModel.addElement(mod);
		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.getSize(); i++) {
			Mod loadedMod = ModManagerWindow.ACTIVE_WINDOW.modModel.get(i);
			modModel.addElement(loadedMod);
		}

		buttonApplyPatch = new JButton("Apply Mix-Ins");
		buttonApplyPatch.addActionListener(this);
		buttonApplyPatch.setEnabled(false);
		buttonApplyPatch.setToolTipText("Applies the selected Mix-Ins to a mod");

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
				patchDesc.setText("Select one or more mix-ins (ctrl+click) on the left to see their descriptions.");
				buttonApplyPatch.setEnabled(false);
			} else {
				updateDescription();
			}
		} else {
			updateDescription();
		}
	}
	
	private void updateDescription(){
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
			/*
			 * for (Patch patch : selectedPatches) {
			 * ModManager.debugLogger.writeMessage("Applying patch " +
			 * patch.getPatchName() + " to " + mod.getModName()); if
			 * (!patch.applyPatch(mod)) { patchFailed = true;
			 * JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
			 * "Patch failed to apply: " + patch.getPatchName(),
			 * "Patch not applied", JOptionPane.ERROR_MESSAGE); } }
			 */
			new AutoTocWindow(mod);
			ArrayList<Patch> failedpatches = paw.getFailedPatches();
			if (failedpatches.size() <= 0) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "All mix-ins were applied.", "Mix-Ins applied", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String str = "The following Mix-Ins failed to apply:\n";
				for (Patch p : failedpatches) {
					str += " - " + p.getPatchName() + "\n";
				}
				str += "Check the debugging log to see why.";
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, str, "Mix-Ins Error", JOptionPane.ERROR_MESSAGE);
			}
			dispose();
			new ModManagerWindow(false);
		}
	}

	class ME3TweaksLibraryUpdater extends SwingWorker<Void, Patch> {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc;

		@Override
		protected Void doInBackground() throws Exception {
			// Download XML from server
			ModManager.debugLogger.writeMessage("================DOWNLOADING MIXIN LIBRARY INFORMATION==============");
			String link;
			if (ModManager.IS_DEBUG) {
				link = "http://webdev-mgamerz.c9.io/modmanager/mixinlibrary/libraryinfo";
			} else {
				link = "http://me3tweaks.com/modmanager/mixinlibrary/libraryinfo";
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
				for (ME3TweaksPatchPackage serverpack : serverpacks) {
					boolean needsDownloaded = true;

					for (int i = 0; i < patchModel.size(); i++) {
						System.out.println("GETTING INFO FOR PATCH "+(i+1)+" OF "+patchModel.size());
						Patch localpatch = patchModel.getElementAt(i);

						if (!localpatch.getPatchName().equals(serverpack.getPatchname())){
							continue;
						}

						//same name
						if (!localpatch.getTargetModule().equals(serverpack.getTargetmodule())){
							continue;
						}

						//same module
						if (!localpatch.getTargetPath().equals(serverpack.getTargetfile())){
							continue;
						}
						//same target
						
						//same name, same module, same target, likely the same - just check if version is newer.
						
						if (localpatch.getPatchVersion() < serverpack.getPatchver()){
							continue;
						}

						//patch does not need downloaded.
						needsDownloaded = false;
						ModManager.debugLogger.writeMessage("Local MixIn "+localpatch.getPatchName()+" is up to date.");
						break;
					}
					if (needsDownloaded) {
						ModManager.debugLogger.writeMessage("Server MixIn "+serverpack.getPatchname()+" is not present locally (or out of date), adding to download queue");
						packsToDownload.add(serverpack);
					}
				}
				
				//download new packs
				for (ME3TweaksPatchPackage pack : packsToDownload){
					ModManager.debugLogger.writeMessage("Downloading MixIn patch: "+pack.getPatchurl());

					//clear old directories, make new ones
					String targetFolderStr = ModManager.appendSlash(ModManager.getPatchLibraryDir()+pack.getFolder());
					ModManager.debugLogger.writeMessage("Patch directory: "+targetFolderStr);
					File targetFolder = new File(targetFolderStr);
					FileUtils.deleteDirectory(targetFolder);
					targetFolder.mkdirs();
					File jsfFile = new File(targetFolderStr+"patch.jsf");
					FileUtils.copyURLToFile(new URL(pack.getPatchurl()), jsfFile);
					String patchDesc = Patch.generatePatchDesc(pack);
					FileUtils.writeStringToFile(new File(targetFolderStr+"patchdesc.ini"), patchDesc);
					
					Patch p = new Patch(targetFolderStr+"patchdesc.ini");
					publish(p);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		public void process(List<Patch> chunks){
			for (Patch p : chunks) {
				patchModel.addElement(p);
			}
		}

	}

}
