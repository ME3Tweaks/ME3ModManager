package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
	private static TreeSet<Pool> poolList = new TreeSet<Pool>();
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

		InputSource basegameSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_basegame.xml");
		InputSource patch1Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_patch1.xml");
		InputSource patch2Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_patch2.xml");
		InputSource testpatchSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_testpatch.xml");
		InputSource mp1Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_mp1.xml");
		InputSource mp2Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_mp2.xml");
		InputSource mp3Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_mp3.xml");
		InputSource mp4Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_mp4.xml");
		InputSource mp5Source = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_mp5.xml");
		InputSource liveiniSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "biogame_liveini.xml");

		InputSource livetlkSource = new InputSource("file:///" + System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "me3tlk.xml");
		File dir = new File(System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "tlkfiles" + File.separator);
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter("xml"), TrueFileFilter.TRUE);
		ArrayList<InputSource> tlkSources = new ArrayList<InputSource>();
		for (File file : files) {
			System.out.println("Loading TLK XML: "+file);
			InputSource source = new InputSource("file:///" + file);
			tlkSources.add(source);
		}

		tlkSources.add(livetlkSource);

		InputSource[] sources = new InputSource[] { basegameSource, testpatchSource, mp1Source, mp2Source, mp3Source, patch1Source, mp4Source, mp5Source, patch2Source,
				liveiniSource };

		//LOAD STRINGS INTO HASHMAP OF ID => STR
		tlkMap = new HashMap<Integer, String>();
		String tlkExpression = "/TlkFile/Strings/String";
		for (InputSource source : tlkSources) {
			NodeList tlkNodes = (NodeList) xpath.evaluate(tlkExpression, source, XPathConstants.NODESET);
			if (tlkNodes != null && tlkNodes.getLength() > 0) {
				for (int i = 0; i < tlkNodes.getLength(); i++) {
					if (tlkNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) tlkNodes.item(i);
						int id = Integer.parseInt(el.getAttribute("id"));
						String str = el.getTextContent();
						//System.out.println(Integer.parseInt(el.getAttribute("id"))+": "+ el.getTextContent());
						tlkMap.put(id, StringEscapeUtils.escapeHtml4(str));
					}
				}
			}
		}

		for (InputSource inputSource : sources) {
			//PARSE - CARDLIST
			String expression = String.format(carddatalistExpression, 4);
			NodeList type4Nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
			//System.out.println("Number of nodes: " + carddataNodes.getLength());
			if (type4Nodes != null && type4Nodes.getLength() > 0) {
				//	System.out.println("Number of Type 4: " + type4Nodes.getLength());
				for (int i = 0; i < type4Nodes.getLength(); i++) {
					if (type4Nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) type4Nodes.item(i);
						RealCard multiCard = new RealCard(el.getTextContent());
						if (RealCard.getHumanName(multiCard.getUniqueName()).equals("Ops Survival Pack Capacity Upgrade")) {
							//System.out.println("REMOVE BREAK");
						}
						boolean removed = removeCardFromMap(multiCard);
						if (removed) {
							//System.out.println("REMOVED A CARD: " + multiCard);
						} else {
							//System.out.println("Didn't remove card: "+multiCard);
						}
						//System.out.println("CARD REM [" + (removed ? "OK" : "MISS") + "]: " + multiCard);
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
			//System.out.println("Number of nodes: " + carddataNodes.getLength());
			if (carddataNodes != null && carddataNodes.getLength() > 0) {
				for (int i = 0; i < carddataNodes.getLength(); i++) {
					if (carddataNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) carddataNodes.item(i);
						RealCard multiCard = new RealCard(el.getTextContent());
						addCardToMap(multiCard);
						//System.out.println("CARD ADD: " + multiCard);

						//System.out.println(multiCard.getCardHTML());
					}
				}
			}

			//PARSE - POOLS
			NodeList poolNodes = (NodeList) xpath.evaluate(poolnameExpression, inputSource, XPathConstants.NODESET);
			//System.out.println("Number of nodes: " + poolNodes.getLength());
			if (poolNodes != null && poolNodes.getLength() > 0) {
				for (int i = 0; i < poolNodes.getLength(); i++) {
					if (poolNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element poolNameNode = (Element) poolNodes.item(i);
						Pool pool = getPoolByName(poolNameNode.getTextContent());
						NodeList contentNodes = null;
						if (inputSource == basegameSource) {
							contentNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 2), poolNameNode, XPathConstants.NODESET);
						} else {
							contentNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 3), poolNameNode, XPathConstants.NODESET);
						}
						NodeList removeNodes = (NodeList) xpath.evaluate(String.format(poolcontentsExpression, 4), poolNameNode, XPathConstants.NODESET);
						//GET CONTENTS OF POOL
						//REMOVE POOL CONTENTS
						if (removeNodes != null && removeNodes.getLength() > 0) {
							for (int x = 0; x < removeNodes.getLength(); x++) {
								if (removeNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
									Element removeNode = (Element) removeNodes.item(x);
									PoolCard multiCard = new PoolCard(removeNode.getTextContent());
									pool.removeCard(multiCard);
									//System.out.println("REMOVED CARD: " + multiCard);
								}
							}
						}
						//ADD POOL CONTENTS
						if (contentNodes != null && contentNodes.getLength() > 0) {
							for (int x = 0; x < contentNodes.getLength(); x++) {
								if (contentNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
									Element contentNode = (Element) contentNodes.item(x);
									//System.out.println("adding card to pool: "+contentNode.getTextContent());
									PoolCard multiCard = new PoolCard(contentNode.getTextContent());
									RealCard fetchedCard = getRealCardFromPoolCard(multiCard);
									if (fetchedCard == null) {
										System.err.println("Card not in realcards: " + multiCard);
									}
									pool.addCard(multiCard);
								}
							}
						} else {
							//try a 1-item array search.
							if (inputSource == basegameSource) {
								contentNodes = (NodeList) xpath.evaluate(String.format(singlepoolcontentsExpression, 2), poolNameNode, XPathConstants.NODESET);
							} else {
								contentNodes = (NodeList) xpath.evaluate(String.format(singlepoolcontentsExpression, 3), poolNameNode, XPathConstants.NODESET);
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

				/*
				 * for (Pool pool : poolList) { System.out.println("::" +
				 * pool.getPoolname() + "::"); for (Card card :
				 * pool.getPoolContents()) { System.out.println("     " +
				 * card.getCardDisplayString()); } System.out.println(); }
				 * 
				 * if (true) { System.exit(0); }
				 */
			}
		} //END OF INPUT SOURCE ITERATION

		//PARSE - PACK METADATA
		NodeList metadataNodes = (NodeList) xpath.evaluate(metadataExpression, liveiniSource, XPathConstants.NODESET);
		if (metadataNodes != null && metadataNodes.getLength() > 0) {
			for (int i = 0; i < metadataNodes.getLength(); i++) {
				if (metadataNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) metadataNodes.item(i);
					String packData = el.getTextContent();
					if (packData.equals("null")) {
						continue;
					}

					//System.out.println("parsing: "+packData);
					PackMetadata packMetadata = new PackMetadata(packData);
					metadataList.add(packMetadata);
				}
			}
		} else {
			System.out.println("NO NODES!");
		}

		//PARSE - PACKS AND SLOTS
		NodeList nodes = (NodeList) xpath.evaluate(packlistExpression, liveiniSource, XPathConstants.NODESET);
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) nodes.item(i);
					String packData = el.getTextContent();
					if (packData.equals("null")) {
						continue;
					}

					//System.out.println("parsing: "+packData);
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

			//generate pack contents
			int num = 0;
			int numtodo = getNumCardsInMap();

			for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
				ArrayList<RealCard> cardsToRemove = new ArrayList<RealCard>(); //clean out idx = -1 cards for consumables, gear, etc. they are cloned as necessary
				for (RealCard card : entry.getValue()) {
					if (card.isConsumable && card.getVersionIdx() == -1) {
						cardsToRemove.add(card);
					}
				}
				entry.getValue().removeAll(cardsToRemove);
			}

			for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
				for (RealCard card : entry.getValue()) {
					System.out.println("Generating html [" + (num++) + "/" + numtodo + "]");
					card.getCardHTML();
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append("<ul>");
			for (Entry<String, StorePack> entry : packnameMap.entrySet()) {
				StorePack pack = entry.getValue();
				System.out.println(
						"RewriteRule ^packs/" + pack.getHumanName().replaceAll(" ", "").toLowerCase() + "/?$ /store_catalog/packs.php?packname=" + pack.getPackName() + " [L]");
				String savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "packcontents" + File.separator + pack.getPackName() + ".html";
				FileUtils.writeStringToFile(new File(savepath), pack.getPackHTML());

				sb.append("<li><a id='" + pack.getPackName() + "' href='/store_catalog/packs/" + pack.getHumanName().replaceAll(" ", "").toLowerCase() + "' title=\""
						+ pack.getDescription() + "\" data-packname='" + pack.getPackName() + "'>" + pack.getHumanName() + "</a></li>\n\t");
			}
			sb.append("</ul>");
			String savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "packcontents" + File.separator + "packheading.html";
			FileUtils.writeStringToFile(new File(savepath), sb.toString());

			//build cards page
			sb = new StringBuilder();
			String previousCategory = "";
			TreeSet<RealCard> allcards = new TreeSet<RealCard>();

			for (Entry<String, TreeSet<RealCard>> entry : carddataMap.entrySet()) {
				for (RealCard card : entry.getValue()) { //
					/*
					 * if (card.getUseVersionIdx() && card.getVersionIdx() < 0
					 * && !card.getCategoryName().equals("weapons") &&
					 * !card.getCategoryName().equals("weaponmods") &&
					 * !card.getCategoryName().equals("misc") &&
					 * !card.getRarity().equals(Card.Rarity.Unused)) { continue;
					 * }
					 */
					allcards.add(card);
				}
			}

			for (RealCard card : allcards) { //
				if (!card.getCategoryName().equals(previousCategory)) {
					sb.append("<hr class='dark_hr_center'>\n");
					switch (card.getCategoryName()) {
					case "gear":
						sb.append("<h3 class='dark centered'>Gear</h3>\n");
						sb.append("<p class='dark centered'>Gear drops up to five times each.</p>\n");
						break;
					case "kits":
						sb.append("<h3 class='dark centered'>Characters</h3>\n");
						sb.append(
								"<p class='dark centered'>Common characters will continue to drop in packs even after they are maxed out. Higher tier packs will yield higher XP on cards.</p>\n");

						break;
					case "consumables":
						sb.append("<h3 class='dark centered'>Consumables</h3>\n");
						sb.append(
								"<p class='dark centered'>Consumables cap out at 255 while saving, but the game will still go over 255 if they appear in a pack you obtain.</p>\n");

						break;
					case "weaponmods":
						sb.append("<h3 class='dark centered'>Weapon Mods</h3>\n");
						sb.append("<p class='dark centered'>Weapon Mods drop up to 5 times each.</p>\n");
						break;
					case "misc":
						sb.append("<h3 class='dark centered'>Miscellaneous</h3>\n");
						sb.append(
								"<p class='dark centered'>Miscellaneous items will drop until their max counts are reached. Credits are only dropped if EA custom support gives you the entitlement to the pack.</p>\n");
						break;
					case "weapons":
						sb.append("<h3 class='dark centered'>Weapons</h3>\n");
						sb.append("<p class='dark centered'>Weapons can each drop up to 10 times.</p>\n");
						break;
					}
				}
				previousCategory = card.getCategoryName();
				//System.out.println(card.getCardDisplayString());
				sb.append(card.getCardHTML());
			}
			savepath = System.getProperty("user.dir") + File.separator + "carddata" + File.separator + "packcontents" + File.separator + "cardlist.html";
			FileUtils.writeStringToFile(new File(savepath), sb.toString());

		}
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
	 * Creates a pool if one does not exist by the specified name. REtunrs the
	 * existing one if one already does. If the pool does not exist, it will be
	 * added and returned from the list.
	 * 
	 * @param poolname
	 *            Pool to find
	 * @return pool
	 */
	public static Pool getPoolByName(String poolname) {
		for (Pool pool : poolList) {
			if (pool.getPoolname().equals(poolname)) {
				return pool;
			}
		}
		Pool pool = new Pool("(PoolName=\"" + poolname + "\")");
		poolList.add(pool);
		return pool;
	}

	/*
	 * public static RealCard getCardByName(String uniqueName, int versionIdx,
	 * RealCard.Rarity rarity) { boolean isCharCard =
	 * RealCard.IsCharacterCard(uniqueName);
	 * 
	 * ArrayList<RealCard> samenameCards = new ArrayList<>(); for (Entry<String,
	 * TreeSet<RealCard>> entry : carddataMap.entrySet()) { for (RealCard card :
	 * entry.getValue()){ if (card.getUniqueName().equals(uniqueName)) {
	 * samenameCards.add(card); if (card.getUseVersionIdx()) { if
	 * (card.getVersionIdx() == versionIdx) { //System.out.println(
	 * "Found card by name and IDX: " + uniqueName); return card; } } else {
	 * //System.out.println("Found card by name only: " + uniqueName); return
	 * card; } } }
	 */

	//hasn't been found yet
	/*
	 * for (Card card : samenameCards) { if (card.getUseVersionIdx() ||
	 * isCharCard) { //it's probably not defined... somehow, the game accepts
	 * this. Card cloneCard = new Card(card); }
	 * extrageneratedCards.add(cloneCard); return cloneCard; } }
	 */

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

		for (RealCard rcard : cardset) {
			if (rcard.getUseVersionIdx()) {
				if (card.getVersionIdx() == rcard.getVersionIdx()) {
					//System.out.println("Found card by name and IDX: " + uniqueName);
					return rcard;
				}
			} else {
				if (card.getUniqueName().equals("MPCredits") && card.getPVIncrementBonus()!=rcard.getPVIncrementBonus()) {
					RealCard cloneCard = new RealCard(rcard); //clonecard
					cloneCard.setVersionIdx(card.getVersionIdx());
					if (card.getRarity() != null) {
						cloneCard.setRarity(card.getRarity());
					}
					cloneCard.setPVIncrementBonus(card.getPVIncrementBonus());
					cardset.add(cloneCard);
					return cloneCard;
				}
				//System.out.println("Found card by name only: " + uniqueName);
				return rcard;
			}
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
			RealCard maxidxcard = cardset.first();
			RealCard cloneCard = new RealCard(maxidxcard); //clonecard
			cloneCard.setVersionIdx(card.getVersionIdx());
			if (card.getRarity() != null) {
				cloneCard.setRarity(card.getRarity());
			}
			cloneCard.setPVIncrementBonus(card.getPVIncrementBonus());
			cardset.add(cloneCard);
			return cloneCard;
		}

		return null;
	}
}
