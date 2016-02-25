package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.me3tweaks.modmanager.objects.Mod;

/** Automates the GUI-Transplanter tool **/
public class GUITransplanterWindow extends JFrame {
	private static final int TRANSPLANT_TYPE_DIRECT = 0;
	private static final int TRANSPLANT_TYPE_CLONE = 1;
	private static final int TRANSPLANT_TYPE_COMPATIBILITY = 2;
	ButtonGroup modTypeButtonGroup = new ButtonGroup();
	JRadioButton directTransplantMod, dlcModCloneMod, cloneMod;

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
	private JLabel opDescriptionLabel;
	private JComboBox<Mod> sourceMods;
	private JComboBox<Mod> targetMods;
	private JProgressBar progressBar;
	private JButton transplantButton;

	public GUITransplanterWindow(JFrame callingWindow) {
		setupWindow(callingWindow);
		setVisible(true);
	}

	private void setupWindow(JFrame callingWindow) {
		setTitle("Mod Manager GUI Transplanter");
		setMinimumSize(new Dimension(450, 200));
		setIconImages(ModManager.ICONS);

		sourceMods = new JComboBox<Mod>();
		targetMods = new JComboBox<Mod>();
		sourceMods.setToolTipText("<html>Source mod to pull GUI files from.<br>This is likely a controller mod for ME3</html>");
		targetMods.setToolTipText("<html>Target mod to use as a target for file transplanting.<br>This is the mod that you are trying to make compatibile with the source mod</html>");

		
		sourceModel = new DefaultComboBoxModel<Mod>();
		targetModel = new DefaultComboBoxModel<Mod>();

		for (int i = 0; i < ModManagerWindow.ACTIVE_WINDOW.modModel.getSize(); i++) {
			sourceModel.addElement(ModManagerWindow.ACTIVE_WINDOW.modModel.getElementAt(i));
			targetModel.addElement(ModManagerWindow.ACTIVE_WINDOW.modModel.getElementAt(i));
		}
		sourceMods.setModel(sourceModel);
		targetMods.setModel(targetModel);

		directTransplantMod = new JRadioButton("Direct");
		cloneMod = new JRadioButton("Clone");
		dlcModCloneMod = new JRadioButton("Compatibility");

		modTypeButtonGroup.add(directTransplantMod);
		modTypeButtonGroup.add(cloneMod);
		modTypeButtonGroup.add(dlcModCloneMod);

		JLabel introLabel = new JLabel(
				"<html>This tool is used to generate a new GUI compatibilty mod between two mods.<br>This tool only transplants the interface files.<br>Therefore, this tool is made mainly for controller support mods.</html>");

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel rootPanel = new JPanel(gbl);
		int sourceRow = 0;

		c.gridx = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		rootPanel.add(introLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		c.gridy = ++sourceRow;
		c.gridwidth = 1;
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBorder(new TitledBorder(new EtchedBorder(), "Source Mod"));
		sourcePanel.add(sourceMods);
		rootPanel.add(sourcePanel, c);

		c.gridx = 1;
		JPanel targetPanel = new JPanel();
		targetPanel.setBorder(new TitledBorder(new EtchedBorder(), "Target Mod"));
		targetPanel.add(targetMods);
		rootPanel.add(targetPanel, c);

		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy = ++sourceRow;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		JPanel typePanel = new JPanel(new FlowLayout());
		typePanel.add(directTransplantMod);
		typePanel.add(cloneMod);
		typePanel.add(dlcModCloneMod);
		JPanel typeContainer = new JPanel(new BorderLayout());
		typeContainer.setBorder(new TitledBorder(new EtchedBorder(), "Transplant Type"));
		typeContainer.add(typePanel, BorderLayout.NORTH);
		opDescriptionLabel = new JLabel("Select a transplant type");

		typeContainer.add(opDescriptionLabel, BorderLayout.SOUTH);

		rootPanel.add(typeContainer, c);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		c.weighty = 0;
		c.gridy = ++sourceRow;
		progressBar = new JProgressBar();
		transplantButton = new JButton("Transplant");
		JPanel operationPanel = new JPanel(new BorderLayout());
		operationPanel.add(progressBar,BorderLayout.CENTER);
		operationPanel.add(transplantButton,BorderLayout.EAST);
		rootPanel.add(operationPanel,c);
		
		add(rootPanel);
		pack();
		setLocationRelativeTo(callingWindow);

		//listeners
		directTransplantMod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				transplantTypeChanged(TRANSPLANT_TYPE_DIRECT);
			}
		});

		cloneMod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				transplantTypeChanged(TRANSPLANT_TYPE_CLONE);
			}
		});

		dlcModCloneMod.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				transplantTypeChanged(TRANSPLANT_TYPE_COMPATIBILITY);
			}
		});

	}

	private void transplantTypeChanged(int type) {
		Mod sourceMod = sourceModel.getElementAt(sourceMods.getSelectedIndex());
		Mod targetMod = targetModel.getElementAt(targetMods.getSelectedIndex());

		switch (type) {
		case TRANSPLANT_TYPE_DIRECT:
			opDescriptionLabel.setText("Transplants GUI files directly from " + sourceMod + " into " + targetMod + ".");
			break;
		case TRANSPLANT_TYPE_CLONE:
			opDescriptionLabel.setText("Clones " + targetMod
					+ " as a new mod, and then performs a direct transplant into the clone. Uses GUI files from " + sourceMod + ".");
			break;
		case TRANSPLANT_TYPE_COMPATIBILITY:
			opDescriptionLabel.setText("Creates a new mod that is used as a compatibility pack so " + targetMod + " will work with " + sourceMod
					+ ". This type of transplant only works for singleplayer and has to be installed along with " + targetMod
					+ ". The compatibility pack can be distributed.");
			break;
		}
		opDescriptionLabel.setText("<html><div style='width: 300px'>" + opDescriptionLabel.getText() + "</div></html>");
	}

	private void performTransplant() {

	}
}
