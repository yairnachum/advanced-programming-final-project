package servlets;

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;

/**
 * Handler for a routed HTTP request. Implementations write a complete
 * HTTP response (status line, headers, body) to the supplied output
 * stream.
 *
 * <p>The server dispatches to a servlet only after it has already
 * parsed the request into a {@link RequestInfo}, so implementations do
 * not need to touch the raw socket beyond writing the response.
 *
 * <h2>Minimal implementation</h2>
 *
 * <pre>{@code
 * import server.RequestParser.RequestInfo;
 * import servlets.Servlet;
 *
 * import java.io.IOException;
 * import java.io.OutputStream;
 *
 * public class PingServlet implements Servlet {
 *     public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
 *         String body = "pong";
 *         String resp =
 *             "HTTP/1.1 200 OK\r\n" +
 *             "Content-Type: text/plain\r\n" +
 *             "Content-Length: " + body.length() + "\r\n" +
 *             "\r\n" +
 *             body;
 *         toClient.write(resp.getBytes());
 *     }
 *
 *     public void close() throws IOException {
 *         // no resources to release
 *     }
 * }
 * }</pre>
 *
 * <p>Register it against a running server:
 *
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 4);
 * server.addServlet("GET", "/ping", new PingServlet());
 * server.start();
 * }</pre>
 *
 * @see server.HTTPServer#addServlet(String, String, Servlet)
 * @see server.RequestParser.RequestInfo
 */
public interface Servlet {
    /**
     * Handles one request.
     *
     * @param ri        parsed request
     * @param toClient  stream to which the HTTP response must be written
     * @throws IOException on I/O failure
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;

    /** Releases any resources held by the servlet. */
    void close() throws IOException;
}
