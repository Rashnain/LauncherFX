package main.java.com.github.rashnain.launcherfx.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.com.github.rashnain.launcherfx.Util;
import main.java.com.github.rashnain.launcherfx.types.LauncherProfile;
import main.java.com.github.rashnain.launcherfx.types.GameInstance;
import main.java.com.github.rashnain.launcherfx.types.GameProfile;

public class SelectProfileController implements Initializable {

	private ResourceBundle resources;
	
	private List<GameInstance> instances;

	@FXML
	private Button loginScreenButton;

	@FXML
	private ListView<GameProfile> listViewVersions;

	@FXML
	private Button newProfile;
	
	@FXML
	private VBox profileEditor;
	
	@FXML
	private ProgressBar loadingBar;
	
	@FXML
	private ChoiceBox<GameProfile> choiceVersion;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;
		this.instances = new ArrayList<>();
		
		this.listViewVersions.setItems(LauncherProfile.getProfile().getGameProfiles());
		this.choiceVersion.setItems(this.listViewVersions.getItems());
		
		this.listViewVersions.getSelectionModel().select(LauncherProfile.getProfile().lastUsedProfile());
		//this.choiceVersion.getSelectionModel().select(LauncherProfile.getProfile().lastUsedProfile());
		updateProfileEditor();
	}
	
	@FXML
	private void onClickOnViewList(MouseEvent mouseEvent) {
		if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
			updateProfileEditor();
		}
	}
	
	@FXML
	private void updateProfileEditor() {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();

		if (selectedVer != null) {
			profileEditor.setVisible(true);
			((Label)((HBox)profileEditor.getChildren().get(1)).getChildren().get(1)).setText(selectedVer.getName());
			
			((Label)((HBox)profileEditor.getChildren().get(3)).getChildren().get(1)).setText(selectedVer.getId());
		} else {
			this.profileEditor.setVisible(false);
		}
	}
	
	@FXML
	private void loadGame() throws Exception {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		boolean ignoreConflicts = false;
		
		if (selectedVer != null) {
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
			
			LauncherProfile launcher = LauncherProfile.getProfile();
			
			boolean manifestExists = false;
			
			if (launcher.isOnline()) {
				Util.downloadFile(Util.VERSION_MANIFEST, "version_manifest_v2.json", launcher.getVersionsDir(), 0);
				JsonObject versionManifest = Util.loadJSON(launcher.getVersionsDir()+"version_manifest_v2.json");;
				JsonArray versionArray = versionManifest.get("versions").getAsJsonArray();
				for (JsonElement entry : versionArray) {
					if (entry.getAsJsonObject().get("id").getAsString().equals(selectedVer.getId())) {
						String versionJsonURL = entry.getAsJsonObject().get("url").getAsString();
						Util.downloadFile(versionJsonURL, selectedVer.getId()+".json", launcher.getVersionsDir()+selectedVer.getId()+"/", 0);
						break;
					}
				}
			}
			
			String versionJsonURI = launcher.getVersionsDir()+selectedVer.getId()+"/"+selectedVer.getId()+".json";
			
			if (new File(versionJsonURI).isFile()) {
				manifestExists = true;
			}
			
			if (manifestExists) {
				JsonObject version = Util.loadJSON(versionJsonURI);
				
				selectedVer.setVersionType(version.get("type").getAsString());
				
				GameInstance instance = new GameInstance(selectedVer.getGameDir());
				
				// Java executable + JVM arguments
				instance.addCommand(selectedVer.getExecutable());
				instance.addCommand(selectedVer.getJvmArguments());
				
				this.loadingBar.setProgress(0.1);
				
				// Library path
				instance.addCommand("-Djava.library.path="+launcher.getVersionsDir()+selectedVer.getId()+"/natives/");
				
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
				String versionJarName = selectedVer.getId()+".jar";
				String versionJarDir = launcher.getVersionsDir()+selectedVer.getId()+"/";
				
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
				instance.addCommand("--username " + launcher.getUsername());
				instance.addCommand("--version " + selectedVer.getId());
				instance.addCommand("--gameDir " + selectedVer.getGameDir());
				instance.addCommand("--assetsDir " + launcher.getAssetsDir());
				instance.addCommand("--assetIndex " + version.get("assetIndex").getAsJsonObject().get("id").getAsString());
				instance.addCommand("--uuid " + "uuid");
				instance.addCommand("--accessToken " + "accessToken");
				instance.addCommand("--userType " + "legacy");
				instance.addCommand("--versionType " + selectedVer.getVersionType());
				instance.addCommand("--width " + selectedVer.getResolutions()[0]);
				instance.addCommand("--height " + selectedVer.getResolutions()[1]);
				
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

			} else {
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.setTitle(this.resources.getString("launch.manifest"));
				dialog.setHeaderText(this.resources.getString("launch.manifest.desc"));
				dialog.show();
			}
		}
	}

	@FXML
	private void goToLoginScreen(ActionEvent event) throws IOException {
		Util.changeRoot("loginScreen", event);
	}

	@FXML
	private void newProfile() {
		GameProfile newProfile = new GameProfile("", "latest-release", new Date());
		
		this.listViewVersions.getItems().add(newProfile);
		
		this.listViewVersions.getSelectionModel().select(newProfile);
		updateProfileEditor();
	}
	
	@FXML
	private void deleteProfile() {
		GameProfile selectedVer = this.listViewVersions.getSelectionModel().getSelectedItem();
		
		if (selectedVer != null) {
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Delete profile");
			dialog.setHeaderText("This profile will be removed.\r\nAll concerned data won't be affected.");
			dialog.setContentText("Do you want to procede ?");
			dialog.getButtonTypes().set(0, ButtonType.YES);
			dialog.getButtonTypes().set(1, ButtonType.NO);
			Optional<ButtonType> choice = dialog.showAndWait();
			if (choice.get() == ButtonType.YES) {
				this.listViewVersions.getItems().remove(selectedVer);
				updateProfileEditor();
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
			GameProfile newProfile = new GameProfile(selectedVer.getName()+" copy", selectedVer.getId(), selectedVer.getLastUsed());
			newProfile.setGameDir(selectedVer.getGameDir());
			newProfile.setExecutable(selectedVer.getExecutable());
			newProfile.setJvmArguments(selectedVer.getJvmArguments());
			newProfile.setVersionType(selectedVer.getVersionType());
			newProfile.setResolutions(selectedVer.getResolutions()[0], selectedVer.getResolutions()[1]);
			this.listViewVersions.getItems().add(newProfile);
			this.listViewVersions.getSelectionModel().select(newProfile);
			updateProfileEditor();
		} else {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("launch.error"));
			dialog.setHeaderText(this.resources.getString("launch.error.desc"));
			dialog.show();
		}
	}
}
