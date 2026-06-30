# PubSub Computational Graph

Final project for *Advanced Programming* (תכנות מתקדם) — Dr. Eliyahu Khalastchi.

A Java framework for building and visualising **computational graphs** over a publish/subscribe core. Topics carry values; Agents subscribe to topics, transform their inputs, and publish back. Everything runs concurrently behind a tiny embedded HTTP server with a live dark‑mode web dashboard.

The final exercise (Ex6) also includes a real‑time **pendulum simulator** built entirely out of pub/sub agents, used as the project's demo of what the framework can express.

---

## Screenshots

> _Screenshots will be added below — placeholders for now._

**Main dashboard** — controls, computational graph, live topic values:

<!-- SCREENSHOT_DASHBOARD -->
_(screenshot pending)_

**Configuration deploy + graph visualisation:**

<!-- SCREENSHOT_GRAPH -->
_(screenshot pending)_

**Pendulum simulator (extension):**

<!-- SCREENSHOT_PENDULUM -->
_(screenshot pending)_

---

## Quick start

Requires JDK 8+. No external dependencies — the project uses only `java.*`.

```bash
# from project root
javac -d out $(find graph configs server servlets views -name '*.java') Main.java
java -cp out Main
```

Then open <http://localhost:8080/app/index.html>.

Press **Enter** in the terminal where `java` is running to shut the server down gracefully.

### Try it
1. In the **Controls** pane, click **Deploy** and upload `simple.conf` (a tiny `A + B → C; C+1 → D` graph).
2. Publish values to topics `A` and `B` via the **Publish** form.
3. Watch the graph view and the topic table update.
4. Visit <http://localhost:8080/app/pendulum.html> for the live pendulum simulator.

---

## Architecture

Five packages, each with a single responsibility:

| Package | Responsibility | Key types |
|---|---|---|
| `graph` | Pub/sub core + graph model | `Agent`, `Topic`, `Message`, `TopicManagerSingleton`, `Node`, `Graph`, `ParallelAgent` |
| `configs` | Config interface + concrete agents | `Config`, `GenericConfig`, `BinOpAgent`, `IncAgent`, `PlusAgent`, `ClockAgent`, `TorqueAgent`, integrators |
| `server` | Minimal HTTP server | `HTTPServer`, `MyHTTPServer`, `RequestParser` |
| `servlets` | Request handlers | `Servlet`, `ConfLoader`, `HtmlLoader`, `TopicDisplayer`, `TopicStateServlet`, `GraphRefresh` |
| `views` | HTML/SVG rendering | `HtmlGraphWriter` |

`Main` lives in the default package and just wires the server's routing table.

### How the pub/sub layer works

- A **Topic** is a named channel. Agents `subscribe(topic)` to listen and `publish(msg)` to push.
- An **Agent** implements `callback(String topic, Message msg)`. When a publish happens, every subscriber's callback is fired.
- `TopicManagerSingleton` is the global registry — `getTopic(name)` lazily creates topics on demand.
- `ParallelAgent` is a **Decorator** that wraps any Agent and runs its callback on its own worker thread, so a slow agent can't stall the publisher. Internally an `ArrayBlockingQueue<Message>` holds pending work; a private `TopicMessage` subclass bundles `(topic, original)` so the queue stays typed as `BlockingQueue<Message>`.

### How a config becomes a graph

`GenericConfig` reads a text file in groups of three lines (`fully.qualified.AgentClass`, comma‑separated input topics, comma‑separated output topics) and instantiates each agent via reflection. The `Graph` class then walks topics & agents to produce a `List<Node>` where edges go _topic → agent_ for inputs and _agent → topic_ for outputs. `Node.hasCycles()` (BFS over edges) lets the view layer mark cyclic components in red.

### How the HTTP layer works

`MyHTTPServer` is a tiny thread‑pooled HTTP/1.1 server (no framework). `RequestParser` reads the request line, headers, and (for multipart uploads) the file body. The router maps `(METHOD, longest‑URI‑prefix) → Servlet`. Each servlet writes its HTTP response straight to the socket.

The dashboard polls `/state` over AJAX every 45 ms for smooth updates without iframe flicker. The graph SVG is regenerated server‑side on every `/graph` hit, using values read from `Topic.lastMessage`.

---

## Extension — pendulum simulator

`Main` boots an extra physics pipeline made of four agents:

```
gravity ┐
length  ├──► TorqueAgent ──► alpha ──► VelocityIntegrator ──► omega ──► PositionIntegrator ──► theta
damping ┘                              ▲                                ▲
tick (30 Hz from ClockAgent) ──────────┴────────────────────────────────┘
```

- `TorqueAgent` computes α = −(g/L)·sin θ − b·ω each tick.
- `VelocityIntegrator` does Forward‑Euler on ω: `ω += α·dt`.
- `PositionIntegrator` does the same on θ.
- `ClockAgent` ticks 30 times a second on a daemon thread.

The result is a fully pub/sub‑driven physics simulation. The web view (`pendulum.html`) polls `/state` and renders the bob, velocity arrow, trail, and energy readout.

---

## Project layout

```
.
├── Main.java                # entry point (default package)
├── graph/                   # pub/sub core
├── configs/                 # Config interface + concrete agents
├── server/                  # embedded HTTP server
├── servlets/                # request handlers
├── views/                   # SVG/HTML rendering
├── html_files/              # static dashboard (theme.css, index.html, etc.)
├── simple.conf              # example PlusAgent + IncAgent config
└── README.md
```

---

## Design notes

- **SOLID / patterns:** Decorator (`ParallelAgent`), Singleton (`TopicManagerSingleton`), Strategy (`Config`), Template Method (`Servlet`), MVC (graph model / html_files view / servlets controller).
- **Concurrency:** publishes are synchronous by default. `ParallelAgent` is the opt‑in escape hatch; the dashboard wraps all dynamically loaded agents in one so the UI never blocks.
- **Java 8 source level:** no records, no `var`, no pattern‑matching `instanceof` — keeps compatibility with the course's grader toolchain.
- **Dark‑mode UI:** consolidated design tokens in `html_files/theme.css` (Fira Sans + Fira Code, OLED slate surfaces, emerald topics / violet agents / amber outputs). WCAG AAA contrast.

---

## Author

Yair Nachum — yairnachum@gmail.com
