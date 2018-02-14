package no.tobask.sb4e;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.oracle.javafx.scenebuilder.kit.glossary.Glossary;

public class JavaProjectGlossary extends Glossary implements IElementChangedListener {

	private String controllerClassName;
	private Map<String, List<String>> fxIds;
	private List<String> eventHandlers;

	public JavaProjectGlossary() {
		JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
	}

	@Override
	public List<String> queryControllerClasses(URL fxmlLocation) {
		IJavaProject project = getJavaProjectFromUrl(fxmlLocation);
		String packageName = getPackageContainingFile(fxmlLocation).getElementName();
		List<ICompilationUnit> candidates = new ArrayList<>();
		for (IPackageFragment pkg : getAllMatchingPackages(packageName, project)) {
			candidates.addAll(getCandidateControllers(pkg, fxmlLocation));
		}
		return candidates.stream().map(c -> c.getElementName()).collect(Collectors.toList());
	}

	@Override
	public List<String> queryFxIds(URL fxmlLocation, String controllerClass, Class<?> targetType) {
		if (controllerClassName == null) {
			controllerClassName = controllerClass;
		}
		if (fxIds == null) {
			ICompilationUnit controller = discoverController(fxmlLocation, controllerClass);
			fxIds = controller == null ? getFxIds(controller) : new HashMap<>();
		}
		return fxIds.getOrDefault(getClassName(targetType), new ArrayList<>());
	}

	@Override
	public List<String> queryEventHandlers(URL fxmlLocation, String controllerClass) {
		if (controllerClassName == null) {
			controllerClassName = controllerClass;
		}
		if (eventHandlers == null) {
			ICompilationUnit controller = discoverController(fxmlLocation, controllerClass);
			eventHandlers = controller == null ?
					getEventHandlers(controller) : new ArrayList<>();
		}
		return eventHandlers;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		if (controllerClassName != null) {
			ICompilationUnit controllerClass = getControllerClass(event);
			if (controllerClass != null) {
				fxIds = getFxIds(controllerClass);
				eventHandlers = getEventHandlers(controllerClass);
			}
		}
	}

	private IJavaProject getJavaProjectFromUrl(URL url) {
		IPath path = new Path(url.getPath());
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFileForLocation(path);
		return JavaCore.create(file.getProject());
	}

	private IPackageFragment getPackageContainingFile(URL fxmlLocation) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = new Path(fxmlLocation.getPath()).removeLastSegments(1);
		IFolder folder = (IFolder) workspaceRoot.getContainerForLocation(path);
		return (IPackageFragment) JavaCore.create(folder);
	}

	private Collection<IPackageFragment> getAllMatchingPackages(String packageName,
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

	private List<IPackageFragmentRoot> getSourceFolders(IJavaProject project) {
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

	private Collection<ICompilationUnit> getCandidateControllers(IPackageFragment pkg,
			URL fxmlLocation) {
		try {
			List<ICompilationUnit> allUnits = new ArrayList<>(Arrays.asList(pkg.
					getCompilationUnits()));
			Stream<ICompilationUnit> matches = allUnits.stream().filter(u
					-> isCandidateControllerClass(u, fxmlLocation));
			return matches.collect(Collectors.toList());
		} catch (JavaModelException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	private boolean isCandidateControllerClass(ICompilationUnit javaClass, URL fxmlLocation) {
		return true;
	}

	private ICompilationUnit discoverController(URL fxmlLocation, String controllerName) {
		String packageName = getPackageContainingFile(fxmlLocation).getElementName();
		IJavaProject project = getJavaProjectFromUrl(fxmlLocation);
		for (IPackageFragment pkg : getAllMatchingPackages(packageName, project)) {
			ICompilationUnit clazz = pkg.getCompilationUnit(controllerName);
			if (clazz.exists()) {
				return clazz;
			}
		}
		return null;
	}

	private String getClassName(Class<?> clas) {
		int classNameStartIdx = clas.getName().lastIndexOf(".") + 1;
		return clas.getName().substring(classNameStartIdx);
	}

	private List<String> getEventHandlers(ICompilationUnit controller) {
		List<String> eventHandlers = new ArrayList<>();
		IType type = controller.findPrimaryType();
		try {
			for (IMethod method : type.getMethods()) {
				if (isEventHandler(method)) {
					eventHandlers.add(method.getElementName());
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return eventHandlers;
	}

	private boolean isEventHandler(IMethod method) {
		boolean isFxmlAnnotated = method.getAnnotation("FXML").exists();
		try {
			boolean returnsVoid = method.getReturnType().equals("V");
			ILocalVariable firstParameter = method.getParameters()[0];
			IType declaringType = method.getDeclaringType();
			String typeSignature = firstParameter.getTypeSignature();
			String simpleName = Signature.getSignatureSimpleName(typeSignature);
			String[][] resolvedNames = declaringType.resolveType(simpleName);
			String qualifiedName = Signature.toQualifiedName(resolvedNames[0]);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Map<String, List<String>> getFxIds(ICompilationUnit controller) {
		Map<String, List<String>> ids = new HashMap<>();
		IType type = controller.findPrimaryType();
		try {
			for (IField field : type.getFields()) {
				if (field.getAnnotation("FXML").exists()) {
					addToIds(ids, field);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return ids;
	}

	private void addToIds(Map<String, List<String>> ids, IField field) {
		try {
			String typeName = field.getTypeSignature();
			typeName = typeName.substring(1, typeName.length()-1);
			String fieldName = field.getElementName();
			List<String> existingIds = ids.putIfAbsent(typeName,
					new ArrayList<>(Arrays.asList(fieldName)));
			if (existingIds != null) {
				existingIds.add(fieldName);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private ICompilationUnit getControllerClass(ElementChangedEvent event) {
		IJavaElementDelta rootDelta = event.getDelta();
		Stack<IJavaElementDelta> queue = new Stack<>();
		queue.push(rootDelta);
		while (!queue.isEmpty()) {
			IJavaElementDelta delta = queue.pop();
			IJavaElement element = delta.getElement();
			if (element instanceof ICompilationUnit &&
					((ICompilationUnit) element).getElementName().equals(controllerClassName)) {
				return (ICompilationUnit) element;
			}
			for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
				queue.push(childDelta);
			}
		}
		return null;
	}

}
