package main.java.com.github.rashnain.launcherfx;

/**
 * Profile types that can be used
 */
public enum PROFILE_TYPE {
	/**
	 * Profile bound to the actual latest release version
	 */
	LATEST_RELEASE,

	/**
	 * Profile bound to the actual latest snapshot version
	 */
	LATEST_SNAPSHOT,

	/**
	 * Profile that can use any version
	 */
	CUSTOM;

	/**
	 * Return the string corresponding to the given type<br>
	 * Ex: LATEST_RELEASE -> "latest-release"
	 * @param type The given type
	 * @return The corresponding string or "custom"
	 */
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

	/**
	 * Return the enum corresponding to the given type<br>
	 * Ex: "latest-release" -> LATEST_RELEASE
	 * @param type The given type
	 * @return The corresponding enum or CUSTOM
	 */
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
