package no.tobask.sb4e.examples;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SwitchCaseController {
	
	@FXML
	private Label label;
	private boolean upper;
	private Button bbaaaajha;
	@FXML private Button abababa;
	
	@FXML
	public void onBtnClick() {
		String currentText = label.getText();
		label.setText(upper ? currentText.toUpperCase() : currentText.toLowerCase());
		upper = !upper;
	}
	
	@FXML
	private void stusffs(String event) {
		
	}

}
