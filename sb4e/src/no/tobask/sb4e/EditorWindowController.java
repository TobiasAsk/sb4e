package no.tobask.sb4e;

import com.oracle.javafx.scenebuilder.app.info.InfoPanelController;
import com.oracle.javafx.scenebuilder.app.selectionbar.SelectionBarController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.i18n.I18N;
import com.oracle.javafx.scenebuilder.kit.editor.panel.content.ContentPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.hierarchy.HierarchyPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.library.LibraryPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.util.AbstractFxmlWindowController;
import com.oracle.javafx.scenebuilder.kit.editor.search.SearchController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;

public class EditorWindowController extends AbstractFxmlWindowController {
	
	EditorController editorController;
	ContentPanelController contentPanelController;
	LibraryPanelController libraryPanelController;
	SelectionBarController selectionBarController;
	HierarchyPanelController hierarchyPanelController;
	InfoPanelController infoPanelController;
	SearchController librarySearchController;
	
	@FXML
	StackPane libraryPanelHost;
	@FXML
	StackPane contentPanelHost;
	@FXML
	StackPane selectionBarHost;
	@FXML
	StackPane hierarchyPanelHost;
	@FXML
	StackPane infoPanelHost;
	@FXML
	StackPane librarySearchPanelHost;

	public EditorWindowController(EditorController editorController) {
		super(EditorWindowController.class.getResource("EditorWindow.fxml"), I18N.getBundle(), false);
		this.editorController = editorController;
		contentPanelController = new ContentPanelController(editorController);
		libraryPanelController = new LibraryPanelController(editorController);
		selectionBarController = new SelectionBarController(editorController);
		hierarchyPanelController = new HierarchyPanelController(editorController);
		infoPanelController = new InfoPanelController(editorController);
		librarySearchController = new SearchController(editorController);
	}
	
	@Override
	protected void controllerDidLoadFxml() {
		libraryPanelHost.getChildren().add(libraryPanelController.getPanelRoot());
		contentPanelHost.getChildren().add(contentPanelController.getPanelRoot());
		selectionBarHost.getChildren().add(selectionBarController.getPanelRoot());
		hierarchyPanelHost.getChildren().add(hierarchyPanelController.getPanelRoot());
		infoPanelHost.getChildren().add(infoPanelController.getPanelRoot());
		librarySearchPanelHost.getChildren().add(librarySearchController.getPanelRoot());
		
		librarySearchController.textProperty().addListener((ov, oldStr, newStr) ->
			libraryPanelController.setSearchPattern(newStr));
	}

	@Override
	public void onCloseRequest(WindowEvent event) {
		
	}
	
	@FXML
	private void onHierarchyShowInfo(ActionEvent event) {
		
	}
	
	@FXML
	private void onHierarchyShowFxId(ActionEvent event) {
		
	}
	
	@FXML
	private void onHierarchyShowNodeId(ActionEvent event) {
		
	}
	
}
