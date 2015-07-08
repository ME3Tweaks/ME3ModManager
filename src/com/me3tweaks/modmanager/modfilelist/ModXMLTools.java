package com.me3tweaks.modmanager.modfilelist;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.me3tweaks.modmanager.MD5Checksum;
import com.me3tweaks.modmanager.Mod;
import com.me3tweaks.modmanager.ModJob;
import com.me3tweaks.modmanager.ModMakerCompilerWindow;
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

	public static String downloadAndVerifyStandardModUpdates(ArrayList<Mod> allMods) {

		return "";
	}

	public static String prepareModMakerUpdateSubmission() {

	}

	public static String checkForModMakerUpdates() {

	}
}
