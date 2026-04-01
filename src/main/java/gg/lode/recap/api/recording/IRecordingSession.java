package gg.lode.recap.api.recording;

import java.util.UUID;

/**
 * Represents a completed recording session with its metadata.
 * Returned when stopping a recording via {@link IRecordingManager#stopRecording}.
 */
public interface IRecordingSession {

    /**
     * Get the unique identifier of this recording.
     */
    String getId();

    /**
     * Get the name assigned to this recording.
     */
    String getName();

    /**
     * Get the name of the player who was recorded.
     */
    String getPlayerName();

    /**
     * Get the UUID of the player who was recorded.
     */
    UUID getPlayerUuid();

    /**
     * Get the timestamp (epoch millis) when the recording started.
     */
    long getStartTimestamp();

    /**
     * Get the timestamp (epoch millis) when the recording ended.
     */
    long getEndTimestamp();

    /**
     * Get the duration of the recording in ticks.
     */
    int getDurationTicks();

    /**
     * Get the duration of the recording in seconds (assuming 20 TPS).
     */
    double getDurationSeconds();

    /**
     * Get the world name where the recording took place.
     */
    String getWorldName();

    /**
     * Get the X coordinate of the player's position at recording start.
     */
    double getOriginX();

    /**
     * Get the Y coordinate of the player's position at recording start.
     */
    double getOriginY();

    /**
     * Get the Z coordinate of the player's position at recording start.
     */
    double getOriginZ();

    /**
     * Whether this recording was in persist mode.
     * Non-persist recordings use a rolling buffer (only last N seconds kept).
     */
    boolean isPersist();
}
