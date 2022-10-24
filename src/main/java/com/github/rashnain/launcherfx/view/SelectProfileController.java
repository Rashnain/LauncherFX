package main.java.com.github.rashnain.launcherfx.view;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import main.java.com.github.rashnain.launcherfx.LauncherFX;
import main.java.com.github.rashnain.launcherfx.Util;

public class SelectProfileController implements Initializable {

	@FXML
    private Button loginScreenButton;

    @Override
	public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    protected void onGoToLoginScreenClick(ActionEvent event) throws IOException {
    	Util.changeRoot("loginScreen", event);
    }
}
