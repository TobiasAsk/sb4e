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
import javafx.embed.swt.FXCanvas;
import no.tobask.sb4e.editors.FXMLEditor;

public class InspectorView implements IPartListener {
	
	private EditorController dummyEditorController = new EditorController();
	private InspectorViewController inspectorViewController;
	private IWorkbenchAccessor workbenchAccessor;
	private Composite parent;
	private IPartService partService;
	private FXCanvas canvas;
	
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
		canvas = new FXCanvas(parent, SWT.NONE);
		canvas.setScene(inspectorViewController.getScene());
		partService.addPartListener(this);
		
		if (workbenchAccessor.hasActiveFxmlEditor()) {
			FXMLEditor editor = workbenchAccessor.getActiveFxmlEditor();
			inspectorViewController.setEditorController(editor.getEditorController());
		}
	}
	
	@Focus
	public void setFocus() {
	}
	
	public void dispose() {
		inspectorViewController.setEditorController(dummyEditorController);
		if (canvas != null) {
			canvas.dispose();
		}
		partService.removePartListener(this);
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		EditorController currentController = inspectorViewController.getEditorController();
		if (part instanceof FXMLEditor) {
			EditorController partController = ((FXMLEditor) part).getEditorController();
			if (currentController != partController) {
				inspectorViewController.setEditorController(partController);
			}
		} else {
			if (!workbenchAccessor.anyFxmlEditorsVisible() &&
					currentController != dummyEditorController) {
				inspectorViewController.setEditorController(dummyEditorController);
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (!workbenchAccessor.hasActiveFxmlEditor()) {
			inspectorViewController.setEditorController(dummyEditorController);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {

	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
	}
}
