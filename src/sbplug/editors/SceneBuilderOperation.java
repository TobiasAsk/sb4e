package sbplug.editors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.oracle.javafx.scenebuilder.kit.editor.JobManager;

public class SceneBuilderOperation extends AbstractOperation {
	
	private JobManager jobManager;

	public SceneBuilderOperation(String label, JobManager jobManager) {
		super(label);
		this.jobManager = jobManager;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return org.eclipse.core.runtime.Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		jobManager.redo();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		jobManager.undo();
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean canUndo() {
		return jobManager.canUndo();
	}
	
	@Override
	public boolean canRedo() {
		return jobManager.canRedo();
	}

}
