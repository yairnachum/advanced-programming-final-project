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
 * The pendulum simulator is started lazily when the user visits
 * {@code /app/pendulum.html} and stopped on the next {@code /upload}, so
 * the dashboard begins with an empty topic registry on a fresh boot.
 *
 * Press Enter on stdin to gracefully shut the server down.
 */
public class Main {
    public static void main(String[] args) throws Exception {
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
