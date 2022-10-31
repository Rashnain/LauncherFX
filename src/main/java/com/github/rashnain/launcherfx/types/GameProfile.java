package main.java.com.github.rashnain.launcherfx.types;

import java.util.Date;

public class GameProfile {
	
	private String name;
	
	private String versionId;
	
	private String versionType;

	private String gameDir;
	
	private int[] resolutions = new int[2];
	
	private String executable;
	
	private String jvmArguments;
	
	private Date lastUsed;
	
	public GameProfile(String name, String versionId, Date lastUsed) {
		this.name = name;
		this.versionId = versionId;
		this.lastUsed = lastUsed;
		this.gameDir = LauncherProfile.getProfile().getDataDir();
		this.resolutions[0] = 854;
		this.resolutions[1] = 480;
		this.executable = "java";
		this.jvmArguments = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
	}
	
	public String toString() {
		if (this.name.equals("")) {
			return "<unnamed configuration>"; // TODO localize this
			// TODO recognize "latest-xxxxx" and prevent from changing name or version
		}
		return name;
	}
	
	public String getName() {
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

	public String getExecutable() {
		return this.executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getVersionType() {
		return this.versionType;
	}

	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	public int[] getResolutions() {
		return this.resolutions;
	}

	public void setResolutions(int width, int height) {
		this.resolutions[0] = width;
		this.resolutions[1] = height;
	}

	public String getGameDir() {
		return this.gameDir;
	}

	public void setGameDir(String directory) {
		this.gameDir = directory;
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
