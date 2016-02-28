package com.me3tweaks.modmanager.objects;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.MountFileEditorWindow;

public class MountFile implements Comparable<MountFile> {
	@Override
	public String toString() {
		return "MountFile [filepath=" + filepath + ", tlkId1=" + tlkId1 + ", tlkId2=" + tlkId2 + ", mountFlag=" + mountFlag + ", mountPriority=" + mountPriority + "]";
	}

	private static int PRIORITY_OFFSET = 16;
	private static int MPSPFLAG_OFFSET = 24;
	private static int TLKOFFSET_1 = 28;
	private static int TLKOFFSET_2 = 32;
	private static final int MOUNTDLC_LENGTH = 108;
	private String associatedModName, associatedDLCName;
	public String getAssociatedModName() {
		if (associatedModName == null) {
			return "Unknown";
		}
		return associatedModName;
	}

	public void setAssociatedModName(String associatedModName) {
		this.associatedModName = associatedModName;
	}

	public String getAssociatedDLCName() {
		if (associatedDLCName == null) {
			return "Unknown";
		}
		return associatedDLCName;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getMountName() {
		return mountName;
	}

	public void setMountName(String mountName) {
		this.mountName = mountName;
	}

	public void setAssociatedDLCName(String associatedDLCName) {
		this.associatedDLCName = associatedDLCName;
	}

	private String filepath;
	private int tlkId1, tlkId2;
	private byte mountFlag;
	private int mountPriority;
	private boolean matchingTLKIds = true;
	private boolean correctSize = true;
	private boolean validMount = true;
	private String reason;
	private String mountName;

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public int getTlkId1() {
		return tlkId1;
	}

	public void setTlkId1(int tlkId1) {
		this.tlkId1 = tlkId1;
	}

	public int getTlkId2() {
		return tlkId2;
	}

	public void setTlkId2(int tlkId2) {
		this.tlkId2 = tlkId2;
	}

	public byte getMountFlag() {
		return mountFlag;
	}

	public void setMountFlag(byte mountFlag) {
		this.mountFlag = mountFlag;
	}

	public int getMountPriority() {
		return mountPriority;
	}

	public void setMountPriority(int mountPriority) {
		this.mountPriority = mountPriority;
	}

	public boolean isMatchingTLKIds() {
		return matchingTLKIds;
	}

	public void setMatchingTLKIds(boolean matchingTLKIds) {
		this.matchingTLKIds = matchingTLKIds;
	}

	public boolean isValidMount() {
		return validMount;
	}

	public void setValidMount(boolean validMount) {
		this.validMount = validMount;
	}

	public MountFile(String path) {
		if (!(new File(path).exists())) {
			return;
		}
		this.filepath = path;
		Path fpath = Paths.get(path);
		byte[] data;
		try {
			data = Files.readAllBytes(fpath);

			if (data.length != MOUNTDLC_LENGTH) {
				correctSize = false;
			}

			//MOUNT PRIORITY
			byte[] priorityArray = new byte[] { 0, 0, data[PRIORITY_OFFSET + 1], data[PRIORITY_OFFSET] };
			ByteBuffer wrapped = ByteBuffer.wrap(priorityArray);
			mountPriority = wrapped.getInt();

			//TLK
			byte[] tlkID1Array = new byte[] { data[TLKOFFSET_1 + 3], data[TLKOFFSET_1 + 2], data[TLKOFFSET_1 + 1], data[TLKOFFSET_1] };
			wrapped = ByteBuffer.wrap(tlkID1Array);
			tlkId1 = wrapped.getInt();

			byte[] tlkID2Array = new byte[] { data[TLKOFFSET_2 + 3], data[TLKOFFSET_2 + 2], data[TLKOFFSET_2 + 1], data[TLKOFFSET_2] };
			wrapped = ByteBuffer.wrap(tlkID2Array);
			int tlkId2 = wrapped.getInt();
			if (tlkId1 != tlkId2) {
				matchingTLKIds = false;
			}

			//MOUNT FLAG (8-BIT)
			mountFlag = data[MPSPFLAG_OFFSET];
		} catch (IOException e) {
			ModManager.debugLogger.writeMessage("Invalid mount file.");
			// TODO Auto-generated catch block
			validMount  = false;
			ModManager.debugLogger.writeErrorWithException("IOException reading mount file:", e);
		}
	}

	public MountFile(String dlcName, String reason) {
		this.mountName = dlcName;
		this.reason = reason;
	}

	public String getMountFlagString() {
		if (filepath != null) {
			return MountFileEditorWindow.getMountDescription(new File(this.filepath));
		} else {
			System.out.println("reas");
			return reason;
		}
	}

	@Override
	public int compareTo(MountFile other) {
		return new Integer(mountPriority).compareTo(other.mountPriority);
	}
}
