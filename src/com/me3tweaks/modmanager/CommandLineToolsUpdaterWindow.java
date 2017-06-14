package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.ProcessResult;

@SuppressWarnings("serial")
public class CommandLineToolsUpdaterWindow extends JDialog implements PropertyChangeListener {
	//String downloadLink, updateScriptLink;
	boolean error = false;
	String version;
	private String fileName;

	JLabel introLabel, statusLabel;
	JProgressBar downloadProgress;

	public CommandLineToolsUpdaterWindow() {
		this.setTitle("Required Command Line Tools Update");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		DownloadTask task = new DownloadTask(ModManager.getTempDir());
		task.addPropertyChangeListener(this);
		ModManager.debugLogger.writeMessage("Downloading Mod Manager Command Line Tools");
		task.execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("This version of Mod Manager requires MM Command Line Tools " + ModManager.getCommandLineToolsRequiredVersion() + ".");
		statusLabel = new JLabel("Downloading new version...");
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);

		updatePanel.add(introLabel);
		updatePanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.add(downloadProgress, BorderLayout.SOUTH);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(updatePanel, BorderLayout.NORTH);
		panel.add(actionPanel, BorderLayout.CENTER);
		panel.add(statusLabel, BorderLayout.SOUTH);
		this.getContentPane().add(panel);
	}

	/**
	 * Update the progress bar's state whenever the progress of download
	 * changes.
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
	 * 
	 * @author www.codejava.net
	 *
	 */
	class DownloadTask extends SwingWorker<Void, Void> {
		private static final int BUFFER_SIZE = 4096;
		private String saveDirectory;
		//private SwingFileDownloadHTTP gui;

		public DownloadTask(String saveDirectory) {
			this.saveDirectory = saveDirectory;
			statusLabel.setText("Downloading update from GitHub...");
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
				util.downloadFile(ModManager.COMMANDLINETOOLS_URL);

				// set file information on the GUI
				fileName = FilenameUtils.getName(ModManager.COMMANDLINETOOLS_URL);

				String saveFilePath = saveDirectory + File.separator + fileName;

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

				if (!buildUpdateScript()) {
					cancel(true);
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(CommandLineToolsUpdaterWindow.this, "Error downloading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
				//String contentType = httpConn.getContentType();
				contentLength = httpConn.getContentLength();

				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);

					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/"), fileURL.length());
					System.err.println("Filename is: " + fileName);
				}

				// opens input stream from the HTTP connection
				inputStream = httpConn.getInputStream();

			} else {
				throw new IOException("No file to download. Server replied HTTP code: " + responseCode);

			}
		}

		public void disconnect() throws IOException {
			inputStream.close();
			httpConn.disconnect();
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
		String[] command = { "cmd.exe", "/c", ModManager.getTempDir() + "commandlinetoolsupdater.cmd" };
		ProcessBuilder p = new ProcessBuilder(command);
		ModManager.debugLogger.writeMessage("Upgrading Command Line Tools.");
		ProcessResult pr = ModManager.runProcess(p);
		if (pr.getReturnCode() != 0) {
			ModManager.debugLogger.writeError("ERROR DOWNLOADING COMMAND LINE TOOLS!");
		}
		//TEST
		ModManager.debugLogger.writeMessage("Extraction script has run, checking for command line tools...");
		File f = new File(ModManager.getCommandLineToolsDir()+"FullAutoTOC.exe");
		if (f.exists()) {
			ModManager.debugLogger.writeMessage("Command Line Tools exists.");
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Command Line Tools downloaded");
		} else {
			ModManager.debugLogger.writeError("Command Line Tools did failed to extract!");
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Command Line Tools failed to download");
		}
		dispose();
	}

	/**
	 * Builds the update script (.cmd) to run when swapping files.
	 * 
	 * @return True if created, false otherwise.
	 */
	private boolean buildUpdateScript() {
		StringBuilder sb = new StringBuilder();
		sb.append("::Update script for Mod Manager Command Line Tools (Mod Manager Build " + ModManager.BUILD_NUMBER + ")");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("echo Command Line Tools Update Script, via Mod Manager Build " + ModManager.BUILD_NUMBER);
		sb.append("\r\n");
		sb.append("pushd data\\temp");
		sb.append("\r\n");
		sb.append("mkdir MMCOMMANDLINETOOLSNEWVERSION");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Extract update");
		sb.append("\r\n\"");
		sb.append(ModManager.getToolsDir());
		sb.append("7z.exe\" -y x " + fileName + " -o\"" + ModManager.getTempDir() + "MMCOMMANDLINETOOLSNEWVERSION\"");
		sb.append("\r\n");
		sb.append("set MMCMDLINETOOLS=%errorlevel%");
		sb.append("\r\n");
		sb.append("if %MMCMDLINETOOLS% EQU 0 (");
		sb.append("\r\n");
		sb.append("    color 0A");
		sb.append("\r\n");
		sb.append("    echo Command Line Tools extracted successfully.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MMCMDLINETOOLS% EQU 1 (");
		sb.append("\r\n");
		sb.append("    color 06");
		sb.append("\r\n");
		sb.append("    echo Command Line Tools extracted with warnings.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MMCMDLINETOOLS% GEQ 2 (");
		sb.append("\r\n");
		sb.append("    color 0C");
		sb.append("\r\n");
		sb.append("    echo CommandLineTools did not extract succesfully. Please report this to FemShep.");
		sb.append("\r\n");
		sb.append("    pause");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Check for build-in update script");
		sb.append("\r\n");
		sb.append("if exist MMCOMMANDLINETOOLSNEWVERSION\\commandlinetoolsupdater.cmd (");
		sb.append("\r\n");
		sb.append("CALL MMCOMMANDLINETOOLSNEWVERSION\\commandlinetoolsupdater.cmd");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("::Remove old folder, copy new one");
		sb.append("if exist \"" + ModManager.getCommandLineToolsDir() + "\" rmdir /S /Q \"");
		sb.append(ModManager.getCommandLineToolsDir());
		sb.append("\"\r\n");
		sb.append("xcopy /Q /Y /S MMCOMMANDLINETOOLSNEWVERSION \"" + ModManager.getCommandLineToolsDir() + "\"");
		sb.append("\r\n");
		sb.append("::Cleanup");
		sb.append("\r\n");
		sb.append("del /Q " + fileName);
		sb.append("\r\n");
		sb.append("rmdir /S /Q MMCOMMANDLINETOOLSNEWVERSION");
		sb.append("\r\n");
		sb.append("call :deleteSelf&exit /b");
		sb.append("\r\n");
		sb.append(":deleteSelf");
		sb.append("\r\n");
		sb.append("start /b \"\" cmd /c del \"%~f0\"&exit /b");

		//sb.append("pause");
		//sb.append("exit");
		try {
			String updatePath = new File(ModManager.getTempDir() + "commandlinetoolsupdater.cmd").getAbsolutePath();
			Files.write(Paths.get(updatePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("Couldn't generate the update script. Must abort.");
			JOptionPane.showMessageDialog(CommandLineToolsUpdaterWindow.this, "Error building update script: " + e.getClass() + "\nCannot continue.", "Updater Error",
					JOptionPane.ERROR_MESSAGE);
			error = true;
			e.printStackTrace();
			dispose();
			return false;
		}
		return true;
	}
}
