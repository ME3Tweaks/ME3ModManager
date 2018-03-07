package com.me3tweaks.modmanager.moddesceditor;

import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MDEOfficialJob {
    private String rawHeader;
    private String rawNewFiles;
    private String rawReplaceFiles;
    private String rawAddFiles;
    private String rawAddTargetFiles;
    private String rawAddReadOnlyTargetFiles;
    private String rawRemoveFiles;
    private String rawRequirementText;
    private String rawFolder;
    private JXCollapsiblePane collapsablePanel;
    private JTextField requirementLabel;

    public JXCollapsiblePane getPanel() {
        return collapsablePanel;
    }

    /**
     * Populated constructor for when you are loading a moddesc.ini official job.
     *
     * @param rawHeader                 Header name
     * @param rawFolder                 moddir value
     * @param rawNewFiles               newfiles list
     * @param rawReplaceFiles           replacefiles list
     * @param rawAddFiles               addfiles list
     * @param rawAddTargetFiles         addfilestargets list
     * @param rawAddReadOnlyTargetFiles addfilesreadonlytargets list
     * @param removeFiles               removefiles list
     * @param rawRequirementText        text to show user when the dlc is missing
     */
    public MDEOfficialJob(String rawHeader, String rawFolder, String rawNewFiles, String rawReplaceFiles, String rawAddFiles, String rawAddTargetFiles,
                          String rawAddReadOnlyTargetFiles, String removeFiles, String rawRequirementText) {
        this.rawHeader = rawHeader;
        this.rawFolder = rawFolder;
        this.rawNewFiles = rawNewFiles;
        this.rawReplaceFiles = rawReplaceFiles;
        this.rawAddFiles = rawAddFiles;
        this.rawAddTargetFiles = rawAddTargetFiles;
        this.rawAddReadOnlyTargetFiles = rawAddReadOnlyTargetFiles;
        this.rawRemoveFiles = removeFiles;
        this.rawRequirementText = rawRequirementText;
        setupPanel();
    }

    /**
     * Blank constructor for when creating a new MDEOfficialJob. Requires a header.
     */
    public MDEOfficialJob(String rawHeader, String rawFolder) {
        this.rawHeader = rawHeader;
        this.rawFolder = rawFolder;
        this.rawNewFiles = "";
        this.rawReplaceFiles = "";
        this.rawAddFiles = "";
        this.rawAddTargetFiles = "";
        this.rawAddReadOnlyTargetFiles = "";
        this.rawRemoveFiles = "";
        this.rawRequirementText = "";
        setupPanel();
    }

    public String getRawFolder() {
        return rawFolder;
    }

    public void setRawFolder(String rawFolder) {
        this.rawFolder = rawFolder;
    }

    public String getRawRequirementText() {
        return rawRequirementText;
    }

    public void setRawRequirementText(String rawRequirementText) {
        this.rawRequirementText = rawRequirementText;
    }

    public String getRawHeader() {
        return rawHeader;
    }

    public void setRawHeader(String rawHeader) {
        this.rawHeader = rawHeader;
    }

    public String getRawNewFiles() {
        return rawNewFiles;
    }

    public void setRawNewFiles(String rawNewFiles) {
        this.rawNewFiles = rawNewFiles;
    }

    public String getRawReplaceFiles() {
        return rawReplaceFiles;
    }

    public void setRawReplaceFiles(String rawReplaceFiles) {
        this.rawReplaceFiles = rawReplaceFiles;
    }

    public String getRawAddFiles() {
        return rawAddFiles;
    }

    public void setRawAddFiles(String rawAddFiles) {
        this.rawAddFiles = rawAddFiles;
    }

    public String getRawAddTargetFiles() {
        return rawAddTargetFiles;
    }

    public void setRawAddTargetFiles(String rawAddTargetFiles) {
        this.rawAddTargetFiles = rawAddTargetFiles;
    }

    public String getRawAddReadOnlyTargetFiles() {
        return rawAddReadOnlyTargetFiles;
    }

    public void setRawAddReadOnlyTargetFiles(String rawAddReadOnlyTargetFiles) {
        this.rawAddReadOnlyTargetFiles = rawAddReadOnlyTargetFiles;
    }

    public String getRawRemoveFiles() {
        return rawRemoveFiles;
    }

    public void setRawRemoveFiles(String rawRemoveFiles) {
        this.rawRemoveFiles = rawRemoveFiles;
    }

    private void setupPanel() {
        collapsablePanel = new JXCollapsiblePane();
        JPanel panel = new JPanel(new VerticalLayout());
        collapsablePanel.add(panel);

        // Task Header Panel
        JButton button = new JButton();

        JLabel taskHeaderLabel = new JLabel(getRawHeader() + " (in " + getRawFolder() + ")", SwingConstants.LEFT);
        taskHeaderLabel.setFont(taskHeaderLabel.getFont().deriveFont(16f));
        taskHeaderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        taskHeaderLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                button.doClick();
            }
        });

        JXPanel jobHeaderPanel = new JXPanel(new FlowLayout(FlowLayout.LEFT));
        jobHeaderPanel.add(button);
        jobHeaderPanel.add(taskHeaderLabel);

        // Task Details (Collapsable)
        JXPanel jobPanel = new JXPanel(new GridBagLayout());
        jobPanel.setBorder(new EmptyBorder(3, ModDescEditorWindow.SUBPANEL_INSET_LEFT, 3, 3));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // TASK DETAILS
        Insets columnRightSideInsets = new Insets(0, 0, 0, 10);
        // REPLACEMENTS
        {
            JXPanel replacementsListPanel = new JXPanel(new GridBagLayout());
            replacementsListPanel.setBorder(new EmptyBorder(3, ModDescEditorWindow.SUBPANEL_INSET_LEFT, 3, 3));
            GridBagConstraints gridC = new GridBagConstraints();

            JLabel replacementsHeader = new JLabel("File Replacements");
            replacementsHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
            jobPanel.add(replacementsHeader, gbc);
            gbc.gridy++;

            if (getRawNewFiles() != null && getRawReplaceFiles() != null) {
                StringTokenizer newStrok = new StringTokenizer(getRawNewFiles(), ";");
                StringTokenizer oldStrok = new StringTokenizer(getRawReplaceFiles(), ";");

                if (newStrok.countTokens() > 0) {
                    JLabel sourceHeader = new JLabel("New file");
                    JLabel replaceHeader = new JLabel("In-game path to replace");
                    sourceHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
                    replaceHeader.setFont(replacementsHeader.getFont().deriveFont(14f));
                    gridC.fill = GridBagConstraints.HORIZONTAL;
                    gridC.gridx = 1;
                    gridC.insets = columnRightSideInsets;
                    gridC.weightx = 0;
                    gridC.anchor = GridBagConstraints.WEST;
                    replacementsListPanel.add(sourceHeader, gridC);
                    gridC.gridx = 2;
                    gridC.weightx = 1;
                    replacementsListPanel.add(replaceHeader, gridC);
                    gridC.gridy++;

                    while (newStrok.hasMoreTokens()) {
                        String newFile = newStrok.nextToken();
                        String oldFile = oldStrok.nextToken();

                        JLabel fileReplaceLabel = new JLabel(newFile);
                        JLabel replacePathLabel = new JLabel(oldFile);

                        JButton minusButton = new JButton("-");

                        gridC.fill = GridBagConstraints.NONE;
                        gridC.gridy++;
                        gridC.gridx = 0;
                        gridC.weightx = 0;
                        replacementsListPanel.add(minusButton, gridC);

                        gridC.gridx = 1;
                        replacementsListPanel.add(fileReplaceLabel, gridC);

                        gridC.gridx = 2;
                        gridC.weightx = 1;
                        gridC.fill = GridBagConstraints.HORIZONTAL;
                        replacementsListPanel.add(replacePathLabel, gridC);
                    }
                } else {
                    //empty list, maybe just added.
                    JLabel noReplacements = new JLabel("No files are replaced in this job.");
                    replacementsListPanel.add(noReplacements, gridC);
                    gbc.gridy++;
                }
            } else {
                // no replace targets
                JLabel noReplacements = new JLabel("No files are replaced in this job.");
                replacementsListPanel.add(noReplacements, gridC);
                gridC.gridy++;
            }

            if (!getRawHeader().equals(ModTypeConstants.BINI)) {
                gridC.gridy++;
                gridC.gridx = 0;
                gridC.weightx = 0;
                gridC.anchor = GridBagConstraints.WEST;
                gridC.gridwidth = 3;
                gridC.fill = GridBagConstraints.NONE;
                JButton addReplacementFile = new JButton("Add replacement file to " + getRawHeader());
                replacementsListPanel.add(addReplacementFile, gridC);
            }

            jobPanel.add(replacementsListPanel, gbc);
        }
        if (!getRawHeader().equals(ModTypeConstants.BINI)) {
            // ADD FILES
            {
                JXPanel additionsListPanel = new JXPanel(new GridBagLayout());
                additionsListPanel.setBorder(new EmptyBorder(3, ModDescEditorWindow.SUBPANEL_INSET_LEFT, 3, 3));
                GridBagConstraints gridC = new GridBagConstraints();
                gbc.gridy++;
                JLabel newFilesHeader = new JLabel("New Additional Files");
                newFilesHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                jobPanel.add(newFilesHeader, gbc);
                gbc.gridy++;

                if (getRawAddFiles() != null && getRawAddTargetFiles() != null) {

                    // Get Raad-only
                    ArrayList<String> readOnlyFiles = new ArrayList<String>();
                    if (getRawAddReadOnlyTargetFiles() != null) {
                        StringTokenizer addTargetReadOnlyStrok = new StringTokenizer(getRawAddReadOnlyTargetFiles(), ";");

                        while (addTargetReadOnlyStrok.hasMoreTokens()) {
                            String readonlytarget = addTargetReadOnlyStrok.nextToken();
                            if (getRawHeader().equals(ModTypeConstants.BASEGAME)) {
                                readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, false);
                            } else {
                                readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, true);
                            }
                            readOnlyFiles.add(readonlytarget);
                        }
                    }
                    StringTokenizer addStrok = new StringTokenizer(getRawAddFiles(), ";");
                    StringTokenizer addTargetsStrok = new StringTokenizer(getRawAddTargetFiles(), ";");

                    /*
                     * gbc.gridy++;
                     */

                    JLabel sourceHeader = new JLabel("New file");
                    JLabel replaceHeader = new JLabel("In-game path to add to");
                    sourceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                    replaceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                    gridC.insets = columnRightSideInsets;
                    gridC.fill = GridBagConstraints.HORIZONTAL;
                    gridC.gridx = 1;
                    additionsListPanel.add(sourceHeader, gridC);
                    gridC.gridx = 2;
                    additionsListPanel.add(replaceHeader, gridC);
                    gridC.gridy++;
                    gridC.anchor = GridBagConstraints.WEST;

                    while (addStrok.hasMoreTokens()) {

                        JButton minusButton = new JButton("-");

                        gridC.fill = GridBagConstraints.NONE;
                        gridC.gridy++;
                        gridC.gridx = 0;
                        gridC.weightx = 0;
                        additionsListPanel.add(minusButton, gridC);

                        String newFile = addStrok.nextToken();
                        String oldFile = addTargetsStrok.nextToken();

                        JLabel fileReplaceLabel = new JLabel(newFile);
                        JLabel replacePathLabel = new JLabel(oldFile);

                        gridC.gridx = 1;

                        additionsListPanel.add(fileReplaceLabel, gridC);
                        gridC.gridx = 2;
                        gridC.weightx = 0;

                        additionsListPanel.add(replacePathLabel, gridC);
                        gridC.fill = GridBagConstraints.HORIZONTAL;

                        gridC.gridx = 3;
                        gridC.weightx = 1;

                        JCheckBox readOnly = new JCheckBox("Read only");
                        if (readOnlyFiles.contains(oldFile)) {
                            readOnly.setSelected(true);
                        }
                        additionsListPanel.add(readOnly, gridC);
                    }
                } else {
                    JLabel noAdditions = new JLabel("No files are added to the game by this job.", SwingConstants.LEFT);
                    gridC.gridy++;
                    gridC.gridx = 0;
                    gridC.weightx = 1;
                    gridC.anchor = GridBagConstraints.WEST;
                    gridC.gridwidth = 3;
                    gridC.fill = GridBagConstraints.NONE;
                    additionsListPanel.add(noAdditions, gridC);
                }

                gridC.gridy++;
                gridC.gridx = 0;
                gridC.weightx = 0;
                gridC.anchor = GridBagConstraints.WEST;
                gridC.gridwidth = 3;
                gridC.fill = GridBagConstraints.NONE;
                JButton addNewFile = new JButton("Add additional file to " + getRawHeader());
                additionsListPanel.add(addNewFile, gridC);

                // end add panel
                gbc.gridy++;
                jobPanel.add(additionsListPanel, gbc);
            }

            // MANUAL ALTFILES
            // ADD FILES
            {
                JXPanel additionsListPanel = new JXPanel(new GridBagLayout());
                additionsListPanel.setBorder(new EmptyBorder(3, ModDescEditorWindow.SUBPANEL_INSET_LEFT, 3, 3));
                GridBagConstraints gridC = new GridBagConstraints();
                gbc.gridy++;
                JLabel newFilesHeader = new JLabel("User selectable options");
                newFilesHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                jobPanel.add(newFilesHeader, gbc);
                gbc.gridy++;

                if (getRawAddFiles() != null && getRawAddTargetFiles() != null) {

                    // Get Raad-only
                    ArrayList<String> readOnlyFiles = new ArrayList<String>();
                    if (getRawAddReadOnlyTargetFiles() != null) {
                        StringTokenizer addTargetReadOnlyStrok = new StringTokenizer(getRawAddReadOnlyTargetFiles(), ";");

                        while (addTargetReadOnlyStrok.hasMoreTokens()) {
                            String readonlytarget = addTargetReadOnlyStrok.nextToken();
                            if (getRawHeader().equals(ModTypeConstants.BASEGAME)) {
                                readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, false);
                            } else {
                                readonlytarget = ResourceUtils.normalizeFilePath(readonlytarget, true);
                            }
                            readOnlyFiles.add(readonlytarget);
                        }
                    }
                    StringTokenizer addStrok = new StringTokenizer(getRawAddFiles(), ";");
                    StringTokenizer addTargetsStrok = new StringTokenizer(getRawAddTargetFiles(), ";");

                    /*
                     * gbc.gridy++;
                     */

                    JLabel sourceHeader = new JLabel("New file");
                    JLabel replaceHeader = new JLabel("In-game path to add to");
                    sourceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                    replaceHeader.setFont(newFilesHeader.getFont().deriveFont(14f));
                    gridC.insets = columnRightSideInsets;
                    gridC.fill = GridBagConstraints.HORIZONTAL;
                    gridC.gridx = 1;
                    additionsListPanel.add(sourceHeader, gridC);
                    gridC.gridx = 2;
                    additionsListPanel.add(replaceHeader, gridC);
                    gridC.gridy++;
                    gridC.anchor = GridBagConstraints.WEST;

                    while (addStrok.hasMoreTokens()) {

                        JButton minusButton = new JButton("-");

                        gridC.fill = GridBagConstraints.NONE;
                        gridC.gridy++;
                        gridC.gridx = 0;
                        gridC.weightx = 0;
                        additionsListPanel.add(minusButton, gridC);

                        String newFile = addStrok.nextToken();
                        String oldFile = addTargetsStrok.nextToken();

                        JLabel fileReplaceLabel = new JLabel(newFile);
                        JLabel replacePathLabel = new JLabel(oldFile);

                        gridC.gridx = 1;

                        additionsListPanel.add(fileReplaceLabel, gridC);
                        gridC.gridx = 2;
                        gridC.weightx = 0;

                        additionsListPanel.add(replacePathLabel, gridC);
                        gridC.fill = GridBagConstraints.HORIZONTAL;

                        gridC.gridx = 3;
                        gridC.weightx = 1;

                        JCheckBox readOnly = new JCheckBox("Read only");
                        if (readOnlyFiles.contains(oldFile)) {
                            readOnly.setSelected(true);
                        }
                        additionsListPanel.add(readOnly, gridC);
                    }
                } else {
                    JLabel noAdditions = new JLabel("No user selection options are available for this job.", SwingConstants.LEFT);
                    gridC.gridy++;
                    gridC.gridx = 0;
                    gridC.weightx = 1;
                    gridC.anchor = GridBagConstraints.WEST;
                    gridC.gridwidth = 3;
                    gridC.fill = GridBagConstraints.NONE;
                    additionsListPanel.add(noAdditions, gridC);
                }

                gridC.gridy++;
                gridC.gridx = 0;
                gridC.weightx = 0;
                gridC.anchor = GridBagConstraints.WEST;
                gridC.gridwidth = 3;
                gridC.fill = GridBagConstraints.NONE;
                JButton addNewFile = new JButton("Add user selectable option to " + getRawHeader());
                additionsListPanel.add(addNewFile, gridC);

                // end add panel
                gbc.gridy++;
                jobPanel.add(additionsListPanel, gbc);
            }
        }

        // REQUIREMENTS
        if (!getRawHeader().equals(ModTypeConstants.BASEGAME) && !getRawHeader().equals(ModTypeConstants.TESTPATCH)) {
            JLabel reasonLabel = new JLabel("Reason for this task");
            reasonLabel.setFont(reasonLabel.getFont().deriveFont(14f));

            gbc.gridy++;
            jobPanel.add(reasonLabel, gbc);

            String reason = "";
            if (getRawRequirementText() != null) {
                reason = getRawRequirementText();
            }
            requirementLabel = new JTextField(reason);
            requirementLabel.setUI(new HintTextFieldUI("Specify a reason"));

            gbc.gridy++;
            jobPanel.add(requirementLabel, gbc);
        }

        JXCollapsiblePane jobPane = new JXCollapsiblePane();
        jobPane.add(jobPanel);
        jobPane.setCollapsed(true);

        Action toggleAction = jobPane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
        button.setAction(toggleAction);
        button.setText("");

        panel.add(jobHeaderPanel);
        panel.add(jobPane);
    }
}
