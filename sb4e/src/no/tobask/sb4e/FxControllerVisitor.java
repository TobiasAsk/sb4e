package no.tobask.sb4e;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import javafx.fxml.FXML;

public class FxControllerVisitor extends ASTVisitor {
	
	private Map<String, String> fxIds = new HashMap<>(); // var name -> type
	private static final String FXML_ANNOTATION = FXML.class.getSimpleName();
	
	@Override
	public boolean visit(FieldDeclaration node) {
		if (isFxIdCandidate(node)) {
			Type type = node.getType();
			ITypeBinding typeBinding = type.resolveBinding();
			String typeName;
			if (typeBinding != null) {
				typeName = typeBinding.getQualifiedName();
			} else {
				if (type.isSimpleType()) {
					typeName = ((SimpleType) type).getName().getFullyQualifiedName();
				} else {
					typeName = "UNKNOWN";
				}
			}
			List<VariableDeclarationFragment> fragments = node.fragments();
			String variableName = fragments.get(0).getName().toString();
			fxIds.put(variableName, typeName);
		}
		return super.visit(node);
	}
	
	
	public Map<String, String> getFxIds() {
		return fxIds;
	}

	private boolean isFxIdCandidate(FieldDeclaration field)	{
		List<IExtendedModifier> modifiers = field.modifiers();
		if (Modifier.isPublic(field.getModifiers())) {
			return true;
		}
		boolean hasFxmlAnnotation = false;
		for (IExtendedModifier modifier : modifiers) {
			if (modifier.isAnnotation()) {
				String annotationName = ((Annotation) modifier).getTypeName().toString();
				hasFxmlAnnotation = hasFxmlAnnotation || annotationName.equals(FXML_ANNOTATION);
			}
		}
		return hasFxmlAnnotation;
	}

}
