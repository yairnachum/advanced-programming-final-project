/**
 * Minimal embedded HTTP/1.1 server with pluggable request handlers.
 *
 * <p>The package provides:
 * <ul>
 *   <li>{@link server.HTTPServer} &mdash; the server contract (start, stop, register handlers).</li>
 *   <li>{@link server.MyHTTPServer} &mdash; a thread-pooled implementation that accepts
 *       TCP connections, parses each request with {@link server.RequestParser},
 *       and dispatches to the {@link servlets.Servlet} whose registered URI is the
 *       longest prefix of the request URI for the matching HTTP method.</li>
 *   <li>{@link server.RequestParser} &mdash; parses an HTTP request into a
 *       {@link server.RequestParser.RequestInfo} snapshot.</li>
 * </ul>
 *
 * <p>The server has no external dependencies &mdash; only {@code java.*}.
 *
 * <h2>Complete usage example</h2>
 *
 * <p>Wire up a handler for {@code GET /hello}, start the server, block until the
 * user hits Enter, then shut it down cleanly.
 *
 * <pre>{@code
 * import server.HTTPServer;
 * import server.MyHTTPServer;
 * import server.RequestParser.RequestInfo;
 * import servlets.Servlet;
 *
 * import java.io.OutputStream;
 *
 * public class Demo {
 *     public static void main(String[] args) throws Exception {
 *         // 1. Create a server on port 8080 with 4 worker threads.
 *         HTTPServer server = new MyHTTPServer(8080, 4);
 *
 *         // 2. Register a servlet for GET requests whose URI starts with "/hello".
 *         server.addServlet("GET", "/hello", new Servlet() {
 *             public void handle(RequestInfo ri, OutputStream out) throws java.io.IOException {
 *                 String body = "Hello, " + ri.getUri();
 *                 String response =
 *                     "HTTP/1.1 200 OK\r\n" +
 *                     "Content-Type: text/plain\r\n" +
 *                     "Content-Length: " + body.length() + "\r\n" +
 *                     "\r\n" +
 *                     body;
 *                 out.write(response.getBytes());
 *             }
 *             public void close() {}
 *         });
 *
 *         // 3. Start the accept loop on a background thread.
 *         server.start();
 *
 *         // 4. Block until the user presses Enter, then shut down.
 *         System.in.read();
 *         server.close();
 *     }
 * }
 * }</pre>
 *
 * @see server.HTTPServer
 * @see server.MyHTTPServer
 * @see server.RequestParser
 * @see servlets.Servlet
 */
package server;
