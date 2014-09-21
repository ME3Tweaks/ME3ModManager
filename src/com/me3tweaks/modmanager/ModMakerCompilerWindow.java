package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class ModMakerCompilerWindow extends JDialog {
	String code, modName;
	private static int TOTAL_STEPS = 10;
	private int stepsCompleted = 1;
	ArrayList<String> requiredCoals = new ArrayList<String>();
	JSONObject modInfo;
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
		modMakerPanel.add(Box.createRigidArea(new Dimension(0,10)));
		modMakerPanel.add(current);
		modMakerPanel.add(currentOperationLabel);
		modMakerPanel.add(currentStepProgress);
		
		modMakerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		this.getContentPane().add(modMakerPanel);
		getModInfo();
	}

	private void getModInfo() {
		String link = "http://www.me3tweaks.com/modmaker/download.php?id="
				+ code;
		try {
			FileUtils.copyURLToFile(new URL(link), new File("mod_info"));
			JSONParser parser = new JSONParser();
			modInfo = (JSONObject) parser.parse(new FileReader("mod_info"));
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
	 * Converts a short name (e.g. MP3, MP4) into the DLC or original coalesced name (Default_DLC_CON_MP3.bin).
	 * @param shortName Short name to convert
	 * @return Coaleced filename or null if unknown.
	 */
	protected String shortNameToCoalFilename(String shortName){
		switch (shortName){
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

	protected void parseModInfo() {
		// modinfo should be defined already.
		JSONObject mod_meta = (JSONObject) modInfo.get("mod_info");
		modName = (String) mod_meta.get("name");
		infoLabel.setText("Compiling " + modName + "...");
		// Find the coals it needs
		JSONArray mod_data = (JSONArray) modInfo.get("mod_data");
		for (Object coal_obj : mod_data) {
			JSONObject coal_data = (JSONObject) coal_obj;
			String coal_name = (String) coal_data.get("coal_name");
			System.out.println("Mod requires "+coal_name);
			requiredCoals.add(shortNameToCoalFilename(coal_name));
		}
		
		

		// Check Coalesceds
		File coalDir = new File("coalesceds");
		coalDir.mkdirs(); // creates if it doens't exist. otherwise nothing.
		ArrayList<String> coals = new ArrayList<String>(requiredCoals); //copy so we don't modify the required ones
		int numToDownload = 0;
		for (int i = coals.size()-1; i >= 0; i--) {
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
	
	class DecompilerWorker extends SwingWorker<Void, Integer>
	{
		private ArrayList<String> coalsToDecompile;
		private JProgressBar progress;
		private int numCoals;
		
		public DecompilerWorker(ArrayList<String> coalsToDownload, JProgressBar progress){
			this.coalsToDecompile = coalsToDownload;
			this.numCoals = coalsToDownload.size();
			this.progress = progress;
    		currentOperationLabel.setText("Decompiling "+coalsToDecompile.get(0));
		}

	    protected Void doInBackground() throws Exception
	    {
	    	int coalsDecompiled = 0;
	    	String path = Paths.get(".").toAbsolutePath().normalize().toString();
	        for (String coal : coalsToDecompile){
	        		String compilerPath = path+"\\DLC Compiler\\Gibbed.MassEffect3.Coalesce.exe";
	        		if (coal.equals("Coalesced.bin")){
	        			//its the default
	        			compilerPath = path+"\\Original Compiler\\Gibbed.MassEffect3.Coalesce.exe";
	        		}
	        		ProcessBuilder decompileProcessBuilder = new ProcessBuilder(compilerPath, "--bin2json", path+"\\coalesceds\\"+coal);
	        		decompileProcessBuilder.redirectErrorStream(true);
	        		decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
	        		Process decompileProcess = decompileProcessBuilder.start();
	        		decompileProcess.waitFor();
					coalsDecompiled++;
					this.publish(coalsDecompiled);
	        }
	    	return null;
	    }
	    
	    @Override
	    protected void process(List<Integer> numCompleted) {
	    	if (numCoals > numCompleted.get(0)){
	    		currentOperationLabel.setText("Decompiling "+coalsToDecompile.get(numCompleted.get(0)));
	    	}
	        progress.setValue(100/(numCoals/numCompleted.get(0)));
	    }

	    protected void done()
	    {
	       //Coals downloaded
	    	stepsCompleted++;
	    	overallProgress.setValue(100/(TOTAL_STEPS/stepsCompleted));
	    	System.out.println("Coals decompiled");
	    	//decompileMods();
	    }
	}

	class CoalDownloadWorker extends SwingWorker<Void, Integer>
	{
		private ArrayList<String> coalsToDownload;
		private JProgressBar progress;
		private int numCoals;
		
		public CoalDownloadWorker(ArrayList<String> coalsToDownload, JProgressBar progress){
			this.coalsToDownload = coalsToDownload;
			this.numCoals = coalsToDownload.size();
			this.progress = progress;
			if (numCoals > 0) {
				currentOperationLabel.setText("Downloading "+coalsToDownload.get(0));
			}
		}

	    protected Void doInBackground() throws Exception
	    {
	    	int coalsCompeted = 0;
	        for (String coal : coalsToDownload){
				try {
					String link = "http://www.me3tweaks.com/coal/"+coal;
					FileUtils.copyURLToFile(new URL(link), new File("coalesceds/"+coal));
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
	    	if (numCoals > numCompleted.get(0)){
	    		currentOperationLabel.setText("Downloading "+coalsToDownload.get(numCompleted.get(0)));
	    	}
	        progress.setValue(100/(numCoals/numCompleted.get(0)));
	    }

	    protected void done()
	    {
	       //Coals downloaded
	    	System.out.println("Coals downloaded");
	    	stepsCompleted++;	    	
	    	overallProgress.setValue(100/(TOTAL_STEPS/stepsCompleted));
	    	decompileMods();
	    }
	}
}
