package main.java.com.github.rashnain.launcherfx.model;

import java.time.Instant;
import java.util.Random;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.com.github.rashnain.launcherfx.LauncherFX;

public class GameProfile {
	
	private static final String defaultGameDir = LauncherProfile.getProfile().getDataDir();
	
	private static final String defaultWidth = "880";
	
	private static final String defaultHeight = "480";
	
	private static final String defaultExe = "java";
	
	private static final String defaultJvmArgs = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
	
	private StringProperty name;
	
	private StringProperty versionId;
	
	private VERSION_TYPE profileType;

	private StringProperty gameDir;
	
	private StringProperty width;
	
	private StringProperty height;
	
	private StringProperty executable;
	
	private StringProperty jvmArguments;
	
	private Instant lastUsed;
	
	private String identifier;
	
	public GameProfile(String name, String versionId, Instant lastUsed, VERSION_TYPE type) {
		this.name = new SimpleStringProperty(name);
		this.versionId = new SimpleStringProperty(versionId);
		this.lastUsed = lastUsed;
		this.profileType = type;
		this.gameDir = new SimpleStringProperty(defaultGameDir);
		this.width = new SimpleStringProperty(defaultWidth);
		this.height = new SimpleStringProperty(defaultHeight);
		this.executable = new SimpleStringProperty(defaultExe);
		this.jvmArguments = new SimpleStringProperty(defaultJvmArgs);
		this.identifier = getUniqueIdentifier();
	}
	
	public String toString() {
		if (this.name.get().equals("")) {
			if (this.profileType == VERSION_TYPE.LATEST_RELEASE) {
				return LauncherFX.getResources().getString("profile.editor.name.lastest.release");
			} else if (this.profileType == VERSION_TYPE.LATEST_SNAPSHOT) {
				return LauncherFX.getResources().getString("profile.editor.name.lastest.snapshot");
			}
			return LauncherFX.getResources().getString("profile.editor.name.default");
		}
		return this.name.get();
	}
	
	private String getUniqueIdentifier() {
		String identifier;
		do {
			identifier = String.valueOf(new Random().nextInt());
		} while (!isAvailable(identifier));
		return identifier;
	}
	
	private boolean isAvailable(String identifier) {
		for (GameProfile e : LauncherProfile.getProfile().getGameProfiles()) {
			if (e.getIdentifier().equals(identifier)) {
				return false;
			}
		}
		return true;
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
	
	public String getVersionId() {
		return this.versionId.get();
	}
	
	public StringProperty getVersionIdProperty() {
		return this.versionId;
	}
	
	public void setVersionId(String id) {
		this.versionId.set(id);
	}

	public VERSION_TYPE getVersionType() {
		return this.profileType;
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
	
	public StringProperty getGameDirProperty() {
		return this.gameDir;
	}

	public void setGameDir(String directory) {
		this.gameDir.set(directory);
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
	
	public StringProperty getWidthProperty() {
		return this.width;
	}

	public void setWidth(String width) {
		this.width.set(width);
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
	
	public StringProperty getHeightProperty() {
		return this.height;
	}
	
	public void setWidth(int width) {
		this.width.set(String.valueOf(width));
	}
	
	public void setHeight(int height) {
		this.height.set(String.valueOf(height));
	}
	
	public void setHeight(String height) {
		this.height.set(height);
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
	
	public StringProperty getExecutableProperty() {
		return this.executable;
	}
	
	public void setExecutable(String executable) {
		this.executable.set(executable);
	}
	
	public String getEditableJvmArguments() {
		if (this.jvmArguments.get().equals(defaultJvmArgs)) {
			return "";
		}
		return this.jvmArguments.get();
	}
	
	public String getJvmArguments() {
		return this.jvmArguments.get();
	}
	
	public StringProperty getJvmArgumentsProperty() {
		return this.jvmArguments;
	}
	
	public void setJvmArguments(String jvmArguments) {
		this.jvmArguments.set(jvmArguments);
	}

	public Instant getLastUsed() {
		return this.lastUsed;
	}

	public void setLastUsed(Instant lastUsed) {
		this.lastUsed = lastUsed;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
