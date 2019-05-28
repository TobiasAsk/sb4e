package no.tobask.sb4e;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jdt.core.IJavaProject;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.oracle.javafx.scenebuilder.kit.library.BuiltinLibrary;
import com.oracle.javafx.scenebuilder.kit.library.IExternalLibraryItemProvider;

public class CustomClassLoaderLibrary extends BuiltinLibrary implements IJobChangeListener {

	private IJavaProject project;

	public CustomClassLoaderLibrary(ClassLoader classLoader) {
		super();
		classLoaderProperty.set(classLoader);		
	}
	
//	@Override
//	protected Collection<IExternalLibraryItemProvider> getExternalItemProviders() {
//		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
//		IConfigurationElement[] configElements = extensionRegistry.getConfigurationElementsFor("pointId");
//		Collection<IExternalLibraryItemProvider> providers = new ArrayList<>();
//		for (IConfigurationElement configElement : configElements) {
//			try {
//				Object provider = configElement.createExecutableExtension("class");
//				if (provider instanceof IExtensionRegistry) {
//					providers.add((IExternalLibraryItemProvider) provider);
//				}
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		return providers;
//	}
	
	public void scanProject() {
		FindFxComponentsJob job = new FindFxComponentsJob(this.project, getClassLoader());
		job.addJobChangeListener(this);
		job.schedule();
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void done(IJobChangeEvent event) {
		FindFxComponentsJob job = (FindFxComponentsJob) event.getJob();
		for (Class<?> component : job.getComponents()) {
//			javafx.application.Platform.runLater(() -> addDefaultItem(component, "Stuff"));
		}
	}

	@Override
	public void running(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub

	}
}
