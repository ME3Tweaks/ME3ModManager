package com.me3tweaks.modmanager.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

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

import com.me3tweaks.modmanager.AboutWindow2;
import com.me3tweaks.modmanager.LogOptionsWindow;
import com.me3tweaks.modmanager.LogWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class HelpMenu {

	public final static String HELP_LINK = "https://me3tweaks.com/modmanager/help/latesthelp.xml";
	public final static String HELP_RESOURCES_LINK = "https://me3tweaks.com/modmanager/help/resources/";

	private static XPath xpath = XPathFactory.newInstance().newXPath();
	private static XPathExpression helpItemExpr;
	private static XPathExpression sublistExpr;
	private static XPathExpression topLevelExpr;

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
			FileUtils.writeStringToFile(ModManager.getHelpFile(), responseString, StandardCharsets.UTF_8);
			ModManager.debugLogger.writeMessage("File written to disk. Exists on filesystem, ready for loading: " + ModManager.getHelpFile().exists());
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
		JMenuItem helpModDescDocumentation, helpForums, helpAbout, helpLogViewer, helpGetLog, helpContactMgamerz;

		helpModDescDocumentation = new JMenuItem("ModDesc File Documentation");
		helpModDescDocumentation.setToolTipText("Opens the documentation for Mod Manager's moddesc.ini format");

		helpAbout = new JMenuItem("About Mod Manager");
		helpAbout.setToolTipText("<html>Shows credits for Mod Manager and source code information</html>");

		helpGetLog = new JMenuItem("Copy log to clipboard");
		helpGetLog.setToolTipText("<html>Flushes the log to disk and then copies it to the clipboard</html>");
		helpGetLog = new JMenuItem("Generate Diagnostics Log");
		helpGetLog.setToolTipText(
				"<html>Allows you to generate a Mod Manager log with diagnostic information for Mgamerz and Mod Developers.<br>Allows you to automatically upload to PasteBin for super easy sharing.</html>");

		helpLogViewer = new JMenuItem("View Mod Manager log");
		helpLogViewer.setToolTipText("<html>View the current session log</html>");

		helpContactMgamerz = new JMenuItem("Contacting Mgamerz");
		helpContactMgamerz.setToolTipText("<html>How to contact Mgamerz</html>");

		HelpMenu.insertLocalHelpMenus(helpMenu);
		helpMenu.addSeparator();
		helpMenu.add(helpLogViewer);
		helpMenu.add(helpGetLog);
		helpMenu.add(helpContactMgamerz);
		helpMenu.addSeparator();
		helpMenu.add(helpModDescDocumentation);
		helpMenu.add(helpAbout);

		helpModDescDocumentation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				URI theURI;
				try {
					theURI = new URI("https://me3tweaks.com/modmanager/documentation/moddesc");
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
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "You must enable logging via the Actions > Options menu before logs are generated.",
							"Logging disabled", JOptionPane.ERROR_MESSAGE);
				} else {
					copyLogToClipboard();
				}
			}
		});
		helpContactMgamerz.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW,
						"<html><div style=\"width:400px;\">Mgamerz (also known as FemShep) is the developer of this program.<br>" + "Please use the ME3Tweaks Discord link in the help menu to contact me.<br>"
								+ "If you have a crash or a bug I will need the Mod Manager log:<br><br>"
								+ "1. Close Mod Manager with logging enabled. Restart Mod Manager, and reproduce your issue.<br>"
								+ "2. Immediately after the issue occurs, go to Help > Generate Diagnostics Log.<br>"
								+ "3. Leave the default options unless instructed otherwise. Upload your log to pastebin via the button.<br>"
								+ "4. In your message on the ME3Tweaks Discord, give me a description of the problem and the steps you took to produce it. INCLUDE THE PASTEBIN LINK.<br>  "
								+ "I will not look into the log to attempt to figure what issue you are having if you don't give me a description.<br>"
								+ "Please do not do any other operations as it makes the logs harder to read.<br>"
								+ "If you submit a crash/bug report without a Mod Manager log there is very little I can do to help you.<br>"
								+ "Please note that I only speak English.</div></html>",
						"Contacting Mgamerz", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		helpAbout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutWindow2();
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
			topLevelExpr = xpath.compile("helpitem|list");
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

			ArrayList<SortableHelpElement> items = new ArrayList<>();
			NodeList topLevelItems = (NodeList) topLevelExpr.evaluate(helpMenuElement, XPathConstants.NODESET);
			for (int i = 0; i < topLevelItems.getLength(); i++) {
				Element topLevelList = (Element) topLevelItems.item(i);
				items.add(new SortableHelpElement(topLevelList));
			}

			Collections.sort(items);
			for (SortableHelpElement sortedItem : items) {
				Element helpElement = sortedItem.element;
				String nodetype = helpElement.getTagName();
				switch (nodetype) {
				case "list":
					JMenu topMenu = new JMenu(helpElement.getAttribute("title"));
					buildSublist(topMenu, helpElement, false);
					helpMenu.add(topMenu);
					break;
				case "helpitem":
					JMenuItem item = createMenuItemFromElement(helpElement, false);
					helpMenu.add(item);
					break;
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException |

				XPathExpressionException e) {
			ModManager.debugLogger.writeErrorWithException("Error loading help file:", e);
		}
	}

	/**
	 * Recursively adds lists and elements to lists using XPATH
	 *
	 * @throws XPathExpressionException
	 */
	private static void buildSublist(JMenu menu, Element xmlElem, boolean validateOnly) throws XPathExpressionException {
		ArrayList<SortableHelpElement> items = new ArrayList<>();
		NodeList sublistsOfMenu = (NodeList) sublistExpr.evaluate(xmlElem, XPathConstants.NODESET);
		for (int i = 0; i < sublistsOfMenu.getLength(); i++) {
			Element subListElem = (Element) sublistsOfMenu.item(i);
			items.add(new SortableHelpElement(subListElem));
		}

		NodeList itemsOfMenu = (NodeList) helpItemExpr.evaluate(xmlElem, XPathConstants.NODESET);
		for (int i = 0; i < itemsOfMenu.getLength(); i++) {
			Element itemElem = (Element) itemsOfMenu.item(i);
			items.add(new SortableHelpElement(itemElem));
		}

		Collections.sort(items);
		for (SortableHelpElement sortedItem : items) {
			Element helpElement = sortedItem.element;
			String nodetype = helpElement.getTagName();
			switch (nodetype) {
			case "list":
				JMenu subMenu = new JMenu(helpElement.getAttribute("title"));
				buildSublist(subMenu, helpElement, validateOnly);
				if (!validateOnly) {
					menu.add(subMenu);
				}
				break;
			case "helpitem":
				JMenuItem item = createMenuItemFromElement(helpElement, validateOnly);
				if (!validateOnly && item != null) {
					menu.add(item);
				}
				break;
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
		if (tooltipText == null || tooltipText.equals("")) {
			tooltipText = itemElem.getAttribute("url");
		}
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
		if (tooltipText != null && !tooltipText.equals("")) {
			subMenuItem.setToolTipText(tooltipText);
		}

		subMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!url.equals("")) {
					try {
						ResourceUtils.openWebpage(new URL(url));
					} catch (MalformedURLException e1) {
						ModManager.debugLogger.writeErrorWithException("Help item has invalid URL:", e1);
					}
				} else if (!resource.equals("")) {
					HelpItemPackage pack = new HelpItemPackage();
					pack.setModalMessage(content);
					pack.setResource(ModManager.getHelpDir() + resource);
					pack.setModalTitle(title);
					new ResourceWindowHelpModal(pack);
				} else {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "<html><div style=\"width: 300px\">" + content + "</div></html>", modalTitle, messageType);
				}
			}
		});

		return subMenuItem;
	}

	private static class SortableHelpElement implements Comparable<SortableHelpElement> {
		@Override
		public String toString() {
			return element.getAttribute("title");
		}

		private Element element;

		public SortableHelpElement(Element element) {
			this.element = element;
		}

		@Override
		public int compareTo(SortableHelpElement other) {
			Integer mypriority = getPriorityValue(element.getAttribute("sort"));
			Integer otherpriority = getPriorityValue(other.element.getAttribute("sort"));
			return mypriority.compareTo(otherpriority);
		}

		private int getPriorityValue(String priority) {
			if (priority == null || priority.equals(""))
				return 0;
			switch (priority) {
			case "low":
				return 1;
			case "medium":
				return 0;
			case "high":
				return -1;
			default:
				return 0;
			}
		}
	}
}