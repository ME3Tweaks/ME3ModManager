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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;

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
	private static int PRIORITY_OFFSET = 16;
	private static int MPSPFLAG_OFFSET = 24;
	private static int TLKOFFSET_1 = 28;
	private static int TLKOFFSET_2 = 32;
	private DefaultComboBoxModel<MountFlag> flagModel;
	private JLabel sStatus;
	private JButton butSave;
	private JTextField sInputField;

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
		tlkIdField.setColumns(8);
		tlkIdField.setUI(new HintTextFieldUI("TLK ID"));
		tlkIdField
				.setToolTipText("<html>This is the name of the DLC, in-game.<br>If this DLC is removed and it is required by the save files, this string will appear at the main menu error screen.<br>If a modified DLC authorizer is not used, this DLC will not load and will display this name at the main menu error screen.<br>You should always provide one for your DLC as this will provide feedback to end-users that something is wrong.</html>");

		JLabel mountFlags = new JLabel("DLC Mount Flags");

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
		sInputField = new JTextField(55);
		sInputField.setUI(new HintTextFieldUI("Select location to save new Mount.dlc"));
		JButton sBrowse = new JButton("Browse...");
		sBrowse.setPreferredSize(new Dimension(100, 19));
		butSave = new JButton("Save");
		butSave.setPreferredSize(new Dimension(100, 23));
		butSave.setEnabled(false);
		sStatus = new JLabel(" ");

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
				"<html>Use this tool to create new or modify existing Mount.dlc files.<br>Loading a file will load its values into the editor.</html>");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		tStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		sStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		loadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tlkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		editorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		savePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(label);
		panel.add(loadPanel);
		//panel.add(tlkPanel);
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
				if (validateInputs()) {
					saveMount(sInputField.getText());
				}
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

	/**
	 * Validates that inputs are in correct ranges Uses 1-size larger data types
	 * to account for maximum values
	 * 
	 * @return
	 */
	protected boolean validateInputs() {
		String mountPriority = priorityField.getText();
		String tlkId = tlkIdField.getText();

		try {
			int mountPriorityVal = Integer.parseInt(mountPriority);
			if (mountPriorityVal > Short.MAX_VALUE || mountPriorityVal < 0) {
				JOptionPane.showMessageDialog(this, "Mount priority must be between 1 and " + Short.MAX_VALUE, "Invalid Mount Priority",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Mount priority must be between 1 and " + Short.MAX_VALUE, "Invalid Mount Priority",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			long tlkIdVal = Long.parseLong(tlkId);
			if (tlkIdVal > Integer.MAX_VALUE || tlkIdVal < Integer.MIN_VALUE) {
				JOptionPane.showMessageDialog(this, "DLC TLK ID must be between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE,
						"Invalid DLC TLK ID", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "DLC TLK ID must be between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE,
					"Invalid DLC TLK ID", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
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

			//MOUNT PRIORITY
			byte[] priorityArray = new byte[] { 0, 0, data[PRIORITY_OFFSET + 1], data[PRIORITY_OFFSET] };
			ByteBuffer wrapped = ByteBuffer.wrap(priorityArray);
			int priorityVal = wrapped.getInt();
			priorityField.setText(Integer.toString(priorityVal));

			//TLK
			byte[] tlkID1Array = new byte[] { data[TLKOFFSET_1 + 3], data[TLKOFFSET_1 + 2], data[TLKOFFSET_1 + 1], data[TLKOFFSET_1] };
			wrapped = ByteBuffer.wrap(tlkID1Array);
			int tlkID1Val = wrapped.getInt();

			byte[] tlkID2Array = new byte[] { data[TLKOFFSET_2 + 3], data[TLKOFFSET_2 + 2], data[TLKOFFSET_2 + 1], data[TLKOFFSET_2] };
			wrapped = ByteBuffer.wrap(tlkID2Array);
			int tlkID2Val = wrapped.getInt();
			if (tlkID1Val != tlkID2Val) {
				lStatus.setText("DLC Name (TLK ID) Values do not match in this Mount.dlc file.");
			}
			tlkIdField.setText(Integer.toString(tlkID1Val));

			//MOUNT FLAG (8-BIT)
			byte mountFlagVal = data[MPSPFLAG_OFFSET];
			for (int i = 0; i < flagModel.getSize(); i++) {
				MountFlag flag = flagModel.getElementAt(i);
				if (flag.getValue() == mountFlagVal) {
					mountFlagsCombobox.setSelectedItem(flag);
					break;
				}
			}

			editorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mount.dlc info (loaded)"));
			
			sInputField.setText(path);
			butSave.setEnabled(true);
		} catch (IOException e) {
			return;
		}
	}

	private void saveMount(String location) {
		String mountPriority = priorityField.getText();
		String tlkId = tlkIdField.getText();
		MountFlag flag = flagModel.getElementAt(mountFlagsCombobox.getSelectedIndex());

		String newData = DEFAULT_MOUNT_DATA;
		byte[] data = DatatypeConverter.parseHexBinary(newData);
		System.out.println(DatatypeConverter.printHexBinary(data));

		//MOUNT PRIORITY
		int priorityVal = Integer.reverseBytes(Integer.parseUnsignedInt(mountPriority));
		priorityVal = (priorityVal >> 16) & 65535;
		byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(priorityVal).array();
		data[PRIORITY_OFFSET] = bytes[2]; //0 and 1 are data from size increase we don't need
		data[PRIORITY_OFFSET + 1] = bytes[3];

		//TLK ID1 & 2
		int tlkVal = Integer.reverseBytes(Integer.parseInt(tlkId));
		bytes = ByteBuffer.allocate(Integer.BYTES).putInt(tlkVal).array();
		data[TLKOFFSET_1] = bytes[0];
		data[TLKOFFSET_1 + 1] = bytes[1];
		data[TLKOFFSET_1 + 2] = bytes[2];
		data[TLKOFFSET_1 + 3] = bytes[3];
		data[TLKOFFSET_2] = bytes[0];
		data[TLKOFFSET_2 + 1] = bytes[1];
		data[TLKOFFSET_2 + 2] = bytes[2];
		data[TLKOFFSET_2 + 3] = bytes[3];
		data[MPSPFLAG_OFFSET] = flag.getValue();
		System.out.println(DatatypeConverter.printHexBinary(data));
		try {
			FileUtils.writeByteArrayToFile(new File(location), data);
			sStatus.setText("File saved.");
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to save Mount.dlc file:", e);
			sStatus.setText("Unable to save .dlc file. Check the debugging logs.");
		}
	}

	public static void main(String[] args) {
		MountFileEditorWindow.ISRUNNINGASMAIN = true;
		new MountFileEditorWindow();
	}

	public static String getMountDescription(File mountFile) {
		byte[] data;
		try {
			data = Files.readAllBytes(mountFile.toPath());
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to read mountfile:", e);
			return "I/O Exception";
		}
		if (data.length != MOUNTDLC_LENGTH) {
			return "Invalid Mount File (size)";
		}

		//MOUNT FLAG (8-BIT)
		byte mountFlagVal = data[MPSPFLAG_OFFSET];
		switch (mountFlagVal) {
		case 8:
			return "SP | Not required 0x"+ Integer.toString(8,16);
		case 9:
			return "SP | Required 0x"+ Integer.toString(9,16);
		case 28:
			return "SP&MP | Not required (SP) 0x"+ Integer.toString(28,16);
		case 12:
			return "MP (PATCH)| Loads in MP 0x"+ Integer.toString(12,16);
		case 20:
			return "MP | Loads in MP 0x"+ Integer.toString(20,16);
		case 52:
			return "MP | Loads in MP 0x"+ Integer.toString(52,16);
		default:
			return "Unknown Mount Flag: 0x"+Integer.toString(mountFlagVal, 16);
		
		}
	}
}
