package sbplug.editors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import com.oracle.javafx.scenebuilder.kit.editor.panel.content.ContentPanelController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.library.LibraryPanelController;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class FXMLEditor extends EditorPart {

	private ContentPanelController contentPanelController;
	private FXCanvas canvas;
	private URL fxmlUrl;
	private boolean dirty = false;
	private EditorController editorController;
	private UndoActionHandler undoActionHandler;
	private RedoActionHandler redoActionHandler;
	
	public EditorController getEditorController() {
		return editorController;
	}
	
	public ContentPanelController getContentPanelController() {
		return contentPanelController;
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
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		canvas = new FXCanvas(parent, SWT.None);
		editorController = new EditorController();
		contentPanelController = new ContentPanelController(editorController);
		LibraryPanelController libPanelController = new LibraryPanelController(editorController);
		
		JobManager jobManager = editorController.getJobManager();
				
        try {
			editorController.setFxmlTextAndLocation(FXOMDocument.readContentFromURL(fxmlUrl), fxmlUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		IUndoContext undoContext = new ObjectUndoContext(this);
		undoActionHandler = new UndoActionHandler(getSite(), undoContext);
		redoActionHandler = new RedoActionHandler(getSite(), undoContext);
						
        jobManager.revisionProperty().addListener((o, oV, nV) -> {
        	dirty = jobManager.canUndo();
        	firePropertyChange(PROP_DIRTY);
        	if (jobManager.getCurrentJob() != null) {
            	SceneBuilderOperation op = new SceneBuilderOperation(jobManager.getCurrentJob().getDescription(), jobManager);
            	op.addContext(undoContext);
            	OperationHistoryFactory.getOperationHistory().add(op);
        	}
        });
        
        BorderPane editorPanel = new BorderPane(contentPanelController.getPanelRoot());
        editorPanel.setTop(new SelectionBarController(editorController).getPanelRoot());
                
		canvas.setScene(new Scene(new SplitPane(libPanelController.getPanelRoot(), editorPanel)));
		Parent root = canvas.getScene().getRoot();
		int e = 2;
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	public IAction getUndoActionHandler() {
		return undoActionHandler;
	}
	
	public IAction getRedoActionHandler() {
		return redoActionHandler;
	}

}
