package main.java.com.github.rashnain.launcherfx.model;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.com.github.rashnain.launcherfx.App;
import main.java.com.github.rashnain.launcherfx.PROFILE_TYPE;
import main.java.com.github.rashnain.launcherfx.utility.JsonUtility;

public class LauncherProfile {
	
	private String workDir;
	
	private String versionsDir;
	
	private String librariesDir;
	
	private String assetsDir;

	private boolean online;

	private ObservableList<GameProfile> gameProfiles;
	
	private String locale;
	
	private String guestUsername;
	
	private static final LauncherProfile instance = new LauncherProfile();
	
	private LauncherProfile() {
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
			GameProfile latest = new GameProfile("", "latest-release", Instant.EPOCH, PROFILE_TYPE.LATEST_RELEASE);
			gameProfiles.add(latest);
		}
		
		try {
			loadSettings();
			System.out.println("Successfully loaded settings.");
		} catch (Exception e) {
			System.out.println("Error loading launcher settings.");
			System.out.println("Ceating default launcher settings.");
			String locale = Locale.getDefault().getLanguage();
			if (App.isAvailableLocale(locale) >= 0) {
				this.locale = locale;
			} else {
				this.locale = "en";
			}
			this.guestUsername = "";
		}
	}
	
	private void loadProfiles() throws Exception {
		JsonObject json = JsonUtility.load(this.workDir+"launcher_profiles.json");
		JsonObject profiles = json.get("profiles").getAsJsonObject();
		for (String key : profiles.keySet()) {
			JsonObject profile = profiles.get(key).getAsJsonObject();
			
			String name = profile.get("name").getAsString();
			String versionId = profile.get("lastVersionId").getAsString();
			String lastUsed = profile.get("lastUsed").getAsString();
			Instant lastUsedDate = Instant.parse(lastUsed);
			
			String versionType = profile.get("type").getAsString();
			PROFILE_TYPE type = PROFILE_TYPE.getAsType(versionType);
			
			GameProfile gameProfile = new GameProfile(name, versionId, lastUsedDate, type);
			
			gameProfile.setIdentifier(key);
			
			if (profile.keySet().contains("gameDir")) {
				gameProfile.setGameDir(profile.get("gameDir").getAsString());
			}
			if (profile.keySet().contains("resolution")) {
				int height = profile.getAsJsonObject("resolution").get("height").getAsInt();
				int width = profile.getAsJsonObject("resolution").get("width").getAsInt();
				gameProfile.setWidth(width);
				gameProfile.setHeight(height);
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
		JsonObject json = JsonUtility.load(this.workDir+"launcher_profiles.json");
		JsonObject settings = json.getAsJsonObject("launcherfx");
		this.locale = settings.get("locale").getAsString();
		this.guestUsername = settings.get("guestUsername").getAsString();
	}
	
	public void setWorkDir(String dir) {
		this.workDir = dir + "/";
		this.versionsDir = this.workDir + "versions/";
		this.librariesDir = this.workDir + "libraries/";
		this.assetsDir = this.workDir + "assets/";
	}
	
	public String getWorkDir() {
		return this.workDir;
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

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public String getGuestUsername() {
		return this.guestUsername;
	}
	
	public void setUsername(String username) {
		this.guestUsername = username;
	}
	
	public GameProfile lastUsedProfile() {
		if (this.gameProfiles.size() == 0) { return null; };
		
		GameProfile lastUsed = this.gameProfiles.get(0);
		for (GameProfile gp : this.gameProfiles) {
			if (gp.getLastUsed().isAfter(lastUsed.getLastUsed())) {
				lastUsed = gp;
			}
		}
		return lastUsed;
	}
	
	private JsonObject generatedJson() {
		JsonObject settings = new JsonObject();
		settings.add("profiles", new JsonObject());
		JsonObject profiles = settings.getAsJsonObject("profiles");
		for (GameProfile gp : this.gameProfiles) {
			profiles.add(gp.getIdentifier(), new JsonObject());
			JsonObject profile = profiles.getAsJsonObject(gp.getIdentifier());
			
			if (!gp.getEditableGameDir().equals("")) {
				profile.add("gameDir", new JsonPrimitive(gp.getGameDir()));
			}
			if (!gp.getEditableJvmArguments().equals("")) {
				profile.add("javaArgs", new JsonPrimitive(gp.getJvmArguments()));
			}
			if (!gp.getEditableExecutable().equals("")) {
				profile.add("javaDir", new JsonPrimitive(gp.getExecutable()));
			}
			profile.add("lastUsed", new JsonPrimitive(gp.getLastUsed().toString()));
			profile.add("lastVersionId", new JsonPrimitive(gp.getVersionId()));
			profile.add("name", new JsonPrimitive(gp.getName()));
			if (!(gp.getEditableWidth().equals("") && gp.getEditableHeight().equals(""))) {
				profile.add("resolution", new JsonObject());
				profile.getAsJsonObject("resolution").add("width", new JsonPrimitive(Integer.valueOf(gp.getWidthOrDefault())));
				profile.getAsJsonObject("resolution").add("height", new JsonPrimitive(Integer.valueOf(gp.getHeightOrDefault())));
			}
			profile.add("type", new JsonPrimitive(PROFILE_TYPE.getAsString(gp.getVersionType())));
		}
		
		settings.add("launcherfx", new JsonObject());
		settings.getAsJsonObject("launcherfx").add("guestUsername", new JsonPrimitive(this.guestUsername));
		settings.getAsJsonObject("launcherfx").add("locale", new JsonPrimitive(this.locale));
		
		return settings;
	}

	public void saveProfile() {
		JsonObject settings = generatedJson();
		
		boolean saved;
		
		saved = JsonUtility.save(this.workDir + "launcher_profiles.json", settings);
		
		if (!saved) {
			Runtime.getRuntime().exit(1);
		}
	}
}
