package no.tobask.sb4e.examples.include;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.VBox;

public class ImagesTabController {
	
	@FXML
	Pagination imagePagination;
	List<VBox> images = new ArrayList<>();
	
	@FXML
	public void initialize() {
		for (int i = 0; i < 5; i++) {
			Label l = new Label();
			l.setText("label #" + i);
			images.add(new VBox(l));
		}
		
		imagePagination.setPageFactory(idx -> images.get(idx));
	}
	
	public String getImage() {
		VBox image = images.get(imagePagination.getCurrentPageIndex());
		Label l = (Label) image.getChildren().get(0);
		return l.getText();
	}

	public void delete() {
		System.out.println("got here");
	}

}
