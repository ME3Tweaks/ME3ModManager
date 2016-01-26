package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class RealCard extends Card implements Comparable<RealCard> {
	private String uniqueName;
	private int maxCount = -1;
	private int versionIdx = -1;
	private Rarity rarity = null;
	private int PVIncrementBonus = -1;
	private int GUIName = -1;
	private int GUIDescription = -1;
	private boolean useVersionIdx;
	private String cardHtmlStr;
	private String cardHTML;
	private ArrayList<StorePack> packsIn;
	public boolean isCharCard;
	public boolean isConsumable;
	public boolean isMisc;
	public ArrayList<SlotPool> inPools; //used when making store html

	/**
	 * Represents a card.
	 * 
	 * @param uniqueNameString
	 *            (UniqueName="AdeptVolus"... etc
	 */
	public RealCard(String uniqueNameString) {
		uniqueName = ValueParserLib.getStringProperty(uniqueNameString, "UniqueName", true);
		maxCount = ValueParserLib.getIntProperty(uniqueNameString, "MaxCount");
		useVersionIdx = ValueParserLib.getBooleanProperty(uniqueNameString, "bUseVersionIdx", true);
		versionIdx = ValueParserLib.getIntProperty(uniqueNameString, "VersionIdx");
		String rarity = ValueParserLib.getStringProperty(uniqueNameString, "Rarity", false);
		if (rarity != null) {
			switch (rarity) {
			case "Rarity_Common": //blue
				this.rarity = Rarity.Common;
				break;
			case "Rarity_Uncommon": //silver
				this.rarity = Rarity.Uncommon;
				break;
			case "Rarity_Rare": //gold
				this.rarity = Rarity.Rare;
				break;
			case "Rarity_UltraRare": //black
				this.rarity = Rarity.UltraRare;
				break;
			}
		}
		if (getCardDisplayString().toLowerCase().contains("unused") || getUniqueName().equals("InfiltratorHumanFemaleBF3")
				|| getUniqueName().equals("SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponStability")) {
			this.rarity = Rarity.Unused;
		}
		PVIncrementBonus = ValueParserLib.getIntProperty(uniqueNameString, "PVIncrementBonus");
		GUIName = ValueParserLib.getIntProperty(uniqueNameString, "GUIName");
		GUIDescription = ValueParserLib.getIntProperty(uniqueNameString, "GUIDescription");
		isCharCard = (getCategoryName().equals("kits") ? true : false);
		isConsumable = (getCategoryName().equals("consumables") ? true : false);
		isMisc = (getCategoryName().equals("misc") ? true : false);
		if (getCategoryName().equals("gear"))
			versionIdx = 0;

	}

	/** Copy Constrcutor **/
	public RealCard(RealCard card) {
		uniqueName = card.uniqueName;
		maxCount = card.maxCount;
		useVersionIdx = card.useVersionIdx;
		versionIdx = card.versionIdx;
		rarity = card.rarity;
		PVIncrementBonus = card.PVIncrementBonus;
		GUIName = card.GUIName;
		GUIDescription = card.GUIDescription;
	}

	public boolean getUseVersionIdx() {
		return useVersionIdx;
	}

	public void setUseVersionIdx(boolean useVersionIdx) {
		this.useVersionIdx = useVersionIdx;
	}

	public String getCardpageHTML() {
		if (cardHtmlStr == null) {
			cardHtmlStr = generateCardHTML();
		}
		return cardHtmlStr;
	}

	public String getCardGameName() {
		return CardParser.tlkMap.get(GUIName);
	}

	private String generateCardHTML() {
		//System.out.println("Card HTML of "+this);
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"card float-shadow " + rarity.toString().toLowerCase() + "\">\n\t");
		sb.append("<img src=\"/images/storecatalog/" + getCategoryName() + "/" + getImageName()
				+ ".png\" onerror=\"if (this.src != '/images/storecatalog/misc/QuestionMark.png') this.src = '/images/storecatalog/misc/QuestionMark.png';\">\n\t");
		//sb.append("<span>" + getCardDisplayString() + "</span>\n");
		sb.append("<span>" + getCardDisplayString() + "</span>\n");
		sb.append("<div class='ttip'>");
		sb.append("<p class='centered'>");
		sb.append(getCardDescription());
		sb.append("</p>");
		sb.append("<hr class='dark_hr_center'>");
		ArrayList<StorePack> packlist = getPacklistsCardIsIn();
		StringBuilder unusedSB = new StringBuilder();

		if (packlist.size() > 0) {
			sb.append("<h3>Can be found in</h3>");
			sb.append("<ul>");
			unusedSB.append("<div class='extrapacks'><h3>Also in</h3>");
			unusedSB.append("<ul>");
			for (StorePack pack : packlist) {
				switch (pack.getPackName()) {
				case "starter":
				case "bronze":
				case "silver":
				case "gold":
				case "goldpremium":
				case "arsenal":
				case "reserves":
				case "equipjumbo":
					sb.append("<li><a class='dark' href='/store_catalog/packs/" + pack.getHumanName().replaceAll(" ", "").toLowerCase() + "' title='"+pack.getHumanName()+" pack page'>"
							+ pack.getHumanName() + "</a></li>");
					break;
				default:
					unusedSB.append("<li>" + pack.getHumanName() + "</li>");
					break;
				}
			}
			unusedSB.append("</ul></div>");
			sb.append("</ul>");
			sb.append(unusedSB.toString());
		} else {
			sb.append("<p>This card does not drop in any store pack. It must be modded into the game.</p>");
		}
		sb.append("</div>");
		sb.append("</div>\n");
		cardHTML = sb.toString();
		return cardHTML;
	}

	private ArrayList<StorePack> getPacklistsCardIsIn() {
		if (packsIn != null) {
			return packsIn;
		}
		ArrayList<StorePack> getPacklistCardIsIn = new ArrayList<>();
		for (Entry<String, StorePack> entry : CardParser.packnameMap.entrySet()) {
			StorePack pack = entry.getValue();
			if (pack.containsCard(this)) {
				getPacklistCardIsIn.add(pack);
			}
		}

		if (getPacklistCardIsIn.size() == 0) {
			for (Entry<String, StorePack> entry : CardParser.packnameMap.entrySet()) {
				StorePack pack = entry.getValue();
				if (pack.containsCard(this)) {
					//System.err.println("Card is in a pack but with diff idx (but no idx exists for this): "+this);
					getPacklistCardIsIn.add(pack);
				}
			}
		}

		if (getPacklistCardIsIn.size() == 0) {
			//System.err.println("Card not in a pack " + this);
		}
		packsIn = getPacklistCardIsIn;
		return packsIn;
	}

	public String getCardName() {
		return CardParser.tlkMap.get(GUIName);
	}

	public String getCardDescription() {
		String desc = CardParser.tlkMap.get(GUIDescription);

		if (desc == null || getUniqueName().equals("SoldierHumanMaleBF3")) {
			return RealCard.getHumanName(uniqueName);
		}
		
		if (desc.startsWith("$")) {
			desc = CardParser.tlkMap.get(Integer.parseInt(desc.substring(1)));
		}
		
		if (desc.startsWith("You can now carry &lt;CUSTOM1&gt;")){
			desc = desc.replace("You can now carry &lt;CUSTOM1&gt;", "You can now carry more");
		}
		
		if (uniqueName.equals("SFXGameContentDLC_CON_MP5.SFXGameEffect_MatchConsumable_Gear_VisionHelmet")) {
			desc = desc.replace("Scan range is &lt;CUSTOM0&gt; meters.\n", "");
		}

		if (desc.startsWith("&lt;CUSTOM2&gt;")) {
			return desc.replace("&lt;CUSTOM2&gt;", Integer.toString(getPVIncrementBonus()));
		}

		if (getCategoryName().equals("consumables")) {
			return desc.replace("&lt;CUSTOM1&gt;", Integer.toString(versionIdx + 1));
		}

		if (desc.contains("&lt;CUSTOM") && getCategoryName().equals("gear")) {
			String lopoff = desc;
			while (lopoff.indexOf(".") > 0) {
				lopoff = lopoff.substring(lopoff.indexOf(".") + 1);
			}
			desc = desc.replace(lopoff, "");
		}

		return desc;
	}

	private String getImageName() {
		if (uniqueName.contains("SFXWeapon_")) {
			return uniqueName.substring(uniqueName.indexOf(".") + 11);
		}
		if (uniqueName.contains("Adept") || uniqueName.contains("Soldier") || uniqueName.contains("Sentinel") || uniqueName.contains("Infiltrator")
				|| uniqueName.contains("Vanguard") || uniqueName.contains("Engineer")) {
			return uniqueName;
		}
		if (uniqueName.contains("SFXGameEffect_MatchConsumable_Gear")) {
			return uniqueName.substring(uniqueName.indexOf(".") + 31);
		}
		if (uniqueName.contains("SFXWeaponMod_")) {
			return uniqueName.substring(uniqueName.indexOf(".") + 14);
		}
		if (uniqueName.contains("SFXPowerCustomActionMP_Consumable")) {
			return uniqueName.substring(uniqueName.indexOf(".") + 24);
		}
		if (uniqueName.contains("SFXGameEffect_MatchConsumable")) {
			return uniqueName.substring(uniqueName.indexOf(".") + 31);
		}
		// TODO Auto-generated method stub
		return uniqueName;
	}

	public String getCategoryName() {
		if (uniqueName.contains("Adept") || uniqueName.contains("Soldier") || uniqueName.contains("Sentinel") || uniqueName.contains("Infiltrator")
				|| uniqueName.contains("Vanguard") || uniqueName.contains("Engineer")) {
			return "kits";
		}
		if (uniqueName.contains("SFXGameEffect_MatchConsumable_Gear")) {
			return "gear";
		}
		if (uniqueName.contains("SFXWeaponMod_")) {
			return "weaponmods";
		}
		if (uniqueName.contains("SFXPowerCustomActionMP_Consumable") || uniqueName.contains("SFXGameEffect_MatchConsumable_")) {
			return "consumables";
		}
		if (uniqueName.contains("SFXWeapon_")) {
			return "weapons";
		}
		// TODO Auto-generated method stub
		return "misc";
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + PVIncrementBonus;
		result = prime * result + maxCount;
		result = prime * result + ((rarity == null) ? 0 : rarity.hashCode());
		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
		result = prime * result + (useVersionIdx ? 1231 : 1237);
		result = prime * result + versionIdx;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RealCard other = (RealCard) obj;
		if (PVIncrementBonus != other.PVIncrementBonus)
			return false;
		if (maxCount != other.maxCount)
			return false;
		if (rarity != other.rarity)
			return false;
		if (uniqueName == null) {
			if (other.uniqueName != null)
				return false;
		} else if (!uniqueName.equals(other.uniqueName))
			return false;
		if (useVersionIdx != other.useVersionIdx)
			return false;
		if (versionIdx != other.versionIdx)
			return false;
		return true;
	}

	public String getCardDisplayString() {
		String str = getCardName(); //gethumanname();
		if (str != null) {
			str = str.replace("&lt;CUSTOM0&gt;", "");

			str = str.replace("&lt;CUSTOM2&gt;", "");
		}
		if (str == null) {
			str = getHumanName(uniqueName);
		}

		if (str.contains("+")/* && !uniqueName.equals("MPCredits")*/) {
			str = str.substring(0, str.indexOf('+')).trim();
		}

		if (versionIdx > -1 && !getCategoryName().equals("gear") && !getCategoryName().equals("kits")) {
			int num = versionIdx + 1;
			ValueParserLib.RomanNumeral rn = new ValueParserLib.RomanNumeral(num);
			str += " " + rn.toString();
		}

		if (PVIncrementBonus > 0 && PVIncrementBonus != 300000) {
			str = (PVIncrementBonus + 1) + " " + str;
			if (!str.endsWith("s")) {
				str = str + "s";
			}
		}

		return str;
	}

	public static String getHumanName(String uniqueName) {
		switch (uniqueName) {
		case "AdeptAsari":
			return "Asari Adept";
		case "AdeptAsariCommando":
			return "Asari Justicar Adept";
		case "AdeptBatarian":
			return "Batarian Slasher Adept";
		case "AdeptCollector":
			return "Awakened Collector Adept";
		case "AdeptDrell":
			return "Drell Adept";
		case "AdeptHumanFemale":
			return "Human Female Adept";
		case "AdeptHumanMale":
			return "Human Male Adept";
		case "AdeptHumanMaleCerberus":
			return "Human Male Adept (Phoenix)";
		case "AdeptKrogan":
			return "Krogan Shaman Adept";
		case "AdeptN7":
			return "N7 Fury";
		case "AdeptVolus":
			return "Volus Adept";
		case "EngineerGeth":
			return "Geth Engineer";
		case "EngineerHumanFemale":
			return "Human Female Engineer";
		case "EngineerHumanMale":
			return "Human Male Engineer";
		case "EngineerMerc":
			return "Talon Mercenary";
		case "EngineerN7":
			return "N7 Demolisher";
		case "EngineerQuarian":
			return "Quarian Engineer";
		case "EngineerQuarianMale":
			return "Quarian Male Engineer";
		case "EngineerSalarian":
			return "Salarian Engineer";
		case "EngineerTurian":
			return "Turian Engineer";
		case "EngineerVolus":
			return "Volus Engineer";
		case "EngineerVorcha":
			return "Vorcha Engineer";
		case "InfiltratorAsari":
			return "Asari Infiltrator";
		case "InfiltratorDrell":
			return "Drell Infiltrator";
		case "InfiltratorFembot":
			return "Alliance Infiltrator Unit";
		case "InfiltratorGeth":
			return "Geth Infiltrator";
		case "InfiltratorHumanFemale":
			return "Human Female Infiltrator";
		case "InfiltratorHumanMale":
			return "Human Infiltrator";
		case "InfiltratorN7":
			return "N7 Shadow";
		case "InfiltratorQuarian":
			return "Quarian Infiltrator";
		case "InfiltratorQuarianMale":
			return "Quarian Male Infiltrator";
		case "InfiltratorSalarian":
			return "Salarian Infiltrator";
		case "MPCapacity_Ammo":
			return "Thermal Clip Pack Capacity Upgrade";
		case "MPCapacity_Revive":
			return "Medi-Gel Capacity Upgrade";
		case "MPCapacity_Rocket":
			return "Cobra Missile Launcher Capacity Upgrade";
		case "MPCapacity_Shield":
			return "Ops Survival Pack Capacity Upgrade";
		case "MPCredits":
			return "Credits";
		case "MPRespec":
			return "Reset Powers";
		case "N7InfiltratorTurian":
			return "Turian Ghost";
		case "N7SoldierTurian":
			return "Turian Havoc";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleAccuracy":
			return "Assault Rifle Precision Scope";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleDamage":
			return "Assault Rifle Extended Barrel";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleForce":
			return "Assault Rifle Piercing Mod";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleMagSize":
			return "Assault Rifle Magazine Upgrade";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleStability":
			return "Assault Rifle Stability Damper";
		case "SFXGameContent.SFXWeaponMod_PistolAccuracy":
			return "Pistol Scope";
		case "SFXGameContent.SFXWeaponMod_PistolDamage":
			return "Pistol High-Caliber Barrel";
		case "SFXGameContent.SFXWeaponMod_PistolMagSize":
			return "Pistol Magazine Upgrade";
		case "SFXGameContent.SFXWeaponMod_PistolReloadSpeed":
			return "Pistol Piercing Mod";
		case "SFXGameContent.SFXWeaponMod_PistolStability":
			return "Pistol Melee Stunner";
		case "SFXGameContent.SFXWeaponMod_SMGAccuracy":
			return "SMG Scope";
		case "SFXGameContent.SFXWeaponMod_SMGConstraintDamage":
			return "SMG Heat Sink"; //named wrong
		case "SFXGameContent.SFXWeaponMod_SMGDamage":
			return "SMG High Caliber Barrel";
		case "SFXGameContent.SFXWeaponMod_SMGMagSize":
			return "SMG Magazine Upgrade";
		case "SFXGameContent.SFXWeaponMod_SMGStability":
			return "SMG Ultralight Materials";
		case "SFXGameContent.SFXWeaponMod_ShotgunAccuracy":
			return "Shotgun Smart Choke";
		case "SFXGameContent.SFXWeaponMod_ShotgunDamage":
			return "Shotgun High Caliber Barrel";
		case "SFXGameContent.SFXWeaponMod_ShotgunMeleeDamage":
			return "Shotgun Blade Attachment";
		case "SFXGameContent.SFXWeaponMod_ShotgunReloadSpeed":
			return "Shotgun Shredder Mod"; //named wrong
		case "SFXGameContent.SFXWeaponMod_ShotgunStability":
			return "Shotgun Spare Thermal Clip"; //named wrong
		case "SFXGameContent.SFXWeaponMod_SniperRifleAccuracy":
			return "Sniper Rifle Enhanced Scope";
		case "SFXGameContent.SFXWeaponMod_SniperRifleConstraintDamage":
			return "Sniper Rifle Piercing Mod";
		case "SFXGameContent.SFXWeaponMod_SniperRifleDamage":
			return "Sniper Rifle Extended Barrel";
		case "SFXGameContent.SFXWeaponMod_SniperRifleReloadSpeed":
			return "Sniper Rifle Spare Thermal Clip";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Argus":
			return "M-55 Argus";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Avenger":
			return "M-8 Avenger";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Cobra":
			return "Phaeston";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Collector":
			return "Collector Rifle";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Falcon":
			return "M-37 Falcon";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Geth":
			return "Geth Pulse Rifle";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Mattock":
			return "M-96 Mattock";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Revenant":
			return "M-76 Revenant";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Saber":
			return "M-99 Saber";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Valkyrie":
			return "N7 Valkyrie";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Vindicator":
			return "M-15 Vindicator";
		case "SFXGameContent.SFXWeapon_Pistol_Carnifex":
			return "M-6 Carnifex";
		case "SFXGameContent.SFXWeapon_Pistol_Eagle":
			return "N7 Eagle";
		case "SFXGameContent.SFXWeapon_Pistol_Ivory":
			return "M-77 Paladin";
		case "SFXGameContent.SFXWeapon_Pistol_Phalanx":
			return "M-5 Phalanx";
		case "SFXGameContent.SFXWeapon_Pistol_Predator":
			return "M-3 Predator";
		case "SFXGameContent.SFXWeapon_Pistol_Scorpion":
			return "Scorpion";
		case "SFXGameContent.SFXWeapon_Pistol_Talon":
			return "M-358 Talon";
		case "SFXGameContent.SFXWeapon_Pistol_Thor":
			return "Arc Pistol";
		case "SFXGameContent.SFXWeapon_SMG_Hornet":
			return "M-25 Hornet";
		case "SFXGameContent.SFXWeapon_SMG_Hurricane":
			return "N7 Hurricane";
		case "SFXGameContent.SFXWeapon_SMG_Locust":
			return "M-12 Locust";
		case "SFXGameContent.SFXWeapon_SMG_Shuriken":
			return "M-4 Shuriken";
		case "SFXGameContent.SFXWeapon_SMG_Tempest":
			return "M-9 Tempest";
		case "SFXGameContent.SFXWeapon_Shotgun_Claymore":
			return "M-300 Claymore";
		case "SFXGameContent.SFXWeapon_Shotgun_Crusader":
			return "N7 Crusader";
		case "SFXGameContent.SFXWeapon_Shotgun_Disciple":
			return "Disciple";
		case "SFXGameContent.SFXWeapon_Shotgun_Eviscerator":
			return "M-22 Eviscerator";
		case "SFXGameContent.SFXWeapon_Shotgun_Geth":
			return "Geth Plasma Shotgun";
		case "SFXGameContent.SFXWeapon_Shotgun_Graal":
			return "Graal Spike Thrower";
		case "SFXGameContent.SFXWeapon_Shotgun_Katana":
			return "M-23 Katana";
		case "SFXGameContent.SFXWeapon_Shotgun_Raider":
			return "AT-12 Raider";
		case "SFXGameContent.SFXWeapon_Shotgun_Scimitar":
			return "M-27 Scimitar";
		case "SFXGameContent.SFXWeapon_Shotgun_Striker":
			return "M-11 Wraith";
		case "SFXGameContent.SFXWeapon_SniperRifle_BlackWidow":
			return "Black Widow";
		case "SFXGameContent.SFXWeapon_SniperRifle_Incisor":
			return "M-29 Incisor";
		case "SFXGameContent.SFXWeapon_SniperRifle_Indra":
			return "M-90 Indra";
		case "SFXGameContent.SFXWeapon_SniperRifle_Javelin":
			return "Javelin";
		case "SFXGameContent.SFXWeapon_SniperRifle_Mantis":
			return "M-92 Mantis";
		case "SFXGameContent.SFXWeapon_SniperRifle_Raptor":
			return "M-13 Raptor";
		case "SFXGameContent.SFXWeapon_SniperRifle_Valiant":
			return "N7 Valiant";
		case "SFXGameContent.SFXWeapon_SniperRifle_Viper":
			return "M-97 Viper";
		case "SFXGameContent.SFXWeapon_SniperRifle_Widow":
			return "M-98 Widow";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_HeadshotDamage":
			return "Targeting VI";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_MeleeDamage":
			return "Strength Enhancer";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_ShieldRegenBonus":
			return "Shield Power Cells";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_StabilityBonus":
			return "Stabilization Module";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_AssaultRifle_Krogan":
			return "Striker Assault Rifle";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_SMG_Geth":
			return "Geth Plasma SMG";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_SniperRifle_Batarian":
			return "Kishock Harpoon Gun";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_AssaultDamageGrenadeCap":
			return "Warfighter Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_PistolDamageBioticDamage":
			return "Commando Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_SMGDamagePowerCooldown":
			return "Expert Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_ShieldStrengthShieldRegen":
			return "Stronghold Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_ShotgunDamageMeleeDamage":
			return "Berserker Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_SniperDamageTechDamage":
			return "Operative Package";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_GrenadeCapacity":
			return "Grenade Capacity";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_HeadshotDamage":
			return "Vulnerability VI";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_MeleeDamage":
			return "Hydraulic Joints";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_PowerBonus_Cooldown":
			return "Structural Ergonomics";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_PowerBonus_Damage":
			return "Adaptive War Amp";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_ShieldRegen":
			return "Multicapacitor";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_ShieldStrength":
			return "Shield Booster";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Stability":
			return "Vibration Damper";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_AssaultRifle":
			return "Assault Rifle Amp";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_Pistol":
			return "Pistol Amp";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_SMG":
			return "SMG Amp";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_Shotgun":
			return "Shotgun Amp";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_SniperRifle":
			return "Sniper Rifle Amp";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Cerberus":
			return "Cerberus Harrier";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Prothean":
			return "Prothean Particle Rifle";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Prothean_MP":
			return "Prothean Particle Rifle";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_Shotgun_Quarian":
			return "Reegar Carbine";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_SniperRifle_Turian":
			return "Krysae Sniper Rifle";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_AssaultRifleDamagePistolDamage":
			return "Combatives Upgrade";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_MeleeDamageBioticDamage":
			return "Martial Biotic Amp";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_ShieldStrengthMeleeDamage":
			return "Juggernaut Shield";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_ShotgunDamageGrenadeCap":
			return "Shock Trooper Upgrade";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_SniperDamageSMGDamage":
			return "Guerrilla Upgrade";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_TechDamagePowerCooldown":
			return "Omni-Capacitors";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_WeaponStabilityAmmoCapacity":
			return "Barrage Upgrade";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_AssaultRifle_LMG":
			return "N7 Typhoon";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_Pistol_Asari":
			return "Acolyte";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_Shotgun_Assault":
			return "N7 Piranha";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Eraser":
			return "Drill Rounds";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Needler":
			return "Explosive Rounds";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Phasic":
			return "Phasic Rounds";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_CobraCapacity":
			return "Armored Compartments";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_MassMedigel":
			return "Medi-Gel Transmitter";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_MedigelCapacity":
			return "Responder Loadout";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_SurvivalCapacity":
			return "Ops-Packs Capacity";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_ThermalCapacity":
			return "Assault Loadout";
		case "SFXGameContentDLC_CON_MP4.SFXWeapon_SMG_Collector":
			return "Collector SMG";
		case "SFXGameContentDLC_CON_MP4.SFXWeapon_SniperRifle_Collector":
			return "Collector Sniper Rifle";
		case "SFXGameContentDLC_CON_MP5.SFXGameEffect_MatchConsumable_Gear_BatarianGauntlet":
			return "Batarian Gauntlet";
		case "SFXGameContentDLC_CON_MP5.SFXGameEffect_MatchConsumable_Gear_VisionHelmet":
			return "Geth Scanner";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_AssaultRifleUltraLight_MP5":
			return "Assault Rifle Ultralight Materials";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_PistolPowerDamage_MP5":
			return "Pistol Power Magnifier";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_SMGPowerDamage_MP5":
			return "SMG Power Magnifier";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_ShotgunUltraLight_MP5":
			return "Shotgun Ultralight Materials";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_SniperRifleUltraLight_MP5":
			return "Sniper Rifle Ultralight Materials";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Adas_MP":
			return "Adas Anti-Synthetic Rifle";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Lancer_MP":
			return "M-7 Lancer";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Spitfire":
			return "Geth Spitfire";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Pistol_Bloodpack_MP":
			return "Executioner Pistol";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Pistol_Silencer_MP":
			return "M-11 Suppressor";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_SMG_Bloodpack_MP":
			return "Blood Pack Punisher";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Shotgun_Salarian_MP":
			return "Venom Shotgun";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleMelee":
			return "Assault Rifle Omni-Blade";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleSuperPen":
			return "Assault Rifle High-Velocity Barrel";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleSuperScope":
			return "Assault Rifle Thermal Scope";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolHeadShot":
			return "Pistol Cranial Trauma System";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolSuperDamage":
			return "Pistol Heavy Barrel";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolUltraLight":
			return "Pistol Ultralight Materials";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SMGPenetration":
			return "SMG High-Velocity Barrel";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SMGStabilization":
			return "SMG Recoil System";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_ShotgunDamageAndPen":
			return "Shotgun High-Velocity Barrel";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_ShotgunSuperMelee":
			return "Shotgun Omni-Blade";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SniperRifleDamageAndPen":
			return "Sniper Rifle High-Velocity Barrel";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SniperRifleSuperScope":
			return "Sniper Rifle Thermal Scope";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_ArmorPiercing":
			return "Armor-Piercing Rounds";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Cryo":
			return "Cryo Rounds";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Disruptor":
			return "Disruptor Rounds";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Incendiary":
			return "Incendiary Rounds";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Warp":
			return "Warp Rounds";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_PowerBonus":
			return "Power Efficiency Module";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_PowerBonusDamage":
			return "Power Amplifier Module";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_ShieldBonus":
			return "Cyclonic Modulator";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_SpeedBonus":
			return "Adrenaline Module";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_AssaultRifle":
			return "Assault Rifle Rail Amp";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_Pistol":
			return "Pistol Rail Amp";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_SMG":
			return "SMG Rail Amp";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_Shotgun":
			return "Shotgun Rail Amp";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_SniperRifle":
			return "Sniper Rifle Rail Amp";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Ammo":
			return "Thermal Clip Pack";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Revive":
			return "Medi-Gel";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Rocket":
			return "Cobra Missile Launcher";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Shield":
			return "Ops Survival Pack";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_AmmoCapacity":
			return "Thermal Clip Storage";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_BioticDamage":
			return "Adaptive War Amp";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_TechDamage":
			return "Engineering Kit";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_All":
			return "Densified Ammunition";
		case "SentinelAsari":
			return "Asari Valkyrie";
		case "SentinelBatarian":
			return "Batarian Sentinel";
		case "SentinelHumanFemale":
			return "Human Female Sentinel";
		case "SentinelHumanMale":
			return "Human Sentinel";
		case "SentinelKrogan":
			return "Krogan Sentinel";
		case "SentinelKroganWarlord":
			return "Krogan Warlord";
		case "SentinelN7":
			return "N7 Paladin";
		case "SentinelTurian":
			return "Turian Sentinel";
		case "SentinelVolus":
			return "Volus Mercenary";
		case "SentinelVorcha":
			return "Vorcha Sentinel";
		case "SoldierBatarian":
			return "Batarian Soldier";
		case "SoldierGeth":
			return "Geth Trooper";
		case "SoldierGethDestroyer":
			return "Geth Juggernaut";
		case "SoldierHumanFemale":
			return "Human Female Soldier";
		case "SoldierHumanMale":
			return "Human Soldier";
		case "SoldierHumanMaleBF3":
			return "Battlefield 3 Soldier";
		case "SoldierKrogan":
			return "Krogan Soldier";
		case "SoldierMQuarian":
			return "Quarian Marksman";
		case "SoldierN7":
			return "N7 Destroyer";
		case "SoldierTurian":
			return "Turian Soldier";
		case "SoldierVorcha":
			return "Vorcha Soldier";
		case "VanguardAsari":
			return "Asari Vanguard";
		case "VanguardBatarian":
			return "Batarian Vanguard";
		case "VanguardDrell":
			return "Drell Vanguard";
		case "VanguardHumanFemale":
			return "Human Female Vanguard";
		case "VanguardHumanMale":
			return "Human Vanguard";
		case "VanguardHumanMaleCerberus":
			return "Phoenix Vanguard";
		case "VanguardKrogan":
			return "Krogan Vanguard";
		case "VanguardN7":
			return "N7 Slayer";
		case "VanguardTurianFemale":
			return "Turian Cabal";
		case "VanguardVolus":
			return "Volus Vanguard";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponStability":
			return "Vibration Damper";
		case "InfiltratorHumanFemaleBF3":
			return "Battlefield 3 Infiltrator";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Polonium":
			return "Polonium Rounds";
		default:
			return "case \"" + uniqueName + "\":\n\treturn \"HUMANNAME\";";
		}
	}

	@Override
	public int compareTo(RealCard other) {
		int result = getCategoryName().compareTo(other.getCategoryName());
		if (result != 0)
			return result;

		if (getRarity() != null && other.getRarity() != null) {
			result = getRarity().compareTo(other.getRarity());
			if (result != 0)
				return result;
		}

		result = getCardDisplayStringNoNum().compareTo(other.getCardDisplayStringNoNum());
		if (result != 0)
			return result;
		//same name
		return ((Integer) getVersionIdx()).compareTo((Integer) other.getVersionIdx());
	}

	private String getCardDisplayStringNoNum() {
		// TODO Auto-generated method stub
		String str = getCardDisplayString();
		while (str.length() > 0 && Character.isDigit(str.charAt(0))) {
			str = str.substring(1);
		}
		return str.trim();
	}

	@Override
	public String toString() {
		return "RealCard [uniqueName=" + uniqueName + ", maxCount=" + maxCount + ", versionIdx=" + versionIdx + ", rarity=" + rarity + ", PVIncrementBonus=" + PVIncrementBonus
				+ ", GUIName=" + GUIName + ", GUIDescription=" + GUIDescription + ", useVersionIdx=" + useVersionIdx + ", isCharCard=" + isCharCard + ", isConsumable="
				+ isConsumable + "]";
	}

	public int getVersionIdx() {
		return versionIdx;
	}

	public void setVersionIdx(int versionIdx) {
		this.versionIdx = versionIdx;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	public int getPVIncrementBonus() {
		return PVIncrementBonus;
	}

	public void setPVIncrementBonus(int pVIncrementBonus) {
		PVIncrementBonus = pVIncrementBonus;
	}

	public int getGUIName() {
		return GUIName;
	}

	public void setGUIName(int gUIName) {
		GUIName = gUIName;
	}

	public static boolean IsCharacterCard(String uniqueName) {
		if (uniqueName.contains("Adept") || uniqueName.contains("Soldier") || uniqueName.contains("Sentinel") || uniqueName.contains("Infiltrator")
				|| uniqueName.contains("Vanguard") || uniqueName.contains("Engineer")) {
			return true;
		}
		return false;
	}

	public void addPool(SlotPool slotPool) {
		if (inPools == null) {
			inPools = new ArrayList<>();
		}
		inPools.add(slotPool);
	}

	public String getPackpageHTML() {
		//System.out.println("Card HTML of "+this);
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"card float-shadow " + rarity.toString().toLowerCase() + "%ROTATECOLORCLASS%\">\n\t");
		sb.append("<img src=\"/images/storecatalog/" + getCategoryName() + "/" + getImageName()
				+ ".png\" onerror=\"if (this.src != '/images/storecatalog/misc/QuestionMark.png') this.src = '/images/storecatalog/misc/QuestionMark.png';\">\n\t");
		//sb.append("<span>" + getCardDisplayString() + "</span>\n");
		sb.append("<span>" + getCardDisplayString() + "</span>\n");
		sb.append("<div class='ttip'>");
		sb.append("<p class='centered'>");
		sb.append(getCardDescription());
		sb.append("</p>");
		sb.append("<hr class='dark_hr_center'>");

		sb.append("<h3>Drop Rate</h3>");
		if (maxCount > 0) {
			sb.append("<p>Max drops: " + maxCount + "</p>");
		} else {
			sb.append("<p>Can drop infinitely</p>");
		}
		sb.append("%GUARANTEE%");
		
		boolean shouldmakeshiny = false;
		sb.append("<ul>");
		if (inPools != null) {
			for (SlotPool pool : inPools) {
				CardPool cardpool = CardParser.getCardPoolByName(pool.getPoolname()); 
				
				sb.append("<div class='pool_droprate'>\n\t");
				sb.append("<p>Pool: " + pool.getPoolname() + "</p>");
				sb.append("<p>Pool Chance: " + pool.getPoolweight() * 100 + "%</p>");
				sb.append("<p>In-Pool Chance: " + ValueParserLib.round((1.0 / cardpool.getPoolContents().size()) * 100,2) + "%</p>");
				sb.append("</div>");
				if (pool.getPoolweight() > 0.99 && cardpool.getPoolContents().size() == 1) {
					shouldmakeshiny = true;
				}
			}
		}
		sb.append("</ul></div>"); //end ttip
		sb.append("</div>\n"); //end card
		cardHTML = sb.toString();
		if (shouldmakeshiny) {
			cardHTML = cardHTML.replace("%ROTATECOLORCLASS%", " cardglow");
			cardHTML = cardHTML.replace("%GUARANTEE%", "<p>This card is guaranteed to drop in this pack.</p>");
		} else {
			cardHTML = cardHTML.replace("%ROTATECOLORCLASS%", "");
			cardHTML = cardHTML.replace("%GUARANTEE%", "");
		}
		return cardHTML;
	}
}