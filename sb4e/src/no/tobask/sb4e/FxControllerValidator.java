package no.tobask.sb4e;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.oracle.javafx.scenebuilder.kit.editor.i18n.I18N;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMInstance;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMObject;

public class FxControllerValidator extends CompilationParticipant {
				
	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch) {
		for (BuildContext file : files) {
			ICompilationUnit clazz = (ICompilationUnit) JavaCore.create(file.getFile());
			String className = clazz.findPrimaryType().getFullyQualifiedName();
			FxmlDocumentListener documentListener = Activator.getFxmlDocumentListener();
			if (documentListener.isAssignedController(className)) {
				URL documentLocation = documentListener.getDocument(className);
				try {
					String fxmlContent = FXOMDocument.readContentFromURL(documentLocation);
					FXOMDocument document = new FXOMDocument(fxmlContent, documentLocation,
							Activator.getClassLoader(), I18N.getBundle());
					CompilationUnit ast = getAst(clazz);
					file.recordNewProblems(getProblems(ast, document));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private CompilationUnit getAst(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setSource(source);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	@Override
	public boolean isActive(IJavaProject project) {
		return true;
	}
	
	private CategorizedProblem[] getProblems(CompilationUnit ast, FXOMDocument document) {
		FxControllerVisitor visitor = new FxControllerVisitor();
		ast.accept(visitor);
		Map<String, String> controllerIds = visitor.getFxIds();
		Map<String, FXOMObject> documentIds = document.collectFxIds();
		List<String> missingIds = new ArrayList<>();
		for (Entry<String, FXOMObject> id : documentIds.entrySet()) {
			if (!controllerIds.containsKey(id.getKey())) {
				FXOMObject fxomObject = id.getValue();
				if (fxomObject instanceof FXOMInstance) {
					FXOMInstance instance = (FXOMInstance) fxomObject;
					missingIds.add(id.getKey() + ";" + instance.getDeclaredClass().getName());
				}
			}
		}

		if (!missingIds.isEmpty()) {
			try {
				IResource resource = ast.getTypeRoot().getCorrespondingResource();
				return new CategorizedProblem[] {new FxControllerProblem(missingIds, resource)};
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return new CategorizedProblem[0];
	}
	
}
