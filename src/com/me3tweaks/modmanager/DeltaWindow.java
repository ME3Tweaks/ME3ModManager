package com.me3tweaks.modmanager;

/**
 * Delta Window applies a mod delta to a mod
 * @author Mgamerz
 *
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
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
import org.xml.sax.SAXException;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModDelta;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ProcessResult;
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
	private Mod mod;
	private ModDelta delta;
	private boolean deleteOnFailedVerify;

	/**
	 * Starts a delta application window for the specified mod, with the
	 * specified delta
	 * 
	 * @param mod
	 *            mod to apply delta to
	 * @param delta
	 *            delta to apply
	 * @param verifyOnly
	 *            Setting this to true will not show any prompts until the end.
	 *            If everything is OK (this variant can be properly applied)
	 *            nothing will show up, otherwise it will throw a dialog.
	 */
	public DeltaWindow(Mod mod, ModDelta delta, boolean verifyOnly, boolean deleteOnFailedVerify) {
		this.mod = mod;
		this.delta = delta;
		this.deleteOnFailedVerify = deleteOnFailedVerify;
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		if (!verifyOnly) {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Switching to " + delta.getDeltaName() + " variant");
		}
		new DeltaWorker(verifyOnly).execute();
		if (verifyOnly) {
			setupWindow();
			setVisible(true);
		}
	}

	private void setupWindow() {
		this.setTitle("Variant Manager");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel(new BorderLayout());
		JLabel operationLabel = new JLabel("<html>Verifying variant for " + mod.getModName() + "<br>" + delta.getDeltaName() + "</html>");
		panel.add(operationLabel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	/**
	 * Starts the delta window so you can restore the original variant
	 * 
	 * @param mod
	 *            Mod to restore to original
	 */
	public DeltaWindow(Mod mod) {
		boolean switched = applyVariant(mod.getModPath(), mod.getModPath() + Mod.VARIANT_FOLDER + File.separator + Mod.ORIGINAL_FOLDER);
		if (switched) {
			new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.GetBioGameDir());
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Reverted to original version of " + mod.getModName());
			ModManager.debugLogger.writeMessage("Completed mod delta reversion");
		} else {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Did not switch to original version of " + mod.getModName());
		}
	}

	/**
	 * Processes a delta and applies it. If a delta is already compiled it will
	 * use that one instead
	 */

	class DeltaWorker extends SwingWorker<Boolean, Integer> {
		private NodeList coalNodeList;
		private ArrayList<String> errors;
		private boolean verifyOnly;

		public DeltaWorker(boolean verifyOnly) {
			this.verifyOnly = verifyOnly;
			errors = new ArrayList<String>();
			ModManager.debugLogger.writeMessage("============PROCESSING DELTAWORKER() for " + delta.getDeltaName() + "==============");
			ModManager.debugLogger.writeMessage("Running in " + (verifyOnly ? "verify only" : "apply") + " mode");
			performCleanup();
		}

		protected Boolean doInBackground() {
			String modFolder = mod.getModPath();
			String originalsFolder = modFolder + Mod.VARIANT_FOLDER + File.separator + Mod.ORIGINAL_FOLDER + File.separator;
			String variantsFolder = modFolder + Mod.VARIANT_FOLDER + File.separator;

			ModManager.debugLogger.writeMessage("Originals Folder: " + originalsFolder);
			ModManager.debugLogger.writeMessage("Variant Folder: " + variantsFolder);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();
			ArrayList<String> coalesceds = new ArrayList<String>();

			//Check for original file first. Backup if not there
			try {
				coalNodeList = ((NodeList) xPath.evaluate("/ModDelta/DeltaData", delta.getDoc().getDocumentElement(), XPathConstants.NODESET)).item(0).getChildNodes();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Error getting list of delta modules:", e);
				JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "<html>Parsing the delta failed. This delta is not valid.</html>", "Delta Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
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
						return false;
					}
					String jobFolder = relevantjob.getSourceDir();
					File backup = new File(originalsFolder + jobFolder + File.separator + coalFilename);
					if (!backup.exists()) {
						new File(originalsFolder + jobFolder).mkdirs();
						ModManager.debugLogger.writeMessage("Creating ORIGINAL backup folder: " + originalsFolder + jobFolder);

						try {
							FileUtils.copyFile(new File(modFolder + jobFolder + File.separator + coalFilename), backup);
						} catch (IOException e) {
							ModManager.debugLogger.writeErrorWithException("Unable to copy file to backup folder:", e);
							addNewError("Unable to backup one of the original files. Check the log to see why.");
							return false;
						}
					}
					//Stage the coalesced file
					ModManager.debugLogger.writeMessage("Staging file: " + backup.getAbsolutePath() + " => " + ModManager.getCompilingDir() + "coalesceds/" + coalFilename);
					File stagedFile = new File(ModManager.getCompilingDir() + "coalesceds/" + coalFilename);
					try {
						FileUtils.copyFile(backup, stagedFile);
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Unable to stage file:", e);
						addNewError("Unable to stage one of the coalesced files for decompiling. Check the log to see why.");
						return false;
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
				commandBuilder.add(path + "coalesceds\\" + FilenameUtils.getName(coal)); //System.out.println("Building command");
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]); //Debug stuff 
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing decompile command: " + sb.toString());
				ProcessBuilder decompileProcessBuilder = new ProcessBuilder(command);
				decompileProcessBuilder.redirectErrorStream(true);
				decompileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				ProcessResult pr = ModManager.runProcess(decompileProcessBuilder);
				if (pr.hadError()) {
					ModManager.debugLogger.writeErrorWithException("Decompiling file failed:", pr.getError());
					addNewError("Decompiling one of the coalesced files failed. Check the log to see why.");
					return false;
				}
			}

			//Decompiled. Now apply edits. MERGEWORKER.
			boolean successful = applyDeltaData();
			if (!successful) {
				return false;
			}

			if (verifyOnly) {
				return true;
			}

			//Edits applied. Now recompile.
			for (String coal : coalesceds) {
				String compilerPath = ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe";
				//ProcessBuilder compileProcessBuilder = new ProcessBuilder(
				//		compilerPath, "--xml2bin", path + "\\coalesceds\\"
				//				+ FilenameUtils.removeExtension(coal)+".xml");\
				ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerPath,
						path + "coalesceds\\" + FilenameUtils.getBaseName(coal) + "\\" + FilenameUtils.getBaseName(coal) + ".xml", "--mode=ToBin");
				//log it
				ModManager.debugLogger.writeMessage("Executing compile command: " + compilerPath + " " + path + "coalesceds\\" + FilenameUtils.getBaseName(coal) + "\\"
						+ FilenameUtils.getBaseName(coal) + ".xml --mode=ToBin");
				compileProcessBuilder.redirectErrorStream(true);
				compileProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				ProcessResult pr = ModManager.runProcess(compileProcessBuilder);
				if (pr.hadError()) {
					ModManager.debugLogger.writeErrorWithException("Decompiling file failed:", pr.getError());
					addNewError("Recompiling one of the coalesced files failed. Check the log to see why.");
					return false;
				}
			}

			//Move these files to the correct variant directory
			String newVariantDir = variantsFolder + delta.getDeltaName();
			File newDir = new File(newVariantDir);
			newDir.mkdirs();

			if (!newDir.exists()) {
				ModManager.debugLogger.writeError("Unable to create variant directory. Could be read-only or invalid name");
				addNewError("Unable to create the variant directory. Folder may be read-only or you may have invalid Windows filepath characters in the name.");
				return false;
			}

			for (String coal : coalesceds) {
				//Stage the coalesced file
				File updatedStagedFile = new File(ModManager.getCompilingDir() + "coalesceds/" + FilenameUtils.getBaseName(coal) + File.separator + FilenameUtils.getName(coal));
				ModManager.debugLogger.writeMessage("Updated Staging file: " + updatedStagedFile.getAbsolutePath());

				//Find same-name directory to place files
				String jobNameToLookup = ME3TweaksUtils.coalFilenameToHeaderName(FilenameUtils.getName(coal));

				ModJob relevantjob = null;
				for (ModJob job : mod.getJobs()) {
					if (job.getJobName().equals(jobNameToLookup)) {
						relevantjob = job;
						break;
					}
				}
				//already have found job, since we had to check this earlier
				File newSourceDir = new File(newVariantDir + File.separator + relevantjob.getSourceDir());
				newSourceDir.mkdirs();
				File newSourceFile = new File(newSourceDir + File.separator + FilenameUtils.getName(coal));
				try {
					FileUtils.copyFile(updatedStagedFile, newSourceFile);
					ModManager.debugLogger.writeMessage("Copied updated staged file to " + newSourceFile);
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Unable to copy updated staged file:", e);
					addNewError("Unable to copy the updated staged file into the variant's directory. Check the log to see why.");
					return false;
				}
			}

			//Apply the variant
			applyVariant(modFolder, newVariantDir);
			new AutoTocWindow(mod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.GetBioGameDir());
			return true;
		}

		private void performCleanup() {
			ModManager.debugLogger.writeMessage("Deleting coalesceds/ folder for deltawindow cleanup");
			FileUtils.deleteQuietly(new File(ModManager.getCompilingDir() + "coalesceds/"));
		}

		private boolean applyDeltaData() {
			for (int i = 0; i < coalNodeList.getLength(); i++) {
				//coalNode is a node containing the coalesced module, such as <MP1> or <BASEGAME>
				Node coalNode = coalNodeList.item(i);
				if (coalNode.getNodeType() == Node.ELEMENT_NODE) {
					String intCoalName = coalNode.getNodeName(); //get the coal name so we can figure out what folder to look in.
					ModManager.debugLogger.writeMessage("Read coalecesed ID: " + intCoalName);
					ModManager.debugLogger.writeMessage("---------------------DELTA APPLICATION - START OF " + intCoalName + "-------------------------");

					String foldername = FilenameUtils.removeExtension(ME3TweaksUtils.internalNameToCoalFilename(intCoalName));
					NodeList filesNodeList = coalNode.getChildNodes();
					for (int j = 0; j < filesNodeList.getLength(); j++) {
						Node fileNode = filesNodeList.item(j);
						if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
							//we now have a file ID such as biogame.
							//We need to load that XML file now.
							String iniFileName = fileNode.getNodeName() + ".xml";
							ModManager.debugLogger
									.writeMessage("Loading Coalesced XML fragment into memory: " + ModManager.getCompilingDir() + "coalesceds\\" + foldername + "\\" + iniFileName);
							Document iniFile = null;
							try {
								String test = FileUtils.readFileToString(new File(ModManager.getCompilingDir() + "coalesceds\\"+foldername+"\\" + iniFileName));
								iniFile = dbFactory.newDocumentBuilder().parse("file:///" + ModManager.getCompilingDir() + "coalesceds\\" + foldername + "\\" + iniFileName);
							} catch (SAXException | IOException | ParserConfigurationException e) {
								ModManager.debugLogger.writeErrorWithException("Exception loading file into memory:", e);
								if (!verifyOnly) {
									JOptionPane.showMessageDialog(DeltaWindow.this, "<html>Unable to load decompiled coalesced file into memory:<br>" + ModManager.getCompilingDir()
											+ "coalesceds\\" + foldername + "\\" + iniFileName + ".</html>", "Delta Error", JOptionPane.ERROR_MESSAGE);
								}
								return false;
							}
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
										isArrayProperty = false;
										break;
									case "ArrayProperty":
										arrayType = property.getAttribute("arraytype");
										matchontype = property.getAttribute("matchontype");
										operation = property.getAttribute("operation");
										UE3type = property.getAttribute("type");
										isArrayProperty = true;
										break;
									case "Section":
										newPropName = property.getAttribute("name");
										operation = property.getAttribute("operation");
										isArrayProperty = false;
										isSection = true;
										break;
									default:
										ModManager.debugLogger.writeError("Unknown delta property type: " + nodeName);
										if (!verifyOnly) {
											JOptionPane.showMessageDialog(DeltaWindow.this,
													"<html>Unknown delta property type: " + nodeName + "<br>You may need to update Mod Manager, or this delta may be invalid.",
													"Delta Error", JOptionPane.ERROR_MESSAGE);
										}
										return false;
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
											ModManager.debugLogger
													.writeError("Could not find the path to a property: " + path + ".\nIn module: " + intCoalName + "<br>File: " + iniFileName);
											if (!verifyOnly) {
												JOptionPane.showMessageDialog(DeltaWindow.this, "<html>Could not find the path to a property: " + path + ".<br>Module: " + intCoalName
														+ "<br>File: " + iniFileName + "<br>This delta is not valid.</html>", "Delta Error", JOptionPane.ERROR_MESSAGE);
											}
											return false;
										}
									}
									if (drilled == null) {
										//we didn't find what we wanted...
										dispose();
										if (!verifyOnly) {
											JOptionPane.showMessageDialog(DeltaWindow.this,
													"<html>Could not find the path " + path + " to property.<br>Module: " + intCoalName + "<br>File: " + iniFileName + "</html>",
													"Compiling Error", JOptionPane.ERROR_MESSAGE);
										}
										return false;
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
													ModManager.debugLogger
															.writeMessage("Found type candidate (" + matchontype + ") for arrayreplace: " + itemToModify.getTextContent());
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
														ModManager.debugLogger.writeError(
																"ERROR: Unknown matching algorithm: " + arrayType + ". does this client need updated? Aborting this stat update.");
														if (!verifyOnly) {
															JOptionPane.showMessageDialog(DeltaWindow.this,
																	"<html>Unsupported delta matching algorithm: " + arrayType
																			+ ".<br>Mod Manager may need to be updated to support this, or the delta may be incorrect.<br>"
																			+ "This part of the delta will be skipped.</html>",
																	"Delta Error", JOptionPane.ERROR_MESSAGE);
														}
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
															if (!verifyOnly) {
																JOptionPane.showMessageDialog(DeltaWindow.this,
																		"<html>Unsupported delta operation: " + operation
																				+ ".<br>Mod Manager may need to be updated to support this, or the delta may be incorrect.</html>",
																		"Delta Error", JOptionPane.ERROR_MESSAGE);
															}
															return false;
														} //end operation [switch]
														break;
													} //end of match = true [if]
												} //end of array matchontype check [if]
											} //end of array property [if]
										} //end of property = element node (not junk) [if]
									} //end of props.length to search through. [for loop]
									if (foundProperty != true) {
										if (verifyOnly) {
											return false;
										}
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
											sb.append("====ARRAY ATTRIBUTE INFO====<br>");
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
										if (!verifyOnly) {
											JOptionPane.showMessageDialog(DeltaWindow.this, sb.toString(), "Delta Error", JOptionPane.ERROR_MESSAGE);
										}
										addNewError("Could not find a property to update/remove.");
										ModManager.debugLogger.writeError(sb.toString());
										return false;
									}
								}
							}
							//end of the file node.
							//Time to save the file...
							if (!verifyOnly) {
								try {
									Transformer transformer = TransformerFactory.newInstance().newTransformer();
									transformer.setOutputProperty(OutputKeys.INDENT, "yes");
									transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
									File outputFile = new File(ModManager.getCompilingDir() + "coalesceds\\" + foldername + "\\" + iniFileName);
									Result output = new StreamResult(outputFile);
									Source input = new DOMSource(iniFile);
									ModManager.debugLogger.writeMessage("Saving file: " + outputFile.toString());
									transformer.transform(input, output);
									ModManager.debugLogger.writeMessage("File saved: " + outputFile.toString());
									//go to next file
								} catch (TransformerFactoryConfigurationError | TransformerException e) {
									ModManager.debugLogger.writeErrorWithException("Error saving modified file!", e);
									JOptionPane.showMessageDialog(DeltaWindow.this, "<html>Error occured saving file: " + e.getMessage() + "</html>", "Delta Error", JOptionPane.ERROR_MESSAGE);
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		private void addNewError(String string) {
			errors.add(string);
		}

		protected void done() {
			boolean success = false;
			performCleanup();
			dispose();
			try {
				success = get(); // this line can throw InterruptedException or ExecutionException
				
				if (success && !verifyOnly) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Switched to variant: " + delta.getDeltaName());
				} else if (success && verifyOnly) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Verified variant: " + delta.getDeltaName());
				} else if (!success && verifyOnly) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Verification failed: " + delta.getDeltaName());
					if (deleteOnFailedVerify) {
						FileUtils.deleteQuietly(new File(delta.getDeltaFilepath()));
					}
					JOptionPane
							.showMessageDialog(DeltaWindow.this,
									"This variant will not be able to fully apply to this mod:\n - " + delta.getDeltaName()
											+ (deleteOnFailedVerify ? "\n\nThis variant will be unavailable for this mod." : ""),
									"Delta Verification Error", JOptionPane.ERROR_MESSAGE);
				} else {
					StringBuilder sb = new StringBuilder();
					for (String str : errors) {
						sb.append("\n");
						sb.append(str);
					}
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error switching variant. Check logs to see why.");
					JOptionPane.showMessageDialog(DeltaWindow.this, "An error occured applying this delta:\n" + sb.toString(), "Delta Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("Unhandled Delta Application Exception:", e);
				JOptionPane.showMessageDialog(DeltaWindow.this,
						"An unhandled error occured while trying to apply the delta:\n" + e.getMessage() + "\n\nYou should report this to Mgamerz.", "Delta Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Unhandled Delta Application Exception:", e);
				JOptionPane.showMessageDialog(DeltaWindow.this,
						"An unhandled error occured while trying to apply the delta:\n" + e.getMessage() + "\n\nYou should report this to Mgamerz.", "Delta Error",
						JOptionPane.ERROR_MESSAGE);
			}
			ModManager.debugLogger.writeMessage("============END OF DELTAWORKER()==============");
		}
	}

	private boolean applyVariant(String modFolder, String variantFolder) {
		if (new File(variantFolder).exists()) {
			try {
				ModManager.debugLogger.writeMessage("Applying Variant: " + variantFolder + " => " + modFolder);
				FileUtils.copyDirectory(new File(variantFolder), new File(modFolder));
				ModManager.debugLogger.writeMessage("Variant applied");
				return true;
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Unable to apply variant:", e);
				return false;
			}
		} else {
			ModManager.debugLogger.writeMessage(
					"Unable to apply variant: Source folder doesn't exist. If applying original (during merge phase) this means the mod may not have been switched before.");
			return false;
		}
	}
}
