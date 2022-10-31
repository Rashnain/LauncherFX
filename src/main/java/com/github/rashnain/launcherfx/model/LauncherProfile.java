package main.java.com.github.rashnain.launcherfx.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.com.github.rashnain.launcherfx.LauncherFX;
import main.java.com.github.rashnain.launcherfx.Util;

public class LauncherProfile {
	
	private ObservableList<GameProfile> gameProfiles;
	
	private JsonObject launcherSettings;

	private String launcherDir;
	
	private String dataDir;
	
	private String versionsDir;
	
	private String librariesDir;
	
	private String assetsDir;
	
	private String username;

	private boolean online;
	
	private static final LauncherProfile instance = new LauncherProfile();
	
	private LauncherProfile() {
		try {
			new URL(LauncherFX.VERSION_MANIFEST).openConnection().getInputStream().available();
			this.online = true;
			System.out.println("Online mode.");
		} catch (IOException e) {
			this.online = false;
			System.out.println("Offline mode.");
		}
		// TODO check if it actually works
		
		this.gameProfiles = FXCollections.observableArrayList();
	}
	
	public void loadProfiles() {
		try {
			this.launcherSettings = Util.loadJSON(this.dataDir+"launcher_profiles.json");
			JsonObject profiles = this.launcherSettings.get("profiles").getAsJsonObject();
			for (String key : profiles.keySet()) {
				JsonObject profile = profiles.get(key).getAsJsonObject();
				
				String name = profile.get("name").getAsString();
				String versionId = profile.get("lastVersionId").getAsString();
				String lastUsed = profile.get("lastUsed").getAsString();
				Date lastUsedDate = Date.from(Instant.parse(lastUsed));
				
				GameProfile gameProfile = new GameProfile(name, versionId, lastUsedDate);
				
				if (profile.keySet().contains("gameDir")) {
					gameProfile.setGameDir(profile.get("gameDir").getAsString());
				}
				if (profile.keySet().contains("resolution")) {
					int height = profile.get("resolution").getAsJsonObject().get("height").getAsInt();
					int width = profile.get("resolution").getAsJsonObject().get("width").getAsInt();
					gameProfile.setResolutions(width, height);
				}
				if (profile.keySet().contains("javaDir")) {
					gameProfile.setExecutable(profile.get("javaDir").getAsString());
				}
				if (profile.keySet().contains("javaArgs")) {
					gameProfile.setJvmArguments(profile.get("javaArgs").getAsString());
				}
				
				this.gameProfiles.add(gameProfile);
				
				System.out.println("Loaded profile " + key);
			}
			System.out.println("Successfully loaded launcher profiles.");

		} catch (Exception e) {
			System.out.println("Error loading launcher profiles.");
			System.out.println("Ceating empty launcher settings.");
			this.launcherSettings.add("profiles", new JsonObject());
			this.launcherSettings.add("settings", new JsonObject());
			this.launcherSettings.add("version", new JsonPrimitive(1));
		}
	}

	public static LauncherProfile getProfile() {
		return LauncherProfile.instance;
	}
	
	public String getLauncherDir() {
		return this.launcherDir;
	}

	public void setLauncherDir(String dir) {
		this.launcherDir = dir;
		this.dataDir = this.launcherDir + "data/";
		this.versionsDir = this.dataDir + "versions/";
		this.librariesDir = this.dataDir + "libraries/";
		this.assetsDir = this.dataDir + "assets/";
	}
	
	public String getDataDir() {
		return this.dataDir;
	}
	
	public String getVersionsDir() {
		return this.versionsDir;
	}

	public String getLibrariesDir() {
		return this.librariesDir;
	}
	
	public String getAssetsDir() {
		return this.assetsDir;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isOnline() {
		return this.online;
	}
	
	public void setOnline(boolean status) {
		this.online = status;
	}
	
	public String getLocale() {
		if (this.launcherSettings.keySet().contains("settings")) {
			JsonObject settings = this.launcherSettings.getAsJsonObject("settings");
			if (!settings.keySet().contains("locale")) {
				settings.add("locale", new JsonPrimitive("en"));
			}
		} else {
			JsonObject locale = new JsonObject();
			locale.add("locale", new JsonPrimitive("en"));
			this.launcherSettings.add("settings", locale);
		}
		
		return this.launcherSettings.getAsJsonObject("settings").get("locale").getAsString();
	}

	public ObservableList<GameProfile> getGameProfiles() {
		return this.gameProfiles;
	}

	public GameProfile lastUsedProfile() {
		if (this.gameProfiles.size() == 0) { return null; };
		
		GameProfile lastUsed = this.gameProfiles.get(0);
		for (GameProfile gp : this.gameProfiles) {
			if (gp.getLastUsed().getTime() > lastUsed.getLastUsed().getTime()) {
				lastUsed = gp;
			}
		}
		return lastUsed;
	}
	
	public void saveProfile() throws IOException {
		System.out.println("Updating launcher_profiles.json.");
		File file = new File(this.dataDir + "launcher_profiles.json");
		if (!file.isFile()) {
			file.createNewFile();
		}
		
		FileOutputStream out = new FileOutputStream(file);
		out.write(this.launcherSettings.toString().getBytes());
		out.close();
		System.out.println("Updated launcher_profiles.json.");
	}
}
