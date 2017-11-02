package sbplug.parts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;

import com.oracle.javafx.scenebuilder.kit.editor.EditorController;

public class EditorE4 {
	
	private URL fxmlUrl;
	private EditorController editorController;
	@Inject
	private MDirtyable dirtyable;
	private boolean dirty;
	
	@Inject
	MPart part;
	
	@Inject
	IEventBroker eventBroker;

	@Persist
	public void doSave() {
		try {
			Path fxmlPath = Paths.get(fxmlUrl.toURI());
			byte[] fxmlBytes = editorController.getFxmlText().getBytes("UTF-8");
			Files.write(fxmlPath, fxmlBytes);
			dirty = false;
			dirtyable.setDirty(dirty);
		} catch (URISyntaxException | IOException e) {
			return;
		}
	}
	
	@PostConstruct
	public void open() {
		part.getTransientData().get("input");
		int a = 3;
	}
	
}
