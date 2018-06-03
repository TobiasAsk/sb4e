package no.tobask.sb4e;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class FxmlDocumentListener implements IResourceChangeListener {

	private Map<String, URL> controllers = new HashMap<>();
	private boolean discoveryPerformed = false;
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(this::visit);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private boolean visit(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (resource != null && resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			String extension = file.getFileExtension();
			if (extension != null && extension.equals("fxml")) {
				try {
					updateControllers(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		}
		return true;
	}
	
	private void updateControllers(IFile fxmlFile) throws IOException {
		String controller = getController(fxmlFile);
		if (controller != null) {
			if (fxmlFile.exists()) {
				controllers.put(controller, fxmlFile.getLocationURI().toURL());
			} else {
				controllers.remove(controller);
			}
		}
	}

	private String getController(IFile fxmlFile) throws IOException {
		try (InputStream inputStream = fxmlFile.getLocationURI().toURL().openStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(" ");
				for (String token : tokens) {
					if (token.startsWith("fx:controller=")) {
						return token.split("=")[1].replaceAll("[^a-zA-Z0-9\\.]", "");
					}
				}
			}
		}
		return null;
	}
	
	public boolean isAssignedController(String controllerName) {
		if (!discoveryPerformed) {
			discoverControllers();
			discoveryPerformed = true;
		}
		return controllers.containsKey(controllerName);
	}

	public URL getDocument(String controllerName) {
		if (!discoveryPerformed) {
			discoverControllers();
			discoveryPerformed = true;
		}
		return controllers.get(controllerName);
	}
	
	private void discoverControllers() {
		try {
			for (IFile fxmlFile : getAllFxmlFilesInWorkspace()) {
				updateControllers(fxmlFile);
			}
		} catch (JavaModelException | IOException e) {
			e.printStackTrace();
		}
	}

	private Collection<IFile> getAllFxmlFilesInWorkspace() throws JavaModelException {
		Collection<IFile> files = new ArrayList<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			IJavaProject javaProject = JavaCore.create(project);
			files.addAll(getFxmlFiles(javaProject));
		}
		return files;
	}

	private Collection<IFile> getFxmlFiles(IJavaProject javaProject) throws JavaModelException {
		Collection<IFile> files = new ArrayList<>();
		for (IPackageFragmentRoot pkgRoot : javaProject.getPackageFragmentRoots()) {
			if (pkgRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				files.addAll(getFxmlFiles(pkgRoot));
			}
		}
		return files;
	}

	private List<IFile> getFxmlFiles(IPackageFragmentRoot pkgRoot) throws JavaModelException {
		List<IFile> files = new ArrayList<>();
		for (IJavaElement child : pkgRoot.getChildren()) {
			if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				files.addAll(getFxmlFiles((IPackageFragment) child));
			}
		}
		return files;
	}
	
	private List<IFile> getFxmlFiles(IPackageFragment pkg) throws JavaModelException {
		List<IFile> files = new ArrayList<>();
		for (Object resource : pkg.getNonJavaResources()) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				String extension = file.getFileExtension();
				if (extension != null && extension.equals("fxml")) {
					files.add(file);
				}
			}
		}
		return files;
	}

}
