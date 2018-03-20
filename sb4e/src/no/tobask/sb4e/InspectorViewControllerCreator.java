package no.tobask.sb4e;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import no.tobask.sb4e.views.InspectorViewController;

public class InspectorViewControllerCreator extends ContextFunction {
	
	@Override
	public Object compute(IEclipseContext context) {
		return ContextInjectionFactory.make(InspectorViewController.class, context);
	}

}
