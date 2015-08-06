package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.me3tweaks.modmanager.cellrenderers.HintTextFieldUI;

public class ContainerRow extends JPanel {
	public static String CONTAINER_TEMPLATE = "\t\t\t\t<!-- CONTAINERNAME -->\n" + "\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"
			+ "\t\t\t\t\t<img class=\"guide purple_card\" src=\"/images/common/no_image.png\">\n"
			+ "\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">CONTAINERNAME</h2>\n"
			+ "\t\t\t\t\t<p>These properties need to be moved to their proper boxes.</p>\n" + "INPUTS_PLACEHOLDER" + "\t\t\t\t</div>\n";
	public static String DETONATION_CONTAINER_TEMPLATE = "\t\t\t\t<!-- DETONATIONVARNAME PARAMETERS  -->\n"
			+ "\t\t\t\t<div class=\"modmaker_attribute_wrapper\">\n"
			+ "\t\t\t\t\t<img class=\"guide hard\" src=\"/images/modmaker/powers/TABLENAME/explosion.jpg\">\n"
			+ "\t\t\t\t\t<h2 class=\"modmaker_attribute_title\">Detonation Parameters</h2>\n"
			+ "\t\t\t\t\t<p>Detonation paramaters determine what gets hit when TABLENAME detonate.</p>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Blocked By Objects</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: BLOCKED_BY_OBJECTS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_blockedbyobjects\" type=\"checkbox\" name=\"DETONATIONVARNAME_blockedbyobjects\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_blockedbyobjects) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Distance Sorted</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: DISTANCE_SORTED</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_distancesorted\" type=\"checkbox\" name=\"DETONATIONVARNAME_distancesorted\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_distancesorted) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Dead Characters</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_DEAD_CHARS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactdeadpawns\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactdeadpawns\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactdeadpawns) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Friendlies</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_FRIENDS</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactfriends\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactfriends\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactfriends) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t<div class=\"modmaker_entry\">\n"
			+ "\t\t\t\t\t\t<div class=\"defaultbox\">\n"
			+ "\t\t\t\t\t\t\t<span class=\"inputtag defaultboxitem\">Impacts Placeables</span>\n"
			+ "\t\t\t\t\t\t\t<span class=\"modmaker_default defaultboxitem\">Default: IMPACTS_PLACEABLES</span>\n"
			+ "\t\t\t\t\t\t</div>\n"
			+ "\t\t\t\t\t\t<input id=\"DETONATIONVARNAME_impactplaceables\" type=\"checkbox\" name=\"DETONATIONVARNAME_impactplaceables\" <?=($mod->powers->mod_powers_TABLENAME_DETONATIONVARNAME_impactplaceables) ? \"checked\" : \"\"?>>\n"
			+ "\t\t\t\t\t</div>\n" + "CONEANGLE" + "\t\t\t\t</div>";
	
	private JTextField containerTitle, containerText;
	private JButton deleteContainerButton, updateContainer;
	/**
	 * Loads the variable and instantiates the interface
	 */
	public void configure() {
		setBorder(new EtchedBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		deleteContainerButton = new JButton("Delete Container");
		add(deleteContainerButton);
		
		containerTitle = new JTextField(20);
		containerTitle.setUI(new HintTextFieldUI("Container Title"));
		add(containerTitle);
		
		containerText = new JTextField(50);
		containerText.setUI(new HintTextFieldUI("Container Text"));
		add(containerText);
		
		updateContainer = new JButton("Update Container");
		add(updateContainer);
	}
	
	public String toString(){
		return containerTitle.getText();
	}

	public JButton getDeleteContainerButton() {
		return deleteContainerButton;
	}

}
