package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CardParser {
	private static TreeSet<CardPool> cardPoolList = new TreeSet<CardPool>();
	private static ArrayList<PackMetadata> metadataList = new ArrayList<PackMetadata>();
	public static HashMap<Integer, String> tlkMap;
	//public static TreeSet<Card> cardList;
	public static TreeSet<StorePack> packList;
	public static HashMap<String, StorePack> packnameMap;
	public static HashMap<String, TreeSet<RealCard>> carddataMap = new HashMap<String, TreeSet<RealCard>>();

	public static void main(String args[]) throws Exception {
		packnameMap = new HashMap<String, StorePack>();
		XPath xpath = XPathFactory.newInstance().newXPath();
		//String basepacklistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='packlist']/Value[@type='2']";
		String packlistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='packlist']/Value[@type=3]";
		String poolnameExpression = "/CoalesceAsset/Sections/Section/Property[@name='poolname']";
		String poolcontentsExpression = "../Property[@name='cardlist']/Value[@type=%d]"; //relative to poolname node
		String singlepoolcontentsExpression = "../Property[@name='cardlist' and @type=%d]";
		String carddatalistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='carddata']/Value[@type=%d]";
		String metadataExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='storeinfoarray']/Value[@type=3]";

		System.out.println("Loading TLK files");

		InputSource basegameSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_basegame.xml");
		InputSource patch1Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_patch1.xml");
		InputSource patch2Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_patch2.xml");
		InputSource testpatchSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_testpatch.xml");
		InputSource mp1Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_mp1.xml");
		InputSource mp2Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_mp2.xml");
		InputSource mp3Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_mp3.xml");
		InputSource mp4Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_mp4.xml");
		InputSource mp5Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_mp5.xml");
		InputSource liveiniSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "biogame_liveini.xml");

		InputSource livetlkSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator
				+ "me3tlk.xml");
		File dir = new File(System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "tlkfiles" + File.separator);
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter("xml"), TrueFileFilter.TRUE);
		ArrayList<InputSource> tlkSources = new ArrayList<InputSource>();
		for (File file : files) {
			//System.out.println("Loading TLK XML: " + file);
			InputSource source = new InputSource("file:///" + file);
			tlkSources.add(source);
		}

		tlkSources.add(livetlkSource);

		InputSource[] sources = new InputSource[] { basegameSource, testpatchSource, mp1Source, mp2Source, mp3Source, patch1Source, mp4Source,
				mp5Source, patch2Source, liveiniSource };

		//LOAD STRINGS INTO HASHMAP OF ID => STR
		tlkMap = new HashMap<Integer, String>();
		String tlkExpression = "/TlkFile/Strings/String";
		int numtlk = tlkSources.size();
		int numtlkloaded = 0;
		System.out.println("Loading TLK into hashmap");
		for (InputSource source : tlkSources) {
			//System.out.println("Parsing TLK file [" + (numtlkloaded + 1) + "/" + numtlk + "]");
			NodeList tlkNodes = (NodeList) xpath.evaluate(tlkExpression, source, XPathConstants.NODESET);
			if (tlkNodes != null && tlkNodes.getLength() > 0) {
				for (int i = 0; i < tlkNodes.getLength(); i++) {
					if (tlkNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) tlkNodes.item(i);
						int id = Integer.parseInt(el.getAttribute("id"));
						String str = el.getTextContent();
						tlkMap.put(id, StringEscapeUtils.escapeHtml4(str));
					}
				}
			}
			numtlkloaded++;
		}

		System.out.println("Reading cards and pools");
		for (InputSource inputSource : sources) {
			//PARSE - CARDLIST
			//System.out.println("Parsing card data");

			String expression = String.format(carddatalistExpression, 4);
			NodeList type4Nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
			if (type4Nodes != null && type4Nodes.getLength() > 0) {
				for (int i = 0; i < type4Nodes.getLength(); i++) {
					if (type4Nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) type4Nodes.item(i);
						RealCard multiCard = new RealCard(el.getTextContent());
						removeCardFromMap(multiCard);
					}
				}
			}

			// TYPE 3/2 ADDITION
			expression = carddatalistExpression;
			if (inputSource == basegameSource) {
				expression = String.format(carddatalistExpression, 2);
			} else {
				expression = String.format(carddatalistExpression, 3);
			}
			NodeList carddataNodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
			if (carddataNodes != null && carddataNodes.getLength() > 0) {
				for (int i = 0; i < carddataNodes.getLength(); i++) {
					if (carddataNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) carddataNodes.item(i);
						RealCard multiCard = new RealCard(el.getTextContent());
						addCardToMap(multiCard);
					}
				}
			}

			//PARSE - POOLS
			NodeList poolNodes = (NodeList) xpath.evaluate(poolnameExpression, inputSource, XPathConstants.NODESET);
			//System.out.println("Parsing pool data");
			if (poolNodes != null && poolNodes.getLength() > 0) {
				for (int i = 0; i < poolNodes.getLength(); i++) {
					if (poolNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element poolNameNode = (Element) poolNodes.item(i);
						CardPool pool = getPoolByName(poolNameNode.getTextContent());
						NodeList contentNodes = null;
						if (inputSource == basegameSource) {
							contentNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 2), poolNameNode, XPathConstants.NODESET);
						} else {
							contentNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 3), poolNameNode, XPathConstants.NODESET);
						}
						NodeList removeNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 4), poolNameNode,
								XPathConstants.NODESET);
						//GET CONTENTS OF POOL
						//REMOVE POOL CONTENTS
						if (removeNodes != null && removeNodes.getLength() > 0) {
							for (int x = 0; x < removeNodes.getLength(); x++) {
								if (removeNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
									Element removeNode = (Element) removeNodes.item(x);
									PoolCard multiCard = new PoolCard(removeNode.getTextContent());
									pool.removeCard(multiCard);
								}
							}
						}
						//ADD POOL CONTENTS
						if (contentNodes != null && contentNodes.getLength() > 0) {
							for (int x = 0; x < contentNodes.getLength(); x++) {
								if (contentNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
									Element contentNode = (Element) contentNodes.item(x);
									PoolCard multiCard = new PoolCard(contentNode.getTextContent());
									RealCard fetchedCard = getRealCardFromPoolCard(multiCard);
									if (fetchedCard == null) {
										System.err.println("Poolcard not in realcards: " + multiCard);
									}
									pool.addCard(multiCard);
								}
							}
						} else {
							//try a 1-item array search.
							if (inputSource == basegameSource) {
								contentNodes = (NodeList) xpath.evaluate(String.format(singlepoolcontentsExpression, 2), poolNameNode,
										XPathConstants.NODESET);
							} else {
								contentNodes = (NodeList) xpath.evaluate(String.format(singlepoolcontentsExpression, 3), poolNameNode,
										XPathConstants.NODESET);
							}
							//GET CONTENTS OF POOL
							if (contentNodes != null && contentNodes.getLength() > 0) {
								for (int x = 0; x < contentNodes.getLength(); x++) {
									if (contentNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
										Element contentNode = (Element) contentNodes.item(x);
										PoolCard singleCard = new PoolCard(contentNode.getTextContent());
										pool.addCard(singleCard);
									}
								}
							}
						}
					}
				}
			}
		} //END OF INPUT SOURCE ITERATION

		//PARSE - PACK METADATA
		System.out.println("Parsing pack metadata");
		NodeList metadataNodes = (NodeList) xpath.evaluate(metadataExpression, liveiniSource, XPathConstants.NODESET);
		if (metadataNodes != null && metadataNodes.getLength() > 0) {
			for (int i = 0; i < metadataNodes.getLength(); i++) {
				if (metadataNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) metadataNodes.item(i);
					String packData = el.getTextContent();
					if (packData.equals("null")) {
						continue;
					}

					PackMetadata packMetadata = new PackMetadata(packData);
					metadataList.add(packMetadata);
				}
			}
		} else {
			System.out.println("NO NODES!");
		}

		//PARSE - PACKS AND SLOTS
		System.out.println("Parsing store slots");
		NodeList nodes = (NodeList) xpath.evaluate(packlistExpression, liveiniSource, XPathConstants.NODESET);
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) nodes.item(i);
					String packData = el.getTextContent();
					if (packData.equals("null")) {
						continue;
					}

					PackSlot packslot = new PackSlot(packData);
					StorePack containingPack = packnameMap.get(packslot.getPackname());
					if (containingPack != null) {
						containingPack.addCardSlot(packslot);
					} else {
						StorePack newPack = new StorePack(packslot.getPackname());
						newPack.setMetadata(getMetadata(packslot.getPackname()));
						newPack.addCardSlot(packslot);
						packnameMap.put(packslot.getPackname(), newPack);
					}
				}
			}
		}
		
		//generate pack contents
		int num = 0;
		int numtodo = carddataMap.entrySet().size();

		//generate pack contents
		num = 0;
		numtodo = getNumCardsInMap();
		System.out.println("Building official/unofficial packs");

		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		sb.append("<li><h2 class=\"slidebar-header centered\">Standard Packs</h2></li>");
		//Build Store Files
		ArrayList<StorePack> officialPacks = new ArrayList<>();
		ArrayList<StorePack> unofficialPacks = new ArrayList<>();
		for (Entry<String, StorePack> entry : packnameMap.entrySet()) {
			if (entry.getValue().isOfficial()) {
				officialPacks.add(entry.getValue());
			} else {
				unofficialPacks.add(entry.getValue());
			}
		}

		Collections.sort(officialPacks);
		Collections.sort(unofficialPacks);

		ArrayList<StorePack> sortedPacks = new ArrayList<>();
		for (StorePack pack : officialPacks) {
			sortedPacks.add(pack);
		}
		for (StorePack pack : unofficialPacks) {
			sortedPacks.add(pack);
		}

		System.out.println("Building pack switch");

		StringBuilder packSwitch = new StringBuilder();
		packSwitch.append("<?php switch($packname){\n");
		for (StorePack pack : sortedPacks) {
			packSwitch.append("case '" + pack.getPackName() + "':\n\t$title = '" + pack.getHumanName() + "';\n");
			packSwitch.append("\tbreak;\n");
		}
		packSwitch.append("default:\n\t$title = 'Store Packs';\n");
		packSwitch.append("\tbreak;\n");
		packSwitch.append("}");
		packSwitch.append("?>");
		String savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "storedata"
				+ File.separator + "packtitleswitch.php";
		FileUtils.writeStringToFile(new File(savepath), packSwitch.toString());

		System.out.println("Building Pack Heading and store pack pages");
		boolean lastWasOfficial = true;
		for (StorePack pack : sortedPacks) {
			System.out.println("RewriteRule ^packs/"+pack.getHumanName().replaceAll(" ", "").toLowerCase()+"/?$ /store_catalog/packs.php?packname="+pack.getPackName()+" [L]");

			savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "storedata"
					+ File.separator + pack.getPackName() + ".html";
			FileUtils.writeStringToFile(new File(savepath), pack.getPackHTML());

			if (lastWasOfficial != pack.isOfficial()) {
				//generating first non official
				sb.append("<li><h2 class=\"slidebar-header centered\">Non-Standard Packs</h2></li>");
			}

			lastWasOfficial = pack.isOfficial();

			sb.append("<li><a id='" + pack.getPackName() + "' href='/store_catalog/packs/" + pack.getHumanName().replaceAll(" ", "").toLowerCase()
					+ "' title=\"" + pack.getDescription() + "\" data-packname='" + pack.getPackName() + "'>" + pack.getHumanName() + "</a></li>\n\t");
		}
		sb.append("</ul>");
		savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "storedata"
				+ File.separator + "packheading.html";
		FileUtils.writeStringToFile(new File(savepath), sb.toString());

		
		System.out.println("Forcing Card HTML to regenerate");

		for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
			for (RealCard card : entry.getValue()) {
				//System.out.println("Generating html [" + (num++) + "/" + numtodo + "] "+card);
				card.forceCardpageHTMLRegen();
			}
		}
		
		//Build pool files
		System.out.println("Build pool pages and pool header");

		sb = new StringBuilder();
		for (CardPool pool : cardPoolList) {
			//RewriteRule ^packs/collectorriflepack/?$ /store_catalog/packs.php?packname=collector7
			savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "pooldata"
					+ File.separator + pool.getPoolname().toLowerCase() + ".html";
			FileUtils.writeStringToFile(new File(savepath), pool.getPoolHTML());
			sb.append("<li><a id='" + pool.getPoolname() + "' href='/store_catalog/pools/" + pool.getPoolname() + "' data-poolname='"
					+ pool.getPoolname() + "'>" + pool.getPoolname() + "</a></li>\n\t");
		}
		sb.append("</ul>");
		savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "pooldata"
				+ File.separator + "poolheading.html";
		FileUtils.writeStringToFile(new File(savepath), sb.toString());

		//cleaning cards
		System.out.println("Cleaning unnecessary cards");
		for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
			//System.out.println("Removing unneeded cards [" + (num++) + "/" + numtodo + "]");
			ArrayList<RealCard> cardsToRemove = new ArrayList<RealCard>(); //clean out idx = -1 cards for consumables, gear, etc. they are cloned as necessary
			for (RealCard card : entry.getValue()) {
				if ((card.isConsumable && card.getVersionIdx() == -1
						&& !card.getUniqueName().equals("SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Ammo")
						&& !card.getUniqueName().equals("SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Shield")
						&& !card.getUniqueName().equals("SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Revive") && !card.getUniqueName().equals(
						"SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Rocket"))) {
					cardsToRemove.add(card);
				}
				if (card.getUniqueName().equals("MPCredits") && card.getPVIncrementBonus() == -1) {
					cardsToRemove.add(card);
				}
			}
			entry.getValue().removeAll(cardsToRemove);
		}
		
		System.out.println("Forcing Card HTML to regenerate");

		for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
			for (RealCard card : entry.getValue()) {
				//System.out.println("Generating html [" + (num++) + "/" + numtodo + "] "+card);
				card.forceCardpageHTMLRegen();
			}
		}

		//build cards page
		sb = new StringBuilder();
		String previousCategory = "";
		TreeSet<RealCard> allcards = new TreeSet<RealCard>();
		System.out.println("Gathering all cards into single set");
		for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
			for (RealCard card : entry.getValue()) { //
				/*
				 * if (card.getUseVersionIdx() && card.getVersionIdx() < 0 &&
				 * !card.getCategoryName().equals("weapons") &&
				 * !card.getCategoryName().equals("weaponmods") &&
				 * !card.getCategoryName().equals("misc") &&
				 * !card.getRarity().equals(Card.Rarity.Unused)) { continue; }
				 */
				boolean cardAdded = allcards.add(card);
				if (!cardAdded) {
					System.err.println("DUPLICATE CARD REJECTED: "+card);
				}
			}
		}

		System.out.println("Building card category switch/card heading/card pages");

		HashMap<String, String> cardTypes = new HashMap<String, String>();
		StringBuilder cardtypeSwitch = new StringBuilder();
		cardtypeSwitch.append("<?php switch($cardtype){\n");
		for (RealCard card : allcards) { //
			if (!card.getCategoryName().equals(previousCategory)) {
				if (!previousCategory.equals("")) {
					sb.append("</div>");
					String fname = previousCategory;
					if (fname.equals("misc")) {
						fname = "miscellaneous";
					}
					if (fname.equals("kits")) {
						fname = "characters";
					}
					savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator
							+ "carddata" + File.separator + fname + ".html";
					System.out.println("Writing card page " + fname);
					FileUtils.writeStringToFile(new File(savepath), sb.toString());
				}
				sb = new StringBuilder();
				switch (card.getCategoryName()) {
				case "gear":
					cardtypeSwitch.append("case 'gear':\n\t$title = 'Gear';\n");
					cardtypeSwitch.append("\tbreak;\n");
					cardTypes.put("Gear", "gear");
					sb.append("<h2 id='card_page_title' class='dark centered'>Gear Cards</h2>\n");
					sb.append("<p class='dark centered'>Gear drops up to five times each.</p>\n");
					break;
				case "kits":
					cardtypeSwitch.append("case 'kits':\n\t$title = 'Character';\n");
					cardtypeSwitch.append("\tbreak;\n");
					sb.append("<h2 id='card_page_title' class='dark centered'>Characters Cards</h2>\n");
					cardTypes.put("Characters", "characters");
					sb.append("<p class='dark centered'>Common characters will continue to drop in packs even after they are maxed out. Higher tier packs will yield higher XP on cards.</p>\n");
					break;
				case "consumables":
					cardtypeSwitch.append("case 'consumables':\n\t$title = 'Consumable';\n");
					cardtypeSwitch.append("\tbreak;\n");
					cardTypes.put("Consumables", "consumables");
					sb.append("<h2 id='card_page_title' class='dark centered'>Consumable Cards</h2>\n");
					sb.append("<p class='dark centered'>Consumables cap out at 255 while saving, but the game will still go over 255 if they appear in a pack you obtain.</p>\n");
					break;
				case "weaponmods":
					cardtypeSwitch.append("case 'weaponmods':\n\t$title = 'Weapon Mod';\n");
					cardtypeSwitch.append("\tbreak;\n");
					cardTypes.put("Weapon Mods", "weaponmods");
					sb.append("<h2 id='card_page_title' class='dark centered'>Weapon Mod Cards</h2>\n");
					sb.append("<p class='dark centered'>Weapon Mods drop up to 5 times each.</p>\n");
					break;
				case "misc":
					cardtypeSwitch.append("case 'misc':\n\t$title = 'Miscellaneous';\n");
					cardtypeSwitch.append("\tbreak;\n");
					cardTypes.put("Miscellaneous", "miscellaneous");
					sb.append("<h2 id='card_page_title' class='dark centered'>Miscellaneous Cards</h2>\n");
					sb.append("<p class='dark centered'>Miscellaneous items will drop until their max counts are reached. Credits are only dropped if EA custom support gives you the entitlement to the pack.</p>\n");
					break;
				case "weapons":
					cardtypeSwitch.append("case 'weapons':\n\t$title = 'Weapon';\n");
					cardtypeSwitch.append("\tbreak;\n");
					cardTypes.put("Weapons", "weapons");
					sb.append("<h2 id='card_page_title' class='dark centered'>Weapon Cards</h2>\n");
					sb.append("<p class='dark centered'>Weapons can each drop up to 10 times.</p>\n");
					break;
				}
				sb.append("<div><input id='glowsearch' placeholder=\"Find a card...\"/></div><div class='cardslist'>");
			}
			previousCategory = card.getCategoryName();
			sb.append(card.getCardpageHTML());
		}

		//write final category
		savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "carddata"
				+ File.separator + previousCategory + ".html";
		System.out.println("Writing card page " + previousCategory);
		FileUtils.writeStringToFile(new File(savepath), sb.toString());

		//write card switch
		cardtypeSwitch.append("default:\n\t$title = 'Card List';\n");
		cardtypeSwitch.append("\tbreak;\n");
		cardtypeSwitch.append("}");
		cardtypeSwitch.append("?>");
		savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "carddata"
				+ File.separator + "cardtypeswitch.php";
		FileUtils.writeStringToFile(new File(savepath), cardtypeSwitch.toString());

		//card types
		
		sb = new StringBuilder();
		sb.append("<li><h2 id='card_type_title' class=\"slidebar-header centered\">Card Types</h2></li>\n\t");
		Iterator<Entry<String, String>> it = cardTypes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			String str = pair.getValue();
			String title = pair.getKey();
			sb.append("<li><a id='" + str + "' href='/store_catalog/cards/" + str + "' title=\"" + title + "\" data-cardtype='" + str + "'>" + title
					+ "</a></li>\n\t");
			it.remove(); // avoids a ConcurrentModificationException
		}
		sb.append("</ul>");

		savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "catdata" + File.separator + "carddata"
				+ File.separator + "cardheading.html";
		FileUtils.writeStringToFile(new File(savepath), sb.toString());
	}

	private static PackMetadata getMetadata(String packname) {
		for (PackMetadata md : metadataList) {
			if (md.getPackname().equals(packname)) {
				return md;
			}
		}
		System.err.println("DIDN'T FIND PACK METADATA!");
		return null;
	}

	/**
	 * Creates a pool if one does not exist by the specified name. Returns the
	 * existing one if one already does. If the pool does not exist, it will be
	 * added and returned from the list.
	 * 
	 * This does not return a pool associated with the store pack, as those ones
	 * determine the weight, but not the contents.
	 * 
	 * @param poolname
	 *            Pool to find
	 * @return pool
	 */
	public static CardPool getPoolByName(String poolname) {
		for (CardPool pool : cardPoolList) {
			if (pool.getPoolname().equals(poolname)) {
				return pool;
			}
		}
		CardPool pool = new CardPool("(PoolName=\"" + poolname + "\")");
		cardPoolList.add(pool);
		return pool;
	}

	public static void addCardToMap(RealCard card) {
		TreeSet<RealCard> cardset = carddataMap.get(card.getUniqueName());
		if (cardset == null) {
			cardset = new TreeSet<RealCard>();
			carddataMap.put(card.getUniqueName(), cardset);
		}
		cardset.add(card);
	}

	public static boolean removeCardFromMap(RealCard card) {
		TreeSet<RealCard> cardset = carddataMap.get(card.getUniqueName());
		if (cardset != null)
			return cardset.remove(card);
		return false;
	}

	public static int getNumCardsInMap() {
		int count = 0;
		for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
			TreeSet<RealCard> cardset = entry.getValue();
			count += cardset.size();
		}
		return count;
	}

	public static RealCard getRealCardFromPoolCard(PoolCard card) {
		TreeSet<RealCard> cardset = carddataMap.get(card.getUniqueName());
		if (cardset == null || cardset.size() == 0) {
			return null;
		}
		boolean isCharCard = false;

		if (card.getCategoryName().equals("kits")) {
			isCharCard = true;
		}
		boolean isConsumable = false;
		if (card.getCategoryName().equals("consumables")) {
			isConsumable = true;
		}
		boolean isMisc = false;
		if (card.getCategoryName().equals("misc")) {
			isMisc = true;
		}

		for (RealCard rcard : cardset) {
			if (rcard.getPVIncrementBonus() == card.getPVIncrementBonus() || card.getCategoryName().equals("weapons") /*
																													 * we
																													 * ignore
																													 * weapon
																													 * pv
																													 */) {
				if (rcard.getUseVersionIdx()) {
					if (card.getVersionIdx() == rcard.getVersionIdx()) {
						return rcard;
					}
				} else {
					return rcard;
				}
			}
		}

		if (isMisc) {
			RealCard maxidxcard = cardset.first();
			RealCard cloneCard = new RealCard(maxidxcard); //clonecard
			cloneCard.setPVIncrementBonus(card.getPVIncrementBonus());
			cardset.add(cloneCard);
			return cloneCard;
		}

		if (isCharCard) {
			RealCard maxidxcard = null;
			for (RealCard rcard : cardset) {
				if (maxidxcard != null) {
					if (rcard.getVersionIdx() > card.getVersionIdx()) {
						maxidxcard = rcard;
					}
				} else {
					maxidxcard = rcard;
				}
			}
			return maxidxcard;
		}
		if (isConsumable) {
			//System.out.println("Cloning consumable and adding to set. Set size now "+cardset.size());
			RealCard maxidxcard = cardset.first();
			RealCard cloneCard = new RealCard(maxidxcard); //clonecard
			cloneCard.setVersionIdx(card.getVersionIdx());
			if (card.getRarity() != null) {
				cloneCard.setRarity(card.getRarity());
			}
			cloneCard.setPVIncrementBonus(card.getPVIncrementBonus());
			cloneCard.getCardpageHTML();
			cardset.add(cloneCard);
			//System.out.println("Cloned and added to set. Set size now "+cardset.size());
			return cloneCard;
		}

		return null;
	}

	public static CardPool getCardPoolByName(String poolname) {
		for (CardPool pool : cardPoolList) {
			if (pool.getPoolname().equals(poolname)) {
				return pool;
			}
		}
		System.out.println("DID NOT FIND POOL: " + poolname);
		return null;
	}
}
