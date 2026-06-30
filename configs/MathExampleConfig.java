package configs;

import graph.BinOpAgent;

/**
 * Demo configuration that wires three {@link BinOpAgent}s into a small
 * arithmetic graph: {@code R1 = A + B}, {@code R2 = A - B},
 * {@code R3 = R1 * R2}.
 */
public class MathExampleConfig implements Config {

    /** {@inheritDoc} */
    @Override
    public void create() {
        new BinOpAgent("plus", "A", "B", "R1", (x,y)->x+y);
        new BinOpAgent("minus", "A", "B", "R2", (x,y)->x-y);
        new BinOpAgent("mul", "R1", "R2", "R3", (x,y)->x*y);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Math Example";
    }

    /** {@inheritDoc} */
    @Override
    public int getVersion() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }

}
