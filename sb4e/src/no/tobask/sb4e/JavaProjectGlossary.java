package no.tobask.sb4e;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import com.oracle.javafx.scenebuilder.app.info.InfoPanelController;
import com.oracle.javafx.scenebuilder.kit.glossary.Glossary;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;

public class JavaProjectGlossary extends Glossary implements IElementChangedListener {

	private static final String FXML_ANNOTATION = FXML.class.getSimpleName();
	
	private String controllerClassName;
	private URL fxmlLocation;
	private InfoPanelController infoPanelController;

	private Map<String, List<String>> fxIds;
	private List<String> eventHandlers;
	private List<ICompilationUnit> candidateControllers;

	public JavaProjectGlossary(String controllerClassName, URL fxmlLocation,
			InfoPanelController infoPanelController) {
		this.controllerClassName = controllerClassName;
		this.fxmlLocation = fxmlLocation;
		this.infoPanelController = infoPanelController;
		JavaCore.addElementChangedListener(this);
	}

	@Override
	public List<String> queryControllerClasses(URL fxmlLocation) {
		if (candidateControllers == null) {
			IJavaProject project = JavaModelUtils.getJavaProjectFromUrl(fxmlLocation);
			String packageName = JavaModelUtils.getPackageContainingFile(fxmlLocation).getElementName();
			candidateControllers = new ArrayList<>();
			for (IPackageFragment pkg : JavaModelUtils.getAllMatchingPackages(packageName, project)) {
				candidateControllers.addAll(getCandidateControllers(pkg));
			}
		}
		return candidateControllers.stream().map(c ->
			getSuggestionName(c)).collect(Collectors.toList());
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
		IJavaElementDelta delta = getClassChangedDelta(event.getDelta());
		if (delta != null) {
			ICompilationUnit affectedClass = (ICompilationUnit) delta.getElement();
			if (fxmlLocation != null && inSameProject(affectedClass)) {
				if (updateControllerCandidates(delta)) {
					infoPanelController.resetSuggestedControllerClasses(fxmlLocation);
				}
				
				if (controllerClassName != null && affectedClass.getElementName()
						.equals(controllerClassName) && delta.getKind() != IJavaElementDelta.REMOVED) {
					fxIds = getFxIds(affectedClass);
					eventHandlers = getEventHandlers(affectedClass);
				}
			}
		}
	}
	
	private String getSuggestionName(ICompilationUnit compilationUnit) {
		try {
			String packageName = compilationUnit.getPackageDeclarations()[0].getElementName();
			String className = compilationUnit.getElementName().split("\\.")[0];
			return packageName + "." + className;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private boolean updateControllerCandidates(IJavaElementDelta delta) {
		boolean updated = false;
		ICompilationUnit compUnit = (ICompilationUnit) delta.getElement();
		if (delta.getKind() == IJavaElementDelta.REMOVED) {
			if (candidateControllers.contains(compUnit)) {
				candidateControllers.remove(compUnit);
				updated = true;
			}
		} else {
			CompilationUnit ast = delta.getCompilationUnitAST();
			CompilationUnit clazz = ast != null ? ast : getAst(compUnit);
			ControllerCandidateChecker checker = new ControllerCandidateChecker();
			clazz.accept(checker);
			boolean isCandidateController = checker.visitedCandidate();
			if (candidateControllers.contains(compUnit)) {
				if (!isCandidateController) {
					candidateControllers.remove(compUnit);
					updated = true;
				}
			} else if (isCandidateController) {
				candidateControllers.add(compUnit);
				updated = true;
			}
		}
		return updated;
	}
			
	private CompilationUnit getAst(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setSource(source);
		return (CompilationUnit) parser.createAST(null);
	}

	private IJavaElementDelta getClassChangedDelta(IJavaElementDelta rootDelta) {
		IJavaElementDelta delta = rootDelta;
		IJavaElement element = delta.getElement();
		while (delta.getAffectedChildren().length > 0 &&
				!(element.getElementType() == IJavaElement.COMPILATION_UNIT)) {
			delta = delta.getAffectedChildren()[0];
			element = delta.getElement();
		}
		return element.getElementType() == IJavaElement.COMPILATION_UNIT ? delta : null;
	}
	
	private boolean inSameProject(ICompilationUnit clazz) {
		return clazz.getJavaProject().equals(JavaModelUtils.getJavaProjectFromUrl(fxmlLocation));
	}
		
	private List<ICompilationUnit> getCandidateControllers(IPackageFragment pkg) {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		Requestor requestor = new Requestor();
		try {
			parser.createASTs(pkg.getCompilationUnits(), null, requestor, null);
			return requestor.getCandidates();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
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

}
