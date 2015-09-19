package com.me3tweaks.modmanager;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

public class MountFileEditor {
		private static boolean ISRUNNINGASMAIN = false;
		private byte[] mountBytes = DatatypeConverter.parseHexBinary("01000000AC020000C20000006B0003008C0A0000000000001C00000092190B0092190B00331500000B0000005A7BBD26DD417E499CC660D2587278EB2E2C6A06130AE44783EA08F387A0E2DA0000000000000000000000000000000000000000000000000000000000000000");
		private static int PRIORITY_OFFSET = 16;

		private static int MPSPFLAG_OFFSET = 24;
		public MountFileEditor(){
			System.out.println(Integer.toHexString(mountBytes[MPSPFLAG_OFFSET]));
		}
		
/*		private void generateMountByteArray() {
			// TODO Auto-generated method stub
			String byteText = "01 00 00 00 AC 02 00 00 C2 00 00 00 6B 00 03 00 8C 0A 00 00 00 00 00 00 1C 00 00 00 92 19 0B 00 92 19 0B 00 33 15 00 00 0B 00 00 00 5A 7B BD 26 DD 41 7E 49 9C C6 60 D2 58 72 78 EB 2E 2C 6A 06 13 0A E4 47 83 EA 08 F3 87 A0 E2 DA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
			String nospacebyteText = "01000000AC020000C20000006B0003008C0A0000000000001C00000092190B0092190B00331500000B0000005A7BBD26DD417E499CC660D2587278EB2E2C6A06130AE44783EA08F387A0E2DA0000000000000000000000000000000000000000000000000000000000000000";
			StringTokenizer strok = new StringTokenizer(byteText, " ");
			ArrayList<Byte> byteList = new ArrayList<Byte>();
			int index = 0;
			while (strok.hasMoreElements()) {
				String tok = strok.nextToken();
				byteList.add(Byte.parseByte(tok,10));
				System.out.println(Integer.toHexString(index)+": "+tok);
			}
		}*/

		public static void main(String[] args) {
			MountFileEditor.ISRUNNINGASMAIN  = true;
			new MountFileEditor();
	}
}
