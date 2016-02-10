package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
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
		//compileTLK("C:\\Users\\Michael\\Desktop\\ME3CMM\\mods\\MP Controller Support\\PATCH1");
		//decompileTLK();
		//initialScan();
		//subsetScan();
		replacementScan();
	}

	/**
	 * Scans and creates a replacement of command strings Input file should be a
	 * tankmaster TLK XML
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws Exception
	 */
	private static void replacementScan() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		File dir = new File("C:\\Users\\Michael\\Desktop\\Moonshine_TLK\\");
		FileFilter fileFilter = new WildcardFileFilter("subset_*.xml");
		File[] files = dir.listFiles(fileFilter);

		for (File f : files) {
			String inputFile = f.getAbsolutePath();
			String savename = f.getParent() + "\\final_" + FilenameUtils.getBaseName(inputFile) + ".xml";
			System.out.println("Performing replacement scan: " + savename);
			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + inputFile);
			origDoc.getDocumentElement().normalize();

			NodeList origStringNodes = (NodeList) xpath.evaluate("/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node singleNode = (Node) origStringNodes.item(i);

				Element oStringElem = (Element) singleNode;
				String content = oStringElem.getTextContent();
				content = content.replace("([Mouse_Btn_L])", "[XBoxB_Btn_RT]");
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
			tr.transform(new DOMSource(origDoc), new StreamResult(new FileOutputStream(savename)));
		}

	}

	/**
	 * Scans a Strings file for [command] entries and removes them, as a subset
	 * of all command strings Input is a tankmaster tlk file
	 * 
	 * @param outputFile
	 * @param inputFile
	 * @throws Exception
	 */
	private static void subsetScan() throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		File dir = new File("C:\\Users\\Michael\\Desktop\\Moonshine_TLK\\");
		FileFilter fileFilter = new WildcardFileFilter("init_*.xml");
		File[] files = dir.listFiles(fileFilter);

		for (File f : files) {
			String initialXMLFile = f.getAbsolutePath();
			String savename = f.getParent() + "\\subset_" + FilenameUtils.getBaseName(initialXMLFile) + ".xml";
			System.out.println("Performing subset scan: " + savename);

			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + initialXMLFile);
			origDoc.getDocumentElement().normalize();

			NodeList origStringNodes = (NodeList) xpath.evaluate("/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node singleNode = (Node) origStringNodes.item(i);

				Element oStringElem = (Element) singleNode;
				String content = oStringElem.getTextContent();
				if (content.contains("XBox") || content.contains("Logo_Dolby") || content.contains("[Exit]") || content.contains("Logo_DTS_DigitalEntertainment")
						|| content.contains("TEMP") || content.contains("UNRECOVERABLE DATA") || content.contains("Logo_NVIDIA_PhysX") || content.contains("Laughter")
						|| content.contains("unintelligible")) {
					singleNode.getParentNode().removeChild(singleNode);
				}
			}

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.VERSION, "1.1");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(origDoc), new StreamResult(new FileOutputStream(savename)));
		}
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

		File dir = new File("C:\\Users\\Michael\\Desktop\\Moonshine_TLK\\");
		FileFilter fileFilter = new WildcardFileFilter("*.xml");
		File[] files = dir.listFiles(fileFilter);

		for (File f : files) {
			Document newDoc = dBuilder.newDocument();
			Element root = newDoc.createElement("Strings");
			String tlkFile = f.getAbsolutePath();
			String savename = f.getParent() + "\\init_" + FilenameUtils.getBaseName(tlkFile) + ".xml";
			System.out.println("Scanning: " + tlkFile);
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
					Element changedElement = newDoc.createElement("String");
					changedElement.setAttribute("id", id);
					changedElement.setTextContent(content);
					root.appendChild(changedElement);
					//System.out.println("Found match: " + id);
				} else {
					//System.out.println("Match failed: "+content);
				}
			}

			newDoc.appendChild(root);

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.VERSION, "1.1");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(newDoc), new StreamResult(new FileOutputStream(savename)));
		}
	}

	private static void decompileTLK() {
		File dir = new File(System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "tlkfiles" + File.separator);
		System.out.println(dir);
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter("tlk"), TrueFileFilter.TRUE);
		for (File file : files) {
			System.out.println(file);
		}

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
		/*
		 * files = dir.listFiles(new FilenameFilter() {
		 * 
		 * @Override public boolean accept(File dir, String name) { return
		 * name.endsWith(".tlk"); } });
		 */

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

	private static void compileTLK(String path) throws IOException {
		File dir = new File(path);
		System.out.println(dir);
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter(".tlk"), FalseFileFilter.FALSE);
		for (File file : files) {
			System.out.print("/BIOGame/DLC/DLC_UPD_Patch01/CookedPCConsole/");
			System.out.print(file.getName());
			System.out.print(";");
		}

		System.out.println();
		if (true) {
			return;
		}

		for (File f : files) {
			ArrayList<String> commandBuilder = new ArrayList<String>();

			String compilerPath = ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe";
			commandBuilder.add(compilerPath);
			commandBuilder.add(f.getAbsolutePath());
			commandBuilder.add(f.getParent() + File.separator + FilenameUtils.getBaseName(f.getAbsolutePath()) + ".xml");
			commandBuilder.add("--mode");
			commandBuilder.add("ToTlk");
			commandBuilder.add("--no-ui");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			//Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			Process p = null;
			System.out.println("Executing: " + sb.toString());
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			p = pb.start();

		}
	}
}
