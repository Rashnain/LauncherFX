package main.java.com.github.rashnain.launcherfx.model;

import java.time.Instant;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.com.github.rashnain.launcherfx.Main;
import main.java.com.github.rashnain.launcherfx.PROFILE_TYPE;
import main.java.com.github.rashnain.launcherfx.utility.JsonUtility;

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

	private String guestUsername;

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
			if (Main.getIndexOfLocale(locale) != -1) {
				this.locale = locale;
			} else {
				this.locale = "en";
			}
			this.guestUsername = "";
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

			GameProfile gameProfile = new GameProfile(name, version, lastUsedInstant, type);

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
		this.guestUsername = settings.get("guestUsername").getAsString();
	}

	/**
	 * Returns a JsonObject representing the launcher's profiles and settings
	 * @return Corresponding JsonObject
	 */
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

	/**
	 * Saves profiles and settings into settings file
	 */
	public void saveProfile() {
		JsonObject settings = generatedJson();

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

	public String getGuestUsername() {
		return this.guestUsername;
	}

	public void setGuestUsername(String username) {
		this.guestUsername = username;
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

	public void setOnlineStatus(boolean online) {
		this.onlineStatus = online;
	}

}
