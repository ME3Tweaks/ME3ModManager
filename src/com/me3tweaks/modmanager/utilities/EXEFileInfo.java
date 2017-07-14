package com.me3tweaks.modmanager.utilities;

	import com.me3tweaks.modmanager.ModManager;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
	
	public class EXEFileInfo {
		public static int MAJOR = 0;
		public static int MINOR = 1;
		public static int BUILD = 2;
		public static int REVISION = 3;
	
		public static int getMajorVersionOfProgram(String path) {
			return getVersionInfo(path)[MAJOR];
		}
	
		public static int getMinorVersionOfProgram(String path) {
			return getVersionInfo(path)[MINOR];
		}
	
		public static int getBuildOfProgram(String path) {
			return getVersionInfo(path)[BUILD];
		}
	
		public static int getRevisionOfProgram(String path) {
			return getVersionInfo(path)[REVISION];
		}
	
		public static int[] getVersionInfo(String path) {
			IntByReference dwDummy = new IntByReference();
			dwDummy.setValue(0);
	
			int versionlength = com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfoSize(path, dwDummy);
	
			byte[] bufferarray = new byte[versionlength];
			Pointer lpData = new Memory(bufferarray.length);
			PointerByReference lplpBuffer = new PointerByReference();
			IntByReference puLen = new IntByReference();
			boolean fileInfoResult = com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfo(path, 0, versionlength, lpData);
			boolean verQueryVal = com.sun.jna.platform.win32.Version.INSTANCE.VerQueryValue(lpData, "\\", lplpBuffer, puLen);
	
			VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
			lplpBufStructure.read();
	
			int v1 = (lplpBufStructure.dwFileVersionMS).intValue() >> 16;
			int v2 = (lplpBufStructure.dwFileVersionMS).intValue() & 0xffff;
			int v3 = (lplpBufStructure.dwFileVersionLS).intValue() >> 16;
			int v4 = (lplpBufStructure.dwFileVersionLS).intValue() & 0xffff;
			return new int[] { v1, v2, v3, v4 };
		}
		
		/**
		 * Compares two version strings. 
		 * 
		 * Use this instead of String.compareTo() for a non-lexicographical 
		 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
		 * 
		 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
		 * 
		 * @param str1 a string of ordinal numbers separated by decimal points. 
		 * @param str2 a string of ordinal numbers separated by decimal points.
		 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
		 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
		 *         The result is zero if the strings are _numerically_ equal.
		 */
		public static int versionCompare(String str1, String str2) {
		    String[] vals1 = str1.split("\\.");
		    String[] vals2 = str2.split("\\.");
		    int i = 0;
		    // set index to first non-equal ordinal or length of shortest version string
		    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
		      i++;
		    }
		    // compare first non-equal ordinal number
		    if (i < vals1.length && i < vals2.length) {
		        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
		        return Integer.signum(diff);
		    }
		    // the strings are equal or one string is a substring of the other
		    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		    return Integer.signum(vals1.length - vals2.length);
		}
	}
