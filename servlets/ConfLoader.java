package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import configs.GenericConfig;
import configs.Pendulum;
import graph.Graph;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

/**
 * Servlet for POST /upload. Receives a configuration file as the request
 * content, clears the current TopicManager state, loads the configuration
 * via GenericConfig, builds the resulting Graph, and returns its HTML
 * rendering (delegated to HtmlGraphWriter).
 *
 * Expected request format produced by from.html: body lines beginning with
 * {@code filename=...\n\n}, followed by the conf body. The conf body itself
 * ends up in {@code ri.getContent()} as plain text.
 */
public class ConfLoader implements Servlet {

    /** {@inheritDoc} */
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        byte[] content = ri.getContent();
        String body = content != null ? new String(content) : "";

        // Stop the pendulum first (if running) so its ClockAgent thread
        // doesn't keep republishing to "tick" after the registry is cleared.
        Pendulum.stop();
        TopicManagerSingleton.get().clear();

        String responseHtml;
        if (body.isEmpty()) {
            responseHtml = "<html><body><h3>No configuration content received.</h3></body></html>";
        } else {
            Path tempFile = Files.createTempFile("uploaded", ".conf");
            try {
                Files.write(tempFile, body.getBytes());
                GenericConfig gc = new GenericConfig();
                gc.setConfFile(tempFile.toString());
                gc.create();

                Graph g = new Graph();
                g.createFromTopics();

                List<String> lines = HtmlGraphWriter.getGraphHTML(g);
                StringBuilder sb = new StringBuilder();
                for (String l : lines) sb.append(l);
                responseHtml = sb.toString();
            } finally {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }

        byte[] htmlBytes = responseHtml.getBytes("UTF-8");
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
