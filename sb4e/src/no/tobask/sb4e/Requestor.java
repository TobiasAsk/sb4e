package no.tobask.sb4e;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Requestor extends ASTRequestor {
	
	private List<ICompilationUnit> candidates = new ArrayList<>();
	private String documentName;
	
	public Requestor(String documentName) {
		this.documentName = documentName;
	}
	
	@Override
	public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
		FxControllerVisitor visitor = new FxControllerVisitor(documentName, source.getElementName());
		ast.accept(visitor);
		if (visitor.isCandidate()) {
			candidates.add(source);
		}
	}
	
	public List<ICompilationUnit> getCandidates() {
		return new ArrayList<>(candidates);
	}

}
