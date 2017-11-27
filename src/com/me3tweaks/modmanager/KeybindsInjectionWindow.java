package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * This window injects a user's customized set of keybinds into the base
 * coalesced file by swapping the BioInput.xml file after decompilation
 * 
 * @author mgamerz
 *
 */
public class KeybindsInjectionWindow extends JDialog {
	private JProgressBar progressbar;
	private JLabel infoLabel;
	private Mod mod;
	private int TOTAL_STEPS = 3;
	private boolean automated;

	public KeybindsInjectionWindow(JFrame callingWindow, Mod mod, boolean automated) {
		this.mod = mod;
		this.automated = automated;
		setupWindow();
		setLocationRelativeTo(callingWindow);
		new KeybindsInjectionWorker().execute();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("Custom Keybinds Injector");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(380, 90));
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);

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
		getContentPane().add(bindingsPanel);
		pack();
	}

	class KeybindsInjectionWorker extends SwingWorker<Boolean, String> {
		private int stepsCompleted = 0;
		private boolean talonremoved = false;

		public KeybindsInjectionWorker() {
			ModManager.debugLogger.writeMessage("==================KeybindsInjectionWorker==============");
			infoLabel.setText("Preparing to decompile " + mod.getModName());
			progressbar.setValue(0);
		}

		protected Boolean doInBackground() throws Exception {
			File userKeybinds = new File(ModManager.getOverrideDir() + "BioInput.xml");

			String destinationBasegamecoal = mod.getBasegameCoalesced(); //points to real copy (null if not already in a job)
			String destinationMP5coal = mod.getModTaskPath("/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin", ModTypeConstants.MP5); //points to real copy
			Mod origMod = mod;
			//copy mod to staging
			boolean BGcopyWholeDirectory = false;
			boolean MP5copyWholeDirectory = false;

			//String finalCoalDest = mod.getBasegameCoalesced(); //points to staging
			String stagingPath = ModManager.getTempDir() + mod.getModName() + "/";
			String stagingIniPath = stagingPath + "moddesc.ini";
			File staging = new File(stagingPath);
			ModManager.debugLogger.writeMessage("Removing existing temp dir if any: " + staging.getAbsolutePath());
			FileUtils.deleteDirectory(staging);
			ModManager.debugLogger.writeMessage("Copying mod to staging directory");
			FileUtils.copyDirectory(new File(mod.getModPath()), staging);
			ModManager.debugLogger.writeMessage("Reloading mod in staging area");
			mod = new Mod(stagingIniPath);
			if (!mod.isValidMod()) {
				ModManager.debugLogger.writeError("Mod in staging is not valid!");
				return false;
			}

			//check if coal job exists first, and if we have a pristine one (or need to DL one)
			//ModJob basegameJob = mod.getJobByModuleName(ModType.BASEGAME);
			String basegamecoalstaging = mod.getBasegameCoalesced(); //points to staging
			if (basegamecoalstaging == null) {
				BGcopyWholeDirectory = true;
				ModManager.debugLogger.writeMessage("Mod does not appear to mod Coalesced.bin, performing prerequesite changes");
				if (!ModManager.hasPristineCoalesced(ModTypeConstants.BASEGAME, ME3TweaksUtils.HEADER)) {
					publish("Getting pristine Coalesced.bin file");
					ME3TweaksUtils.downloadPristineCoalesced(ModTypeConstants.BASEGAME, ME3TweaksUtils.HEADER);
				} else {
					ModManager.debugLogger.writeMessage("Mod already modifies basegame Coalesced, using that one instead of vanilla one");

				}
				//String pristineBase = ModManager.getPristineCoalesced(ModType.BASEGAME);
				ModJob basegamejob = null;
				//check if it has basegame modjob
				for (ModJob job : mod.jobs) {
					if (job.getJobType() == ModJob.BASEGAME) {
						basegamejob = job;
						String jobFolder = ModManager.appendSlash(new File(job.getFilesToReplace().get(0)).getParentFile().getAbsolutePath());
						String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));
						//System.out.println(relativepath);
						FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModTypeConstants.BASEGAME, ME3TweaksUtils.HEADER)),
								new File(ModManager.appendSlash(mod.getModPath()) + relativepath + "Coalesced.bin"));
						job.addFileReplace(mod.getModPath() + relativepath + "Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin", false);
						destinationBasegamecoal = ModManager.appendSlash(mod.getModPath()) + relativepath + "Coalesced.bin";
						break;
					}
				}

				if (basegamejob == null) {
					//no basegame header, but has tasks, and does not mod coal
					//means it doesn't modify basegame files at all so we can just add the header and set modver to 3 (or max of both in case of 2 as modcoal was not set)
					ModJob job = new ModJob();
					job.setOwningMod(mod);
					File basegamefolder = new File(ModManager.appendSlash(mod.getModPath()) + "BASEGAME");
					basegamefolder.mkdirs();
					FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModTypeConstants.BASEGAME, ME3TweaksUtils.HEADER)),
							new File(ModManager.appendSlash(mod.getModPath()) + "BASEGAME/Coalesced.bin"));
					destinationBasegamecoal = mod.getModPath() + "BASEGAME/Coalesced.bin";
					job.addFileReplace(ModManager.appendSlash(mod.getModPath()) + "BASEGAME/Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin", false);

					//Add TOC
					if (!ModManager.hasPristineTOC("BASEGAME", ME3TweaksUtils.HEADER)) {
						ME3TweaksUtils.downloadPristineTOC("BASEGAME", ME3TweaksUtils.HEADER);
					}

					File destTOC = new File(ModManager.appendSlash(mod.getModPath()) + "BASEGAME/PCConsoleTOC.bin");
					FileUtils.copyFile(new File(ModManager.getPristineTOC("BASEGAME", ME3TweaksUtils.HEADER)), destTOC);
					job.addFileReplace(ModManager.appendSlash(mod.getModPath()) + "BASEGAME/PCConsoleTOC.bin", "\\BIOGame\\PCConsoleTOC.bin", false);
					mod.addTask("BASEGAME", job);
				}

				//write new moddesc.ini file
				String descini = mod.createModDescIni(true, mod.modCMMVer);
				ModManager.debugLogger.writeMessage("Writing new moddesc.ini with new coal modding job");
				FileUtils.writeStringToFile(new File(stagingIniPath), descini);

				//reload mod in staging with new job added
				ModManager.debugLogger.writeMessage("Reloading Staging mod with new moddesc.ini file");
				mod = new Mod(stagingIniPath);
				basegamecoalstaging = mod.getBasegameCoalesced();
			} else {
				ModManager.debugLogger.writeMessage("Mod has coalesced job, using existing coalesced as base");
			}

			if (basegamecoalstaging == null) {
				ModManager.debugLogger.writeError("ERROR, Basegame coal is NULL! Could not source a basegame coalesced to use.");
				return false;
			}

			//MP5 TALON MERC FIX
			//detect if talon keybinds are present int our user keybinds
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			Document iniFile = dbFactory.newDocumentBuilder().parse("file:///" + userKeybinds.getAbsolutePath());
			iniFile.getDocumentElement().normalize();
			NodeList sectionsList = iniFile.getElementsByTagName("Section");
			for (int x = 0; x < sectionsList.getLength(); x++) {
				Node n = sectionsList.item(x);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					String name = n.getAttributes().getNamedItem("name").getTextContent();
					if (name.contains("sfxgame.sfxgamemodedefault_merc")) {
						talonremoved = true;
						ModManager.debugLogger.writeMessage("Detected talon keybindings in user keybinds");
						TOTAL_STEPS = 6;
						break;
					}
				}
			}
			String mp5coalstaging = null;
			if (talonremoved) {
				//check if coal job exists first, and if we have a pristine one (or need to DL one)
				mp5coalstaging = mod.getModTaskPath("/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin", ModTypeConstants.MP5); //points to staging
				if (mp5coalstaging == null) {
					MP5copyWholeDirectory = true;
					ModManager.debugLogger.writeMessage("Mod does not appear to mod MP5 Coalesced.bin, performing prerequesite changes");
					if (!ModManager.hasPristineCoalesced(ModTypeConstants.MP5, ME3TweaksUtils.HEADER)) {
						publish("Getting pristine Default_DLC_CON_MP5.bin file");
						ME3TweaksUtils.downloadPristineCoalesced(ModTypeConstants.MP5, ME3TweaksUtils.HEADER);
					}
					ModJob mp5job = null;
					//check if it has mp5 modjob
					for (ModJob job : mod.jobs) {
						if (job.getJobType() == ModJob.DLC && job.getJobName().equals(ModTypeConstants.MP5)) {
							mp5job = job;
							String jobFolder = ModManager.appendSlash(new File(job.getFilesToReplace().get(0)).getParentFile().getAbsolutePath());
							String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));
							//System.out.println(relativepath);
							FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModTypeConstants.MP5, ME3TweaksUtils.HEADER)),
									new File(ModManager.appendSlash(mod.getModPath()) + relativepath + "Default_DLC_CON_MP5.bin"));
							destinationMP5coal = origMod.getModPath() + relativepath + "Default_DLC_CON_MP5.bin";
							job.addFileReplace(mod.getModPath() + relativepath + "Default_DLC_CON_MP5.bin", "/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin",
									false);
							break;
						}
					}

					if (mp5job == null) {
						//no mp5 header, but has tasks, and does not mod coal
						//means it doesn't modify mp5 files at all so we can just add the header and set modver to 3 (or max of both in case of 2 as modcoal was not set)
						ModJob job = new ModJob(ModTypeConstants.getDLCPath(ModTypeConstants.MP5), ModTypeConstants.MP5, "Required to fix Talon Mercenary Keybinds");
						job.setOwningMod(mod);
						File mp5folder = new File(ModManager.appendSlash(mod.getModPath()) + "MP5");
						mp5folder.mkdirs();
						FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModTypeConstants.MP5, ME3TweaksUtils.HEADER)),
								new File(ModManager.appendSlash(mod.getModPath()) + "MP5/Default_DLC_CON_MP5.bin"));
						job.addFileReplace(ModManager.appendSlash(mod.getModPath()) + "MP5/Default_DLC_CON_MP5.bin",
								"/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin", false);
						destinationMP5coal = origMod.getModPath() + "MP5/Default_DLC_CON_MP5.bin";

						//Add TOC
						if (!ModManager.hasPristineTOC(ModTypeConstants.MP5, ME3TweaksUtils.HEADER)) {
							ME3TweaksUtils.downloadPristineTOC(ModTypeConstants.MP5, ME3TweaksUtils.HEADER);
						}

						File destTOC = new File(ModManager.appendSlash(mod.getModPath()) + "MP5/PCConsoleTOC.bin");
						FileUtils.copyFile(new File(ModManager.getPristineTOC(ModTypeConstants.MP5, ME3TweaksUtils.HEADER)), destTOC);
						job.addFileReplace(ModManager.appendSlash(mod.getModPath()) + "MP5/PCConsoleTOC.bin", "/BIOGame/DLC/DLC_CON_MP5/PCConsoleTOC.bin", false);

						mod.addTask(ModTypeConstants.MP5, job);
					}
					double newCmmVer = Math.max(mod.modCMMVer, 3.0);
					mod.modCMMVer = newCmmVer;

					//write new moddesc.ini file
					String descini = mod.createModDescIni(true, mod.modCMMVer);
					ModManager.debugLogger.writeMessage("Writing new moddesc.ini with new coal modding job");
					FileUtils.writeStringToFile(new File(stagingIniPath), descini);

					//reload mod in staging with new job added
					ModManager.debugLogger.writeMessage("Reloading Staging mod with new moddesc.ini file");
					mod = new Mod(stagingIniPath);
					mp5coalstaging = mod.getModTaskPath("/BIOGame/DLC/DLC_CON_MP5/CookedPCConsole/Default_DLC_CON_MP5.bin", ModTypeConstants.MP5); //points to staging
				} else {
					ModManager.debugLogger.writeMessage("Mod has MP5 Coalesced job, using existing coalesced as base");
				}
			}
			//decompile==========================================
			String compilingDir = ModManager.getCompilingDir();
			File basegamecoalFile = new File(compilingDir + "/coalesceds/Coalesced.bin");
			File mp5coalFile = new File(compilingDir + "/coalesceds/Default_DLC_CON_MP5.bin");
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression pathExpr = null;
			FileUtils.copyFile(new File(basegamecoalstaging), basegamecoalFile);

			String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";
			ProcessBuilder decompileProcessBuilder = new ProcessBuilder(compilerPath, basegamecoalFile.getAbsolutePath());
			decompileProcessBuilder.redirectErrorStream(true);
			decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			ModManager.runProcess(decompileProcessBuilder);

			stepsCompleted++;

			//replace file
			publish("Installing custom keybinds");

			File modInputXML = new File(compilingDir + "coalesceds/Coalesced/BioInput.xml");
			FileUtils.copyFile(userKeybinds, modInputXML);
			ModManager.debugLogger.writeMessage("Copied user keybinds into staging: " + modInputXML.getAbsolutePath());
			stepsCompleted++;

			//recompile
			publish("Recompiling BASEGAME " + mod.getModName() + " with new keybinds");
			ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerPath, compilingDir + "coalesceds\\Coalesced\\Coalesced.xml", "--mode=ToBin");
			ModManager.runProcess(compileProcessBuilder);
			stepsCompleted++;

			//copy back to staging
			File newCompiledCoal = new File(compilingDir + "coalesceds\\Coalesced\\Coalesced.bin");
			FileUtils.copyFile(newCompiledCoal, new File(basegamecoalstaging));
			ModManager.debugLogger.writeMessage("Copied new coalesced file back to staging " + newCompiledCoal + " => " + basegamecoalstaging);

			if (BGcopyWholeDirectory) {
				//copy folder
				File destdir = new File(origMod.getModPath());
				FileUtils.deleteDirectory(destdir);
				FileUtils.copyDirectory(staging, destdir);
				ModManager.debugLogger.writeMessage("Copied updated mod back to mod directory " + staging + " => " + destdir);
			} else {
				//update bin only
				FileUtils.copyFile(new File(basegamecoalstaging), new File(destinationBasegamecoal));
				ModManager.debugLogger.writeMessage("Copied custom keybinds Coalesced.bin back to mod directory " + basegamecoalstaging + " => " + destinationBasegamecoal);
			}
			stepsCompleted++;

			//REMOVE TALON KEYBINDINGS...====================================================================
			if (talonremoved) {
				publish("Decompiling MP5...");
				FileUtils.copyFile(new File(mp5coalstaging), mp5coalFile);
				decompileProcessBuilder = new ProcessBuilder(compilerPath, mp5coalFile.getAbsolutePath());
				decompileProcessBuilder.redirectErrorStream(true);
				decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				ModManager.runProcess(decompileProcessBuilder);
				stepsCompleted++;
				publish("Removing Talon Keybinds from MP5 BioInput");

				iniFile = dbFactory.newDocumentBuilder().parse("file:///" + ModManager.getCompilingDir() + "coalesceds\\Default_DLC_CON_MP5\\Default_DLC_CON_MP5.xml");
				iniFile.getDocumentElement().normalize();
				ModManager.debugLogger.writeMessage("Loaded " + iniFile.getDocumentURI() + " into memory.");

				//Remove BioInput from the manifest

				try {
					pathExpr = xpath.compile("/CoalesceFile/Assets/Asset[@source=\"BioInput.xml\"]");
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				Node node = null;
				try {
					node = (Node) pathExpr.evaluate(iniFile, XPathConstants.NODE);
					if (node != null) {
						node.getParentNode().removeChild(node);
						ModManager.debugLogger.writeMessage("Removed BioInput.xml from MP5 coalesced manifest.");
					} else {
						ModManager.debugLogger.writeError("The node to remove from the MP5 manifest is null, cannot remove bioinput. Check the expression code.");
					}
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
				File outputFile = new File(ModManager.getCompilingDir() + "coalesceds\\Default_DLC_CON_MP5\\Default_DLC_CON_MP5.xml");
				Result output = new StreamResult(outputFile);
				Source input = new DOMSource(iniFile);
				ModManager.debugLogger.writeMessage("Saving file: " + outputFile.toString());
				transformer.transform(input, output);
				stepsCompleted++;

				//recompile
				publish("Recompiling MP5 " + mod.getModName() + " with no talon keybinds");
				ModManager.debugLogger.writeMessage("Recompiling MP5 Coalesced");
				compileProcessBuilder = new ProcessBuilder(compilerPath, compilingDir + "coalesceds\\Default_DLC_CON_MP5\\Default_DLC_CON_MP5.xml", "--mode=ToBin");
				ModManager.runProcess(compileProcessBuilder);
				stepsCompleted++;

				//copy back to staging
				File newCompiledMP5Coal = new File(compilingDir + "coalesceds\\Default_DLC_CON_MP5\\Default_DLC_CON_MP5.bin");
				FileUtils.copyFile(newCompiledMP5Coal, new File(mp5coalstaging));
				ModManager.debugLogger.writeMessage("Copied new MP5 coalesced file back to staging " + newCompiledMP5Coal + " => " + mp5coalstaging);

				if (MP5copyWholeDirectory) {
					//copy folder
					File destdir = new File(origMod.getModPath());
					FileUtils.deleteDirectory(destdir);
					FileUtils.copyDirectory(staging, destdir);
					ModManager.debugLogger.writeMessage("Copied removed-talon keybinds folder back to mod directory " + staging + " => " + destdir);
				} else {
					//update bin only
					FileUtils.copyFile(new File(mp5coalstaging), new File(destinationMP5coal)); //shouldn't assume a user dir, but... eh...
					ModManager.debugLogger
							.writeMessage("Copied removed-talon MP5 keybinds Default_DLC_CON_MP5.bin back to mod directory " + mp5coalstaging + " => " + destinationBasegamecoal);
				}

				//END MP5
			}

			FileUtils.deleteDirectory(staging);
			FileUtils.deleteDirectory(new File(compilingDir + "coalesceds/"));
			ModManager.debugLogger.writeMessage("==================END KeybindsInjectionWorker==============");
			mod = new Mod(origMod.getDescFile());
			return true;
		}

		@Override
		protected void process(List<String> status) {
			//System.out.println("Steps completed: " + stepsCompleted + "/" + TOTAL_STEPS);
			infoLabel.setText(status.get(status.size() - 1));
			progressbar.setIndeterminate(false);
			progressbar.setValue((int) (100 / (TOTAL_STEPS / (float) stepsCompleted)));
		}

		protected void done() {
			dispose();
			boolean result = false;
			try {
				result = get(); // this line can throw InterruptedException or ExecutionException
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeError("Error occured in KeybindsInjectionWorker():");
				ModManager.debugLogger.writeException(e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			if (!automated || !result) {
				if (result) {
					new AutoTocWindow(mod,AutoTocWindow.LOCALMOD_MODE,ModManagerWindow.GetBioGameDir());
					String removedTalonStr = "";
					if (talonremoved) {
						removedTalonStr = "\nKeybindings for the Talon Mercenary have been removed from Reckoning as they were detected in your override file.";
					}
					JOptionPane.showMessageDialog(KeybindsInjectionWindow.this, "Your custom keybinds have been inserted into " + mod.getModName() + "." + removedTalonStr,
							"Injection Complete", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(KeybindsInjectionWindow.this,
							"An error occured inserting your keybinds into into " + mod.getModName() + ".\nThe Mod Manager log will have more information.", "Injection Failed",
							JOptionPane.ERROR_MESSAGE);
				}
				ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
				ModManagerWindow.ACTIVE_WINDOW.highlightMod(mod);
			}
		}
	}
}
