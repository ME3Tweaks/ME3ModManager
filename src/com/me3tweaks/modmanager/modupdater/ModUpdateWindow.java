package com.me3tweaks.modmanager.modupdater;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

@SuppressWarnings("serial")
public class ModUpdateWindow extends JDialog implements PropertyChangeListener {
    String downloadSource;
    boolean error = false;
    JButton cancelButton;
    JLabel statusLabel;
    JProgressBar downloadProgress;
    private UpdatePackage upackage;
    int jobCode;

    public ModUpdateWindow(UpdatePackage upackage) {
        this.upackage = upackage;
        downloadSource = "https://me3tweaks.com/mods/updates/";
        setupWindow();
    }

    /**
     * Starts a single-mod update process
     *
     * @param frame
     */
    public void startUpdate(JFrame frame) {
        jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("SingleModUpdate","Updating mod");
        ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updating " + upackage.getMod().getModName());
        if (upackage.isModmakerupdate()) {
            //get default language
            //set combobox from settings
            new ModMakerCompilerWindow(Integer.toString(upackage.getMod().getModMakerCode()), ModMakerEntryWindow.getDefaultLanguages());
        } else {
            MultithreadDownloadTask task = new MultithreadDownloadTask(upackage);
            task.addPropertyChangeListener(this);
            task.execute();
            setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
            setVisible(true); //EDT will stall until this modal window closes
            ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
            ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
        }
    }

    /**
     * Starts a multi-mod update process (does not display modals)
     *
     * @param amuw
     */
    public boolean startAllModsUpdate(AllModsUpdateWindow amuw) {
        jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("MultimodUpdate","Updating mods");
        ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updating " + upackage.getMod().getModName());
        if (upackage.isModmakerupdate()) {
            //validate biogamedir (TLK)
            if (!ModManagerWindow.validateBIOGameDir()) {
                ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("ModMaker mods not updatable, invalid BIOGame directory");
                return false;
            }

            //get default language
            //set combobox from settings
            ModManager.debugLogger.writeMessage("Starting AllModsUpdate for MODMAKER MOD");
            statusLabel.setText("Upgrading via ModMaker Compiler");
            ModMakerCompilerWindow mcw = new ModMakerCompilerWindow(upackage.getMod(), ModMakerEntryWindow.getDefaultLanguages());
            while (mcw.isShowing()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            MultithreadDownloadTask task = new MultithreadDownloadTask(upackage, amuw);
            task.addPropertyChangeListener(this);
            task.execute();
            setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
            setVisible(true);
            ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
            return task.wasSuccessful();
        }
        ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
        return true;
    }

    private void setupWindow() {
        setTitle("Updating mod");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(300, 90));
        setResizable(false);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setIconImages(ModManager.ICONS);
        pack();

        JPanel panel = new JPanel(new BorderLayout());

        downloadProgress = new JProgressBar();
        downloadProgress.setStringPainted(true);
        downloadProgress.setIndeterminate(false);
        downloadProgress.setEnabled(false);

        statusLabel = new JLabel("0/0 files downloaded");

        panel.add(new JLabel(upackage.getMod().getModName() + " is updating..."), BorderLayout.NORTH);
        panel.add(downloadProgress, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        // updatePanel.add(actionPanel);
        // updatePanel.add(sizeLabel);

        // aboutPanel.add(loggingMode, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.getContentPane().add(panel);
    }

    void setStatusText(String text) {
        statusLabel.setText(text);
    }

    /**
     * Update the progress bar's state whenever the progress of download
     * changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            downloadProgress.setValue(progress);
        }
    }

    /**
     * Execute file download in a background thread and update the progress.
     *
     * @author www.codejava.net
     */
    class MultithreadDownloadTask extends SwingWorker<Void, Object> {
        private static final int BUFFER_SIZE = 4096;
        private String saveDirectory;
        private UpdatePackage upackage;
        private int numFilesToDownload;
        private int numProcessed; //may have race condition
        private long bytesDownloaded; //may have race condition
        private long totalBytes;
        private AllModsUpdateWindow amuw;
        private ExecutorService downloadExecutor;

        public MultithreadDownloadTask(UpdatePackage upackage) {
            String modpath = upackage.getMod().getModPath();
            String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
            this.saveDirectory = ModManager.getTempDir() + "update" + File.separator + updateFolder;
            this.upackage = upackage;
            numFilesToDownload = upackage.getFilesToDownload().size();
            upackage.sortByLargestFirst();
            for (ManifestModFile mf : upackage.getFilesToDownload()) {
                totalBytes += mf.getLzmasize() > 0 ? mf.getLzmasize() : mf.getSize();
            }
            statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
            ModManager.debugLogger.writeMessage("Created a download task");
        }

        public MultithreadDownloadTask(UpdatePackage upackage, AllModsUpdateWindow amuw) {
            String modpath = upackage.getMod().getModPath();
            String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
            MultithreadDownloadTask.this.saveDirectory = ModManager.getTempDir() + updateFolder;
            MultithreadDownloadTask.this.upackage = upackage;
            MultithreadDownloadTask.this.amuw = amuw;
            numFilesToDownload = upackage.getFilesToDownload().size();
            for (ManifestModFile mf : upackage.getFilesToDownload()) {
                totalBytes += mf.getLzmasize() > 0 ? mf.getLzmasize() : mf.getSize();
            }
            statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
            ModManager.debugLogger.writeMessage("Created an all mods download task");
        }

        /**
         * Executed in background thread
         */
        @Override
        protected Void doInBackground() throws Exception {
            ModManager.debugLogger.writeMessage("Deleting existing update directory: " + saveDirectory);
            try {
                FileUtils.deleteDirectory(new File(saveDirectory));
            } catch (IOException e) {
                ModManager.debugLogger.writeError("Unable to delete update directory");
                ModManager.debugLogger.writeException(e);
            }

            ModManager.debugLogger.writeMessage("Downloading update from server source: " + downloadSource);
            downloadExecutor = Executors.newFixedThreadPool(4);
            ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
            for (ManifestModFile mf : upackage.getFilesToDownload()) {
                if (mf.getLzmasize() > 0 && !SevenZip.isInitializedSuccessfully()) {
                    SevenZip.initSevenZipFromPlatformJAR(); //preload to prevent race condition
                }
                DownloadTask dt = new DownloadTask(mf, downloadSource + upackage.getServerFolderName() + "/");
                futures.add(downloadExecutor.submit(dt));
            }
            downloadExecutor.shutdown();
            downloadExecutor.awaitTermination(60, TimeUnit.MINUTES);
            for (Future<Boolean> f : futures) {
                try {
                    Boolean result = f.get();
                    if (result == false) {
                        error = true;
                        ModManager.debugLogger.writeError("A file failed to download.");
                        throw new Exception("One or more files failed to download.");
                    }
                } catch (ExecutionException e) {
                    ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE DOWNLOADING FILE (AFTER EXECUTOR FINISHED): ", e);
                }
            }

            if (!error) {
                executeUpdate();
            }
            return null;
        }

        private boolean decompressLZMAFile(String lzmaFile, String expectedHash) {
            RandomAccessFile randomAccessFile = null;
            String decompressedFileLocation = lzmaFile.substring(0, lzmaFile.length() - 5);
            ModManager.debugLogger.writeMessage("Decompressing LZMA file to " + decompressedFileLocation);
            IInArchive inArchive = null;
            try {
                randomAccessFile = new RandomAccessFile(lzmaFile, "r");
                inArchive = SevenZip.openInArchive(null, // autodetect archive type
                        new RandomAccessFileInStream(randomAccessFile));

                // Getting simple interface of the archive inArchive
                ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

                for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                    if (!item.isFolder()) {
                        ExtractOperationResult result;

                        result = item.extractSlow(new ISequentialOutStream() {
                            public int write(byte[] data) throws SevenZipException {
                                FileOutputStream fos = null;
                                try {
                                    File path = new File(decompressedFileLocation);

                                    if (!path.getParentFile().exists()) {
                                        path.getParentFile().mkdirs();
                                    }

                                    if (!path.exists()) {
                                        path.createNewFile();
                                    }
                                    fos = new FileOutputStream(path, true);
                                    fos.write(data);
                                } catch (SevenZipException e) {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    PrintStream ps = new PrintStream(baos);
                                    e.printStackTrace(ps);
                                    try {
                                        ModManager.debugLogger.writeError("Error while decompressing LZMA: " + baos.toString("utf-8"));
                                    } catch (UnsupportedEncodingException e1) {
                                        //this shouldn't happen.
                                    }
                                } catch (IOException e) {
                                    ModManager.debugLogger.writeErrorWithException("IOException while extracting " + lzmaFile, e);
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (fos != null) {
                                            fos.flush();
                                            fos.close();
                                        }
                                    } catch (IOException e) {
                                        ModManager.debugLogger.writeErrorWithException("Could not close FileOutputStream", e);
                                    }
                                }
                                return data.length; // Return amount of consumed data
                            }
                        });

                        if (result == ExtractOperationResult.OK) {
                            ModManager.debugLogger.writeMessage("Decompression complete.");
                            if (expectedHash != null) {
                                String hash = MD5Checksum.getMD5Checksum(decompressedFileLocation);
                                if (expectedHash.equals(hash)) {
                                    throw new Exception("Hash check failed for decompressed file");
                                }
                            }
                            return true;
                        } else {
                            ModManager.debugLogger.writeError("Error extracting item: " + result);
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                ModManager.debugLogger.writeErrorWithException("Error occured decompressing LZMA file:", e);
            } finally {
                if (inArchive != null) {
                    try {
                        inArchive.close();
                    } catch (SevenZipException e) {
                        System.err.println("Error closing archive: " + e);
                    }
                }
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        System.err.println("Error closing file: " + e);
                    }
                }
                boolean deleted = FileUtils.deleteQuietly(new File(lzmaFile));
                if (!deleted) {
                    System.err.println("Unable to delete compressed file after decompression attempt");
                }
            }
            return false;
        }

        public boolean wasSuccessful() {
            return !error;
        }

        class DownloadTask implements Callable<Boolean> {
            private String baseurl;
            private ManifestModFile mf;

            public DownloadTask(ManifestModFile mf, String baseurl) {
                this.baseurl = baseurl;
                this.mf = mf;
            }

            @Override
            public Boolean call() throws Exception {
                String relPath = null;
                try {
                    relPath = mf.getRelativePath();
                    if (mf.getLzmasize() > 0) {
                        relPath += ".lzma";
                    }
                    String saveFilePath = saveDirectory + File.separator + relPath;

                    HTTPDownloadUtil util = new HTTPDownloadUtil();
                    File saveFile = new File(saveFilePath);
                    new File(saveFile.getParent()).mkdirs();
                    ModManager.debugLogger.writeMessage("Downloading file: " + baseurl + relPath);

                    util.downloadFile(baseurl + relPath);
                    InputStream inputStream = util.getInputStream();
                    // opens an output stream to save into file
                    FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead = -1;
                    int percentCompleted = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted()) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesDownloaded += bytesRead;
                        percentCompleted = (int) (bytesDownloaded * 100 / totalBytes);
                        setProgress(percentCompleted);
                    }
                    outputStream.close();
                    if (!Thread.currentThread().isInterrupted()) {
                        ModManager.debugLogger.writeMessage("Downloaded file: " + baseurl + relPath + ", saved to disk as " + new File(saveFilePath).length() + " bytes");
                        if (mf.getLzmasize() > 0) {
                            File downloadedLZMAFile = new File(saveFilePath);
                            //check downloaded file
                            if (mf.getLzmasize() != downloadedLZMAFile.length()) {
                                ModManager.debugLogger.writeError("Downloaded LZMA file size is wrong: EXPECTED:\n" + mf.getLzmasize() + ", GOT:" + downloadedLZMAFile.length());
                                error = true;
                                downloadExecutor.shutdownNow();
                                throw new IOException("Downloaded file does not match listed size in manifest:\n" + saveFilePath);
                            }
                            String downloadedChecksum = MD5Checksum.getMD5Checksum(saveFilePath);
                            if (!mf.getLzmahash().equals(downloadedChecksum)) {
                                ModManager.debugLogger.writeError("Downloaded LZMA file hash check failed:\nEXPECTED: " + mf.getLzmahash() + "\nGOT:" + downloadedChecksum);
                                error = true;
                                downloadExecutor.shutdownNow();
                                throw new IOException("Downloaded file failed hash check:\n" + saveFilePath);
                            }

                            //decompress LZMA file
                            if (!decompressLZMAFile(saveFilePath, mf.getLzmahash())) {
                                error = true;
                                downloadExecutor.shutdownNow();
                                throw new IOException("Error decompressing: " + saveFilePath + "\nCheck the log to see more info.");
                            }
                        }

                        publish(++numProcessed);
                        return true;
                    }
                    return false;
                } catch (IOException e) {
                    ModManager.debugLogger.writeErrorWithException("Failed to download file " + (baseurl + relPath) + ":", e);
                    return false;
                }
            }
        }

        @Override
        public void process(List<Object> chunks) {
            setStatusText(numProcessed + "/" + numFilesToDownload + " files downloaded");
        }

        public void executeUpdate() {
            ModManager.debugLogger.writeMessage("Applying downloaded update " + saveDirectory + " => " + upackage.getMod().getModPath());
            File updateDirectory = new File(saveDirectory);
            File modPath = new File(upackage.getMod().getModPath());
            if (upackage.getFilesToDownload().size() > 0) {
                try {
                    FileUtils.copyDirectory(updateDirectory, modPath);
                    ModManager.debugLogger.writeMessage("Copied new files");
                } catch (IOException e) {
                    ModManager.debugLogger.writeError("Unable to copy update directory over the source");
                    ModManager.debugLogger.writeException(e);
                    error = true;
                }
            }

            for (String str : upackage.getFilesToDelete()) {
                String fileToDelete = upackage.getMod().getModPath() + str; //path is relative
                ModManager.debugLogger.writeMessage("Deleting unused file: " + fileToDelete);
                File file = new File(fileToDelete);
                FileUtils.deleteQuietly(file);
            }
            ModManager.debugLogger.writeMessage("Update applied, verifying mod...");
            Mod verifyingMod = new Mod(upackage.getMod().getDescFile());
            if (!verifyingMod.isValidMod()) {
                ModManager.debugLogger.writeError("UPDATE HAS CAUSED MOD TO BECOME INVALID!");
                error = true;
            }

            try {
                FileUtils.deleteDirectory(updateDirectory);
            } catch (IOException e) {
                ModManager.debugLogger.writeError("Unable to delete update directory");
                ModManager.debugLogger.writeException(e);
            }

            ModManager.debugLogger.writeMessage("Staging directory cleanup complete, update has been applied");
        }

        /**
         * Executed in Swing's event dispatching thread
         */
        @Override
        protected void done() {
            dispose();
            if (amuw == null && !error) {
                JOptionPane.showMessageDialog(ModUpdateWindow.this, upackage.getMod().getModName() + " has been successfully updated.\nYou will need to apply " + upackage.getMod().getModName()
                        + " for the update to take effect.\nMod Manager will now reload mods.", "Update successful", JOptionPane.INFORMATION_MESSAGE);
            } else if (amuw == null) {
                JOptionPane.showMessageDialog(ModUpdateWindow.this, upackage.getMod().getModName() + " failed to update. The Mod Manager log will have more information.", "Updated failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * A utility that downloads a file from a URL.
     *
     * @author www.codejava.net
     */
    class HTTPDownloadUtil {

        private HttpURLConnection httpConn;

        /**
         * hold input stream of HttpURLConnection
         */
        private InputStream inputStream;

        private String fileName;
        private int contentLength;

        /**
         * Opens a stream to the file on the server for transfer
         *
         * @param fileURL HTTP URL of the file to be downloaded
         * @throws IOException
         */
        public void downloadFile(String fileURL) throws IOException {

            URL url;
            try {
                url = ME3TweaksUtils.convertToURLEscapingIllegalCharacters(fileURL);
            } catch (Exception e) {
                throw new IOException("URL could not be encoded for downloading: " + fileURL);
            }
            httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                contentLength = httpConn.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }

                // output for debugging purpose only
                // opens input stream from the HTTP connection
                inputStream = httpConn.getInputStream();

            } else {
                throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
            }
        }

        public void disconnect() throws IOException {
            inputStream.close();
            httpConn.disconnect();
        }

        public String getFileName() {
            return this.fileName;
        }

        public int getContentLength() {
            return this.contentLength;
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }
    }
}
