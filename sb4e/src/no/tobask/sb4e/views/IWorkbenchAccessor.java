package no.tobask.sb4e.views;

import no.tobask.sb4e.editors.FXMLEditor;

public interface IWorkbenchAccessor {
	
	public boolean anyFxmlEditorsVisible();
	public boolean hasActiveFxmlEditor();
	public FXMLEditor getActiveFxmlEditor();

}
