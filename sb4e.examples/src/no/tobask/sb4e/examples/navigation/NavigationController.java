package no.tobask.sb4e.examples.navigation;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

public class NavigationController {
	
	@FXML
	ListView<String> pages;
	
	@FXML
	StackPane pageHost;
	
	@FXML
	private void initialize() {
		pages.getItems().addAll("Home", "Settings");
		pages.getSelectionModel().selectedItemProperty().addListener((oV, oldPage, newPage) ->
				onPageSelected(newPage));
	}
	
	private void onPageSelected(String page) {
		if (page == null) {
			return;
		} else if (page.equals("Settings")) {
			pageHost.getChildren().clear();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsPage.fxml"));
			try {
				Parent settingsPageRoot = loader.load();
				pageHost.getChildren().add(settingsPageRoot);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (page.equals("Home")) {
			pageHost.getChildren().clear();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
			try {
				Parent homePageRoot = loader.load();
				pageHost.getChildren().add(homePageRoot);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
