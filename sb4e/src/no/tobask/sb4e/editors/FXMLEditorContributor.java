package no.tobask.sb4e.editors;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

public class FXMLEditorContributor extends EditorActionBarContributor {
	
	public FXMLEditorContributor() {
	}
	
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof FXMLEditor) {
			FXMLEditor editor = (FXMLEditor) targetEditor;
			IActionBars actionBars = getActionBars();
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), editor.getUndoActionHandler());
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), editor.getRedoActionHandler());
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), editor.getCopyHandler());
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), editor.getCutHandler());
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), editor.getPasteHandler());
			actionBars.updateActionBars();
		}
	}

}
