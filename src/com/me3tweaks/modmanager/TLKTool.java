package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
				System.out.println("Processing index: " + currentIndex);
				Document origDoc = dbFactory.newDocumentBuilder().parse(
						"file:///" + origFolder + File.separator + origPrefix + lang + File.separator + origPrefix + lang + currentIndex + ".xml");
				origDoc.getDocumentElement().normalize();

				Document xbxDoc = dbFactory.newDocumentBuilder().parse(
						"file:///" + xbxFolder + File.separator + xboxPrefix + lang + File.separator + xboxPrefix + lang + currentIndex + ".xml");
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
		
		for (TLKNode node : differentIds) {
			System.out.println(node);
		}
	}

	private static void decompileTLK() {

		File dir = new File("G:\\mods\\ControllerSupport\\TLK\\MOONSHINE\\");
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
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectErrorStream(true);
				p = pb.start();
				returncode = p.waitFor();
			} catch (IOException | InterruptedException e) {
			}

		}

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
