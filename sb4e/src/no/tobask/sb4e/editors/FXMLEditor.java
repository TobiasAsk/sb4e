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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swt.FXCanvas;
import javafx.fxml.FXMLLoader;
import no.tobask.sb4e.Activator;
import no.tobask.sb4e.CustomClassLoaderLibrary;
import no.tobask.sb4e.EclipseProjectClassLoader;
import no.tobask.sb4e.JavaModelUtils;
import no.tobask.sb4e.JavaProjectGlossary;
import no.tobask.sb4e.handlers.SceneBuilderControlActionHandler;
import no.tobask.sb4e.handlers.SceneBuilderEditActionHandler;
import no.tobask.sb4e.handlers.SceneBuilderOperation;

public class FXMLEditor extends EditorPart implements IInputChangeListener {

	private EditorController editorController;
	private FXCanvas canvas;
	private URL fxmlUrl;
	private ICompilationUnit controllerClass;
	private boolean dirty = false;
	private JavaProjectGlossary glossary;

	private IUndoContext undoContext;
	private IOperationHistory operationHistory;
	private IAction copyHandler;
	private IAction cutHandler;
	private IAction pasteHandler;
	private IAction deleteHandler;
	private UndoActionHandler undoActionHandler;
	private RedoActionHandler redoActionHandler;
	private String fxmlText = null;
	private EditorInputWatcher inputWatcher;
	private boolean saveWasInvoked;
	private ProblemsLabelDecorator decorator = new ProblemsLabelDecorator();

	private ChangeListener<Number> editorSelectionListener = (oV, oldNum, newNum) -> {
		FXOMObject fxomRoot = editorController.getFxomDocument().getFxomRoot();
		if (fxomRoot == null) {
			return;
		}
		String controllerName = fxomRoot.getFxController();
		if (controllerName == null) {
			return;
		}
		if (controllerClass == null || !JavaModelUtils.getQualifiedName(controllerClass).equals(controllerName)) {
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
							IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
							IDE.gotoMarker(editor, createMarker(file, location));
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	};

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
		inputWatcher = new EditorInputWatcher(((FileEditorInput) input).getFile(), this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(inputWatcher);
	}

	@Override
	public void createPartControl(Composite parent) {
		// IMPORTANT: instantiate the canvas before the controllers so that the javafx
		// toolkit is initialized
		canvas = new FXCanvas(parent, SWT.None);
		editorController = new EditorController();
		EditorWindowController editorWindowController = new EditorWindowController(editorController);
		// make sure the other controllers have their references set before setting the
		// input file
		// for the editor controller so they can react to the update
		try {
			ClassLoader classLoader = new EclipseProjectClassLoader(getProject());
			try {
				((EclipseProjectClassLoader) classLoader).init();
			} catch (CoreException e) {
				e.printStackTrace();
				classLoader = FXMLLoader.getDefaultClassLoader();
			}
			editorController.setLibrary(new CustomClassLoaderLibrary(classLoader));

			fxmlText = FXOMDocument.readContentFromURL(fxmlUrl);
			editorController.setFxmlTextAndLocation(fxmlText, fxmlUrl);

			FXOMObject root = editorController.getFxomDocument().getFxomRoot();
			String controllerName = null;
			if (root != null) {
				controllerName = root.getFxController();
			}
			glossary = new JavaProjectGlossary(controllerName, fxmlUrl);
			editorController.setGlossary(glossary);
			JavaCore.addElementChangedListener(glossary, ElementChangedEvent.POST_CHANGE);

			copyHandler = new SceneBuilderControlActionHandler(editorController, ControlAction.COPY);
			cutHandler = new SceneBuilderEditActionHandler(editorController, EditAction.CUT);
			pasteHandler = new SceneBuilderEditActionHandler(editorController, EditAction.PASTE);
			deleteHandler = new SceneBuilderEditActionHandler(editorController, EditAction.DELETE);

			IResource input = ((FileEditorInput) getEditorInput()).getFile();
			try {
				IMarker[] problemMarkers = input.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
				if (problemMarkers.length > 0) {
					ImageDescriptor descriptor = Activator.imageDescriptorFromPlugin("no.tobask.sb4e",
							"icons/SceneBuilderLogo@2x.png");
					Image editorIcon = (Image) descriptor.createResource(PlatformUI.getWorkbench().getDisplay());
					setTitleImage(decorator.decorateImage(editorIcon, input));
					firePropertyChange(PROP_TITLE);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

			setupUndoRedo();
			editorController.startFileWatching();
			editorController.getSelection().revisionProperty().addListener(editorSelectionListener);
			canvas.setScene(editorWindowController.getScene());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private IJavaProject getProject() {
		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		IProject project = input.getFile().getProject();
		return JavaCore.create(project);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((FileEditorInput) getEditorInput()).getFile();
		try {
			String newFxmlText = editorController.getFxmlText(false);
			byte[] fxmlBytes = newFxmlText.getBytes("UTF-8");
			updateDirtyStatus(false);
			saveWasInvoked = true;
			file.setContents(new ByteArrayInputStream(fxmlBytes), IResource.FORCE, monitor);
			this.fxmlText = newFxmlText;
		} catch (UnsupportedEncodingException | CoreException e) {
			e.printStackTrace();
			updateDirtyStatus(true);
		}
	}

	@Override
	public void doSaveAs() {
		Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		IPath path = dialog.getResult();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		String newFxmlText = editorController.getFxmlText(false);
		byte[] fxmlBytes = newFxmlText.getBytes();
		try {
			file.create(new ByteArrayInputStream(fxmlBytes), IResource.FORCE, null);
			FileEditorInput newInput = new FileEditorInput(file);
			executeOnUiThread(() -> {
				setInputWithNotify(newInput);
				setPartName(newInput.getName());
			});
			inputWatcher.setInput(file);
			updateDirtyStatus(false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		super.dispose();
		if (canvas != null) {
			canvas.dispose();
		}
		if (glossary != null) {
			JavaCore.removeElementChangedListener(glossary);
		}
		if (inputWatcher != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(inputWatcher);
		}
	}

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

	public IAction getDeleteHandler() {
		return deleteHandler;
	}

	public EditorController getEditorController() {
		return editorController;
	}

	private void setupUndoRedo() {
		JobManager jobManager = editorController.getJobManager();
		jobManager.revisionProperty().addListener((observable, oldValue, newValue) -> {
			String newFxmlText = editorController.getFxmlText(false);
			updateDirtyStatus(!newFxmlText.equals(this.fxmlText));

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
				if (file.getName().equals(controllerClass.getElementName())
						&& getSite().getPage().isPartVisible(editor)) {
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
				return new int[] { start - 1, end - 1 };
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean isFreshJob(Job job) {
		SceneBuilderOperation topOperation = (SceneBuilderOperation) operationHistory.getRedoOperation(undoContext);
		return topOperation == null || topOperation.getJob() != job;
	}

	private void loadFxml(URL location) {
		Platform.runLater(() -> {
			try {
				fxmlText = FXOMDocument.readContentFromURL(location);
				editorController.setFxmlTextAndLocation(fxmlText, fxmlUrl);
				glossary.setFxmlLocation(fxmlUrl);
				updateDirtyStatus(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void updateDirtyStatus(boolean dirty) {
		boolean previousStatus = this.dirty;
		this.dirty = dirty;
		if (previousStatus != dirty) {
			firePropertyChange(PROP_DIRTY);
		}
	}

	private void executeOnUiThread(Runnable runnable) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(runnable);
	}

	@Override
	public void inputRemoved() {
		if (dirty) {
			executeOnUiThread(() -> doSaveAs());
		} else {
			executeOnUiThread(() -> getSite().getPage().closeEditor(FXMLEditor.this, false));
		}
	}

	@Override
	public void inputContentChanged() {
		if (saveWasInvoked) {
			saveWasInvoked = false;
			return;
		}
		if (dirty) {
			executeOnUiThread(() -> {
				boolean reload = (Dialog.OK != MessageDialog.open(MessageDialog.WARNING, getSite().getShell(),
						"Reload data?",
						getEditorInput().getName() + " has changed outside editor, would you like to reload it?",
						SWT.SHEET, "Keep editing", "Reload data"));
				if (reload) {
					loadFxml(fxmlUrl);
				}
			});
		} else {
			loadFxml(fxmlUrl);
		}
	}

	@Override
	public void inputMoved(IPath newLocation) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(newLocation);
		FileEditorInput newInput = new FileEditorInput(file);
		try {
			loadFxml(file.getLocationURI().toURL());
			executeOnUiThread(() -> {
				setInputWithNotify(newInput);
				setPartName(newInput.getName());
			});
			inputWatcher.setInput(file);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void inputMarkersChanged() {
		IResource input = ((FileEditorInput) getEditorInput()).getFile();
		ImageDescriptor descriptor = Activator.imageDescriptorFromPlugin("no.tobask.sb4e",
				"icons/SceneBuilderLogo@2x.png");
		Display display = PlatformUI.getWorkbench().getDisplay();
		Image editorIcon = (Image) descriptor.createResource(display);
		display.asyncExec(() -> {
			setTitleImage(decorator.decorateImage(editorIcon, input));
			firePropertyChange(PROP_TITLE);
		});
	}

}
