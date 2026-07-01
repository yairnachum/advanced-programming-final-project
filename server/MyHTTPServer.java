package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.RequestParser.RequestInfo;
import servlets.Servlet;


/**
 * Thread-based {@link HTTPServer} implementation. Listens on a TCP
 * port, parses incoming requests via {@link RequestParser}, and
 * dispatches each one to the {@link Servlet} whose registered URI is
 * the longest prefix of the request URI for the matching HTTP method.
 * Per-client work runs on a fixed-size thread pool.
 *
 * <p>The class extends {@link Thread}: calling {@link #start()} launches
 * the accept loop on that thread; the caller may then block on user
 * input (or any other latch) and finally call {@link #close()} to
 * release the socket, worker pool, and every registered
 * {@link Servlet}.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // 1. Server on port 8080 with 4 worker threads.
 * MyHTTPServer server = new MyHTTPServer(8080, 4);
 *
 * // 2. Register handlers. Longest URI prefix wins per method.
 * server.addServlet("GET",  "/app/",   new HtmlLoader("html_files"));
 * server.addServlet("POST", "/publish", new TopicDisplayer());
 *
 * // 3. Fire the accept loop on a background thread.
 * server.start();
 *
 * // 4. Block main until the operator hits Enter, then shut down.
 * System.in.read();
 * server.close();
 * }</pre>
 *
 * @see HTTPServer
 * @see RequestParser
 * @see Servlet
 */
public class MyHTTPServer extends Thread implements HTTPServer{

    private final int port;
    private final int nThreads;
    private final Map<String, Servlet> getServlets;
    private final Map<String, Servlet> postServlets;
    private final Map<String, Servlet> deleteServlets;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    /**
     * @param port     TCP port to listen on
     * @param nThreads worker-thread pool size for handling clients
     */
    public MyHTTPServer(int port,int nThreads){
        this.port = port;
        this.nThreads = nThreads;
        this.getServlets = new ConcurrentHashMap<String, Servlet>();
        this.postServlets = new ConcurrentHashMap<String, Servlet>();
        this.deleteServlets = new ConcurrentHashMap<String, Servlet>();
    }

    /** {@inheritDoc} */
    public void addServlet(String httpCommanmd, String uri, Servlet s){
        Map<String, Servlet> map = mapFor(httpCommanmd);
        if (map != null) map.put(uri, s);
    }

    /** {@inheritDoc} */
    public void removeServlet(String httpCommanmd, String uri){
        Map<String, Servlet> map = mapFor(httpCommanmd);
        if (map != null) map.remove(uri);
    }

    private Map<String, Servlet> mapFor(String httpCommand) {
        if (httpCommand == null) return null;
        String c = httpCommand.toUpperCase();
        if (c.equals("GET")) return getServlets;
        if (c.equals("POST")) return postServlets;
        if (c.equals("DELETE")) return deleteServlets;
        return null;
    }

    /** Accept loop: blocks on the server socket and dispatches each client to the worker pool. */
    public void run(){
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            threadPool = Executors.newFixedThreadPool(nThreads);

            while (running) {
                try {
                    final Socket client = serverSocket.accept();
                    threadPool.execute(new Runnable() {
                        public void run() {
                            handleClient(client);
                        }
                    });
                } catch (SocketTimeoutException e) {
                    // re-check running flag
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket client) {
        BufferedReader reader = null;
        OutputStream output = null;
        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = client.getOutputStream();
            RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri != null) {
                Map<String, Servlet> map = mapFor(ri.getHttpCommand());
                if (map != null) {
                    Servlet servlet = findLongestMatch(map, ri.getUri());
                    if (servlet != null) {
                        servlet.handle(ri, output);
                    }
                }
            }
        } catch (Exception e) {
            // swallow per-client errors so they don't kill the worker
        } finally {
            try { if (output != null) output.flush(); } catch (IOException e) {}
            try { client.close(); } catch (IOException e) {}
        }
    }

    private Servlet findLongestMatch(Map<String, Servlet> servlets, String uri) {
        String longestKey = null;
        for (String key : servlets.keySet()) {
            if (uri.startsWith(key)) {
                if (longestKey == null || key.length() > longestKey.length()) {
                    longestKey = key;
                }
            }
        }
        return longestKey == null ? null : servlets.get(longestKey);
    }

    /** {@inheritDoc} Closes the server socket, every registered servlet, and the worker pool. */
    public void close(){
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {}

        closeAll(getServlets);
        closeAll(postServlets);
        closeAll(deleteServlets);

        if (threadPool != null) threadPool.shutdownNow();
    }

    private void closeAll(Map<String, Servlet> servlets) {
        for (Servlet s : servlets.values()) {
            try { s.close(); } catch (IOException e) {}
        }
    }

}
