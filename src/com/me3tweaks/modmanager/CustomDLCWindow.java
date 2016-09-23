package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
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

public class CustomDLCWindow extends JDialog {

	protected static final int COL_FOLDER = 0;
	protected static final int COL_NAME = 1;
	protected static final int COL_MOUNT = 2;
	public static final int COL_MOUNT_PRIORITY = 3;
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
		setMinimumSize(new Dimension(300, 200));

		mountList = new ArrayList<MountFile>();

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
			} else {
				//try to lookup via 3rd party service
				dlcName = ME3TweaksUtils.getThirdPartyModName(dir);
			}
			
			String pcConsole = path+"CookedPCConsole/";
			File mountFile = new File(pcConsole+"Mount.dlc");
			File sfarFile = new File(pcConsole+"Default.sfar");
			MountFile mount = new MountFile(mountFile.getAbsolutePath());
			ModManager.debugLogger.writeMessage("Found mount file: "+mount);
			mount.setAssociatedDLCName(dir);
			if (!mountFile.exists()){
				mount.setReason("No Mount.dlc file");
			} else if (!sfarFile.exists()){
				mount.setReason("No SFAR");
			} else {
				//String mount = MountFileEditorWindow.getMountDescription(mountFile);
				mount.setAssociatedModName(dlcName);
			}
			mountList.add(mount);
		}
		
		Collections.sort(mountList);

		//TABLE
		Object[][] data = new Object[datasize][5];
		for (int i = 0; i < datasize; i++) {
			MountFile mount = mountList.get(i);
			data[i][COL_FOLDER] = mount.getAssociatedDLCName();
			data[i][COL_NAME] = mount.getAssociatedModName();
			data[i][COL_MOUNT] = mount.getMountFlagString();
			data[i][COL_MOUNT_PRIORITY] = mount.getMountPriority();
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
		String[] columnNames = { "DLC Folder", "DLC Name", "Mount Flag", "Mount Priority","Delete DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_ACTION;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.getColumnModel().getColumn(COL_MOUNT_PRIORITY).setCellRenderer(centerRenderer);
		
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);
		
		JLabel mpLabel = new JLabel("<html><div style=\"text-align: center;\">Custom DLC will never authorize unless you use a DLC bypass.<br>You can check for Custom DLC conflicts using the Custom DLC Conflict Detector tool in the Mod Management menu.<br>Custom DLCs that have MP in their Mount Flag will make all players require that DLC in order to join the lobby.</div></html>",SwingConstants.CENTER);
		panel.add(mpLabel,BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5,5,5,5));
		add(panel);
		pack();
	}
}
