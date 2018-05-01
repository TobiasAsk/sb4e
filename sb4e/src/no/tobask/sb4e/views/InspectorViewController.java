package no.tobask.sb4e.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.i18n.I18N;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.util.AbstractFxmlWindowController;
import com.oracle.javafx.scenebuilder.kit.editor.search.SearchController;

import javafx.embed.swt.FXCanvas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;

public class InspectorViewController extends AbstractFxmlWindowController {

	private InspectorPanelController inspectorPanelController;
	private SearchController inspectorSearchController;

	@FXML
	private StackPane inspectorSearchPanelHost;
	@FXML
	private StackPane inspectorPanelHost;

	public InspectorViewController() {
		super(InspectorViewController.class.getResource("InspectorView.fxml"), I18N.getBundle(), false);
		EditorController dummyEdtCtrl = new EditorController();
		new FXCanvas(new Shell(Display.getDefault()), SWT.NONE); // hack to initialize javafx toolkit
		inspectorPanelController = new InspectorPanelController(dummyEdtCtrl);
		inspectorSearchController = new SearchController(dummyEdtCtrl);
	}
	
	public InspectorPanelController getInspectorPanelController() {
		return inspectorPanelController;
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
