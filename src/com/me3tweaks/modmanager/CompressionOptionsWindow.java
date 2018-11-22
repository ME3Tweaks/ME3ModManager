package com.me3tweaks.modmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompressionOptionsWindow extends JDialog {
    private JCheckBox multiThreadCheckBox;
    private JSlider compressionLevelSlider;

    private int selectedCompressionLevel = -1;
    private boolean selectedMultiThread;

    public CompressionOptionsWindow(JFrame callingWindow, int defaultCompressionLevel, boolean defaultMultiThread) {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
        ModManager.debugLogger.writeMessage("Opening Compression Settings Window");
        setupWindow(defaultCompressionLevel, defaultMultiThread);
        setLocationRelativeTo(callingWindow);
        setVisible(true);
    }

    private void setupWindow(int defaultCompressionLevel, boolean defaultMultiThread) {
        setTitle("Mod Deployment Compression Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(ModManager.ICONS);
        setResizable(false);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        JLabel header = new JLabel("Select compression level", SwingConstants.CENTER);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsPanel.add(header);
        compressionLevelSlider = new JSlider(JSlider.HORIZONTAL,
                0, 9, defaultCompressionLevel);
        compressionLevelSlider.setMajorTickSpacing(1);
        compressionLevelSlider.setPaintTicks(true);
        compressionLevelSlider.setPaintLabels(true);

        compressionLevelSlider.setToolTipText("Higher compression will take longer to compress and use more memory, but will use less disk space in the end");

        multiThreadCheckBox = new JCheckBox("Multithreaded compression");
        multiThreadCheckBox.setToolTipText("Compress in parallel, which uses more memory but significantly speeds up compression");
        multiThreadCheckBox.setSelected(defaultMultiThread);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedCompressionLevel = compressionLevelSlider.getValue();
                selectedMultiThread = multiThreadCheckBox.isSelected();
                dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cancelButton.setToolTipText("Cancels deployment");
        optionsPanel.add(compressionLevelSlider);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(multiThreadCheckBox);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        optionsPanel.add(buttonPanel);

        optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(optionsPanel);
        pack();
    }

    public int getCompressionLevel() {
        return selectedCompressionLevel;

    }

    public boolean getMultithreaded() {
        return selectedMultiThread;
    }
}
