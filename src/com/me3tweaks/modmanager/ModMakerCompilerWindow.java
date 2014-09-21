package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class ModMakerCompilerWindow extends JDialog {
	String code, modName;
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
		modMakerPanel.add(current);
		modMakerPanel.add(currentOperationLabel);
		modMakerPanel.add(currentStepProgress);
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

	protected void parseModInfo() {
		// modinfo should be defined already.
		JSONObject mod_meta = (JSONObject) modInfo.get("mod_info");
		modName = (String) mod_meta.get("name");
		infoLabel.setText("Compiling " + modName + "...");

		System.out.println("ParseModInfo");

		// Check Coalesceds
		File coalDir = new File("coalesceds");
		coalDir.mkdirs(); // creates if it doens't exist. otherwise nothing.
		ArrayList<String> coals = new ArrayList<String>();
		// Have to manually ad them... thanks Oracle!
		coals.add("Default_DLC_CON_MP1.bin");
		coals.add("Default_DLC_CON_MP2.bin");
		coals.add("Default_DLC_CON_MP3.bin");
		coals.add("Default_DLC_CON_MP4.bin");
		coals.add("Default_DLC_CON_MP5.bin");
		coals.add("Default_DLC_UPD_Patch01.bin");
		coals.add("Default_DLC_UPD_Patch02.bin");
		int numToDownload = 0;
		for (int i = 6; i >= 0; i--) {
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

	class CoalDownloadWorker extends SwingWorker<Void, Integer>
	{
		private ArrayList<String> coalsToDownload;
		private JProgressBar progress;
		private int numCoals;
		
		public CoalDownloadWorker(ArrayList<String> coalsToDownload, JProgressBar progress){
			this.coalsToDownload = coalsToDownload;
			this.numCoals = coalsToDownload.size();
			this.progress = progress;
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
	        progress.setValue(100/(numCoals/numCompleted.get(0)));
	    }

	    protected void done()
	    {
	       //Coals downloaded
	    	System.out.println("Coals downloaded");
	    	
	    }
	}
}
