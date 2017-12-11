package sbplug.uitest;

import java.util.Stack;
import java.util.function.Predicate;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class JavaFxButtonFinder {
	
	public Button findButton(FXCanvas canvas, String text) {
		return (Button) findNode(canvas.getScene().getRoot(), (node) -> {
			if (node instanceof Button) {
				String buttonText = ((Button) node).getText();
				return buttonText != null && buttonText.equals(text);
			}
			return false;
		});
	}
	
	public StackPane findLibraryPanel(FXCanvas canvas) {
		return (StackPane) findNode(canvas.getScene().getRoot(), (node) -> {
			String id = node.getId();
			return id != null && id.equals("libPane");
		});
	}
	
	private Node findNode(Node root, Predicate<Node> predicate) {
		Stack<Node> queue = new Stack<Node>();
		queue.push(root);
		
		while (!queue.isEmpty()) {
			Node node = queue.pop();
			if (predicate.test(node)) {
				return node;
			}
			
			if (node instanceof Parent) {
				for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
					if (child instanceof SubScene) {
						queue.push(((SubScene) child).getRoot());
					} else {
						queue.push(child);
					}
				}
			}
		}
		return null;
	}
	
}
