package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.valueparsers.biodifficulty.Category;
import com.me3tweaks.modmanager.valueparsers.possessionwaves.Difficulty;
import com.me3tweaks.modmanager.valueparsers.sharedassignment.SharedDifficulty;
import com.me3tweaks.modmanager.valueparsers.wavelist.Wave;

@SuppressWarnings("serial")
public class ModMakerCompilerWindow extends JDialog {
	boolean modExists = false, error = false;
	String code, modName, modDescription, modId, modDev, modVer;
	ModManagerWindow callingWindow;
	private static double TOTAL_STEPS = 9;
	private static String DOWNLOADED_XML_FILENAME = "mod_info";
	private int stepsCompleted = 1;
	private double modMakerVersion;
	ArrayList<String> requiredCoals = new ArrayList<String>();
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;
	ArrayList<String> languages;
	//JSONObject mod_object, mod_info;
	Element infoElement, dataElement, tlkElement;
	JLabel infoLabel, currentOperationLabel;
	JProgressBar overallProgress, currentStepProgress;

	public ModMakerCompilerWindow(ModManagerWindow callingWindow, String code, ArrayList<String> languages) {
		this.code = code;
		this.languages = languages;
		this.callingWindow = callingWindow;
		this.setTitle("ModMaker Compiler");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(420, 200));
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
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane,BoxLayout.LINE_AXIS));
		infoLabel = new JLabel("Preparing to compile " + code + "...", SwingConstants.CENTER);
		infoPane.add(Box.createHorizontalGlue());
		infoPane.add(infoLabel);
		infoPane.add(Box.createHorizontalGlue());
		
		modMakerPanel.add(infoPane);
		//JLabel overall = new JLabel("Overall progress");
		TitledBorder overallBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Overall Progress");
		//JLabel current = new JLabel("Current operation");
		TitledBorder currentBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Current Operation");
		currentOperationLabel = new JLabel("Downloading mod information...", SwingConstants.CENTER);
		overallProgress = new JProgressBar(0, 100);
		overallProgress.setStringPainted(true);
		overallProgress.setIndeterminate(false);
		overallProgress.setEnabled(false);

		currentStepProgress = new JProgressBar(0, 100);
		currentStepProgress.setStringPainted(true);
		currentStepProgress.setIndeterminate(false);
		currentStepProgress.setEnabled(false);
		
		JPanel overallPanel = new JPanel();
		overallPanel.setBorder(overallBorder);
		overallPanel.add(overallProgress);

		modMakerPanel.add(overallPanel);
		modMakerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		JPanel currentPanel = new JPanel(new BorderLayout());
		currentPanel.setBorder(currentBorder);
		
		currentPanel.add(currentOperationLabel);
		currentPanel.add(currentStepProgress, BorderLayout.SOUTH);
		modMakerPanel.add(currentPanel);
		modMakerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setResizable(false);
		this.getContentPane().add(modMakerPanel);
		}

	private void getModInfo() {
		//String link = "http://www.me3tweaks.com/modmaker/download.php?id="
		//		+ code;
		ModManager.debugLogger.writeMessage("================DOWNLOADING MOD INFORMATION==============");
		String link;
		if (ModManager.IS_DEBUG) {
			link = "https://webdev-c9-mgamerz.c9.io/modmaker/download.php?id="+ code;
		} else {
			link = "https://me3tweaks.com/modmaker/download.php?id="+ code;
		}
		ModManager.debugLogger.writeMessage("Fetching mod from "+link);
		try {
			File downloaded = new File(DOWNLOADED_XML_FILENAME);
			downloaded.delete();
			FileUtils.copyURLToFile(new URL(link), downloaded);
			ModManager.debugLogger.writeMessage("Mod downloaded to "+downloaded);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			ModManager.debugLogger.writeMessage("Loading mod information into memory.");
			doc = dBuilder.parse(downloaded);
			ModManager.debugLogger.writeMessage("Mod information loaded into memory.");
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
				ModManager.debugLogger.writeMessage("Downloaded mod information indicates this mod doesn't exist on modmaker.");
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
		case "TESTPATCH":
			return "Default_DLC_TestPatch.bin";
		case "FROM_ASHES":
			return "Default_DLC_HEN_PR.bin";
		case "APPEARANCE":
			return "Default_DLC_CON_APP01.bin";
		case "FIREFIGHT":
			return "Default_DLC_CON_GUN01.bin";
		case "GROUNDSIDE":
			return "Default_DLC_CON_GUN02.bin";
		case "EXTENDED_CUT":
			return "Default_DLC_CON_END.bin";
		case "LEVIATHAN":
			return "Default_DLC_CON_Pack001.bin";
		case "OMEGA":
			return "Default_DLC_CON_Pack002.bin";
		case "CITADEL":
			return "Default_DLC_CON_Pack003.bin";
		case "CITADEL_BASE":
			return "Default_DLC_CON_Pack003_Base.bin";
		default:
			ModManager.debugLogger.writeMessage("ERROR: UNKNOWN COAL FROM SHORTNAME: "+shortName);
			return null;
		}
	}
	

	/**
	 * Converts a coalesced filename to a header you can place in moddesc.ini files.
	 * @param coalName Coalesced file
	 * @return Corresponding heading
	 */
	protected String coalFileNameToModDescName(String coalName) {
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
		case "Default_DLC_TestPatch.bin":
			return "TESTPATCH";
		case "Default_DLC_HEN_PR.bin":
			return "FROM_ASHES";
		case "Default_DLC_CON_APP01.bin":
			return "APPEARANCE";
		case "Default_DLC_CON_GUN01.bin":
			return "FIREFIGHT";
		case "Default_DLC_CON_GUN02.bin":
			return "GROUNDSIDE";
		case "Default_DLC_CON_END.bin":
			return "EXTENDED_CUT";
		case "Default_DLC_CON_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_CON_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_CON_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "CITADEL_BASE";
		default:
			ModManager.debugLogger.writeMessage("ERROR: Unable to convert "+coalName+" to it's filename.");
			return null;
		}
	}

	/**
	 * Converts a coal filename (Default_DLC_CON_MP3.bin) into the short name for the DLC folder for mod packages
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
		case "Default_DLC_TestPatch.bin":
			return "TESTPATCH";
		case "Default_DLC_HEN_PR.bin":
			return "FROM_ASHES";
		case "Default_DLC_CON_APP01.bin":
			return "APPEARANCE";
		case "Default_DLC_CON_GUN01.bin":
			return "FIREFIGHT";
		case "Default_DLC_CON_GUN02.bin":
			return "GROUNDSIDE";
		case "Default_DLC_CON_END.bin":
			return "EXTENDED_CUT";
		case "Default_DLC_CON_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_CON_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_CON_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "CITADEL_BASE";
		default:
			ModManager.debugLogger.writeMessage("UNRECOGNIZED COAL FILE: "+coalName);
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
		case "Default_DLC_TestPatch.bin":
			return "/BIOGame/DLC/DLC_TestPatch/CookedPCConsole/Default_DLC_TestPatch.bin";
		case "Default_DLC_HEN_PR.bin":
			return "/BIOGame/DLC/DLC_HEN_PR/CookedPCConsole/Default_DLC_HEN_PR.bin";
		case "Default_DLC_CON_APP01.bin":
			return "/BIOGame/DLC/DLC_CON_APP01/CookedPCConsole/Default_DLC_CON_APP01.bin";
		case "Default_DLC_CON_GUN01.bin":
			return "/BIOGame/DLC/DLC_CON_GUN01/CookedPCConsole/Default_DLC_CON_GUN01.bin";
		case "Default_DLC_CON_GUN02.bin":
			return "/BIOGame/DLC/DLC_CON_GUN02/CookedPCConsole/Default_DLC_CON_GUN02.bin";
		case "Default_DLC_CON_END.bin":
			return "/BIOGame/DLC/DLC_CON_END/CookedPCConsole/Default_DLC_CON_END.bin";
		case "Default_DLC_CON_Pack001.bin":
			return "/BIOGame/DLC/DLC_CON_Pack001/CookedPCConsole/Default_DLC_CON_Pack001.bin";
		case "Default_DLC_CON_Pack002.bin":
			return "/BIOGame/DLC/DLC_CON_Pack002/CookedPCConsole/Default_DLC_CON_Pack002.bin";
		case "Default_DLC_CON_Pack003.bin":
			return "/BIOGame/DLC/DLC_CON_Pack003/CookedPCConsole/Default_DLC_CON_Pack003.bin";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "/BIOGame/DLC/DLC_CON_Pack003_Base/CookedPCConsole/Default_DLC_CON_Pack003_Base.bin";
		default:
			ModManager.debugLogger.writeMessage("ERROR: UNRECOGNIZED COAL FILE: "+coalName);
			return null;
		}
	}
	
	/**
	 * Converts the server TLKData tag name directory in the cookedpcconsole directory.
	 * @param coalName name of coal being packed into the mod
	 * @return path to the file to repalce
	 */
	protected String tlkShortNameToFileName(String shortTLK) {
		switch (shortTLK) {
		case "INT":
			return "BIOGame_INT.tlk";
		case "ESN":
			return "BIOGame_ESN.tlk";
		case "DEU":
			return "BIOGame_DEU.tlk";
		case "ITA":
			return "BIOGame_ITA.tlk";
		case "FRA":
			return "BIOGame_FRA.tlk";
		case "RUS":
			return "BIOGame_RUS.tlk";
		case "POL":
			return "BIOGame_POL.tlk";
		default:
			ModManager.debugLogger.writeMessage("UNRECOGNIZED TLK FILE: "+shortTLK);
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
			return "\\BIOGame\\PCConsoleTOC.bin";
			
			
		case "Default_DLC_TestPatch.bin":
			return "/BIOGame/DLC/DLC_TestPatch/PCConsoleTOC.bin";
		case "Default_DLC_HEN_PR.bin":
			return "/BIOGame/DLC/DLC_HEN_PR/PCConsoleTOC.bin";
		case "Default_DLC_CON_APP01.bin":
			return "/BIOGame/DLC/DLC_CON_APP01/PCConsoleTOC.bin";
		case "Default_DLC_CON_GUN01.bin":
			return "/BIOGame/DLC/DLC_CON_GUN01/PCConsoleTOC.bin";
		case "Default_DLC_CON_GUN02.bin":
			return "/BIOGame/DLC/DLC_CON_GUN02/PCConsoleTOC.bin";
		case "Default_DLC_CON_END.bin":
			return "/BIOGame/DLC/DLC_CON_END/PCConsoleTOC.bin";
		case "Default_DLC_CON_Pack001.bin":
			return "/BIOGame/DLC/DLC_CON_Pack001/PCConsoleTOC.bin";
		case "Default_DLC_CON_Pack002.bin":
			return "/BIOGame/DLC/DLC_CON_Pack002/PCConsoleTOC.bin";
		case "Default_DLC_CON_Pack003.bin":
			return "/BIOGame/DLC/DLC_CON_Pack003/PCConsoleTOC.bin";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "/BIOGame/DLC/DLC_CON_Pack003_Base/PCConsoleTOC.bin";	
		
		default:
			ModManager.debugLogger.writeMessage("[coalFileNameToDLCTOCDIR] UNRECOGNIZED COAL FILE: "+coalName);
			return null;
		}
	}

	protected void parseModInfo() {
		ModManager.debugLogger.writeMessage("============Parsing modinfo==============");
		NodeList infoNodeList = doc.getElementsByTagName("ModInfo");
		infoElement = (Element) infoNodeList.item(0); //it'll be the only element. Hopefully!
		NodeList nameElement = infoElement.getElementsByTagName("Name");
		modName = nameElement.item(0).getTextContent();
		ModManager.debugLogger.writeMessage("Mod Name: "+modName);
		
		NodeList descElement = infoElement.getElementsByTagName("Description");
		modDescription = descElement.item(0).getTextContent();
		ModManager.debugLogger.writeMessage("Mod Description: "+modDescription);
		
		NodeList devElement = infoElement.getElementsByTagName("Author");
		modDev = devElement.item(0).getTextContent();
		ModManager.debugLogger.writeMessage("Mod Dev: "+modDev);
		
		NodeList versionElement = infoElement.getElementsByTagName("Version");
		if (versionElement.getLength() > 0) {
			modVer = devElement.item(0).getTextContent();
			ModManager.debugLogger.writeMessage("Mod Version: "+modVer);
		}
		
		NodeList idElement = infoElement.getElementsByTagName("id");
		modId = idElement.item(0).getTextContent();
		ModManager.debugLogger.writeMessage("ModMaker ID: "+modId);
		
		NodeList modmakerVersionElement = infoElement.getElementsByTagName("ModMakerVersion");
		String modModMakerVersion = modmakerVersionElement.item(0).getTextContent();
		ModManager.debugLogger.writeMessage("Mod information file was built using modmaker "+modModMakerVersion);
		
		modMakerVersion = Double.parseDouble(modModMakerVersion);
		if (modMakerVersion > ModManager.MODMAKER_VERSION_SUPPORT) {
			//ERROR! We can't compile this version.
			ModManager.debugLogger.writeMessage("FATAL ERROR: This version of mod manager does not support this version of modmaker.");
			ModManager.debugLogger.writeMessage("FATAL ERROR: This version supports up to ModMaker version: "+ModManager.MODMAKER_VERSION_SUPPORT);
			ModManager.debugLogger.writeMessage("FATAL ERROR: This mod was built with ModMaker version: "+modModMakerVersion);
			JOptionPane.showMessageDialog(null,
				    "<html>This mod was built with a newer version of ModMaker than this version of Mod Manager can support.<br>You need to download the latest copy of Mod Manager to compile this mod.</html>",
				    "Compiling Error",
				    JOptionPane.ERROR_MESSAGE);
			error = true;
			dispose();
			return;
		}
		
		//Check the name
		File moddir = new File(modName);
		if (moddir.isDirectory()) {
			try {
				ModManager.debugLogger.writeMessage("Removing existing mod directory, will recreate when complete");
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
				ModManager.debugLogger.writeMessage("ini file descriptor found in mod: "+intCoalName);	
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
				ModManager.debugLogger.writeMessage("Coal doesn't exist, need to download: "+coal);
				numToDownload++;
			} else {
				ModManager.debugLogger.writeMessage("Coal already exists, skipping download: "+coal);
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
			ModManager.debugLogger.writeMessage("==================DecompilerWorker==============");

			this.coalsToDecompile = coalsToDecompile;
			this.numCoals = coalsToDecompile.size();
			this.progress = progress;
			currentOperationLabel.setText("Decompiling "
					+ this.coalsToDecompile.get(0));
			progress.setValue(0);
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
				ModManager.debugLogger.writeMessage("Executing decompile command: "+
						compilerPath+" "+ path + "\\coalesceds\\ "
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
			progress.setIndeterminate(false);
			progress.setValue((int) (100 / (numCoals / (float)numCompleted.get(0))));
		}

		protected void done() {
			// Coals decompiled
			stepsCompleted++;
			overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted))  + 0.5));
			ModManager.debugLogger.writeMessage("COALS: DECOMPILED...");
			new MergeWorker(progress).execute();
		}
	}

	class CompilerWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> coalsToCompile;
		private JProgressBar progress;
		private int numCoals;

		public CompilerWorker(ArrayList<String> coalsToCompile,
				JProgressBar progress) {
			ModManager.debugLogger.writeMessage("==================CompilerWorker==============");
			this.coalsToCompile = coalsToCompile;
			this.numCoals = coalsToCompile.size();
			this.progress = progress;
			currentOperationLabel.setText("Recompiling "
					+ this.coalsToCompile.get(0));
			progress.setIndeterminate(false);
			progress.setValue(0);
		}

		protected Void doInBackground() throws Exception {
			int coalsCompiled = 0;
			String path = Paths.get(".").toAbsolutePath().normalize()
					.toString();
			for (String coal : coalsToCompile) {
				String compilerPath = path
						+ "\\Tankmaster Compiler\\MassEffect3.Coalesce.exe";
				//ProcessBuilder compileProcessBuilder = new ProcessBuilder(
				//		compilerPath, "--xml2bin", path + "\\coalesceds\\"
				//				+ FilenameUtils.removeExtension(coal)+".xml");
				ProcessBuilder compileProcessBuilder = new ProcessBuilder(
						compilerPath, path + "\\coalesceds\\"
								+ FilenameUtils.removeExtension(coal)+"\\"+FilenameUtils.removeExtension(coal)+".xml", "--mode=ToBin");
				//log it
				ModManager.debugLogger.writeMessage("Executing compile command: "+
						compilerPath+" "+path + "\\coalesceds\\"
								+ FilenameUtils.removeExtension(coal)+"\\"+FilenameUtils.removeExtension(coal)+".xml --mode=ToBin");
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
			progress.setIndeterminate(false);
			progress.setValue((int) (100 / ((double)numCoals / numCompleted.get(0)) + 0.5)); //crazy rounding trick for integer.
		}

		protected void done() {
			// Coals downloaded
			stepsCompleted+=2;
			overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted))));
			ModManager.debugLogger.writeMessage("COALS: RECOMPILED...");
			new TLKWorker(progress, languages).execute();
		}
	}

	class CoalDownloadWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> coalsToDownload;
		private JProgressBar progress;
		private int numCoals;

		public CoalDownloadWorker(ArrayList<String> coalsToDownload,
				JProgressBar progress) {
			progress.setIndeterminate(true);
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
					ModManager.debugLogger.writeMessage("Downloading Coalesced: "+link);
					FileUtils.copyURLToFile(new URL(link), new File(
							"coalesceds/" + coal));
					ModManager.debugLogger.writeMessage("Saved coalesced to: "+(new File(
							"coalesceds/" + coal)).getAbsolutePath());
					coalsCompeted++;
					this.publish(coalsCompeted);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					ModManager.debugLogger.writeMessage("Failed to download coalesced file due to malformed URL.");
					ModManager.debugLogger.writeException(e);
				} catch (IOException e) {
					ModManager.debugLogger.writeMessage("Failed to download coalesced file due to IO Exception.");
					ModManager.debugLogger.writeException(e);
				}
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			progress.setIndeterminate(false);
			if (numCoals > numCompleted.get(0)) {
				currentOperationLabel.setText("Downloading "
						+ coalsToDownload.get(numCompleted.get(0)));
			}
			progress.setValue((int) (100 / (numCoals / (float)numCompleted.get(0))));
			overallProgress.setValue((int) (100 / ((double)TOTAL_STEPS / (stepsCompleted+
					((numCoals/(double)numCompleted.get(0))/100)
					))));
		}

		protected void done() {
			// Coals downloaded
			ModManager.debugLogger.writeMessage("Required coalesceds downloaded");
			stepsCompleted++;
			overallProgress.setValue((int) (100 / (TOTAL_STEPS / (float)stepsCompleted)));
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
			ModManager.debugLogger.writeMessage("=============MERGEWORKER=============");
			this.progress = progress;
			currentOperationLabel.setText("Merging Coalesced files...");
			progress.setIndeterminate(true);
			progress.setValue(0);
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
					ModManager.debugLogger.writeMessage("---------------------MODMAKER COMPILER START OF "+intCoalName+"-------------------------");

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
							//ModManager.printDocument(iniFile, System.out);
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
									boolean isSection = false;
									Element property = (Element) newproperty;
									String newPropName = null;
									String arrayType = null;
									String operation = null;
									String matchontype = null;
									String UE3type = null;
									String nodeName = property.getNodeName();
									switch (nodeName) {
									case "Property":
										newPropName = property.getAttribute("name");
										operation = property.getAttribute("operation");
										UE3type = property.getAttribute("type");
										System.out.println(newPropName+" is a property");

										isArrayProperty = false;
										break;
									case "ArrayProperty":
										arrayType = property.getAttribute("arraytype");
										matchontype = property.getAttribute("matchontype");
										operation = property.getAttribute("operation");
										UE3type = property.getAttribute("type");
										System.out.println("Array property");

										isArrayProperty = true;
										break;
									case "Section":
										
										newPropName = property.getAttribute("name");
										System.out.println(newPropName+" is a section");
										operation = property.getAttribute("operation");
										isArrayProperty = false;
										isSection = true;
										break;
									}
									
									if (isSection) {
										//can't drill to it.
										switch (operation) {
										case "addition":
											//adds a section
											ModManager.debugLogger.writeMessage("Creating new section: "+newPropName);
											Element newElement;
											newElement = iniFile.createElement("Section");
											newElement.setAttribute("name", newPropName);
											sections.appendChild(newElement);
											break;
										case "subtraction":
											//remove this section
											ModManager.debugLogger.writeMessage("Subtracting section: "+newPropName);
											boolean sectionFound = false;
											for (int l = 0; l < SectionList.getLength(); l++) {
												//iterate over all sections...
												Node n = SectionList.item(l); //L, not a 1.
												if (n.getNodeType() == Node.ELEMENT_NODE) {
													Element sectionElem = (Element) n;
													if (sectionElem.getAttribute("name").equals(newPropName)) {
														//this is the one to remove.
														sections.removeChild(n);
														sectionFound = true;
														break;
													}
												}
											}
											if (sectionFound) {
												continue;
											} else {
												System.err.println("SHOULDNT REACH THIS! SUBTRACT SECTION");
												continue;
											}
										case "clear":
											//gets rid of all children, leaving the node
											ModManager.debugLogger.writeMessage("Clearing section: "+newPropName);
											boolean cleared = false;
											for (int l = 0; l < SectionList.getLength(); l++) {
												//iterate over all sections...
												Node n = SectionList.item(l); //L, not a 1.
												if (n.getNodeType() == Node.ELEMENT_NODE) {
													Element sectionElem = (Element) n;
													if (sectionElem.getAttribute("name").equals(newPropName)) {
														//this is the one to remove.
														while (n.hasChildNodes()) {
													        n.removeChild(n.getFirstChild());
														}
														cleared = true;
														break;
													}
												}
											}
											if (cleared) {
												continue;
											} else {
												System.err.println("SHOULDNT REACH THIS! CLEAR SECTION");
												continue;
											}
										}
										continue;
									}
									
									String newValue = property.getTextContent();
									
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
												//ModManager.debugLogger.writeMessage("Checking attribute: "+drilled.getAttribute("name"));
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
												    "<html>Could not find the path "+path+" to property.<br>Module: "+intCoalName+"<br>File: "+iniFileName+"</html>",
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
											    "<html>Could not find the path "+path+" to property.<br>Module: "+intCoalName+"<br>File: "+iniFileName+"</html>",
											    "Compiling Error",
											    JOptionPane.ERROR_MESSAGE);
										return null;
									}
									if (operation.equals("addition")) {
										//only for arrays
										//we won't find anything to match, since it obviously can't exist. Add it from here.
										ModManager.debugLogger.writeMessage("Creating new property with operation ADDITION");
										Element newElement;
										if (isArrayProperty) {
											newElement = drilled.getOwnerDocument().createElement("Value");
										} else {
											newElement = drilled.getOwnerDocument().createElement("Property");
											newElement.setAttribute("name", newPropName);
										}
										if (UE3type != null && !UE3type.equals("")) {
											newElement.setAttribute("type", UE3type);
										}
										newElement.setTextContent(newValue);
										drilled.appendChild(newElement);
										continue; //continue property loop
									}
									
									
									//we've drilled down as far as we can.
									
									//we are where we want to be. Now we can set the property or array value.
									//drilled is the element (parent of our property) that we want.
									NodeList props = drilled.getChildNodes(); //get children of the path (<property> list)
									ModManager.debugLogger.writeMessage("Number of child property/elements to search: "+props.getLength());
									boolean foundProperty = false;
									for (int m = 0; m < props.getLength(); m++){
										Node propertyNode = props.item(m);
										if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
											Element itemToModify = (Element) propertyNode;
											//Check on property
											if (!isArrayProperty) {
												//property
												boolean shouldBreak = false;
												switch (operation) {
												case "assignment":
													if (itemToModify.getAttribute("name").equals(newPropName)) {
														itemToModify.setTextContent(newValue);
														ModManager.debugLogger.writeMessage("Assigning "+newPropName+" to "+newValue);
														foundProperty = true;
														shouldBreak = true;
													}
													break;
												case "subtraction":
													if (itemToModify.getAttribute("name").equals(newPropName)) {
														ModManager.debugLogger.writeMessage("Subtracting property "+newPropName);
														Node itemParent = itemToModify.getParentNode();
														itemParent.removeChild(itemToModify);
														foundProperty = true;
														shouldBreak = true;
														break;
													}
												}
												if (shouldBreak) {
													break;
												}
											} else {
												//Check on ArrayProperty
												//ModManager.debugLogger.writeMessage("Candidates only will be returned if they are of type: "+matchontype);
												//ModManager.debugLogger.writeMessage("Scanning property type: "+itemToModify.getAttribute("type"));
												if (itemToModify.getAttribute("type").equals(matchontype)) {
													//potential array value candidate...
													boolean match = false;
													ModManager.debugLogger.writeMessage("Found type candidate ("+matchontype+") for arrayreplace: "+itemToModify.getTextContent());
													switch (arrayType) {
													//Must use individual matching algorithms so we can figure out if something matches.
													case "exactvalue": {
														if (itemToModify.getTextContent().equals(newValue)) {
															ModManager.debugLogger.writeMessage("Property match found.");
															match = true;
														}
													}
													break;
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
													case "wavelist": {
														//Match on Difficulty
														Wave existing = new Wave(itemToModify.getTextContent());
														Wave importing = new Wave(newValue);
														if (existing.matchIdentifiers(importing)) {
															match = true;
															ModManager.debugLogger.writeMessage("Wavelist match on "+existing.difficulty);
															newValue = importing.createWaveString(); //doens't really matter, but makes me feel good my code works
														} else {
															//CHECK FOR COLLECTOR PLAT WAVE 5.
															String cplatwave5 = "(Difficulty=DO_Level3,Enemies=( (EnemyType=\"WAVE_COL_Scion\"), (EnemyType=\"WAVE_COL_Praetorian\", MinCount=1, MaxCount=1), (EnemyType=\"WAVE_CER_Phoenix\", MinCount=2, MaxCount=2), (EnemyType=\"WAVE_CER_Phantom\", MinCount=3, MaxCount=3) ))";
															if (path.equals("sfxwave_horde_collector5 sfxwave_horde_collector&enemies") && importing.difficulty.equals("DO_Level3")) {
																System.out.println("BREAK");
																
															}
															//System.out.println(itemToModify.getTextContent());
															if (itemToModify.getTextContent().equals(cplatwave5) && path.equals("sfxwave_horde_collector5 sfxwave_horde_collector&enemies") && importing.difficulty.equals("DO_Level4")) {
																match = true;
																newValue = importing.createWaveString(); //doens't really matter, but makes me feel good my code works
															}
														}
													}
													break;
													case "possessionwaves": {
														//Match on Difficulty/DoLevel
														//Match on Difficulty
														Difficulty existing = new Difficulty(itemToModify.getTextContent());
														Difficulty importing = new Difficulty(newValue);
														if (existing.matchIdentifiers(importing)) {
															match = true;
															//newValue = importing.createDifficultyString(); //doens't really matter, but makes me feel good my code works
															//and it was broken
														}
													}
													break;
													case "wavecost": {
														//Match on SharedDifficulty (DO_Level)
														SharedDifficulty existing = new SharedDifficulty(itemToModify.getTextContent());
														SharedDifficulty importing = new SharedDifficulty(newValue);
														if (existing.matchIdentifiers(importing)) {
															match = true;
														}
													}
													break;
													case "wavebudget": {
														//Match on SharedDifficulty (DO_Level)
														SharedDifficulty existing = new SharedDifficulty(itemToModify.getTextContent());
														SharedDifficulty importing = new SharedDifficulty(newValue);
														if (existing.matchIdentifiers(importing)) {
															match = true;
														}
													}
													break;
													default:
														ModManager.debugLogger.writeMessage("ERROR: Unknown matching algorithm: "+arrayType+" does this client need updated? Aborting this stat update.");
														JOptionPane.showMessageDialog(null,
															    "<html>Unknown matching algorithm from ME3Tweaks: "+arrayType+".<br>You should check for updates to Mod Manager.<br>This mod will not fully compile.</html>",
															    "Compiling Error",
															    JOptionPane.ERROR_MESSAGE);
														break;
													} //end matching algorithm switch
													if (match) {
														foundProperty = true;
														switch (operation) {
														case "subtraction":
															Node itemParent = itemToModify.getParentNode();
															itemParent.removeChild(itemToModify);
															ModManager.debugLogger.writeMessage("Removed array value: "+newValue);
															break;
														case "modify": //same as assignment right now
														case "assignment":
															itemToModify.setTextContent(newValue);
															ModManager.debugLogger.writeMessage("Assigned array value: "+newValue);
															break;
														default:
															ModManager.debugLogger.writeMessage("ERROR: Unknown matching algorithm: "+arrayType+" does this client need updated? Aborting this stat update.");
															JOptionPane.showMessageDialog(null,
																    "<html>Unknown operation from ME3Tweaks: "+operation+".<br>You should check for updates to Mod Manager.<br>This mod will not fully compile.</html>",
																    "Compiling Error",
																    JOptionPane.ERROR_MESSAGE);
															break;
														} //end operation [switch]
														break;
													} //end of match = true [if]
												} //end of array matchontype check [if]
											} //end of array property [if]
										} //end of property = element node (not junk) [if]
									} //end of props.length to search through. [for loop]
									if (foundProperty != true) {
										if (modMakerVersion > 1.0) {
											StringBuilder sb = new StringBuilder();
											sb.append("<html>Could not find the following attribute:<br>");
											sb.append("Coalesced File: ");
											sb.append(intCoalName);
											sb.append("<br>");
											sb.append("Subfile: ");
											sb.append(fileNode.getNodeName());
											sb.append("<br>");
											
											sb.append("Path: ");
											sb.append(path);
											sb.append("<br>");
											sb.append("Operation: ");
											sb.append(operation);
											sb.append("<br>");
											if (isArrayProperty) {
												sb.append("====ARRAY ATTRIBUTE INFO=======<br>");
												sb.append("Array matching algorithm: ");
												sb.append(arrayType);
												sb.append("<br>Matching type: ");
												sb.append(matchontype);
											} else {
												sb.append("====STANDARD ATTRIBUTE INFO====<br>");
												sb.append("Keyed Property Name: ");
												sb.append(newPropName);
											}
											sb.append("<br>=================");
											sb.append("<br>");
											sb.append("New data: ");
											sb.append(newValue);
											sb.append("</html>");
											
											JOptionPane.showMessageDialog(null,
												    sb.toString(),
												    "Compiling Error",
												    JOptionPane.ERROR_MESSAGE);
											ModManager.debugLogger.writeMessage(sb.toString());
										}
									}
								}
							}
							//end of the file node.
							//Time to save the file...
							Transformer transformer = TransformerFactory.newInstance().newTransformer();
							transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
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

		protected void done() {
			// Coals downloaded
			if (error) {
				return;
			}
			ModManager.debugLogger.writeMessage("Finished merging coals.");

			stepsCompleted++;
			overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted))  + 0.5));
			new CompilerWorker(requiredCoals, progress).execute();
		}
	}

	class TLKWorker extends SwingWorker<Void, Integer> {
		private JProgressBar progress;
		boolean error = false;
		ArrayList<String> languages;
		int jobsToDo = 0, jobsDone = 0;

		public TLKWorker(JProgressBar progress, ArrayList<String> languages) {
			ModManager.debugLogger.writeMessage("Beginning TLK Decompile operation.");
			this.progress = progress;
			this.languages = languages;
			currentOperationLabel.setText("Creating language specific changes...");
			progress.setIndeterminate(true);
			progress.setValue(0);
		}

		protected Void doInBackground() throws Exception {
			/*if (true) {
				ModManager.debugLogger.writeMessage("Debug skipping TLK");
				return null; //skip tlk, mod doesn't have it
			}*/
			NodeList tlkElementNodeList = doc.getElementsByTagName("TLKData");
			if (tlkElementNodeList.getLength() < 1) {
				ModManager.debugLogger.writeMessage("No TLK in mod file, or length is 0.");
				return null; //skip tlk, mod doesn't have it
			}
			tlkElement = (Element) tlkElementNodeList.item(0);
						
			NodeList tlkNodeList = tlkElement.getChildNodes();
			//Iterate over the tlk entries.
			//get number of jobs.

			for (int i = 0; i < tlkNodeList.getLength(); i++) {
				//coalNode is a node containing the coalesced module, such as <MP1> or <BASEGAME>
				Node tlkNode = tlkNodeList.item(i);
				if (tlkNode.getNodeType() == Node.ELEMENT_NODE) {
					if (languages.contains(tlkNode.getNodeName())) {
						jobsToDo+=3; //decomp, edit, comp
					}
				}
			}
			
			for (int i = 0; i < tlkNodeList.getLength(); i++) {
				Node tlkNode = tlkNodeList.item(i);
				if (tlkNode.getNodeType() == Node.ELEMENT_NODE) {
					String tlkType = tlkNode.getNodeName(); //get the tlk name so we can figure out what tlk to modify
					if (languages.contains(tlkNode.getNodeName())) {
						ModManager.debugLogger.writeMessage("Read TLK ID: "+tlkType);
						ModManager.debugLogger.writeMessage("---------------------START OF "+tlkType+"-------------------------");
							
						//decompile TLK to tlk folder
						File tlkdir = new File("tlk");
						tlkdir.mkdirs(); // created tlk directory
						
						//START OF TLK DECOMPILE=========================================================
						ArrayList<String> commandBuilder = new ArrayList<String>();
						/*
						 ME3 EXPLORER VERSION
					    commandBuilder.add("");
						commandBuilder.add("-tlkeditor");
						commandBuilder.add("decompile");
						commandBuilder.add(ModManager.appendSlash(callingWindow.fieldBiogameDir.getText())+"CookedPCConsole\\"+tlkShortNameToFileName(tlkType));
						commandBuilder.add(ModManager.appendSlash(tlkdir.getAbsolutePath().toString())+"BIOGame_"+tlkType+".xml");
						*/
						String path = Paths.get(".").toAbsolutePath().normalize()
								.toString();
						String compilerPath = path
								+ "\\Tankmaster TLK\\MassEffect3.TlkEditor.exe";
						commandBuilder.add(compilerPath);
						commandBuilder.add(ModManager.appendSlash(callingWindow.fieldBiogameDir.getText())+"CookedPCConsole\\"+tlkShortNameToFileName(tlkType));
						commandBuilder.add(ModManager.appendSlash(tlkdir.getAbsolutePath().toString())+"BIOGame_"+tlkType+".xml");
						commandBuilder.add("--mode");
						commandBuilder.add("ToXml");
						commandBuilder.add("--no-ui");
						
						//System.out.println("Building command");
						String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
						//Debug stuff
						StringBuilder sb = new StringBuilder();
						for (String arg : command){
							sb.append(arg+" ");
						}
						ModManager.debugLogger.writeMessage("Executing TLK Decompile command: "+sb.toString());
						Process p = null;
						int returncode = 1;
						try {
							ProcessBuilder pb = new ProcessBuilder(command);
							pb.redirectErrorStream(true);
							ModManager.debugLogger.writeMessage("Executing process for TLK Decompile Job.");
							p = pb.start();
							returncode = p.waitFor();
							ModManager.debugLogger.writeMessage("TLK Job return code: "+returncode);
						} catch (IOException | InterruptedException e) {
							ModManager.debugLogger.writeException(e);
						}
						this.publish(++jobsDone);
						//END OF DECOMPILE==================================================
						//load the decompiled XML file into memory
						ModManager.debugLogger.writeMessage("Loading TLK XML (file 0) "+tlkType+" into memory.");
						String tlkFile0 = ModManager.appendSlash(tlkdir.getAbsolutePath().toString())+"BIOGame_"+tlkType+"\\"+"BIOGame_"+tlkType+"0.xml"; //tankmaster's compiler splits it into files.
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document tlkXMLFile = dBuilder.parse(tlkFile0);
						tlkXMLFile.getDocumentElement().normalize();
						ModManager.debugLogger.writeMessage("Loaded TLK "+tlkXMLFile.getDocumentURI()+" into memory.");
						
						//id only test
						NodeList stringsInTLK = tlkXMLFile.getElementsByTagName("String");
						
						NodeList localizedNodeList = tlkNode.getChildNodes();
						//Iterate over the tlk entries.
						for (int j = 0; j < localizedNodeList.getLength(); j++) {
							//coalNode is a node containing the coalesced module, such as <MP1> or <BASEGAME>
							Node stringNode = localizedNodeList.item(j);
							if (stringNode.getNodeType() == Node.ELEMENT_NODE) {
								Element stringElement = (Element) stringNode;
								
								String id = stringElement.getAttribute("id");
								String content = stringElement.getTextContent();
								ModManager.debugLogger.writeMessage("Scanning for string id "+id+"...");
								
								//scan XML for it...
								for (int s = 0; s < stringsInTLK.getLength(); s++){
									//get node
									Node tlkStringNode = stringsInTLK.item(s);
									if (tlkStringNode.getNodeType() == Node.ELEMENT_NODE) {
										Element stringTLKElement = (Element) tlkStringNode;
										//System.out.println("Checking "+id+" vs "+stringTLKElement.getAttribute("id"));
										if (stringTLKElement.getAttribute("id").equals(id)) {
											ModManager.debugLogger.writeMessage("Updating string id "+id+" to "+content);
											stringTLKElement.setTextContent(content);
										}
									}
								}
							}
						}
						//end of the file node.
						//Time to save the file...
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						File outputFile = new File(tlkFile0);
						Result output = new StreamResult(outputFile);
						Source input = new DOMSource(tlkXMLFile);
						ModManager.debugLogger.writeMessage("Saving file: "+outputFile.toString());
						transformer.transform(input, output);
	
						this.publish(++jobsDone);
						//create new TLK file from this.
						//START OF TLK COMPILE=========================================================
						ArrayList<String> tlkCompileCommandBuilder = new ArrayList<String>();
						tlkCompileCommandBuilder.add(compilerPath);
						tlkCompileCommandBuilder.add(ModManager.appendSlash(tlkdir.getAbsolutePath().toString())+"BIOGame_"+tlkType+".xml");
						tlkCompileCommandBuilder.add(ModManager.appendSlash(tlkdir.getAbsolutePath().toString())+"BIOGame_"+tlkType+".tlk");
						tlkCompileCommandBuilder.add("--mode");
						tlkCompileCommandBuilder.add("ToTlk");
						tlkCompileCommandBuilder.add("--no-ui");
						
						//System.out.println("Building command");
						String[] tlkCompilecommand = tlkCompileCommandBuilder.toArray(new String[commandBuilder.size()]);
						//Debug stuff
						StringBuilder tlkCompileCommandsb = new StringBuilder();
						for (String arg : tlkCompilecommand){
							tlkCompileCommandsb.append(arg+" ");
						}
						ModManager.debugLogger.writeMessage("Executing TLK Compile command: "+tlkCompileCommandsb.toString());
						p = null;
						try {
							ProcessBuilder pb = new ProcessBuilder(tlkCompilecommand);
							pb.redirectErrorStream(true);
							ModManager.debugLogger.writeMessage("Executing process for TLK Compile Job.");
							//p = Runtime.getRuntime().exec(command);
							p = pb.start();
							returncode = p.waitFor();
						} catch (IOException | InterruptedException e) {
							ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e));
							e.printStackTrace();
						}
						//END OF COMPILE==================================================
						this.publish(++jobsDone);
					} else {
						ModManager.debugLogger.writeMessage("Language not chosen for TLK compiling: skipping "+tlkType);
					}
				}
			}
			return null;
		}
		
		@Override
		protected void process(List<Integer> numCompleted) {
			progress.setValue((int)(100 / (jobsToDo / (float)numCompleted.get(0))));
		}

		protected void done() {
			// tlks decompiled.
			if (error) {
				return;
			}
			ModManager.debugLogger.writeMessage("Finished decompiling TLK files.");

			stepsCompleted++;
			overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted))  + 0.5));
			new TOCDownloadWorker(requiredCoals, progress).execute();

		}
	}

	/**
	 * Creates a CMM Mod package from the completed previous steps.
	 */
	private void createCMMMod() {
		ModManager.debugLogger.writeMessage("----Creating CMM Mod Package and descriptor-----");
		boolean error = false;
		currentOperationLabel.setText("Creating mod directory and descriptor...");
		File moddir = new File(modName);
		ModManager.debugLogger.writeMessage("Mod package directory set to: "+moddir.getAbsolutePath());
		moddir.mkdirs(); // created mod directory

		// Write mod descriptor file
		Wini ini;
		File moddesc = new File(moddir + "\\moddesc.ini");
		try {
			ModManager.debugLogger.writeMessage("Checking for moddesc.ini: "+moddesc.getAbsolutePath());
			if (!moddesc.exists()) {
				ModManager.debugLogger.writeMessage("moddesc.ini does not exist, creating new one for this mod.");
				moddesc.createNewFile();
			}
			ModManager.debugLogger.writeMessage("Creating in-memory moddesc.ini");
			ini = new Wini(moddesc);
			ini.put("ModManager", "cmmver", 3.0);
			ini.put("ModInfo", "modname", modName);
			ini.put("ModInfo", "moddev", modDev);
			ini.put("ModInfo", "moddesc", modDescription+"<br>Created with ME3Tweaks ModMaker.");
			ini.put("ModInfo", "modsite", "https://me3tweaks.com/modmaker");
			ini.put("ModInfo", "modid", modId);
			if (modVer != null) {
				ini.put("ModInfo", "modver", modVer);
			}
			// Create directories, move files to them
			for (String reqcoal : requiredCoals) {
				File compCoalDir = new File(moddir.toString() + "\\"+coalFilenameToShortName(reqcoal)); //MP4, PATCH2 folders in mod package
				compCoalDir.mkdirs();
				String fileNameWithOutExt = FilenameUtils.removeExtension(reqcoal);
				//copy coal
				File coalFile = new File("coalesceds\\"+fileNameWithOutExt+"\\"+reqcoal);
				File destCoal = new File(compCoalDir+"\\"+reqcoal);
				destCoal.delete();
				if (coalFile.renameTo(destCoal)){
					ModManager.debugLogger.writeMessage("Moved "+reqcoal+" to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" to the proper mod element directory. Could already exist.");
				}
				//copy pcconsoletoc
				File tocFile = new File("toc\\"+coalFilenameToShortName(reqcoal)+"\\PCConsoleTOC.bin");
				File destToc = new File(compCoalDir+"\\PCConsoleTOC.bin");
				destToc.delete();
				if (tocFile.renameTo(destToc)){
					ModManager.debugLogger.writeMessage("Moved "+reqcoal+" TOC to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" TOC to the proper mod element directory. Could already exist.");
				}
				if (reqcoal.equals("Coalesced.bin")) {
					//it is basegame. copy the tlk files!
					String[] tlkFiles = {"INT", "ESN", "DEU", "ITA", "FRA", "RUS", "POL"};
					for (String tlkFilename : tlkFiles) {
						File compiledTLKFile = new File("tlk\\"+"BIOGame_"+tlkFilename+".tlk");
						if (!compiledTLKFile.exists()) {
							ModManager.debugLogger.writeMessage("TLK file "+compiledTLKFile+" is missing, might not have been selected for compilation. skipping.");
							continue;
						}
						File destTLKFile= new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
						if (compiledTLKFile.renameTo(destTLKFile)){
							ModManager.debugLogger.writeMessage("Moved "+compiledTLKFile+" TLK to BASEGAME directory");
						} else {
							ModManager.debugLogger.writeMessage("Didn't move "+compiledTLKFile+" TLK to the BASEGAME directory. Could already exist.");
						}
					}
				}

				
				boolean basegame = 	reqcoal.equals("Coalesced.bin");
				ini.put(coalFileNameToModDescName(reqcoal), "moddir", coalFilenameToShortName(reqcoal));
				
				if (basegame) {
					StringBuilder newsb = new StringBuilder();
					StringBuilder replacesb = new StringBuilder();
					//coalesced
					newsb.append(reqcoal);
					replacesb.append(coalFileNameToDLCDir(reqcoal));
					
					//tlk, if they exist.
					String[] tlkFiles = {"INT", "ESN", "DEU", "ITA", "FRA", "RUS", "POL"};
					for (String tlkFilename : tlkFiles) {
						File basegameTLKFile = new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
						if (basegameTLKFile.exists()) {
							newsb.append(";BIOGame_"+tlkFilename+".tlk");
							replacesb.append(";\\BIOGame\\CookedPCConsole\\BIOGame_"+tlkFilename+".tlk");
							continue;
						}
					}
					newsb.append(";PCConsoleTOC.bin");
					replacesb.append(";"+coalFileNameToDLCTOCDir(reqcoal));
					ini.put(coalFileNameToModDescName(reqcoal), "newfiles", newsb.toString());
					ini.put(coalFileNameToModDescName(reqcoal), "replacefiles", replacesb.toString());					
				} else {
					ini.put(coalFileNameToModDescName(reqcoal), "newfiles", reqcoal+";PCConsoleTOC.bin");
					ini.put(coalFileNameToModDescName(reqcoal), "replacefiles", coalFileNameToDLCDir(reqcoal)+";"+coalFileNameToDLCTOCDir(reqcoal));					
				}


				File compCoalSourceDir = new File("coalesceds\\"+fileNameWithOutExt);
				try {
					if (!ModManager.IS_DEBUG) {
						FileUtils.deleteDirectory(compCoalSourceDir);
						ModManager.debugLogger.writeMessage("Deleted compiled coal directory: "+compCoalSourceDir);
					}
				} catch (IOException e) {
					ModManager.debugLogger.writeMessage("IOException deleting compCoalSourceDir.");
					ModManager.debugLogger.writeException(e);
				}
			}
			
			//TLK Only, no coalesced
			if (languages.size() > 0 && !requiredCoals.contains("Coalesced.bin")) {
				File compCoalDir = new File(moddir.toString() + "\\"+coalFilenameToShortName("Coalesced.bin")); //MP4, PATCH2 folders in mod package
				compCoalDir.mkdirs();
				ini.put(coalFileNameToModDescName("Coalesced.bin"), "moddir", coalFilenameToShortName("Coalesced.bin"));
				
				//MOVE THE TLK FILES
				for (String tlkFilename : languages) {
					File compiledTLKFile = new File("tlk\\"+"BIOGame_"+tlkFilename+".tlk");
					if (!compiledTLKFile.exists()) {
						ModManager.debugLogger.writeMessage("TLK file "+compiledTLKFile+" is missing, might not have been selected for compilation. skipping.");
						continue;
					}
					File destTLKFile= new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
					if (compiledTLKFile.renameTo(destTLKFile)){
						ModManager.debugLogger.writeMessage("Moved "+compiledTLKFile+" TLK to BASEGAME directory");
					} else {
						ModManager.debugLogger.writeMessage("Didn't move "+compiledTLKFile+" TLK to the BASEGAME directory. Could already exist.");
					}
				}
				
				//MOVE PCCONSOLETOC.bin
				File tocFile = new File("toc\\"+coalFilenameToShortName("Coalesced.bin")+"\\PCConsoleTOC.bin");
				File destToc = new File(compCoalDir+"\\PCConsoleTOC.bin");
				destToc.delete();
				if (tocFile.renameTo(destToc)){
					ModManager.debugLogger.writeMessage("Moved BASEGAME TOC to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move BASEGAME TOC to the proper mod element directory. Could already exist.");
				}
				StringBuilder newsb = new StringBuilder();
				StringBuilder replacesb = new StringBuilder();
				
				//tlk, if they exist.
				for (String tlkFilename : languages) {
					File basegameTLKFile = new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
					if (basegameTLKFile.exists()) {
						newsb.append(";BIOGame_"+tlkFilename+".tlk");
						replacesb.append(";\\BIOGame\\CookedPCConsole\\BIOGame_"+tlkFilename+".tlk");
						continue;
					}
				}
				newsb.append(";PCConsoleTOC.bin");
				replacesb.append(";"+coalFileNameToDLCTOCDir("Coalesced.bin"));
				ini.put(coalFileNameToModDescName("Coalesced.bin"), "newfiles", newsb.toString());
				ini.put(coalFileNameToModDescName("Coalesced.bin"), "replacefiles", replacesb.toString());					
			}
			ModManager.debugLogger.writeMessage("Writing memory ini to disk.");
			ini.store();
			ModManager.debugLogger.writeMessage("Removing temporary directories:");
			try {
				if (!ModManager.IS_DEBUG) {
					FileUtils.deleteDirectory(new File("tlk"));
					ModManager.debugLogger.writeMessage("Deleted tlk");
					FileUtils.deleteDirectory(new File("toc"));
					ModManager.debugLogger.writeMessage("Deleted toc");
					FileUtils.deleteDirectory(new File("coalesceds"));
					ModManager.debugLogger.writeMessage("Deleted coalesceds");
				}
			} catch (IOException e) {
				ModManager.debugLogger.writeMessage("IOException deleting one of the tlk/toc/coalesced directories.");
				ModManager.debugLogger.writeException(e);
			}
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			ModManager.debugLogger.writeMessage("IOException in main chunk of CreateCMMMod()! Setting error flag to true.");
			ModManager.debugLogger.writeException(e);
			error=true;
		}

		//TOC the mod
		ModManager.debugLogger.writeMessage("Loading moddesc for verification...");
		Mod newMod = new Mod(moddesc.toString());
		
		if (!newMod.validMod) {
			//SOMETHING WENT WRONG!
			ModManager.debugLogger.writeMessage("Mod failed validation. Setting error flag to true.");
			error = true;
			JOptionPane.showMessageDialog(this, modName+" was not successfully created.\nCheck the debugging file me3cmm_last_run_log.txt,\nand make sure debugging is enabled in Help>About.\nContact FemShep if you need help via the forums.", "Mod Not Created", JOptionPane.ERROR_MESSAGE);
		}
		File file = new File(DOWNLOADED_XML_FILENAME);
		file.delete();
		ModManager.debugLogger.writeMessage("Deleted downloaded me3tweaks modinfo file");
		if (!error) {
			ModManager.debugLogger.writeMessage("Running AutoTOC on new mod: "+modName);
			new AutoTocWindow(callingWindow, newMod);
			stepsCompleted++;
			ModManager.debugLogger.writeMessage("Mod successfully created:" +modName);
			ModManager.debugLogger.writeMessage("===========END OF MODMAKER========");
			//Mod Created!
			dispose();
			JOptionPane.showMessageDialog(this, modName+" was successfully created!", "Mod Created", JOptionPane.INFORMATION_MESSAGE);
			callingWindow.dispose();
			new ModManagerWindow(false); 
		} else {
			dispose();
		}
	}
	
	class TOCDownloadWorker extends SwingWorker<Void, Integer> {
		private ArrayList<String> tocsToDownload;
		private JProgressBar progress;
		private int numtoc;

		public TOCDownloadWorker(ArrayList<String> tocsToDownload,
				JProgressBar progress) {
			progress.setIndeterminate(true);
			progress.setValue(0);
			this.tocsToDownload = new ArrayList<String>(tocsToDownload); //clone for downloading
			if (languages.size() > 0 && !this.tocsToDownload.contains("Coalesced.bin")) {
				this.tocsToDownload.add("Coalesced.bin");
			}
			this.numtoc = this.tocsToDownload.size();
			this.progress = progress;
			if (numtoc > 0) {
				currentOperationLabel.setText("Downloading "
						+ coalToTOCString(this.tocsToDownload.get(0)));
			}
			ModManager.debugLogger.writeMessage("============TOCDownloadWorker==========");
			
		}

		protected Void doInBackground() throws Exception {
			int tocsCompleted = 0;
			for (String toc : tocsToDownload) {
				try {
					String link = "http://www.me3tweaks.com/toc/" + coalFilenameToShortName(toc) + "/PCConsoleTOC.bin";
					ModManager.debugLogger.writeMessage("Downloading TOC file: "+link);
					FileUtils.copyURLToFile(new URL(link), new File(
							"toc/" + coalFilenameToShortName(toc) +"/PCConsoleTOC.bin"));
					ModManager.debugLogger.writeMessage("Saved TOC file to "+(new File(
							"toc/" + coalFilenameToShortName(toc) +"/PCConsoleTOC.bin")).getAbsolutePath());
					tocsCompleted++;
					this.publish(tocsCompleted);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					ModManager.debugLogger.writeException(e);
				} catch (Exception e) {
					ModManager.debugLogger.writeException(e);
				}
			}
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			
			progress.setIndeterminate(false);
			if (numtoc > numCompleted.get(0)) {
				currentOperationLabel.setText("Downloading "
						+ coalFilenameToShortName(tocsToDownload.get(numCompleted.get(0))) +"/PCConsoleTOC.bin");
			}
			progress.setValue((int)(100 / (numtoc / (float)numCompleted.get(0))));
		}

		protected void done() {
			// Coals downloaded
			ModManager.debugLogger.writeMessage("TOCs downloaded");
			stepsCompleted++;
			overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted))));
			createCMMMod();
		}
	}
	
	public static String docToString(Document doc) {
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

	public String coalToTOCString(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "MP1 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP2.bin":
			return "MP2 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP3.bin":
			return "MP3 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP4.bin":
			return "MP4 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP5.bin":
			return "MP5 PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1 PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2 PCConsoleTOC.bin";
		case "Coalesced.bin":
			return "BASEGAME PCConsoleTOC.bin";
		default:
			return null;
		}
	}
}
