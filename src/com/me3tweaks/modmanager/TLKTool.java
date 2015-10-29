package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TLKTool {

	static class TLKNode {
		@Override
		public String toString() {
			return "TLKNode [id=" + id + ", xboxString=" + xboxString + ", pcString=" + pcString + ", index=" + index + "]";
		}

		String id, xboxString, pcString;
		int index;

		public TLKNode(String id, int index, String xboxString, String pcString) {
			this.id = id;
			this.index = index;
			this.xboxString = xboxString;
			this.pcString = pcString;
		}
	}

	public static void main(String[] args) throws Exception {

		decompileTLK();

		System.out.println("Scanning XML files");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc;

		String xbxFolder = "G:\\mods\\ControllerSupport\\TLK\\MOONSHINE\\";
		String origFolder = "G:\\mods\\ControllerSupport\\TLK\\ORIG\\";

		//String[] langs = new String[] { "DEU", "ESN", "FRA", "INT", "ITA", "POL", "RUS" };
		String[] langs = new String[] { "INT" };

		String origPrefix = "BIOGame_";
		String xboxPrefix = "DLC_CON_XBX_";
		int maxIndex = 8;
		ArrayList<TLKNode> differentIds = new ArrayList<TLKNode>();

		for (String lang : langs) {
			for (int currentIndex = 0; currentIndex < maxIndex; currentIndex++) {
				Document origDoc = dbFactory.newDocumentBuilder()
						.parse("file:///" + origFolder + File.separator + origPrefix + lang + File.separator + origPrefix + lang + currentIndex + ".xml");
				origDoc.getDocumentElement().normalize();

				Document xbxDoc = dbFactory.newDocumentBuilder()
						.parse("file:///" + xbxFolder + File.separator + xboxPrefix + lang + File.separator + xboxPrefix + lang + currentIndex + ".xml");
				xbxDoc.getDocumentElement().normalize();

				NodeList origStringNodes = origDoc.getElementsByTagName("String");
				NodeList xbxStringNodes = xbxDoc.getElementsByTagName("String");

				System.out.println("NODES: " + origStringNodes.getLength());

				for (int i = 0; i < origStringNodes.getLength(); i++) {
					Element oStringElem = (Element) origStringNodes.item(i);
					Element xStringElem = (Element) xbxStringNodes.item(i);

					if (!oStringElem.getTextContent().equals(xStringElem.getTextContent())) {
						String oID = oStringElem.getAttribute("id");
						System.out.println("Diff on ID " + oID);
						differentIds.add(new TLKNode(oID, currentIndex, xStringElem.getTextContent(), oStringElem.getTextContent()));
					}
				}
			}
		}

		//add changes file
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Document post700 = dbFactory.newDocumentBuilder().parse("file:///G:\\mods\\ControllerSupport\\TLK\\post7000.xml");
		post700.getDocumentElement().normalize();
		NodeList postStringNodes = post700.getElementsByTagName("string");
		for (int i = 0; i < postStringNodes.getLength(); i++) {
			Element postStringElem = (Element) postStringNodes.item(i);
			Element dataElem = (Element) xpath.evaluate("data", postStringElem, XPathConstants.NODE);
			Element idElem = (Element) xpath.evaluate("id", postStringElem, XPathConstants.NODE); // I get null here.
			differentIds.add(new TLKNode(idElem.getTextContent(), 9, dataElem.getTextContent(), "none"));
		}

		Document newDoc = dBuilder.newDocument();
		Element root = newDoc.createElement("Strings");
		for (TLKNode node : differentIds) {
			Element changedElement = newDoc.createElement("String");
			changedElement.setAttribute("id", node.id);
			changedElement.setTextContent(node.xboxString);
			root.appendChild(changedElement);
		}

		newDoc.appendChild(root);

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		tr.transform(new DOMSource(newDoc), new StreamResult(new FileOutputStream("G:\\mods\\ControllerSupport\\TLK\\changes.xml")));
	}

	private static void decompileTLK() {

		File dir = new File("H:\\Google Drive\\Mass Effect 3 Modding\\TLK\\DLC");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".tlk");
			}
		});

		for (File f : files) {
			ArrayList<String> commandBuilder = new ArrayList<String>();

			String compilerPath = ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe";
			commandBuilder.add(compilerPath);
			commandBuilder.add(f.getAbsolutePath());
			commandBuilder.add(f.getParent() + File.separator + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".xml");
			commandBuilder.add("--mode");
			commandBuilder.add("ToXml");
			commandBuilder.add("--no-ui");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			//Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			Process p = null;
			int returncode = 1;
			try {
				System.out.println("Executing: " + sb.toString());
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectErrorStream(true);
				p = pb.start();
				returncode = p.waitFor();
			} catch (IOException | InterruptedException e) {
			}

		}
		System.exit(0);
		
		
		dir = new File("G:\\mods\\ControllerSupport\\TLK\\ORIG\\");
		files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".tlk");
			}
		});

		for (File f : files) {
			ArrayList<String> commandBuilder = new ArrayList<String>();

			String compilerPath = ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe";
			commandBuilder.add(compilerPath);
			commandBuilder.add(f.getAbsolutePath());
			commandBuilder.add(f.getParent() + File.separator + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".xml");
			commandBuilder.add("--mode");
			commandBuilder.add("ToXml");
			commandBuilder.add("--no-ui");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			//Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			Process p = null;
			int returncode = 1;
			try {
				System.out.println("Executing: " + sb.toString());
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectErrorStream(true);
				p = pb.start();
				returncode = p.waitFor();
			} catch (IOException | InterruptedException e) {
			}

		}

		if (true)
			return;
	}
}
