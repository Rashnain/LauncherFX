package main.java.com.github.rashnain.launcherfx.types;

import java.util.HashMap;
import java.util.Map;

public class LauncherProfile {

	private String launcherDir;
	
	private String assetsDir;
	
	private String librariesDir;
	
	private String versionsDir;
	
	private String username;
	
	private Map<String, String> data;
	
	private static LauncherProfile instance = new LauncherProfile();
	
	private LauncherProfile() {
		this.launcherDir = System.getProperty("user.dir") + "/data/";
		this.assetsDir = launcherDir + "assets/";
		this.librariesDir = launcherDir + "libraries/";
		this.versionsDir = launcherDir + "versions/";
		this.username = "pseudo";
		data = new HashMap<>();
		data.put("--username", this.username);
		data.put("--uuid", "uuid");
		data.put("--accessToken", "accessToken");
		data.put("--clientId", "clientId");
		data.put("--xuid", "xuid");
		data.put("--userType", "legacy");
	}

	public String getLauncherDir() {
		return this.launcherDir;
	}

	public void setLauncherDir(String launcherDir) {
		this.launcherDir = launcherDir;
	}

	public String getAssetsDir() {
		return this.assetsDir;
	}

	public void setAssetsDir(String assetsDir) {
		this.assetsDir = assetsDir;
	}

	public String getLibrariesDir() {
		return this.librariesDir;
	}

	public void setLibrariesDir(String librariesDir) {
		this.librariesDir = librariesDir;
	}

	public String getVersionsDir() {
		return this.versionsDir;
	}

	public void setVersionsDir(String versionsDir) {
		this.versionsDir = versionsDir;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public static LauncherProfile getProfile() {
		return instance;
	}
	
	public Map<String, String> getData() {
		return this.data;
	}

}
