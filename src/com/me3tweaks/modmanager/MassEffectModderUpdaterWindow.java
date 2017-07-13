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
public class MassEffectModderUpdaterWindow extends JDialog implements PropertyChangeListener {
	// String downloadLink, updateScriptLink;
	boolean error = false;
	int version;
	JLabel introLabel, statusLabel;
	JProgressBar downloadProgress;
	private boolean startAfterFinish;

	public MassEffectModderUpdaterWindow(int version, boolean startAfterFinish) {
		ModManager.debugLogger.writeMessage("Opening MassEffecTModderUpdaterWindow - Download link: " + ModManager.MASSEFFECTMODDER_DOWNLOADLINK);
		this.startAfterFinish = startAfterFinish;
		this.version = version;

		setupWindow();

		DownloadTask task = new DownloadTask(ModManager.getTempDir());
		task.addPropertyChangeListener(this);
		ModManager.debugLogger.writeMessage("Downloading Mass Effect Modder v" + version);
		task.execute();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Downloading Mass Effect Modder");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel(new BorderLayout());
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		introLabel = new JLabel("Mod Manager is downloading Mass Effect Modder.");
		statusLabel = new JLabel("<html>Downloading MEM from GitHub...<br>Mass Effect Modder is not part of Mod Manager.</html>");

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
				util.downloadFile(ModManager.MASSEFFECTMODDER_DOWNLOADLINK);

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
				publish(new ThreadCommand("SET_STATUS_TEXT", "<html>Extracting Mass Effect Modder...<br>Mass Effect Modder is not part of Mod Manager.</html>"));

				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.get7zExePath());
				commandBuilder.add("-y"); // overwrite
				commandBuilder.add("x"); // extract
				commandBuilder.add(saveFilePath);// 7z file
				commandBuilder.add("-o" + ModManager.getMEMDirectory()); // extraction path
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("Extracting Mass Effect Modder...");
				ProcessBuilder pb = new ProcessBuilder(command);
				ModManager.runProcess(pb);

			} catch (IOException ex) {
				ModManager.debugLogger.writeErrorWithException("Error downloading Mass Effect Modder: ", ex);
				publish(new ThreadCommand("DOWNLOAD_ERROR","Error downloading file:\n" + ex.getMessage()));
				setProgress(0);
				error = true;
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
					JOptionPane.showMessageDialog(MassEffectModderUpdaterWindow.this, tc.getMessage(), "Download Error", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updated Mass Effect Modder");
			dispose();
			if (startAfterFinish) {
				ProcessBuilder pb = new ProcessBuilder(ModManager.getMEMDirectory() + "MassEffectModder.exe");
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

		private String fileName = "MassEffectModderUpdate.7z";
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

				inputStream = httpConn.getInputStream();

			} else {
				throw new IOException("Server replied with HTTP code: " + responseCode);
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
}
