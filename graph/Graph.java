package graph;

import java.util.ArrayList;
import java.util.HashMap;

import graph.TopicManagerSingleton.TopicManager;

/**
 * Computational graph built from the current pub/sub topology.
 * Extends {@link ArrayList} of {@link Node}s for convenient iteration.
 * Nodes are named {@code T<topic>} for topics and {@code A<agent>} for
 * agents; edges go topic-to-subscriber and publisher-to-topic.
 */
public class Graph extends ArrayList<Node>{

    /** Returns {@code true} if any node in the graph participates in a cycle. */
    public boolean hasCycles() {
        for (Node n : this) {
            if (n.hasCycles()) return true;
        }
        return false;
    }

    /**
     * Rebuilds this graph by walking every topic in the
     * {@link TopicManagerSingleton} and adding nodes/edges for every
     * topic-subscriber and publisher-topic relationship.
     */
    public void createFromTopics(){
        TopicManager tm = TopicManagerSingleton.get();
        HashMap<String, Node> nodes = new HashMap<String, Node>();

        for (Topic topic : tm.getTopics()) {
            String topicNodeName = "T" + topic.name;
            Node topicNode = nodes.get(topicNodeName);
            if (topicNode == null) {
                topicNode = new Node(topicNodeName);
                nodes.put(topicNodeName, topicNode);
            }
            for (Agent a : topic.subs) {
                String agentNodeName = "A" + a.getName();
                Node agentNode = nodes.get(agentNodeName);
                if (agentNode == null) {
                    agentNode = new Node(agentNodeName);
                    nodes.put(agentNodeName, agentNode);
                }
                topicNode.addEdge(agentNode);
            }
            for (Agent a : topic.pubs) {
                String agentNodeName = "A" + a.getName();
                Node agentNode = nodes.get(agentNodeName);
                if (agentNode == null) {
                    agentNode = new Node(agentNodeName);
                    nodes.put(agentNodeName, agentNode);
                }
                agentNode.addEdge(topicNode);
            }
        }
        this.clear();
        this.addAll(nodes.values());
    }


}
