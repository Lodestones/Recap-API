package gg.lode.recap.api.recording;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IRecordingManager {

    /**
     * Start recording a player's actions in persist mode (all frames kept).
     *
     * @param player the player to record
     * @param name   a name for this recording
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, String name);

    /**
     * Start recording a player's actions with an auto-generated name.
     * <p>
     * When {@code persist} is {@code false}, the recording operates in rolling buffer mode —
     * only the last 30 seconds of frames are kept in memory. Older frames are discarded
     * as new ones are captured. This is useful for moderation: always record players, and
     * only save when an incident occurs.
     * <p>
     * When {@code persist} is {@code true}, all frames are streamed to disk (default behavior).
     * <p>
     * The recording name is auto-generated. Use {@link #stopRecording(Player, String)} to
     * assign a meaningful name when saving.
     *
     * @param player  the player to record
     * @param persist true for full recording, false for rolling buffer (last 30 seconds)
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, boolean persist);

    /**
     * Start recording a player's actions with an auto-generated name and custom buffer duration.
     * <p>
     * When {@code persist} is {@code false}, only the last {@code maxSeconds} of frames
     * are kept in memory. When {@code persist} is {@code true}, {@code maxSeconds} is ignored
     * and all frames are kept.
     * <p>
     * The recording name is auto-generated. Use {@link #stopRecording(Player, String)} to
     * assign a meaningful name when saving.
     *
     * @param player     the player to record
     * @param persist    true for full recording, false for rolling buffer
     * @param maxSeconds maximum seconds to keep in the rolling buffer (only used when persist is false)
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, boolean persist, int maxSeconds);

    /**
     * Start recording a player's actions.
     * <p>
     * When {@code persist} is {@code false}, the recording operates in rolling buffer mode —
     * only the last 30 seconds of frames are kept in memory. Older frames are discarded
     * as new ones are captured. This is useful for moderation: always record players, and
     * only save when an incident occurs.
     * <p>
     * When {@code persist} is {@code true}, all frames are streamed to disk (default behavior).
     *
     * @param player  the player to record
     * @param name    a name for this recording
     * @param persist true for full recording, false for rolling buffer (last 30 seconds)
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, String name, boolean persist);

    /**
     * Start recording a player's actions with a custom rolling buffer duration.
     * <p>
     * When {@code persist} is {@code false}, only the last {@code maxSeconds} of frames
     * are kept in memory. When {@code persist} is {@code true}, {@code maxSeconds} is ignored
     * and all frames are kept.
     *
     * @param player     the player to record
     * @param name       a name for this recording
     * @param persist    true for full recording, false for rolling buffer
     * @param maxSeconds maximum seconds to keep in the rolling buffer (only used when persist is false)
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, String name, boolean persist, int maxSeconds);

    /**
     * Stop recording a player and save the recording.
     *
     * @param player the player to stop recording
     * @return the completed recording session with metadata, or null if the player was not recording
     *         or if saving failed
     */
    @Nullable IRecordingSession stopRecording(Player player);

    /**
     * Stop recording a player, assign a name, and save the recording.
     * <p>
     * This is useful when the recording was started with an auto-generated name
     * (e.g. moderation rolling buffer) and you want to assign a meaningful name
     * at save time.
     *
     * @param player the player to stop recording
     * @param name   the name to assign to the saved recording
     * @return the completed recording session with metadata, or null if the player was not recording,
     *         the name is already taken, or if saving failed
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
     * Delete all saved recordings whose names match the given regex pattern.
     * <p>
     * Example: {@code clearAllRecordings("combatlog-.*")} deletes all combat log recordings.
     *
     * @param regex the regex pattern to match recording names against
     * @return the number of recordings deleted
     */
    int clearAllRecordings(String regex);

    // --- World-scoped match recordings (v1.0.13+) ---

    /**
     * Start a world-scoped recording for an entire match.
     * <p>
     * Unlike per-player recordings, a world recording captures the entire map state:
     * all block changes (mining, pistons, fluid flow, redstone, etc.) as a global
     * delta stream, and non-player entity state (mobs, items, projectiles) with
     * position, rotation, velocity, and metadata. This enables full-world replay
     * with seeking to any location and time.
     * <p>
     * One world recording per match per world. If a recording already exists for
     * this match+world pair, it is stopped and replaced.
     *
     * @param matchIdentifier a unique identifier for this match (e.g., "match-2026-09-10-001")
     * @param worldName       the world being recorded (e.g., "world", "arena")
     * @return a handle to the active recording session, or null if creation failed
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
     * Record a synthetic block placement action. Called when a plugin drives a player entity
     * that does not send real client packets (e.g., a Catalyst bot). The placement is recorded
     * as if a normal BlockPlaceEvent occurred, without firing one (avoiding protection plugin issues).
     * <p>
     * This is a no-op if no recording is active for the player.
     *
     * @param player the player entity performing the placement
     * @param block the block that was placed
     * @param material the material that was placed
     * @param oldState the block state before placement
     */
    void recordSyntheticBlockPlace(Player player, Block block, Material material, BlockState oldState);

    /**
     * Enable voice-chat capture for a player's current recording.
     * <p>
     * This requires Simple Voice Chat to be installed on the server and the voice capture hook
     * to be registered. If voice capture is not available, this call has no effect.
     *
     * @param player the player whose recording should capture voice
     */
    void enableVoiceCapture(Player player);

    /**
     * Enable voice-chat capture for a named recording.
     * <p>
     * This is the name-targeted variant — use this when a player holds several concurrent
     * recordings and you need to enable voice capture on a specific one.
     * <p>
     * This requires Simple Voice Chat to be installed on the server and the voice capture hook
     * to be registered. If voice capture is not available, this call has no effect.
     *
     * @param recordingName the name of the recording to enable voice capture for
     */
    void enableVoiceCapture(String recordingName);
}
