package sbplug.uitest;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.ChildrenControlFinder;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCanvas;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.embed.swt.FXCanvas;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class InspectorViewTest {
	
	private static SWTWorkbenchBot bot;
	private static final String PROJECT_NAME = "prj1";
	private static final String FILE_NAME = "some.fxml";
	private Button button;
	private StackPane libraryPanel;

	@BeforeClass
	public static void setUp() {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
	
	@Test
	public void editorSelection_triggersInspectorViewUpdate() {
		// create a project with a fxml file, open the fxml file, select a gui object
		// and check that the selection is reflected in the inspector view
		
		IProject project = createProject(PROJECT_NAME);
		IFile file = createFile(FILE_NAME, project);
		SWTBotEditor editor = openEditor();
		SWTBotView view = openView();
		
		ChildrenControlFinder finder = new ChildrenControlFinder(editor.getWidget());
		List<Canvas> canvases = finder.findControls(WidgetOfType.widgetOfType(Canvas.class));
		final FXCanvas canvas = (FXCanvas) canvases.get(0);
		final JavaFxButtonFinder buttonFinder = new JavaFxButtonFinder();
				
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				button = buttonFinder.findButton(canvas, "Button");
				libraryPanel = buttonFinder.findLibraryPanel(canvas);
			}
		});
		button.localtosc
		int buttonX = (int) (button.getLayoutX() + libraryPanel.getWidth());
		SWTBotCanvas botCanvas = new SWTBotCanvas(canvas);
		botCanvas.click(buttonX, (int) button.getLayoutY());
		
		ChildrenControlFinder finderForView = new ChildrenControlFinder(view.getWidget());
	}
	
	private SWTBotView openView() {
		bot.menu("Window").menu("Show View").menu("Other...").click();
		SWTBotShell showView = bot.shell("Show View");
		showView.activate();
		bot.tree().expandNode("Sb").select("Inspector View");
		bot.button("Open").click();
		return bot.viewByTitle("Inspector View");
	}
	
	private IProject createProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	private SWTBotEditor openEditor() {
		SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		ChildrenControlFinder finder = new ChildrenControlFinder(packageExplorer.getWidget());
		List<Tree> trees = finder.findControls(WidgetOfType.widgetOfType(Tree.class));
		Tree tree = trees.get(0);
		SWTBotTree botTree = new SWTBotTree(tree);
		SWTBotTreeItem fileToOpen = botTree.expandNode(PROJECT_NAME).getNode(FILE_NAME);
		fileToOpen.contextMenu("Open With").menu("SB FXML Editor").click();
		return bot.activeEditor();
	}
	
	private IFile createFile(String fileName, IProject project) {
		IFile file = project.getFile("some.fxml");
		InputStream fxmlFile = getClass().getResourceAsStream("GreatFxml.fxml");
		try {
			project.open(null);
			file.create(fxmlFile, false, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return file;
	}
	
}
