package main.java.com.github.rashnain.launcherfx.model;

import java.util.Date;
import java.util.Random;

import main.java.com.github.rashnain.launcherfx.LauncherFX;

public class GameProfile {
	
	public static final String defaultGameDir = LauncherProfile.getProfile().getDataDir();
	
	public static final int[] defaultResolution = {880, 480};
	
	public static final String defaultJava = "java";
	
	public static final String defaultJvmArgs = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
	
	private String name;
	
	private String versionId;
	
	private VERSION_TYPE versionType;

	private String gameDir;
	
	private int[] resolution = new int[2];
	
	private String executable;
	
	private String jvmArguments;
	
	private Date lastUsed;
	
	private String identifier;
	
	public GameProfile(String name, String versionId, Date lastUsed, VERSION_TYPE type) {
		this.name = name;
		this.versionId = versionId;
		this.lastUsed = lastUsed;
		this.versionType = type;
		this.gameDir = defaultGameDir;
		this.resolution[0] = defaultResolution[0];
		this.resolution[1] = defaultResolution[1];
		this.executable = defaultJava;
		this.jvmArguments = defaultJvmArgs;
		this.identifier = getUniqueIdentifier();
	}
	
	public String toString() {
		if (this.name.equals("")) {
			if (this.versionType == VERSION_TYPE.LATEST_RELEASE) {
				return LauncherFX.resources.getString("profile.editor.name.lastest.release");
			} else if (this.versionType == VERSION_TYPE.LATEST_SNAPSHOT) {
				return LauncherFX.resources.getString("profile.editor.name.lastest.snapshot");
			}
			return LauncherFX.resources.getString("profile.editor.name.default");
		}
		return this.name;
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

	public String getEditableName() {
		if (this.versionType != VERSION_TYPE.CUSTOM) {
			return toString();
		}
		return this.name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersionId() {
		return this.versionId;
	}
	
	public void setVersionId(String id) {
		this.versionId = id;
	}

	public VERSION_TYPE getVersionType() {
		return this.versionType;
	}
	
	public VERSION_TYPE getVersionTypeAsString() {
		return this.versionType;
	}

	public String getGameDir() {
		if (this.gameDir == defaultGameDir) {
			return "";
		}
		return this.gameDir;
	}

	public void setGameDir(String directory) {
		this.gameDir = directory;
	}

	public int[] getResolution() {
		return this.resolution;
	}

	public void setResolution(int width, int height) {
		this.resolution[0] = width;
		this.resolution[1] = height;
	}

	public String getWitdth() {
		if (this.resolution[0] == defaultResolution[0]) {
			return "";
		}
		return String.valueOf(this.resolution[0]);
	}
	
	public String getHeight() {
		if (this.resolution[1] == defaultResolution[1]) {
			return "";
		}
		return String.valueOf(this.resolution[1]);
	}

	public String getExecutable() {
		if (this.executable == defaultJava) {
			return "";
		}
		return this.executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getEditableJvmArguments() {
		return this.jvmArguments;
	}

	public String getJvmArguments() {
		if (this.jvmArguments == defaultJvmArgs) {
			return "";
		}
		return this.jvmArguments;
	}
	
	public void setJvmArguments(String jvmArguments) {
		this.jvmArguments = jvmArguments;
	}

	public Date getLastUsed() {
		return this.lastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
