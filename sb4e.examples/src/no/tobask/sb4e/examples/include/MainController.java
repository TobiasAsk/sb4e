package no.tobask.sb4e.examples.include;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {
	
	@FXML
	ImagesTabController imageTabController;
	@FXML
	TabPane mainWindow;
	
	@FXML
	public void onDelete(ActionEvent event) {
		Tab currentTab = mainWindow.getSelectionModel().getSelectedItem();
		if (currentTab.getText().equals("Images")) {
			imageTabController.delete();
		}
	}

}
