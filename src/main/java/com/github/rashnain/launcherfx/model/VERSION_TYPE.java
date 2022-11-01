package main.java.com.github.rashnain.launcherfx.model;

public enum VERSION_TYPE {
	CUSTOM, LATEST_RELEASE, LATEST_SNAPSHOT;
	
	public static String getAsString(VERSION_TYPE type) {
		switch (type) {
		case LATEST_RELEASE:
			return "latest-release";
		case LATEST_SNAPSHOT:
			return "latest-snapshot";
		default:
			return "custom";
		}
	}
	
	public static VERSION_TYPE getAsType(String type) {
		switch (type) {
		case ("latest-release"):
			return VERSION_TYPE.LATEST_RELEASE;
		case ("latest-snapshot"):
			return VERSION_TYPE.LATEST_SNAPSHOT;
		default:
			return VERSION_TYPE.CUSTOM;
		}
	}
}
