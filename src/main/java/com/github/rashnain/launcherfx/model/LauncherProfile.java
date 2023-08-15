package com.github.rashnain.launcherfx.model;

import com.github.rashnain.launcherfx.Main;
import com.github.rashnain.launcherfx.PROFILE_TYPE;
import com.github.rashnain.launcherfx.utility.JsonUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class representing the launcher's settings
 */
public class LauncherProfile {

	private static final LauncherProfile instance = new LauncherProfile();

	private final ObservableList<GameProfile> gameProfiles;
	private final List<MicrosoftAccount> accounts;

	private String workDir;

	private String assetsDir;

	private String librariesDir;

	private String versionsDir;

	private boolean isGuest;

	private String guestUsername;

	private MicrosoftAccount currentAccount;

	private boolean rememberMe;

	private String locale;

	private boolean onlineStatus;

	private LauncherProfile() {
		gameProfiles = FXCollections.observableArrayList();
		accounts = new ArrayList<>();
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
			String hostLocale = Locale.getDefault().getLanguage();
			if (Main.getIndexOfLocale(hostLocale) != -1) {
				locale = hostLocale;
			} else {
				locale = "en";
			}
			guestUsername = "";
			currentAccount = new MicrosoftAccount();
			rememberMe = true;
		}
	}

	/**
	 * Load profiles from settings file
	 */
	private void loadProfiles() {
		JsonObject json = JsonUtility.load(workDir + "launcher_profiles.json");
		JsonObject profiles = json.getAsJsonObject("profiles");
		for (String key : profiles.keySet()) {
			JsonObject profile = profiles.getAsJsonObject(key);

			String name = profile.get("name").getAsString();
			String version = profile.get("lastVersionId").getAsString();
			String lastUsedString = profile.get("lastUsed").getAsString();
			String profileType = profile.get("type").getAsString();

			Instant lastUsed;
			try {
				lastUsed = Instant.parse(lastUsedString);
			} catch (DateTimeParseException e) {
				System.out.println("Couldn't parse instant '" + lastUsedString + "', " + e.getMessage());
				lastUsed = Instant.EPOCH;
			}
			PROFILE_TYPE type = PROFILE_TYPE.getAsType(profileType);

			GameProfile gameProfile = new GameProfile(key, lastUsed, version, name, type);

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

			gameProfiles.add(gameProfile);

			System.out.println("Loaded profile " + key);
		}
	}

	/**
	 * Load settings from settings file
	 */
	private void loadSettings() {
		JsonObject json = JsonUtility.load(workDir + "launcher_profiles.json");
		JsonObject settings = json.getAsJsonObject("launcherfx");
		locale = settings.get("locale").getAsString();
		if (Main.getIndexOfLocale(locale) == -1) {
			locale = "en";
		}
		guestUsername = getIfItExists(settings, "guestUsername").getAsString();
		rememberMe = getIfItExists(settings, "rememberMe").getAsBoolean();

		JsonArray jsonAcounts = settings.getAsJsonArray("accounts");
		for (int i = 0; i < jsonAcounts.size(); i++) {
			JsonObject jsonAccount = (JsonObject) jsonAcounts.get(i);

			String username = jsonAccount.get("username").getAsString();
			String uuid = jsonAccount.get("uuid").getAsString();
			String accessToken = jsonAccount.get("accessToken").getAsString();
			String refreshToken = jsonAccount.get("refreshToken").getAsString();
			String clientId = jsonAccount.get("clientId").getAsString();
			String xuid = jsonAccount.get("xuid").getAsString();
			String lastUsedString = jsonAccount.get("lastUsed").getAsString();

			Instant lastUsed;
			try {
				lastUsed = Instant.parse(lastUsedString);
			} catch (DateTimeParseException e) {
				System.out.println("Couldn't parse instant '" + lastUsedString + "', " + e.getMessage());
				lastUsed = Instant.EPOCH;
			}

			MicrosoftAccount account = new MicrosoftAccount(username, uuid, accessToken, refreshToken, clientId, xuid, lastUsed);
			accounts.add(account);
		}
		if (!accounts.isEmpty())
			currentAccount = lastUsedAccount();
		else
			currentAccount = new MicrosoftAccount();
	}

	/**
	 * Returns a JsonObject representing the launcher's profiles and settings
	 * @return Corresponding JsonObject
	 */
	private JsonObject generateJson() {
		JsonObject settings = new JsonObject();
		settings.add("profiles", new JsonObject());
		JsonObject profiles = settings.getAsJsonObject("profiles");
		for (GameProfile gp : gameProfiles) {
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

		launcherfx.add("locale", new JsonPrimitive(locale));
		launcherfx.add("guestUsername", new JsonPrimitive(guestUsername));
		launcherfx.add("rememberMe", new JsonPrimitive(rememberMe));

		launcherfx.add("accounts", new JsonArray());
		JsonArray jsonAccounts = launcherfx.getAsJsonArray("accounts");
		for (MicrosoftAccount account : accounts) {
			if (account.getUsername().isEmpty()) continue;
			jsonAccounts.add(new JsonObject());
			JsonObject jsonAccount = (JsonObject) jsonAccounts.get(accounts.indexOf(account));
			jsonAccount.add("username", new JsonPrimitive(account.getUsername()));
			jsonAccount.add("uuid", new JsonPrimitive(account.getUuid()));
			jsonAccount.add("accessToken", new JsonPrimitive(account.getAccessToken()));
			jsonAccount.add("refreshToken", new JsonPrimitive(account.getRefreshToken()));
			jsonAccount.add("clientId", new JsonPrimitive(account.getClientId()));
			jsonAccount.add("xuid", new JsonPrimitive(account.getXuid()));
			jsonAccount.add("lastUsed", new JsonPrimitive(account.getLastUsed().toString()));
		}

		return settings;
	}

	/**
	 * Saves profiles and settings into settings file
	 */
	public void saveProfile() {
		JsonObject settings = generateJson();

		System.out.println("Saving settings into launcher_profiles.json.");

		boolean saved = JsonUtility.save(workDir + "launcher_profiles.json", settings);

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
		return gameProfiles;
	}

	public List<MicrosoftAccount> getAccounts() {
		return accounts;
	}

	public int indexOfAccount(String uuid) {
		for (MicrosoftAccount account : accounts)
			if (account.getUuid().equals(uuid)) return accounts.indexOf(account);
		return -1;
	}

	/**
	 * Returns the index of the last used profile
	 * @return Index of last used profile or null there is no profile
	 */
	public GameProfile lastUsedProfile() {
		if (gameProfiles.isEmpty())
			return null;
		GameProfile lastUsed = gameProfiles.get(0);
		for (GameProfile gp : gameProfiles) {
			if (gp.getLastUsed().isAfter(lastUsed.getLastUsed())) {
				lastUsed = gp;
			}
		}
		return lastUsed;
	}

	public MicrosoftAccount lastUsedAccount() {
		if (accounts.isEmpty())
			return null;
		MicrosoftAccount lastUsed = accounts.get(0);
		for (MicrosoftAccount ma : accounts) {
			if (ma.getLastUsed().isAfter(lastUsed.getLastUsed())) {
				lastUsed = ma;
			}
		}
		return lastUsed;
	}

	public String getWorkDir() {
		return workDir;
	}

	/**
	 * Defines the launcher's working directory and its subfolders
	 * @param dir Directory
	 */
	public void setWorkDir(String dir) {
		workDir = dir + "/";
		assetsDir = workDir + "assets/";
		librariesDir = workDir + "libraries/";
		versionsDir = workDir + "versions/";
	}

	public String getVersionsDir() {
		return versionsDir;
	}

	public String getLibrariesDir() {
		return librariesDir;
	}

	public String getAssetsDir() {
		return assetsDir;
	}

	public MicrosoftAccount getCurrentAccount() {
		return currentAccount;
	}

	public void setCurrentAccount(MicrosoftAccount account) {
		currentAccount = account;
	}

	public String getGuestUsername() {
		return guestUsername;
	}

	public void setGuestUsername(String username) {
		guestUsername = username;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public boolean getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(boolean status) {
		onlineStatus = status;
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
	public JsonElement getIfItExists(JsonObject object, String member) {
		if (object.has(member)) {
			return object.get(member);
		}
		return new JsonPrimitive("");
	}

}
