package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.ui.ButtonColumn;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Shows conflicts between custom dlc mods.
 * 
 * @author Michael
 *
 */
public class CustomDLCConflictWindow extends JDialog {

	private static final int COL_FILENAME = 0;
	private static final int COL_SUPERCEDING = 1;
	private static final int COL_SUPERCEDED = 2;

	public CustomDLCConflictWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setPreferredSize(new Dimension(500, 500));
		setTitle("Custom DLC Conflicts");

		// TODO Auto-generated method stub
		//JTextPane tp = new JTextPane();
		String biogameDirectory = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		ArrayList<String> installedDLCs = ModManager.getInstalledDLC(biogameDirectory);
		ArrayList<CustomDLC> customDLCs = new ArrayList<CustomDLC>();
		for (String dlc : installedDLCs) {
			File mountFile = new File(biogameDirectory + "DLC/" + dlc + File.separator + " CookedPCConsole/Mount.dlc");
			if (!ModType.isKnownDLCFolder(dlc) && dlc.startsWith("DLC_") && mountFile.exists()) {
				customDLCs.add(new CustomDLC(new MountFile(mountFile.getAbsolutePath()), dlc));
			}
		}

		//get conflicts, create table
		
		HashMap<String, ArrayList<CustomDLC>> items = ModManager.getCustomDLCConflicts(customDLCs, biogameDirectory + "DLC/");
		int datasize = items.entrySet().size();
		Object[][] data = new Object[datasize][3];
		int i = 0;
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : items.entrySet()) {
			String key = entry.getKey();
			ArrayList<CustomDLC> value = entry.getValue();
			
			//write values to table data
			data[i][COL_FILENAME] = key;
			data[i][COL_SUPERCEDING] = value.get(value.size());
			String superceeded = "";
			for (int x = 0; x < value.size() - 2; x++) {
				superceeded += value.get(x) + " ";
			}
			
			data[i][COL_SUPERCEDED] = superceeded;
		}

		String[] columnNames = { "Filename", "Superceding file", "Superceeded DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		JScrollPane scrollpane = new JScrollPane(table);
		add(scrollpane);
		pack();
	}
}
