package graph;

import java.util.ArrayList;

/**
 * Named channel in the pub/sub system. Tracks the agents that
 * subscribe to (consume) it and the agents that publish to (produce)
 * it, and caches the most recently published {@link Message}.
 *
 * Instances are created via {@link TopicManagerSingleton} rather than
 * directly; the package-private constructor enforces this.
 */
public class Topic {
    /** Topic name (unique within the {@link TopicManagerSingleton}). */
    public final String name;
    /** Agents currently subscribed to this topic. */
    public final ArrayList<Agent> subs;
    /** Agents declared as publishers of this topic. */
    public final ArrayList<Agent> pubs;
    /** Most recent message published through this topic (null if none yet). */
    public volatile Message lastMessage;

    Topic(String name){
        this.name=name;
        this.subs = new ArrayList<>();
        this.pubs = new ArrayList<>();
    }

    /** Registers {@code a} as a subscriber. */
    public void subscribe(Agent a){
        subs.add(a);
    }

    /** Removes {@code a} from the subscriber list. */
    public void unsubscribe(Agent a){
        subs.remove(a);
    }

    /**
     * Caches {@code m} as the latest message and delivers it to every
     * current subscriber. Iteration is done over a snapshot so
     * subscribers may modify the subscription list during callback.
     */
    public void publish(Message m){
        this.lastMessage = m;
        for (Agent a : new ArrayList<>(subs)) {
            a.callback(name, m);
        }
    }

    /** Registers {@code a} as a publisher (for graph visualization). */
    public void addPublisher(Agent a){
        pubs.add(a);
    }

    /** Removes {@code a} from the publisher list. */
    public void removePublisher(Agent a){
        pubs.remove(a);
    }


}
