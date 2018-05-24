package no.tobask.sb4e.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.mockito.ArgumentCaptor;

import javafx.embed.swt.FXCanvas;
import no.tobask.sb4e.FxControllerProblem;
import no.tobask.sb4e.FxControllerValidator;
import no.tobask.sb4e.FxmlDocumentListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;

public class FxControllerValidatorTest {
	
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
	
	@Rule
	public final ExternalResource projectResource = new JavaProjectResource(project);
	
	@Test
	public void buildStarting_discoversAndReportsProblems_whenThereAreProblems() throws MalformedURLException {
		IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class);
		FxmlDocumentListener documentListener = mock(FxmlDocumentListener.class);
		context.set(FxmlDocumentListener.class, documentListener);
		FxControllerValidator validator = new FxControllerValidator();
		
		BuildContext buildContext = mock(BuildContext.class);
		IFile controller = project.getFolder("src").getFile("TestController.java");
		IFile document = project.getFolder("src").getFile("Test.fxml");
		
		when(buildContext.getFile()).thenReturn(controller);
		when(documentListener.isAssignedController("src.TestController")).thenReturn(true);
		when(documentListener.getDocument("src.TestController"))
			.thenReturn(document.getLocationURI().toURL());

		validator.buildStarting(new BuildContext[] {buildContext}, true);
		ArgumentCaptor<CategorizedProblem[]> problems = ArgumentCaptor
				.forClass(CategorizedProblem[].class);
		verify(buildContext).recordNewProblems(problems.capture());
		CategorizedProblem[] recordedProblems = problems.getValue();
		assertEquals(1, recordedProblems.length);
		FxControllerProblem problem = (FxControllerProblem) recordedProblems[0];
		String[] arguments = problem.getArguments();
		assertEquals(1, arguments.length);
		assertEquals("mainWindow;javafx.scene.layout.AnchorPane", arguments[0]);
	}

}
