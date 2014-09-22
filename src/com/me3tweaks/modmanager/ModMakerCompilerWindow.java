package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class ModMakerCompilerWindow extends JDialog {
	String code, modName;
	private static int TOTAL_STEPS = 10;
	private static String DOWNLOADED_JSON_FILENAME = "mod_info";
	private int stepsCompleted = 1;
	ArrayList<String> requiredCoals = new ArrayList<String>();
	JSONObject mod_object, mod_info;
	/**
	 * Mod Data is the mod_data element. It contains an array of coal_name (MP4) and files arrays as objects.
	 */
	JSONArray mod_data;
	JLabel infoLabel, currentOperationLabel;
	JProgressBar overallProgress, currentStepProgress;

	public ModMakerCompilerWindow(JFrame callingWindow, String code) {
		this.code = code;
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
		this.setVisible(true);
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
		getModInfo();
	}

	private void getModInfo() {
		String link = "http://www.me3tweaks.com/modmaker/download.php?id="
				+ code;
		try {
			FileUtils.copyURLToFile(new URL(link), new File(DOWNLOADED_JSON_FILENAME));
			JSONParser parser = new JSONParser();
			mod_object = (JSONObject) parser.parse(new FileReader(DOWNLOADED_JSON_FILENAME));
			parseModInfo();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
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
		case "ORIGINAL":
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
		default:
			return null;
		}
	}

	protected void parseModInfo() {
		// modinfo should be defined already.
		mod_info = (JSONObject) mod_object.get("mod_info");
		modName = (String) mod_info.get("name");
		
		//Check the name
		File moddir = new File(modName);
		if (moddir.isDirectory()) {
			try {
				System.out.println("DEBUGGING: Remove existing mod directory");
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
		// Find the coals it needs
		mod_data = (JSONArray) mod_object.get("mod_data");
		for (Object coal_obj : mod_data) {
			JSONObject coal_data = (JSONObject) coal_obj;
			String coal_name = (String) coal_data.get("coal_name");
			System.out.println("Mod requires " + coal_name);
			requiredCoals.add(shortNameToCoalFilename(coal_name));
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
	 * Runs the Coalesced files through Gibbed's decompiler
	 */
	public void decompileMods() {
		// TODO Auto-generated method stub

		new DecompilerWorker(requiredCoals, currentStepProgress).execute();

	}

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
						+ "\\DLC Compiler\\Gibbed.MassEffect3.Coalesce.exe";
				if (coal.equals("Coalesced.bin")) {
					// its the default
					compilerPath = path
							+ "\\Original Compiler\\Gibbed.MassEffect3.Coalesce.exe";
				}
				ProcessBuilder decompileProcessBuilder = new ProcessBuilder(
						compilerPath, "--bin2json", path + "\\coalesceds\\"
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
			System.out.println("Coals decompiled");
			new JsonMergeWorker(progress).execute();
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
						+ "\\DLC Compiler\\Gibbed.MassEffect3.Coalesce.exe";
				if (coal.equals("Coalesced.bin")) {
					// its the default
					compilerPath = path
							+ "\\Original Compiler\\Gibbed.MassEffect3.Coalesce.exe";
				}
				ProcessBuilder decompileProcessBuilder = new ProcessBuilder(
						compilerPath, "--json2bin", path + "\\coalesceds\\"
								+ FilenameUtils.removeExtension(coal) + "\\");
				decompileProcessBuilder.redirectErrorStream(true);
				decompileProcessBuilder
						.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				Process decompileProcess = decompileProcessBuilder.start();
				decompileProcess.waitFor();
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
			System.out.println("Coals recompiled");
			createCMMMod();
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
			System.out.println("Coals downloaded");
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			decompileMods();
		}
	}
	
	/**
	 * After coals are downloaded and decompiled, this worker is created and
	 * merges the contents of the downloaded mod into all of the decompiled json files.
	 */
	class JsonMergeWorker extends SwingWorker<Void, Integer> {
		private int numFilesToMerge;
		private JProgressBar progress;

		public JsonMergeWorker(JProgressBar progress) {
			this.progress = progress;
		}

		protected Void doInBackground() throws Exception {
			int coalsCompeted = 0;
			// we are going to parse the mod_data array and then look at all the
			// files in the array.
			// Haha wow this is going to be ugly.
			
			
			/*
			 * Structure of mod_data array and elements
			 * mod_data [ARRAY]
			 *    |-coal_name (What Coalesced file, MP1, Original, PATCH1...)
			 *    |-files [ARRAY] (what .json files to merge)
			 *      |- filename (what .json file we are going to merge)
			 *      |- merge_data (data to actually merge - this object will match the structure of the filename item when read into memory.)
			 *        |- elements... etc
			 */

			for (Object coal_obj : mod_data) {
				JSONObject coal_data = (JSONObject) coal_obj; //an object in the array
				String coal_name = (String) coal_data.get("coal_name"); //Coalesced file we are going to modify. NOT the json.
				
				JSONArray files = (JSONArray) coal_data.get("files"); //an object in the array
				int localNumberToMerge = files.size();
				for (Object json_file : files){
					//Objects in the files array contain filename and merge data
					JSONObject json_fileobj = (JSONObject) json_file;
					String filename = (String) json_fileobj.get("filename");
					/**
					 * Data we want to merge into the file, downloaded from me3tweaks
					 */
					JSONObject merge_data = (JSONObject) json_fileobj.get("merge_data");
					
					//From here we must algorithmically drill down as far as we can into the json and add only the end nodes.
					try {
						JSONParser parser = new JSONParser();
						String shortNameDir = "coalesceds\\"+FilenameUtils.removeExtension(shortNameToCoalFilename(coal_name));
						System.out.println("Merging file "+coal_name);
						FileReader jsonReader = new FileReader(shortNameDir+"\\"+filename+".json");
						
						/**
						 * The actual file on the filesystem we are merging into. This is the .json that will go into the Coalesced file.
						 */
						JSONObject bioMergeTarget = (JSONObject) parser.parse(jsonReader); //load original into memory.
						jsonReader.close();
						//recursively drill into the json...
						bioMergeTarget = drillObject(bioMergeTarget, merge_data);
						System.out.println(bioMergeTarget.toJSONString());
						
						//write to file
						FileWriter file = new FileWriter(shortNameDir+"\\"+filename+".json");
						file.write(bioMergeTarget.toJSONString());
						file.flush();
						file.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		return null;
		}
		
		private JSONObject drillObject(JSONObject filesystem, JSONObject toMerge){
			Iterator<String> itr = toMerge.keySet().iterator();
			while (itr.hasNext()){
				String keyname = itr.next();
				Object drilledToMerge = toMerge.get(keyname);
				Object drilledFilesystem = filesystem.get(keyname);
				
				if (drilledToMerge instanceof JSONObject) {
					filesystem.put(keyname, drillObject((JSONObject)drilledFilesystem, (JSONObject)drilledToMerge));
				} else
				if (drilledToMerge instanceof JSONArray) {
					filesystem.put(keyname, drillArray((JSONArray)drilledFilesystem, (JSONArray)drilledToMerge));
				} else {
					//check what data type we have
					filesystem.put(keyname, drilledToMerge);
					//System.out.println("bottom level object: "+keyname+" -> "+drilledToMerge.get(keyname));
				}
			}
			return filesystem;
		}
		
		private JSONArray drillArray(JSONArray filesystem, JSONArray toMerge){
			for (int i = 0; i < toMerge.size(); i++) {
				Object obj = toMerge.get(i);
				if (obj instanceof JSONArray) {
					filesystem.set(i, drillArray((JSONArray)obj, (JSONArray)obj));
					//return drillArray((JSONArray)obj, (JSONArray)obj);
				} else 
				if (obj instanceof JSONObject) {
					//JSONObject arrayObj = (JSONObject) obj;
					filesystem.set(i, drillObject((JSONObject)obj, (JSONObject)obj));
					
					//arrayObjarrayObj.put(toMerge., );
					//return 
				}/* else {
					filesystem.put(keyname, drilledToMerge);
					System.out.println("Bottom level of array: "+obj);
				} */
				else {
					System.out.println("Not an array or object in an array.");
				}
			}
			return filesystem;
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
			System.out.println("Coals downloaded");
			stepsCompleted++;
			overallProgress.setValue(100 / (TOTAL_STEPS / stepsCompleted));
			new CompilerWorker(requiredCoals, progress).execute();
		}
	}



	/**
	 * Creates a CMM Mod package from the completed previous steps.
	 */
	private void createCMMMod() {
		File moddir = new File(modName);
		moddir.mkdirs(); // created mod directory

		// Write mod descriptor file
		Wini ini;
		try {
			File moddesc = new File(moddir + "\\moddesc.ini");
			if (!moddesc.exists())
				moddesc.createNewFile();
			ini = new Wini(moddesc);
			ini.put("ModManager", "cmmver", 3.0);
			ini.put("ModInfo", "modname", modName);
			ini.put("ModInfo", "moddesc", "Created with Mod Maker");
			ini.put("ModInfo", "modsite", "http://me3tweaks.com");
			
			// Create directories, move files to them
			for (String reqcoal : requiredCoals) {
				File compCoalDir = new File(moddir.toString() + "\\"+coalFilenameToShortName(reqcoal));
				compCoalDir.mkdirs();
				File coalFile = new File("coalesceds\\"+reqcoal);
				if (coalFile.renameTo(new File(compCoalDir+"\\"+reqcoal))){
					System.out.println("Moved "+reqcoal+" to proper mod element directory");
				} else {
					System.err.println("ERROR! Didn't move "+reqcoal+" to the proper mod element directory. Could already exist.");
				}
				String fileNameWithOutExt = FilenameUtils.removeExtension(reqcoal);
				File compCoalSourceDir = new File("coalesceds\\"+fileNameWithOutExt);
				
				//TODO: Add PCConsoleTOC.bin to the desc file.
				
				ini.put(coalNameToModDescName(reqcoal), "moddir", coalFilenameToShortName(reqcoal));
				ini.put(coalNameToModDescName(reqcoal), "newfiles", reqcoal);
				ini.put(coalNameToModDescName(reqcoal), "replacefiles", coalFileNameToDLCDir(reqcoal));

				
				/*			try {
					FileUtils.deleteDirectory(compCoalSourceDir);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				
				
				ini.store();
			}
			
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Mod file encountered an I/O error while attempting to write it. Mod Descriptor not saved.");
		}

		
		
		//Mod Created!
	}
}
