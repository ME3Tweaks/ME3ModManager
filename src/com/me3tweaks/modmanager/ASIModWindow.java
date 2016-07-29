package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.objects.ASIMod;
import com.me3tweaks.modmanager.objects.ASIUpdateGroup;
import com.me3tweaks.modmanager.objects.InstalledASIMod;
import com.me3tweaks.modmanager.ui.ASIActionColumn;
import com.me3tweaks.modmanager.ui.MultiLineTableCell;
import com.me3tweaks.modmanager.utilities.MD5Checksum;

public class ASIModWindow extends JDialog {

	protected static final int COL_ASIFILENAME = 0;
	protected static final int COL_DESCRIPTION = 1;
	public static final int COL_ACTION = 2;
	private String gamedir;
	private static final String ASI_MANIFEST_LINK = "https://me3tweaks.com/mods/asi/getmanifest";
	private File asiDir;
	private boolean manifestLoaded = false;
	private XPathExpression updateGroupExpr;
	private XPathExpression asiModExpr;
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private ArrayList<ASIUpdateGroup> updategroups;
	private ArrayList<InstalledASIMod> installedASIs;

	public ASIModWindow(String gamedir) {
		ModManager.debugLogger.writeMessage("Opening ASI window.");
		this.gamedir = gamedir;
		String asidir = ModManager.appendSlash(gamedir) + "Binaries/win32/asi";
		asiDir = new File(asidir);
		if (!asiDir.exists()) {
			asiDir.mkdirs();
		}
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("ASI Manager");
		setModal(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(300, 200));
		loadLocalManifest(true);
		loadInstalledASIMods();

		ArrayList<ASIMod> latestASIs = getLatestASIs();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html>ASI Mod Management</html>", SwingConstants.CENTER);
		JButton updateLocalManifest = new JButton("Download latest ASI manifest");
		updateLocalManifest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateLocalManifest.setText("Fetching...");
				updateLocalManifest.setEnabled(false);
				getOnlineASIManifest();
				dispose();
				new ASIModWindow(gamedir);
			}
		});
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(infoLabel, BorderLayout.NORTH);
		topPanel.add(updateLocalManifest, BorderLayout.SOUTH);
		panel.add(topPanel, BorderLayout.NORTH);

		//TABLE
		Object[][] data = new Object[latestASIs.size()][3];
		for (int i = 0; i < latestASIs.size(); i++) {
			ASIMod mod = latestASIs.get(i);
			String headingtext = "";
			if (getInstalledModByHash(mod.getHash()) != null) {
				headingtext = "Installed, up to date";
			} else {
				//check for it in the update group...
				if (findOutdatedInstalledModByManifestMod(mod) != null) {
					headingtext = "Installed, outdated";
				} else {
					headingtext = "Not Installed";
				}
			}

			//String filepath = ModManager.appendSlash(asiDir.getAbsolutePath()) + asifile;
			data[i][COL_ASIFILENAME] = mod;
			data[i][COL_DESCRIPTION] = mod.getDescription();
			data[i][COL_ACTION] = "<html><center>" + headingtext + "</center></html>";
		}
		/*
		 * for (int i = 0; i < files.length; i++) { String asifile = files[i];
		 * String filepath = ModManager.appendSlash(asiDir.getAbsolutePath()) +
		 * asifile; data[i][COL_ASIFILENAME] = asifile; try { data[i][COL_HASH]
		 * = MD5Checksum.getMD5Checksum(filepath); } catch (Exception e1) { //
		 * TODO Auto-generated catch block e1.printStackTrace();
		 * data[i][COL_HASH] = "Hash failure";
		 * 
		 * } data[i][COL_DESCRIPTION] = "Loading..."; data[i][COL_ACTION] =
		 * "Uninstall"; }
		 */

		Action actionButtonClick = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				//String path = ModManager.appendSlash(asiDir + File.separator + 
				//ModManager.debugLogger.writeMessage("Deleting installed ASI mod: " + path);
				//FileUtils.deleteQuietly(new File(path));
				//Object breakpoint = table.getModel();
				//((DefaultTableModel) table.getModel()).removeRow(modelRow);
				;
				ASIActionDialog aad = new ASIActionDialog(ASIModWindow.this, (ASIMod) table.getModel().getValueAt(modelRow, COL_ASIFILENAME));
			}
		};
		String[] columnNames = { "ASI Mod", "Description", "Actions" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		final MultiLineTableCell mltc = new MultiLineTableCell();

		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return column == COL_ACTION;
			}

			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column == COL_DESCRIPTION) {
					return mltc;
				} else {
					return super.getCellRenderer(row, column);
				}
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);

		ASIActionColumn aac = new ASIActionColumn(table, actionButtonClick, COL_ACTION);

		//DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		//centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		//table.getColumnModel().getColumn(COL_ACTION).setCellRenderer(centerRenderer);

		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane, BorderLayout.CENTER);

		JLabel mpLabel = new JLabel(
				"<html><div style=\"text-align: center;\">ASI mods can run arbitrary code and should be used with caution.<br>ASI mods that are installable here are verified by ME3Tweaks.</div></html>",
				SwingConstants.CENTER);
		panel.add(mpLabel, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
	}

	private InstalledASIMod findOutdatedInstalledModByManifestMod(ASIMod mod) {
		if (updategroups == null) {
			return null;
		}
		for (ASIUpdateGroup agroup : updategroups) {
			if (agroup.getModVersions().contains(mod)) {
				//see if anything beyond first version has a hash match...
				if (agroup.getModVersions().size() > 1) {
					int ver = 0;
					for (ASIMod manmod : agroup.getModVersions()) {
						if (ver == 0) {
							ver++; //skip; first, which is 'latest'.
							continue;
						}
						for (InstalledASIMod installed : installedASIs) {
							if (manmod.getHash().equals(installed.getHash())) {
								//MATCH
								return installed;
							}
						}
						ver++;
					}
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private void loadInstalledASIMods() {
		String[] installedASIfiles = asiDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return name.endsWith(".asi");
			}
		});

		installedASIs = new ArrayList<>();
		for (String installed : installedASIfiles) {
			InstalledASIMod iam = new InstalledASIMod();
			iam.setInstalledPath(ModManager.appendSlash(asiDir.getAbsolutePath()) + installed);
			iam.setFilename(FilenameUtils.getBaseName(installed));
			try {
				iam.setHash(MD5Checksum.getMD5Checksum(ModManager.appendSlash(asiDir.getAbsolutePath()) + installed));
				installedASIs.add(iam);
			} catch (Exception e1) {
				ModManager.debugLogger.writeErrorWithException("ASI mod is installed but unable to get hash: " + installed, e1);
			}
		}
	}

	/**
	 * Fetches a list of the latest ASI versions for displaying
	 * 
	 * @return
	 */
	private ArrayList<ASIMod> getLatestASIs() {
		ArrayList<ASIMod> mods = new ArrayList<>();
		if (updategroups != null) {
			for (ASIUpdateGroup group : updategroups) {
				if (group.getModVersions().size() > 0) {
					mods.add(group.getModVersions().get(0));
				}
			}
		}
		return mods;
	}

	private void loadLocalManifest(boolean fetchOnlineManifest) {
		File manifestFile = ModManager.getASIManifestFile();
		if (manifestFile.exists()) {
			//Parse, download resources
			DocumentBuilder db;
			updategroups = new ArrayList<ASIUpdateGroup>();
			try {
				updateGroupExpr = xpath.compile("/ASIManifest/updategroup");
				asiModExpr = xpath.compile("asimod");
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new FileReader(ModManager.getASIManifestFile()));
				Document doc = db.parse(is);

				//Parse items
				NodeList updateGroups = (NodeList) updateGroupExpr.evaluate(doc, XPathConstants.NODESET);
				ASIUpdateGroup currentGroup = null;
				for (int i = 0; i < updateGroups.getLength(); i++) {
					if (currentGroup != null) {
						currentGroup.sortVersions();
					}
					Element updateGroup = (Element) updateGroups.item(i);
					int groupID = Integer.parseInt(updateGroup.getAttribute("groupid"));
					currentGroup = new ASIUpdateGroup(groupID);
					updategroups.add(currentGroup);
					NodeList asiMod = (NodeList) asiModExpr.evaluate(updateGroup, XPathConstants.NODESET);
					for (int j = 0; j < asiMod.getLength(); j++) {
						Element modVer = (Element) asiMod.item(j);
						ASIMod mod = new ASIMod(modVer);
						currentGroup.addVersion(mod);
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | NullPointerException e) {
				ModManager.debugLogger.writeErrorWithException("Error loading asi manifest file:", e);
				manifestLoaded = false;
				return;
			}
			manifestLoaded = true;
		} else {
			if (fetchOnlineManifest) {
				getOnlineASIManifest();
				loadLocalManifest(false);
			} else {
				manifestLoaded = false;
			}
		}
	}

	private InstalledASIMod getInstalledModByHash(String hash) {
		if (installedASIs == null) {
			return null;
		}
		for (InstalledASIMod mod : installedASIs) {
			if (mod.getHash().equals(hash)) {
				return mod;
			}
		}
		return null;
	}

	private ASIMod getManifestModByHash(String hash) {
		if (updategroups == null) {
			return null;
		}
		for (ASIUpdateGroup group : updategroups) {
			for (ASIMod mod : group.getModVersions()) {
				if (mod.getHash().equals(hash)) {
					return mod;
				}
			}
		}
		return null;
	}

	/**
	 * Contacts ME3Tweaks and fetches the latest ASI mod info.
	 */
	public static void getOnlineASIManifest() {
		URIBuilder urib;
		String responseString = null;
		try {
			urib = new URIBuilder(ASI_MANIFEST_LINK);
			HttpClient httpClient = HttpClientBuilder.create().build();
			URI uri = urib.build();
			ModManager.debugLogger.writeMessage("Getting latest ASI mod manifest from link: " + uri.toASCIIString());
			HttpResponse response = httpClient.execute(new HttpGet(uri));
			responseString = new BasicResponseHandler().handleResponse(response);
			FileUtils.writeStringToFile(ModManager.getASIManifestFile(), responseString);
			ModManager.debugLogger.writeMessage("File written to disk. Exists on filesystem, ready for loading: " + ModManager.getASIManifestFile().exists());
		} catch (IOException | URISyntaxException e) {
			ModManager.debugLogger.writeErrorWithException("Error fetching latest asi mod manifest file:", e);
		}
	}

	/**
	 * Displays actions on MANIFEST mods - not independently installed mods.
	 * 
	 * @author Mgamerz
	 *
	 */
	class ASIActionDialog extends JDialog {
		private ASIMod mod;

		public ASIActionDialog(JDialog parentFrame, ASIMod mod) {
			this.mod = mod;
			setupWindow();
			setLocationRelativeTo(parentFrame);
			setVisible(true);
		}

		private void setupWindow() {
			setTitle(mod.getName() + " Actions");
			setIconImages(ModManager.ICONS);
			setModal(true);

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.gridx = 0;
			JButton installButton = new JButton("Install ASI Mod");
			JButton uninstallButton = new JButton("Uninstall ASI Mod");

			InstalledASIMod installedMod = getInstalledModByHash(mod.getHash());
			System.out.println("BREAK");
			if (installedMod != null) {
				//already installed
				panel.add(uninstallButton, c);
			} else {
				//todo: check for updates.
				panel.add(installButton, c);
			}

			c.gridy++;

			JButton sourceCode = new JButton("View source code");
			sourceCode.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						ModManager.openWebpage(new URL(mod.getSourceCode()));
						ASIActionDialog.this.dispose();
					} catch (MalformedURLException e1) {
						ModManager.debugLogger.writeErrorWithException("Invalid source code URL " + mod.getSourceCode(), e1);
						JOptionPane.showMessageDialog(null, "<html>The specified source code URL is not valid:<br>" + mod.getSourceCode(), "Invalid Source Code Link",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			panel.add(sourceCode, c);
			c.gridy++;

			add(panel);
			pack();
		}
	}

	public void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 50; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}
	
	class ASIModInstaller extends SwingWorker<Void, Void> {
		private ASIMod mod;

		public ASIModInstaller(ASIMod mod) {
			this.mod = mod;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			try {
				ModManager.debugLogger.writeMessage("Downloading ASI from URL: "+mod.getDownloadURL());
				FileUtils.copyURLToFile(new URL(mod.getDownloadURL()), new File(ModManager.appendSlash(asiDir.getAbsolutePath())+mod.getInstallName()+"-v"+mod.getVersion()+".asi"));
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Error fetching ASI file from URL: "+mod.getDownloadURL(), e);
			}
			
			return null;
		}
		@Override
		public void done() {
			//TODO: RELOAD TABLE INFO?
		}
	}
}
