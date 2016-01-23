package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CardParser {
	private static TreeSet<Pool> poolList = new TreeSet<Pool>();
	private static ArrayList<PackMetadata> metadataList = new ArrayList<PackMetadata>();
	public static HashMap<Integer, String> livetlkMap;
	public static TreeSet<Card> cardList;
	public static TreeSet<StorePack> packList;
	public static HashMap<String, StorePack> packnameMap;
	
	
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

		InputSource basegameSource = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_basegame.xml");
		InputSource patch1Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_patch1.xml");
		InputSource patch2Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_patch2.xml");
		InputSource testpatchSource = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_testpatch.xml");
		InputSource mp1Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_mp1.xml");
		InputSource mp2Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_mp2.xml");
		InputSource mp3Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_mp3.xml");
		InputSource mp4Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_mp4.xml");
		InputSource mp5Source = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_mp5.xml");
		InputSource liveiniSource = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\biogame_liveini.xml");

		InputSource livetlkSource = new InputSource("file:///" + System.getProperty("user.dir") + "\\carddata\\me3tlk.xml");

		InputSource[] sources = new InputSource[] { basegameSource, testpatchSource, mp1Source, mp2Source, mp3Source, patch1Source, mp4Source, mp5Source, patch2Source,
				liveiniSource };

		cardList = new TreeSet<Card>(); //for list of all cards

		//LOAD STRINGS INTO HASHMAP OF ID => STR
		livetlkMap = new HashMap<Integer, String>();
		String tlkExpression = "/TlkFile/Strings/String";
		NodeList tlkNodes = (NodeList) xpath.evaluate(tlkExpression, livetlkSource, XPathConstants.NODESET);
		if (tlkNodes != null && tlkNodes.getLength() > 0) {
			for (int i = 0; i < tlkNodes.getLength(); i++) {
				if (tlkNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) tlkNodes.item(i);
					livetlkMap.put(Integer.parseInt(el.getAttribute("id")), el.getTextContent());
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
						Card multiCard = new Card(el.getTextContent());
						if (Card.getHumanName(multiCard.getUniqueName()).equals("Ops Survival Pack Capacity Upgrade")) {
							//System.out.println("REMOVE BREAK");
						}
						boolean removed = cardList.remove(multiCard);
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
						Card multiCard = new Card(el.getTextContent());
						if (multiCard.getUniqueName().equals("AdeptAsari")) {
							System.out.println("ADD BREAK");
						}
						cardList.add(multiCard);
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
									Card multiCard = new Card(removeNode.getTextContent());
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
									Card multiCard = new Card(contentNode.getTextContent());
									Card fetchedCard = getCardByName(multiCard.getUniqueName(), multiCard.getVersionIdx(), multiCard.getRarity());
									if (fetchedCard == null) {
										System.err.println("Missing " + multiCard);
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
										Card singleCard = new Card(contentNode.getTextContent());
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

			/*
			 * for (Card card : cardList) { if (card.getRarity() == null) {
			 * System.err.println("NULL RARITY: " + card); }
			 * 
			 * if (card.getUniqueName().equals("AdeptAsari")) {
			 * System.out.println("AsariAdept EXISTS!");
			 * System.out.println(card); } }
			 */

			//generate pack contents
			StringBuilder sb = new StringBuilder();

			for (Entry<String, StorePack> entry : packnameMap.entrySet()) {
				StorePack pack = entry.getValue();
				System.out.println(pack.getSRPackName());
				String savepath = System.getProperty("user.dir") + "\\carddata\\packcontents\\" + pack.getPackName() + ".html";
				FileUtils.writeStringToFile(new File(savepath), pack.getPackHTML());

				sb.append("<a id='" + pack.getPackName() + "' class='roundedbutton' href='#" + pack.getPackName() + "' data-packname='" + pack.getPackName() + "'>"
						+ pack.getHumanName() + "</a>\n\t");
			}
			String savepath = System.getProperty("user.dir") + "\\carddata\\packcontents\\packheading.html";
			FileUtils.writeStringToFile(new File(savepath), sb.toString());

			//build cards page
			sb = new StringBuilder();
			String previousCategory = "";
			int num = 0;
			int numtodo = cardList.size();
			for (Card card : cardList) {
				//System.out.println("["+(num++)+"/"+numtodo+"]");
				if (card.getUseVersionIdx() && card.getVersionIdx() < 0 && !card.getCategoryName().equals("weapons") && !card.getCategoryName().equals("weaponmods") && !card.getCategoryName().equals("misc") && !card.getRarity().equals(Card.Rarity.Unused)) {
					//System.out.println("Skipping "+card);
					continue;
				}
				if (!card.getCategoryName().equals(previousCategory)) {
					sb.append("<hr class='dark_hr_center'>\n");
				}
				previousCategory = card.getCategoryName();
				//System.out.println(card.getCardDisplayString());
				sb.append(card.getCardHTML());
			}
			savepath = System.getProperty("user.dir") + "\\carddata\\packcontents\\cardlist.html";
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

	public static Card getCardByName(String uniqueName, int versionIdx, Card.Rarity rarity) {
		ArrayList<Card> samenameCards = new ArrayList<>();
		for (Card card : cardList) {
			if (card.getUniqueName().equals(uniqueName)) {
				samenameCards.add(card);
				if (card.getUseVersionIdx()) {
					if (card.getVersionIdx() == versionIdx) {
						//System.out.println("Found card by name and IDX: " + uniqueName);
						return card;
					}
				} else {
					//System.out.println("Found card by name only: " + uniqueName);
					return card;
				}
			}
		}
		//hasn't been found yet
		for (Card card : samenameCards) {
			if (card.getUseVersionIdx()) {
				//it's probably not defined... somehow, the game accepts this.
				Card cloneCard = new Card(card);
				cloneCard.setVersionIdx(versionIdx);
				if (rarity != null) {
					cloneCard.setRarity(rarity);
				}
				cardList.add(cloneCard);
				return cloneCard;
			}
		}
		return null;
	}
}
