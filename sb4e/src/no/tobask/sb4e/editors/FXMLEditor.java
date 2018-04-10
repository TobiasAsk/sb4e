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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;
import com.oracle.javafx.scenebuilder.kit.editor.JobManager;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController.ControlAction;
import com.oracle.javafx.scenebuilder.kit.editor.EditorController.EditAction;
import com.oracle.javafx.scenebuilder.kit.editor.job.Job;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMObject;

import javafx.beans.value.ChangeListener;
import javafx.embed.swt.FXCanvas;
import no.tobask.sb4e.CustomClassLoaderLibrary;
import no.tobask.sb4e.EclipseProjectsClassLoader;
import no.tobask.sb4e.JavaModelUtils;
import no.tobask.sb4e.JavaProjectGlossary;
import no.tobask.sb4e.handlers.SceneBuilderControlActionHandler;
import no.tobask.sb4e.handlers.SceneBuilderEditActionHandler;
import no.tobask.sb4e.handlers.SceneBuilderOperation;

public class FXMLEditor extends EditorPart {

	private URL fxmlUrl;
	private boolean dirty = false;
	private EditorController editorController;
	private UndoActionHandler undoActionHandler;
	private RedoActionHandler redoActionHandler;
	private IUndoContext undoContext;
	private IOperationHistory operationHistory;
	private ICompilationUnit controllerClass;
	private IAction copyHandler;
	private IAction cutHandler;
	private IAction pasteHandler;

	public IAction getUndoActionHandler() {
		return undoActionHandler;
	}
	
	public IAction getRedoActionHandler() {
		return redoActionHandler;
	}
	
	public IAction getCopyHandler() {
		return copyHandler;
	}
	
	public IAction getCutHandler() {
		return cutHandler;
	}

	public IAction getPasteHandler() {
		return pasteHandler;
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
		editorController.setLibrary(new CustomClassLoaderLibrary(new EclipseProjectsClassLoader()));
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
		String controllerName = editorController.getFxomDocument().getFxomRoot().getFxController();
		editorController.setGlossary(new JavaProjectGlossary(controllerName, fxmlUrl,
				editorWindowController.infoPanelController));
		canvas.setScene(editorWindowController.getScene());
		editorController.getSelection().revisionProperty().addListener(editorSelectionListener);
		
		copyHandler = new SceneBuilderControlActionHandler(editorController, ControlAction.COPY);
		cutHandler = new SceneBuilderEditActionHandler(editorController, EditAction.CUT);
		pasteHandler = new SceneBuilderEditActionHandler(editorController, EditAction.PASTE);
	}
	
	private ChangeListener<Number> editorSelectionListener = (oV, oldNum, newNum) -> {
		FXOMObject fxomRoot = editorController.getFxomDocument().getFxomRoot();
		if (fxomRoot == null) {
			return;
		}
		String controllerName = fxomRoot.getFxController();
		if (controllerName == null) {
			return;
		}
		if (controllerClass == null || !controllerClass.getElementName().equals(controllerName)) {
			controllerClass = JavaModelUtils.getClass(fxmlUrl, controllerName);
			if (controllerClass == null) {
				return;
			}
		}
		IEditorPart editor = getEditorEditingController();
		if (editor != null) {
			try {
				FXOMObject selectedObject = editorController.getSelection().getHitItem();
				if (selectedObject != null) {
					String fxId = selectedObject.getFxId();
					if (fxId != null) {
						int[] location = getLocation(fxId, controllerClass);
						if (location != null) {
							IFile file = ((FileEditorInput) editor.getEditorInput())
									.getFile();
							IDE.gotoMarker(editor, createMarker(file, location));
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	};
	
	private IMarker createMarker(IFile file, int[] location) throws CoreException {
		IMarker marker = file.createMarker(IMarker.TEXT);
		marker.setAttribute(IMarker.CHAR_START, location[0]);
		marker.setAttribute(IMarker.CHAR_END, location[1]);
		return marker;
	}
	
	private IEditorPart getEditorEditingController() {
		for (IEditorReference editorRef : getSite().getPage().getEditorReferences()) {
			IEditorPart editor = editorRef.getEditor(true);
			if (editor != null && editor.getEditorInput() instanceof FileEditorInput) {
				IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
				if (file.getName().equals(controllerClass.getElementName()) &&
						getSite().getPage().isPartVisible(editor)) {
					return editor;
				}
			}
		}
		return null;
	}
	
	private int[] getLocation(String fxId, ICompilationUnit controllerClass) {
		IType type = controllerClass.findPrimaryType();
		IField field = type.getField(fxId);
		if (field.exists()) {
			try {
				ISourceRange sourceRange = field.getSourceRange();
				int end = sourceRange.getOffset() + sourceRange.getLength();
				int start = end - fxId.length();
				return new int[] {start-1, end-1};
			} catch (JavaModelException e) {
				e.printStackTrace();
			}			
		}
		return null;
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
