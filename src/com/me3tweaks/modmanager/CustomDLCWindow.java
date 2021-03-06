package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.MetaCMM;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.ui.ButtonColumn;
import com.me3tweaks.modmanager.ui.CustomDLCManagerToggleButtonColumn;

public class CustomDLCWindow extends JDialog {

	protected static final int COL_FOLDER = 0;
	protected static final int COL_NAME = 1;
	public static final int COL_MOUNT_PRIORITY = 2;
	protected static final int COL_TOGGLE = 3;
	protected static final int COL_ACTION = 4;
	private String bioGameDir;
	private ArrayList<MountFile> mountList;

	public CustomDLCWindow(String bioGameDir) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening custom DLC window.");
		this.bioGameDir = bioGameDir;
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("Custom DLC Manager");
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(700, 350));

		mountList = new ArrayList<MountFile>();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html><center>Installed Custom DLCs<br>Disabled DLCs start with an x. Mass Effect 3 only loads DLCs that start with DLC_.</center></html>");
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

		panel.add(infoLabel, BorderLayout.NORTH);

		File mainDlcDir = new File(ModManager.appendSlash(bioGameDir) + "DLC/");
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory() && (name.toLowerCase().startsWith("xdlc_") || name.toLowerCase().startsWith("dlc_"));
			}
		});

		int datasize = 0;
		for (String dir : directories) {
			String displayDir = dir;
			if (dir.toLowerCase().startsWith("xdlc_")) {
				displayDir = dir.substring(1);
			}

			if (ModTypeConstants.isKnownDLCFolder(displayDir)) {
				continue;
			}
			datasize++;
			String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + dir);
			String dlcName = "Unknown";
			File metaFile = new File(path + ModInstallWindow.CUSTOMDLC_METADATA_FILE);
			if (metaFile.exists()) {
				MetaCMM meta = new MetaCMM(metaFile);
				dlcName = meta.getModName();
				if (meta.getModVersion() != null) {
					dlcName += " " + meta.getModVersion();
				}
			} else {
				// try to lookup via 3rd party service

				dlcName = ME3TweaksUtils.getThirdPartyModName(displayDir, false);
			}

			String pcConsole = path + "CookedPCConsole/";
			File mountFile = new File(pcConsole + "Mount.dlc");
			File sfarFile = new File(pcConsole + "Default.sfar");
			MountFile mount = new MountFile(mountFile.getAbsolutePath());
			ModManager.debugLogger.writeMessage("Found mount file: " + mount);
			mount.setAssociatedDLCName(dir);
			if (!mountFile.exists()) {
				mount.setReason("No Mount.dlc file");
			} else if (!sfarFile.exists()) {
				mount.setReason("No SFAR");
			} else {
				// String mount =
				// MountFileEditorWindow.getMountDescription(mountFile);
				mount.setAssociatedModName(dlcName);
			}
			mountList.add(mount);
		}

		Collections.sort(mountList);
		Collections.reverse(mountList); // Descending
		// TABLE
		Object[][] data = new Object[datasize][5];
		for (int i = 0; i < datasize; i++) {
			MountFile mount = mountList.get(i);
			data[i][COL_FOLDER] = mount.getAssociatedDLCName();
			data[i][COL_NAME] = mount.getAssociatedModName();
			data[i][COL_MOUNT_PRIORITY] = mount.getMountPriority();
			data[i][COL_TOGGLE] = mount.getAssociatedDLCName().toLowerCase().startsWith("xdlc_") ? CustomDLCManagerToggleButtonColumn.STR_ENABLE
					: CustomDLCManagerToggleButtonColumn.STR_DISABLE;
			data[i][COL_ACTION] = "Delete DLC";
		}

		Action delete = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can delete DLC.", "MassEffect3.exe is running",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + table.getModel().getValueAt(modelRow, COL_FOLDER));
				ModManager.debugLogger.writeMessage("Deleting Custom DLC folder: " + path);
				if (FileUtils.deleteQuietly(new File(path))) {
					((DefaultTableModel) table.getModel()).removeRow(modelRow);
				} else {
					// Failed
					ModManager.debugLogger.writeError("Failed to delete folder!");
				}
			}
		};

		Action toggle = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can enable or disable DLC.",
							"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				String dlcname = (String) table.getModel().getValueAt(modelRow, COL_FOLDER);
				String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + dlcname);
				ModManager.debugLogger.writeMessage("Toggling Custom DLC folder: " + path);
				String newname = "MM_PLACEHOLDER";
				boolean disabling = false;
				if (dlcname.toLowerCase().startsWith("x")) {
					// Enable
					newname = dlcname.substring(1);
					disabling = false;
				} else {
					// Disable
					newname = "x" + dlcname;
					disabling = true;
				}

				File currentPath = new File(path);
				File toggledPath = new File(mainDlcDir.getAbsolutePath() + File.separator + newname);
				if (currentPath.renameTo(toggledPath)) {
					DefaultTableModel tm = (DefaultTableModel) table.getModel();
					tm.setValueAt(disabling ? CustomDLCManagerToggleButtonColumn.STR_ENABLE : CustomDLCManagerToggleButtonColumn.STR_DISABLE, modelRow, COL_TOGGLE);
					tm.setValueAt(newname, modelRow, COL_FOLDER);
				}
			}
		};
		String[] columnNames = { "DLC Folder", "DLC Name", "Mount Priority", "Toggle DLC", "Delete DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_ACTION || column == COL_TOGGLE;
			}
		};
		table.setRowHeight(30);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);
		CustomDLCManagerToggleButtonColumn buttonColumn2 = new CustomDLCManagerToggleButtonColumn(table, toggle, COL_TOGGLE);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_MOUNT_PRIORITY).setCellRenderer(centerRenderer);
		table.setAutoCreateRowSorter(true);
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JLabel alotStatusLabel = new JLabel(ModManager.isALOTInstalled(ModManagerWindow.GetBioGameDir()) ? "ALOT (texture mod) is installed" : "ALOT (texture mod) is not installed");
        alotStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        alotStatusLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        bottomPanel.add(alotStatusLabel);

		// bottomPanel.setLayout(new Flow);
		String bypassmessage = "Your game has a DLC bypass installed. Custom DLC will be able to authorize.";
		if (!ModManager.hasKnownDLCBypass(bioGameDir)) {
			bypassmessage = "Your game has does not have a DLC bypass installed. Custom DLC will not authorize.";
		}
		JLabel mpLabel = new JLabel("<html><div style=\"text-align: center;\">" + bypassmessage + "</div></html>", SwingConstants.CENTER);
		mpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel toggleAllPanel = new JPanel();
		JButton viewConflictsButton = new JButton("View conflicts");
		JButton enableAllButton = new JButton("Enable All Custom DLC");
		JButton disableAllButton = new JButton("Disable All Custom DLC");

		ActionListener toggleAll = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can enable or disable DLC.",
							"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
					return;
				}
				DefaultTableModel tm = (DefaultTableModel) table.getModel();
				boolean enabling = e.getSource() == enableAllButton;

				for (int row = 0; row < tm.getRowCount(); row++) {
					//for(int col = 0;col < dm2.getColumnCount();col++) {
					//	System.out.println(dm2.getValueAt(row, col));
					//}
					String dlcname = (String) table.getModel().getValueAt(row, COL_FOLDER);
					String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + dlcname);
					if (dlcname.toLowerCase().startsWith("x") && !enabling) {
						continue; //skip
					}
					if (dlcname.toUpperCase().startsWith("DLC_") && enabling) {
						continue; //skip
					}

					String newname = enabling ? dlcname.substring(1) : "x" + dlcname;

					File currentPath = new File(path);
					File toggledPath = new File(mainDlcDir.getAbsolutePath() + File.separator + newname);
					if (currentPath.renameTo(toggledPath)) {
						tm.setValueAt(!enabling ? CustomDLCManagerToggleButtonColumn.STR_ENABLE : CustomDLCManagerToggleButtonColumn.STR_DISABLE, row, COL_TOGGLE);
						tm.setValueAt(newname, row, COL_FOLDER);
					}
				}

			}

		};

		viewConflictsButton.setToolTipText("View conflicts between Custom DLCs (and GUI mods)");
		viewConflictsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		viewConflictsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new CustomDLCConflictWindow();
			}
		});

		enableAllButton.setToolTipText("Enable all Custom DLC listed in this window");
		enableAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		enableAllButton.addActionListener(toggleAll);
		disableAllButton.addActionListener(toggleAll);

		disableAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		disableAllButton.setToolTipText("Disable all Custom DLC listed in this window");
		toggleAllPanel.setLayout(new BoxLayout(toggleAllPanel, BoxLayout.X_AXIS));
		toggleAllPanel.add(Box.createHorizontalGlue());
		toggleAllPanel.add(viewConflictsButton);
		toggleAllPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		toggleAllPanel.add(enableAllButton);
		toggleAllPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		toggleAllPanel.add(disableAllButton);
		toggleAllPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		toggleAllPanel.add(Box.createHorizontalGlue());

		bottomPanel.add(toggleAllPanel);
		bottomPanel.add(mpLabel);

		panel.add(bottomPanel, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		updateRowHeights(table);
	}

	private void updateRowHeights(JTable table) {
		for (int row = 0; row < table.getRowCount(); row++) {
			int rowHeight = table.getRowHeight();

			for (int column = 0; column < table.getColumnCount(); column++) {
				Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			table.setRowHeight(row, rowHeight);
		}
	}
}
