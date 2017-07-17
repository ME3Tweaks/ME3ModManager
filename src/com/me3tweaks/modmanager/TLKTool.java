package com.me3tweaks.modmanager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
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
import javax.xml.xpath.XPathExpression;
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

import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.utilities.datatypeconverter.DatatypeConverter;

public class TLKTool {

	static int[] ignoredIds = new int[] { 320166, 180997, 330510, 340843, 340858, 351755, 371720, 372162, 375050, 579961, 581459, 589952, 627586, 699487, 699498, 699522, 717634,
			717644, 727788, 727789 };

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
		orderINTFile();
		buildITAFromINTSPController();
		replacePCPlaceholdersWithXbox();
		verifySameIds();
		//performFix();
		//replacementScan();
		//compileTLK("E:\\MPTLK\\mp5");
		//decompileTLK("E:\\MPTLK\\mp5");
		//comparisonScan();
		//initialScanTankmaster();
		//nonINTME2ToolScan();
		//initialScanME2Tool();
		//subsetScan();

		//compileTLK("E:\\Google Drive\\SP Controller Support\\TLK\\moonshine_tlk\\");
		//String folderpath = "C:\\Users\\\Desktop\\tlk\\BIOGame_ITA\\";
		//combineIntoSingleFile(folderpath);
	}

	private static void verifySameIds() throws Exception {
		// TODO Auto-generated method stub
		File intFile = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_INT0.xml");
		File itaFile = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_ITA0.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("/TlkFile/Strings/String");

		ArrayList<Integer> englishIds = new ArrayList<>();
		Document doc = builder.parse(intFile);
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			String idStr = n.getAttributes().getNamedItem("id").getTextContent();
			String content = n.getTextContent();
			Integer id = Integer.parseInt(idStr);
			englishIds.add(id);
		}

		ArrayList<Integer> italianIds = new ArrayList<>();
		doc = builder.parse(itaFile);
		nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			String idStr = n.getAttributes().getNamedItem("id").getTextContent();
			String content = n.getTextContent();
			Integer id = Integer.parseInt(idStr);
			italianIds.add(id);
		}

		Collections.sort(italianIds);
		Collections.sort(englishIds);

		System.out.println("English controller file count: " + englishIds.size());
		System.out.println("Italian controller file count: " + italianIds.size());

		if (italianIds.size() != englishIds.size()) {
			System.err.println("SIZE MISMATCH!");
			System.exit(0);
		}

		int wrongcount = 0;
		for (int i = 0; i < englishIds.size(); i++) {
			int englishid = englishIds.get(i);
			int italianid = italianIds.get(i);
			if (englishid != italianid) {
				wrongcount++;
				System.err.println("English ID does not match Italian ID: " + englishid + " vs " + italianid);
			}
		}
		System.out.println("Number wrong: " + wrongcount);

	}

	private static void orderINTFile() throws Exception {
		TreeMap<Integer, String> englishTLKEntries = new TreeMap<Integer, String>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("/TlkFile/Strings/String");
		File f = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_INT0.xml");
		Document doc = builder.parse(f);
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			String idStr = n.getAttributes().getNamedItem("id").getTextContent();
			String content = n.getTextContent();
			Integer id = Integer.parseInt(idStr);
			englishTLKEntries.put(id, content);
		}

		//output new file
		Document outdoc = builder.newDocument();
		Element rootElement = outdoc.createElement("TlkFile");
		rootElement.setAttribute("name", "DLC_CON_XBX_INT/DLC_CON_XBX_INT0.xml");
		outdoc.appendChild(rootElement);
		Element strings = outdoc.createElement("Strings");
		rootElement.appendChild(strings);
		for (Map.Entry<Integer, String> englishEntry : englishTLKEntries.entrySet()) {
			Integer intStrId = englishEntry.getKey();
			String intStr = englishEntry.getValue();
			Element strElem = outdoc.createElement("String");
			strElem.setTextContent(intStr);
			strElem.setAttribute("id", Integer.toString(intStrId));
			strings.appendChild(strElem);
		}

		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(outdoc);

		StreamResult result = new StreamResult(new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_INT0_Ordered.xml"));
		transformer.transform(source, result);
	}

	private static void buildITAFromINTSPController() throws Exception {
		HashMap<Integer, String> englishTLKEntries = new HashMap<Integer, String>();
		HashMap<Integer, String> italianTLKEntries = new HashMap<Integer, String>();
		HashMap<Integer, String> englishControllerTLKEntries = new HashMap<Integer, String>();

		//Get english files and parse them
		File englishDir = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\BIOGame_INT");
		File[] engishFiles = englishDir.listFiles((dir, name) -> {
			return name.toLowerCase().endsWith(".xml");
		});

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("/TlkFile/Strings/String");

		for (File f : engishFiles) {
			Document doc = builder.parse(f);
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String idStr = n.getAttributes().getNamedItem("id").getTextContent();
				String content = n.getTextContent();
				Integer id = Integer.parseInt(idStr);
				englishTLKEntries.put(id, content);
			}
		}

		System.out.println("English: " + englishTLKEntries.size() + " items");

		File italianDir = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\BIOGame_ITA");
		File[] italianFiles = italianDir.listFiles((dir, name) -> {
			return name.toLowerCase().endsWith(".xml");
		});
		for (File f : italianFiles) {
			Document doc = builder.parse(f);
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String idStr = n.getAttributes().getNamedItem("id").getTextContent();
				String content = n.getTextContent();
				Integer id = Integer.parseInt(idStr);
				italianTLKEntries.put(id, content);
			}
		}

		System.out.println("Italian: " + italianTLKEntries.size() + " items");

		File controllerDir = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_INT");
		File[] controllerFiles = controllerDir.listFiles((dir, name) -> {
			return name.toLowerCase().endsWith(".xml");
		});
		for (File f : controllerFiles) {
			Document doc = builder.parse(f);
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String idStr = n.getAttributes().getNamedItem("id").getTextContent();
				String content = n.getTextContent();
				Integer id = Integer.parseInt(idStr);
				if (id >= 135000000) {
					continue; //skip.
				}
				englishControllerTLKEntries.put(id, content);
			}
		}

		System.out.println("Controller: " + englishControllerTLKEntries.size() + " items");
		ArrayList<Integer> manualModifications = new ArrayList<>();
		//ArrayList<Integer> idsToCopy = new ArrayList<>(); 
		ArrayList<TLKIDPair> idsToCopy = new ArrayList<>();
		int numMissing = 0;
		for (Map.Entry<Integer, String> entry : englishControllerTLKEntries.entrySet()) {
			boolean matchFound = false;
			Integer controllerStrId = entry.getKey();
			String controllerStr = entry.getValue();
			for (Map.Entry<Integer, String> englishEntry : englishTLKEntries.entrySet()) {
				Integer intStrId = englishEntry.getKey();
				String intStr = englishEntry.getValue();
				if (intStr.equals(controllerStr)) {
					System.out.println("Found match: " + intStrId);
					idsToCopy.add(new TLKIDPair(intStrId, controllerStrId));
					matchFound = true;
					break;
				}
			}
			if (!matchFound) {
				//Will require manual tuning...
				boolean containsKey = italianTLKEntries.containsKey(controllerStrId);
				if (containsKey) {
					manualModifications.add(controllerStrId);
					idsToCopy.add(new TLKIDPair(controllerStrId, controllerStrId)); //copy exact ID over.
				} else {
					System.err.println("No match found for controller str, copying directly: " + controllerStrId + " " + controllerStr);
					numMissing++;
				}
			}
		}

		Collections.sort(idsToCopy);
		System.out.println("Number of manual entries: " + numMissing);

		//Build output file.
		Document doc = builder.newDocument();
		Element rootElement = doc.createElement("TlkFile");
		rootElement.setAttribute("name", "DLC_CON_XBX_ITA/DLC_CON_XBX_ITA0.xml");
		doc.appendChild(rootElement);
		Element strings = doc.createElement("Strings");
		rootElement.appendChild(strings);

		for (TLKIDPair id : idsToCopy) {
			Element strElem = doc.createElement("String");
			String content = italianTLKEntries.get(id.getIdSource());
			if (manualModifications.contains(id.getIdTarget())) {
				content = "[NEEDS ITALIAN REVIEW] " + content;
			}
			strElem.setTextContent(content);
			strElem.setAttribute("id", Integer.toString(id.getIdTarget()));
			strings.appendChild(strElem);
		}

		Element strElem = doc.createElement("String");
		String content = "SP Native Controller Support DLC Module";
		strElem.setTextContent(content);
		strElem.setAttribute("id", Integer.toString(135000000));
		strings.appendChild(strElem);

		strElem = doc.createElement("String");
		content = "DLC_CON_XBX";
		strElem.setTextContent(content);
		strElem.setAttribute("id", Integer.toString(135000001));
		strings.appendChild(strElem);

		strElem = doc.createElement("String");
		content = "it-it";
		strElem.setTextContent(content);
		strElem.setAttribute("id", Integer.toString(135000002));
		strings.appendChild(strElem);

		strElem = doc.createElement("String");
		content = "Male";
		strElem.setTextContent(content);
		strElem.setAttribute("id", Integer.toString(135000003));
		strings.appendChild(strElem);

		strElem = doc.createElement("String");
		content = "Female";
		strElem.setTextContent(content);
		strElem.setAttribute("id", Integer.toString(135000004));
		strings.appendChild(strElem);

		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);

		StreamResult result = new StreamResult(new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_ITA0.xml"));
		transformer.transform(source, result);

		System.out.println("Done. Manual modifications for the following IDs are required:");
		for (int id : manualModifications) {
			System.out.println(" - " + id);
		}
	}

	static class TLKIDPair implements Comparable<TLKIDPair> {
		private int idSource, idTarget;

		public TLKIDPair(int id1, int id2) {
			this.idSource = id1;
			this.idTarget = id2;
		}

		public int getIdSource() {
			return idSource;
		}

		public int getIdTarget() {
			return idTarget;
		}

		@Override
		public int compareTo(TLKIDPair o) {
			return new Integer(getIdTarget()).compareTo(o.getIdTarget());
		}

	}

	private static void performFix() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("C:\\Users\\Michael\\Desktop\\set.txt"));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			StringTokenizer stk = new StringTokenizer(line, " ");
			String id = stk.nextToken();
			String cat = stk.nextToken();
			String out = "UPDATE dynamicmixinlibrary SET category = " + cat + " WHERE id = " + id + ";";
			System.out.println(out);
		}
	}

	private static void parseJPatchString(String string) {
		final char ESC = 0xA7;
		final char EQL = 0xA3;
		final char MOD = 0xA6;
		byte[] data = DatatypeConverter.parseHexBinary(string);
		int bytepos = 0;
		int twoprev = 0;
		int prev = 0;
		int current = 0;
		boolean readingdata = false;
		int op = -1;
		for (byte b : data) {
			int val = b & 0xFF;
			twoprev = prev;
			prev = current;
			current = val;

			if (current == ESC) {
				System.out.println("Read ESC at " + bytepos);
			}

			if (readingdata) {
				switch (op) {
				case MOD:
					//read until end
					break;
				}
			}

			if (current == MOD && prev == ESC && twoprev != ESC) {
				System.out.println("Read OP MOD at " + bytepos);
				readingdata = true;
				bytepos++;
				continue;
			}
			if (current == EQL && prev == ESC && twoprev != ESC) {
				System.out.println("Read OP EQL at " + bytepos);
				readingdata = true;
				bytepos++;
				continue;
			}

			bytepos++;
		}
	}

	/**
	 * Combines tankmaster's decompiled files into a single XML file
	 * 
	 * @param folderpath
	 * @throws Exception
	 */
	private static void combineIntoSingleFile(String folderpath) throws Exception {
		// TODO Auto-generated method stub
		File folder = new File(folderpath);
		ArrayList<File> allfiles = new ArrayList<>();

		//basegame
		FileFilter fileFilter = new WildcardFileFilter("BIOGame_*.xml");
		File[] files = folder.listFiles(fileFilter);

		for (File f : files) {
			System.out.println(f);
		}
		if (files[2].getName().contains("10")) {

			ArrayList<File> reorderArray = new ArrayList<File>(Arrays.asList(files));

			reorderArray.add(reorderArray.remove(2));
			files = (File[]) reorderArray.toArray(new File[reorderArray.size()]);
			/*
			 * File temp = files[2]; files[2] = files[11]; files[11] = temp;
			 */
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		Document newDoc = dBuilder.newDocument();
		Element root = newDoc.createElement("TlkFile");
		Element strings = newDoc.createElement("Strings");

		root.appendChild(strings);
		newDoc.appendChild(root);

		String lang = null;
		System.out.println("FILES...");
		for (File file : files) {
			String basename = FilenameUtils.getBaseName(file.getAbsolutePath());
			if (lang == null) {
				lang = basename.substring(basename.length() - 4, basename.length() - 1);
			}
			//load XML
			Document intDoc = dbFactory.newDocumentBuilder().parse("file:///" + file.getAbsolutePath());
			intDoc.getDocumentElement().normalize();
			System.out.println("Loaded " + basename);
			NodeList intStringNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String", intDoc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("NODE COUNT: " + intStringNodes.getLength());
			for (int i = 0; i < intStringNodes.getLength(); i++) {
				Node singleNode = (Node) intStringNodes.item(i);
				singleNode.getParentNode().removeChild(singleNode);
				Element oStringElem = (Element) singleNode;
				String id = oStringElem.getAttribute("id");

				Element clonedItem = newDoc.createElement("String");
				clonedItem.setAttribute("id", id);
				clonedItem.setTextContent(oStringElem.getTextContent());
				strings.appendChild(clonedItem);
			}
		}
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty(OutputKeys.VERSION, "1.0");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		String saveTo = folder.getParent() + "\\tankmaster_base_" + lang + ".xml";
		System.out.println("SAVING TO " + saveTo);
		tr.transform(new DOMSource(newDoc), new StreamResult(new FileOutputStream(folder.getParent() + "\\tankmaster_base_" + lang + ".xml")));
	}

	private static void nonINTME2ToolScan() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		ArrayList<Integer> idsToExport = new ArrayList<Integer>();

		//input
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		String infile = "C:\\Users\\\\Desktop\\GD\\moonshine_tlk\\DLC_CON_XBX_INT\\DLC_CON_XBX_INT0.xml";
		Document intDoc = dbFactory.newDocumentBuilder().parse("file:///" + infile);
		intDoc.getDocumentElement().normalize();

		NodeList intStringNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String", intDoc.getDocumentElement(), XPathConstants.NODESET);
		for (int i = 0; i < intStringNodes.getLength(); i++) {
			Node singleNode = (Node) intStringNodes.item(i);
			singleNode.getParentNode().removeChild(singleNode);
			Element oStringElem = (Element) singleNode;
			String id = oStringElem.getAttribute("id");
			int idAsInt = Integer.parseInt(id);
			idsToExport.add(idAsInt);
			System.out.println("Save this ID: " + idAsInt);
		}

		//output
		String outFolder = "C:\\Users\\\\Desktop\\GD\\diffs\\";
		File dir = new File("C:\\Users\\\\Desktop\\GD\\tankmaster_original_tlk\\");
		FileFilter fileFilter = new WildcardFileFilter("tankmaster_base_*.xml");
		File[] files = dir.listFiles(fileFilter);

		HashMap<String, ArrayList<TLKNode>> nameElementMap = new HashMap<>();
		for (File f : files) {
			String lang = FilenameUtils.getBaseName(f.getAbsolutePath());
			while (lang.indexOf("_") > 0) {
				lang = lang.substring(lang.indexOf("_") + 1);
			}
			lang = lang.substring(0, 3).toLowerCase();
			if (lang.equals("int")) {
				continue; //skip int
			} else {
				System.out.println(lang);
			}

			ArrayList<TLKNode> tlks = nameElementMap.get(lang);
			if (tlks == null) {
				tlks = new ArrayList<TLKNode>();
				nameElementMap.put(lang, tlks);
			}
			String tlkFile = f.getAbsolutePath();
			System.out.println("Scanning: " + tlkFile);

			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + tlkFile);
			origDoc.getDocumentElement().normalize();

			NodeList origStringNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node singleNode = (Node) origStringNodes.item(i);
				singleNode.getParentNode().removeChild(singleNode);
				Element oStringElem = (Element) singleNode;

				String id = oStringElem.getAttribute("id");
				int idAsInt = Integer.parseInt(id);

				if (!idsToExport.contains(idAsInt)) {
					continue; //ignore this
				}
				System.out.println("Saving id");
				TLKNode node = new TLKNode(idAsInt, oStringElem.getTextContent());
				tlks.add(node);
				//System.out.println("Found match: " + id);
			}
		}
		System.out.println("SAVING");

		for (Map.Entry<String, ArrayList<TLKNode>> langEntry : nameElementMap.entrySet()) {
			if (langEntry.getValue().size() <= 0) {
				continue;
			}
			System.out.println("has a node.");

			Document doc = dBuilder.newDocument();
			Element root = doc.createElement("Strings");
			doc.appendChild(root);
			for (TLKNode node : langEntry.getValue()) {
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
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(outFolder + "diff_" + langEntry.getKey() + ".xml")));
		}
	}

	private static class ReplacementNode {
		private String src;
		private String repl;

		public ReplacementNode(String src, String repl) {
			this.src = src;
			this.repl = repl;
		}
	}

	/**
	 * Scans and creates a replacement of command strings. Works with Tankmaster
	 * TLK
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws Exception
	 */
	private static void replacementScan() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//input

		HashMap<Integer, ReplacementNode> localizedUncommonKeyMap = new HashMap<>();
		localizedUncommonKeyMap.put(675852, new ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_RT"));
		localizedUncommonKeyMap.put(786429, new ReplacementNode("[Q]", "[XBoxB_Btn_DPadL]"));
		/*
		 * 
		 * localizedUncommonKeyMap.put(338642, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_A")); //Press [XBoxB_Btn_A]
		 * to use Singularity localizedUncommonKeyMap.put(338681, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_A")); //Press [XBoxB_Btn_A]
		 * to use First Aid localizedUncommonKeyMap.put(338685, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_A")); //Press [XBoxB_Btn_A]
		 * to use Overload localizedUncommonKeyMap.put(338689, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_A"));
		 * localizedUncommonKeyMap.put(563797, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_A"));
		 * localizedUncommonKeyMap.put(720736, new
		 * ReplacementNode("Mouse_Btn_L", "XboxB_Btn_B")); //press back to exit
		 * turret
		 * 
		 * localizedUncommonKeyMap.put(345715, new
		 * ReplacementNode("Mouse_Btn_R", "XBoxB_Btn_LT")); //code segments
		 * (might be unused localizedUncommonKeyMap.put(563797, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_RT")); //back (done) to
		 * exit, click to fire localizedUncommonKeyMap.put(720735, new
		 * ReplacementNode("Mouse_Btn_L", "XBoxB_Btn_RT")); //Hold
		 * [XBoxB_Btn_RT] to charge weapon localizedUncommonKeyMap.put(720736,
		 * new ReplacementNode("Mouse_Btn_L", "XboxB_Btn_B")); //press back to
		 * exit turret localizedUncommonKeyMap.put(720736, new
		 * ReplacementNode("Mouse_Btn_L", "XboxB_Btn_B")); //press back to exit
		 * turret localizedUncommonKeyMap.put(720736, new
		 * ReplacementNode("Mouse_Btn_L", "XboxB_Btn_B")); //press back to exit
		 * turret
		 */
		HashMap<Integer, Integer> pullFromOrigLang = new HashMap<>();
		pullFromOrigLang.put(335363, 336943); //Fly between systems in galaxy view...
		pullFromOrigLang.put(338524, 335435); //Press [XBoxB_Btn_B] to end the mission.
		pullFromOrigLang.put(338636, 338634); //Select Singularity
		pullFromOrigLang.put(338642, 338639); //Press [XBoxB_Btn_A] to use Singularity
		pullFromOrigLang.put(338681, 338679); //Press [XBoxB_Btn_A] to use First Aid
		pullFromOrigLang.put(338683, 338682); //Press [XBoxB_Btn_LS] to select Overload
		pullFromOrigLang.put(338685, 338684); //Press [XBoxB_Btn_A] to use Overload
		pullFromOrigLang.put(338689, 338688); //Press [XBoxB_Btn_A] to equip grenade launcher
		pullFromOrigLang.put(339230, 309316); //[XBoxB_Btn_A] Pick up pistol
		pullFromOrigLang.put(339231, 309384); //Enter cover you are facing
		pullFromOrigLang.put(339232, 337416); //[XBoxB_Btn_B] Exit cover
		pullFromOrigLang.put(339242, 333751); //Reload
		pullFromOrigLang.put(339243, 309384); //duplicate
		pullFromOrigLang.put(339245, 333754); //fire
		pullFromOrigLang.put(339246, 309384); //duplicate
		pullFromOrigLang.put(339247, 337421); //Hold .... to zoom
		pullFromOrigLang.put(339251, 282501); //[XBoxB_Btn_A] Pick up grenade launcher
		pullFromOrigLang.put(339252, 333755); //Hold [XBoxB_Btn_LB] to select grenade launcher
		pullFromOrigLang.put(339278, 337421); //duplicate
		pullFromOrigLang.put(339280, 288593); //storm through fire
		pullFromOrigLang.put(339289, 337426); //Hold [XBoxB_Btn_X] to switch back to previous weapon
		pullFromOrigLang.put(339293, 334793); //Hold... to access power wheel
		pullFromOrigLang.put(339297, 334793); //power wheel dup
		pullFromOrigLang.put(339298, 337427); //Wrong power: Press to open
		pullFromOrigLang.put(339300, 334835); //[XBoxB_Btn_RB] Hold to access the Power Wheel and use Overload on the crates
		pullFromOrigLang.put(339314, 242442); //Hold [XBoxB_Btn_RB] and use Overload on the drones
		pullFromOrigLang.put(339315, 326670); //Hold [XBoxB_Btn_RB] and use Warp on the door
		//pullFromOrigLang.put(339317,); //REQUIRES MANUAL: Use ([Shared_SquadMove1]) and ([Shared_SquadMove2]) to send your squadmates to waypoints
		pullFromOrigLang.put(339319, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(339323, 245674); //[UI_XBoxB_Btn_RT] to perform a Renegade action (when shown on screen)
		pullFromOrigLang.put(339706, 339709); //Use [XBoxB_Btn_A] to fire
		pullFromOrigLang.put(341015, 341014); //Press [XBoxB_Btn_Start] and select "Squad" to upgrade powers
		pullFromOrigLang.put(341022, 341021); //[XBoxB_Btn_R3] Display direction of current objective
		pullFromOrigLang.put(341303, 341302); //[UI_XBoxB_Btn_LT] to perform a Paragon action (when shown on screen)
		pullFromOrigLang.put(341337, 278987); //Hold [XBoxB_Btn_RB] to use First Aid to heal your squad
		//pullFromOrigLang.put(341338,); //REQUIRES MANUAL ([Shared_Melee]) to melee attack
		pullFromOrigLang.put(341341, 278984); //Hold [XBoxB_Btn_LT] to aim weapon
		//pullFromOrigLang.put(341345,); //REQUIRES MANUAL ([PC_SwapWeapon]) Switch to another weapon
		pullFromOrigLang.put(341347, 338677); //Press [XBoxB_Btn_Start] and select the Squad screen to spend points on powers
		pullFromOrigLang.put(341392, 348631); //Accept
		pullFromOrigLang.put(343066, 343065); //Press and hold [XBoxB_Btn_A] to recover something from the ground.
		pullFromOrigLang.put(343068, 343067); //Press ([Shared_ShowMap]) to recover something on the ground.
		pullFromOrigLang.put(343070, 343069); //Press [XBoxB_Btn_X] to speed boost.
		pullFromOrigLang.put(344116, 343132); //Your journal updates mission objectives. Press [XBoxB_Btn_Start] to access your journal.
		pullFromOrigLang.put(344118, 343141); //In danger? Hold [XBoxB_Btn_RB] to raise the Power Wheel. Unleashing powers on your enemies could save your life.
		pullFromOrigLang.put(344119, 343143); //Hold [XBoxB_Btn_A] to sprint.
		//pullFromOrigLang.put(344120,); //REQUIRES MANUAL Command squadmates to attack enemies and use powers by using ([Shared_SquadMove1]) or ([Shared_SquadMove2]).
		pullFromOrigLang.put(344121, 343146); //The radar arrow points to your next objective. Hold [XBoxB_Btn_RB] to view the radar.
		pullFromOrigLang.put(344122, 343147); //Instantly use mapped powers by pressing [XBoxB_Btn_LB], [XBoxB_Btn_RB], or [XBoxB_Btn_Y]. Change mapped powers by holding [XBoxB_Btn_RB].
		pullFromOrigLang.put(344123, 343148); //Reload weapons frequently to avoid being unable to fire during combat. Press [XBoxB_Btn_X] to reload.
		pullFromOrigLang.put(344124, 343149); //You can change your difficulty level at any time. Select [XBoxB_Btn_Start] to access the Options Menu.
		pullFromOrigLang.put(344125, 337426); //Hold [XBoxB_Btn_X] to switch back to previous weapon THIS IS DIFFERENT FROM MOONSHINES A BIT (NO YOUR)
		pullFromOrigLang.put(344126, 343157); //When you're in cover, climb low obstacles by holding [XBoxB_Btn_LS] forward and pressing [XBoxB_Btn_A]
		pullFromOrigLang.put(344127, 343180); //When the "[UI_XBoxB_Btn_LT]" symbol displays during a conversation, press [UI_XBoxB_Btn_LT] to have Shepard take a heroic Paragon action.
		pullFromOrigLang.put(344128, 343161); //Your radar reveals the locations of nearby enemies. Hold [XBoxB_Btn_RB] to view the radar.
		pullFromOrigLang.put(344129, 343164); //In trouble? Use [XBoxB_Btn_LB] to bring up weapon wheel and find nearby cover.
		pullFromOrigLang.put(344130, 343159); //When the "[UI_XBoxB_Btn_RT]" symbol is displayed in a conversation, press [UI_XBoxB_Btn_RT] to have Shepard make a bold Renegade move.
		//pullFromOrigLang.put(344131,); //REQUIRES MANUAL To command your squadmates to approach a target, move them into place using ([Shared_SquadMove1]) or ([Shared_SquadMove2]).
		pullFromOrigLang.put(344132, 343191); //Tap [XBoxB_Btn_B] for a melee attack against nearby enemies.
		pullFromOrigLang.put(344133, 343198); //Fire some weapons more quickly by tapping [XBoxB_Btn_RT].
		pullFromOrigLang.put(344134, 343203); //Access map kiosks in civilized areas for directions to important landmarks. If you're lost, press [XBoxB_Btn_R3] to bring up a navigational arrow.
		pullFromOrigLang.put(344135, 343206); //Replaying a section? Jump through cut scenes by pressing [XBoxB_Btn_Start]. Skip lines in a conversation by pressing [XBoxB_Btn_X].
		pullFromOrigLang.put(344136, 343209); //While you're in low cover, vault over it by holding [XBoxB_Btn_LS] forward and pressing [XBoxB_Btn_A].
		pullFromOrigLang.put(345216, 345104); //[XBoxB_Btn_X] Thermal Clip
		pullFromOrigLang.put(345715, 345730); //Press [XBoxB_Btn_A] to select code segments that match the target code-segment
		pullFromOrigLang.put(345729, 345728); //Press [XBoxB_Btn_A] to select matching node
		pullFromOrigLang.put(345731, 345730); //Press [XBoxB_Btn_A] to select code segments that match the target code-segment
		pullFromOrigLang.put(345999, 346000); //Press to Show Map
		pullFromOrigLang.put(346001, 346002);
		pullFromOrigLang.put(346003, 346002);
		pullFromOrigLang.put(346119, 346118);
		pullFromOrigLang.put(346395, 346394);
		pullFromOrigLang.put(349072, 351791);
		pullFromOrigLang.put(349073, 351792);
		pullFromOrigLang.put(349240, 351791);
		pullFromOrigLang.put(349241, 351792);
		pullFromOrigLang.put(349305, 351791); //Exit the Hammerhead
		pullFromOrigLang.put(349306, 351792); //Enter the Hammerhead
		pullFromOrigLang.put(349326, 351791);
		pullFromOrigLang.put(349327, 351792);
		pullFromOrigLang.put(349408, 351791);
		pullFromOrigLang.put(349409, 351792);
		pullFromOrigLang.put(349413, 345718);
		pullFromOrigLang.put(349415, 351791);
		pullFromOrigLang.put(349416, 351792);
		pullFromOrigLang.put(349559, 351791);
		pullFromOrigLang.put(349560, 351792);
		pullFromOrigLang.put(349583, 343177);
		pullFromOrigLang.put(349596, 348565);
		pullFromOrigLang.put(349607, 349605); //Hold [XBoxB_Btn_RB] to use an ammo power
		pullFromOrigLang.put(351463, 350937); //Press [XBoxB_Btn_A] to jump
		pullFromOrigLang.put(351464, 288593); //Storm through the fire
		pullFromOrigLang.put(369236, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385606, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385610, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385612, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385614, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385616, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(385618, 385605); //Hold [XBoxB_Btn_RB] and use Warp on the mech
		pullFromOrigLang.put(388894, 388893); //[XBoxB_Btn_R3] Press to Show Map -- FLERPY EDITS BEGIN HERE
		pullFromOrigLang.put(388896, 388895); //[XBoxB_Btn_R3] Press to Show Map
		pullFromOrigLang.put(502401, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(502402, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507525, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507526, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507527, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507528, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507577, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(507578, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(552671, 552672); //Press [XBoxB_Btn_LT] to focus on point of interest
		pullFromOrigLang.put(554184, 554257); //Press [XBoxB_Btn_DPadL] and [XBoxB_Btn_DPadR] to order squad to move or attack
		pullFromOrigLang.put(554185, 554272); //Press [XBoxB_Btn_A] to take cover
		pullFromOrigLang.put(554190, 543205); //Hold [XBoxB_Btn_LSUp] and then [XBoxB_Btn_A] to sprint out of cover
		pullFromOrigLang.put(554205, 554272); //Press [XBoxB_Btn_A] to take cover
		pullFromOrigLang.put(554215, 554216); //[XBoxB_Btn_A] Take cover
		pullFromOrigLang.put(554219, 543206); //Hold [XBoxB_Btn_LS] and press [XBoxB_Btn_A] to jump to near cover
		pullFromOrigLang.put(554220, 543207); //Hold [XBoxB_Btn_LS] and press [XBoxB_Btn_A] to turn corner
		pullFromOrigLang.put(554253, 554254); //[XBoxB_Btn_B] Break free
		pullFromOrigLang.put(554255, 554256); //Hold [XBoxB_Btn_LB] to swap to the sniper rifle
		pullFromOrigLang.put(554274, 554268); //Hold [XBoxB_Btn_B] to melee from cover
		pullFromOrigLang.put(555170, 338638); //Hold [XBoxB_Btn_LB] to switch weapons
		pullFromOrigLang.put(555172, 558124); //Hold [XBoxB_Btn_RB] to use powers
		pullFromOrigLang.put(555174, 334793); //Hold [XBoxB_Btn_RB] to display power wheel
		pullFromOrigLang.put(555204, 555200); //Hold [XBoxB_Btn_LSLeft] and press [XBoxB_Btn_A] to roll to next cover
		pullFromOrigLang.put(555205, 555201); //Move to the edge and press [XBoxB_Btn_L3] to slide around the corner
		pullFromOrigLang.put(555263, 555262); //Hold [XBoxB_Btn_B] to quick-kill from cover
		pullFromOrigLang.put(558663, 558662); //Press [XBoxB_Btn_X] to swap to heavy pistol
		pullFromOrigLang.put(558665, 558664); //Press [XBoxB_Btn_X] to swap to assault rifle
		pullFromOrigLang.put(562469, 562470); //Press [XBoxB_Btn_Start] and select Squad to level up
		pullFromOrigLang.put(562474, 562473); //Hold [XBoxB_Btn_A] to sprint
		pullFromOrigLang.put(562479, 562478); //Hold [XBoxB_Btn_LS] and press [XBoxB_Btn_A] to evade roll
		pullFromOrigLang.put(562481, 562480); //Hold [XBoxB_Btn_X] to swap weapons
		pullFromOrigLang.put(562483, 562482); //Hold [XBoxB_Btn_LS] and press [XBoxB_Btn_A] to vault over low cover
		pullFromOrigLang.put(563797, 563796); //Press [XBoxB_Btn_LSDown] to exit turret and [XBoxB_Btn_RT] to fire
		//pullFromOrigLang.put(571131,); REQUIRES MANUAL: To sneak-melee an enemy walk behind it and press and hold [XBoxB_Btn_B]
		//pullFromOrigLang.put(571132,); REQUIRES MANUAL: To sneak-melee an enemy walk behind it and press and hold [XBoxB_Btn_B]
		pullFromOrigLang.put(604097, 604098); //Press [XBoxB_Btn_B] rapidly to escape
		pullFromOrigLang.put(618884, 618885); //You are now in spectator mode. Use [XBoxB_Btn_LB] and [XBoxB_Btn_RB] to cycle through teammates
		pullFromOrigLang.put(619366, 619367); //You are now in flycam mode. Use [XBoxB_Btn_LB] or [XBoxB_Btn_RB] to return to spectator mode
		pullFromOrigLang.put(675661, 552673); //Hold [XBoxB_Btn_A] to run
		pullFromOrigLang.put(675662, 552674); //Hold or press [XBoxB_Btn_A] to leap across
		pullFromOrigLang.put(675841, 552664); //Hold [XBoxB_Btn_LSUp] and press [XBoxB_Btn_A] to climb up from cover
		pullFromOrigLang.put(675847, 501296); //Hold [XBoxB_Btn_LT] to aim and press [XBoxB_Btn_RT] to fire
		pullFromOrigLang.put(675848, 369315); //Press [XBoxB_Btn_B] to melee
		pullFromOrigLang.put(675849, 553098); //Hold [XBoxB_Btn_B] to heavy melee
		pullFromOrigLang.put(675850, 209098); //Press [XBoxB_Btn_X] to reload
		//pullFromOrigLang.put(675852,675851); REQUIRES MANUAL: String matches but is on multiple lines //Hold [XBoxB_Btn_LT] to pop up from cover and click [XBoxB_Btn_RT] to fire
		pullFromOrigLang.put(696668, 696667); //Press [XBoxB_Btn_R3] to display objectives
		pullFromOrigLang.put(705044, 705039); //Press [XBoxB_Btn_A] to activate
		pullFromOrigLang.put(705045, 705040); //Press [XBoxB_Btn_A] to revive
		pullFromOrigLang.put(705046, 562480); //Press [XBoxB_Btn_X] to swap weapons
		pullFromOrigLang.put(705047, 705042); //Mash [XBoxB_Btn_A] to extend your life!
		pullFromOrigLang.put(705561, 705037); //Press [XBoxB_Btn_DPadD] to use a medi-gel to revive yourself
		pullFromOrigLang.put(705562, 705038); //Press [XBoxB_Btn_DPadL] to spend an ammo pack to resupply your ammo
		pullFromOrigLang.put(706260, 670436); //Hold [XBoxB_Btn_LB] to select any weapon
		pullFromOrigLang.put(718564, 670181); //Double-tap [XBoxB_Btn_A] while walking toward low cover to quick climb --FLERPY EDITS END HERE
		//pullFromOrigLang.put(719838,); //REQUIRES TRANSLATION (By order of alliance command... press a then start
		pullFromOrigLang.put(720735, 720733); //Hold [XBoxB_Btn_RT] to charge weapon
		pullFromOrigLang.put(720736, 720734); //Move [XBoxB_Btn_LSDown] to exit turret
		pullFromOrigLang.put(721051, 719733); //The Mission Computer is the in-game menu.
		//pullFromOrigLang.put(721561,); //REQUIRES MANUAL Press ([Shared_SquadMove1]) or ([Shared_SquadMove2]) to order a squadmate to attack, move to cover, or take a position.
		//pullFromOrigLang.put(721562,); //requies manual
		//pullFromOrigLang.put(721563,); //reuise manual
		//pullFromOrigLang.put(721564,); //reuies manual
		//pullFromOrigLang.put(722042,); //can ignore
		pullFromOrigLang.put(722220, 722219); //Press [XBoxB_Btn_R3] to access the map while playing.
		pullFromOrigLang.put(722318, 722319); //Move by using [XBoxB_Btn_LS].
		pullFromOrigLang.put(722320, 722321); //Move [XBoxB_Btn_RS] to look around
		pullFromOrigLang.put(722322, 722323); //To run, press and hold [XBoxB_Btn_A] while moving forward.
		pullFromOrigLang.put(722324, 722325); //To fire a weapon, press [XBoxB_Btn_RT]. To reload, press [XBoxB_Btn_X].
		pullFromOrigLang.put(722326, 722327); //To dodge attacks and projectiles or to get out of cover quickly, move using [XBoxB_Btn_LS] and press [XBoxB_Btn_A].
		pullFromOrigLang.put(722328, 722329); //To perform a basic melee attack,
		pullFromOrigLang.put(722331, 722330); //Switch to the previous weapon held by pressing and holding [XBoxB_Btn_X].
		pullFromOrigLang.put(722332, 722333); //To bring up the Weapon Menu, press and hold
		pullFromOrigLang.put(722334, 722335); //To bring up the Power Wheel, press and hold
		pullFromOrigLang.put(722371, 722372); //powers can be mapped...
		//pullFromOrigLang.put(722373,); REQUIRES MANUAL Point your targeting reticle and press [XBoxB_Btn_DPadL] or [XBoxB_Btn_DPadR] to order a squadmate to a position.
		//pullFromOrigLang.put(722374,); //REQUIERS MANUAL Point the targeting reticle behind a cover position and press ([Shared_SquadMove1]) or ([Shared_SquadMove2]) to order a squadmate to take cover.
		//pullFromOrigLang.put(722375,); //REQUIRES TRANSLATION - To Point the targeting reticle at an enemy and press [XBoxB_Btn_DPadL] or [XBoxB_Btn_DPadR] to order a squadmate to attack the target with their respective default power. Any squadmate powers mapped to [XBoxB_Btn_DPadL] and [XBoxB_Btn_DPadR] work the same way. 

		//$722837
		//pullFromOrigLang.put(722376,); //REQUIRES MANUAL Point the targeting reticle at an enemy and press ([Shared_SquadAttack]) to order both squadmates to open fire on the opponent.
		//pullFromOrigLang.put(722377,); //REQUIRES MANUAL Press ([Shared_SquadFollow]) to rally squadmates to your current position.
		pullFromOrigLang.put(722398, 722397); //1. Take cover by pressing [XBoxB_Btn_A] 
		pullFromOrigLang.put(722401, 722402); //Move from one cover spot to adjacent cover on
		pullFromOrigLang.put(722403, 722404); //Round a corner in cover by standing at
		pullFromOrigLang.put(722405, 722406); //Vault over low cover by holding [XBoxB_Btn_LSUp] and pressing [XBoxB_Btn_A]. 
		pullFromOrigLang.put(722407, 722408); //Storm forward out of cover by holding [XBoxB_Btn_LSUp]
		pullFromOrigLang.put(722409, 722410); //Instantly kill enemies standing on the other side of a shared piece of low cover by moving across from them and pressing and holding [XBoxB_Btn_B]
		pullFromOrigLang.put(722411, 722412); //Attack enemies around the edges of cover by standing at the edge and pressing [XBoxB_Btn_B].
		pullFromOrigLang.put(722413, 722414); //Pop out of cover to fire accurately by 
		pullFromOrigLang.put(722415, 722416); //Fire over cover without standing up by pressing [XBoxB_Btn_RT].
		pullFromOrigLang.put(722585, 722586); //Fly between systems in galaxy view...
		pullFromOrigLang.put(722610, 722611); //Fly the Normandy across a system...
		pullFromOrigLang.put(722615, 722616); //Fly the Normandy through a cluster with 
		pullFromOrigLang.put(722617, 722618); //Mass relays are used as the 
		pullFromOrigLang.put(722622, 722623); //Press [UI_XBoxB_Btn_LT] to use
		pullFromOrigLang.put(723517, 723516); //Press [XBoxB_Btn_B] to exit Atlas
		pullFromOrigLang.put(724281, 724283); //Hold [XBoxB_Btn_LSLeft] to move into position
		pullFromOrigLang.put(724282, 724280); //Hold [XBoxB_Btn_LSRight] to move into position
		pullFromOrigLang.put(724629, 724630); //Hold [XBoxB_Btn_LT] to accurately aim your weapon.
		pullFromOrigLang.put(724748, 724568); //Press [Console_NavAssistanceOrCoverTurn] to show Udina's location on the map
		pullFromOrigLang.put(724947, 724946); //The current number of supplies you are carrying 
		pullFromOrigLang.put(813911, 813910); //-Move the [XBoxB_Btn_LS] to operate the claw...
		pullFromOrigLang.put(814588, 814589); //place a bet
		pullFromOrigLang.put(813590, 813589); //Use the stick to target...
		//pullFromOrigLang.put(786429,); REQUIRES MANUAL - PRESS Q TO DISTRACT SELECTED GUARD
		pullFromOrigLang.put(817553, 813055);
		pullFromOrigLang.put(817554, 813055);
		pullFromOrigLang.put(817555, 813051);
		pullFromOrigLang.put(817556, 813051);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		//get list of diffs to operate on.
		File dir = new File("E:\\Google Drive\\SP Controller Support\\TLK\\diffs");
		FileFilter fileFilter = new WildcardFileFilter("diff_*.xml");
		File[] files = dir.listFiles(fileFilter);

		String originalTlkDir = "E:\\Google Drive\\SP Controller Support\\TLK\\tankmaster_original_tlk\\"; //tankmaster
		String prefixCitPath = "E:\\Google Drive\\SP Controller Support\\TLK\\dlc_tlk\\DLC_EXP_Pack003_"; //tankmaster
		for (File f : files) {
			String inputFile = f.getAbsolutePath();
			String lang = FilenameUtils.getBaseName(inputFile);
			lang = lang.substring(lang.length() - 3, lang.length());

			File folder = new File(f.getParent() + "\\DLC_CON_XBX_" + lang.toUpperCase() + "\\");
			folder.mkdirs();
			String savename = f.getParent() + "\\DLC_CON_XBX_" + lang.toUpperCase() + "\\DLC_CON_XBX_" + lang.toUpperCase() + "0.xml";
			System.out.println("Performing replacement scan: " + savename);
			Document origDoc = dbFactory.newDocumentBuilder().parse("file:///" + inputFile);
			origDoc.getDocumentElement().normalize();

			//TANKMASTER
			Document basegameTlk = dbFactory.newDocumentBuilder().parse("file:///" + originalTlkDir + "tankmaster_base_" + lang + ".xml");
			basegameTlk.getDocumentElement().normalize();

			//TANKMASTER
			Document citTlk = dbFactory.newDocumentBuilder().parse("file:///" + prefixCitPath + lang + "/DLC_EXP_Pack003_" + lang + "1.xml");
			citTlk.getDocumentElement().normalize();

			NodeList origStringNodes = (NodeList) xpath.evaluate("/Strings/String", origDoc.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < origStringNodes.getLength(); i++) {
				Node singleNode = (Node) origStringNodes.item(i);
				Element oStringElem = (Element) singleNode;
				String idAsStr = oStringElem.getAttribute("id");
				//System.out.println("\t\tpullFromOrigLang.put("+idAsStr+",);");
				//System.out.println("Parsing ["+(i+1)+"/"+origStringNodes.getLength()+"]");
				int idAsInt = Integer.parseInt(idAsStr);
				if (idAsInt >= 135000000) {
					continue;
				}

				String content = oStringElem.getTextContent();
				Integer useId = pullFromOrigLang.get(idAsInt);
				if (useId != null) {
					//retreive from oriignal TLK
					NodeList originalTLKNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String[@id=" + useId + "]", basegameTlk.getDocumentElement(), XPathConstants.NODESET);
					if (originalTLKNodes.getLength() > 0) {

						//inBG
						Element idElem = (Element) originalTLKNodes.item(0);
						oStringElem.setTextContent(idElem.getTextContent());
						continue;
					}
					//check citadel
					NodeList citTLKNodes = (NodeList) xpath.evaluate("/TlkFile/Strings/String[@id='" + useId + "']", citTlk.getDocumentElement(), XPathConstants.NODESET);
					if (citTLKNodes.getLength() > 0) {
						Element idElem = (Element) citTLKNodes.item(0);
						oStringElem.setTextContent(idElem.getTextContent());
						continue;
					} else {
						System.out.println("NOT IN CIT: " + useId);
					}

				}
				if (!content.contains("[") && !idAsStr.equals("719838") || idAsStr.equals("")) {
					System.out.println("Translation required: " + idAsStr);
					continue; //speedup
				}

				if (localizedUncommonKeyMap.containsKey(idAsInt)) {
					content = content.replace(localizedUncommonKeyMap.get(idAsInt).src, localizedUncommonKeyMap.get(idAsInt).repl);
				}

				//content = content.replace("([Mouse_Btn_L])", "[XBoxB_Btn_RT]");
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
				//System.out.println(idAsStr+" Item done manually: "+content);
				oStringElem.setTextContent(content);
			}

			NodeList oldRoot = (NodeList) xpath.evaluate("/Strings", origDoc, XPathConstants.NODESET);
			if (oldRoot.getLength() > 0) {
				Element stringselem = (Element) oldRoot.item(0);
				Element tlkFileElem = origDoc.createElement("TlkFile");
				tlkFileElem.setAttribute("name", "DLC_CON_XBX_" + lang.toUpperCase() + "/DLC_CON_XBX_" + lang.toUpperCase() + "0.xml");
				tlkFileElem.appendChild(stringselem);
				origDoc.appendChild(tlkFileElem);
			}

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.VERSION, "1.0");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(origDoc), new StreamResult(new FileOutputStream(savename)));
		}
	}

	private static void replacePCPlaceholdersWithXbox() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("/TlkFile/Strings/String");
		File f = new File("E:\\Google Drive\\Mass Effect 3 Modding\\TLK\\SP Controller - ITA\\DLC_CON_XBX_ITA0.xml");
		Document doc = builder.parse(f);
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		int fixed = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			String idStr = n.getAttributes().getNamedItem("id").getTextContent();
			String originalcontent = n.getTextContent();
			Integer id = Integer.parseInt(idStr);
			String content = originalcontent.replace("([Shared_ShowMap])", "[XBoxB_Btn_A]");
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

			//NON () items
			content = content.replace("[Shared_Shoot]", "[XBoxB_Btn_RT]");
			content = content.replace("[Shared_Action]", "[XBoxB_Btn_A]");
			content = content.replace("[Shared_Melee]", "[XBoxB_Btn_B]");
			content = content.replace("[Shared_Aim]", "[XBoxB_Btn_LT]");
			content = content.replace("[Q]", "[XBoxB_Btn_DPadL]"); //distract guard

			n.setTextContent(content);

			if (!content.equals(originalcontent)) {
				fixed++;
				System.out.println("Automatic fix applied to " + id);
			}
		}
		System.out.println("Fixed " + fixed + " items");

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty(OutputKeys.VERSION, "1.0");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// send DOM to file
		tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(f)));
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
	private static void comparisonScan() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
		String origFolder = "E:\\Google Drive\\SP Controller Support\\TLK\\original_tlk\\";
		String moonFolder = "E:\\Google Drive\\SP Controller Support\\TLK\\moonshine_tlk\\";
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
		FileFilter ofileFilter = new WildcardFileFilter("bg_*.xml");
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
					//System.out.println("content mismatch");
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
			ArrayList<TLKNode> tlks = nameElementMap.get(lang);
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
					TLKNode node = new TLKNode(idAsInt, content);
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
			for (TLKNode node : langEntry.getValue()) {
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

	private static void decompileTLK(String path) {
		//		File dir = new File(System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "tlkfiles" + File.separator);
		File dir = new File(path);

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

	private static void compileTLK(String path) throws IOException, InterruptedException {
		File dir = new File(path);
		System.out.println(dir);
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter(".xml"), FalseFileFilter.FALSE);
		for (File file : files) {
			System.out.println(file.getAbsolutePath());
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
			p.waitFor();
		}
	}

	/**
	 * Compiles a TLK file from a .xml file (tankmaster)
	 * 
	 * @param file
	 * @return
	 */
	public static ProcessResult compileTLK(File file) {
		ArrayList<String> commandBuilder = new ArrayList<String>();

		String compilerPath = ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe";
		commandBuilder.add(compilerPath);
		commandBuilder.add(file.getAbsolutePath());
		commandBuilder.add("--mode");
		commandBuilder.add("ToTlk");
		commandBuilder.add("--no-ui");
		String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
		ProcessBuilder pb = new ProcessBuilder(command);
		return ModManager.runProcess(pb);
	}

	/**
	 * Decompiles a TLK file
	 * 
	 * @return
	 */
	public static ProcessResult decompileTLK(File file) {
		ArrayList<String> commandBuilder = new ArrayList<String>();

		String compilerPath = ModManager.getTankMasterTLKDir() + "MassEffect3.TlkEditor.exe";
		commandBuilder.add(compilerPath);
		commandBuilder.add(file.getAbsolutePath()); //inputfile
		commandBuilder.add(file.getParent() + File.separator + FilenameUtils.getBaseName(file.getAbsolutePath()) + ".xml"); //manifest name for output
		commandBuilder.add("--mode");
		commandBuilder.add("ToXml");
		commandBuilder.add("--no-ui");
		String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
		return ModManager.runProcess(new ProcessBuilder(command));
	}
}
