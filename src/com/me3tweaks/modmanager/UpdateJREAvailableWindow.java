package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.ArchUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;

import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class UpdateJREAvailableWindow extends JDialog implements ActionListener, PropertyChangeListener {
	String downloadLink, updateScriptLink;
	boolean error = false;
	String version;
	JLabel introLabel, versionsLabel, changelogLabel, sizeLabel;
	JButton updateButton, notNowButton;
	JSONObject updateInfo;
	JProgressBar downloadProgress;
	private JPanel downloadPanel;

	public UpdateJREAvailableWindow(JSONObject updateInfo) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening JRE update available window");
		this.updateInfo = updateInfo;
		version = (String) updateInfo.get("jre_latest_version_hr");
		downloadLink = (String) updateInfo.get("jre_download_link");

		ModManager.debugLogger.writeMessage("JRE Update info:");
		ModManager.debugLogger.writeMessage(" - Version: " + version);
		ModManager.debugLogger.writeMessage(" - Primary Download: " + downloadLink);
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("JRE Update Available");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setIconImages(ModManager.ICONS);

		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel();
		introLabel.setText("<html>An update for Mod Manager's Java Runtime is available.</html>");

		String bitnessUpgrade = "";
		boolean x86 = ArchUtils.getProcessor().is32Bit();
		if (x86 && ResourceUtils.is64BitWindows()) {
			bitnessUpgrade = "<br>This will upgrade Mod Manager from 32-bit java to 64-bit java.";
		}
		if (!x86 && !ModManager.isUsingBundledJRE()) {
			bitnessUpgrade = "<br>This will switch Mod Manager from using your system JRE to a bundled version.";
		}

		versionsLabel = new JLabel("<html>Local Version: " + System.getProperty("java.version") + "<br>" + "Latest Version: " + version + bitnessUpgrade + "</html>");

		String release_notes = (String) updateInfo.get("jre_latest_release_notes");
		changelogLabel = new JLabel("<html><div style=\"width:270px;\">" + release_notes + "</div></html>");
		updateButton = new JButton("Install Update");
		updateButton.addActionListener(this);
		notNowButton = new JButton("Not now");
		notNowButton.addActionListener(this);
		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(true);
		downloadProgress.setEnabled(false);

		sizeLabel = new JLabel(" "); //space or it won't pack properly
		sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		//Panel setup
		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.Y_AXIS));
		versionPanel.add(versionsLabel);
		versionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Update Information"));

		JPanel changeLogPanel = new JPanel();
		changeLogPanel.setLayout(new BoxLayout(changeLogPanel, BoxLayout.Y_AXIS));
		changeLogPanel.setBorder(new TitledBorder(new EtchedBorder(), "Changelog"));

		updatePanel.add(introLabel);
		updatePanel.add(versionPanel);

		changeLogPanel.add(changelogLabel);

		updatePanel.add(changeLogPanel);
		updatePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(updateButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Actions"));
		actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		updatePanel.add(actionPanel);

		downloadPanel = new JPanel();
		downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
		downloadPanel.add(downloadProgress);
		downloadPanel.add(sizeLabel);
		downloadPanel.setBorder(new TitledBorder(new EtchedBorder(), "Download Progress"));
		downloadPanel.setVisible(false);
		updatePanel.add(downloadPanel);

		actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		downloadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		changeLogPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		getContentPane().add(updatePanel);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	void setStatusText(String text) {
		sizeLabel.setText(text);
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
	class DownloadTask extends SwingWorker<Void, ThreadCommand> {
		private static final int BUFFER_SIZE = 4096;
		private String saveDirectory;
		//private SwingFileDownloadHTTP gui;
		private long fileSize = 0;
		private long totalBytesRead = 0;

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
				//downloadLink = "https://me3tweaks.com/modmanager/updates/62/ME3CMM.7z";
				//downloadLink2 = "https://github.com/Mgamerz/me3modmanager/releases/download/4.4/ME3CMM.7z";
				publish(new ThreadCommand("UPDATE_STATUS", "Downloading update..."));
				util.downloadFile(downloadLink);
				// set file information on the GUI

				String saveFilePath = saveDirectory + File.separator + "JRE.7z";

				InputStream inputStream = util.getInputStream();
				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = -1;
				totalBytesRead = 0;
				fileSize = util.getContentLength();

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					publish(new ThreadCommand("UPDATE_PROGRESS", null, totalBytesRead));
				}

				outputStream.close();

				util.disconnect();

				if (!buildUpdateScript()) {
					cancel(true);
				}
			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("ERROR DOWNLOADING UPDATE: ", ex);
				JOptionPane.showMessageDialog(UpdateJREAvailableWindow.this, "Error downloading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				publish(new ThreadCommand("UPDATE_PROGRESS", null, new Integer(0)));
				error = true;
				cancel(true);
			}
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand latest : chunks) {
				switch (latest.getCommand()) {
				case "UPDATE_PROGRESS":
					downloadProgress.setIndeterminate(false);
					int percentCompleted = (int) (totalBytesRead * 100 / fileSize);
					setStatusText(
							"Downloading Update...  " + ResourceUtils.humanReadableByteCount(totalBytesRead, true) + "/" + ResourceUtils.humanReadableByteCount(fileSize, true));
					setProgress(percentCompleted);
					break;
				case "UPDATE_STATUS":
					setStatusText(latest.getMessage());
					break;
				}
			}
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
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}

				// opens input stream from the HTTP connection
				inputStream = httpConn.getInputStream();

			} else {
				if (responseCode == 404) {
					throw new FileNotFoundException("File to download does not exist (404)");
				} else {
					throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
				}
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
		if (e.getSource() == updateButton) {
			ModManager.debugLogger.writeMessage("User has accepted the update");
			updateButton.setEnabled(false);
			downloadPanel.setVisible(true);
			pack();
			DownloadTask task = new DownloadTask(ModManager.getTempDir());
			task.addPropertyChangeListener(this);
			task.execute();
		}
	}

	/**
	 * Shuts down Mod Manager and runs the update script
	 */
	public void runUpdateScript() {
		ModManager.debugLogger.writeMessage("Running external update command.");
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", ModManager.getTempDir() + "updater.cmd" };
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.start();
			ModManager.debugLogger.writeMessage("Upgrading JRE.");
			ModManager.MOD_MANAGER_UPDATE_READY = true; //do not delete temp
			System.exit(0);
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("FAILED TO RUN AUTO UPDATER:", e);
			JOptionPane.showMessageDialog(UpdateJREAvailableWindow.this, "Mod Manager had a critical exception attempting to run the updater.\nPlease report this to FemShep.",
					"Updating Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Builds the update script (.cmd) to run when swapping files.
	 * 
	 * @return True if created, false otherwise.
	 */
	private boolean buildUpdateScript() {
		StringBuilder sb = new StringBuilder();
		sb.append("::Update script for Mod Manager JRE");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("setlocal");
		sb.append("\r\n");
		sb.append("echo Current directory: %CD%");
		sb.append("\r\n");
		sb.append("pushd data\\temp");
		sb.append("\r\n");
		sb.append("::Wait for 2 seconds so the JVM fully exits.");
		sb.append("\r\n");
		sb.append("PING 1.1.1.1 -n 1 -w 2000 >NUL");
		sb.append("\r\n");
		sb.append("rmdir /S /Q \"" + System.getProperty("user.dir") + "\\data\\jre-x64\"");
		sb.append("\r\n");
		sb.append("mkdir \"" + ModManager.getTempDir() + "NewVersion\"");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Extract update");
		sb.append("\r\n\"");
		sb.append(ModManager.get7zExePath());
		sb.append("\" -y x \"" + ModManager.getTempDir() + "JRE.7z\" -o\"" + ModManager.getTempDir() + "NewJREVersion\"");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("set MODMAN=%errorlevel%");
		sb.append("\r\n");
		sb.append("if %MODMAN% EQU 0 (");
		sb.append("\r\n");
		sb.append("    color 0A");
		sb.append("\r\n");
		sb.append("    echo Mod Manager JRE extracted successfully.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MODMAN% EQU 1 (");
		sb.append("\r\n");
		sb.append("    color 06");
		sb.append("\r\n");
		sb.append("    echo Mod Manager JRE extracted with warnings.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MODMAN% GEQ 2 (");
		sb.append("\r\n");
		sb.append("    color 0C");
		sb.append("\r\n");
		sb.append("    echo Mod Manager JRE did not extract succesfully. Please report this to FemShep.");
		sb.append("\r\n");
		sb.append("    pause");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("::Update the files");
		sb.append("\r\n");
		sb.append("xcopy /Y /S \"" + ModManager.getTempDir() + "NewJREVersion\" \"" + System.getProperty("user.dir") + "\"");
		sb.append("\r\n");

		sb.append("::Cleanup");
		sb.append("\r\n");
		sb.append("del /Q \"" + ModManager.getTempDir() + "JRE.7z\"");
		sb.append("\r\n");
		sb.append("rmdir /S /Q \"" + ModManager.getTempDir() + "NewJREVersion\"");
		sb.append("\r\n");
		sb.append("::Run Mod Manager");
		sb.append("\r\n");
		sb.append("popd");
		sb.append("\r\n");
		//sb.append("pause");
		sb.append("\r\n");
		sb.append("ME3CMM.exe --jre-update-from ");
		sb.append(System.getProperty("java.version"));
		sb.append(" " + (ModManager.isUsingBundledJRE() ? "bundled" : "system"));
		sb.append("\r\n");
		sb.append("endlocal");
		sb.append("\r\n");
		sb.append("call :deleteSelf&exit /b");
		sb.append("\r\n");
		sb.append(":deleteSelf");
		sb.append("\r\n");
		sb.append("start /b \"\" cmd /c del \"%~f0\"&exit /b");

		//sb.append("pause");
		//sb.append("exit");
		try {
			String updatePath = new File(ModManager.getTempDir() + "updater.cmd").getAbsolutePath();
			Files.write(Paths.get(updatePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("Couldn't generate the update script. Must abort.");
			JOptionPane.showMessageDialog(UpdateJREAvailableWindow.this, "Error building update script: " + e.getClass() + "\nCannot continue.", "Updater Error",
					JOptionPane.ERROR_MESSAGE);
			error = true;
			e.printStackTrace();
			dispose();
			return false;
		}
		return true;
	}
}
