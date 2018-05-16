package no.tobask.sb4e;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static FxmlDocumentListener fxmlDocumentListener =
			new FxmlDocumentListener();
	private static EclipseProjectsClassLoader classLoader = new EclipseProjectsClassLoader();

	public static BundleContext getContext() {
		return context;
	}
	
	public static FxmlDocumentListener getFxmlDocumentListener() {
		return fxmlDocumentListener;
	}
	
	public static EclipseProjectsClassLoader getClassLoader() {
		return classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fxmlDocumentListener,
				IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fxmlDocumentListener);
	}

}
