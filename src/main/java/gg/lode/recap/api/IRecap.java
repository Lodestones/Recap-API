package gg.lode.recap.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import gg.lode.recap.api.recording.IRecordingManager;
import gg.lode.recap.api.scene.ISceneManager;
import gg.lode.recap.api.timeline.TimelineMarker;

public interface IRecap {
    IRecordingManager getRecordingManager();
    ISceneManager getSceneManager();

    /**
     * Plays a recording somewhere else: its capture origin is mapped onto {@code anchor}, so the
     * NPC and any recorded blocks land there no matter where the footage was taken. Record once,
     * replay in any arena.
     *
     * @param recordingName a stored recording, not a scene
     * @param anchor        where the recording's origin should land
     * @param loop          whether the replay loops
     * @return the playback session id, or null if the recording is missing or wouldn't start
     */
    String playRecordingAnchored(String recordingName, Location anchor, boolean loop);

    /**
     * Plays a recording back at the coordinates it was actually taken at, optionally in another world.
     *
     * <p>The opposite of {@link #playRecordingAnchored}: reviewing a match means the footage has to
     * land exactly where it happened, or you're watching a fight beside the terrain it was fought on.
     *
     * <p>{@code worldName} is there because a replay server loads the archived map under its own
     * name, so the world holding those coordinates isn't the one the recording names. Pass null to
     * use the recorded world.
     *
     * @return the playback session id, or null when the recording or world is missing
     */
    @Nullable String playRecordingAtRecordedPosition(String recordingName,
                                                     @Nullable String worldName, boolean loop);

    /**
     * Stops a playback session started by {@link #playRecordingAnchored}.
     *
     * @return true if a session with that id was running
     */
    boolean stopPlayback(String sessionId);

    /**
     * How far into a match its recordings already reach, in ticks. Zero when it has none.
     *
     * <p>For picking a match back up after a restart mid-match. The tick counter doesn't outlive the
     * process, and counting from zero again would stamp everything recorded afterwards as starting
     * with the match. Asked of the recordings rather than a clock, because the recordings are what
     * the answer has to agree with.
     */
    default int recordedTicksFor(String matchId) {
        return 0;
    }

    /**
     * Labels the subject of a playback, drawn just before its name.
     *
     * <p>Overrides whatever the recording said it was, so this is for callers that want their own
     * wording, colour, or a team prefix the recording couldn't have known. MiniMessage, so
     * {@code "<dark_gray>[MIA] "} works; include the trailing space if you want one. Null or blank
     * clears the label entirely rather than falling back to the recording's own.
     *
     * @return whether a playback by that id was found
     */
    default boolean setPlaybackLabel(String sessionId, String label) {
        return false;
    }

    /**
     * The entity id of the subject a playback is replaying, or -1.
     *
     * <p>A replayed subject is built from packets, so there's no {@code Player} and nothing in
     * {@code getOnlinePlayers()}. If you want to hang something off it (a nametag plugin mounting a
     * text display, say) the entity id is the only handle there is.
     *
     * <p>Only meaningful while the playback runs. For a multi-subject session you get the first.
     */
    default int playbackEntityId(String sessionId) {
        return -1;
    }

    /**
     * The uuid the subject's body is spawned under, or null.
     *
     * <p>Companion to {@link #playbackEntityId(String)}, for callers that speak uuids instead of
     * entity ids, like anything talking to a client mod.
     */
    default UUID playbackEntityUuid(String sessionId) {
        return null;
    }

    /**
     * Whether the subject is talking at the frame being shown, for drawing a speaking indicator.
     *
     * <p>Answered from the recorded voice track and the playback clock, so it follows seeking and
     * pausing. Held briefly past the last recorded frame so it doesn't flicker between words.
     */
    default boolean isPlaybackSpeaking(String sessionId) {
        return false;
    }

    /**
     * What the recording said its subject was ({@code "MIA"} for a stand-in), or an empty string.
     * Playback already draws this; it's exposed for callers that would rather draw it themselves.
     */
    default String playbackTag(String sessionId) {
        return "";
    }

    /**
     * The subject's health at the frame being shown, or -1 when it isn't known yet. Half-hearts
     * included. Absorption is tracked separately, so this is what the player had rather than what
     * kind of hearts they were.
     */
    default float playbackHealth(String sessionId) {
        return -1f;
    }

    /**
     * Absorption hearts on the subject at the frame being shown.
     *
     * <p>Separate from {@link #playbackHealth(String)} because they're drawn separately, yellow over
     * red. Zero for any recording made before absorption was kept.
     */
    default float playbackAbsorption(String sessionId) {
        return 0f;
    }

    /**
     * Limits who can see a playback, or lifts the limit.
     *
     * <p>Only the named players get the subject and everything it emits, sounds and particles
     * included, so a hidden subject really is hidden. Null or empty means everybody, the default.
     *
     * <p>Changeable at any time: drop someone off the list and the subject leaves their screen next
     * tick; add someone and it spawns for them. That's what makes per-viewer choices work, like one
     * reviewer hiding the people already out while the person next to them watches everyone.
     *
     * @return whether a playback by that id was found
     */
    default boolean setPlaybackViewers(String sessionId, Collection<UUID> viewers) {
        return false;
    }

    /**
     * Outlines a subject on one viewer's screen in a colour, or takes the outline off.
     *
     * <p>Per viewer, because an outline is the watcher's choice, not something the footage says.
     *
     * <p>It lives here because the two packets an outline needs are ones Recap owns and re-sends:
     * the glow bit rides in the body's flags, and the colour comes from its team. An outline drawn
     * from outside survives a few frames, then loses its colour and goes out.
     *
     * @param colour a colour name the way Minecraft spells it ({@code GREEN}, {@code RED}), or null
     *               to remove the outline. An unknown name counts as null.
     * @return whether a playback by that id was found
     */
    default boolean setPlaybackGlow(String sessionId, UUID viewer, String colour) {
        return false;
    }

    /**
     * Holds or resumes a match's world recording without closing it, for a match the game itself
     * has paused.
     *
     * <p>The world recorder runs on its own timer, which keeps ticking while the game is frozen. Left
     * alone, the map's recording ends up minutes longer than the player tracks from the same match,
     * and a replay lines the two up by tick. Held, both sides stop counting and the pause never
     * appears in the footage.
     *
     * <p>Not the same as stopping. A stopped world recording gets written out, a second one becomes a
     * second file, and playback takes the newest for a match and world, so stop-and-start would
     * quietly throw away everything before the pause.
     *
     * @return whether a live world recording for that pair was found
     */
    default boolean pauseWorldRecording(String matchId, String worldName, boolean paused) {
        return false;
    }

    /**
     * Replays a world recording into a live world: every block change the match made, back in the
     * order and at the pace it happened.
     *
     * <p>Player recordings put the people back, this puts the map back around them. Without it you
     * get a fight on a frozen map, which reads fine until somebody dies to something that isn't there.
     *
     * <p>Entities in the recording aren't replayed yet.
     *
     * @param targetWorldName world to replay into, or null for the recorded name. A replay server
     *                        loads an archived map under its own name, so these differ in practice
     * @return the playback session id, or null when no such recording or world exists
     */
    @Nullable String playWorldRecording(String matchId, String worldName,
                                        @Nullable String targetWorldName, boolean loop);

    /**
     * Makes sure every player recording for a match is on this server, fetching whatever is missing
     * from shared storage.
     *
     * <p><b>Blocks.</b> Call it off the main thread. Recordings are named
     * {@code match-<matchId>-<player>}, which is the only thing tying a person's footage to a match,
     * so treat that naming as a contract.
     *
     * @return how many of the match's recordings are present locally afterwards
     */
    int ensureMatchRecordings(String matchId);

    /**
     * Makes sure a match's world recording is on this server, fetching it when it isn't.
     *
     * <p><b>Blocks</b>, and may pull tens of megabytes. Call it off the main thread, alongside
     * whatever else your replay server loads asynchronously.
     */
    boolean ensureWorldRecording(String matchId, String worldName);

    /**
     * Notes something that happened in a match, stamped at the tick the match is currently at.
     *
     * <p>For what Recap can't work out by watching: which blow was a kill, when a round turned, who
     * took an objective. Call it as the thing happens. The tick comes from the recording, not from
     * the caller, because a marker timed by wall clock drifts against the footage on any server that
     * dips below 20 TPS, and a feed minutes out of step reads as a broken replay.
     *
     * <p>Ignored when no match is being recorded, so calling it unconditionally is fine.
     *
     * @param type what kind of thing happened, in your own vocabulary
     * @param data the detail; copied, and must contain no null keys or values
     * @return true when it was recorded
     */
    boolean addTimelineMarker(String matchId, String type, Map<String, String> data);

    /**
     * Every marker recorded for a match, in the order they happened. Empty when there are none, so
     * you never have to ask whether the feature was in use at the time.
     */
    List<TimelineMarker> getTimelineMarkers(String matchId);

    /**
     * The markers recorded at or before a tick, newest last.
     *
     * <p>What a live feed wants while a replay plays, and why a seek doesn't dump an hour of events
     * at once: ask again after a jump and you get what had happened by then.
     */
    List<TimelineMarker> getTimelineMarkersUpTo(String matchId, int upToTick);

    /**
     * Makes sure a match's markers are on this server, fetching them when they aren't.
     *
     * <p><b>Blocks.</b> Call it off the main thread next to {@link #ensureWorldRecording}.
     *
     * @return true when markers are present locally afterwards, including when there simply are none
     */
    boolean ensureMatchTimeline(String matchId);

    /**
     * Holds a playback where it is. The NPCs stop and nothing is lost. A review is mostly spent
     * stopped and stepping, so pause is the normal state rather than the exception.
     */
    boolean pausePlayback(String sessionId, boolean paused);

    /**
     * Jumps a playback to a tick.
     *
     * <p>Seeking backwards replays from the start internally: a recording is a stream of changes, so
     * the only way to know what something was at tick N is to apply everything up to it. Either way
     * the caller just sees a jump.
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
     * <p>Replay actors are packet-level NPCs with no Bukkit entity behind them, so you can't find one
     * by looking at the world. Anything pointing a camera at a recorded player (a spectate menu, a
     * jump-to-player control) has to ask.
     */
    default Location getPlaybackLocation(String sessionId) {
        return null;
    }

    /** Pause, seek and speed for a world playback, so the map scrubs with the people on it. */
    boolean pauseWorldPlayback(String sessionId, boolean paused);

    boolean seekWorldPlayback(String sessionId, int tick);

    boolean setWorldPlaybackSpeed(String sessionId, double speed);

    /**
     * Stops a world playback started by {@link #playWorldRecording}.
     *
     * @return true if a session with that id was running
     */
    boolean stopWorldPlayback(String sessionId);

    /**
     * Opts a server-to-client packet type into capture. Allowlisted packets are recorded as raw
     * bytes per tick and replayed verbatim to NPC viewers.
     *
     * @param packetTypeName the PacketEvents {@code PacketType.Play.Server} enum name, for example
     *                       {@code "SOUND_EFFECT"}, {@code "PARTICLE"}, {@code "SET_TITLE_TEXT"}
     */
    void capturePacketType(String packetTypeName);

    /** Stops capturing a packet type. Recordings already on disk are unaffected. */
    void uncapturePacketType(String packetTypeName);

    Set<String> getCapturedPacketTypes();

    /**
     * Opts plugin-message traffic on one channel into capture and replay. PLUGIN_MESSAGE itself
     * doesn't need allowlisting separately: any S2C plugin message on a captured channel is recorded.
     *
     * <p>Handy for client-mod ecosystems such as Lectern's {@code lectern:receive_event}, where the
     * effect packets sent to the recorded player get replayed to NPC viewers.
     *
     * @param channel the exact namespaced channel, for example {@code "lectern:receive_event"}
     */
    void capturePluginMessageChannel(String channel);

    void uncapturePluginMessageChannel(String channel);

    Set<String> getCapturedPluginMessageChannels();
}
