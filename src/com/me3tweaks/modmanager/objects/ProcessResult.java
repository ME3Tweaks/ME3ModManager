package com.me3tweaks.modmanager.objects;

public class ProcessResult {
	private int returnCode;
	private Exception error;

	public int getReturnCode() {
		return returnCode;
	}

	public boolean hadError() {
		return error != null;
	}

	public ProcessResult(int returnCode, Exception e) {
		super();
		this.returnCode = returnCode;
		this.error = e;
	}

	public Throwable getError() {
		return error;
	}
}
