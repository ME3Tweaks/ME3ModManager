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
public class ME3ExplorerUpdaterWindow extends JDialog implements PropertyChangeListener {
	//String downloadLink, updateScriptLink;
	boolean error = false;
	String version;
	JLabel introLabel, statusLabel;
	JProgressBar downloadProgress;

	public ME3ExplorerUpdaterWindow(JFrame callingWindow) {
		this.setTitle("Required ME3Explorer Update");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		DownloadTask task = new DownloadTask(ModManager.getTempDir());
		task.addPropertyChangeListener(this);
		ModManager.debugLogger.writeMessage("Downloading ME3Explorer.7z");
		task.execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("This version of Mod Manager requires ME3Explorer "+ModManager.MIN_REQUIRED_ME3EXPLORER_MAIN+"."+ModManager.MIN_REQUIRED_ME3EXPLORER_MINOR+"."+ModManager.MIN_REQUIRED_ME3EXPLORER_REV+" or higher.");
		statusLabel = new JLabel("Downloading new version...");
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);
		
		
		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.add(downloadProgress, BorderLayout.SOUTH);

		
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panel.add(updatePanel, BorderLayout.NORTH);
		panel.add(actionPanel, BorderLayout.CENTER);
		panel.add(statusLabel,BorderLayout.SOUTH);
		this.getContentPane().add(panel);
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
	        statusLabel.setText("Downloading update from ME3Tweaks...");
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
	            util.downloadFile("https://me3tweaks.com/modmanager/tools/me3explorer.7z");
	             
	            // set file information on the GUI
	             
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
	            JOptionPane.showMessageDialog(ME3ExplorerUpdaterWindow.this, "Error downloading file: " + ex.getMessage(),
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
	    	} else {
	    		dispose();
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
/*	            System.out.println("Content-Type = " + contentType);
	            System.out.println("Content-Disposition = " + disposition);
	            System.out.println("Content-Length = " + contentLength);
	            System.out.println("fileName = " + fileName);*/
	 
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
	
	public void runUpdateScript() {
		// TODO Auto-generated method stub
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", ModManager.getTempDir()+"me3expupdater.cmd" };
		try {
			ProcessBuilder p = new ProcessBuilder(command);
			ModManager.debugLogger.writeMessage("Upgrading ME3Explorer.");
			Process updater = p.start();
			updater.waitFor();
			ModManager.debugLogger.writeMessage("ME3Explorer should have been updated...");
			dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Builds the update script (.cmd) to run when swapping files.
	 * @return True if created, false otherwise.
	 */
	private boolean buildUpdateScript(){
		StringBuilder sb = new StringBuilder();
		sb.append("::Update script for Mod Manager ME3Explorer (Mod Manager Build "+ModManager.BUILD_NUMBER+")");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("echo ME3Explorer Update Script, via Mod Manager Build "+ModManager.BUILD_NUMBER);
		sb.append("\r\n");
		sb.append("pushd data\\temp");
		sb.append("\r\n");
		sb.append("mkdir ME3EXPNewVersion");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Extract update");
		sb.append("\r\n\"");
		sb.append(ModManager.getToolsDir());
		sb.append("7za.exe\" -y x ME3Explorer.7z -o\""+ModManager.getTempDir()+"ME3EXPNewVersion\"");
		sb.append("\r\n");
		sb.append("set ME3EXP=%errorlevel%");
		sb.append("\r\n");
		sb.append("if %ME3EXP% EQU 0 (");
		sb.append("\r\n");
		sb.append("    color 0A");
		sb.append("\r\n");
		sb.append("    echo ME3Explorer extracted successfully.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %ME3EXP% EQU 1 (");
		sb.append("\r\n");
		sb.append("    color 06");
		sb.append("\r\n");
		sb.append("    echo ME3Explorer extracted with warnings.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %ME3EXP% GEQ 2 (");
		sb.append("\r\n");
		sb.append("    color 0C");
		sb.append("\r\n");
		sb.append("    echo ME3Explorer did not extract succesfully. Please report this to FemShep.");
		sb.append("\r\n");
		sb.append("    pause");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Check for build-in update script");
		sb.append("\r\n");
		sb.append("if exist ME3EXPNewVersion\\me3expupdater.cmd (");
		sb.append("\r\n");
		sb.append("CALL ME3EXPNewVersion\\me3expupdater.cmd");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("::Remove old folder, copy new one");
		sb.append("\r\n");
		sb.append("rmdir /S /Q ");
		sb.append(ModManager.getME3ExplorerEXEDirectory_LEGACY(false));
		sb.append("\r\n");
		sb.append("xcopy /Q /Y /S ME3EXPNewVersion \""+ModManager.getME3ExplorerEXEDirectory_LEGACY(false)+"\"");
		sb.append("\r\n");
		sb.append("::Cleanup");
		sb.append("\r\n");
		sb.append("del /Q ME3Explorer.7z");
		sb.append("\r\n");
		sb.append("rmdir /S /Q ME3EXPNewVersion");
		sb.append("\r\n");
		sb.append("pause");
		sb.append("\r\n");
		sb.append("call :deleteSelf&exit /b");
		sb.append("\r\n");
		sb.append(":deleteSelf");
		sb.append("\r\n");
		sb.append("start /b \"\" cmd /c del \"%~f0\"&exit /b");
		
		
		
		//sb.append("pause");
		//sb.append("exit");
		try {
			String updatePath = new File(ModManager.getTempDir()+"me3expupdater.cmd").getAbsolutePath();
			Files.write( Paths.get(updatePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("Couldn't generate the update script. Must abort.");
            JOptionPane.showMessageDialog(ME3ExplorerUpdaterWindow.this, "Error building update script: " + e.getClass()+"\nCannot continue.",
                    "Updater Error", JOptionPane.ERROR_MESSAGE);           
			error = true;
			e.printStackTrace();
			dispose();
			return false;
		}
		return true;
	}
}
