package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
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
import com.me3tweaks.modmanager.ui.ButtonColumn;
import com.me3tweaks.modmanager.ui.MultiLineTableCell;

public class ASIModWindow extends JDialog {

	protected static final int COL_ASIFILENAME = 0;
	protected static final int COL_HASH = 1;
	protected static final int COL_DESCRIPTION = 2;
	public static final int COL_ACTION = 3;
	private String gamedir;
	private static final String ASI_MANIFEST_LINK = "https://me3tweaks.com/mods/asi/getmanifest";
	private File asiDir;
	private boolean manifestLoaded = false;
	private XPathExpression updateGroupExpr;
	private XPathExpression asiModExpr;
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private ArrayList<ASIMod> asimods;

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
		String[] files = asiDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return name.endsWith(".asi");
			}
		});

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
		Object[][] data = new Object[latestASIs.size()][4];
		for (int i = 0; i < latestASIs.size(); i++) {
			ASIMod mod = latestASIs.get(i);
			String asifile = mod.getName();

			//String filepath = ModManager.appendSlash(asiDir.getAbsolutePath()) + asifile;
			data[i][COL_ASIFILENAME] = asifile;
			data[i][COL_HASH] = mod.getHash();
			data[i][COL_DESCRIPTION] = mod.getDescription();
			data[i][COL_ACTION] = "Install";
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

		Action delete = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				String path = ModManager.appendSlash(asiDir + File.separator + table.getModel().getValueAt(modelRow, COL_ASIFILENAME));
				ModManager.debugLogger.writeMessage("Deleting installed ASI mod: " + path);
				FileUtils.deleteQuietly(new File(path));
				Object breakpoint = table.getModel();
				((DefaultTableModel) table.getModel()).removeRow(modelRow);
			}
		};
		String[] columnNames = { "ASI Mod", "Hash", "Description", "Uninstall" };
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
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ButtonColumn buttonColumn = new ButtonColumn(table, delete, COL_ACTION);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_ACTION).setCellRenderer(centerRenderer);

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

	private ArrayList<ASIMod> getLatestASIs() {
		ArrayList<ASIMod> mods = new ArrayList<>();
		ArrayList<Integer> updategroups = new ArrayList<>();
		if (asimods != null) {
			for (ASIMod mod : asimods) {
				int ug = mod.getUpdateGroup();
				if (!updategroups.contains(ug)) {
					updategroups.add(ug);
				}
			}

			for (int ug : updategroups) {
				ASIMod highestVer = null;
				for (ASIMod mod : asimods) {
					if (mod.getUpdateGroup() == ug) {
						if (highestVer == null || mod.getVersion() > highestVer.getVersion()) {
							highestVer = mod;
						}
					}
				}
				if (highestVer != null) {
					//in case we have an empty update group...

					mods.add(highestVer);
				} else {
					ModManager.debugLogger.writeError("EMPTY UPDATE GROUP IN MANIFEST: " + ug);
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
			asimods = new ArrayList<ASIMod>();
			try {
				updateGroupExpr = xpath.compile("/ASIManifest/updategroup");
				asiModExpr = xpath.compile("asimod");
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new FileReader(ModManager.getASIManifestFile()));
				Document doc = db.parse(is);

				//Parse items
				NodeList updateGroups = (NodeList) updateGroupExpr.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < updateGroups.getLength(); i++) {
					Element updateGroup = (Element) updateGroups.item(i);
					int groupID = Integer.parseInt(updateGroup.getAttribute("groupid"));
					NodeList asiMod = (NodeList) asiModExpr.evaluate(updateGroup, XPathConstants.NODESET);
					for (int j = 0; j < asiMod.getLength(); j++) {
						Element modVer = (Element) asiMod.item(j);
						ASIMod mod = new ASIMod(modVer, groupID);
						asimods.add(mod);
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | NullPointerException e) {
				ModManager.debugLogger.writeErrorWithException("Error loading help file:", e);
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

	private ASIMod getModByHash(String hash) {
		if (asimods == null) {
			return null;
		}
		for (ASIMod mod : asimods) {
			if (mod.getHash().equals(hash)) {
				return mod;
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
}
