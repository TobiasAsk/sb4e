package no.tobask.sb4e.test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.inspector.InspectorPanelController;

import no.tobask.sb4e.editors.FXMLEditor;
import no.tobask.sb4e.views.IWorkbenchAccessor;
import no.tobask.sb4e.views.InspectorView;
import no.tobask.sb4e.views.InspectorViewController;

public class InspectorViewTest {

	private InspectorView inspectorView;
	private InspectorViewController inspectorViewController;
	private IWorkbenchAccessor workbenchAccessor;
	private Composite parent;
	private IPartService partService;

	@Before
	public void setUp() {
		inspectorViewController = mock(InspectorViewController.class);
		workbenchAccessor = mock(IWorkbenchAccessor.class);
		parent = mock(Composite.class);
		partService = mock(IPartService.class);
		inspectorView = new InspectorView(inspectorViewController, workbenchAccessor, parent, partService);
	}

	@Test
	public void setsEditorCtrler_WhenEditorIsActivated_AndEditorHasNewEditorCtrler() {
		FXMLEditor editor = mock(FXMLEditor.class);
		InspectorPanelController inspectorPanelController = mock(InspectorPanelController.class);
		EditorController editorController = mock(EditorController.class);
		EditorController editorEditorController = mock(EditorController.class);

		when(inspectorViewController.getInspectorPanelController()).thenReturn(inspectorPanelController);
		when(inspectorPanelController.getEditorController()).thenReturn(editorController);
		when(editor.getEditorController()).thenReturn(editorEditorController);
		
		inspectorView.partActivated(editor);
		verify(inspectorPanelController).setEditorController(editorEditorController);
	}

	@Test
	public void resetsEditorCtrler_WhenOtherPartIsActivated_AndNoFxmlEditorsAreVisible() {
		IWorkbenchPart part = mock(IWorkbenchPart.class);
		InspectorPanelController inspectorPanelController = mock(InspectorPanelController.class);
		EditorController editorController = mock(EditorController.class);

		when(inspectorViewController.getInspectorPanelController()).thenReturn(inspectorPanelController);
		when(inspectorPanelController.getEditorController()).thenReturn(editorController);
		when(workbenchAccessor.anyFxmlEditorsVisible()).thenReturn(false);
		
		inspectorView.partActivated(part);
		ArgumentCaptor<EditorController> editorCtrlerArgument = ArgumentCaptor.
				forClass(EditorController.class);
		verify(inspectorPanelController).setEditorController(editorCtrlerArgument.capture());
		assertEquals(null, editorCtrlerArgument.getValue().getFxmlLocation());
	}
	
}
