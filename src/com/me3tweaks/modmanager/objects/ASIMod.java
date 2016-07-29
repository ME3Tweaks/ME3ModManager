package com.me3tweaks.modmanager.objects;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

/**
 * Represents an ASI mod from the manifest
 * 
 * @author Mgamerz
 *
 */
public class ASIMod implements Comparable<ASIMod> {
	@Override
	public String toString() {
		return name;
	}

	private String name, installName, author, description;
	private String downloadURL, hash, sourceCode;
	private double version = 0;
	private static XPath xpath = XPathFactory.newInstance().newXPath();

	public ASIMod(Element modVer) throws XPathExpressionException {
		name = xpath.evaluate("name", modVer);
		installName = xpath.evaluate("installedname", modVer);
		author = xpath.evaluate("author", modVer);
		description = xpath.evaluate("description", modVer);
		downloadURL = xpath.evaluate("downloadURL", modVer);
		hash = xpath.evaluate("hash", modVer);
		sourceCode = xpath.evaluate("sourcecode", modVer);
		String strversion = xpath.evaluate("version", modVer);
		version = Double.parseDouble(strversion);
	}

	public String getName() {
		return name;
	}

	public String getInstallName() {
		return installName;
	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public String getHash() {
		return hash;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public double getVersion() {
		return version;
	}

	@Override
	public int compareTo(ASIMod other) {
		if (version < other.getVersion())
			return -1;
		if (version > other.getVersion())
			return 1;
		return 0;
	}
}
