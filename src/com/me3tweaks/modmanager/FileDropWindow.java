package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArchUtils;

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.StarterKitWindow.StarterKitProgressDialog;
import com.me3tweaks.modmanager.mapmesh.MapMeshViewer;
import com.me3tweaks.modmanager.modmaker.ModMakerCompilerWindow;
import com.me3tweaks.modmanager.modmaker.ModMakerEntryWindow;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class FileDropWindow extends JDialog {
	ArrayList<Path> droppedFiles;
	boolean show = true;
	private final Object lock = new Lock(); //threading wait() and notifyall();

	public FileDropWindow(JFrame parentFrame, ArrayList<Path> paths) {
		this.droppedFiles = paths;
		setupWindow2();
		if (show) {
			setVisible(true);
		}
	}

	private void setupWindow2() {
		setTitle("Dropped files task selector");
		setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel();

		File droppedFile = droppedFiles.get(0).toFile();
		if (droppedFile.isDirectory()) {
			//only first directory is supported right now.
			ArrayList<File> folder = new ArrayList<>();
			folder.add(droppedFile);

			panel.setLayout(new BorderLayout());

			JLabel headerLabel = new JLabel(
					"<html><center>" + droppedFile
							+ "<br>Select what operation to perform on the contents of this folder.<br>Hover over each button to see a description.</center></html>",
					SwingConstants.CENTER);
			panel.add(headerLabel, BorderLayout.NORTH);

			JButton decompileAllTLK = new JButton("Decompile all TLK files");

			JButton compileAllTLK = new JButton("Compile all as TLK XML Manifests");
			JButton compileAllCoalesced = new JButton("Compile all as Coalesced manifest");
			JButton sideloadAllModMaker = new JButton("Sideload all as ModMaker Mod files");

			JButton decompileAllCoalesced = new JButton("Decompile all Coalesced files");

			JButton decompressAllPcc = new JButton("Decompress all PCC files");
			JButton compressAllPcc = new JButton("Compress all PCC files");
			JButton dumpAllPCC = new JButton("Dump all PCC files");

			JButton[] tlkButtons = new JButton[] { decompileAllTLK };
			JButton[] binButtons = new JButton[] { decompileAllCoalesced };
			JButton[] pccButtons = new JButton[] { compressAllPcc, dumpAllPCC, decompressAllPcc };
			JButton[] xmlButtons = new JButton[] { compileAllTLK, compileAllCoalesced, sideloadAllModMaker };

			JPanel operationsPanel = new JPanel(new GridBagLayout());

			//LAYOUT ORDER
			//XML
			JPanel xmlFilePanel = new JPanel(new BorderLayout());
			//JPanel xmlFilePanel = new JPanel();
			//xmlFilePanel.setLayout(new BoxLayout(xmlFilePanel, BoxLayout.PAGE_AXIS));

			TitledBorder xmlBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "XML File Operations");
			xmlFilePanel.setBorder(xmlBorder);
			xmlFilePanel.add(compileAllTLK, BorderLayout.NORTH);
			xmlFilePanel.add(compileAllCoalesced, BorderLayout.CENTER);
			xmlFilePanel.add(sideloadAllModMaker, BorderLayout.SOUTH);

			//PCC
			JPanel pccFilePanel = new JPanel(new BorderLayout());

			TitledBorder pccBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "PCC File Operations");
			pccFilePanel.setBorder(pccBorder);
			pccFilePanel.add(decompressAllPcc, BorderLayout.NORTH);
			pccFilePanel.add(compressAllPcc, BorderLayout.CENTER);
			pccFilePanel.add(dumpAllPCC, BorderLayout.SOUTH);

			//TLK
			JPanel tlkFilePanel = new JPanel(new BorderLayout());

			TitledBorder tlkBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "TLK File Operations");
			tlkFilePanel.setBorder(tlkBorder);
			tlkFilePanel.add(decompileAllTLK, BorderLayout.CENTER);

			//BIN
			JPanel binFilePanel = new JPanel(new BorderLayout());

			TitledBorder binBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "BIN File Operations");
			binFilePanel.setBorder(binBorder);
			binFilePanel.add(decompileAllCoalesced, BorderLayout.CENTER);

			compileAllTLK.setToolTipText("<html>Treats each .xml file in the folder as a TankMaster TLK manifest.<br>Will attempt to compile all of them.</html>");
			decompileAllTLK.setToolTipText("<html>Decompiles all TLK files using the TankMaster compiler tool included with Mod Manager.</html>");
			decompileAllCoalesced
					.setToolTipText("<html>Decompiles all Coalesced.bin files (will use header info) using the TankMaster compiler tool included with Mod Manager.</html>");
			decompressAllPcc.setToolTipText("<html>Decompresses all PCC files to their uncompressed state</html>");
			compressAllPcc.setToolTipText("<html>Compresses all PCC files to their compressed state, using the game's method of compression</html>");
			sideloadAllModMaker.setToolTipText("<html>Sideload all XML files as ModMaker mods and compile them in batch mode</html>");
			dumpAllPCC.setToolTipText("Dump searchable information from PCC files");

			compileAllTLK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose COMPILE_TLK operation");
					new BatchWorker(folder, BatchWorker.COMPILE_TLK, null).execute();
					dispose();
				}
			});

			decompileAllTLK.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose DECOMPILE_TLK operation");
					new BatchWorker(folder, BatchWorker.DECOMPILE_TLK, null).execute();
					dispose();
				}
			});

			compileAllCoalesced.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose COMPILE_COAL operation");
					new BatchWorker(folder, BatchWorker.COMPILE_COAL, null).execute();
					dispose();
				}
			});

			decompileAllCoalesced.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose DECOMPILE_COAL operation");
					new BatchWorker(folder, BatchWorker.DECOMPILE_COAL, null).execute();
					dispose();
				}
			});

			decompressAllPcc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose DECOMPRESS_PCC operation");
					new BatchWorker(folder, BatchWorker.DECOMPRESS_PCC, null).execute();
					dispose();
				}
			});

			compressAllPcc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose COMPRESS_PCC operation");
					new BatchWorker(folder, BatchWorker.COMPRESS_PCC, null).execute();
					dispose();
				}
			});

			sideloadAllModMaker.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose SIDELOAD_MODMAKER operation");
					new BatchWorker(folder, BatchWorker.SIDELOAD_MODMAKER, null).execute();
					dispose();
				}
			});

			dumpAllPCC.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
					//get only pcc files
					Collection<File> files = FileUtils.listFiles(droppedFile, new String[] { "pcc" }, false);
					ArrayList<Path> dumpfiles = new ArrayList<>();
					for (File f : files) {
						dumpfiles.add(f.toPath());
					}
					new PCCDataDumperWindow(dumpfiles);
				}
			});

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			operationsPanel.add(xmlFilePanel, c);
			c.gridx = 1;
			c.gridy = 0;
			operationsPanel.add(pccFilePanel, c);
			c.gridx = 0;
			c.gridy = 1;
			operationsPanel.add(tlkFilePanel, c);
			c.gridx = 1;
			c.gridy = 1;
			operationsPanel.add(binFilePanel, c);
			panel.add(operationsPanel, BorderLayout.CENTER);

			//Disable buttons as necessary by analyzing what files are in the folder
			String[] parsedExtensions = new String[] { "pcc", "xml", "bin", "tlk" };
			Collection<File> files = FileUtils.listFiles(droppedFile, parsedExtensions, false);

			boolean foundPCCfiles = false;
			boolean foundXMLfiles = false;
			boolean foundTLKfiles = false;
			boolean foundBINfiles = false;

			//figure out which buttons to enable
			for (File f : files) {
				String extension = FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase();
				switch (extension) {
				case "pcc":
					foundPCCfiles = true;
					break;
				case "xml":
					foundXMLfiles = true;
					break;
				case "tlk":
					foundTLKfiles = true;
					break;
				case "bin":
					foundBINfiles = true;
					break;
				}
			}
			if (!foundPCCfiles) {
				for (JButton b : pccButtons) {
					b.setEnabled(false);
					b.setToolTipText("No PCC files were found in the listed folder.");
				}
			}
			if (!foundXMLfiles) {
				for (JButton b : xmlButtons) {
					b.setEnabled(false);
					b.setToolTipText("No XML files were found in the listed folder.");
				}
			}
			if (!foundTLKfiles) {
				for (JButton b : tlkButtons) {
					b.setEnabled(false);
					b.setToolTipText("No TLK files were found in the listed folder.");
				}
			}
			if (!foundBINfiles) {
				for (JButton b : binButtons) {
					b.setEnabled(false);
					b.setToolTipText("No BIN files were found in the listed folder.");
				}
			}
		} else {
			setResizable(false);
			panel.setLayout(new BorderLayout());

			ArrayList<File> filesToPass = new ArrayList<>();
			ModManager.debugLogger.writeMessage("Files that will be passed to client workers:");
			for (Path f : droppedFiles) {
				filesToPass.add(f.toFile());
				ModManager.debugLogger.writeMessage(f.toFile().getAbsolutePath());
			}
			String topLabelStr = "<html><center>" + filesToPass.size() + " files were dropped onto Mod Manager.<br>Select what you'd like to do with these files.</center></html>";
			if (filesToPass.size() == 1) {
				topLabelStr = "<html><center>" + droppedFile.getName() + "<br>Select what you'd like to do with this file.</center></html>";

			}

			JLabel topLabel = new JLabel(topLabelStr);

			topLabel.setHorizontalAlignment(JLabel.CENTER);
			panel.add(topLabel, BorderLayout.NORTH);
			String extension = FilenameUtils.getExtension(droppedFile.getAbsolutePath()); //we already ensured all extensions are the same.
			switch (extension) {
			case "xml": {

				JButton compileTLK = new JButton("Compile as TLK manifest");
				JButton compileCoalesced = new JButton("Compile as coalesced manifest");
				JButton sideloadModmaker = new JButton("Sideload as ModMaker mod");

				JPanel xmlFilePanel = new JPanel();
				xmlFilePanel.setLayout(new BoxLayout(xmlFilePanel, BoxLayout.PAGE_AXIS));

				TitledBorder xmlBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "XML File Operations");
				xmlFilePanel.setBorder(xmlBorder);

				JPanel tlkXMLPanel = new JPanel();
				TitledBorder tlkManifestBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "XML - TLK Manifest Files");
				tlkXMLPanel.setBorder(tlkManifestBorder);
				tlkXMLPanel.add(compileTLK);

				JPanel coalescedXMLPanel = new JPanel();
				TitledBorder coalescedManifestBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "XML - Coalesced Manifest Files");
				coalescedXMLPanel.setBorder(coalescedManifestBorder);
				coalescedXMLPanel.add(compileCoalesced);

				JPanel modmakerXMLPanel = new JPanel();
				TitledBorder modmakerManifestBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "XML - ModMaker mod delta files");
				modmakerXMLPanel.setBorder(modmakerManifestBorder);
				modmakerXMLPanel.add(sideloadModmaker);

				xmlFilePanel.add(coalescedXMLPanel);
				xmlFilePanel.add(tlkXMLPanel);
				xmlFilePanel.add(modmakerXMLPanel);

				panel.add(xmlFilePanel, BorderLayout.CENTER);

				compileTLK.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ModManager.debugLogger.writeMessage("User chose singular COMPILE_TLK operation");
						new BatchWorker(filesToPass, BatchWorker.COMPILE_TLK, null).execute();
						dispose();
					}
				});

				compileCoalesced.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ModManager.debugLogger.writeMessage("User chose singular COMPILE_COAL operation");
						new BatchWorker(filesToPass, BatchWorker.COMPILE_COAL, null).execute();
						dispose();
					}
				});

				sideloadModmaker.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						//TODO - make it work with multiple files.
						ModManager.debugLogger.writeMessage("User chose singular SIDELOAD_MODMAKER operation");
						new BatchWorker(filesToPass, BatchWorker.SIDELOAD_MODMAKER, null).execute();
						dispose();
					}
				});
				break;
			}
			case "pcc":

			{
				JButton decompressAllPcc = new JButton("Decompress PCC files");
				JButton compressAllPcc = new JButton("Compress PCC files");

				decompressAllPcc.setToolTipText("Decompresses PCC files to their uncompressed state.");
				compressAllPcc.setToolTipText("Compresses PCC files to their compressed state - this does not work on DLC files.");

				JPanel pccFilePanel = new JPanel();
				pccFilePanel.setLayout(new BoxLayout(pccFilePanel, BoxLayout.LINE_AXIS));

				TitledBorder xmlBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "PCC File Operations");
				pccFilePanel.setBorder(xmlBorder);

				JPanel compressionPCCPanel = new JPanel(new BorderLayout());
				TitledBorder compressionBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "PCC - Compression options");
				compressionPCCPanel.setBorder(compressionBorder);
				compressionPCCPanel.add(decompressAllPcc, BorderLayout.NORTH);
				compressionPCCPanel.add(compressAllPcc, BorderLayout.SOUTH);

				JButton aipathfinding = new JButton("View AI Pathfinding in Map Pathfinding Viewer");
				aipathfinding.setToolTipText("View this file in the pathfinding viewer - file must contain path nodes (typically BioD)");
				if (!ResourceUtils.is64BitWindows()) {
					aipathfinding.setEnabled(false);
					aipathfinding.setToolTipText("Requires 64-bit Windows");
				} else if (filesToPass.size() > 1) {
					aipathfinding.setEnabled(false);
					aipathfinding.setToolTipText("Drop only one file onto Mod Manager's window to use this drag and drop shortcut.");
				}
				JButton dumppcc = new JButton("Dump PCC info with PCC Data Dumper");
				JPanel readPCCPanel = new JPanel(new BorderLayout());
				dumppcc.setToolTipText("Dump PCC information to disk in an easily readable format");

				TitledBorder coalescedManifestBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "PCC - Data Viewers");
				readPCCPanel.setBorder(coalescedManifestBorder);
				readPCCPanel.add(aipathfinding, BorderLayout.NORTH);
				readPCCPanel.add(dumppcc, BorderLayout.SOUTH);

				pccFilePanel.add(compressionPCCPanel);
				pccFilePanel.add(readPCCPanel);

				panel.add(pccFilePanel, BorderLayout.CENTER);

				decompressAllPcc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//TODO - make it work with multiple files.
						ModManager.debugLogger.writeMessage("User chose singular COMPRESS_PCC operation");
						new BatchWorker(filesToPass, BatchWorker.DECOMPRESS_PCC, null).execute();
						dispose();
					}
				});

				aipathfinding.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ModManager.debugLogger.writeMessage("User chose singular PATHFINDING_VIEW operation");
						dispose();
						new MapMeshViewer(droppedFile);
					}
				});

				compressAllPcc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ModManager.debugLogger.writeMessage("User chose singular COMPRESS_PCC operation");
						new BatchWorker(filesToPass, BatchWorker.COMPRESS_PCC, null).execute();
						dispose();
					}
				});

				dumppcc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
						new PCCDataDumperWindow(droppedFiles); //Files.walk returns this so we pass this instead.
					}
				});
				break;

			}
			case "bin": {
				JButton decompileCoalescedFile = new JButton("Decompile Coalesced Files");

				JPanel binFilePanel = new JPanel();
				binFilePanel.setLayout(new BoxLayout(binFilePanel, BoxLayout.PAGE_AXIS));

				TitledBorder coalescedFilePanelBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "BIN File Operations");
				binFilePanel.setBorder(coalescedFilePanelBorder);

				JPanel coalescedPanel = new JPanel();
				TitledBorder coalescedBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "BIN - Coalesced File");
				coalescedPanel.setBorder(coalescedBorder);
				coalescedPanel.add(decompileCoalescedFile);

				binFilePanel.add(coalescedPanel, BorderLayout.CENTER);

				//Check if coalesced
				byte[] buffer = new byte[4];

				for (File eachFile : filesToPass) {
					try (InputStream is = new FileInputStream(eachFile.getAbsolutePath())) {
						if (is.read(buffer) != buffer.length) {
							// do something
							decompileCoalescedFile.setEnabled(false);
							decompileCoalescedFile.setToolTipText("One of the dropped files is not a coalesced file.");
							ModManager.debugLogger.writeMessage(eachFile+" is is not long enough to be a coalesced file.");
							return;
						}
						int magic = ResourceUtils.byteArrayToInt(buffer);
						if (magic != ModManager.COALESCED_MAGIC_NUMBER) {
							//not a coalesced file
							decompileCoalescedFile.setEnabled(false);
							decompileCoalescedFile.setToolTipText("One of the dropped files is not a coalesced file.");
							ModManager.debugLogger.writeMessage(eachFile+"'s magic number does not match a coalesced file.");
						}
						is.close();
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Error reading input binary file! (Coalesced Drop)", e);
					}
				}
				panel.add(binFilePanel, BorderLayout.CENTER);
				decompileCoalescedFile.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ModManager.debugLogger.writeMessage("User chose singular DECOMPILE_COAL operation");
						new BatchWorker(filesToPass, BatchWorker.DECOMPILE_COAL, null).execute();
						dispose();
					}
				});

				break;
			}
			case "dlc": {
				//This case is unused as there is nothing useful to batch in this.
				//ModManagerWindow will automatically open mount editor instead
				//Code left here in case it is useful in the future for something.
				JButton editMountButton = new JButton("Edit Mount.dlc in Mount Editor (NOT IMPLEMENTED)");

				JPanel mountFilePanel = new JPanel();
				mountFilePanel.setLayout(new BoxLayout(mountFilePanel, BoxLayout.PAGE_AXIS));

				TitledBorder mountFileBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mount.dlc file operations");
				mountFilePanel.setBorder(mountFileBorder);

				JPanel mountDLCOperationsPanel = new JPanel();
				TitledBorder mountOperationsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DLC metadata file");
				mountDLCOperationsPanel.setBorder(mountOperationsBorder);
				mountDLCOperationsPanel.add(editMountButton);

				mountFilePanel.add(mountDLCOperationsPanel);

				panel.add(mountFilePanel, BorderLayout.CENTER);

				//No action listener.

				break;
			}
			case "asi": {
				//This case is unused as there is nothing useful to batch in this.
				//ModManagerWindow will automatically show the import window instead.
				//Code left here in case it is useful in the future for something.
				JButton installASIButton = new JButton("Install ASI");
				installASIButton.setAlignmentX(CENTER_ALIGNMENT);
				JPanel asiFilePanel = new JPanel();
				asiFilePanel.setLayout(new BoxLayout(asiFilePanel, BoxLayout.PAGE_AXIS));
				TitledBorder asiBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "ASI Native Mod options");
				asiFilePanel.setBorder(asiBorder);

				JPanel asiInstallationPanel = new JPanel(new BorderLayout());
				TitledBorder coalescedBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "ASI - Native Mod");
				asiInstallationPanel.setBorder(coalescedBorder);
				asiInstallationPanel.setAlignmentX(CENTER_ALIGNMENT);
				JLabel warning = new JLabel("<html>Install ASI mods at your own risk, as they can execute native code.</html>");
				warning.setAlignmentX(CENTER_ALIGNMENT);

				asiInstallationPanel.add(warning, BorderLayout.NORTH);
				asiInstallationPanel.add(installASIButton, BorderLayout.CENTER);
				asiFilePanel.add(asiInstallationPanel);

				panel.add(asiFilePanel, BorderLayout.CENTER);

				installASIButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String copyDest = ModManager.appendSlash(new File(ModManagerWindow.GetBioGameDir()).getParent()) + "Binaries\\win32\\asi\\" + droppedFile.getName();
						try {
							FileUtils.copyFile(droppedFile, new File(copyDest));
							ModManager.debugLogger.writeMessage("Installed dropped ASI File: " + droppedFile + " to " + copyDest);
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Installed " + droppedFile.getName());
						} catch (IOException e1) {
							ModManager.debugLogger.writeErrorWithException("ASI Mod install failed " + copyDest + ":", e1);
							JOptionPane.showMessageDialog(null, "Unable to install ASI mod.\nYou may need to run Mod Manager as an administrator.", "Installation Failure",
									JOptionPane.ERROR_MESSAGE);
						}
						dispose();
					}
				});
				break;
			}
			case "tlk": {
				JButton decompileTLKButton = new JButton("Decompile TLK");
				decompileTLKButton.setAlignmentX(CENTER_ALIGNMENT);
				JPanel tlkFilePanel = new JPanel();
				tlkFilePanel.setLayout(new BoxLayout(tlkFilePanel, BoxLayout.PAGE_AXIS));

				TitledBorder tlkFileBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "TLK file options");
				tlkFilePanel.setBorder(tlkFileBorder);

				JPanel tlkDecompilePanel = new JPanel(new BorderLayout());
				TitledBorder tlkLocalizeBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "TLK - String localization file");
				tlkDecompilePanel.setBorder(tlkLocalizeBorder);

				tlkDecompilePanel.add(decompileTLKButton, BorderLayout.CENTER);
				tlkFilePanel.add(tlkDecompilePanel);

				panel.add(tlkFilePanel, BorderLayout.CENTER);

				decompileTLKButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						//TODO - make it work with multiple files.
						ModManager.debugLogger.writeMessage("User chose singular DECOMPILE_TLK operation");
						new BatchWorker(filesToPass, BatchWorker.DECOMPILE_TLK, null).execute();
						dispose();
					}
				});
				break;
			}
			case "txt": {
				JButton pathfindingButton = new JButton("Open file in Map Pathfinding Viewer");
				if (!ResourceUtils.is64BitWindows()) {
					pathfindingButton.setEnabled(false);
					pathfindingButton.setToolTipText("Requires 64-bit Windows");
				}
				pathfindingButton.setAlignmentX(CENTER_ALIGNMENT);
				JPanel textFilePanel = new JPanel();
				textFilePanel.setLayout(new BoxLayout(textFilePanel, BoxLayout.PAGE_AXIS));
				TitledBorder textFileBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "TXT - Text file options");
				textFilePanel.setBorder(textFileBorder);

				JPanel pathfindingDumpPanel = new JPanel(new BorderLayout());
				TitledBorder pathfindingDumpBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "TXT - PCC or Pathfinding dump");
				pathfindingDumpPanel.setBorder(pathfindingDumpBorder);
				pathfindingDumpPanel.setAlignmentX(CENTER_ALIGNMENT);

				pathfindingDumpPanel.add(pathfindingButton, BorderLayout.NORTH);
				pathfindingDumpPanel.add(new JLabel("<html><center>" + droppedFiles.get(0).toFile().getAbsolutePath() + "</center></html>", SwingConstants.CENTER),
						BorderLayout.SOUTH);
				textFilePanel.add(pathfindingDumpPanel);

				panel.add(textFilePanel, BorderLayout.CENTER);

				pathfindingButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
						new MapMeshViewer(droppedFiles.get(0).toFile());
					}
				});
				break;
			}
			default: {
				JPanel failPanel = new JPanel();
				failPanel.add(new JLabel("<html><center>Unsupported Drag & Drop file extension: " + extension + "</center></html>", SwingConstants.CENTER));
				panel.add(failPanel);
			}
			}

		}

		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.add(panel, BorderLayout.CENTER);

		JPanel supportedTypesPanel = new JPanel(new GridLayout(4, 2));

		JLabel othersHeader = new JLabel("<html><center>Supported file drop types:</center></html>", SwingConstants.CENTER);

		supportedTypesPanel.add(new JLabel(".pcc (ME3 package file)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".bin (Coalesced file)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".tlk (Localization file)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".txt (PCC data dump file)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".xml (Multiple types)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".dlc (Mount.dlc file)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel(".asi (Runtime native mod)", SwingConstants.CENTER));
		supportedTypesPanel.add(new JLabel("Folder (batch job)", SwingConstants.CENTER));

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(othersHeader, BorderLayout.NORTH);
		bottomPanel.add(supportedTypesPanel, BorderLayout.CENTER);

		rootPanel.add(bottomPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		add(rootPanel);
		setMinimumSize(new Dimension(200, 200));
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	/**
	 * Folder based batch worker
	 * 
	 * @author Mgamerz
	 *
	 */
	static class BatchWorker extends SwingWorker<Void, String> {

		protected static final int COMPILE_TLK = 0;
		protected static final int DECOMPILE_TLK = 1;
		protected static final int COMPILE_COAL = 2;
		protected static final int DECOMPILE_COAL = 3;
		protected static final int DECOMPRESS_PCC = 4;
		protected static final int COMPRESS_PCC = 5;
		protected static final int SIDELOAD_MODMAKER = 6;

		private int operation;
		public final Object lock = new Lock(); //threading wait() and notifyall();
		public boolean completed = false;
		private StarterKitProgressDialog dialog;
		private ArrayList<File> files;

		//TODO: Make Batch Worker accept a a list of files/folders to parse instead of just calculating from directory

		public BatchWorker(ArrayList<File> files, int operation, StarterKitProgressDialog dialog) {
			this.operation = operation;
			this.files = files;
			this.dialog = dialog; //can be null.
		}

		@Override
		protected Void doInBackground() throws Exception {
			ArrayList<File> files = this.files;
			if (files.get(0).isDirectory()) {
				//get files from directory
				SuffixFileFilter suff = null;
				switch (operation) {
				case DECOMPILE_TLK:
					suff = new SuffixFileFilter(".tlk");
					break;
				case COMPRESS_PCC:
				case DECOMPRESS_PCC:
					suff = new SuffixFileFilter(".pcc");
					break;
				case SIDELOAD_MODMAKER:
				case COMPILE_TLK:
				case COMPILE_COAL:
					suff = new SuffixFileFilter(".xml");
					break;
				}
				if (suff == null) {
					return null;
				}

				files = (ArrayList<File>) FileUtils.listFiles(files.get(0), suff, FalseFileFilter.INSTANCE);
			}
			int processed = 0;
			for (File file : files) {
				processed++;
				switch (operation) {
				case COMPILE_TLK: {
					publish("Compiling " + FilenameUtils.getName(file.getAbsolutePath()));
					ProcessResult pr = TLKTool.compileTLK(file);
					break;
				}
				case DECOMPILE_TLK: {
					publish("Decompiling " + FilenameUtils.getName(file.getAbsolutePath()));
					ProcessResult pr = TLKTool.decompileTLK(file);
					break;
				}
				case COMPRESS_PCC: {
					publish("Compressing " + FilenameUtils.getName(file.getAbsolutePath()));
					ProcessResult pr = ModManager.compressPCC(file, file);
					break;
				}
				case DECOMPRESS_PCC: {
					publish("Decompressing " + FilenameUtils.getName(file.getAbsolutePath()));
					ProcessResult pr = ModManager.decompressPCC(file, file);
					break;
				}
				case SIDELOAD_MODMAKER:
					publish("Sideloading ModMaker mod [" + processed + "/" + files.size() + "]");
					ModMakerCompilerWindow mcw = new ModMakerCompilerWindow(file.getAbsolutePath(), ModMakerEntryWindow.getDefaultLanguages());
					while (mcw.isShowing()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				case COMPILE_COAL: {
					publish("Compiling " + FilenameUtils.getBaseName(file.getAbsolutePath()));
					ProcessResult pr = CoalescedWindow.compileCoalesced(file.getAbsolutePath());
					break;
				}
				case DECOMPILE_COAL: {
					publish("Decompiling " + FilenameUtils.getBaseName(file.getAbsolutePath()));
					ProcessResult pr = CoalescedWindow.decompileCoalesced(file.getAbsolutePath());
					break;
				}
				}
			}
			return null;
		}

		@Override
		protected void process(List<String> chunks) {
			if (dialog != null) {
				dialog.infoLabel.setText(chunks.get(chunks.size() - 1));
			} else if (ModManagerWindow.ACTIVE_WINDOW != null)
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(chunks.get(chunks.size() - 1));
		}

		@Override
		protected void done() {
			completed = true;
			synchronized (lock) {
				lock.notifyAll();
			}
			if (ModManagerWindow.ACTIVE_WINDOW != null)
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Batch operation completed");
		}
	}
}
