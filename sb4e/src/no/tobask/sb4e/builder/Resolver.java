package no.tobask.sb4e.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class Resolver implements IMarkerResolutionGenerator2 {

	private static final String FX_PROBLEM_TYPE = "fxProblemType";
	private static final String MISSING_CTRLER_TYPE = "fxProblem.missingController";
	private static final String MISSING_EH_TYPE = "fxProblem.missingEventHandler";

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		String problemType = marker.getAttribute(FX_PROBLEM_TYPE, "");
		if (MISSING_CTRLER_TYPE.equals(problemType)) {
			// add controller to project
			return new IMarkerResolution[] { new MissingControllerResolution() };
		} else if (MISSING_EH_TYPE.equals(problemType)) {
			// add event handler to controller
			return new IMarkerResolution[] { new MissingEventHandlerResolution() };
		}
		return new IMarkerResolution[] {};
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

}
