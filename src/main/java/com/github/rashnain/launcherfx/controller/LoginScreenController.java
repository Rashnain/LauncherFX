package main.java.com.github.rashnain.launcherfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.java.com.github.rashnain.launcherfx.Main;
import main.java.com.github.rashnain.launcherfx.model.LauncherProfile;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Controller of the login screen
 */
public class LoginScreenController {

	private boolean initialized;

	private ResourceBundle resources;

	private LauncherProfile launcher;

	@FXML
	private TextField microsoftEmail;

	@FXML
	private TextField microsoftPassword;

	@FXML
	private Button microsoftButton;

	@FXML
	private TextField guestPseudo;

	@FXML
	private Button guestButton;

	@FXML
	private ChoiceBox<String> languages;

	/**
	 * Initialize view
	 */
	public void initializeView() {
		if (!initialized) {
			this.resources = Main.getResources();
			this.launcher = LauncherProfile.getProfile();

			for (String lang : Main.availableLocales) {
				languages.getItems().add(lang + " - " + resources.getString("language."+lang));
			}

			languages.getSelectionModel().select(Main.getIndexOfLocale(launcher.getLocale()));
			languages.setOnAction( e -> changeLanguage() );

			initialized = true;
		}
		if (!launcher.getGuestUsername().equals("")) {
			guestPseudo.setText(launcher.getGuestUsername());
			guestButton.requestFocus();
		}
	}

	/**
	 * When the focus is on a Microsoft credential TextField, the enter key will fire the connection button
	 * @param event The key typed
	 */
	@FXML
	private void onKeyPressedMicrosoft(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			microsoftButton.fire();
		}
	}

	/**
	 * Connection with a Microsoft account<br>
	 * Not yet implemented
	 */
	@FXML
	private void microsoftLogging() {
		if (areMicrosoftCredentialsValid()) {
			Alert dialog = new Alert(AlertType.INFORMATION);
			dialog.setTitle("Microsoft account logging");
			dialog.setHeaderText("Not yet implemented.");
			dialog.show();
		}
	}

	/**
	 * Returns wether the Microsoft credentials are valid<br>
	 * Email:<br>
	 * - Needs to be a correct email address, e.g. zyx@exemple.com<br>
	 * - Only some specials characters are authorized but I have no idea how to know them<br>
	 * Password:<br>
	 * - 8 characters minimum<br>
	 * - Only some specials characters are authorized but I have no idea how to know them
	 * @return true if valid, false otherwise
	 */
	private boolean areMicrosoftCredentialsValid() {
		boolean valid = true;
		String errorString = "";

		if (!microsoftEmail.getText().matches("^.+@.+[.].+$")) {
			errorString += "- " + this.resources.getString("microsoft.login.error.email") + "\r\n";
			valid = false;
		}

		if (!microsoftPassword.getText().matches("^.{8,}$")) {
			errorString += "- " + this.resources.getString("microsoft.login.error.password");
			valid = false;
		}

		if (!valid) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("error.invalidinputs"));
			dialog.setHeaderText(this.resources.getString("error.howtofix"));
			dialog.setContentText(errorString);
			dialog.show();
		}

		return valid;
	}

	/**
	 * When the focus is on the pseudo TextField, the enter key will fire the connection button
	 * @param event The key typed
	 */
	@FXML
	private void onKeyPressedGuest(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			guestButton.fire();
		}
	}

	/**
	 * Connection without account
	 */
	@FXML
	private void guestLogging() throws IOException {
		if (isGuestValid()) {
			launcher.setGuestUsername(guestPseudo.getText());
			Main.switchView();
		}
	}

	/**
	 * Returns wether the guest's username is valid<br>
	 * Needs to be between 3 and 16 caracters (inclusive)<br>
	 * Only letters (lowercase or uppercase), digits or underscores
	 * @return true if valid, false otherwise
	 */
	private boolean isGuestValid() {
		boolean valid = true;

		if (!guestPseudo.getText().matches("^[a-zA-Z0-9_]{3,16}$")) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.setTitle(this.resources.getString("error.invalidinputs"));
			dialog.setHeaderText(this.resources.getString("error.howtofix"));
			dialog.setContentText(this.resources.getString("guest.login.error"));
			dialog.show();
			valid = false;
		}

		return valid;
	}

	/**
	 * Change the launcher's language setting, and show a dialog to inform user about when the language will change
	 */
	private void changeLanguage() {
		int index = languages.getSelectionModel().getSelectedIndex();
		launcher.setLocale(Main.availableLocales[index]);

		Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setTitle(resources.getString("language.change.title"));
		dialog.setHeaderText(resources.getString("language.change.desc"));
		dialog.showAndWait();
	}

}
