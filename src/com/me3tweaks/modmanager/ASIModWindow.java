package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.swing.table.TableModel;
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

import com.me3tweaks.modmanager.modupdater.ManifestModFile;
import com.me3tweaks.modmanager.objects.ASIMod;
import com.me3tweaks.modmanager.objects.ASIUpdateGroup;
import com.me3tweaks.modmanager.objects.InstalledASIMod;
import com.me3tweaks.modmanager.ui.ASIActionColumn;
import com.me3tweaks.modmanager.ui.MultiLineTableCell;
import com.me3tweaks.modmanager.ui.SwingLink;
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
	private JTable table;

	public ASIModWindow(String gamedir) {
		ModManager.debugLogger.writeMessage("Opening ASI window.");
		this.gamedir = ModManager.appendSlash(gamedir);
		String asidir = this.gamedir + "Binaries/win32/asi";
		asiDir = new File(asidir);
		if (!asiDir.exists()) {
			asiDir.mkdirs();
		}
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public ASIModWindow() {
		// TODO Auto-generated constructor stub
	}

	private void setupWindow() {
		setIconImages(ModManager.ICONS);
		setTitle("ASI Mod Manager");
		setModal(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(300, 200));
		loadLocalManifest(true);
		installedASIs = loadInstalledASIMods(asiDir);

		ArrayList<ASIMod> latestASIs = getLatestASIs();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel infoLabel = new JLabel("<html>ASI Mod Management</html>", SwingConstants.CENTER);

		Action installbinkasi = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ModManagerWindow.ACTIVE_WINDOW.toolsInstallBinkw32asi.doClick();
				dispose();
				new ASIModWindow(gamedir);
			}
		};

		JLabel binkw32 = new JLabel("Binkw32 ASI loader installed");
		if (!ModManager.checkIfASIBinkBypassIsInstalled(gamedir + "BioGame\\")) {
			binkw32 = new SwingLink("Binkw32 ASI loader not installed. Click to install...",
					"ASI mods will not load without the binkw32 ASI loader. You can install ASI mods but they won't do anything without this loader.", installbinkasi);
		}
		binkw32.setHorizontalAlignment(JLabel.CENTER);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(infoLabel, BorderLayout.NORTH);
		topPanel.add(binkw32, BorderLayout.CENTER);
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

		String[] columnNames = { "ASI Mod", "Description", "Actions" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		for (int i = 0; i < installedASIs.size(); i++) {
			InstalledASIMod mod = installedASIs.get(i);
			if (getManifestModByHash(mod.getHash()) == null) {
				//String filepath = ModManager.appendSlash(asiDir.getAbsolutePath()) + asifile;
				Object[] row = new Object[3];
				row[COL_ASIFILENAME] = mod;
				row[COL_DESCRIPTION] = "Manually installed ASI. This ASI has not been verified by ME3Tweaks. If you wish to have it verified, please visit the forums.";
				row[COL_ACTION] = "<html><center>Manually Installed</center></html>";
				model.addRow(row);
			}
		}

		Action actionButtonClick = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				//String path = ModManager.appendSlash(asiDir + File.separator + 
				//ModManager.debugLogger.writeMessage("Deleting installed ASI mod: " + path);
				//FileUtils.deleteQuietly(new File(path));
				//Object breakpoint = table.getModel();
				//((DefaultTableModel) table.getModel()).removeRow(modelRow);
				Object val = table.getModel().getValueAt(modelRow, COL_ASIFILENAME);
				if (val instanceof ASIMod) {
					new ASIActionDialog(ASIModWindow.this, (ASIMod) val);
				} else if (val instanceof InstalledASIMod) {
					new ASIActionDialog(ASIModWindow.this, (InstalledASIMod) val);
				}
			}
		};
		final MultiLineTableCell mltc = new MultiLineTableCell();

		table = new JTable(model) {
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

		JPanel bottomPanel = new JPanel(new BorderLayout());
		JLabel mpLabel = new JLabel(
				"<html><div style=\"text-align: center;\">ASI mods can run arbitrary code and should be used with caution.<br>ASI mods that are installable here are verified by ME3Tweaks.</div></html>",
				SwingConstants.CENTER);
		bottomPanel.add(mpLabel, BorderLayout.SOUTH);

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
		bottomPanel.add(updateLocalManifest, BorderLayout.CENTER);

		panel.add(bottomPanel, BorderLayout.SOUTH);
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

	private static ArrayList<InstalledASIMod> loadInstalledASIMods(File asiDir) {
		String[] installedASIfiles = asiDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return name.endsWith(".asi");
			}
		});

		ArrayList<InstalledASIMod> installedASIs = new ArrayList<>();
		for (String installed : installedASIfiles) {
			InstalledASIMod iam = new InstalledASIMod();
			iam.setInstalledPath(ModManager.appendSlash(asiDir.getAbsolutePath()) + installed);
			iam.setFilename(FilenameUtils.getBaseName(installed));
			try {
				iam.setHash(MD5Checksum.getMD5Checksum(ModManager.appendSlash(asiDir.getAbsolutePath()) + installed));
				installedASIs.add(iam);
				ModManager.debugLogger.writeMessage("Detected installed ASI mod: " + iam.toLogString());
			} catch (Exception e1) {
				ModManager.debugLogger.writeErrorWithException("ASI mod is installed but unable to get hash: " + installed, e1);
			}
		}

		return installedASIs;
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
		updategroups = new ArrayList<ASIUpdateGroup>();

		if (manifestFile.exists()) {
			//Parse, download resources
			DocumentBuilder db;
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
						ASIMod mod = new ASIMod(modVer, groupID);
						currentGroup.addVersion(mod);
					}
				}
				if (currentGroup != null) {
					currentGroup.sortVersions();
				}
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | NullPointerException e) {
				ModManager.debugLogger.writeErrorWithException("Error loading asi manifest file:", e);
				manifestLoaded = false;
				return;
			}
			manifestLoaded = true;
		} else {
			if (fetchOnlineManifest) {
				boolean fetchedOnlineManifest = getOnlineASIManifest();
				if (fetchedOnlineManifest) {
					loadLocalManifest(false);
				} else {
					manifestLoaded = false;
				}
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
	public static boolean getOnlineASIManifest() {
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
			return true;
		} catch (IOException | URISyntaxException e) {
			ModManager.debugLogger.writeErrorWithException("Error fetching latest asi mod manifest file:", e);
			if (ModManager.getASIManifestFile().exists()) {
				ModManager.debugLogger.writeError("The old manifest will be loaded.");
			} else {
				ModManager.debugLogger.writeError("No manifest exists locally. New ASIs will not be usable within Mod Manager.");
			}
		}
		return false;
	}

	/**
	 * Displays actions on MANIFEST mods - not independently installed mods.
	 * 
	 * @author Mgamerz
	 *
	 */
	class ASIActionDialog extends JDialog {
		private ASIMod mod;
		private InstalledASIMod installedmod;

		public ASIActionDialog(JDialog parentFrame, ASIMod mod) {
			this.mod = mod;
			setupWindow();
			setLocationRelativeTo(parentFrame);
			setVisible(true);
		}

		public ASIActionDialog(JDialog parentFrame, InstalledASIMod mod) {
			this.installedmod = mod;
			setupWindow();
			setLocationRelativeTo(parentFrame);
			setVisible(true);
		}

		private void setupWindow() {
			if (mod != null) {
				setTitle(mod.getName() + " Actions");
			} else {
				setTitle(installedmod.getFilename() + " Actions");
			}
			setIconImages(ModManager.ICONS);
			setModal(true);

			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			panel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.gridx = 0;
			c.weightx = 1;
			c.weighty = 0;
			c.fill = GridBagConstraints.HORIZONTAL;

			JLabel nameLabel = new JLabel("PLACEHOLDER", SwingConstants.CENTER);
			if (mod != null) {
				nameLabel.setText(mod.getName() + " by " + mod.getAuthor());
			} else {
				nameLabel.setText(installedmod.getFilename() + " (Manually installed)");
			}

			JLabel installStatus = new JLabel("Install status", SwingConstants.RIGHT);
			JLabel serverStatus = new JLabel("Server status");

			c.gridwidth = 2;
			c.anchor = GridBagConstraints.NORTH;
			panel.add(nameLabel, c);
			c.gridy++;
			if (mod != null) {
				ASIUpdateGroup ug = findManifestModUpdateGroup(mod);
				if (ug == null) {
					ModManager.debugLogger.writeError("Did not find update group for manifest mod. That shouldn't happen..");
				}
				String group = "Update Group " + (ug != null ? ug.getGroupID() : "[Error]");
				JLabel updateGroup = new JLabel(group, SwingConstants.CENTER);
				panel.add(updateGroup, c);
				c.gridy++;
			}

			c.gridwidth = 1;
			panel.add(serverStatus, c);

			c.gridx = 1;
			panel.add(installStatus, c);

			c.gridx = 0;
			c.gridy++;

			String description = "This ASI mod is not listed in the ME3Tweaks ASI mod manifest. You will not be able to download this from ASI Mod Management if you delete it.";
			if (mod != null) {
				description = mod.getDescription();
			}
			JLabel descriptionLabel = new JLabel("<html><div style=\"width: 300px\">" + description + "</div></html>");
			c.gridwidth = 2;
			panel.add(descriptionLabel, c);

			c.gridwidth = 1;
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

			if (mod == null) {
				sourceCode.setEnabled(false);
				sourceCode.setToolTipText("This mod is not in the server manifest from ME3Tweaks. Source code is not available.");
			}

			panel.add(sourceCode, c);

			c.gridx = 1;
			JButton installButton = new JButton("Install ASI Mod");
			JButton uninstallButton = new JButton("Uninstall ASI Mod");

			if (mod != null) {
				serverStatus.setText("Server version: " + mod.getVersion());
				InstalledASIMod installedMod = getInstalledModByHash(mod.getHash());

				if (installedMod != null) {
					//already installed
					panel.add(uninstallButton, c);
					installStatus.setText("Installed Version: " + getManifestModByHash(installedMod.getHash()).getVersion());
				} else {
					//todo: check for updates.
					//find if outdated
					installedMod = findOutdatedInstalledModByManifestMod(mod);
					if (installedMod != null) {
						installButton.setText("Update ASI Mod");
						installStatus.setText("Installed Version: " + getManifestModByHash(installedMod.getHash()).getVersion());
					} else {
						installStatus.setText("Not installed");
					}
					panel.add(installButton, c);
				}
			} else {
				panel.add(uninstallButton, c);
				installStatus.setText("Installed, not verified");
				serverStatus.setText("Not on server");
			}

			installButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (ModManager.isMassEffect3Running()) {
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can install an ASI.", "MassEffect3.exe is running",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (mod.getDownloadURL() == null) {
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
								"This ASI had an error while parsing the download link. Please report this to femshep with a Mod Manager log.", "No download link available",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					new ASIModInstaller(mod).execute();
					dispose();
				}
			});

			uninstallButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (ModManager.isMassEffect3Running()) {
						JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can uninstall an ASI.", "MassEffect3.exe is running",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					InstalledASIMod im = installedmod;
					if (im == null) {
						im = getInstalledModByHash(mod.getHash());
					}
					if (im != null) {
						ModManager.debugLogger.writeMessage("Deleting installed ASI mod: " + im.getInstalledPath());
						File installedFile = new File(im.getInstalledPath());
						FileUtils.deleteQuietly(installedFile);
						TableModel model = table.getModel();
						for (int i = 0; i < model.getRowCount(); i++) {
							Object modobj = model.getValueAt(i, COL_ASIFILENAME);
							if (mod != null && modobj instanceof ASIMod) {
								ASIMod m = (ASIMod) modobj;
								if (m == mod) {
									if (installedFile.exists()) {
										ModManager.debugLogger.writeMessage("Failed to delete installed ASI mod: " + im.getInstalledPath());
										model.setValueAt("<html><center>Installed, Failed to uninstall</center></html>", i, COL_ACTION);
									} else {
										ModManager.debugLogger.writeMessage("Deleted installed ASI mod: " + im.getInstalledPath());
										model.setValueAt("<html><center>Not Installed</center></html>", i, COL_ACTION);

									}
									break;
								}
							} else if (installedmod != null && modobj instanceof InstalledASIMod) {
								InstalledASIMod m = (InstalledASIMod) model.getValueAt(i, COL_ASIFILENAME);
								if (m == installedmod) {
									if (installedFile.exists()) {
										ModManager.debugLogger.writeMessage("Failed to delete installed ASI mod: " + im.getInstalledPath());
										model.setValueAt("<html><center>Installed, Failed to uninstall</center></html>", i, COL_ACTION);
									} else {
										ModManager.debugLogger.writeMessage("Deleted installed ASI mod: " + im.getInstalledPath());
										//model.setValueAt("<html><center>Not Installed</center></html>", i, COL_ACTION);
										((DefaultTableModel) model).removeRow(i);
									}
									break;
								}
							}
						}
						installedASIs = loadInstalledASIMods(asiDir);
						dispose();
					}
				}
			});

			c.gridy++;
			c.gridx = 0;

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

	public ASIUpdateGroup findManifestModUpdateGroup(ASIMod mod) {
		if (mod != null) {
			for (ASIUpdateGroup g : updategroups) {
				if (g.getModVersions().contains(mod)) {
					return g;
				}
			}
		}
		return null;
	}

	class ASIModInstaller extends SwingWorker<Integer, Void> {
		private static final int FAIL_OTHER = 100;
		private static final int SUCCESS_OK = 0;
		private static final int FAIL_SERVER_404 = 1;
		private static final int FAIL_BAD_HASH = 2;
		private ASIMod mod;

		public ASIModInstaller(ASIMod mod) {
			this.mod = mod;
			TableModel model = table.getModel();
			for (int i = 0; i < model.getRowCount(); i++) {
				ASIMod m = (ASIMod) model.getValueAt(i, COL_ASIFILENAME);
				if (m == mod) {
					model.setValueAt("<html><center>Installing...</center></html>", i, COL_ACTION);
					break;
				}
			}
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				ModManager.debugLogger.writeMessage("Downloading ASI from URL: " + mod.getDownloadURL());
				//File dest = new File(ModManager.appendSlash(asiDir.getAbsolutePath()) + mod.getInstallName() + "-v" + mod.getVersion() + ".asi");
				File dest = new File(ModManager.getTempDir() + mod.getInstallName() + "-v" + mod.getVersion() + ".asi");
				FileUtils.deleteQuietly(dest);
				FileUtils.copyURLToFile(new URL(mod.getDownloadURL()), dest);
				ModManager.debugLogger.writeMessage("Checksumming downloaded file: " + dest);
				String checksum = MD5Checksum.getMD5Checksum(dest.getAbsolutePath());
				if (!checksum.equals(mod.getHash())) {
					FileUtils.deleteQuietly(dest);
					ModManager.debugLogger.writeError("HASH FAILURE: DOWNLOADED " + checksum + ", requires " + mod.getHash());
					return FAIL_BAD_HASH;
				}
				ModManager.debugLogger.writeMessage("Checksum OK");
				ASIUpdateGroup updategroup = null;
				for (ASIUpdateGroup gp : updategroups) {
					if (gp.getModVersions().contains(mod)) {
						updategroup = gp;
						break;
					}
				}

				for (InstalledASIMod im : installedASIs) {
					if (updategroup.containsModWithHash(im.getHash())) {
						im.deleteMod();
					}
				}

				//install ASI
				File installdest = new File(ModManager.appendSlash(asiDir.getAbsolutePath()) + mod.getInstallName() + "-v" + mod.getVersion() + ".asi");
				FileUtils.deleteQuietly(installdest);
				ModManager.debugLogger.writeMessage("Installing ASI mod " + dest + " => " + installdest);
				FileUtils.copyFile(dest, installdest);
				ModManager.debugLogger.writeMessage("ASI mod " + mod.getName() + " v" + mod.getVersion() + " was installed");
			} catch (FileNotFoundException e) {
				ModManager.debugLogger.writeErrorWithException("Error fetching ASI file from URL: " + mod.getDownloadURL(), e);
				return FAIL_SERVER_404;
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Error fetching ASI file from URL: " + mod.getDownloadURL(), e);
				return FAIL_OTHER;
			}

			return SUCCESS_OK;
		}

		@Override
		public void done() {
			//TODO: RELOAD TABLE INFO?
			try {
				installedASIs = loadInstalledASIMods(asiDir);
				int retval = get();
				//OK
				TableModel model = table.getModel();
				for (int i = 0; i < model.getRowCount(); i++) {
					ASIMod m = (ASIMod) model.getValueAt(i, COL_ASIFILENAME);
					if (m == mod) {
						if (retval == SUCCESS_OK) {
							model.setValueAt("<html><center>Installed, up to date</center></html>", i, COL_ACTION);
							return;
						} else {
							//NOT GOOD
							String reason = "Install failed";
							switch (retval) {
							case FAIL_BAD_HASH:
								reason = "Hash Check Failed";
								break;
							case FAIL_SERVER_404:
								reason = "Server 404";
								break;
							case FAIL_OTHER:
								reason = "Install Failed";
								break;
							}
							model.setValueAt("<html><center>" + ASIActionColumn.ERROR_STR + ": " + reason + "<br>Check Mod Manager logs</center></html>", i, COL_ACTION);
						}
					}
				}
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Uncaught ASI mod download execution exeception: ", e);
				return;
			}
		}
	}

	public static boolean IsASIModGroupInstalled(int group) {
		ASIModWindow amw = new ASIModWindow();
		amw.gamedir = new File(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText()).getParent();
		String asidir = ModManager.appendSlash(amw.gamedir) + "Binaries/win32/asi";
		amw.asiDir = new File(asidir);
		if (!amw.asiDir.exists()) {
			amw.asiDir.mkdirs();
		}
		amw.loadLocalManifest(true);
		amw.installedASIs = ASIModWindow.loadInstalledASIMods(amw.asiDir);

		ASIUpdateGroup ug = null;
		for (ASIUpdateGroup g : amw.updategroups) {
			if (g.getGroupID() == group) {
				ug = g;
				break;
			}
		}

		if (ug != null) {
			for (int i = 0; i < amw.installedASIs.size(); i++) {
				InstalledASIMod mod = amw.installedASIs.get(i);
				ASIMod manifestmod = amw.getManifestModByHash(mod.getHash());
				if (ug.getModVersions().contains(manifestmod)) {
					return true;
				}
			}
		} else {
			ModManager.debugLogger.writeError("Update group is not present in the manifest: " + group + ". An ASI mod in this group cannot exist. Please report this to femshep.");
		}
		return false;
	}

	public static ArrayList<InstalledASIMod> getOutdatedASIMods(String biogamedir) {
		ArrayList<InstalledASIMod> outdatedasi = new ArrayList<>();
		ASIModWindow amw = new ASIModWindow();
		amw.gamedir = new File(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText()).getParent();
		String asidir = ModManager.appendSlash(amw.gamedir) + "Binaries/win32/asi";
		amw.asiDir = new File(asidir);
		if (!amw.asiDir.exists()) {
			amw.asiDir.mkdirs();
		}
		amw.loadLocalManifest(true);
		amw.installedASIs = loadInstalledASIMods(amw.asiDir);
		ArrayList<ASIMod> latestASIs = amw.getLatestASIs();
		ModManager.debugLogger.writeMessage("Looking for outdated versions of installed ASIs...");
		for (int i = 0; i < latestASIs.size(); i++) {
			ASIMod mod = latestASIs.get(i);
			if (amw.getInstalledModByHash(mod.getHash()) == null) {
				//check for it in the update group...
				InstalledASIMod outdatedmod = amw.findOutdatedInstalledModByManifestMod(mod);
				if (outdatedmod != null) {
					ModManager.debugLogger.writeMessage("Found outdated ASI " + outdatedmod);
					outdatedasi.add(outdatedmod);
				}
			}
		}
		ModManager.debugLogger.writeMessage("Outdated ASI check has completed.");
		return outdatedasi;
	}
}
