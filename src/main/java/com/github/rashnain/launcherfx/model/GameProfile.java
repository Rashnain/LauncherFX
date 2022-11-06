package main.java.com.github.rashnain.launcherfx.model;

import java.time.Instant;
import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.com.github.rashnain.launcherfx.Main;
import main.java.com.github.rashnain.launcherfx.PROFILE_TYPE;

/**
 * Class representing a game profile
 */
public class GameProfile {

	private static final String defaultGameDir = LauncherProfile.getProfile().getWorkDir();

	private static final String defaultWidth = "880";

	private static final String defaultHeight = "480";

	private static final String defaultExe = "java";

	private static final String defaultJvmArgs = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

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
		this.gameDir = new SimpleStringProperty(defaultGameDir);
		this.jvmArguments = new SimpleStringProperty(defaultJvmArgs);
		this.executable = new SimpleStringProperty(defaultExe);
		this.lastUsed = lastUsed;
		this.version = new SimpleStringProperty(version);
		this.name = new SimpleStringProperty(name);
		this.width = new SimpleStringProperty(defaultWidth);
		this.height = new SimpleStringProperty(defaultHeight);
		this.profileType = type;
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
		if (this.name.get().equals("")) {
			if (this.profileType == PROFILE_TYPE.LATEST_RELEASE) {
				return Main.getResources().getString("profile.editor.name.lastest.release");
			} else if (this.profileType == PROFILE_TYPE.LATEST_SNAPSHOT) {
				return Main.getResources().getString("profile.editor.name.lastest.snapshot");
			}
			return Main.getResources().getString("profile.editor.name.default");
		}
		return this.name.get();
	}

	/**
	 * @return a unique identifier
	 */
	private static String getUniqueIdentifier() {
		String identifier;
		do {
			identifier = UUID.randomUUID().toString().replace("-", "");
			System.out.println(identifier);
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
		return this.gameDir.get();
	}

	public String getEditableGameDir() {
		if (this.gameDir.get() == defaultGameDir) {
			return "";
		}
		return this.gameDir.get();
	}

	public String getGameDirOrDefault() {
		if (this.gameDir.get().equals("")) {
			return defaultGameDir;
		}
		return this.gameDir.get();
	}

	public StringProperty getGameDirProperty() {
		return this.gameDir;
	}

	public void setGameDir(String directory) {
		this.gameDir.set(directory);
	}

	public String getJvmArguments() {
		return this.jvmArguments.get();
	}

	public String getEditableJvmArguments() {
		if (this.jvmArguments.get().equals(defaultJvmArgs)) {
			return "";
		}
		return this.jvmArguments.get();
	}

	public StringProperty getJvmArgumentsProperty() {
		return this.jvmArguments;
	}

	public void setJvmArguments(String jvmArguments) {
		this.jvmArguments.set(jvmArguments);
	}

	public String getExecutable() {
		return this.executable.get();
	}

	public String getEditableExecutable() {
		if (this.executable.get() == defaultExe) {
			return "";
		}
		return this.executable.get();
	}

	public String getExecutableOrDefault() {
		if (this.executable.get().equals("")) {
			return defaultExe;
		}
		return this.executable.get();
	}

	public StringProperty getExecutableProperty() {
		return this.executable;
	}

	public void setExecutable(String executable) {
		this.executable.set(executable);
	}

	public Instant getLastUsed() {
		return this.lastUsed;
	}

	public void setLastUsed(Instant lastUsed) {
		this.lastUsed = lastUsed;
	}

	public String getVersion() {
		return this.version.get();
	}

	public StringProperty getVersionProperty() {
		return this.version;
	}

	public void setVersion(String id) {
		this.version.set(id);
	}

	public String getName() {
		return this.name.get();
	}

	public StringProperty getNameProperty() {
		return this.name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getWidth() {
		return this.width.get();
	}

	public String getEditableWidth() {
		if (this.width.get() == defaultWidth) {
			return "";
		}
		return this.width.get();
	}

	public String getWidthOrDefault() {
		if (this.width.get().equals("")) {
			return defaultWidth;
		}
		return this.width.get();
	}

	public StringProperty getWidthProperty() {
		return this.width;
	}

	public void setWidth(String width) {
		this.width.set(width);
	}

	public void setWidth(int width) {
		this.width.set(String.valueOf(width));
	}

	public String getHeight() {
		return this.height.get();
	}

	public String getEditableHeight() {
		if (this.height.get() == defaultHeight) {
			return "";
		}
		return this.height.get();
	}

	public String getHeightOrDefault() {
		if (this.height.get().equals("")) {
			return defaultHeight;
		}
		return this.height.get();
	}

	public StringProperty getHeightProperty() {
		return this.height;
	}

	public void setHeight(String height) {
		this.height.set(height);
	}

	public void setHeight(int height) {
		this.height.set(String.valueOf(height));
	}

	public PROFILE_TYPE getVersionType() {
		return this.profileType;
	}

}
