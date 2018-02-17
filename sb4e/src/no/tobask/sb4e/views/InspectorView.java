package no.tobask.sb4e.views;

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
import no.tobask.sb4e.InspectorViewController;
import no.tobask.sb4e.editors.FXMLEditor;

public class InspectorView extends ViewPart implements IPartListener {
	
	private InspectorPanelController inspectorPanelController;
	private EditorController defaultEditorController = new EditorController();
	
	public InspectorPanelController getInspectorPanelController() {
		return inspectorPanelController;
	}
	
	public void createPartControl(Composite parent) {
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
		inspectorPanelController = new InspectorPanelController(defaultEditorController);
		InspectorViewController inspectorViewController =
				new InspectorViewController(defaultEditorController, inspectorPanelController);
		canvas.setScene(inspectorViewController.getScene());
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
	}
	
	public void setFocus() {
	}
	
	private boolean anyFxmlEditorsVisible() {
//		IWorkbenchPage activePage = getSite().getWorkbenchWindow().getActivePage();
//		for (IEditorReference editorReference : activePage.getEditorReferences()) {
//			IWorkbenchPart part = editorReference.getPart(false);
//			if (part instanceof FXMLEditor && activePage.isPartVisible(part)) {
//				return true;
//			}
//		}
//		return false;
		return getSite().getWorkbenchWindow().getActivePage().getActiveEditor()
				instanceof FXMLEditor;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		EditorController currentController = inspectorPanelController.getEditorController();
		if (part instanceof FXMLEditor) {
			EditorController partController = ((FXMLEditor) part).getEditorController();
			if (currentController != partController) {
				inspectorPanelController.setEditorController(partController);
			}
		} else {
			if (!anyFxmlEditorsVisible() && currentController != defaultEditorController) {
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
		if (!(activeEditor instanceof FXMLEditor)) {
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
