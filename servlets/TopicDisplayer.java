package servlets;

import java.io.IOException;
import java.io.OutputStream;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import server.RequestParser.RequestInfo;

/**
 * Servlet for GET /publish.
 *
 * If the request includes both topic and message parameters, publishes
 * the message to the named topic via TopicManagerSingleton. Either way,
 * the response is an HTML fragment listing every topic and its most
 * recent published value (read directly from {@link Topic#lastMessage}).
 *
 * Loads the PubSub Console design tokens via /app/theme.css so the
 * table inherits dark-mode typography and colors.
 */
public class TopicDisplayer implements Servlet {

    /** {@inheritDoc} */
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        TopicManager tm = TopicManagerSingleton.get();

        String topicName = ri.getParameters().get("topic");
        String message = ri.getParameters().get("message");

        if (topicName != null && message != null) {
            tm.getTopic(topicName).publish(new Message(message));
        }

        StringBuilder body = new StringBuilder();
        body.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">");
        body.append("<link rel=\"stylesheet\" href=\"/app/theme.css\">");
        body.append("<style>");
        body.append("body{padding:14px;min-height:100vh}");
        body.append(".tbl-card{border:1px solid var(--border);border-radius:var(--radius);overflow:hidden;background:var(--surface)}");
        body.append(".tbl-head{display:flex;align-items:center;gap:8px;padding:10px 14px;background:var(--surface-2);border-bottom:1px solid var(--border);font-family:var(--font-mono);font-size:var(--text-xs);letter-spacing:.14em;text-transform:uppercase;color:var(--text-muted)}");
        body.append(".tbl-head .dot{width:6px;height:6px;border-radius:50%;background:var(--output);box-shadow:0 0 6px var(--output-glow);animation:led-pulse 1.6s ease infinite}");
        body.append(".tbl-head .count{margin-left:auto;color:var(--text-faint);font-size:var(--text-xs)}");
        body.append(".empty{padding:32px 16px;text-align:center;color:var(--text-faint);font-size:var(--text-sm);font-style:italic}");
        body.append("</style></head><body>");

        long count = 0;
        for (Topic t : tm.getTopics()) count++;

        body.append("<div class=\"tbl-card\">");
        body.append("<div class=\"tbl-head\"><span class=\"dot\"></span><span>topic values</span><span class=\"count\">")
            .append(count).append(" registered</span></div>");

        if (count == 0) {
            body.append("<div class=\"empty\">no topics yet — publish a value or deploy a configuration</div>");
        } else {
            body.append("<table class=\"data-table\">");
            body.append("<thead><tr><th>Topic</th><th>Last Message</th></tr></thead>");
            body.append("<tbody>");
            for (Topic t : tm.getTopics()) {
                Message m = t.lastMessage;
                body.append("<tr><td>").append(escape(t.name)).append("</td>");
                if (m != null) {
                    body.append("<td class=\"value\">").append(escape(m.asText)).append("</td>");
                } else {
                    body.append("<td class=\"value empty\">&mdash;</td>");
                }
                body.append("</tr>");
            }
            body.append("</tbody></table>");
        }
        body.append("</div></body></html>");

        String content = body.toString();
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Content-Type: text/html; charset=utf-8\r\n");
        response.append("Content-Length: ").append(content.getBytes("UTF-8").length).append("\r\n");
        response.append("\r\n");
        response.append(content);

        toClient.write(response.toString().getBytes("UTF-8"));
        toClient.flush();
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
