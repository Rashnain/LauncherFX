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

public class ProfilesScreenController {
	
	private boolean initialized;
	
	private ResourceBundle resources;
	
	private LauncherProfile launcher;
	
	private List<GameInstance> instances;
	
	private GameProfile previous;
	
	@FXML
	private Label pseudo;
	
	@FXML
	private Label selectedProfileVersion;
	
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
	private ChoiceBox<String> version;
	
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
						version.getItems().add(e.getAsJsonObject().get("id").getAsString());
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
					if (jo.get("type").getAsString().equals("release") && !version.getItems().contains(sub.getName())) {
						version.getItems().add(sub.getName());
					}
				}
			}
			
			this.listViewVersions.setItems(launcher.getGameProfiles());
			this.choiceBoxVersion.setItems(this.listViewVersions.getItems());
			
			this.listViewVersions.getSelectionModel().select(launcher.lastUsedProfile());
			this.choiceBoxVersion.getSelectionModel().select(launcher.lastUsedProfile());
			updateProfileEditor();
			updateVersionString();
			
			this.choiceBoxVersion.setOnAction( e -> updateVersionString() );
			
			initialized = true;
		}
		pseudo.setText(launcher.getGuestUsername());
	}
	
	@FXML
	private void onClickOnViewList(MouseEvent event) throws Exception {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() > 1) {
				loadGame(this.listViewVersions.getSelectionModel().getSelectedItem());
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
		if (event.getCode().equals(KeyCode.N)) {
			newProfile();
		}
		if (event.getCode().equals(KeyCode.D)) {
			duplicateProfile();
		}
	}
	
	@FXML
	private void updateProfileEditor() {
		GameProfile profile = this.listViewVersions.getSelectionModel().getSelectedItem();
		
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
				version.setDisable(false);
				version.getSelectionModel().select(profile.getVersion());
				profile.getVersionProperty().bind(version.getSelectionModel().selectedItemProperty());
			} else {
				name.setText(profile.toString());
				name.setDisable(true);
				version.getSelectionModel().select(profile.getVersion());
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
			hideProfileEditor(true);
		}
	}
	
	private void hideProfileEditor(boolean visibility) {
		for (int i = 0; i < profileEditor.getChildren().size()-1; i ++) {
			profileEditor.getChildren().get(i).setVisible(!visibility);
		}
	}
	
	@FXML
	private void onPlayButtonAction() throws Exception {
		loadGame(this.choiceBoxVersion.getSelectionModel().getSelectedItem());
	}
	
	private void loadGame(GameProfile ver) throws Exception {
		boolean ignoreConflicts = false;
		
		// checks if there isn't another instance running in the same directory
		if (ver != null) {
			this.choiceBoxVersion.getSelectionModel().select(ver);
			checkResolution();
			launcher.saveProfile();
			ver.setLastUsed(Instant.now());
			this.choiceBoxVersion.getSelectionModel().select(launcher.lastUsedProfile());
			ignoreConflicts = true;
			Iterator<GameInstance> it = this.instances.iterator();
			while (it.hasNext()) {
				GameInstance gi = it.next();
				if (!gi.getProcess().isAlive()) {
					it.remove();
					continue;
				}
				if (gi.getGameDir().equals(ver.getGameDir())) {
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
					if (entry.getAsJsonObject().get("id").getAsString().equals(ver.getVersion())) {
						String versionJsonURL = entry.getAsJsonObject().get("url").getAsString();
						FileUtility.download(versionJsonURL, ver.getVersion()+".json", launcher.getVersionsDir()+ver.getVersion()+"/", 0);
						break;
					}
				}
			}
			
			String versionJsonURI = launcher.getVersionsDir()+ver.getVersion()+"/"+ver.getVersion()+".json";
			
			if (new File(versionJsonURI).isFile()) {
				manifestExists = true;
			}
			
			if (manifestExists) {
				JsonObject version = JsonUtility.load(versionJsonURI);
				
				GameInstance instance = new GameInstance(ver.getGameDir());
				
				// Java executable + JVM arguments
				instance.addCommand("\""+ver.getExecutableOrDefault()+"\"");
				instance.addCommand(ver.getJvmArguments());
				
				this.loadingBar.setProgress(0.1);
				
				// Library path
				instance.addCommand("-Djava.library.path=\""+launcher.getVersionsDir()+ver.getVersion()+"/natives/\"");
				
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
				String versionJarName = ver.getVersion()+".jar";
				String versionJarDir = launcher.getVersionsDir()+ver.getVersion()+"/";
				
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
				instance.addCommand("--version " + ver.getVersion());
				instance.addCommand("--gameDir " + "\""+ver.getGameDirOrDefault()+"\"");
				instance.addCommand("--assetsDir " + "\""+launcher.getAssetsDir()+"\"");
				instance.addCommand("--assetIndex " + version.get("assetIndex").getAsJsonObject().get("id").getAsString());
				instance.addCommand("--uuid " + UUID.nameUUIDFromBytes(("OfflinePlayer:"+launcher.getGuestUsername()).getBytes()));
				instance.addCommand("--accessToken " + "accessToken");
				instance.addCommand("--userType " + "legacy");
				instance.addCommand("--versionType " + version.get("type").getAsString());
				instance.addCommand("--width " + ver.getWidthOrDefault());
				instance.addCommand("--height " + ver.getHeightOrDefault());
				
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
	
	private void showNoSelectionDialog() {
		Alert dialog = new Alert(AlertType.ERROR);
		dialog.setTitle(this.resources.getString("launch.error"));
		dialog.setHeaderText(this.resources.getString("launch.error.desc"));
		dialog.showAndWait();
	}
	
	@FXML
	private void goToLoginScreen() throws IOException {
		Main.switchView();
	}
	
	@FXML
	private void newProfile() {
		GameProfile newProfile = new GameProfile();
		
		this.listViewVersions.getItems().add(newProfile);
		
		this.listViewVersions.getSelectionModel().select(newProfile);
		updateProfileEditor();
		this.choiceBoxVersion.getSelectionModel().select(launcher.lastUsedProfile());
		updateVersionString();
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
				this.choiceBoxVersion.getSelectionModel().select(launcher.lastUsedProfile());
				updateVersionString();
				launcher.saveProfile();
			}
		} else {
			showNoSelectionDialog();
		}
	}
	
	@FXML
	private void duplicateProfile() {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		if (selectedVer != null) {
			GameProfile newProfile = new GameProfile(selectedVer.getLastUsed(), selectedVer.getVersion(), selectedVer.toString()+" "+resources.getString("profile.editor.copy"), PROFILE_TYPE.CUSTOM);
			newProfile.setGameDir(selectedVer.getGameDir());
			newProfile.setWidth(selectedVer.getWidth());
			newProfile.setHeight(selectedVer.getHeight());
			newProfile.setExecutable(selectedVer.getExecutable());
			newProfile.setJvmArguments(selectedVer.getJvmArguments());
			this.listViewVersions.getItems().add(newProfile);
			this.listViewVersions.getSelectionModel().select(newProfile);
			updateProfileEditor();
			launcher.saveProfile();
		} else {
			showNoSelectionDialog();
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
		GameProfile gp = choiceBoxVersion.getSelectionModel().getSelectedItem();
		DirectoryChooser dc = new DirectoryChooser();
		dc.setInitialDirectory(new File(gp.getGameDirOrDefault()));
		File f = dc.showDialog(Main.getPrimaryStage());
		if (f != null) {
			gameDir.setText(f.getAbsolutePath());
		}
	}

	@FXML
	private void selectExe() {
		FileChooser fc = new FileChooser();
		File f = fc.showOpenDialog(Main.getPrimaryStage());
		if (f != null) {
			java.setText(f.getAbsolutePath());
		}
	}
	
	@FXML
	private void updateListView() {
		GameProfile gp = this.listViewVersions.getSelectionModel().getSelectedItem();
		GameProfile selected = this.choiceBoxVersion.getSelectionModel().getSelectedItem();
		int index = this.listViewVersions.getItems().indexOf(gp);
		this.listViewVersions.getItems().set(index, gp);
		if (gp == selected) {
			this.choiceBoxVersion.getSelectionModel().select(gp);
		}
	}
	
	private void updateVersionString() {
		GameProfile profile = this.choiceBoxVersion.getSelectionModel().getSelectedItem();
		if (profile != null) {
			String version = profile.getVersion();
			selectedProfileVersion.setText(version);
		} else {
			selectedProfileVersion.setText("");
		}
	}
}
