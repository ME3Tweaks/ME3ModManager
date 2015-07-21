package com.me3tweaks.modmanager.basegamedb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
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

import com.me3tweaks.modmanager.DebugLogger;
import com.me3tweaks.modmanager.MD5Checksum;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ResourceUtils;

public class BasegameHashDB extends JFrame implements ActionListener{
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
	
	public static void main(String[] args) {
		isRunningAsMain = true;
		//init the debug logger.
		ModManager.debugLogger = new DebugLogger();
		ModManager.debugLogger.initialize();
		ModManager.logging = true;
		new BasegameHashDB(null, "I:/Origin Games/Mass Effect 3/", true);
	}
	
	public BasegameHashDB(JFrame callingWindow, String basePath, boolean showGUI){
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
	
	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		// TODO Auto-generated method stub
		infoLabel = new JLabel("<html>Loading repair database...</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);
		
		startMap = new JButton("Update DB");
		startMap.addActionListener(this);
		startMap.setEnabled(false);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		
		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.add(startMap, BorderLayout.EAST);
		progressPanel.add(progressBar, BorderLayout.CENTER);
		northPanel.add(progressPanel, BorderLayout.CENTER);
		northPanel.setBorder(new EmptyBorder(5,5,5,5));
		rootPanel.add(northPanel, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		consoleArea.setText("The basegame repair database keeps track of your preferred configuration of files, so when restoring you get a certain set of files restored, such as texture swaps or a specific Coalesced file.\n"
				+ "\nUpdate the database only when your game is in the state you want to always restore to. For example, vanilla. It will take several minutes to create or update the database. When files are installed through a BASEGAME mod descriptor, files that are backed up and restored are checked against this database for authenticity.\n"
				+ "\nFiles that fail this check when restoring indicates that the currently backed up file does not match your default configuration, and will prompt you before it is restored."
				+ "\n\nNote that this database only covers the basegame files and not files in DLC.");
		consoleArea.setEditable(false);
		
		rootPanel.add(consoleArea,BorderLayout.CENTER);
		getContentPane().add(rootPanel);
		this.setPreferredSize(new Dimension(405,370));
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        	if (dbConnection != null) {
	        		try {
						dbConnection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	if (isRunningAsMain) {
	        		System.exit(0);
	        	}
		    }
		});
		setTitle("Basegame Repair Database");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		pack();
		this.setLocationRelativeTo(callingWindow);
	}
	
	
	
	public boolean loadDatabase(){
		ModManager.debugLogger.writeMessage("Loading basegame database.");
		try {
            // connect method #1 - embedded driver
			File databases = new File(ModManager.getDatabaseDir());
			databases.mkdirs();
            String repairInfoURL = "jdbc:derby:data/databases/repairinfo;create=true";
            dbConnection = DriverManager.getConnection(repairInfoURL);
            if (dbConnection != null) {
            	ModManager.debugLogger.writeMessage("Loaded basegame database.");
            	databaseLoaded = true;
                return true;
            }

        } catch (SQLException ex) {
        	System.out.println("exception.");
			ModManager.debugLogger.writeException(ex);
        }
		ModManager.debugLogger.writeMessage("Basegame database failed to load.");
		databaseLoaded = false;
		return false;
	}
	
	/**
	 * Creates a hashmap of all files from the given directory. 
	 */
	public void createFileHashmap(){
		// TODO Auto-generated method stub
		Iterable<File> files = FileUtils.listFiles(new File(basePath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		ArrayList<File> filesToHash = new ArrayList<File>();
		for (File file : files) {
			filesToHash.add(file);
		}
		startMap.setEnabled(false);
		ModManager.debugLogger.writeMessage("Starting repairinfo thread.");
		hmw = new HashmapWorker(filesToHash,progressBar);
		hmw.execute();
	}
	
	public void updateDB(ArrayList<File> filesToUpdate) {
		hmw = new HashmapWorker(filesToUpdate,null);
		hmw.execute();
	}
	
	class HashmapWorker extends SwingWorker<Void, Integer> {
		private ArrayList<File> filesToHash;
		private JProgressBar progress;
		private int numFiles;

		public HashmapWorker(ArrayList<File> filesToHash,
				JProgressBar progress) {
			ModManager.debugLogger.writeMessage("Loading HashmapWorker...");
			this.filesToHash = filesToHash;
			this.numFiles = filesToHash.size();
			this.progress = progress;
			if (showGUI) {
				infoLabel.setText("Preparing to create restoration map...");
			}
		}

		protected Void doInBackground() throws Exception {
			ModManager.debugLogger.writeMessage("Starting background thread for basegame hash db");
			PreparedStatement insertStatement, selectStatement,updateStatement = null;
			DatabaseMetaData dbmd = dbConnection.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, "BASEGAMEFILES", null);
			if (!rs.next()) {
				//create the table.
				ModManager.debugLogger.writeMessage("Creating basegamefiles table.");
				try
		        {
		            stmt = dbConnection.createStatement();
		            stmt.execute("CREATE TABLE basegamefiles ("
		            		+ "filepath VARCHAR(200) PRIMARY KEY,"
		            		+ "hash VARCHAR(65) NOT NULL,"
		            		+ "filesize BIGINT NOT NULL)");
		            //stmt.close();
		        }
		        catch (SQLException sqlExcept)
		        {
		        	ModManager.debugLogger.writeException(sqlExcept);
		        }
			}
			
			int filesProcessed = 0;
			String insertString = "INSERT INTO basegamefiles (filepath, hash, filesize) VALUES (?,?,?)";
			String selectString = "SELECT * FROM BASEGAMEFILES WHERE filepath = ?";
			String updateString = "UPDATE basegamefiles SET hash=?, filesize=? WHERE filepath=?";
			insertStatement = dbConnection.prepareStatement(insertString);
			selectStatement = dbConnection.prepareStatement(selectString);
			updateStatement = dbConnection.prepareStatement(updateString);
			for (File file : filesToHash) {
				String fileKey = ResourceUtils.getRelativePath(file.getAbsolutePath(), basePath, File.separator);
				ModManager.debugLogger.writeMessage("Cataloging "+fileKey);
				//select first, to see if it's in the DB...
				selectStatement.setString(1, fileKey);
				selectStatement.execute();
				ResultSet srs = selectStatement.getResultSet();
				if (!srs.next()){
					//INSERT - ITS NOT THERE.
					ModManager.debugLogger.writeMessage("Inserting entry "+fileKey);
					try
			        {
						insertStatement.setString(1, fileKey);
						insertStatement.setString(2, MD5Checksum.getMD5Checksum(file.getAbsolutePath()));
						insertStatement.setLong(3, file.length());
						insertStatement.execute();
			        }
			        catch (SQLException sqlExcept)
			        {
			        	ModManager.debugLogger.writeException(sqlExcept);
			        }
				} else {
					//UPDATE SINCE IT EXISTS.
					ModManager.debugLogger.writeMessage("Updating entry "+fileKey);
					try
			        {
						updateStatement.setString(3, fileKey);
						updateStatement.setString(1, MD5Checksum.getMD5Checksum(file.getAbsolutePath()));
						updateStatement.setLong(2, file.length());
						updateStatement.execute();
			        }
			        catch (SQLException sqlExcept)
			        {
			        	ModManager.debugLogger.writeException(sqlExcept);
			        }
				}
				
				filesProcessed++;
				this.publish(filesProcessed);
			}
			ModManager.debugLogger.writeMessage("Shutting down basegame db thread");
			return null;
		}

		@Override
		protected void process(List<Integer> numCompleted) {
			if (showGUI) {
				if (numFiles > numCompleted.get(0)) {
					String fileName = ResourceUtils.getRelativePath(filesToHash.get(numCompleted.get(0)).getAbsolutePath(), basePath, File.separator);
					infoLabel.setText("<html>Cataloging file information for:<br>"
							+ fileName+"</html>");
				}
				progress.setValue((int) (100 / (numFiles / (float)numCompleted.get(0))));
			}
		}

		protected void done() {
			// Coals decompiled
			if (showGUI) {
				progress.setValue(100);
				infoLabel.setText("<html>The repair database has been updated.</html>");
				startMap.setEnabled(true);
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
					infoLabel.setText("Database loaded.");
				}
			} else {
				if (showGUI) {
					infoLabel.setText("Database can't be loaded.");
					JOptionPane.showMessageDialog(null, "Unable to connect to database. Do you have multiple Mod Manager windows open?", "Database error", JOptionPane.ERROR_MESSAGE);
				}				
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == startMap) {
			createFileHashmap();
		}
	}

	public RepairFileInfo getFileInfo(String relativePath) {
		if (databaseLoaded) {
			try {
				String selectString = "SELECT * FROM BASEGAMEFILES WHERE filepath = ?";
				PreparedStatement stmt;
				stmt = dbConnection.prepareStatement(selectString);
				stmt.setString(1, relativePath);
				stmt.execute();
				ResultSet srs = stmt.getResultSet();
				if (!srs.next()){
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
				ModManager.debugLogger.writeMessage("Error getting file info from database for "+relativePath);
				ModManager.debugLogger.writeException(e);
			}
		}
		return null;
	}


}
