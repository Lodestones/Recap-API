package gg.lode.recap.api;

import gg.lode.recap.api.recording.IRecordingManager;
import gg.lode.recap.api.scene.ISceneManager;

public interface IRecap {
    IRecordingManager getRecordingManager();
    ISceneManager getSceneManager();

    /**
     * Opt-in a server→client packet type for capture during active recordings.
     * Allowlisted packets are recorded as raw bytes per tick and replayed
     * verbatim to NPC viewers when the recording is played back.
     *
     * The type name is the PacketEvents {@code PacketType.Play.Server} enum
     * name — e.g. {@code "SOUND_EFFECT"}, {@code "ENTITY_SOUND_EFFECT"},
     * {@code "PARTICLE"}, {@code "SET_TITLE_TEXT"}, {@code "PLUGIN_MESSAGE"}.
     *
     * @param packetTypeName PacketEvents type enum name
     */
    void capturePacketType(String packetTypeName);

    /**
     * Stop capturing a previously-allowlisted packet type. Recordings already
     * on disk are unaffected.
     */
    void uncapturePacketType(String packetTypeName);

    java.util.Set<String> getCapturedPacketTypes();

    /**
     * Opt-in plugin-messaging traffic on a specific channel for capture and
     * replay. The PLUGIN_MESSAGE packet type does not need to be allowlisted
     * separately — any S2C plugin message on a captured channel is recorded.
     *
     * Useful for client-mod ecosystems (e.g. Lectern's
     * {@code lectern:receive_event}): record effect packets sent to the player
     * being captured, replay them to NPC viewers.
     *
     * @param channel exact channel identifier, namespaced ({@code "lectern:receive_event"})
     */
    void capturePluginMessageChannel(String channel);

    void uncapturePluginMessageChannel(String channel);

    java.util.Set<String> getCapturedPluginMessageChannels();
}
