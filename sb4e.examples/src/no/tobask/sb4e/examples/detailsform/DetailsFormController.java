package no.tobask.sb4e.examples.detailsform;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class DetailsFormController {

	@FXML
	Button okButton;
	@FXML
	Button cancelButton;
	@FXML
	PasswordField passwordField;

	@FXML
	public void initialize() {
		passwordField.focusedProperty().addListener((oV, oldValue, newValue) -> {
			if (!newValue) validatePassword();
		});
	}

	private void validatePassword() {
		String username = passwordField.getText();
		if (username == null || username.isEmpty()) {
			passwordField.setStyle("-fx-background-color: red");
			passwordField.setPromptText("Invalid password");
		} else {
			passwordField.setStyle("");
		}
	}
	
	@FXML
	private void onOkButtonClicked(ActionEvent event) {
		System.out.println("yeah");
	}

	@FXML
	private void onCancelButtonClicked(ActionEvent event) {
		Platform.exit();
	}

}
