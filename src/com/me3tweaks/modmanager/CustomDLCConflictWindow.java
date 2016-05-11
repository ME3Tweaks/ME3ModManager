package com.me3tweaks.modmanager;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JTextPane;

import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Shows conflicts between custom dlc mods.
 * 
 * @author Michael
 *
 */
public class CustomDLCConflictWindow extends JDialog {

	public CustomDLCConflictWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		System.out.println("Setting visible");
		setVisible(true);
	}

	private void setupWindow() {
		setPreferredSize(new Dimension(500,500));
		setTitle("Custom DLC Conflicts");
		
		// TODO Auto-generated method stub
		JTextPane tp = new JTextPane();
		HashMap<String, String> items = ModManager.getCustomDLCConflicts(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		for (Map.Entry<String, String> entry : items.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			
			value = (new File(value).getName());
			
			ResourceUtils.appendToPane(tp, key + " => " + value, Color.BLUE);
			// ...
		}
		add(tp);
		pack();
	}
}
