package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import configs.Pendulum;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;

/**
 * Servlet for GET /app/. Serves static HTML files from a configurable
 * directory (passed via the constructor, so the resource folder is NOT
 * hard-coded inside the servlet).
 *
 * Maps URI segments after "app" to a relative path inside the directory:
 *   /app/index.html       -> &lt;dir&gt;/index.html
 *   /app/sub/page.html    -> &lt;dir&gt;/sub/page.html
 */
public class HtmlLoader implements Servlet {

    private final String dir;

    /**
     * @param dir base directory containing the static files to serve
     */
    public HtmlLoader(String dir) {
        this.dir = dir;
    }

    /** {@inheritDoc} */
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String[] segments = ri.getUriSegments();

        StringBuilder rel = new StringBuilder();
        for (int i = 1; i < segments.length; i++) {
            if (i > 1) rel.append("/");
            rel.append(segments[i]);
        }
        String relativePath = rel.length() == 0 ? "index.html" : rel.toString();

        Path file = Paths.get(dir, relativePath).normalize();
        Path base = Paths.get(dir).toAbsolutePath().normalize();

        // Reject paths trying to escape the served folder
        if (!file.toAbsolutePath().normalize().startsWith(base)) {
            writeNotFound(toClient);
            return;
        }

        if (!Files.exists(file) || Files.isDirectory(file)) {
            writeNotFound(toClient);
            return;
        }

        // Lazy-boot the pendulum pipeline the first time its page is served.
        // Keeps the main dashboard registry empty until the user explicitly
        // opens the pendulum simulator. On the reverse direction — navigating
        // back to the main console — stop the pendulum and clear its topics
        // so the dashboard's topic table doesn't keep showing pendulum state.
        // We only clear when the pendulum was the thing producing topics; if
        // the user had a configuration loaded, leave it untouched.
        if (relativePath.endsWith("pendulum.html")) {
            Pendulum.start();
        } else if (relativePath.endsWith("index.html") && Pendulum.isRunning()) {
            Pendulum.stop();
            TopicManagerSingleton.get().clear();
        }

        byte[] content = Files.readAllBytes(file);
        String contentType = guessContentType(relativePath);

        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 200 OK\r\n");
        header.append("Content-Type: ").append(contentType).append("\r\n");
        header.append("Content-Length: ").append(content.length).append("\r\n");
        header.append("\r\n");
        toClient.write(header.toString().getBytes("UTF-8"));
        toClient.write(content);
        toClient.flush();
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }

    private void writeNotFound(OutputStream toClient) throws IOException {
        String body = "<!DOCTYPE html><html><body><h2>404 Not Found</h2></body></html>";
        byte[] bytes = body.getBytes("UTF-8");
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 404 Not Found\r\n");
        header.append("Content-Type: text/html; charset=utf-8\r\n");
        header.append("Content-Length: ").append(bytes.length).append("\r\n");
        header.append("\r\n");
        toClient.write(header.toString().getBytes("UTF-8"));
        toClient.write(bytes);
        toClient.flush();
    }

    private String guessContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
