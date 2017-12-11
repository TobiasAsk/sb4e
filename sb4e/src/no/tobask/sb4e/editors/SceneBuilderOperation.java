package no.tobask.sb4e.editors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.oracle.javafx.scenebuilder.kit.editor.JobManager;
import com.oracle.javafx.scenebuilder.kit.editor.job.Job;

public class SceneBuilderOperation extends AbstractOperation {
	
	private JobManager jobManager;
	private Job job;

	public SceneBuilderOperation(String label, JobManager jobManager, IUndoContext undoContext) {
		super(label);
		addContext(undoContext);
		this.jobManager = jobManager;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		job = jobManager.getRedoStack().get(0);
		jobManager.redo();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		job = jobManager.getUndoStack().get(0);
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
	
	public Job getJob() {
		return job;
	}

}
