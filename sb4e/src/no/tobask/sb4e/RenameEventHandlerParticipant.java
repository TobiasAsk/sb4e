package no.tobask.sb4e;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.i18n.I18N;

import javafx.fxml.FXMLLoader;
import no.tobask.sb4e.coherence.FxmlDocumentListener;

public class RenameEventHandlerParticipant extends RenameParticipant {
	
	FXOMDocument view;
	String oldName;
		
	@Override
	protected boolean initialize(Object element) {
		IMethod fxEventHandler = (IMethod) element;
		ICompilationUnit controller = fxEventHandler.getCompilationUnit();
		String controllerName = controller.findPrimaryType().getFullyQualifiedName();
		FxmlDocumentListener documentListener = Activator.getFxmlDocumentListener();
		if (documentListener.isAssignedController(controllerName)) {
			try {
				URL viewLocation = documentListener.getDocument(controllerName).getLocationURI().toURL();
				String viewContent = FXOMDocument.readContentFromURL(viewLocation);
				this.oldName = fxEventHandler.getElementName();
				ClassLoader loader = new EclipseProjectClassLoader(controller.getJavaProject());
				try {
					((EclipseProjectClassLoader) loader).init();
				} catch (CoreException | MalformedURLException e) {
					loader = FXMLLoader.getDefaultClassLoader();
				}
				this.view = new FXOMDocument(viewContent, viewLocation, loader,
						I18N.getBundle());
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new ViewEventHandlerChange(this.view, this.oldName, getArguments().getNewName());
	}

}
