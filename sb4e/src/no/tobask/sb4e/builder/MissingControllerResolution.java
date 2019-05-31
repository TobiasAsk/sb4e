package no.tobask.sb4e.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class MissingControllerResolution implements IMarkerResolution2 {

	@Override
	public String getLabel() {
		return "Add controller to project";
	}

	@Override
	public void run(IMarker marker) {
		IResource resource = marker.getResource();
		String controllerName = marker.getAttribute("controllerName", "");
		NewClassWizardPage page = new NewClassWizardPage();
		IWizard wizard = new Wizard() {

			@Override
			public boolean performFinish() {
				try {
					page.createType(null);
				} catch (CoreException | InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}

			@Override
			public void addPages() {
				IJavaProject project = JavaCore.create(resource.getProject());
				try {
					IPackageFragment pkg = project
							.findPackageFragment(resource.getParent().getFullPath());
					page.setPackageFragment(pkg, true);
					page.setPackageFragmentRoot((IPackageFragmentRoot) pkg.getParent(), true);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				page.setTitle("Create controller");
				page.setTypeName(getUnqualifiedName(controllerName), false);
				addPage(page);
			}
		};

		WizardDialog dialog = new WizardDialog(
				PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), wizard);
		if (dialog.open() == Dialog.OK) {
			try {
				marker.delete();
				IType type = page.getCreatedType();
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
						(IFile) type.getResource());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	private String getUnqualifiedName(String qualifiedName) {
		String[] parts = qualifiedName.split("\\.");
		return parts[parts.length - 1];
	}

	@Override
	public String getDescription() {
		return "Add controller to project";
	}

	@Override
	public Image getImage() {
		return null;
	}

}
