package com.me3tweaks.modmanager.valueparsers.bioweapon;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class HTMLGenerator {
	static String outputFolder = "html/";
	static String templateFile = "template.php";
	static String bwFile = "bw.xml";
	
	//parameters
	static String weaponNamePlaceholder = "WEAPON_NAME";
	static String descriptionPlaceholder = "DESCRIPTION_TEXT";
	static String balanceChangesPlaceholder = "BALANCE_CHANGES_AREA";
	static String loadPlaceholder = "LOADNAME";
	static String humanPlaceholder = "HUMAN_NAME";
	//BLOCKS
	static String damageContainerPlaceholder = "DAMAGE_CONTAINER";
	static String damagePlaceholder = "DAMAGE_BLOCK";
	static String weightPlaceholder = "WEIGHT_BLOCK";
	static String rpmContainerPlaceholder = "RPM_CONTAINER";
	static String rpmPlaceholder = "RPM_BLOCK";
	static String rpmIncreasePlaceholder = "ROFINCREASE_BLOCK";
	static String spareAmmoPlaceholder = "SPAREAMMO_BLOCK";
	static String magsizePlaceholder = "MAGSIZE_BLOCK";
	static String recoilContainerPlaceholder = "RECOIL_CONTAINER";
	static String recoilStatPlaceholder = "HIPRECOIL_BLOCK";
	static String kishockrecoilStatPlaceholder = "KISHOCK_RECOIL_BLOCK";
	static String zoomrecoilStatPlaceholder = "ZOOMRECOIL_BLOCK";
	static String rechargeRatePlaceholder = "RECHARGE_RATE_BLOCK";
	static String accuracyPenaltyPlaceholder = "ACCURACYPENALTY_CONTAINER";
	static String accuracyErrorPlaceholder = "ACCURACYERROR_CONTAINER";
	static String hipPenaltyPlaceholder = "HIP_PENALTY_BLOCK";
	static String zoomPenaltyPlaceholder = "ZOOM_PENALTY_BLOCK";
	static String ammopershotPlaceholder = "AMMOPERSHOT_BLOCK";
	static String chargingPlaceholder = "CHARGING_CONTAINER";
	static String forcefirePlaceholder = "FORCEFIRE_BLOCK";
	static String chakramPlaceholder = "CHAKRAM_CONTAINER";
	static String headshotPlaceholder = "HEADSHOT_BLOCK";
	static String dotPlaceholder = "DAMAGEOVERTIME_BLOCK";
	static String punisherPlaceholder = "PUNISHER_CONTAINER";
	static String typhoonPlaceholder = "TYPHOON_CONTAINER";
	static String roundsPerBurstPlaceholder = "ROUNDSPERBURST_BLOCK";
	static String aimerrorPlaceholder = "STANDARDAIMERROR_BLOCK";
	static String zoomaimerrorPlaceholder = "ZOOMAIMERROR_BLOCK";
	static String minrefirePlaceholder = "MINREFIRE_BLOCK";
	static String tracerangePlaceholder = "TRACERANGE_BLOCK";
	static String gethshotgunPlaceholder = "GETHSHOTGUN_BLOCK";
	static String penetrationPlaceholder = "PENETRATION_CONTAINER";
	static String arcpistolPlaceholder = "ARCPISTOL_BLOCK";
	static String silencerPlaceholder = "SILENCER_CONTAINER";
	static String maxchargePlaceholder = "MAXCHARGE_BLOCK";
	static String minchargePlaceholder = "MINCHARGE_BLOCK";
	static String venomPlaceholder = "VENOM_CONTAINER";
	static String reloadspeedPlaceholder = "RELOADSPEED_BLOCK";
	//REPLACEMENTS
	static String balanceChangesBlock = ""+
			"<div class=\"newlinediv\">\n"+
    		"            <div class=\"warning\">\n"+
    		"        	 <p>The following properties are locked by balance changes:</p>\n"+
    		"        	     <ul>\n"+
    		"REPLACE_BLOCK"+
    		"        	     </ul>\n"+
    		"            </div>\n"+
    		"        </div>";
	
	static String damageContainer = ""+
    "                <!-- DAMAGE -->\n" +
    "                <div class=\"modmaker_attribute_wrapper\">\n" +
    "                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/damage.jpg\">\n" +
    "                    <h2 class=\"modmaker_attribute_title\">Damage</h2>\n" +
    "                    <p>Defines how much damage this weapon does and its modifiers.</p>\n" +
    "DAMAGE_BLOCK\n"+
    "HEADSHOT_BLOCK\n"+
    "DAMAGEOVERTIME_BLOCK\n"+
    "GETHSHOTGUN_BLOCK\n"+
    "                </div>";
	
	static String damageBlock = ""+
		    "                    <div class=\"modmaker_entry\">\n" +
		    "                        <div class=\"defaultbox\">\n" +
		    "                            <span class=\"inputtag defaultboxitem\">Lv 1 Damage</span>\n" +
		    "                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_damage_min?></span>\n" +
		    "                        </div>\n" +
		    "                        <input id=\"damage_min\" class=\"short_input\" type=\"text\" name=\"damage_min\" placeholder=\"Lv 1 Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_damage_min?>\">\n" +
		    "                    </div>\n" +
		    "                    <div class=\"modmaker_entry\">\n" +
		    "                        <div class=\"defaultbox\">\n" +
		    "                            <span class=\"inputtag defaultboxitem\">Lv 10 Damage</span>\n" +
		    "                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_damage_max?></span>\n" +
		    "                        </div>\n" +
		    "                        <input id=\"damage_max\" class=\"short_input\" type=\"text\" name=\"damage_max\" placeholder=\"Lv 10 Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_damage_max?>\">\n" +
		    "                    </div>";
	
	static String weightBlock = ""+
			"                <!-- WEAPON WEIGHT -->\n" + 
			"                <div class=\"modmaker_attribute_wrapper\">\n" + 
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/encumbrance.jpg\">\n" + 
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Weight</h2>\n" + 
			"                    <p>Sets how much impact choosing this weapon will have on your shared cooldown.</p>\n" + 
			"                    <div class=\"modmaker_entry\">\n" + 
			"                        <div class=\"defaultbox\">\n" + 
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Weapon Weight</span>\n" + 
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_encumbranceweight_min?></span>\n" + 
			"                        </div>\n" + 
			"                       <input id=\"encumbranceweight_min\" class=\"short_input\" type=\"text\" name=\"encumbranceweight_min\" placeholder=\"Lv 1 Weight\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_encumbranceweight_min?>\">\n" + 
			"                    </div>\n" + 
			"                    <div class=\"modmaker_entry\">\n" + 
			"                        <div class=\"defaultbox\">\n" + 
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Weapon Weight</span>\n" + 
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_encumbranceweight_max?></span>\n" + 
			"                        </div>\n" + 
			"                        <input id=\"encumbranceweight_max\" class=\"short_input\" type=\"text\" name=\"encumbranceweight_max\" placeholder=\"Lv 10 Weight\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_encumbranceweight_max?>\">\n" + 
			"                    </div>\n" + 
			"                </div>";
	
	static String rpmContainer = ""+
			"                <!-- RATE OF FIRE -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/rof.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Rate of Fire</h2>\n" +
			"                    <p>Defines how often you can shoot this weapon in terms of rounds per minute.</p>\n" +
			"RPM_BLOCKROFINCREASE_BLOCKMINREFIRE_BLOCKTRACERANGE_BLOCK\n"+
			"                </div>";
	static String rpmBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Rounds Per Minute</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_rateoffire_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"rateoffire_max\" class=\"short_input\" type=\"text\" name=\"rateoffire_max\" placeholder=\"Rate of Fire\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_rateoffire_max?>\">\n" +
			"                    </div>\n";
	static String tracerangeBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Weapon Max Range</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_tracerange?> cm</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"tracerange\" class=\"short_input\" type=\"text\" name=\"tracerange\" placeholder=\"Tracer Range\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_tracerange?>\">\n" +
			"                    </div>\n";
	static String rampBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Starting RPM</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_minrof?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"minrof\" class=\"short_input\" type=\"text\" name=\"minrof\" placeholder=\"Cool RPM\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_minrof?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Ramp Up Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_ramptime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"ramptime\" class=\"short_input\" type=\"text\" name=\"ramptime\" placeholder=\"Ramp up time\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_ramptime?>\">\n" +
			"                    </div>";
	
	static String heatUpBlock =""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Heat up time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_timetoheatup?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"timetoheatup\" class=\"short_input\" type=\"text\" name=\"timetoheatup\" placeholder=\"Max RPM Delay\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_timetoheatup?>\">\n" +
			"                    </div>";
	
	static String spareAmmoBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Spare Ammo</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_maxspareammo_min?> rounds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"maxspareammo_min\" class=\"short_input\" type=\"text\" name=\"maxspareammo_min\" placeholder=\"Spare Ammo\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_maxspareammo_min?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Spare Ammo</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_maxspareammo_max?> rounds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"maxspareammo_max\" class=\"short_input\" type=\"text\" name=\"maxspareammo_max\" placeholder=\"Spare Ammo\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_maxspareammo_max?>\">\n" +
			"                    </div>";
	
	static String magsizeBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Magazine</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_magsize_min?> rounds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"magsize_min\" class=\"short_input\" type=\"text\" name=\"magsize_min\" placeholder=\"Magazine Size\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_magsize_min?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Magazine</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_magsize_max?> rounds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"magsize_max\" class=\"short_input\" type=\"text\" name=\"magsize_max\" placeholder=\"Magazine Size\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_magsize_max?>\">\n" +
			"                    </div>";
	
	static String recoilStatBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Recoil</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_recoil_min?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"recoil_min\" class=\"short_input\" type=\"text\" name=\"recoil_min\" placeholder=\"Recoil\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_recoil_min?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Recoil</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_recoil_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"recoil_max\" class=\"short_input\" type=\"text\" name=\"recoil_max\" placeholder=\"Recoil\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_recoil_max?>\">\n" +
			"                    </div>";
	
	static String kishockrecoilStatBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charge Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_kishockrecoilmultiplier?> x Recoil</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"kishockrecoilmultiplier\" class=\"short_input\" type=\"text\" name=\"kishockrecoilmultiplier\" placeholder=\"Recoil\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_kishockrecoilmultiplier?>\">\n" +
			"                    </div>\n";

	static String zoomrecoilStatBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Aiming Recoil</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_zoomrecoil_min?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"zoomrecoil_min\" class=\"short_input\" type=\"text\" name=\"zoomrecoil_min\" placeholder=\"Aiming Recoil\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_zoomrecoil_min?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Aiming Recoil</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_zoomrecoil_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"zoomrecoil_max\" class=\"short_input\" type=\"text\" name=\"zoomrecoil_max\" placeholder=\"Aiming Recoil\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_zoomrecoil_max?>\">\n" +
			"                    </div>\n"+
			"KISHOCK_RECOIL_BLOCK";
	
	static String recoilContainer = ""+
			"                <!-- RECOIL -->\n"+
			"                <div class=\"modmaker_attribute_wrapper\">\n"+
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/recoil.jpg\">\n"+
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Recoil</h2>\n"+
			"                    <p>Recoil defines how the weapon reacts when fired, causing muzzle climb.</p>\n"+
			"HIPRECOIL_BLOCK\n"+
			"ZOOMRECOIL_BLOCK\n"+
			"ARCPISTOL_BLOCK\n"+
			"                </div>";
	
	static String rechargeBlock = ""+
			"                    <hr>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Partial Delay</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_partialrechargedelay?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"partialrechargedelay\" class=\"short_input\" type=\"text\" name=\"partialrechargedelay\" placeholder=\"Partial Recharge\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_partialrechargedelay?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Depleted Delay</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_fullrechargedelay?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"fullrechargedelay\" class=\"short_input\" type=\"text\" name=\"fullrechargedelay\" placeholder=\"Full Recharge\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_fullrechargedelay?>\">\n" +
			"                    </div>\n" +
			"                    <p>These delays are how long it takes for ammo to begin recharging from either a partially depleted magazine or a fully depleted one.</p>\n" +
			"                    <hr>\n"+
			"RECHARGE_RATE_BLOCK";
	
	static String reloadspeedBlock = ""+
	"                    <div class=\"modmaker_entry\">\n"+               
	"                        <div class=\"defaultbox\">\n"+               
	"                            <span class=\"inputtag defaultboxitem\">Reload Speed</span>\n"+               
	"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_reloadduration_max?> seconds</span>\n"+               
	"                        </div>\n"+               
	"                        <input id=\"reloadduration_max\" class=\"short_input\" type=\"text\" name=\"reloadduration_max\" placeholder=\"Reload Time\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_reloadduration_max?>\">\n"+               
	"                    </div>";
	
	static String rechargeRateBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Recharge Rate Per Sec</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_rechargeratepersecond?> of 1</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"rechargeratepersecond\" class=\"short_input\" type=\"text\" name=\"rechargeratepersecond\" placeholder=\"Full Recharge\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_rechargeratepersecond?>\">\n" +
			"                    </div>";
	
	static String roundsPerBurstBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Rounds Per Burst</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_roundsperburst?> round(s)</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"roundsperburst\" class=\"short_input\" type=\"text\" name=\"roundsperburst\" placeholder=\"Rounds Per Burst\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_roundsperburst?>\">\n" +
			"                    </div>";
	
	static String accuracyPenaltyContainer = ""+
			"                <div class=\"modmaker_attribute_wrapper\">\n"+
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/accuracy_penalty.jpg\">\n"+
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Aiming Penalties</h2>\n"+
			"                    <p>Changes how your accuracy is affected when firing.</p>\n"+
			"HIP_PENALTY_BLOCK\n"+
			"ZOOM_PENALTY_BLOCK\n"+
			"                </div>";
	
	static String accuracyErrorContainer = ""+
			"                <div class=\"modmaker_attribute_wrapper\">\n"+
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/accuracy_error.jpg\">\n"+
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Accuracy Error</h2>\n"+
			"                    <p>Changes how much error your weapon has in relation to bullets going to the target.</p>\n"+
			"STANDARDAIMERROR_BLOCK\n"+
			"ZOOMAIMERROR_BLOCK\n"+
			"                </div>";	
	

	
	static String hipPenaltyBlock = ""+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Hip Fire Penalty</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_accfirepenalty_min?></span>\n"+
			"                        </div>\n"+
			"                        <input id=\"accfirepenalty_min\" class=\"short_input\" type=\"text\" name=\"accfirepenalty_min\" placeholder=\"Hip Fire Penalty\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_accfirepenalty_min?>\">\n"+
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Hip Fire Penalty</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_accfirepenalty_max?></span>\n"+
			"                        </div>\n"+
			"                        <input id=\"accfirepenalty_max\" class=\"short_input\" type=\"text\" name=\"accfirepenalty_max\" placeholder=\"Hip Fire Penalty\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_accfirepenalty_max?>\">\n"+
			"                    </div>\n";
	
	static String zoomPenaltyBlock = ""+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Aiming Penalty</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_zoomaccfirepenalty_min?></span>\n"+
			"                        </div>\n"+
			"                        <input id=\"zoomaccfirepenalty_min\" class=\"short_input\" type=\"text\" name=\"zoomaccfirepenalty_min\" placeholder=\"Zoom Fire Penalty\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_zoomaccfirepenalty_min?>\">\n"+
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Aiming Penalty</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_zoomaccfirepenalty_max?></span>\n"+
			"                        </div>\n"+
			"                        <input id=\"zoomaccfirepenalty_max\" class=\"short_input\" type=\"text\" name=\"zoomaccfirepenalty_max\" placeholder=\"Zoom Fire Penalty\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_zoomaccfirepenalty_max?>\">\n"+
			"                    </div>\n";
	
	static String ammopershotBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Ammo Per Shot</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_ammopershot?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"ammopershot\" class=\"short_input\" type=\"text\" name=\"ammopershot\" placeholder=\"Ammo Per Shot\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_ammopershot?>\">\n" +
			"                    </div>\n";
	
	
	static String penetrationBlock = ""+
			"                <!-- WEAPON WEIGHT -->\n" + 
			"                <div class=\"modmaker_attribute_wrapper\">\n" + 
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/penetrate.jpg\">\n" + 
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Penetration</h2>\n" + 
			"                    <p>Penetration determines how far through blocking objects a round will go and still do damage on impact.</p>\n" + 
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Penetration Distance</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_distancepenetrated?> cm</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"distancepenetrated\" class=\"short_input\" type=\"text\" name=\"distancepenetrated\" placeholder=\"Ammo Per Shot\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_distancepenetrated?>\">\n" +
			"                    </div>\n"+
			"                </div>";
	
	static String chargingContainer = ""+
			"                <!-- WEAPON CHARGING -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/shared/charging.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">Weapon Charging</h2>\n" +
			"                    <p>This is a weapon that can be charged. When fully charged, you can force the weapon to fire or allow it to hold its charge.</p>\n" +
			"MINCHARGE_BLOCK\n"+
			"MAXCHARGE_BLOCK\n"+
			"FORCEFIRE_BLOCK\n"+
			"                </div>";
	
	static String forceFireBlock = ""+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Force Fire On Charge</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=(\\$mod->weapon->mod_weapon_WEAPON_NAME_bforcefireaftercharge) ? \"True\" : \"False\"?></span>\n"+
			"                        </div>\n"+
			"                        <input id=\"bforcefireaftercharge\" type=\"checkbox\" name=\"bforcefireaftercharge\" <?=(\\$mod->weapon->mod_weapon_WEAPON_NAME_bforcefireaftercharge) ? \"checked\" : \"\"?>>\n"+
			"                    </div>";
	
	static String minchargeBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Min Charge Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_minchargetime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"minchargetime\" class=\"short_input\" type=\"text\" name=\"minchargetime\" placeholder=\"Min Charge Time\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_minchargetime?>\">\n" +
			"                    </div>";

	static String maxchargeBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Max Charge Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_maxchargetime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"maxchargetime\" class=\"short_input\" type=\"text\" name=\"maxchargetime\" placeholder=\"Max Charge Time\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_maxchargetime?>\">\n" +
			"                    </div>";

	
	static String gethplasmashotgunBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">No Charge Damage</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_instantfiredamage?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"instantfiredamage\" class=\"short_input\" type=\"text\" name=\"instantfiredamage\" placeholder=\"No Charge Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_instantfiredamage?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charged - 1 Ball Hit</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: +<?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_firsthitdamage?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"firsthitdamage\" class=\"short_input\" type=\"text\" name=\"firsthitdamage\" placeholder=\"1st Hit Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_firsthitdamage?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Charged - 2 Ball Hit</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: +<?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_secondhitdamage?> x Damage</span>\n" +
			"                        </div>\n"+
			"                        <input id=\"secondhitdamage\" class=\"short_input\" type=\"text\" name=\"secondhitdamage\" placeholder=\"2nd Hit Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_secondhitdamage?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n"+
			"                        <div class=\"defaultbox\">\n"+
			"                            <span class=\"inputtag defaultboxitem\">Charged - 3 Ball Hit</span>\n"+
			"                            <span class=\"modmaker_default defaultboxitem\">Default: +<?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_thirdhitdamage?> x Damage</span>\n" +
			"                        </div>\n"+
			"                        <input id=\"thirdhitdamage\" class=\"short_input\" type=\"text\" name=\"thirdhitdamage\" placeholder=\"3rd Hit Damage\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_firsthitdamage?>\">\n" +
			"                    </div>";
	
	
	static String reckoningBlock = ""+
			"                <!-- CHAKRAM LAUNCHER SETTINGS -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/reckoning/chakramcharge.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">Chakram Launcher Charging</h2>\n" +
			"                    <p>The Chakram Launcher uses a different naming style of stats for charging compared to other charging weapons.</p>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charge Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_reckoningchargetime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"reckoningchargetime\" class=\"short_input\" type=\"text\" name=\"reckoningchargetime\" placeholder=\"Charge Time\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_reckoningchargetime?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charged Damage Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_reckoningdamagemultiplier?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"reckoningdamagemultiplier\" class=\"short_input\" type=\"text\" name=\"reckoningdamagemultiplier\" placeholder=\"Charged Dmg Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_reckoningdamagemultiplier?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charged Recoil Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_reckoningrecoilmultiplier?> x Recoil</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"reckoningrecoilmultiplier\" class=\"short_input\" type=\"text\" name=\"reckoningrecoilmultiplier\" placeholder=\"Charged Recoil Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_reckoningrecoilmultiplier?>\">\n" +
			"                    </div>\n" +
			"                </div>";
	
	static String punisherBlock = ""+
			"                <!-- BLOOD PACK PUNISHER SETTINGS -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/bloodpackpunisher/apround.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">Armor Piercing Rounds</h2>\n" +
			"                    <p>Stats specific to the Blood Pack Punisher's occasional AP rounds.</p>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Rounds before AP</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_shotsperapround?> rounds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"shotsperapround\" class=\"short_input\" type=\"text\" name=\"shotsperapround\" placeholder=\"Shots till AP\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_shotsperapround?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">AP Damage Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_apdamagemultiplier?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"apdamagemultiplier\" class=\"short_input\" type=\"text\" name=\"apdamagemultiplier\" placeholder=\"AP Damage Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_apdamagemultiplier?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">AP Recoil Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_aprecoilmultiplier?> x Recoil</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"aprecoilmultiplier\" class=\"short_input\" type=\"text\" name=\"aprecoilmultiplier\" placeholder=\"AP Recoil Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_aprecoilmultiplier?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">AP Aiming Recoil Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_apzoomrecoilmultiplier?> x A. Recoil</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"apzoomrecoilmultiplier\" class=\"short_input\" type=\"text\" name=\"apzoomrecoilmultiplier\" placeholder=\"AP Zoom Recoil Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_apzoomrecoilmultiplier?>\">\n" +
			"                    </div>\n" +
			"                </div>";
	
	static String typhoonBlock = ""+
			"                <!-- TYPHOON SETTINGS -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/typhoon/typhoon.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">N7 Typhoon Stats</h2>\n" +
			"                    <p>Stats specific to the N7 Typhoon's ramp up and damage protection.</p>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Damage Protection</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_damagereductionamount?> of 1</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"damagereductionamount\" class=\"short_input\" type=\"text\" name=\"damagereductionamount\" placeholder=\"Damage Protection\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_damagereductionamount?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charge Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_fullychargedtime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"fullychargedtime\" class=\"short_input\" type=\"text\" name=\"fullychargedtime\" placeholder=\"Ramp Charge Time?\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_fullychargedtime?>\">\n" +
			"                    </div>\n" +
			"                </div>";
	
	static String silencerBlock = ""+
			"                <!-- SUPPRESSOR SETTINGS -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/suppressor/suppressor.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">M-11 Suppressor Stats</h2>\n" +
			"                    <p>The suppressor does not give away the users location when firing for a short duration of time, giving them a non-visual cloak.</p>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Cloak Duration</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_silencercloaklength?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"silencercloaklength\" class=\"short_input\" type=\"text\" name=\"silencercloaklength\" placeholder=\"Cloak Length\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_silencercloaklength?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Cloak Cooldown</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_silencercloakcooldowntime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"silencercloakcooldowntime\" class=\"short_input\" type=\"text\" name=\"silencercloakcooldowntime\" placeholder=\"Cloak Cooldown?\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_silencercloakcooldowntime?>\">\n" +
			"                    </div>\n" +
			"                </div>";
	
	static String venomBlock = ""+
			"                <!-- VENOM SETTINGS -->\n" +
			"                <div class=\"modmaker_attribute_wrapper\">\n" +
			"                    <img class=\"guide purple_card\" src=\"/images/modmaker/weapons/venom/grenades.jpg\">\n" +
			"                    <h2 class=\"modmaker_attribute_title\">Venom Shotgun Grenades</h2>\n" +
			"                    <p>The Venom Shotgun fires grenades when cloaked. The grenades and the \"splitting\" of the main shot into grenades cause explosions that do damage.</p>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Split Explosion Damage</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_split_explosiondamage?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"split_explosiondamage\" class=\"short_input\" type=\"text\" name=\"split_explosiondamage\" placeholder=\"Split Damage Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_split_explosiondamage?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Number of Grenades</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_split_numshardstospawn?> grenades</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"split_numshardstospawn\" class=\"short_input\" type=\"text\" name=\"split_numshardstospawn\" placeholder=\"# of Grenades\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_split_numshardstospawn?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Grenade Damage Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_shard_explosiondamage?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"shard_explosiondamage\" class=\"short_input\" type=\"text\" name=\"shard_explosiondamage\" placeholder=\"Shard Damage Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_shard_explosiondamage?>\">\n" +
			"                    </div>\n" +
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Grenade Explosion Radius</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_shard_explosionradius?> x cm</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"shard_explosionradius\" class=\"short_input\" type=\"text\" name=\"shard_explosionradius\" placeholder=\"Explosion Radius\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_shard_explosionradius?>\">\n" +
			"                    </div>\n" +
			"                </div>";
/*
  <Section name="sfxgamecontentdlc_con_mp5.sfxprojectile_salarianblastsplitter_mp">
    <Property name="explosiondamage" type="0">1.f</Property>
    <Property name="numshardstospawn" type="0">3</Property>
  </Section>*/
	
	static String arcpistolBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Charged Recoil Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_thorrecoilmultiplier?> x Recoil</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"thorrecoilmultiplier\" class=\"short_input\" type=\"text\" name=\"thorrecoilmultiplier\" placeholder=\"Recoil Mult\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_thorrecoilmultiplier?>\">\n" +
			"                    </div>";
	
	static String headshotBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 1 Headshot Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_headshotdamagemultiplier_min?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"headshotdamagemultiplier_min\" class=\"short_input\" type=\"text\" name=\"headshotdamagemultiplier_min\" placeholder=\"Lv 1 Headshot\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_headshotdamagemultiplier_min?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Lv 10 Headshot Multiplier</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_headshotdamagemultiplier_max?> x Damage</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"headshotdamagemultiplier_max\" class=\"short_input\" type=\"text\" name=\"headshotdamagemultiplier_max\" placeholder=\"Lv 10 Headshot \" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_headshotdamagemultiplier_max?>\">\n" +
			"                    </div>";
	
	static String aimerrorBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Min Hip Fire Drift</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_minaimerror_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"minaimerror_max\" class=\"short_input\" type=\"text\" name=\"minaimerror_max\" placeholder=\"Min Aim Error\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_minaimerror_max?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Max Hip Fire Drift</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_maxaimerror_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"maxaimerror_max\" class=\"short_input\" type=\"text\" name=\"maxaimerror_max\" placeholder=\"Max Aim Error\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_maxaimerror_max?>\">\n" +
			"                    </div>\n";

	static String minrefireBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Min Refire Time</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_minrefiretime?> seconds</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"minrefiretime\" class=\"short_input\" type=\"text\" name=\"minrefiretime\" placeholder=\"Time Between Bursts\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_minrefiretime?>\">\n" +
			"                    </div>\n";
	
	static String zoomaimerrorBlock = ""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Min Aiming Drift</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_minzoomaimerror_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"minzoomaimerror_max\" class=\"short_input\" type=\"text\" name=\"minzoomaimerror_max\" placeholder=\"Min Zoom Error\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_minzoomaimerror_max?>\">\n" +
			"                    </div>\n"+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">Max Aiming Drift</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_maxzoomaimerror_max?></span>\n" +
			"                        </div>\n" +
			"                        <input id=\"maxzoomaimerror_max\" class=\"short_input\" type=\"text\" name=\"maxzoomaimerror_max\" placeholder=\"Max Zoom Error\" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_maxzoomaimerror_max?>\">\n" +
			"                    </div>";
	
	static String dotBlock =""+
			"                    <div class=\"modmaker_entry\">\n" +
			"                        <div class=\"defaultbox\">\n" +
			"                            <span class=\"inputtag defaultboxitem\">DoT Duration</span>\n" +
			"                            <span class=\"modmaker_default defaultboxitem\">Default: <?=\\$defaultsmod->weapon->mod_weapon_WEAPON_NAME_dotduration?> second</span>\n" +
			"                        </div>\n" +
			"                        <input id=\"dotduration\" class=\"short_input\" type=\"text\" name=\"dotduration\" placeholder=\"Lv 10 Headshot \" value=\"<?=\\$mod->weapon->mod_weapon_WEAPON_NAME_dotduration?>\">\n" +
			"                    </div>";
			
	/**
	 * <weaponName, InfoObject>
	 */
	static HashMap<String, HTMLParams> fileMap;
	
	public static void main(String[] args) throws IOException {
		File outFolder = new File(outputFolder);
		outFolder.mkdirs();
		fileMap = new HashMap<String, HTMLParams>();
		//printKeys();
		generateData();
		
		HTMLParams params_assaultrifle_adas_mp= new HTMLParams();
		params_assaultrifle_adas_mp.setHumanName("Adas Anti-Synthetic Rifle");
		params_assaultrifle_adas_mp.setDescription("These settings affect the Adas, a slow firing assault rifle that has a 200% damage bonus against shields and staggers groups of enemies.");
		params_assaultrifle_adas_mp.setFileName("adas.php");
		params_assaultrifle_adas_mp.setLoadName("Assaultrifleadasmp");
		params_assaultrifle_adas_mp.addBalanceChange("Damage: 178.1 - 222.6");
		fileMap.put("assaultrifle_adas_mp", params_assaultrifle_adas_mp);
		
		HTMLParams params_assaultrifle_lancer_mp= new HTMLParams();
		params_assaultrifle_lancer_mp.setHumanName("M-7 Avenger");
		params_assaultrifle_lancer_mp.setDescription("These settings affect the Lancer, a weapon from Mass Effect 1 that does not use thermal clips. It is an upgraded Avenger.");
		params_assaultrifle_lancer_mp.setFileName("lancer.php");
		params_assaultrifle_lancer_mp.setLoadName("Assaultriflelancermp");
		params_assaultrifle_lancer_mp.setBalanceChanges(null);
		params_assaultrifle_lancer_mp.setInfiniteAmmo(true);
		fileMap.put("assaultrifle_lancer_mp", params_assaultrifle_lancer_mp);
		
		HTMLParams params_assaultrifle_argus= new HTMLParams();
		params_assaultrifle_argus.setHumanName("M-7 Argus");
		params_assaultrifle_argus.setDescription("These settings affect the Argus, a 3-shot battle rifle with good accuracy but high recoil.");
		params_assaultrifle_argus.setFileName("argus.php");
		params_assaultrifle_argus.setLoadName("Assaultrifleargus");
		params_assaultrifle_argus.addBalanceChange("Damage: 164.8 - 206.0");
		fileMap.put("assaultrifle_argus", params_assaultrifle_argus);

		HTMLParams params_assaultrifle_avenger= new HTMLParams();
		params_assaultrifle_avenger.setHumanName("M-8 Lancer");
		params_assaultrifle_avenger.setDescription("These settings affect the M-8 Avenger, a very common assault rifle that does little damage and reloads quickly.");
		params_assaultrifle_avenger.setFileName("avenger.php");
		params_assaultrifle_avenger.setLoadName("Assaultrifleavenger");
		fileMap.put("assaultrifle_avenger", params_assaultrifle_avenger);

		HTMLParams params_assaultrifle_cobra= new HTMLParams();
		params_assaultrifle_cobra.setHumanName("Phaesteon");
		params_assaultrifle_cobra.setDescription("These settings affect the Phaesteon, a turian weapon that has a large magazine and fair accuracy.");
		params_assaultrifle_cobra.setFileName("phaesteon.php");
		params_assaultrifle_cobra.setLoadName("Assaultriflecobra");
		params_assaultrifle_cobra.addBalanceChange("Damage: 41.9 - 52.4");
		params_assaultrifle_cobra.addBalanceChange("Weight: 1.35 - 0.8");
		params_assaultrifle_cobra.addBalanceChange("Spare Ammo: 350 - 450");
		fileMap.put("assaultrifle_cobra", params_assaultrifle_cobra);

		HTMLParams params_assaultrifle_collector= new HTMLParams();
		params_assaultrifle_collector.setHumanName("Collector Rifle");
		params_assaultrifle_collector.setDescription("These settings affect the Collector Rifle, a standard assault rifle with fair accuracy and magazine size.");
		params_assaultrifle_collector.setFileName("collectorrifle.php");
		params_assaultrifle_collector.setLoadName("Assaultriflecollector");
		params_assaultrifle_collector.addBalanceChange("Damage: 55.4 - 69.2");
		params_assaultrifle_collector.addBalanceChange("Weight: 1.6 - 1.2");
		params_assaultrifle_collector.addBalanceChange("Spare Ammo: 308 - 392");
		fileMap.put("assaultrifle_collector", params_assaultrifle_collector);

		HTMLParams params_assaultrifle_falcon= new HTMLParams();
		params_assaultrifle_falcon.setHumanName("M-37 Falcon");
		params_assaultrifle_falcon.setDescription("These settings affect the Falcon, an assault rifle that shoots grenades that bounce.");
		params_assaultrifle_falcon.setFileName("falcon.php");
		params_assaultrifle_falcon.setLoadName("Assaultriflefalcon");
		params_assaultrifle_falcon.addBalanceChange("Damage: 279.2 - 349.0");
		params_assaultrifle_falcon.addBalanceChange("Magazine Size: 6 rounds");
		params_assaultrifle_falcon.addBalanceChange("Spare Ammo: 18 - 28");
		params_assaultrifle_falcon.addBalanceChange("RPM: 50");
		params_assaultrifle_falcon.addBalanceChange("Weight: 1.75 - 1.0");		
		fileMap.put("assaultrifle_falcon", params_assaultrifle_falcon);

		HTMLParams params_assaultrifle_geth= new HTMLParams();
		params_assaultrifle_geth.setHumanName("Geth Pulse Rifle");
		params_assaultrifle_geth.setDescription("These settings affect the Geth Pulse Rifle, a weak but fast and accurate assault rifle.");
		params_assaultrifle_geth.setFileName("gethpulserifle.php");
		params_assaultrifle_geth.setLoadName("Assaultriflegeth");
		params_assaultrifle_geth.addBalanceChange("Damage: 28.8 - 36.0");
		params_assaultrifle_geth.addBalanceChange("Headshot Multiplier: 3x");
		params_assaultrifle_geth.addBalanceChange("Magazine Size: 100 rounds");
		params_assaultrifle_geth.addBalanceChange("Spare Ammo: 480 - 640");
		fileMap.put("assaultrifle_geth", params_assaultrifle_geth);

		HTMLParams params_assaultrifle_mattock= new HTMLParams();
		params_assaultrifle_mattock.setHumanName("M-96 Mattock");
		params_assaultrifle_mattock.setDescription("These settings affect the Mattock, a semi-automatic assault rifle that is accurate and powerful.");
		params_assaultrifle_mattock.setFileName("mattock.php");
		params_assaultrifle_mattock.setLoadName("Assaultriflemattock");
		params_assaultrifle_mattock.setBalanceChanges(null);
		params_assaultrifle_mattock.addBalanceChange("Damage: 103.6 129.5");
		fileMap.put("assaultrifle_mattock", params_assaultrifle_mattock);

		HTMLParams params_assaultrifle_reckoning= new HTMLParams();
		params_assaultrifle_reckoning.setHumanName("Chakram Launcher");
		params_assaultrifle_reckoning.setDescription("These settings affect the Chakram Launcher, a weapon similar to the Scorpion, but fires faster and can be charged. ModMaker mods add this weapon to the multiplayer store.");
		params_assaultrifle_reckoning.setFileName("chakram.php");
		params_assaultrifle_reckoning.setLoadName("Assaultriflereckoning");
		params_assaultrifle_reckoning.setBalanceChanges(null);
		params_assaultrifle_reckoning.setChakramLauncher(true);
		fileMap.put("assaultrifle_reckoning", params_assaultrifle_reckoning);

		HTMLParams params_assaultrifle_revenant= new HTMLParams();
		params_assaultrifle_revenant.setHumanName("M-76 Revenant");
		params_assaultrifle_revenant.setDescription("These settings affect the Revenant, a fast firing assault rifle with very poor accuracy.");
		params_assaultrifle_revenant.setFileName("revenant.php");
		params_assaultrifle_revenant.setLoadName("Assaultriflerevenant");
		params_assaultrifle_revenant.setBalanceChanges(null);
		params_assaultrifle_revenant.addBalanceChange("Damage: 63.6 - 79.5");
		params_assaultrifle_revenant.addBalanceChange("Weight: 1.8 - 1.25");
		params_assaultrifle_revenant.addBalanceChange("Spare Ammo: 360 - 480");
		fileMap.put("assaultrifle_revenant", params_assaultrifle_revenant);

		HTMLParams params_assaultrifle_saber= new HTMLParams();
		params_assaultrifle_saber.setHumanName("M-99 Saber");
		params_assaultrifle_saber.setDescription("These settings affect the Saber, a DMR that has near perfect accuracy.");
		params_assaultrifle_saber.setFileName("saber.php");
		params_assaultrifle_saber.setLoadName("Assaultriflesaber");
		params_assaultrifle_saber.addBalanceChange("Damage: 460.0 - 575.0");
		params_assaultrifle_saber.addBalanceChange("Weight: 1.7 - 1.4");
		params_assaultrifle_saber.addBalanceChange("Spare Ammo: 40 - 56");
		fileMap.put("assaultrifle_saber", params_assaultrifle_saber);

		HTMLParams params_assaultrifle_valkyrie= new HTMLParams();
		params_assaultrifle_valkyrie.setHumanName("N7 Valkyrie");
		params_assaultrifle_valkyrie.setDescription("These settings affect the Valkyrie, a two shot battle rifle with a 3x headshot multiplier.");
		params_assaultrifle_valkyrie.setFileName("valkyrie.php");
		params_assaultrifle_valkyrie.setLoadName("Assaultriflevalkyrie");
		params_assaultrifle_valkyrie.addBalanceChange("Damage: 119.9 149.9");
		params_assaultrifle_valkyrie.addBalanceChange("Weight: 1.5 - 1.25");
		params_assaultrifle_valkyrie.addBalanceChange("Headshot Multiplier: 3x");
		fileMap.put("assaultrifle_valkyrie", params_assaultrifle_valkyrie);

		HTMLParams params_assaultrifle_vindicator= new HTMLParams();
		params_assaultrifle_vindicator.setHumanName("M-15 Vindicator");
		params_assaultrifle_vindicator.setDescription("These settings affect the Vinidcator, a 3 shot battle rifle with relatively low damage.");
		params_assaultrifle_vindicator.setFileName("vindicator.php");
		params_assaultrifle_vindicator.setLoadName("Assaultriflevindicator");
		params_assaultrifle_vindicator.addBalanceChange("Damage: 68.6 - 85.8");
		params_assaultrifle_vindicator.addBalanceChange("Weight: 1.25 - 0.7");
		fileMap.put("assaultrifle_vindicator", params_assaultrifle_vindicator);

		HTMLParams params_heavy_blackstar= new HTMLParams();
		params_heavy_blackstar.setHumanName("Reaper Blackstar");
		params_heavy_blackstar.setDescription("These settings affect the Blackstar, a heavy weapon found in singleplayer. It charges up and fires a single high damage explosive bomb.");
		params_heavy_blackstar.setFileName("blackstar.php");
		params_heavy_blackstar.setLoadName("Heavyblackstar");
		params_heavy_blackstar.setBalanceChanges(null);
		params_heavy_blackstar.setForceFireAfterCharge(true);
		fileMap.put("heavy_blackstar", params_heavy_blackstar);

		HTMLParams params_heavy_cain= new HTMLParams();
		params_heavy_cain.setHumanName("M-920 Cain");
		params_heavy_cain.setDescription("These settings affect the Cain, a heavy weapon in singleplayer that takes several seconds to charge but annihilates nearly everything in sight.");
		params_heavy_cain.setFileName("cain.php");
		params_heavy_cain.setLoadName("Heavycain");
		params_heavy_cain.setBalanceChanges(null);
		params_heavy_cain.setAmmoPerShot(true);
		params_heavy_cain.setForceFireAfterCharge(true);
		fileMap.put("heavy_cain", params_heavy_cain);

		HTMLParams params_heavy_flamethrower_npc= new HTMLParams();
		params_heavy_flamethrower_npc.setHumanName("M-451 Firestorm (NPC)");
		params_heavy_flamethrower_npc.setDescription("These settings affect the flamethrower that non-player characters in single player use.");
		params_heavy_flamethrower_npc.setFileName("npcflamethrower.php");
		params_heavy_flamethrower_npc.setLoadName("Heavyflamethrowernpc");
		params_heavy_flamethrower_npc.setBalanceChanges(null);
		fileMap.put("heavy_flamethrower_npc", params_heavy_flamethrower_npc);

		HTMLParams params_heavy_flamethrower_player= new HTMLParams();
		params_heavy_flamethrower_player.setHumanName("M-451 Firestorm (Player)");
		params_heavy_flamethrower_player.setDescription("These settings affect the flamethrower that Shepard uses on some single player missions.");
		params_heavy_flamethrower_player.setFileName("playerflamethrower.php");
		params_heavy_flamethrower_player.setLoadName("Heavyflamethrowerplayer");
		params_heavy_flamethrower_player.setBalanceChanges(null);
		params_heavy_flamethrower_player.setForceFireAfterCharge(true);
		fileMap.put("heavy_flamethrower_player", params_heavy_flamethrower_player);

		HTMLParams params_heavy_minigun= new HTMLParams();
		params_heavy_minigun.setHumanName("Geth Spitfire (SP)");
		params_heavy_minigun.setDescription("These settings affect the Geth Spitfire in SP (named Minigun), a heavy weapon used in some stages by Shepard.");
		params_heavy_minigun.setFileName("minigun.php");
		params_heavy_minigun.setLoadName("Heavyminigun");
		params_heavy_minigun.setBalanceChanges(null);
		fileMap.put("heavy_minigun", params_heavy_minigun);

		HTMLParams params_heavy_mountedgun= new HTMLParams();
		params_heavy_mountedgun.setHumanName("Mounted Turret Gun");
		params_heavy_mountedgun.setDescription("These settings affect the turret guns found on some stages in singleplayer, such as Menae.");
		params_heavy_mountedgun.setFileName("mountedturret.php");
		params_heavy_mountedgun.setLoadName("Heavymountedgun");
		params_heavy_mountedgun.setBalanceChanges(null);
		fileMap.put("heavy_mountedgun", params_heavy_mountedgun);

		HTMLParams params_heavy_titanmissilelauncher= new HTMLParams();
		params_heavy_titanmissilelauncher.setHumanName("Hydra Missile Launcher");
		params_heavy_titanmissilelauncher.setDescription("These settings affect the singleplayer rocket launcher that launches multiple homing missiles.");
		params_heavy_titanmissilelauncher.setFileName("titanmissilelauncher.php");
		params_heavy_titanmissilelauncher.setLoadName("Heavytitanmissilelauncher");
		params_heavy_titanmissilelauncher.setBalanceChanges(null);
		fileMap.put("heavy_titanmissilelauncher", params_heavy_titanmissilelauncher);

		HTMLParams params_pistol_carnifex= new HTMLParams();
		params_pistol_carnifex.setHumanName("M-6 Carnifex");
		params_pistol_carnifex.setDescription("These settings affect the Carnifex, a slow firing yet accurate sidearm.");
		params_pistol_carnifex.setFileName("carnifex.php");
		params_pistol_carnifex.setLoadName("Pistolcarnifex");
		params_pistol_carnifex.addBalanceChange("Weight: 1.2 - 0.7");
		fileMap.put("pistol_carnifex", params_pistol_carnifex);

		HTMLParams params_pistol_eagle= new HTMLParams();
		params_pistol_eagle.setHumanName("N7 Eagle");
		params_pistol_eagle.setDescription("These settings affect the Eagle, a fully automatic pistol that additionally has good accuracy.");
		params_pistol_eagle.setFileName("eagle.php");
		params_pistol_eagle.setLoadName("Pistoleagle");
		params_pistol_eagle.addBalanceChange("Damage: 86.1 - 107.7");
		params_pistol_eagle.addBalanceChange("Magazine Size: 24 rounds");
		params_pistol_eagle.addBalanceChange("Weight: 0.45 - 0.25");
		params_pistol_eagle.addBalanceChange("Spare Ammo: 192 - 240");
		fileMap.put("pistol_eagle", params_pistol_eagle);

		HTMLParams params_pistol_ivory= new HTMLParams();
		params_pistol_ivory.setHumanName("M-77 Paladin");
		params_pistol_ivory.setDescription("These settings affect the Paladin, a variant of the Carnfiex with a smaller magazine but more damage.");
		params_pistol_ivory.setFileName("paladin.php");
		params_pistol_ivory.setLoadName("Pistolivory");
		params_pistol_ivory.addBalanceChange("Damage: 424.9 - 531.1");
		params_pistol_ivory.addBalanceChange("Magazine Size: 3 rounds");
		params_pistol_ivory.addBalanceChange("Weight: 1.0 - 0.7");
		params_pistol_ivory.addBalanceChange("Spare Ammo: 21 - 33");		
		fileMap.put("pistol_ivory", params_pistol_ivory);

		HTMLParams params_pistol_phalanx= new HTMLParams();
		params_pistol_phalanx.setHumanName("M-5 Phalanx");
		params_pistol_phalanx.setDescription("These settings affect the Phalanx, a slow firing pistol that does moderate damage.");
		params_pistol_phalanx.setFileName("phalanx.php");
		params_pistol_phalanx.setLoadName("Pistolphalanx");
		params_pistol_phalanx.setBalanceChanges(null);
		fileMap.put("pistol_phalanx", params_pistol_phalanx);

		HTMLParams params_pistol_predator= new HTMLParams();
		params_pistol_predator.setHumanName("M-3 Predator");
		params_pistol_predator.setDescription("These settings affect the Predator, a weak but fast firing sidearm.");
		params_pistol_predator.setFileName("predator.php");
		params_pistol_predator.setLoadName("Pistolpredator");
		params_pistol_predator.setBalanceChanges(null);
		fileMap.put("pistol_predator", params_pistol_predator);

		HTMLParams params_pistol_scorpion= new HTMLParams();
		params_pistol_scorpion.setHumanName("Scorpion");
		params_pistol_scorpion.setDescription("These settings affect the Scorpion, an adhesive explosive launcher. The explosives detonate on proximity or after a set amount of time.");
		params_pistol_scorpion.setFileName("scorpion.php");
		params_pistol_scorpion.setLoadName("Pistolscorpion");
		params_pistol_scorpion.addBalanceChange("Weight: 1.1 - 0.6");
		fileMap.put("pistol_scorpion", params_pistol_scorpion);

		HTMLParams params_pistol_talon= new HTMLParams();
		params_pistol_talon.setHumanName("M-358 Talon");
		params_pistol_talon.setDescription("These settings affect the Talon, a mini shotgun in the shape of a pistol.");
		params_pistol_talon.setFileName("talon.php");
		params_pistol_talon.setLoadName("Pistoltalon");
		params_pistol_talon.addBalanceChange("Damage: 93.7 - 117.1");
		params_pistol_talon.addBalanceChange("Weight: 0.9 - 0.6");
		params_pistol_talon.addBalanceChange("Spare Ammo: 24 - 36");
		fileMap.put("pistol_talon", params_pistol_talon);
		
		HTMLParams params_pistol_thor= new HTMLParams();
		params_pistol_thor.setHumanName("Arc Pistol");
		params_pistol_thor.setDescription("These settings affect the Arc Pistol, a quarian charge weapon that fires multiple shots when charged.");
		params_pistol_thor.setFileName("arcpistol.php");
		params_pistol_thor.setLoadName("Pistolthor");
		params_pistol_thor.addBalanceChange("Damage: 77.2 - 96.5");
		params_pistol_thor.addBalanceChange("Weight: 1.1 - 0.6");
		params_pistol_thor.addBalanceChange("Charged Damage Multiplier: 3x");
		params_pistol_thor.setArcPistol(true);
		fileMap.put("pistol_thor", params_pistol_thor);

		HTMLParams params_shotgun_claymore= new HTMLParams();
		params_shotgun_claymore.setHumanName("M-300 Claymore");
		params_shotgun_claymore.setDescription("These settings affect the Claymore, a very powerful short range shotgun. Commonly carried by headbutting Krogan squads.");
		params_shotgun_claymore.setFileName("claymore.php");
		params_shotgun_claymore.setLoadName("Shotgunclaymore");
		params_shotgun_claymore.addBalanceChange("Damage: 167.8 - 206.0)");
		fileMap.put("shotgun_claymore", params_shotgun_claymore);

		HTMLParams params_shotgun_crusader= new HTMLParams();
		params_shotgun_crusader.setHumanName("N7 Crusader");
		params_shotgun_crusader.setDescription("These settings affect the Crusader, a near perfect aim shotgun that acts more like a sniper rifle than a shotgun.");
		params_shotgun_crusader.setFileName("crusader.php");
		params_shotgun_crusader.setLoadName("Shotguncrusader");
		params_shotgun_crusader.addBalanceChange("Damage: 630.7 - 788.4");
		params_shotgun_crusader.addBalanceChange("Weight: 2.3 - 2.0");
		fileMap.put("shotgun_crusader", params_shotgun_crusader);

		HTMLParams params_shotgun_disciple= new HTMLParams();
		params_shotgun_disciple.setHumanName("Disciple");
		params_shotgun_disciple.setDescription("These settings affect the Disciple, a very light yet weak weapon that has a very high stagger chance, and is often found on caster classes.");
		params_shotgun_disciple.setFileName("disciple.php");
		params_shotgun_disciple.setLoadName("Shotgundisciple");
		params_shotgun_disciple.addBalanceChange("Damage: 55.0 - 69.5");
		fileMap.put("shotgun_disciple", params_shotgun_disciple);

		HTMLParams params_shotgun_eviscerator= new HTMLParams();
		params_shotgun_eviscerator.setHumanName("M-22 Eviscerator");
		params_shotgun_eviscerator.setDescription("These settings affect the Eviscerator, a shotgun with good range and damage, though a fairly restrictive magazine size and rate of fire.");
		params_shotgun_eviscerator.setFileName("eviscerator.php");
		params_shotgun_eviscerator.setLoadName("Shotguneviscerator");
		params_shotgun_eviscerator.addBalanceChange("Damage: 64.6 - 80.7");
		params_shotgun_eviscerator.addBalanceChange("Weight: 1.25 - 0.7");
		fileMap.put("shotgun_eviscerator", params_shotgun_eviscerator);

		HTMLParams params_shotgun_geth= new HTMLParams();
		params_shotgun_geth.setHumanName("Geth Plasma Shotgun");
		params_shotgun_geth.setDescription("These settings affect the Geth Plasma Shotgun, a shotgun that staggers most enemies and can be charged for additional damage.");
		params_shotgun_geth.setFileName("gethplasmashotgun.php");
		params_shotgun_geth.setLoadName("Shotgungeth");
		params_shotgun_geth.setBalanceChanges(null);
		params_shotgun_geth.setGethShotgun(true);
		fileMap.put("shotgun_geth", params_shotgun_geth);

		HTMLParams params_shotgun_graal= new HTMLParams();
		params_shotgun_graal.setHumanName("Graal Spike Thrower");
		params_shotgun_graal.setDescription("These settings affect the Graal, a weapon that causes bleedout damage for a short amount of time and can be charged for more damage.");
		params_shotgun_graal.setFileName("graal.php");
		params_shotgun_graal.setLoadName("Shotgungraal");
		params_shotgun_graal.addBalanceChange("Damage: 88.0 - 110.0");
		params_shotgun_graal.addBalanceChange("Headshot Multiplier: 3x");
		params_shotgun_graal.setDamageOverTime(true);
		fileMap.put("shotgun_graal", params_shotgun_graal);

		HTMLParams params_shotgun_katana= new HTMLParams();
		params_shotgun_katana.setHumanName("M-23 Katana");
		params_shotgun_katana.setDescription("These settings affect the Katana, a standard issue shotgun with no standout weaknesses or strengths.");
		params_shotgun_katana.setFileName("katana.php");
		params_shotgun_katana.setLoadName("Shotgunkatana");
		params_shotgun_katana.setBalanceChanges(null);
		fileMap.put("shotgun_katana", params_shotgun_katana);

		HTMLParams params_shotgun_raider= new HTMLParams();
		params_shotgun_raider.setHumanName("AT-12 Raider");
		params_shotgun_raider.setDescription("These settings affect the Raider, a deadly dual barreled shotgun that requires the user to be at point blank.");
		params_shotgun_raider.setFileName("raider.php");
		params_shotgun_raider.setLoadName("Shotgunraider");
		params_shotgun_raider.addBalanceChange("Damage: 100.0 - 125.0");
		params_shotgun_raider.addBalanceChange("Spare Ammo: 30 - 40");
		fileMap.put("shotgun_raider", params_shotgun_raider);

		HTMLParams params_shotgun_scimitar= new HTMLParams();
		params_shotgun_scimitar.setHumanName("M-27 Scimitar");
		params_shotgun_scimitar.setDescription("These settings affect the Scimitar, a decent all around shotgun with a good magazine size, range, and rate of fire.");
		params_shotgun_scimitar.setFileName("scimitar.php");
		params_shotgun_scimitar.setLoadName("Shotgunscimitar");
		params_shotgun_scimitar.addBalanceChange("Weight: 1.15 - 0.6");
		fileMap.put("shotgun_scimitar", params_shotgun_scimitar);

		HTMLParams params_shotgun_striker= new HTMLParams();
		params_shotgun_striker.setHumanName("M-11 Wraith");
		params_shotgun_striker.setDescription("These settings affect the Wraith, a variant of the Eviscerator with a smaller magazine but more damage.");
		params_shotgun_striker.setFileName("wraith.php");
		params_shotgun_striker.setLoadName("Shotgunstriker");
		params_shotgun_striker.addBalanceChange("Damage: 117.6 - 147.0");
		params_shotgun_striker.addBalanceChange("Weight: 1.2 - 0.9");
		params_shotgun_striker.addBalanceChange("Spare Ammo: 18 - 28");
		fileMap.put("shotgun_striker", params_shotgun_striker);

		HTMLParams params_smg_hornet= new HTMLParams();
		params_smg_hornet.setHumanName("M-25 Hornet");
		params_smg_hornet.setDescription("These settings affect the Hornet, a burst fire SMG with more damage and recoil than the Shuriken.");
		params_smg_hornet.setFileName("hornet.php");
		params_smg_hornet.setLoadName("Smghornet");
		params_smg_hornet.addBalanceChange("Weight: 0.85 - 0.45");
		params_smg_hornet.addBalanceChange("Spare Ammo: 168 - 216");
		fileMap.put("smg_hornet", params_smg_hornet);

		HTMLParams params_smg_hurricane= new HTMLParams();
		params_smg_hurricane.setHumanName("N7 Hurricane");
		params_smg_hurricane.setDescription("These settings affect the Hurricane, a very high rate of fire SMG with decent damage, fairly small magazine, and high level of recoil.");
		params_smg_hurricane.setFileName("hurricane.php");
		params_smg_hurricane.setLoadName("Smghurricane");
		params_smg_hurricane.addBalanceChange("Damage: 102.5 - 128.1");
		params_smg_hurricane.addBalanceChange("Weight: 0.85 - 0.45");
		params_smg_hurricane.addBalanceChange("Spare Ammo: 280 - 360");
		params_smg_hurricane.addBalanceChange("Recoil: 0.75");
		params_smg_hurricane.addBalanceChange("Aiming Recoil: 0.85");
		params_smg_hurricane.setAmmoPerShot(true);
		fileMap.put("smg_hurricane", params_smg_hurricane);

		HTMLParams params_smg_locust= new HTMLParams();
		params_smg_locust.setHumanName("M-12 Locust");
		params_smg_locust.setDescription("These settings affect the Locust, a SMG that was once famous and is now infamous for its low damage output.");
		params_smg_locust.setFileName("locust.php");
		params_smg_locust.setLoadName("Smglocust");
		params_smg_locust.addBalanceChange("Damage: 40.8 - 51.0");
		params_smg_locust.addBalanceChange("Magazine Size: 25 rounds");
		params_smg_locust.addBalanceChange("Spare Ammo: 275 - 350");
		params_smg_locust.addBalanceChange("Headshot Multiplier: 3x");
		fileMap.put("smg_locust", params_smg_locust);
		
		HTMLParams params_smg_shuriken= new HTMLParams();
		params_smg_shuriken.setHumanName("M-4 Shuriken");
		params_smg_shuriken.setDescription("These settings affect the Shuriken, a burst fire SMG.");
		params_smg_shuriken.setFileName("shuriken.php");
		params_smg_shuriken.setLoadName("Smgshuriken");
		params_smg_shuriken.addBalanceChange("Damage: 38.7 - 48.3");
		params_smg_shuriken.addBalanceChange("Weight: 0.45 - 0.2");
		params_smg_shuriken.addBalanceChange("Spare Ammo: 360 - 468");
		fileMap.put("smg_shuriken", params_smg_shuriken);

		HTMLParams params_smg_tempest= new HTMLParams();
		params_smg_tempest.setHumanName("M-9 Tempest");
		params_smg_tempest.setDescription("These settings affect the Tempest, a SMG with a large clip and modest damage for an SMG.");
		params_smg_tempest.setFileName("tempest.php");
		params_smg_tempest.setLoadName("Smgtempest");
		params_smg_tempest.addBalanceChange("Damage: 47.5 - 59.4");
		params_smg_tempest.addBalanceChange("Weight: 0.65 - 0.3");
		params_smg_tempest.addBalanceChange("Spare Ammo: 400 - 500");
		fileMap.put("smg_tempest", params_smg_tempest);
		
		HTMLParams params_sniperrifle_blackwidow= new HTMLParams();
		params_sniperrifle_blackwidow.setHumanName("Black Widow");
		params_sniperrifle_blackwidow.setDescription("These settings affect the Black Widow, a variant of the Widow that trades some of the damage for a 3 round magazine.");
		params_sniperrifle_blackwidow.setFileName("blackwidow.php");
		params_sniperrifle_blackwidow.setLoadName("Sniperrifleblackwidow");
		params_sniperrifle_blackwidow.addBalanceChange("Damage: 739.0 - 923.8");
		params_sniperrifle_blackwidow.addBalanceChange("Weight: 2.3 - 2.0");
		params_sniperrifle_blackwidow.setRoundsPerBurst(false);
		fileMap.put("sniperrifle_blackwidow", params_sniperrifle_blackwidow);

		HTMLParams params_sniperrifle_incisor= new HTMLParams();
		params_sniperrifle_incisor.setHumanName("M-29 Incisor");
		params_sniperrifle_incisor.setDescription("These settings affect the Incisor, a 3 shot sniper rifle that seems to have been downgraded since Mass Effect 2.");
		params_sniperrifle_incisor.setFileName("incisor.php");
		params_sniperrifle_incisor.setLoadName("Sniperrifleincisor");
		params_sniperrifle_incisor.addBalanceChange("Damage: 98.0 - 122.5");
		fileMap.put("sniperrifle_incisor", params_sniperrifle_incisor);

		HTMLParams params_sniperrifle_indra= new HTMLParams();
		params_sniperrifle_indra.setHumanName("M-90 Indra");
		params_sniperrifle_indra.setDescription("These settings affect the Indra, a fully automatic sniper rifle. It is essentially an assault rifle with a scope.");
		params_sniperrifle_indra.setFileName("indra.php");
		params_sniperrifle_indra.setLoadName("Sniperrifleindra");
		params_sniperrifle_indra.addBalanceChange("Damage: 72.6 - 92.2");
		params_sniperrifle_indra.addBalanceChange("Weight: 1.0 - 0.7");
		fileMap.put("sniperrifle_indra", params_sniperrifle_indra);

		HTMLParams params_sniperrifle_javelin= new HTMLParams();
		params_sniperrifle_javelin.setHumanName("Javelin");
		params_sniperrifle_javelin.setDescription("These settings affect the Javelin, a sniper rifle that can shoot through walls and has a very high powered scope.");
		params_sniperrifle_javelin.setFileName("javelin.php");
		params_sniperrifle_javelin.setLoadName("Sniperriflejavelin");
		params_sniperrifle_javelin.addBalanceChange("Damage: 1236.6 - 1545.8");
		params_sniperrifle_javelin.addBalanceChange("Weight: 2.7 - 2.4");
		params_sniperrifle_javelin.addBalanceChange("Spare Ammo: 5 - 15");
		fileMap.put("sniperrifle_javelin", params_sniperrifle_javelin);

		HTMLParams params_sniperrifle_mantis= new HTMLParams();
		params_sniperrifle_mantis.setHumanName("M-92 Mantis");
		params_sniperrifle_mantis.setDescription("These settings affect the Mantis, a decent single shot sniper rifle with good damage.");
		params_sniperrifle_mantis.setFileName("mantis.php");
		params_sniperrifle_mantis.setLoadName("Sniperriflemantis");
		params_sniperrifle_mantis.addBalanceChange("Damage: 738.7 - 886.4");
		fileMap.put("sniperrifle_mantis", params_sniperrifle_mantis);

		HTMLParams params_sniperrifle_raptor= new HTMLParams();
		params_sniperrifle_raptor.setHumanName("M-13 Raptor");
		params_sniperrifle_raptor.setDescription("These settings affect the Raptor, a fast firing semi-automatic sniper rifle that does modest damage.");
		params_sniperrifle_raptor.setFileName("raptor.php");
		params_sniperrifle_raptor.setLoadName("Sniperrifleraptor");
		params_sniperrifle_raptor.addBalanceChange("Damage: 86.0 - 107.5");
		params_sniperrifle_raptor.addBalanceChange("Weight: 1.0 - 0.7");
		fileMap.put("sniperrifle_raptor", params_sniperrifle_raptor);

		HTMLParams params_sniperrifle_valiant= new HTMLParams();
		params_sniperrifle_valiant.setHumanName("N7 Valiant");
		params_sniperrifle_valiant.setDescription("These settings affect the Valiant, a fast firing sniper rifle with a very quick reload speed, but relatively low damage.");
		params_sniperrifle_valiant.setFileName("valiant.php");
		params_sniperrifle_valiant.setLoadName("Sniperriflevaliant");
		params_sniperrifle_valiant.addBalanceChange("Damage: 396.2 - 515.5");
		params_sniperrifle_valiant.addBalanceChange("Spare Ammo: 30 - 40");
		fileMap.put("sniperrifle_valiant", params_sniperrifle_valiant);

		HTMLParams params_sniperrifle_viper= new HTMLParams();
		params_sniperrifle_viper.setHumanName("M-97 Viper");
		params_sniperrifle_viper.setDescription("These settings affect the Viper, a modest sniper rifle that does not do lots of damage, but has a larger than normal magazine.");
		params_sniperrifle_viper.setFileName("viper.php");
		params_sniperrifle_viper.setLoadName("Sniperrifleviper");
		params_sniperrifle_viper.addBalanceChange("Damage: 292.1 - 365.2");
		params_sniperrifle_viper.addBalanceChange("Weight: 1.25 - 0.7");
		fileMap.put("sniperrifle_viper", params_sniperrifle_viper);

		HTMLParams params_sniperrifle_widow= new HTMLParams();
		params_sniperrifle_widow.setHumanName("M-98 Widow");
		params_sniperrifle_widow.setDescription("These settings affect the Widow, a one shot sniper rifle that does a very high amount of damage.");
		params_sniperrifle_widow.setFileName("widow.php");
		params_sniperrifle_widow.setLoadName("Sniperriflewidow");
		params_sniperrifle_widow.addBalanceChange("Damage: 997.0 - 1246.3");
		fileMap.put("sniperrifle_widow", params_sniperrifle_widow);

		HTMLParams params_assaultrifle_krogan= new HTMLParams();
		params_assaultrifle_krogan.setHumanName("Striker Assault Rifle");
		params_assaultrifle_krogan.setDescription("These settings affect the Striker, an assault rifle with low damage and accuracy, but effectively staggers most units.");
		params_assaultrifle_krogan.setFileName("striker.php");
		params_assaultrifle_krogan.setLoadName("Assaultriflekrogan");
		params_assaultrifle_krogan.setBalanceChanges(null);
		fileMap.put("assaultrifle_krogan", params_assaultrifle_krogan);

		HTMLParams params_smg_geth= new HTMLParams();
		params_smg_geth.setHumanName("Geth Plasma SMG");
		params_smg_geth.setDescription("These settings affect the Geth Plasma SMG, a fast firing SMG that does almost no damage. It ramps up rate of fire after a short duration.");
		params_smg_geth.setFileName("gethplasmasmg.php");
		params_smg_geth.setLoadName("Smggeth"); 
		params_smg_geth.addBalanceChange("Damage: 16.1 - 20.1");
		params_smg_geth.addBalanceChange("Weight: 0.65 - 0.3");
		params_smg_geth.addBalanceChange("Spare Ammo: 600 - 800");
		fileMap.put("smg_geth", params_smg_geth);

		HTMLParams params_sniperrifle_batarian= new HTMLParams();
		params_sniperrifle_batarian.setHumanName("Kishock Harpoon Gun");
		params_sniperrifle_batarian.setDescription("These settings affect the Kishock Harpoon Gun, a weapon that does damage over time and has a 3x headshot multiplier. It can be charged for an extra 75% damage.");
		params_sniperrifle_batarian.setFileName("kishock.php");
		params_sniperrifle_batarian.setLoadName("Sniperriflebatarian");
		params_sniperrifle_batarian.addBalanceChange("Damage: 890.7 - 1113.4");
		params_sniperrifle_batarian.addBalanceChange("Charged Damage Multiplier: 1.75x");
		params_sniperrifle_batarian.setKishockRecoil(true);
		params_sniperrifle_batarian.setForceFireAfterCharge(true);
		params_sniperrifle_batarian.setRoundsPerBurst(false);
		fileMap.put("sniperrifle_batarian", params_sniperrifle_batarian);

		HTMLParams params_assaultrifle_cerberus= new HTMLParams();
		params_assaultrifle_cerberus.setHumanName("Harrier Assault Rifle");
		params_assaultrifle_cerberus.setDescription("These settings affect the Cerberus Harrier, arguably the best gun in the game with good damage, accuracy, rate of fire, and modest spare ammo.");
		params_assaultrifle_cerberus.setFileName("harrier.php");
		params_assaultrifle_cerberus.setLoadName("Assaultriflecerberus");
		params_assaultrifle_cerberus.addBalanceChange("Weight: 1.75 - 1.25");
		fileMap.put("assaultrifle_cerberus", params_assaultrifle_cerberus);

		HTMLParams params_assaultrifle_prothean_mp= new HTMLParams();
		params_assaultrifle_prothean_mp.setHumanName("Prothean Particle Rifle");
		params_assaultrifle_prothean_mp.setDescription("These settings affect the Prothean Particle Rifle, a beam weapon that does not use thermal clips and ramps up rate of fire after a short duration.");
		params_assaultrifle_prothean_mp.setFileName("particlerifle.php");
		params_assaultrifle_prothean_mp.setLoadName("Assaultrifleprotheanmp");
		params_assaultrifle_prothean_mp.addBalanceChange("Damage: 19.2 - 25.8");
		params_assaultrifle_prothean_mp.addBalanceChange("Magazine Size: 100 - 125 rounds");
		params_assaultrifle_prothean_mp.setInfiniteAmmo(true);
		params_assaultrifle_prothean_mp.setHeatUp(true);
		fileMap.put("assaultrifle_prothean_mp", params_assaultrifle_prothean_mp);

		HTMLParams params_shotgun_quarian= new HTMLParams();
		params_shotgun_quarian.setHumanName("Reegar Carbine");
		params_shotgun_quarian.setDescription("These settings affect the Reegar, a short range shotgun that has an incredible rate of fire for a very short duration.");
		params_shotgun_quarian.setFileName("reegar.php");
		params_shotgun_quarian.setLoadName("Shotgunquarian");
		params_shotgun_quarian.addBalanceChange("Weight: 1.75 - 1.25");
		params_shotgun_quarian.setForceFireAfterCharge(true);
		fileMap.put("shotgun_quarian", params_shotgun_quarian);

		HTMLParams params_sniperrifle_turian= new HTMLParams();
		params_sniperrifle_turian.setHumanName("Krysae Sniper Rifle");
		params_sniperrifle_turian.setDescription("These settings affect the Krysae, a once excellent weapon that is now the shame of the Turian empire.");
		params_sniperrifle_turian.setFileName("krysae.php");
		params_sniperrifle_turian.setLoadName("Sniperrifleturian");
		params_sniperrifle_turian.addBalanceChange("Damage: 493.4 - 616.8");
		params_sniperrifle_turian.addBalanceChange("Min Charge Time: 0 seconds");
		params_sniperrifle_turian.addBalanceChange("Max Charge Time: 0 seconds");
		params_sniperrifle_turian.addBalanceChange("Spare Ammo: 9 - 18");
		params_sniperrifle_turian.addBalanceChange("RPM: 35");
		params_sniperrifle_turian.addBalanceChange("Recoil: 10.0");
		params_sniperrifle_turian.addBalanceChange("Aiming Recoil: 5.0");
		fileMap.put("sniperrifle_turian", params_sniperrifle_turian);

		HTMLParams params_assaultrifle_lmg= new HTMLParams();
		params_assaultrifle_lmg.setHumanName("N7 Typhoon");
		params_assaultrifle_lmg.setDescription("These settings affect the Typhoon, a light machine gun that ramps up for extra damage and rate of fire, and has native armor penetration.");
		params_assaultrifle_lmg.setFileName("typhoon.php");
		params_assaultrifle_lmg.setLoadName("Assaultriflelmg");
		params_assaultrifle_lmg.addBalanceChange("Damage Multiplier: 1.5");
		params_assaultrifle_lmg.addBalanceChange("Distance Penetrated: 25");
		params_assaultrifle_lmg.addBalanceChange("Recoil: 0.21");
		params_assaultrifle_lmg.addBalanceChange("Aiming Recoil: 0.21");
		params_assaultrifle_lmg.setRampUp(true);
		params_assaultrifle_lmg.setTyphoon(true);
		params_assaultrifle_lmg.setRoundsPerBurst(false);
		fileMap.put("assaultrifle_lmg", params_assaultrifle_lmg);

		HTMLParams params_pistol_asari= new HTMLParams();
		params_pistol_asari.setHumanName("Acolyte");
		params_pistol_asari.setDescription("These settings affect the Acolyte, a charge weapon that does 500% damage to shields and barriers.");
		params_pistol_asari.setFileName("acolyte.php");
		params_pistol_asari.setLoadName("Pistolasari");
		params_pistol_asari.addBalanceChange("Damage: 420.2 - 490.0");
		params_pistol_asari.addBalanceChange("Min Charge Time: 1 second");
		params_pistol_asari.addBalanceChange("Max Charge Time: 1.1 seconds");
		params_pistol_asari.addBalanceChange("RPM: 80");
		fileMap.put("pistol_asari", params_pistol_asari);

		HTMLParams params_shotgun_assault= new HTMLParams();
		params_shotgun_assault.setHumanName("N7 Piranha");
		params_shotgun_assault.setDescription("These settings affect the Piranha, a fast firing shotgun that does lots of damage at short range.");
		params_shotgun_assault.setFileName("piranha.php");
		params_shotgun_assault.setLoadName("Shotgunassault");
		params_shotgun_assault.addBalanceChange("Damage: 61.7 - 77.1");
		params_shotgun_assault.addBalanceChange("Magazine Size: 6 rounds");
		fileMap.put("shotgun_assault", params_shotgun_assault);

		HTMLParams params_smg_collector= new HTMLParams();
		params_smg_collector.setHumanName("Collector SMG");
		params_smg_collector.setDescription("These settings affect the Collector SMG, an SMG that does not use thermal clips and recharges ammo very fast.");
		params_smg_collector.setFileName("collectorsmg.php");
		params_smg_collector.setLoadName("Smgcollector");
		params_smg_collector.addBalanceChange("Damage: 41.2 - 51.5");
		params_smg_collector.addBalanceChange("Magazine Size: 30 - 40 rounds");
		params_smg_collector.addBalanceChange("Weight: 0.85 - 0.45");
		params_smg_collector.addBalanceChange("Recharge rate per second: 20%");
		params_smg_collector.setInfiniteAmmo(true);
		fileMap.put("smg_collector", params_smg_collector);

		HTMLParams params_sniperrifle_collector= new HTMLParams();
		params_sniperrifle_collector.setHumanName("Collector Sniper Rifle");
		params_sniperrifle_collector.setDescription("These settings affect the Collector Sniper Rifle, a beam weapon with a very small ");
		params_sniperrifle_collector.setFileName("collectorsniperrifle.php");
		params_sniperrifle_collector.setLoadName("Sniperriflecollector");
		params_sniperrifle_collector.addBalanceChange("Damage: 73.0 - 91.3");
		params_sniperrifle_collector.addBalanceChange("Magazine Size: 35 - 45 rounds");
		params_sniperrifle_collector.setInfiniteAmmo(true);
		params_sniperrifle_collector.setHeatUp(true);
		fileMap.put("sniperrifle_collector", params_sniperrifle_collector);

		HTMLParams params_assaultrifle_spitfire= new HTMLParams();
		params_assaultrifle_spitfire.setHumanName("Geth Spitfire (MP)");
		params_assaultrifle_spitfire.setDescription("These settings affect the MP Geth Spitfire, a geth weapon that slows down most holders and has a huge magazine.");
		params_assaultrifle_spitfire.setFileName("spitfire.php");
		params_assaultrifle_spitfire.setLoadName("Assaultriflespitfire");
		params_assaultrifle_spitfire.addBalanceChange("Damage: 62.2 - 77.8");
		params_assaultrifle_spitfire.setRampUp(true);
		fileMap.put("assaultrifle_spitfire", params_assaultrifle_spitfire);

		HTMLParams params_pistol_bloodpack_mp= new HTMLParams();
		params_pistol_bloodpack_mp.setHumanName("Executioner Pistol");
		params_pistol_bloodpack_mp.setDescription("These settings affect the Executioner Pistol, a deadly one shot pistol.");
		params_pistol_bloodpack_mp.setFileName("executioner.php");
		params_pistol_bloodpack_mp.setLoadName("Pistolbloodpackmp");
		params_pistol_bloodpack_mp.setBalanceChanges(null);
		fileMap.put("pistol_bloodpack_mp", params_pistol_bloodpack_mp);

		HTMLParams params_pistol_silencer_mp= new HTMLParams();
		params_pistol_silencer_mp.setHumanName("M-11 Suppressor");
		params_pistol_silencer_mp.setDescription("These settings affect the Suppressor, a weak gun with a high headshot damage bonus.");
		params_pistol_silencer_mp.setFileName("suppressor.php");
		params_pistol_silencer_mp.setLoadName("Pistolsilencermp");
		params_pistol_silencer_mp.setBalanceChanges(null);
		params_pistol_silencer_mp.setHeadshotMultiplier(true);
		params_pistol_silencer_mp.setSilencer(true);
		fileMap.put("pistol_silencer_mp", params_pistol_silencer_mp);

		HTMLParams params_shotgun_salarian_mp= new HTMLParams();
		params_shotgun_salarian_mp.setHumanName("Venom Shotgun");
		params_shotgun_salarian_mp.setDescription("These settings affect the Venom Shotgun, a shotgun that staggers most enemies and splits into multiple grenades when charged.");
		params_shotgun_salarian_mp.setFileName("venom.php");
		params_shotgun_salarian_mp.setLoadName("Shotgunsalarianmp");
		params_shotgun_salarian_mp.addBalanceChange("Charged Damage Multiplier: 0.4167x");
		params_shotgun_salarian_mp.setVenom(true);
		fileMap.put("shotgun_salarian_mp", params_shotgun_salarian_mp);

		HTMLParams params_smg_bloodpack_mp= new HTMLParams();
		params_smg_bloodpack_mp.setHumanName("Blood Pack Punisher");
		params_smg_bloodpack_mp.setDescription("These settings affect the Blood Pack Punisher, an SMG that fires an armor piercing round every 3 shots.");
		params_smg_bloodpack_mp.setFileName("bloodpackpunisher.php");
		params_smg_bloodpack_mp.setLoadName("Smgbloodpackmp");
		params_smg_bloodpack_mp.addBalanceChange("Damage: 40.1 - 50.1");
		params_smg_bloodpack_mp.setPunisher(true);
		fileMap.put("smg_bloodpack_mp", params_smg_bloodpack_mp);
		
		params_sniperrifle_incisor.setZoomAimError(false);
		params_sniperrifle_javelin.setZoomAimError(false);
		params_sniperrifle_mantis.setZoomAimError(false);
		params_sniperrifle_valiant.setZoomAimError(false);
		params_sniperrifle_viper.setZoomAimError(false);
		params_assaultrifle_argus.setMinRefireTime(true);
		params_assaultrifle_avenger.setMinRefireTime(true);
		params_assaultrifle_reckoning.setMinRefireTime(true);
		params_assaultrifle_saber.setMinRefireTime(true);
		params_assaultrifle_valkyrie.setMinRefireTime(true);
		params_assaultrifle_vindicator.setMinRefireTime(true);
		params_pistol_eagle.setMinRefireTime(true);
		params_shotgun_geth.setMinRefireTime(true);
		params_shotgun_raider.setMinRefireTime(true);
		params_shotgun_scimitar.setMinRefireTime(true);
		params_smg_hornet.setMinRefireTime(true);
		params_smg_hurricane.setMinRefireTime(true);
		params_smg_shuriken.setMinRefireTime(true);
		params_smg_tempest.setMinRefireTime(true);
		params_sniperrifle_mantis.setMinRefireTime(true);
		params_sniperrifle_raptor.setMinRefireTime(true);
		params_sniperrifle_viper.setMinRefireTime(true);
		params_assaultrifle_krogan.setMinRefireTime(true);
		params_smg_collector.setMinRefireTime(true);
		params_assaultrifle_adas_mp.setMinRefireTime(true);
		params_assaultrifle_lancer_mp.setMinRefireTime(true);
		params_smg_bloodpack_mp.setMinRefireTime(true);
		
		params_heavy_flamethrower_npc.setTraceRange(true);
		params_heavy_flamethrower_player.setTraceRange(true);
		params_heavy_minigun.setTraceRange(true);
		params_heavy_mountedgun.setTraceRange(true);
		params_assaultrifle_prothean_mp.setTraceRange(true);
		params_shotgun_quarian.setTraceRange(true);
		params_assaultrifle_lmg.setTraceRange(true);
		params_sniperrifle_collector.setTraceRange(true);
		params_assaultrifle_spitfire.setTraceRange(true);
		
		params_heavy_minigun.setPenetration(true);
		params_heavy_mountedgun.setPenetration(true);
		params_shotgun_crusader.setPenetration(true);
		params_sniperrifle_blackwidow.setPenetration(true);
		params_sniperrifle_javelin.setPenetration(true);
		params_sniperrifle_widow.setPenetration(true);
		params_assaultrifle_krogan.setPenetration(true);
		params_sniperrifle_batarian.setPenetration(true);
		params_sniperrifle_turian.setPenetration(true);
		params_assaultrifle_lmg.setPenetration(true);
		params_assaultrifle_adas_mp.setPenetration(true);
		params_assaultrifle_spitfire.setPenetration(true);
		params_pistol_bloodpack_mp.setPenetration(true);
		
		params_assaultrifle_cobra.setRoundsPerBurst(false);
		params_assaultrifle_collector.setRoundsPerBurst(false);
		params_assaultrifle_geth.setRoundsPerBurst(false);
		params_assaultrifle_revenant.setRoundsPerBurst(false);
		params_heavy_cain.setRoundsPerBurst(false);
		params_heavy_minigun.setRoundsPerBurst(false);
		params_heavy_mountedgun.setRoundsPerBurst(false);
		params_pistol_scorpion.setRoundsPerBurst(false);
		params_shotgun_graal.setRoundsPerBurst(false);
		params_sniperrifle_blackwidow.setRoundsPerBurst(false);
		params_sniperrifle_widow.setRoundsPerBurst(false);
		params_sniperrifle_batarian.setRoundsPerBurst(false);
		params_assaultrifle_cerberus.setRoundsPerBurst(false);
		params_assaultrifle_lmg.setRoundsPerBurst(false);
		params_assaultrifle_spitfire.setRoundsPerBurst(false);
		params_shotgun_salarian_mp.setRoundsPerBurst(false);
		params_assaultrifle_reckoning.setMaxCharge(true);
		params_heavy_blackstar.setMaxCharge(true);
		params_heavy_blackstar.setMinCharge(true);
		params_heavy_blackstar.setForceFireAfterCharge(true);
		params_heavy_cain.setMaxCharge(true);
		params_heavy_cain.setMinCharge(true);
		params_heavy_cain.setForceFireAfterCharge(true);
		params_heavy_flamethrower_player.setMaxCharge(true);
		params_heavy_flamethrower_player.setMinCharge(true);
		params_heavy_flamethrower_player.setForceFireAfterCharge(true);
		params_heavy_titanmissilelauncher.setMaxCharge(true);
		params_heavy_titanmissilelauncher.setMinCharge(true);
		params_heavy_titanmissilelauncher.setForceFireAfterCharge(true);
		params_pistol_thor.setMaxCharge(true);
		params_pistol_thor.setMinCharge(true);
		params_shotgun_geth.setMaxCharge(true);
		params_shotgun_graal.setMaxCharge(true);
		params_shotgun_graal.setMinCharge(true);
		params_sniperrifle_javelin.setMaxCharge(true);
		params_sniperrifle_javelin.setMinCharge(true);
		params_sniperrifle_batarian.setMaxCharge(true);
		params_sniperrifle_batarian.setMinCharge(true);
		params_sniperrifle_batarian.setForceFireAfterCharge(true);
		params_shotgun_quarian.setMaxCharge(true);
		params_shotgun_quarian.setMinCharge(true);
		params_shotgun_quarian.setForceFireAfterCharge(true);
		params_sniperrifle_turian.setMaxCharge(true);
		params_sniperrifle_turian.setMinCharge(true);
		params_pistol_asari.setMaxCharge(true);
		params_pistol_asari.setMinCharge(true);
		params_shotgun_salarian_mp.setMaxCharge(true);
		params_shotgun_salarian_mp.setMinCharge(true);
		
		params_sniperrifle_incisor.setZoomPenalty(false);
		params_sniperrifle_javelin.setZoomPenalty(false);
		params_sniperrifle_mantis.setZoomPenalty(false);
		params_sniperrifle_valiant.setZoomPenalty(false);
		params_sniperrifle_viper.setZoomPenalty(false);
		
		params_heavy_blackstar.setReloadSpeed(false);
		params_heavy_cain.setReloadSpeed(false);
		params_heavy_flamethrower_npc.setReloadSpeed(false);
		params_heavy_flamethrower_player.setReloadSpeed(false);
		params_heavy_minigun.setReloadSpeed(false);
		params_heavy_mountedgun.setReloadSpeed(false);
		params_heavy_titanmissilelauncher.setReloadSpeed(false);
		params_heavy_blackstar.setSpareAmmo(false);
		params_heavy_cain.setSpareAmmo(false);
		params_heavy_flamethrower_npc.setSpareAmmo(false);
		params_heavy_flamethrower_player.setSpareAmmo(false);
		params_heavy_titanmissilelauncher.setSpareAmmo(false);

		
		generatePages();
	}
	
	private static void printKeys() {
		try {		
			String input_text = getInput();
			StringBuilder sb = new StringBuilder();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					//We are now at at a section element. this is a table
					String tableName = getTableName(sectionElement.getAttribute("name"));
					sb.append("HTMLParams params");
					sb.append(tableName);
					sb.append("= new HTMLParams();\n");
					
					//name
					sb.append("params");
					sb.append(tableName);
					sb.append(".setHumanName(\"NAME\");\n");
					
					//description
					sb.append("params");
					sb.append(tableName);
					sb.append(".setDescription(\"These settings affect the \");\n");
					
					//filename
					sb.append("params");
					sb.append(tableName);
					sb.append(".setFileName(\"FILE.php\");\n");
					
					//load
					sb.append("params");
					sb.append(tableName);
					sb.append(".setLoadName(\"");
					sb.append(getLoadName(tableName));
					sb.append("\");\n");
					//balance
					sb.append("params");
					sb.append(tableName);
					sb.append(".setBalanceChanges(null);\n");
					
					//put
					sb.append("fileMap.put(\"");
					sb.append(tableName);
					sb.append("\", params");
					sb.append(tableName);
					sb.append(");\n\n");
				}
			}
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void generateData() {
		try {		
			String input_text = getInput();
			StringBuilder sb = new StringBuilder();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(input_text));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList section = doc.getElementsByTagName("Section");
			for (int i = 0; i < section.getLength(); i++) {
				Element sectionElement = (Element) section.item(i);
				if (sectionElement.getNodeType() == Node.ELEMENT_NODE) {
					//We are now at at a section element. this is a table
					String tableName = getTableName(sectionElement.getAttribute("name"));
					NodeList propertyList = sectionElement.getChildNodes();
					boolean hasZoomPenalty = false;
					for (int k = 0; k < propertyList.getLength(); k++){
						Node scannednode = propertyList.item(k);
						if (scannednode.getNodeType() == Node.ELEMENT_NODE) {
							Element prop = (Element) scannednode;
							String name = prop.getAttribute("name");
							switch (name) {
								case "maxspareammo":
									hasZoomPenalty = true;
									break;
							}
						}
					}
					
					if (!hasZoomPenalty) {
						sb.append("params_");
						sb.append(tableName);
						sb.append(".setSpareAmmo(false);\n");						
					}

					/*
					//description
					sb.append("params");
					sb.append(tableName);
					sb.append(".setDescription(\"These settings affect the \");\n");
					
					//filename
					sb.append("params");
					sb.append(tableName);
					sb.append(".setFileName(\"FILE.php\");\n");
					
					//load
					sb.append("params");
					sb.append(tableName);
					sb.append(".setLoadName(\"");
					sb.append(getLoadName(tableName));
					sb.append("\");\n");
					//balance
					sb.append("params");
					sb.append(tableName);
					sb.append(".setBalanceChanges(null);\n");
					
					//put
					sb.append("fileMap.put(\"");
					sb.append(tableName);
					sb.append("\", params");
					sb.append(tableName);
					sb.append(");\n\n");*/
				}
			}
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
					
	private static String getInput() throws IOException{
		String wrappedXML = FileUtils.readFileToString(new File(bwFile));
		wrappedXML = "<bioweapon>"+wrappedXML+"</bioweapon>";
		return wrappedXML;
	}
	
	private static String getTableName(String sectionName) {
		if (sectionName.equals("sfxgame.sfxweapon")) {
			return "_sfxweapon";
		}
		sectionName = sectionName.substring(sectionName.indexOf("."));
		sectionName = sectionName.substring(sectionName.indexOf("_")+1);
		System.out.println(sectionName);
		return sectionName;
	}
	
	private static String getLoadName(String tableName) {
		String weaponName = getWeaponName(tableName);
		//public function loadWeaponX() {
		return Character.toUpperCase(weaponName.charAt(0)) + weaponName.toLowerCase().substring(1); //have only first letter capitalized.
	}
	
	private static String getWeaponName(String tableName) {
		return tableName.replaceAll("_", "");
	}
	
	private static void generatePages() throws IOException{
		for (Map.Entry<String, HTMLParams> entry : fileMap.entrySet()) {
		    String key = entry.getKey();
		    HTMLParams value = entry.getValue();
		    String page = FileUtils.readFileToString(new File(templateFile));
		    
		    page = page.replaceAll(descriptionPlaceholder, value.getDescription());
		    page = page.replaceAll(humanPlaceholder, value.getHumanName());
		    page = page.replaceAll(loadPlaceholder, value.getLoadName());
		    
		    boolean hasRecoil = true;
		    boolean hasZoomRecoil = true;
		    boolean hasRechargeRate = true;
		    boolean hasRPM = true;
		    boolean noPenetration = false;
		    //damage
		    boolean hasDamage = true;
		    boolean hasMinCharge = true;
		    boolean hasMaxCharge = true;
		    
		    if (value.getBalanceChanges() != null) {
		    	String balanceBlock = balanceChangesBlock.replaceAll("REPLACE_BLOCK", generateBalanceList(value));
		    	page = page.replaceAll(balanceChangesPlaceholder, balanceBlock);
		    	for (String str : value.getBalanceChanges()) {
			    	if (str.contains("Spare Ammo")) {
			    		page = page.replaceAll(spareAmmoPlaceholder, "");
			    	}
			    	if (str.contains("Weight")) {
			    		page = page.replaceAll(weightPlaceholder, "");
			    	}
			    	if (str.contains("Damage")) {
			    		hasDamage = false;
			    	}
			    	if (str.contains("RPM")) {
			    		hasRPM = false;
			    	}
			    	if (str.contains("Magazine Size")) {
			    		page = page.replaceAll(magsizePlaceholder, "");
			    	}
			    	if (str.startsWith("Recoil")) {
			    		hasRecoil = false;
			    	}
			    	if (str.startsWith("Aiming Recoil")) {
			    		hasZoomRecoil = false;
			    	}
			    	if (str.startsWith("Recharge rate per second")){
			    		hasRechargeRate = false;
			    	}
			    	if (str.contains("Distance Penetrated")) {
			    		noPenetration = true;
			    	}
			    	if (str.contains("Min Charge Time")) {
			    		hasMinCharge = false;
			    	}
			    	if (str.contains("Max Charge Time")) {
			    		hasMaxCharge = false;
			    	}
			    	
			    }
		    }
		    //ADD CONTAINERS IF NECESSARY
		    if (!hasRecoil && !hasZoomRecoil) {
			    page = page.replaceAll(recoilContainerPlaceholder, "");
	    	} else {
			    page = page.replaceAll(recoilContainerPlaceholder, recoilContainer);
	    	}
		    
		    if (hasDamage || value.hasDamageOverTime() || value.hasHeadshotMultiplier()) {
			    page = page.replaceAll(damageContainerPlaceholder, damageContainer);
		    } else {
			    page = page.replaceAll(damageContainerPlaceholder, "");
		    }
		    
		    if (hasRPM || value.hasMinRefireTime() || value.hasRampUp() || value.hasTraceRange()) {
		    	page = page.replaceAll(rpmContainerPlaceholder, rpmContainer);
		    } else {
			    page = page.replaceAll(rpmContainerPlaceholder, "");
		    }
	    	
		    if ((hasMinCharge && value.hasMinCharge()) || (hasMaxCharge && value.hasMaxCharge())  || value.hasForceFireAfterCharge()) {
		    	page = page.replaceAll(chargingPlaceholder, chargingContainer);
		    } else {
			    page = page.replaceAll(chargingPlaceholder, "");
		    }
		    
	    	//PERFORM REPLACEMENTS
		    page = page.replaceAll(balanceChangesPlaceholder, "");
	    	page = page.replaceAll(weightPlaceholder, weightBlock);
	    	
	    	//page = page.replaceAll(accuracyPenaltyPlaceholder, accuracyPenaltyContainer);
	    	
	    	//if (value.hasAimPenalty() || value.hasAimPenalty()) {
		    	page = page.replaceAll(accuracyPenaltyPlaceholder, accuracyPenaltyContainer);
	    	//}
	    	
	    	if (value.hasAimError() || value.hasZoomAimError()) {
		    	page = page.replaceAll(accuracyErrorPlaceholder, accuracyErrorContainer);
	    	}
	    	
	    	/*if (value.hasAim() || value.hasZoomAimError()) {
		    	page = page.replaceAll(accuracyPenaltyPlaceholder, accuracyErrorContainer);
	    	}*/
	    	page = page.replaceAll(hipPenaltyPlaceholder, hipPenaltyBlock);
	    	
	    	if (hasRPM) {
		    	page = page.replaceAll(rpmPlaceholder, rpmBlock);
	    	} else {
		    	page = page.replaceAll(rpmPlaceholder, "");
	    	}
	    	
	    	if (value.hasPenetration() && !noPenetration){
	    		page = page.replaceAll(penetrationPlaceholder, penetrationBlock);
	    	} else {
		    	page = page.replaceAll(penetrationPlaceholder, "");
	    	}
	    	
	    	if (value.hasMinRefireTime()) {
		    	page = page.replaceAll(minrefirePlaceholder, minrefireBlock);
	    	} else {
		    	page = page.replaceAll(minrefirePlaceholder, "");
	    	}
	    	
	    	if (value.hasTraceRange()) {
		    	page = page.replaceAll(tracerangePlaceholder, tracerangeBlock);
	    	} else {
		    	page = page.replaceAll(tracerangePlaceholder, "");
	    	}
	    	
	    	if (value.hasAimError()) {
		    	page = page.replaceAll(aimerrorPlaceholder, aimerrorBlock);
	    	} else {
		    	page = page.replaceAll(aimerrorPlaceholder, "");
	    	}
	    	
	    	if (value.hasZoomAimError()) {
		    	page = page.replaceAll(zoomaimerrorPlaceholder, zoomaimerrorBlock);
	    	} else {
		    	page = page.replaceAll(zoomaimerrorPlaceholder, "");
	    	}
	    	
	    	if (value.hasAmmoPerShot()) {
		    	page = page.replaceAll(ammopershotPlaceholder, ammopershotBlock);
	    	} else {
		    	page = page.replaceAll(ammopershotPlaceholder, "");
	    	}
	    	
	    	if (hasDamage) {
		    	page = page.replaceAll(damagePlaceholder, damageBlock);
	    	} else {
		    	page = page.replaceAll(damagePlaceholder, "");
	    	}
	    	
	    	if (value.isChakramLauncher()) {
		    	page = page.replaceAll(chakramPlaceholder, reckoningBlock);
	    	} else {
		    	page = page.replaceAll(chakramPlaceholder, "");
	    	}
	    	
	    	if (value.isArcPistol()) {
	    		page = page.replaceAll(arcpistolPlaceholder, arcpistolBlock);
	    	} else {
		    	page = page.replaceAll(arcpistolPlaceholder, "");
	    	}
	    	
	    	if (value.isSilencer()) {
	    		page = page.replaceAll(silencerPlaceholder, silencerBlock);
	    	} else {
		    	page = page.replaceAll(silencerPlaceholder, "");
	    	}
	    	
	    	if (value.isTyphoon()) {
		    	page = page.replaceAll(typhoonPlaceholder, typhoonBlock);
	    	} else {
		    	page = page.replaceAll(typhoonPlaceholder, "");
	    	}
	    	
	    	if (value.isPunisher()) {
		    	page = page.replaceAll(punisherPlaceholder, punisherBlock);
	    	} else {
		    	page = page.replaceAll(punisherPlaceholder, "");
	    	}
	    	
	    	if (value.isVenom()) {
	    		page = page.replaceAll(venomPlaceholder, venomBlock);
	    	} else {
		    	page = page.replaceAll(venomPlaceholder, "");
	    	}
	    	
	    	if (value.isGethShotgun()) {
		    	page = page.replaceAll(gethshotgunPlaceholder, gethplasmashotgunBlock);
	    	} else {
		    	page = page.replaceAll(gethshotgunPlaceholder, "");
	    	}
	    	
	    	if (value.hasZoomPenalty()) {
	    		page = page.replaceAll(zoomPenaltyPlaceholder, zoomPenaltyBlock);
	    	} else {
		    	page = page.replaceAll(zoomPenaltyPlaceholder, "");
	    	}
	    	
	    	if (value.hasRoundsPerBurst()) {
	    		page = page.replaceAll(roundsPerBurstPlaceholder, roundsPerBurstBlock);
	    	} else {
		    	page = page.replaceAll(roundsPerBurstPlaceholder, "");
	    	}
	    	
	    	if (value.hasDamageOverTime()) {
	    		System.out.println("HAS DOTBLOCK");
	    		page = page.replaceAll(dotPlaceholder, dotBlock);
	    	} else {
		    	page = page.replaceAll(dotPlaceholder, "");
	    	}
	    	
	    	if (value.hasRampUp()) {
	    		page = page.replaceAll(rpmIncreasePlaceholder, rampBlock);
	    	} else if (value.hasHeatUp()) {
	    		page = page.replaceAll(rpmIncreasePlaceholder, heatUpBlock);
	    	} else {
	    		page = page.replaceAll(rpmIncreasePlaceholder, "");
	    	}
	    	
	    	page = page.replaceAll(magsizePlaceholder, magsizeBlock);

	    	if (value.hasInfiniteAmmo()) {
		    	page = page.replaceAll(spareAmmoPlaceholder, rechargeBlock);
		    	if (hasRechargeRate) {
			    	page = page.replaceAll(rechargeRatePlaceholder, rechargeRateBlock);
		    	} else {
			    	page = page.replaceAll(rechargeRatePlaceholder, "");
		    	}
	    	} else {
	    		if (value.hasSpareAmmo()) {
	    			page = page.replaceAll(spareAmmoPlaceholder, spareAmmoBlock);
	    		} else {
	    			page = page.replaceAll(spareAmmoPlaceholder, "");
	    		}
	    	}
	    	
	    	//charging
	    	if (hasMinCharge && value.hasMinCharge()) {
		    	page = page.replaceAll(minchargePlaceholder, minchargeBlock);
	    	} else {
		    	page = page.replaceAll(minchargePlaceholder, "");
	    	}
	    	
	    	if (hasMaxCharge && value.hasMaxCharge()) {
		    	page = page.replaceAll(maxchargePlaceholder, maxchargeBlock);
	    	} else {
		    	page = page.replaceAll(maxchargePlaceholder, "");
	    	}
	    	
	    	if (value.hasForceFireAfterCharge()) {
		    	page = page.replaceAll(forcefirePlaceholder, forceFireBlock);
	    	} else {
		    	page = page.replaceAll(forcefirePlaceholder, "");
	    	}
	    	
	    	if (value.hasReloadSpeed()) {
		    	page = page.replaceAll(reloadspeedPlaceholder, reloadspeedBlock);
	    	} else {
		    	page = page.replaceAll(reloadspeedPlaceholder, "");
	    	}
	    	
	    	if (hasRecoil) {
	    		page = page.replaceAll(recoilStatPlaceholder, recoilStatBlock);
	    	} else {
	    		page = page.replaceAll(recoilStatPlaceholder, "");
	    	}
	    	
	    	if (value.hasHeadshotMultiplier()) {
	    		page = page.replaceAll(headshotPlaceholder, headshotBlock);
	    	} else {
	    		page = page.replaceAll(headshotPlaceholder, "");
	    	}
	    	
	    	if (hasZoomRecoil) {
	    		page = page.replaceAll(zoomrecoilStatPlaceholder, zoomrecoilStatBlock);
	    		if (value.hasKishockRecoil()) {
		    		page = page.replaceAll(kishockrecoilStatPlaceholder, kishockrecoilStatBlock);
	    		} else {
		    		page = page.replaceAll(kishockrecoilStatPlaceholder, "");
	    		}
	    	} else {
	    		page = page.replaceAll(zoomrecoilStatPlaceholder, "");
	    	}
		    
		    page = page.replaceAll(weaponNamePlaceholder, key);

		    
		    String folder = "";
		    if (key.contains("sniperrifle")) {
		    	folder = "sniper/";
		    }
		    if (key.contains("shotgun")) {
		    	folder = "shotgun/";
		    }
		    if (key.contains("assaultrifle")) {
		    	folder = "assault/";
		    }
		    if (key.contains("smg")) {
		    	folder = "smg/";
		    }
		    if (key.contains("pistol")) {
		    	folder = "pistol/";
		    }
		    if (key.contains("heavy")) {
		    	folder = "heavy/";
		    }
		    
		    FileUtils.writeStringToFile(new File(outputFolder+folder+value.getFileName()), page);
		    // ...
		}
	}
	
	private static String generateBalanceList(HTMLParams params) {
		StringBuilder sb = new StringBuilder();
		for (String str : params.getBalanceChanges()){
			sb.append("        	         <li>");
			sb.append(str);
			sb.append("</li>\n");
		}
		return sb.toString();
	}
}
