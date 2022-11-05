package main.java.com.github.rashnain.launcherfx;

public enum PROFILE_TYPE {
	CUSTOM, LATEST_RELEASE, LATEST_SNAPSHOT;

	public static String getAsString(PROFILE_TYPE type) {
		switch (type) {
		case LATEST_RELEASE:
			return "latest-release";
		case LATEST_SNAPSHOT:
			return "latest-snapshot";
		default:
			return "custom";
		}
	}

	public static PROFILE_TYPE getAsType(String type) {
		switch (type) {
		case ("latest-release"):
			return PROFILE_TYPE.LATEST_RELEASE;
		case ("latest-snapshot"):
			return PROFILE_TYPE.LATEST_SNAPSHOT;
		default:
			return PROFILE_TYPE.CUSTOM;
		}
	}
}
