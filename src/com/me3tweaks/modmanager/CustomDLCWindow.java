package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import com.me3tweaks.modmanager.objects.ModType;
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
		ModManager.debugLogger.writeMessage("Opening custom DLC window.");
		this.bioGameDir = bioGameDir;
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("Custom DLC Manager");
		setModal(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(700, 350));

		mountList = new ArrayList<MountFile>();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html><center>Installed Custom DLCs<br>Disabled DLCs start with an x. Mass Effect 3 only loads DLCs that start with DLC_.</center></html>");
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(infoLabel, BorderLayout.NORTH);

		File mainDlcDir = new File(ModManager.appendSlash(bioGameDir) + "DLC/");
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				System.out.println(name);
				return new File(current, name).isDirectory() && (name.toLowerCase().startsWith("xdlc_") || name.toLowerCase().startsWith("dlc_"));
			}
		});

		int datasize = 0;
		for (String dir : directories) {
			if (ModType.isKnownDLCFolder(dir)) {
				continue;
			}
			datasize++;
			String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + dir);
			String dlcName = "Unknown";
			File metaFile = new File(path + ModInstallWindow.CUSTOMDLC_METADATA_FILE);
			if (metaFile.exists()) {
				try {
					dlcName = FileUtils.readFileToString(metaFile);
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("Unable to read metadata file about customdlc:", e1);
				}
			} else {
				//try to lookup via 3rd party service
				String displayDir = dir;
				if (dir.toLowerCase().startsWith("xdlc_")) {
					displayDir = dir.substring(1);
				}
				dlcName = ME3TweaksUtils.getThirdPartyModName(displayDir);
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
				//String mount = MountFileEditorWindow.getMountDescription(mountFile);
				mount.setAssociatedModName(dlcName);
			}
			mountList.add(mount);
		}

		Collections.sort(mountList);
		Collections.reverse(mountList); //Descending
		//TABLE
		Object[][] data = new Object[datasize][5];
		for (int i = 0; i < datasize; i++) {
			MountFile mount = mountList.get(i);
			data[i][COL_FOLDER] = mount.getAssociatedDLCName();
			data[i][COL_NAME] = mount.getAssociatedModName();
			data[i][COL_MOUNT_PRIORITY] = mount.getMountPriority();
			data[i][COL_TOGGLE] = mount.getAssociatedDLCName().toLowerCase().startsWith("xdlc_") ? CustomDLCManagerToggleButtonColumn.STR_ENABLE : CustomDLCManagerToggleButtonColumn.STR_DISABLE;
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
					//Failed
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
					//Enable
					newname = dlcname.substring(1);
					disabling = false;
				} else {
					//Disable
					newname = "x" + dlcname;
					disabling = true;
				}

				File currentPath = new File(path);
				File toggledPath = new File(mainDlcDir.getAbsolutePath() + File.separator + newname);
				if (currentPath.renameTo(toggledPath)) {
					table.setValueAt(disabling ? CustomDLCManagerToggleButtonColumn.STR_ENABLE : CustomDLCManagerToggleButtonColumn.STR_DISABLE, modelRow, COL_TOGGLE);
					table.setValueAt(newname, modelRow, COL_FOLDER);
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

		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);
		CustomDLCManagerToggleButtonColumn buttonColumn2 = new CustomDLCManagerToggleButtonColumn(table, toggle, COL_TOGGLE);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_MOUNT_PRIORITY).setCellRenderer(centerRenderer);

		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);

		JLabel mpLabel = new JLabel(
				"<html><div style=\"text-align: center;\">Custom DLC will never authorize unless you use a DLC bypass.<br>You can check for Custom DLC conflicts using the Custom DLC Conflict Detector tool in the Mod Management menu.<br>Custom DLCs that have MP in their Mount Flag will make all players require that DLC in order to join the lobby.</div></html>",
				SwingConstants.CENTER);
		panel.add(mpLabel, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		updateRowHeights(table);
	}
	
	private void updateRowHeights(JTable table)
	{
	    for (int row = 0; row < table.getRowCount(); row++)
	    {
	        int rowHeight = table.getRowHeight();

	        for (int column = 0; column < table.getColumnCount(); column++)
	        {
	            Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
	            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
	        }

	        table.setRowHeight(row, rowHeight);
	    }
	}
}
