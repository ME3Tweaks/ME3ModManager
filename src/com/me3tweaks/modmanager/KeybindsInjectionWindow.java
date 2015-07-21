package com.me3tweaks.modmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;


/**
 * This window injects a user's customized set of keybinds into the base coalesced file by swapping the BioInput.xml file after decompilation
 * @author mjperez
 *
 */
public class KeybindsInjectionWindow extends JDialog {
	private JProgressBar progressbar;
	private JLabel infoLabel;
	private Mod mod;
	private final int TOTAL_STEPS = 3;


	public KeybindsInjectionWindow(JFrame callingWindow, Mod mod){
		this.mod = mod;
		setupWindow();
		//has basegame coal already
		this.setLocationRelativeTo(callingWindow);
		new DecompilerWorker().execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("Custom Keybinds Injector");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 110));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		
		JPanel bindingsPanel = new JPanel();
		bindingsPanel.setLayout(new BoxLayout(bindingsPanel, BoxLayout.PAGE_AXIS));
		infoLabel = new JLabel("Injecting custom keybinds into " + mod.getModName() + "...");
		infoLabel.setBorder(BorderFactory.createLineBorder(Color.black));
		infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		bindingsPanel.add(infoLabel);

		progressbar = new JProgressBar(0, 100);
		progressbar.setStringPainted(true);
		progressbar.setIndeterminate(false);
		progressbar.setEnabled(false);

		bindingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		bindingsPanel.add(progressbar);
		bindingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(bindingsPanel);
		this.pack();
	}
	
	class DecompilerWorker extends SwingWorker<Void, String> {
		private int stepsCompleted = 0;

		public DecompilerWorker() {
			ModManager.debugLogger.writeMessage("==================DecompilerWorker (KEYBINDS)==============");
			infoLabel.setText("Preparing to decompile "+mod.getModName());
			progressbar.setValue(0);
		}

		protected Void doInBackground() throws Exception {
			//check if coal job exists first, and if we have a pristine one (or need to DL one)
			String basegamecoal = mod.getBasegameCoalesced();
			if (basegamecoal == null) {
				if (!ModManager.hasPristineCoalesced(ModType.BASEGAME)){
					publish("Getting pristine Coalesced.bin file");
					ME3TweaksUtils.downloadPristineCoalesced(ModType.BASEGAME, ME3TweaksUtils.HEADER);
				}
				//String pristineBase = ModManager.getPristineCoalesced(ModType.BASEGAME);
				ModJob basegamejob = null;
				//check if it has basegame modjob
				for (ModJob job : mod.jobs) {
					if (job.modType == ModJob.BASEGAME){
						basegamejob = job;
						String jobFolder = ModManager.appendSlash(new File(job.getNewFiles()[0]).getParentFile().getAbsolutePath());
						String relativepath = ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator);
						FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME)), new File(mod.getModPath()+relativepath+"Coalesced.bin"));
						job.addFileReplace(mod.getModPath()+relativepath+"Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
						break;
					}
				}
							
				if (basegamejob == null) {
					ModJob job = new ModJob();
					File basegamefolder = new File(mod.getModPath()+"BASEGAME");
					basegamefolder.mkdirs();
					FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME)), new File(mod.getModPath()+"BASEGAME/Coalesced.bin"));
					job.addFileReplace(mod.getModPath()+"BASEGAME/Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
					mod.addTask(ModType.BASEGAME, job);
				}
				//move folder to staging area, then upgrade to standard format
				FileUtils.moveDirectory(new File(mod.modPath), new File(ModManager.getTempDir()+mod.modName));
				String stagingPath = ModManager.getTempDir()+mod.modName;
				mod.modPath = stagingPath;//used in createNewmod()
				KeybindsInjectionWindow.this.mod = mod.createNewMod();
				FileUtils.deleteDirectory(new File(stagingPath));
				basegamecoal = mod.getBasegameCoalesced();
			}

			//decompile
			String path = ModManager.getCompilingDir();
			File coalFile = new File(path+"/coalesceds/Coalesced.bin");
			FileUtils.copyFile(new File(basegamecoal), coalFile);
			
			String compilerPath = ModManager.getTankMasterCompilerDir()+"MassEffect3.Coalesce.exe";
			ProcessBuilder decompileProcessBuilder = new ProcessBuilder(compilerPath, coalFile.getAbsolutePath());
			ModManager.debugLogger.writeMessage("Executing decompile command: " + compilerPath + " " + coalFile.getAbsolutePath());
			decompileProcessBuilder.redirectErrorStream(true);
			decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process decompileProcess = decompileProcessBuilder.start();
			decompileProcess.waitFor();
			stepsCompleted++;
			
			//replace file
			publish("Installing custom keybinds");
			File userKeybinds = new File(ModManager.getOverrideDir()+"BioInput.xml");
			FileUtils.copyFile(userKeybinds, new File(path+"/coalesceds/Coalesced/BioInput.xml"));
			ModManager.debugLogger.writeMessage("Copied user keybinds into staging");
			stepsCompleted++;
			
			//recompile
			publish("Recompiling "+mod.getModName());
			ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerPath,
					path + "\\coalesceds\\Coalesced\\Coalesced.xml", "--mode=ToBin");
			//log it
			ModManager.debugLogger.writeMessage("Executing compile command: " + compilerPath +" "+
					path + "\\coalesceds\\Coalesced\\Coalesced.xml --mode=ToBin");
			compileProcessBuilder.redirectErrorStream(true);
			compileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process compileProcess = compileProcessBuilder.start();
			compileProcess.waitFor();
			stepsCompleted++;
			
			//copy back
			FileUtils.copyFile(coalFile,new File(basegamecoal));
			ModManager.debugLogger.writeMessage("Copied custom keybinds Coalesced.bin back to mod directory");
			return null;
		}

		@Override
		protected void process(List<String> status) {
			infoLabel.setText(status.get(status.size()-1));
			progressbar.setIndeterminate(false);
			progressbar.setValue((int) (100 / (TOTAL_STEPS / (float) stepsCompleted)));
		}

		protected void done() {
			dispose();
			JOptionPane.showMessageDialog(KeybindsInjectionWindow.this, "Your custom keybinds have been inserted into "+mod.getModName(), "Injection Complete", JOptionPane.INFORMATION_MESSAGE);
			new ModManagerWindow(false);
		}
	}
}
