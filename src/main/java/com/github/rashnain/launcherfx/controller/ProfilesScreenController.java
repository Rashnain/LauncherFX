package com.github.rashnain.launcherfx.controller;

import com.github.rashnain.launcherfx.Main;
import com.github.rashnain.launcherfx.PROFILE_TYPE;
import com.github.rashnain.launcherfx.model.GameInstance;
import com.github.rashnain.launcherfx.model.GameProfile;
import com.github.rashnain.launcherfx.model.LauncherProfile;
import com.github.rashnain.launcherfx.model.MicrosoftAccount;
import com.github.rashnain.launcherfx.utility.FileUtility;
import com.github.rashnain.launcherfx.utility.JsonUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

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
	private Button deleteButton;

	@FXML
	private ProgressBar loadingBar;

	@FXML
	private ChoiceBox<GameProfile> choiceBoxProfile;

	@FXML
	private Label selectedProfileVersion;

	@FXML
	private Button playButton;

	@FXML
	private Label pseudoStatus;

	@FXML
	private Label pseudo;

	/**
	 * Initialize view
	 */
	public void initializeView() {
		if (!initialized) {
			resources = Main.getResources();
			launcher = LauncherProfile.getProfile();
			instances = new ArrayList<>();

			try {
				FileUtility.download(Main.VERSION_MANIFEST, "version_manifest_v2.json", launcher.getVersionsDir());
				System.out.println("Online mode.");
				launcher.setOnlineStatus(true);
				// Initialize version list
				JsonObject versionManifest = JsonUtility.load(launcher.getVersionsDir()+"version_manifest_v2.json");
				for (JsonElement e : versionManifest.getAsJsonArray("versions")) {
					choiceBoxVersion.getItems().add(e.getAsJsonObject().get("id").getAsString());
				}
			} catch (IOException e1) {
				System.out.println("Offline mode.");
				launcher.setOnlineStatus(false);
			}

			JsonObject versionManifest = JsonUtility.load(launcher.getVersionsDir()+"version_manifest_v2.json");
			if (versionManifest != null) {
				JsonObject latest = versionManifest.getAsJsonObject("latest");
				GameProfile.latestRelease = latest.get("release").getAsString();
				GameProfile.latestSnapshot = latest.get("snapshot").getAsString();
				System.out.println("Latest release is " + GameProfile.latestRelease);
				System.out.println("Latest snapshot is " + GameProfile.latestSnapshot);
			} else {
				System.out.println("Couldn't define latest versions.");
			}

			// Initialize version list
			File verDir = new File(launcher.getVersionsDir());
			for (File subDir : verDir.listFiles()) {
				if (new File(subDir.getAbsolutePath()+"/").isDirectory()) {
					try {
						JsonObject jo = JsonUtility.load(subDir.getAbsolutePath()+"/"+subDir.getName()+".json");
						if (!choiceBoxVersion.getItems().contains(jo.get("id").getAsString())) {
							choiceBoxVersion.getItems().add(jo.get("id").getAsString());
						}
					} catch (Exception e) {
						System.out.println("Malformed version manifest.");
						continue;
					}
				}
			}

			listViewProfile.setItems(launcher.getGameProfiles());
			choiceBoxProfile.setItems(launcher.getGameProfiles());

			listViewProfile.getSelectionModel().selectedIndexProperty().addListener(
				(obs, oldval, newval) -> updateProfileEditor() );

			listViewProfile.getSelectionModel().select(launcher.lastUsedProfile());
			choiceBoxProfile.getSelectionModel().select(listViewProfile.getSelectionModel().getSelectedItem());
			updateVersionLabel();

			choiceBoxProfile.setOnAction( e -> updateVersionLabel() );
			choiceBoxVersion.setOnAction( e -> updateVersionLabel() );

			initialized = true;
		}

		if (launcher.isGuest()) {
			pseudo.setText(launcher.getGuestUsername());
			pseudoStatus.setText("Guest");
		} else {
			pseudo.setText(launcher.getCurrentAccount().getUsername());
			pseudoStatus.setText("");
		}
	}

	/**
	 * Launch the selected profile if the ListView is double clicked
	 * @param event Mouse
	 * @throws Exception if the instance throw an exception
	 */
	@FXML
	private void onClickOnViewList(MouseEvent event) throws Exception {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() > 1) {
				if (!playButton.isDisabled()) {
					GameProfile profile = listViewProfile.getSelectionModel().getSelectedItem();
					choiceBoxProfile.getSelectionModel().select(profile);
					loadGame(profile);
				}
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
	private void updateProfileEditor() {
		GameProfile profile = listViewProfile.getSelectionModel().getSelectedItem();

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
				deleteButton.setDisable(false);
			} else {
				name.setDisable(true);
				name.setText(profile.toString());
				choiceBoxVersion.setDisable(true);
				choiceBoxVersion.getSelectionModel().select(profile.getVersion());
				deleteButton.setDisable(true);
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
		loadGame(choiceBoxProfile.getSelectionModel().getSelectedItem());
	}

	/**
	 * Launch the given profile
	 * @param profile profile to launch
	 * @throws Exception if the instance throw an exception
	 */
	private void loadGame(GameProfile profile) {
		boolean authorizeLaunch = false;

		if (profile != null) {
			authorizeLaunch = true;
			checkResolution();
			profile.setLastUsed(Instant.now());
			launcher.saveProfile();
			// checks if there is another instance running in the same directory
			Iterator<GameInstance> it = instances.iterator();
			while (it.hasNext()) {
				GameInstance gi = it.next();
				if (!gi.getProcess().isAlive()) {
					it.remove();
					continue;
				}
				if (gi.getGameDir().equals(profile.getGameDir())) {
					Alert dialog = new Alert(AlertType.CONFIRMATION);
					dialog.initOwner(Main.getPrimaryStage());
					dialog.setTitle(resources.getString("launch.error.instance"));
					dialog.setHeaderText(resources.getString("launch.error.instance.title"));
					dialog.setContentText(resources.getString("launch.error.instance.desc"));
					dialog.getButtonTypes().set(0, ButtonType.YES);
					dialog.getButtonTypes().set(1, ButtonType.NO);
					Optional<ButtonType> choice = dialog.showAndWait();
					if (choice.get() != ButtonType.YES) {
						authorizeLaunch = false;
					}
					break;
				}
			}
		} else {
			showNoSelectionDialog();
		}

		if (authorizeLaunch) {
			boolean manifestExists = false;

			if (launcher.getOnlineStatus()) {
				JsonObject versionManifest = JsonUtility.load(launcher.getVersionsDir()+"version_manifest_v2.json");
				JsonArray versionArray = versionManifest.getAsJsonArray("versions");
				for (JsonElement entry : versionArray) {
					if (entry.getAsJsonObject().get("id").getAsString().equals(profile.getVersion())) {
						String versionJsonURL = entry.getAsJsonObject().get("url").getAsString();
						try {
							FileUtility.download(versionJsonURL, profile.getVersion()+".json", launcher.getVersionsDir()+profile.getVersion()+"/", 0);
						} catch (IOException e) {
							showCantDownloadDialog();
						}
						break;
					}
				}
			}

			String versionJsonURI = launcher.getVersionsDir()+profile.getVersion()+"/"+profile.getVersion()+".json";

			if (new File(versionJsonURI).isFile()) {
				manifestExists = true;
			}

			if (manifestExists) {
				GameInstance instance = new GameInstance(profile);

				loadingBar.progressProperty().unbind();
				loadingBar.visibleProperty().unbind();
				playButton.disableProperty().unbind();

				loadingBar.progressProperty().bind(instance.getLoadingProgressProperty());
				loadingBar.visibleProperty().bind(instance.getLoadingVisibilityProperty());
				playButton.disableProperty().bind(instance.getLoadingVisibilityProperty());

				instance.startThread();

				instances.add(instance);
			} else {
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.initOwner(Main.getPrimaryStage());
				dialog.setTitle(resources.getString("launch.error.manifest"));
				dialog.setHeaderText(resources.getString("launch.error.manifest.desc"));
				dialog.show();
			}
		}
	}

	/**
	 * Display a message saying that no profile is selected
	 */
	private void showNoSelectionDialog() {
		Alert dialog = new Alert(AlertType.ERROR);
		dialog.initOwner(Main.getPrimaryStage());
		dialog.setTitle(resources.getString("launch.error"));
		dialog.setHeaderText(resources.getString("launch.error.desc"));
		dialog.showAndWait();
	}

	/**
	 * Display a message saying that a file could not be downloaded
	 */
	private void showCantDownloadDialog() {
		Alert dialog = new Alert(AlertType.ERROR);
		dialog.initOwner(Main.getPrimaryStage());
		dialog.setTitle(resources.getString("launch.error.connection"));
		dialog.setHeaderText(resources.getString("launch.error.connection.desc"));
		dialog.showAndWait();
	}

	/**
	 * Switch to the login view
	 */
	@FXML
	private void goToLoginScreen() {
		if (!launcher.isRememberMe()) {
			if (launcher.getAccounts().isEmpty())
				launcher.setCurrentAccount(new MicrosoftAccount());
			else
				launcher.setCurrentAccount(launcher.lastUsedAccount());
		}
		Main.switchView();
	}

	/**
	 * Creates a new profile and select it
	 */
	@FXML
	private void newProfile() {
		GameProfile profile = new GameProfile();

		listViewProfile.getItems().add(profile);

		listViewProfile.getSelectionModel().select(profile);
		updateProfileEditor();
		choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
		updateVersionLabel();
		launcher.saveProfile();
	}

	/**
	 * Deletes the selected profile
	 */
	@FXML
	private void deleteProfile() {
		GameProfile profile = listViewProfile.getSelectionModel().getSelectedItem();

		if (profile != null) {
			if (profile.getVersionType() == PROFILE_TYPE.CUSTOM) {
				Alert dialog = new Alert(AlertType.CONFIRMATION);
				dialog.initOwner(Main.getPrimaryStage());
				dialog.setTitle(resources.getString("profile.delete.title"));
				dialog.setHeaderText(resources.getString("profile.delete.header"));
				dialog.setContentText(resources.getString("profile.delete.content"));
				dialog.getButtonTypes().set(0, ButtonType.YES);
				dialog.getButtonTypes().set(1, ButtonType.NO);
				Optional<ButtonType> choice = dialog.showAndWait();
				if (choice.get() == ButtonType.YES) {
					listViewProfile.getItems().remove(profile);
					updateProfileEditor();
					choiceBoxProfile.getSelectionModel().select(launcher.lastUsedProfile());
					updateVersionLabel();
					launcher.saveProfile();
				}
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
		GameProfile profile = listViewProfile.getSelectionModel().getSelectedItem();

		if (profile != null) {
			GameProfile newProfile = new GameProfile(profile.getLastUsed(), profile.getVersion(), profile.toString()+" "+resources.getString("profile.editor.copy"), PROFILE_TYPE.CUSTOM);
			newProfile.setGameDir(profile.getGameDir());
			newProfile.setWidth(profile.getWidth());
			newProfile.setHeight(profile.getHeight());
			newProfile.setExecutable(profile.getExecutable());
			newProfile.setJvmArguments(profile.getJvmArguments());
			listViewProfile.getItems().add(newProfile);
			listViewProfile.getSelectionModel().select(newProfile);
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
		GameProfile editing = listViewProfile.getSelectionModel().getSelectedItem();
		GameProfile selected = choiceBoxProfile.getSelectionModel().getSelectedItem();
		int index = listViewProfile.getItems().indexOf(editing);
		listViewProfile.getItems().set(index, editing);
		if (editing == selected) {
			choiceBoxProfile.getSelectionModel().select(editing);
		}
	}

	/**
	 * Update the label showing the selected profile's version
	 */
	private void updateVersionLabel() {
		GameProfile profile = choiceBoxProfile.getSelectionModel().getSelectedItem();
		if (profile != null) {
			String version = profile.getVersion();
			selectedProfileVersion.setText(version);
		} else {
			selectedProfileVersion.setText("");
		}
	}

}
