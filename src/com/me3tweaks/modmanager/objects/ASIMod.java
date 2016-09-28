package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Element;

import com.me3tweaks.modmanager.ModManager;

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

	public ASIMod(Element modVer, int updategroup) throws XPathExpressionException {
		name = xpath.evaluate("name", modVer);
		installName = xpath.evaluate("installedname", modVer);
		author = xpath.evaluate("author", modVer);
		description = xpath.evaluate("description", modVer);
		hash = xpath.evaluate("hash", modVer);
		sourceCode = xpath.evaluate("sourcecode", modVer);
		String strversion = xpath.evaluate("version", modVer);
		version = Double.parseDouble(strversion);

		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("updategroup", Integer.toString(updategroup)));
		params.add(new BasicNameValuePair("version", Double.toString(version)));
		try {
			URIBuilder urib = new URIBuilder("https://me3tweaks.com/mods/asi/getasi");
			urib.setParameters(params);
			downloadURL = urib.build().toString();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Failed to create the download link for this ASI! User will be unable to download it: "+name,e);
		}
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
		if (version > other.getVersion())
			return -1;
		if (version < other.getVersion())
			return 1;
		return 0;
	}
}
