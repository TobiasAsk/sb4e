package no.tobask.sb4e;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import com.oracle.javafx.scenebuilder.kit.glossary.Glossary;

public class JavaProjectGlossary extends Glossary {

	@Override
	public List<String> queryControllerClasses(URL fxmlLocation) {
		IJavaProject javaProject = javaProjectFromUrl(fxmlLocation);
		ArrayList<String> classes = new ArrayList<String>();
		try {
			String packagePath = "/" + javaProject.getProject().getName() + "/src";
			IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(new Path(packagePath));
			for (IJavaElement child : root.getChildren()) {
				if (child instanceof IPackageFragment) {
					IPackageFragment fragment = (IPackageFragment) child;
					for (ICompilationUnit javaClass : fragment.getCompilationUnits()) {
						if (isControllerClass(javaClass)) {
							classes.add(javaClass.getElementName());
						}
					}
				}
			}
		} catch (JavaModelException jme) {
			jme.printStackTrace();
		}
		return classes;
	}
	
	private boolean isControllerClass(ICompilationUnit javaClass) {
		return true;
	}

	private IJavaProject javaProjectFromUrl(URL url) {
		IPath path = new Path(url.getPath());
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFileForLocation(path);
		return JavaCore.create(file.getProject());
	}

	@Override
	public List<String> queryFxIds(URL fxmlLocation, String controllerClass, Class<?> targetType) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public List<String> queryEventHandlers(URL fxmlLocation, String controllerClass) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

}
