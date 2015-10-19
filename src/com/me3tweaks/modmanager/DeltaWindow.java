package com.me3tweaks.modmanager;

/**
 * Delta Window applies a mod delta to a mod
 * @author Mgamerz
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModDelta;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.valueparsers.biodifficulty.Category;
import com.me3tweaks.modmanager.valueparsers.enemytype.EnemyType;
import com.me3tweaks.modmanager.valueparsers.id.ID;
import com.me3tweaks.modmanager.valueparsers.possessionwaves.Difficulty;
import com.me3tweaks.modmanager.valueparsers.sharedassignment.SharedDifficulty;
import com.me3tweaks.modmanager.valueparsers.waveclass.WaveClass;
import com.me3tweaks.modmanager.valueparsers.wavelist.Wave;

public class DeltaWindow extends JDialog {

	@SuppressWarnings("serial")
	ArrayList<String> requiredCoals = new ArrayList<String>();
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	Document doc;
	JLabel infoLabel, currentOperationLabel;
	JProgressBar overallProgress, currentStepProgress;
	private Mod mod;
	private ModDelta delta;

	/**
	 * Starts a modmaker session for a user-selected download
	 * 
	 * @param code
	 *            code to download
	 * @param languages
	 *            languages to compile
	 */
	public DeltaWindow(Mod mod, ModDelta delta) {
		this.mod = mod;
		this.delta = delta;
		setupWindow();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		new PreprocessWorker().execute();
	}

	private void setupWindow() {
		this.setTitle("Applying Delta");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(420, 167));
		this.setIconImages(ModManager.ICONS);

		JPanel deltaPanel = new JPanel();
		deltaPanel.setLayout(new BoxLayout(deltaPanel, BoxLayout.PAGE_AXIS));
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.LINE_AXIS));
		infoLabel = new JLabel("Applying delta: " + delta.getDeltaName(), SwingConstants.CENTER);
		infoPane.add(Box.createHorizontalGlue());
		infoPane.add(infoLabel);
		infoPane.add(Box.createHorizontalGlue());

		deltaPanel.add(infoPane);
		//JLabel overall = new JLabel("Overall progress");
		TitledBorder overallBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Overall Progress");
		//JLabel current = new JLabel("Current operation");
		TitledBorder currentBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Current Operation");
		currentOperationLabel = new JLabel("Preparing to apply delta", SwingConstants.CENTER);
		overallProgress = new JProgressBar(0, 100);
		overallProgress.setStringPainted(true);
		overallProgress.setIndeterminate(false);
		overallProgress.setEnabled(false);

		currentStepProgress = new JProgressBar(0, 100);
		currentStepProgress.setStringPainted(true);
		currentStepProgress.setIndeterminate(false);
		currentStepProgress.setEnabled(false);

		JPanel overallPanel = new JPanel(new BorderLayout());
		overallPanel.setBorder(overallBorder);
		overallPanel.add(overallProgress, BorderLayout.CENTER);

		deltaPanel.add(overallPanel);
		deltaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		JPanel currentPanel = new JPanel(new BorderLayout());
		currentPanel.setBorder(currentBorder);

		currentPanel.add(currentOperationLabel);
		currentPanel.add(currentStepProgress, BorderLayout.SOUTH);
		deltaPanel.add(currentPanel);
		deltaPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setResizable(false);
		this.getContentPane().add(deltaPanel);
		this.pack();
	}

	/**
	 * Prepreprocesses a delta by making sure original files are stored in the
	 * ORIGINAL directory
	 */

	class PreprocessWorker extends SwingWorker<Void, Integer> {
		private NodeList coalNodeList;

		public PreprocessWorker() {
			ModManager.debugLogger.writeMessage("============PREPROCESS DELTA==============");
			currentOperationLabel.setText("Preprocessing Delta");
		}

		protected Void doInBackground() {
			String modFolder = mod.getModPath();
			String originalsFolder = modFolder + Mod.ORIGINAL_FOLDER + File.separator;
			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();
			ArrayList<String> coalesceds = new ArrayList<String>();
			/*
			 * File originals = new File(originalsFolder); if
			 * (!originals.exists()) { originals.mkdirs(); }
			 */

			//Check for original file first. Backup if not there

			try {
				coalNodeList = ((NodeList) xPath.evaluate("/ModDelta/DeltaData", delta.getDoc().getDocumentElement(), XPathConstants.NODESET))
						.item(0).getChildNodes();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//Check for backups first
			for (int i = 0; i < coalNodeList.getLength(); i++) {
				//coalNode is a node containing the coalesced module, such as <MP1>
				Node coalNode = coalNodeList.item(i);

				if (coalNode.getNodeType() == Node.ELEMENT_NODE) {
					String intCoalName = coalNode.getNodeName(); //get the coal name so we can figure out what folder to look in.
					String jobNameToLookup = ME3TweaksUtils.internalNameToHeaderName(intCoalName);
					String coalFilename = ME3TweaksUtils.internalNameToCoalFilename(intCoalName);

					ModJob relevantjob = null;
					//CHECK FOR BACKUP. Use relative paths since user might now use standard folder names.
					for (ModJob job : mod.getJobs()) {
						if (job.getJobName().equals(jobNameToLookup)) {
							relevantjob = job;
							break;
						}
					}

					if (relevantjob == null) {
						//couldn't find file to apply delta to
						addNewError("Mod does not modify but delta does: " + jobNameToLookup);
						return null;
					}
					String jobFolder = relevantjob.getSourceDir();
					File backup = new File(originalsFolder + jobFolder + File.separator + coalFilename);
					if (!backup.exists()) {
						new File(originalsFolder + jobFolder).mkdirs();
						try {
							FileUtils.copyFile(new File(modFolder + jobFolder + File.separator + coalFilename), backup);
						} catch (IOException e) {
							ModManager.debugLogger.writeErrorWithException("Unable to copy file to backup folder:", e);
							addNewError("Unable to backup one of the original files. Check the log to see why.");
							return null;
						}
					}
					//Stage the coalesced file
					File stagedFile = new File(ModManager.getCompilingDir() + "coalesceds/" + coalFilename);
					try {
						FileUtils.copyFile(new File(ModManager.getPristineCoalesced(jobNameToLookup, ME3TweaksUtils.HEADER)), stagedFile);
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Unable to stage file:", e);
						addNewError("Unable to stage one of the coalesced files for decompiling. Check the log to see why.");
						return null;
					}
					coalesceds.add(stagedFile.getAbsolutePath());
				}
			}
			//Backups completed.

			//Decompile the staged files
			String path = ModManager.getCompilingDir();
			for (String coal : coalesceds) {
				String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";

				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(compilerPath);
				commandBuilder.add(path + "coalesceds\\" + coal); //System.out.println("Building command");
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]); //Debug stuff 
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing decompile command: " + sb.toString());

				ProcessBuilder decompileProcessBuilder = new ProcessBuilder(command);
				decompileProcessBuilder.redirectErrorStream(true);
				decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				Process decompileProcess;
				try {
					decompileProcess = decompileProcessBuilder.start();
					decompileProcess.waitFor();
				} catch (IOException | InterruptedException e) {
					ModManager.debugLogger.writeErrorWithException("Decompiling file failed:", e);
					addNewError("Decompiling one of the coalesced files failed. Check the log to see why.");
					return null;
				}
			}
			
			

			
			
			//Decompiled. Now apply edits.

			//Edits applied. Now recompile.

			return null;
		}

		private void addNewError(String string) {
			// TODO Auto-generated method stub

		}

		class CompilerWorker extends SwingWorker<Void, Integer> {
			private ArrayList<String> coalsToCompile;
			private JProgressBar progress;
			private int numCoals;

			public CompilerWorker(ArrayList<String> coalsToCompile, JProgressBar progress) {
				ModManager.debugLogger.writeMessage("==================CompilerWorker==============");
				this.coalsToCompile = coalsToCompile;
				this.numCoals = coalsToCompile.size();
				this.progress = progress;
				currentOperationLabel.setText("Recompiling " + this.coalsToCompile.get(0));
				progress.setIndeterminate(false);
				progress.setValue(0);
			}

			protected Void doInBackground() throws Exception {
				int coalsCompiled = 0;
				String path = ModManager.getCompilingDir();
				for (String coal : coalsToCompile) {
					String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";
					//ProcessBuilder compileProcessBuilder = new ProcessBuilder(
					//		compilerPath, "--xml2bin", path + "\\coalesceds\\"
					//				+ FilenameUtils.removeExtension(coal)+".xml");
					ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerPath, path + "\\coalesceds\\"
							+ FilenameUtils.removeExtension(coal) + "\\" + FilenameUtils.removeExtension(coal) + ".xml", "--mode=ToBin");
					//log it
					ModManager.debugLogger.writeMessage("Executing compile command: " + compilerPath + " " + path + "\\coalesceds\\"
							+ FilenameUtils.removeExtension(coal) + "\\" + FilenameUtils.removeExtension(coal) + ".xml --mode=ToBin");
					compileProcessBuilder.redirectErrorStream(true);
					compileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					Process compileProcess = compileProcessBuilder.start();
					compileProcess.waitFor();
					coalsCompiled++;
					this.publish(coalsCompiled);
				}
				return null;
			}

			@Override
			protected void process(List<Integer> numCompleted) {
				if (numCoals > numCompleted.get(0)) {
					currentOperationLabel.setText("Recompiling " + coalsToCompile.get(numCompleted.get(0)));
				}
				progress.setIndeterminate(false);
				progress.setValue((int) (100 / ((double) numCoals / numCompleted.get(0)) + 0.5)); //crazy rounding trick for integer.
			}

			protected void done() {
				// Coals recompiled
				try {
					get(); // this line can throw InterruptedException or ExecutionException
				} catch (ExecutionException e) {
					ModManager.debugLogger.writeMessage("Error occured in CompilerWorker():");
					ModManager.debugLogger.writeException(e);
					JOptionPane.showMessageDialog(DeltaWindow.this,
							"An error occured while trying to recompile modified coalesced xml files into a coalesced.bin file:\n" + e.getMessage()
									+ "\n\nYou should report this to FemShep via the Forums link in the help menu.", "Compiling Error",
							JOptionPane.ERROR_MESSAGE);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				/*
				 * if (error) { dispose(); return; } stepsCompleted += 2;
				 * overallProgress.setValue((int) ((100 / (TOTAL_STEPS /
				 * stepsCompleted)))); ModManager.debugLogger
				 * .writeMessage("COALS: RECOMPILED..."); new
				 * TLKWorker(progress, languages).execute();
				 */
			}
		}

		/**
		 * 
		 * /** Decompiles a coalesced into .xml files using tankmaster's tools.
		 * 
		 * @author Mgamerz
		 */

		class DecompilerWorker extends SwingWorker<Void, Integer> {
			private JProgressBar progress;

			public DecompilerWorker(ArrayList<String> coalsToDecompile, JProgressBar progress) {
				ModManager.debugLogger.writeMessage("==================DecompilerWorker==============");
				this.progress = progress;
				currentOperationLabel.setText("Decompiling " + this.coalsToDecompile.get(0));
				progress.setValue(0);
			}

			protected Void doInBackground() throws Exception {
				int coalsDecompiled = 0; 
				String path = ModManager.getCompilingDir();
				for (String coal : coalsToDecompile) {
					String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";

					ArrayList<String> commandBuilder = new ArrayList<String>();
					commandBuilder.add(compilerPath);
					commandBuilder.add(path + "coalesceds\\" + coal); //System.out.println("Building command");
					String[] command = commandBuilder.toArray(new String[commandBuilder.size()]); //Debug stuff StringBuilder sb = new
					StringBuilder();
					for (String arg : command) {
						sb.append(arg + " ");
					}
					ModManager.debugLogger.writeMessage("Executing decompile command: " + sb.toString());

					ProcessBuilder decompileProcessBuilder = new ProcessBuilder(command);
					decompileProcessBuilder.redirectErrorStream(true);
					decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					Process decompileProcess = decompileProcessBuilder.start();
					decompileProcess.waitFor();
					coalsDecompiled++;
					this.publish(coalsDecompiled);
				}
				return null;
			}

			@Override
			protected void process(List<Integer> numCompleted) {
				if (numCoals > numCompleted.get(0)) {
					currentOperationLabel.setText("Decompiling " + coalsToDecompile.get(numCompleted.get(0)));
				}
				progress.setIndeterminate(false);
				progress.setValue((int) (100 / (numCoals / (float) numCompleted.get(0))));
			}

			protected void done() { // Coals decompiled stepsCompleted++; 
				try {
					get(); // this line can throw InterruptedException or ExecutionException 
				} catch (ExecutionException e) {
					ModManager.debugLogger.writeMessage("Error occured in DecompilerWorker():");
					ModManager.debugLogger.writeException(e);
					JOptionPane.showMessageDialog(DeltaWindow.this, "An error occured while decompiling coalesced files:\n" + e.getMessage()
							+ "\n\nYou should report this to FemShep via the Forums link in the help menu.", "Compiling Error",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				} catch (InterruptedException e) { // TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				if (error) {
					dispose();
					return;
				}

				overallProgress.setValue((int) ((100 / (TOTAL_STEPS / stepsCompleted)) + 0.5));
				ModManager.debugLogger.writeMessage("COALS: DECOMPILED...");
				new MergeWorker(progress).execute();
			}
		}

		/**
		 * After coals are downloaded and decompiled, this worker is created and
		 * merges the contents of the downloaded mod into all of the decompiled
		 * json files.
		 */
		class MergeWorker extends SwingWorker<Void, Integer> {
			boolean error = false;

			public MergeWorker() {
				ModManager.debugLogger.writeMessage("=============MERGEWORKER=============");
				currentOperationLabel.setText("Applying delta...");
				currentStepProgress.setValue(0);
			}

			protected Void doInBackground() throws Exception {
				// we are going to parse the mod_data array and then look at all the
				// files in the array.
				// Haha wow this is going to be ugly.

				/*
				 * Structure of mod_data array and elements <ModInfo> <Coalesced
				 * ID> <Filename> <Properties (with path attribute)>
				 */

				//Iterate over the coalesceds.
				for (int i = 0; i < coalNodeList.getLength(); i++) {
					//coalNode is a node containing the coalesced module, such as <MP1> or <BASEGAME>
					Node coalNode = coalNodeList.item(i);
					if (coalNode.getNodeType() == Node.ELEMENT_NODE) {
						String intCoalName = coalNode.getNodeName(); //get the coal name so we can figure out what folder to look in.
						ModManager.debugLogger.writeMessage("Read coalecesed ID: " + intCoalName);
						ModManager.debugLogger.writeMessage("---------------------MODMAKER COMPILER START OF " + intCoalName
								+ "-------------------------");

						String foldername = FilenameUtils.removeExtension(ME3TweaksUtils.internalNameToCoalFilename(intCoalName));
						NodeList filesNodeList = coalNode.getChildNodes();
						for (int j = 0; j < filesNodeList.getLength(); j++) {
							Node fileNode = filesNodeList.item(j);
							if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
								//we now have a file ID such as biogame.
								//We need to load that XML file now.
								String iniFileName = fileNode.getNodeName() + ".xml";
								ModManager.debugLogger.writeMessage("Loading Coalesced XML fragment into memory: " + ModManager.getCompilingDir()
										+ "coalesceds\\" + foldername + "\\" + iniFileName);
								Document iniFile = dbFactory.newDocumentBuilder().parse(
										"file:///" + ModManager.getCompilingDir() + "coalesceds\\" + foldername + "\\" + iniFileName);
								iniFile.getDocumentElement().normalize();
								ModManager.debugLogger.writeMessage("Loaded " + iniFile.getDocumentURI() + " into memory.");
								//ModManager.printDocument(iniFile, System.out);
								NodeList assetList = iniFile.getElementsByTagName("CoalesceAsset");
								Element coalesceAsset = (Element) assetList.item(0);
								NodeList sectionsTagList = coalesceAsset.getElementsByTagName("Sections");
								Element sections = (Element) sectionsTagList.item(0);
								NodeList SectionList = sections.getElementsByTagName("Section");

								//We are now at at the "sections" array.
								//We now need to iterate over the dataElement list of properties's path attribute, and drill into this one so we know where to replace.
								NodeList mergeList = fileNode.getChildNodes();
								for (int k = 0; k < mergeList.getLength(); k++) {
									//for every property in this filenode (of the data to merge)...
									Node newproperty = mergeList.item(k);
									if (newproperty.getNodeType() == Node.ELEMENT_NODE) {
										//<Property type="2" name="defaultgravityz" path="engine.worldinfo">-50</Property>
										boolean isArrayProperty = false;
										boolean isSection = false;
										Element property = (Element) newproperty;
										String newPropName = null;
										String arrayType = null;
										String operation = null;
										String matchontype = null;
										String UE3type = null;
										String nodeName = property.getNodeName();
										switch (nodeName) {
										case "Property":
											newPropName = property.getAttribute("name");
											operation = property.getAttribute("operation");
											UE3type = property.getAttribute("type");
											System.out.println(newPropName + " is a property");

											isArrayProperty = false;
											break;
										case "ArrayProperty":
											arrayType = property.getAttribute("arraytype");
											matchontype = property.getAttribute("matchontype");
											operation = property.getAttribute("operation");
											UE3type = property.getAttribute("type");
											System.out.println("Array property");

											isArrayProperty = true;
											break;
										case "Section":

											newPropName = property.getAttribute("name");
											System.out.println(newPropName + " is a section");
											operation = property.getAttribute("operation");
											isArrayProperty = false;
											isSection = true;
											break;
										}

										if (isSection) {
											//can't drill to it.
											switch (operation) {
											case "addition":
												//adds a section
												ModManager.debugLogger.writeMessage("Creating new section: " + newPropName);
												Element newElement;
												newElement = iniFile.createElement("Section");
												newElement.setAttribute("name", newPropName);
												sections.appendChild(newElement);
												break;
											case "subtraction":
												//remove this section
												ModManager.debugLogger.writeMessage("Subtracting section: " + newPropName);
												boolean sectionFound = false;
												for (int l = 0; l < SectionList.getLength(); l++) {
													//iterate over all sections...
													Node n = SectionList.item(l); //L, not a 1.
													if (n.getNodeType() == Node.ELEMENT_NODE) {
														Element sectionElem = (Element) n;
														if (sectionElem.getAttribute("name").equals(newPropName)) {
															//this is the one to remove.
															sections.removeChild(n);
															sectionFound = true;
															break;
														}
													}
												}
												if (sectionFound) {
													continue;
												} else {
													System.err.println("SHOULDNT REACH THIS! SUBTRACT SECTION");
													continue;
												}
											case "clear":
												//gets rid of all children, leaving the node
												ModManager.debugLogger.writeMessage("Clearing section: " + newPropName);
												boolean cleared = false;
												for (int l = 0; l < SectionList.getLength(); l++) {
													//iterate over all sections...
													Node n = SectionList.item(l); //L, not a 1.
													if (n.getNodeType() == Node.ELEMENT_NODE) {
														Element sectionElem = (Element) n;
														if (sectionElem.getAttribute("name").equals(newPropName)) {
															//this is the one to remove.
															while (n.hasChildNodes()) {
																n.removeChild(n.getFirstChild());
															}
															cleared = true;
															break;
														}
													}
												}
												if (cleared) {
													continue;
												} else {
													System.err.println("SHOULDNT REACH THIS! CLEAR SECTION");
													continue;
												}
											}
											continue;
										}

										String newValue = property.getTextContent();

										//first tokenize the path...
										String path = property.getAttribute("path");
										StringTokenizer drillTokenizer = new StringTokenizer(path, "&"); // - splits this in the event we need to drill down. Spaces are valid it seems in the path.
										Element drilled = null;
										NodeList drilledList = SectionList;
										while (drillTokenizer.hasMoreTokens()) {
											//drill
											String drillTo = drillTokenizer.nextToken();
											ModManager.debugLogger.writeMessage("Drilling to find: " + drillTo);
											boolean pathfound = false;
											for (int l = 0; l < drilledList.getLength(); l++) {
												//iterate over all sections...
												Node drilledNode = drilledList.item(l); //L, not a 1.
												if (drilledNode.getNodeType() == Node.ELEMENT_NODE) {
													drilled = (Element) drilledNode;
													//ModManager.debugLogger.writeMessage("Checking attribute: "+drilled.getAttribute("name"));
													if (!drilled.getAttribute("name").equals(drillTo)) {
														continue;
													} else {
														//this is the section we want.
														ModManager.debugLogger.writeMessage("Found " + drillTo);
														drilledList = drilled.getChildNodes();
														pathfound = true;
														break;
													}
												}
											}
											if (!pathfound) {
												dispose();
												JOptionPane.showMessageDialog(null, "<html>Could not find the path " + path
														+ " to property.<br>Module: " + intCoalName + "<br>File: " + iniFileName + "</html>",
														"Compiling Error", JOptionPane.ERROR_MESSAGE);
												error = true;
												return null;
											}
										}
										if (drilled == null) {
											//we didn't find what we wanted...
											dispose();
											error = true;
											JOptionPane.showMessageDialog(null, "<html>Could not find the path " + path + " to property.<br>Module: "
													+ intCoalName + "<br>File: " + iniFileName + "</html>", "Compiling Error",
													JOptionPane.ERROR_MESSAGE);
											return null;
										}
										if (operation.equals("addition")) {
											//only for arrays
											//we won't find anything to match, since it obviously can't exist. Add it from here.
											ModManager.debugLogger.writeMessage("Creating new property with operation ADDITION");
											Element newElement;
											if (isArrayProperty) {
												newElement = drilled.getOwnerDocument().createElement("Value");
											} else {
												newElement = drilled.getOwnerDocument().createElement("Property");
												newElement.setAttribute("name", newPropName);
											}
											if (UE3type != null && !UE3type.equals("")) {
												newElement.setAttribute("type", UE3type);
											}
											newElement.setTextContent(newValue);
											drilled.appendChild(newElement);
											continue; //continue property loop
										}

										//we've drilled down as far as we can.

										//we are where we want to be. Now we can set the property or array value.
										//drilled is the element (parent of our property) that we want.
										NodeList props = drilled.getChildNodes(); //get children of the path (<property> list)
										ModManager.debugLogger.writeMessage("Number of child property/elements to search: " + props.getLength());
										boolean foundProperty = false;
										for (int m = 0; m < props.getLength(); m++) {
											Node propertyNode = props.item(m);
											if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
												Element itemToModify = (Element) propertyNode;
												//Check on property
												if (!isArrayProperty) {
													//property
													boolean shouldBreak = false;
													switch (operation) {
													case "assignment":
														if (itemToModify.getAttribute("name").equals(newPropName)) {
															itemToModify.setTextContent(newValue);
															ModManager.debugLogger.writeMessage("Assigning " + newPropName + " to " + newValue);
															foundProperty = true;
															shouldBreak = true;
														}
														break;
													case "subtraction":
														if (itemToModify.getAttribute("name").equals(newPropName)) {
															ModManager.debugLogger.writeMessage("Subtracting property " + newPropName);
															Node itemParent = itemToModify.getParentNode();
															itemParent.removeChild(itemToModify);
															foundProperty = true;
															shouldBreak = true;
															break;
														}
													}
													if (shouldBreak) {
														break;
													}
												} else {
													//Check on ArrayProperty
													//ModManager.debugLogger.writeMessage("Candidates only will be returned if they are of type: "+matchontype);
													//ModManager.debugLogger.writeMessage("Scanning property type: "+itemToModify.getAttribute("type"));
													if (itemToModify.getAttribute("type").equals(matchontype)) {
														//potential array value candidate...
														boolean match = false;
														ModManager.debugLogger.writeMessage("Found type candidate (" + matchontype
																+ ") for arrayreplace: " + itemToModify.getTextContent());
														switch (arrayType) {
														//Must use individual matching algorithms so we can figure out if something matches.
														case "exactvalue": {
															if (itemToModify.getTextContent().equals(newValue)) {
																ModManager.debugLogger.writeMessage("exact Property match found.");
																match = true;
															}
														}
															break;
														case "biodifficulty": {
															//Match on Category (name)
															Category existing = new Category(itemToModify.getTextContent());
															Category importing = new Category(newValue);
															if (existing.matchIdentifiers(importing)) {
																ModManager.debugLogger.writeMessage("Match found: " + existing.categoryname);
																existing.merge(importing);
																newValue = existing.createCategoryString();
																match = true;
															} else {
																ModManager.debugLogger.writeMessage("Match failed: " + existing.categoryname);
															}
														}
															break;
														case "wavelist": {
															//Match on Difficulty
															Wave existing = new Wave(itemToModify.getTextContent());
															Wave importing = new Wave(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
																ModManager.debugLogger.writeMessage("Wavelist match on " + existing.difficulty);
																newValue = importing.createWaveString(); //doens't really matter, but makes me feel good my code works
															} else {
																//CHECK FOR COLLECTOR PLAT WAVE 5.
																String cplatwave5 = "(Difficulty=DO_Level3,Enemies=( (EnemyType=\"WAVE_COL_Scion\"), (EnemyType=\"WAVE_COL_Praetorian\", MinCount=1, MaxCount=1), (EnemyType=\"WAVE_CER_Phoenix\", MinCount=2, MaxCount=2), (EnemyType=\"WAVE_CER_Phantom\", MinCount=3, MaxCount=3) ))";
																/*
																 * if (path .
																 * equals (
																 * "sfxwave_horde_collector5 sfxwave_horde_collector&enemies"
																 * ) &&
																 * importing .
																 * difficulty .
																 * equals (
																 * "DO_Level3"
																 * )) { System
																 * .out. println
																 * ( "BREAK" );
																 * 
																 * }
																 */
																//System.out.println(itemToModify.getTextContent());
																if (itemToModify.getTextContent().equals(cplatwave5)
																		&& path.equals("sfxwave_horde_collector5 sfxwave_horde_collector&enemies")
																		&& importing.difficulty.equals("DO_Level4")) {
																	match = true;
																	newValue = importing.createWaveString(); //doens't really matter, but makes me feel good my code works
																}
															}
														}
															break;
														case "possessionwaves": {
															//Match on Difficulty/DoLevel
															//Match on Difficulty
															Difficulty existing = new Difficulty(itemToModify.getTextContent());
															Difficulty importing = new Difficulty(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
																//newValue = importing.createDifficultyString(); //doens't really matter, but makes me feel good my code works
																//and it was broken
															}
														}
															break;
														case "shareddifficulty":
														case "wavebudget": {
															//Match on SharedDifficulty (DO_Level)
															SharedDifficulty existing = new SharedDifficulty(itemToModify.getTextContent());
															SharedDifficulty importing = new SharedDifficulty(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
															}
														}
															break;
														case "enemytype":
														case "wavecost": { //wavecost is old name for enemytype (modmaker 1.6)
															EnemyType existing = new EnemyType(itemToModify.getTextContent());
															EnemyType importing = new EnemyType(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
															}
														}
															break;
														case "waveclass": {
															WaveClass existing = new WaveClass(itemToModify.getTextContent());
															WaveClass importing = new WaveClass(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
															}
														}
															break;
														case "id": {
															ID existing = new ID(itemToModify.getTextContent());
															ID importing = new ID(newValue);
															if (existing.matchIdentifiers(importing)) {
																match = true;
															}
														}
															break;
														default:
															ModManager.debugLogger.writeError("ERROR: Unknown matching algorithm: " + arrayType
																	+ ". does this client need updated? Aborting this stat update.");
															JOptionPane
																	.showMessageDialog(
																			null,
																			"<html>Unknown matching algorithm from ME3Tweaks: "
																					+ arrayType
																					+ ".<br>You should check for updates to Mod Manager.<br>This mod will not fully compile.</html>",
																			"Compiling Error", JOptionPane.ERROR_MESSAGE);
															break;
														} //end matching algorithm switch
														if (match) {
															foundProperty = true;
															switch (operation) {
															case "subtraction":
																Node itemParent = itemToModify.getParentNode();
																itemParent.removeChild(itemToModify);
																ModManager.debugLogger.writeMessage("Removed array value: " + newValue);
																break;
															case "modify": //same as assignment right now
															case "assignment":
																itemToModify.setTextContent(newValue);
																ModManager.debugLogger.writeMessage("Assigned array value: " + newValue);
																break;
															default:
																ModManager.debugLogger.writeMessage("ERROR: Unknown matching algorithm: " + arrayType
																		+ " does this client need updated? Aborting this stat update.");
																JOptionPane
																		.showMessageDialog(
																				null,
																				"<html>Unknown operation from ME3Tweaks: "
																						+ operation
																						+ ".<br>You should check for updates to Mod Manager.<br>This mod will not fully compile.</html>",
																				"Compiling Error", JOptionPane.ERROR_MESSAGE);
																break;
															} //end operation [switch]
															break;
														} //end of match = true [if]
													} //end of array matchontype check [if]
												} //end of array property [if]
											} //end of property = element node (not junk) [if]
										} //end of props.length to search through. [for loop]
										if (foundProperty != true) {
											StringBuilder sb = new StringBuilder();
											sb.append("<html>Could not find the following attribute:<br>");
											sb.append("Coalesced File: ");
											sb.append(intCoalName);
											sb.append("<br>");
											sb.append("Subfile: ");
											sb.append(fileNode.getNodeName());
											sb.append("<br>");

											sb.append("Path: ");
											sb.append(path);
											sb.append("<br>");
											sb.append("Operation: ");
											sb.append(operation);
											sb.append("<br>");
											if (isArrayProperty) {
												sb.append("====ARRAY ATTRIBUTE INFO=======<br>");
												sb.append("Array matching algorithm: ");
												sb.append(arrayType);
												sb.append("<br>Matching type: ");
												sb.append(matchontype);
											} else {
												sb.append("====STANDARD ATTRIBUTE INFO====<br>");
												sb.append("Keyed Property Name: ");
												sb.append(newPropName);
											}
											sb.append("<br>=================");
											sb.append("<br>");
											sb.append("New data: ");
											sb.append(newValue);
											sb.append("</html>");

											JOptionPane.showMessageDialog(null, sb.toString(), "Compiling Error", JOptionPane.ERROR_MESSAGE);
											ModManager.debugLogger.writeMessage(sb.toString());
										}
									}
								}
								//end of the file node.
								//Time to save the file...
								Transformer transformer = TransformerFactory.newInstance().newTransformer();
								transformer.setOutputProperty(OutputKeys.INDENT, "yes");
								transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
								File outputFile = new File(ModManager.getCompilingDir() + "coalesceds\\" + foldername + "\\" + iniFileName);
								Result output = new StreamResult(outputFile);
								Source input = new DOMSource(iniFile);
								ModManager.debugLogger.writeMessage("Saving file: " + outputFile.toString());
								transformer.transform(input, output);
							}
						}
					}
				}

				return null;
			}

			protected void done() {
				// Merge thread finished
				try {
					get(); // this line can throw InterruptedException or ExecutionException
				} catch (ExecutionException e) {
					ModManager.debugLogger.writeMessage("Error occured in MergeWorker():");
					ModManager.debugLogger.writeException(e);
					JOptionPane.showMessageDialog(DeltaWindow.this,
							"An error occured while trying to merge mod delta into coalesced files:\n" + e.getMessage()
									+ "\n\nYou should report this to FemShep via the Forums link in the help menu.", "Compiling Error",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				if (error) {
					dispose();
					return;
				}
				ModManager.debugLogger.writeMessage("Finished merging coals.");

				/*
				 * stepsCompleted++; overallProgress.setValue((int) ((100 /
				 * (TOTAL_STEPS / stepsCompleted)) + 0.5)); new
				 * CompilerWorker(requiredCoals, progress).execute();
				 */
			}
		}
	}

	/*
	 * public void finishModMaker(Mod newMod) { overallProgress.setValue(95); if
	 * (ModManager.AUTO_INJECT_KEYBINDS && hasKeybindsOverride()) {
	 * ModManager.debugLogger.writeMessage(
	 * "Mod Manager has preference to auto install keybinds and keybinds override file is present."
	 * ); new KeybindsInjectionWindow(ModManagerWindow.ACTIVE_WINDOW, newMod,
	 * true); overallProgress.setValue(98); }
	 * ModManager.debugLogger.writeMessage("Running AutoTOC on new mod: " +
	 * modName); new AutoTocWindow(newMod, AutoTocWindow.LOCALMOD_MODE,
	 * ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
	 * overallProgress.setValue(100); stepsCompleted++;
	 * ModManager.debugLogger.writeMessage("Mod successfully created:" +
	 * modName);
	 * ModManager.debugLogger.writeMessage("===========END OF MODMAKER========"
	 * ); //Mod Created! dispose(); if (mod == null) { //updater supresses this
	 * window JOptionPane.showMessageDialog(this, modName +
	 * " was successfully created!", "Mod Created",
	 * JOptionPane.INFORMATION_MESSAGE); new ModManagerWindow(false); } }
	 */

	/*
	 * public static String docToString(Document doc) { try { StringWriter sw =
	 * new StringWriter(); TransformerFactory tf =
	 * TransformerFactory.newInstance(); Transformer transformer =
	 * tf.newTransformer();
	 * transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	 * transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	 * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	 * transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	 * 
	 * transformer.transform(new DOMSource(doc), new StreamResult(sw)); return
	 * sw.toString(); } catch (Exception ex) { throw new
	 * RuntimeException("Error converting to String", ex); } }
	 */

	public String coalToTOCString(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "MP1 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP2.bin":
			return "MP2 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP3.bin":
			return "MP3 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP4.bin":
			return "MP4 PCConsoleTOC.bin";
		case "Default_DLC_CON_MP5.bin":
			return "MP5 PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1 PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2 PCConsoleTOC.bin";
		case "Coalesced.bin":
			return "BASEGAME PCConsoleTOC.bin";
		default:
			return null;
		}
	}
}
