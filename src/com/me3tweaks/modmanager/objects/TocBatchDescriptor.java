package com.me3tweaks.modmanager.objects;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Describes a batch toc job, containing a list of search terms (for toceditor in me3exp) mapped to the size to set them to
 * @author Michael
 *
 */
public class TocBatchDescriptor {
	private ArrayList<AbstractMap.SimpleEntry<String, Long>> nameSizePairs;

	public ArrayList<AbstractMap.SimpleEntry<String, Long>> getNameSizePairs() {
		return nameSizePairs;
	}

	public void setNameSizePairs(ArrayList<AbstractMap.SimpleEntry<String, Long>> nameSizePair) {
		this.nameSizePairs = nameSizePair;
	}

	public void addNameSizePair(String tocupdatesearchterm, long size) {
		AbstractMap.SimpleEntry<String, Long> newpair = new AbstractMap.SimpleEntry<String, Long>(tocupdatesearchterm, size);
		nameSizePairs.add(newpair);
	}

	public TocBatchDescriptor() {
		nameSizePairs = new ArrayList<AbstractMap.SimpleEntry<String, Long>>();
	}
}
