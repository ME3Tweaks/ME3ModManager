package com.me3tweaks.modmanager.modupdater;

import com.me3tweaks.modmanager.DeltaWindow;
import com.me3tweaks.modmanager.ME3TweaksUpdaterServiceWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow.AllModsDownloadTask;
import com.me3tweaks.modmanager.objects.*;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.ini4j.Wini;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ModXMLTools {
    static DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder docBuilder;

    public static void generateXMLFileList(Mod mod) {
        new ManifestGeneratorUpdateCompressor(mod).execute();
    }

    private static class ManifestGeneratorUpdateCompressor extends SwingWorker<String, ThreadCommand> {
        private Mod mod;
        private String changelog;
        private int jobCode;
        boolean aborted = false;
        private File manifestFile;
        private String compressedfulloutputfolder;
        private String compresseddeltaoutputfolder;
        private int forcepushtoserverResult = -1;
        private final Object lock = new ModManager.Lock(); //threading wait() and notifyall();

        private ArrayList<File> updatedFiles;

        public ManifestGeneratorUpdateCompressor(Mod mod) {
            this.mod = mod;
            //Verify Deltas
            for (ModDelta delta : mod.getModDeltas()) {
                new DeltaWindow(mod, delta, true, false);
            }
            FileUtils.deleteQuietly(new File(mod.getModPath() + "VARIANTS")); //nuke variants.
            String changelog = JOptionPane.showInputDialog(ModManagerWindow.ACTIVE_WINDOW,
                    "Enter a short changelog users will see when updating your mod.\nKeep it to 1 sentence or less as this will be shown in a dialog to users.\nLeave blank for no changelog.", "Enter Changelog",
                    JOptionPane.PLAIN_MESSAGE);
            if (changelog != null) {
                this.changelog = changelog;
                jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("ManifestGenerator", "Preparing mod for updater service");
            } else {
                ModManager.debugLogger.writeMessage("Aborting package build due to user cancellation");
                aborted = true;
            }
        }

        @Override
        protected String doInBackground() throws Exception {
            if (aborted) {
                return "";
            }
            if (mod.getModMakerCode() > 0) {
                ModManager.debugLogger.writeError("ModMaker codes use the ID");
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "ModMaker mods can't use classic updater"));
                return "";
            }

            if (mod.getClassicUpdateCode() <= 0) {
                ModManager.debugLogger.writeError("Mod must have an ME3Tweaks update code for updating. Contact Mgamerz if you need one.");
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Mod needs an updatecode in ModInfo"));
                return "";
            }



            if (mod.getVersion() <= 0) {
                ModManager.debugLogger.writeError("Mod must have a double/numeric version number for updating");
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Mod requires numeric version number"));
                return "";
            }

            if (mod.getServerModFolder().equals(Mod.DEFAULT_SERVER_FOLDER)) {
                ModManager.debugLogger.writeError("Mod must have [UPDATES]serverfolder set.");
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Mod requires serverfolder in moddesc"));
                return "";
            }

            CountDownLatch latch = new CountDownLatch(1);
            ModManagerWindow.ModDeploymentThread mdt = new ModManagerWindow.ModDeploymentThread(mod);
            mdt.stageOnly = true;
            mdt.latch = latch;
            ModManager.debugLogger.writeMessage("Waiting for deployment thread to finish...");
            mdt.execute();
            latch.await();

            if (!mdt.result) {
                ModManager.debugLogger.writeError("Deployment thread failed to return a proper staged mod!");
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Failed to stage mod"));
                return "";
            }

            String originalPath = mod.getModPath();
            mod = mdt.getMod(); //gets the staged mod
            String newPath = mod.getModPath();
            if (originalPath.equalsIgnoreCase(newPath)) {
                ModManager.debugLogger.writeError("New mod path is the same as the old one! This mod was not staged. This is a bug or error - aborting updater service session");
                return "";
            }
            ModManager.debugLogger.writeMessage("Deployment thread exited, continuing ME3Tweaks Updater Servicing thread");

            //check blacklisted files
            for (String blf : mod.getBlacklistedFiles()) {
                File f = new File(mod.getModPath() + blf);
                if (f.exists()) {
                    ModManager.debugLogger.writeError("A blacklisted file " + f
                            + " exists in mod folder. Blacklisted files will be deleted when the mod is applied. Remove this file from your distribution or remove the blacklisting in moddesc");
                    publish(new ThreadCommand("SET_BOTTOM_TEXT", "Mod has a blacklisted file (check moddesc)"));
                    return "";
                }
            }

            if (mod.getSideloadURL() != null) {
                String[] schemes = {"http", "https"}; // DEFAULT schemes = "http", "https", "ftp"
                UrlValidator urlValidator = new UrlValidator(schemes);
                if (!urlValidator.isValid(mod.getSideloadURL())) {
                    if (mod.getSideloadOnlyTargets().size() > 0) {
                        ModManager.debugLogger.writeError("Mod has invalid sideload URL, and some files are marked for sideloading only. Aborting manifest generation");
                        publish(new ThreadCommand("SET_BOTTOM_TEXT", "Invalid Sideload URL. Manifest requires valid sideload URL"));
                        return "";
                    } else {
                        ModManager.debugLogger
                                .writeError("Mod has invalid sideload URL, but no files are currently marked for sideloading, so we will continue manifest generation");
                    }
                }
            }

            //prepare foldernames
            String foldername = mod.getServerModFolder();
            if (mod.getServerModFolder().equals(Mod.DEFAULT_SERVER_FOLDER)) {
                foldername = mod.getModName().replaceAll(" ", "").toLowerCase();
                foldername = foldername.replaceAll("\\\\", "-").toLowerCase();
                foldername = foldername.replaceAll("/", "-").toLowerCase();
            } else {
                if (foldername.lastIndexOf('/') > 0) {
                    foldername = foldername.substring(foldername.lastIndexOf('/'));
                }
            }

            publish(new ThreadCommand("SET_BOTTOM_TEXT", "Fetching online manifest"));

            //SIMULATE REVERSE UPDATE
            //CHECK FOR FILE EXISTENCE IN MOD UPDATE FOLDER, LZMA HASHES.
            //FILES THAT FAIL THIS WILL BE ADDED TO COLLECTION OF FILES TO UDPATE
            Collection<File> newversionfiles = FileUtils.listFiles(new File(mod.getModPath()), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            updatedFiles = new ArrayList<>();
            UpdatePackage up = null;

            Document doc = getOnlineInfo("https://me3tweaks.com/mods/getlatest_batch", false, mod.getClassicUpdateCode());

            boolean deltaUpdate = false;
            if (doc.getElementsByTagName("mod").getLength() > 0) {
                ModManager.debugLogger.writeMessage("Running reverse-update using server manifest");
                deltaUpdate = true;
            }
            //Commented out: cached manifest. Must use server manifest.
            /*else if (manifestFile.exists()) {
                ModManager.debugLogger.writeMessage("Running reverse-update using cached manifest");
                String oldmanifest = FileUtils.readFileToString(manifestFile);
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(oldmanifest));
                doc = db.parse(is);
                deltaUpdate = true;
            }*/

            String sideloadoutputfolder = ModManager.getME3TweaksUpdaterServiceFolder() + "Sideload" + File.separator + foldername + File.separator;
            compressedfulloutputfolder = ModManager.getME3TweaksUpdaterServiceFolder() + "Full" + File.separator + foldername + File.separator;
            compresseddeltaoutputfolder = ModManager.getME3TweaksUpdaterServiceFolder() + "UpdateDelta" + File.separator + foldername + File.separator;
            if (!deltaUpdate) {
                compresseddeltaoutputfolder = compressedfulloutputfolder;
            }
            File deltaOutFolder = new File(compresseddeltaoutputfolder);
            FileUtils.deleteDirectory(deltaOutFolder);
            deltaOutFolder.mkdirs();

            if (deltaUpdate) {
                //check local files against old manifest. Changes will be considered updates and will be added to the updates folder.
                try {

                    double modversion = mod.getVersion();
                    mod.setVersion(0.001);
                    publish(new ThreadCommand("SET_BOTTOM_TEXT", "Calculating what files to use in delta update"));
                    up = checkForClassicUpdate(mod, doc, null);
                    mod.setVersion(modversion); //restore to the original value

                    if (up != null) {
                        if (up.getVersion() >= mod.getVersion()) {
                            ModManager.debugLogger.writeMessage("Server version is equal or higher to current version, which is not an update.");
                            publish(new ThreadCommand("SET_BOTTOM_TEXT", "Waiting for user input"));
                            publish(new ThreadCommand("MANIFEST_ON_SERVER_NEWER_OR_SAME", Double.toString(up.getVersion())));
                            synchronized (lock) {
                                while (forcepushtoserverResult == -1) {
                                    try {
                                        ModManager.debugLogger.writeMessage("Waiting for user to select force push, build locally, or cancel");
                                        lock.wait();
                                    } catch (InterruptedException e) {
                                        ModManager.debugLogger.writeErrorWithException("Unable to wait for force push prompt lock:", e);
                                    }
                                }
                            }

                            ModManager.debugLogger.writeMessage("User chose option: " + forcepushtoserverResult);
                            if (forcepushtoserverResult == 2) {
                                publish(new ThreadCommand("SET_BOTTOM_TEXT","Server version (" + up.getVersion() + ") >= moddesc version (" + mod.getVersion() + "), not an update"));
                                ModManager.debugLogger.writeMessage("User cancelled preparations.");
                                return "";
                            }
                        }
                        for (ManifestModFile mf : up.getFilesToDownload()) {
                            File f = new File(mod.getModPath() + File.separator + mf.getRelativePath());
                            if (f.exists()) {
                                ModManager.debugLogger.writeMessage("Add file to upload list: "+mf.getRelativePath());
                                updatedFiles.add(f);
                            } else {
                                ModManager.debugLogger.writeMessage("File not found in staged mod, while present in previous manifest. File is no longer available: "+mf.getRelativePath());
//                                removedfiles.add(f);
                            }
                        }
                        int numComputed = 0;
                        for (ManifestModFile mf : up.getUpToDateFiles()) {
                            numComputed++;
                            File f = new File(mod.getModPath() + File.separator + mf.getRelativePath());
                            String srcFile = f.getAbsolutePath();
                            String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
                            String outputFile = compressedfulloutputfolder + relativePath + ".lzma";
                            if (!(new File(outputFile).exists())) {
                                ModManager.debugLogger.writeMessage("Up to date local file not found compressed ("+outputFile+")- compressing for new manifest generation...: "+relativePath);
                                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Compressing up to date files for manifest building ["+numComputed+"/"+up.getUpToDateFiles().size()+"]"));
                                new File(outputFile).getParentFile().mkdirs();
                                String[] procargs = {ModManager.getToolsDir() + "lzma.exe", "e", srcFile, outputFile, "-d26", "-mt" + Runtime.getRuntime().availableProcessors()};
                                ProcessBuilder p = new ProcessBuilder(procargs);
                                ModManager.runProcess(p);
                                String outfileChecksum = MD5Checksum.getMD5Checksum(outputFile);
                                if (!outfileChecksum.equals(mf.getLzmahash())) {
                                    ModManager.debugLogger.writeError("LocalLy rebuilt lzma does not have hash match to server! This shouldn't happen unless there's a bug in the update check code... File that failed hash: "+mf.getRelativePath()+", local hash: "+outfileChecksum+", manifest on server lists "+mf.getLzmahash());
                                    publish(new ThreadCommand("SET_BOTTOM_TEXT","Error building update: A regenerated file has wrong hash"));
                                    return null;
                                }
                            }
                        }
                        for (String str : up.getFilesToDelete()) {
                            File f = new File(mod.getModPath()+str);
                            if (f.exists()) {
                                //reverse - new files have been added
                                updatedFiles.add(f);
                            }
                        }
                    } else {
                        //no update.
                        ModManager.debugLogger.writeMessage("No files to update. Exiting update service mod preparer thread.");
                        publish(new ThreadCommand("SET_BOTTOM_TEXT","No changes detected from previous generated manifest"));
                        return null;
                    }
                } catch (Exception e) {
                    ModManager.debugLogger.writeErrorWithException("Error loading old manifest. Performing a full compression. Error: ", e);
                    for (File f : newversionfiles) {
                        updatedFiles.add(f); //variants check
                    }
                }
            } else {
                ModManager.debugLogger.writeMessage("No old manifest - all files treated as new.");
                for (File f : newversionfiles) {
                    if (!f.getAbsolutePath().equals(mod.getModPath() + "WORKSPACE")) {
                        updatedFiles.add(f);
                    }
                }
            }

            //Compressing mod to /serverupdate
            long startTime = System.currentTimeMillis();
            //COMPRESSING FILES................
            ModManager.debugLogger.writeMessage("Compressing files...");

            int numFiles = updatedFiles.size();
            int processed = 1;
            for (File file : updatedFiles) {
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Compressing " + FilenameUtils.getBaseName(file.getAbsolutePath()), processed + "/" + numFiles));
                String srcFile = file.getAbsolutePath();
                String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
                String outputFile = compresseddeltaoutputfolder + relativePath + ".lzma";
                new File(outputFile).getParentFile().mkdirs();

                String[] procargs = {ModManager.getToolsDir() + "lzma.exe", "e", srcFile, outputFile, "-d26", "-mt" + Runtime.getRuntime().availableProcessors()};
                ProcessBuilder p = new ProcessBuilder(procargs);
                ModManager.runProcess(p);
                processed++;
            }

            //Update the full distribution folder (assuming we are doing a delta build)
            if (!compressedfulloutputfolder.equals(compresseddeltaoutputfolder)) {
                ModManager.debugLogger.writeMessage("Building updatedelta folder for server upload");

                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Applying delta to full server package", null));
                for (File newfile : updatedFiles) {
                    if (!newfile.exists()) {
                        continue; //it was deleted in this package, existed in old one. reverse update made it look like this file was required for download
                    }
                    //copy from update to full
                    ModManager.debugLogger.writeMessage("Copying updated file from update package to full server package: " + newfile);
                    String relativePath = ResourceUtils.getRelativePath(newfile.getAbsolutePath(), mod.getModPath(), File.separator);
                    File updatedfile = new File(compresseddeltaoutputfolder + relativePath + ".lzma");
                    File oldfile = new File(compressedfulloutputfolder + relativePath + ".lzma");
                    FileUtils.deleteQuietly(oldfile);
                    FileUtils.copyFile(updatedfile, oldfile);
                }
                if (up != null) {
                    for (String delfile : up.getFilesToDelete()) {
                        File newfile = new File(compressedfulloutputfolder + delfile);
                        if (newfile.exists()) {
                            ModManager.debugLogger.writeMessage("Update package says to delete existing file: " + delfile);
                        }
                    }
                }
            }

            //Prepare manifest
            ModManager.debugLogger.writeMessage("Preparing to generate manifest file (hashes, lzma hashes, sizes, sideloads)");
            try {
                docBuilder = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                ModManager.debugLogger.writeErrorWithException("Parser Configuration Error (dafuq?):", e);
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Parser Configuration Error (See log)"));
                return "";
            }

            Document modDoc = docBuilder.newDocument();
            Element rootElement = modDoc.createElement("mod");
            rootElement.setAttribute("type", "classic");
            rootElement.setAttribute("version", Double.toString(mod.getVersion()));
            rootElement.setAttribute("updatecode", Integer.toString(mod.getClassicUpdateCode()));
            rootElement.setAttribute("folder", mod.getServerModFolder());
            rootElement.setAttribute("manifesttype", "full");
            if (changelog != null) {
                rootElement.setAttribute("changelog", changelog);
            }
            if (mod.getSideloadURL() != null) {
                //already validated above
                Element sideloadElement = modDoc.createElement("sideloadurl");
                sideloadElement.setTextContent(mod.getSideloadURL());
                rootElement.appendChild(sideloadElement);

            }

            processed = 1;
            numFiles = newversionfiles.size();

            //PREPARE SIDELOAD PACKAGE
            boolean hassideload = false;
            for (File file : newversionfiles) {
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Preparing sideload package"));
                String srcFile = file.getAbsolutePath();
                String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
                String normalizedRelativePath = relativePath.replaceAll("\\\\", "/");

                if (mod.getSideloadOnlyTargets().contains(normalizedRelativePath)) {
                    hassideload = true;
                    File sideloadfile = new File(sideloadoutputfolder + relativePath);
                    sideloadfile.getParentFile().mkdirs();
                    FileUtils.deleteQuietly(sideloadfile);
                    FileUtils.copyFile(new File(srcFile), sideloadfile);
                }
            }
            if (hassideload) {
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Building sideload package"));
                File moddesc = new File(mod.getModPath() + "moddesc.ini");
                //DECREMENT VERSION SO SERVER VERSION APPEARS AS UPDATE.
                Wini ini = new Wini(moddesc);
                ini.put("ModInfo", "modver", Math.max(0.001, Math.floor((mod.getVersion() - 0.01) * 100) / 100));
                ini.store(new File(sideloadoutputfolder + "moddesc.ini"));

                String sideload7z = ModManager.appendSlash(new File(sideloadoutputfolder).getParent()) + foldername + "-sideload.7z";
                FileUtils.deleteQuietly(new File(sideload7z));
                String[] procargs = {"cmd", "/c", "start", "Building Sideload Package", ModManager.getToolsDir() + "7z", "a", "-r", "-mx9", "-mmt", sideload7z,
                        sideloadoutputfolder};
                ProcessBuilder p = new ProcessBuilder(procargs);
                ModManager.runProcess(p);
            }
            //GENERATE MANIFEST
            ModManager.debugLogger.writeMessage("Generating full manifest");
            for (File file : newversionfiles) {
                publish(new ThreadCommand("SET_BOTTOM_TEXT", "Building server manifest [" + processed + "/" + numFiles + "]"));

                String srcFile = file.getAbsolutePath();
                String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
                String lzmafile = compressedfulloutputfolder + relativePath + ".lzma";
                if (new File(lzmafile).exists()) { //Skip files not staged
                    Element fileElement = modDoc.createElement("sourcefile");
                    try {
                        fileElement.setAttribute("hash", MD5Checksum.getMD5Checksum(srcFile));
                        fileElement.setAttribute("size", Long.toString(new File(srcFile).length()));
                        fileElement.setAttribute("lzmahash", MD5Checksum.getMD5Checksum(lzmafile));
                        fileElement.setAttribute("lzmasize", Long.toString(new File(lzmafile).length()));
                        String normalizedRelativePath = relativePath.replaceAll("\\\\", "/");
                        if (mod.getSideloadOnlyTargets().contains(normalizedRelativePath)) {
                            fileElement.setAttribute("sideloadonly", "true");
                        }
                    } catch (DOMException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        ModManager.debugLogger.writeErrorWithException("Exception while generating part of manifest: ", e);
                    }
                    fileElement.setTextContent(relativePath);
                    rootElement.appendChild(fileElement);
                } else {
                    ModManager.debugLogger.writeMessage("Skipping file for manifest generating (was not compressed): " + relativePath);
                }
                processed++;
            }

            //add blacklisted files
            for (String str : mod.getBlacklistedFiles()) {
                Element blacklistedelement = modDoc.createElement("blacklistedfile");
                blacklistedelement.setTextContent(str);
                rootElement.appendChild(blacklistedelement);
            }

            FileUtils.deleteDirectory(new File(mod.getModPath()));

            long finishTime = System.currentTimeMillis();
            ModManager.debugLogger.writeMessage("Manifest ready. Took " + ((finishTime - startTime) / 1000) + " seconds.");

            modDoc.appendChild(rootElement);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            String manifest = ModMakerCompilerWindow.docToString(modDoc);
            manifestFile = new File(ModManager.getME3TweaksUpdaterServiceFolder() + "Manifests" + File.separator + foldername + ".xml");
            manifestFile.getParentFile().mkdirs();
            FileUtils.writeStringToFile(manifestFile, manifest);
            clpbrd.setContents(new StringSelection(manifest), null);

            ModManager.debugLogger.writeMessage(manifest);

            publish(new ThreadCommand("SET_BOTTOM_TEXT", mod.getModName() + " prepared for updater service"));
            if (forcepushtoserverResult == 1) {
                ResourceUtils.openFolderInExplorer(ModManager.getME3TweaksUpdaterServiceFolder());
            }
            return "OK";
        }

        @Override
        protected void process(List<ThreadCommand> chunks) {
            for (ThreadCommand command : chunks) {
                switch (command.getCommand()) {
                    case "MANIFEST_ON_SERVER_NEWER_OR_SAME":
                        Object[] choices = {"Force push to server", "Prepare locally only", "Cancel"};
                        String message = "<html><div style='width: 250px'>The manifest on the ME3Tweaks Updater Service is the same version or higher than the current mod version you are uploading.<br>" +
                                "Current server version: " + command.getMessage() + "<br>" +
                                "Local version you are preparing to upload: " + mod.getVersion() + "<br><br>" +
                                "You can force your version to upload to the server, prepare the update locally on disk without pushing to the server, or cancel this operation.<div></html>";
                        synchronized (lock) {
                            forcepushtoserverResult = JOptionPane.showOptionDialog(ModManagerWindow.ACTIVE_WINDOW, message, "Mod same or newer on server", JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                            lock.notifyAll(); //wake up thread
                        }
                        break;
                    case "SET_BOTTOM_TEXT":
                        ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(command.getMessage());
                        break;
                }
            }
        }

        @Override
        public void done() {
            if (!aborted) {
                ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
            }
            try {
                String result = get();
                if (result != null && result.equals("")) {
                    //FAILED
                    ModManager.debugLogger.writeError("Failed to build server packages.");
                    //ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to prepare mod for updater service");
                } else {
                    if (result != null && result.equals("OK") && (forcepushtoserverResult == -1 || forcepushtoserverResult == 0)) {
                        new ME3TweaksUpdaterServiceWindow(mod, manifestFile, compressedfulloutputfolder, compresseddeltaoutputfolder, updatedFiles);
                    }
                }
            } catch (Exception e) {
                ModManager.debugLogger.writeErrorWithException("Error while creating manifest: ", e);
            }
        }

    }

    public static ArrayList<UpdatePackage> validateLatestAgainstServer(ArrayList<Mod> mods, AllModsDownloadTask allModsDownloadTask) {
        String updateURL = "https://me3tweaks.com/mods/getlatest_batch";
        ModManager.debugLogger.writeMessage("Checking for updates of the following mods:");
        ArrayList<Mod> modmakerMods = new ArrayList<>();
        ArrayList<Mod> classicMods = new ArrayList<>();
        for (Mod mod : mods) {
            ModManager.debugLogger.writeMessage(mod.getModMakerCode() > 0 ? mod.getModMakerCode() + " " + mod.getModName() + " " + mod.getVersion() + "(ModMaker)"
                    : mod.getClassicUpdateCode() + " " + mod.getModName() + " " + mod.getVersion() + " (Classic)");
            if (mod.getModMakerCode() > 0) {
                modmakerMods.add(mod);
            } else {
                classicMods.add(mod);
            }
        }
        Document doc = getOnlineInfo(updateURL, modmakerMods, classicMods);
        if (allModsDownloadTask != null) {
            allModsDownloadTask.setManifestDownloaded();
        }
        ArrayList<UpdatePackage> updates = new ArrayList<>();
        for (Mod mod : modmakerMods) {
            UpdatePackage update = checkForModMakerUpdate(mod, doc);
            if (update != null) {
                updates.add(update);
            }
        }

        for (Mod mod : classicMods) {
            if (allModsDownloadTask != null) {
                allModsDownloadTask.publishUpdate(mod.getModName());
            }
            UpdatePackage update = checkForClassicUpdate(mod, doc, allModsDownloadTask);
            if (update != null) {
                updates.add(update);
            }
        }

        return updates;
    }

    private static UpdatePackage checkForModMakerUpdate(Mod mod, Document doc) {
        // got document, now parse metainfo
        if (doc != null) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element modElem = null;
            try {
                modElem = (Element) xPath.evaluate("/updatemanifest/modmakermod[@id=" + mod.getModMakerCode() + "]", doc, XPathConstants.NODE);
                if (modElem == null) {
                    ModManager.debugLogger.writeError("Mod not found in update manifest " + mod.getModMakerCode() + " " + mod.getModName());
                    return null;
                }
            } catch (XPathExpressionException e1) {
                ModManager.debugLogger.writeErrorWithException("Xpath Expression Error: ", e1);
                return null;
            }

            // for all mods in serverlist
            double serverModVer = Double.parseDouble(modElem.getAttribute("version"));
            String serverModName = modElem.getAttribute("name");
            String changelog = modElem.getAttribute("changelog");
            if (mod.getVersion() >= serverModVer) {
                ModManager.debugLogger.writeMessage(mod.getModName() + " up to date. Local version: " + mod.getVersion() + " Server Version: " + serverModVer);
                return null; // not an update
            } else {
                ModManager.debugLogger.writeMessage(mod.getModName() + " - ModMaker Mod is outdated, local:" + mod.getVersion() + " server: " + serverModVer);
                return new UpdatePackage(mod, serverModName, serverModVer, changelog);
            }
        } else {
            ModManager.debugLogger.writeMessage("XML document from server was null.");
        }
        return null;
    }

    private static UpdatePackage checkForClassicUpdate(Mod mod, Document doc, AllModsUpdateWindow.AllModsDownloadTask amdt) {
        // got document, now parse metainfo
        if (doc != null) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element modElem = null;
            try {
                modElem = (Element) xPath.evaluate("/updatemanifest/mod[@updatecode=" + mod.getClassicUpdateCode() + "]", doc, XPathConstants.NODE);
                if (modElem == null) {
                    modElem = (Element) xPath.evaluate("/mod[@updatecode=" + mod.getClassicUpdateCode() + "]", doc, XPathConstants.NODE);
                    if (modElem == null) {
                        ModManager.debugLogger.writeError("Mod not found in update manifest " + mod.getClassicUpdateCode() + " " + mod.getModName());
                        return null;
                    }
                }
            } catch (XPathExpressionException e1) {
                ModManager.debugLogger.writeErrorWithException("Xpath Expression Error: ", e1);
                return null;
            }

            // for all mods in serverlist
            double serverModVer = Double.parseDouble(modElem.getAttribute("version"));
            String serverFolder = modElem.getAttribute("folder");
            String manifesttype = modElem.getAttribute("manifesttype");
            boolean isFullManifest = manifesttype.equals("full"); //currently unused
            String changelog = modElem.getAttribute("changelog");
            if (mod.getVersion() >= serverModVer) {
                ModManager.debugLogger.writeMessage(mod.getModName() + " is up to date");
                return null; // not an update
            }
            ModManager.debugLogger.writeMessage("Mod is outdated, local:" + mod.getVersion() + " server: " + serverModVer);
            // build manifest of files
            ArrayList<ManifestModFile> serverFiles = new ArrayList<ManifestModFile>();
            String sideloadURL = null;
            try {
                sideloadURL = xPath.evaluate("sideloadurl", modElem);
            } catch (XPathExpressionException e1) {
                ModManager.debugLogger.writeErrorWithException("Error trying to find sideload url in manifest:", e1);
            }

            NodeList serverFileList = modElem.getElementsByTagName("sourcefile");

            //Build list of file objects for comparison
            int numTotalFiles = serverFileList.getLength();
            int numCheckedFiles = 0;
            for (int j = 0; j < numTotalFiles; j++) {
                Element fileElem = (Element) serverFileList.item(j);
                String svrHash = fileElem.getAttribute("hash");
                long srvSize = Long.parseLong(fileElem.getAttribute("size"));
                String svrCompressedHash = fileElem.getAttribute("lzmahash");
                long svrCompressedSize = -1;
                try {
                    svrCompressedSize = Long.parseLong(fileElem.getAttribute("lzmasize"));
                } catch (NumberFormatException e) {
                    //not stored on server as LZMA
                }

                ManifestModFile metafile = new ManifestModFile(fileElem.getTextContent(), svrHash, srvSize, svrCompressedHash, svrCompressedSize);
                if (fileElem.getAttribute("sideloadonly") != null && !fileElem.getAttribute("sideloadonly").equals("")) {
                    metafile.setSideloadOnly(true);
                }
                serverFiles.add(metafile);
            }
            String modpath = ModManager.appendSlash(mod.getModPath());

            ArrayList<String> filesToRemove = new ArrayList<String>();
            NodeList serverBlacklist = modElem.getElementsByTagName("blacklistedfile");
            for (int j = 0; j < serverBlacklist.getLength(); j++) {
                Element fileElem = (Element) serverBlacklist.item(j);
                String blacklisted = fileElem.getTextContent();
                if (blacklisted.contains("..")) {
                    //Malicious attempt possible
                    ModManager.debugLogger.writeError("Server indicates a file with path .. is blacklisted. The file path indicated on the server is: " + blacklisted
                            + "\nThis may be a malicious piece of data from the server. This file will be skipped");
                    continue;
                }
                File blacklistedlocalfile = new File(modpath + blacklisted);
                ModManager.debugLogger.writeMessage("Checking for blacklisted file: " + blacklistedlocalfile + " exits? " + blacklistedlocalfile.exists());

                if (blacklistedlocalfile.exists()) {
                    ModManager.debugLogger.writeMessage("Blacklisted file exists and will be deleted: " + blacklisted);
                    filesToRemove.add(modpath + blacklisted);
                }
            }

            ModManager.debugLogger.writeMessage("Number of files in manifest: " + serverFiles.size());

            // get list of new files
            ArrayList<ManifestModFile> newFiles = new ArrayList<ManifestModFile>();
            ArrayList<ManifestModFile> filesUpToDate = new ArrayList<ManifestModFile>();

            for (ManifestModFile mf : serverFiles) {
                numCheckedFiles++;
                if (amdt != null) {
                    amdt.setUpdateCalculationProgress(numCheckedFiles, numTotalFiles);
                }
                File localFile = new File(modpath + mf.getRelativePath());

                // check existence
                if (!localFile.exists()) {
                    newFiles.add(mf);
                    ModManager.debugLogger.writeMessage(mf.getRelativePath() + " does not exist locally, adding to update list");
                    continue;
                }

                // check size
                if (localFile.length() != mf.getSize()) {
                    newFiles.add(mf);
                    ModManager.debugLogger
                            .writeMessage(mf.getRelativePath() + " size has changed (local: " + localFile.length() + " | server: " + mf.getSize() + "), adding to update list");
                    continue;
                }

                // check hash
                try {
                    if (!MD5Checksum.getMD5Checksum(localFile.getAbsolutePath()).equals(mf.getHash())) {
                        newFiles.add(mf);
                        ModManager.debugLogger.writeMessage(mf.getRelativePath() + " hash is different, adding to update list");
                        continue;
                    }
                } catch (Exception e) {
                    ModManager.debugLogger.writeError("Exception generating MD5.");
                    ModManager.debugLogger.writeException(e);
                }
                ModManager.debugLogger.writeMessage(mf.getRelativePath() + " is up to date");
                filesUpToDate.add(mf);
            }

            // check for files that DON'T exist on the server
            if (isFullManifest) {
                ModManager.debugLogger.writeMessage("Checking for files that are no longer necessary");
                for (ModJob job : mod.getJobs()) {
                    for (String srcFile : job.getFilesToReplace()) {
                        String relativePathLowercase = ResourceUtils.getRelativePath(srcFile, modpath, File.separator).toLowerCase().replaceAll("\\\\", "/");
                        String relativePath = ResourceUtils.getRelativePath(srcFile, modpath, File.separator).replaceAll("\\\\", "/");
                        boolean existsOnServer = false;
                        for (ManifestModFile mf : serverFiles) {
                            if (mf.getRelativePath().toLowerCase().equals(relativePathLowercase)) {
                                existsOnServer = true;
                                continue;
                            }
                        }
                        if (!existsOnServer) {
                            // file needs to be removed
                            ModManager.debugLogger.writeMessage(relativePath + " is not in updated version of mod on server, marking for removal");
                            filesToRemove.add(relativePath);
                        }
                    }
                    for (AlternateFile altFile : job.getAlternateFiles()) {
                        String relativePathLower =  altFile.getAltFile().replaceAll("\\\\", "/").toLowerCase();
                        //String relativePath =  ResourceUtils.getRelativePath(altFile.getAltFile(), modpath, File.separator).toLowerCase().replaceAll("\\\\", "/");
                        boolean existsOnServer = false;
                        for (ManifestModFile mf : serverFiles) {
                            if (mf.getRelativePath().toLowerCase().equals(relativePathLower)) {
                                existsOnServer = true;
                                continue;
                            }
                        }
                        if (!existsOnServer) {
                            // file needs to be removed
                            ModManager.debugLogger.writeMessage(relativePathLower + " is not in updated version of mod on server (job altfile), marking for removal");
                            filesToRemove.add(altFile.getAltFile());
                        }
                    }
                }
            }

            for (AlternateFile altFile : mod.getAlternateFiles()) {
                if (altFile.getOperation().equals(AlternateFile.OPERATION_NOINSTALL)) {
                    continue;
                }
                String relativePathLower =  altFile.getAltFile().toLowerCase().replaceAll("\\\\", "/");
                String relativePath =  altFile.getAltFile().toLowerCase().replaceAll("\\\\", "/");
                boolean existsOnServer = false;
                for (ManifestModFile mf : serverFiles) {
                    if (mf.getRelativePath().toLowerCase().equals(relativePathLower)) {
                        existsOnServer = true;
                        continue;
                    }
                }
                if (!existsOnServer) {
                    // file needs to be removed
                    ModManager.debugLogger.writeMessage(relativePathLower + " is not in updated version of mod on server (mod altfile), marking for removal");
                    filesToRemove.add(altFile.getAltFile());
                }
            }

            ModManager.debugLogger.writeMessage("Update check complete, number of outdated/missing files: " + newFiles.size() + ", files to remove: " + filesToRemove.size());
            if (newFiles.size() == 0 && filesToRemove.size() == 0) {
                //server lists update, but local copy matches server
                return null;
            }
            UpdatePackage up = new UpdatePackage(mod, serverModVer, newFiles, filesUpToDate, filesToRemove,serverFolder, changelog);
            if (sideloadURL != null) {
                up.setSideloadURL(sideloadURL);
            }
            for (ManifestModFile mf : newFiles) {
                if (mf.isSideloadOnly()) {
                    //REQUIRES SIDELOAD!
                    ModManager.debugLogger.writeError(
                            "This mod has a file marked for update that the developer has specified as sideload-only. The update cannot proceed until all sideload only files match their server counterparts. Advertising sideload update. The URL for the sideloading is "
                                    + sideloadURL);
                    up.setRequiresSideload(true);
                }
            }
            return up;

        } else {
            ModManager.debugLogger.writeMessage("Server returned a null document. Guess there's no update.");
        }
        return null;

    }// end classic update

    private static Document getOnlineInfo(String updateURL, boolean modmakerMod, int updatecode) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        // params.add(new BasicNameValuePair("updatecode",
        // Integer.toString(mod.getClassicUpdateCode())));
        params.add(new BasicNameValuePair((modmakerMod ? "modmaker" : "classic") + "updatecode[]", Integer.toString(updatecode)));
        URIBuilder urib;
        String responseString = null;
        try {
            urib = new URIBuilder(updateURL);
            urib.setParameters(params);
            HttpClient httpClient = HttpClientBuilder.create().build();
            URI uri = urib.build();
            ModManager.debugLogger.writeMessage("Getting latest mod info from link: " + uri.toASCIIString());
            HttpResponse response = httpClient.execute(new HttpGet(uri));
            responseString = new BasicResponseHandler().handleResponse(response);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (responseString == null) {
            // error occured
            return null;
        }

        // got XML, build document for reading
        Document doc = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseString));
            doc = db.parse(is);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (doc == null) {
            return null;
        }

        return doc;
    }

    private static Document getOnlineInfo(String updateURL, ArrayList<Mod> modmakerMods, ArrayList<Mod> classicMods) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        // params.add(new BasicNameValuePair("updatecode",
        // Integer.toString(mod.getClassicUpdateCode())));

        for (Mod mmMod : modmakerMods) {
            params.add(new BasicNameValuePair("modmakerupdatecode[]", Integer.toString(mmMod.getModMakerCode())));
        }
        for (Mod mmMod : classicMods) {
            params.add(new BasicNameValuePair("classicupdatecode[]", Integer.toString(mmMod.getClassicUpdateCode())));
        }

        URIBuilder urib;
        String responseString = null;
        try {
            urib = new URIBuilder(updateURL);
            urib.setParameters(params);
            HttpClient httpClient = HttpClientBuilder.create().build();
            URI uri = urib.build();
            ModManager.debugLogger.writeMessage("Getting latest mod info from link: " + uri.toASCIIString());
            HttpResponse response = httpClient.execute(new HttpGet(uri));
            responseString = new BasicResponseHandler().handleResponse(response);
            ModManager.debugLogger.writeMessageConditionally("Response from server:\n" + responseString, ModManager.LOG_MOD_INIT);
        } catch (URISyntaxException e) {
            ModManager.debugLogger.writeErrorWithException("Error getting online mod update info:", e);
        } catch (ClientProtocolException e) {
            ModManager.debugLogger.writeErrorWithException("Error getting online mod update info:", e);
        } catch (IOException e) {
            ModManager.debugLogger.writeErrorWithException("Error getting online mod update info:", e);
        }

        if (responseString == null) {
            // error occured
            ModManager.debugLogger.writeError("Server response was null.");
            return null;
        }

        // got XML, build document for reading
        Document doc = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseString));
            doc = db.parse(is);
        } catch (SAXException e) {
            ModManager.debugLogger.writeErrorWithException("Server responded with invalid XML:", e);
        } catch (IOException e) {
            ModManager.debugLogger.writeErrorWithException("IOException generating document:", e);
        } catch (ParserConfigurationException e) {
            ModManager.debugLogger.writeErrorWithException("Parser configuration error...?", e);
        }

        if (doc == null) {
            return null;
        }
        return doc;
    }
}
