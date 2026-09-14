package gg.lode.recap.api.recording;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IRecordingManager {

    // The startRecording family below all do the same thing, they just vary in what you supply.
    //
    //   persist = true   every frame is streamed to disk. The default.
    //   persist = false  rolling-buffer mode: only the last N seconds stay in memory and older
    //                    frames fall off as new ones arrive. Handy for moderation, where you record
    //                    everyone all the time and only save when something actually happens.
    //                    N defaults to 30 seconds unless you pass maxSeconds.
    //
    // Skip the name and one is generated for you; hand a real one to
    // stopRecording(Player, String) when you save. Every overload returns true if recording started.

    /** Records a player under a given name, keeping every frame. */
    boolean startRecording(Player player, String name);

    /** Records a player under a generated name. */
    boolean startRecording(Player player, boolean persist);

    /** Records a player under a generated name, with your own rolling-buffer length. */
    boolean startRecording(Player player, boolean persist, int maxSeconds);

    /** Records a player under a given name. */
    boolean startRecording(Player player, String name, boolean persist);

    /** Records a player under a given name, with your own rolling-buffer length. */
    boolean startRecording(Player player, String name, boolean persist, int maxSeconds);

    /**
     * Starts a recording that doesn't begin at the start of the match.
     *
     * <p>One match can produce several recordings: a player revived, reconnected, or picked back up
     * after a mid-match restart. Those start minutes in. {@code startTick} says how far in, and
     * playback holds the track until then, so the match reads as one timeline instead of every track
     * firing at once and putting the same player on the map twice.
     *
     * <p>It gets its own name rather than another overload because the fourth int on
     * {@link #startRecording(Player, String, boolean, int)} is a rolling-buffer length, and two
     * methods differing only in what an int means is a bug waiting to happen.
     *
     * @param startTick ticks from the start of the match to this recording's first frame, 0 when it
     *                  starts with the match
     */
    default boolean startRecordingAt(Player player, String name, boolean persist, int startTick) {
        return startRecording(player, name, persist);
    }

    /**
     * Labels what a subject is, shown above its head on playback.
     *
     * <p>Mostly for stand-ins. A bot wears the player's name, skin and uuid, so its recording looks
     * exactly like theirs and a reviewer has no way to tell nobody was driving. Only the caller
     * knows, so the caller says.
     *
     * <p>Keep it short, a couple of letters reads best above a head. Null or blank clears it. It
     * lands on the saved recording, so you can set it any time before the recording stops.
     *
     * @return whether a live recording by that name was found
     */
    default boolean tagRecording(String recordingName, String tag) {
        return false;
    }

    /**
     * Stop recording a player and save the recording.
     *
     * @param player the player to stop recording
     * @return the completed recording session with metadata, or null if the player was not recording
     *         or if saving failed
     */
    @Nullable IRecordingSession stopRecording(Player player);

    /**
     * Stops recording a player, names the result, and saves it. This is the one to use when the
     * recording started with a generated name and you only know what to call it at save time.
     *
     * @return the completed session, or null if the player wasn't recording, the name is taken, or
     *         saving failed
     */
    @Nullable IRecordingSession stopRecording(Player player, String name);

    /**
     * Cancel a recording without saving it. The recorded data is discarded.
     *
     * @param player the player to cancel recording for
     * @return true if a recording was cancelled
     */
    boolean cancelRecording(Player player);

    /**
     * Check if a player is currently being recorded.
     */
    boolean isRecording(Player player);

    /**
     * Get all available recording names.
     */
    Collection<String> getRecordingNames();

    /**
     * Delete a recording by name.
     *
     * @return true if deleted
     */
    boolean deleteRecording(String name);

    /**
     * Delete all saved recordings.
     *
     * @return the number of recordings deleted
     */
    int clearAllRecordings();

    /**
     * Deletes every saved recording whose name matches a regex, for example
     * {@code clearAllRecordings("combatlog-.*")}.
     *
     * @return the number of recordings deleted
     */
    int clearAllRecordings(String regex);

    // --- World-scoped match recordings (v1.0.13+) ---

    /**
     * Starts a world-scoped recording covering a whole match.
     *
     * <p>Where a player recording follows one person, this follows the map: every block change
     * (mining, pistons, fluid, redstone) as one delta stream, plus non-player entities with their
     * position, rotation, velocity and metadata. That's what makes full-world replay work, seekable
     * to any place and time.
     *
     * <p>One per match per world. Starting a second for the same pair stops and replaces the first.
     *
     * @param matchIdentifier unique id for this match, for example "match-2026-09-10-001"
     * @return a handle to the live session, or null if it couldn't be created
     */
    @Nullable IWorldRecordingSession startWorldRecording(String matchIdentifier, String worldName);

    /**
     * Stop and finalize a world recording.
     *
     * @param matchIdentifier the match identifier used when calling {@link #startWorldRecording}
     * @param worldName       the world name
     * @return true if a recording was stopped
     */
    boolean stopWorldRecording(String matchIdentifier, String worldName);

    /**
     * Check if a world recording is active for a given match+world pair.
     */
    boolean isWorldRecording(String matchIdentifier, String worldName);

    /**
     * Records a block placement that no client packet will ever describe, because a plugin is
     * driving the player (a Catalyst bot, say). It's recorded as though a BlockPlaceEvent had
     * fired, without actually firing one, so protection plugins stay out of it.
     *
     * <p>No-op when the player isn't being recorded.
     */
    void recordSyntheticBlockPlace(Player player, Block block, Material material, BlockState oldState);

    /**
     * Turns on voice-chat capture for a player's current recording. Needs Simple Voice Chat
     * installed and the capture hook registered; without those it quietly does nothing.
     */
    void enableVoiceCapture(Player player);

    /**
     * Turns on voice-chat capture for one named recording. Use this when a player is holding
     * several recordings at once and only one of them should capture audio. Same requirements as
     * {@link #enableVoiceCapture(Player)}.
     */
    void enableVoiceCapture(String recordingName);
}
