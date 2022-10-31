package main.java.com.github.rashnain.launcherfx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.java.com.github.rashnain.launcherfx.LauncherFX;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;

import java.io.IOException;
import java.util.ResourceBundle;

public class LoginScreenController {
	
	private ResourceBundle resources;
	
	@FXML
	private TextField guestPseudo;
	
	@FXML
	private TextField microsoftEmail;
	
	@FXML
	private TextField microsoftPassword;
	
	@FXML
	private Button microsoftButton;
	
	@FXML
	private Button guestButton;
	
	public void initializeView() {
		this.resources = LauncherFX.resources;
	}
	
	@FXML
	private void microsoftLogging() {
		if (checkMicrosoft()) {
			Alert dialog = new Alert(AlertType.INFORMATION);
			dialog.setTitle("Microsoft account logging");
			dialog.setHeaderText("Not yet implemented.");
			dialog.show();
		}
	}
	
	@FXML
	private void guestLogging() throws IOException {
		if (checkGuest()) {
			LauncherProfile.getProfile().setUsername(guestPseudo.getText());
			LauncherFX.switchView();
		}
	}
	
	private boolean checkGuest() {
		boolean valid = true;
		String errorString = "";
		
		if (guestPseudo.getText().trim().length() == 0) {
			errorString += "- " + this.resources.getString("guest.login.error.pseudo");
			valid = false;
		}
		
		if (!valid) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("error.invalidinputs"));
			dialog.setHeaderText(this.resources.getString("error.fixtocontinue"));
			dialog.setContentText(errorString);
			dialog.show();
		}
		
		return valid;
	}
	
	private boolean checkMicrosoft() {
		boolean valid = true;
		String errorString = "";
		
		if (microsoftEmail.getText().trim().length() == 0) {
			errorString += "- " + this.resources.getString("microsoft.login.error.email") + "\r\n";
			valid = false;
		}
		
		if (microsoftPassword.getText().trim().length() == 0) {
			errorString += "- " + this.resources.getString("microsoft.login.error.password");
			valid = false;
		}
		
		if (!valid) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("error.invalidinputs"));
			dialog.setHeaderText(this.resources.getString("error.fixtocontinue"));
			dialog.setContentText(errorString);
			dialog.show();
		}
		
		return valid;
	}
	
	@FXML
	private void onKeyPressedMicrosoft(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			microsoftButton.fire();
		}
	}
	
	@FXML
	private void onKeyPressedGuest(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			guestButton.fire();
		}
	}
}
