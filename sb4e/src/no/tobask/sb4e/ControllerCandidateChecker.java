package no.tobask.sb4e;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import javafx.fxml.FXML;

public class ControllerCandidateChecker extends ASTVisitor {

	private static final String FXML_ANNOTATION = FXML.class.getSimpleName();
	private boolean hasEmptyConstructor;
	private boolean hasFxmlAnnotatedMembers;
	
	private boolean hasConstructor;
		
	@Override
	public boolean visit(FieldDeclaration node) {
		return visitBodyDeclaration(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.isConstructor()) {
			hasConstructor = true;
			hasEmptyConstructor = hasEmptyConstructor || node.parameters().size() == 0;
		}
		return visitBodyDeclaration(node);
	}
	
	@SuppressWarnings("unchecked")
	private boolean visitBodyDeclaration(BodyDeclaration node) {
		List<IExtendedModifier> modifiers = node.modifiers();
		for (IExtendedModifier modifier : modifiers) {
			if (modifier.isAnnotation()) {
				String annotationName = ((Annotation) modifier).getTypeName().toString();
				if (annotationName != null && annotationName.equals(FXML_ANNOTATION)) {
					hasFxmlAnnotatedMembers = true;
				}
			}
		}
		return false;
	}
	
	public boolean visitedCandidate() {
		return (hasEmptyConstructor || !hasConstructor) && hasFxmlAnnotatedMembers;
	}
	
}
