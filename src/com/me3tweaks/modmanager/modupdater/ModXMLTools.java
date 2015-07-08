package com.me3tweaks.modmanager.modupdater;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import com.me3tweaks.modmanager.MD5Checksum;
import com.me3tweaks.modmanager.Mod;
import com.me3tweaks.modmanager.ModJob;
import com.me3tweaks.modmanager.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ResourceUtils;

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
	public static String generateXMLList(Mod mod) {
		if (mod.getModMakerCode() > 0 ) {
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

		for (ModJob job : mod.getJobs()) {
			for (String srcFile : job.getNewFiles()) {
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
		//add moddesc.ini
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
		
		//add coalesced if legacy
		if (mod.getCMMVer() <= 2.0) {
			//add moddesc.ini
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
		
		modDoc.appendChild(rootElement);
		return ModMakerCompilerWindow.docToString(modDoc);
	}

	/**
	 * Takes a mod and validates it against the server for latest versions (also checks locally against hashes)
	 * This method should be run in a background thread.
	 * @param mod Mod to check against
	 * @return null if up to date, otherwise updatepackage describing an applicable update operation
	 */
	public static UpdatePackage validateLatestAgainstServer(Mod mod) {
		String updateURL;
		if (ModManager.IS_DEBUG) {
			updateURL = "http://webdev-mgamerz.c9.io/mods/getlatest";
		} else {
			updateURL = "http://me3tweaks.com/mods/getlatest";
		}
		ModManager.debugLogger.writeMessage("=========Checking for update of "+mod.getModName()+"=========");

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair("updatecode", Integer.toString(mod.getClassicUpdateCode())));
		params.add(new BasicNameValuePair("updatecode", Integer.toString(9)));

		
		URIBuilder urib;
		String responseString = null;
		try {
			urib = new URIBuilder(updateURL);
			urib.setParameters(params);
			HttpClient httpClient = HttpClientBuilder.create().build();
			URI uri = urib.build();
			//HttpResponse response = httpClient.execute(new HttpGet(uri));
			//responseString = new BasicResponseHandler().handleResponse(response);
			responseString = "<mod type=\"classic\" updatecode=\"6\" version=\"1.1\"><sourcefile hash=\"2d6afeb3f441f36812e5b6b29a92ff6f\" size=\"129193\">MP3\\Default_DLC_CON_MP3.bin</sourcefile><sourcefile hash=\"dd9364bcb818e2b19c1dfd9ff16f702e\" size=\"12052\">MP3\\PCConsoleTOC.bin</sourcefile><sourcefile hash=\"59e5c732c54ae6b85502825b32ce0e5b\" size=\"268966\">MP4\\Default_DLC_CON_MP4.bin</sourcefile><sourcefile hash=\"64ccc9b5e5a6689d391f4e0ee75e36a2\" size=\"31579\">MP4\\DLC_CON_MP4_INT.tlk</sourcefile><sourcefile hash=\"5df75de20a1d302c15ca93a359c066cb\" size=\"18188\">MP4\\PCConsoleTOC.bin</sourcefile><sourcefile hash=\"b516be02585deb1888084da2ecb3f20c\" size=\"873\">moddesc.ini</sourcefile><sourcefile hash=\"5ba2ac87b25aed7509b656f572677d24\" size=\"1701695\">Coalesced.bin</sourcefile></mod>";
			} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
/*		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();*/
		}
		
		if (responseString == null) {
			//error occured
			return null;
		}
		
		//got XML, build document for reading
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
		
		if (doc == null ) {
			return null;
		}
		
		//got document, now parse metainfo
		NodeList modList = doc.getElementsByTagName("mod");
		if (modList.getLength() < 1) {
			ModManager.debugLogger.writeMessage("XML response has no <mod> tags, error from server");
			return null;
		}
		
		//for all mods in serverlist
		for (int i = 0; i < modList.getLength(); i++) {
			Element modElem = (Element) modList.item(i);
			double serverModVer = Double.parseDouble(modElem.getAttribute("version"));
			String serverFolder = modElem.getAttribute("folder");
			if (mod.getVersion() >= serverModVer) {
				ModManager.debugLogger.writeMessage("Mod up to date");
				continue; //not an update
			}
			ModManager.debugLogger.writeMessage("Mod is outdated, local:"+mod.getVersion()+" server: "+serverModVer);
			//build manifest of files
			ArrayList<ManifestModFile> serverFiles = new ArrayList<ManifestModFile>();
			NodeList serverFileList = modElem.getElementsByTagName("sourcefile");
			for (int j = 0; j < serverFileList.getLength(); j++) {
				Element fileElem = (Element) serverFileList.item(j);
				ManifestModFile metafile = new ManifestModFile(fileElem.getTextContent(), fileElem.getAttribute("hash"), Long.parseLong(fileElem.getAttribute("size")));
				serverFiles.add(metafile);
			}
			
			ModManager.debugLogger.writeMessage("Number of files in manifest: "+serverFiles.size());
			
			//get list of new files
			ArrayList<ManifestModFile> newFiles = new ArrayList<ManifestModFile>();
			String modpath = ModManager.appendSlash(mod.getModPath());
			
			for (ManifestModFile mf : serverFiles) {
				File localFile = new File(modpath + mf.getRelativePath());
				
				//check existence
				if (!localFile.exists()){
					newFiles.add(mf);
					ModManager.debugLogger.writeMessage(mf.getRelativePath()+" does not exist, adding to update list");
					continue;
				}
				
				//check size
				if (localFile.length() != mf.getFilesize()) {
					newFiles.add(mf);
					ModManager.debugLogger.writeMessage(mf.getRelativePath()+" size has changed (local: "+localFile.length()+" | server: "+mf.getFilesize()+"), adding to update list");
					continue;
				}
				
				//check hash
				try {
					if (!MD5Checksum.getMD5Checksum(localFile.getAbsolutePath()).equals(mf.getHash())) {
						newFiles.add(mf);
						ModManager.debugLogger.writeMessage(mf.getRelativePath()+" hash is different, adding to update list");
						continue;
					}
				} catch (Exception e) {
					ModManager.debugLogger.writeError("Exception generating MD5.");
					ModManager.debugLogger.writeException(e);
				}
				ModManager.debugLogger.writeMessage(mf.getRelativePath()+" is up to date");
			}
			
			//check for files that DON'T exist on the server
			ArrayList<String> filesToRemove = new ArrayList<String>();
			for (ModJob job : mod.getJobs()) {
				for (String srcFile : job.getNewFiles()) {
					String relativePath = ResourceUtils.getRelativePath(srcFile, modpath, File.separator).toLowerCase();
					boolean existsOnServer = false;
					for (ManifestModFile mf : serverFiles){
						if (mf.getRelativePath().toLowerCase().equals(relativePath)){
							existsOnServer = true;
							continue;
						}
					}
					if (!existsOnServer){
						//file needs to be removed
						ModManager.debugLogger.writeMessage(relativePath+" is not in updated version of mod on server, marking for removal");
						filesToRemove.add(srcFile);
					}
				}
			}
			
			System.out.println("Update check complete, number of outdated/missing files: "+newFiles.size()+", files to remove: "+filesToRemove.size());
			return new UpdatePackage(mod, serverModVer, newFiles, filesToRemove, serverFolder);
		}
		
		return null; //shouldn't get here at this time
	}
	
	public static boolean executeUpdate(UpdatePackage update) {
		
	}

	public static String prepareModMakerUpdateSubmission() {

	}

	public static String checkForModMakerUpdates() {

	}
}
