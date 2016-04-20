package com.me3tweaks.modmanager.modupdater;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import com.me3tweaks.modmanager.ModManager;

public class LZMATest {
	public static void main(String[] args) {
		String[] realargs = { "test.lzma" };
		args = realargs;
		if (args.length == 0) {
			System.out.println("Usage: java ExtractItemsSimple <archive-name>");
			return;
		}
		
	}
}
