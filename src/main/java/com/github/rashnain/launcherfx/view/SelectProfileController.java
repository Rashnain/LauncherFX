package main.java.com.github.rashnain.launcherfx.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.com.github.rashnain.launcherfx.Util;
import main.java.com.github.rashnain.launcherfx.types.LauncherProfile;
import main.java.com.github.rashnain.launcherfx.types.GameProfile;

public class SelectProfileController implements Initializable {

	// private Stage primaryStage;

	private ResourceBundle resources;

	private ObservableList<GameProfile> listVersions;

	@FXML
	private Button loginScreenButton;

	@FXML
	private ListView<GameProfile> listViewVersions;

	@FXML
	private Button newProfile;
	
	@FXML
	private VBox profileData;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO stores data in some way and load them here

		this.resources = resources;
		this.listVersions = FXCollections.observableArrayList();
		GameProfile ver = new GameProfile("Vanilla 1.18.1", "1.18.1");
		ver.setExecutable("\"C:/Program Files/Java/Eclipse Temurin/JDK 17/bin/javaw.exe\"");
		this.listVersions.add(ver);
		profileData.setVisible(false);
		this.listViewVersions.setItems(this.listVersions);
	}
	
	@FXML
	private void verifySelection() {
		int index = this.listViewVersions.getFocusModel().getFocusedIndex();

		if (index != -1) {
			profileData.setVisible(true);
			GameProfile selectedVer = this.listVersions.get(index);
			((Label)((HBox)profileData.getChildren().get(1)).getChildren().get(1)).setText(selectedVer.getName());
			
			((Label)((HBox)profileData.getChildren().get(3)).getChildren().get(1)).setText(selectedVer.getId());
		}
	}

	@FXML
	private void onClickOnViewList(MouseEvent mouseEvent) {
		if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
			verifySelection();
		}
	}
	
	@FXML
	private void loadGame() throws IOException {
		int index = this.listViewVersions.getFocusModel().getFocusedIndex();

		if (index != -1) {
			GameProfile selectedVer = this.listVersions.get(index);
			LauncherProfile launcherProfile = LauncherProfile.getProfile();
			
			JsonObject versionManifest = Util.downloadJSON(Util.MANIFEST_VERIONS).getAsJsonObject();
			
			JsonArray versionsArray = versionManifest.get("versions").getAsJsonArray();
			
			String versionURL = "";

			for (JsonElement entry : versionsArray) {
				if (entry.getAsJsonObject().get("id").getAsString().equals(selectedVer.getId())) {
					versionURL = entry.getAsJsonObject().get("url").getAsString();
					break;
				}
			}

			if (!versionURL.equals("")) {
				JsonObject version = Util.downloadJSON(versionURL).getAsJsonObject();
				
				JsonArray game = version.get("arguments").getAsJsonObject().get("game").getAsJsonArray();

				// Java executable + JVM arguments
				String executionCommand = selectedVer.getExecutable() + " -Xmx4G ";
				// Natives directory
				executionCommand += "-Djava.library.path="+launcherProfile.getVersionsDir()+selectedVer.getId()+"/natives/ ";
				
				// TODO include -cp
				// Version JAR
				JsonObject clientJar = version.get("downloads").getAsJsonObject().get("client").getAsJsonObject();
				String versionJarURL = clientJar.get("url").getAsString();
				int versionJarSize = clientJar.get("size").getAsInt();
				Util.downloadFile(versionJarURL, selectedVer.getId()+".jar", launcherProfile.getVersionsDir()+selectedVer.getId()+"/", versionJarSize);
				executionCommand += launcherProfile.getVersionsDir()+selectedVer.getId()+"/"+selectedVer.getId()+".jar" + " ";
				
				// Main class
				executionCommand += version.get("mainClass").getAsString() + " ";
				
				// Parameter (username, uuid, ...)
				for (int i = 0; i < game.size(); i += 2) {
					if (game.get(i).isJsonPrimitive()) { // ignore optional parameter
						String gameArgument = game.get(i).getAsString();
						executionCommand += gameArgument + " ";
						if (launcherProfile.getData().containsKey(gameArgument)) {
							executionCommand += launcherProfile.getData().get(gameArgument) + " ";
						} else if (selectedVer.toHashMap().containsKey(gameArgument)) {
							executionCommand += selectedVer.toHashMap().get(gameArgument) + " ";
						}
					}
				}
				// Parameter (width, height)
				if (selectedVer.toHashMap().containsKey("--width")) {
					executionCommand += "--width " + selectedVer.toHashMap().get("--width") + " --height " + selectedVer.toHashMap().get("--height");
				}
				
				System.out.println(executionCommand);
				
				//Process proc = Runtime.getRuntime().exec(executionCommand);
			}
		} else {
			Alert dialog = new Alert(AlertType.INFORMATION);
			dialog.setTitle(this.resources.getString("launch.error"));
			dialog.setHeaderText(this.resources.getString("launch.error.desc"));
			dialog.show();
		}
	}

	@FXML
	protected void goToLoginScreen(ActionEvent event) throws IOException {
		Util.changeRoot("loginScreen", event);
	}

	@FXML
	protected void newProfile() {
		Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setTitle("New profile");
		dialog.setHeaderText("Not yet implemented.");
		dialog.show();
	}
}
