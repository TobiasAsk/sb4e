package no.tobask.sb4e.coherence;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMInstance;
import com.oracle.javafx.scenebuilder.kit.fxom.FXOMObject;
import com.oracle.javafx.scenebuilder.kit.i18n.I18N;

import no.tobask.sb4e.Activator;
import no.tobask.sb4e.FxControllerVisitor;

public class FxControllerValidator extends CompilationParticipant {

	FxmlDocumentListener documentListener;

	public FxControllerValidator() {
		documentListener = Activator.getFxmlDocumentListener();
	}

	public void setDocumentListener(FxmlDocumentListener documentListener) {
		this.documentListener = documentListener;
	}

//	@Override
//	public void buildStarting(BuildContext[] files, boolean isBatch) {
//		for (BuildContext file : files) {
//			ICompilationUnit clazz = (ICompilationUnit) JavaCore.create(file.getFile());
//			String className = clazz.findPrimaryType().getFullyQualifiedName();
//			if (documentListener.isAssignedController(className)) {
//				URL documentLocation = documentListener.getDocument(className);
//				try {
//					String fxmlContent = FXOMDocument.readContentFromURL(documentLocation);
//					FXOMDocument document = new FXOMDocument(fxmlContent, documentLocation,
//							Activator.getClassLoader(), I18N.getBundle());
//					file.recordNewProblems(getProblems(clazz, document));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

//	@Override
//	public void buildFinished(IJavaProject project) {
//		// iterate over all comp units
//		try {
//			for (ICompilationUnit clazz : getCompUnits(project)) {
//				String className = clazz.findPrimaryType().getFullyQualifiedName();
//				if (documentListener.isAssignedController(className)) {
//					URL documentLocation = documentListener.getDocument(className);
//					try {
//						String fxmlContent = FXOMDocument.readContentFromURL(documentLocation);
//						FXOMDocument document = new FXOMDocument(fxmlContent, documentLocation,
//								Activator.getClassLoader(), I18N.getBundle());
//						String missingIds = getMissingIds(clazz, document);
//						if (!missingIds.isEmpty()) {
//							clazz.getResource().deleteMarkers("no.tobask.sb4e.fxcontrollerproblemmarker", false, 1);
//							IMarker marker = clazz.getResource().createMarker("no.tobask.sb4e.fxcontrollerproblemmarker");
//							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
//							marker.setAttribute(IMarker.LINE_NUMBER, 1);
//							marker.setAttribute(IMarker.MESSAGE, "There are fx:ids declared in the FXML document that are missing from its controller");
//							marker.setAttribute("missingIds", missingIds);
//						}
//					} catch (IOException | CoreException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public void reconcile(ReconcileContext context) {
		ICompilationUnit clazz = context.getWorkingCopy();
		String className = clazz.findPrimaryType().getFullyQualifiedName();
		if (documentListener.isAssignedController(className)) {
			URL documentLocation = documentListener.getDocument(className);
			try {
				String fxmlContent = FXOMDocument.readContentFromURL(documentLocation);
				FXOMDocument document = new FXOMDocument(fxmlContent, documentLocation, Activator.getClassLoader(),
						I18N.getBundle());
				CategorizedProblem[] problems = getProblems(context.getAST(AST.JLS11), document);
				if (problems != null) {
					context.putProblems("no.tobask.sb4e.fxcontrollerproblemmarker", problems);
				}
			} catch (IOException | JavaModelException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ICompilationUnit> getCompUnits(IJavaProject project) throws JavaModelException {
		List<ICompilationUnit> units = new ArrayList<>();
		for (IPackageFragmentRoot pkgRoot : project.getPackageFragmentRoots()) {
			if (pkgRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				units.addAll(getCompUnits(pkgRoot));
			}
		}
		return units;
	}

	private List<ICompilationUnit> getCompUnits(IPackageFragmentRoot pkgRoot) throws JavaModelException {
		List<ICompilationUnit> units = new ArrayList<>();
		for (IJavaElement child : pkgRoot.getChildren()) {
			if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				ICompilationUnit[] pkgUnits = ((IPackageFragment) child).getCompilationUnits();
				units.addAll(Arrays.asList(pkgUnits));
			}
		}
		return units;
	}

	private CompilationUnit getAst(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
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

//	private String getMissingIds(ICompilationUnit clazz, FXOMDocument document) {
//		CompilationUnit ast = getAst(clazz);
//		String documentName = getDocumentName(document.getLocation());
//		FxControllerVisitor visitor = new FxControllerVisitor(documentName, clazz.getElementName());
//		ast.accept(visitor);
//		Map<String, List<String>> controllerIds = visitor.getFxIds();
//		Map<String, FXOMObject> documentIds = document.collectFxIds();
//		List<String> missingIds = new ArrayList<>();
//		for (Entry<String, FXOMObject> docId : documentIds.entrySet()) {
//			FXOMInstance instance = (FXOMInstance) docId.getValue();
//			String componentType = instance.getDeclaredClass().getSimpleName();
//			String id = docId.getKey();
//			List<String> idsForInstanceType = controllerIds.get(componentType);
//			if (idsForInstanceType == null || !idsForInstanceType.contains(id)) {
//				missingIds.add(id + ";" + instance.getDeclaredClass().getName());
//			}
//		}
//		return String.join("|", missingIds);
//	}

	private CategorizedProblem[] getProblems(CompilationUnit ast, FXOMDocument document) {
		String documentName = getDocumentName(document.getLocation());
		FxControllerVisitor visitor = new FxControllerVisitor(documentName, ast.getJavaElement().getElementName());
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
				return new CategorizedProblem[] { new FxControllerProblem(missingIds, resource) };
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
