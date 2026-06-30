package servlets;

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;

/**
 * Handler for a routed HTTP request. Implementations write a complete
 * HTTP response (status line, headers, body) to the supplied output
 * stream.
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
