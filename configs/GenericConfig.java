package configs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graph.Agent;
import graph.ParallelAgent;

/**
 * Reflection-driven {@link Config} that builds an agent network from a
 * plain-text file. The file is read three lines at a time:
 * <pre>
 *   line 1: fully-qualified agent class name
 *   line 2: comma-separated subscription topic names
 *   line 3: comma-separated publication topic names
 * </pre>
 * Each created agent is wrapped in a {@link ParallelAgent} so its
 * callbacks run on a dedicated worker thread.
 */
public class GenericConfig implements Config {

    private String confFile;
    private final List<ParallelAgent> agents = new ArrayList<ParallelAgent>();

    /** Sets the path of the configuration file to load. */
    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }

    /** {@inheritDoc} Parses the configured file and instantiates the described agents. */
    @Override
    public void create() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(confFile));
        } catch (IOException e) {
            return;
        }
        List<String> nonEmpty = new ArrayList<String>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) nonEmpty.add(line.trim());
        }
        if (nonEmpty.size() % 3 != 0) return;

        for (int i = 0; i < nonEmpty.size(); i += 3) {
            String className = nonEmpty.get(i);
            String[] subs = nonEmpty.get(i + 1).split(",");
            String[] pubs = nonEmpty.get(i + 2).split(",");
            try {
                Class<?> cls = Class.forName(className);
                Constructor<?> ctor = cls.getConstructor(String[].class, String[].class);
                Agent agent = (Agent) ctor.newInstance(new Object[] { subs, pubs });
                ParallelAgent pa = new ParallelAgent(agent, 100);
                agents.add(pa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "GenericConfig";
    }

    /** {@inheritDoc} */
    @Override
    public int getVersion() {
        return 1;
    }

    /** {@inheritDoc} Closes every {@link ParallelAgent} created in {@link #create()}. */
    @Override
    public void close() {
        for (ParallelAgent pa : agents) {
            pa.close();
        }
        agents.clear();
    }
}
