package gg.lode.recap.api.recording;

/**
 * Represents an active world-scoped recording session for a match.
 * <p>
 * A world recording captures the entire map state — block changes, entity spawns/updates,
 * and other environment data — suitable for full-world replay with seeking capabilities.
 * <p>
 * Returned by {@link IRecordingManager#startWorldRecording} and thread-safe for
 * concurrent calls from multiple event listeners.
 */
public interface IWorldRecordingSession {

    /**
     * Get the unique session identifier.
     */
    String getSessionId();

    /**
     * Get the match identifier this session is recording.
     */
    String getMatchName();

    /**
     * Get the world name being recorded.
     */
    String getWorldName();

    /**
     * Get the file path where this recording is being saved.
     */
    String getOutputPath();

    /**
     * Check if this session is still active (recording).
     */
    boolean isActive();

    /**
     * Get the number of ticks recorded so far.
     */
    int getTickCount();

    /**
     * Get the approximate duration in seconds since recording started.
     */
    long getDurationSeconds();
}
