package no.tobask.sb4e.editors;

import org.eclipse.core.runtime.IPath;

public interface IInputChangeListener {
	
	public void inputRemoved();
	public void inputContentChanged();
	public void inputMoved(IPath newLocation);

}
