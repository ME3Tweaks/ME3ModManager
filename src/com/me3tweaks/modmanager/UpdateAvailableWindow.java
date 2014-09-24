package com.me3tweaks.modmanager;

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

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class UpdateAvailableWindow extends JDialog implements ActionListener, PropertyChangeListener {
	String downloadLink, updateScriptLink;
	JLabel introLabel, versionsLabel, changelogLabel, sizeLabel;
	JButton updateButton;
	JSONObject updateInfo;
	JProgressBar downloadProgress;

	public UpdateAvailableWindow(JSONObject updateInfo, JFrame callingWindow) {
		this.updateInfo = updateInfo;
		downloadLink = (String) updateInfo.get("download_link");
		updateScriptLink = (String) updateInfo.get("update_script_link");
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
		
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);
		
		sizeLabel = new JLabel();
		
		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		updatePanel.add(versionsLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));

		updatePanel.add(changelogLabel);
		updatePanel.add(updateButton);
		updatePanel.add(downloadProgress);
		updatePanel.add(sizeLabel);

		
		//aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		updatePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(updatePanel);
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
	    private String downloadURL;
	    private String saveDirectory;
	    //private SwingFileDownloadHTTP gui;
	     
	    public DownloadTask(String downloadURL, String saveDirectory) {
	        this.downloadURL = downloadURL;
	        this.saveDirectory = saveDirectory;
	    }
	     
	    /**
	     * Executed in background thread
	     */
	    @Override
	    protected Void doInBackground() throws Exception {
	    	//Download 7za.exe so we can decompress the update as a single file
	    	try {
	            HTTPDownloadUtil util = new HTTPDownloadUtil();
	            util.downloadFile("http://me3tweaks.com/download/7za.exe");
	             
	            // set file information on the GUI
	            setStatusText("Downloading 7zip decompressor...");
	             
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
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error downloading file: " + ex.getMessage(),
	                    "Error", JOptionPane.ERROR_MESSAGE);           
	            ex.printStackTrace();
	            setProgress(0);
	            cancel(true);          
	        }
	    	
	    	//Download the update
	    	try {

	        	//Download update
	            HTTPDownloadUtil util = new HTTPDownloadUtil();
	            util.downloadFile(updateScriptLink);
	             
	            // set file information on the GUI
	            setStatusText("Downloading update script...");
	             
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
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error downloading file: " + ex.getMessage(),
	                    "Error", JOptionPane.ERROR_MESSAGE);           
	            ex.printStackTrace();
	            setProgress(0);
	            cancel(true);          
	        }
	    	
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
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error downloading file: " + ex.getMessage(),
	                    "Error", JOptionPane.ERROR_MESSAGE);           
	            ex.printStackTrace();
	            setProgress(0);
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
			DownloadTask task = new DownloadTask(downloadLink, "update");
			task.addPropertyChangeListener(this);
			task.execute();
		}
		
	}
}
