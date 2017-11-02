package sbplug.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import sbplug.editors.FXMLEditor;

public class InspectorView {
	
	private InspectorPanelController inspectorPanelController;
	private EditorController defaultEditorController = new EditorController();
	private boolean closingLastEditor;

	@Inject
	private IPartService partService;
	
	@Inject
	ECommandService commandService;
	
	@PostConstruct
	public void createPartControl(Composite parent) {
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
		inspectorPanelController = new InspectorPanelController(defaultEditorController);
		canvas.setScene(new Scene(inspectorPanelController.getPanelRoot()));
		
		
		
		partService.addPartListener(new IPartListener() {
			
			@Override
			public void partOpened(IWorkbenchPart part) {
				
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
				if (part instanceof FXMLEditor) {
					closingLastEditor = true;
				}
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
				if (part instanceof FXMLEditor && closingLastEditor) {
					setInspPanelEditorController(defaultEditorController);	
				}
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
				
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part instanceof FXMLEditor) {
					closingLastEditor = false;
					EditorController ec = ((FXMLEditor) part).getEditorController();
					setInspPanelEditorController(ec);
				}
			}
		});
	}
	
	private void setInspPanelEditorController(EditorController editorController) {
		inspectorPanelController.setEditorController(editorController);
	}

	@Focus
	public void setFocus() {
//		myLabelInView.setFocus();

	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s==null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		// Test if label exists (inject methods are called before PostConstruct)
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

		// Test if label exists (inject methods are called before PostConstruct)
	}
}
