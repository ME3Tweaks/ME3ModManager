package com.me3tweaks.modmanager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.RestoreMode;
import com.me3tweaks.modmanager.ui.NumReqButtonColumn;
import com.me3tweaks.modmanager.ui.SFARColumn;
import com.me3tweaks.modmanager.ui.SelectiveRestoreTableCellRenderer;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class SelectiveRestoreWindow extends JDialog {
    private static final int NUM_COLUMNS = 8;
    public static final int COL_HUMNAME = 0;
    public static final int COL_INTNAME = 1;
    public static final int COL_INSTALLED = 2;
    public static final int COL_BACKEDUP = 3;
    public static final int COL_MODIFIED = 4;
    public static final int COL_ACTION_SFAR = 5;
    public static final int COL_ACTION_UNPACKED = 6;
    public static final int COL_ACTION_DEL_UNPACKED = 7;

    JLabel infoLabel;
    // CheckBoxList dlcList;
    String consoleQueue[];
    boolean windowOpen = true;
    String currentText;
    String BioGameDir;
    JPanel checkBoxPanel;
    JButton backupButton;
    JButton basegameRestoreButton;
    JButton basegameFolderButton;
    JButton serverCoalescedButton;
    private Object[][] dlcTableData;
    private String[] headerArray;
    private JTable table;

    /**
     * Manually invoked backup window
     *
     * @param BioGameDir
     */
    public SelectiveRestoreWindow(String BioGameDir) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
        ModManager.debugLogger.writeMessage("==============STARTING THE SELECTIVE RESTORE WINDOW==============");
        // callingWindow.setEnabled(false);
        this.BioGameDir = BioGameDir;
        setupWindow();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Custom Restore");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(ModManager.ICONS);
        setPreferredSize(new Dimension(820, 450));

        JPanel rootPanel = new JPanel(new BorderLayout());
        infoLabel = new JLabel("Select data to restore");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(infoLabel);
        rootPanel.add(topPanel, BorderLayout.NORTH);

        //DLC TABLE DATA SOURCE
        headerArray = ModTypeConstants.getDLCHeaderNameArray();
        dlcTableData = new Object[headerArray.length][NUM_COLUMNS];

        //TABLE
        Action restoreSfar = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (ModManager.isMassEffect3Running()) {
                    JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can restore SFARs.", "MassEffect3.exe is running",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                String header = (String) table.getModel().getValueAt(modelRow, COL_HUMNAME);
                ModManager.debugLogger.writeMessage("==RESTORE SFAR CLICKED: " + header + "==");
                new RestoreFilesWindow(BioGameDir, header, RestoreMode.SFAR_HEADER_RESTORE);
                updateTable();
            }
        };
        Action restoreUnpacked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (ModManager.isMassEffect3Running()) {
                    JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can restore unpacked files.",
                            "MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                String header = (String) table.getModel().getValueAt(modelRow, COL_HUMNAME);
                ModManager.debugLogger.writeMessage("==RESTORE UNPACKED CLICKED: " + header + "==");
                new RestoreFilesWindow(BioGameDir, header, RestoreMode.UNPACKED_HEADER_RESTORE);
                updateTable();
            }
        };
        Action deleteUnpacked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (ModManager.isMassEffect3Running()) {
                    JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can delete unpacked files.",
                            "MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                String header = (String) table.getModel().getValueAt(modelRow, COL_HUMNAME);
                int result = JOptionPane.showConfirmDialog(SelectiveRestoreWindow.this,
                        "Delete unpacked files for " + header + "?\nThis will additionally delete any unpacked backups Mod Manager has made of these files.",
                        "Delete Unpacked Files", JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    ModManager.debugLogger.writeMessage("==DELETE UNPACKED CLICKED: " + header + "==");
                    new RestoreFilesWindow(BioGameDir, header, RestoreMode.UNPACKED_HEADER_DELETE);
                    updateTable();
                }
            }
        };
        String[] columnNames = {"DLC Name", "Internal Name", "Installed", "Backed Up", "SFAR Status", "Restore SFAR", "Restore Unpacked", "Delete Unpacked"};
        DefaultTableModel model = new DefaultTableModel(dlcTableData, columnNames);
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                Object value = table.getValueAt(row, column);
                switch (column) {
                    case COL_ACTION_SFAR:
                        return value != null && table.getValueAt(row, column).equals("RESTORE");
                    case COL_ACTION_UNPACKED:
                        return value != null && !table.getValueAt(row, column).equals(0);
                    case COL_ACTION_DEL_UNPACKED:
                        return value != null && !table.getValueAt(row, column).equals(0);
                }
                return column == -1; //TODO
            }
        };
        table.setDefaultRenderer(Object.class, new SelectiveRestoreTableCellRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        SFARColumn sfarCol = new SFARColumn(table, restoreSfar, COL_ACTION_SFAR);
        NumReqButtonColumn unpackedCol = new NumReqButtonColumn(table, restoreUnpacked, COL_ACTION_UNPACKED);
        NumReqButtonColumn deleteUnpackedCol = new NumReqButtonColumn(table, deleteUnpacked, COL_ACTION_DEL_UNPACKED);
        updateTable();
        JScrollPane scrollpane = new JScrollPane(table);
        rootPanel.add(scrollpane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
        JLabel alotStatusLabel = new JLabel(ModManager.isALOTInstalled(ModManagerWindow.GetBioGameDir()) ? "ALOT (texture mod) is installed" : "ALOT (texture mod) is not installed");
        alotStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        alotStatusLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        bottomPanel.add(alotStatusLabel);


        JPanel basegamePanel = new JPanel();
        basegamePanel.setLayout(new BoxLayout(basegamePanel, BoxLayout.LINE_AXIS));
        basegameRestoreButton = new JButton();
        basegameFolderButton = new JButton("Open backup folder");
        basegameFolderButton.setToolTipText("<html>Opens the Mod Manager file backup folder</html>");

        basegameRestoreButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new RestoreFilesWindow(BioGameDir, RestoreMode.BASEGAME);
            }
        });

        serverCoalescedButton = new JButton("Delete local balance changes");
        if (ModManager.areBalanceChangesInstalled(BioGameDir)) {
            serverCoalescedButton.setToolTipText("<html>Deletes the local balance changes file. Game will use the official server one.</html>");
            serverCoalescedButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ModManager.isMassEffect3Running()) {
                        JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can delete the local server balance changes file.",
                                "MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    new RestoreFilesWindow(BioGameDir, RestoreMode.BALANCE_CHANGES);
                    if (ModManager.areBalanceChangesInstalled(BioGameDir)) {
                        serverCoalescedButton.setEnabled(true);
                        serverCoalescedButton.setToolTipText("<html>Deletes the local balance changes file. Game will use the official server one.</html>");
                    } else {
                        serverCoalescedButton.setEnabled(false);
                        serverCoalescedButton.setToolTipText("<html>No balance change override file is installed</html>");
                    }
                }
            });
        } else {
            serverCoalescedButton.setEnabled(false);
            serverCoalescedButton.setToolTipText("<html>No balance change override file is installed.</html>");
        }
        basegameFolderButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceUtils.openDir(ModManager.appendSlash(new File(BioGameDir).getParent()) + "cmmbackup\\BIOGame\\");
            }
        });

        bottomPanel.add(basegamePanel);

        //CALCULATE NUM BACKED UP BASEGAME FILES
        String me3dir = (new File(BioGameDir)).getParent();
        String basegamebackupfolder = ModManager.appendSlash(me3dir) + "cmmbackup\\BIOGame\\";
        String blacklist = ModManager.appendSlash(me3dir) + "cmmbackup\\BIOGame\\DLC\\";
        File backupdir = new File(basegamebackupfolder);
        ModManager.debugLogger.writeMessage("Calculating num of basegame backup files: " + backupdir);
        int numfiles = 0;
        if (backupdir.exists()) {
            Collection<File> backupfiles = FileUtils.listFiles(backupdir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File f : backupfiles) {
                if (f.getAbsolutePath().toLowerCase().startsWith(blacklist.toLowerCase())) {
                    continue;
                }
                numfiles++;
            }
            basegameRestoreButton.setText("Restore " + numfiles + " basegame file" + ((numfiles != 1) ? "s" : ""));
            if (numfiles < 1) {
                basegameRestoreButton.setText("No basegame files backed up yet");
                basegameRestoreButton.setEnabled(false);
                basegameRestoreButton.setToolTipText("<html>Mod Manager has not backed up any files that mods have modified yet</html>");
            } else {
                basegameRestoreButton.setText("Restore " + numfiles + " basegame file" + ((numfiles != 1) ? "s" : ""));
                basegameRestoreButton.setEnabled(true);
                basegameRestoreButton.setToolTipText("<html>Mod Manager has backed up basegame files as mods were installed.<br>Click this button to restore them.</html>");
            }
        } else {
            basegameRestoreButton.setText("No basegame files backed up yet");
            basegameRestoreButton.setToolTipText("<html>Mod Manager has not backed up any files that mods have modified yet</html>");
            basegameRestoreButton.setEnabled(false);

            basegameFolderButton.setEnabled(false);
            basegameFolderButton.setToolTipText("<html>Backup folder not yet created<br>When files are backed up this folder will be automatically created</html>");
        }

        basegamePanel.add(Box.createHorizontalGlue());
        basegamePanel.add(serverCoalescedButton);
        basegamePanel.add(Box.createRigidArea(new Dimension(15, 15)));
        basegamePanel.add(basegameRestoreButton);
        basegamePanel.add(Box.createRigidArea(new Dimension(15, 15)));

        basegamePanel.add(basegameFolderButton);
        basegamePanel.add(Box.createHorizontalGlue());

        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(rootPanel);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                windowOpen = false;
                // methods will read this variable
            }
        });
        pack();
        setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
    }

    /**
     * Updates data in the dlcTableData table
     */
    protected void updateTable() {
        ModManager.debugLogger.writeMessage("===UPDATING CUSTOM RESTORE TABLE===");
        HashMap<String, Long> sizesMap = ModTypeConstants.getSizesMap();
        HashMap<String, String> nameMap = ModTypeConstants.getHeaderFolderMap();

        int rowIndex = -1;
        // Add and enable/disable DLC checkboxes and add to hashmap
        String vanillaBackupPath = VanillaBackupWindow.GetFullBackupPath(false);
        for (String dlcName : headerArray) {
            rowIndex++;
            table.setValueAt(ME3TweaksUtils.headerNameToShortDLCFolderName(dlcName), rowIndex, COL_INTNAME);
            table.setValueAt(dlcName, rowIndex, COL_HUMNAME);

            String filepath = ModManager.appendSlash(BioGameDir) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
            File dlcPath = new File(filepath);
            // Check if directory exists
            if (!dlcPath.exists()) {
                ModManager.debugLogger.writeMessage(dlcName + " DLC folder not present.");
                setDLCNotInstalled(rowIndex);
                continue;
            }

            // The folder exists.
            File mainSfar = new File(dlcPath + "\\Default.sfar");
            File testpatchSfar = new File(dlcPath + "\\Patch_001.sfar");
            File mainSfarbackup = new File(dlcPath + "\\Default.sfar.bak");
            File testpatchSfarbackup = new File(dlcPath + "\\Patch_001.sfar.bak");
            ModManager.debugLogger.writeMessage("Looking for Default.sfar, Patch_001.sfar in " + filepath);

            //SFAR exists.
            table.setValueAt(mainSfar.exists() || testpatchSfar.exists() ? "YES" : "NO", rowIndex, COL_INSTALLED);
            //check for backups

            //check for vanilla backup
            if (!mainSfarbackup.exists() && !testpatchSfarbackup.exists()) { //check for vanilla backup
                //no normal backup.
                if (vanillaBackupPath != null) {
                    //Attempt fetch from complete game backup
                    String backupPath = vanillaBackupPath + "\\BIOGame\\";
                    backupPath += ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
                    testpatchSfarbackup = new File(backupPath + "Patch_001.sfar");
                    mainSfarbackup = new File(backupPath + "Default.sfar");
                }
            }

            if (!mainSfarbackup.exists() && !testpatchSfarbackup.exists()) {
                table.setValueAt("NO", rowIndex, COL_BACKEDUP);
                table.setValueAt("NO BACKUP", rowIndex, COL_ACTION_SFAR);
            } else {
                table.setValueAt("YES", rowIndex, COL_BACKEDUP);
                table.setValueAt("RESTORE", rowIndex, COL_ACTION_SFAR);
            }

            if (mainSfar.exists()) {
                if (mainSfar.length() != sizesMap.get(dlcName)) {
                    table.setValueAt("MODIFIED" + (mainSfar.length() > sizesMap.get(dlcName) ? "+" : "-"), rowIndex, COL_MODIFIED);
                } else {
                    table.setValueAt("ORIGINAL", rowIndex, COL_MODIFIED);
                }
            } else if (testpatchSfar.exists()) {
                if (testpatchSfar.length() != sizesMap.get(dlcName) && testpatchSfar.length() != ModTypeConstants.TESTPATCH_16_SIZE) {
                    table.setValueAt("MODIFIED" + (testpatchSfar.length() > sizesMap.get(dlcName) ? "+" : "-"), rowIndex, COL_MODIFIED);
                } else {
                    table.setValueAt("ORIGINAL", rowIndex, COL_MODIFIED);
                }
            } else {
                table.setValueAt("N/A", rowIndex, COL_MODIFIED);
            }

            //CALCULATE UNPACKED
            //load Basegame DB
            String me3dir = (new File(BioGameDir)).getParent();
            String dlcbackupfolder = ModManager.appendSlash(me3dir) + "cmmbackup\\BIOGame\\DLC\\";
            String specificdlcbackupfolder = dlcbackupfolder + nameMap.get(dlcName);
            File backupdir = new File(specificdlcbackupfolder);
            ModManager.debugLogger.writeMessage("Calculating num of unpacked files: " + backupdir);
            if (backupdir.exists()) {
                Collection<File> backupfiles = FileUtils.listFiles(backupdir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                table.setValueAt(backupfiles.size(), rowIndex, COL_ACTION_UNPACKED);

            } else {
                table.setValueAt(0, rowIndex, COL_ACTION_UNPACKED);
            }
            ArrayList<String> unpackedFiles = getUnpackedFilesList(new String[]{dlcName});
            table.setValueAt(unpackedFiles.size(), rowIndex, COL_ACTION_DEL_UNPACKED);
            continue;
        }
        //else {
        //	ModManager.debugLogger.writeMessage(dlcName + " folder exists, but SFAR is not present.");
        //		setDLCNotInstalled(rowIndex);
        //		continue;
        //	}

        ModManager.debugLogger.writeMessage("===END OF UPDATING CUSTOM RESTORE TABLE===");
    }

    private void setDLCNotInstalled(int i) {
        table.setValueAt("NO", i, COL_INSTALLED);
        table.setValueAt("N/A", i, COL_MODIFIED);
        table.setValueAt(0, i, COL_ACTION_UNPACKED);
        table.setValueAt(0, i, COL_ACTION_DEL_UNPACKED);
        table.setValueAt("N/A", i, COL_MODIFIED);
        table.setValueAt("N/A", i, COL_BACKEDUP);
    }

    public static ArrayList<String> getUnpackedFilesList(String[] dlcHeaders) {
        ArrayList<String> filepaths = new ArrayList<String>();

        for (String header : dlcHeaders) {
            String dlcFolderPath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(header));
            File dlcDirectory = new File(dlcFolderPath);

            if (dlcDirectory.exists()) {
                try {
                    Files.walk(Paths.get(dlcDirectory.getAbsolutePath()))
                            .filter(p -> !p.toString().endsWith(".sfar") && !p.toString().endsWith(".bak") && !p.toFile().isDirectory()).forEach(p -> filepaths.add(p.toString()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    ModManager.debugLogger.writeErrorWithException("ERROR LISTING UNPACKED FILES FOR DLC: " + header, e);
                }
                /*
                 * File files[] = dlcDirectory.listFiles(); for (File file :
                 * files) { if (file.isFile()) { String filepath =
                 * file.getAbsolutePath(); if (!filepath.endsWith(".sfar") &&
                 * !filepath.endsWith(".bak")) { filepaths.add(filepath); } } }
                 */
                //Find Movies folder
                File moviesFolder = new File(ModManager.appendSlash(dlcDirectory.getParent()) + "Movies\\");
                if (moviesFolder.exists()) {
                    File movieFiles[] = moviesFolder.listFiles();
                    for (File file : movieFiles) {
                        if (file.isFile()) {
                            String filepath = file.getAbsolutePath();
                            if (!filepath.endsWith(".sfar") && !filepath.endsWith(".bak")) {
                                ModManager.debugLogger.writeMessage("Unpacked file: " + filepath);
                                filepaths.add(filepath);
                            }
                        }
                    }
                    filepaths.add(moviesFolder.getAbsolutePath());
                }
                //find PCConsoleTOC.bin for it
                File dlcConsoleTOC = new File(ModManager.appendSlash(dlcDirectory.getParent()) + "PCConsoleTOC.bin");
                if (dlcConsoleTOC.exists()) {
                    //ModManager.debugLogger.writeMessage("Unpacked file: " + dlcConsoleTOC.getAbsolutePath());
                    filepaths.add(dlcConsoleTOC.getAbsolutePath());
                }
            }
        }
        return filepaths;
    }
}
