package no.tobask.sb4e;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class FindFxComponentsJob extends Job {

	private Collection<Class<?>> components = new ArrayList<>();
	private IJavaProject project;
	private ClassLoader classLoader;

	public Collection<Class<?>> getComponents() {
		return components;
	}

	public FindFxComponentsJob(IJavaProject project, ClassLoader classLoader) {
		super("Find FX components job");
		this.project = project;
		this.classLoader = classLoader;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			for (ICompilationUnit clazz : getCompUnits(this.project)) {
				if (isCustomComponent(clazz)) {
					String className = clazz.findPrimaryType().getFullyQualifiedName();
					Class<?> componentClass = this.classLoader.loadClass(className);
					components.add(componentClass);
				}
			}
		} catch (JavaModelException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

	private boolean isCustomComponent(ICompilationUnit clazz) throws JavaModelException {
		IType type = clazz.findPrimaryType();
		ITypeHierarchy superClassHierachy = type.newSupertypeHierarchy(null);
		IType[] superclasses = superClassHierachy.getAllSuperclasses(type);
		return Arrays.asList(superclasses).stream().anyMatch(this::isJavaFxClass);
	}
	
	private boolean isJavaFxClass(IType type) {
		String name = type.getFullyQualifiedName();
		return name.startsWith("javafx") && !name.startsWith("javafx.application");
	}

	private Collection<ICompilationUnit> getCompUnits(IJavaProject project) throws JavaModelException {
		Collection<ICompilationUnit> classes = new ArrayList<>();
		for (IPackageFragmentRoot root : getSourceFolders(project)) {
			classes.addAll(getCompUnits(root));
		}
		return classes;
	}

	private Collection<ICompilationUnit> getCompUnits(IPackageFragmentRoot root) throws JavaModelException {
		List<ICompilationUnit> classes = new ArrayList<>();
		for (IJavaElement child : root.getChildren()) {
			if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				IPackageFragment pkg = (IPackageFragment) child;
				classes.addAll(Arrays.asList(pkg.getCompilationUnits()));
			}
		}
		return classes;
	}

	private Collection<IPackageFragmentRoot> getSourceFolders(IJavaProject project) throws JavaModelException {
		List<IPackageFragmentRoot> roots = Arrays.asList(project.getPackageFragmentRoots());
		return roots.stream().filter(this::isSourceFolder).collect(Collectors.toList());
	}

	private boolean isSourceFolder(IPackageFragmentRoot root) {
		try {
			return root.getKind() == IPackageFragmentRoot.K_SOURCE;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return true;
		}
	}

}
