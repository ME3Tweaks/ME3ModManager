package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.valueparsers.biodifficulty.Category;
import com.me3tweaks.modmanager.valueparsers.wavelist.Wave;

@SuppressWarnings("serial")
public class ModMakerCompilerWindow extends JDialog {
	boolean modExists = false, error = false;
	String code, modName, modDescription;
	ModManagerWindow callingWindow;
	private static int TOTAL_STEPS = 10;
	private static String DOWNLOADED_XML_FILENAME = "mod_info";
	private int stepsCompleted = 1;
	ArrayList<String> requiredCoals = new ArrayList<String>();
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;
	JSONObject mod_object, mod_info;
	Element infoElement, dataElement;
	JLabel infoLabel, currentOperationLabel;
	JProgressBar overallProgress, currentStepProgress;

	public ModMakerCompilerWindow(ModManagerWindow callingWindow, String code) {
		this.code = code;
		this.callingWindow = callingWindow;
		this.setTitle("Mod Maker Compiler");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(420, 228));
		// this.setResizable(false);
		// this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		getModInfo();
		if (!error) {
			this.setVisible(true);
		}
	}

	private void setupWindow() {
		JPanel modMakerPanel = new JPanel();
		modMakerPanel.setLayout(new BoxLayout(modMakerPanel,
				BoxLayout.PAGE_AXIS));
		infoLabel = new JLabel("Preparing to compile " + code + "...");
		modMakerPanel.add(infoLabel);

		JLabel overall = new JLabel("Overall progress");
		JLabel current = new JLabel("Current operation");
		currentOperationLabel = new JLabel("Downloading mod information...");
		overallProgress = new JProgressBar(0, 100);
		overallProgress.setStringPainted(true);
		overallProgress.setIndeterminate(false);
		overallProgress.setEnabled(false);

		currentStepProgress = new JProgressBar(0, 100);
		currentStepProgress.setStringPainted(true);
		currentStepProgress.setIndeterminate(false);
		currentStepProgress.setEnabled(false);

		modMakerPanel.add(overall);
		modMakerPanel.add(overallProgress);
		modMakerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		modMakerPanel.add(current);
		modMakerPanel.add(currentOperationLabel);
		modMakerPanel.add(currentStepProgress);

		modMakerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.getContentPane().add(modMakerPanel);
		}

	private void getModInfo() {
		//String link = "http://www.me3tweaks.com/modmaker/download.php?id="
		//		+ code;
		String link = "http://webdev-c9-mgamerz.c9.io/modmaker/download.php?id="
				+ code;
		System.out.println("Fetching mod from "+link);
		try {
			File downloaded = new File(DOWNLOADED_XML_FILENAME);
			FileUtils.copyURLToFile(new URL(link), downloaded);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(downloaded);
			doc.getDocumentElement().normalize();
			NodeList errors = doc.getElementsByTagName("error");
			if (errors.getLength() > 0) {
				//error occured.
				dispose();
				error = true;
				JOptionPane.showMessageDialog(null,
					    "<html>No mod with id "+code+" was found on ME3Tweaks.</html>",
					    "Compiling Error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			parseModInfo();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Converts a short name (e.g. MP3, MP4) into the DLC or original coalesced
	 * name (Default_DLC_CON_MP3.bin).
	 * 
	 * @param shortName
	 *            Short name to convert
	 * @return Coaleced filename or null if unknown.
	 */
	protected String shortNameToCoalFilename(String shortName) {
		switch (shortName) {
		case "MP1":
			return "Default_DLC_CON_MP1.bin";
		case "MP2":
			return "Default_DLC_CON_MP2.bin";
		case "MP3":
			return "Default_DLC_CON_MP3.bin";
		case "MP4":
			return "Default_DLC_CON_MP4.bin";
		case "MP5":
			return "Default_DLC_CON_MP5.bin";
		case "PATCH1":
			return "Default_DLC_UPD_Patch01.bin";
		case "PATCH2":
			return "Default_DLC_UPD_Patch02.bin";
		case "BASEGAME":
			return "Coalesced.bin";
		default:
			return null;
		}
	}
	

	/**
	 * Converts a coalesced filename to a header you can place in moddesc.ini files.
	 * @param coalName Coalesced file
	 * @return Corresponding heading
	 */
	protected String coalNameToModDescName(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "RESURGENCE";
		case "Default_DLC_CON_MP2.bin":
			return "REBELLION";
		case "Default_DLC_CON_MP3.bin":
			return "EARTH";
		case "Default_DLC_CON_MP4.bin":
			return "RETALIATION";
		case "Default_DLC_CON_MP5.bin":
			return "RECKONING";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2";
		case "Coalesced.bin":
			return "BASEGAME";
		default:
			return null;
		}
	}

	/**
	 * Converts a coal filename (Default_DLC_CON_MP3.bin) into the short name for the DLC (MP3
	 * 
	 * @param coalName
	 *            Filename to convert to a short name
	 * @return Short name or null if unknown.
	 */
	protected String coalFilenameToShortName(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "MP1";
		case "Default_DLC_CON_MP2.bin":
			return "MP2";
		case "Default_DLC_CON_MP3.bin":
			return "MP3";
		case "Default_DLC_CON_MP4.bin":
			return "MP4";
		case "Default_DLC_CON_MP5.bin":
			return "MP5";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2";
		case "Coalesced.bin":
			return "BASEGAME";
		default:
			System.out.println("UNRECOGNIZED COAL FILE: "+coalName);
			return null;
		}
	}
	
	/**
	 * Converts the Coalesced.bin filenames to their respective directory in the .sfar files.
	 * @param coalName name of coal being packed into the mod
	 * @return path to the file to repalce
	 */
	protected String coalFileNameToDLCDir(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "/BIOGame/DLC/DLC_CON_MP1/CookedPCConsole/Default_DLC_CON_MP1.bin";
		case "Default_DLC_CON_MP2.bin":
			return "/BIOGame/DLC/DLC_CON_MP2/CookedPCConsole/Default_DLC_CON_MP2.bin";
		case "Default_DLC_CON_MP3.bin":
			return "/BIOGame/DLC/DLC_CON_MP3/CookedPCConsole/Default_DLC_CON_MP3.bin";
		case "Default_DLC_CON_MP4.bin":
			return "/BIOGame/DLC/DLC_CON_MP4/CookedPCConsole/Default_DLC_CON_MP4.bin";
		case "Default_DLC_CON_MP5.bin":
			return "/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin";
		case "Default_DLC_UPD_Patch01.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch01/CookedPCConsole/Default_DLC_UPD_Patch01.bin";
		case "Default_DLC_UPD_Patch02.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch02/CookedPCConsole/Default_DLC_UPD_Patch02.bin";
		case "Coalesced.bin":
			return "\\BIOGame\\CookedPCConsole\\Coalesced.bin";
		default:
			System.out.println("UNRECOGNIZED COAL FILE: "+coalName);
			return null;
		}
	}
	
	/**
	 * Converts the Coalesced.bin filenames to their respective PCConsoleTOC directory in the .sfar files.
	 * @param coalName name of coal being packed into the mod
	 * @return path to the file to repalce
	 */
	protected String coalFileNameToDLCTOCDir(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "/BIOGame/DLC/DLC_CON_MP1/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP2.bin":
			return "/BIOGame/DLC/DLC_CON_MP2/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP3.bin":
			return "/BIOGame/DLC/DLC_CON_MP3/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP4.bin":
			return "/BIOGame/DLC/DLC_CON_MP4/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP5.bin":
			return "/BIOGame/DLC/DLC_CON_MP5/PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch01.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch01/PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch02.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch02/PCConsoleTOC.bin";
		case "Coalesced.bin":
			//not used
			return "\\BIOGame\\PCConsoleTOC.bin";
		default:
			System.out.println("[coalFileNameToDLCTOCDIR] UNRECOGNIZED COAL FILE: "+coalName);
			return null;
		}
	}

	protected void parseModInfo() {
		NodeList infoNodeList = doc.getElementsByTagName("ModInfo");
		infoElement = (Element) infoNodeList.item(0); //it'll be the only element. Hopefully!
		NodeList nameElement = infoElement.getElementsByTagName("Name");
		modName = nameElement.item(0).getTextContent();
		
		NodeList descElement = infoElement.getElementsByTagName("Description");
		modDescription = descElement.item(0).getTextContent();
		
		//Check the name
		File moddir = new File(modName);
		if (moddir.isDirectory()) {
			try {
				ModManager.debugLogger.writeMessage("DEBUGGING: Remove existing mod directory");
				FileUtils.deleteDirectory(moddir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*dispose();
			JOptionPane.showMessageDialog(null,
				    "<html>A mod with this name already exists.</html>",
				    "Compiling Error",
				    JOptionPane.ERROR_MESSAGE);*/
		}
		
		//Debug remove
		infoLabel.setText("Compiling " + modName + "...");
		NodeList dataNodeList = doc.getElementsByTagName("ModData");
		dataElement = (Element) dataNodeList.item(0);
		NodeList fileNodeList = dataElement.getChildNodes();
		//Find the coals it needs, iterate over the files list.
		for (int i = 0; i < fileNodeList.getLength(); i++) {
			Node fileNode = fileNodeList.item(i);
			if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
				//filters out the #text nodes. Don't know what those really are.
				String intCoalName = fileNode.getNodeName();
				System.out.println("File descriptor found in mod: "+intCoalName);	
				requiredCoals.add(shortNameToCoalFilename(intCoalName));
			}
		}
		
		// Check Coalesceds
		File coalDir = new File("coalesceds");
		coalDir.mkdirs(); // creates if it doens't exist. otherwise nothing.
		ArrayList<String> coals = new ArrayList<String>(requiredCoals); // copy
																		// so we
																		// don't
																		// modify
																		// the
																		// required
																		// ones
		int numToDownload = 0;
		for (int i = coals.size() - 1; i >= 0; i--) {
			String coal = coals.get(i); // go in reverse order otherwise we get
										// null pointer
			File coalFile = new File("coalesceds/" + coal);
			if (!coalFile.exists()) {
				numToDownload++;
			} else {
				coals.remove(i);
			}
		}
		if (numToDownload > 0) {
			currentOperationLabel.setText("Downloading Coalesced files...");
			currentStepProgress.setIndeterminate(false);
		}
		// Check and download
		new CoalDownloadWorker(coals, currentStepProgress).execute();
	}

	/**
	 * Runs the Coalesced files through Tankmasters decompiler
	 */
	public void decompileMods() {
		// TODO Auto-generated method stub

		new DecompilerWorker(requiredCoals, currentStepProgress).execute();
	} /*

	/**
	 * Decompiles a coalesced into .xml files using tankmaster's tools.
	 * @author Michael
	 *
	 */
	class DecompilerWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> coalsToDecompile;
		private JProgressBar progress;
		private int numCoals;

		public DecompilerWorker(ArrayList<String> coalsToDecompile,
				JProgressBar progress) {
			this.coalsToDecompile = coalsToDecompile;
			this.numCoals = coalsToDecompile.size();
			this.progress = progress;
			currentOperationLabel.setText("Decompiling "
					+ this.coalsToDecompile.get(0));
		}

		protected Void doInBackground() throws Exception {
			int coalsDecompiled = 0;
			String path = Paths.get(".").toAbsolutePath().normalize()
					.toString();
			for (String coal : coalsToDecompile) {
				String compilerPath = path
						+ "\\Tankmaster Compiler\\MassEffect3.Coalesce.exe";
				ProcessBuilder decompileProcessBuilder = new ProcessBuilder(
						compilerPath, path + "\\coalesceds\\"
								+ coal);
				decompileProcessBuilder.redirectErrorStream(true);
				decompileProcessBuilder
						.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				Process decompileProcess = decompileProcessBuilder.start();
				decompileProcess.waitFor();
				coalsDecompiled++;
				this.publish(coalsDecompiled);
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (numCoals > numCompleted.get(0)) {
				currentOperationLabel.setText("Decompiling "
						+ coalsToDecompile.get(numCompleted.get(0)));
			}
			progress.setValue(100 / (numCoals / numCompleted.get(0)));
		}

		protected void done() {
			// Coals downloaded
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			ModManager.debugLogger.writeMessage("Coals decompiled");
			new MergeWorker(progress).execute();
		}
	}

	class CompilerWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> coalsToCompile;
		private JProgressBar progress;
		private int numCoals;

		public CompilerWorker(ArrayList<String> coalsToCompile,
				JProgressBar progress) {
			this.coalsToCompile = coalsToCompile;
			this.numCoals = coalsToCompile.size();
			this.progress = progress;
			currentOperationLabel.setText("Recompiling "
					+ this.coalsToCompile.get(0));
		}

		protected Void doInBackground() throws Exception {
			int coalsCompiled = 0;
			String path = Paths.get(".").toAbsolutePath().normalize()
					.toString();
			for (String coal : coalsToCompile) {
				String compilerPath = path
						+ "\\Tankmaster Compiler\\MassEffect3.Coalesce.exe";
				ProcessBuilder compileProcessBuilder = new ProcessBuilder(
						compilerPath, "--xml2bin", path + "\\coalesceds\\"
								+ FilenameUtils.removeExtension(coal)+".xml");
				//log it
				ModManager.debugLogger.writeMessage("Executing compile command: "+
						compilerPath+" --xml2bin "+ path + "\\coalesceds\\"
								+ FilenameUtils.removeExtension(coal)+".xml");
				compileProcessBuilder.redirectErrorStream(true);
				compileProcessBuilder
						.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				Process compileProcess = compileProcessBuilder.start();
				compileProcess.waitFor();
				coalsCompiled++;
				this.publish(coalsCompiled);
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (numCoals > numCompleted.get(0)) {
				currentOperationLabel.setText("Recompiling "
						+ coalsToCompile.get(numCompleted.get(0)));
			}
			progress.setValue(100 / (numCoals / numCompleted.get(0)));
		}

		protected void done() {
			// Coals downloaded
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			ModManager.debugLogger.writeMessage("Coals recompiled");
			new TOCDownloadWorker(coalsToCompile, progress).execute();
		}
	}

	class CoalDownloadWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> coalsToDownload;
		private JProgressBar progress;
		private int numCoals;

		public CoalDownloadWorker(ArrayList<String> coalsToDownload,
				JProgressBar progress) {
			this.coalsToDownload = coalsToDownload;
			this.numCoals = coalsToDownload.size();
			this.progress = progress;
			if (numCoals > 0) {
				currentOperationLabel.setText("Downloading "
						+ coalsToDownload.get(0));
			}
		}

		protected Void doInBackground() throws Exception {
			int coalsCompeted = 0;
			for (String coal : coalsToDownload) {
				try {
					String link = "http://www.me3tweaks.com/coal/" + coal;
					FileUtils.copyURLToFile(new URL(link), new File(
							"coalesceds/" + coal));
					coalsCompeted++;
					this.publish(coalsCompeted);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (numCoals > numCompleted.get(0)) {
				currentOperationLabel.setText("Downloading "
						+ coalsToDownload.get(numCompleted.get(0)));
			}
			progress.setValue(100 / (numCoals / numCompleted.get(0)));
		}

		protected void done() {
			// Coals downloaded
			ModManager.debugLogger.writeMessage("Coals downloaded");
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			decompileMods();
		}
	}
	
	/**
	 * After coals are downloaded and decompiled, this worker is created and
	 * merges the contents of the downloaded mod into all of the decompiled json files.
	 */
	class MergeWorker extends SwingWorker<Void, Integer> {
		private int numFilesToMerge;
		private JProgressBar progress;
		boolean error = false;

		public MergeWorker(JProgressBar progress) {
			System.out.println("Beginning MERGE operation.");
			this.progress = progress;
			currentOperationLabel.setText("Merging Coalesced files...");
		}

		protected Void doInBackground() throws Exception {
			int coalsCompeted = 0;
			// we are going to parse the mod_data array and then look at all the
			// files in the array.
			// Haha wow this is going to be ugly.
			
			
			/*
			 * Structure of mod_data array and elements
			 * <ModInfo>
			 * 	<Coalesced ID>
			 *   <Filename>
			 *    <Properties (with path attribute)>
			 */

			NodeList coalNodeList = dataElement.getChildNodes();
			//Iterate over the coalesceds.
			for (int i = 0; i < coalNodeList.getLength(); i++) {
				//coalNode is a node containing the coalesced module, such as <MP1> or <BASEGAME>
				Node coalNode = coalNodeList.item(i);
				if (coalNode.getNodeType() == Node.ELEMENT_NODE) {
					String intCoalName = coalNode.getNodeName(); //get the coal name so we can figure out what folder to look in.
					ModManager.debugLogger.writeMessage("Read coalecesed ID: "+intCoalName);
					String foldername = FilenameUtils.removeExtension(shortNameToCoalFilename(intCoalName));
					NodeList filesNodeList = coalNode.getChildNodes();
					for (int j = 0; j < filesNodeList.getLength(); j++) {
						Node fileNode = filesNodeList.item(j);
						if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
							//we now have a file ID such as biogame.
							//We need to load that XML file now.
							String iniFileName = fileNode.getNodeName()+".xml";
							Document iniFile = dbFactory.newDocumentBuilder().parse("coalesceds\\"+foldername+"\\"+iniFileName);
							iniFile.getDocumentElement().normalize();
							ModManager.debugLogger.writeMessage("Loaded "+iniFile.getDocumentURI()+" into memory.");
							NodeList assetList = iniFile.getElementsByTagName("CoalesceAsset");
							Element coalesceAsset = (Element) assetList.item(0);
							NodeList sectionsTagList = coalesceAsset.getElementsByTagName("Sections");
							Element sections = (Element) sectionsTagList.item(0);
							NodeList SectionList = sections.getElementsByTagName("Section");
							
							//We are now at at the "sections" array.
							//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
							NodeList mergeList = fileNode.getChildNodes();
							for (int k = 0; k < mergeList.getLength(); k++){
								//for every property in this filenode (of the data to merge)...
								Node newproperty = mergeList.item(k);
								if (newproperty.getNodeType() == Node.ELEMENT_NODE) {
									//<Property type="2" name="defaultgravityz" path="engine.worldinfo">-50</Property>
									boolean isArrayProperty = false;
									Element property = (Element) newproperty;
									String newPropName = null;
									String arrayType = null;
									String operation = null;
									String matchontype = null;
									if (property.getNodeName().equals("Property")) {
										//Property
										newPropName = property.getAttribute("name");
										isArrayProperty = false;
									} else {
										//ArrayProperty
										//<ArrayProperty path="sfxgamempcontent.sfxdifficultyhandlermp&amp;level2difficultydata" type="3" arraytype="biodifficulty" matchontype="3" operation="modify">(Cate...</ArrayProperty>
										arrayType = property.getAttribute("arraytype");
										matchontype = property.getAttribute("matchontype");
										operation = property.getAttribute("operation");
										isArrayProperty = true;

									}
									
									String newValue = newproperty.getTextContent();
									
									//first tokenize the path...
									String path = property.getAttribute("path");
									StringTokenizer drillTokenizer = new StringTokenizer(path,"&"); // - splits this in the event we need to drill down. Spaces are valid it seems in the path.
									Element drilled = null;
									NodeList drilledList = SectionList;
									while (drillTokenizer.hasMoreTokens()){
										//drill
										String drillTo = drillTokenizer.nextToken();
										ModManager.debugLogger.writeMessage("Drilling to find: "+drillTo);
										boolean pathfound = false;
										for (int l = 0; l < drilledList.getLength(); l++) {
											//iterate over all sections...
											Node drilledNode = drilledList.item(l); //L, not a 1.
											if (drilledNode.getNodeType() == Node.ELEMENT_NODE) {
												drilled = (Element) drilledNode;
												//System.out.println("Checking attribute: "+drilled.getAttribute("name"));
												if (!drilled.getAttribute("name").equals(drillTo)) {
													continue;
												} else {
													//this is the section we want.
													ModManager.debugLogger.writeMessage("Found "+drillTo);
													drilledList = drilled.getChildNodes();
													pathfound = true;
													break;
												}
											}
										}
										if (!pathfound) {
											dispose();
											JOptionPane.showMessageDialog(null,
												    "<html>Could not find the path "+path+" to property.</html>",
												    "Compiling Error",
												    JOptionPane.ERROR_MESSAGE);
											error = true;
											return null;
										}
									}
									if (drilled == null) {
										//we didn't find what we wanted...
										dispose();
										error = true;
										JOptionPane.showMessageDialog(null,
											    "<html>Could not find the path "+path+" to property.</html>",
											    "Compiling Error",
											    JOptionPane.ERROR_MESSAGE);
										return null;
									}
									//we are where we want to be. Now we can set the property or array value.
									//drilled is the element (parent of our property) that we want.
									NodeList props = drilled.getChildNodes(); //get children of the path (<property> list)
									ModManager.debugLogger.writeMessage("Number of child property/elements to search: "+props.getLength());
									for (int m = 0; m < props.getLength(); m++){
										Node propertyNode = props.item(m);
										if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
											Element itemToModify = (Element) propertyNode;
											//Check on property
											if (!isArrayProperty) {
												if (itemToModify.getAttribute("name").equals(newPropName)) {
													itemToModify.setTextContent(newValue);
													ModManager.debugLogger.writeMessage("Set "+newPropName+" to "+newValue);
													break;
												}
											} else {
												//Check on ArrayProperty
												ModManager.debugLogger.writeMessage("Candidates only will be returned if they are of type: "+matchontype);
												ModManager.debugLogger.writeMessage("Scanning property type: "+itemToModify.getAttribute("type"));
												if (itemToModify.getAttribute("type").equals(matchontype)) {
													//potential array value candidate...
													boolean match = false;
													ModManager.debugLogger.writeMessage("Found candidate for arrayreplace: "+itemToModify.getTextContent());
													switch (arrayType) {
													//Must use individual matching algorithms so we can figure out if something matches.
														case "biodifficulty": {
															//Match on Category (name)
															Category existing = new Category(itemToModify.getTextContent());
															Category importing = new Category(newValue);
															if (existing.matchIdentifiers(importing)) {
																ModManager.debugLogger.writeMessage("Match found: "+existing.categoryname);
																existing.merge(importing);
																newValue = existing.createCategoryString();
																match = true;
															} else {
																ModManager.debugLogger.writeMessage("Match failed: "+existing.categoryname);
															}
														}
														break;
														case "wave": {
															//Match on Difficulty
															Wave existing = new Wave(itemToModify.getTextContent());
															Wave importing = new Wave(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
																newValue = importing.createWaveString(); //doens't really matter, but makes me feel good my code works
															}
														}
														break;
														default:
															ModManager.debugLogger.writeMessage("ERROR: Unknown matching algorithm - does this client need updated?");
															break;
													} //end switch
													if (match) {
														itemToModify.setTextContent(newValue);
														ModManager.debugLogger.writeMessage("Set array property to "+newValue);
														break;
													}
												}
											}
										}
									}
								}
							}
							//end of the file node.
							//Time to save the file...
							Transformer transformer = TransformerFactory.newInstance().newTransformer();
							File outputFile = new File("coalesceds\\"+foldername+"\\"+iniFileName);
							Result output = new StreamResult(outputFile);
							Source input = new DOMSource(iniFile);
							ModManager.debugLogger.writeMessage("Saving file: "+outputFile.toString());
							transformer.transform(input, output);
						}
					}
				}
			}
			
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			/*if (numCoals > numCompleted.get(0)) {
				currentOperationLabel.setText("Downloading "
						+ coalsToDownload.get(numCompleted.get(0)));
			}
			progress.setValue(100 / (numCoals / numCompleted.get(0)));*/
		}

		protected void done() {
			// Coals downloaded
			if (error) {
				return;
			}
			ModManager.debugLogger.writeMessage("Finished merging coals.");

			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			new CompilerWorker(requiredCoals, progress).execute();
		}
	}



	/**
	 * Creates a CMM Mod package from the completed previous steps.
	 */
	private void createCMMMod() {
		currentOperationLabel.setText("Creating mod directory and descriptor...");
		File moddir = new File(modName);
		moddir.mkdirs(); // created mod directory

		// Write mod descriptor file
		Wini ini;
		File moddesc = new File(moddir + "\\moddesc.ini");
		try {
			if (!moddesc.exists())
				moddesc.createNewFile();
			ini = new Wini(moddesc);
			ini.put("ModManager", "cmmver", 3.0);
			ini.put("ModInfo", "modname", modName);
			ini.put("ModInfo", "moddesc", modDescription+"<br>Created with Mod Maker.");
			ini.put("ModInfo", "modsite", "http://me3tweaks.com");
			ini.put("ModInfo", "modid", "8");
			
			// Create directories, move files to them
			for (String reqcoal : requiredCoals) {
				File compCoalDir = new File(moddir.toString() + "\\"+coalFilenameToShortName(reqcoal)); //MP4, PATCH2 folders in mod package
				compCoalDir.mkdirs();
				String fileNameWithOutExt = FilenameUtils.removeExtension(reqcoal);
				//copy coal
				File coalFile = new File("coalesceds\\"+reqcoal);
				File destCoal = new File(compCoalDir+"\\"+reqcoal);
				destCoal.delete();
				if (coalFile.renameTo(destCoal)){
					ModManager.debugLogger.writeMessage("Moved "+reqcoal+" to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" to the proper mod element directory. Could already exist.");
				}
				//copy pcconsoletoc
				if (!reqcoal.equals("Coalesced.bin")) {
					File tocFile = new File("toc\\"+coalFilenameToShortName(reqcoal)+"\\PCConsoleTOC.bin");
					File destToc = new File(compCoalDir+"\\PCConsoleTOC.bin");
					destToc.delete();
					if (tocFile.renameTo(destToc)){
						ModManager.debugLogger.writeMessage("Moved "+reqcoal+" TOC to proper mod element directory");
					} else {
						ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" TOC to the proper mod element directory. Could already exist.");
					}
				}

				File compCoalSourceDir = new File("coalesceds\\"+fileNameWithOutExt);
				
				//TODO: Add PCConsoleTOC.bin to the desc file.
				boolean basegame = 	reqcoal.equals("Coalesced.bin");
				
				ini.put(coalNameToModDescName(reqcoal), "moddir", coalFilenameToShortName(reqcoal));
				
				if (basegame) {
					ini.put(coalNameToModDescName(reqcoal), "newfiles", reqcoal);
					ini.put(coalNameToModDescName(reqcoal), "replacefiles", coalFileNameToDLCDir(reqcoal));					
				} else {
					ini.put(coalNameToModDescName(reqcoal), "newfiles", reqcoal+";PCConsoleTOC.bin");
					ini.put(coalNameToModDescName(reqcoal), "replacefiles", coalFileNameToDLCDir(reqcoal)+";"+coalFileNameToDLCTOCDir(reqcoal));					
				}


				
				try {
					FileUtils.deleteDirectory(compCoalSourceDir);
					FileUtils.deleteDirectory(compCoalSourceDir);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ini.store();
			}
			
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Mod file encountered an I/O error while attempting to write it. Mod Descriptor not saved.");
		}

		//TOC the mod
		ModManager.debugLogger.writeMessage("Running autotoc on mod.");
		Mod newMod = new Mod(moddesc.toString());
		new AutoTocWindow(callingWindow, newMod);
		
		//Mod Created!
		dispose();
		JOptionPane.showMessageDialog(this, modName+" was successfully created!", "Mod Created", JOptionPane.INFORMATION_MESSAGE);
		callingWindow.dispose();
		new ModManagerWindow(false);
	}
	
	class TOCDownloadWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> tocsToDownload;
		private JProgressBar progress;
		private int numtoc;

		public TOCDownloadWorker(ArrayList<String> tocsToDownload,
				JProgressBar progress) {
			this.tocsToDownload = tocsToDownload;
			if (this.tocsToDownload.contains("Coalesced.bin")) {
				this.numtoc = tocsToDownload.size() - 1;
			} else {
				this.numtoc = tocsToDownload.size();

			}
			this.progress = progress;
			if (numtoc > 0) {
				currentOperationLabel.setText("Downloading "
						+ tocsToDownload.get(0));
			}
		}

		protected Void doInBackground() throws Exception {
			int tocsCompleted = 0;
			for (String toc : tocsToDownload) {
				if (toc.equals("Coalesced.bin")) {
					continue; //ignore basegame
				}
				try {
					String link = "http://www.me3tweaks.com/toc/" + coalFilenameToShortName(toc) + "/PCConsoleTOC.bin";
					ModManager.debugLogger.writeMessage("Downloading TOC file: "+link);
					FileUtils.copyURLToFile(new URL(link), new File(
							"toc/" + coalFilenameToShortName(toc) +"/PCConsoleTOC.bin"));
					tocsCompleted++;
					this.publish(tocsCompleted);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (numtoc > numCompleted.get(0)) {
				currentOperationLabel.setText("Downloading "
						+ tocsToDownload.get(numCompleted.get(0)));
			}
			progress.setValue(100 / (numtoc / numCompleted.get(0)));
		}

		protected void done() {
			// Coals downloaded
			ModManager.debugLogger.writeMessage("TOCs downloaded");
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			createCMMMod();
		}
	}
	
	public static String toString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
}
