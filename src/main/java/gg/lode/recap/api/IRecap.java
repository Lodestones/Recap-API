package gg.lode.recap.api;

import org.jetbrains.annotations.Nullable;

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
     * Play a recording at the coordinates it was actually recorded at, optionally in a different
     * world.
     *
     * <p>{@link #playRecordingAnchored} exists to move a recording somewhere else — record once,
     * replay in any arena. Reviewing a match is the opposite problem: the footage has to land
     * exactly where it happened, or a reviewer is watching a fight play out beside the terrain it
     * was fought on.
     *
     * <p>{@code worldName} covers the case that makes this necessary: a replay server loads the
     * archived map under its own name, so the world holding those coordinates is not the world the
     * recording names. Pass null to use the recorded world.
     *
     * @param recordingName name of a stored recording
     * @param worldName     world to play into, or null for the one recorded
     * @param loop          whether the replay loops
     * @return the playback session id, or {@code null} if the recording or world is missing
     */
    @Nullable String playRecordingAtRecordedPosition(String recordingName,
                                                     @Nullable String worldName, boolean loop);

    /**
     * Stop an active playback session started via
     * {@link #playRecordingAnchored}.
     *
     * @return {@code true} if a session with that id was running
     */
    boolean stopPlayback(String sessionId);

    /**
     * Replays a world recording into a live world: the block changes a whole match made, put back
     * in the order and at the pace they happened.
     *
     * <p>Player recordings put the people back; this puts the map back around them. Run together,
     * the fight happens on terrain that changes the way it changed at the time — the explosion goes
     * off, the wall goes up, the tunnel gets dug. Without it a replay is a fight on a frozen map,
     * which is legible right up until somebody dies to something that is not there.
     *
     * <p>Entities in the recording are not replayed yet.
     *
     * @param matchId         match the recording was made for
     * @param worldName       world as it was named when recorded
     * @param targetWorldName world to replay into, or null for the recorded name — a replay server
     *                        loads an archived map under its own name, so these differ in practice
     * @param loop            whether to restart at the end
     * @return the playback session id, or {@code null} when no such recording or world exists
     */
    @Nullable String playWorldRecording(String matchId, String worldName,
                                        @Nullable String targetWorldName, boolean loop);

    /**
     * Makes sure every player recording for a match is on this server, fetching from shared storage
     * the ones that are not.
     *
     * <p><b>Blocks.</b> Call it off the main thread. Recordings are named
     * {@code match-<matchId>-<player>}, which is the only thing tying a person's footage to a
     * match, so that naming is a contract rather than a convenience.
     *
     * @return how many of the match's recordings are present locally afterwards
     */
    int ensureMatchRecordings(String matchId);

    /**
     * Makes sure a match's world recording is on this server, fetching it from shared storage when
     * it is not.
     *
     * <p><b>Blocks.</b> Call it off the main thread — it may download tens of megabytes. A replay
     * server already loads a match asynchronously, which is where this belongs.
     *
     * @return true when the recording is present locally afterwards
     */
    boolean ensureWorldRecording(String matchId, String worldName);

    /**
     * Stops a world playback started by {@link #playWorldRecording}.
     *
     * @return {@code true} if a session with that id was running
     */
    boolean stopWorldPlayback(String sessionId);

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
