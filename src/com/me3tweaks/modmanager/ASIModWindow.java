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
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.ui.ButtonColumn;
import com.me3tweaks.modmanager.utilities.MD5Checksum;

public class ASIModWindow extends JDialog {

	protected static final int COL_ASIFILENAME = 0;
	protected static final int COL_HASH = 1;
	protected static final int COL_DESCRIPTION = 2;
	public static final int COL_UNINSTALL = 3;
	private String gamedir;
	private File asiDir;

	public ASIModWindow(String gamedir) {
		ModManager.debugLogger.writeMessage("Opening ASI window.");
		this.gamedir = gamedir;
		String asidir = ModManager.appendSlash(gamedir) + "Binaries/win32/asi";
		asiDir = new File(asidir);
		if (!asiDir.exists()) {
			asiDir.mkdirs();
		}
		
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("ASI Manager");
		setModal(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(300, 200));

		String[] files = asiDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return name.endsWith(".asi");
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html>Installed ASI Mods</html>",SwingConstants.CENTER);
		panel.add(infoLabel, BorderLayout.NORTH);

		int datasize = 0;
		//TABLE
		Object[][] data = new Object[datasize][3];
		for (int i = 0; i < files.length; i++) {
			String asifile = files[i];
			data[i][COL_ASIFILENAME] = FilenameUtils.getName(asifile);
			try {
				data[i][COL_HASH] = MD5Checksum.getMD5Checksum(asifile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				data[i][COL_HASH] = "Hash failure";

			}
			data[i][COL_DESCRIPTION] = "Loading...";
			data[i][COL_UNINSTALL] = "Uninstall";
		}

		Action delete = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				String path = ModManager.appendSlash(asiDir + File.separator+ table.getModel().getValueAt(modelRow, COL_ASIFILENAME));
				ModManager.debugLogger.writeMessage("Deleting installed ASI mod: "+path);
				FileUtils.deleteQuietly(new File(path));
				Object breakpoint = table.getModel();
				((DefaultTableModel) table.getModel()).removeRow(modelRow);
			}
		};
		String[] columnNames = { "ASI Mod", "Hash", "Description", "Uninstall"};
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_UNINSTALL;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_UNINSTALL);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.getColumnModel().getColumn(COL_UNINSTALL).setCellRenderer(centerRenderer);
		
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);
		
		JLabel mpLabel = new JLabel("<html><div style=\"text-align: center;\">ASI mods can run arbitrary code and should be used with caution.</div></html>",SwingConstants.CENTER);
		panel.add(mpLabel,BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5,5,5,5));
		add(panel);
		pack();
	}
}
