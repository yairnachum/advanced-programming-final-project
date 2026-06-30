package servlets;

import java.io.IOException;
import java.io.OutputStream;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import server.RequestParser.RequestInfo;

/**
 * Generic state-snapshot servlet. Returns a JSON object mapping every
 * topic name to its most recent numeric value (or {@code null} for
 * topics that have not yet been published to or whose values are not
 * numeric).
 *
 * Used by browser-side visualizations (pendulum, etc.) that poll the
 * current pub/sub state to drive their UI.
 *
 * Hand-rolled JSON serialization (no Gson dependency).
 */
public class TopicStateServlet implements Servlet {

    /** {@inheritDoc} */
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        TopicManager tm = TopicManagerSingleton.get();
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Topic t : tm.getTopics()) {
            if (!first) json.append(',');
            first = false;
            json.append('"').append(jsonEscape(t.name)).append("\":");
            Message m = t.lastMessage;
            if (m == null) {
                json.append("null");
            } else {
                double d = m.asDouble;
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    json.append("null");
                } else {
                    json.append(formatDouble(d));
                }
            }
        }
        json.append('}');

        byte[] bytes = json.toString().getBytes("UTF-8");
        StringBuilder hdr = new StringBuilder();
        hdr.append("HTTP/1.1 200 OK\r\n");
        hdr.append("Content-Type: application/json; charset=utf-8\r\n");
        hdr.append("Cache-Control: no-store\r\n");
        hdr.append("Content-Length: ").append(bytes.length).append("\r\n");
        hdr.append("\r\n");
        toClient.write(hdr.toString().getBytes("UTF-8"));
        toClient.write(bytes);
        toClient.flush();
    }

    /** {@inheritDoc} */
    public void close() throws IOException {}

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String formatDouble(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e16) {
            return Long.toString((long) d);
        }
        return Double.toString(d);
    }
}
