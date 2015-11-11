package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.ui.ButtonColumn;

public class CustomDLCWindow extends JDialog {

	protected static final int COL_FOLDER = 0;
	protected static final int COL_NAME = 1;
	protected static final int COL_MOUNT = 2;
	protected static final int COL_ACTION = 3;
	private ArrayList<String> nameList;
	private ArrayList<String> folderList;
	private String bioGameDir;
	private ArrayList<String> mountList;

	public CustomDLCWindow(String bioGameDir) {
		this.bioGameDir = bioGameDir;
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("Custom DLCs");
		setModal(true);
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(300, 200));

		nameList = new ArrayList<String>();
		folderList = new ArrayList<String>();
		mountList = new ArrayList<String>();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html>Installed Custom DLCs</html>",SwingConstants.CENTER);
		panel.add(infoLabel, BorderLayout.NORTH);

		File mainDlcDir = new File(ModManager.appendSlash(bioGameDir) + "DLC/");
		String[] directories = mainDlcDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		int datasize = 0;
		for (String dir : directories) {
			if (ModType.isKnownDLCFolder(dir)) {
				continue;
			}
			datasize ++;
			String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath()+ File.separator + dir);
			String dlcName = "Unknown";
			File metaFile = new File(path + ModInstallWindow.CUSTOMDLC_METADATA_FILE);
			if (metaFile.exists()) {
				try {
					dlcName = FileUtils.readFileToString(metaFile);
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("Unable to read metadata file about customdlc:", e1);
				}
			}
			
			String pcConsole = path+"CookedPCConsole/";
			File mountFile = new File(pcConsole+"Mount.dlc");
			File sfarFile = new File(pcConsole+"Default.sfar");
			
			if (!mountFile.exists()){
				dlcName = "No Mount.dlc file";
				mountList.add(dlcName);
			} else if (!sfarFile.exists()){
				dlcName = "No Default.sfar file";
				mountList.add("Not checked");
			} else {
				String mount = MountFileEditorWindow.getMountDescription(mountFile);
				mountList.add(mount);
			}
			folderList.add(dir);
			nameList.add(dlcName);
		}

		//TABLE
		Object[][] data = new Object[datasize][4];
		for (int i = 0; i < datasize; i++) {
			data[i][COL_FOLDER] = folderList.get(i);
			data[i][COL_NAME] = nameList.get(i);
			data[i][COL_MOUNT] = mountList.get(i);
			data[i][COL_ACTION] = "Delete DLC";
		}

		Action delete = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				String path = ModManager.appendSlash(mainDlcDir.getAbsolutePath() + File.separator+ table.getModel().getValueAt(modelRow, COL_FOLDER));
				ModManager.debugLogger.writeMessage("Deleting Custom DLC folder: "+path);
				FileUtils.deleteQuietly(new File(path));
				Object breakpoint = table.getModel();
				((DefaultTableModel) table.getModel()).removeRow(modelRow);
			}
		};
		String[] columnNames = { "DLC Folder", "DLC Name", "DLC Mount Flag","Delete DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_ACTION;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);
		JScrollPane scrollpane = new JScrollPane(table);

		panel.add(scrollpane, BorderLayout.CENTER);
		
		JLabel mpLabel = new JLabel("<html><div style=\"text-align: center;\">Custom DLC will never authorize unless you use a DLC bypass.<br>DLC that have MP in their Mount Flag will make all players require that DLC.</div></html>",SwingConstants.CENTER);
		panel.add(mpLabel,BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5,5,5,5));
		add(panel);
		pack();
	}
}
