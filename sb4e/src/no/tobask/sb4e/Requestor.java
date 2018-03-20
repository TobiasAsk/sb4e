package no.tobask.sb4e;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Requestor extends ASTRequestor {
	
	private List<ICompilationUnit> candidates = new ArrayList<>();
	
	@Override
	public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
		ControllerCandidateChecker checker = new ControllerCandidateChecker();
		ast.accept(checker);
		if (checker.visitedCandidate()) {
			candidates.add(source);
		}
	}
	
	public List<ICompilationUnit> getCandidates() {
		return new ArrayList<>(candidates);
	}

}
