package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Decorator that runs a wrapped {@link Agent}'s callbacks on a single
 * dedicated worker thread. Incoming messages are queued so publishers
 * never block on the wrapped agent's processing.
 */
public class ParallelAgent implements Agent {

    private final Agent agent;
    private final BlockingQueue<Message> queue;
    private final Thread worker;
    private volatile boolean running = true;

    private static class TopicMessage extends Message {
        final String topic;
        final Message original;
        TopicMessage(String topic, Message original) {
            super("");
            this.topic = topic;
            this.original = original;
        }
    }

    /**
     * @param agent    the wrapped agent whose callbacks are run on the worker thread
     * @param capacity bounded queue capacity for pending messages
     */
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.worker = new Thread(() -> {
            while (running) {
                try {
                    Message m = queue.take();
                    if (!running) break;
                    if (m instanceof TopicMessage) {
                        TopicMessage tm = (TopicMessage) m;
                        agent.callback(tm.topic, tm.original);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        worker.start();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return agent.getName();
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        agent.reset();
    }

    /** {@inheritDoc} Enqueues the message for asynchronous processing. */
    @Override
    public void callback(String topic, Message msg) {
        try {
            queue.put(new TopicMessage(topic, msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** {@inheritDoc} Stops the worker thread and closes the wrapped agent. */
    @Override
    public void close() {
        running = false;
        worker.interrupt();
        agent.close();
    }
}
