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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
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

	/**
	 * Generates a XML representation of a mod for storing on ME3Tweaks.com
	 * 
	 * @param mod
	 *            Mod to create XML for
	 * @return XML string of mod
	 */
	public static String generateModXMLList(Mod mod) {
		if (mod.getModMakerCode() > 0) {
			System.err.println("ModMaker codes use the ID");
			return "";
		}

		if (mod.getClassicUpdateCode() <= 0) {
			System.err.println("Mod does not have a classic update code set");
			return "";
		}

		if (mod.getVersion() <= 0) {
			System.err.println("Mod must have a double/numeric version number");
			return "";
		}

		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document modDoc = docBuilder.newDocument();
		Element rootElement = modDoc.createElement("mod");
		rootElement.setAttribute("type", "classic");
		rootElement.setAttribute("version", Double.toString(mod.getVersion()));
		rootElement.setAttribute("updatecode", Integer.toString(mod.getClassicUpdateCode()));
		rootElement.setAttribute("folder", "PUT_SERVER_FOLDER_HERE");
		rootElement.setAttribute("manifesttype", "full");

		for (ModJob job : mod.getJobs()) {
			for (String srcFile : job.getFilesToReplace()) {
				Element fileElement = modDoc.createElement("sourcefile");
				try {
					fileElement.setAttribute("hash", MD5Checksum.getMD5Checksum(srcFile));
					fileElement.setAttribute("size", Long.toString(new File(srcFile).length()));

				} catch (DOMException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				fileElement.setTextContent(ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator));
				rootElement.appendChild(fileElement);
			}
		}
		// add moddesc.ini
		Element fileElement = modDoc.createElement("sourcefile");
		File descFile = new File(mod.getDescFile());

		try {
			fileElement.setAttribute("hash", MD5Checksum.getMD5Checksum(mod.getDescFile()));
			fileElement.setAttribute("size", Long.toString(descFile.length()));
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		fileElement.setTextContent(ResourceUtils.getRelativePath(mod.getDescFile(), mod.getModPath(), File.separator));
		rootElement.appendChild(fileElement);

		// add coalesced if legacy
		if (mod.getCMMVer() <= 2.0) {
			// add moddesc.ini
			Element coalElement = modDoc.createElement("sourcefile");
			String coalStr = mod.getModPath() + File.separator + "Coalesced.bin";
			File coalFile = new File(coalStr);

			try {
				coalElement.setAttribute("hash", MD5Checksum.getMD5Checksum(coalStr));
				coalElement.setAttribute("size", Long.toString(coalFile.length()));
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			coalElement.setTextContent(ResourceUtils.getRelativePath(coalStr, mod.getModPath(), File.separator));
			rootElement.appendChild(coalElement);
		}

		//add deltas
		for (ModDelta delta : mod.getModDeltas()) {
			Element element = modDoc.createElement("sourcefile");
			File deltafile = new File(delta.getDeltaFilepath());

			try {
				element.setAttribute("hash", MD5Checksum.getMD5Checksum(delta.getDeltaFilepath()));
				element.setAttribute("size", Long.toString(deltafile.length()));
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			element.setTextContent(ResourceUtils.getRelativePath(delta.getDeltaFilepath(), mod.getModPath(), File.separator));
			rootElement.appendChild(element);
		}

		modDoc.appendChild(rootElement);
		return ModMakerCompilerWindow.docToString(modDoc);
	}

	public static void generateXMLFileList(Mod mod) {
		new ManifestGeneratorUpdateCompressor(mod).execute();
		/*
		 * if (mod.getModMakerCode() > 0) {
		 * System.err.println("ModMaker codes use the ID"); return ""; }
		 * 
		 * if (mod.getClassicUpdateCode() <= 0) {
		 * System.err.println("Mod does not have a classic update code set");
		 * return ""; }
		 * 
		 * if (mod.getVersion() <= 0) {
		 * System.err.println("Mod must have a double/numeric version number");
		 * return ""; }
		 * 
		 * try { docBuilder = docFactory.newDocumentBuilder(); } catch
		 * (ParserConfigurationException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * Document modDoc = docBuilder.newDocument(); Element rootElement =
		 * modDoc.createElement("mod"); rootElement.setAttribute("type",
		 * "classic"); rootElement.setAttribute("version",
		 * Double.toString(mod.getVersion()));
		 * rootElement.setAttribute("updatecode",
		 * Integer.toString(mod.getClassicUpdateCode()));
		 * rootElement.setAttribute("folder", "PUT_SERVER_FOLDER_HERE");
		 * rootElement.setAttribute("manifesttype", "full");
		 * 
		 * Collection<File> files = FileUtils.listFiles(new
		 * File(mod.getModPath()), TrueFileFilter.INSTANCE,
		 * TrueFileFilter.INSTANCE); for (File file : files) { String srcFile =
		 * file.getAbsolutePath(); Element fileElement =
		 * modDoc.createElement("sourcefile"); try {
		 * fileElement.setAttribute("hash",
		 * MD5Checksum.getMD5Checksum(srcFile));
		 * fileElement.setAttribute("size", Long.toString(new
		 * File(srcFile).length()));
		 * 
		 * } catch (DOMException e) { e.printStackTrace(); } catch (Exception e)
		 * { e.printStackTrace(); }
		 * fileElement.setTextContent(ResourceUtils.getRelativePath(srcFile,
		 * mod.getModPath(), File.separator));
		 * rootElement.appendChild(fileElement); }
		 * 
		 * modDoc.appendChild(rootElement); return
		 * ModMakerCompilerWindow.docToString(modDoc);
		 */
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
				System.err.println("Mod does not have a classic update code set");
				publish(new ThreadCommand("Mod needs an updatecode in ModInfo", "ERROR"));
				return "";
			}

			if (mod.getVersion() <= 0) {
				System.err.println("Mod must have a double/numeric version number");
				publish(new ThreadCommand("Mod requires numeric version number", "ERROR"));
				return "";
			}

			Collection<File> files = FileUtils.listFiles(new File(mod.getModPath()), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			//Compressing mod to /serverupdate

			String compressedoutputfolder = System.getProperty("user.dir") + File.separator + "serverupdate" + File.separator;
			File f = new File(compressedoutputfolder);
			FileUtils.deleteDirectory(f);
			f.mkdirs();
			int numFiles = files.size();
			int processed = 1;
			for (File file : files) {
				if (FilenameUtils.getExtension(file.getAbsolutePath()).equals(".bak")) {
					processed++;
					continue;
				}
				publish(new ThreadCommand("Compressing " + FilenameUtils.getBaseName(file.getAbsolutePath()), processed + "/" + numFiles));
				String srcFile = file.getAbsolutePath();
				String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
				String outputFile = compressedoutputfolder + relativePath + ".lzma";
				new File(outputFile).getParentFile().mkdirs();
				String[] procargs = { ModManager.getToolsDir() + "lzma.exe", "e", srcFile, outputFile, "-d26" };
				ProcessBuilder p = new ProcessBuilder(procargs);
				ModManager.runProcess(p);
				processed++;
			}

			//Generating XML Manifest
			try {
				docBuilder = docFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Document modDoc = docBuilder.newDocument();
			Element rootElement = modDoc.createElement("mod");
			rootElement.setAttribute("type", "classic");
			rootElement.setAttribute("version", Double.toString(mod.getVersion()));
			rootElement.setAttribute("updatecode", Integer.toString(mod.getClassicUpdateCode()));
			rootElement.setAttribute("folder", mod.getServerModFolder());
			rootElement.setAttribute("manifesttype", "full");
			processed = 1;
			for (File file : files) {
				publish(new ThreadCommand("Creating mod manifest", processed + "/" + numFiles));

				String srcFile = file.getAbsolutePath();
				String relativePath = ResourceUtils.getRelativePath(srcFile, mod.getModPath(), File.separator);
				Element fileElement = modDoc.createElement("sourcefile");
				try {
					fileElement.setAttribute("hash", MD5Checksum.getMD5Checksum(srcFile));
					fileElement.setAttribute("size", Long.toString(new File(srcFile).length()));
					fileElement.setAttribute("lzmahash", MD5Checksum.getMD5Checksum(compressedoutputfolder + relativePath + ".lzma"));
					fileElement.setAttribute("lzmasize", Long.toString(new File(compressedoutputfolder + relativePath + ".lzma").length()));
				} catch (DOMException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				fileElement.setTextContent(relativePath);
				rootElement.appendChild(fileElement);
				processed++;
			}

			modDoc.appendChild(rootElement);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(new StringSelection(ModMakerCompilerWindow.docToString(modDoc)), null);
			publish(new ThreadCommand("Manifest copied to clipboard, mod in /serverupdate.", "Done"));
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(chunks.get(chunks.size() - 1).getCommand() + " ["
					+ chunks.get(chunks.size() - 1).getMessage() + "]");
		}

		@Override
		public void done() {

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
		String updateURL;
		if (ModManager.IS_DEBUG) {
			updateURL = "http://webdev-mgamerz.c9.io/mods/getlatest";
		} else {
			updateURL = "https://me3tweaks.com/mods/getlatest";
		}
		ModManager.debugLogger.writeMessage("=========Checking for update of " + mod.getModName() + "=========");
		if (mod.getModMakerCode() > 0) {
			Document doc = getOnlineInfo(updateURL, true, mod.getModMakerCode());
			return checkForModMakerUpdate(mod, doc);
		}
		if (mod.getModMakerCode() <= 0) {
			Document doc = getOnlineInfo(updateURL, false, mod.getClassicUpdateCode());
			return checkForClassicUpdate(mod, doc);
		}
		return null;
	}

	public static ArrayList<UpdatePackage> validateLatestAgainstServer(ArrayList<Mod> mods, AllModsDownloadTask allModsDownloadTask) {
		String updateURL;
		if (ModManager.IS_DEBUG) {
			updateURL = "http://webdev-mgamerz.c9.io/mods/getlatest_batch";
		} else {
			updateURL = "https://me3tweaks.com/mods/getlatest_batch";
		}
		ModManager.debugLogger.writeMessage("Checking for updates of the following mods:");
		ArrayList<Mod> modmakerMods = new ArrayList<>();
		ArrayList<Mod> classicMods = new ArrayList<>();
		for (Mod mod : mods) {
			ModManager.debugLogger.writeMessage(mod.getModMakerCode() > 0 ? mod.getModMakerCode() + " " + mod.getModName() + " " + mod.getVersion()
					+ "(ModMaker)" : mod.getClassicUpdateCode() + " " + mod.getModName() + " " + mod.getVersion() + " (Classic)");
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
			UpdatePackage update = checkForClassicUpdate(mod, doc);
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
			if (mod.getVersion() >= serverModVer) {
				ModManager.debugLogger.writeMessage(mod.getModName() + " up to date. Local version: " + mod.getVersion() + " Server Version: "
						+ serverModVer);
				return null; // not an update
			} else {
				ModManager.debugLogger.writeMessage(mod.getModName() + " - ModMaker Mod is outdated, local:" + mod.getVersion() + " server: "
						+ serverModVer);
				return new UpdatePackage(mod, serverModName, serverModVer);
			}
		} else {
			ModManager.debugLogger.writeMessage("XML document from server was null.");
		}
		return null;
	}

	private static UpdatePackage checkForClassicUpdate(Mod mod, Document doc) {
		// got document, now parse metainfo
		if (doc != null) {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Element modElem = null;
			try {
				modElem = (Element) xPath.evaluate("/updatemanifest/mod[@updatecode=" + mod.getClassicUpdateCode() + "]", doc, XPathConstants.NODE);
				if (modElem == null) {
					ModManager.debugLogger.writeError("Mod not found in update manifest " + mod.getClassicUpdateCode() + " " + mod.getModName());
					return null;
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
			boolean isFullManifest = manifesttype.equals("full");
			if (mod.getVersion() >= serverModVer) {
				ModManager.debugLogger.writeMessage(mod.getModName() + " is up to date");
				return null; // not an update
			}
			ModManager.debugLogger.writeMessage("Mod is outdated, local:" + mod.getVersion() + " server: " + serverModVer);
			// build manifest of files
			ArrayList<ManifestModFile> serverFiles = new ArrayList<ManifestModFile>();
			NodeList serverFileList = modElem.getElementsByTagName("sourcefile");
			for (int j = 0; j < serverFileList.getLength(); j++) {
				Element fileElem = (Element) serverFileList.item(j);
				String svrHash = fileElem.getAttribute("hash");
				long srvSize = Long.parseLong(fileElem.getAttribute("size"));
				String svrCompressedHash = fileElem.getAttribute("lzmahash");
				System.out.println("COMPRESSED HASH: "+svrCompressedHash);
				long svrCompressedSize = -1;
				try {
					svrCompressedSize = Long.parseLong(fileElem.getAttribute("lzmasize"));
					System.out.println("COMPRESSED SIZE: "+svrCompressedSize);
				} catch (NumberFormatException e) {
					//not stored on server as LZMA
					System.err.println("Server manifest has no LZMA size (NFE)");
				}

				ManifestModFile metafile = new ManifestModFile(fileElem.getTextContent(), svrHash, srvSize, svrCompressedHash, svrCompressedSize);
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
					ModManager.debugLogger.writeError("Server indicates a file with path .. is blacklisted. The file path indicated on the server is: "+blacklisted+"\nThis may be a malicious piece of data from the server. This file will be skipped");
					continue;
				}
				ModManager.debugLogger.writeError("Blacklisted file: "+blacklisted);
				filesToRemove.add(modpath+blacklisted);
			}

			ModManager.debugLogger.writeMessage("Number of files in manifest: " + serverFiles.size());

			// get list of new files
			ArrayList<ManifestModFile> newFiles = new ArrayList<ManifestModFile>();

			for (ManifestModFile mf : serverFiles) {
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
					ModManager.debugLogger.writeMessage(mf.getRelativePath() + " size has changed (local: " + localFile.length() + " | server: "
							+ mf.getSize() + "), adding to update list");
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

/*				// Check legacy Coalesced.bin
				if (mod.getCMMVer() < 3.0 && mod.modsCoal()) {
					boolean existsOnServer = false;
					for (ManifestModFile mf : serverFiles) {
						if (mf.getRelativePath().toLowerCase().equals("Coalesced.bin")) {
							existsOnServer = true;
							continue;
						}
					}
					if (!existsOnServer) {
						// file needs to be removed
						ModManager.debugLogger.writeMessage("Coalesced.bin is not in updated version of mod on server, marking for removal");
						filesToRemove.add(ModManager.appendSlash(mod.getModPath()) + "Coalesced.bin");
					}
				}*/
			}

			ModManager.debugLogger.writeMessage("Update check complete, number of outdated/missing files: " + newFiles.size() + ", files to remove: "
					+ filesToRemove.size());
			if (newFiles.size() == 0 && filesToRemove.size() == 0) {
				//server lists update, but local copy matches server
				return null;
			}
			return new UpdatePackage(mod, serverModVer, newFiles, filesToRemove, serverFolder);

		} else {
			ModManager.debugLogger.writeMessage("Server returned a null document.");
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
