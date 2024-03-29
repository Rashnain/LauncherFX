package com.github.rashnain.launcherfx.controller;

import com.github.rashnain.launcherfx.Main;
import com.github.rashnain.launcherfx.model.LauncherProfile;
import com.github.rashnain.launcherfx.model.MicrosoftAccount;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller of the login screen
 */
public class LoginScreenController {

	private boolean initialized;

	private ResourceBundle resources;

	private LauncherProfile launcher;

	private int nextAccountButtonLayoutY;

	@FXML
	private TextField microsoftEmail;

	@FXML
	private PasswordField microsoftPassword;

	@FXML
	private Button microsoftLoginButton;

	@FXML
	private CheckBox microsoftRememberMe;

	@FXML
	private Button microsoftReconnectButton;

	@FXML
	private Button microsoftNewAccountButton;

	@FXML
	private Button microsoftAccountsButton;

	@FXML
	private AnchorPane microsoftAccountListAP;

	@FXML
	private ScrollPane microsoftAccountListSP;

	@FXML
	private Button microsoftRestoreButton;

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
			resources = Main.getResources();
			launcher = LauncherProfile.getProfile();

			for (String lang : Main.availableLocales) {
				languages.getItems().add(lang + " - " + resources.getString("language."+lang));
			}

			languages.getSelectionModel().select(Main.getIndexOfLocale(launcher.getLocale()));
			languages.setOnAction( e -> changeLanguage() );

			microsoftRememberMe.setSelected(launcher.isRememberMe());

			initialized = true;
		}
		guestPseudo.setText(launcher.getGuestUsername());
		if (!launcher.getCurrentAccount().getUsername().isEmpty() && !launcher.getCurrentAccount().getRefreshToken().isEmpty()) {
			microsoftEmail.setVisible(false);
			microsoftPassword.setVisible(false);
			microsoftLoginButton.setVisible(false);
			microsoftReconnectButton.setText(launcher.getCurrentAccount().getUsername());
			microsoftReconnectButton.setVisible(true);
			microsoftNewAccountButton.setVisible(true);
			microsoftAccountsButton.setVisible(true);
			microsoftRestoreButton.setVisible(false);
			microsoftRememberMe.setVisible(false);
			microsoftReconnectButton.requestFocus();
		} else {
			guestPseudo.requestFocus();
		}
	}

	/**
	 * When the focus is on a Microsoft credential TextField, the enter key will fire the connection button
	 * @param event The key typed
	 */
	@FXML
	private void onKeyPressedMicrosoft(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			microsoftLoginButton.fire();
		}
	}

	@FXML
	private void microsoftLogging() {
		if (areMicrosoftCredentialsValid()) {
			MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
			try {
				MicrosoftAuthResult result = authenticator.loginWithCredentials(microsoftEmail.getText(), microsoftPassword.getText());
				MicrosoftAccount account = new MicrosoftAccount(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), result.getRefreshToken(), result.getClientId(), result.getXuid(), Instant.now());
				launcher.setCurrentAccount(account);
				if (launcher.isRememberMe()) {
					int index = launcher.indexOfAccount(account.getUuid());
					if (index == -1)
						launcher.getAccounts().add(account);
					else
						launcher.getAccounts().set(index, account);
				}
				microsoftEmail.setText("");
				microsoftPassword.setText("");
				launcher.setGuestStatus(false);
				Main.switchView();
			} catch (Exception e) {
				Alert dialog = new Alert(AlertType.ERROR);
				dialog.initOwner(Main.getPrimaryStage());
				dialog.setTitle(resources.getString("microsoft.login.error.dialog.title"));
				dialog.setContentText(resources.getString("microsoft.login.error.dialog.connect"));
				dialog.setHeaderText("");
				dialog.show();
			}
		}
	}

	@FXML
	private void microsoftReconnect() {
		MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
		MicrosoftAccount account = launcher.getCurrentAccount();
        try {
			launcher.setGuestStatus(false);
			MicrosoftAuthResult result = authenticator.loginWithRefreshToken(account.getRefreshToken());
			account.setUsername(result.getProfile().getName());
			account.setUuid(result.getProfile().getId());
			account.setAccessToken(result.getAccessToken());
			account.setRefreshToken(result.getRefreshToken());
			account.setClientId(result.getClientId());
			account.setXuid(result.getXuid());
			account.setLastUsed(Instant.now());
			Main.switchView();
		} catch (Exception e) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.initOwner(Main.getPrimaryStage());
			dialog.setTitle(resources.getString("microsoft.login.error.dialog.title"));
			dialog.setHeaderText("");
			dialog.setContentText(resources.getString("microsoft.login.error.dialog.reconnect"));
			dialog.show();
		}
    }

	@FXML
	private void microsoftRestore() {
		microsoftEmail.setVisible(false);
		microsoftPassword.setVisible(false);
		microsoftLoginButton.setVisible(false);
		microsoftReconnectButton.setText(launcher.lastUsedAccount().getUsername());
		microsoftReconnectButton.setVisible(true);
		microsoftNewAccountButton.setVisible(true);
		microsoftAccountsButton.setVisible(true);
		microsoftRestoreButton.setVisible(false);
		microsoftAccountListSP.setVisible(false);
		microsoftAccountListAP.getChildren().clear();
		microsoftRememberMe.setVisible(false);
		microsoftReconnectButton.requestFocus();
	}

	@FXML
	private void microsoftAnotherAccount() {
		microsoftReconnectButton.setVisible(false);
		microsoftNewAccountButton.setVisible(false);
		microsoftAccountsButton.setVisible(false);
		microsoftAccountListSP.setVisible(true);
		microsoftRestoreButton.setVisible(true);

		launcher.getAccounts().sort((one, two) -> {
            if (one.getLastUsed().isAfter(two.getLastUsed()))
                return -1;
            else if (one.getLastUsed().isBefore(two.getLastUsed()))
                return 1;
            return 0;
        });

		nextAccountButtonLayoutY = 0;
		for (MicrosoftAccount ma : launcher.getAccounts()) {
			createAccountButtons(ma);
		}
		microsoftAccountListAP.setPrefHeight(nextAccountButtonLayoutY - 2);
		microsoftAccountListSP.setHmax(nextAccountButtonLayoutY - 2);
	}

	@FXML
	private void microsoftNewAccount() {
		microsoftReconnectButton.setVisible(false);
		microsoftNewAccountButton.setVisible(false);
		microsoftAccountsButton.setVisible(false);
		microsoftEmail.setVisible(true);
		microsoftPassword.setVisible(true);
		microsoftLoginButton.setVisible(true);
		microsoftRestoreButton.setVisible(true);
		microsoftRememberMe.setVisible(true);
		microsoftEmail.requestFocus();
	}

	private void createAccountButtons(MicrosoftAccount account) {
		Button accountButton = new Button();
		accountButton.setText(account.getUsername());
		accountButton.setLayoutX(1);
		accountButton.setLayoutY(nextAccountButtonLayoutY);
		accountButton.setPrefWidth(178);
		accountButton.getStyleClass().add("buttonGreenMedium");
		accountButton.setOnAction(e -> {
			microsoftAccountListSP.setVisible(false);
			microsoftAccountListAP.getChildren().clear();
			launcher.setCurrentAccount(account);
			launcher.getCurrentAccount().setLastUsed(Instant.now());
			microsoftReconnect();
		});

		Button deleteButton = new Button();
		deleteButton.setText("✖");
		deleteButton.setLayoutX(181);
		deleteButton.setLayoutY(nextAccountButtonLayoutY);
		deleteButton.setPrefWidth(38);
		deleteButton.getStyleClass().add("buttonGreenMedium");
		deleteButton.setOnAction(e -> deleteAccount(deleteButton, account));

		microsoftAccountListAP.getChildren().addAll(accountButton, deleteButton);
		nextAccountButtonLayoutY += 37;
	}

	private void deleteAccount(Button source, MicrosoftAccount account) {
		Alert dialog = new Alert(AlertType.CONFIRMATION);
		dialog.initOwner(Main.getPrimaryStage());
		dialog.setTitle(resources.getString("microsoft.accounts.delete"));
		dialog.setHeaderText("");
		dialog.setContentText(resources.getString("microsoft.accounts.delete.desc"));
		dialog.getButtonTypes().set(0, ButtonType.YES);
		dialog.getButtonTypes().set(1, ButtonType.NO);

		Optional<ButtonType> response = dialog.showAndWait();
		if (response.isPresent() && response.get() == ButtonType.YES) {
			ObservableList<Node> children = microsoftAccountListAP.getChildren();
			int indexBT = children.indexOf(source);
			microsoftAccountListAP.getChildren().remove(indexBT - 1, indexBT + 1);

			for (int i = 0, layoutY = 0; i < children.size(); i += 2, layoutY += 37) {
				Node accountNode = children.get(i);
				Node deleteNode = children.get(i + 1);
				if (accountNode.getLayoutY() != layoutY) {
					accountNode.setLayoutY(layoutY);
					deleteNode.setLayoutY(layoutY);
				}
			}
			nextAccountButtonLayoutY -= 37;
			microsoftAccountListAP.setPrefHeight(nextAccountButtonLayoutY - 2);
			microsoftAccountListSP.setHmax(nextAccountButtonLayoutY - 2);
			launcher.getAccounts().remove(account);
			MicrosoftAccount newCurrentAccount = launcher.lastUsedAccount();
			if (newCurrentAccount == null) {
				newCurrentAccount = new MicrosoftAccount();
				microsoftAccountListSP.setVisible(false);
				microsoftReconnectButton.setVisible(false);
				microsoftNewAccountButton.setVisible(false);
				microsoftAccountsButton.setVisible(false);
				microsoftRestoreButton.setVisible(false);
				microsoftEmail.setVisible(true);
				microsoftPassword.setVisible(true);
				microsoftLoginButton.setVisible(true);
				microsoftRememberMe.setVisible(true);
			}
			launcher.setCurrentAccount(newCurrentAccount);
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
			errorString += "- " + resources.getString("microsoft.login.error.email") + "\r\n";
			valid = false;
		}

		if (!microsoftPassword.getText().matches("^.{8,}$")) {
			errorString += "- " + resources.getString("microsoft.login.error.password");
			valid = false;
		}

		if (!valid) {
			Alert dialog = new Alert(AlertType.ERROR);
			dialog.initOwner(Main.getPrimaryStage());
			dialog.setTitle(resources.getString("error.invalidinputs"));
			dialog.setHeaderText(resources.getString("error.howtofix"));
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
	 * Connection without account<br>
	 * Switch to the profiles view
	 */
	@FXML
	private void guestLogging() throws IOException {
		if (isGuestValid()) {
			launcher.setGuestUsername(guestPseudo.getText());
			launcher.setGuestStatus(true);
			Main.switchView();
		}
	}

	@FXML
	private void rememberMeChanged() {
		launcher.setRememberMe(microsoftRememberMe.isSelected());
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
			dialog.initOwner(Main.getPrimaryStage());
			dialog.setTitle(resources.getString("error.invalidinputs"));
			dialog.setHeaderText(resources.getString("error.howtofix"));
			dialog.setContentText(resources.getString("guest.login.error"));
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
		dialog.initOwner(Main.getPrimaryStage());
		dialog.setTitle(resources.getString("language.change.title"));
		dialog.setHeaderText(resources.getString("language.change.desc"));
		dialog.showAndWait();
	}

}
