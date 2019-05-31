package no.tobask.sb4e.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import no.tobask.sb4e.coherence.SimpleFxmlParser;

public class FxProjectBuilder extends IncrementalProjectBuilder {

	private static final String FXML_EXTENSION = "fxml";
	private static final String JAVA_EXTENSION = "java";

	public static final String BUILDER_ID = "no.tobask.sb4e.fxProjectBuilder";
	private static final String MARKER_TYPE = "no.tobask.sb4e.fxProblem";
	private static final String FX_PROBLEM_TYPE = "fxProblemType";
	private static final String MISSING_CTRLER_TYPE = "fxProblem.missingController";
	private static final String MISSING_EH_TYPE = "fxProblem.missingEventHandler";
	private static final String MISSING_FXID_TYPE = "fxProblem.missingFxId";
	private static final String UNRESOLVED_CTRL_MSG = "The specified controller can not be found in the project";
	private static final String EVENT_HANDLER_NAME_ATTRIBUTE = "eventHandlerName";
	private static final String FXID_ATTRIBUTE = "fxId";

	private Map<IFile, String> controllerAssociations; // view -> controller
	private SimpleFxmlParser parser = new SimpleFxmlParser();

	class FxDeltaVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource.isDerived()) {
				return false;
			}
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						if (FXML_EXTENSION.equals(file.getFileExtension())) {
							checkView(file);
							break;
						} else if (JAVA_EXTENSION.equals(file.getFileExtension())) {
							ICompilationUnit controllerClass = JavaCore
									.createCompilationUnitFrom(file);
							IFile view = getView(controllerClass);
							if (view != null) {
								try {
									checkCoherence(controllerClass, view, true);
								} catch (IOException | XMLStreamException e) {
									e.printStackTrace();
								}
							}
							break;
						}
					case IResourceDelta.REMOVED:
						break;
					default:
						break;
				}
				return false;
			}
			// return true to continue visiting children.
			return true;
		}

		private IFile getView(ICompilationUnit controllerClass) {
			String qualifiedName = controllerClass.findPrimaryType().getFullyQualifiedName();
			for (Entry<IFile, String> association : controllerAssociations.entrySet()) {
				if (association.getValue().equals(qualifiedName)) {
					return association.getKey();
				}
			}
			return null;
		}
	}

	class ControllerAssociationDiscoverer implements IResourceVisitor {

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource.isDerived()) {
				return false;
			}
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				if (FXML_EXTENSION.equals(file.getFileExtension())) {
					String controllerName = getControllerName(file);
					if (controllerName != null) {
						controllerAssociations.put(file, controllerName);
					}
				}
				return false;
			}
			return true;
		}

		private String getControllerName(IFile file) {
			try {
				parser.setDocumentLocation(file.getLocationURI().toURL());
				parser.parseDocument();
				return parser.getControllerName();
			} catch (IOException | XMLStreamException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	class FXMLDocumentChecker implements IResourceVisitor {

		@Override
		public boolean visit(IResource resource) {
			if (resource.isDerived()) {
				return false;
			}
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				if (FXML_EXTENSION.equals(file.getFileExtension())) {
					checkView(file);
				}
			}
			// return true to continue visiting children.
			return true;
		}
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			controllerAssociations = new HashMap<>();
			getProject().accept(new FXMLDocumentChecker());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		if (controllerAssociations == null) {
			controllerAssociations = new HashMap<>();
			getProject().accept(new ControllerAssociationDiscoverer());
		}
		delta.accept(new FxDeltaVisitor());
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	private void checkView(IFile viewDocument) {
		deleteMarkers(viewDocument);
		try {
			parser.setDocumentLocation(viewDocument.getLocationURI().toURL());
			parser.parseDocument();

			// 1: check top-level view -> controller association
			String controllerName = parser.getControllerName();
			if (controllerName != null) {
				controllerAssociations.put(viewDocument, controllerName);
				IJavaProject project = JavaCore.create(getProject());
				IType controllerType = project.findType(controllerName);
				if (controllerType == null) {
					// controller not found
					addMarker(viewDocument, UNRESOLVED_CTRL_MSG, 1, IMarker.SEVERITY_ERROR,
							MISSING_CTRLER_TYPE, controllerName);
					return;
				}

				// 2: check event handler references
				checkCoherence(controllerType.getCompilationUnit(), viewDocument, false);
			}
		} catch (IOException | XMLStreamException | JavaModelException e) {
			e.printStackTrace();
		}
	}

	private void checkCoherence(ICompilationUnit controller, IFile viewDocument, boolean parse)
			throws IOException, XMLStreamException {
		deleteMarkers(viewDocument);
		deleteMarkers((IFile) controller.getResource());

		if (parse) {
			parser.setDocumentLocation(viewDocument.getLocationURI().toURL());
			parser.parseDocument();
		}

		// event handlers
		List<String> controllerEventHandlers = getEventHandlers(controller);
		List<String> viewEventHandlers = parser.getEventHandlers();
		List<String> missingFromCtrler = new ArrayList<>(viewEventHandlers);
		missingFromCtrler.removeAll(controllerEventHandlers);
		for (String missingEventHandler : missingFromCtrler) {
			addMissingEventHandlerMarker((IFile) controller.getResource(), missingEventHandler);
		}

		// fx:ids
		Map<String, String> fxIdsInController = getFxIds(controller);
		Map<String, String> fxIdsInView = parser.getFxIds();
		for (Entry<String, String> id : fxIdsInController.entrySet()) {
			if (!fxIdsInView.containsKey(id.getKey())
					|| !fxIdsInView.get(id.getKey()).equals(id.getValue())) {
				int lineNumber = getFieldLineNumber(id.getKey(), controller);
				addUnresolvedCirMarker((IFile) controller.getResource(), id.getKey(), lineNumber);
			}
		}
	}

	private int getFieldLineNumber(String unresolvedId, ICompilationUnit controller) {
		IType type = controller.findPrimaryType();
		IField field = type.getField(unresolvedId);
		try {
			String source = type.getCompilationUnit().getSource();
			String sourceUpToMethod = source.substring(0, field.getSourceRange().getOffset());
			Pattern lineEnd = Pattern.compile("$", Pattern.MULTILINE | Pattern.DOTALL); //$NON-NLS-1$
			return lineEnd.split(sourceUpToMethod).length;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void addMarker(IFile file, String message, int lineNumber, int severity,
			String problemType, String controllerName) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(FX_PROBLEM_TYPE, problemType);
			if (MISSING_CTRLER_TYPE.equals(problemType)) {
				marker.setAttribute("controllerName", controllerName);
			}
		} catch (CoreException e) {
		}
	}

	private void addMissingEventHandlerMarker(IFile controller, String eventHandlerName) {
		try {
			IMarker marker = controller.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE,
					eventHandlerName + " is missing from the controller");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(FX_PROBLEM_TYPE, MISSING_EH_TYPE);
			marker.setAttribute(EVENT_HANDLER_NAME_ATTRIBUTE, eventHandlerName);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void addUnresolvedCirMarker(IFile controller, String id, int lineNumber) {
		try {
			IMarker marker = controller.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE,
					"The fx:id " + id + " is missing from the view, and will not be injected");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(FX_PROBLEM_TYPE, MISSING_FXID_TYPE);
			marker.setAttribute(FXID_ATTRIBUTE, id);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	private List<String> getEventHandlers(ICompilationUnit controller) {
		List<String> eventHandlers = new ArrayList<>();
		IType type = controller.findPrimaryType();
		try {
			for (IMethod method : type.getMethods()) {
				IAnnotation fxmlAnnotation = method.getAnnotation("FXML");
				if (fxmlAnnotation.exists()) {
					eventHandlers.add(method.getElementName());
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return eventHandlers;
	}

	private Map<String, String> getFxIds(ICompilationUnit controller) {
		Map<String, String> ids = new HashMap<>();
		IType type = controller.findPrimaryType();
		try {
			for (IField field : type.getFields()) {
				IAnnotation fxmlAnnotation = field.getAnnotation("FXML");
				if (fxmlAnnotation.exists()) {
					String typeName = Signature.getSignatureSimpleName(field.getTypeSignature());
					String qualifiedName = String.join(".", type.resolveType(typeName)[0]);
					ids.put(field.getElementName(), qualifiedName);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return ids;
	}

}
