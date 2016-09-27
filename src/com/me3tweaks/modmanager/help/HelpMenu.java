package com.me3tweaks.modmanager.help;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

import com.me3tweaks.modmanager.AboutWindow;
import com.me3tweaks.modmanager.LogOptionsWindow;
import com.me3tweaks.modmanager.LogWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.HelpMenuItem;
import com.me3tweaks.modmanager.utilities.MD5Checksum;

public class HelpMenu {

	public final static String HELP_LINK = "https://me3tweaks.com/modmanager/help/latesthelp.xml";
	public final static String HELP_RESOURCES_LINK = "https://me3tweaks.com/modmanager/help/resources/";

	private static XPath xpath = XPathFactory.newInstance().newXPath();
	private static XPathExpression helpItemExpr;
	private static XPathExpression sublistExpr;

	/**
	 * Contacts ME3Tweaks and fetches the latest help info. If any resources are
	 * missing, they are downloaded to data/help.
	 * 
	 * @return
	 */
	public static void getOnlineHelp() {

		URIBuilder urib;
		String responseString = null;
		try {
			urib = new URIBuilder(HELP_LINK);
			HttpClient httpClient = HttpClientBuilder.create().build();
			URI uri = urib.build();
			ModManager.debugLogger.writeMessage("Getting latest help info from link: " + uri.toASCIIString());
			HttpResponse response = httpClient.execute(new HttpGet(uri));
			responseString = new BasicResponseHandler().handleResponse(response);
			FileUtils.writeStringToFile(ModManager.getHelpFile(), responseString);
			ModManager.debugLogger
					.writeMessage("File written to disk. Exists on filesystem, ready for loading: " + ModManager.getHelpFile().exists());
			//Parse, download resources
			DocumentBuilder db;
			try {
				helpItemExpr = xpath.compile("helpitem");
				sublistExpr = xpath.compile("list");
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new FileReader(ModManager.getHelpFile()));
				Document doc = db.parse(is);

				//Parse items
				Element helpMenuElement = (Element) doc.getElementsByTagName("helpmenu").item(0);
				NodeList topLevelLists = (NodeList) sublistExpr.evaluate(helpMenuElement, XPathConstants.NODESET);
				for (int i = 0; i < topLevelLists.getLength(); i++) {
					Element topLevelList = (Element) topLevelLists.item(i);
					buildSublist(null, topLevelList, true);
				}

			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | NullPointerException e) {
				ModManager.debugLogger.writeErrorWithException("Error loading help file:", e);
			}
		} catch (IOException | URISyntaxException e) {
			ModManager.debugLogger.writeErrorWithException("Error fetching latest help file:", e);
		}
	}

	/**
	 * Reads the cached latesthelp.xml file in the data/help directory and
	 * parses it into menus to add
	 * 
	 * @return
	 */
	public static JMenu constructHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		JMenuItem helpPost, helpForums, helpAbout, helpLogViewer, helpGetLog, helpEmailFemShep;

		helpPost = new JMenuItem("View FAQ");
		helpPost.setToolTipText("Opens the Mod Manager FAQ");
		helpForums = new JMenuItem("Forums");
		helpForums.setToolTipText("Opens the ME3Tweaks forums");
		helpAbout = new JMenuItem("About Mod Manager");
		helpAbout.setToolTipText("<html>Shows credits for Mod Manager and source code information</html>");

		helpGetLog = new JMenuItem("Generate Diagnostics Log");
		helpGetLog.setToolTipText("<html>Allows you to generate a Mod Manager log with diagnostic information for FemShep and Mod Developers.<br>Allows you to automatically upload to PasteBin for super easy sharing.</html>");

		helpLogViewer = new JMenuItem("View Mod Manager log");
		helpLogViewer.setToolTipText("<html>View the current session log</html>");

		helpEmailFemShep = new JMenuItem("Contact FemShep");
		helpEmailFemShep.setToolTipText("<html>Contact FemShep via email</html>");

		helpMenu.add(helpPost);
		helpMenu.add(helpForums);
		HelpMenu.insertLocalHelpMenus(helpMenu);
		helpMenu.addSeparator();
		helpMenu.add(helpLogViewer);
		helpMenu.add(helpGetLog);
		helpMenu.add(helpEmailFemShep);
		helpMenu.addSeparator();
		helpMenu.add(helpAbout);

		helpPost.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				URI theURI;
				try {
					theURI = new URI("http://me3tweaks.com/tools/modmanager/faq");
					java.awt.Desktop.getDesktop().browse(theURI);
				} catch (URISyntaxException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});

		helpForums.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URI theURI;
				try {
					theURI = new URI("http://me3tweaks.com/forums");
					java.awt.Desktop.getDesktop().browse(theURI);
				} catch (URISyntaxException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});

		helpLogViewer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new LogWindow();
			}
		});

		helpGetLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!ModManager.logging) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
							"You must enable logging via the Actions > Options menu before logs are generated.", "Logging disabled",
							JOptionPane.ERROR_MESSAGE);
				} else {
					copyLogToClipboard();
				}
			}
		});
		helpEmailFemShep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								ModManagerWindow.ACTIVE_WINDOW,
								"<html><div style=\"width:400px;\">FemShep is the developer of this program.<br>"
										+ "Please email me if you have crashes or bugs, or use the forums.<br>"
										+ "If you have a crash or a bug I will need the Mod Manager log.<br><br>"
										+ "1. Close Mod Manager with logging enabled. Restart Mod Manager, and reproduce your issue.<br>"
										+ "2. Immediately after the issue occurs, go to Help > Generate Diagnostics Log.<br>"
										+ "3. Leave the default options unless instructed otherwise. Upload your log to pastebin.<br>"
										+ "4. In your email, give me a description of the problem and the steps you took to produce it. INCLUDE THE PASTEBIN LINK.<br>  "
										+ "I will not look into the log to attempt to figure what issue you are having if you don't give me a description.<br>"
										+ "Please do not do any other operations as it makes the logs harder to read.<br>"
										+ "If you submit a crash/bug report without a Mod Manager log there is very little I can do to help you.<br>"
										+ "Please note that I only speak English.<br><br>"
										+ "You can email me at femshep@me3tweaks.com.</div></html>", "Contacting FemShep",
								JOptionPane.INFORMATION_MESSAGE);
			}
		});
		helpForums.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				URI theURI;
				try {
					theURI = new URI("http://me3tweaks.com/forums");
					java.awt.Desktop.getDesktop().browse(theURI);
				} catch (URISyntaxException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});

		helpAbout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutWindow(ModManagerWindow.ACTIVE_WINDOW);
			}
		});

		return helpMenu;
	}

	public static void copyLogToClipboard() {
		LogOptionsWindow low = new LogOptionsWindow(ModManagerWindow.ACTIVE_WINDOW);
	}

	private static void insertLocalHelpMenus(JMenu helpMenu) {
		// TODO Auto-generated method stub
		if (!ModManager.getHelpFile().exists()) {
			JMenuItem helpNotLoaded = new JMenuItem("Local help not available");
			helpNotLoaded.setEnabled(false);
			helpNotLoaded.setToolTipText("Local help files are missing or unable to load");
			helpMenu.add(helpNotLoaded);
			ModManager.debugLogger.writeError("Local help file does not exist: " + ModManager.getHelpFile());
			return;
		}
		try {
			helpItemExpr = xpath.compile("helpitem");
			sublistExpr = xpath.compile("list");
		} catch (XPathExpressionException e1) {
			JMenuItem helpNotLoaded = new JMenuItem("XPATH ERROR LOADING LOCAL HELP");
			helpNotLoaded.setEnabled(false);
			helpNotLoaded.setToolTipText("Local help failed to load due to an XPATH failure");
			helpMenu.add(helpNotLoaded);
			ModManager.debugLogger.writeErrorWithException("XPATH FAILURE IN HELP: ", e1);
			return;
		}

		ArrayList<JMenu> menus = new ArrayList<JMenu>();

		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new FileReader(ModManager.getHelpFile()));
			Document doc = db.parse(is);

			//Parse items
			Element helpMenuElement = (Element) doc.getElementsByTagName("helpmenu").item(0);
			NodeList topLevelLists = (NodeList) sublistExpr.evaluate(helpMenuElement, XPathConstants.NODESET);
			for (int i = 0; i < topLevelLists.getLength(); i++) {
				Element topLevelList = (Element) topLevelLists.item(i);
				JMenu topMenu = new JMenu(topLevelList.getAttribute("title"));

				buildSublist(topMenu, topLevelList, false);
				helpMenu.add(topMenu);
			}

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			ModManager.debugLogger.writeErrorWithException("Error loading help file:", e);
		}
	}

	/**
	 * Recursively adds lists and elements to lists using XPATH
	 * 
	 * @param topMenu
	 *            Menu to add sublists and elements to
	 * @param xmlElem
	 * @throws XPathExpressionException
	 */
	private static void buildSublist(JMenu menu, Element xmlElem, boolean validateOnly) throws XPathExpressionException {
		NodeList sublistsOfMenu = (NodeList) sublistExpr.evaluate(xmlElem, XPathConstants.NODESET);
		for (int i = 0; i < sublistsOfMenu.getLength(); i++) {
			Element subListElem = (Element) sublistsOfMenu.item(i);
			JMenu subMenu = new JMenu(subListElem.getAttribute("title"));
			buildSublist(subMenu, subListElem, validateOnly);
			if (!validateOnly) {
				menu.add(subMenu);
			}
		}

		NodeList itemsOfMenu = (NodeList) helpItemExpr.evaluate(xmlElem, XPathConstants.NODESET);
		for (int i = 0; i < itemsOfMenu.getLength(); i++) {
			Element itemElem = (Element) itemsOfMenu.item(i);
			JMenuItem item = createMenuItemFromElement(itemElem, validateOnly);
			if (!validateOnly) {
				menu.add(item);
			}
		}
	}

	/**
	 * Creates a JMenuItem for this element. Fetches and validates resources
	 * only if the flag is set.
	 * 
	 * @param itemElem
	 *            Element to parse
	 * @param validateResource
	 *            Set to true to only validate resources (don't care about
	 *            return object).
	 * @return null if validated, jmenuitem otherwise
	 */
	private static JMenuItem createMenuItemFromElement(Element itemElem, boolean validateResource) {
		String resource = itemElem.getAttribute("resource");
		if (validateResource) {
			if (!resource.equals("")) {
				String resourceHash = itemElem.getAttribute("md5");
				String resSizeString = itemElem.getAttribute("size");
				long resourceSize = 0;
				if (!resSizeString.equals("")) {
					resourceSize = Long.parseLong(resSizeString);
				}

				File localResource = new File(ModManager.getHelpDir() + resource);
				if (!localResource.exists()) {
					//need to download resource.
					try {
						FileUtils.copyURLToFile(new URL(HELP_RESOURCES_LINK + resource), new File(ModManager.getHelpDir() + resource));
					} catch (MalformedURLException e1) {
						ModManager.debugLogger.writeErrorWithException("Malformed URL getting help resources:", e1);
					} catch (IOException e1) {
						ModManager.debugLogger.writeErrorWithException("I/O Exception getting help resource:", e1);
					}
				} else {
					//verify resource
					long localSize = new File(ModManager.getHelpDir() + resource).length();
					if (resourceSize > 0 && localSize != resourceSize) {
						//redownload
						try {
							ModManager.debugLogger.writeMessage("Resource sizes didn't match fail, downloading new version of resource.");
							FileUtils.copyURLToFile(new URL(HELP_RESOURCES_LINK + resource), new File(ModManager.getHelpDir() + resource));
						} catch (MalformedURLException e1) {
							ModManager.debugLogger.writeErrorWithException("Exception downloading help file:", e1);
						} catch (IOException e1) {
							ModManager.debugLogger.writeErrorWithException("Exception downloading help file:", e1);
						}
					} else {
						if (!resourceHash.equals("")) {
							try {
								String localMD5 = MD5Checksum.getMD5Checksum(ModManager.getHelpDir() + resource);
								if (!resourceHash.equals(localMD5)) {
									ModManager.debugLogger.writeMessage("Help resource hash fail, downloading new version of resource.");
									FileUtils.copyURLToFile(new URL(HELP_RESOURCES_LINK + resource), new File(ModManager.getHelpDir() + resource));
								}
							} catch (Exception e1) {
								ModManager.debugLogger.writeErrorWithException("Exception generating checksum for file:", e1);
							}
						}
					}
				}
			}
			return null;
		}

		String title = itemElem.getAttribute("title");
		String tooltipText = itemElem.getAttribute("tooltip");
		String url = itemElem.getAttribute("url");
		String content = itemElem.getTextContent();
		String modalTitle = itemElem.getAttribute("modaltitle");
		int messageType;
		switch (itemElem.getAttribute("modalicon")) {
		case "INFO":
			messageType = JOptionPane.INFORMATION_MESSAGE;
			break;
		case "WARNING":
			messageType = JOptionPane.WARNING_MESSAGE;
			break;
		case "ERROR":
			messageType = JOptionPane.ERROR_MESSAGE;
			break;
		default:
			messageType = JOptionPane.INFORMATION_MESSAGE;
		}

		JMenuItem subMenuItem = new JMenuItem(title);
		subMenuItem.setToolTipText(tooltipText);

		subMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!url.equals("")) {
					try {
						ModManager.openWebpage(new URL(url));
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else if (!resource.equals("")) {
					HelpItemPackage pack = new HelpItemPackage();
					pack.setModalMessage(content);
					pack.setResource(ModManager.getHelpDir() + resource);
					pack.setModalTitle(title);
					new ResourceWindowHelpModal(pack);
				} else {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "<html><div style=\"width: 300px\">" + content + "</div></html>",
							modalTitle, messageType);
				}
			}
		});

		return subMenuItem;
	}

	/**
	 * Reads the cached latesthelp.xml file in the data/help directory and
	 * parses it into menus to add
	 * 
	 * @return
	 */
	public static JMenu getHowDoIMenu_OLD() {
		JMenu menu = new JMenu("Commonly Asked Questions");
		ArrayList<HelpMenuItem> menuItems = new ArrayList<HelpMenuItem>();
		menuItems
				.add(new HelpMenuItem(
						"How do I add a mod",
						"Adding Mods to Mod Manager",
						"To add a mod to Mod Manager, you will need a Mod Manager mod package. This is a folder containing a valid moddesc.ini file.<br>Put this folder in the mods/ directory and then restart Mod Manager."));
		menuItems.add(new HelpMenuItem("How do I remove a mod", "Removing a mod from Mod Manager",
				"To remove a mod from Mod Manager, delete its folder from the mods/ directory."));
		menuItems
				.add(new HelpMenuItem(
						"How do I install a mod",
						"Install a mod",
						"Before installing a mod, make sure your DLC is backed up and you have created your repair game database. Then simply choose the mod on the left and click install. You may need to run ME3CMM.exe as administrator depending on where you put Mass Effect 3."));
		menuItems
				.add(new HelpMenuItem(
						"How do I uninstall a mod",
						"Uninstalling Mods",
						"Uninstalling mods means you are restoring files. You cannot selectively uninstall a mod with Mod Manager, you can only return to your snapshotted state or original DLC depending on how it was before you installed the mod. Due to how easy mods are to install this isn't much of an issue. Use the items in the restore menu to restore your files. Hover over the entries to get a description of what each does."));
		menuItems
				.add(new HelpMenuItem(
						"Can I use texture mods with Mod Manager",
						"Mixing Mod Types",
						"Mod Manager is not meant for installing texture mods, as it is full file replacement and texture mods modify a lot of files. As of 4.1 Beta 2 there is limited support for mixing Mod Manager mods and non Mod Manager mods.<br><br>First, backup your DLC with Mod Manager from the vanilla ME3 state.<br>Then install your texture mods. This will unpack your DLC so it is important you back it up with Mod Manager first. It will be very difficult to do this step (and have the game work or be restored) after you start using Mod Manager for mod management.<br>Now use Mod Manager, create your repair database, and use the option in the options menu to use game TOC files. You can then use Mod Manager like normal, but once you start using Mod Manager, modifications outside of it can and likely will break the game.<br><br>Mod Manager does not support mixing mods this way, so if you have issues with this method, you are on your own. There are simply too many different problems that can occur."));
		menuItems
				.add(new HelpMenuItem(
						"Can I get banned for using mods",
						"Ban Notice",
						"Technically you can with Multiplayer mods. However BioWare has no active staff to do so and they haven't banned anyone since early 2013. That doesn't mean you should run mega-million, 1 hit kill or masochist mods in public lobbies."));

		for (HelpMenuItem item : menuItems) {
			menu.add(item.createMenuItem());
		}

		JMenuItem dlcFailure = new JMenuItem("DLC is not authorized/not loading");
		dlcFailure.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//new DLCFailedWindow();
			}
		});
		menu.add(dlcFailure);

		return menu;

	}
}
