package com.me3tweaks.modmanager.moddesceditor;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class MDEOfficialTaskSelector extends JDialog {
    private final ModDescEditorWindow windowRef;
    private int result = -1;

    public MDEOfficialTaskSelector(ModDescEditorWindow windowRef) {
        this.windowRef = windowRef;
        setupWindow();
    }

    private void setupWindow() {
        this.setTitle("Add Custom DLC Folder for installation");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setIconImages(ModManager.ICONS);
        this.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        this.setMinimumSize(new Dimension(300, 300));

        ArrayList<MDEOfficialJob> jobs = windowRef.getOfficialJobs();

        JPanel chooserPanel = new JPanel(new BorderLayout());
        chooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        String[] headerArray = ModTypeConstants.getDLCHeaderNameArray();
        ArrayList<String> headerList = new ArrayList<>(Arrays.asList(headerArray));
        headerList.add(0,"BASEGAME"); //add basegame header
        //check sfar size.

        int i = 0;
        // Add and enable/disable DLC checkboxes and add to hashmap
        JPanel MPHeadersPanel = new JPanel(new VerticalLayout());
        JPanel SPHeadersPanel = new JPanel(new VerticalLayout());
        for (String dlcName : headerList) {
            JCheckBox checkbox = new JCheckBox(dlcName);
            checkbox.setVerticalAlignment(SwingConstants.CENTER);
            JXCollapsiblePane backupPane = new JXCollapsiblePane();
            backupPane.add(checkbox);

            JButton button = new JButton(dlcName);

            if (i < 9) {
                MPHeadersPanel.add(button);
            } else {
                SPHeadersPanel.add(button);
            }
            final int y = i; //final for passing into anonymous class
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    result = y;
                    dispose();
                }
            });
            for (MDEOfficialJob job : jobs){
                if (job.getRawHeader().equals(dlcName)) {
                    button.setEnabled(false);
                    break;
                }
            }
            i++;
        }
        JPanel listPanel = new JPanel(new HorizontalLayout());
        listPanel.add(MPHeadersPanel);
        listPanel.add(SPHeadersPanel);
        chooserPanel.add(listPanel, BorderLayout.CENTER);

        JButton chooseButton = new JButton("Cancel adding job");
        chooserPanel.add(chooseButton, BorderLayout.SOUTH);

        add(chooserPanel);
        pack();
        setLocationRelativeTo(windowRef);
    }

    public int getResult() {
        return result;
    }
}
