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
import java.util.ArrayList;
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

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.json.simple.JSONObject;

import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class UpdateAvailableWindow extends JDialog implements ActionListener, PropertyChangeListener {
	String downloadLink, downloadLink2, updateScriptLink, manualLink, changelogLink;
	boolean error = false;
	String version;
	String buildHash;
	long build;
	JLabel introLabel, versionsLabel, changelogLabel, sizeLabel;
	JButton updateButton, notNowButton, nextUpdateButton, manualDownloadButton;
	JSONObject updateInfo;
	JProgressBar downloadProgress;
	private JButton changelogButton;
	private JPanel downloadPanel;

	public UpdateAvailableWindow(JSONObject updateInfo) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening update available window");
		this.updateInfo = updateInfo;
		build = (long) updateInfo.get("latest_build_number");
		version = (String) updateInfo.get("latest_version_hr");
		downloadLink = (String) updateInfo.get("download_link");
		downloadLink2 = (String) updateInfo.get("download_link2");
		manualLink = (String) updateInfo.get("manual_link");
		changelogLink = (String) updateInfo.get("changelog_link");
		buildHash = (String) updateInfo.get("build_md5");

		if (manualLink == null) {
			manualLink = downloadLink2 == null ? downloadLink : downloadLink2;
		}
		ModManager.debugLogger.writeMessage("Update info:");
		ModManager.debugLogger.writeMessage(" - Version: " + version);
		ModManager.debugLogger.writeMessage(" - Build: " + build);
		ModManager.debugLogger.writeMessage(" - Primary Download: " + downloadLink2);
		ModManager.debugLogger.writeMessage(" - Fallback Download: " + downloadLink);
		ModManager.debugLogger.writeMessage(" - Manual Download: " + manualLink);
		ModManager.debugLogger.writeMessage(" - Changelog: " + changelogLink);
		ModManager.debugLogger.writeMessage(" - " + version);
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Update Available");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setIconImages(ModManager.ICONS);

		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel();
		String latest_version_hr = (String) updateInfo.get("latest_version_hr");
		long latest_build_number = (long) updateInfo.get("latest_build_number");

		//calculate local hash
		String buildHash = (String) updateInfo.get("build_md5");
		boolean hashMismatch = false;
		try {
			String currentHash = MD5Checksum.getMD5Checksum("ME3CMM.exe");
			if (buildHash != null && !buildHash.equals("") && !currentHash.equals(buildHash)) {
				//hash mismatch
				hashMismatch = true;
			}
		} catch (Exception e) {
			//ModManager.debugLogger.writeErrorWithException("Unable to hash ME3CMM.exe:", e1);
		}

		if (hashMismatch && latest_build_number == ModManager.BUILD_NUMBER) {
			introLabel.setText("A minor update for Mod Manager is available.");
		} else {
			introLabel.setText("An update for Mod Manager is available from ME3Tweaks.");
		}

		versionsLabel = new JLabel("<html>Local Version: " + ModManager.VERSION + " (Build " + ModManager.BUILD_NUMBER + ")<br>" + "Latest Version: " + latest_version_hr
				+ " (Build " + latest_build_number + ")</html>");

		String release_notes = (String) updateInfo.get("release_notes");
		changelogLabel = new JLabel("<html><div style=\"width:270px;\">" + release_notes + "</div></html>");
		updateButton = new JButton("Install Update");
		updateButton.addActionListener(this);
		notNowButton = new JButton("Not now");
		notNowButton.addActionListener(this);
		nextUpdateButton = new JButton("Skip this build");
		nextUpdateButton.addActionListener(this);
		manualDownloadButton = new JButton("Manual Download");
		manualDownloadButton.addActionListener(this);
		changelogButton = new JButton("View full changelog");
		changelogButton.addActionListener(this);

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
		if (changelogLink != null && !changelogLink.equals("")) {
			changeLogPanel.add(changelogButton);
		}

		updatePanel.add(changeLogPanel);
		updatePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(updateButton);
		actionPanel.add(manualDownloadButton);
		actionPanel.add(nextUpdateButton);
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
				try {
					publish(new ThreadCommand("UPDATE_STATUS", "Downloading update..."));
					util.downloadFile(downloadLink2 == null ? downloadLink : downloadLink2);
				} catch (FileNotFoundException e) {
					if (downloadLink2 != null) {
						//try downloadLink
						publish(new ThreadCommand("UPDATE_STATUS", "Downloading update via fallback..."));
						ModManager.debugLogger.writeMessage("Primary download link failed (" + downloadLink2 + "), falling back to ME3Tweaks");
						util.downloadFile(downloadLink);
					}
				}
				// set file information on the GUI
				publish(new ThreadCommand("UPDATE_STATUS", "Downloading update..."));

				String saveFilePath = saveDirectory + File.separator + "ME3CMM.7z";

				InputStream inputStream = util.getInputStream();
				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = -1;
				totalBytesRead = 0;
				int percentCompleted = 0;
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
				ArrayList<Pair<String,String>> telemetryData = new ArrayList<Pair<String,String>>();
				telemetryData.add(new ImmutablePair<>("telemetrykey","MODMANAGERUPDATE_ATTEMPT"));
				telemetryData.add(new ImmutablePair<>("startbuild",Long.toString(ModManager.BUILD_NUMBER)));
				telemetryData.add(new ImmutablePair<>("destbuild",Long.toString(build)));
				ME3TweaksUtils.SubmitTelemetry(telemetryData,true);
				while (true){
					//do nothing
				}
			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("ERROR DOWNLOADING UPDATE: ", ex);
				JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error downloading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
			URL url;
			try {
				url = ME3TweaksUtils.convertToURLEscapingIllegalCharacters(fileURL);
			} catch (Exception e) {
				throw new IOException("URL could not be encoded for downloading: " + fileURL);
			}
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
			manualDownloadButton.setEnabled(false);
			nextUpdateButton.setEnabled(false);
			downloadPanel.setVisible(true);
			pack();
			DownloadTask task = new DownloadTask(ModManager.getTempDir());
			task.addPropertyChangeListener(this);
			task.execute();
		} else if (e.getSource() == nextUpdateButton) {
			ModManager.debugLogger.writeMessage("User is skipping this build");
			JOptionPane.showMessageDialog(this, "Outdated builds of Mod Manager are not supported.\nYou can make this update visible again in the options window.",
					"Unsupported Warning", JOptionPane.WARNING_MESSAGE);
			//write to ini that we don't want update
			Wini ini;
			try {
				File settings = new File(ModManager.SETTINGS_FILENAME);
				if (!settings.exists())
					settings.createNewFile();
				ini = new Wini(settings);
				ini.put("Settings", "nextupdatedialogbuild", build + 1);
				ModManager.debugLogger.writeMessage("Ignoring current update, will show again when build " + (build + 1) + " is released.");
				ini.store();
			} catch (InvalidFileFormatException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", ex);
			}
			dispose();
		} else if (e.getSource() == manualDownloadButton) {
			try {
				ResourceUtils.openWebpage(new URI(manualLink));
				dispose();
			} catch (URISyntaxException e1) {
				ModManager.debugLogger.writeException(e1);
			}
			dispose();
		} else if (e.getSource() == changelogButton) {
			try {
				ResourceUtils.openWebpage(new URI(changelogLink));
			} catch (URISyntaxException e1) {
				ModManager.debugLogger.writeException(e1);
			}
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
			ModManager.debugLogger.writeMessage("Upgrading to build " + build + ", shutting down.");
			ModManager.MOD_MANAGER_UPDATE_READY = true; //do not delete temp
			System.exit(0);
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("FAILED TO RUN AUTO UPDATER:", e);
			JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Mod Manager had a critical exception attempting to run the updater.\nPlease report this to Mgamerz.",
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
		sb.append("::Update script for Mod Manager " + ModManager.VERSION + " (Build " + build + ")");
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
		sb.append("mkdir " + ModManager.getTempDir() + "NewVersion");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("::Extract update");
		sb.append("\r\n\"");
		sb.append(ModManager.get7zExePath());
		sb.append("\" -y x \"" + ModManager.getTempDir() + "ME3CMM.7z\" -o\"" + ModManager.getTempDir() + "NewVersion\"");
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("set MODMAN=%errorlevel%");
		sb.append("\r\n");
		sb.append("if %MODMAN% EQU 0 (");
		sb.append("\r\n");
		sb.append("    color 0A");
		sb.append("\r\n");
		sb.append("    echo Mod Manager extracted successfully.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MODMAN% EQU 1 (");
		sb.append("\r\n");
		sb.append("    color 06");
		sb.append("\r\n");
		sb.append("    echo Mod Manager extracted with warnings.");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");

		sb.append("if %MODMAN% GEQ 2 (");
		sb.append("\r\n");
		sb.append("    color 0C");
		sb.append("\r\n");
		sb.append("    echo Mod Manager did not extract succesfully. Please report this to Mgamerz.");
		sb.append("\r\n");
		sb.append("    pause");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("::Check for build-in update script");
		sb.append("\r\n");
		sb.append("if exist \"" + ModManager.getTempDir() + "NewVersion\\update.cmd\" (");
		sb.append("\r\n");
		sb.append("CALL \"" + ModManager.getTempDir() + "NewVersion\\update.cmd\"");
		sb.append("\r\n");
		sb.append(")");
		sb.append("\r\n");
		sb.append("::Update the files");
		sb.append("\r\n");
		sb.append("xcopy /Y /S \"" + ModManager.getTempDir() + "NewVersion\" \"" + System.getProperty("user.dir") + "\"");
		sb.append("\r\n");

		sb.append("::Cleanup");
		sb.append("\r\n");
		sb.append("del /Q \"" + ModManager.getTempDir() + "ME3CMM.7z\"");
		sb.append("\r\n");
		sb.append("rmdir /S /Q \"" + ModManager.getTempDir() + "NewVersion\"");
		sb.append("\r\n");
		sb.append("::Run Mod Manager");
		sb.append("\r\n");
		sb.append("popd");
		sb.append("\r\n");
		sb.append("echo Starting Mod Manager...");
		sb.append("\r\n");
		sb.append("\r\n");
		//sb.append("pause");
		sb.append("\r\n");
		if (build == ModManager.BUILD_NUMBER) {
			sb.append("ME3CMM.exe --minor-update-from ");
		} else {
			sb.append("ME3CMM.exe --update-from ");
		}
		sb.append(ModManager.BUILD_NUMBER);
		if (build == ModManager.BUILD_NUMBER) {
			sb.append(" ");
			sb.append(buildHash);
		}
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
			ModManager.debugLogger.writeErrorWithException("Couldn't generate the update script. Must abort.",e);
			JOptionPane.showMessageDialog(UpdateAvailableWindow.this, "Error building update script: " + e.getClass() + "\nCannot continue.", "Updater Error",
					JOptionPane.ERROR_MESSAGE);
			error = true;
			dispose();
			return false;
		}
		return true;
	}
}
