package no.tobask.sb4e.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

public class MissingEventHandlerResolution implements IMarkerResolution2 {

	private static final String EVENT_HANDLER_NAME_ATTRIBUTE = "eventHandlerName";
	private static final String FXML_IMPORT = "javafx.fxml.FXML";

	@Override
	public String getLabel() {
		return "Add event handler to controller";
	}

	@Override
	public void run(IMarker marker) {
		IFile file = (IFile) marker.getResource();
		ICompilationUnit compUnit = JavaCore.createCompilationUnitFrom(file);
		String eventHandlerName = marker.getAttribute(EVENT_HANDLER_NAME_ATTRIBUTE, "");
		try {
			IType type = compUnit.findPrimaryType();
			type.createMethod("@FXML private void " + eventHandlerName + "() {}", null, true, null);
			IImportDeclaration fxmlImport = compUnit.getImport(FXML_IMPORT);
			if (!fxmlImport.exists()) {
				compUnit.createImport(FXML_IMPORT, null, null);
			}
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getDescription() {
		return "Add event handler to controller";
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

}
