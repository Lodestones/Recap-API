package gg.lode.recap.api;

import java.util.UUID;
import java.util.Collection;
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
     * How far into a match its recordings already reach, in ticks.
     *
     * <p>The furthest point any track for this match covers: the greatest start tick plus duration
     * across all of them, which is where recording stopped. Zero when the match has none.
     *
     * <p>For a server picking a match back up after restarting in the middle of it. The tick counter
     * it was using does not outlive the process, and counting from zero again stamps everything
     * recorded afterwards as starting with the match. Asked of the recordings rather than worked out
     * from a clock because the recordings are what the answer has to agree with.
     *
     * <p>Default-implemented for older builds, which answer zero and so behave as they did.
     */
    default int recordedTicksFor(String matchId) {
        return 0;
    }

    /**
     * Labels the subject of a playback, drawn before its name.
     *
     * <p>Overrides whatever the recording itself said it was. The recording's own label is the default,
     * so a stand-in is marked without anybody asking; this is for a caller that wants its own wording,
     * colour, or a team prefix the recording could not have known.
     *
     * <p>MiniMessage, so a colour can be given: {@code "<dark_gray>[MIA] "}. Trailing space included if
     * one is wanted — the label is placed immediately before the name. Null or blank clears it back to
     * nothing rather than back to the recording's own.
     *
     * @return whether a playback by that id was found
     */
    default boolean setPlaybackLabel(String sessionId, String label) {
        return false;
    }

    /**
     * The entity id of the subject a playback is replaying, or -1.
     *
     * <p>For anything that wants to hang something off the subject — a nametag plugin mounting a text
     * display above it, most obviously. A replayed subject is built from packets and is not an entity
     * the server has, so an id is the only handle that exists for it; there is no {@code Player} and
     * nothing in {@code getOnlinePlayers()}.
     *
     * <p>Only meaningful while the playback is running, and only for a session replaying one subject.
     * The first of them otherwise.
     */
    default int playbackEntityId(String sessionId) {
        return -1;
    }

    /**
     * The uuid the subject's body is spawned under, or null.
     *
     * <p>The companion to {@link #playbackEntityId(String)}, for the callers that need a uuid rather
     * than an id — anything addressing the body through a client mod, which speaks in uuids because
     * that is what the client knows an entity by. A replayed subject has no {@code Player} and is in
     * nobody's entity list, so this is the only place the uuid can come from.
     *
     * <p>Default null so an older implementation still links.
     */
    default UUID playbackEntityUuid(String sessionId) {
        return null;
    }

    /**
     * Whether the subject is talking at the frame being shown.
     *
     * <p>For drawing a speaking indicator over a replayed subject, the way one is drawn over a live
     * player. Answered from the recorded voice track and the playback clock, so it follows seeking and
     * pausing: scrubbing to the middle of a sentence lights it, pausing puts it out.
     *
     * <p>Held briefly past the last recorded frame, so the indicator does not flicker between words.
     *
     * <p>Default false so an older implementation still links.
     */
    default boolean isPlaybackSpeaking(String sessionId) {
        return false;
    }

    /**
     * What the recording behind a playback said its subject was, or an empty string.
     *
     * <p>The label set at capture time — {@code "MIA"} for a stand-in. Playback draws it itself, so
     * this is for a caller that would rather draw it another way, and needs to know there is something
     * to draw.
     */
    default String playbackTag(String sessionId) {
        return "";
    }

    /**
     * The health of a playback's subject at the frame being shown, or -1 when it is not known yet.
     *
     * <p>As the recording stored it, half-hearts included. Absorption was not recorded apart from it, so
     * this is what the player had rather than what kind of hearts they were.
     */
    default float playbackHealth(String sessionId) {
        return -1f;
    }

    /**
     * Absorption hearts on a playback's subject at the frame being shown.
     *
     * <p>Apart from {@link #playbackHealth(String)} because they are drawn apart — yellow over red — and
     * because a caller showing a total wants to add them while one choosing a colour wants to know
     * whether there are any. Zero for a recording made before they were kept, which is every recording
     * made before this method existed.
     */
    default float playbackAbsorption(String sessionId) {
        return 0f;
    }

    /**
     * Limits who can see a playback, or lifts the limit.
     *
     * <p>Only the named players receive the subject and everything it emits — its sounds and particles
     * included, so a hidden subject is hidden rather than merely invisible. Null or empty means
     * everybody, which is the default.
     *
     * <p>May be changed at any time: a viewer taken off the list has the subject removed from their
     * screen on the next tick, and one added gets it spawned for them. That is what makes per-viewer
     * choices possible — a reviewer hiding the people who were already out while the person beside them
     * keeps watching all of them.
     *
     * @param sessionId the playback to restrict
     * @param viewers   who may see it, or null for everybody
     * @return whether a playback by that id was found
     */
    default boolean setPlaybackViewers(String sessionId, Collection<UUID> viewers) {
        return false;
    }

    /**
     * Holds or resumes a match's world recording without closing it.
     *
     * <p>For a match the game itself has paused. The world recorder advances on its own timer, which
     * keeps running while the game is frozen, so a paused match would otherwise leave the map's
     * recording minutes longer than the player tracks taken during the same match — and a replay
     * lines the two up by tick. Held, both sides stop counting and the pause does not exist in the
     * footage at all, which is what a reviewer wants: the match, not the interruption.
     *
     * <p>Not the same as stopping it. A stopped world recording is written out and a second one would
     * be a second file, and playback takes the newest for a match and world — so stopping and starting
     * would quietly discard everything before the pause.
     *
     * @param matchId   the match whose world recording to hold
     * @param worldName the recorded world's name
     * @param paused    true to hold, false to carry on
     * @return whether a live world recording for that pair was found
     */
    default boolean pauseWorldRecording(String matchId, String worldName, boolean paused) {
        return false;
    }

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
     * Notes something that happened in a match, at the tick the match is currently at.
     *
     * <p>For what Recap cannot know by watching: which blow was a kill, when a round turned, who took
     * an objective. Call it as the thing happens and it lands on the right tick, because the tick comes
     * from the recording rather than from the caller — a marker timed by wall clock drifts against the
     * footage on any server that dips below twenty ticks a second, and a feed minutes out of step reads
     * as a broken replay.
     *
     * <p>Ignored when no match is being recorded, so it is safe to call unconditionally.
     *
     * @param matchId the match being recorded
     * @param type    what kind of thing happened, in your own vocabulary
     * @param data    the detail; copied, and must contain no null keys or values
     * @return true when it was recorded
     */
    boolean addTimelineMarker(String matchId, String type, java.util.Map<String, String> data);

    /**
     * Every marker recorded for a match, in the order they happened.
     *
     * <p>Empty when the match has none, which is the normal state of a match recorded before anything
     * wrote any — so a caller never has to ask whether the feature was in use at the time.
     */
    java.util.List<gg.lode.recap.api.timeline.TimelineMarker> getTimelineMarkers(String matchId);

    /**
     * The markers recorded at or before a tick, newest last.
     *
     * <p>What a feed wants while a replay plays, and the reason a seek does not replay an hour of
     * events at once: ask again after a jump and the answer is what had happened by then.
     *
     * @param upToTick the recording tick being shown
     */
    java.util.List<gg.lode.recap.api.timeline.TimelineMarker> getTimelineMarkersUpTo(String matchId, int upToTick);

    /**
     * Makes sure a match's markers are on this server, fetching them from shared storage when not.
     *
     * <p><b>Blocks.</b> Call it off the main thread, alongside {@link #ensureWorldRecording}: a replay
     * server loads a match asynchronously, which is where this belongs.
     *
     * @return true when markers are present locally afterwards, including when the match simply has none
     */
    boolean ensureMatchTimeline(String matchId);

    /**
     * Holds a playback where it is. The NPCs stop; nothing is lost.
     *
     * <p>A review is mostly spent stopped and stepping, not watching at speed, so pause is the
     * normal state rather than the exception.
     */
    boolean pausePlayback(String sessionId, boolean paused);

    /**
     * Jumps a playback to a tick.
     *
     * <p>Seeking backwards replays from the start internally — a recording is a stream of changes,
     * so the only way to know what a thing was at tick N is to apply everything up to it. Callers
     * see a jump either way.
     */
    boolean seekPlayback(String sessionId, int tick);

    /** Sets playback rate; 1.0 is real time. */
    boolean setPlaybackSpeed(String sessionId, double speed);

    /** Where a playback has reached, in ticks, or -1 when there is no such session. */
    int getPlaybackTick(String sessionId);

    /** How long a playback runs, in ticks, or -1 when there is no such session. */
    int getPlaybackDuration(String sessionId);

    /**
     * Where a playback's actor is standing right now, or null when it has none.
     *
     * <p>Replay actors are packet-level NPCs with no Bukkit entity behind them, so a caller
     * cannot find one by looking at the world. Anything that wants to point a camera at a
     * recorded player — a spectate menu, a jump-to-player control — has to ask.
     *
     * <p>Default null so an older implementation still links.
     */
    default org.bukkit.Location getPlaybackLocation(String sessionId) {
        return null;
    }

    /** Pause, seek and speed for a world playback, so the map scrubs with the people on it. */
    boolean pauseWorldPlayback(String sessionId, boolean paused);

    boolean seekWorldPlayback(String sessionId, int tick);

    boolean setWorldPlaybackSpeed(String sessionId, double speed);

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
