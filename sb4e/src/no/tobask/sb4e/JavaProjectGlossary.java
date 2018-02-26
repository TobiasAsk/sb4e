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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.oracle.javafx.scenebuilder.kit.glossary.Glossary;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;

public class JavaProjectGlossary extends Glossary implements IElementChangedListener {

	private String controllerClassName;
	private Map<String, List<String>> fxIds;
	private List<String> eventHandlers;
	private static final String FXML_ANNOTATION = FXML.class.getSimpleName();

	public JavaProjectGlossary(String controllerClassName) {
		this.controllerClassName = controllerClassName;
		JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
	}

	@Override
	public List<String> queryControllerClasses(URL fxmlLocation) {
		IJavaProject project = JavaModelUtils.getJavaProjectFromUrl(fxmlLocation);
		String packageName = JavaModelUtils.getPackageContainingFile(fxmlLocation).getElementName();
		List<ICompilationUnit> candidates = new ArrayList<>();
		for (IPackageFragment pkg : JavaModelUtils.getAllMatchingPackages(packageName, project)) {
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
			ICompilationUnit controller = JavaModelUtils.getClass(fxmlLocation, controllerClass);
			fxIds = controller == null ? new HashMap<>() : getFxIds(controller);
		}
		return fxIds.getOrDefault(Signature.getSimpleName(targetType.getName()), new ArrayList<>());
	}

	@Override
	public List<String> queryEventHandlers(URL fxmlLocation, String controllerClass) {
		if (controllerClassName == null) {
			controllerClassName = controllerClass;
		}
		if (eventHandlers == null) {
			ICompilationUnit controller = JavaModelUtils.getClass(fxmlLocation, controllerClass);
			eventHandlers = controller == null ?
					new ArrayList<>() : getEventHandlers(controller);
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
		IType type = javaClass.findPrimaryType();
		return hasNoOrEmptyConstructor(type) && hasFxmlAnnotatedMembers(type);
	}
	
	private boolean hasFxmlAnnotatedMembers(IType type) {
		try {
			List<IMethod> methods = new ArrayList<>(Arrays.asList(type.getMethods()));
			List<IField> fields = new ArrayList<>(Arrays.asList(type.getFields()));
			boolean anyMethodsAnnotated = methods.stream().anyMatch(m ->
					m.getAnnotation(FXML_ANNOTATION).exists());
			boolean anyFieldsAnnotated = fields.stream().anyMatch(f -> 
					f.getAnnotation(FXML_ANNOTATION).exists());
			return anyFieldsAnnotated || anyMethodsAnnotated;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean hasNoOrEmptyConstructor(IType type) {
		try {
			for (IMethod method : type.getMethods()) {
				if (method.isConstructor()) {
					return method.getParameters().length == 0;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return true;
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

	private boolean isEventHandler(IMethod method) throws JavaModelException {
		boolean isFxmlAnnotated = method.getAnnotation(FXML_ANNOTATION).exists();
		boolean returnsVoid = method.getReturnType().equals(Signature.SIG_VOID);
		if (isFxmlAnnotated && returnsVoid) {
			// valid ones are:
			// 1) zero parameters
			// 2) one parameter of javafx.event type
			// 3) three parameters, where the first one is of ObservableValue type
			ILocalVariable[] parameters = method.getParameters();
			if (parameters.length == 0) {
				return true;
			} else if (parameters.length == 1) {
				return isSubclass(parameters[0], Event.class);
			} else if (parameters.length == 3) {
				return isSubclass(parameters[0], ObservableValue.class);
			}
		}
		return false;
	}
	
	private boolean isSubclass(ILocalVariable parameter, Class<?> clazz) throws JavaModelException {
		String simpleName = Signature.getSignatureSimpleName(parameter.getTypeSignature());
		IType declaringType = parameter.getDeclaringMember().getDeclaringType();
		String[][] resolvedNames = declaringType.resolveType(simpleName);
		String superClassName = resolvedNames[0][0];
		return superClassName.equals(Signature.getQualifier(clazz.getName()));
	}

	private Map<String, List<String>> getFxIds(ICompilationUnit controller) {
		Map<String, List<String>> ids = new HashMap<>();
		IType type = controller.findPrimaryType();
		try {
			for (IField field : type.getFields()) {
				if (field.getAnnotation(FXML_ANNOTATION).exists()) {
					addToIds(ids, field);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return ids;
	}

	private void addToIds(Map<String, List<String>> ids, IField field) throws JavaModelException {
		String typeName = Signature.getSignatureSimpleName(field.getTypeSignature());
		String fieldName = field.getElementName();
		List<String> existingIds = ids.putIfAbsent(typeName,
				new ArrayList<>(Arrays.asList(fieldName)));
		if (existingIds != null) {
			existingIds.add(fieldName);
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
