package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

		//decompileTLK();
		//initialScan();
		subsetScan("C:\\Users\\Michael\\Desktop\\BIOGAME_COMMANDSTR.txt", "C:\\Users\\Michael\\Desktop\\patch1_int.xml");
		replacementScan("C:\\Users\\Michael\\Desktop\\patch1_int.xml", "C:\\Users\\Michael\\Desktop\\patch1_int_completed.xml");
	}

	private static void replacementScan(String inputFile, String outputFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + inputFile);
		origDoc.getDocumentElement().normalize();

		NodeList origStringNodes = (NodeList) xpath.evaluate("/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

		for (int i = 0; i < origStringNodes.getLength(); i++) {
			Node singleNode = (Node) origStringNodes.item(i);

			Element oStringElem = (Element) singleNode;
			String content = oStringElem.getTextContent();
			content = content.replace("([Shared_ShowMap])", "[XBoxB_Btn_A]");
			content = content.replace("([Mouse_Btn_R])", "[XBoxB_Btn_LT]");
			content = content.replace("[Mouse_Btn_R]", "[XBoxB_Btn_LT]");
			content = content.replace("([Shared_Shoot])", "[XBoxB_Btn_RT]");
			content = content.replace("([Shared_Action])", "[XBoxB_Btn_A]");
			content = content.replace("([PC_MoveForward])", "[XBoxB_Btn_LSUp]");
			content = content.replace("([Shared_Melee])", "[XBoxB_Btn_B]");
			content = content.replace("([Shared_Aim])", "[XBoxB_Btn_LT]");
			content = content.replace("([PC_StrafeLeft])", "[XBoxB_Btn_LSLeft]");
			content = content.replace("([PC_StrafeRight])", "[XBoxB_Btn_LSRight]");
			content = content.replace("([Shared_SquadFollow])", "[XBoxB_Btn_DPadD]");
			content = content.replace("([Shared_SquadAttack])", "[XBoxB_Btn_DPadU]");
			content = content.replace("([Shared_CoverTurn])", "[XBoxB_Btn_L3]");
			content = content.replace("([Shared_ExitAtlas])", "[XBoxB_Btn_B]");
			content = content.replace("([PC_HotKey5])", "[XBoxB_Btn_DPadU]");
			content = content.replace("([PC_HotKey6])", "[XBoxB_Btn_DPadR]");
			content = content.replace("([PC_HotKey7])", "[XBoxB_Btn_DPadD]");
			content = content.replace("([PC_HotKey8])", "[XBoxB_Btn_DPadL]");
			content = content.replace("([Shared_SquadMove1])", "[XBoxB_Btn_DPadL]");
			content = content.replace("([Shared_SquadMove2])", "[XBoxB_Btn_DPadR]");
			content = content.replace("([PC_EnterCommandMenu])", "[XBoxB_Btn_R3]");

			content = content.replace("([PC_SwapWeapon])", "[XBoxB_Btn_X]");
			content = content.replace("([Shared_Menu])", "[XBoxB_Btn_Start]");
			content = content.replace("([PC_Reload])", "[XBoxB_Btn_X]");
			content = content.replace("([PC_HotKey1])", "[XBoxB_Btn_LB]");
			content = content.replace("([PC_HotKey2])", "[XBoxB_Btn_RB]");
			content = content.replace("([PC_HotKey3])", "[XBoxB_Btn_Y]");
			content = content.replace("([PC_MoveBackward])", "[XBoxB_Btn_LSDown]");
			content = content.replace("([PC_NextWeapon])", "[XBoxB_Btn_X]");
			
			oStringElem.setTextContent(content);
		}

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		tr.transform(new DOMSource(origDoc), new StreamResult(new FileOutputStream(outputFile)));
	}

	/**
	 * Scans a Strings file for xbox entries and removes them
	 * 
	 * @param outputFile
	 * @param inputFile
	 * @throws Exception
	 */
	private static void subsetScan(String inputFile, String outputFile) throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + inputFile);
		origDoc.getDocumentElement().normalize();

		NodeList origStringNodes = (NodeList) xpath.evaluate("/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

		for (int i = 0; i < origStringNodes.getLength(); i++) {
			Node singleNode = (Node) origStringNodes.item(i);

			Element oStringElem = (Element) singleNode;
			String content = oStringElem.getTextContent();
			if (content.contains("XBox") || content.contains("Logo_Dolby") || content.contains("[Exit]")  || content.contains("Logo_DTS_DigitalEntertainment") || content.contains("TEMP")
					|| content.contains("UNRECOVERABLE DATA") || content.contains("Logo_NVIDIA_PhysX") || content.contains("Laughter") || content.contains("unintelligible")) {
				singleNode.getParentNode().removeChild(singleNode);
			}
		}

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		tr.transform(new DOMSource(origDoc), new StreamResult(new FileOutputStream(outputFile)));
	}

	/**
	 * Scans a XML version of a TLK file (me2/3, not tankmaster) and gets rid of
	 * anything without [ ] UI elements. Writes to a TLK Tankmaster format
	 * 
	 * @throws Exception
	 */
	private static void initialScan() throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		//output
		Document newDoc = dBuilder.newDocument();
		Element root = newDoc.createElement("Strings");

		String tlkFile = "C:\\Users\\Michael\\Desktop\\biogame_xbx.xml";

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Pattern pattern = Pattern.compile("\\[[a-zA-Z1-9_]+\\]");

		Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + tlkFile);
		origDoc.getDocumentElement().normalize();

		NodeList origStringNodes = (NodeList) xpath.evaluate("/tlkFile/string", origDoc.getDocumentElement(), XPathConstants.NODESET);

		for (int i = 0; i < origStringNodes.getLength(); i++) {
			Node singleNode = (Node) origStringNodes.item(i);
			singleNode.getParentNode().removeChild(singleNode);
			Element oStringElem = (Element) singleNode;

			String id = xpath.evaluate("id", oStringElem);
			String content = xpath.evaluate("data", oStringElem);
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				/*
				 * System.out.println("ok"); } if
				 * (content.matches("\\[[a-zA-Z1-9_]+\\]")) {
				 */ Element changedElement = newDoc.createElement("String");
				changedElement.setAttribute("id", id);
				changedElement.setTextContent(content);
				root.appendChild(changedElement);
				System.out.println("Found match: " + id);
			} else {
				//System.out.println("Match failed: "+content);
			}
		}

		newDoc.appendChild(root);

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		tr.transform(new DOMSource(newDoc), new StreamResult(new FileOutputStream("C:\\Users\\Michael\\Desktop\\XBX.xml")));
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
