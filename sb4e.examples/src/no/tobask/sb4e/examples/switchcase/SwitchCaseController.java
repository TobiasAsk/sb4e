package no.tobask.sb4e.examples.switchcase;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SwitchCaseController {
	
	@FXML
	Button button;
	
	@FXML
	Label label;
	
	boolean upperCase;
	
	@FXML
	private void onButtonClick(ActionEvent event) {
		String labelText = label.getText();
		String updatedLabelText = upperCase ? labelText.toUpperCase() : labelText.toLowerCase();
		label.setText(updatedLabelText);
		upperCase = !upperCase;
	}

}
