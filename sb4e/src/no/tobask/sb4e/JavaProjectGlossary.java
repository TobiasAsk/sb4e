package no.tobask.sb4e;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import com.oracle.javafx.scenebuilder.kit.glossary.Glossary;

public class JavaProjectGlossary extends Glossary implements IElementChangedListener {
	
	private String qualifiedControllerName;
	private URL fxmlLocation;

	private Map<String, List<String>> fxIds;
	private List<String> eventHandlers;
	private List<ICompilationUnit> candidateControllers;

	public JavaProjectGlossary(String controllerClassName, URL fxmlLocation) {
		this.qualifiedControllerName = controllerClassName;
		this.fxmlLocation = fxmlLocation;
	}

	@Override
	public List<String> queryControllerClasses(URL fxmlLocation) {
		if (candidateControllers == null) {
			IJavaProject project = JavaModelUtils.getJavaProjectFromUrl(fxmlLocation);
			IJavaElement pkgWithFile = JavaModelUtils.getPackageContainingFile(fxmlLocation);
			String packageName = (pkgWithFile.getElementType() == IJavaElement.PACKAGE_FRAGMENT) ? 
					pkgWithFile.getElementName() : "";
			candidateControllers = new ArrayList<>();
			for (IPackageFragment pkg : JavaModelUtils.getAllMatchingPackages(packageName, project)) {
				candidateControllers.addAll(getCandidateControllers(pkg));
			}
		}
		return candidateControllers.stream().map(c ->
			JavaModelUtils.getQualifiedName(c)).collect(Collectors.toList());
	}

	@Override
	public List<String> queryFxIds(URL fxmlLocation, String controllerClass, Class<?> targetType) {
		String previousController = qualifiedControllerName;
		qualifiedControllerName = controllerClass;
		if (qualifiedControllerName == null) {
			return new ArrayList<>();
		}
		if (!previousController.equals(qualifiedControllerName) || fxIds == null) {
			ICompilationUnit controller = JavaModelUtils.getClass(fxmlLocation, qualifiedControllerName);
			fxIds = controller == null ? new HashMap<>() : getFxIds(controller);
		}
		String typeName = Signature.getSimpleName(targetType.getName());
		List<String> ids = fxIds.getOrDefault(typeName, new ArrayList<>());
		return new ArrayList<>(ids);
	}

	@Override
	public List<String> queryEventHandlers(URL fxmlLocation, String controllerClass) {
		String previousController = qualifiedControllerName;
		qualifiedControllerName = controllerClass;
		if (qualifiedControllerName == null) {
			return new ArrayList<>();
		}
		if (!previousController.equals(qualifiedControllerName) || eventHandlers == null) {
			ICompilationUnit controller = JavaModelUtils.getClass(fxmlLocation, qualifiedControllerName);
			eventHandlers = controller == null ?
					new ArrayList<>() : getEventHandlers(controller);
		}
		return new ArrayList<>(eventHandlers);
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta delta = getClassChangedDelta(event.getDelta());
		boolean revised = false;
		if (delta != null) {
			ICompilationUnit affectedClass = (ICompilationUnit) delta.getElement();
			if (fxmlLocation != null && inSameProject(affectedClass)) {
				List<ICompilationUnit> prevCandidates = new ArrayList<>(candidateControllers);
				updateControllerCandidates(delta);
				if (!prevCandidates.equals(candidateControllers)) {
					revised = true;
				}
				
				if (delta.getKind() != IJavaElementDelta.REMOVED && qualifiedControllerName != null
						&& JavaModelUtils.getQualifiedName(affectedClass).equals(qualifiedControllerName)) {
					Map<String, List<String>> prevFxIds = fxIds;
					fxIds = getFxIds(affectedClass);
					List<String> prevEventHandlers = eventHandlers;
					eventHandlers = getEventHandlers(affectedClass);
					if (!fxIds.equals(prevFxIds) || !eventHandlers.equals(prevEventHandlers)) {
						revised = true;
					}
				}
			}
		}
		if (revised) {
			incrementRevision();
		}
	}
		
	private void updateControllerCandidates(IJavaElementDelta delta) {
		ICompilationUnit compUnit = (ICompilationUnit) delta.getElement();
		if (delta.getKind() == IJavaElementDelta.REMOVED) {
			if (candidateControllers.contains(compUnit)) {
				candidateControllers.remove(compUnit);
			}
		} else {
			CompilationUnit ast = delta.getCompilationUnitAST();
			CompilationUnit clazz = ast != null ? ast : getAst(compUnit);
			FxControllerVisitor checker = new FxControllerVisitor(getDocumentName(fxmlLocation),
					compUnit.getElementName());
			clazz.accept(checker);
			boolean isCandidateController = checker.isCandidate();

			if (isCandidateController && !candidateControllers.contains(compUnit)) {
				candidateControllers.add(compUnit);
			} else if (!isCandidateController && candidateControllers.contains(compUnit)) {
				candidateControllers.remove(compUnit);
			}
		}
	}
			
	private CompilationUnit getAst(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setSource(source);
		parser.setResolveBindings(true);
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
		Requestor requestor = new Requestor(getDocumentName(fxmlLocation));
		try {
			parser.createASTs(pkg.getCompilationUnits(), null, requestor, null);
			return requestor.getCandidates();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	private String getDocumentName(URL url) {
		String path = url.getFile();
		int start = path.lastIndexOf("/") + 1;
		int end = path.lastIndexOf(".");
		return path.substring(start, end);
	}
			
	private List<String> getEventHandlers(ICompilationUnit controller) {
		CompilationUnit compUnit = getAst(controller);
		FxControllerVisitor checker = new FxControllerVisitor(getDocumentName(fxmlLocation),
				controller.getElementName());
		compUnit.accept(checker);
		return checker.getEventHandlers();
	}

	private Map<String, List<String>> getFxIds(ICompilationUnit controller) {
		CompilationUnit compUnit = getAst(controller);
		FxControllerVisitor checker = new FxControllerVisitor(getDocumentName(fxmlLocation),
				controller.getElementName());
		compUnit.accept(checker);
		return checker.getFxIds();
	}

}
