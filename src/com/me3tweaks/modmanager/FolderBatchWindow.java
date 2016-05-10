package com.me3tweaks.modmanager;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

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
			JLabel headerLabel = new JLabel("<html>You dropped a folder onto Mod Manager:<br>" + droppedFile
					+ "<br>Select what operation to perform on the contents of this folder.<br>Hover over each button to see a description.</html>");
			panel.add(headerLabel, c);

			JButton compileAllTLK = new JButton("Compile all TLK XML Manifests");

			JButton decompileAllTLK = new JButton("Decompile all TLK files");
			JButton compileAllCoalesced = new JButton("Compile all Coalesced manifest");
			JButton decompileAllCoalesced = new JButton("Deompile all Coalesced files");
			JButton decompressAllPcc = new JButton("Decompress all PCC files");
			JButton compressAllPcc = new JButton("Compress all PCC files");
			JButton sideloadAllModMaker = new JButton("Sideload all ModMaker XML files");

			compileAllTLK
					.setToolTipText("<html>Treats each .xml file in the folder as a TankMaster TLK manifest.<br>Will attempt to compile all of them.</html>");
			decompileAllTLK.setToolTipText("<html>Decompiles all TLK files using the TankMaster compiler tool included with Mod Manager.</html>");
			decompileAllCoalesced
					.setToolTipText("<html>Decompils all Coalesced.bin files (will use header info) using the TankMaster compiler tool included with Mod Manager.</html>");
			decompressAllPcc.setToolTipText("<html>Decompresses all PCC files to their uncompressed state</html>");
			compressAllPcc.setToolTipText("<html>Compresses all PCC files to their compressed state, using the game's method of compression</html>");
			sideloadAllModMaker.setToolTipText("<html>Sideload all XML files as ModMaker mods and compile them in batch mode</html>");

			compileAllTLK.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose COMPILE_TLK operation");
					new BatchWorker(droppedFile, BatchWorker.COMPILE_TLK).execute();
					dispose();
				}
			});

			decompileAllTLK.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose DECOMPILE_TLK operation");
					new BatchWorker(droppedFile, BatchWorker.DECOMPILE_TLK).execute();
					dispose();
				}
			});

			compileAllCoalesced.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose COMPILE_COAL operation");
					new BatchWorker(droppedFile, BatchWorker.COMPILE_COAL).execute();
					dispose();
				}
			});

			decompileAllCoalesced.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ModManager.debugLogger.writeMessage("User chose DECOMPILE_COAL operation");
					new BatchWorker(droppedFile, BatchWorker.DECOMPILE_COAL).execute();
					dispose();
				}
			});

			decompressAllPcc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose DECOMPRESS_PCC operation");
					new BatchWorker(droppedFile, BatchWorker.DECOMPRESS_PCC).execute();
					dispose();
				}
			});

			compressAllPcc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose COMPRESS_PCC operation");
					new BatchWorker(droppedFile, BatchWorker.COMPRESS_PCC).execute();
					dispose();
				}
			});

			sideloadAllModMaker.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ModManager.debugLogger.writeMessage("User chose SIDELOAD_MODMAKER operation");
					new BatchWorker(droppedFile, BatchWorker.SIDELOAD_MODMAKER).execute();
					dispose();
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
			panel.add(decompressAllPcc, c);
			c.gridy++;
			panel.add(compressAllPcc, c);
			c.gridy++;
			panel.add(sideloadAllModMaker, c);
			c.gridy++;

		} else {
			String extension = FilenameUtils.getExtension(droppedFile.getAbsolutePath());
			switch (extension) {
			case "xml":
				JLabel headerLabel = new JLabel("<html>You dropped an XML file onto Mod Manager.<br>" + droppedFile
						+ "<br>Select what operation to perform with this file.</html>");
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

	static class BatchWorker extends SwingWorker<Void, String> {

		protected static final int COMPILE_TLK = 0;
		protected static final int DECOMPILE_TLK = 1;
		protected static final int COMPILE_COAL = 2;
		protected static final int DECOMPILE_COAL = 3;
		protected static final int DECOMPRESS_PCC = 4;
		protected static final int COMPRESS_PCC = 5;
		protected static final int SIDELOAD_MODMAKER = 6;

		private int operation;
		private File folder;

		public BatchWorker(File droppedFile, int operation) {
			this.operation = operation;
			this.folder = droppedFile;
		}

		@Override
		protected Void doInBackground() throws Exception {
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
			Collection<File> files = FileUtils.listFiles(folder, suff, FalseFileFilter.INSTANCE);
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
			if (ModManagerWindow.ACTIVE_WINDOW != null)
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(chunks.get(chunks.size() - 1));
		}

		@Override
		protected void done() {
			if (ModManagerWindow.ACTIVE_WINDOW != null)
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Batch operation completed");
		}
	}
}
