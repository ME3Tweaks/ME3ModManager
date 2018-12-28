package com.me3tweaks.modmanager;

import com.jcraft.jsch.*;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ThreadCommand;
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
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ME3TweaksUpdaterServiceWindow extends JDialog {
    private final String compressedfulloutputfolder;
    private final String deltaoutputfolder;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JList manifestLists;
    private JProgressBar progressBar;
    private File manifestFile;
    private Mod mod;
    private JButton loginButton;
    private JButton copyManifestsButton, validateManifestButton;
    private JLabel taskLabel;
    private ArrayList<File> updatedFiles; //delta only
    private JPanel manifestActionsPanel;

    public ME3TweaksUpdaterServiceWindow(Mod mod, File manifestFile, String compressedfulloutputfolder, String deltaoutputfolder, ArrayList<File> updatedFiles) {
        super(null, ModalityType.APPLICATION_MODAL);
        this.mod = mod;
        this.manifestFile = manifestFile;
        this.compressedfulloutputfolder = compressedfulloutputfolder;
        this.deltaoutputfolder = deltaoutputfolder;
        this.updatedFiles = updatedFiles;
        setupWindow();
        setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
        setVisible(true);
    }

    private void setupWindow() {
        setIconImages(ModManager.ICONS);
        setTitle("ME3Tweaks Updater Service");
        setMinimumSize(new Dimension(240, 230));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(ModManager.ICONS);

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
        constraints.anchor = GridBagConstraints.PAGE_START;

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
        updaterService.setHorizontalAlignment(JLabel.CENTER);
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
                ModManager.debugLogger.writeMessage("Clicked login, running uploader thread...");
                new ME3TweaksUpdaterServicingThread().execute();
            }
        });


        //endregion

        //region Manifestslisting

        //endregion

        progressBar = new JProgressBar();

        rootPanel.add(loginPanel, constraints);
        constraints.gridy = ++gridy;

        rootPanel.add(progressBar, constraints);

        constraints.gridy = ++gridy;
        manifestActionsPanel = new JPanel(new BorderLayout());

        copyManifestsButton = new JButton("Activate");
        copyManifestsButton.setToolTipText("<html>Copies manifest from your server directory into the live production directory on ME3Tweaks.<br>On the hour, every hour, manifests are automatically copied from 3rd party accounts to production.<br>You can use this option to force this immediately.</html>");
        copyManifestsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceUtils.openWebpage("https://me3tweaks.com/mods/latestxml/copy3rdpartymanifests");
            }
        });

        manifestActionsPanel.add(copyManifestsButton, BorderLayout.WEST);
        validateManifestButton = new JButton("Validate live manifest");
        copyManifestsButton.setToolTipText("<html>Triggers ME3Tweaks to read and validate the current production manifest.<br>If you have not yet activated the manifest this may be out of sync from the backing data.<br>This option checks for LZMA files and their sizes to ensure clients won't have issues downloading the update.</html>");
        validateManifestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceUtils.openWebpage("https://me3tweaks.com/mods/latestxml/validatemanifests?file=" + manifestFile.getName());
            }
        });
        manifestActionsPanel.setVisible(false);
        manifestActionsPanel.add(validateManifestButton, BorderLayout.EAST);
        rootPanel.add(manifestActionsPanel, constraints);

        constraints.gridy = ++gridy;
        if (mod == null) {
            mod = new Mod();
            mod.setModName("Fanciful Edi Test");
        }
        taskLabel = new JLabel("<html><center>Login to upload to updater service<br>" + mod.getModName() + "</center></html>", SwingConstants.CENTER);
        taskLabel.setHorizontalAlignment(JLabel.CENTER);
        rootPanel.add(taskLabel, constraints);

        constraints.weighty = 1;
        rootPanel.add(new JLabel(""), constraints);
        add(rootPanel);
        pack();
        if (!usernameField.getText().equals("")) {
            passwordField.requestFocus();
        }
        getRootPane().setDefaultButton(loginButton);
    }


    class ME3TweaksUpdaterServicingThread extends SwingWorker<Void, ThreadCommand> {
        long totalBytesTransferredThisFile = 0;
        long totalBytesTransferred = 0;
        long totalBytesToTransfer = 1;

        public ME3TweaksUpdaterServicingThread() {
            ModManager.debugLogger.writeMessage("Disabling login button");
            loginButton.setEnabled(false);
            progressBar.setIndeterminate(true);
        }

        public Void doInBackground() throws Exception {
            Wini settings = ModManager.LoadSettingsINI();
            JSch jsch = new JSch();
            Session session = null;
            try {
                publish(new ThreadCommand("TASK_UPDATE", "Connecting to server"));
                String username = usernameField.getText();
                ModManager.debugLogger.writeMessage("Opening session to ME3Tweaks with username " + username);

                session = jsch.getSession(username, "ftp.me3tweaks.com", 22);


                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(new String(passwordField.getPassword()));
                try {
                    ModManager.debugLogger.writeMessage("Connecting to server using password authentication");
                    session.connect();
                } catch (JSchException je) {
                    publish(new ThreadCommand("TASK_UPDATE", "Connection error: " + je.getMessage()));
                    ModManager.debugLogger.writeErrorWithException("Connection error: ", je);
                    return null;
                }

                ModManager.debugLogger.writeMessage("Connected to server.");

                if (!username.contains("_me3tweaks")) {
                    //Not third party
                    ModManager.debugLogger.writeMessage("Not third party - disabling activate button as it will automatically activate itself");
                    publish(new ThreadCommand("DISABLE_COPY_BUTTON"));
                }

                Channel channel = session.openChannel("sftp");
                ModManager.debugLogger.writeMessage("Connecting to FTP over SSH");

                channel.connect();
                ModManager.debugLogger.writeMessage("Connected to FTP over SSH");

                publish(new ThreadCommand("HIDE_LOGIN"));

                ModManager.debugLogger.writeMessage("Checking if manifest already exists on server");

                ChannelSftp sftpChannel = (ChannelSftp) channel;
                String manifestsPathRoot = settings.get("UpdaterService", "manifestspath");
                Vector filelist = sftpChannel.ls(manifestsPathRoot);
                boolean manifestAlreadyOnServer = false;
                for (int i = 0; i < filelist.size(); i++) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                    if (entry.getFilename().equals(manifestFile.getName())) {
                        ModManager.debugLogger.writeMessage("Manifest already exists on server");
                        manifestAlreadyOnServer = true;
                        break;
                    }
                }
                if (!manifestAlreadyOnServer) {
                    ModManager.debugLogger.writeMessage("Manifest is not already on server");
                }

                String onServerManifestPath = ModManager.appendForwardSlash(manifestsPathRoot) + manifestFile.getName();
                String serverUpdateRoot = ModManager.appendForwardSlash(settings.get("UpdaterService", "lzmastoragepath"));

                ModManager.debugLogger.writeMessage("Server manifest path: "+onServerManifestPath);
                ModManager.debugLogger.writeMessage("Server LZMA storage path: "+serverUpdateRoot);

                if (manifestAlreadyOnServer) {
                    //region Delta upload
                    //Get manifest and set version to 0.001 while we upload new files.
                    ModManager.debugLogger.writeMessage("Downloading existing manifest");

                    String manifestOnServerStr = "";
                    publish(new ThreadCommand("TASK_UPDATE", "Disable updates for existing clients"));
                    try (InputStream is = sftpChannel.get(onServerManifestPath)) {
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            manifestOnServerStr += line + "\n";
                        }
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error reading existing manifest string from server:", e);
                    }

                    String newXml = "";
                    Document d;
                    try {
                        ModManager.debugLogger.writeMessage("Reading existing manifest into XML document");

                        d = ResourceUtils.loadXMLFromString(manifestOnServerStr);
                        Element root = d.getDocumentElement();
                        root.setAttribute("version", "0.001");
                        newXml = ModMakerCompilerWindow.docToString(d);
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error creating un-updatable manifest for upload duration:", e);
                    }

                    //write xml to server
                    ModManager.debugLogger.writeMessage("Writing decremented manifest back to server");
                    try (OutputStream out = sftpChannel.put(onServerManifestPath, ChannelSftp.OVERWRITE)) {
                        OutputStreamWriter writer = new OutputStreamWriter(out);
                        writer.write(newXml);
                        writer.flush();
                    } catch (IOException e) {
                        ModManager.debugLogger.writeErrorWithException("Error writing xml to server:", e);
                    }

                    ModManager.debugLogger.writeMessage("Uploading folder to server: "+deltaoutputfolder+" -> "+serverUpdateRoot);
                    UploadFolder(sftpChannel, deltaoutputfolder, serverUpdateRoot);

                    //Upload full new manifest
                    try {
                        ModManager.debugLogger.writeMessage("Publishing new manifest");
                        publish(new ThreadCommand("TASK_UPDATE", "Uploading manifest"));

                        sftpChannel.put(manifestFile.getAbsolutePath(), onServerManifestPath, ChannelSftp.OVERWRITE);
                        publish(new ThreadCommand("TASK_UPDATE", mod.getModName() + "<br>uploaded to updater service"));
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error uploading manifest: ", e);
                    }
                    publish(new ThreadCommand("SHOW_ACTION_PANEL"));
                    //endregion
                } else {
                    //region Upload everything
                    ModManager.debugLogger.writeMessage("New mod - uploading folder to server: "+deltaoutputfolder+" -> "+serverUpdateRoot);
                    UploadFolder(sftpChannel, compressedfulloutputfolder, serverUpdateRoot);

                    try {
                        ModManager.debugLogger.writeMessage("Publishing manifest");
                        publish(new ThreadCommand("TASK_UPDATE", "Uploading manifest"));
                        sftpChannel.put(manifestFile.getAbsolutePath(), onServerManifestPath, ChannelSftp.OVERWRITE);
                        publish(new ThreadCommand("TASK_UPDATE", mod.getModName() + "<br>uploaded to updater service"));
                        publish(new ThreadCommand("SHOW_ACTION_PANEL"));
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Error uploading manifest: ", e);
                    }
                    //endregion
                }
                ModManager.debugLogger.writeMessage("Servicing complete, disconnecting");

                sftpChannel.exit();
                session.disconnect();
            } catch (JSchException ex) {
                publish(new ThreadCommand("TASK_UPDATE", "JSch Error - see logs"));
                ModManager.debugLogger.writeErrorWithException("Jsch Error in uploader thread: ", ex);
            } catch (SftpException ex) {
                publish(new ThreadCommand("TASK_UPDATE", "SFTP Error - see logs"));
                ModManager.debugLogger.writeErrorWithException("SFTP Error in uploader thread: ", ex);
            }
            return null;
        }

        private void UploadFolder(ChannelSftp sftpChannel, String folderSource, String serverUpdateRoot) throws Exception {
            //Create folders first
            publish(new ThreadCommand("TASK_UPDATE", "Creating folders"));

            folderSource = ResourceUtils.removeTrailingSlashes(folderSource);
            Predicate<Path> fpredicate = p -> Files.isDirectory(p);
            ArrayList<Path> foldersAsPath = (ArrayList<Path>) Files.walk(Paths.get(folderSource)).filter(fpredicate).collect(Collectors.toList());
            ArrayList<String> relativePaths = new ArrayList<>();
            relativePaths.add(FilenameUtils.getName(folderSource));
            for (Path folder : foldersAsPath) {
                if (folder.toString().equalsIgnoreCase(folderSource)) {
                    continue;
                }
                relativePaths.add(FilenameUtils.getName(folderSource) + "/" + ResourceUtils.normalizeFilePath(ResourceUtils.getRelativePath(folder.toString(), folderSource, File.separator), false));
            }

            for (String folder : relativePaths) {
                //Create on server
                String fullpath = serverUpdateRoot + folder;
                System.out.println(fullpath);
                try {
                    sftpChannel.cd(fullpath);
                    ModManager.debugLogger.writeMessage("Directory created on server: " + fullpath);
                } catch (SftpException e) {
                    sftpChannel.mkdir(fullpath);
                    sftpChannel.cd(fullpath);
                    ModManager.debugLogger.writeMessage("Directory already exists on server, skipping creation: " + fullpath);
                }
            }

            Predicate<Path> predicate = p -> Files.isRegularFile(p) && !Files.isDirectory(p);
            try {
                Path compressedRootPath = Paths.get(folderSource);
                totalBytesToTransfer = ResourceUtils.GetDirectorySize(compressedRootPath, true);
                ModManager.debugLogger.writeMessage("Amount of data to upload: " + ResourceUtils.humanReadableByteCount(totalBytesToTransfer, true));
                ArrayList<Path> files = (ArrayList<Path>) Files.walk(compressedRootPath).filter(predicate).collect(Collectors.toList());
                relativePaths = new ArrayList<>();
                for (Path file : files) {
                    relativePaths.add(FilenameUtils.getName(folderSource) + "/" + ResourceUtils.normalizeFilePath(ResourceUtils.getRelativePath(file.toString(), folderSource, File.separator), false));
                }

                for (String file : relativePaths) {
                    //oo... hacky...
                    String sourceFile = ResourceUtils.normalizeFilePath(folderSource + file.substring(FilenameUtils.getName(folderSource).length()), true);
                    String destFile = ResourceUtils.normalizeFilePath(serverUpdateRoot + file, false);
                    //System.out.println(sourceFile+" -> "+destFile);
                    ModManager.debugLogger.writeMessage("Uploading " + destFile);

                    publish(new ThreadCommand("TASK_UPDATE", "Uploading<br>" + FilenameUtils.getName(destFile)));
                    sftpChannel.put(sourceFile, destFile, new SystemOutProgressMonitor(), ChannelSftp.OVERWRITE);
                    ModManager.debugLogger.writeMessage("Uploaded " + destFile);
                }
            } catch (Exception e) {
                ModManager.debugLogger.writeErrorWithException("Error uploading files: ", e);
            }
        }

        @Override
        protected void process(List<ThreadCommand> chunks) {
            for (ThreadCommand tc : chunks) {
                String command = tc.getCommand();
                switch (command) {
                    case "PROGRESS_UPDATE":
                        progressBar.setValue((int) (totalBytesTransferred * 100.0 / totalBytesToTransfer));
                        progressBar.setIndeterminate(false);
                        break;
                    case "TASK_UPDATE":
                        taskLabel.setText("<html><center>" + tc.getMessage() + "</center></html>");
                        break;
                    case "HIDE_LOGIN":
                        loginButton.setVisible(false);
                        break;
                    case "DISABLE_COPY_BUTTON":
                        copyManifestsButton.setEnabled(false);
                        break;
                    case "SHOW_ACTION_PANEL":
                        manifestActionsPanel.setVisible(true);
                        break;
                }
            }
        }

        public void done() {
            loginButton.setEnabled(true);
            progressBar.setIndeterminate(false);
        }

        public class SystemOutProgressMonitor implements SftpProgressMonitor {
            public SystemOutProgressMonitor() {
            }

            @Override
            public void init(int op, java.lang.String src, java.lang.String dest, long max) {
                System.out.println("STARTING: " + op + " " + src + " -> " + dest + " total: " + max);
            }

            @Override
            public boolean count(long bytes) {
                //for(int x=0; x < bytes; x++) {
                System.out.print("#");
                totalBytesTransferred += bytes;
                totalBytesTransferredThisFile += bytes;
                //}
                publish(new ThreadCommand("PROGRESS_UPDATE"));
                return (true);
            }

            @Override
            public void end() {

            }
        }
    }
}
