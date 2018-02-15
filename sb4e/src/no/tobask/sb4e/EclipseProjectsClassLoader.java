package no.tobask.sb4e;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

public class EclipseProjectsClassLoader extends ClassLoader {
	
	Collection<URLClassLoader> loaders = new ArrayList<>();
	
	public EclipseProjectsClassLoader() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			loaders.add(getProjectClassLoader(JavaCore.create(project)));
		}
	}
	
	private URLClassLoader getProjectClassLoader(IJavaProject javaProject) {
		ClassLoader parentClassLoader = javaProject.getClass().getClassLoader();
		try {
			URL[] urls = getClassPathAsUrls(javaProject);
			return new URLClassLoader(urls, parentClassLoader);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private URL[] getClassPathAsUrls(IJavaProject javaProject) throws CoreException {
		String[] classPath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		List<URL> urls = new ArrayList<>();
		for (String entry : classPath) {
			IPath path = new Path(entry);
			try {
				urls.add(path.toFile().toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		for (URLClassLoader loader : loaders) {
			try {
				Class<?> clazz = loader.loadClass(name);
				return clazz;
			} catch (ClassNotFoundException e) {
			}
		}
		throw new ClassNotFoundException();
	}

}
