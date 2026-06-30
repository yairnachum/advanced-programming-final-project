package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * Forward-Euler integrator for angular position.
 *
 * Subscribes to {@code omega} (angular velocity) and {@code dt}. On
 * each {@code omega} update, advances {@code theta} by
 * {@code omega * dt} and publishes the new value.
 */
public class PositionIntegrator implements Agent {

    private final String name;
    private final String omegaTopic;
    private final String dtTopic;
    private final String thetaTopic;

    /**
     * @param name       display name
     * @param omegaTopic angular velocity input
     * @param dtTopic    timestep input
     * @param thetaTopic angular position output (also read to obtain the previous value)
     */
    public PositionIntegrator(String name,
                              String omegaTopic, String dtTopic,
                              String thetaTopic) {
        this.name = name;
        this.omegaTopic = omegaTopic;
        this.dtTopic = dtTopic;
        this.thetaTopic = thetaTopic;

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(omegaTopic).subscribe(this);
        tm.getTopic(dtTopic).subscribe(this);
        tm.getTopic(thetaTopic).addPublisher(this);
    }

    /** {@inheritDoc} */
    public String getName() { return name; }
    /** {@inheritDoc} */
    public void reset() {}
    /** {@inheritDoc} */
    public void close() {}

    /** {@inheritDoc} Integrates a new {@code theta} on every omega update. */
    public void callback(String topic, Message msg) {
        if (!topic.equals(omegaTopic)) return;

        if (msg == null || Double.isNaN(msg.asDouble)) return;
        double omega = msg.asDouble;

        TopicManager tm = TopicManagerSingleton.get();
        double dt = read(tm, dtTopic, 0.033);
        double theta = read(tm, thetaTopic, 0.0);
        double newTheta = theta + omega * dt;
        tm.getTopic(thetaTopic).publish(new Message(newTheta));
    }

    private static double read(TopicManager tm, String topicName, double fallback) {
        Message m = tm.getTopic(topicName).lastMessage;
        if (m == null) return fallback;
        double d = m.asDouble;
        if (Double.isNaN(d)) return fallback;
        return d;
    }
}
