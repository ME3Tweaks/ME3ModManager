package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
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
import org.xml.sax.SAXException;

public class TLKTool {

	static int[] ignoredIds = new int[] { 320166, 180997, 330510, 340843, 340858, 351755, 371720, 372162, 375050, 579961, 581459, 589952, 627586,
			699487, 699498, 699522, 717634, 717644, 727788, 727789 };

	static class TLKNode {

		int id;
		String content;

		public TLKNode(int id, String content) {
			super();
			this.id = id;
			this.content = content;
		}

	}

	public static void main(String[] args) throws Exception {
		//compileTLK("C:\\Users\\Michael\\Desktop\\ME3CMM\\mods\\MP Controller Support\\PATCH1");
		//decompileTLK();
		//comparisonScan();
		initialScanTankmaster();
		//initialScanME2Tool();
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

		File dir = new File("f:\\Moonshine_TLK\\");
		FileFilter fileFilter = new WildcardFileFilter("add_*.xml");
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
				if (content.contains("XBox") || content.contains("Logo_Dolby") || content.contains("[Exit]")
						|| content.contains("Logo_DTS_DigitalEntertainment") || content.contains("TEMP") || content.contains("UNRECOVERABLE DATA")
						|| content.contains("Logo_NVIDIA_PhysX") || content.contains("Laughter") || content.contains("unintelligible")) {
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
	 * Scans and outputs a list of diffs between two string XML files
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 * 
	 * @throws Exception
	 */
	private static void comparisonScan() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
			TransformerException {
		String origFolder = "F:\\original_tlk\\";
		String moonFolder = "F:\\moonshine_tlk\\";
		ArrayList<String> languageKeys = new ArrayList<String>();

		//make map for corrensponding names.
		File moondir = new File(moonFolder);
		FileFilter mfileFilter = new WildcardFileFilter("XBX_*.xml");
		File[] moonfiles = moondir.listFiles(mfileFilter);

		HashMap<String, String> moonMap = new HashMap<String, String>();

		for (File mf : moonfiles) {
			String fname = FilenameUtils.getBaseName(mf.getAbsolutePath());
			fname = fname.substring(fname.indexOf("_") + 1).toLowerCase();
			moonMap.put(fname, mf.getAbsolutePath());
			languageKeys.add(fname);
		}

		File origdir = new File(origFolder);
		FileFilter ofileFilter = new WildcardFileFilter("orig_*.xml");
		File[] origfiles = origdir.listFiles(ofileFilter);

		HashMap<String, String> origMap = new HashMap<String, String>();

		for (File of : origfiles) {
			String fname = FilenameUtils.getBaseName(of.getAbsolutePath());
			fname = fname.substring(fname.indexOf("_") + 1).toLowerCase();
			origMap.put(fname, of.getAbsolutePath());
		}

		//Perform Comparison
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		for (String str : languageKeys) {
			System.out.println("Diffing " + str);
			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + origMap.get(str));
			origDoc.getDocumentElement().normalize();

			Document moonDoc = dbFactory.newDocumentBuilder().parse("file:///" + moonMap.get(str));
			moonDoc.getDocumentElement().normalize();

			System.out.println("Parsing ORIG");
			NodeList origStringNodes = (NodeList) xpath.evaluate("/tlkFile/string", origDoc.getDocumentElement(), XPathConstants.NODESET);

			System.out.println("Parsing MOON");
			NodeList moonStringNodes = (NodeList) xpath.evaluate("/tlkFile/string", moonDoc.getDocumentElement(), XPathConstants.NODESET);
			Document newDoc = dBuilder.newDocument();
			Element root = newDoc.createElement("Strings");

			System.out.println("Starting DIFF");
			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node origNode = (Node) origStringNodes.item(i);
				origNode.getParentNode().removeChild(origNode);
				Element oStringElem = (Element) origNode;
				String oid = xpath.evaluate("id", oStringElem);
				int oidAsInt = Integer.parseInt(oid);
				if (Arrays.asList(ignoredIds).contains(oidAsInt)) {
					continue; //ignore this
				}
				String ocontent = xpath.evaluate("data", oStringElem);

				//get moon v
				Node moonNode = (Node) moonStringNodes.item(i);
				if (moonNode == null || moonNode.getParentNode() == null) {
					System.out.println("moon has no match");
				}
				moonNode.getParentNode().removeChild(moonNode);
				Element mStringElem = (Element) moonNode;
				String mid = xpath.evaluate("id", mStringElem);
				int midAsInt = Integer.parseInt(mid);
				String mcontent = xpath.evaluate("data", mStringElem);

				//get diff
				if (!mcontent.equals(ocontent)) {
					Element changedElement = newDoc.createElement("String");
					changedElement.setAttribute("id", mid);
					changedElement.setTextContent(mcontent);
					root.appendChild(changedElement);
					System.out.println("content mismatch");
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
			tr.transform(new DOMSource(newDoc), new StreamResult(new FileOutputStream(moonFolder + "diff_" + str + ".xml")));
		}
	}

	/**
	 * Scans a XML version of a TLK file (me2/3, not tankmaster) and gets rid of
	 * anything without [ ] UI elements. Writes to a TLK Tankmaster format
	 * 
	 * @throws Exception
	 */
	private static void initialScanME2Tool() throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		//output

		File dir = new File("f:\\Moonshine_TLK\\");
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
				int idAsInt = Integer.parseInt(id);
				if (Arrays.asList(ignoredIds).contains(idAsInt)) {
					continue; //ignore this
				}
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

	/**
	 * Scans a XML version of a TLK file (tankmaster format) and gets rid of
	 * anything without [ ] UI elements. Writes to a TLK Tankmaster format
	 * 
	 * @throws Exception
	 */
	private static void initialScanTankmaster() throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		//output
		String outFolder = "F:\\moonshine_tlk\\";
		File dir = new File("F:\\moonshine_tlk\\dlc\\");
		FileFilter fileFilter = new WildcardFileFilter("*.xml");
		File[] files = dir.listFiles(fileFilter);

		HashMap<String, ArrayList<TLKNode>> nameElementMap = new HashMap<>();
		for (File f : files) {
			String lang = FilenameUtils.getBaseName(f.getAbsolutePath());
			while (lang.indexOf("_") > 0) {
				lang = lang.substring(lang.indexOf("_") + 1);
			}
			lang = lang.substring(0, 3);

			Document newDoc = dBuilder.newDocument();
			ArrayList<TLKNode> tlks =  nameElementMap.get(lang);
			if (tlks == null) {
				tlks = new ArrayList<TLKNode>();
				nameElementMap.put(lang, tlks);
			}
			String tlkFile = f.getAbsolutePath();
			String savename = f.getParent() + "\\init_" + FilenameUtils.getBaseName(tlkFile) + ".xml";
			System.out.println("Scanning: " + tlkFile);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			Pattern pattern = Pattern.compile("\\[[a-zA-Z1-9_]+\\]");

			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + tlkFile);
			origDoc.getDocumentElement().normalize();

			NodeList origStringNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node singleNode = (Node) origStringNodes.item(i);
				singleNode.getParentNode().removeChild(singleNode);
				Element oStringElem = (Element) singleNode;

				String id = oStringElem.getAttribute("id");
				int idAsInt = Integer.parseInt(id);
				if (Arrays.asList(ignoredIds).contains(idAsInt)) {
					continue; //ignore this
				}
				String content = oStringElem.getTextContent();
				Matcher matcher = pattern.matcher(content);
				if (matcher.find()) {
					TLKNode node = new TLKNode(idAsInt,content);
					tlks.add(node);
					//System.out.println("Found match: " + id);
				} else {
					//System.out.println("Match failed: "+content);
				}
			}
		}

		for (Map.Entry<String, ArrayList<TLKNode>> langEntry : nameElementMap.entrySet()) {
			if (langEntry.getValue().size() <= 0) {
				continue;
			}

			Document doc = dBuilder.newDocument();
			Element root = doc.createElement("Strings");
			doc.appendChild(root);
			for (TLKNode node : langEntry.getValue()){
				Element elem = doc.createElement("String");
				elem.setAttribute("id", Integer.toString(node.id));
				elem.setTextContent(node.content);
				root.appendChild(elem);
			}

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.VERSION, "1.1");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(outFolder + "add_" + langEntry.getKey() + ".xml")));
		}
	}

	private static void decompileTLK() {
		//		File dir = new File(System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "tlkfiles" + File.separator);
		File dir = new File("F:\\moonshine_tlk\\DLC_HEN_PR\\");

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
