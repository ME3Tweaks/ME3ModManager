package com.me3tweaks.modmanager.utilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Extract7zCallback {
    public static class MyExtractCallback implements IArchiveExtractCallback {
        private int hash = 0;
        private int size = 0;
        private int index;
        private IInArchive inArchive;
        //private HashMap<>

        public MyExtractCallback(IInArchive inArchive) {
            this.inArchive = inArchive;
        }

        public ISequentialOutStream getStream(int index, 
                ExtractAskMode extractAskMode) throws SevenZipException {
            this.index = index;
            if (extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }
            return new ISequentialOutStream() {

                public int write(byte[] data) throws SevenZipException {
                    hash ^= Arrays.hashCode(data);
                    size += data.length;
                    
                    String outputDirectory = (String) inArchive.getProperty(index, PropID.PATH);
                    //if ()
                    try {
                    	FileUtils.writeByteArrayToFile(new File("D:/"), data);
                    } catch (IOException e) {
                    	// TODO Auto-generated catch block
                    	e.printStackTrace();
                    }
                    return data.length; // Return amount of proceed data
                }
            };
        }

        public void prepareOperation(ExtractAskMode extractAskMode) 
                throws SevenZipException {
        }

        public void setOperationResult(ExtractOperationResult 
                extractOperationResult) throws SevenZipException {
            if (extractOperationResult != ExtractOperationResult.OK) {
                System.err.println("Extraction error");
            } else {
                System.out.println(String.format("%9X | %10s | %s", hash, size,
                        inArchive.getProperty(index, PropID.PATH)));
                hash = 0;
                size = 0;
            }
        }

        public void setCompleted(long completeValue) throws SevenZipException {
        }

        public void setTotal(long total) throws SevenZipException {
        }

    }

    public static void main(String[] args) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        try {
        	randomAccessFile = new RandomAccessFile("D:/testmod.7z", "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));

            System.out.println("   Hash   |    Size    | Filename");
            System.out.println("----------+------------+---------");

            int count = inArchive.getNumberOfItems();
            List<Integer> itemsToExtract = new ArrayList<Integer>();
            //1st pass - find moddesc.ini files.
            for (int i = 0; i < count; i++) {
            	String path = (String) inArchive.getProperty(i,PropID.PATH);
            	if (FilenameUtils.getName(path).equalsIgnoreCase("moddesc.ini")) {
            		System.out.println("Moddesc in archive: "+path);
            		itemsToExtract.add(Integer.valueOf(i));
                }
            }
            int[] items = new int[itemsToExtract.size()];
            int i = 0;
            for (Integer integer : itemsToExtract) {
                items[i++] = integer.intValue();
            }
            inArchive.extract(items, false, // Non-test mode
                    new MyExtractCallback(inArchive));
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