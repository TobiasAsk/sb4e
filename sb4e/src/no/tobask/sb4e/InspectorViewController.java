package no.tobask.sb4e;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.i18n.I18N;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.util.AbstractFxmlWindowController;
import com.oracle.javafx.scenebuilder.kit.editor.search.SearchController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;

public class InspectorViewController extends AbstractFxmlWindowController {

	InspectorPanelController inspectorPanelController;
	SearchController inspectorSearchController;

	@FXML
	StackPane inspectorSearchPanelHost;
	@FXML
	StackPane inspectorPanelHost;

	public InspectorViewController(EditorController editorController,
			InspectorPanelController inspectorPanelController) {
		super(InspectorViewController.class.getResource("InspectorView.fxml"), I18N.getBundle(), false);
		this.inspectorPanelController = inspectorPanelController;
		inspectorSearchController = new SearchController(editorController);
	}

	@Override
	protected void controllerDidLoadFxml() {
		inspectorSearchPanelHost.getChildren().add(inspectorSearchController.getPanelRoot());
		inspectorPanelHost.getChildren().add(inspectorPanelController.getPanelRoot());
		
		inspectorSearchController.textProperty().addListener((ov, oldStr, newStr) ->
			inspectorPanelController.setSearchPattern(newStr));
	}

	@Override
	public void onCloseRequest(WindowEvent event) {

	}

	@FXML
	private void onInspectorShowAllAction(ActionEvent event) {

	}

	@FXML
	private void onInspectorShowEditedAction(ActionEvent event) {

	}

	@FXML
	private void onInspectorViewSectionsAction(ActionEvent event) {

	}

	@FXML
	private void onInspectorViewByPropertyNameAction(ActionEvent event) {

	}
	
	@FXML
	private void onInspectorViewByPropertyTypeAction(ActionEvent event) {

	}

}
