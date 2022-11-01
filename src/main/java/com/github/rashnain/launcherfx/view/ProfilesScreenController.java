package main.java.com.github.rashnain.launcherfx.view;

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
import main.java.com.github.rashnain.launcherfx.LauncherFX;
import main.java.com.github.rashnain.launcherfx.Util;
import main.java.com.github.rashnain.launcherfx.model.GameInstance;
import main.java.com.github.rashnain.launcherfx.model.GameProfile;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;
import main.java.com.github.rashnain.launcherfx.model.VERSION_TYPE;

public class ProfilesScreenController {
	
	private boolean initialized;
	
	private ResourceBundle resources;
	
	private LauncherProfile launcher;
	
	private List<GameInstance> instances;
	
	private GameProfile previous;
	
	@FXML
	private Label pseudo;
	
	@FXML
	private Label pseudoStatus;
	
	@FXML
	private ListView<GameProfile> listViewVersions;
	
	@FXML
	private ChoiceBox<GameProfile> choiceBoxVersion;
	
	@FXML
	private ProgressBar loadingBar;
	
	@FXML
	private VBox profileEditor;
	
	@FXML
	private TextField name;
	
	@FXML
	private TextField version;
	
	@FXML
	private TextField gameDir;
	
	@FXML
	private TextField width;
	
	@FXML
	private TextField height;
	
	@FXML
	private TextField java;
	
	@FXML
	private TextField jvmArgs;
	
	public void initializeView() {
		if (!initialized) {
			this.resources = LauncherFX.getResources();
			this.launcher = LauncherProfile.getProfile();
			this.instances = new ArrayList<>();
			
			this.listViewVersions.setItems(launcher.getGameProfiles());
			this.choiceBoxVersion.setItems(this.listViewVersions.getItems());
			
			this.listViewVersions.getSelectionModel().select(launcher.lastUsedProfile());
			this.choiceBoxVersion.getSelectionModel().select(launcher.lastUsedProfile());
			updateProfileEditor();
			
			initialized = true;
		}
		pseudo.setText(launcher.getGuestUsername());
	}
	
	@FXML
	private void onClickOnViewList(MouseEvent event) throws Exception {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() > 1) {
				loadGame();
			} else {
				updateProfileEditor();
			}
		}
	}
	
	@FXML
	private void onKeyPressedViewList(KeyEvent event) {
		if (event.getCode().equals(KeyCode.DELETE)) {
			deleteProfile();
		}
		// TODO add CTRL+D for duplication and CTRL+N for creation
	}
	
	@FXML
	private void updateProfileEditor() {
		GameProfile profile = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		if (profile != null) {
			profileEditor.setVisible(true);
			
			if (previous != null) {
				previous.getNameProperty().unbind();
				previous.getVersionIdProperty().unbind();
				previous.getGameDirProperty().unbind();
				previous.getWidthProperty().unbind();
				previous.getHeightProperty().unbind();
				previous.getExecutableProperty().unbind();
				previous.getJvmArgumentsProperty().unbind();
			}
			
			if (profile.getVersionType() == VERSION_TYPE.CUSTOM) {
				name.setDisable(false);
				name.setText(profile.getName());
				profile.getNameProperty().bind(name.textProperty());
				version.setDisable(false);
				version.setText(profile.getVersionId());
				profile.getVersionIdProperty().bind(version.textProperty());
			} else {
				name.setText(profile.toString());
				name.setDisable(true);
				version.setText(profile.getVersionId());
				version.setDisable(true);
			}
			gameDir.setText(profile.getEditableGameDir());
			profile.getGameDirProperty().bind(gameDir.textProperty());
			width.setText(profile.getEditableWidth());
			profile.getWidthProperty().bind(width.textProperty());
			height.setText(profile.getEditableHeight());
			profile.getHeightProperty().bind(height.textProperty());
			checkResolution();
			java.setText(profile.getEditableExecutable());
			profile.getExecutableProperty().bind(java.textProperty());
			jvmArgs.setText(profile.getJvmArguments());
			profile.getJvmArgumentsProperty().bind(jvmArgs.textProperty());
			
			previous = profile;
		} else {
			this.profileEditor.setVisible(false);
		}
	}
	
	@FXML
	private void loadGame() throws Exception {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		boolean ignoreConflicts = false;
		
		// checks if there isn't another instance running in the same directory
		if (selectedVer != null) {
			checkResolution();
			launcher.saveProfile();
			ignoreConflicts = true;
			Iterator<GameInstance> it = this.instances.iterator();
			while (it.hasNext()) {
				GameInstance gi = it.next();
				if (!gi.getProcess().isAlive()) {
					it.remove();
					continue;
				}
				if (gi.getGameDir().equals(selectedVer.getGameDir())) {
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
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("launch.error"));
			dialog.setHeaderText(this.resources.getString("launch.error.desc"));
			dialog.show();
		}

		if (ignoreConflicts) {
			this.loadingBar.setProgress(0);
			this.loadingBar.setVisible(true);
			
			boolean manifestExists = false;
			
			if (launcher.isOnline()) {
				Util.downloadFile(LauncherFX.VERSION_MANIFEST, "version_manifest_v2.json", launcher.getVersionsDir(), 0);
				JsonObject versionManifest = Util.loadJSON(launcher.getVersionsDir()+"version_manifest_v2.json");;
				JsonArray versionArray = versionManifest.get("versions").getAsJsonArray();
				for (JsonElement entry : versionArray) {
					if (entry.getAsJsonObject().get("id").getAsString().equals(selectedVer.getVersionId())) {
						String versionJsonURL = entry.getAsJsonObject().get("url").getAsString();
						Util.downloadFile(versionJsonURL, selectedVer.getVersionId()+".json", launcher.getVersionsDir()+selectedVer.getVersionId()+"/", 0);
						break;
					}
				}
			}
			
			String versionJsonURI = launcher.getVersionsDir()+selectedVer.getVersionId()+"/"+selectedVer.getVersionId()+".json";
			
			if (new File(versionJsonURI).isFile()) {
				manifestExists = true;
			}
			
			if (manifestExists) {
				JsonObject version = Util.loadJSON(versionJsonURI);
				
				GameInstance instance = new GameInstance(selectedVer.getGameDir());
				
				// Java executable + JVM arguments
				instance.addCommand(selectedVer.getExecutableOrDefault());
				instance.addCommand(selectedVer.getJvmArguments());
				
				this.loadingBar.setProgress(0.1);
				
				// Library path
				instance.addCommand("-Djava.library.path="+launcher.getVersionsDir()+selectedVer.getVersionId()+"/natives/");
				
				// Classpath, TODO download only required libraries
				instance.addCommand("-cp");
				JsonArray libraries = version.get("libraries").getAsJsonArray();
				for (JsonElement lib : libraries) {
					JsonObject libo = lib.getAsJsonObject();
					if (Util.shouldUseLibrary(libo)) {
						JsonObject artifact = libo.get("downloads").getAsJsonObject().get("artifact").getAsJsonObject();
						String libPath = artifact.getAsJsonObject().get("path").getAsString();
						
						String libURL = artifact.getAsJsonObject().get("url").getAsString();
						int libSize = artifact.getAsJsonObject().get("size").getAsInt();
						String libName = libPath.split("/")[libPath.split("/").length-1];
						String libDir = libPath.substring(0, libPath.lastIndexOf("/")+1);
						
						String nativesString = Util.getNativesString(libo);
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
							Util.downloadFile(libURL, libName, launcher.getLibrariesDir()+libDir, libSize);
						}
						instance.addCommand(launcher.getLibrariesDir()+libDir+libName, ";");
					}
				}
				
				this.loadingBar.setProgress(0.3);
				
				// Version JAR
				String versionJarName = selectedVer.getVersionId()+".jar";
				String versionJarDir = launcher.getVersionsDir()+selectedVer.getVersionId()+"/";
				
				if (!new File(versionJarDir+versionJarName).isFile()) {
					JsonObject clientJar = version.get("downloads").getAsJsonObject().get("client").getAsJsonObject();
					String versionJarURL = clientJar.get("url").getAsString();
					int versionJarSize = clientJar.get("size").getAsInt();
					Util.downloadFile(versionJarURL, versionJarName, versionJarDir, versionJarSize);
				}
				instance.addCommand(versionJarDir+versionJarName);
				
				this.loadingBar.setProgress(0.4);
				
				// Main class
				instance.addCommand(version.get("mainClass").getAsString());
				
				// Parameters
				instance.addCommand("--username " + launcher.getGuestUsername());
				instance.addCommand("--version " + selectedVer.getVersionId());
				instance.addCommand("--gameDir " + selectedVer.getGameDirOrDefault());
				instance.addCommand("--assetsDir " + launcher.getAssetsDir());
				instance.addCommand("--assetIndex " + version.get("assetIndex").getAsJsonObject().get("id").getAsString());
				instance.addCommand("--uuid " + UUID.nameUUIDFromBytes(("OfflinePlayer:"+launcher.getGuestUsername()).getBytes()));
				instance.addCommand("--accessToken " + "accessToken");
				instance.addCommand("--userType " + "legacy");
				instance.addCommand("--versionType " + version.get("type").getAsString());
				instance.addCommand("--width " + selectedVer.getWidthOrDefault());
				instance.addCommand("--height " + selectedVer.getHeightOrDefault());
				
				this.loadingBar.setProgress(0.5);
				
				// Assets
				JsonObject assetIndex = version.get("assetIndex").getAsJsonObject();
				String assetIndexURL = assetIndex.get("url").getAsString();
				String assetIndexName = assetIndexURL.split("/")[assetIndexURL.split("/").length-1];
				String assetIndexDir = launcher.getAssetsDir()+"indexes/";
				
				if (!new File(assetIndexDir+assetIndexName).isFile()) {
					int assetIndexSize = assetIndex.get("size").getAsInt();
					Util.downloadFile(assetIndexURL, assetIndexName, assetIndexDir, assetIndexSize);
					JsonObject assets = Util.loadJSON(assetIndexDir+assetIndexName).getAsJsonObject().get("objects").getAsJsonObject();
					for(Entry<String, JsonElement> e : assets.entrySet()) {
						JsonObject asset = e.getValue().getAsJsonObject();
						String assetName = asset.get("hash").getAsString();
						String assetDir = assetName.substring(0, 2)+"/";
						int assetSize = asset.get("size").getAsInt();
						Util.downloadFile("https://resources.download.minecraft.net/"+assetDir+assetName, assetName, launcher.getAssetsDir()+"objects/"+assetDir, assetSize);
					}
				}
				
				this.loadingBar.setProgress(1.0);
				this.loadingBar.setVisible(false);
				
				System.out.println(instance.getCommand());
				
				instance.runInstance();
				
				this.instances.add(instance);
				
				selectedVer.setLastUsed(Instant.now());

			} else {
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.setTitle(this.resources.getString("launch.manifest"));
				dialog.setHeaderText(this.resources.getString("launch.manifest.desc"));
				dialog.show();
			}
		}
	}
	
	@FXML
	private void goToLoginScreen() throws IOException {
		LauncherFX.switchView();
	}
	
	@FXML
	private void newProfile() {
		GameProfile newProfile = new GameProfile("", "latest-release", Instant.EPOCH, VERSION_TYPE.CUSTOM);
		
		this.listViewVersions.getItems().add(newProfile);
		
		this.listViewVersions.getSelectionModel().select(newProfile);
		updateProfileEditor();
		launcher.saveProfile();
	}
	
	@FXML
	private void deleteProfile() {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		if (selectedVer != null) {
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle(this.resources.getString("profile.delete.title"));
			dialog.setHeaderText(this.resources.getString("profile.delete.header"));
			dialog.setContentText(this.resources.getString("profile.delete.content"));
			dialog.getButtonTypes().set(0, ButtonType.YES);
			dialog.getButtonTypes().set(1, ButtonType.NO);
			Optional<ButtonType> choice = dialog.showAndWait();
			if (choice.get() == ButtonType.YES) {
				this.listViewVersions.getItems().remove(selectedVer);
				updateProfileEditor();
				launcher.saveProfile();
			}
		} else {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("launch.error"));
			dialog.setHeaderText(this.resources.getString("launch.error.desc"));
			dialog.show();
		}
	}
	
	@FXML
	private void duplicateProfile() {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		if (selectedVer != null) {
			GameProfile newProfile = new GameProfile(selectedVer.toString()+" "+resources.getString("profile.editor.copy"), selectedVer.getVersionId(), selectedVer.getLastUsed(), VERSION_TYPE.CUSTOM);
			newProfile.setGameDir(selectedVer.getGameDir());
			newProfile.setExecutable(selectedVer.getExecutable());
			newProfile.setJvmArguments(selectedVer.getJvmArguments());
			newProfile.setWidth(selectedVer.getWidth());
			newProfile.setHeight(selectedVer.getHeight());
			this.listViewVersions.getItems().add(newProfile);
			this.listViewVersions.getSelectionModel().select(newProfile);
			updateProfileEditor();
			launcher.saveProfile();
		} else {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("launch.error"));
			dialog.setHeaderText(this.resources.getString("launch.error.desc"));
			dialog.show();
		}
	}
	
	private void checkResolution() {
		if (!width.getText().matches("^[1-9][0-9]{2,3}$")) {
			width.setText("");
		}
		if (!height.getText().matches("^[1-9][0-9]{2,3}$")) {
			height.setText("");
		}
	}
	
	@FXML
	private void selectDir() {
		DirectoryChooser fc = new DirectoryChooser();
		File f = fc.showDialog(LauncherFX.primaryStage);
		if (f != null) {
			gameDir.setText(f.getAbsolutePath());
		}
	}

	@FXML
	private void selectExe() {
		FileChooser fc = new FileChooser();
		File f = fc.showOpenDialog(LauncherFX.primaryStage);
		if (f != null) {
			java.setText(f.getAbsolutePath());
		}
	}
}
