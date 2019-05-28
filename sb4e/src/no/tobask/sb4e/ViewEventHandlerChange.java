package no.tobask.sb4e;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMPropertyT;

public class ViewEventHandlerChange extends ResourceChange {

	private FXOMDocument view;
	private String oldName;
	private String newName;

	public ViewEventHandlerChange(FXOMDocument view, String oldName, String newName) {
		this.view = view;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	protected IResource getModifiedResource() {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(view.getLocation().toURI())[0];
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			List<FXOMPropertyT> eventHandlers = view.getFxomRoot().collectEventHandlers();
			Optional<FXOMPropertyT> handler = eventHandlers.stream().filter(prop -> prop.getValue().endsWith(oldName))
					.findFirst();
			if (handler.isPresent()) {
				handler.get().setValue("#" + newName);
				InputStream stream = new ByteArrayInputStream(view.getFxmlText(false).getBytes("UTF-8"));
				IFile viewFile = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocationURI(view.getLocation().toURI())[0];
				viewFile.setContents(stream, IResource.FORCE, pm);
				return new ViewEventHandlerChange(view, newName, oldName);
			}
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

}
