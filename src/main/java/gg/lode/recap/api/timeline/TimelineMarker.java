package gg.lode.recap.api.timeline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Something worth noting that happened at a point in a match, written by whoever knows it happened.
 *
 * <p>Recap records what a player did, not what any of it meant. Which blow was a kill, when a round
 * turned, who captured what, that's the game's knowledge. Add markers while the match runs, read them
 * back at playback, and Recap stamps the tick so they line up with the footage.
 *
 * <h2>Why it's a type plus a map, not something you extend</h2>
 *
 * <p>Markers get written to a file and read back elsewhere, often on a replay server that doesn't
 * have the writing plugin installed. A subclass can't be read back without its class, so typed
 * markers would either fail to load or force every reader to ship every writer's plugin. Recap also
 * hands its API to implementations child-first, which is exactly where two copies of one class stop
 * being the same class.
 *
 * <p>So the stored shape is deliberately dull: a {@code type} you pick, and a map of strings. Want a
 * typed view? Define one in your own plugin. It costs a constructor and keeps the coupling on your
 * side of the line:
 *
 * <pre>{@code
 * record KillMarker(String killer, String victim) {
 *     static KillMarker of(TimelineMarker marker) {
 *         return new KillMarker(marker.get("killer"), marker.get("victim"));
 *     }
 * }
 * }</pre>
 *
 * <p>A marker whose type nobody recognises still reads fine: it's a type and some fields, and a UI
 * can show it or skip it.
 *
 * @param type  what kind of thing happened, in whatever vocabulary you like
 * @param tick  the recording tick it happened on, stamped by Recap rather than by the caller
 * @param data  the detail, as strings, so it survives storage without agreeing a schema first
 */
public record TimelineMarker(String type, int tick, Map<String, String> data) {

    public TimelineMarker(String type, int tick, Map<String, String> data) {
        this.type = type;
        this.tick = tick;
        // Copied and frozen: markers get handed to anything that asks, and one caller editing what
        // another is reading is a bug nobody would think to look for here.
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    /** A field, or null when this marker does not carry it. Never throws on an unknown name. */
    public String get(String key) {
        return data.get(key);
    }

    /** A field, or {@code fallback} when it's missing. Handy for markers written by older versions. */
    public String getOr(String key, String fallback) {
        String value = data.get(key);
        return value == null ? fallback : value;
    }
}
