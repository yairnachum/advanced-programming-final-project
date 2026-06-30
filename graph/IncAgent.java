package graph;

import graph.TopicManagerSingleton.TopicManager;

/**
 * Agent that publishes {@code input + 1} whenever its single input
 * topic produces a numeric message.
 */
public class IncAgent implements Agent {

    private final String[] subs;
    private final String[] pubs;

    /**
     * @param subs topic names to subscribe to; only index 0 is read
     * @param pubs topic names to publish to; only index 0 is written
     */
    public IncAgent(String[] subs, String[] pubs) {
        this.subs = subs;
        this.pubs = pubs;
        TopicManager tm = TopicManagerSingleton.get();
        if (subs.length > 0) tm.getTopic(subs[0]).subscribe(this);
        if (pubs.length > 0) tm.getTopic(pubs[0]).addPublisher(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "IncAgent";
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
    }

    /** {@inheritDoc} */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) return;
        if (pubs.length > 0) {
            TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(msg.asDouble + 1));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }
}
