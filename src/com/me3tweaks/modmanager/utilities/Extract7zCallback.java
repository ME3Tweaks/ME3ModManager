package com.me3tweaks.modmanager.utilities;import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class Extract7zCallback {
    public static void main(String[] args) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        try {
            randomAccessFile = new RandomAccessFile("C:\\Users\\Michael\\workspace\\modmanager3\\mods\\expandedgalaxymod_controllercompat102.7z", "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));

            // Getting simple interface of the archive inArchive
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            System.out.println("   Hash   |    Size    | Filename");
            System.out.println("----------+------------+---------");

            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                //final int[] hash = new int[] { 0 };
                if (!item.isFolder()) {
                    ExtractOperationResult result;

                    final long[] sizeArray = new long[1];
                    result = item.extractSlow(new ISequentialOutStream() {
                        public int write(byte[] data) throws SevenZipException {
                            //hash[0] ^= Arrays.hashCode(data); // Consume data
                            sizeArray[0] += data.length;
                            
                            try {
								FileUtils.writeByteArrayToFile(new File(item.getPath()), data);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            return data.length; // Return amount of consumed data
                        }
                    });

                    if (result == ExtractOperationResult.OK) {
                        System.out.println(String.format("%s | %10s | %s", 
                                "ni", sizeArray[0], item.getPath()));
                    } else {
                        System.err.println("Error extracting item: " + result);
                    }
                }
            }
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
}