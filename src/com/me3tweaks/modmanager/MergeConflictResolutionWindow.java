package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;

public class MergeConflictResolutionWindow extends JDialog implements ActionListener {
	JTextField newNameField;
	JButton mergeButton, favorLeft, favorRight;
	HashMap<String, ArrayList<String>> conflictFiles;
	HashMap<String, ArrayList<ButtonGroup>> buttonGroups;
	private Mod mod1;
	private Mod mod2;
	private MergeModWindow callingWindow;
	
	public MergeConflictResolutionWindow(MergeModWindow callingWindow, Mod mod1, Mod mod2){
		this.callingWindow = callingWindow;
		this.mod1 = mod1;
		this.mod2 = mod2;
		buttonGroups = new HashMap<String, ArrayList<ButtonGroup>>();
		setupWindow();
		setVisible(true);
	}
	
	private void setupWindow(){
		setTitle("Merge Conflicts");
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));

		JPanel contentPanel = new JPanel(new BorderLayout());
		JScrollPane listScroller = new JScrollPane(contentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		JLabel info = new JLabel("<html>The mods you have chosen to merge both attempt to install the same files.<br>Since mods are full file replacement, you must choose which file you will use for the merged mod.</html>");
		topPanel.add(info, BorderLayout.NORTH);
		
		JPanel favorPanel = new JPanel(new BorderLayout());
		favorLeft = new JButton("Select all from "+mod1.getModName());
		favorRight = new JButton("Select all from "+mod2.getModName());
		favorLeft.addActionListener(this);
		favorRight.addActionListener(this);
		
		favorPanel.add(favorLeft, BorderLayout.WEST);
		favorPanel.add(favorRight, BorderLayout.EAST);
		topPanel.add(favorPanel, BorderLayout.CENTER);
		
		JPanel conflictPanel = new JPanel();
		conflictPanel.setLayout(new BoxLayout(conflictPanel, BoxLayout.PAGE_AXIS));
		conflictFiles = mod1.getConflictsWithMod(mod2);
		Iterator<Map.Entry<String, ArrayList<String>>>  it = conflictFiles.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>> )it.next();
	        String module = pairs.getKey();
	        JPanel moduleConflictPanel = new JPanel();
	        moduleConflictPanel.setLayout(new BoxLayout(moduleConflictPanel, BoxLayout.PAGE_AXIS));
	        TitledBorder moduleBorder = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					"Conflicts in "+module);
	        moduleConflictPanel.setBorder(moduleBorder);
	        //moduleConflictPanel.add(moduleLabel);
	        
	        for (String conflictFile : pairs.getValue()) {
		        JPanel singleConflictPanel = new JPanel(new BorderLayout());
		        TitledBorder conflictBorder = BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
						FilenameUtils.getName(conflictFile));
		        singleConflictPanel.setBorder(conflictBorder);
		        ButtonGroup bg = new ButtonGroup();
		        //JLabel fileLabel = new JLabel(FilenameUtils.getName(conflictFile));
		        
		        JRadioButton mod1Button = new JRadioButton("Use "+mod1.getModName());
		        mod1Button.setActionCommand("left");
		        mod1Button.addActionListener(this);
		        
		        JRadioButton mod2Button = new JRadioButton("Use "+mod2.getModName());
		        mod2Button.setActionCommand("right");
		        mod2Button.addActionListener(this);
		        bg.add(mod1Button);
		        bg.add(mod2Button);
		        if (buttonGroups.containsKey(module)) {
		        	buttonGroups.get(module).add(bg);
		        } else {
		        	ArrayList<ButtonGroup> moduleGroup = new ArrayList<ButtonGroup>();
		        	moduleGroup.add(bg);
		        	buttonGroups.put(module, moduleGroup);
		        }
		        //singleConflictPanel.add(fileLabel, BorderLayout.NORTH);
		        singleConflictPanel.add(mod1Button, BorderLayout.WEST);
		        singleConflictPanel.add(mod2Button, BorderLayout.EAST);
		        moduleConflictPanel.add(singleConflictPanel);
	        }
	        conflictPanel.add(moduleConflictPanel);
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		//for (String )
		
		mergeButton = new JButton("Merge Mods");
		mergeButton.addActionListener(this);
		mergeButton.setEnabled(false);
		contentPanel.add(topPanel, BorderLayout.NORTH);
		contentPanel.add(conflictPanel, BorderLayout.CENTER);
		contentPanel.add(mergeButton, BorderLayout.SOUTH);
		
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		add(listScroller);
		pack();
		setLocationRelativeTo(callingWindow);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("left") || e.getActionCommand().equals("right")) {
			//radiobutton click
			if (canSubmit()) {
				mergeButton.setEnabled(true);
			}
		}
		System.out.println(e.getActionCommand());
	}
	
	private boolean canSubmit() {
		for (Map.Entry<String, ArrayList<ButtonGroup>> entry : buttonGroups.entrySet()) {
		    String key = entry.getKey();
		    ArrayList<ButtonGroup> groups = entry.getValue();
			for (int i = 0; i < groups.size(); i++) { //for all groups
				ButtonGroup bg = groups.get(i);
				Enumeration<AbstractButton> enumer = bg.getElements();
				int X = MergeModWindow.LEFT;
				while (enumer.hasMoreElements()) { //for buttons in the group
					JRadioButton button = (JRadioButton) enumer.nextElement();
					if (button.isSelected()) {
						System.out.println("FOUND THE SOURCE OF THE CLICK");
						break;
					}
					if (X == MergeModWindow.LEFT) {
						X = MergeModWindow.RIGHT; //
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private void resolveConflicts() {
		for (Map.Entry<String, ArrayList<ButtonGroup>> entry : buttonGroups.entrySet()) {
		    String key = entry.getKey();
		    ArrayList<ButtonGroup> groups = entry.getValue();
			for (int i = 0; i < groups.size(); i++) { //for all groups
				ButtonGroup bg = groups.get(i);
				Enumeration<AbstractButton> enumer = bg.getElements();
				int X = MergeModWindow.LEFT;
				while (enumer.hasMoreElements()) { //for buttons in the group
					JRadioButton button = (JRadioButton) enumer.nextElement();
					if (button.isSelected()) {
						break;
					}
					if (X == MergeModWindow.LEFT) {
						X = MergeModWindow.RIGHT; //
					} else {
						System.err.print("Cannot resolve conflicts, one box was not selected!");
					}
				}
			}
		}
	}

}
