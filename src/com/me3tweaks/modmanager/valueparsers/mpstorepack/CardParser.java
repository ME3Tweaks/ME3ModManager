package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CardParser {
	public static void main(String args[]) throws Exception {
		TreeSet<String> cardNameSet = new TreeSet<String>();
		HashMap<String, StorePack> packnameMap = new HashMap<String, StorePack>();
		XPath xpath = XPathFactory.newInstance().newXPath();
		//String basepacklistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='packlist']/Value[@type='2']";
		String packlistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='packlist']/Value[@type=%d]";
		String poolnameExpression = "/CoalesceAsset/Sections/Section/Property[@name='poolname']";
		String poolcontentsExpression = "../Property[@name='cardlist']/Value"; //relative to poolname node
		String singlepoolcontentsExpression = "../Property[@name='cardlist' and @type=3]";
		String carddatalistExpression = "/CoalesceAsset/Sections/Section[@name='sfxgamempcontent.sfxgawreinforcementmanager']/Property[@name='carddata']/Value[@type=%d]";

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

		InputSource[] sources = new InputSource[] { basegameSource, testpatchSource, mp1Source, mp2Source, mp3Source, patch1Source, mp4Source, mp5Source, patch2Source,
				liveiniSource };

		LinkedHashSet<Card> cardList = new LinkedHashSet<Card>();

		for (InputSource inputSource : sources) {
			if (false) {
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
							if (Card.getHumanName(multiCard.uniqueName).equals("Ops Survival Pack Capacity Upgrade")) {
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
							if (Card.getHumanName(multiCard.uniqueName).equals("Ops Survival Pack Capacity Upgrade")) {
								//System.out.println("ADD BREAK");
							}
							cardList.add(multiCard);
							//System.out.println("CARD ADD: " + multiCard);

							//System.out.println(multiCard.getCardHTML());
						}
					}
				}

				for (Card card : cardList) {
					System.out.println(card.getCardHTML());
				}

				if (true) {
					System.exit(0);
				}
			}
			
			HashMap<String, LinkedHashSet<Card>> poolToContentsMap = new HashMap<>();
			//PARSE - POOLS
			NodeList poolNodes = (NodeList) xpath.evaluate(poolnameExpression, inputSource, XPathConstants.NODESET);
			System.out.println("Number of nodes: " + poolNodes.getLength());
			if (poolNodes != null && poolNodes.getLength() > 0) {
				for (int i = 0; i < poolNodes.getLength(); i++) {
					if (poolNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) poolNodes.item(i);
						NodeList contentNodes = (NodeList) xpath.evaluate(poolcontentsExpression, el, XPathConstants.NODESET);
						//GET CONTENTS OF POOL
						if (contentNodes != null && contentNodes.getLength() > 0) {
							for (int x = 0; x < contentNodes.getLength(); x++) {
								if (contentNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
									Element contentNode = (Element) contentNodes.item(x);
									Card multiCard = new Card(contentNode.getTextContent());
									cardNameSet.add(multiCard.getUniqueName());
								}
							}
						} else {
							//try a 1-item array search.
							contentNodes = (NodeList) xpath.evaluate(singlepoolcontentsExpression, el, XPathConstants.NODESET);
							//GET CONTENTS OF POOL
							if (contentNodes != null && contentNodes.getLength() > 0) {
								for (int x = 0; x < contentNodes.getLength(); x++) {
									if (contentNodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
										Element contentNode = (Element) contentNodes.item(x);
										Card singleCard = new Card(contentNode.getTextContent());
										cardNameSet.add(singleCard.getUniqueName());
										//System.out.println(singleCard.getCardHTML());
									}
								}
							}
						}
					}
				}
			}
		}

		if (true) {
			System.exit(0);
		}
		//PARSE - SLOTS
		NodeList nodes = (NodeList) xpath.evaluate(packlistExpression, liveiniSource, XPathConstants.NODESET);

		System.out.println("Number of nodes: " + nodes.getLength());

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
						StorePack newPack = new StorePack();
						newPack.addCardSlot(packslot);
						packnameMap.put(packslot.getPackname(), newPack);
					}
				}
			}

			for (Entry<String, StorePack> entry : packnameMap.entrySet()) {
				String packname = entry.getKey();
				StorePack pack = entry.getValue();
				System.out.println(packname + ": " + pack.getContentsString());
			}
		}
	}
}
