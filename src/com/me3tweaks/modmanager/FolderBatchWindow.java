package com.me3tweaks.modmanager;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.objects.ProcessResult;

public class FolderBatchWindow extends JDialog {
	File droppedFile;

	public FolderBatchWindow(JFrame parentFrame, File file) {
		droppedFile = file;
		setupWindow();
		setLocationRelativeTo(parentFrame);
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Batch Task Selector");
		setIconImages(ModManager.ICONS);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		if (droppedFile.isDirectory()) {
			JLabel headerLabel = new JLabel(
					"<html>You dropped a folder onto Mod Manager.<br>Select what operation to perform on the contents of this folder.<br>Hover over each button to see a description.</html>");
			panel.add(headerLabel, c);

			JButton compileAllTLK = new JButton("Compile all TLK XML Manifests");
			JButton decompileAllTLK = new JButton("Decompile all TLK files");
			JButton compileAllCoalesced = new JButton("Compile all Coalesced manifest");
			JButton decompileAllCoalesced = new JButton("Deompile all Coalesced files");

			compileAllTLK.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			decompileAllTLK.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			compileAllCoalesced.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			decompileAllCoalesced.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			c.gridy++;
			panel.add(compileAllTLK, c);
			c.gridy++;

			panel.add(decompileAllTLK, c);
			c.gridy++;

			panel.add(compileAllCoalesced, c);
			c.gridy++;

			panel.add(decompileAllCoalesced, c);

		} else {
			String extension = FilenameUtils.getExtension(droppedFile.getAbsolutePath());
			switch (extension) {
			case "xml":
				JLabel headerLabel = new JLabel("<html>You dropped an XML file onto Mod Manager.<br>Select what operation to perform with this file.</html>");
				panel.add(headerLabel, c);

				JButton compileTLK = new JButton("Compile TLK (TLK Manifest (Tankmaster only))");
				JButton compileCoalesced = new JButton("Compile Coalesced (Coalesced Manifest)");
				JButton sideloadModMaker = new JButton("Sideload ModMaker mod (Mod Delta)");

				compileTLK.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ProcessResult pr = TLKTool.compileTLK(droppedFile);
						if (pr.getReturnCode() == 0) {
							ModManager.debugLogger.writeMessage("Compiled dropped TLK manifest");
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Compiled TLK file");
						} else {
							ModManager.debugLogger.writeError("Error compiling dropped TLK manifest [" + pr.getReturnCode() + "]");
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error compiling TLK file [" + pr.getReturnCode() + "]");
						}
						dispose();
					}
				});

				compileCoalesced.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ProcessResult pr = CoalescedWindow.compileCoalesced(droppedFile.getAbsolutePath());
						if (pr.getReturnCode() == 0) {
							ModManager.debugLogger.writeMessage("Compiled dropped coalesced manifest");
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Compiled Coalesced file");
						} else {
							ModManager.debugLogger.writeError("Error compiling dropped Coalesced manifest [" + pr.getReturnCode() + "]");
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error compiling Coalesced file [" + pr.getReturnCode() + "]");
						}
						dispose();
					}
				});
				sideloadModMaker.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
						ModManager.debugLogger.writeMessage("Sideloading dropped ModMaker mod");
						ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Sideloading ModMaker mod...");
						new ModMakerCompilerWindow(droppedFile.getAbsolutePath(), ModMakerEntryWindow.getDefaultLanguages());
					}
				});

				c.gridy++;
				panel.add(compileTLK, c);
				c.gridy++;

				panel.add(compileCoalesced, c);
				c.gridy++;

				panel.add(sideloadModMaker, c);
				c.gridy++;
			}

		}
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();

	}
}
