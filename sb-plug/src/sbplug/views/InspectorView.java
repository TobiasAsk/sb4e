package sbplug.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import sbplug.editors.FXMLEditor;

public class InspectorView extends ViewPart implements IPartListener {
	
	private InspectorPanelController inspectorPanelController;
	private EditorController defaultEditorController = new EditorController();
	
	public InspectorPanelController getInspectorPanelController() {
		return inspectorPanelController;
	}
	
	public void createPartControl(Composite parent) {
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
		inspectorPanelController = new InspectorPanelController(defaultEditorController);
		canvas.setScene(new Scene(inspectorPanelController.getPanelRoot()));
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
	}
	
	public void setFocus() {
	}

	@Override
	public void partActivated(IWorkbenchPart part) {		
		if (part instanceof FXMLEditor) {
			inspectorPanelController.setEditorController(((FXMLEditor) part)
					.getEditorController());
		} else {
			if (inspectorPanelController.getEditorController() != defaultEditorController) {
				// the setter is a bit costly, so only call it if the update is needed
				// (a fxml editor was active before this new part)
				inspectorPanelController.setEditorController(defaultEditorController);
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (activeEditor == null || !(activeEditor instanceof FXMLEditor)) {
			inspectorPanelController.setEditorController(defaultEditorController);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {

	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
	}
}
