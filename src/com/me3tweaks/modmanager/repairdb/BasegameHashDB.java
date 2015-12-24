package com.me3tweaks.modmanager.repairdb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.utilities.DebugLogger;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class BasegameHashDB extends JFrame implements ActionListener {
	static boolean isRunningAsMain = false;
	private String basePath;
	JLabel infoLabel;
	JTextArea consoleArea;
	String consoleQueue[];
	String currentText;
	JProgressBar progressBar;
	Connection dbConnection;
	JButton startMap;
	HashmapWorker hmw;
	boolean showGUI = false;
	boolean databaseLoaded = false;
	JFrame callingWindow;
	private static Statement stmt = null;
	private boolean noDB = true;

	public static void main(String[] args) throws SQLException {
		isRunningAsMain = true;
		//init the debug logger.
		ModManager.debugLogger = new DebugLogger();
		ModManager.debugLogger.initialize();
		ModManager.logging = true;
		new BasegameHashDB(null, "I:/Origin Games/Mass Effect 3/", true); //debug only
	}

	public BasegameHashDB(JFrame callingWindow, String basePath, boolean showGUI) throws SQLException{
		this.callingWindow = callingWindow;
		this.showGUI = showGUI;
		this.basePath = basePath;
		if (showGUI) {
			setupWindow();
			new DatabaseLoader().execute();
			setVisible(true);
		} else {
			loadDatabase();
		}
	}

	public void shutdownDB() {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		// TODO Auto-generated method stub
		infoLabel = new JLabel("<html>Loading repair database...</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		startMap = new JButton("Loading database");
		startMap.addActionListener(this);
		startMap.setEnabled(false);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		JPanel borderPanel = new JPanel(new BorderLayout()); //hack for border
		borderPanel.add(progressBar, BorderLayout.CENTER);
		borderPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.add(startMap, BorderLayout.EAST);
		progressPanel.add(borderPanel, BorderLayout.CENTER);
		northPanel.add(progressPanel, BorderLayout.CENTER);
		northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rootPanel.add(northPanel, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		consoleArea.setText("The game repair database keeps track of your preferred game configuration, so when restoring files you will be returned to the snapshotted state.\n"
				+ "\nCreate or update the game repair DB to make a snapshot of all file hashes and filesizes so that when you install a new mod, the file that is backed up is known to be the one you want.\n"
				+ "\nWhen restoring files, the game database checks the backed up files match the ones in the snapshot, and will show you a message if they don't."
				+ "\n\nThe game repair database only works with unpacked DLC files and basegame files, not .sfar files. Modifications done outside of Mod Manager are unsupported by FemShep, so I won't help you fix problems with non Mod Manager mods.\n"
				+ "If you choose to use non Mod Manager mods, you will need to enable the pre-install TOC option in the options page.");
		consoleArea.setEditable(false);

		rootPanel.add(consoleArea, BorderLayout.CENTER);
		getContentPane().add(rootPanel);
		this.setPreferredSize(new Dimension(425, 390));
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				shutdownDB();
				if (isRunningAsMain) {
					System.exit(0);
				}
			}
		});
		setTitle("Game Repair Database");
		this.setIconImages(ModManager.ICONS);
		pack();
		this.setLocationRelativeTo(callingWindow);
	}

	public boolean loadDatabase() throws SQLException {
		ModManager.debugLogger.writeMessage("Loading game repair database.");
		// connect method #1 - embedded driver
		File databases = new File(ModManager.getDatabaseDir());
		databases.mkdirs();
		String repairInfoURL = "jdbc:derby:data/databases/repairinfo;create=true";
		dbConnection = DriverManager.getConnection(repairInfoURL);
		if (dbConnection != null) {
			ModManager.debugLogger.writeMessage("Loaded game repair database.");
			databaseLoaded = true;
			return true;
		}

		ModManager.debugLogger.writeMessage("Game repair database failed to load.");
		databaseLoaded = false;
		return false;
	}

	/**
	 * Creates a hashmap of all files from the given directory.
	 */
	public void createFileHashmap() {
		// TODO Auto-generated method stub

		startMap.setEnabled(false);
		startMap.setText("Getting list of files...");
		ModManager.debugLogger.writeMessage("Starting repairinfo thread.");
		hmw = new HashmapWorker(progressBar);
		hmw.execute();
	}

	public void updateDB(ArrayList<File> filesToUpdate) {
		hmw = new HashmapWorker(filesToUpdate, null);
		hmw.execute();
	}

	class HashmapWorker extends SwingWorker<Void, Integer> {
		private ArrayList<File> filesToHash;
		private JProgressBar progress;
		private int numFiles;

		/**
		 * Full hashmap worker. Use to update or create the full DB.
		 * 
		 * @param progress
		 *            progressbar to use
		 */
		public HashmapWorker(JProgressBar progress) {
			ModManager.debugLogger.writeMessage("Loading HashmapWorker...");
			//this.filesToHash = basePath;
			//this.numFiles = basePath.size();
			this.progress = progress;
			if (showGUI) {
				infoLabel.setText("Preparing to create restoration map...");
			}
		}

		/**
		 * External HashMap Worker object, for updating specific entries
		 * 
		 * @param filesToUpdate
		 *            files to update
		 * @param progress
		 *            progressbar to use
		 */
		public HashmapWorker(ArrayList<File> filesToUpdate, JProgressBar progress) {
			ModManager.debugLogger.writeMessage("Loading HashmapWorker...");
			this.filesToHash = filesToUpdate;
			this.numFiles = filesToUpdate.size();
			this.progress = progress;
			if (showGUI) {
				infoLabel.setText("Preparing to create restoration map...");
			}
		}

		protected Void doInBackground() throws Exception {
			ModManager.debugLogger.writeMessage("Starting background thread for game repair hash db");

			if (filesToHash == null) {
				Iterable<File> files = FileUtils.listFiles(new File(basePath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				filesToHash = new ArrayList<File>();
				for (File file : files) {
					if (file.getAbsolutePath().toLowerCase().startsWith((ModManager.appendSlash(basePath) + "cmmbackup").toLowerCase())) {
						ModManager.debugLogger.writeError("Skipping cmmbackup file " + file);
						continue; //skip backups folder
					}
					filesToHash.add(file);
				}
			}
			this.numFiles = filesToHash.size();
			PreparedStatement insertStatement, selectStatement, updateStatement = null;
			DatabaseMetaData dbmd = dbConnection.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, "BASEGAMEFILES", null);
			if (!rs.next()) {
				//create the table.
				ModManager.debugLogger.writeMessage("Creating basegamefiles table.");
				try {
					stmt = dbConnection.createStatement();
					stmt.execute("CREATE TABLE basegamefiles (" + "filepath VARCHAR(200) PRIMARY KEY," + "hash VARCHAR(65) NOT NULL," + "filesize BIGINT NOT NULL)");
					//stmt.close();
				} catch (SQLException sqlExcept) {
					ModManager.debugLogger.writeException(sqlExcept);
				}
			}

			int filesProcessed = 0;
			String insertString = "INSERT INTO basegamefiles (filepath, hash, filesize) VALUES (UPPER(?),?,?)";
			String selectString = "SELECT * FROM BASEGAMEFILES WHERE filepath = UPPER(?)";
			String updateString = "UPDATE basegamefiles SET hash=?, filesize=? WHERE filepath=UPPER(?)";
			insertStatement = dbConnection.prepareStatement(insertString);
			selectStatement = dbConnection.prepareStatement(selectString);
			updateStatement = dbConnection.prepareStatement(updateString);
			for (File file : filesToHash) {
				if (file.getAbsolutePath().toLowerCase().endsWith(".bak") || file.getAbsolutePath().toLowerCase().endsWith(".sfar") || !file.exists()) {
					filesProcessed++;
					continue; //skip backups and sfars and files that were deleted before we started
				}
				String fileKey = ResourceUtils.getRelativePath(file.getAbsolutePath(), basePath, File.separator);
				ModManager.debugLogger.writeMessage("Cataloging " + fileKey.toUpperCase());
				//select first, to see if it's in the DB...
				selectStatement.setString(1, fileKey.toUpperCase());
				selectStatement.execute();
				ResultSet srs = selectStatement.getResultSet();
				if (!srs.next()) {
					//INSERT - ITS NOT THERE.
					ModManager.debugLogger.writeMessage("Inserting entry " + fileKey.toUpperCase());
					try {
						insertStatement.setString(1, fileKey.toUpperCase());
						insertStatement.setString(2, MD5Checksum.getMD5Checksum(file.getAbsolutePath()));
						insertStatement.setLong(3, file.length());
						insertStatement.execute();
					} catch (SQLException sqlExcept) {
						ModManager.debugLogger.writeException(sqlExcept);
					}
				} else {
					//UPDATE SINCE IT EXISTS.
					ModManager.debugLogger.writeMessage("Updating entry " + fileKey.toUpperCase());
					try {
						updateStatement.setString(3, fileKey.toUpperCase());
						updateStatement.setString(1, MD5Checksum.getMD5Checksum(file.getAbsolutePath()));
						updateStatement.setLong(2, file.length());
						updateStatement.execute();
					} catch (SQLException sqlExcept) {
						ModManager.debugLogger.writeException(sqlExcept);
					}

					//Check if file should be deleted as a backup
					File bgdir = new File(ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText()));
					String me3dir = ModManager.appendSlash(bgdir.getParent());
					// Make backup folder if it doesn't exist
					String backupfolderpath = me3dir.toString() + "cmmbackup\\";

					File backupFile = new File(backupfolderpath + fileKey);
					if (backupFile.exists()) {
						//check it dubsguy.jpg
						if (backupFile.length() != srs.getLong("filesize") || !MD5Checksum.createChecksum(backupFile.getAbsolutePath()).equals(srs.getString("hash"))) {
							//mismatch. delete it.
							ModManager.debugLogger.writeMessage("Deleting backed up file that no longer matches game file: " + fileKey);
							FileUtils.deleteQuietly(backupFile);
						}
					}
				}
				filesProcessed++;
				this.publish(filesProcessed);
			}
			ModManager.debugLogger.writeMessage("Shutting down game repair db thread");
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (showGUI) {
				if (numFiles > numCompleted.get(0)) {
					String fileName = ResourceUtils.getRelativePath(filesToHash.get(numCompleted.get(0)).getAbsolutePath(), basePath, File.separator);
					infoLabel.setText("<html>Updated file information for:<br>" + fileName + "</html>");
				}
				progress.setValue((int) (100 / (numFiles / (float) numCompleted.get(0))));
			}
		}

		protected void done() {
			// Coals decompiled
			if (showGUI) {
				progress.setValue(100);
				infoLabel.setText("<html>The repair database has been updated.</html>");
				startMap.setEnabled(true);
				startMap.setText("Database updated");
			}
			ModManager.debugLogger.writeMessage("Hashmap created.");
		}
	}

	class DatabaseLoader extends SwingWorker<Void, Void> {
		boolean dbHasLoaded = false;

		protected Void doInBackground() throws Exception {
			dbHasLoaded = loadDatabase();
			return null;
		}

		protected void done() {
			if (dbHasLoaded) {
				if (showGUI) {
					startMap.setEnabled(true);
					if (isBasegameTableCreated()) {
						noDB = false;
						infoLabel.setText("Database loaded.");
						startMap.setText("Update Database");
					} else {
						noDB = true;
						infoLabel.setText("No repair database has been created yet.");
						startMap.setText("Create Database");
					}
				}
			} else {
				if (showGUI) {
					infoLabel.setText("Database can't be loaded.");
					JOptionPane.showMessageDialog(null, "Unable to connect to database.\nDo you have multiple Mod Manager windows open?", "Database error",
							JOptionPane.ERROR_MESSAGE);
					dispose();
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == startMap) {
			if (isBasegameTableCreated()) {
				int result = JOptionPane.showConfirmDialog(BasegameHashDB.this,
						"Updating your DB will delete any backed up unpacked/basegame files\nthat no longer match their currently installed counterpart.\nUpdate the database?",
						"Update Game Repair Database", JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					createFileHashmap();
				}
			} else {
				createFileHashmap();
			}
		}
	}

	public RepairFileInfo getFileInfo(String relativePath) {
		if (databaseLoaded) {
			try {
				String selectString = "SELECT * FROM BASEGAMEFILES WHERE filepath = UPPER(?)";
				PreparedStatement stmt;
				stmt = dbConnection.prepareStatement(selectString);
				ModManager.debugLogger.writeMessage("Querying database: SELECT * FROM BASEGAMEFILES WHERE filepath = " + relativePath.toUpperCase());
				stmt.setString(1, relativePath.toUpperCase());
				stmt.execute();
				ResultSet srs = stmt.getResultSet();
				if (!srs.next()) {
					return null; //not in the db...
				} else {
					RepairFileInfo rfi = new RepairFileInfo();
					rfi.filePath = relativePath;
					rfi.filesize = srs.getLong("filesize");
					rfi.md5 = srs.getString("hash");
					return rfi;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				ModManager.debugLogger.writeErrorWithException("Error getting file info from database for " + relativePath, e);
			}
		}
		return null;
	}

	public boolean isBasegameTableCreated() {
		DatabaseMetaData dbmd;
		try {
			dbmd = dbConnection.getMetaData();
			ResultSet rs = dbmd.getTables(null, "APP", "BASEGAMEFILES", null);
			if (!rs.next()) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}
