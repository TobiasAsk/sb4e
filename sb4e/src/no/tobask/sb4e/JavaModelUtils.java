package no.tobask.sb4e;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

public class JavaModelUtils {

	public static ICompilationUnit getClass(URL rootLocation, String className) {
		String packageName = getPackageContainingFile(rootLocation).getElementName();
		IJavaProject project = getJavaProjectFromUrl(rootLocation);
		String simpleName = toSimpleName(className);
		for (IPackageFragment pkg : getAllMatchingPackages(packageName, project)) {
			ICompilationUnit clazz = pkg.getCompilationUnit(simpleName);
			if (clazz.exists()) {
				return clazz;
			}
		}
		return null;
	}

	public static IJavaElement getPackageContainingFile(URL fileLocation) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = new Path(fileLocation.getPath()).removeLastSegments(1);
		IFolder folder = (IFolder) workspaceRoot.getContainerForLocation(path);
		return JavaCore.create(folder);
	}

	public static IJavaProject getJavaProjectFromUrl(URL fileLocation) {
		IPath path = new Path(fileLocation.getPath());
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFileForLocation(path);
		return JavaCore.create(file.getProject());
	}

	public static Collection<IPackageFragment> getAllMatchingPackages(String packageName,
			IJavaProject project) {
		Collection<IPackageFragment> packages = new ArrayList<>();
		for (IPackageFragmentRoot folder : getSourceFolders(project)) {
			IPackageFragment pkg = folder.getPackageFragment(packageName);
			if (pkg.exists()) {
				packages.add(pkg);
			}
		}
		return packages;
	}
	
	public static String getQualifiedName(ICompilationUnit compilationUnit) {
		try {
			String packageName = compilationUnit.getPackageDeclarations()[0].getElementName();
			String className = compilationUnit.getElementName().split("\\.")[0];
			return packageName + "." + className;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static List<IPackageFragmentRoot> getSourceFolders(IJavaProject project) {
		List<IPackageFragmentRoot> sourceFolders = new ArrayList<>();
		try {
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					sourceFolders.add(root);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return sourceFolders;
	}
		
	private static String toSimpleName(String qualifiedName) {
		String[] parts = qualifiedName.split("\\.");
		return parts[parts.length-1] + ".java";
	}

}
