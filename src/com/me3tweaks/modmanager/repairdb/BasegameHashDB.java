package com.me3tweaks.modmanager.repairdb;

import java.awt.BorderLayout;
import java.awt.Dialog;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class BasegameHashDB extends JFrame implements ActionListener {
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

	public BasegameHashDB(JFrame callingWindow, String basePath, boolean showGUI) throws SQLException {
		this.callingWindow = callingWindow == null ? ModManagerWindow.ACTIVE_WINDOW : callingWindow;
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
		setTitle("Game Repair Database");
		setIconImages(ModManager.ICONS);

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
		progressBar.setIndeterminate(true);
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
		consoleArea.setText(
				"The game repair database allows Mod Manager to quickly restore files by checking against their hash. The database contains a list of your game files' hashes, and this is used to verify integrity of files for restoring/backing up. This is how Origin does game repairs.\n\nThis process does not create a backup of your files.");
		consoleArea.setEditable(false);
		JScrollPane jsp = new JScrollPane(consoleArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		rootPanel.add(jsp, BorderLayout.CENTER);
		getContentPane().add(rootPanel);
		int extraheight = 0;
		if (UIManager.getLookAndFeel().getID().equals("Windows")) {
			extraheight = 60;
		}

		Dimension size = new Dimension(425, 220 + extraheight);

		setPreferredSize(size);
		setMinimumSize(size);
		consoleArea.setCaretPosition(0);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				shutdownDB();
			}
		});

		pack();
		setLocationRelativeTo(callingWindow);
	}

	public boolean loadDatabase() throws SQLException {
		ModManager.debugLogger.writeMessage("Loading game repair database.");
		// connect method #1 - embedded driver
		File databases = new File(ModManager.getDatabaseDir());
		databases.mkdirs();
		String repairInfoURL = "jdbc:derby:repairinfo;create=true"; //derby home is in data/databases
		dbConnection = DriverManager.getConnection(repairInfoURL);
		if (dbConnection != null) {
			if (progressBar != null) {
				progressBar.setIndeterminate(false);
			}
			ModManager.debugLogger.writeMessage("Loaded game repair database.");
			databaseLoaded = true;
			return true;
		}

		ModManager.debugLogger.writeMessage("Game repair database failed to load.");
		databaseLoaded = false;
		progressBar.setIndeterminate(false);
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

	class HashmapWorker extends SwingWorker<Void, ThreadCommand> {
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
				infoLabel.setText("Getting complete file list. This may take some time...");
				this.progress.setIndeterminate(true);
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
				infoLabel.setText("Getting complete file list.\nThis may take some time...");
				this.progress.setIndeterminate(true);
			}
		}

		protected Void doInBackground() throws Exception {
			ModManager.debugLogger.writeMessage("Starting background thread for game repair hash db");

			if (filesToHash == null) {
				Iterable<File> files = FileUtils.listFiles(new File(basePath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				filesToHash = new ArrayList<File>();
				for (File file : files) {
					if (file.getAbsolutePath().toLowerCase().startsWith((ModManager.appendSlash(basePath) + "cmmbackup").toLowerCase())) {
						//ModManager.debugLogger.writeError("Skipping cmmbackup file " + file);
						continue; //skip backups folder
					}
					if (file.getName().equalsIgnoreCase("PCConsoleTOC.bin")) {
						continue; //skip PCConsoleTOC as they'll be updated outside of mod installs. Especially the basegame one.
					}
					filesToHash.add(file);
				}
			}
			publish(new ThreadCommand("UPDATE_BUTTON", "Updating database"));

			numFiles = filesToHash.size();
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
			} else {
				//clear the table.
				ModManager.debugLogger.writeMessage("Clearing basegamefiles table.");
				try {
					stmt = dbConnection.createStatement();
					stmt.execute("DELETE FROM basegamefiles WHERE 1=1");
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
					try {
						String md5 = MD5Checksum.getMD5Checksum(file.getAbsolutePath());
						ModManager.debugLogger.writeMessage(
								"INSERT INTO basegamefiles (filepath, hash, filesize) VALUES (UPPER(" + fileKey.toUpperCase() + ")," + md5 + "," + file.length() + ")");
						insertStatement.setString(1, fileKey.toUpperCase());
						insertStatement.setString(2, md5);
						insertStatement.setLong(3, file.length());
						insertStatement.execute();
					} catch (SQLException sqlExcept) {
						ModManager.debugLogger.writeException(sqlExcept);
					}
				} else {
					//UPDATE SINCE IT EXISTS.
					try {
						String md5 = MD5Checksum.getMD5Checksum(file.getAbsolutePath());
						ModManager.debugLogger
								.writeMessage("UPDATE basegamefiles SET hash=" + md5 + ", filesize=" + file.length() + " WHERE filepath=UPPER(" + fileKey.toUpperCase() + ")");
						updateStatement.setString(1, md5);
						updateStatement.setLong(2, file.length());
						updateStatement.setString(3, fileKey.toUpperCase());
						updateStatement.execute();
					} catch (SQLException sqlExcept) {
						ModManager.debugLogger.writeException(sqlExcept);
					}

					//Check if file should be deleted as a backup
					File bgdir = new File(ModManager.appendSlash(ModManagerWindow.GetBioGameDir()));
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
				publish(new ThreadCommand("UPDATE_PROGRESS", null, filesProcessed));
			}
			ModManager.debugLogger.writeMessage("Shutting down game repair db thread");
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> commands) {
			if (showGUI) {
				for (ThreadCommand tc : commands) {
					String command = tc.getCommand();
					switch (command) {
					case "UPDATE_BUTTON":
						startMap.setText(tc.getMessage());
						break;
					case "UPDATE_PROGRESS":
						if (numFiles > (int) tc.getData()) {
							this.progress.setIndeterminate(false);
							String fileName = ResourceUtils.getRelativePath(filesToHash.get((int) tc.getData()).getAbsolutePath(), basePath, File.separator);
							infoLabel.setText("<html>Updating file information for:<br>" + fileName + "</html>");
						}
						progress.setValue((int) ((int) tc.getData() * 100.0 / numFiles));
						break;
					}
				}
			}
		}

		protected void done() {
			// Coals decompiled
			if (showGUI) {
				dispose();
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("The repair database has been updated");
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
						infoLabel.setText("Database loaded.");
						startMap.setText("Update Database");
					} else {
						infoLabel.setText("No repair database has been created yet.");
						startMap.setText("Create Database");
					}
				}
			} else {
				if (showGUI) {
					infoLabel.setText("Database failed to load.");
					JOptionPane.showMessageDialog(BasegameHashDB.this, "<html>The game repair database failed to load.<br>"
							+ "Only one connection to the local repair database is allowed at a time.<br>"
							+ "Please make sure you only have one instance of Mod Manager running.<br>Mod Manager appears as Java (TM) Platform Binary (or javaw.exe on Windows Vista/7) in Task Manager.<br><br>If the issue persists and you are sure only one instance is running, close Mod Manager and delete the<br>data\\databases folder.<br>You will need to re-create the game repair database afterwards.<br><br>If this *STILL* does not fix your issue, please send a log to Mgamerz through the help menu.</html>",
							"Database Failure", JOptionPane.ERROR_MESSAGE);
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
			if (isBasegameTableCreated()) {
				try {
					String selectString = "SELECT * FROM BASEGAMEFILES WHERE filepath = UPPER(?)";
					PreparedStatement stmt;
					stmt = dbConnection.prepareStatement(selectString);
					ModManager.debugLogger.writeMessage("Querying database: SELECT * FROM BASEGAMEFILES WHERE filepath = UPPER(" + relativePath.toUpperCase() + ")");
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
			} else {
				ModManager.debugLogger.writeMessage("Game repair database is not created.");
				return null;
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
