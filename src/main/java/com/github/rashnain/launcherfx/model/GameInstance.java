package main.java.com.github.rashnain.launcherfx.model;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import main.java.com.github.rashnain.launcherfx.utility.FileUtility;
import main.java.com.github.rashnain.launcherfx.utility.JsonUtility;
import main.java.com.github.rashnain.launcherfx.utility.LibraryUtility;

/**
 * Class representing a game instance
 */
public class GameInstance {

	private StringBuilder command;

	private GameProfile profile;

	private DoubleProperty loadingProgress;

	private BooleanProperty loadingVisibility;

	private Process process;

	/**
	 * Create an instance with this profile
	 * @param gameDir Directory the game will work with
	 */
	public GameInstance(GameProfile profile) {
		this.command = new StringBuilder();
		this.profile = profile;
		this.loadingProgress = new SimpleDoubleProperty(0);
		this.loadingVisibility = new SimpleBooleanProperty(true);
	}

	/**
	 * Starts a thread that prepare and launch the instance
	 */
	public void startThread() {

		Thread t = new Thread() {
			public void run() {
				try {
					prepareInstance();
					System.out.println(getCommand());
					runInstance();
				} catch (Exception e) {
					loadingVisibility.setValue(false);
					e.printStackTrace();
				}
			}
		};

		t.start();
	}

	/**
	 * Prepare the instance
	 * @throws Exception Download errors
	 */
	private void prepareInstance() throws Exception {
		LauncherProfile launcher = LauncherProfile.getProfile();

		String versionJsonURI = launcher.getVersionsDir()+profile.getVersion()+"/"+profile.getVersion()+".json";

		JsonObject version = JsonUtility.load(versionJsonURI);

		// Java executable + JVM arguments
		addCommand("\""+profile.getExecutableOrDefault()+"\"");
		addCommand(profile.getJvmArguments());

		// Library path
		addCommand("-Djava.library.path=\""+launcher.getVersionsDir()+profile.getVersion()+"/natives/\"");

		// Classpath
		addCommand("-cp");
		JsonArray libraries = version.getAsJsonArray("libraries");
		for (JsonElement lib : libraries) {
			JsonObject libo = lib.getAsJsonObject();
			String libURL = "";
			String libName = "";
			String libDir = "";
			int libSize = 0;

			// checks artifact
			if (LibraryUtility.shouldUseLibrary(libo)) {
				JsonObject download = libo.getAsJsonObject("downloads");
				JsonObject artifact = download.getAsJsonObject("artifact");
				if (artifact != null) {
					String libPath = artifact.getAsJsonObject().get("path").getAsString();
	
					libURL = artifact.getAsJsonObject().get("url").getAsString();
					libSize = artifact.getAsJsonObject().get("size").getAsInt();
					libName = libPath.split("/")[libPath.split("/").length-1];
					libDir = libPath.substring(0, libPath.lastIndexOf("/")+1);
					FileUtility.download(libURL, libName, launcher.getLibrariesDir()+libDir, libSize);
					addCommand("\""+launcher.getLibrariesDir()+libDir+libName, "\";");
				}

				// checks natives
				String nativesString = LibraryUtility.getNativesString(libo);
				if (!nativesString.equals("")) {
					JsonObject classifiers = libo.getAsJsonObject("downloads").getAsJsonObject("classifiers");
					JsonObject natives = classifiers.getAsJsonObject(nativesString);
					libURL = natives.get("url").getAsString();
					libSize = natives.get("size").getAsInt();
					String nativesPath = natives.get("path").getAsString();
					libName = nativesPath.split("/")[nativesPath.split("/").length-1];
					libDir = nativesPath.substring(0, nativesPath.lastIndexOf("/")+1);
					FileUtility.download(libURL, libName, launcher.getLibrariesDir()+libDir, libSize);
					// extract natives executables
					if (libo.has("extract")) {
						JsonArray exclude = libo.getAsJsonObject("extract").getAsJsonArray("exclude");
						List<String> excludeList = new ArrayList<>();
						for (JsonElement e : exclude) {
							excludeList.add(e.getAsString());
						}
						FileUtility.unzip(launcher.getLibrariesDir()+libDir+libName, launcher.getVersionsDir()+profile.getVersion()+"/natives/", excludeList);
					}
					addCommand("\""+launcher.getLibrariesDir()+libDir+libName, "\";");
				}
			}
		}

		// Version JAR
		String versionJarName = profile.getVersion()+".jar";
		String versionJarDir = launcher.getVersionsDir()+profile.getVersion()+"/";

		if (!new File(versionJarDir+versionJarName).isFile()) {
			JsonObject clientJar = version.getAsJsonObject("downloads").getAsJsonObject("client");
			String versionJarURL = clientJar.get("url").getAsString();
			int versionJarSize = clientJar.get("size").getAsInt();
			FileUtility.download(versionJarURL, versionJarName, versionJarDir, versionJarSize);
		}
		addCommand("\""+versionJarDir+versionJarName+"\"");

		// Main class
		addCommand(version.get("mainClass").getAsString());

		// Parameters
		String arguments = "";
		if (version.has("minecraftArguments")) {
			arguments = version.get("minecraftArguments").getAsString() + " ";
		} else {
			JsonArray argsArray = version.getAsJsonObject("arguments").getAsJsonArray("game");
			for (JsonElement e : argsArray) {
				if (e.isJsonPrimitive()) {
					arguments += e.getAsString() + " ";
				}
			}
		}
		arguments += "--width ${resolution_width} --height ${resolution_height}";
		System.out.println(arguments);

		// username
		arguments = arguments.replace("${auth_player_name}", launcher.getGuestUsername());
		// version
		arguments = arguments.replace("${version_name}", profile.getVersion());
		// game dir
		arguments = arguments.replace("${game_directory}", "\""+profile.getGameDirOrDefault()+"\"");
		// assets dir
		arguments = arguments.replace("${assets_root}", "\""+launcher.getAssetsDir()+"\"");
		arguments = arguments.replace("${game_assets}", "\""+launcher.getAssetsDir()+"\"");
		// assets index
		arguments = arguments.replace("${assets_index_name}", version.getAsJsonObject("assetIndex").get("id").getAsString());
		// uuid
		arguments = arguments.replace("${auth_uuid}", ""+UUID.nameUUIDFromBytes(("OfflinePlayer:"+launcher.getGuestUsername()).getBytes()));
		// auth access
		arguments = arguments.replace("${auth_session}", "accessToken");
		arguments = arguments.replace("${auth_access_token}", "accessToken");
		// user properties
		arguments = arguments.replace("${user_properties}", "{}");
		// user type
		arguments = arguments.replace("${user_type}", "legacy");
		// version type
		arguments = arguments.replace("${version_type}", version.get("type").getAsString());
		// width
		arguments = arguments.replace("${resolution_width}", profile.getWidthOrDefault());
		// height
		arguments = arguments.replace("${resolution_height}", profile.getHeightOrDefault());

		addCommand(arguments);

		// Assets
		JsonObject assetIndex = version.getAsJsonObject("assetIndex");
		String assetIndexURL = assetIndex.get("url").getAsString();
		String assetIndexName = assetIndexURL.split("/")[assetIndexURL.split("/").length-1];
		String assetIndexDir = launcher.getAssetsDir()+"indexes/";

		if (!new File(assetIndexDir+assetIndexName).isFile()) {
			int assetIndexSize = assetIndex.get("size").getAsInt();
			FileUtility.download(assetIndexURL, assetIndexName, assetIndexDir, assetIndexSize);
			JsonObject assets = JsonUtility.load(assetIndexDir+assetIndexName).getAsJsonObject("objects");
			for(Entry<String, JsonElement> e : assets.entrySet()) {
				JsonObject asset = e.getValue().getAsJsonObject();
				String assetName = asset.get("hash").getAsString();
				String assetDir = assetName.substring(0, 2)+"/";
				int assetSize = asset.get("size").getAsInt();
				FileUtility.download("https://resources.download.minecraft.net/"+assetDir+assetName, assetName, launcher.getAssetsDir()+"objects/"+assetDir, assetSize);
			}
		}

		loadingVisibility.setValue(false);
	}

	/**
	 * Execute the instance's command
	 * @throws IOException If an error occure when launching
	 */
	private void runInstance() throws IOException {
		this.process = Runtime.getRuntime().exec(getCommand(), null, new File(profile.getGameDirOrDefault()));
		consumeStdIn();
		consumeStdErr();
	}

	/**
	 * Consume standard input buffer of the process<br>
	 * Must be done otherwise the game freeze once the buffer is full
	 */
	private void consumeStdIn() {
		Thread t = new Thread() {
			public void run() {
				Scanner in = new Scanner(process.getInputStream());
				while (in.hasNext()) {
					System.out.println(in.nextLine());
				}
				System.out.println("Instance terminated.");
			}
		};
		t.start();
	}

	/**
	 * Consume standard error buffer of the process<br>
	 * Must be done otherwise the game freeze once the buffer is full
	 */
	private void consumeStdErr() {
		Thread t = new Thread() {
			public void run() {
				Scanner err = new Scanner(process.getErrorStream());
				while (err.hasNext()) {
					System.out.println(err.nextLine());
				}
			}
		};
		t.start();
	}

	/**
	 * @return the instance's command
	 */
	private String getCommand() {
		return this.command.toString();
	}

	/**
	 * Adds a command
	 * @param cmd the command
	 * @param lineEnd end of the command
	 */
	private void addCommand(String cmd, String lineEnd) {
		this.command.append(cmd + lineEnd);
	}

	/**
	 * Adds a command, ending with a space
	 * @param cmd the command
	 */
	private void addCommand(String cmd) {
		addCommand(cmd, " ");
	}

	/**
	 * Used to know if a directory is already being used or not<br>
	 * To inform user that launching multiple instances in the same directory can cause bug
	 * @return the instance's game directory
	 */
	public String getGameDir() {
		return this.profile.getGameDir();
	}

	/**
	 * @return instance's loading progress property
	 */
	public DoubleProperty getLoadingProgressProperty() {
		return this.loadingProgress;
	}

	/**
	 * @return instance's loading visibility property
	 */
	public BooleanProperty getLoadingVisibilityProperty() {
		return this.loadingVisibility;
	}

	/**
	 * @return instance's process
	 */
	public Process getProcess() {
		return this.process;
	}

}
