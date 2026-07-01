# Prompt to paste into Gemini

Copy everything below the line into Gemini. It's fully self-contained — no prior context about the project or course is needed.

---

I need you to create **three PowerPoint / Google Slides slides in Hebrew** for a final project presentation. Output them as either (a) a downloadable .pptx file, or (b) if you can't produce a file, give me each slide as a clear layout description plus full slide text I can paste into Google Slides myself. Use a clean modern dark theme (deep navy / slate background, white and cyan text, monospace font for code-like terms).

## Context you need

**Course:** תכנות מתקדם (Advanced Programming), Open University of Israel, taught by ד"ר אליהו חלסצ'י.
**Submitter (one person):** יאיר נחום, ת.ז. 322270646, מייל yairnachum@gmail.com.
**Project name:** PubSub Computational Graph — Final Project (Exercise 6).
**GitHub:** https://github.com/yairnachum/advanced-programming-final-project

## What the project is (background)

The project is a Java framework for building and visualising **computational graphs** on top of a **publish/subscribe** core. The core abstractions are:
- **Topic** — a named channel that holds a value; agents publish to it and subscribe from it.
- **Agent** — a component that reads inputs from topics, performs a computation, and publishes the result to another topic.
- Connecting agents through shared topics forms a directed graph — the "computational graph" — which the system draws live in a browser dashboard.

The framework is fully implemented from scratch in Java 8, without any external libraries. Even the HTTP server, request parser, and thread pool are handwritten.

Configurations are loaded dynamically from `.conf` files via reflection: the file lists agent class names and their input/output topics, and `GenericConfig` instantiates them at runtime.

As an **extension beyond the required scope**, the project also includes a real-time **pendulum simulator** built entirely from four pub/sub agents: `ClockAgent` (source, publishes ticks 30 times per second), `TorqueAgent` (computes angular acceleration α = −(g/L)·sin(θ) − b·ω), `VelocityIntegrator` (Forward-Euler on ω), `PositionIntegrator` (Forward-Euler on θ). This demonstrates the framework's expressive power — non-trivial physics falls naturally out of the pub/sub model with zero shared mutable state.

## The design in detail

Code is organised into **five packages**, each with a single responsibility (per the Single-Responsibility principle of SOLID):

| Package | Role | Key classes |
|---|---|---|
| `graph` | Pub/sub core + graph model | `Agent`, `Topic`, `Message`, `TopicManagerSingleton`, `Node`, `Graph`, `ParallelAgent`, `BinOpAgent`, `PlusAgent`, `IncAgent` |
| `configs` | Config interface + concrete agents (incl. pendulum) | `Config`, `GenericConfig`, `ClockAgent`, `TorqueAgent`, `VelocityIntegrator`, `PositionIntegrator`, `Pendulum` |
| `server` | Minimal HTTP server | `HTTPServer`, `MyHTTPServer`, `RequestParser` |
| `servlets` | HTTP request handlers | `Servlet`, `ConfLoader`, `HtmlLoader`, `TopicDisplayer`, `TopicStateServlet`, `GraphRefresh` |
| `views` | HTML/SVG rendering | `HtmlGraphWriter` |

Design patterns used:
- **Singleton** — `TopicManagerSingleton` (global topic registry).
- **Decorator** — `ParallelAgent` wraps any `Agent` and drains its work off a `BlockingQueue<Message>` on a dedicated worker thread; implements the Active Object concurrency pattern.
- **Strategy** — the `Config` interface lets any config source (file, code, DSL) plug into the same loader.
- **Template Method** — `Servlet` interface with `handle(request, out)`.
- **Model / View / Controller (MVC)** — `graph` = Model, `html_files/*.html` + `views` = View, `servlets` = Controller.

Concurrency: publishes are synchronous by default; `ParallelAgent` is the opt-in escape hatch so a slow subscriber never blocks the publisher.

Frontend: dark-mode OLED aesthetic, Fira Sans + Fira Code typography, live SVG graph regenerated server-side, AJAX polling every 45 ms for jitter-free updates.

## The three slides I need

### Slide 1 — Title / Cover
- Big title (Hebrew): **פרויקט תכנות מתקדם — גרף חישובי מבוסס Publish/Subscribe**
- Subtitle: **פרויקט סופי — תרגיל 6**
- Course info block: קורס תכנות מתקדם, האוניברסיטה הפתוחה, מרצה: ד"ר אליהו חלסצ'י
- Submitter block: מגיש: יאיר נחום · ת.ז. 322270646 · yairnachum@gmail.com
- Small footer: GitHub URL from above
- Visual: subtle background — maybe abstract node-and-edge lines, dots, or a network graph pattern in low opacity.

### Slide 2 — סיפור הרקע (Background Story)
- Header: **סיפור הרקע**
- Two-column layout:
  - Left column: 3–4 bullet points in Hebrew explaining the problem and idea:
    - הצורך במסגרת פשוטה לבניית מערכות מקבילות מבוססות אירועים.
    - הרעיון: Topics מחזיקים ערכים, Agents מגיבים ומפרסמים — החיבור ביניהם יוצר גרף חישובי.
    - היעד: מסגרת שמאפשרת להגדיר גרפים דינמית (קובץ .conf), להריץ אותם במקביל, ולהמחיש אותם בזמן אמת בדפדפן.
    - הרחבה: סימולטור מטוטלת פיזיקלי הבנוי כולו מ-4 סוכני pub/sub, כדי להוכיח את עוצמת המודל.
  - Right column: a small diagram showing `Topic A → Agent → Topic B → Agent → Topic C`, with rectangles for topics and circles for agents. Use emerald green for topics and violet for agents (matches the actual dashboard palette).

### Slide 3 — Design של הפרויקט
- Header: **Design של הפרויקט**
- Top: a horizontal architecture diagram with the 5 package boxes in order: **graph → configs → server → servlets → views**, plus **Main** as the entry point on the left, arrows showing dependency direction.
- Bottom left: **תבניות עיצוב (Design Patterns)** — list of the 5 patterns above (Singleton, Decorator, Strategy, Template Method, MVC), each with the class name that implements it in parentheses.
- Bottom right: **עקרונות מפתח** — 3 bullets:
  - **SOLID** — כל חבילה עם אחריות אחת ברורה.
  - **Java 8 clean** — ללא תלות חיצונית, שרת HTTP מאפס.
  - **Concurrency-safe** — `ParallelAgent` כעטיפה לפי דפוס Active Object.

## Style requirements
- Use Hebrew (RTL) throughout. English is only for code identifiers (class names, package names, `Topic`, `Agent`, etc.) which should stay in monospace font.
- No stock photos. Diagrams should be simple geometric shapes (rectangles + circles + arrows).
- Consistent color palette: dark navy background (~#0F172A), text mostly white/off-white, accents in emerald (#22C55E) for topics and violet (#A78BFA) for agents, cyan for links/highlights.
- Slide 2 and Slide 3 should feel visually consistent with each other and with Slide 1.
- Fit within 16:9 aspect ratio, avoid overcrowding, prefer whitespace.

Please generate the slides.
