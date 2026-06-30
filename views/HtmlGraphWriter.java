package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.Graph;
import graph.Message;
import graph.Node;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

/**
 * View-layer class that renders a Graph into HTML.
 *
 * Loads {@code html_files/graph.html} and substitutes {@code {GRAPH_SVG}}
 * with an SVG body built from the Graph. Colors match the PubSub Console
 * design system (dark OLED palette, emerald topics, violet agents,
 * amber output, rose cycles).
 *
 * Visual conventions:
 *   - Topics  : rounded rectangles, emerald accent, current value above the rect.
 *   - Agents  : circles, violet accent.
 *   - Cycle   : nodes participating in a cycle are tinted rose-red.
 *   - Edges   : curved Bezier, slate stroke with arrow markers.
 */
public class HtmlGraphWriter {

    private static final String TEMPLATE_PATH = "html_files/graph.html";
    private static final String PLACEHOLDER = "{GRAPH_SVG}";

    /**
     * Renders {@code g} using each topic's current {@link Topic#lastMessage}
     * as the per-topic value chip.
     *
     * @return a single-element list whose entry is the rendered HTML
     */
    public static List<String> getGraphHTML(Graph g) {
        Map<String, Message> values = new HashMap<String, Message>();
        TopicManager tm = TopicManagerSingleton.get();
        for (Topic t : tm.getTopics()) {
            if (t.lastMessage != null) values.put(t.name, t.lastMessage);
        }
        return getGraphHTML(g, values);
    }

    /**
     * Renders {@code g} using the explicit map of topic-name to value
     * for the displayed chips.
     *
     * @return a single-element list whose entry is the rendered HTML
     */
    public static List<String> getGraphHTML(Graph g, Map<String, Message> values) {
        String svg = buildSvg(g, values);
        String template = loadTemplate();
        String filled = template.replace(PLACEHOLDER, svg);
        List<String> out = new ArrayList<String>();
        out.add(filled);
        return out;
    }

    private static String loadTemplate() {
        try {
            Path p = Paths.get(TEMPLATE_PATH);
            if (Files.exists(p)) {
                return new String(Files.readAllBytes(p), "UTF-8");
            }
        } catch (IOException ignored) {}
        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Graph</title></head>"
             + "<body style=\"background:#020617;color:#F8FAFC\">" + PLACEHOLDER + "</body></html>";
    }

    private static String buildSvg(Graph g, Map<String, Message> values) {
        if (g == null || g.isEmpty()) {
            return "<div style=\"padding:60px;text-align:center;color:#64748B;font-family:'Fira Sans',sans-serif;font-size:13px;font-style:italic\">"
                 + "no graph loaded yet — deploy a configuration to begin"
                 + "</div>";
        }

        int n = g.size();
        int width = 760, height = 540;
        int cx = width / 2, cy = height / 2;
        int radius = Math.min(width, height) / 2 - 90;

        Map<Node, int[]> pos = new HashMap<Node, int[]>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            int x = cx + (int) (radius * Math.cos(angle));
            int y = cy + (int) (radius * Math.sin(angle));
            pos.put(g.get(i), new int[]{x, y});
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<svg width=\"").append(width).append("\" height=\"").append(height)
          .append("\" viewBox=\"0 0 ").append(width).append(' ').append(height)
          .append("\" xmlns=\"http://www.w3.org/2000/svg\" style=\"display:block;margin:0 auto\">");

        // Defs: drop shadow + arrow markers (slate/red)
        sb.append("<defs>");
        sb.append("<filter id=\"node-shadow\" x=\"-50%\" y=\"-50%\" width=\"200%\" height=\"200%\">")
          .append("<feDropShadow dx=\"0\" dy=\"3\" stdDeviation=\"4\" flood-color=\"#000\" flood-opacity=\"0.55\"/>")
          .append("</filter>");
        sb.append("<marker id=\"arrow\" viewBox=\"0 0 10 10\" refX=\"10\" refY=\"5\" ")
          .append("markerWidth=\"7\" markerHeight=\"7\" orient=\"auto-start-reverse\">")
          .append("<path d=\"M 0 0 L 10 5 L 0 10 z\" fill=\"#64748B\"/></marker>");
        sb.append("<marker id=\"arrow-cycle\" viewBox=\"0 0 10 10\" refX=\"10\" refY=\"5\" ")
          .append("markerWidth=\"7\" markerHeight=\"7\" orient=\"auto-start-reverse\">")
          .append("<path d=\"M 0 0 L 10 5 L 0 10 z\" fill=\"#EF4444\"/></marker>");
        sb.append("</defs>");

        // Edges (curved Bezier)
        for (Node node : g) {
            int[] p1 = pos.get(node);
            if (p1 == null) continue;
            boolean cyclic = node.hasCycles();
            for (Node target : node.getEdges()) {
                int[] p2 = pos.get(target);
                if (p2 == null) continue;
                double dx = p2[0] - p1[0], dy = p2[1] - p1[1];
                double len = Math.sqrt(dx*dx + dy*dy);
                if (len < 1) continue;
                double ux = dx / len, uy = dy / len;
                int sx = p1[0] + (int)(ux * 32);
                int sy = p1[1] + (int)(uy * 32);
                int ex = p2[0] - (int)(ux * 32);
                int ey = p2[1] - (int)(uy * 32);
                int mx = (sx + ex) / 2, my = (sy + ey) / 2;
                int cx2 = mx + (cx - mx) / 6;
                int cy2 = my + (cy - my) / 6;
                String stroke = cyclic ? "#EF4444" : "#64748B";
                String marker = cyclic ? "arrow-cycle" : "arrow";
                sb.append("<path d=\"M ").append(sx).append(' ').append(sy)
                  .append(" Q ").append(cx2).append(' ').append(cy2)
                  .append(' ').append(ex).append(' ').append(ey)
                  .append("\" fill=\"none\" stroke=\"").append(stroke).append("\" ")
                  .append("stroke-width=\"1.8\" stroke-linecap=\"round\" ")
                  .append("marker-end=\"url(#").append(marker).append(")\"/>");
            }
        }

        // Nodes
        for (Node node : g) {
            int[] p = pos.get(node);
            if (p == null) continue;
            String name = node.getName();
            String label = (name.length() > 1) ? name.substring(1) : name;
            boolean cyclic = node.hasCycles();
            boolean isTopic = name.startsWith("T");

            // Topic value chip above the rectangle
            if (isTopic) {
                Message msg = values != null ? values.get(label) : null;
                if (msg != null) {
                    String val = msg.asText;
                    int chipW = Math.max(38, val.length() * 8 + 16);
                    sb.append("<g>");
                    sb.append("<rect x=\"").append(p[0] - chipW/2).append("\" y=\"").append(p[1] - 52)
                      .append("\" width=\"").append(chipW).append("\" height=\"22\" rx=\"11\" ry=\"11\" ")
                      .append("fill=\"#0E1223\" stroke=\"#334155\" stroke-width=\"1\"")
                      .append(" filter=\"url(#node-shadow)\"/>");
                    sb.append("<text x=\"").append(p[0]).append("\" y=\"").append(p[1] - 36)
                      .append("\" text-anchor=\"middle\" font-size=\"11\" font-weight=\"600\" ")
                      .append("fill=\"#22C55E\" font-family=\"'Fira Code',ui-monospace,monospace\">")
                      .append(escape(val)).append("</text>");
                    sb.append("</g>");
                }
            }

            String fill, stroke;
            if (cyclic) {
                fill = "rgba(239,68,68,0.18)"; stroke = "#EF4444";
            } else if (isTopic) {
                fill = "rgba(34,197,94,0.14)"; stroke = "#22C55E";
            } else {
                fill = "rgba(167,139,250,0.14)"; stroke = "#A78BFA";
            }

            if (isTopic) {
                sb.append("<rect x=\"").append(p[0] - 32).append("\" y=\"").append(p[1] - 20)
                  .append("\" width=\"64\" height=\"40\" rx=\"8\" ry=\"8\" ")
                  .append("fill=\"").append(fill).append("\" stroke=\"").append(stroke)
                  .append("\" stroke-width=\"1.6\" filter=\"url(#node-shadow)\"/>");
            } else {
                sb.append("<circle cx=\"").append(p[0]).append("\" cy=\"").append(p[1])
                  .append("\" r=\"30\" fill=\"").append(fill).append("\" stroke=\"").append(stroke)
                  .append("\" stroke-width=\"1.6\" filter=\"url(#node-shadow)\"/>");
            }
            sb.append("<text x=\"").append(p[0]).append("\" y=\"").append(p[1] + 4)
              .append("\" text-anchor=\"middle\" font-size=\"12\" font-weight=\"600\" ")
              .append("font-family=\"'Fira Code',ui-monospace,monospace\" ")
              .append("fill=\"#F8FAFC\">")
              .append(escape(label)).append("</text>");
        }

        sb.append("</svg>");

        if (g.hasCycles()) {
            sb.append("<div style=\"margin:14px auto;padding:10px 14px;background:rgba(239,68,68,.10);")
              .append("border:1px solid rgba(239,68,68,.40);color:#FCA5A5;font-size:12px;")
              .append("border-radius:8px;max-width:520px;font-family:'Fira Sans',sans-serif;letter-spacing:.02em\">")
              .append("<strong style=\"color:#EF4444;font-family:'Fira Code',monospace;letter-spacing:.08em\">CYCLE DETECTED</strong> &nbsp;&middot;&nbsp;")
              .append("nodes participating are highlighted in red.")
              .append("</div>");
        }

        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
