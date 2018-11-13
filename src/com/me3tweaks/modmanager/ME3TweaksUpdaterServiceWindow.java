package com.me3tweaks.modmanager;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class ME3TweaksUpdaterServiceWindow {
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JButton fetchManifestsButton;
    private JPanel root;

    public ME3TweaksUpdaterServiceWindow() {
        fetchManifestsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JSch jsch = new JSch();
                Session session = null;
                try {
                    session = jsch.getSession(usernameField.getText(), "ftp.me3tweaks.com", 22);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.setPassword(new String(passwordField.getPassword()));
                    session.connect();

                    Channel channel = session.openChannel("sftp");
                    channel.connect();
                    ChannelSftp sftpChannel = (ChannelSftp) channel;
                    Vector filelist = sftpChannel.ls(".");
                    for(int i=0; i<filelist.size();i++){
                        ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                        System.out.println(entry.getFilename());
                    }

                    //sftpChannel.get("/tmpremote/testDownload.txt", "/tmplocal/testDownload.txt");
                    sftpChannel.exit();
                    session.disconnect();
                } catch (JSchException ex) {
                    ex.printStackTrace();
                } catch (SftpException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ME3TweaksUpdaterServiceWindow");
        frame.setContentPane(new ME3TweaksUpdaterServiceWindow().root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
