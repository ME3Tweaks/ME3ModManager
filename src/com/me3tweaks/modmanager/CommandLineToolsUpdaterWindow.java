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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;

@SuppressWarnings("serial")
public class CommandLineToolsUpdaterWindow extends JDialog implements PropertyChangeListener {
	//String downloadLink, updateScriptLink;
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
		introLabel = new JLabel("This version of Mod Manager requires Command Line Tools " + ModManager.getCommandLineToolsRequiredVersion() + ".");
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
	class DownloadTask extends SwingWorker<Boolean, ThreadCommand> {
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
		protected Boolean doInBackground() throws Exception {
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

				publish(new ThreadCommand("SET_PROGRESSBAR_INDETERMINATE"));
				publish(new ThreadCommand("SET_STATUS_TEXT", "<html>Extracting Command Line Tools...</html>"));

				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getToolsDir() + "7z.exe");
				commandBuilder.add("-y"); // overwrite
				commandBuilder.add("x"); // extract
				commandBuilder.add(saveFilePath);// 7z file
				commandBuilder.add("-o" + ModManager.getCommandLineToolsDir()); // extraction path
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("Extracting Command Line Tools...");
				ProcessBuilder pb = new ProcessBuilder(command);
				ProcessResult pr = ModManager.runProcess(pb);
				FileUtils.deleteQuietly(new File(saveFilePath));
				boolean removeOutdatedGUITransplanter = FileUtils.deleteQuietly(new File(ModManager.getToolsDir() + "GUITransplanter"));
				if (removeOutdatedGUITransplanter) {
					ModManager.debugLogger.writeMessage("Removed outdated GUITransplanter directory.");
				}
				return pr.getReturnCode() == 0;
			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("Error downloading Command Line Tools: ", ex);
				setProgress(0);
				publish(new ThreadCommand("DOWNLOAD_ERROR", "Error downloading file:\n" + ex.getMessage()));
				return false;
			}
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "SET_PROGRESSBAR_INDETERMINATE":
					downloadProgress.setIndeterminate(true);
					break;
				case "SET_STATUS_TEXT":
					statusLabel.setText(tc.getMessage());
					break;
				case "DOWNLOAD_ERROR":
					JOptionPane.showMessageDialog(CommandLineToolsUpdaterWindow.this, tc.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
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
			try {
				boolean success = get();
				if (success) {
					ModManager.debugLogger.writeMessage("Command line tools downloaded.");
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Command Line Tools updated");
				} else {
					ModManager.debugLogger.writeError("Command line tools failed to download! Mod Manager will not work properly.");
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Command Line Tools failed to download");
					JOptionPane.showMessageDialog(CommandLineToolsUpdaterWindow.this,
							"A required update for Mod Manager Command Line Tools failed to download.\nMod Manager will not work properly without this update.", "Critical error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (InterruptedException | ExecutionException e) {
				ModManager.debugLogger.writeError("Command line tools failed to download! Mod Manager will not work properly.");
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Command Line Tools failed to download");
				JOptionPane.showMessageDialog(CommandLineToolsUpdaterWindow.this,
						"A required update for Mod Manager Command Line Tools failed to download.\nMod Manager will not work properly without this update.", "Critical error",
						JOptionPane.ERROR_MESSAGE);
			}
			dispose();
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
				throw new IOException("Server replied with HTTP code: " + responseCode);

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
}
