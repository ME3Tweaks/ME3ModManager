package com.me3tweaks.modmanager.objects;

public class ProcessResult {
	private int returnCode;
	private String output;
	private Exception error;

	/**
	 * Gets the return code of the process.
	 * @return return code from process
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * Returns if the process had an exception.
	 * @return true if error is not null, otherwise false
	 */
	public boolean hadError() {
		return error != null;
	}

	public ProcessResult(int returnCode, String output, Exception e) {
		super();
		this.returnCode = returnCode;
		this.output = output;
		this.error = e;
	}

	/**
	 * Returns the throwable that was thrown during running the process
	 * @return Throwable error, otherwise false if no error.
	 */
	public Throwable getError() {
		return error;
	}

	public String getOutput() {
		return output;
	}
}
