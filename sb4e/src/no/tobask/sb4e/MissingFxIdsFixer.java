package no.tobask.sb4e;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

public class MissingFxIdsFixer implements IQuickFixProcessor {

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return problemId == 1234;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		CompilationUnit unit = context.getASTRoot();
		AST unitAst = unit.getAST();
		TypeDeclaration type = (TypeDeclaration) unit.types().get(0);

		ASTRewrite rewrite = ASTRewrite.create(unitAst);
		ListRewrite bodyDeclarationsRewrite = rewrite.getListRewrite(type,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		ListRewrite importsRewrite = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);

		List<ImportDeclaration> imports = unit.imports();
		List<String> importNames = imports.stream().map(i -> i.getName().getFullyQualifiedName())
				.collect(Collectors.toList());
		
		String[] missingIds = locations[0].getProblemArguments();
		for (String missingId : missingIds) {
			String[] parts = missingId.split(";");
			String variableName = parts[0];
			String variableType = parts[1];
			String[] variableTypeParts = variableType.split("\\.");
			
			VariableDeclarationFragment variableFragment = unitAst.newVariableDeclarationFragment();
			variableFragment.setName(unitAst.newSimpleName(variableName));
			
			FieldDeclaration field = unitAst.newFieldDeclaration(variableFragment);
			String simpleTypeName = variableTypeParts[variableTypeParts.length-1];
			field.setType(unitAst.newSimpleType(unitAst.newName(simpleTypeName)));
			
			AST fieldAst = field.getAST();
			MarkerAnnotation annotation = fieldAst.newMarkerAnnotation();
			annotation.setTypeName(unitAst.newSimpleName("FXML"));
			field.modifiers().add(annotation);
			
			ASTNode lastField = getLastField(type);
			if (lastField == null) {
				bodyDeclarationsRewrite.insertFirst(field, null);
			} else {
				bodyDeclarationsRewrite.insertAfter(field, lastField, null);				
			}
			
			if (!importNames.contains(variableType)) {
				ImportDeclaration importDeclaration = unitAst.newImportDeclaration();
				importDeclaration.setName(unitAst.newName(variableType));
				importsRewrite.insertLast(importDeclaration, null);
			}
		}
		
		ASTRewriteCorrectionProposal fix = new ASTRewriteCorrectionProposal("Add missing fields",
				context.getCompilationUnit(), rewrite, 1);
		return new IJavaCompletionProposal[] {fix};
	}
	
	private ASTNode getLastField(TypeDeclaration type) {
		List<BodyDeclaration> bodyDeclarations = type.bodyDeclarations();
		ASTNode lastField = null;
		for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
			if (bodyDeclaration.getNodeType() == ASTNode.FIELD_DECLARATION) {
				lastField = bodyDeclaration;
			}
		}
		return lastField;
	}

}
