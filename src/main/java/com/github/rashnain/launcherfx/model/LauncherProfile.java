package com.github.rashnain.launcherfx.model;

import java.time.Instant;
import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.github.rashnain.launcherfx.Main;
import com.github.rashnain.launcherfx.PROFILE_TYPE;
import com.github.rashnain.launcherfx.utility.JsonUtility;

/**
 * Class representing the launcher's settings
 */
public class LauncherProfile {

	private static final LauncherProfile instance = new LauncherProfile();

	private ObservableList<GameProfile> gameProfiles;

	private String workDir;

	private String assetsDir;

	private String librariesDir;

	private String versionsDir;

	private boolean isGuest;

	private String guestUsername;

	private String username;

	private String uuid;

	private String accessToken;

	private String refreshToken;

	private String clientId;

	private String xuid;

	private String locale;

	private boolean onlineStatus;

	private LauncherProfile() {
		this.gameProfiles = FXCollections.observableArrayList();
	}

	/**
	 * Returns LauncherProfile instance
	 */
	public static LauncherProfile getProfile() {
		return LauncherProfile.instance;
	}

	/**
	 * Load profiles and settings from settings file or create them
	 */
	public void loadProfile() {
		try {
			loadProfiles();
			System.out.println("Successfully loaded all profiles.");
		} catch (Exception e) {
			System.out.println("Error loading launcher profiles.");
			System.out.println("Ceating default profiles.");
			GameProfile release = new GameProfile(Instant.EPOCH, "latest-release", "", PROFILE_TYPE.LATEST_RELEASE);
			gameProfiles.add(release);
			GameProfile snapshot = new GameProfile(Instant.EPOCH, "latest-snapshot", "", PROFILE_TYPE.LATEST_SNAPSHOT);
			gameProfiles.add(snapshot);
		}

		try {
			loadSettings();
			System.out.println("Successfully loaded settings.");
		} catch (Exception e) {
			System.out.println("Error loading launcher settings.");
			System.out.println("Ceating default launcher settings.");
			String locale = Locale.getDefault().getLanguage();
			if (Main.getIndexOfLocale(locale) != -1) {
				this.locale = locale;
			} else {
				this.locale = "en";
			}
			this.guestUsername = "";
			this.username = "";
			this.uuid = "";
			this.accessToken = "";
			this.refreshToken = "";
			this.clientId = "";
			this.xuid = "";
		}
	}

	/**
	 * Load profiles from settings file
	 * @throws Exception If a profile is malformed
	 */
	private void loadProfiles() throws Exception {
		JsonObject json = JsonUtility.load(this.workDir + "launcher_profiles.json");
		JsonObject profiles = json.getAsJsonObject("profiles");
		for (String key : profiles.keySet()) {
			JsonObject profile = profiles.getAsJsonObject(key);

			String name = profile.get("name").getAsString();
			String version = profile.get("lastVersionId").getAsString();
			String lastUsed = profile.get("lastUsed").getAsString();
			String versionType = profile.get("type").getAsString();

			Instant lastUsedInstant = Instant.parse(lastUsed);
			PROFILE_TYPE type = PROFILE_TYPE.getAsType(versionType);

			GameProfile gameProfile = new GameProfile(key, lastUsedInstant, version, name, type);

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

	/**
	 * Load settings from settings file
	 * @throws Exception If the settings are malformed
	 */
	private void loadSettings() throws Exception {
		JsonObject json = JsonUtility.load(this.workDir + "launcher_profiles.json");
		JsonObject settings = json.getAsJsonObject("launcherfx");
		this.locale = settings.get("locale").getAsString();
		if (Main.getIndexOfLocale(this.locale) == -1) {
			this.locale = "en";
		}
		this.guestUsername = getIfItExixts(settings, "guestUsername").getAsString();
		this.username = getIfItExixts(settings, "username").getAsString();
		this.uuid = getIfItExixts(settings, "uuid").getAsString();
		this.accessToken = getIfItExixts(settings, "accessToken").getAsString();
		this.refreshToken = getIfItExixts(settings, "refreshToken").getAsString();
		this.clientId = getIfItExixts(settings, "clientId").getAsString();
		this.xuid = getIfItExixts(settings, "xuid").getAsString();
	}

	/**
	 * Returns a JsonObject representing the launcher's profiles and settings
	 * @return Corresponding JsonObject
	 */
	private JsonObject generateJson() {
		JsonObject settings = new JsonObject();
		settings.add("profiles", new JsonObject());
		JsonObject profiles = settings.getAsJsonObject("profiles");
		for (GameProfile gp : this.gameProfiles) {
			profiles.add(gp.getIdentifier(), new JsonObject());
			JsonObject profile = profiles.getAsJsonObject(gp.getIdentifier());

			if (!gp.getEditableGameDir().isEmpty()) {
				profile.add("gameDir", new JsonPrimitive(gp.getGameDir()));
			}
			if (!gp.getEditableJvmArguments().isEmpty()) {
				profile.add("javaArgs", new JsonPrimitive(gp.getJvmArguments()));
			}
			if (!gp.getEditableExecutable().isEmpty()) {
				profile.add("javaDir", new JsonPrimitive(gp.getExecutable()));
			}
			profile.add("lastUsed", new JsonPrimitive(gp.getLastUsed().toString()));
			profile.add("lastVersionId", new JsonPrimitive(gp.getVersionProperty().getValue()));
			profile.add("name", new JsonPrimitive(gp.getName()));
			if (!(gp.getEditableWidth().isEmpty() && gp.getEditableHeight().isEmpty())) {
				profile.add("resolution", new JsonObject());
				profile.getAsJsonObject("resolution").add("width", new JsonPrimitive(Integer.valueOf(gp.getWidthOrDefault())));
				profile.getAsJsonObject("resolution").add("height", new JsonPrimitive(Integer.valueOf(gp.getHeightOrDefault())));
			}
			profile.add("type", new JsonPrimitive(PROFILE_TYPE.getAsString(gp.getVersionType())));
		}

		settings.add("launcherfx", new JsonObject());
		JsonObject launcherfx = settings.getAsJsonObject("launcherfx");
		launcherfx.add("locale", new JsonPrimitive(this.locale));
		launcherfx.add("guestUsername", new JsonPrimitive(this.guestUsername));
		launcherfx.add("username", new JsonPrimitive(this.username));
		launcherfx.add("uuid", new JsonPrimitive(this.uuid));
		launcherfx.add("accessToken", new JsonPrimitive(this.accessToken));
		launcherfx.add("refreshToken", new JsonPrimitive(this.refreshToken));
		launcherfx.add("clientId", new JsonPrimitive(this.clientId));
		launcherfx.add("xuid", new JsonPrimitive(this.xuid));

		return settings;
	}

	/**
	 * Saves profiles and settings into settings file
	 */
	public void saveProfile() {
		JsonObject settings = generateJson();

		System.out.println("Saving settings into launcher_profiles.json.");

		boolean saved = JsonUtility.save(this.workDir + "launcher_profiles.json", settings);

		if (!saved) {
			System.out.println("Couldn't save profile");
			Runtime.getRuntime().exit(1);
		}

		System.out.println("Saved settings.");
	}

	/**
	 * @return List of loaded GameProfiles
	 */
	public ObservableList<GameProfile> getGameProfiles() {
		return this.gameProfiles;
	}

	/**
	 * Returns the index of the last used profile
	 * @return Index of last used profile or null there is no profile
	 */
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

	public String getWorkDir() {
		return this.workDir;
	}

	/**
	 * Defines the launcher's working directory and its subfolders
	 * @param dir Directory
	 */
	public void setWorkDir(String dir) {
		this.workDir = dir + "/";
		this.assetsDir = this.workDir + "assets/";
		this.librariesDir = this.workDir + "libraries/";
		this.versionsDir = this.workDir + "versions/";
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

	public String getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return this.refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getXuid() {
		return this.xuid;
	}

	public void setXuid(String xuid) {
		this.xuid = xuid;
	}

	public String getGuestUsername() {
		return this.guestUsername;
	}

	public void setGuestUsername(String guestUsername) {
		this.guestUsername = guestUsername;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUUID() {
		return this.uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean getOnlineStatus() {
		return this.onlineStatus;
	}

	public void setOnlineStatus(boolean status) {
		this.onlineStatus = status;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public void setGuestStatus(boolean status) {
		isGuest = status;
	}

	/**
	 * Returns the member of a JsonObject if it has it, otherwise it gives a empty string JsonPrimitive.
	 * @param object the JsonObject
	 * @param member the member
	 */
	public JsonElement getIfItExixts(JsonObject object, String member) {
		if (object.has(member)) {
			return object.get(member);
		}
		return new JsonPrimitive("");
	}

}
