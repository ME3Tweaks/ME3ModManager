package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.LayerUI;
import javax.swing.text.NumberFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.me3tweaks.modmanager.FolderBatchWindow.BatchWorker;
import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextAreaUI;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.MountFlagCellRenderer;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class StarterKitWindow extends JDialog {
	JTextField modName, internalDisplayName;
	JFormattedTextField internalTLKId, mountPriority, internalDLCName;
	private JTextField modDeveloper;
	private JTextField modSite;
	private JTextArea modDescription;
	private DefaultComboBoxModel<MountFlag> flagModel;
	private JComboBox<MountFlag> mountFlagsCombobox;

	public StarterKitWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow() {
		setTitle("Custom DLC Starter Kit Builder");
		setPreferredSize(new Dimension(500, 500));
		setIconImages(ModManager.ICONS);
		LayerUI<JFormattedTextField> layerUI = new ValidationLayerUI();

		//format spec
		NumberFormat tlkFormat = NumberFormat.getInstance();
		tlkFormat.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(tlkFormat);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE / 2);
		formatter.setCommitsOnValidEdit(true);

		NumberFormat mountFormat = NumberFormat.getInstance();
		mountFormat.setGroupingUsed(false);
		NumberFormatter mountformatter = new NumberFormatter(mountFormat);
		mountformatter.setValueClass(Short.class);
		mountformatter.setMinimum(1);
		mountformatter.setMaximum(Short.MAX_VALUE);
		mountformatter.setCommitsOnValidEdit(true);

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();

		int labelColumn = 0;
		int fieldColumn = 1;
		c.weightx = 1;
		c.gridx = labelColumn;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Mod Name"), c);
		c.gridy++;
		panel.add(new JLabel("Mod Developer"), c);
		c.gridy++;
		panel.add(new JLabel("Mod Web Site (Optional)"), c);
		c.gridy++;
		panel.add(new JLabel("Internal DLC Name"), c);
		c.gridy++;
		panel.add(new JLabel("Internal Name"), c);
		c.gridy++;
		panel.add(new JLabel("Internal Name TLK ID"), c);
		c.gridy++;
		panel.add(new JLabel("Mount Priority"), c);
		c.gridy++;
		panel.add(new JLabel("Mount Flag"), c);

		modName = new JTextField();
		modDeveloper = new JTextField();
		modSite = new JTextField();
		internalDLCName = new JFormattedTextField();
		internalDisplayName = new JTextField();
		internalTLKId = new JFormattedTextField(tlkFormat);
		mountPriority = new JFormattedTextField(mountFormat);
		modDescription = new JTextArea();
		mountFlagsCombobox = new JComboBox<MountFlag>();
		flagModel = new DefaultComboBoxModel<MountFlag>();
		flagModel.addElement(new MountFlag("SP | Does not require DLC in save file", 8));
		flagModel.addElement(new MountFlag("SP | Requires DLC in save file", 9));
		flagModel.addElement(new MountFlag("SP&MP | Does not require DLC in save file", 28));
		flagModel.addElement(new MountFlag("MP (PATCH) | Loads in MP", 12));
		flagModel.addElement(new MountFlag("MP | Loads in MP", 20));
		flagModel.addElement(new MountFlag("MP | Loads in MP", 52));

		mountFlagsCombobox.setModel(flagModel);
		mountFlagsCombobox.setRenderer(new MountFlagCellRenderer());
		mountFlagsCombobox.setToolTipText(
				"<html>Mount flags determine when this DLC is loaded and if it is required by save files.<br>Having a DLC load in MP will require all players to have the DLC installed or connections will be refused.</html>");

		modName.setUI(new HintTextFieldUI("A Most Excellent Mod", true));
		modDeveloper.setUI(new HintTextFieldUI("GatorZ", true));
		modSite.setUI(new HintTextFieldUI("http://me3tweaks.com/forums/...", true));
		internalDLCName.setUI(new HintTextFieldUI("ExcellentMod", true));
		internalDisplayName.setUI(new HintTextFieldUI("Excellent DLC Module", true));
		internalTLKId.setUI(new HintTextFieldUI("13370000", true));
		mountPriority.setUI(new HintTextFieldUI("4500", true));
		modDescription.setUI(new HintTextAreaUI(
				"Mod description goes here.\nThis is what will appear in Mod Manager when the user selects your mod.\nThis is the moddesc attribute in moddesc.ini under [ModInfo].\nNewlines will be replaced with <br>."));
		modName.setToolTipText("<html>Name of this mod that Mod Manager will display.<br>This is the moddesc modname value under [ModInfo]</html>");
		modDeveloper.setToolTipText("<html>Developer of this mod. Likely your modding scene alias.<br>This is the moddesc moddev value under [ModInfo]</html>");
		modSite.setToolTipText(
				"<html>Optional website that will show up in Mod Manager the user can click to get help, more info, etc about the mod.<br>This is the moddesc modsite value under [ModInfo]</html>");
		internalDLCName.setToolTipText(
				"<html>The internal name for the DLC, after the standard DLC_MOD.<br>The hint for this textbox would mean the DLC folder is named DLC_MOD_ExcellentMod.</html>");
		internalDisplayName.setToolTipText("<html>Internal name for this DLC. If a DLC fails to load, the user may see this name at the main menu.</html>");
		internalTLKId.setToolTipText(
				"<html>TLK ID to use for your generated TLK file.<br>A TLK file will be created for the main 6 languages<br>and the internal DLC name will be set on this one.<br>The mount file will point to this value.</html>");
		mountPriority.setToolTipText(
				"<html>Mount priority of your mod. Official DLC ends around 3300.<br>Mods that have pcc files with the same name will only load the higher mount priority version.</html>");

		c.gridy = 0;
		c.gridx = fieldColumn;
		panel.add(modName, c);
		c.gridy++;
		panel.add(modDeveloper, c);
		c.gridy++;
		panel.add(modSite, c);
		c.gridy++;
		panel.add(internalDLCName, c);
		c.gridy++;
		panel.add(internalDisplayName, c);
		c.gridy++;
		panel.add(new JLayer<JFormattedTextField>(internalTLKId, layerUI), c);
		c.gridy++;
		panel.add(new JLayer<JFormattedTextField>(mountPriority, layerUI), c);
		c.gridy++;
		panel.add(mountFlagsCombobox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(modDescription, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
		c.gridy++;

		JButton createButton = new JButton("Generate Starter Kit");
		JProgressBar progressBar = new JProgressBar();

		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateFields()) {
					String modpath = ModManager.getModsDir() + modName.getText();
					File modpathfile = new File(modpath);
					if (modpathfile.exists() && modpathfile.isDirectory()) {
						int result = JOptionPane.showConfirmDialog(StarterKitWindow.this,
								"A mod named " + modName.getText() + " already exists.\nDelete this mod and create the starter kit in its place?", "Mod already exists",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (result == JOptionPane.NO_OPTION) {
							return;
						} else {
							try {
								FileUtils.deleteDirectory(modpathfile);
							} catch (IOException e1) {
								return;
							}
						}
						System.out.println(result);
					}
					//validate...
					int internaltlkidval = Integer.parseInt(internalTLKId.getText().trim());
					int mountpriorityval = Integer.parseInt(mountPriority.getText().trim());

					//create
					StarterKitGenerator starterKit = new StarterKitGenerator(createButton, progressBar, StarterKitWindow.this);
					starterKit.setModname(modName.getText().trim());
					starterKit.setModdev(modDeveloper.getText().trim());
					starterKit.setModsite(modSite.getText().trim());
					String moddesc = modDescription.getText().trim();
					if (moddesc.length() <= 0) {
						moddesc = "This mod was generated by Mod Manager Custom DLC Starter Kit.";
					}
					starterKit.setModdesc(moddesc);
					starterKit.setInternaldisplayname(internalDisplayName.getText().trim());
					starterKit.setInternaldlcname(internalDLCName.getText().trim());
					starterKit.setMountflag(flagModel.getElementAt(mountFlagsCombobox.getSelectedIndex()));
					starterKit.setMountpriority(mountpriorityval);
					starterKit.setTlkid(internaltlkidval);
					ModManager.debugLogger.writeMessage("Executing StarterKit Generator");
					starterKit.execute();
				}
			}
		});
		c.gridx = 0;
		c.weighty = 0;
		panel.add(createButton, c);
		c.gridy++;

		progressBar.setVisible(false);
		panel.add(progressBar, c);
		add(panel);

		//DEBUG ONLY
		/*
		 * modName.setText("Starter Kit Testing");
		 * internalDisplayName.setText("Starter Kit Testing");
		 * internalTLKId.setText("6700000"); mountPriority.setText("4505");
		 * internalDLCName.setText("StartKitTest");
		 * modDeveloper.setText("Gatomade");
		 * modSite.setText("http://example.com"); modDescription.setText(
		 * "This is what a mod description looks like.\nIt has multiple lines.\nOf txt."
		 * );
		 */
		pack();
	}

	private boolean validateFields() {
		String modname = modName.getText().trim();
		if (modname.length() <= 0) {
			showErrorMessage("Invalid mod name. The mod name must be at least 1 alphanumeric character.");
			return false;
		}

		//ASCII only check
		if (!modname.chars().allMatch(c -> c == 0x20 || c == 0x5F || (c > 0x30 && c < 0x3A) || (c > 0x40 && c < 0x5B) || (c > 0x60 && c < 0x7B))) {
			showErrorMessage("Invalid Mod Name. Only spaces, underscores and alphanumeric characters are allowed. (Windows restriction)");
			return false;
		}

		String modDev = modDeveloper.getText().trim();
		if (modDev.length() <= 0) {
			showErrorMessage("Invalid mod developer name. Enter the name you want to Mod Manager to show as the developer.");
			return false;
		}

		String modsite = modSite.getText().trim();
		if (!modsite.equals("")) {
			String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
			UrlValidator urlValidator = new UrlValidator(schemes);
			if (!urlValidator.isValid(modsite)) {
				showErrorMessage(
						"Invalid mod site. This is the clickable link at the bottom of Mod Manager mod descriptions in the right side pane.\nThe specified URL is not valid.");
				return false;
			}
		}

		String intDispName = internalDisplayName.getText().trim();
		if (intDispName.length() <= 0 || intDispName.length() > 75) {
			showErrorMessage("Invalid Internal Name. Valid lengths are between 1 and 75 characters.");
			return false;
		}

		//Internal DLC Name
		String intDLCName = internalDLCName.getText().trim();
		if (intDLCName.length() < 1 || intDLCName.length() > 20) {
			showErrorMessage("Invalid Internal DLC Name. Length must be between 1 and 20 characters.\nThis is the part that comes after DLC_MOD_, e.g. DNA for DLC_MOD_DNA.");
			return false;
		}

		//ASCII only check
		if (!intDLCName.chars().allMatch(c -> c == 0x5F || (c > 0x30 && c < 0x3A) || (c > 0x40 && c < 0x5B) || (c > 0x60 && c < 0x7B))) {
			showErrorMessage("Invalid Internal DLC Name. Only underscore and alphanumeric characters are allowed by the game.");
			return false;
		}

		//Internal TLK ID
		String intTLKId = internalTLKId.getText().trim();
		try {
			int s = Integer.parseInt(intTLKId);
			if (s < 1 || s > Integer.MAX_VALUE / 2) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			showErrorMessage("Invalid Internal TLK ID. Value must be between 1 and " + Integer.MAX_VALUE / 2 + ".");
			return false;
		}

		//Mount Priority
		String priorityString = mountPriority.getText().trim();
		try {
			short s = Short.parseShort(priorityString);
			if (s < 1 || s > 32767) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			showErrorMessage("Invalid Mount Priority. Value must be between 1 and " + Short.MAX_VALUE + ".");
			return false;
		}

		ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfoByMountID(priorityString);
		if (tpmi != null) {
			int result = JOptionPane.showConfirmDialog(StarterKitWindow.this,
					"<html><div style='width: 400px'>The mod you are creating has the same mount priority as " + tpmi.getModname()
							+ ". You should change this priority to prevent conflicts.<br><br>When mount priorities conflict, the game behaves in an undefined manner.<br>Change mount priority?</div></html>",
					"Conflicting Mount Priority", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				return false;
			}
		}
		return true;
	}

	private void showErrorMessage(String string) {
		JOptionPane.showMessageDialog(this, string, "Invalid value", JOptionPane.ERROR_MESSAGE);
	}

	static class StarterKitGenerator extends SwingWorker<Boolean, ThreadCommand> {
		private String modname, moddev, moddesc, modsite, internaldlcname, internaldisplayname;

		public void setModname(String modname) {
			this.modname = modname;
		}

		public void setModdesc(String moddesc) {
			this.moddesc = moddesc;
		}

		public void setModsite(String modsite) {
			this.modsite = modsite;
		}

		public void setInternaldlcname(String internaldlcname) {
			this.internaldlcname = internaldlcname;
		}

		public void setInternaldisplayname(String internaldisplayname) {
			this.internaldisplayname = internaldisplayname;
		}

		public void setTlkid(int tlkid) {
			this.tlkid = tlkid;
		}

		public void setMountpriority(int mountpriority) {
			this.mountpriority = mountpriority;
		}

		public void setMountflag(MountFlag mountflag) {
			this.mountflag = mountflag;
		}

		public void setModdev(String moddev) {
			this.moddev = moddev;
		}

		private int tlkid, mountpriority;
		private MountFlag mountflag;
		private JComponent progressBar;
		private JComponent createButton;
		private Mod generatedMod;
		public final Object lock = new Lock(); //threading wait() and notifyall();
		public boolean completed = false;
		private StarterKitProgressDialog dialog;
		private JDialog callingDialog;

		public StarterKitGenerator(JComponent createButton, JComponent progressBar, JDialog callingDialog) {
			this.createButton = createButton;
			this.progressBar = progressBar;
			this.callingDialog = callingDialog;
			if (createButton != null) {
				createButton.setVisible(false);
			}
			if (progressBar != null) {
				progressBar.setVisible(true);
			}
			dialog = new StarterKitProgressDialog(callingDialog, "Starter Kit Generator", new Dimension(350, 80));
			dialog.progressBar.setIndeterminate(true);
			dialog.infoLabel.setText("Extracting resources");
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			publish(new ThreadCommand("SET_DIALOG_VISIBLE"));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Extracting default resources"));
			String modpath = ModManager.getModsDir() + modname + File.separator;

			//create mod dir
			File modpathfile = new File(modpath);
			boolean madedir = modpathfile.mkdirs();

			//create custom dlc folders
			String cookedPath = modpath + "DLC_MOD_" + internaldlcname + File.separator + "CookedPCConsole" + File.separator;
			File cookedpcconsole = new File(cookedPath);
			boolean madecookeddir = cookedpcconsole.mkdirs();

			//extract resources
			ModManager.ExportResource("/Default.sfar", cookedPath + "Default.sfar");
			ModManager.ExportResource("/Mount.dlc", cookedPath + "Mount.dlc");
			String coalpath = cookedPath + "Default_DLC_MOD_" + internaldlcname + ".bin";
			ModManager.ExportResource("/Default_DLC_MOD_StarterKit.bin", coalpath);
			String[] langs = { "INT", "DEU", "ESN", "FRA", "POL", "RUS" };

			for (String lang : langs) {
				publish(new ThreadCommand("SET_DIALOG_TEXT", "Updating TLK for " + lang));
				String output = cookedPath + "DLC_MOD_" + internaldlcname + "_" + lang + ".xml";
				ModManager.ExportResource("/StarterKitTLK.xml", output);
				String replaceOutput = FileUtils.readFileToString(new File(output), "UTF-8");
				ModManager.debugLogger.writeMessage("DEBUGGING: LANGUAGE INPUT TEXT WAS READ AS:");
				ModManager.debugLogger.writeMessage(replaceOutput);
				String langcode = "";
				switch (lang) {
				case "INT":
					langcode = "en-us";
					break;
				case "DEU":
					langcode = "de-de";
					break;
				case "ESN":
					langcode = "es-es";
					break;
				case "FRA":
					langcode = "fr-fr";
					break;
				case "RUS":
					langcode = "ru-ru";
					break;
				case "POL":
					langcode = "pl-pl";
					break;
				}

				//Set values
				replaceOutput = replaceOutput.replaceAll("%STARTKIT%", internaldlcname + "_" + lang);
				replaceOutput = replaceOutput.replaceAll("%INTERNALDISPLAYNAME%", internaldisplayname);
				replaceOutput = replaceOutput.replaceAll("%INTERNALDLCNAME%", "DLC_MOD_" + internaldlcname);
				replaceOutput = replaceOutput.replaceAll("%LANG%", langcode);

				//set ID numbers
				int currid = tlkid;
				replaceOutput = replaceOutput.replaceAll("%INTERNALDISPLAYNAMEID%", Integer.toString(currid));
				currid++;
				replaceOutput = replaceOutput.replaceAll("%INTERNALDLCNAMEID%", Integer.toString(currid));
				currid++;
				replaceOutput = replaceOutput.replaceAll("%LANGID%", Integer.toString(currid));
				currid++;
				replaceOutput = replaceOutput.replaceAll("%MALEID%", Integer.toString(currid));
				currid++;
				replaceOutput = replaceOutput.replaceAll("%FEMALEID%", Integer.toString(currid));
				ModManager.debugLogger.writeMessage("----------------------");
				ModManager.debugLogger.writeMessage("AFTER TRANSFORMATION, OUTPUT IS NOW:");
				ModManager.debugLogger.writeMessage(replaceOutput);

				FileUtils.writeStringToFile(new File(output), replaceOutput, StandardCharsets.UTF_8);
			}
			//Compile TLK.
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 25));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Compiling TLKs..."));
			FolderBatchWindow.BatchWorker bw = new FolderBatchWindow.BatchWorker(cookedpcconsole, BatchWorker.COMPILE_TLK, dialog);
			bw.execute();
			synchronized (bw.lock) {
				while (!bw.completed) {
					try {
						bw.lock.wait();
					} catch (InterruptedException ex) {
						// TODO Auto-generated catch block
						ModManager.debugLogger.writeErrorWithException("Unable to wait for for folder batch to finish:", ex);
					}
				}
			}
			ModManager.debugLogger.writeMessage("Folder batch worker has completed. Resuming StarterKitGenerator");
			//while tlk is compiling do more work on .bin file.
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 50));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Compiling Default_DLC_MOD_" + internaldlcname));
			CoalescedWindow.decompileCoalesced(coalpath);
			File bioenginefile = new File(cookedPath + "Default_DLC_MOD_" + internaldlcname + File.separator + "BioEngine.xml");
			String bioengine = FileUtils.readFileToString(bioenginefile, "UTF-8");
			String newengine = bioengine.replaceAll("StarterKit", internaldlcname); //update bioengine
			boolean deleted = FileUtils.deleteQuietly(bioenginefile);

			FileUtils.writeStringToFile(new File(cookedPath + "Default_DLC_MOD_" + internaldlcname + File.separator + "BioEngine.xml"), newengine, StandardCharsets.UTF_8); //writeback

			//recompile and move up a dir
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 60));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Moving Default_DLC_MOD_" + internaldlcname));
			ModManager.debugLogger.writeMessage("Recompiling Default_DLC_MOD_" + internaldlcname + ".bin");
			CoalescedWindow.compileCoalesced(cookedPath + "Default_DLC_MOD_" + internaldlcname + File.separator + "Default_DLC_MOD_" + internaldlcname + ".xml");
			FileUtils.deleteQuietly(new File(coalpath));
			ModManager.debugLogger.writeMessage("Moving Default_DLC_MOD_" + internaldlcname + ".bin to " + coalpath);
			FileUtils.moveFile(new File(cookedPath + "Default_DLC_MOD_" + internaldlcname + File.separator + "Default_DLC_MOD_" + internaldlcname + ".bin"), new File(coalpath));

			//update mount.dlc
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 70));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Updating Mount.dlc"));
			ModManager.debugLogger.writeMessage("Updating Mount.dlc");
			MountFileEditorWindow.SaveMount(cookedPath + "Mount.dlc", Integer.toString(tlkid), mountflag, mountpriority);

			MountFile mf = new MountFile(cookedPath + "Mount.dlc");
			//create workspace
			ModManager.debugLogger.writeMessage("Creating mod workspace");
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 75));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Configuring mod workspace"));
			String tlkpath = modpath + "WORKSPACE" + File.separator + "TLKs" + File.separator;
			File tlkpathfile = new File(tlkpath);
			tlkpathfile.mkdirs();
			List<File> files = (List<File>) FileUtils.listFiles(cookedpcconsole, new String[] { "xml" }, false);
			for (File file : files) {
				System.out.println(file);
				FileUtils.moveFile(file, new File(tlkpath + FilenameUtils.getName(file.getAbsolutePath())));
			}

			//move coaleseced folder
			ModManager.debugLogger.writeMessage("Moving Coalesced folder");
			FileUtils.moveDirectory(new File(cookedPath + "Default_DLC_MOD_" + internaldlcname),
					new File(modpath + "WORKSPACE" + File.separator + "Default_DLC_MOD_" + internaldlcname));

			//Create moddesc.ini
			ModManager.debugLogger.writeMessage("Creating moddesc.ini for " + modname);
			publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 95));
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Generating new Mod Manager mod"));

			Mod startermod = new Mod();
			startermod.setModPath(modpath);
			ModJob custdlcjob = new ModJob("DLC_MOD_" + internaldlcname, ModType.CUSTOMDLC, "");

			custdlcjob.setJobName(ModType.CUSTOMDLC); //backwards, it appears...
			custdlcjob.setJobType(ModJob.CUSTOMDLC);
			ArrayList<String> destFolders = new ArrayList<>();
			ArrayList<String> srcFolders = new ArrayList<>();
			destFolders.add("DLC_MOD_" + internaldlcname);
			srcFolders.add(modpath + "DLC_MOD_" + internaldlcname);
			custdlcjob.setDestFolders(destFolders);
			custdlcjob.setSourceFolders(srcFolders);
			startermod.addTask(ModType.CUSTOMDLC, custdlcjob);
			startermod.setAuthor(moddev);
			startermod.setModDescription(moddesc);
			startermod.setSite(modsite);
			startermod.setModName(modname);
			startermod.setVersion(1.0);
			FileUtils.writeStringToFile(new File(modpath + "moddesc.ini"), startermod.createModDescIni(false, 4.2), StandardCharsets.UTF_8);

			//reload newly written mod.
			ModManager.debugLogger.writeMessage("Loading moddesc.ini to verify mod is valid");
			startermod = new Mod(modpath + "moddesc.ini");
			if (!startermod.isValidMod()) {
				//ERROR!
				ModManager.debugLogger.writeError("Failed to produce valid starter kit mod");
				return false;
			} else {
				//Run autotoc on mod
				publish(new ThreadCommand("SET_DIALOG_PROGRESS", null, 98));
				publish(new ThreadCommand("SET_DIALOG_TEXT", "Running AutoTOC on mod"));
				new AutoTocWindow(startermod, AutoTocWindow.LOCALMOD_MODE, null);
			}
			generatedMod = startermod;
			publish(new ThreadCommand("SET_DIALOG_TEXT", "Mod created."));
			return true;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "SET_DIALOG_VISIBLE":
					dialog.setVisible(true);
					break;
				case "SET_DIALOG_TEXT":
					dialog.infoLabel.setText(tc.getMessage());
					break;
				case "SET_DIALOG_PROGRESS":
					int value = (int) tc.getData();
					dialog.progressBar.setIndeterminate(value <= 0);
					dialog.progressBar.setValue(value);
					break;
				default:
					ModManager.debugLogger.writeError("Unknown thread command in starter kit generator: " + tc.getCommand());
					break;
				}
			}
		}

		@Override
		public void done() {
			completed = true;
			boolean OK = false;
			try {
				OK = get();
			} catch (Throwable e) {
				ModManager.debugLogger.writeErrorWithException("Failure creating starter kit:", e);
			}
			if (createButton != null) {
				createButton.setVisible(true);
			}
			if (progressBar != null) {
				progressBar.setVisible(false);
			}
			dialog.dispose();
			synchronized (lock) {
				lock.notifyAll(); //wake up thread
			}
			if (callingDialog instanceof StarterKitWindow) {
				if (OK) {
					JOptionPane.showMessageDialog(callingDialog,
							modname + " has been created.\nPlace files into the mod's DLC_MOD_" + internaldlcname
									+ " folder to add game files to the mod.\nReload Mod Manager before installing so it refreshes the list of files in the folder.\nBe sure to run AutoTOC on the mod before installation.",
							modname + " created", JOptionPane.INFORMATION_MESSAGE);
					callingDialog.dispose();
					ResourceUtils.openDir(generatedMod.getModPath());
					new ModManagerWindow(false);
				} else {
					JOptionPane.showMessageDialog(callingDialog,
							modname + " was not successfully created.\nReview the Mod Manager log in the help menu for more detailed information.\nIf you continue to have issues contact FemShep with the log attached.",
							modname + " not created", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		public Mod getGeneratedMod() {
			return generatedMod;
		}
	}

	static class StarterKitProgressDialog extends JDialog {

		JLabel infoLabel;
		JProgressBar progressBar;

		public StarterKitProgressDialog(JDialog callingDialog, String title, Dimension size) {
			setupDialog(callingDialog, title, size);
		}

		private void setupDialog(JDialog dialog, String title, Dimension size) {
			JPanel aboutPanel = new JPanel(new BorderLayout());
			infoLabel = new JLabel("<html>Placeholder text</html>", SwingConstants.CENTER);
			aboutPanel.add(infoLabel, BorderLayout.NORTH);
			progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			progressBar.setIndeterminate(false);
			aboutPanel.add(progressBar, BorderLayout.CENTER);
			aboutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			this.getContentPane().add(aboutPanel);
			this.setTitle(title);
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.setResizable(false);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setIconImages(ModManager.ICONS);
			this.setPreferredSize(size);
			this.pack();
			this.setLocationRelativeTo(dialog);
		}
	}

	class ValidationLayerUI extends LayerUI<JFormattedTextField> {
		@Override
		public void paint(Graphics g, JComponent c) {
			super.paint(g, c);

			JLayer jlayer = (JLayer) c;
			JFormattedTextField ftf = (JFormattedTextField) jlayer.getView();
			if (!ftf.isEditValid()) {
				Graphics2D g2 = (Graphics2D) g.create();

				// Paint the red X.
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = c.getWidth();
				int h = c.getHeight();
				int s = 8;
				int pad = 4;
				int x = w - pad - s;
				int y = (h - s) / 2;
				g2.setPaint(Color.red);
				g2.fillRect(x, y, s + 1, s + 1);
				g2.setPaint(Color.white);
				g2.drawLine(x, y, x + s, y + s);
				g2.drawLine(x, y + s, x + s, y);

				g2.dispose();
			}
		}
	}
}
