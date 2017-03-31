package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultRowSorter;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.ui.CustomDLCManagerToggleButtonColumn;

public class OfficialDLCWindow extends JDialog {

	protected static final int COL_FOLDER = 0;
	protected static final int COL_NAME = 1;
	public static final int COL_MOUNT_PRIORITY = 2;
	protected static final int COL_TOGGLE = 3;
	private String bioGameDir;
	private ArrayList<OfficialDLCInfo> installedOfficialDLCList = new ArrayList<OfficialDLCInfo>();
	boolean somethingChanged;

	public OfficialDLCWindow(String bioGameDir) {
		ModManager.debugLogger.writeMessage("Opening Official DLC window.");
		this.bioGameDir = bioGameDir;
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
				"Items in here are only for mod developers and users testing different game configurations.\nIf you don't know what you're doing, don't use this tool!",
				"DEVELOPERS ONLY", JOptionPane.WARNING_MESSAGE);
		setIconImages(ModManager.ICONS);
		setTitle("Official DLC Manager");
		setModal(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(700, 350));

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel(
				"<html><center>Installed Official DLCs<br>Disabled DLCs start with an x. Mass Effect 3 only loads DLCs that start with DLC_.</center></html>");
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

		for (String dir : directories) {
			String checkDir = dir;
			if (checkDir.startsWith("xDLC_")) {
				checkDir = checkDir.substring(1);
			}
			if (!ModType.isKnownDLCFolder(checkDir)) {
				continue;
			} else {
				OfficialDLCInfo odi = new OfficialDLCInfo(dir);
				installedOfficialDLCList.add(odi); //add unmodified
			}
		}

		//TABLE
		Collections.sort(installedOfficialDLCList);
		Collections.reverse(installedOfficialDLCList);

		Object[][] data = new Object[installedOfficialDLCList.size()][4];
		int datasize = installedOfficialDLCList.size();

		for (int i = 0; i < datasize; i++) {
			OfficialDLCInfo odi = installedOfficialDLCList.get(i);
			data[i][COL_FOLDER] = odi.getFolderName();
			data[i][COL_NAME] = odi.getDisplayName();
			data[i][COL_MOUNT_PRIORITY] = odi.getMountPriority();
			data[i][COL_TOGGLE] = odi.getFolderName().toLowerCase().startsWith("xdlc_") ? CustomDLCManagerToggleButtonColumn.STR_ENABLE
					: CustomDLCManagerToggleButtonColumn.STR_DISABLE;
		}

		Action toggle = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can enable or disable DLC.",
							"MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JTable table = (JTable) e.getSource();
				int modelRow = table.convertRowIndexToModel(Integer.valueOf(e.getActionCommand()));
				String dlcname = (String) table.getModel().getValueAt(modelRow, COL_FOLDER);
				String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator + dlcname);
				ModManager.debugLogger.writeMessage("Toggling Official DLC folder: " + path);
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
					somethingChanged = true;
				}
			}
		};
		String[] columnNames = { "DLC Folder", "DLC Name", "Mount Priority", "Toggle DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_TOGGLE;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == COL_MOUNT_PRIORITY) {
					return Short.class;
				}
				return getValueAt(0, columnIndex).getClass();
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(COL_NAME).setMinWidth(200);
		tcm.getColumn(COL_MOUNT_PRIORITY).setMaxWidth(150);
		tcm.getColumn(COL_MOUNT_PRIORITY).setMinWidth(150);

		CustomDLCManagerToggleButtonColumn buttonColumn2 = new CustomDLCManagerToggleButtonColumn(table, toggle, COL_TOGGLE);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_MOUNT_PRIORITY).setCellRenderer(centerRenderer);

		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);

		//JLabel mpLabel = new JLabel(
		//		"<html><div style=\"text-align: center;\">Custom DLC will never authorize unless you use a DLC bypass.<br>You can check for Custom DLC conflicts using the Custom DLC Conflict Detector tool in the Mod Management menu.</div></html>",
		//		SwingConstants.CENTER);
		//panel.add(mpLabel, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		updateRowHeights(table);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (somethingChanged) {
					JOptionPane.showMessageDialog(OfficialDLCWindow.this, "The DLC configuration has changed.\nMods will reload to update their configuration.",
							"Mods require reloading", JOptionPane.INFORMATION_MESSAGE);
					new ModManagerWindow(false);
				}
			}
		});
	}

	private class OfficialDLCInfo implements Comparable<OfficialDLCInfo> {
		private String initialFolderName, displayName;
		private int mountPriority;

		public OfficialDLCInfo(String dir) {
			initialFolderName = dir;
			String realName = dir;
			if (realName.startsWith("xDLC_")) {
				realName = realName.substring(1);
			}
			ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(realName);
			if (tpmi != null) {
				displayName = tpmi.getModname();
				mountPriority = tpmi.getMountPriority();
			} else {
				displayName = "Lookup failed";
				mountPriority = 0;
			}
		}

		public String getFolderName() {
			return initialFolderName;
		}

		public void setFolderName(String folderName) {
			this.initialFolderName = folderName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public int getMountPriority() {
			return mountPriority;
		}

		public void setMountPriority(int mountPriority) {
			this.mountPriority = mountPriority;
		}

		@Override
		public int compareTo(OfficialDLCInfo other) {
			return mountPriority > other.mountPriority ? +1 : mountPriority < other.mountPriority ? -1 : 0;
		}

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
