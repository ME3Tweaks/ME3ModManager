package com.me3tweaks.modmanager.objects;

/**
 * Defines an object that contains a command and optionally a message for passing from background to UI threads
 * @author mjperez
 *
 */
public class ThreadCommand {
	public final static String COMMAND_ERROR = "ERROR";
	
	private String command;
	private String message;
	private Object data;
	public ThreadCommand(String command, String message) {
		super();
		this.command = command;
		this.message = message;
	}
	public ThreadCommand(String command, String message, Object data) {
		super();
		this.command = command;
		this.message = message;
		this.data = data;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
}
