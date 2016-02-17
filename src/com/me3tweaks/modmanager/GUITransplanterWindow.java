package com.me3tweaks.modmanager;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.me3tweaks.modmanager.objects.Mod;

/** Automates the GUI-Transplanter tool **/
public class GUITransplanterWindow extends JFrame {

	public static void main(String[] args) {
		ModManager.loadLogger();
		ModManager.debugLogger.initialize();
		ModManager.logging = true;
		
		String exe = ModManager.getGUITransplanterCLI();
		String swfLibrary = "E:\\Google Drive\\SP Controller Support\\XBOX_SWF_FILES";
		//File targetFolder = new File("C:\\Users\\Michael\\workspace\\modmanager3\\mods\\EGMControllerCompat\\DLC_CON_MAPMOD_Xbox\\CookedPCConsole");
		//File targetFolder = new File("C:\\Users\\Michael\\workspace\\modmanager3\\mods\\Expanded Galaxy Mod\\DLC_CON_MAPMOD\\CookedPCConsole");
		File targetFolder = new File("E:\\scratch");
		File[] files = targetFolder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".pcc");
		    }
		});
		
		for (File file : files) {
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(exe);
			commandBuilder.add("--inputfolder");
			commandBuilder.add(swfLibrary);
			commandBuilder.add("--injectswf");
			commandBuilder.add("--targetfile");
			commandBuilder.add(file.getAbsolutePath());
			
			//System.out.println("Building command");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			//Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command) {
				sb.append(arg + " ");
			}
			ModManager.debugLogger.writeMessage(sb.toString());

			ProcessBuilder injectSWFProcess = new ProcessBuilder(command);
			injectSWFProcess.redirectErrorStream(true);
			injectSWFProcess.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			ModManager.runProcess(injectSWFProcess);
		}
	}
	
	DefaultComboBoxModel<Mod> sourceModel;
	DefaultComboBoxModel<Mod> targetModel;
	

	public GUITransplanterWindow(JFrame callingWindow) {
		setupWindow(callingWindow);
		setVisible(true);
	}

	private void setupWindow(JFrame callingWindow) {
		setTitle("Mod Manager GUI Transplanter");
		setMinimumSize(new Dimension(450, 200));
		setIconImages(ModManager.ICONS);
		
		JComboBox<Mod> sourceMods = new JComboBox<Mod>();
		JComboBox<Mod> targetMods = new JComboBox<Mod>();
		
		
		

		
		sourceModel = new DefaultComboBoxModel<Mod>();
		targetModel = new DefaultComboBoxModel<Mod>();

		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.getSize(); i++) {
			sourceModel.addElement(ModManagerWindow.ACTIVE_WINDOW.modModel.getElementAt(i));
			targetModel.addElement(ModManagerWindow.ACTIVE_WINDOW.modModel.getElementAt(i));
		}
		sourceMods.setModel(sourceModel);
		targetMods.setModel(targetModel);
		
		JLabel introLabel = new JLabel("Select a GUI source");
		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel rootPanel = new JPanel(gbl);
		int sourceRow = 0;
		
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		rootPanel.add(introLabel,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 1;
		rootPanel.add(sourceMods,c);
		
		c.gridx = 1;
		rootPanel.add(targetMods,c);
		
		
		
		add(rootPanel);
		pack();
		setLocationRelativeTo(callingWindow);
	}
	
	private void performTransplant(){
		
	}
}
