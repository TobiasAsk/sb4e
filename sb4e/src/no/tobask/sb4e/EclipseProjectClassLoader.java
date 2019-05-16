package no.tobask.sb4e;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;

public class EclipseProjectClassLoader extends ClassLoader {
	
	private URLClassLoader loader;
	private IJavaProject project;

	public EclipseProjectClassLoader(IJavaProject project) {
		this.project = project;
	}

	public void init() throws CoreException, MalformedURLException {
		ClassLoader parentClassLoader = project.getClass().getClassLoader();
		URL[] urls = getClassPathAsUrls(project);
		loader = new URLClassLoader(urls, parentClassLoader);
	}

	private URL[] getClassPathAsUrls(IJavaProject javaProject) throws CoreException, MalformedURLException {
		String[] classPath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		List<URL> urls = new ArrayList<>();
		for (String entry : classPath) {
			IPath path = new Path(entry);
			urls.add(path.toFile().toURI().toURL());
		}
		return urls.toArray(new URL[urls.size()]);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name);
	}

}
