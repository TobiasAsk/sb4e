package no.tobask.sb4e.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;

import javafx.embed.swt.FXCanvas;
import no.tobask.sb4e.editors.FXMLEditor;

public class InspectorView implements IPartListener {
	
	private EditorController dummyEditorController = new EditorController();
	private InspectorViewController inspectorViewController;
	private IWorkbenchAccessor workbenchAccessor;
	private Composite parent;
	private IPartService partService;
	
	@Inject
	public InspectorView(InspectorViewController inspectorViewController,
			IWorkbenchAccessor workbenchAccessor, Composite parent, IPartService partService) {
		this.inspectorViewController = inspectorViewController;
		this.workbenchAccessor = workbenchAccessor;
		this.parent = parent;
		this.partService = partService;
	}
	
	@PostConstruct
	public void createGui() {
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
		canvas.setScene(inspectorViewController.getScene());
		partService.addPartListener(this);
	}
	
	@Focus
	public void setFocus() {
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		InspectorPanelController inspectorPanelController = inspectorViewController
				.getInspectorPanelController();
		EditorController currentController = inspectorPanelController.getEditorController();
		if (part instanceof FXMLEditor) {
			EditorController partController = ((FXMLEditor) part).getEditorController();
			if (currentController != partController) {
				inspectorPanelController.setEditorController(partController);
			}
		} else {
			if (!workbenchAccessor.anyFxmlEditorsVisible() &&
					currentController != dummyEditorController) {
				inspectorPanelController.setEditorController(dummyEditorController);
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (!workbenchAccessor.hasActiveFxmlEditor()) {
			InspectorPanelController inspectorPanelController = inspectorViewController
					.getInspectorPanelController();
			inspectorPanelController.setEditorController(dummyEditorController);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {

	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
	}
}
