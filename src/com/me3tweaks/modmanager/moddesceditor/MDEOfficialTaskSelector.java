package com.me3tweaks.modmanager.moddesceditor;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.ModTypeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MDEOfficialTaskSelector extends JDialog {
    private final ModDescEditorWindow windowRef;

    public MDEOfficialTaskSelector(ModDescEditorWindow windowRef) {
        this.windowRef = windowRef;
        setupWindow();
    }

    private void setupWindow(){
        this.setTitle("Add Custom DLC Folder for installation");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setIconImages(ModManager.ICONS);
        this.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        this.setMinimumSize(new Dimension(300, 300));
        ArrayList<String> headers = new ArrayList<>(Arrays.asList(ModTypeConstants.getDLCHeaderNameArray()));
        for (MDEOfficialJob oj : windowRef.getOfficialJobs()) {
            headers.remove(oj.getRawHeader());
        }

        DefaultListModel dlm = new DefaultListModel();
        for(String p : headers ){
            dlm.addElement(p.toString());
        }
        JList choiceList = new JList(dlm);
        JPanel chooserPanel = new JPanel(new BorderLayout());
        chooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        chooserPanel.add(choiceList, BorderLayout.CENTER);

        JButton chooseButton = new JButton("Add job for this header");
        chooserPanel.add(chooseButton, BorderLayout.SOUTH);

        add(chooserPanel);
        pack();
        setLocationRelativeTo(windowRef);
    }
}
