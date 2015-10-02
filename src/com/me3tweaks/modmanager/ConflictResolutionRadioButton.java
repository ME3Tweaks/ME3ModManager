package com.me3tweaks.modmanager;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButton;

public class ConflictResolutionRadioButton extends JRadioButton {
	public static final int REPLACECONFLICT = 0;
	public static final int ADDCONFLICT = 1;
	public static final int REPLACEREMOVECONFLICT = 2;
	public static final int ADDREMOVECONFLICT = 3;
	
	public static final int REPLACE = 0;
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	
	public int getConflictType() {
		return conflictType;
	}

	public void setConflictType(int conflictType) {
		this.conflictType = conflictType;
	}

	public int conflictType = -1;
	public boolean isLeft = true;
	public String module;
	public String conflictTarget;
	public String sourcePath;

	public boolean isLeft() {
		return isLeft;
	}

	public void setLeft(boolean isLeft) {
		this.isLeft = isLeft;
	}

	public String getModule() {
		return module;
	}

	public ConflictResolutionRadioButton() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(Icon icon, boolean selected) {
		super(icon, selected);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(String text, boolean selected) {
		super(text, selected);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public ConflictResolutionRadioButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getConflictTarget() {
		return conflictTarget;
	}

	public void setConflictTarget(String conflictTarget) {
		this.conflictTarget = conflictTarget;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
}
