# Prompt to paste into Gemini — add a summary slide

If you're still in the same Gemini conversation as the previous 3 slides, use the short version. Otherwise, use the long self-contained version.

---

## Short version (same conversation)

Please add a fourth slide to the same deck — a **summary / closing slide** — matching the same visual style as the previous three (dark navy background ~#0F172A, Hebrew RTL, emerald + violet accents, monospace font for identifiers, 16:9). Contents:

- Header (Hebrew): **מה למדתי**
- Three main takeaways as short bullet points in Hebrew:
  1. **הפרדת אחריות** — עקרון SOLID לא כתאוריה אלא ככלי מעשי לחלוקה לחבילות.
  2. **תבניות עיצוב** — Decorator, Singleton, Strategy, Template Method — פתרונות טבעיים לבעיות אמיתיות, לא כפייה על הקוד.
  3. **Concurrency נכון** — העברת הודעות (`ParallelAgent`) במקום shared state ו-locks.
- Footer line in a subtle color: **תודה על הצפייה · GitHub: yairnachum/advanced-programming-final-project**
- Small visual accent — maybe a small node-and-edge motif in the corner (three or four nodes connected by arrows, low opacity), or a checkmark cluster near each bullet.

Keep whitespace generous. This is the last thing viewers see, so it should feel calm and confident, not crowded.

---

## Long version (fresh conversation)

I need you to design **one PowerPoint / Google Slides slide in Hebrew**. Output it as either a downloadable .pptx or as a clear layout description plus full slide text I can paste into Google Slides. Use a clean modern dark theme (deep navy #0F172A background, white and off-white text, emerald #22C55E and violet #A78BFA as accent colors, monospace font for code identifiers).

### Context

The slide is the **closing slide** of a 5-minute video for a final Java project. The project (already presented in earlier slides) is a publish/subscribe computational-graph framework, plus a live pendulum-simulator extension. The video's tone is calm and technical.

### The slide

- Aspect ratio: 16:9.
- Hebrew, RTL.
- Header (big, centered near the top): **מה למדתי**
- Body: three bullet points, each with a short bold Hebrew phrase followed by a one-line explanation:
  1. **הפרדת אחריות** — עקרון SOLID לא כתאוריה אלא ככלי מעשי לחלוקה לחבילות.
  2. **תבניות עיצוב** — Decorator, Singleton, Strategy, Template Method — פתרונות טבעיים לבעיות אמיתיות.
  3. **Concurrency נכון** — העברת הודעות (`ParallelAgent`) במקום shared state ו-locks.
  (Bullet titles in emerald/violet accents; body text in off-white.)
- Footer, low-emphasis, near the bottom of the slide: **תודה על הצפייה · GitHub: yairnachum/advanced-programming-final-project**
- Optional decorative element (subtle, low opacity): a small graph motif — three or four rectangles and circles connected by arrows — in a corner, or three small emerald checkmarks aligned with the bullets.

### Style

- No stock photos, no clipart.
- Whitespace generous — this is the last thing viewers see; it should feel calm and confident.
- Keep code identifiers (`ParallelAgent`, SOLID, Decorator, etc.) in monospace / code font.
- Hebrew punctuation and spacing correct for RTL.

Please generate the slide.
