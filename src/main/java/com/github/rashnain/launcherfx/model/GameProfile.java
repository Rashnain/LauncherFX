package main.java.com.github.rashnain.launcherfx.model;

import java.util.Date;

import main.java.com.github.rashnain.launcherfx.LauncherFX;

public class GameProfile {
	
	public static final String defaultGameDir = LauncherProfile.getProfile().getDataDir();
	
	public static final int[] defaultResolution = {880, 480};
	
	public static final String defaultJava = "java";
	
	public enum VERSIONTYPE {
		CUSTOM, LATEST_RELEASE, LATEST_SNAPSHOT
	}
	
	private String name;
	
	private String versionId;
	
	private VERSIONTYPE versionType;

	private String gameDir;
	
	private int[] resolution = new int[2];
	
	private String executable;
	
	private String jvmArguments;
	
	private Date lastUsed;
	
	public GameProfile(String name, String versionId, Date lastUsed, VERSIONTYPE type) {
		this.name = name;
		this.versionId = versionId;
		this.lastUsed = lastUsed;
		this.versionType = type;
		this.gameDir = defaultGameDir;
		this.resolution = defaultResolution;
		this.executable = defaultJava;
		this.jvmArguments = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
	}
	
	public String toString() {
		if (this.name.equals("")) {
			if (this.versionType == VERSIONTYPE.LATEST_RELEASE) {
				return LauncherFX.resources.getString("profile.editor.name.lastest.release");
			} else if (this.versionType == VERSIONTYPE.LATEST_SNAPSHOT) {
				return LauncherFX.resources.getString("profile.editor.name.lastest.snapshot");
			}
			return LauncherFX.resources.getString("profile.editor.name.default");
		}
		return this.name;
	}
	
	public String getName() {
		if (this.versionType != VERSIONTYPE.CUSTOM) {
			return toString();
		}
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return this.versionId;
	}
	
	public void setId(String id) {
		this.versionId = id;
	}

	public VERSIONTYPE getVersionType() {
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
		if (this.resolution == defaultResolution) {
			return new int[2];
		}
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

	public String getJvmArguments() {
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
}
