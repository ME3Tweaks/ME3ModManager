package com.me3tweaks.modmanager.moddesceditor;

import org.jdesktop.swingx.JXCollapsiblePane;

import javax.swing.*;
import java.awt.*;

public class MDEOfficialJobNewFile {

    private JXCollapsiblePane collapsiblePanel;

    public JXCollapsiblePane getPanel() {
        return collapsiblePanel;
    }
    public MDEOfficialJobNewFile(String sourcefile, String destfile, boolean readOnly) {
        setupPanel(sourcefile,destfile,readOnly);
    }

    private void setupPanel(String sourcefile, String destfile, boolean readOnly) {
        collapsiblePanel = new JXCollapsiblePane();
        JPanel panel = new JPanel(new GridBagLayout());
        collapsiblePanel.add(panel);

        JButton minusButton = new JButton("-");
        GridBagConstraints gridC = new GridBagConstraints();
        gridC.fill = GridBagConstraints.NONE;
        gridC.gridx = 0;
        gridC.weightx = 0;
        panel.add(minusButton, gridC);

        JLabel fileReplaceLabel = new JLabel(sourcefile);
        JLabel replacePathLabel = new JLabel(destfile);

        gridC.gridx = 1;

        panel.add(fileReplaceLabel, gridC);
        gridC.gridx = 2;
        gridC.weightx = 0.5;

        panel.add(replacePathLabel, gridC);
        gridC.fill = GridBagConstraints.HORIZONTAL;

        gridC.gridx = 3;
        gridC.weightx = 1;

        JCheckBox readOnlyC = new JCheckBox("Read only");
        readOnlyC.setSelected(readOnly);

        panel.add(readOnlyC, gridC);
    }
}