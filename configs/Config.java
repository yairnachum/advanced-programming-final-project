package configs;

/**
 * Loadable description of a pub/sub computational graph. Implementations
 * wire up agents and topics in {@link #create()} and release any
 * acquired resources in {@link #close()}.
 */
public interface Config {
    /** Instantiates the agents and wires them to their topics. */
    void create();

    /** Returns the configuration's display name. */
    String getName();

    /** Returns the configuration's version number. */
    int getVersion();

    /** Releases any resources held by the configuration. */
    void close();
}
