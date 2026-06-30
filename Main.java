import configs.ClockAgent;
import configs.PositionIntegrator;
import configs.TorqueAgent;
import configs.VelocityIntegrator;
import graph.Message;
import graph.TopicManagerSingleton;
import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.GraphRefresh;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import servlets.TopicStateServlet;

/**
 * Application entry point. Wires up the MyHTTPServer with three servlets:
 *   GET  /publish - publishes a message to a topic and renders the table
 *   POST /upload  - accepts a configuration file and renders the graph
 *   GET  /app/    - serves static files from the html_files directory
 *
 * Press Enter on stdin to gracefully shut the server down.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // ===== Pendulum simulator: 4 agents wired into a physics pipeline. =====
        // Seed initial conditions first so the agents have well-defined inputs.
        TopicManagerSingleton.get().getTopic("gravity").publish(new Message(9.8));
        TopicManagerSingleton.get().getTopic("length").publish(new Message(1.0));
        TopicManagerSingleton.get().getTopic("damping").publish(new Message(0.10));
        TopicManagerSingleton.get().getTopic("dt").publish(new Message(0.033));
        TopicManagerSingleton.get().getTopic("theta").publish(new Message(0.7));
        TopicManagerSingleton.get().getTopic("omega").publish(new Message(0.0));

        // TorqueAgent     : alpha = -(g/L)*sin(theta) - damping*omega
        new TorqueAgent("torque",
                "tick", "theta", "omega", "gravity", "length", "damping", "alpha");
        // VelocityIntegrator: omega += alpha * dt
        new VelocityIntegrator("v_int", "alpha", "dt", "omega");
        // PositionIntegrator: theta += omega * dt
        new PositionIntegrator("p_int", "omega", "dt", "theta");
        // ClockAgent: ticks every ~33ms (30 Hz). Drives the whole pipeline.
        new ClockAgent("clock", "tick", 33);

        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/graph", new GraphRefresh());
        server.addServlet("GET", "/state", new TopicStateServlet());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.start();
        System.out.println("Server running on http://localhost:8080/app/index.html  (press Enter to stop)");
        System.in.read();
        server.close();
        System.out.println("done");
    }
}
