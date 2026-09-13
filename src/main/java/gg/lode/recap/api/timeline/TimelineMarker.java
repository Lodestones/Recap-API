package gg.lode.recap.api.timeline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Something worth noting that happened at a point in a match, written by whoever knows it happened.
 *
 * <p>Recap records what a player did; it has no idea what any of it meant. Which blow was a kill, when
 * a round changed, who captured what — that is the game's knowledge, and before this there was nowhere
 * to put it, so a replay showed a fight with no account of it. A plugin adds markers while the match
 * runs and reads them back at playback, and Recap supplies the tick so they line up with the footage.
 *
 * <h2>Why this is a type and a map, and not something to extend</h2>
 *
 * <p>A marker is written to a file and read back somewhere else — often on a replay server that does
 * not have the plugin that wrote it installed. A subclass cannot be read back without its class, so
 * typed markers would either fail to load or oblige every reader to carry every writer's plugin. Recap
 * also hands its API to implementations child-first, which is exactly where two copies of one class
 * stop being the same class.
 *
 * <p>So the stored shape is deliberately dull: a {@code type} you choose and a map of strings. Define a
 * typed view over it in your own plugin if you want one — it costs a constructor and keeps the
 * coupling on your side of the line:
 *
 * <pre>{@code
 * record KillMarker(String killer, String victim) {
 *     static KillMarker of(TimelineMarker marker) {
 *         return new KillMarker(marker.get("killer"), marker.get("victim"));
 *     }
 * }
 * }</pre>
 *
 * <p>A marker whose type nobody recognises still reads: it is a type and some fields, and a UI can show
 * it or ignore it.
 *
 * @param type  what kind of thing happened, in whatever vocabulary the writer chooses
 * @param tick  the recording tick it happened on, stamped by Recap rather than supplied by the caller
 * @param data  the detail, as strings, so it survives storage without a schema being agreed first
 */
public record TimelineMarker(String type, int tick, Map<String, String> data) {

    public TimelineMarker(String type, int tick, Map<String, String> data) {
        this.type = type;
        this.tick = tick;
        // Copied and made unmodifiable: a marker is handed to anything that asks, and one caller
        // editing what another is reading is a bug nobody would think to look for here.
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    /** A field, or null when this marker does not carry it. Never throws on an unknown name. */
    public String get(String key) {
        return data.get(key);
    }

    /** A field, or {@code fallback} when absent — for reading a marker written by an older version. */
    public String getOr(String key, String fallback) {
        String value = data.get(key);
        return value == null ? fallback : value;
    }
}
