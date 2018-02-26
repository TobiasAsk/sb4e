package no.tobask.sb4e.handlers;

import org.eclipse.jface.action.Action;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController.ControlAction;

public class SceneBuilderControlActionHandler extends Action {
	
	private EditorController editorController;
	private ControlAction controlAction;
	
	public SceneBuilderControlActionHandler(EditorController editorController, ControlAction controlAction) {
		this.editorController = editorController;
		this.controlAction = controlAction;
	}
	
	@Override
	public boolean isEnabled() {
		return editorController.canPerformControlAction(controlAction);
	}
	
	@Override
	public void run() {
		if (editorController.canPerformControlAction(controlAction)) {
			editorController.performControlAction(controlAction);
		}
	}

}
