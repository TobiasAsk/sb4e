package no.tobask.sb4e.handlers;

import org.eclipse.jface.action.Action;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController.EditAction;

public class SceneBuilderEditActionHandler extends Action {
	
	private EditorController editorController;
	private EditAction editAction;
	
	public SceneBuilderEditActionHandler(EditorController editorController, EditAction editAction) {
		this.editorController = editorController;
		this.editAction = editAction;
	}
	
	@Override
	public boolean isEnabled() {
		return editorController.canPerformEditAction(editAction);
	}
	
	@Override
	public void run() {
		if (editorController.canPerformEditAction(editAction)) {
			editorController.performEditAction(editAction);
		}
	}
}
