package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class UpdateAvailableWindow extends JDialog implements ActionListener, PropertyChangeListener {
	String downloadLink, updateScriptLink;
	boolean error = false;
	String version;
	long build;
	JLabel introLabel, versionsLabel, changelogLabel, sizeLabel;
	JButton updateButton, notNowButton, nextUpdateButton;
	JSONObject updateInfo;
	JProgressBar downloadProgress;

	public UpdateAvailableWindow(JSONObject updateInfo, JFrame callingWindow) {
		this.updateInfo = updateInfo;
		build = (long) updateInfo.get("latest_build_number");
		version = (String) updateInfo.get("latest_version_hr");
		downloadLink = (String) updateInfo.get("download_link");
		this.setTitle("Update Available");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		long width = (long) updateInfo.get("dialog_width"); //dialog size is determined by the latest build information. This is because it might have a long changelog.
		long height = (long) updateInfo.get("dialog_height");
		this.setPreferredSize(new Dimension((int)width, (int)height));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("An update to Mod Manager is available from ME3Tweaks.");
		String latest_version_hr = (String) updateInfo.get("latest_version_hr");
		long latest_build_number = (long) updateInfo.get("latest_build_number");

		versionsLabel = new JLabel("<html>Local Version: "+ModManager.VERSION+" (Build "+ModManager.BUILD_NUMBER+")<br>"
				+ "Latest Version: "+latest_version_hr+" (Build "+latest_build_number+")</html>");
		String release_notes = (String) updateInfo.get("release_notes");
		changelogLabel = new JLabel(release_notes);
		updateButton = new JButton("Download Update");
		updateButton.addActionListener(this);
		notNowButton = new JButton("Not now");
		notNowButton.addActionListener(this);
		nextUpdateButton = new JButton("Remind me on the next update");
		nextUpdateButton.addActionListener(this);
		
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);
		
		sizeLabel = new JLabel(" ");
		
		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		updatePanel.add(versionsLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));

		updatePanel.add(changelogLabel);
		
		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.add(updateButton, BorderLayout.WEST);
		actionPanel.add(nextUpdateButton, BorderLayout.EAST);
		actionPanel.add(downloadProgress, BorderLayout.SOUTH);
		//updatePanel.add(actionPanel);
		//updatePanel.add(sizeLabel);

		
		//aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		updatePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panel.add(updatePanel, BorderLayout.NORTH);
		panel.add(actionPanel, BorderLayout.CENTER);
		panel.add(sizeLabel,BorderLayout.SOUTH);
		this.getContentPane().add(panel);
	}
	
    void setStatusText(String text) {
    	sizeLabel.setText(text);
    }
     
    /**
     * Update the progress bar's state whenever the progress of download changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            downloadProgress.setValue(progress);
        }
    }
	
	/**
	 * Execute file download in a background thread and update the progress.
	 * @author www.codejava.net
	 *
	 */
	class DownloadTask extends SwingWorker<Void, Void> {
	    private static final int BUFFER_SIZE = 4096;   
	    private String saveDirectory;
	    //private SwingFileDownloadHTTP gui;
	     
	    public DownloadTask(String saveDirectory) {
	        this.saveDirectory = saveDirectory;
	    }
	     
	    /**
	     * Executed in background thread
	     */
	    @Override
	    protected Void doInBackground() throws Exception {
	    	//Download the update
	    	try {

	        	//Download update
	            HTTPDownloadUtil util = new HTTPDownloadUtil();
	            util.downloadFile(downloadLink);
	             
	            // set file information on the GUI
	            setStatusText("Downloading update...");
	             
	            String saveFilePath = saveDirectory + File.separator + util.getFileName();
	 
	            InputStream inputStream = util.getInputStream();
	            // opens an output stream to save into file
	            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
	 
	            byte[] buffer = new byte[BUFFER_SIZE];
	            int bytesRead = -1;
	            long totalBytesRead = 0;
	            int percentCompleted = 0;
	            long fileSize = util.getContentLength();
	 
	            while ((bytesRead = inputStream.read(buffer)) != -1) {
	                outputStream.write(buffer, 0, bytesRead);
	                totalBytesRead += bytesRead;
	                percentCompleted = (int) (totalBytesRead * 100 / fileSize);
	 
	                setProgress(percentCompleted);         
	            }
	 
	            outputStream.close();
	 
	            util.disconnect();
	            
	            if (!buildUpdateScript()){
	            	cancel(true);
	            }
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error downloading file: " + ex.getMessage(),
	                    "Error", JOptionPane.ERROR_MESSAGE);           
	            ex.printStackTrace();
	            setProgress(0);
	            error = true;
	            cancel(true);          
	        }
	        return null;
	    }
	 
	    /**
	     * Executed in Swing's event dispatching thread
	     */
	    @Override
	    protected void done() {
	    	//TODO: Install update through the update script
	    	if (!error) {
	    		runUpdateScript();
	    	}
	    }  
	}
	 
	/**
	 * A utility that downloads a file from a URL.
	 *
	 * @author www.codejava.net
	 *
	 */
	class HTTPDownloadUtil {
	 
	    private HttpURLConnection httpConn;
	 
	    /**
	     * hold input stream of HttpURLConnection
	     */
	    private InputStream inputStream;
	 
	    private String fileName;
	    private int contentLength;
	 
	    /**
	     * Downloads a file from a URL
	     *
	     * @param fileURL
	     *            HTTP URL of the file to be downloaded
	     * @throws IOException
	     */
	    public void downloadFile(String fileURL) throws IOException {
	        URL url = new URL(fileURL);
	        httpConn = (HttpURLConnection) url.openConnection();
	        int responseCode = httpConn.getResponseCode();
	 
	        // always check HTTP response code first
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            String disposition = httpConn.getHeaderField("Content-Disposition");
	            String contentType = httpConn.getContentType();
	            contentLength = httpConn.getContentLength();
	 
	            if (disposition != null) {
	                // extracts file name from header field
	                int index = disposition.indexOf("filename=");
	                if (index > 0) {
	                    fileName = disposition.substring(index + 10,
	                            disposition.length() - 1);
	                }
	            } else {
	                // extracts file name from URL
	                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
	                        fileURL.length());
	            }
	 
	            // output for debugging purpose only
	            System.out.println("Content-Type = " + contentType);
	            System.out.println("Content-Disposition = " + disposition);
	            System.out.println("Content-Length = " + contentLength);
	            System.out.println("fileName = " + fileName);
	 
	            // opens input stream from the HTTP connection
	            inputStream = httpConn.getInputStream();
	 
	        } else {
	            throw new IOException(
	                    "No file to download. Server replied HTTP code: "
	                            + responseCode);
	            
	        }
	    }
	 
	    public void disconnect() throws IOException {
	        inputStream.close();
	        httpConn.disconnect();
	    }
	 
	    public String getFileName() {
	        return this.fileName;
	    }
	 
	    public int getContentLength() {
	        return this.contentLength;
	    }
	 
	    public InputStream getInputStream() {
	        return this.inputStream;
	    }
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == updateButton){
			updateButton.setEnabled(false);
			File updateDir = new File("update");
			updateDir.mkdirs();
			DownloadTask task = new DownloadTask("update");
			task.addPropertyChangeListener(this);
			task.execute();
		} else 
		if (e.getSource() == notNowButton) {
			dispose();
			return;
		} else 
		if (e.getSource() == nextUpdateButton) {
			//write to ini that we don't want update
			Wini ini;
			try {
				File settings = new File(ModManager.settingsFilename);
				if (!settings.exists())
					settings.createNewFile();
				ini = new Wini(settings);
				ini.put("Settings", "nextupdatedialogbuild", build+1);
				ModManager.debugLogger.writeMessage("Ignoring current update, will show again when "+(build+1)+ " is released.");
				ini.store();
			} catch (InvalidFileFormatException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
			}
			dispose();
		}
		
	}
	
	public void runUpdateScript() {
		// TODO Auto-generated method stub
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", System.getProperty("user.dir")+"\\update\\updater.cmd" };
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ModManager.debugLogger.writeMessage("Upgrading to build "+build+", shutting down.");
		System.exit(0);
	}

	/**
	 * Builds the update script (.cmd) to run when swapping files.
	 * @return True if created, false otherwise.
	 */
	private boolean buildUpdateScript(){
		String updateFolder = System.getProperty("user.dir")+"\\update\\";
		StringBuilder sb = new StringBuilder();
		sb.append("::Update script for Mod Manager 3.0 (Build "+build+")");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("echo Current directory: %CD%");
		sb.append("\r\n");
		sb.append("pushd update");
		sb.append("\r\n");
		sb.append("::Wait for 2 seconds so the JVM fully exits.");
		sb.append("\r\n");
		sb.append("PING 1.1.1.1 -n 1 -w 2000 >NUL");
		sb.append("\r\n");
		sb.append("mkdir NewVersion");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Extract update");
		sb.append("\r\n");
		sb.append("7za.exe -y e ME3CMM.7z -o"+updateFolder+"NewVersion");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Update the files");
		sb.append("\r\n");
		sb.append("xcopy /Y NewVersion "+System.getProperty("user.dir"));
		sb.append("\r\n");
		
		sb.append("::Delete compressed update");
		sb.append("\r\n");
		sb.append("del /Q ME3CMM.7z");
		sb.append("\r\n");
		
		sb.append("::Run Mod Manager");
		sb.append("\r\n");
		sb.append("popd");
		//sb.append("\r\n");
		//sb.append("echo Current directory: %CD%");
		sb.append("\r\n");
		sb.append("ME3CMM.exe --update-complete");
		sb.append("\r\n");
		sb.append("rmdir /S /Q NewVersion");
		sb.append("\r\n");
		sb.append("call :deleteSelf&exit /b");
		sb.append("\r\n");
		sb.append(":deleteSelf");
		sb.append("\r\n");
		sb.append("start /b \"\" cmd /c del \"%~f0\"&exit /b");
		
		
		
		//sb.append("pause");
		//sb.append("exit");
		try {
			String updatePath = new File(".\\update\\updater.cmd").getAbsolutePath();
			Files.write( Paths.get(updatePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("Couldn't generate the update script. Must abort.");
            JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error building update script: " + e.getClass()+"\nCannot continue.",
                    "Updater Error", JOptionPane.ERROR_MESSAGE);           
			error = true;
			e.printStackTrace();
			dispose();
			return false;
		}
		return true;
	}
}
