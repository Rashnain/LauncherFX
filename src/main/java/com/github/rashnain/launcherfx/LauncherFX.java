package main.java.com.github.rashnain.launcherfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LauncherFX extends Application {

	private Stage primaryStage;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
    	// TODO stores data in some way and load them here

    	this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(LauncherFX.class.getResource("view/loginScreen.fxml"));
        // TODO need de use the user selected language
        Locale locale = new Locale(Locale.getDefault().toLanguageTag());
        loader.setResources(ResourceBundle.getBundle("main.java.com.github.rashnain.launcherfx.locales.lang", locale));
        Scene scene = new Scene(loader.load(), 880, 550);
        this.primaryStage.setTitle("LauncherFX");
        this.primaryStage.setResizable(false);
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }
}
