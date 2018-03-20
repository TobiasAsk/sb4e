package no.tobask.sb4e.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import no.tobask.sb4e.editors.FXMLEditor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class FXMLEditorTest {
	
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
	
	@Rule
	public final ExternalResource projectResource = new JavaProjectResource(project);
	
	@Test
	public void opening_properlyInitializesEditor() {
		FileEditorInput input = new FileEditorInput(project.getFolder("src").getFile("Test.fxml"));
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IEditorPart editor = activePage.openEditor(input, "sb4e.editors.FXMLEditor");
			assertNotNull(editor);
			assertEquals(FXMLEditor.class, editor.getClass());
			
			FXMLEditor fxmlEditor = (FXMLEditor) editor;
			String title = fxmlEditor.getPartName();
			assertEquals("Test.fxml", title);
			
			EditorController editorController = fxmlEditor.getEditorController();
			assertNotNull(editorController);
			URL inputUrl = input.getURI().toURL();
			assertEquals(inputUrl, editorController.getFxmlLocation());
		} catch (PartInitException e) {
			e.printStackTrace();
			fail("Could not open editor");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
