package server;

import servlets.Servlet;

/**
 * Minimal HTTP server contract. Listens on a port, routes incoming
 * requests to {@link Servlet}s keyed by HTTP method and URI prefix,
 * and runs as a {@link Runnable} so it can be started on its own
 * thread.
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
