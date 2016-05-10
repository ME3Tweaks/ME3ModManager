package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.FolderBatchWindow.BatchWorker;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.ui.HintTextAreaUI;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.MountFlagCellRenderer;
import com.me3tweaks.modmanager.utilities.DebugLogger;

public class StarterKitWindow extends JDialog {
	JTextField modName, internalDisplayName, internalTLKId, mountPriority, internalDLCName;
	private JTextField modDeveloper;
	private JTextField modSite;
	private JTextArea modDescription;
	private DefaultComboBoxModel<MountFlag> flagModel;
	private JComboBox<MountFlag> mountFlagsCombobox;
	private JButton createButton;
	private JProgressBar progressBar;

	public static void main(String[] args) {
		ModManager.debugLogger = new DebugLogger();
		ModManager.debugLogger.initialize();
		new StarterKitWindow();
	}

	public StarterKitWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow() {
		setTitle("Custom DLC Starter Kit Builder");
		setPreferredSize(new Dimension(500, 500));
		setIconImages(ModManager.ICONS);
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
		internalDLCName = new JTextField();
		internalDisplayName = new JTextField();
		internalTLKId = new JTextField();
		mountPriority = new JTextField();
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
		mountFlagsCombobox
				.setToolTipText("<html>Mount flags determine when this DLC is loaded and if it is required by save files.<br>Having a DLC load in MP will require all players to have the DLC installed or connections will be refused.</html>");

		modName.setUI(new HintTextFieldUI("A Most Excellent Mod", true));
		modDeveloper.setUI(new HintTextFieldUI("GatorZ", true));
		modSite.setUI(new HintTextFieldUI("http://me3tweaks.com/forums/...", true));
		internalDLCName.setUI(new HintTextFieldUI("ExcellentMod", true));
		internalDisplayName.setUI(new HintTextFieldUI("Excellent DLC Module", true));
		internalTLKId.setUI(new HintTextFieldUI("13370000", true));
		mountPriority.setUI(new HintTextFieldUI("4500", true));
		modDescription
				.setUI(new HintTextAreaUI(
						"Mod description goes here.\nThis is what will appear in Mod Manager when the user selects your mod.\nThis is the moddesc attribute in moddesc.ini under [ModInfo].\nNewlines will be replaced with <br>."));
		modName.setToolTipText("<html>Name of this mod that Mod Manager will display.<br>This is the moddesc modname value under [ModInfo]</html>");
		modDeveloper
				.setToolTipText("<html>Developer of this mod. Likely your modding scene alias.<br>This is the moddesc moddev value under [ModInfo]</html>");
		modSite.setToolTipText("<html>Optional website that will show up in Mod Manager the user can click to get help, more info, etc about the mod.<br>This is the moddesc modsite value under [ModInfo]</html>");
		internalDLCName
				.setToolTipText("<html>The internal name for the DLC, after the standard DLC_CON.<br>The hint for this textbox would mean the DLC folder is named DLC_CON_ExcellentMod.</html>");
		internalDisplayName
				.setToolTipText("<html>Internal name for this DLC. If a DLC fails to load, the user may see this name at the main menu.</html>");
		internalTLKId
				.setToolTipText("<html>TLK ID to use for your generated TLK file.<br>A TLK file will be created for the main 6 languages<br>and the internal DLC name will be set on this one.<br>The mount file will point to this value.</html>");
		mountPriority
				.setToolTipText("<html>Mount priority of your mod. Official DLC ends around 3300.<br>Mods that have pcc files with the same name will only load the higher mount priority version.</html>");

		internalTLKId.setInputVerifier(new PositiveIntVerifier());
		mountPriority.setInputVerifier(new PositiveIntVerifier());

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
		panel.add(internalTLKId, c);
		c.gridy++;
		panel.add(mountPriority, c);
		c.gridy++;
		panel.add(mountFlagsCombobox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(modDescription, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
		c.gridy++;

		createButton = new JButton("Generate Starter Kit");
		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String modpath = ModManager.getModsDir() + modName.getText();
				File modpathfile = new File(modpath);
				if (modpathfile.exists() && modpathfile.isDirectory()) {
					int result = JOptionPane.showConfirmDialog(StarterKitWindow.this, "A mod named " + modName.getText()
							+ " already exists.\nDelete this mod and create the starter kit in its place?", "Mod already exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.CANCEL_OPTION) {
						return;
					} else {
						try {
							FileUtils.deleteDirectory(modpathfile);
						} catch (IOException e1) {
							return;
						}
					}
				}
				//validate...
				int internaltlkidval = Integer.parseInt(internalTLKId.getText());
				int mountpriorityval = Integer.parseInt(mountPriority.getText());

				//create
				StarterKitGenerator starterKit = new StarterKitGenerator();
				starterKit.setInternaldisplayname(internalDisplayName.getText());
				starterKit.setModname(modName.getText());
				starterKit.setModdev(modDeveloper.getText());
				starterKit.setModsite(modSite.getText());
				starterKit.setModdesc(modDescription.getText());
				starterKit.setInternaldisplayname(internalDisplayName.getText());
				starterKit.setInternaldlcname(internalDLCName.getText());
				starterKit.setMountflag(flagModel.getElementAt(mountFlagsCombobox.getSelectedIndex()));
				starterKit.setMountpriority(mountpriorityval);
				starterKit.setTlkid(internaltlkidval);
				ModManager.debugLogger.writeMessage("Executing StarterKit Generator");
				starterKit.execute();
			}
		});
		c.gridx = 0;
		c.weighty = 0;
		panel.add(createButton, c);
		c.gridy++;
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		panel.add(progressBar, c);
		add(panel);

		//DEBUG ONLY
		modName.setText("Starter Kit Testing");
		internalDisplayName.setText("Starter Kit Testing");
		internalTLKId.setText("6700000");
		mountPriority.setText("4505");
		internalDLCName.setText("StartKitTest");
		modDeveloper.setText("Gatomade");
		modSite.setText("http://example.com");
		modDescription.setText("This is what a mod description looks like.\nIt has multiple lines.\nOf txt.");
		pack();
	}

	class PositiveIntVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent input) {
			String text = ((JTextField) input).getText();
			try {
				int value = Integer.parseInt(text);
				return (value > 0 && value < 20000);
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	class StarterKitGenerator extends SwingWorker<Void, Void> {
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

		private String modname, moddev, moddesc, modsite, internaldlcname, internaldisplayname;

		public void setModdev(String moddev) {
			this.moddev = moddev;
		}

		private int tlkid, mountpriority;
		private MountFlag mountflag;

		public StarterKitGenerator() {
			createButton.setVisible(false);
			progressBar.setVisible(true);
			progressBar.setIndeterminate(true);
			//pack();
		}

		@Override
		protected Void doInBackground() throws Exception {
			String modpath = ModManager.getModsDir() + modname + File.separator;

			//create mod dir
			File modpathfile = new File(modpath);
			boolean madedir = modpathfile.mkdirs();

			//create custom dlc folders
			String cookedPath = modpath + "DLC_CON_" + internaldlcname + File.separator + "CookedPCConsole" + File.separator;
			File cookedpcconsole = new File(cookedPath);
			boolean madecookeddir = cookedpcconsole.mkdirs();

			//extract resources
			ModManager.ExportResource("/Default.sfar", cookedPath + "Default.sfar");
			ModManager.ExportResource("/Mount.dlc", cookedPath + "Mount.dlc");
			String coalpath = cookedPath + "Default_DLC_CON_" + internaldlcname + ".bin";
			ModManager.ExportResource("/Default_DLC_CON_StarterKit.bin", coalpath);
			String[] langs = { "INT", "DEU", "ESN", "FRA", "POL", "RUS" };

			for (String lang : langs) {
				System.out.println("Processing " + lang);
				String output = cookedPath + "DLC_CON_" + internaldlcname + "_" + lang + ".xml";
				ModManager.ExportResource("/StarterKitTLK.xml", output);
				String replaceOutput = FileUtils.readFileToString(new File(output));

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
				replaceOutput = replaceOutput.replaceAll("%INTERNALDLCNAME%", "DLC_CON_" + internaldlcname);
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
				FileUtils.writeStringToFile(new File(output), replaceOutput);
			}
			//Compile TLK.
			new FolderBatchWindow.BatchWorker(cookedpcconsole, BatchWorker.COMPILE_TLK).execute();

			//while tlk is compiling do more work on .bin file.
			CoalescedWindow.decompileCoalesced(coalpath);
			File bioenginefile = new File(cookedPath + "Default_DLC_CON_" + internaldlcname + File.separator + "BioEngine.xml");
			String bioengine = FileUtils.readFileToString(bioenginefile);
			String newengine = bioengine.replaceAll("StarterKit", internaldlcname); //update bioengine
			System.out.println("Same in/out? " + bioengine.equals(newengine));
			boolean deleted = FileUtils.deleteQuietly(bioenginefile);
			System.out.println("Deleted: " + deleted);
			FileUtils.writeStringToFile(new File(cookedPath + "Default_DLC_CON_" + internaldlcname + File.separator + "BioEngine.xml"), newengine); //writeback

			//recompile and move up a dir
			CoalescedWindow.compileCoalesced(cookedPath + "Default_DLC_CON_" + internaldlcname + File.separator + "Default_DLC_CON_"
					+ internaldlcname + ".xml");
			FileUtils.deleteQuietly(new File(coalpath));
			FileUtils.copyFile(new File(cookedPath + "Default_DLC_CON_" + internaldlcname + File.separator + "Default_DLC_CON_" + internaldlcname
					+ ".bin"), new File(coalpath));

			//update mount.dlc
			MountFileEditorWindow.SaveMount(cookedPath + "Mount.dlc", Integer.toString(mountpriority), Integer.toString(tlkid), mountflag,
					mountpriority);

			//create workspace
			Thread.sleep(5000); //debug for now
			String tlkpath = modpath + "WORKSPACE" + File.separator + "TLKs" + File.separator;
			File tlkpathfile = new File(tlkpath);
			tlkpathfile.mkdirs();
			List<File> files = (List<File>) FileUtils.listFiles(cookedpcconsole, new String[] { "xml" }, false);
			for (File file : files) {
				System.out.println(file);
				FileUtils.moveFile(file, new File(tlkpath + FilenameUtils.getName(file.getAbsolutePath())));
			}

			//move coaleseced folder
			FileUtils.moveDirectory(new File(cookedPath + "Default_DLC_CON_" + internaldlcname), new File(modpath + "WORKSPACE" + File.separator
					+ "Default_DLC_CON_" + internaldlcname));

			//Create moddesc.ini
			Mod startermod = new Mod();
			startermod.setModPath(modpath);
			ModJob custdlcjob = new ModJob("DLC_CON_" + internaldlcname, ModType.CUSTOMDLC, "");

			custdlcjob.setJobName(ModType.CUSTOMDLC); //backwards, it appears...
			custdlcjob.setJobType(ModJob.CUSTOMDLC);
			ArrayList<String> destFolders = new ArrayList<>();
			ArrayList<String> srcFolders = new ArrayList<>();
			destFolders.add("DLC_CON_" + internaldlcname);
			srcFolders.add(modpath + "DLC_CON_" + internaldlcname);
			custdlcjob.setDestFolders(destFolders);
			custdlcjob.setSourceFolders(srcFolders);
			startermod.addTask(ModType.CUSTOMDLC, custdlcjob);
			startermod.setAuthor(moddev);
			startermod.setModDescription(moddesc);
			startermod.setSite(modsite);
			startermod.setModName(modname);
			FileUtils.writeStringToFile(new File(modpath + "moddesc.ini"), startermod.createModDescIni(false, 4.2));

			//reload newly written mod.
			startermod = new Mod(modpath + "moddesc.ini");
			if (!startermod.isValidMod()) {
				//ERROR!
				ModManager.debugLogger.writeError("Failed to produce valid starter kit mod");
			} else {
				//Run autotoc on mod
				new AutoTocWindow(startermod, AutoTocWindow.LOCALMOD_MODE, null);
			}
			return null;
		}

		@Override
		public void done() {
			try {
				get();
			} catch (Throwable e) {
				ModManager.debugLogger.writeErrorWithException("Failure creating starter kit:", e);
			}
			progressBar.setVisible(false);
			createButton.setVisible(true);
		}
	}

}
