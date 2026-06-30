package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide registry of {@link Topic}s. Exposed only as a singleton
 * to ensure every agent across the system publishes and subscribes
 * against the same topic instances.
 */
public class TopicManagerSingleton {

    /** Holds the registry of named topics. Accessed via {@link TopicManagerSingleton#get()}. */
    public static class TopicManager{
        private static final TopicManager instance = new TopicManager();
        private final ConcurrentHashMap<String, Topic> topics;

        private TopicManager() {
            this.topics = new ConcurrentHashMap<>();
        }

        /** Returns the topic with the given name, creating it on first request. */
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        /** Returns a live view of every registered topic. */
        public Collection<Topic> getTopics() {
            return topics.values();
        }

        /** Removes every registered topic. */
        public void clear() {
            topics.clear();
        }
    }

    /** Returns the singleton {@link TopicManager}. */
    public static TopicManager get(){
        return TopicManager.instance;
    }
}
