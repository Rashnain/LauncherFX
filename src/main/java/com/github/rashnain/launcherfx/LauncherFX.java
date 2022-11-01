package main.java.com.github.rashnain.launcherfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;
import main.java.com.github.rashnain.launcherfx.view.LoginScreenController;
import main.java.com.github.rashnain.launcherfx.view.ProfilesScreenController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LauncherFX extends Application {
	
	public static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

	private static Stage primaryStage;
	
	private static Parent loginScreenView;

	private static LoginScreenController loginScreenController;
	
	private static Parent profilesScreenView;

	private static ProfilesScreenController profilesScreenController;
	
	private static final String[] availableLocales = {"en", "fr"};

	public static ResourceBundle resources;

	public static void main(String[] args) throws IOException {
		LauncherProfile launcher = LauncherProfile.getProfile();
		
		// Working directory
		if (args.length >= 2 && args[0].equals("--workDir")) {
			if (new File(args[1]).isAbsolute()) {
				launcher.setLauncherDir(args[1] + "/");
			} else {
				launcher.setLauncherDir(System.getProperty("user.dir") + "/" + args[1] + "/");
			}
		} else {
			launcher.setLauncherDir(System.getProperty("user.dir") + "/");
		}
		
		File workDir = new File(launcher.getLauncherDir());
		workDir.mkdirs();
		if (!workDir.isDirectory()) {
			launcher.setLauncherDir(System.getProperty("user.dir") + "/");
			System.out.println("Could not use custom work directory, will use default directory as such.");
		}
		
		System.out.println("Working directory : " + launcher.getLauncherDir());
		
		// Load launcher settings
		launcher.loadProfile();
		
		// Launch
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		LauncherFX.primaryStage = primaryStage;
		
		Locale locale = LauncherProfile.getProfile().getLocale();
		resources = ResourceBundle.getBundle("main.java.com.github.rashnain.launcherfx.resources.locales.lang", locale);
		
		FXMLLoader loginScreen = new FXMLLoader(LauncherFX.class.getResource("view/LoginScreen.fxml"));
		loginScreen.setResources(resources);
		loginScreenView = loginScreen.load();
		loginScreenController = loginScreen.getController();
		loginScreenController.initializeView();
		
		FXMLLoader profilesScreen = new FXMLLoader(LauncherFX.class.getResource("view/ProfilesScreen.fxml"));
		profilesScreen.setResources(resources);
		profilesScreenView = profilesScreen.load();
		profilesScreenController = profilesScreen.getController();
		
		primaryStage.setTitle("LauncherFX");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(loginScreenView, 880, 550));
		primaryStage.show();
	}
	
	public static void switchView() {
		Scene scene = primaryStage.getScene();
		if (scene.getRoot() == loginScreenView) {
			profilesScreenController.initializeView();
			scene.setRoot(profilesScreenView);
		} else {
			scene.setRoot(loginScreenView);
		}
	}
	
	public static boolean isAvailableLocale(String locale) {
		for (String availableLocale : availableLocales) {
			if (locale.equals(availableLocale)) {
				return true;
			}
		}
		return false;
	}
}
