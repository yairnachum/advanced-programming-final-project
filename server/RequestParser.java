package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-rolled HTTP request reader. Parses the method, URI, query-string
 * parameters, any body lines that look like {@code key=value}, and the
 * remaining body content into a {@link RequestInfo}.
 */
public class RequestParser {

    /**
     * Reads one HTTP request from {@code reader} and returns its
     * structured form, or {@code null} if the stream is empty or
     * malformed.
     *
     * @param reader stream positioned at the start of the request line
     * @return parsed request, or {@code null}
     * @throws IOException if the underlying reader fails
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine == null || firstLine.isEmpty()) return null;

        String[] parts = firstLine.split(" ");
        if (parts.length < 2) return null;
        String httpCommand = parts[0];
        String uri = parts[1];

        String pathOnly = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
        List<String> segments = new ArrayList<String>();
        for (String s : pathOnly.split("/")) {
            if (!s.isEmpty()) segments.add(s);
        }
        String[] uriSegments = segments.toArray(new String[0]);

        Map<String, String> parameters = new HashMap<String, String>();
        if (uri.contains("?")) {
            String query = uri.substring(uri.indexOf("?") + 1);
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    parameters.put(kv[0], kv[1]);
                }
            }
        }

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // consume header lines
        }

        // Use reader.ready() so a real socket (no more bytes to send until response) doesn't block.
        while (reader.ready()) {
            line = reader.readLine();
            if (line == null || line.isEmpty()) break;
            String[] kv = line.split("=", 2);
            if (kv.length == 2) {
                parameters.put(kv[0], kv[1]);
            }
        }

        StringBuilder content = new StringBuilder();
        while (reader.ready()) {
            line = reader.readLine();
            if (line == null || line.isEmpty()) break;
            content.append(line).append("\n");
        }

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content.toString().getBytes());
    }

	/**
	 * Immutable snapshot of a parsed HTTP request, exposed to servlets
	 * through {@code servlets.Servlet#handle}.
	 */
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        /**
         * @param httpCommand HTTP method (GET, POST, DELETE, ...)
         * @param uri         raw request URI including any query string
         * @param uriSegments path segments split on {@code '/'}, empty segments removed
         * @param parameters  query-string and body {@code key=value} pairs
         * @param content     remaining request body bytes
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        /** Returns the HTTP method. */
        public String getHttpCommand() {
            return httpCommand;
        }

        /** Returns the raw request URI. */
        public String getUri() {
            return uri;
        }

        /** Returns the non-empty path segments. */
        public String[] getUriSegments() {
            return uriSegments;
        }

        /** Returns the merged map of query-string and body parameters. */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /** Returns any body bytes beyond the {@code key=value} parameter lines. */
        public byte[] getContent() {
            return content;
        }
    }
}
