package main.java.com.github.rashnain.launcherfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LauncherFX extends Application {
	
	private Stage primaryStage;
	
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
		
		// Load settings and profiles
		launcher.loadProfiles();
		
		// Launch
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		FXMLLoader loader = new FXMLLoader(LauncherFX.class.getResource("view/loginScreen.fxml"));
		Locale locale = new Locale(LauncherProfile.getProfile().getLocale());
		loader.setResources(ResourceBundle.getBundle("main.java.com.github.rashnain.launcherfx.locales.lang", locale));
		Scene scene = new Scene(loader.load(), 880, 550);
		this.primaryStage.setTitle("LauncherFX");
		this.primaryStage.setResizable(false);
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}
}
