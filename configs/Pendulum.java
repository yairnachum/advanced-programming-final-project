package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;

/**
 * Lifecycle manager for the pendulum simulator pipeline.
 *
 * The pendulum is built out of four agents (clock, torque, two integrators)
 * wired through six topics. Instead of booting at startup and polluting the
 * topic registry for every dashboard view, the pendulum is started lazily
 * (the first time {@code /app/pendulum.html} is served) and stopped when
 * the user uploads a configuration through {@code /upload}.
 *
 * Both {@link #start()} and {@link #stop()} are idempotent and safe to
 * call from any thread.
 */
public final class Pendulum {

    private static Agent clock;
    private static Agent torque;
    private static Agent vInt;
    private static Agent pInt;
    private static boolean running = false;

    private Pendulum() {}

    /**
     * Seed the pendulum's parameter topics with their initial values and
     * spin up the four agents. No-op if the pendulum is already running.
     */
    public static synchronized void start() {
        if (running) return;
        TopicManagerSingleton.get().getTopic("gravity").publish(new Message(9.8));
        TopicManagerSingleton.get().getTopic("length").publish(new Message(1.0));
        TopicManagerSingleton.get().getTopic("damping").publish(new Message(0.10));
        TopicManagerSingleton.get().getTopic("dt").publish(new Message(0.033));
        TopicManagerSingleton.get().getTopic("theta").publish(new Message(0.7));
        TopicManagerSingleton.get().getTopic("omega").publish(new Message(0.0));

        torque = new TorqueAgent("torque",
                "tick", "theta", "omega", "gravity", "length", "damping", "alpha");
        vInt = new VelocityIntegrator("v_int", "alpha", "dt", "omega");
        pInt = new PositionIntegrator("p_int", "omega", "dt", "theta");
        clock = new ClockAgent("clock", "tick", "rate", 30.0);

        // Publish the initial rate AFTER the ClockAgent subscribes, so the
        // dashboard's topic table picks it up and a future slider change can
        // flow through the same publish path.
        TopicManagerSingleton.get().getTopic("rate").publish(new Message(30.0));
        running = true;
    }

    /**
     * Stop all four agents (including the ClockAgent daemon thread) and
     * release references. No-op if the pendulum is not running.
     *
     * Topics are not cleared here — the caller is expected to call
     * {@code TopicManagerSingleton.get().clear()} when it wants a
     * fresh registry.
     */
    public static synchronized void stop() {
        if (!running) return;
        if (clock != null) clock.close();
        if (torque != null) torque.close();
        if (vInt != null) vInt.close();
        if (pInt != null) pInt.close();
        clock = torque = vInt = pInt = null;
        running = false;
    }

    /** @return {@code true} if the pendulum pipeline is currently active. */
    public static synchronized boolean isRunning() {
        return running;
    }
}
