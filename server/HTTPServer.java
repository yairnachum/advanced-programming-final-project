package server;

import servlets.Servlet;

/**
 * Minimal HTTP server contract. Listens on a port, routes incoming
 * requests to {@link Servlet}s keyed by HTTP method and URI prefix,
 * and runs as a {@link Runnable} so it can be started on its own
 * thread.
 *
 * <p>Routing rule: for each incoming request the server picks the
 * registered servlet whose URI is the <em>longest prefix</em> of the
 * request URI, restricted to the request's HTTP method. Only
 * {@code GET}, {@code POST} and {@code DELETE} are routed.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Instantiate an implementation (e.g. MyHTTPServer) via the interface.
 * HTTPServer server = new MyHTTPServer(8080, 4);
 *
 * // Register handlers by (method, URI-prefix).
 * server.addServlet("GET",    "/app/",   new HtmlLoader("html_files"));
 * server.addServlet("POST",   "/upload", new ConfLoader());
 * server.addServlet("DELETE", "/topic",  new MyDeleteServlet());
 *
 * // Start the accept loop (implementations typically run it on a
 * // background thread) and block main until Enter is pressed.
 * server.start();
 * System.in.read();
 *
 * // Graceful shutdown: closes the listening socket, every registered
 * // servlet, and any worker pool.
 * server.close();
 * }</pre>
 *
 * @see MyHTTPServer
 * @see RequestParser
 * @see Servlet
 */
public interface HTTPServer extends Runnable{
    /** Registers {@code s} to handle requests matching {@code httpCommanmd} + {@code uri} prefix. */
    public void addServlet(String httpCommanmd, String uri, Servlet s);

    /** Removes the servlet registered for the given method and URI prefix. */
    public void removeServlet(String httpCommanmd, String uri);

    /** Starts the server (typically on a background thread). */
    public void start();

    /** Stops accepting connections and releases server resources. */
    public void close();
}
