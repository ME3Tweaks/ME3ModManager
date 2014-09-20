package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;


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
		//this.setResizable(false);
		//this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel modMakerPanel = new JPanel();
		modMakerPanel.setLayout(new BoxLayout(modMakerPanel, BoxLayout.PAGE_AXIS));
		infoLabel = new JLabel("Preparing to compile "+code+"...");
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
	
	private void getModInfo(){
		final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		try {
			String link = "http://127.0.0.1/modmaker/download.php?id="+code; //development link
			//String link = "http://me3tweaks.com/modmaker/download.php?id="+code;
			currentStepProgress.setIndeterminate(true);
			asyncHttpClient.prepareGet(link).execute(new AsyncCompletionHandler<Response>(){

			    @Override
			    public Response onCompleted(Response response) throws Exception{
			        // Do something with the Response
			        // ...
			    	modInfo = new JSONObject(response.getResponseBody());
			    	if (modInfo.has("error")){
			    		//an error occured
			    		System.err.println("Mod does not exist on the server: "+code);
			    		asyncHttpClient.close();
				        return response;
			    	}
			    	parseModInfo();
			    	asyncHttpClient.close();
			        return response;
			    }

			    @Override
			    public void onThrowable(Throwable t){
			        // Something wrong happened.
			    	asyncHttpClient.close();
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void parseModInfo() {
		//modinfo should be defined already.
		try {
			modName = modInfo.getJSONObject("mod_info").getString("name");
			infoLabel.setText("Compiling "+modName+"...");
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
