package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class Card {
	String uniqueName;
	int maxCount = -1;
	int versionIdx = -1;
	Rarity rarity = null;
	public enum Rarity { //rarity is the background of the card.
		Common("Rarity_Common"), //blue
		Uncommon("Rarity_Uncommon"), //silver
		Rare("Rarity_Rare"), //gold
		UltraRare("Rarity_UltraRare") //black
		;

		private final String raritytext;

		/**
		 * @param text
		 */
		private Rarity(final String text) {
			this.raritytext = text;
		}

		@Override
		public String toString() {
			return raritytext;
		}
	}

	/**
	 * Represents a card.
	 * 
	 * @param uniqueNameString (UniqueName="AdeptVolus"... etc
	 */
	public Card(String uniqueNameString) {
		uniqueName = ValueParserLib.getStringProperty(uniqueNameString, "UniqueName", true);
		maxCount = ValueParserLib.getIntProperty(uniqueNameString, "MaxCount");
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
	}
	
	public String getCardHTML(){
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"card float_shadow "+rarity.toString().toLowerCase()+"\">\n\t");
		sb.append("<img src=\"/images/storecatalog/"+getCategoryName()+"/"+getImageName()+".png\">\n\t");
		sb.append("<span>"+getHumanName(uniqueName)+"</span>\n");
		sb.append("</div>\n");
		return sb.toString();
	}

	private String getImageName() {
		if (uniqueName.contains("SFXWeapon_")){
			return uniqueName.substring(uniqueName.indexOf(".")+11);
		}
		if (uniqueName.contains("Adept") || uniqueName.contains("Soldier") || uniqueName.contains("Sentinel") || uniqueName.contains("Infiltrator") || uniqueName.contains("Vanguard") || uniqueName.contains("Engineer")){
			return uniqueName;
		}
		if (uniqueName.contains("SFXGameEffect_MatchConsumable_Gear")) {
			return uniqueName.substring(uniqueName.indexOf(".")+31);
		}
		if (uniqueName.contains("SFXWeaponMod_")) {
			return uniqueName.substring(uniqueName.indexOf(".")+14);
		}
		if (uniqueName.contains("SFXPowerCustomActionMP_Consumable")){
			return uniqueName.substring(uniqueName.indexOf(".")+24);
		}
		// TODO Auto-generated method stub
		return uniqueName;
	}

	private String getCategoryName() {
		if (uniqueName.contains("SFXWeapon")){
			return "weapons";
		}
		if (uniqueName.contains("Adept") || uniqueName.contains("Soldier") || uniqueName.contains("Sentinel") || uniqueName.contains("Infiltrator") || uniqueName.contains("Vanguard") || uniqueName.contains("Engineer")){
			return "kits";
		}
		if (uniqueName.contains("SFXGameEffect_MatchConsumable_Gear")) {
			return "gear";
		}
		if (uniqueName.contains("SFXWeaponMod_")) {
			return "weaponmods";
		}
		if (uniqueName.contains("SFXPowerCustomActionMP_Consumable")){
			return "consumables";
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
			return "HUMANNAME";
		case "AdeptVolus":
			return "HUMANNAME";
		case "EngineerGeth":
			return "HUMANNAME";
		case "EngineerHumanFemale":
			return "HUMANNAME";
		case "EngineerHumanMale":
			return "HUMANNAME";
		case "EngineerMerc":
			return "HUMANNAME";
		case "EngineerN7":
			return "HUMANNAME";
		case "EngineerQuarian":
			return "HUMANNAME";
		case "EngineerQuarianMale":
			return "HUMANNAME";
		case "EngineerSalarian":
			return "HUMANNAME";
		case "EngineerTurian":
			return "HUMANNAME";
		case "EngineerVolus":
			return "HUMANNAME";
		case "EngineerVorcha":
			return "HUMANNAME";
		case "InfiltratorAsari":
			return "HUMANNAME";
		case "InfiltratorDrell":
			return "HUMANNAME";
		case "InfiltratorFembot":
			return "HUMANNAME";
		case "InfiltratorGeth":
			return "HUMANNAME";
		case "InfiltratorHumanFemale":
			return "HUMANNAME";
		case "InfiltratorHumanMale":
			return "HUMANNAME";
		case "InfiltratorN7":
			return "HUMANNAME";
		case "InfiltratorQuarian":
			return "HUMANNAME";
		case "InfiltratorQuarianMale":
			return "HUMANNAME";
		case "InfiltratorSalarian":
			return "HUMANNAME";
		case "MPCapacity_Ammo":
			return "HUMANNAME";
		case "MPCapacity_Revive":
			return "HUMANNAME";
		case "MPCapacity_Rocket":
			return "HUMANNAME";
		case "MPCapacity_Shield":
			return "HUMANNAME";
		case "MPCredits":
			return "HUMANNAME";
		case "MPRespec":
			return "HUMANNAME";
		case "N7InfiltratorTurian":
			return "HUMANNAME";
		case "N7SoldierTurian":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleAccuracy":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleForce":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleMagSize":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_AssaultRifleStability":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_PistolAccuracy":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_PistolDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_PistolMagSize":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_PistolReloadSpeed":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_PistolStability":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SMGAccuracy":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SMGConstraintDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SMGDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SMGMagSize":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SMGStability":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_ShotgunAccuracy":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_ShotgunDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_ShotgunMeleeDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_ShotgunReloadSpeed":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_ShotgunStability":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SniperRifleAccuracy":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SniperRifleConstraintDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SniperRifleDamage":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeaponMod_SniperRifleReloadSpeed":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Argus":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Avenger":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Cobra":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Collector":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Falcon":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Geth":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Mattock":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Revenant":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Saber":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Valkyrie":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_AssaultRifle_Vindicator":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Carnifex":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Eagle":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Ivory":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Phalanx":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Predator":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Scorpion":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Talon":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Pistol_Thor":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SMG_Hornet":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SMG_Hurricane":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SMG_Locust":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SMG_Shuriken":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SMG_Tempest":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Claymore":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Crusader":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Disciple":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Eviscerator":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Geth":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Graal":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Katana":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Raider":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Scimitar":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_Shotgun_Striker":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_BlackWidow":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Incisor":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Indra":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Javelin":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Mantis":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Raptor":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Valiant":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Viper":
			return "HUMANNAME";
		case "SFXGameContent.SFXWeapon_SniperRifle_Widow":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_HeadshotDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_MeleeDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_ShieldRegenBonus":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXGameEffect_MatchConsumable_StabilityBonus":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_AssaultRifle_Krogan":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_SMG_Geth":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP1.SFXWeapon_SniperRifle_Batarian":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_AssaultDamageGrenadeCap":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_PistolDamageBioticDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_SMGDamagePowerCooldown":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_ShieldStrengthShieldRegen":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_ShotgunDamageMeleeDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Combo_SniperDamageTechDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_GrenadeCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_HeadshotDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_MeleeDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_PowerBonus_Cooldown":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_PowerBonus_Damage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_ShieldRegen":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_ShieldStrength":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_Stability":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_AssaultRifle":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_Pistol":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_SMG":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_Shotgun":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_SniperRifle":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Cerberus":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Prothean":
			return "Prothean Particle Rifle";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_AssaultRifle_Prothean_MP":
			return "Prothean Particle Rifle";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_Shotgun_Quarian":
			return "Reegar Carbine";
		case "SFXGameContentDLC_CON_MP2.SFXWeapon_SniperRifle_Turian":
			return "Krysae Sniper Rifle";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_AssaultRifleDamagePistolDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_MeleeDamageBioticDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_ShieldStrengthMeleeDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_ShotgunDamageGrenadeCap":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_SniperDamageSMGDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_TechDamagePowerCooldown":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXGameEffect_MatchConsumable_Gear_Combo_WeaponStabilityAmmoCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_AssaultRifle_LMG":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_Pistol_Asari":
			return "Acolyte";
		case "SFXGameContentDLC_CON_MP3.SFXWeapon_Shotgun_Assault":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Eraser":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Needler":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_AmmoPower_Phasic":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_CobraCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_MassMedigel":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_MedigelCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_SurvivalCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXGameEffect_MatchConsumable_Gear_ThermalCapacity":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP4.SFXWeapon_SMG_Collector":
			return "Collector SMG";
		case "SFXGameContentDLC_CON_MP4.SFXWeapon_SniperRifle_Collector":
			return "Collector Sniper Rifle";
		case "SFXGameContentDLC_CON_MP5.SFXGameEffect_MatchConsumable_Gear_BatarianGauntlet":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXGameEffect_MatchConsumable_Gear_VisionHelmet":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_AssaultRifleUltraLight_MP5":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_PistolPowerDamage_MP5":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_SMGPowerDamage_MP5":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_ShotgunUltraLight_MP5":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeaponMod_SniperRifleUltraLight_MP5":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Adas_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Lancer_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_AssaultRifle_Spitfire":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Pistol_Bloodpack_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Pistol_Silencer_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_SMG_Bloodpack_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_CON_MP5.SFXWeapon_Shotgun_Salarian_MP":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleMelee":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleSuperPen":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_AssaultRifleSuperScope":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolHeadShot":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolSuperDamage":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_PistolUltraLight":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SMGPenetration":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SMGStabilization":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_ShotgunDamageAndPen":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_ShotgunSuperMelee":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SniperRifleDamageAndPen":
			return "HUMANNAME";
		case "SFXGameContentDLC_Shared.SFXWeaponMod_SniperRifleSuperScope":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_ArmorPiercing":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Cryo":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Disruptor":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Incendiary":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_AmmoPower_Warp":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_PowerBonus":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_PowerBonusDamage":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_ShieldBonus":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_SpeedBonus":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_AssaultRifle":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_Pistol":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_SMG":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_Shotgun":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXGameEffect_MatchConsumable_WeaponDamageBonus_SniperRifle":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Ammo":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Revive":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Rocket":
			return "HUMANNAME";
		case "SFXGameMPContent.SFXPowerCustomActionMP_Consumable_Shield":
			return "HUMANNAME";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_AmmoCapacity":
			return "HUMANNAME";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_BioticDamage":
			return "HUMANNAME";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_TechDamage":
			return "HUMANNAME";
		case "SFXGameMPContentDLC_Shared_MP.SFXGameEffect_MatchConsumable_Gear_WeaponDamage_All":
			return "HUMANNAME";
		case "SentinelAsari":
			return "HUMANNAME";
		case "SentinelBatarian":
			return "HUMANNAME";
		case "SentinelHumanFemale":
			return "HUMANNAME";
		case "SentinelHumanMale":
			return "HUMANNAME";
		case "SentinelKrogan":
			return "HUMANNAME";
		case "SentinelKroganWarlord":
			return "HUMANNAME";
		case "SentinelN7":
			return "HUMANNAME";
		case "SentinelTurian":
			return "HUMANNAME";
		case "SentinelVolus":
			return "HUMANNAME";
		case "SentinelVorcha":
			return "HUMANNAME";
		case "SoldierBatarian":
			return "HUMANNAME";
		case "SoldierGeth":
			return "HUMANNAME";
		case "SoldierGethDestroyer":
			return "HUMANNAME";
		case "SoldierHumanFemale":
			return "HUMANNAME";
		case "SoldierHumanMale":
			return "HUMANNAME";
		case "SoldierHumanMaleBF3":
			return "HUMANNAME";
		case "SoldierKrogan":
			return "HUMANNAME";
		case "SoldierMQuarian":
			return "HUMANNAME";
		case "SoldierN7":
			return "HUMANNAME";
		case "SoldierTurian":
			return "HUMANNAME";
		case "SoldierVorcha":
			return "HUMANNAME";
		case "VanguardAsari":
			return "HUMANNAME";
		case "VanguardBatarian":
			return "HUMANNAME";
		case "VanguardDrell":
			return "HUMANNAME";
		case "VanguardHumanFemale":
			return "HUMANNAME";
		case "VanguardHumanMale":
			return "HUMANNAME";
		case "VanguardHumanMaleCerberus":
			return "HUMANNAME";
		case "VanguardKrogan":
			return "HUMANNAME";
		case "VanguardN7":
			return "HUMANNAME";
		case "VanguardTurianFemale":
			return "HUMANNAME";
		case "VanguardVolus":
			return "Volus Vanguard";
		default:
			return "case \"" + uniqueName + "\":\n\treturn \"HUMANNAME\";";
		}
	}
}
