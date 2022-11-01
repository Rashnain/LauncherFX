package main.java.com.github.rashnain.launcherfx.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.com.github.rashnain.launcherfx.LauncherFX;
import main.java.com.github.rashnain.launcherfx.Util;

public class LauncherProfile {
	
	private JsonObject settings;
	
	private String launcherDir;
	
	private String dataDir;
	
	private String versionsDir;
	
	private String librariesDir;
	
	private String assetsDir;

	private boolean online;

	private ObservableList<GameProfile> gameProfiles;
	
	private Locale locale;
	
	private String guestUsername;
	
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
		
		this.settings = new JsonObject();
		this.gameProfiles = FXCollections.observableArrayList();
	}
	
	public static LauncherProfile getProfile() {
		return LauncherProfile.instance;
	}
	
	public void loadProfile() throws IOException {
		try {
			loadProfiles();
			System.out.println("Successfully loaded all profiles.");
		} catch (Exception e) {
			System.out.println("Error loading launcher profiles.");
			System.out.println("Ceating default profile.");
			GameProfile latest = new GameProfile("", "latest-release", new Date(), VERSION_TYPE.LATEST_RELEASE);
			gameProfiles.add(latest);
		}
		
		try {
			loadSettings();
			System.out.println("Successfully loaded settings.");
		} catch (Exception e) {
			System.out.println("Error loading launcher settings.");
			System.out.println("Ceating default launcher settings.");
			if (LauncherFX.isAvailableLocale(Locale.getDefault().getLanguage())) {
				this.locale = Locale.getDefault();
			} else {
				this.locale = new Locale("en");
			}
			this.guestUsername = "";
		}
		
		saveProfile();
	}
	
	private void loadProfiles() throws Exception {
		JsonObject json = Util.loadJSON(this.dataDir+"launcher_profiles.json");
		JsonObject profiles = json.get("profiles").getAsJsonObject();
		for (String key : profiles.keySet()) {
			JsonObject profile = profiles.get(key).getAsJsonObject();
			
			String name = profile.get("name").getAsString();
			String versionId = profile.get("lastVersionId").getAsString();
			String lastUsed = profile.get("lastUsed").getAsString();
			Date lastUsedDate = Date.from(Instant.parse(lastUsed));
			
			String versionType = profile.get("type").getAsString();
			VERSION_TYPE type = VERSION_TYPE.getAsType(versionType);
			
			GameProfile gameProfile = new GameProfile(name, versionId, lastUsedDate, type);
			
			gameProfile.setIdentifier(key);
			
			if (profile.keySet().contains("gameDir")) {
				gameProfile.setGameDir(profile.get("gameDir").getAsString());
			}
			if (profile.keySet().contains("resolution")) {
				int height = profile.get("resolution").getAsJsonObject().get("height").getAsInt();
				int width = profile.get("resolution").getAsJsonObject().get("width").getAsInt();
				gameProfile.setResolution(width, height);
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
	}
	
	private void loadSettings() throws Exception {
		JsonObject json = Util.loadJSON(this.dataDir+"launcher_profiles.json");
		JsonObject settings = json.getAsJsonObject("settings");
		this.locale = new Locale(settings.get("locale").getAsString());
		this.guestUsername = settings.get("guestUsername").getAsString();
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

	public ObservableList<GameProfile> getGameProfiles() {
		return this.gameProfiles;
	}
	
	public boolean isOnline() {
		return this.online;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public String getUsername() {
		return this.guestUsername;
	}
	
	public void setUsername(String username) {
		this.guestUsername = username;
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
	
	private void updateJson() {
		this.settings.add("profiles", new JsonObject());
		JsonObject profiles = this.settings.getAsJsonObject("profiles");
		for (GameProfile gp : this.gameProfiles) {
			profiles.add(gp.getIdentifier(), new JsonObject());
			JsonObject profile = profiles.getAsJsonObject(gp.getIdentifier());
			
			profile.add("name", new JsonPrimitive(gp.getName()));
			profile.add("lastVersionId", new JsonPrimitive(gp.getVersionId()));
			profile.add("lastUsed", new JsonPrimitive(gp.getLastUsed().toInstant().toString()));
			profile.add("type", new JsonPrimitive(VERSION_TYPE.getAsString(gp.getVersionType())));
			
			if (!gp.getGameDir().equals("")) {
				profile.add("gameDir", new JsonPrimitive(gp.getGameDir()));
			}
			if (gp.getResolution() != GameProfile.defaultResolution) {
				profile.add("resolution", new JsonObject());
				profile.getAsJsonObject("resolution").add("width", new JsonPrimitive(gp.getResolution()[0]));
				profile.getAsJsonObject("resolution").add("height", new JsonPrimitive(gp.getResolution()[1]));
			}
			if (!gp.getExecutable().equals("")) {
				profile.add("javaDir", new JsonPrimitive(gp.getExecutable()));
			}
			if (!gp.getJvmArguments().equals("")) {
				profile.add("javaArgs", new JsonPrimitive(gp.getJvmArguments()));
			}
		}
		
		this.settings.add("settings", new JsonObject());
		this.settings.getAsJsonObject("settings").add("guestUsername", new JsonPrimitive(this.guestUsername));
		this.settings.getAsJsonObject("settings").add("locale", new JsonPrimitive(this.locale.getLanguage()));
	}
	
	public void saveProfile() throws IOException {
		updateJson();
		System.out.println("Saving settings into launcher_profiles.json.");
		
		File file = new File(this.dataDir + "launcher_profiles.json");
		if (!file.isFile()) {
			file.createNewFile();
		}
		
		FileOutputStream out = new FileOutputStream(file);
		out.write(this.settings.toString().getBytes()); // TODO save JSON in a human friendly way
		out.close();
		System.out.println("Saved settings.");
	}
}
