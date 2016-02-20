package com.me3tweaks.modmanager.modupdater;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class ModUpdateWindow extends JDialog implements PropertyChangeListener {
	String downloadSource;
	boolean error = false;
	JButton cancelButton;
	JLabel statusLabel;
	JProgressBar downloadProgress;
	private UpdatePackage upackage;

	public ModUpdateWindow(UpdatePackage upackage) {
		this.upackage = upackage;
		if (ModManager.IS_DEBUG) {
			downloadSource = "http://webdev-mgamerz.c9.io/mods/updates/";
		} else {
			downloadSource = "http://me3tweaks.com/mods/updates/";
		}
		setupWindow();
	}

	/**
	 * Starts a single-mod update process
	 * 
	 * @param frame
	 */
	public void startUpdate(JFrame frame) {
		if (upackage.isModmakerupdate()) {
			//get default language
			//set combobox from settings
			new ModMakerCompilerWindow(Integer.toString(upackage.getMod().getModMakerCode()), ModMakerEntryWindow.getDefaultLanguages());
		} else {
			MultithreadDownloadTask task = new MultithreadDownloadTask(upackage);
			task.addPropertyChangeListener(this);
			task.execute();
			setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
			setVisible(true); //EDT will stall until this modal window closes
			new ModManagerWindow(false);
		}
	}

	/**
	 * Starts a multi-mod update process (does not display modals)
	 * 
	 * @param amuw
	 */
	public boolean startAllModsUpdate(AllModsUpdateWindow amuw) {
		if (upackage.isModmakerupdate()) {
			//validate biogamedir (TLK)
			if (!ModManagerWindow.validateBIOGameDir()) {
				ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.setText("ModMaker mods not updatable, invalid BIOGame directory");
				return false;
			}

			//get default language
			//set combobox from settings

			statusLabel.setText("Upgrading via ModMaker Compiler");
			ModMakerCompilerWindow mcw = new ModMakerCompilerWindow(upackage.getMod(), ModMakerEntryWindow.getDefaultLanguages());
			while (mcw.isShowing()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			DownloadTask task = new DownloadTask(upackage, amuw);
			task.addPropertyChangeListener(this);
			task.execute();
			setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
			setVisible(true);
			return task.wasSuccessful();
			
		}
		return true;
	}

	private void setupWindow() {
		this.setTitle("Updating mod");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(300, 90));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		this.pack();

		JPanel panel = new JPanel(new BorderLayout());

		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);

		statusLabel = new JLabel("0/0 files downloaded");

		panel.add(new JLabel(upackage.getMod().getModName() + " is updating..."), BorderLayout.NORTH);
		panel.add(downloadProgress, BorderLayout.CENTER);
		panel.add(statusLabel, BorderLayout.SOUTH);
		// updatePanel.add(actionPanel);
		// updatePanel.add(sizeLabel);

		// aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);
	}

	void setStatusText(String text) {
		statusLabel.setText(text);
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
	class DownloadTask extends SwingWorker<Void, Object> {
		private static final int BUFFER_SIZE = 4096;
		private String saveDirectory;
		private UpdatePackage upackage;
		private int numFilesToDownload;
		private int numProcessed;
		private long bytesDownloaded;
		private long totalBytes;
		private AllModsUpdateWindow amuw;

		public DownloadTask(UpdatePackage upackage) {
			String modpath = upackage.getMod().getModPath();
			String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
			this.saveDirectory = "update" + File.separator + updateFolder;
			this.upackage = upackage;
			numFilesToDownload = upackage.getFilesToDownload().size();
			for (ManifestModFile mf : upackage.getFilesToDownload()) {
				totalBytes += mf.getFilesize();
			}
			statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
			ModManager.debugLogger.writeMessage("Created a download task");
		}

		public boolean wasSuccessful() {
			return !error;
		}

		public DownloadTask(UpdatePackage upackage, AllModsUpdateWindow amuw) {
			String modpath = upackage.getMod().getModPath();
			String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
			this.saveDirectory = ModManager.getTempDir() + updateFolder;
			this.upackage = upackage;
			this.amuw = amuw;
			numFilesToDownload = upackage.getFilesToDownload().size();
			for (ManifestModFile mf : upackage.getFilesToDownload()) {
				totalBytes += mf.getFilesize();
			}
			statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
			ModManager.debugLogger.writeMessage("Created an all mods download task");
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			// Iterate through files to download and put them in the update
			// folder
			ModManager.debugLogger.writeMessage("Downloading update from server source: " + downloadSource);
			try {
				HTTPDownloadUtil util = new HTTPDownloadUtil();
				for (ManifestModFile mf : upackage.getFilesToDownload()) {
					String saveFilePath = saveDirectory + File.separator + mf.getRelativePath();
					File saveFile = new File(saveFilePath);
					new File(saveFile.getParent()).mkdirs();
					String link = downloadSource + upackage.getServerFolderName() + "/" + mf.getRelativePath();
					ModManager.debugLogger.writeMessage("Downloading file: " + link);

					util.downloadFile(link);
					InputStream inputStream = util.getInputStream();
					// opens an output stream to save into file
					FileOutputStream outputStream = new FileOutputStream(saveFilePath);

					byte[] buffer = new byte[BUFFER_SIZE];
					int bytesRead = -1;
					int percentCompleted = 0;

					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
						bytesDownloaded += bytesRead;
						percentCompleted = (int) (bytesDownloaded * 100 / totalBytes);
						//						percentCompleted = (int) (bytesDownloaded * 100 / totalBytes);

						System.out.println(bytesDownloaded * 100 +"/"+ totalBytes+" = "+ percentCompleted);
						percentCompleted = Math.min(percentCompleted, 100);
						setProgress(percentCompleted);
					}

					outputStream.close();
					publish(++numProcessed);
				}
				util.disconnect();
				executeUpdate();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(ModUpdateWindow.this, "Error downloading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				ModManager.debugLogger.writeErrorWithException("IOException while updating mod.", ex);
				setProgress(0);
				error = true;
				cancel(true);
			}
			return null;
		}

		@Override
		public void process(List<Object> chunks) {
			setStatusText(numProcessed + "/" + numFilesToDownload + " files downloaded");
		}

		public void executeUpdate() {
			ModManager.debugLogger.writeMessage("Applying downloaded update " + saveDirectory + " => " + upackage.getMod().getModPath());
			File updateDirectory = new File(saveDirectory);
			try {
				FileUtils.copyDirectory(updateDirectory, new File(upackage.getMod().getModPath()));
				ModManager.debugLogger.writeMessage("Installed new files");
			} catch (IOException e) {
				ModManager.debugLogger.writeError("Unable to copy update directory over the source");
				ModManager.debugLogger.writeException(e);
			}

			for (String str : upackage.getFilesToDelete()) {
				ModManager.debugLogger.writeMessage("Deleting unused file: " + str);
				File file = new File(str);
				file.delete();
			}
			ModManager.debugLogger.writeMessage("Update applied, cleaning up");

			try {
				FileUtils.deleteDirectory(updateDirectory);
			} catch (IOException e) {
				ModManager.debugLogger.writeError("Unable to delete update directory");
				ModManager.debugLogger.writeException(e);
			}

			ModManager.debugLogger.writeMessage("Mod cleaned up, install finished");
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			// TODO: Install update through the update script
			try {
				get();
			} catch (ExecutionException | InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("UPDATE FAILED:", e);
				error = true;
			}
			
			dispose();
			if (amuw == null && error != true) {
				JOptionPane.showMessageDialog(null, upackage.getMod().getModName() + " has been successfully updated.\nMod Manager will now reload mods.");
			}
		}
	}

	/**
	 * Execute file download in a background thread and update the progress.
	 * 
	 * @author www.codejava.net
	 * 
	 */
	class MultithreadDownloadTask extends SwingWorker<Void, Object> {
		private static final int BUFFER_SIZE = 4096;
		private String saveDirectory;
		private UpdatePackage upackage;
		private int numFilesToDownload;
		private int numProcessed; //may have race condition
		private long bytesDownloaded; //may have race condition
		private long totalBytes;
		private AllModsUpdateWindow amuw;

		public MultithreadDownloadTask(UpdatePackage upackage) {
			String modpath = upackage.getMod().getModPath();
			String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
			this.saveDirectory = "update" + File.separator + updateFolder;
			this.upackage = upackage;
			numFilesToDownload = upackage.getFilesToDownload().size();
			upackage.sortByLargestFirst();
			for (ManifestModFile mf : upackage.getFilesToDownload()) {
				totalBytes += mf.getFilesize();
			}
			statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
			ModManager.debugLogger.writeMessage("Created a download task");
		}

		public MultithreadDownloadTask(UpdatePackage upackage, AllModsUpdateWindow amuw) {
			String modpath = upackage.getMod().getModPath();
			String updateFolder = ResourceUtils.getRelativePath(modpath, ModManager.getModsDir(), File.separator);
			MultithreadDownloadTask.this.saveDirectory = ModManager.getTempDir() + updateFolder;
			MultithreadDownloadTask.this.upackage = upackage;
			MultithreadDownloadTask.this.amuw = amuw;
			numFilesToDownload = upackage.getFilesToDownload().size();
			for (ManifestModFile mf : upackage.getFilesToDownload()) {
				totalBytes += mf.getFilesize();
			}
			statusLabel.setText("0/" + upackage.getFilesToDownload().size() + " files downloaded");
			ModManager.debugLogger.writeMessage("Created an all mods download task");
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			ModManager.debugLogger.writeMessage("Downloading update from server source: " + downloadSource);
			ExecutorService downloadExecutor = Executors.newFixedThreadPool(4);
			ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
			for (ManifestModFile mf : upackage.getFilesToDownload()) {
				DownloadTask dt = new DownloadTask(mf.getRelativePath(), downloadSource + upackage.getServerFolderName() + "/" + mf.getRelativePath());
				futures.add(downloadExecutor.submit(dt));
			}
			downloadExecutor.shutdown();
			downloadExecutor.awaitTermination(60, TimeUnit.MINUTES);
			for (Future<Boolean> f : futures) {
				try {
					Boolean result = f.get();
					if (result == false) {
						error = true;
						ModManager.debugLogger.writeError("update failed.");
					}
				} catch (ExecutionException e) {
					ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE DOWNLOADING FILE (AFTER EXECUTOR FINISHED): ", e);
				}
			}

			if (!error) {
				executeUpdate();
			}
			return null;
		}

		class DownloadTask implements Callable<Boolean> {
			private String url;
			private String relativePath;

			public DownloadTask(String relativePath, String url) {
				this.url = url;
				this.relativePath = relativePath;
			}

			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				try {
					HTTPDownloadUtil util = new HTTPDownloadUtil();
					String saveFilePath = saveDirectory + File.separator + relativePath;
					File saveFile = new File(saveFilePath);
					new File(saveFile.getParent()).mkdirs();
					ModManager.debugLogger.writeMessage("Downloading file: " + url);

					util.downloadFile(url);
					InputStream inputStream = util.getInputStream();
					// opens an output stream to save into file
					FileOutputStream outputStream = new FileOutputStream(saveFilePath);

					byte[] buffer = new byte[BUFFER_SIZE];
					int bytesRead = -1;
					int percentCompleted = 0;

					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
						bytesDownloaded += bytesRead;
						percentCompleted = (int) (bytesDownloaded * 100 / totalBytes);
						setProgress(percentCompleted);
					}
					outputStream.close();
					publish(++numProcessed);
					return true;
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Failed to download file " + relativePath + ":", e);
					return false;
				}
			}
		}

		@Override
		public void process(List<Object> chunks) {
			setStatusText(numProcessed + "/" + numFilesToDownload + " files downloaded");
		}

		public void executeUpdate() {
			ModManager.debugLogger.writeMessage("Applying downloaded update " + saveDirectory + " => " + upackage.getMod().getModPath());
			File updateDirectory = new File(saveDirectory);
			try {
				FileUtils.copyDirectory(updateDirectory, new File(upackage.getMod().getModPath()));
				ModManager.debugLogger.writeMessage("Installed new files");
			} catch (IOException e) {
				ModManager.debugLogger.writeError("Unable to copy update directory over the source");
				ModManager.debugLogger.writeException(e);
			}

			for (String str : upackage.getFilesToDelete()) {
				ModManager.debugLogger.writeMessage("Deleting unused file: " + str);
				File file = new File(str);
				file.delete();
			}
			ModManager.debugLogger.writeMessage("Update applied, cleaning up");

			try {
				FileUtils.deleteDirectory(updateDirectory);
			} catch (IOException e) {
				ModManager.debugLogger.writeError("Unable to delete update directory");
				ModManager.debugLogger.writeException(e);
			}

			ModManager.debugLogger.writeMessage("Mod cleaned up, install finished");
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			// TODO: Install update through the update script
			dispose();
			if (amuw == null && !error) {
				JOptionPane.showMessageDialog(null, upackage.getMod().getModName() + " has been successfully updated.\nMod Manager will now reload mods.", "Update successful", JOptionPane.INFORMATION_MESSAGE);
			} else if (amuw == null) {
				JOptionPane.showMessageDialog(null, upackage.getMod().getModName() + " failed to update. The debugging log will have more information.", "Updated failed", JOptionPane.ERROR_MESSAGE);
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
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}

				// output for debugging purpose only
				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("Content-Length = " + contentLength);
				System.out.println("fileName = " + fileName);

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
