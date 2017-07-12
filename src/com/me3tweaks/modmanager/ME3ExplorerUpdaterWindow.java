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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class ME3ExplorerUpdaterWindow extends JDialog implements PropertyChangeListener {
	// String downloadLink, updateScriptLink;
	boolean error = false;
	String version;
	JLabel introLabel, statusLabel;
	JProgressBar downloadProgress;
	private boolean startAfterFinish;

	public ME3ExplorerUpdaterWindow(String versionStr, boolean startAfterFinish) {
		this.startAfterFinish = startAfterFinish;
		this.version = versionStr;
		setupWindow();
		DownloadTask task = new DownloadTask(ModManager.getTempDir());
		task.addPropertyChangeListener(this);
		ModManager.debugLogger.writeMessage("Downloading ME3Explorer " + versionStr);
		task.execute();
		setVisible(true);
	}

	private void setupWindow() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);
		setTitle("ME3Explorer Update");
		setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel(new BorderLayout());
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("Mod Manager is downloading a new copy of ME3Explorer.");
		statusLabel = new JLabel("<html>Downloading ME3Explorer from GitHub...<br>ME3Explorer is not part of Mod Manager.</html>");

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
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
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

		public DownloadTask(String saveDirectory) {
			this.saveDirectory = saveDirectory;
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			// Download the update
			try {

				// Download update
				HTTPDownloadUtil util = new HTTPDownloadUtil();
				util.downloadFile(ModManager.LATEST_ME3EXPLORER_URL);

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
				publish(new ThreadCommand("SET_PROGRESSBAR_INDETERMINATE"));
				publish(new ThreadCommand("SET_STATUS_TEXT", "<html>Extracting ME3Explorer...<br>ME3Explorer is not part of Mod Manager.</html>"));

				FileUtils.deleteQuietly(new File(ModManager.getME3ExplorerEXEDirectory()));
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.get7zExePath());
				commandBuilder.add("-y"); // overwrite
				commandBuilder.add("x"); // extract
				commandBuilder.add(saveFilePath);// 7z file
				commandBuilder.add("-o" + ResourceUtils.normalizeFilePath(ModManager.getDataDir(), true)); // extraction
																											// path
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("Extracting ME3Explorer...");
				ProcessBuilder pb = new ProcessBuilder(command);
				int returncode = ModManager.runProcess(pb).getReturnCode();
				if (returncode == 0) {
					File configFile = new File(ModManager.getME3ExplorerEXEDirectory() + "ME3Explorer.exe.config");
					if (configFile.exists()) {
						disableME3ExplorerUnpackStartup(configFile.getAbsolutePath());
					}
				}
			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("Error downloading ME3Explorer: ", ex);
				setProgress(0);
				error = true;
				publish(new ThreadCommand("DOWNLOAD_ERROR", "Error downloading file:\n" + ex.getMessage()));
				cancel(true);
			}
			return null;
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
					JOptionPane.showMessageDialog(ME3ExplorerUpdaterWindow.this, tc.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updated ME3Explorer");
			dispose();
			if (startAfterFinish) {
				ProcessBuilder pb = new ProcessBuilder(ModManager.getME3ExplorerEXEDirectory() + "ME3Explorer.exe");
				ModManager.runProcessDetached(pb);
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
				throw new IOException("Error downloading file:\nServer replied with HTTP code: " + responseCode);
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

	/**
	 * Modifies the ME3Explorer configuration file to prevent it from asking on
	 * startup if the game should be unpacked.
	 * 
	 * @param configfilePath
	 *            Path to the configuration XML file.
	 * @return
	 */
	public boolean disableME3ExplorerUnpackStartup(String configfilePath) {
		// configuration/userSettings/ME3Explorer.Properties.Settings/setting
		// DisableDLCCheckOnStart = true
		try {

			// Create a document by parsing a XML file
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new File(configfilePath));

			// Get a node using XPath
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/configuration/userSettings/ME3Explorer.Properties.Settings/setting[@name='DisableDLCCheckOnStart']/value";
			Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);

			if (node != null) {
				// Set the node content
				node.setTextContent("True");

				// Write changes to a file
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(document), new StreamResult(new File(configfilePath)));
			}
		} catch (Exception e) {
			// Handle exception
		}
		return false;
	}
}
