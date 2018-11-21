package no.tobask.sb4e;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;

public class FxControllerVisitor extends ASTVisitor {
	
	private static final String FXML_ANNOTATION = FXML.class.getSimpleName();
	private List<String> eventHandlers = new ArrayList<>();
	private Map<String, List<String>> fxIds = new HashMap<>();
	
	private boolean hasEmptyConstructor;
	private boolean hasConstructor;
	private String documentName;
	private String className;
	
	public FxControllerVisitor(String documentName, String className) {
		this.documentName = documentName;
		this.className = className;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.isConstructor()) {
			hasConstructor = true;
			hasEmptyConstructor = hasEmptyConstructor || node.parameters().size() == 0;
		}
		
		if ((isPublic(node) || hasFxmlAnnotation(node)) && isValidEventHandler(node)) {
			String name = node.getName().getFullyQualifiedName();
			eventHandlers.add(name);
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isValidEventHandler(MethodDeclaration node) {
		List<SingleVariableDeclaration> parameters = node.parameters();
		if (parameters.size() == 0) {
			return true;
		} else if (parameters.size() == 1) {
			SingleVariableDeclaration param = parameters.get(0);
			return isSubClassOf(Event.class.getName(), param.getType());
		} else if (parameters.size() == 3) {
			SingleVariableDeclaration firstParam = parameters.get(0);
			return isSubClassOf(ObservableValue.class.getName(), firstParam.getType());
		}
		return false;
	}
	
	private boolean isPublic(BodyDeclaration node) {
		return Modifier.isPublic(node.getModifiers());
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if ((isPublic(node) || hasFxmlAnnotation(node)) && isSubClassOf("javafx", node.getType())) {
			addToIds(node);
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private void addToIds(FieldDeclaration node) {
		Type type = node.getType();
		if (type.isSimpleType()) {
			String typeName = ((SimpleType) type).getName().getFullyQualifiedName();
			List<VariableDeclarationFragment> fragments = node.fragments();
			String varName = fragments.get(0).getName().getFullyQualifiedName();
			List<String> prevIds = fxIds.putIfAbsent(typeName,
					new ArrayList<>(Arrays.asList(varName)));
			if (prevIds != null) {
				prevIds.add(varName);
			}
 		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean hasFxmlAnnotation(BodyDeclaration node) {
		List<IExtendedModifier> modifiers = node.modifiers();
		for (IExtendedModifier modifier : modifiers) {
			if (modifier.isAnnotation()) {
				String annotationName = ((Annotation) modifier).getTypeName().getFullyQualifiedName();
				if (annotationName != null && annotationName.equals(FXML_ANNOTATION)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isSubClassOf(String superClass, Type classType) {
		if (classType.isPrimitiveType()) {
			return false;
		}
		ITypeBinding binding = classType.resolveBinding();
		if (binding != null) {
			ITypeBinding objectType = classType.getAST().resolveWellKnownType("java.lang.Object");
			ITypeBinding type = binding;
			while (type.getSuperclass() != objectType) {
				type = type.getSuperclass();
				if (type.getQualifiedName().startsWith(superClass)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<String> getEventHandlers() {
		return new ArrayList<>(eventHandlers);
	}
	
	public Map<String, List<String>> getFxIds() {
		return new HashMap<>(fxIds);
	}
	
	public boolean isCandidate() {
		boolean hasFxMembers = eventHandlers.size() > 0 || fxIds.size() > 0;
		boolean contentMatch = (hasEmptyConstructor || !hasConstructor) && hasFxMembers;
		boolean nameMatch = className.startsWith(documentName);
		return contentMatch || nameMatch;
	}
	
}
