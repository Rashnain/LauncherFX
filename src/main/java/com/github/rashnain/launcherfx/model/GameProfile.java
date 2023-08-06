package com.github.rashnain.launcherfx.model;

import com.github.rashnain.launcherfx.Main;
import com.github.rashnain.launcherfx.PROFILE_TYPE;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Class representing a game profile
 */
public class GameProfile {

	private static final String defaultGameDir = LauncherProfile.getProfile().getWorkDir();

	private static final String defaultWidth = "880";

	private static final String defaultHeight = "480";

	private static final String defaultExe = "java";

	private static final String defaultJvmArgs = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

	public static String latestRelease = "latest-release";

	public static String latestSnapshot = "latest-snapshot";

	private String identifier;

	private StringProperty gameDir;

	private StringProperty jvmArguments;

	private StringProperty executable;

	private Instant lastUsed;

	private StringProperty version;

	private StringProperty name;

	private StringProperty width;

	private StringProperty height;

	private PROFILE_TYPE profileType;

	/**
	 * Creates a game profile with a specified identifier, last used time, version, name and profile type
	 * @param identifier
	 * @param lastUsed
	 * @param version
	 * @param name
	 * @param type
	 */
	public GameProfile(String identifier, Instant lastUsed, String version, String name, PROFILE_TYPE type) {
		this.identifier = identifier;
		gameDir = new SimpleStringProperty(defaultGameDir);
		jvmArguments = new SimpleStringProperty(defaultJvmArgs);
		executable = new SimpleStringProperty(defaultExe);
		this.lastUsed = lastUsed;
		this.version = new SimpleStringProperty(version);
		this.name = new SimpleStringProperty(name);
		width = new SimpleStringProperty(defaultWidth);
		height = new SimpleStringProperty(defaultHeight);
		profileType = type;
	}

	/**
	 * Creates a game profile with a specified last used time, version, name and profile type
	 * @param lastUsed
	 * @param version
	 * @param name
	 * @param type
	 */
	public GameProfile(Instant lastUsed, String version, String name, PROFILE_TYPE type) {
		this(getUniqueIdentifier(), lastUsed, version, name, type);
	}

	/**
	 * Creates a game profile without name and set to the latest release version
	 */
	public GameProfile() {
		this(getUniqueIdentifier(), Instant.EPOCH, "latest-release", "", PROFILE_TYPE.CUSTOM);
	}

	/**
	 * Returns a string representation of the object.<br>
	 * For a GameProfile it returns the version attribut
	 */
	public String toString() {
		if (name.get().isEmpty()) {
			if (profileType == PROFILE_TYPE.LATEST_RELEASE) {
				return Main.getResources().getString("profile.editor.name.lastest.release");
			} else if (profileType == PROFILE_TYPE.LATEST_SNAPSHOT) {
				return Main.getResources().getString("profile.editor.name.lastest.snapshot");
			}
			return Main.getResources().getString("profile.editor.name.default");
		}
		return name.get();
	}

	/**
	 * @return a unique identifier
	 */
	private static String getUniqueIdentifier() {
		String identifier;
		do {
			identifier = UUID.randomUUID().toString().replace("-", "");
		} while (!isUniqueIdentifier(identifier));
		return identifier;
	}

	/**
	 * Returns wether the given identifier is unique among the existing profiles' identifiers
	 * @param identifier
	 * @return true if its unique, false otherwise
	 */
	private static boolean isUniqueIdentifier(String identifier) {
		for (GameProfile e : LauncherProfile.getProfile().getGameProfiles()) {
			if (e.getIdentifier().equals(identifier)) {
				return false;
			}
		}
		return true;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getGameDir() {
		return gameDir.get();
	}

	public String getEditableGameDir() {
		if (gameDir.get() == defaultGameDir) {
			return "";
		}
		return gameDir.get();
	}

	public String getGameDirOrDefault() {
		if (gameDir.get().isEmpty()) {
			return defaultGameDir;
		}
		return gameDir.get();
	}

	public StringProperty getGameDirProperty() {
		return gameDir;
	}

	public void setGameDir(String directory) {
		gameDir.set(directory);
	}

	public String getJvmArguments() {
		return jvmArguments.get();
	}

	public String getEditableJvmArguments() {
		if (jvmArguments.get().equals(defaultJvmArgs)) {
			return "";
		}
		return jvmArguments.get();
	}

	public StringProperty getJvmArgumentsProperty() {
		return jvmArguments;
	}

	public void setJvmArguments(String jvmArgs) {
		jvmArguments.set(jvmArgs);
	}

	public String getExecutable() {
		return executable.get();
	}

	public String getEditableExecutable() {
		if (executable.get() == defaultExe) {
			return "";
		}
		return executable.get();
	}

	public String getExecutableOrDefault() {
		if (executable.get().isEmpty()) {
			return defaultExe;
		}
		return executable.get();
	}

	public StringProperty getExecutableProperty() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable.set(executable);
	}

	public Instant getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Instant lastUsed) {
		this.lastUsed = lastUsed;
	}

	public String getVersion() {
		if (profileType == PROFILE_TYPE.LATEST_RELEASE) {
			return latestRelease;
		} else if (profileType == PROFILE_TYPE.LATEST_SNAPSHOT) {
			return latestSnapshot;
		}
		return version.get();
	}

	public StringProperty getVersionProperty() {
		return version;
	}

	public void setVersion(String id) {
		version.set(id);
	}

	public String getName() {
		return name.get();
	}

	public StringProperty getNameProperty() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getWidth() {
		return width.get();
	}

	public String getEditableWidth() {
		if (width.get() == defaultWidth) {
			return "";
		}
		return width.get();
	}

	public String getWidthOrDefault() {
		if (width.get().isEmpty()) {
			return defaultWidth;
		}
		return width.get();
	}

	public StringProperty getWidthProperty() {
		return width;
	}

	public void setWidth(String width) {
		this.width.set(width);
	}

	public void setWidth(int width) {
		this.width.set(String.valueOf(width));
	}

	public String getHeight() {
		return height.get();
	}

	public String getEditableHeight() {
		if (height.get() == defaultHeight) {
			return "";
		}
		return height.get();
	}

	public String getHeightOrDefault() {
		if (height.get().isEmpty()) {
			return defaultHeight;
		}
		return height.get();
	}

	public StringProperty getHeightProperty() {
		return height;
	}

	public void setHeight(String height) {
		this.height.set(height);
	}

	public void setHeight(int height) {
		this.height.set(String.valueOf(height));
	}

	public PROFILE_TYPE getVersionType() {
		return profileType;
	}

}
