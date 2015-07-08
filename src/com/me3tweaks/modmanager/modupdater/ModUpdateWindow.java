package com.me3tweaks.modmanager.modupdater;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ResourceUtils;

@SuppressWarnings("serial")
public class ModUpdateWindow extends JDialog implements PropertyChangeListener {
	String downloadSource;
	boolean error = false;
	JButton cancelButton;
	JLabel statusLabel;
	JProgressBar downloadProgress;
	private JFrame callingWindow;
	private UpdatePackage upackage;

	public ModUpdateWindow(JFrame callingWindow, UpdatePackage upackage) {
		this.callingWindow = callingWindow;
		this.upackage = upackage;
		if (ModManager.IS_DEBUG) {
			downloadSource = "https://webdev-mgamerz.c9.io/mods/updates/";
		} else {
			downloadSource = "http://me3tweaks.com/mods/updates/";
		}
		
		this.setTitle("Updating mod");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(300, 90));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
	}
	
	public void startUpdate() {
		DownloadTask task = new DownloadTask(upackage);
		task.addPropertyChangeListener(this);
		task.execute();
		setVisible(true);
	}

	private void setupWindow() {
		JPanel panel = new JPanel(new BorderLayout());
		
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);
		
		statusLabel = new JLabel("0/0 files downloaded");
		
		panel.add(new JLabel(upackage.getMod().getModName()+" is updating..."),BorderLayout.NORTH);
		panel.add(downloadProgress, BorderLayout.CENTER);
		panel.add(statusLabel, BorderLayout.SOUTH);
		//updatePanel.add(actionPanel);
		//updatePanel.add(sizeLabel);

		
		//aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(panel);
	}
	
	void setStatusText(String text) {
    	statusLabel.setText(text);
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
	class DownloadTask extends SwingWorker<Void, Object> {
	    private static final int BUFFER_SIZE = 4096;   
	    private String saveDirectory;
		private UpdatePackage upackage;
		private int numFilesToDownload;
		private int numProcessed;
	     
	    public DownloadTask(UpdatePackage upackage) {
	    	String modpath = upackage.getMod().getModPath();
    		String updateFolder = ResourceUtils.getRelativePath(modpath, System.getProperty("user.dir"), File.separator);
	        this.saveDirectory = saveDirectory + File.separator + updateFolder;
	        this.upackage = upackage;
	        numFilesToDownload = upackage.getFilesToDownload().size();
	    	ModManager.debugLogger.writeMessage("Created a download task");

	    }
	     
	    /**
	     * Executed in background thread
	     */
	    @Override
	    protected Void doInBackground() throws Exception {
	    	//Iterate through files to download and put them in the update folder
	    	ModManager.debugLogger.writeMessage("Downloading update from server source: "+downloadSource);
	    	try {
	    		HTTPDownloadUtil util = new HTTPDownloadUtil();
	    		for (ManifestModFile mf : upackage.getFilesToDownload()){
	    			String saveFilePath = saveDirectory + File.separator + mf.getRelativePath();
	    			File saveFile = new File(saveFilePath);
	    			saveFilePath = saveFile.getParent();
	    			new File(saveFilePath).mkdirs();
	    			String link = downloadSource + upackage.getServerFolderName() + "/" + mf.getRelativePath();
	    			util.downloadFile(link);
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
	    		}
	            util.disconnect();
	            
	            if (!buildUpdateScript()){
	            	cancel(true);
	            }
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(ModUpdateWindow.this, "Error downloading file: " + ex.getMessage(),
	                    "Error", JOptionPane.ERROR_MESSAGE);           
	            ex.printStackTrace();
	            setProgress(0);
	            error = true;
	            cancel(true);      
	        }
	        return null;
	    }
	    
	    @Override
	    public void process(List<Object> chunks) {
	    	numProcessed++;
	    	setStatusText("Aquired "+numProcessed+"/"+numFilesToDownload+" files");
	    }
	 
	    /**
	     * Executed in Swing's event dispatching thread
	     */
	    @Override
	    protected void done() {
	    	//TODO: Install update through the update script
	    	if (!error) {
	    		//runUpdateScript();
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
	     * Opens a stream to the file on the server for transfer
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
	
	public void runUpdateScript() {
		// TODO Auto-generated method stub
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", System.getProperty("user.dir")+"\\update\\updater.cmd" };
		try {
			ModManager.debugLogger.writeMessage("Upgrading "+upackage.getMod().getModName());

			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
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
		String updateFolder = System.getProperty("user.dir")+"\\update\\";
		StringBuilder sb = new StringBuilder();
		sb.append("::Mod Update script for Mod Manager 3.1");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("echo Current directory: %CD%");
		sb.append("\r\n");
		sb.append("pushd update");
		sb.append("::Update the files");
		sb.append("\r\n");
		sb.append("xcopy /Y /S NewVersion "+System.getProperty("user.dir"));
		sb.append("\r\n");
		sb.append("::Cleanup");
		sb.append("\r\n");
		sb.append("del /Q ME3CMM.7z");
		sb.append("\r\n");
		sb.append("rmdir /S /Q NewVersion");
		sb.append("\r\n");
		sb.append("::Run Mod Manager");
		sb.append("\r\n");
		sb.append("popd");
		sb.append("\r\n");
		//sb.append("pause");
		sb.append("\r\n");
		sb.append("ME3CMM.exe --update-from ");
		sb.append(ModManager.BUILD_NUMBER);
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
            JOptionPane.showMessageDialog(ModUpdateWindow.this, "Error building update script: " + e.getClass()+"\nCannot continue.",
                    "Updater Error", JOptionPane.ERROR_MESSAGE);           
			error = true;
			e.printStackTrace();
			dispose();
			return false;
		}
		return true;
	}

}
