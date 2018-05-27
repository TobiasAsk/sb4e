package no.tobask.sb4e.examples;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SwitchCaseController {
	
	@FXML
	private Label label;
	private boolean upper;
	
	@FXML
	public void onBtnClick() {
		String currentText = label.getText();
		label.setText(upper ? currentText.toUpperCase() : currentText.toLowerCase());
		upper = !upper;
	}

}
