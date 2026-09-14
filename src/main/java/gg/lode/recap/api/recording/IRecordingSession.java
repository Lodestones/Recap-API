package gg.lode.recap.api.recording;

import java.util.UUID;

/**
 * A finished recording and its metadata, handed back by
 * {@link IRecordingManager#stopRecording}.
 */
public interface IRecordingSession {

    String getId();

    String getName();

    String getPlayerName();

    UUID getPlayerUuid();

    /** When recording started, epoch millis. */
    long getStartTimestamp();

    /** When recording ended, epoch millis. */
    long getEndTimestamp();

    int getDurationTicks();

    /** Duration in seconds, assuming 20 TPS. */
    double getDurationSeconds();

    String getWorldName();

    /** Where the player stood when recording began. */
    double getOriginX();

    double getOriginY();

    double getOriginZ();

    /** False means it ran as a rolling buffer, keeping only the last N seconds. */
    boolean isPersist();
}
