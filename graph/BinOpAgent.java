package graph;

import java.util.function.BinaryOperator;

import graph.TopicManagerSingleton.TopicManager;

/**
 * Generic two-input agent that applies a {@link BinaryOperator} to its
 * inputs and publishes the result once both inputs have been received.
 */
public class BinOpAgent implements Agent {

    private final String name;
    private final String input1Name;
    private final String input2Name;
    private final String outputName;
    private final BinaryOperator<Double> op;
    private Double input1;
    private Double input2;

    /**
     * @param name        display name
     * @param input1Name  first input topic
     * @param input2Name  second input topic
     * @param outputName  output topic
     * @param op          binary operation applied to the two inputs
     */
    public BinOpAgent(String name,
                      String input1Name,
                      String input2Name,
                      String outputName,
                      BinaryOperator<Double> op) {
        this.name = name;
        this.input1Name = input1Name;
        this.input2Name = input2Name;
        this.outputName = outputName;
        this.op = op;
        this.input1 = null;
        this.input2 = null;

        TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(input1Name).subscribe(this);
        tm.getTopic(input2Name).subscribe(this);
        tm.getTopic(outputName).addPublisher(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        input1 = 0.0;
        input2 = 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) return;
        if (topic.equals(input1Name)) {
            input1 = msg.asDouble;
        } else if (topic.equals(input2Name)) {
            input2 = msg.asDouble;
        }
        if (input1 != null && input2 != null) {
            double result = op.apply(input1, input2);
            TopicManagerSingleton.get().getTopic(outputName).publish(new Message(result));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }
}
