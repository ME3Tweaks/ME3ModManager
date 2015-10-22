package com.me3tweaks.modmanager;

import java.util.ArrayList;

import javax.swing.JMenu;

import com.me3tweaks.modmanager.objects.HelpMenuItem;

public class HelpMenu {

	public static JMenu getHowDoIMenu() {
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

		menuItems
				.add(new HelpMenuItem(
						"How do I mix mods",
						"Mod Mixing",
						"Mod Mixing is only supported if you do it through Mod Manager. You can merge mods through the tools menu utility. If you are layering mods (installing a mod over an already modified game) this is not supported, but you can improve compatability by going to Actions > Options and turning on the use game TOC files option."));

		menuItems
				.add(new HelpMenuItem(
						"My DLC is corrupt/won't verify",
						"DLC Verification Issues",
						"If the title screen shows that your DLC is not verifying you must launch the game via Mod Manager (you may need to run as admin), or install the Binkw32 bypass (via the tools menu). The game by default does not accept modified DLC and will not load anything in the DLC"));

		for (HelpMenuItem item : menuItems) {
			menu.add(item.createMenuItem());
		}
		return menu;

	}
}
