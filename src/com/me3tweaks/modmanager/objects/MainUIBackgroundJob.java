package com.me3tweaks.modmanager.objects;

public class MainUIBackgroundJob {
	private String uiText;
	private String taskName;

	public MainUIBackgroundJob(String taskname, String uiText) {
		this.taskName = taskName;
		this.uiText = uiText;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MainUIBackgroundJob other = (MainUIBackgroundJob) obj;
		return hashCode() == other.hashCode();
	}

	public String getTaskName() {
		return taskName;
	}
	public String getUIText() {
		return uiText;
	}


	public MainUIBackgroundJob(String taskName) {
		this.taskName = taskName;
	}
}
