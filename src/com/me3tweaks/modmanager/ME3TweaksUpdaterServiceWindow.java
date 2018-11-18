package com.me3tweaks.modmanager;

import com.jcraft.jsch.*;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.Wini;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ME3TweaksUpdaterServiceWindow extends JDialog {
    private final String compressedfulloutputfolder;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JList manifestLists;
    private JProgressBar progressBar;
    private File manifestFile;
    private Mod mod;
    private JButton loginButton;

    public ME3TweaksUpdaterServiceWindow(Mod mod, File manifestFile, String compressedfulloutputfolder) {
        this.mod = mod;
        this.manifestFile = manifestFile;
        this.compressedfulloutputfolder = compressedfulloutputfolder;
        setupWindow();
        pack();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("ME3Tweaks Updater Service");
        Wini settings = ModManager.LoadSettingsINI();

        JPanel rootPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        int gridx = 0;
        int gridy = 0;
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.ipadx = 2;
        constraints.ipady = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;

        //region LoginPanel

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int y = 0;
        int x = 0;

        c.gridx = y;
        c.gridy = x;
        c.ipadx = 2;
        c.ipady = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        JLabel me3TweaksTextLabel = new JLabel("ME3Tweaks");
        JLabel me3tweaksLogoLabel = null;
        try {
            BufferedImage me3tweaksLogo = ImageIO.read(ModMakerEntryWindow.class.getResourceAsStream("/resource/me3tweaks.png"));
            me3tweaksLogo = ResourceUtils.getScaledInstance(me3tweaksLogo, 200, 44, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            me3tweaksLogoLabel = new JLabel(new ImageIcon(me3tweaksLogo));
            me3tweaksLogoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (Exception e1) {
            ModManager.debugLogger.writeErrorWithException("Unable to load ME3Tweaks Logo image from jar:", e1);
        }

        c.gridwidth = 3;
        if (me3tweaksLogoLabel == null) {
            loginPanel.add(me3TweaksTextLabel, c);
        } else {
            loginPanel.add(me3tweaksLogoLabel, c);
        }

        JLabel updaterService = new JLabel("Updater Service");
        updaterService.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.gridy = ++y;
        loginPanel.add(updaterService, c);


        JLabel usernameLabel = new JLabel("Username");
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = ++y;
        loginPanel.add(usernameLabel, c);

        usernameField = new JTextField();
        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = y;
        loginPanel.add(usernameField, c);

        JLabel passwordLabel = new JLabel("Password");
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = ++y;
        loginPanel.add(passwordLabel, c);


        passwordField = new JPasswordField();
        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = y;
        loginPanel.add(passwordField, c);

        loginButton = new JButton("Log in");
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridy = ++y;
        loginPanel.add(loginButton, c);

        usernameField.setText(settings.get("UpdaterService", "username"));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginThread().execute();
            }
        });

        if (!usernameField.getText().equals("")) {
            passwordField.requestFocusInWindow();
        }

        //endregion

        //region Manifestslisting

        //endregion

        progressBar = new JProgressBar();

        rootPanel.add(loginPanel, constraints);
        constraints.gridy = ++gridy;

        rootPanel.add(progressBar, constraints);
        add(rootPanel);
        getRootPane().setDefaultButton(loginButton);
    }

    class LoginThread extends SwingWorker<Void, Void> {
        public LoginThread() {
            loginButton.setEnabled(false);
            progressBar.setIndeterminate(true);
        }

        public Void doInBackground() throws Exception {
            Wini settings = ModManager.LoadSettingsINI();
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
                String manifestsPathRoot = settings.get("UpdaterService", "manifestspath");
                Vector filelist = sftpChannel.ls(manifestsPathRoot);
                boolean manifestAlreadyOnServer = false;
                for (int i = 0; i < filelist.size(); i++) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                    if (entry.getFilename().equals(manifestFile.getName())) {
                        manifestAlreadyOnServer = true;
                        break;
                    }
                }

                String onServerManifestPath = ModManager.appendForwardSlash(manifestsPathRoot) + manifestFile.getName();
                String serverUpdateRoot = ModManager.appendForwardSlash(settings.get("UpdaterService", "lzmastoragepath"));

                if (false && manifestAlreadyOnServer) {
                    //region Delta upload
                    //Get manifest and set version to 0.001 while we upload new files.

                    String manifestOnServerStr = "";

                    try (InputStream is = sftpChannel.get(onServerManifestPath)) {
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            manifestOnServerStr += line + "\n";
                        }
                    } catch (Exception e) {
                        System.err.println("Exception");
                    }

                    String newXml = "";
                    try {
                        Document d = ResourceUtils.loadXMLFromString(manifestOnServerStr);
                        Element root = d.getDocumentElement();
                        root.setAttribute("version", "0.001");
                        newXml = ModMakerCompilerWindow.docToString(d);
                    } catch (Exception e) {
                        System.err.println("Exception");
                    }

                    //write xml to server
                    try (OutputStream out = sftpChannel.put(onServerManifestPath, ChannelSftp.OVERWRITE)) {
                        OutputStreamWriter writer = new OutputStreamWriter(out);
                        writer.write(newXml);
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("Exception");
                    }
                    System.out.println("Updated MANIFEST");
                    //endregion
                } else {
                    //region Upload everything

                    //Create folders first
                    Predicate<Path> fpredicate = p -> Files.isDirectory(p);
                    var foldersAsPath = (ArrayList<Path>) Files.walk(Paths.get(compressedfulloutputfolder)).filter(fpredicate).collect(Collectors.toList());
                    ArrayList<String> relativePaths = new ArrayList<>();
                    relativePaths.add(FilenameUtils.getName(compressedfulloutputfolder));
                    for (Path folder : foldersAsPath) {
                        if (folder.toString().equalsIgnoreCase(compressedfulloutputfolder)) {
                            continue;
                        }
                        relativePaths.add(FilenameUtils.getName(compressedfulloutputfolder) + "/" + ResourceUtils.normalizeFilePath(ResourceUtils.getRelativePath(folder.toString(), compressedfulloutputfolder, File.separator), false));
                    }

                    for (String folder : relativePaths) {
                        //Create on server
                        String fullpath = serverUpdateRoot + folder;
                        System.out.println(fullpath);
                        try {
                            sftpChannel.cd(fullpath);
                            System.out.println("Path exists: " + fullpath);
                        } catch (SftpException e) {
                            sftpChannel.mkdir(fullpath);
                            sftpChannel.cd(fullpath);
                            System.out.println("Created path: " + fullpath);
                        }
                    }


                    Predicate<Path> predicate = p -> Files.isRegularFile(p) && !Files.isDirectory(p);
                    try {
                        var files = (ArrayList<Path>) Files.walk(Paths.get(compressedfulloutputfolder)).filter(predicate).collect(Collectors.toList());
                        relativePaths = new ArrayList<>();
                        for (Path file : files) {
                            relativePaths.add(FilenameUtils.getName(compressedfulloutputfolder) + "/" + ResourceUtils.normalizeFilePath(ResourceUtils.getRelativePath(file.toString(), compressedfulloutputfolder, File.separator), false));
                        }

                        for (String file : relativePaths) {
                            //oo... hacky...
                            String sourceFile = ResourceUtils.normalizeFilePath(compressedfulloutputfolder + file.substring(FilenameUtils.getName(compressedfulloutputfolder).length()), true);
                            String destFile = ResourceUtils.normalizeFilePath(serverUpdateRoot + file,false);
                            //System.out.println(sourceFile+" -> "+destFile);
                            System.out.println("Uploading " + destFile);
                            sftpChannel.put(sourceFile, destFile, ChannelSftp.OVERWRITE);
                            System.out.println("Uploaded " + file);
                        }
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error uploading files: ", e);
                    }


                    try {
                        sftpChannel.put(manifestFile.getAbsolutePath(), onServerManifestPath, ChannelSftp.OVERWRITE);
                        System.out.println("Updated MANIFEST");
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error uploading manifest: ", e);
                    }
                    //endregion
                }

                sftpChannel.exit();
                session.disconnect();
            } catch (
                    JSchException ex) {
                ex.printStackTrace();
            } catch (
                    SftpException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        public void done() {
            loginButton.setEnabled(true);
            progressBar.setIndeterminate(false);
        }
    }
}
