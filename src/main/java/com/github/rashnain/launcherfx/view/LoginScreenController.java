package main.java.com.github.rashnain.launcherfx.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import main.java.com.github.rashnain.launcherfx.Util;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginScreenController implements Initializable {

    @FXML
    private TextField pseudoField;
    
    @FXML
    private Button guestButton;

    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	pseudoField.requestFocus();
        pseudoField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
            	guestButton.fire();
            }
        });
    }

    @FXML
    protected void onLoginMicrosoftButtonClick() {
        Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.setTitle("Microsoft account login");
        dialog.setHeaderText("Not yet implemented");
        dialog.show();
    }

    @FXML
    protected void onLoginGuestButtonClick(ActionEvent event) throws IOException {
    	Util.changeRoot("selectProfile", event);
    }
}
