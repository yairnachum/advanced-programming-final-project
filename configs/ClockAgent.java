package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * Background tick generator. Runs a daemon thread that publishes a
 * monotonically increasing counter to the {@code tickTopic} at
 * {@code intervalMs} intervals.
 *
 * Drives the pendulum integration loop: every tick fires
 * {@link TorqueAgent}, which cascades through the rest of the
 * computational graph.
 */
public class ClockAgent implements Agent {

    private final String name;
    private final String tickTopic;
    private final long intervalMs;
    private final Thread worker;
    private volatile boolean running = true;
    private volatile long counter = 0;

    /**
     * @param name       display name for the agent
     * @param tickTopic  topic on which ticks are published
     * @param intervalMs delay between ticks, in milliseconds
     */
    public ClockAgent(String name, String tickTopic, long intervalMs) {
        this.name = name;
        this.tickTopic = tickTopic;
        this.intervalMs = intervalMs;

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(tickTopic).addPublisher(this);

        worker = new Thread(new Runnable() {
            public void run() { tick(); }
        }, "clock-" + name);
        worker.setDaemon(true);
        worker.start();
    }

    /** {@inheritDoc} */
    public String getName() { return name; }

    /** {@inheritDoc} Resets the tick counter to zero. */
    public void reset() { counter = 0; }

    /** {@inheritDoc} Stops the tick thread. */
    public void close() {
        running = false;
        worker.interrupt();
    }

    /** {@inheritDoc} No-op: {@code ClockAgent} is a source and ignores incoming messages. */
    public void callback(String topic, Message msg) {
        // ClockAgent is a source: it has no inputs.
    }

    private void tick() {
        TopicManager tm = TopicManagerSingleton.get();
        while (running) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                break;
            }
            counter++;
            tm.getTopic(tickTopic).publish(new Message((double) counter));
        }
    }
}
