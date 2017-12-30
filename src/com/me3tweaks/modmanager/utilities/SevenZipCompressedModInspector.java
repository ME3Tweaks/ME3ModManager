package com.me3tweaks.modmanager.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.ModImportArchiveWindow;
import com.me3tweaks.modmanager.ModImportArchiveWindow.ImportWorker;
import com.me3tweaks.modmanager.ModImportArchiveWindow.ScanWorker;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ThirdPartyImportingInfo;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.objects.ThreadCommand;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class SevenZipCompressedModInspector {
	public static class DecompressModToDiskCallback implements IArchiveExtractCallback {
		private IInArchive inArchive;
		private ArrayList<String> parentPathsToExtract;
		private ImportWorker importworker;
		private int numTotalFiles, numCompletedFiles = 0;
		private CompressedMod compressedMod;

		public DecompressModToDiskCallback(IInArchive inArchive, ArrayList<String> parentPathsToExtract,
				int numTotalFiles, CompressedMod compressedMod, ModImportArchiveWindow.ImportWorker importworker) {
			this.inArchive = inArchive;
			this.numTotalFiles = numTotalFiles;
			this.parentPathsToExtract = parentPathsToExtract;
			this.importworker = importworker;
			this.compressedMod = compressedMod; // can be null. only used if mod is directly zipped or is unofficial
			this.numCompletedFiles = 0;
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			if (extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}

			String outputDirectory = (String) inArchive.getProperty(index, PropID.PATH); // in-archive path of item we
																							// are extracting
			boolean modFound = false;
			String modName = compressedMod.getModName();
			modName = modName.replaceAll("/", "-");
			modName = modName.replaceAll("\\\\", "-");
			for (String inArchivePathParent : parentPathsToExtract) {
				if (inArchivePathParent == null || outputDirectory.startsWith(inArchivePathParent)) {
					if (inArchivePathParent == null) {
						// prevents path creation
						outputDirectory = modName + File.separator + outputDirectory;
					} else {
						File path = new File(inArchivePathParent);
						String parent = path.getParent();
						if (compressedMod.isOfficiallySupported()) {
							if (parent != null) {
								outputDirectory = ResourceUtils.getRelativePath(outputDirectory, parent,
										File.separator);
							} else {
								outputDirectory = modName + "\\" + outputDirectory;
							}
						} else {
							String shortenedOutput = outputDirectory;
							if (inArchivePathParent.contains("\\")) {
								shortenedOutput = outputDirectory.substring(inArchivePathParent.length());
							}
							// unofficial mod
							outputDirectory = modName + "\\" + shortenedOutput;
						}
					}

					// this file is part of this modpath...
					modFound = true;
					break;
				}

				if (!modFound) {
					return null;
				}
			}

			outputDirectory = ModManager.getModsDir() + outputDirectory;
			//
			// System.out.println(index + " " + outputDirectory);
			final String finaldir = outputDirectory;
			final File path = new File(finaldir);
			if (path.exists()) {
				ModManager.debugLogger.writeMessage("Removing pre-existing file: " + path);
				FileUtils.deleteQuietly(path); // prevents appending issuess
			}

			if (importworker != null) {
				importworker.publishUpdate(new ThreadCommand("EXTRACTING_FILE", FilenameUtils.getName(finaldir)));
			}
			numCompletedFiles++;
			int progress = (int) Math.min(100, (numCompletedFiles * 100.0 / numTotalFiles));
			importworker.setProgressValue(progress);
			return new ISequentialOutStream() {

				public int write(byte[] data) throws SevenZipException {
					// ModManager.debugLogger.writeMessage("Decompressing chunk to " + finaldir);
					FileOutputStream fos = null;
					try {

						if (!path.getParentFile().exists()) {
							path.getParentFile().mkdirs();
						}

						if (!path.exists()) {
							path.createNewFile();
						}
						fos = new FileOutputStream(path, true);
						fos.write(data);
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("IOException while extracting " + finaldir, e);
					} finally {
						try {
							if (fos != null) {
								fos.flush();
								fos.close();
							}
						} catch (IOException e) {
							ModManager.debugLogger.writeErrorWithException("Could not close FileOutputStream", e);
						}
					}
					return data.length; // Return amount of processed data
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			if (extractOperationResult != ExtractOperationResult.OK) {
				System.err.println("Extraction error");
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {

		}

		public void setTotal(long total) throws SevenZipException {
		}
	}

	public static class DecompressFileToMemoryCallback implements IArchiveExtractCallback {
		private int currentIndex = -1;
		private IInArchive inArchive;
		private ArrayList<String> parentPathsToExtract;
		private HashMap<String, ByteArrayInOutStream> outputStreams = new HashMap<>();
		private int numTotalItems;
		private int numItemsDone = 0;
		private ScanWorker scanworker;

		// private HashMap<>

		public DecompressFileToMemoryCallback(IInArchive inArchive, ArrayList<String> parentPathsToExtract,
				int numTotalItems, ScanWorker scanworker) {
			this.inArchive = inArchive;
			this.parentPathsToExtract = parentPathsToExtract;
			this.numTotalItems = numTotalItems;
			this.scanworker = scanworker;
			this.numItemsDone = 0;
		}

		private HashMap<String, ByteArrayInOutStream> getOutputStreams() {
			return outputStreams;
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			this.currentIndex = index;
			numItemsDone++;
			if (scanworker != null) {
				scanworker.setProgressValue((int) ((numItemsDone * 1.0 / numTotalItems) * 100));
			}

			if (extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}

			String archiveName = (String) inArchive.getProperty(currentIndex, PropID.PATH);
			ModManager.debugLogger.writeMessage("Extracting for scan: " + archiveName);
			return new ISequentialOutStream() {

				public int write(byte[] data) throws SevenZipException {

					ByteArrayInOutStream fos = outputStreams.get(archiveName);
					if (fos == null) {
						fos = new ByteArrayInOutStream();
						outputStreams.put(archiveName, fos);
					}
					try {
						fos.write(data);
					} catch (IOException e) {
						ModManager.debugLogger.writeErrorWithException("Error extracting for scan:", e);
					} finally {
						try {
							if (fos != null) {
								fos.flush();
								fos.close();
							}
						} catch (IOException e) {
							ModManager.debugLogger.writeErrorWithException("Could not close ByteOutputStream", e);
						}
					}
					return data.length; // Return amount of proceed data
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			if (extractOperationResult != ExtractOperationResult.OK) {
				ModManager.debugLogger.writeError("Error occured extraction mods for scanning.");
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
		}

		public void setTotal(long total) throws SevenZipException {
		}

	}

	/**
	 * Extracts compressed mods from an archive. This is a very complex method...
	 * 
	 * @param archivePath
	 * @param compressedModsToExtract
	 * @param importWorker
	 * @return
	 */
	public static boolean extractCompressedModsFromArchive(String archivePath,
			ArrayList<CompressedMod> compressedModsToExtract, ModImportArchiveWindow.ImportWorker importWorker) {

		// check for local mods with same names
		for (CompressedMod cm : compressedModsToExtract) {
			String outputpath = ModManager.getModsDir();
			String extractTop = cm.getDescLocationInArchive();
			File parent = null;
			if (extractTop != null) {
				// Descfile Extracting
				parent = new File(extractTop).getParentFile();
			}
			if (parent == null) {
				// top level extract
				String modName = cm.getModName();
				modName = modName.replaceAll("/", "-");
				modName = modName.replaceAll("\\\\", "-"); // prevents path creation
				outputpath += modName;
			} else {
				outputpath += parent.getName();
			}

			File outputdir = new File(outputpath);
			if (outputdir.exists() && outputdir.isDirectory()) {
				// ask if sideloading or new mod
				int result = importWorker.askIfSideloadOrNew(cm.getModName());
				if (result == ModImportArchiveWindow.IMPORT_AS_NEW_OPTION) {
					ModManager.debugLogger.writeMessage("Deleting existing mod folder: " + outputdir);
					boolean deletesuccessful = FileUtils.deleteQuietly(outputdir);
					if (!deletesuccessful) {
						ModManager.debugLogger.writeError("FAILED TO DELETE FOLDER: " + outputdir);
					}
				} else if (result == ModImportArchiveWindow.IMPORT_AS_SIDELOAD_OPTION) {
					ModManagerWindow.forceUpdateOnReloadList.add(cm.getModDescMod().getClassicUpdateCode());
				} else {
					// cancel
					return false;
				}

			}
		}

		ModManager.debugLogger.writeMessage(
				"Extracting " + compressedModsToExtract.size() + " compressed mods from archive:" + archivePath);

		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		boolean error = false;
		try {
			randomAccessFile = new RandomAccessFile(archivePath, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			int count = inArchive.getNumberOfItems();

			// get parent paths of mods.
			int windowIndex = 0;
			for (CompressedMod compressedMod : compressedModsToExtract) {
				windowIndex++;
				ArrayList<Integer> itemsToExtract = new ArrayList<Integer>();
				ArrayList<String> parentPathsToExtract = new ArrayList<String>();
				if (compressedMod.isOfficiallySupported()) {
					String parent = new File(compressedMod.getDescLocationInArchive()).getParent();
					ModManager.debugLogger
							.writeMessage("Marked mod parent path:" + parent + " (if null, extract everything.)");
					parentPathsToExtract.add(parent);
				} else {
					String importRoot = compressedMod.getUnofficialImportRoot();
					ModManager.debugLogger.writeMessage("Marking folder for extraction (unofficial import): "
							+ compressedMod.getUnofficialImportRoot());
					parentPathsToExtract.add(compressedMod.getUnofficialImportRoot());
				}

				int numItems = 0;
				// 1st pass - Get all files and a count.
				for (int i = 0; i < count; i++) {
					String path = (String) inArchive.getProperty(i, PropID.PATH);
					// ModManager.debugLogger.writeMessage("Iterating over archive files: " + path);
					for (String str : parentPathsToExtract) {
						boolean folder = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
						if (!folder && (str == null || path.startsWith(str))) {
							// null str means parent path resolved to nothing, extract whole folder.
							ModManager.debugLogger.writeMessage("Adding item to extract: " + path);
							itemsToExtract.add(i);
							numItems++;
							break;
						}
					}
				}

				int[] items = new int[itemsToExtract.size()];
				int i = 0;
				for (Integer integer : itemsToExtract) {
					items[i++] = integer.intValue(); // indexes to extract
					// System.out.println(integer + " " + inArchive.getProperty(integer,
					// PropID.PATH));
				}
				importWorker.publishUpdate(new ThreadCommand("EXTRACTING_MOD", compressedMod.getModName()));
				if (compressedModsToExtract.size() > 1) {
					importWorker.publishUpdate(new ThreadCommand("SET_MAINWINDOW_TEXT",
							"Importing mods [" + windowIndex + "/" + compressedModsToExtract.size() + "]"));
				}
				DecompressModToDiskCallback dftmc = new DecompressModToDiskCallback(inArchive, parentPathsToExtract,
						numItems, compressedMod, importWorker);
				inArchive.extract(items, false, // Non-test mode
						dftmc);
				if (!compressedMod.isOfficiallySupported()) {
					String modName = compressedMod.getModName();
					modName = modName.replaceAll("/", "-");
					modName = modName.replaceAll("\\\\", "-");

					String outputDirectory = ModManager.getModsDir() + "\\" + modName + "\\moddesc.ini";
					FileUtils.writeStringToFile(new File(outputDirectory), compressedMod.getUnofficialModDescString(),
							"UTF-8");
					ModManager.debugLogger
							.writeMessage("wrote unofficial moddesc.ini file for " + compressedMod.getModName());
				}
				System.out.println("COMPLETED EXTRACT FOR " + compressedMod.getModName());
			}
		} catch (SevenZipException e) {
			// ModManager.debugLogger.writeErrorWithException("First Potential Cause of
			// SevenZipException while extracting mod:", e.getCauseFirstPotentialThrown());
			// ModManager.debugLogger.writeErrorWithException("Last Potential Cause of
			// SevenZipException while extracting mod:", e.getCauseLastPotentialThrown());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			try {
				ModManager.debugLogger.writeError("Error while extracting 7zip archive: " + baos.toString("utf-8"));
				ModManager.debugLogger.writeErrorWithException("Stack Trace:", e);
			} catch (UnsupportedEncodingException e1) {
				// this shouldn't happen.
			}
			error = true;
		} catch (Exception e) {
			error = true;
			ModManager.debugLogger.writeErrorWithException("Error extracting mod(s) from archive:", e);
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					System.err.println("Error closing file: " + e);
				}
			}
		}
		return !error;
	}

	public static ArrayList<CompressedMod> getCompressedModsInArchive(String archivePath, ScanWorker scanWorker) {
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		ScanWorker scanworker = scanWorker;
		int nummodsfound = 0;
		ArrayList<String> rootDLCFolders = new ArrayList<String>();

		try {
			randomAccessFile = new RandomAccessFile(archivePath, "r");
			long fileSize = randomAccessFile.length();
			ArrayList<ThirdPartyImportingInfo> thirdPartyImportsWithThisSize = ME3TweaksUtils
					.getThirdPartyImportingInfosBySize(fileSize);

			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			int count = inArchive.getNumberOfItems();
			ArrayList<String> parentPathsToExtract = new ArrayList<String>();
			ArrayList<Integer> itemsToExtract = new ArrayList<Integer>();
			// 1st pass - find moddesc.ini files. Catalog their parent directories.
			for (int i = 0; i < count; i++) {
				String path = (String) inArchive.getProperty(i, PropID.PATH);
				if (FilenameUtils.getBaseName(path).startsWith("DLC_") && !path.contains("\\")) {
					rootDLCFolders.add(path);
				}
				if (FilenameUtils.getName(path).equalsIgnoreCase("moddesc.ini")) {
					// System.out.println("Moddesc in archive: "+path);
					String parent = new File(path).getParent();
					ModManager.debugLogger.writeMessage("Found moddesc file in archive in folder: " + parent);
					if (scanworker != null) {
						scanworker.publishUpdate(new ThreadCommand("FOUND_MODFILE", null, ++nummodsfound));
					}
					boolean shouldAdd = true;
					for (String str : parentPathsToExtract) {
						if (parent.startsWith(str)) {
							shouldAdd = false; // recursive mod. This mod is in a folder of another mod. Do not add this
												// mod.
							break;
						}
					}
					if (shouldAdd) {
						parentPathsToExtract.add(parent);
						itemsToExtract.add(i);
					}
				}
			}

			// Decompress moddesc.ini
			if (!itemsToExtract.isEmpty()) {
				int[] items = new int[itemsToExtract.size()];
				int i = 0;
				for (Integer integer : itemsToExtract) {
					items[i++] = integer.intValue();
				}
				if (scanworker != null) {
					scanworker.publishUpdate(new ThreadCommand("POST_SUBTEXT"));
				}
				DecompressFileToMemoryCallback dftmc = new DecompressFileToMemoryCallback(inArchive,
						parentPathsToExtract, count, scanworker);
				inArchive.extract(items, false, // Non-test mode
						dftmc);
				ModManager.debugLogger.writeMessage("Building compressed mods list from extracted streams");
				HashMap<String, ByteArrayInOutStream> outputs = dftmc.getOutputStreams();
				ArrayList<CompressedMod> compressed = new ArrayList<>();
				for (Map.Entry<String, ByteArrayInOutStream> entry : outputs.entrySet()) {
					String key = entry.getKey();
					ByteArrayInOutStream value = entry.getValue();
					ModManager.debugLogger.writeMessage(
							"Loading compressed mod descriptor. - may throw errors for missing files - OK to ignore.");
					Mod mod = new Mod(value);
					CompressedMod cm = new CompressedMod();
					ModManager.debugLogger.writeMessage("Finished loading compressed mod descriptor.");
					cm.setModDescMod(mod);
					cm.setModDescription(mod.getModDisplayDescription());
					cm.setModName(mod.getModName());
					cm.setDescLocationInArchive(key);
					cm.setOfficiallySupported(true);
					compressed.add(cm);
				}
				return compressed;
			}

			// No moddesc.ini files found. Lets do a second pass via size

			if (thirdPartyImportsWithThisSize != null && thirdPartyImportsWithThisSize.size() > 0) {
				// hash file
				scanworker.publishUpdate(new ThreadCommand("SET_SUBTEXT_SCANNING",
						"Looking up importing info from Third Party Importing Service..."));
				String hash = MD5Checksum.getMD5Checksum(archivePath);
				ThirdPartyImportingInfo impinfo = null;
				System.out.println("HASH: " + hash);

				for (ThirdPartyImportingInfo info : thirdPartyImportsWithThisSize) {
					System.out.println("fhash: " + info.getMd5() + " " + info.getFilename());

					if (info.getMd5().equals(hash)) {
						impinfo = info;
						break;
					}
				}
				if (impinfo != null) {
					ArrayList<CompressedMod> compressed = new ArrayList<>();
					ModManager.debugLogger.writeMessage("Got third party importing info TPIS.");
					ArrayList<String> modFolders = new ArrayList<>();
					for (int i = 0; i < count; i++) {
						boolean isFolder = (boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
						if (isFolder) {
							String path = (String) inArchive.getProperty(i, PropID.PATH);
							if (path.startsWith(impinfo.getInarchivepathtosearch()) && path.contains("CookedPCConsole")
									&& path.contains("DLC_")) {
								String withoutParentInfo = path.substring(impinfo.getInarchivepathtosearch().length());
								if (withoutParentInfo.startsWith("\\")) {
									withoutParentInfo = withoutParentInfo.substring(1);
								}

								int currentSubIndex = impinfo.getSubidrectorydepth();
								while (currentSubIndex > 0) {
									int subDirIndicatorIndex = withoutParentInfo.indexOf("\\");
									if (subDirIndicatorIndex >= 0) {
										withoutParentInfo = withoutParentInfo.substring(subDirIndicatorIndex);
										if (withoutParentInfo.startsWith("\\")) {
											withoutParentInfo = withoutParentInfo.substring(1);
										} else {
											break;
										}
									}
									currentSubIndex--;
								}

								String cookedPCConsoleFolder = path;
								String dlcFolderName = new File(cookedPCConsoleFolder).getParentFile().getName();
								if (dlcFolderName != null) {
									ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(dlcFolderName);
									if (tpmi != null) {
										ModManager.debugLogger.writeMessage(
												"Generating compressed mod (TPMI-based) for " + tpmi.getModname());
										Wini moddesc = new Wini();
										moddesc.add("ModManager", "cmmver", ModManager.MODDESC_VERSION_SUPPORT);
										moddesc.add("ModInfo", "modname", tpmi.getModname());
										moddesc.add("ModInfo", "moddev", tpmi.getModauthor());
										moddesc.add("ModInfo", "modsite", tpmi.getModsite());
										moddesc.add("ModInfo", "moddesc", tpmi.getModdescription());
										moddesc.add("ModInfo", "unofficial", "true");
										moddesc.add("CUSTOMDLC", "sourcedirs", dlcFolderName);
										moddesc.add("CUSTOMDLC", "destdirs", dlcFolderName);
										StringWriter writer = new StringWriter();
										moddesc.store(writer);

										Mod mod = new Mod(writer);
										CompressedMod cm = new CompressedMod();
										cm.setUnofficialModDescString(writer.toString());
										File extractRoot = new File(cookedPCConsoleFolder).getParentFile(); // will be
																											// DLC_xxxxx
										boolean hasParent = false;
										if (extractRoot.getParentFile() != null) { // extract folder contents but not
																					// folder itself
											hasParent = true;
											extractRoot = extractRoot.getParentFile();
										} else {
											extractRoot = new File("");
										}
										System.out.println(extractRoot.toString());
										String inarchivepath = extractRoot.toString();
										if (hasParent) {
											inarchivepath += "\\";
										}
										cm.setUnofficialImportRoot(inarchivepath);
										cm.setModDescMod(mod);
										cm.setModDescription(mod.getModDisplayDescription());
										cm.setModName(mod.getModName());
										compressed.add(cm);
									}
								}
							}
						}
					}
					return compressed;
				}
			}

			// Root files were found and no third party data was available, final pass
			if (rootDLCFolders.size() > 0) {
				// maybe we can pull mod from here
				ArrayList<CompressedMod> compressed = new ArrayList<>();

				for (String str : rootDLCFolders) {
					ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(str);
					if (tpmi != null) {
						ModManager.debugLogger
								.writeMessage("Generating compressed mod (UNOFFICIAL) for " + tpmi.getModname());

						Wini moddesc = new Wini();
						moddesc.add("ModManager", "cmmver", ModManager.MODDESC_VERSION_SUPPORT);
						moddesc.add("ModInfo", "modname", tpmi.getModname());
						moddesc.add("ModInfo", "moddev", tpmi.getModauthor());
						moddesc.add("ModInfo", "modsite", tpmi.getModsite());
						moddesc.add("ModInfo", "moddesc", tpmi.getModdescription());
						moddesc.add("ModInfo", "unofficial", "true");
						moddesc.add("CUSTOMDLC", "sourcedirs", str);
						moddesc.add("CUSTOMDLC", "destdirs", str);
						StringWriter writer = new StringWriter();
						moddesc.store(writer);

						Mod mod = new Mod(writer);
						CompressedMod cm = new CompressedMod();
						cm.setUnofficialModDescString(writer.toString());
						cm.setUnofficialImportRoot(str);
						cm.setFullyUnofficial();
						cm.setModDescMod(mod);
						cm.setModDescription(mod.getModDisplayDescription());
						cm.setModName(mod.getModName());
						compressed.add(cm);
					}
				}
				return compressed;
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("SEVENZIPLIB Error: ", e);
		} finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					ModManager.debugLogger.writeErrorWithException("Error closing archive: ", e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error closing file: ", e);
				}
			}
		}

		return new ArrayList<CompressedMod>();

	}
}