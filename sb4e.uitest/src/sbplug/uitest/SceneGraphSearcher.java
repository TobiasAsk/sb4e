package sbplug.uitest;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.oracle.javafx.scenebuilder.kit.fxom.FXOMDocument;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Button;

public class SceneGraphSearcher {
	
	private FXCanvas canvas;
	private Group contentGroup;
	
	public SceneGraphSearcher(FXCanvas canvas) {
		this.canvas = canvas;
		contentGroup = findContentGroup();
	}
	
	private List<Node> findNodes(Node root, Predicate<Node> predicate) {
		Stack<Node> queue = new Stack<Node>();
		queue.push(root);
		List<Node> matches = new ArrayList<Node>();
		while (!queue.isEmpty()) {
			Node node = queue.pop();
			if (predicate.test(node)) {
				matches.add(node);
			}
			if (node instanceof Parent) {
				for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
					queue.push(child instanceof SubScene ? ((SubScene) child).getRoot() : child);
				}
			}
		}
		return matches;
	}
	
	private void sceneGraphOperation(Node root, Consumer<Node> operation) {
		Stack<Node> queue = new Stack<Node>();
		queue.push(root);
		while (!queue.isEmpty()) {
			Node node = queue.pop();
			operation.accept(node);
			if (node instanceof Parent) {
				for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
					queue.push(child instanceof SubScene ? ((SubScene) child).getRoot() : child);
				}
			}
		}
	}
	
	private List<Node> sceneGraphSearch(Node root, Predicate<Node> predicate) {
		List<Node> matches = new ArrayList<Node>();
		sceneGraphOperation(root, n -> {
			if (predicate.test(n)) {
				matches.add(n);
			}
		});
		return matches;
	}
	
	private boolean matches(Node node, Node other) {
		return node.getClass().isInstance(other);
	}
	
	private boolean hasNode(List<Node> nodes, Node node) {
		return nodes.stream().anyMatch(n -> matches(n, node));
	}
	
	private boolean parentMatches(Parent parent, Parent other) {
		return matches(parent, other) && parent.getChildrenUnmodifiable().stream().allMatch(n -> 
				hasNode(other.getChildrenUnmodifiable(), n));
	}
	
	private Node findNode(Node root, Predicate<Node> predicate) {
		List<Node> matches = sceneGraphSearch(root, predicate);
		return matches.isEmpty() ? null : matches.get(0);
	}
	
	public boolean matchesContent(FXOMDocument document) {
		Node contentRoot = contentGroup.getChildren().get(0);
		Node documentRoot = (Node) document.getSceneGraphRoot();
		return contentRoot.getClass().isInstance(documentRoot);
	}
	
	public Button findButton(String text) {
		return (Button) findNode(contentGroup, n -> n instanceof Button &&
				((Button) n).getText() != null && ((Button) n).getText().equals(text));
	}
	
	private Group findContentGroup() {
		return (Group) findNode(canvas.getScene().getRoot(), node -> node.getId() != null &&
				node.getId().equals("contentGroup"));
	}
	
	public boolean hasContentGroup() {
		return contentGroup != null;
	}
	
}
