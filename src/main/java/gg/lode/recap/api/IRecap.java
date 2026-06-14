package gg.lode.recap.api;

import gg.lode.recap.api.recording.IRecordingManager;
import gg.lode.recap.api.scene.ISceneManager;

public interface IRecap {
    IRecordingManager getRecordingManager();
    ISceneManager getSceneManager();

    /**
     * Play a recording anchored at a target location: the recording's capture
     * origin is mapped onto {@code anchor}, so the whole replay (player NPC plus
     * any recorded blocks) lands at {@code anchor} regardless of where the
     * recording was made. This is the primitive for replaying a recording in a
     * reused arena — record once, replay at any arena's anchor point.
     *
     * @param recordingName name of a stored recording (not a scene)
     * @param anchor        world location to map the recording's origin onto
     * @param loop          whether the replay loops
     * @return the playback session id, or {@code null} if the recording is
     *         missing or could not start
     */
    String playRecordingAnchored(String recordingName, org.bukkit.Location anchor, boolean loop);

    /**
     * Stop an active playback session started via
     * {@link #playRecordingAnchored}.
     *
     * @return {@code true} if a session with that id was running
     */
    boolean stopPlayback(String sessionId);

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
