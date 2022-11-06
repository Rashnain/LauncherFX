package main.java.com.github.rashnain.launcherfx.controller;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.java.com.github.rashnain.launcherfx.Main;
import main.java.com.github.rashnain.launcherfx.PROFILE_TYPE;
import main.java.com.github.rashnain.launcherfx.model.GameInstance;
import main.java.com.github.rashnain.launcherfx.model.GameProfile;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;
import main.java.com.github.rashnain.launcherfx.utility.FileUtility;
import main.java.com.github.rashnain.launcherfx.utility.JsonUtility;
import main.java.com.github.rashnain.launcherfx.utility.LibraryUtility;

/**
 * Controller of the profiles screen
 */
public class ProfilesScreenController {

	private boolean initialized;

	private ResourceBundle resources;

	private LauncherProfile launcher;

	private List<GameInstance> instances;

	private GameProfile previous;

	@FXML
	private ListView<GameProfile> listViewProfile;

	@FXML
	private VBox profileEditor;

	@FXML
	private TextField name;

	@FXML
	private ChoiceBox<String> choiceBoxVersion;

	@FXML
	private TextField gameDir;

	@FXML
	private TextField width;

	@FXML
	private TextField height;

	@FXML
	private TextField executable;

	@FXML
	private TextField jvmArgs;

	@FXML
	private ProgressBar loadingBar;

	@FXML
	private ChoiceBox<GameProfile> choiceBoxProfile;

	@FXML
	private Label selectedProfileVersion;

	@FXML
	private Label pseudoStatus;

	@FXML
	private Label pseudo;

	/**
	 * Initialize view
	 */
	public void initializeView() {
		if (!initialized) {
			this.resources = Main.getResources();
			this.launcher = LauncherProfile.getProfile();
			this.instances = new ArrayList<>();

			try {
				FileUtility.download(Main.VERSION_MANIFEST, "version_manifest_v2.json", launcher.getVersionsDir());
				System.out.println("Online mode.");
				launcher.setOnlineStatus(true);
				// Initialize version list
				JsonObject versionManifest = JsonUtility.load(launcher.getVersionsDir()+"version_manifest_v2.json");
				for (JsonElement e : versionManifest.getAsJsonArray("versions")) {
					if (e.getAsJsonObject().get("type").getAsString().equals("release")) {
						choiceBoxVersion.getItems().add(e.getAsJsonObject().get("id").getAsString());
					}
				}
			} catch (IOException e1) {
				System.out.println("Offline mode.");
				launcher.setOnlineStatus(false);
			}

			// Initialize version list
			File ver = new File(launcher.getVersionsDir());
			for (File sub : ver.listFiles()) {
				if (new File(sub.getAbsolutePath()+"/").isDirectory()) {
					JsonObject jo = JsonUtility.load(sub.getAbsolutePath()+"/"+sub.getName()+".json");
					if (jo.get("type").getAsString().equals("release") && !choiceBoxVersion.getItems().contains(sub.getName())) {
						choiceBoxVersion.getItems().add(sub.getName());
					}
				}
			}

			this.listViewProfile.setItems(launcher.getGameProfiles());
			this.choiceBoxProfile.setItems(this.listViewProfile.getItems());

			this.listViewProfile.getSelectionModel().select(launcher.lastUsedProfile());
			this.choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
			updateProfileEditor();
			updateVersionLabel();

			this.choiceBoxProfile.setOnAction( e -> updateVersionLabel() );

			initialized = true;
		}
		pseudo.setText(launcher.getGuestUsername());
	}

	/**
	 * Update profile editor with a single click, or launch the selected profile with a double click
	 * @param event Mouse
	 * @throws Exception if the instance throw an exception
	 */
	@FXML
	private void onClickOnViewList(MouseEvent event) throws Exception {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() > 1) {
				loadGame(this.listViewProfile.getSelectionModel().getSelectedItem());
			} else {
				updateProfileEditor();
			}
		}
	}

	/**
	 * Bind keys to action :<br>
	 * - DELETE to delete profile<br>
	 * - N to create a new profile<br>
	 * - D to duplicate profile
	 * @param event The key typed
	 */
	@FXML
	private void onKeyPressedViewList(KeyEvent event) {
		if (event.getCode().equals(KeyCode.DELETE)) {
			deleteProfile();
		}
		if (event.getCode().equals(KeyCode.N)) {
			newProfile();
		}
		if (event.getCode().equals(KeyCode.D)) {
			duplicateProfile();
		}
	}

	/**
	 * Update the profile editor<br>
	 * And the bindings to save change in real time
	 */
	@FXML
	private void updateProfileEditor() {
		GameProfile profile = this.listViewProfile.getSelectionModel().getSelectedItem();

		if (profile != null) {
			hideProfileEditor(false);

			if (previous != null) {
				previous.getNameProperty().unbind();
				previous.getVersionProperty().unbind();
				previous.getGameDirProperty().unbind();
				previous.getWidthProperty().unbind();
				previous.getHeightProperty().unbind();
				previous.getExecutableProperty().unbind();
				previous.getJvmArgumentsProperty().unbind();
			}

			if (profile.getVersionType() == PROFILE_TYPE.CUSTOM) {
				name.setDisable(false);
				name.setText(profile.getName());
				profile.getNameProperty().bind(name.textProperty());
				choiceBoxVersion.setDisable(false);
				choiceBoxVersion.getSelectionModel().select(profile.getVersion());
				profile.getVersionProperty().bind(choiceBoxVersion.getSelectionModel().selectedItemProperty());
			} else {
				name.setText(profile.toString());
				name.setDisable(true);
				choiceBoxVersion.getSelectionModel().select(profile.getVersion());
				choiceBoxVersion.setDisable(true);
			}
			gameDir.setText(profile.getEditableGameDir());
			profile.getGameDirProperty().bind(gameDir.textProperty());
			width.setText(profile.getEditableWidth());
			profile.getWidthProperty().bind(width.textProperty());
			height.setText(profile.getEditableHeight());
			profile.getHeightProperty().bind(height.textProperty());
			checkResolution();
			executable.setText(profile.getEditableExecutable());
			profile.getExecutableProperty().bind(executable.textProperty());
			jvmArgs.setText(profile.getJvmArguments());
			profile.getJvmArgumentsProperty().bind(jvmArgs.textProperty());

			previous = profile;
		} else {
			hideProfileEditor(true);
		}
	}

	/**
	 * Hide or show the profile editor<br>
	 * Do not hide the buttons
	 * @param visibility
	 */
	private void hideProfileEditor(boolean visibility) {
		for (int i = 0; i < profileEditor.getChildren().size()-1; i ++) {
			profileEditor.getChildren().get(i).setVisible(!visibility);
		}
	}

	/**
	 * Launch the selected profile when clicking the play button
	 * @throws Exception if the instance throw an exception
	 */
	@FXML
	private void onPlayButtonAction() throws Exception {
		loadGame(this.choiceBoxProfile.getSelectionModel().getSelectedItem());
	}

	/**
	 * Launch the selected profile
	 * @param profile
	 * @throws Exception if the instance throw an exception
	 */
	private void loadGame(GameProfile profile) throws Exception {
		boolean ignoreConflicts = false;

		// checks if there isn't another instance running in the same directory
		if (profile != null) {
			this.choiceBoxProfile.getSelectionModel().select(profile);
			checkResolution();
			launcher.saveProfile();
			profile.setLastUsed(Instant.now());
			this.choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
			ignoreConflicts = true;
			Iterator<GameInstance> it = this.instances.iterator();
			while (it.hasNext()) {
				GameInstance gi = it.next();
				if (!gi.getProcess().isAlive()) {
					it.remove();
					continue;
				}
				if (gi.getGameDir().equals(profile.getGameDir())) {
					Alert dialog = new Alert(AlertType.CONFIRMATION);
					dialog.setTitle(this.resources.getString("launch.running.instance"));
					dialog.setHeaderText(this.resources.getString("launch.running.instance.title"));
					dialog.setContentText(this.resources.getString("launch.running.instance.desc"));
					dialog.getButtonTypes().set(0, ButtonType.YES);
					dialog.getButtonTypes().set(1, ButtonType.NO);
					Optional<ButtonType> choice = dialog.showAndWait();
					if (choice.get() == ButtonType.YES) {
						ignoreConflicts = true;
					} else {
						ignoreConflicts = false;
					}
				}
			}
		} else {
			showNoSelectionDialog();
		}

		if (ignoreConflicts) {
			this.loadingBar.setProgress(0);
			this.loadingBar.setVisible(true);

			boolean manifestExists = false;

			if (launcher.getOnlineStatus()) {
				JsonObject versionManifest = JsonUtility.load(launcher.getVersionsDir()+"version_manifest_v2.json");
				JsonArray versionArray = versionManifest.get("versions").getAsJsonArray();
				for (JsonElement entry : versionArray) {
					if (entry.getAsJsonObject().get("id").getAsString().equals(profile.getVersion())) {
						String versionJsonURL = entry.getAsJsonObject().get("url").getAsString();
						FileUtility.download(versionJsonURL, profile.getVersion()+".json", launcher.getVersionsDir()+profile.getVersion()+"/", 0);
						break;
					}
				}
			}

			String versionJsonURI = launcher.getVersionsDir()+profile.getVersion()+"/"+profile.getVersion()+".json";

			if (new File(versionJsonURI).isFile()) {
				manifestExists = true;
			}

			if (manifestExists) {
				JsonObject version = JsonUtility.load(versionJsonURI);

				GameInstance instance = new GameInstance(profile.getGameDir());

				// Java executable + JVM arguments
				instance.addCommand("\""+profile.getExecutableOrDefault()+"\"");
				instance.addCommand(profile.getJvmArguments());

				this.loadingBar.setProgress(0.1);

				// Library path
				instance.addCommand("-Djava.library.path=\""+launcher.getVersionsDir()+profile.getVersion()+"/natives/\"");

				// Classpath, TODO download only required libraries
				instance.addCommand("-cp");
				JsonArray libraries = version.get("libraries").getAsJsonArray();
				for (JsonElement lib : libraries) {
					JsonObject libo = lib.getAsJsonObject();
					if (LibraryUtility.shouldUseLibrary(libo)) {
						JsonObject artifact = libo.get("downloads").getAsJsonObject().get("artifact").getAsJsonObject();
						String libPath = artifact.getAsJsonObject().get("path").getAsString();

						String libURL = artifact.getAsJsonObject().get("url").getAsString();
						int libSize = artifact.getAsJsonObject().get("size").getAsInt();
						String libName = libPath.split("/")[libPath.split("/").length-1];
						String libDir = libPath.substring(0, libPath.lastIndexOf("/")+1);

						String nativesString = LibraryUtility.getNativesString(libo);
						if (!nativesString.equals("")) {
							JsonObject classifiers = libo.get("downloads").getAsJsonObject().get("classifiers").getAsJsonObject();
							JsonObject natives = classifiers.get(nativesString).getAsJsonObject();
							libURL = natives.get("url").getAsString();
							libSize = natives.get("size").getAsInt();
							String nativesPath = natives.get("path").getAsString();
							libName = nativesPath.split("/")[nativesPath.split("/").length-1];
							libDir = nativesPath.substring(0, nativesPath.lastIndexOf("/")+1);
						}
						if (!new File(launcher.getLibrariesDir()+libDir+libName).isFile()) {
							FileUtility.download(libURL, libName, launcher.getLibrariesDir()+libDir, libSize);
						}
						instance.addCommand("\""+launcher.getLibrariesDir()+libDir+libName, "\";");
					}
				}

				this.loadingBar.setProgress(0.3);

				// Version JAR
				String versionJarName = profile.getVersion()+".jar";
				String versionJarDir = launcher.getVersionsDir()+profile.getVersion()+"/";

				if (!new File(versionJarDir+versionJarName).isFile()) {
					JsonObject clientJar = version.get("downloads").getAsJsonObject().get("client").getAsJsonObject();
					String versionJarURL = clientJar.get("url").getAsString();
					int versionJarSize = clientJar.get("size").getAsInt();
					FileUtility.download(versionJarURL, versionJarName, versionJarDir, versionJarSize);
				}
				instance.addCommand("\""+versionJarDir+versionJarName+"\"");

				this.loadingBar.setProgress(0.4);

				// Main class
				instance.addCommand(version.get("mainClass").getAsString());

				// Parameters
				instance.addCommand("--username " + launcher.getGuestUsername());
				instance.addCommand("--version " + profile.getVersion());
				instance.addCommand("--gameDir " + "\""+profile.getGameDirOrDefault()+"\"");
				instance.addCommand("--assetsDir " + "\""+launcher.getAssetsDir()+"\"");
				instance.addCommand("--assetIndex " + version.get("assetIndex").getAsJsonObject().get("id").getAsString());
				instance.addCommand("--uuid " + UUID.nameUUIDFromBytes(("OfflinePlayer:"+launcher.getGuestUsername()).getBytes()));
				instance.addCommand("--accessToken " + "accessToken");
				instance.addCommand("--userType " + "legacy");
				instance.addCommand("--versionType " + version.get("type").getAsString());
				instance.addCommand("--width " + profile.getWidthOrDefault());
				instance.addCommand("--height " + profile.getHeightOrDefault());

				this.loadingBar.setProgress(0.5);

				// Assets
				JsonObject assetIndex = version.get("assetIndex").getAsJsonObject();
				String assetIndexURL = assetIndex.get("url").getAsString();
				String assetIndexName = assetIndexURL.split("/")[assetIndexURL.split("/").length-1];
				String assetIndexDir = launcher.getAssetsDir()+"indexes/";

				if (!new File(assetIndexDir+assetIndexName).isFile()) {
					int assetIndexSize = assetIndex.get("size").getAsInt();
					FileUtility.download(assetIndexURL, assetIndexName, assetIndexDir, assetIndexSize);
					JsonObject assets = JsonUtility.load(assetIndexDir+assetIndexName).getAsJsonObject().get("objects").getAsJsonObject();
					for(Entry<String, JsonElement> e : assets.entrySet()) {
						JsonObject asset = e.getValue().getAsJsonObject();
						String assetName = asset.get("hash").getAsString();
						String assetDir = assetName.substring(0, 2)+"/";
						int assetSize = asset.get("size").getAsInt();
						FileUtility.download("https://resources.download.minecraft.net/"+assetDir+assetName, assetName, launcher.getAssetsDir()+"objects/"+assetDir, assetSize);
					}
				}

				this.loadingBar.setProgress(1.0);
				this.loadingBar.setVisible(false);

				System.out.println(instance.getCommand());

				instance.runInstance();

				this.instances.add(instance);

			} else {
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.setTitle(this.resources.getString("launch.manifest"));
				dialog.setHeaderText(this.resources.getString("launch.manifest.desc"));
				dialog.show();
			}
		}
	}

	/**
	 * Display a message saying that no profile is selected
	 */
	private void showNoSelectionDialog() {
		Alert dialog = new Alert(AlertType.ERROR);
		dialog.setTitle(this.resources.getString("launch.error"));
		dialog.setHeaderText(this.resources.getString("launch.error.desc"));
		dialog.showAndWait();
	}

	/**
	 * Switch to the login view
	 */
	@FXML
	private void goToLoginScreen() {
		Main.switchView();
	}

	/**
	 * Creates a new profile and select it
	 */
	@FXML
	private void newProfile() {
		GameProfile newProfile = new GameProfile();

		this.listViewProfile.getItems().add(newProfile);

		this.listViewProfile.getSelectionModel().select(newProfile);
		updateProfileEditor();
		this.choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
		updateVersionLabel();
		launcher.saveProfile();
	}

	/**
	 * Deletes the selected profile
	 */
	@FXML
	private void deleteProfile() {
		GameProfile selectedVer = this.listViewProfile.getSelectionModel().getSelectedItem();

		if (selectedVer != null) {
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle(this.resources.getString("profile.delete.title"));
			dialog.setHeaderText(this.resources.getString("profile.delete.header"));
			dialog.setContentText(this.resources.getString("profile.delete.content"));
			dialog.getButtonTypes().set(0, ButtonType.YES);
			dialog.getButtonTypes().set(1, ButtonType.NO);
			Optional<ButtonType> choice = dialog.showAndWait();
			if (choice.get() == ButtonType.YES) {
				this.listViewProfile.getItems().remove(selectedVer);
				updateProfileEditor();
				this.choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
				updateVersionLabel();
				launcher.saveProfile();
			}
		} else {
			showNoSelectionDialog();
		}
	}

	/**
	 * Duplicate the selected profile and select the new profile
	 */
	@FXML
	private void duplicateProfile() {
		GameProfile selectedVer = this.listViewProfile.getSelectionModel().getSelectedItem();

		if (selectedVer != null) {
			GameProfile newProfile = new GameProfile(selectedVer.getLastUsed(), selectedVer.getVersion(), selectedVer.toString()+" "+resources.getString("profile.editor.copy"), PROFILE_TYPE.CUSTOM);
			newProfile.setGameDir(selectedVer.getGameDir());
			newProfile.setWidth(selectedVer.getWidth());
			newProfile.setHeight(selectedVer.getHeight());
			newProfile.setExecutable(selectedVer.getExecutable());
			newProfile.setJvmArguments(selectedVer.getJvmArguments());
			this.listViewProfile.getItems().add(newProfile);
			this.listViewProfile.getSelectionModel().select(newProfile);
			updateProfileEditor();
			launcher.saveProfile();
		} else {
			showNoSelectionDialog();
		}
	}

	/**
	 * Checks if the resolutions are valide<br>
	 * They are valid if they are between 100 and 9999
	 */
	private void checkResolution() {
		if (!width.getText().matches("^[1-9][0-9]{2,3}$")) {
			width.setText("");
		}
		if (!height.getText().matches("^[1-9][0-9]{2,3}$")) {
			height.setText("");
		}
	}

	/**
	 * Open a window to select the game directory
	 */
	@FXML
	private void selectDir() {
		GameProfile gp = choiceBoxProfile.getSelectionModel().getSelectedItem();
		DirectoryChooser dc = new DirectoryChooser();
		dc.setInitialDirectory(new File(gp.getGameDirOrDefault()));
		File f = dc.showDialog(Main.getPrimaryStage());
		if (f != null) {
			gameDir.setText(f.getAbsolutePath());
		}
	}

	/**
	 * Open a window to select the Jave executable
	 */
	@FXML
	private void selectExe() {
		FileChooser fc = new FileChooser();
		File f = fc.showOpenDialog(Main.getPrimaryStage());
		if (f != null) {
			executable.setText(f.getAbsolutePath());
		}
	}

	/**
	 * Update the list view, used in case a profile's name was changed
	 */
	@FXML
	private void updateListView() {
		GameProfile editing = this.listViewProfile.getSelectionModel().getSelectedItem();
		GameProfile selected = this.choiceBoxProfile.getSelectionModel().getSelectedItem();
		int index = this.listViewProfile.getItems().indexOf(editing);
		this.listViewProfile.getItems().set(index, editing);
		if (editing == selected) {
			this.choiceBoxProfile.getSelectionModel().select(editing);
		}
	}

	/**
	 * Update the label showing the selected profile's version
	 */
	private void updateVersionLabel() {
		GameProfile profile = this.choiceBoxProfile.getSelectionModel().getSelectedItem();
		if (profile != null) {
			String version = profile.getVersion();
			selectedProfileVersion.setText(version);
		} else {
			selectedProfileVersion.setText("");
		}
	}

}
