package no.tobask.sb4e;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

public class FxControllerProblem extends CategorizedProblem {
	
	List<String> missingIds;
	IResource resource;

	public FxControllerProblem(List<String> missingIds, IResource resource) {
		this.missingIds = missingIds;
		this.resource = resource;
	}

	@Override
	public String[] getArguments() {
		return missingIds.toArray(new String[0]);
	}

	@Override
	public int getID() {
		return 1234;
	}

	@Override
	public String getMessage() {
		return "One or more fx:ids from associated FXML document do not have corresponding fields";
	}

	@Override
	public char[] getOriginatingFileName() {
		return resource.getName().toCharArray();
	}

	@Override
	public int getSourceEnd() {
		return -1;
	}

	@Override
	public int getSourceLineNumber() {
		return 1;
	}

	@Override
	public int getSourceStart() {
		return 0;
	}

	@Override
	public boolean isError() {
		return false;
	}

	@Override
	public boolean isWarning() {
		return true;
	}

	@Override
	public void setSourceEnd(int sourceEnd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSourceLineNumber(int lineNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSourceStart(int sourceStart) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCategoryID() {
		return CAT_POTENTIAL_PROGRAMMING_PROBLEM;
	}

	@Override
	public String getMarkerType() {
		return "no.tobask.sb4e.fxcontrollerproblemmarker";
	}

}
