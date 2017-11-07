package sbplug.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

public class FXMLEditorContributor extends EditorActionBarContributor {

	public FXMLEditorContributor() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof FXMLEditor) {
			FXMLEditor editor = (FXMLEditor) targetEditor;
			getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), editor.getUndoActionHandler());
			getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), editor.getRedoActionHandler());
			getActionBars().updateActionBars();
		}
	}

}
