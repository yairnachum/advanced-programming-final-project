package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * Background tick generator. Runs a daemon thread that publishes a
 * monotonically increasing counter to the {@code tickTopic}. The publish
 * interval is derived from a Hz value supplied on the {@code rateTopic};
 * publishing a new rate at runtime smoothly changes the cadence on the
 * next iteration of the tick loop.
 *
 * Drives the pendulum integration loop: every tick fires
 * {@link TorqueAgent}, which cascades through the rest of the
 * computational graph.
 */
public class ClockAgent implements Agent {

    private final String name;
    private final String tickTopic;
    private final String rateTopic;
    private volatile long intervalMs;
    private final Thread worker;
    private volatile boolean running = true;
    private volatile long counter = 0;

    /**
     * @param name       display name for the agent
     * @param tickTopic  topic on which ticks are published
     * @param rateTopic  topic carrying the desired rate, in Hz; the clock
     *                   subscribes and re-reads its interval whenever this
     *                   topic publishes a new value
     * @param initialHz  starting rate used until {@code rateTopic} receives
     *                   its first message
     */
    public ClockAgent(String name, String tickTopic, String rateTopic, double initialHz) {
        this.name = name;
        this.tickTopic = tickTopic;
        this.rateTopic = rateTopic;
        this.intervalMs = hzToInterval(initialHz);

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(tickTopic).addPublisher(this);
        tm.getTopic(rateTopic).subscribe(this);

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

    /**
     * {@inheritDoc} Updates the publish interval when {@code rateTopic}
     * receives a new Hz value.
     */
    public void callback(String topic, Message msg) {
        if (!rateTopic.equals(topic)) return;
        double hz = msg.asDouble;
        if (Double.isNaN(hz)) return;
        intervalMs = hzToInterval(hz);
    }

    /** Convert Hz to a millisecond interval, clamped to a sane band. */
    private static long hzToInterval(double hz) {
        if (hz <= 0) return 1000;
        long ms = Math.round(1000.0 / hz);
        if (ms < 5)    ms = 5;
        if (ms > 2000) ms = 2000;
        return ms;
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
