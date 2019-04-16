package no.tobask.sb4e.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

public class EditorInputWatcher implements IResourceChangeListener {

	private IFile input;
	private IInputChangeListener listener;

	public EditorInputWatcher(IFile input, IInputChangeListener listener) {
		this.input = input;
		this.listener = listener;
	}

	public void setInput(IFile input) {
		this.input = input;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}
		delta = delta.findMember(input.getFullPath());
		if (delta != null) {
			int flags = delta.getFlags();
			switch (delta.getKind()) {
			case IResourceDelta.CHANGED:
				if ((IResourceDelta.CONTENT & flags) != 0) {
					listener.inputContentChanged();
				}
				break;
			case IResourceDelta.REMOVED:
				if ((IResourceDelta.MOVED_TO & flags) != 0) {
					listener.inputMoved(delta.getMovedToPath());
				} else {
					listener.inputRemoved();
				}
				break;
			default:
				break;
			}
		}
	}

}
