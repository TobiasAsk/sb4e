package no.tobask.sb4e.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.junit.rules.ExternalResource;

public class JavaProjectResource extends ExternalResource {
	
	IProject project;
	
	public JavaProjectResource(IProject project) {
		this.project = project;
	}
	
	@Override
	protected void before() throws Throwable {
		if (!project.exists()) {
			project.create(null);
			project.open(null);
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] {JavaCore.NATURE_ID});
			project.setDescription(desc, null);
		}
		IFolder sourceFolder = project.getFolder("src");
		if (!sourceFolder.exists()) {
			sourceFolder.create(true, true, null);
		}
		IFile fxmlFile = sourceFolder.getFile("Test.fxml");
		if (!fxmlFile.exists()) {
			InputStream stream = FXMLEditorTest.class.getResourceAsStream("Test.fxml");
			fxmlFile.create(stream, true, null);
		}
		IPackageFragment pkg = (IPackageFragment) JavaCore.create(sourceFolder);
		if (!pkg.getCompilationUnit("TestController.java").exists()) {
			String controllerContents = getContents("controller.txt");
			pkg.createCompilationUnit("TestController.java", controllerContents, true, null);
		}
	}
	
	private String getContents(String fileName) throws IOException {
		StringBuilder builder = new StringBuilder();
		InputStream stream = getClass().getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}

}
