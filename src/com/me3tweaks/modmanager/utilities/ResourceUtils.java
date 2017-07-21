package com.me3tweaks.modmanager.utilities;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

/**
 * Contains generic methods for basic tasks
 *
 *
 */
public class ResourceUtils {
	/**
	 * Opens a directory in Windows Explorer.
	 * 
	 * @param dir
	 *            Directory to open
	 */
	public static void openDir(String dir) {
		try {
			Desktop.getDesktop().open(new File(dir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("I/O Exception while opening directory " + dir + ".", e);
		}
	}

	public static boolean decompressLZMAFile(String lzmaFile, String expectedHash) {
		RandomAccessFile randomAccessFile = null;
		String decompressedFileLocation = lzmaFile.substring(0, lzmaFile.length() - 5);
		ModManager.debugLogger.writeMessage("Decompressing LZMA file: " + lzmaFile);
		IInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(lzmaFile, "r");
			inArchive = SevenZip.openInArchive(null, // autodetect archive type
					new RandomAccessFileInStream(randomAccessFile));

			// Getting simple interface of the archive inArchive
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				if (!item.isFolder()) {
					ExtractOperationResult result;

					result = item.extractSlow(new ISequentialOutStream() {
						public int write(byte[] data) throws SevenZipException {
							FileOutputStream fos = null;
							try {
								File path = new File(decompressedFileLocation);

								if (!path.getParentFile().exists()) {
									path.getParentFile().mkdirs();
								}

								if (!path.exists()) {
									path.createNewFile();
								}
								fos = new FileOutputStream(path, true);
								fos.write(data);
							} catch (SevenZipException e) {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								PrintStream ps = new PrintStream(baos);
								e.printStackTrace(ps);
								try {
									ModManager.debugLogger.writeError("Error while decompressing LZMA: " + baos.toString("utf-8"));
								} catch (UnsupportedEncodingException e1) {
									// this shouldn't happen.
								}
							} catch (IOException e) {
								ModManager.debugLogger.writeErrorWithException("IOException while extracting " + lzmaFile, e);
								e.printStackTrace();
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
							return data.length; // Return amount of consumed
												// data
						}
					});

					if (result == ExtractOperationResult.OK) {
						ModManager.debugLogger.writeMessage("Decompression complete.");
						if (expectedHash != null) {
							String hash = MD5Checksum.getMD5Checksum(decompressedFileLocation);
							if (expectedHash.equals(hash)) {
								throw new Exception("Hash check failed for decompressed file");
							}
						}
						return true;
					} else {
						ModManager.debugLogger.writeError("Error extracting item: " + result);
						return false;
					}
				}
			}
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error occured decompressing LZMA file:", e);
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
			boolean deleted = FileUtils.deleteQuietly(new File(lzmaFile));
			if (!deleted) {
				System.err.println("Unable to delete compressed file after decompression attempt");
			}
		}
		return false;
	}

	/**
	 * Returns line number where carot is in a component that has a carot
	 * 
	 * @param component
	 *            component to find carot position of
	 * @return line number
	 */
	public static int getLineAtCaret(JTextComponent component) {
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();

		return root.getElementIndex(caretPosition) + 1;
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Appends stylized text to a textpane
	 * 
	 * @param tp
	 *            Textpane
	 * @param msg
	 *            Message to append. Will automatically place newline at end.
	 * @param c
	 *            Color of text.
	 */
	public static void appendToPane(JTextPane tp, String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		int len = tp.getDocument().getLength(); // same value as
		// getText().length();
		tp.setCaretPosition(len); // place caret at the end (with no selection)

		StyledDocument doc = tp.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), msg + "\n", aset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts Newline characters to br tags
	 * 
	 * @param input
	 *            input string
	 * @return string with newlines replaced with br
	 */
	public static String convertNewlineToBr(String input) {
		return input.replaceAll("\n", "<br>");
	}

	/**
	 * Get the relative path from one file to another, specifying the directory
	 * separator. If one of the provided resources does not exist, it is assumed
	 * to be a file unless it ends with '/' or '\'.
	 * 
	 * @param targetPath
	 *            targetPath is calculated to this file
	 * @param basePath
	 *            basePath is calculated from this file
	 * @param pathSeparator
	 *            directory separator. The platform default is not assumed so
	 *            that we can test Unix behaviour when running on Windows (for
	 *            example)
	 * @return
	 */
	public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {

		// Normalize the paths
		String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
		String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

		// Undo the changes to the separators made by normalization
		if (pathSeparator.equals("/")) {
			normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

		} else if (pathSeparator.equals("\\")) {
			normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

		} else {
			throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
		}

		String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		StringBuffer common = new StringBuffer();

		int commonIndex = 0;
		while (commonIndex < target.length && commonIndex < base.length && target[commonIndex].equals(base[commonIndex])) {
			common.append(target[commonIndex] + pathSeparator);
			commonIndex++;
		}

		if (commonIndex == 0) {
			// No single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized.
			throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath + "'");
		}

		// The number of directories we have to backtrack depends on whether the
		// base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		//
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a
		// file or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but
		// it's the best I can do
		boolean baseIsFile = true;

		File baseResource = new File(normalizedBasePath);

		if (baseResource.exists()) {
			baseIsFile = baseResource.isFile();

		} else if (basePath.endsWith(pathSeparator)) {
			baseIsFile = false;
		}

		StringBuffer relative = new StringBuffer();

		if (base.length != commonIndex) {
			int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

			for (int i = 0; i < numDirsUp; i++) {
				relative.append(".." + pathSeparator);
			}
		}
		relative.append(normalizedTargetPath.substring(common.length()));
		return relative.toString();
	}

	public static class PathResolutionException extends RuntimeException {
		PathResolutionException(String msg) {
			super(msg);
		}
	}

	public static String getForwardSlashVersion(String str) {
		return str.replaceAll("\\\\", "/"); // string regex string again.
	}

	private final static String[] hexSymbols = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	public final static int BITS_PER_HEX_DIGIT = 4;

	public static String toHexFromByte(final byte b) {
		byte leftSymbol = (byte) ((b >>> BITS_PER_HEX_DIGIT) & 0x0f);
		byte rightSymbol = (byte) (b & 0x0f);

		return (hexSymbols[leftSymbol] + hexSymbols[rightSymbol]);
	}

	public static String toHexFromBytes(final byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return ("");
		}

		// there are 2 hex digits per byte
		StringBuilder hexBuffer = new StringBuilder(bytes.length * 2);

		// for each byte, convert it to hex and append it to the buffer
		for (int i = 0; i < bytes.length; i++) {
			hexBuffer.append(toHexFromByte(bytes[i]));
		}

		return (hexBuffer.toString());
	}

	public static String padLeadingZeros(String hexString, int length) {
		while (hexString.length() < length)
			hexString = "0" + hexString;
		return hexString;
	}

	public static int byteArrayToInt(byte[] buffer) {
		if (buffer.length != 4) {
			return -1;
		}
		return (buffer[0] << 24) & 0xff000000 | (buffer[1] << 16) & 0x00ff0000 | (buffer[2] << 8) & 0x0000ff00 | (buffer[3] << 0) & 0x000000ff;
	}

	/**
	 * Converts backslashes to forwardslashes, or vice versa
	 * 
	 * @param absolutePath
	 *            Path to convert
	 * @param backslash
	 *            Set to true to make backwards slash, false to make
	 *            forwardslashes
	 * @return
	 */
	public static String normalizeFilePath(String absolutePath, boolean backslash) {
		if (!backslash) {
			return absolutePath.replaceAll("\\\\", "/");
		}
		return absolutePath.replaceAll("/", "\\\\");
	}

	public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Error opening webpage:", e);
			}
		}
	}

	public static boolean openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
			return true;
		} catch (URISyntaxException e) {
			ModManager.debugLogger.writeErrorWithException("Error opening webpage: ", e);
			return false;
		}
	}

	/**
	 * Replaces all break (br between <>) lines with a newline character. Used
	 * to add newlines to ini4j.
	 * 
	 * @param string
	 *            String to parse
	 * @return String that has been fixed
	 */
	public static String convertBrToNewline(String string) {
		String br = "<br>";
		if (string == null) {
			return string;
		}
		return string.replaceAll(br, "\n");
	}

	/**
	 * Opens a web page from the specified string
	 * 
	 * @param url
	 *            URL
	 */
	public static void openWebpage(String url) {
		// TODO Auto-generated method stub
		try {
			openWebpage(new URL(url));
		} catch (MalformedURLException e) {
			ModManager.debugLogger.writeErrorWithException("Error opening page: " + url, e);
		}
	}

	public static ArrayList<File> createOneItemFileList(File path) {
		ArrayList<File> files = new ArrayList<>();
		files.add(path);
		return files;
	}

	/**
	 * Returns if this is 64-bit windows (OS, not VM).
	 * @return true if 64bit, false otherwise
	 */
	public static boolean is64BitWindows() {
		if (ModManager.FORCE_32BIT_MODE) {
			return false;
		}
		if (System.getProperty("os.name").contains("Windows")) {
			return (System.getenv("ProgramFiles(x86)") != null);
		} else {
			return (System.getProperty("os.arch").indexOf("64") != -1);
		}
	}

	/**
	 * Opens the specified folder in explorer. Do not quote the item being passed here, it will be quoted when run.
	 * @param folder folder path to open in explorer
	 */
	public static void openFolderInExplorer(String folder) {
		ModManager.runProcessDetached(new ProcessBuilder("explorer.exe", "\""+folder+"\""));
	}
	
	
	/**
	 * Attempts to calculate the size of a file or directory.
	 * 
	 * <p>
	 * Since the operation is non-atomic, the returned value may be inaccurate.
	 * However, this method is quick and does its best.
	 * @param path Path to calculate size of
	 * @param includeBackups Include files in folders that contain cmmbackups or end with .bak
	 */
	public static long GetDirectorySize(Path path, boolean includeBackups) {

	    final AtomicLong size = new AtomicLong(0);

	    try {
	        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	            @Override
	            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	            	if (!includeBackups && FilenameUtils.getExtension(file.toString()).equals(".bak")) {
	            		return FileVisitResult.CONTINUE; //don't include. bak
	            	}
	            	
	            	if (!includeBackups && file.toString().contains("cmmbackups")) {
	            		return FileVisitResult.CONTINUE; //don't include cmmbackup
	            	}
	            	
	                size.addAndGet(attrs.size());
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult visitFileFailed(Path file, IOException exc) {

	                System.out.println("skipped: " + file + " (" + exc + ")");
	                // Skip folders that can't be traversed
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

	                if (exc != null)
	                    System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
	                // Ignore errors traversing a folder
	                return FileVisitResult.CONTINUE;
	            }
	        });
	    } catch (IOException e) {
	        throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
	    }

	    return size.get();
	}
}