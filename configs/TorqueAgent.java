package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * Computes angular acceleration of a simple pendulum.
 *
 * On every {@code tick}, reads the current values of {@code theta},
 * {@code omega}, {@code gravity}, {@code length}, and {@code damping}
 * from their respective topics (via {@link graph.Topic#lastMessage}) and
 * publishes {@code alpha = -(g/L)*sin(theta) - damping*omega} to the
 * {@code alphaTopic}.
 *
 * Subscribes to all input topics for graph-wiring purposes; only the
 * tick event actually triggers a computation. This keeps the graph
 * visualization complete without creating a publish loop.
 */
public class TorqueAgent implements Agent {

    private final String name;
    private final String tickTopic;
    private final String thetaTopic;
    private final String omegaTopic;
    private final String gravityTopic;
    private final String lengthTopic;
    private final String dampingTopic;
    private final String alphaTopic;

    /**
     * @param name         display name
     * @param tickTopic    topic whose updates trigger a torque computation
     * @param thetaTopic   current angle (rad)
     * @param omegaTopic   current angular velocity (rad/s)
     * @param gravityTopic gravitational acceleration (m/s^2); falls back to 9.8
     * @param lengthTopic  pendulum length (m); falls back to 1.0
     * @param dampingTopic damping coefficient; falls back to 0.0
     * @param alphaTopic   topic to publish the computed angular acceleration on
     */
    public TorqueAgent(String name,
                       String tickTopic,
                       String thetaTopic, String omegaTopic,
                       String gravityTopic, String lengthTopic, String dampingTopic,
                       String alphaTopic) {
        this.name = name;
        this.tickTopic = tickTopic;
        this.thetaTopic = thetaTopic;
        this.omegaTopic = omegaTopic;
        this.gravityTopic = gravityTopic;
        this.lengthTopic = lengthTopic;
        this.dampingTopic = dampingTopic;
        this.alphaTopic = alphaTopic;

        TopicManager tm = TopicManagerSingleton.get();
        // Subscribe to ALL inputs (for graph completeness) but only act on tick.
        tm.getTopic(tickTopic).subscribe(this);
        tm.getTopic(thetaTopic).subscribe(this);
        tm.getTopic(omegaTopic).subscribe(this);
        tm.getTopic(gravityTopic).subscribe(this);
        tm.getTopic(lengthTopic).subscribe(this);
        tm.getTopic(dampingTopic).subscribe(this);
        tm.getTopic(alphaTopic).addPublisher(this);
    }

    /** {@inheritDoc} */
    public String getName() { return name; }

    /** {@inheritDoc} */
    public void reset() {}

    /** {@inheritDoc} */
    public void close() {}

    /** {@inheritDoc} Computes and publishes angular acceleration on every tick. */
    public void callback(String topic, Message msg) {
        if (!topic.equals(tickTopic)) return; // ignore non-tick events

        TopicManager tm = TopicManagerSingleton.get();
        double theta   = read(tm, thetaTopic,   0.0);
        double omega   = read(tm, omegaTopic,   0.0);
        double gravity = read(tm, gravityTopic, 9.8);
        double length  = read(tm, lengthTopic,  1.0);
        double damping = read(tm, dampingTopic, 0.0);

        // Equation of motion for a damped simple pendulum
        double alpha = -(gravity / length) * Math.sin(theta) - damping * omega;
        tm.getTopic(alphaTopic).publish(new Message(alpha));
    }

    private static double read(TopicManager tm, String topicName, double fallback) {
        Message m = tm.getTopic(topicName).lastMessage;
        if (m == null) return fallback;
        double d = m.asDouble;
        if (Double.isNaN(d)) return fallback;
        return d;
    }
}
