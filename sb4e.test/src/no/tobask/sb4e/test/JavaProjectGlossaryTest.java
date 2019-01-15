package no.tobask.sb4e.test;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import static org.junit.Assert.*;
import java.net.URL;
import java.util.List;

import no.tobask.sb4e.JavaProjectGlossary;

public class JavaProjectGlossaryTest {
	
	JavaProjectGlossary glossary;
	URL fxmlLocation;
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
	
	@Rule
	public final ExternalResource projectResource = new JavaProjectResource(project);
		
	@Test
	public void discoversCandidates() throws Exception {
		fxmlLocation = project.getFolder("src").getFile("Test.fxml").getLocationURI().toURL();
		glossary = new JavaProjectGlossary("", fxmlLocation);
		List<String> controllerSuggestions = glossary.queryControllerClasses(fxmlLocation);
		assertEquals(1, controllerSuggestions.size());
	}
	
	@Test
	public void reactsToRemoval() throws Exception {
		fxmlLocation = project.getFolder("src").getFile("Test.fxml").getLocationURI().toURL();
		glossary = new JavaProjectGlossary("", fxmlLocation);
		JavaCore.addElementChangedListener(glossary, ElementChangedEvent.POST_CHANGE);
		assertEquals(1, glossary.queryControllerClasses(fxmlLocation).size());
		getController().delete(true, null);
		assertEquals(1, glossary.getRevision());;
		assertEquals(0, glossary.queryControllerClasses(fxmlLocation).size());
	}

	private ICompilationUnit getController() {
		IFolder folder = project.getFolder("src");
		IPackageFragment pkg = (IPackageFragment) JavaCore.create(folder);
		return pkg.getCompilationUnit("TestController.java");
	}
	
}
