package com.me3tweaks.modmanager.modupdater;

import java.awt.Toolkit;
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

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

import com.me3tweaks.modmanager.DeltaWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow.AllModsDownloadTask;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModDelta;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ModXMLTools {
	static DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder docBuilder;

	public static void generateXMLFileList(Mod mod) {
		new ManifestGeneratorUpdateCompressor(mod).execute();
	}

	private static class ManifestGeneratorUpdateCompressor extends SwingWorker<String, ThreadCommand> {
		private Mod mod;

		public ManifestGeneratorUpdateCompressor(Mod mod) {
			this.mod = mod;
			//Verify Deltas
			for (ModDelta delta : mod.getModDeltas()) {
				new DeltaWindow(mod, delta, true, false);
			}
		}

		@Override
		protected String doInBackground() throws Exception {
			if (mod.getModMakerCode() > 0) {
				System.err.println("ModMaker codes use the ID");
				publish(new ThreadCommand("ModMaker mods can't use classic updater", "ERROR"));
				return "";
			}

			if (mod.getClassicUpdateCode() <= 0) {
				ModManager.debugLogger.writeError("Mod must have an ME3Tweaks update code for updating. Contact FemShep if you need one.");
				publish(new ThreadCommand("Mod needs an updatecode in ModInfo", "ERROR"));
				return "";
			}

			if (mod.getVersion() <= 0) {
				ModManager.debugLogger.writeError("Mod must have a double/numeric version number for updating");
				publish(new ThreadCommand("Mod requires numeric version number", "ERROR"));
				return "";
			}

			//check blacklisted files
			for (String blf : mod.getBlacklistedFiles()) {
				File f = new File(mod.getModPath() + blf);
				if (f.exists()) {
					ModManager.debugLogger.writeError("A blacklisted file " + f
							+ " exists in mod folder. Blacklisted files will be deleted when the mod is applied. Remove this file from your distribution or remove the blacklisting in moddesc");
					publish(new ThreadCommand("Mod has a blacklisted file (check moddesc)", "ERROR"));
					return "";
				}
			}

			if (mod.getSideloadURL() != null) {
				String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
				UrlValidator urlValidator = new UrlValidator(schemes);
				if (!urlValidator.isValid(mod.getSideloadURL())) {
					if (mod.getSideloadOnlyTargets().size() > 0) {
						ModManager.debugLogger.writeError("Mod has invalid sideload URL, and some files are marked for sideloading only. Aborting manifest generation");
						publish(new ThreadCommand("Invalid Sideload URL. Manifest requires valid sideload URL", null));
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

			}

			File manifestFile = new File(
					System.getProperty("user.dir") + File.separator + "ME3TweaksUpdaterService" + File.separator + "Manifests" + File.separator + foldername + ".xml");

			//SIMULATE REVERSE UPDATE
			//CHECK FOR FILE EXISTENCE IN MOD UPDATE FOLDER, LZMA HASHES.
			//FILES THAT FAIL THIS WILL BE ADDED TO COLLECTION OF FILES TO UDPATE
			Collection<File> newversionfiles = FileUtils.listFiles(new File(mod.getModPath()), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			ArrayList<File> updatedfiles = new ArrayList<>();
			UpdatePackage up = null;
			if (manifestFile.exists()) {
				ModManager.debugLogger.writeMessage("Running reverse-update from old manifest");
				//check local files against old manifest. Changes will be considered updates and will be added to the updates folder.
				String oldmanifest = FileUtils.readFileToString(manifestFile);
				Document doc = null;
				try {
					DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(oldmanifest));
					doc = db.parse(is);
					double modversion = mod.getVersion();
					mod.setVersion(0.001);
					publish(new ThreadCommand("Calculating what files to use in delta update", null));
					up = checkForClassicUpdate(mod, doc, null);
					mod.setVersion(modversion); //restore, since this is pointe
					if (up != null) {
						for (ManifestModFile mf : up.getFilesToDownload()) {
							File f = new File(mod.getModPath() + File.separator + mf.getRelativePath());
							assert f.exists();
							updatedfiles.add(f);
						}
						for (String str : up.getFilesToDelete()) {
							File f = new File(str);
							if (f.exists()) {
								//reverse - new files have been added
								updatedfiles.add(f);
							}
						}
					} else {
						//no update.
						ModManager.debugLogger.writeMessage("No files to update. Exiting update service mod preparer thread.");
						publish(new ThreadCommand("No changes detected from previous generated manifest", null));
						return null;
					}
				} catch (Exception e) {
					ModManager.debugLogger.writeErrorWithException("Error loading old manifest. Performing a full compression. Error: ", e);
					for (File f : newversionfiles) {
						updatedfiles.add(f); //variants check
					}
				}
			} else {
				ModManager.debugLogger.writeMessage("No old manifest - all files treated as new.");
				for (File f : newversionfiles) {
					updatedfiles.add(f);
				}
			}

			//Compressing mod to /serverupdate

			long startTime = System.currentTimeMillis();
			String sideloadoutputfolder = System.getProperty("user.dir") + File.separator + "ME3TweaksUpdaterService" + File.separator + "Sideload" + File.separator + foldername
					+ File.separator;
			String compressedfulloutputfolder = System.getProperty("user.dir") + File.separator + "ME3TweaksUpdaterService" + File.separator + "Full" + File.separator + foldername
					+ File.separator;
			String compressedupdateoutputfolder = System.getProperty("user.dir") + File.separator + "ME3TweaksUpdaterService" + File.separator + "UpdateDelta" + File.separator
					+ foldername + File.separator;

			if (!manifestFile.exists()) {
				compressedupdateoutputfolder = compressedfulloutputfolder; //don't use update folder
			}

			//COMPRESSING FILES................
			ModManager.debugLogger.writeMessage("Compressing files...");

			File f = new File(compressedupdateoutputfolder);
			FileUtils.deleteDirectory(f);
			f.mkdirs();
			int numFiles = updatedfiles.size();
			int processed = 1;
			for (File file : updatedfiles) {
				if (FilenameUtils.getExtension(file.getAbsolutePath()).equals(".bak")) {
					processed++;
					continue;
				}
				publish(new ThreadCommand("Compressing " + FilenameUtils.getBaseName(file.getAbsolutePath()), processed + "/" + numFiles));
				String srcFile = file.getAbsolutePath();
				String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
				String outputFile = compressedupdateoutputfolder + relativePath + ".lzma";
				new File(outputFile).getParentFile().mkdirs();

				String[] procargs = { ModManager.getToolsDir() + "lzma.exe", "e", srcFile, outputFile, "-d26", "-mt" + Runtime.getRuntime().availableProcessors() };
				ProcessBuilder p = new ProcessBuilder(procargs);
				ModManager.runProcess(p);
				processed++;
			}

			//Update the full distribution folder (assuming we are doing a delta build)
			if (!compressedfulloutputfolder.equals(compressedupdateoutputfolder)) {
				ModManager.debugLogger.writeMessage("Building updatedelta folder for server upload");

				publish(new ThreadCommand("Applying delta to full server package", null));
				for (File newfile : updatedfiles) {
					if (!newfile.exists()) {
						continue; //it was deleted in this package, existed in old one. reverse update made it look like this file was required for download
					}
					//copy from update to full
					ModManager.debugLogger.writeMessage("Copying updated file from update package to full server package: " + newfile);
					String relativePath = ResourceUtils.getRelativePath(newfile.getAbsolutePath(), mod.getModPath(), File.separator);
					File updatedfile = new File(compressedupdateoutputfolder + relativePath + ".lzma");
					File oldfile = new File(compressedfulloutputfolder + relativePath + ".lzma");
					FileUtils.deleteQuietly(oldfile);
					FileUtils.copyFile(updatedfile, oldfile);
				}
				if (up != null) {
					for (String delfile : up.getFilesToDelete()) {
						File newfile = new File(compressedfulloutputfolder + delfile);
						if (newfile.exists()) {
							/*
							 * ModManager.debugLogger.writeMessage(
							 * "Deleting unnecessary file from full server package: "
							 * + delfile); FileUtils.deleteQuietly(new
							 * File(compressedfulloutputfolder + delfile));
							 * FileUtils.copyFile(new File(delfile), new
							 * File(compressedfulloutputfolder + delfile));
							 */
							System.out.println("Update package says to delete existing file: " + delfile);
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
				publish(new ThreadCommand("Parser Configuration Error (See log)", "ERROR"));
				return null;
			}

			Document modDoc = docBuilder.newDocument();
			Element rootElement = modDoc.createElement("mod");
			rootElement.setAttribute("type", "classic");
			rootElement.setAttribute("version", Double.toString(mod.getVersion()));
			rootElement.setAttribute("updatecode", Integer.toString(mod.getClassicUpdateCode()));
			rootElement.setAttribute("folder", mod.getServerModFolder());
			rootElement.setAttribute("manifesttype", "full");
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
				publish(new ThreadCommand("Preparing sideload package"));
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
				publish(new ThreadCommand("Building sideload package"));
				File moddesc = new File(mod.getModPath() + "moddesc.ini");
				//DECREMENT VERSION SO SERVER VERSION APPEARS AS UPDATE.
				Wini ini = new Wini(moddesc);
				ini.put("ModInfo", "modver", Math.max(0.001, Math.floor((mod.getVersion() - 0.01) * 100) / 100));
				ini.store(new File(sideloadoutputfolder + "moddesc.ini"));

				String sideload7z = ModManager.appendSlash(new File(sideloadoutputfolder).getParent()) + foldername + "-sideload.7z";
				FileUtils.deleteQuietly(new File(sideload7z));
				String[] procargs = { "cmd", "/c", "start", "Building Sideload Package", ModManager.getToolsDir() + "7z", "a", "-r", "-mx9", "-mmt", sideload7z,
						sideloadoutputfolder };
				ProcessBuilder p = new ProcessBuilder(procargs);
				ModManager.runProcess(p);
			}
			//GENERATE MANIFEST
			ModManager.debugLogger.writeMessage("Generating full manifest");
			for (File file : newversionfiles) {
				publish(new ThreadCommand("Building server manifest", processed + "/" + numFiles));

				String srcFile = file.getAbsolutePath();
				String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
				Element fileElement = modDoc.createElement("sourcefile");
				try {
					fileElement.setAttribute("hash", MD5Checksum.getMD5Checksum(srcFile));
					fileElement.setAttribute("size", Long.toString(new File(srcFile).length()));
					fileElement.setAttribute("lzmahash", MD5Checksum.getMD5Checksum(compressedfulloutputfolder + relativePath + ".lzma"));
					fileElement.setAttribute("lzmasize", Long.toString(new File(compressedfulloutputfolder + relativePath + ".lzma").length()));
					String normalizedRelativePath = relativePath.replaceAll("\\\\", "/");
					if (mod.getSideloadOnlyTargets().contains(normalizedRelativePath)) {
						fileElement.setAttribute("sideloadonly", "true");
					}
				} catch (DOMException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				fileElement.setTextContent(relativePath);
				rootElement.appendChild(fileElement);
				processed++;
			}

			//add blacklisted files
			for (String str : mod.getBlacklistedFiles()) {
				Element blacklistedelement = modDoc.createElement("blacklistedfile");
				blacklistedelement.setTextContent(str);
				rootElement.appendChild(blacklistedelement);
			}

			long finishTime = System.currentTimeMillis();
			ModManager.debugLogger.writeMessage("Manifest ready. Took " + ((finishTime - startTime) / 1000) + " seconds.");

			modDoc.appendChild(rootElement);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			String manifest = ModMakerCompilerWindow.docToString(modDoc);
			manifestFile.getParentFile().mkdirs();
			FileUtils.writeStringToFile(manifestFile, manifest);
			clpbrd.setContents(new StringSelection(manifest), null);

			publish(new ThreadCommand(mod.getModName() + " prepared for updater service", null));
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(chunks.get(chunks.size() - 1).getCommand()
					+ (chunks.get(chunks.size() - 1).getMessage() != null ? " [" + chunks.get(chunks.size() - 1).getMessage() + "]" : ""));
		}

		@Override
		public void done() {
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Error while creating manifest: ", e);
			}
		}

	}

	/**
	 * Takes a mod and validates it against the server for latest versions (also
	 * checks locally against hashes) This method should be run in a background
	 * thread.
	 * 
	 * @param mod
	 *            Mod to check against
	 * @return null if up to date, otherwise updatepackage describing an
	 *         applicable update operation
	 */
	public static UpdatePackage validateLatestAgainstServer(Mod mod) {
		String updateURL = "https://me3tweaks.com/mods/getlatest";
		ModManager.debugLogger.writeMessage("=========Checking for update of " + mod.getModName() + "=========");
		if (mod.getModMakerCode() > 0) {
			Document doc = getOnlineInfo(updateURL, true, mod.getModMakerCode());
			return checkForModMakerUpdate(mod, doc);
		}
		if (mod.getModMakerCode() <= 0) {
			Document doc = getOnlineInfo(updateURL, false, mod.getClassicUpdateCode());
			return checkForClassicUpdate(mod, doc, null);
		}
		return null;
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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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
				System.out.println("Checking for blacklisted file: " + blacklistedlocalfile + " exits? " + blacklistedlocalfile.exists());

				if (blacklistedlocalfile.exists()) {
					ModManager.debugLogger.writeMessage("Blacklisted file exists and will be deleted: " + blacklisted);
					filesToRemove.add(modpath + blacklisted);
				}
			}

			ModManager.debugLogger.writeMessage("Number of files in manifest: " + serverFiles.size());

			// get list of new files
			ArrayList<ManifestModFile> newFiles = new ArrayList<ManifestModFile>();

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
			}

			// check for files that DON'T exist on the server
			if (isFullManifest) {
				ModManager.debugLogger.writeMessage("Checking for files that are no longer necessary");
				for (ModJob job : mod.getJobs()) {
					for (String srcFile : job.getFilesToReplace()) {
						String relativePath = ResourceUtils.getRelativePath(srcFile, modpath, File.separator).toLowerCase().replaceAll("\\\\", "/");
						boolean existsOnServer = false;
						for (ManifestModFile mf : serverFiles) {
							if (mf.getRelativePath().toLowerCase().equals(relativePath)) {
								existsOnServer = true;
								continue;
							}
						}
						if (!existsOnServer) {
							// file needs to be removed
							ModManager.debugLogger.writeMessage(relativePath + " is not in updated version of mod on server, marking for removal");
							filesToRemove.add(srcFile);
						}
					}
				}
			}

			ModManager.debugLogger.writeMessage("Update check complete, number of outdated/missing files: " + newFiles.size() + ", files to remove: " + filesToRemove.size());
			if (newFiles.size() == 0 && filesToRemove.size() == 0) {
				//server lists update, but local copy matches server
				return null;
			}
			UpdatePackage up = new UpdatePackage(mod, serverModVer, newFiles, filesToRemove, serverFolder, changelog);
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
		params.add(new BasicNameValuePair("updatecode", Integer.toString(updatecode)));
		params.add(new BasicNameValuePair("modtype", modmakerMod ? "modmaker" : "classic"));

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
			ModManager.debugLogger.writeMessage("Response from server:\n" + responseString);
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
