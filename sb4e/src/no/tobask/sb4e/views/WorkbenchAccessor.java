package no.tobask.sb4e.views;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import no.tobask.sb4e.editors.FXMLEditor;

public class WorkbenchAccessor implements IWorkbenchAccessor {
	
	private IEclipseContext context;
	
	@Inject
	public WorkbenchAccessor(IEclipseContext context) {
		this.context = context;
	}

	@Override
	public boolean anyFxmlEditorsVisible() {
		IWorkbenchPage activePage = context.get(IWorkbenchPage.class);
		for (IEditorReference editorReference : activePage.getEditorReferences()) {
			IWorkbenchPart part = editorReference.getPart(false);
			if (part instanceof FXMLEditor && activePage.isPartVisible(part)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasActiveFxmlEditor() {
		IWorkbenchPage activePage = context.get(IWorkbenchPage.class);
		IEditorPart activeEditor = activePage.getActiveEditor();
		return activeEditor instanceof FXMLEditor;
	}

	@Override
	public FXMLEditor getActiveFxmlEditor() {
		IWorkbenchPage activePage = context.get(IWorkbenchPage.class);
		return (FXMLEditor) activePage.getActiveEditor();
	}

}
