package no.tobask.sb4e.views;

import org.eclipse.e4.tools.compat.parts.DIViewPart;

public class InspectorViewWrapper extends DIViewPart<InspectorView> {

	public InspectorViewWrapper() {
		super(InspectorView.class);
	}
	
	@Override
	public void dispose() {
		getComponent().dispose();
		super.dispose();
	}

}
