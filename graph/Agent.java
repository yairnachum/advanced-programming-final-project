package graph;

/**
 * Participant in the pub/sub computational graph. An agent subscribes
 * to zero or more input topics and may publish to zero or more output
 * topics. Implementations encapsulate the unit of work performed when
 * an input message arrives.
 */
public interface Agent {
    /** Returns the agent's display name (used as the graph node label). */
    String getName();

    /** Resets any internal state held by the agent. */
    void reset();

    /**
     * Invoked by {@link Topic#publish(Message)} when a subscribed
     * topic receives a new message.
     *
     * @param topic the topic that produced the message
     * @param msg   the published message
     */
    void callback(String topic, Message msg);

    /** Releases any resources held by the agent (threads, sockets, etc.). */
    void close();
}
