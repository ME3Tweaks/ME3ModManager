package com.me3tweaks.modmanager.objects;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.ModManager;

/**
 * Mod Delta describes a change that can be performed on the original mods files
 * to make a slight variant. They make use of deltas like ModMaker
 * 
 * @author mgamerz
 *
 */
public class ModDelta {
	private String deltaFilepath;
	private String deltaDescription;
	private String deltaName;
	private boolean validDelta;

	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder dBuilder;
	private Document doc;

	public ModDelta(String file) {
		preprocessDelta(file);
	}

	/**
	 * Generates metadata information about mod. Gets name, description, files
	 * modified etc
	 * 
	 * @param file
	 */
	private void preprocessDelta(String file) {
		File deltaFile = new File(file);
		if (!deltaFile.exists()) {
			ModManager.debugLogger.writeError("Delta file does not exist: " + file);
			validDelta = false;
			return;
		}
		ModManager.debugLogger.writeMessageConditionally("Loading delta: " + file, ModManager.LOG_MOD_INIT);
		String deltaText;
		try {
			deltaText = FileUtils.readFileToString(new File(file));
		} catch (IOException e1) {
			validDelta = false;
			ModManager.debugLogger.writeErrorWithException("Delta could not be read from the filesystem.", e1);
			return;
		}

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(deltaText.getBytes("utf-8"))));
		} catch (SAXException | IOException e1) {
			validDelta = false;
			ModManager.debugLogger.writeErrorWithException("Delta could not be read from the filesystem.", e1);
			return;
		} //http://stackoverflow.com/questions/1706493/java-net-malformedurlexception-no-protocol
		catch (ParserConfigurationException e) {
			validDelta = false;
			ModManager.debugLogger.writeErrorWithException("Parser configuration exception...", e);
			return;
		}
		doc.getDocumentElement().normalize();

		//XPath to get name and description
		XPath xPath = XPathFactory.newInstance().newXPath();
		try {
			deltaName = xPath.evaluate("/ModDelta/DeltaInfo/Name", doc.getDocumentElement());
			deltaDescription = xPath.evaluate("/ModDelta/DeltaInfo/Description", doc.getDocumentElement());
		} catch (XPathExpressionException e1) {
			validDelta = false;
			ModManager.debugLogger.writeErrorWithException(
					"DeltaInfo paths were not found: /ModDelta/DeltaInfo requires a NAME and DESCRIPTION element.", e1);
			return;
		}
		
		validDelta = true;
	}
	
	public Document getDoc() {
		return doc;
	}

	public void parseDelta(){
		
	}

	@Override
	public String toString() {
		return "ModDelta [deltaFilepath=" + deltaFilepath + ", deltaDescription=" + deltaDescription + ", deltaName=" + deltaName + ", validDelta="
				+ validDelta + "]";
	}

	public String getDeltaFilepath() {
		return deltaFilepath;
	}

	public void setDeltaFilepath(String deltaFilepath) {
		this.deltaFilepath = deltaFilepath;
	}

	public String getDeltaDescription() {
		return deltaDescription;
	}

	public void setDeltaDescription(String deltaDescription) {
		this.deltaDescription = deltaDescription;
	}

	public String getDeltaName() {
		return deltaName;
	}

	public void setDeltaName(String deltaName) {
		this.deltaName = deltaName;
	}

	public boolean isValidDelta() {
		return validDelta;
	}

	public void setValidDelta(boolean validDelta) {
		this.validDelta = validDelta;
	}
}
