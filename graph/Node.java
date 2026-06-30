package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * A node in the computational graph. Holds an outgoing edge list and
 * an optional {@link Message} associated with the node (typically a
 * topic's most recent value).
 */
public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    /** Creates a node with the given display name and no outgoing edges. */
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<Node>();
    }

    /** Returns the node's display name. */
    public String getName() {
        return name;
    }

    /** Sets the node's display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the live list of outgoing edges. */
    public List<Node> getEdges() {
        return edges;
    }

    /** Replaces the outgoing edge list. */
    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    /** Returns the message currently associated with this node, or {@code null}. */
    public Message getMsg() {
        return msg;
    }

    /** Associates a message with this node. */
    public void setMsg(Message msg) {
        this.msg = msg;
    }

    /** Adds an outgoing edge to {@code node}. */
    public void addEdge(Node node) {
        edges.add(node);
    }

    /** Returns {@code true} if any cycle in the graph passes through this node. */
    public boolean hasCycles() {
        return hasCyclesHelper(new HashSet<Node>(), new HashSet<Node>());
    }

    private boolean hasCyclesHelper(HashSet<Node> visited, HashSet<Node> stack) {
        if (stack.contains(this)) return true;
        if (visited.contains(this)) return false;
        visited.add(this);
        stack.add(this);
        for (Node next : edges) {
            if (next.hasCyclesHelper(visited, stack)) return true;
        }
        stack.remove(this);
        return false;
    }
}
