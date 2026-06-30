package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * Forward-Euler integrator for angular velocity.
 *
 * Subscribes to {@code alpha} (angular acceleration) and {@code dt}
 * (timestep). On each {@code alpha} update, reads the current
 * {@code omega} from its topic, advances it by {@code alpha * dt},
 * and publishes the new value.
 *
 * Subscribing to {@code dt} is for graph completeness; the value is
 * fetched from the topic on demand.
 */
public class VelocityIntegrator implements Agent {

    private final String name;
    private final String alphaTopic;
    private final String dtTopic;
    private final String omegaTopic;

    /**
     * @param name       display name
     * @param alphaTopic angular acceleration input
     * @param dtTopic    timestep input (read on demand)
     * @param omegaTopic angular velocity output (also read to obtain the previous value)
     */
    public VelocityIntegrator(String name,
                              String alphaTopic, String dtTopic,
                              String omegaTopic) {
        this.name = name;
        this.alphaTopic = alphaTopic;
        this.dtTopic = dtTopic;
        this.omegaTopic = omegaTopic;

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(alphaTopic).subscribe(this);
        tm.getTopic(dtTopic).subscribe(this);
        tm.getTopic(omegaTopic).addPublisher(this);
    }

    /** {@inheritDoc} */
    public String getName() { return name; }
    /** {@inheritDoc} */
    public void reset() {}
    /** {@inheritDoc} */
    public void close() {}

    /** {@inheritDoc} Integrates a new {@code omega} on every alpha update. */
    public void callback(String topic, Message msg) {
        if (!topic.equals(alphaTopic)) return; // dt is read on demand

        if (msg == null || Double.isNaN(msg.asDouble)) return;
        double alpha = msg.asDouble;

        TopicManager tm = TopicManagerSingleton.get();
        double dt = read(tm, dtTopic, 0.033);
        double omega = read(tm, omegaTopic, 0.0);
        double newOmega = omega + alpha * dt;
        tm.getTopic(omegaTopic).publish(new Message(newOmega));
    }

    private static double read(TopicManager tm, String topicName, double fallback) {
        Message m = tm.getTopic(topicName).lastMessage;
        if (m == null) return fallback;
        double d = m.asDouble;
        if (Double.isNaN(d)) return fallback;
        return d;
    }
}
