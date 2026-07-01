/**
 * Request handlers for the embedded {@link server.HTTPServer}.
 *
 * <p>The package provides:
 * <ul>
 *   <li>{@link servlets.Servlet} &mdash; the interface every handler implements.</li>
 *   <li>{@link servlets.HtmlLoader} &mdash; serves static files from a directory.</li>
 *   <li>{@link servlets.ConfLoader} &mdash; accepts a multipart {@code POST} of a
 *       configuration file and deploys it as a computational graph.</li>
 *   <li>{@link servlets.TopicDisplayer} &mdash; publishes a value to a topic and
 *       returns the updated topic table.</li>
 *   <li>{@link servlets.TopicStateServlet} &mdash; JSON snapshot of every topic's
 *       last value; used by the dashboard's polling loop.</li>
 *   <li>{@link servlets.GraphRefresh} &mdash; regenerates the graph SVG.</li>
 * </ul>
 *
 * <h2>Writing your own Servlet</h2>
 *
 * <p>A {@code Servlet} owns the entire HTTP response: it must write the status
 * line, headers, blank line, and body directly to the {@code OutputStream}.
 * Query-string parameters and body {@code key=value} pairs are already parsed
 * into {@link server.RequestParser.RequestInfo#getParameters()}.
 *
 * <pre>{@code
 * package myapp;
 *
 * import server.RequestParser.RequestInfo;
 * import servlets.Servlet;
 *
 * import java.io.IOException;
 * import java.io.OutputStream;
 *
 * public class EchoServlet implements Servlet {
 *     public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
 *         String name = ri.getParameters().get("name");
 *         if (name == null) name = "world";
 *
 *         String body = "Hello, " + name + "!";
 *         byte[] bytes = body.getBytes("UTF-8");
 *
 *         String headers =
 *             "HTTP/1.1 200 OK\r\n" +
 *             "Content-Type: text/plain; charset=utf-8\r\n" +
 *             "Content-Length: " + bytes.length + "\r\n" +
 *             "\r\n";
 *
 *         toClient.write(headers.getBytes("UTF-8"));
 *         toClient.write(bytes);
 *     }
 *
 *     public void close() throws IOException {
 *         // release resources here (files, sockets, ...)
 *     }
 * }
 * }</pre>
 *
 * <p>Register the handler with the server:
 *
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 4);
 * server.addServlet("GET", "/echo", new EchoServlet());
 * server.start();
 * }</pre>
 *
 * @see servlets.Servlet
 * @see server.HTTPServer
 * @see server.RequestParser.RequestInfo
 */
package servlets;
