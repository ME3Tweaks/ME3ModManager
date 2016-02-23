package com.me3tweaks.modmanager.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModImportArchiveWindow;
import com.me3tweaks.modmanager.ModImportArchiveWindow.ImportWorker;
import com.me3tweaks.modmanager.ModImportArchiveWindow.ScanWorker;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.CompressedMod;
import com.me3tweaks.modmanager.objects.Mod;

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
		private ImportWorker worker;
		private int numTotalFiles, numCompletedFiles = 0;

		//private HashMap<>

		public DecompressModToDiskCallback(IInArchive inArchive, ArrayList<String> parentPathsToExtract, int numTotalFiles, ModImportArchiveWindow.ImportWorker worker) {
			this.inArchive = inArchive;
			this.numTotalFiles = numTotalFiles;
			this.parentPathsToExtract = parentPathsToExtract;
			this.worker = worker;
			for (String modpath : parentPathsToExtract) {
				FileUtils.deleteQuietly(new File(ModManager.getModsDir() + FilenameUtils.getBaseName(modpath)));
			}
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			if (extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}

			String outputDirectory = (String) inArchive.getProperty(index, PropID.PATH);
			boolean modFound = false;
			for (String modpath : parentPathsToExtract) {
				if (outputDirectory.startsWith(modpath)) {
					//this file is part of this modpath...
					String parentPath = new File(modpath).getParent();
					if (parentPath != null) {
						outputDirectory = ResourceUtils.getRelativePath(outputDirectory, parentPath, File.separator);
					}
					modFound = true;
					break;
				}
			}
			if (!modFound) {
				return null;
			}

			outputDirectory = ModManager.getModsDir() + outputDirectory;
			//
			//System.out.println(index + " " + outputDirectory);
			final String finaldir = outputDirectory;

			//System.out.println("Extracting: " + inArchive.getProperty(index, PropID.PATH));
			return new ISequentialOutStream() {

				public int write(byte[] data) throws SevenZipException {
					FileOutputStream fos = null;
					try {
						File path = new File(finaldir);

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
					return data.length; // Return amount of proceed data
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			if (extractOperationResult != ExtractOperationResult.OK) {
				System.err.println("Extraction error");
			} else {
				System.out.println("Extraction done.");
				numCompletedFiles++;
				int progress = (int) Math.min(100, ((numCompletedFiles * 1.0) / numTotalFiles) * 100);
				System.out.println("Progress: " + ((numCompletedFiles * 1.0) / numTotalFiles) * 100);
				worker.setProgressValue(progress);
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

		//private HashMap<>

		public DecompressFileToMemoryCallback(IInArchive inArchive, ArrayList<String> parentPathsToExtract, int numTotalItems) {
			this.inArchive = inArchive;
			this.parentPathsToExtract = parentPathsToExtract;
			this.numTotalItems = numTotalItems;
			System.out.println("Starting decompress to memory");
		}

		private HashMap<String, ByteArrayInOutStream> getOutputStreams() {
			return outputStreams;
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			this.currentIndex = index;
			numItemsDone++;
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
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				System.err.println("Extraction error");
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
		}

		public void setTotal(long total) throws SevenZipException {
		}

	}

	public static void extractCompressedModsFromArchive(String archivePath, ArrayList<CompressedMod> compressedModsToExtract, ModImportArchiveWindow.ImportWorker importWorker) {
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archivePath, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			int count = inArchive.getNumberOfItems();
			ArrayList<Integer> itemsToExtract = new ArrayList<Integer>();

			//get parent paths of mods.
			ArrayList<String> parentPathsToExtract = new ArrayList<String>();
			for (CompressedMod compressedMod : compressedModsToExtract) {
				parentPathsToExtract.add(new File(compressedMod.getDescLocationInArchive()).getParent());
			}

			int numItems = 0;
			//1st pass - Get all files and a count.
			for (int i = 0; i < count; i++) {
				String path = (String) inArchive.getProperty(i, PropID.PATH);
				for (String str : parentPathsToExtract) {
					boolean folder = (Boolean) inArchive.getProperty(i, PropID.IS_FOLDER);
					if (!folder && path.startsWith(str)) {
						itemsToExtract.add(i);
						numItems++;
						break;
					}
				}
			}

			int[] items = new int[itemsToExtract.size()];
			int i = 0;
			for (Integer integer : itemsToExtract) {
				items[i++] = integer.intValue();
				System.out.println(integer + " " + inArchive.getProperty(integer, PropID.PATH));
			}
			DecompressModToDiskCallback dftmc = new DecompressModToDiskCallback(inArchive, parentPathsToExtract, numItems, importWorker);
			inArchive.extract(items, false, // Non-test mode
					dftmc);
		} catch (SevenZipException e) {
			//ModManager.debugLogger.writeErrorWithException("First Potential Cause of SevenZipException while extracting mod:", e.getCauseFirstPotentialThrown());
			//ModManager.debugLogger.writeErrorWithException("Last Potential Cause of SevenZipException while extracting mod:", e.getCauseLastPotentialThrown());
			e.printStackTraceExtended();
		} catch (Exception e) {

			System.err.println("Error occurs: " + e);
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
	}

	public static ArrayList<CompressedMod> getCompressedModsInArchive(String archivePath) {
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archivePath, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			int count = inArchive.getNumberOfItems();
			ArrayList<String> parentPathsToExtract = new ArrayList<String>();
			ArrayList<Integer> itemsToExtract = new ArrayList<Integer>();
			//1st pass - find moddesc.ini files. Catalog their parent directories.
			for (int i = 0; i < count; i++) {
				String path = (String) inArchive.getProperty(i, PropID.PATH);
				if (FilenameUtils.getName(path).equalsIgnoreCase("moddesc.ini")) {
					//System.out.println("Moddesc in archive: "+path);
					String parent = new File(path).getParent();
					System.out.println("Found mod: " + parent);
					boolean shouldAdd = true;
					for (String str : parentPathsToExtract) {
						if (parent.startsWith(str)) {
							shouldAdd = false; //recursive mod. This mod is in a folder of another mod. Do not add this mod.
							break;
						}
					}
					if (shouldAdd) {
						parentPathsToExtract.add(parent);
						itemsToExtract.add(i);
					}
				}
			}
			int[] items = new int[itemsToExtract.size()];
			int i = 0;
			for (Integer integer : itemsToExtract) {
				items[i++] = integer.intValue();
			}
			DecompressFileToMemoryCallback dftmc = new DecompressFileToMemoryCallback(inArchive, parentPathsToExtract, count);
			inArchive.extract(items, false, // Non-test mode
					dftmc);
			ModManager.debugLogger.writeMessage("Building compressed mods list from extracted streams");
			HashMap<String, ByteArrayInOutStream> outputs = dftmc.getOutputStreams();
			ArrayList<CompressedMod> compressed = new ArrayList<>();
			for (Map.Entry<String, ByteArrayInOutStream> entry : outputs.entrySet()) {
				String key = entry.getKey();
				ByteArrayInOutStream value = entry.getValue();
				Mod mod = new Mod(value);
				CompressedMod cm = new CompressedMod();
				cm.setModDescMod(mod);
				cm.setModDescription(mod.getModDisplayDescription());
				cm.setModName(mod.getModName());
				cm.setDescLocationInArchive(key);
				compressed.add(cm);
			}
			return compressed;
		} catch (Exception e) {
			System.err.println("Error occurs: " + e);
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
		return new ArrayList<CompressedMod>();
	}
}