package no.tobask.sb4e;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import org.eclipse.ui.PlatformUI;

import com.oracle.javafx.scenebuilder.kit.i18n.I18N;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMInstance;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMObject;

public class FxControllerValidator extends CompilationParticipant {
					
	FxmlDocumentListener documentListener;
	
	public FxControllerValidator() {
		documentListener = Activator.getFxmlDocumentListener();
	}
	
	public void setDocumentListener(FxmlDocumentListener documentListener) {
		this.documentListener = documentListener;
	}
	
	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch) {
		for (BuildContext file : files) {
			ICompilationUnit clazz = (ICompilationUnit) JavaCore.create(file.getFile());
			String className = clazz.findPrimaryType().getFullyQualifiedName();
			if (documentListener.isAssignedController(className)) {
				URL documentLocation = documentListener.getDocument(className);
				try {
					String fxmlContent = FXOMDocument.readContentFromURL(documentLocation);
					FXOMDocument document = new FXOMDocument(fxmlContent, documentLocation,
							Activator.getClassLoader(), I18N.getBundle());
					file.recordNewProblems(getProblems(clazz, document));
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
	
	private String getDocumentName(URL url) {
		String path = url.getFile();
		int start = path.lastIndexOf("/") + 1;
		int end = path.lastIndexOf(".");
		return path.substring(start, end);
	}
	
	private CategorizedProblem[] getProblems(ICompilationUnit clazz, FXOMDocument document) {
		CompilationUnit ast = getAst(clazz);
		String documentName = getDocumentName(document.getLocation());
		FxControllerVisitor visitor = new FxControllerVisitor(documentName, clazz.getElementName());
		ast.accept(visitor);
		Map<String, List<String>> controllerIds = visitor.getFxIds();
		Map<String, FXOMObject> documentIds = document.collectFxIds();
		List<String> missingIds = new ArrayList<>();
		for (Entry<String, FXOMObject> docId : documentIds.entrySet()) {
			FXOMInstance instance = (FXOMInstance) docId.getValue();
			String componentType = instance.getDeclaredClass().getSimpleName();
			String id = docId.getKey();
			List<String> idsForInstanceType = controllerIds.get(componentType);
			if (idsForInstanceType == null || !idsForInstanceType.contains(id)) {
				missingIds.add(id + ";" + instance.getDeclaredClass().getName());
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
