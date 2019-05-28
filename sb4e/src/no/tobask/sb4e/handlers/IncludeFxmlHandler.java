package no.tobask.sb4e.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController.EditAction;

import no.tobask.sb4e.editors.FXMLEditor;

public class IncludeFxmlHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection menuSelection = HandlerUtil.getActiveMenuSelection(event);
		if (menuSelection instanceof IStructuredSelection) {
			IStructuredSelection treeSelection = (IStructuredSelection) menuSelection;
			IFile selectedFile = (IFile) treeSelection.getFirstElement();
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor instanceof FXMLEditor) {
				EditorController editorController = ((FXMLEditor) activeEditor).getEditorController();
				editorController.performIncludeFxml(selectedFile.getLocation().toFile());
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
