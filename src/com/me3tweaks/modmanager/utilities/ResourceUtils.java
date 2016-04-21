package com.me3tweaks.modmanager.utilities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager;

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

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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

		// The number of directories we have to backtrack depends on whether the base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		// 
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but it's the best I can do
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

	static class PathResolutionException extends RuntimeException {
		PathResolutionException(String msg) {
			super(msg);
		}
	}

	public static String getForwardSlashVersion(String str) {
		return str.replaceAll("\\\\", "/"); //string regex string again.
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

	/*
	 * public static void main(final String[] args) throws IOException { try
	 * 
	 * }
	 */
}