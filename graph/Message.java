package graph;

import java.util.Date;

/**
 * Immutable value passed between agents through topics. A message
 * carries the same payload in three forms (raw bytes, text, and a
 * parsed {@code double}) so subscribers can read whichever
 * representation they need without re-parsing.
 */
public class Message {
    /** Payload as raw bytes (defensive copy when constructed from a byte array). */
    public final byte[] data;
    /** Payload as text. */
    public final String asText;
    /** Payload parsed as a {@code double}, or {@link Double#NaN} if not numeric. */
    public final double asDouble;
    /** Wall-clock time at which this message was constructed. */
    public final Date date;

    /** Builds a message from the given text. */
    public Message(String text) {
        this.asText = text;
        this.data = text.getBytes();
        double parsed;
        try {
            parsed = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            parsed = Double.NaN;
        }
        this.asDouble = parsed;
        this.date = new Date();
    }

    /** Builds a message from raw bytes; the array is cloned. */
    public Message(byte[] data) {
        this.data = data.clone();
        this.asText = new String(data);
        double parsed;
        try {
            parsed = Double.parseDouble(this.asText);
        } catch (NumberFormatException e) {
            parsed = Double.NaN;
        }
        this.asDouble = parsed;
        this.date = new Date();
    }

    /** Builds a message from a numeric value. */
    public Message(double value) {
        this(Double.toString(value));
    }
}
