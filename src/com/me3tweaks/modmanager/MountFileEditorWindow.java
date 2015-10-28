package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.DatatypeConverter;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.ui.MountFlagCellRenderer;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class MountFileEditorWindow extends JDialog {
	private static final int MOUNTDLC_LENGTH = 108;
	private static boolean ISRUNNINGASMAIN = false;
	private static final String DEFAULT_MOUNT_DATA = "01000000AC020000C20000006B0003008C0A0000000000001C00000092190B0092190B00331500000B0000005A7BBD26DD417E499CC660D2587278EB2E2C6A06130AE44783EA08F387A0E2DA0000000000000000000000000000000000000000000000000000000000000000";
	/*
	 * private byte[] mountBytes = DatatypeConverter .parseHexBinary(
	 * "01000000AC020000C20000006B0003008C0A0000000000001C00000092190B0092190B00331500000B0000005A7BBD26DD417E499CC660D2587278EB2E2C6A06130AE44783EA08F387A0E2DA0000000000000000000000000000000000000000000000000000000000000000"
	 * );
	 */private JComboBox<MountFlag> mountFlagsCombobox;
	private JFormattedTextField tlkIdField;
	private JFormattedTextField priorityField;
	private JLabel lStatus;
	private JPanel editorPanel;
	private static int PRIORITY_OFFSET = 16 * 2;
	private static int MPSPFLAG_OFFSET = 24 * 2;
	private static int TLKOFFSET_1 = 28 * 2;
	private static int TLKOFFSET_2 = 32 * 2;
	private DefaultComboBoxModel<MountFlag> flagModel;

	private static DecimalFormat integerFormat = new DecimalFormat("#");

	public MountFileEditorWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Mount.dlc Editor");
		setIconImages(ModManager.ICONS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		GridBagConstraints c = new GridBagConstraints();

		//Load Panel
		JPanel loadPanel = new JPanel(new GridBagLayout());
		loadPanel.setBorder(new TitledBorder(new EtchedBorder(), "Load a Mount.dlc file for editing (optional)"));
		JTextField lInputField = new JTextField(55);
		lInputField.setUI(new HintTextFieldUI("Select a Mount.dlc"));
		JButton lBrowse = new JButton("Browse...");
		lBrowse.setPreferredSize(new Dimension(100, 19));
		JButton butLoadMount = new JButton("Load Mount");
		butLoadMount.setEnabled(false);
		butLoadMount.setPreferredSize(new Dimension(100, 23));
		lStatus = new JLabel(" ");

		c = new GridBagConstraints();
		c.gridx = 0;
		loadPanel.add(lInputField, c);

		c.gridx = 1;
		loadPanel.add(lBrowse, c);

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		loadPanel.add(lStatus, c);

		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		loadPanel.add(butLoadMount, c);

		//TLK Panel
		JPanel tlkPanel = new JPanel(new GridBagLayout());
		tlkPanel.setBorder(new TitledBorder(new EtchedBorder(), "Load a TLK file (optional)"));
		JTextField tInputField = new JTextField(55);
		tInputField.setUI(new HintTextFieldUI("Select a TLK file"));
		JButton tBrowse = new JButton("Browse...");
		tBrowse.setPreferredSize(new Dimension(100, 19));
		JButton butLoadTLK = new JButton("Load TLK");
		butLoadTLK.setEnabled(false);
		butLoadTLK.setPreferredSize(new Dimension(100, 23));
		JLabel tStatus = new JLabel(" ");

		c = new GridBagConstraints();
		c.gridx = 0;
		tlkPanel.add(tInputField, c);

		c.gridx = 1;
		tlkPanel.add(tBrowse, c);

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		tlkPanel.add(tStatus, c);

		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		tlkPanel.add(butLoadTLK, c);

		//Editor Panel
		editorPanel = new JPanel(new GridBagLayout());
		editorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mount.dlc info (default)"));
		JLabel priority = new JLabel("Mount Priority");
		priorityField = new JFormattedTextField(integerFormat);
		priorityField.setColumns(5);
		priorityField.setUI(new HintTextFieldUI("Priority"));
		priorityField.setToolTipText("Higher values will make same-name files (and coalesced) supercede DLCs with lower Mount.dlc priorities");

		Insets leftRightInsets = new Insets(0, 15, 0, 15);
		Insets noInsets = new Insets(0, 0, 0, 0);

		JLabel tlkId = new JLabel("DLC TLK ID");
		tlkIdField = new JFormattedTextField(integerFormat);
		tlkIdField.setColumns(5);
		tlkIdField.setUI(new HintTextFieldUI("TLK ID"));
		tlkIdField
				.setToolTipText("<html>This is the name of the DLC, in-game.<br>If this DLC is removed and it is required by the save files, this string will appear at the main menu error screen.<br>If a modified DLC authorizer is not used, this DLC will not load and will display this name at the main menu error screen.<br>You should always provide one for your DLC as this will provide feedback to end-users that something is wrong.</html>");

		JLabel mountFlags = new JLabel("DLC Mount Flags");

		mountFlagsCombobox = new JComboBox<MountFlag>();
		flagModel = new DefaultComboBoxModel<MountFlag>();
		flagModel.addElement(new MountFlag("SP | Requires DLC in save file", 9));
		flagModel.addElement(new MountFlag("SP/MP | Does not require DLC in save file", 28));
		flagModel.addElement(new MountFlag("MP (PATCH) | Loads in MP", 12));
		flagModel.addElement(new MountFlag("MP | Loads in MP", 20));
		flagModel.addElement(new MountFlag("MP | Loads in MP", 52));

		mountFlagsCombobox.setModel(flagModel);
		mountFlagsCombobox.setRenderer(new MountFlagCellRenderer());
		mountFlagsCombobox
				.setToolTipText("<html>Mount flags determine when this DLC is loaded and if it is required by save files.<br>Having a DLC load in MP will require all players to have the DLC installed or connections will be refused.<br>TESTPATCH ignores the Mount.dlc flag and always loads regardless at startup.</html>");
		c = new GridBagConstraints();
		c.gridx = 0;
		editorPanel.add(priority, c);

		c.gridy = 1;
		editorPanel.add(priorityField, c);

		c.gridx = 1;
		c.gridy = 0;
		c.insets = leftRightInsets;
		editorPanel.add(tlkId, c);

		c.gridy = 1;
		editorPanel.add(tlkIdField, c);

		c.insets = noInsets;
		c.gridx = 2;
		c.gridy = 0;
		editorPanel.add(mountFlags, c);

		c.gridy = 1;
		editorPanel.add(mountFlagsCombobox, c);
		/*
		 * c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.WEST;
		 * editorPanel.add(tStatus, c);
		 * 
		 * c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
		 * editorPanel.add(tLoad, c);
		 */
		//SAVE Panel
		JPanel savePanel = new JPanel(new GridBagLayout());
		savePanel.setBorder(new TitledBorder(new EtchedBorder(), "Save Mount.dlc file"));
		JTextField sInputField = new JTextField(55);
		sInputField.setUI(new HintTextFieldUI("Select location to save new Mount.dlc"));
		JButton sBrowse = new JButton("Browse...");
		sBrowse.setPreferredSize(new Dimension(100, 19));
		JButton butSave = new JButton("Save");
		butSave.setPreferredSize(new Dimension(100, 23));
		butSave.setEnabled(false);
		JLabel sStatus = new JLabel(" ");

		c = new GridBagConstraints();
		c.gridx = 0;
		savePanel.add(sInputField, c);

		c.gridx = 1;
		savePanel.add(sBrowse, c);

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		savePanel.add(sStatus, c);

		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		savePanel.add(butSave, c);

		JLabel label = new JLabel(
				"<html>Use this tool to create new or modify existing Mount.dlc files. To make a new one simply don't load an existing one.<br>Load a TLK file to verify your inputs are what you want.</html>");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		tStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		sStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		loadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tlkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		editorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		savePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(label);
		panel.add(loadPanel);
		panel.add(tlkPanel);
		panel.add(editorPanel);
		panel.add(savePanel);

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();

		butLoadMount.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadMount(lInputField.getText());
			}
		});
		
		butSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveMount(sInputField.getText());
			}
		});

		lBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dlcChooser = new JFileChooser();
				File tryDir = new File(lInputField.getText());
				if (tryDir.exists() && tryDir.isFile()) {
					dlcChooser.setCurrentDirectory(tryDir.getParentFile());
				} else {
					dlcChooser.setCurrentDirectory(new File("."));
				}
				dlcChooser.setSelectedFile(new File("Mount.dlc"));
				dlcChooser.setDialogTitle("Select Mount.dlc to load");
				dlcChooser.setAcceptAllFileFilterUsed(false);
				dlcChooser.setFileFilter(new FileNameExtensionFilter("ME3 DLC Mount File (Mount.dlc)", "dlc"));

				if (dlcChooser.showOpenDialog(MountFileEditorWindow.this) == JFileChooser.APPROVE_OPTION) {
					String chosenFile = dlcChooser.getSelectedFile().getAbsolutePath();
					lInputField.setText(chosenFile);
					lStatus.setText(" ");
					butLoadMount.setEnabled(true);
				}
			}
		});

		sBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveChooser = new JFileChooser();
				File tryFile = new File(sInputField.getText());
				File loadFile = new File(lInputField.getText());
				if (tryFile.exists() && tryFile.isFile()) {
					saveChooser.setCurrentDirectory(tryFile.getParentFile());
				} else if (loadFile.exists() && loadFile.isFile()) {
					saveChooser.setCurrentDirectory(loadFile.getParentFile());
				} else {
					saveChooser.setCurrentDirectory(new File("."));
				}
				saveChooser.setSelectedFile(new File("Mount.dlc"));
				saveChooser.setDialogTitle("Select where to save Mount.dlc");
				//binChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				//
				// disable the "All files" option.
				//
				saveChooser.setAcceptAllFileFilterUsed(false);
				saveChooser.setFileFilter(new FileNameExtensionFilter("ME3 DLC Mount File (Mount.dlc)", "dlc"));

				if (saveChooser.showSaveDialog(MountFileEditorWindow.this) == JFileChooser.APPROVE_OPTION) {
					String chosenFile = saveChooser.getSelectedFile().getAbsolutePath();
					sInputField.setText(chosenFile);
					butSave.setEnabled(true);
				} else {

				}
			}
		});

	}

	private void loadMount(String path) {
		if (!(new File(path).exists())) {
			return;
		}
		Path fpath = Paths.get(path);
		try {
			byte[] data = Files.readAllBytes(fpath);
			if (data.length != MOUNTDLC_LENGTH) {
				lStatus.setText("Invalid Mount.dlc file, must be " + MOUNTDLC_LENGTH + " bytes");
			}
			String hex = ResourceUtils.toHexFromBytes(data);

			//PRIORITY (16 BIT (SHORT))
			String mountPriority = hex.substring(PRIORITY_OFFSET, PRIORITY_OFFSET + 4);
			System.out.println("MOUNT PRIORITY HEX: " + mountPriority);
			System.out.println("As unsigned int: " + Integer.parseUnsignedInt(mountPriority, 16));
			int priorityVal = Integer.reverseBytes(Integer.parseUnsignedInt(mountPriority, 16));
			priorityVal = priorityVal >> 16;
			System.out.println("As reversed and shifted: "+priorityVal);

			priorityField.setText(Integer.toString(priorityVal));

			//TLK1/TLK2 (32 BIT EACH (INTEGER))
			String tlkID1 = hex.substring(TLKOFFSET_1, TLKOFFSET_1 + 8);
			String tlkID2 = hex.substring(TLKOFFSET_2, TLKOFFSET_2 + 8);
			System.out.println("TLK HEX " + tlkID1);
			int tlkId1Val = Integer.reverseBytes(Integer.parseUnsignedInt(tlkID1, 16));
			int tlkId2Val = Integer.reverseBytes(Integer.parseUnsignedInt(tlkID2, 16));
			if (tlkId1Val != tlkId2Val) {
				lStatus.setText("DLC Name (TLK ID) Values do not match in this Mount.dlc file.");
			}
			tlkIdField.setText(Integer.toString(tlkId1Val));

			//MOUNT FLAG (8-BIT)
			String mountFlag = hex.substring(MPSPFLAG_OFFSET, MPSPFLAG_OFFSET + 2);
			byte mountFlagVal = Byte.parseByte(mountFlag, 16);
			System.out.println("MOUNT FLAG: " + mountFlag);
			//priorityField.setText(Short.toString(priorityVal));
			for (int i = 0; i < flagModel.getSize(); i++) {
				MountFlag flag = flagModel.getElementAt(i);
				if (flag.getValue() == mountFlagVal) {
					mountFlagsCombobox.setSelectedItem(flag);
					break;
				}
			}

			editorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mount.dlc info (loaded)"));
		} catch (IOException e) {
			return;
		}
	}

	private void saveMount(String location) {
		String mountPriority = priorityField.getText();
		String tlkId = tlkIdField.getText();
		MountFlag flag = flagModel.getElementAt(mountFlagsCombobox.getSelectedIndex());
		
		String newData = DEFAULT_MOUNT_DATA;
		
		//MOUNT PRIORITY
		String preData = newData.substring(0,PRIORITY_OFFSET);
		String postData = newData.substring(PRIORITY_OFFSET+4);
		
		int priorityVal = Integer.reverseBytes(Integer.parseUnsignedInt(mountPriority, 16));
		priorityVal = priorityVal >> 16;
		String priorityString = Integer.toString(priorityVal);
		String hexToInput = ResourceUtils.padLeadingZeros(Integer.toHexString(priorityVal),4);
		System.out.println(mountPriority +" = r0x"+hexToInput);
		//newData = 0x
		/*
		//PRIORITY (16 BIT (SHORT))
		String mountPriority = hex.substring(PRIORITY_OFFSET, PRIORITY_OFFSET + 4);
		System.out.println("MOUNT PRIORITY BYTES: " + mountPriority);

		int priorityVal = Integer.reverseBytes(Integer.parseUnsignedInt(mountPriority, 16));
		priorityField.setText(Integer.toString(priorityVal));

		//TLK1/TLK2 (32 BIT EACH (INTEGER))
		String tlkID1 = hex.substring(TLKOFFSET_1, TLKOFFSET_1 + 8);
		String tlkID2 = hex.substring(TLKOFFSET_2, TLKOFFSET_2 + 8);
		System.out.println("TLK HEX " + tlkID1);
		int tlkId1Val = Integer.reverseBytes(Integer.parseUnsignedInt(tlkID1, 16));
		int tlkId2Val = Integer.reverseBytes(Integer.parseUnsignedInt(tlkID2, 16));
		if (tlkId1Val != tlkId2Val) {
			lStatus.setText("DLC Name (TLK ID) Values do not match in this Mount.dlc file.");
		}
		tlkIdField.setText(Integer.toString(tlkId1Val));

		//MOUNT FLAG (8-BIT)
		String mountFlag = hex.substring(MPSPFLAG_OFFSET, MPSPFLAG_OFFSET + 2);
		byte mountFlagVal = Byte.parseByte(mountFlag, 16);
		System.out.println("MOUNT FLAG: " + mountFlag);
		//priorityField.setText(Short.toString(priorityVal));
		for (int i = 0; i < flagModel.getSize(); i++) {
			MountFlag flag = flagModel.getElementAt(i);
			if (flag.getValue() == mountFlagVal) {
				mountFlagsCombobox.setSelectedItem(flag);
				break;
			}
		}

		editorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mount.dlc info (loaded)"));
*/	}

	/*
	 * private void generateMountByteArray() { // TODO Auto-generated method
	 * stub String byteText =
	 * "01 00 00 00 AC 02 00 00 C2 00 00 00 6B 00 03 00 8C 0A 00 00 00 00 00 00 1C 00 00 00 92 19 0B 00 92 19 0B 00 33 15 00 00 0B 00 00 00 5A 7B BD 26 DD 41 7E 49 9C C6 60 D2 58 72 78 EB 2E 2C 6A 06 13 0A E4 47 83 EA 08 F3 87 A0 E2 DA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
	 * ; String nospacebyteText =
	 * "01000000AC020000C20000006B0003008C0A0000000000001C00000092190B0092190B00331500000B0000005A7BBD26DD417E499CC660D2587278EB2E2C6A06130AE44783EA08F387A0E2DA0000000000000000000000000000000000000000000000000000000000000000"
	 * ; StringTokenizer strok = new StringTokenizer(byteText, " ");
	 * ArrayList<Byte> byteList = new ArrayList<Byte>(); int index = 0; while
	 * (strok.hasMoreElements()) { String tok = strok.nextToken();
	 * byteList.add(Byte.parseByte(tok,10));
	 * System.out.println(Integer.toHexString(index)+": "+tok); } }
	 */

	public static void main(String[] args) {
		MountFileEditorWindow.ISRUNNINGASMAIN = true;
		new MountFileEditorWindow();
	}
}
