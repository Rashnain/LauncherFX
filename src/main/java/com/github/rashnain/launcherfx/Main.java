package com.github.rashnain.launcherfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import com.github.rashnain.launcherfx.controller.LoginScreenController;
import com.github.rashnain.launcherfx.controller.ProfilesScreenController;
import com.github.rashnain.launcherfx.model.LauncherProfile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class initializing the launcher
 */
public class Main extends Application {

	public static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

	public static final String[] availableLocales = {"en", "fr"};

	private static Stage primaryStage;

	private static ResourceBundle resources;

	private static Parent loginScreenView;

	private static LoginScreenController loginScreenController;

	private static Parent profilesScreenView;

	private static ProfilesScreenController profilesScreenController;

	private static LauncherProfile launcher;

	/**
	 * Load or create settings and launch the app
	 * @param args Program arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		launcher = LauncherProfile.getProfile();

		// Working directory
		if (args.length >= 2 && args[0].equals("--workDir")) {
			if (new File(args[1]).isAbsolute()) {
				launcher.setWorkDir(args[1]);
			} else {
				launcher.setWorkDir(System.getProperty("user.dir") + "/" + args[1]);
			}
		} else {
			launcher.setWorkDir(System.getProperty("user.dir") + "/data");
		}
		File workDir = new File(launcher.getWorkDir());
		workDir.mkdirs();
		if (!workDir.isDirectory()) {
			launcher.setWorkDir(System.getProperty("user.dir"));
			System.out.println("Could not use custom work directory, will use default directory instead.");
		}

		System.out.println("Working directory : " + launcher.getWorkDir());

		// Load launcher's settings
		launcher.loadProfile();

		// Launch
		launch();
	}

	/**
	 * Initialize views, controllers and the stage
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		Main.primaryStage = primaryStage;

		Locale locale = new Locale(launcher.getLocale());
		resources = ResourceBundle.getBundle("com.github.rashnain.launcherfx.locale.lang", locale);

		FXMLLoader loginScreen = new FXMLLoader(Main.class.getResource("view/LoginScreen.fxml"));
		loginScreen.setResources(resources);
		loginScreenView = loginScreen.load();
		loginScreenController = loginScreen.getController();

		FXMLLoader profilesScreen = new FXMLLoader(Main.class.getResource("view/ProfilesScreen.fxml"));
		profilesScreen.setResources(resources);
		profilesScreenView = profilesScreen.load();
		profilesScreenController = profilesScreen.getController();

		primaryStage.setOnCloseRequest( e -> { launcher.saveProfile(); Runtime.getRuntime().exit(1); } );
		primaryStage.getIcons().add(new Image(Main.class.getResource("img/icon.png").toExternalForm()));
		primaryStage.setTitle("Test");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(new Pane(), 1067, 600));
		primaryStage.getScene().getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
		primaryStage.show();

		switchView();
	}

	/**
	 * Switch the scene from a view to the other
	 */
	public static void switchView() {
		Scene scene = primaryStage.getScene();
		if (scene.getRoot() == loginScreenView) {
			scene.setRoot(profilesScreenView);
			profilesScreenController.initializeView();
		} else {
			scene.setRoot(loginScreenView);
			loginScreenController.initializeView();
		}
	}

	/**
	 * Returns the index of the given locale if the launcher is localized for it
	 * @param locale Locale in language format
	 * @return the index or -1
	 */
	public static int getIndexOfLocale(String locale) {
		for (int i = 0; i < availableLocales.length; i ++) {
			if (locale.equals(availableLocales[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the launcher's primary stage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * Returns the launcher's resource bundle
	 */
	public static ResourceBundle getResources() {
		return resources;
	}

}
