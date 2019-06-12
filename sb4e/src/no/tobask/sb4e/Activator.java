package no.tobask.sb4e;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import no.tobask.sb4e.coherence.FxmlDocumentListener;

public class Activator extends AbstractUIPlugin {

	private static BundleContext context;
	private static FxmlDocumentListener fxmlDocumentListener =
			new FxmlDocumentListener();

	public static BundleContext getContext() {
		return context;
	}
	
	public static FxmlDocumentListener getFxmlDocumentListener() {
		return fxmlDocumentListener;
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
