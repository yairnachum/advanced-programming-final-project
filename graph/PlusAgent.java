package graph;

import graph.TopicManagerSingleton.TopicManager;

/**
 * Agent that publishes the sum of its two subscribed inputs every time
 * either input updates. Subscribes to {@code subs[0]} and {@code subs[1]}
 * and publishes to {@code pubs[0]}.
 */
public class PlusAgent implements Agent {

    private final String[] subs;
    private final String[] pubs;
    private double x = 0.0;
    private double y = 0.0;

    /**
     * @param subs topic names to read; index 0 is {@code x}, index 1 is {@code y}
     * @param pubs topic names to publish to; index 0 is the sum
     */
    public PlusAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length > 0) tm.getTopic(subs[0]).subscribe(this);
        if (subs.length > 1) tm.getTopic(subs[1]).subscribe(this);
        if (pubs.length > 0) tm.getTopic(pubs[0]).addPublisher(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "PlusAgent";
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        x = 0.0;
        y = 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) return;
        if (subs.length > 0 && topic.equals(subs[0])) {
            x = msg.asDouble;
        } else if (subs.length > 1 && topic.equals(subs[1])) {
            y = msg.asDouble;
        }
        if (pubs.length > 0) {
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(x + y));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }
}
