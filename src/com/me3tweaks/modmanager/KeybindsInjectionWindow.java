package com.me3tweaks.modmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;


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
		new KeybindsInjectionWorker().execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("Custom Keybinds Injector");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 90));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		
		JPanel bindingsPanel = new JPanel();
		bindingsPanel.setLayout(new BoxLayout(bindingsPanel, BoxLayout.PAGE_AXIS));
		infoLabel = new JLabel("Injecting custom keybinds into " + mod.getModName() + "...");
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
	
	class KeybindsInjectionWorker extends SwingWorker<Void, String> {
		private int stepsCompleted = 0;

		public KeybindsInjectionWorker() {
			ModManager.debugLogger.writeMessage("==================KeybindsInjectionWorker==============");
			infoLabel.setText("Preparing to decompile "+mod.getModName());
			progressbar.setValue(0);
		}

		protected Void doInBackground() throws Exception {
			//copy mod to staging
			boolean copyWholeDirectory = false;
			String finalCoalDest = mod.getBasegameCoalesced(); //points to staging
			String stagingPath = ModManager.getTempDir()+mod.getModName()+"/";
			String stagingIniPath = stagingPath+"moddesc.ini";
			File staging = new File(stagingPath);
			ModManager.debugLogger.writeMessage("Removing existing temp dir if any: "+staging.getAbsolutePath());
			FileUtils.deleteDirectory(staging);
			ModManager.debugLogger.writeMessage("Copying mod to staging directory");
			FileUtils.copyDirectory(new File(mod.getModPath()), staging);
			ModManager.debugLogger.writeMessage("Reloading mod in staging area");
			mod = new Mod(stagingIniPath);
			if (!mod.isValidMod()){
				ModManager.debugLogger.writeError("Mod in staging is not valid!");
				return null;
			}
			
			//check if coal job exists first, and if we have a pristine one (or need to DL one)
			String basegamecoal = mod.getBasegameCoalesced(); //points to staging
			if (basegamecoal == null) {
				copyWholeDirectory = true;
				ModManager.debugLogger.writeMessage("Mod does not appear to mod Coalesced.bin, performing prerequesite changes");
				if (!ModManager.hasPristineCoalesced(ModType.BASEGAME,ME3TweaksUtils.HEADER)){
					publish("Getting pristine Coalesced.bin file");
					ME3TweaksUtils.downloadPristineCoalesced(ModType.BASEGAME, ME3TweaksUtils.HEADER);
				}
				//String pristineBase = ModManager.getPristineCoalesced(ModType.BASEGAME);
				ModJob basegamejob = null;
				//check if it has basegame modjob
				for (ModJob job : mod.jobs) {
					if (job.getModType() == ModJob.BASEGAME){
						basegamejob = job;
						String jobFolder = ModManager.appendSlash(new File(job.getNewFiles()[0]).getParentFile().getAbsolutePath());
						String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));
						System.out.println(relativepath);
						FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME,ME3TweaksUtils.HEADER)), new File(ModManager.appendSlash(mod.getModPath())+relativepath+"Coalesced.bin"));
						job.addFileReplace(mod.getModPath()+relativepath+"Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
						break;
					}
				}
							
				if (basegamejob == null) {
					//no basegame header, but has tasks, and does not mod coal
					//means it doesn't modify basegame files at all so we can just add the header and set modver to 3 (or max of both in case of 2 as modcoal was not set)
					double newCmmVer = Math.max(mod.modCMMVer, 3.0);
					ModJob job = new ModJob();
					File basegamefolder = new File(ModManager.appendSlash(mod.getModPath())+"BASEGAME");
					basegamefolder.mkdirs();
					FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME,ME3TweaksUtils.HEADER)), new File(ModManager.appendSlash(mod.getModPath())+"BASEGAME/Coalesced.bin"));
					job.addFileReplace(ModManager.appendSlash(mod.getModPath())+"BASEGAME/Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
					mod.addTask(ModType.BASEGAME, job);
					mod.modCMMVer = newCmmVer;
				}
				
				//write new moddesc.ini file
				String descini = mod.createModDescIni(mod.modCMMVer);
				ModManager.debugLogger.writeMessage("Writing new moddesc.ini with new coal modding job");
				FileUtils.writeStringToFile(new File(stagingIniPath) , descini);
				
				//reload mod in staging with new job added
				ModManager.debugLogger.writeMessage("Reloading Staging mod with new moddesc.ini file");
				mod = new Mod(stagingIniPath);

				//move folder to staging area, then upgrade to standard format
/*				File destDir = new File(ModManager.getTempDir()+mod.modName);
				ModManager.debugLogger.writeMessage("Removing existing temp dir if any: "+destDir.getAbsolutePath());
				FileUtils.deleteDirectory(destDir);
				FileUtils.moveDirectory(new File(mod.modPath), destDir);
				mod = new Mod(stagingPath+File.separator+"moddesc.ini");
				//KeybindsInjectionWindow.KeybindsInjectionWindow.this.mod = mod.createNewMod();
				FileUtils.deleteDirectory(new File(stagingPath));*/
				basegamecoal = mod.getBasegameCoalesced();
			} else {
				ModManager.debugLogger.writeMessage("Mod has coalesced job, using existing coalesced as base");
			}

			if (basegamecoal == null) {
				System.err.println("ERROR, BGC IS NULL");
				return null;
			}
			
			//decompile
			String compilingDir = ModManager.getCompilingDir();
			File coalFile = new File(compilingDir+"/coalesceds/Coalesced.bin");
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
			File modInputXML = new File(compilingDir+"coalesceds/Coalesced/BioInput.xml");
			FileUtils.copyFile(userKeybinds, modInputXML);
			ModManager.debugLogger.writeMessage("Copied user keybinds into staging: "+modInputXML.getAbsolutePath());
			stepsCompleted++;
			
			//recompile
			publish("Recompiling "+mod.getModName());
			ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerPath,
					compilingDir + "coalesceds\\Coalesced\\Coalesced.xml", "--mode=ToBin");
			//log it
			ModManager.debugLogger.writeMessage("Executing compile command: " + compilerPath +" "+
					compilingDir + "\\coalesceds\\Coalesced\\Coalesced.xml --mode=ToBin");
			compileProcessBuilder.redirectErrorStream(true);
			compileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process compileProcess = compileProcessBuilder.start();
			compileProcess.waitFor();
			stepsCompleted++;
			
			//copy back to staging
			File newCompiledCoal = new File(compilingDir + "coalesceds\\Coalesced\\Coalesced.bin");
			FileUtils.copyFile(newCompiledCoal,new File(basegamecoal));

			
			
			if (copyWholeDirectory) {
				//copy folder
				File destdir = new File(ModManager.getModsDir()+mod.getModName());
				FileUtils.deleteDirectory(destdir);
				FileUtils.copyDirectory(staging, destdir);
				ModManager.debugLogger.writeMessage("Copied updated mod back to mod directory");
			} else {
				//update bin only
				FileUtils.copyFile(newCompiledCoal,new File(finalCoalDest));
				ModManager.debugLogger.writeMessage("Copied custom keybinds Coalesced.bin back to mod directory");
			}
			FileUtils.deleteDirectory(staging);
			FileUtils.deleteDirectory(new File(compilingDir+"coalesceds/"));
			ModManager.debugLogger.writeMessage("==================END KeybindsInjectionWorker==============");

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
			try { 
	            get(); // this line can throw InterruptedException or ExecutionException
	        } 
	        catch (ExecutionException e) {
	            ModManager.debugLogger.writeMessage("Error occured in KeybindsInjectionWorker():");
	            ModManager.debugLogger.writeException(e);
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			JOptionPane.showMessageDialog(KeybindsInjectionWindow.this, "Your custom keybinds have been inserted into "+mod.getModName()+".", "Injection Complete", JOptionPane.INFORMATION_MESSAGE);
			new ModManagerWindow(false);
		}
	}
}
