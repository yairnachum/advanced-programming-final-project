package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import graph.Graph;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

/**
 * Servlet for GET /graph. Returns the current computational graph rendered
 * as HTML/SVG, reflecting whatever topics and agents are currently
 * registered with TopicManagerSingleton. The graph nodes include each
 * topic's latest published value (via HtmlGraphWriter).
 *
 * This endpoint is read-only — it does not modify any state — so it is
 * safe to poll on a short interval for a live view.
 */
public class GraphRefresh implements Servlet {

    /** {@inheritDoc} */
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        Graph g = new Graph();
        g.createFromTopics();
        List<String> lines = HtmlGraphWriter.getGraphHTML(g);
        StringBuilder sb = new StringBuilder();
        for (String l : lines) sb.append(l);
        byte[] htmlBytes = sb.toString().getBytes("UTF-8");

        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 200 OK\r\n");
        header.append("Content-Type: text/html; charset=utf-8\r\n");
        header.append("Content-Length: ").append(htmlBytes.length).append("\r\n");
        header.append("\r\n");
        toClient.write(header.toString().getBytes("UTF-8"));
        toClient.write(htmlBytes);
        toClient.flush();
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }
}
