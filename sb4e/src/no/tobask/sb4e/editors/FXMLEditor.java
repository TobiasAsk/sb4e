package no.tobask.sb4e.editors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.oracle.javafx.scenebuilder.app.selectionbar.SelectionBarController;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.JobManager;
import com.oracle.javafx.scenebuilder.kit.editor.job.Job;
import com.oracle.javafx.scenebuilder.kit.editor.panel.content.ContentPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.library.LibraryPanelController;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import no.tobask.sb4e.EditorWindowController;
import no.tobask.sb4e.JavaProjectGlossary;

public class FXMLEditor extends EditorPart {

	private URL fxmlUrl;
	private boolean dirty = false;
	private EditorController editorController;
	private UndoActionHandler undoActionHandler;
	private RedoActionHandler redoActionHandler;
	private IUndoContext undoContext;
	private IOperationHistory operationHistory;
	
	public IAction getUndoActionHandler() {
		return undoActionHandler;
	}
	
	public IAction getRedoActionHandler() {
		return redoActionHandler;
	}
	
	public EditorController getEditorController() {
		return editorController;
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInputWithNotify(input);
		
		if (input instanceof FileEditorInput) {
			FileEditorInput fxmlFile = (FileEditorInput) input;
			setPartName(fxmlFile.getName());
			try {
				fxmlUrl = fxmlFile.getURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		operationHistory = OperationHistoryFactory.getOperationHistory();
		undoContext = new ObjectUndoContext(this);
		undoActionHandler = new UndoActionHandler(site, undoContext);
		redoActionHandler = new RedoActionHandler(site, undoContext);
	}
	
	private void setupUndoRedo() {
		JobManager jobManager = editorController.getJobManager();
        jobManager.revisionProperty().addListener((observable, oldValue, newValue) -> {
        	dirty = jobManager.canUndo();
        	firePropertyChange(PROP_DIRTY);
        	if (!jobManager.canRedo()) {
        		// only add unique, previously unseen jobs to the operation history
            	Job currentJob = jobManager.getCurrentJob();
            	if (isFreshJob(currentJob)) {
            		String label = currentJob.getDescription();
            		operationHistory.add(new SceneBuilderOperation(label, jobManager, undoContext));
            	}
        	}
        });
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// IMPORTANT: instantiate the canvas before the controllers so that the javafx
		// toolkit is initialized
		FXCanvas canvas = new FXCanvas(parent, SWT.None);
		editorController = new EditorController();
		editorController.setGlossary(new JavaProjectGlossary());
		setupUndoRedo();
		EditorWindowController editorWindowController = new EditorWindowController(editorController);
		// make sure the other controllers have their references set before setting the input file
		// for the editor controller so they can react to the update
		try {
			editorController.setFxmlTextAndLocation(FXOMDocument
					.readContentFromURL(fxmlUrl), fxmlUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		canvas.setScene(editorWindowController.getScene());
	}
	
	private boolean isFreshJob(Job job) {
    	SceneBuilderOperation topOperation = (SceneBuilderOperation) operationHistory
    			.getRedoOperation(undoContext);
    	return topOperation == null || topOperation.getJob() != job;
	}
		
	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((FileEditorInput) getEditorInput()).getFile();
		byte[] fxmlBytes = null;
		try {
			fxmlBytes = editorController.getFxmlText().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		try {
			file.setContents(new ByteArrayInputStream(fxmlBytes), IResource.FORCE, monitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		dirty = false;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
