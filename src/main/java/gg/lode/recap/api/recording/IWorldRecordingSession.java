package gg.lode.recap.api.recording;

/**
 * A live world-scoped recording for a match: block changes, entity spawns and updates, and the rest
 * of the environment, enough to replay the whole map and seek around in it.
 *
 * <p>Handed back by {@link IRecordingManager#startWorldRecording}, and safe to call from several
 * event listeners at once.
 */
public interface IWorldRecordingSession {

    String getSessionId();

    /** The match this session is recording. */
    String getMatchName();

    String getWorldName();

    /** Where the recording is being written. */
    String getOutputPath();

    /** Whether it's still recording. */
    boolean isActive();

    int getTickCount();

    /** Roughly how long it has been running, in seconds. */
    long getDurationSeconds();
}
