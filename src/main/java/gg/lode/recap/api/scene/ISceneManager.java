package gg.lode.recap.api.scene;

import org.bukkit.Location;

import java.util.Collection;

public interface ISceneManager {

    /**
     * Create a new empty scene.
     *
     * @param name the scene name
     * @return true if created
     */
    boolean createScene(String name);

    /**
     * Add a recording to a scene with optional offsets.
     *
     * @param sceneName     the scene to add to
     * @param recordingName the recording to add
     * @param startDelay    delay in ticks before this recording starts
     * @param offsetX       X position offset
     * @param offsetY       Y position offset
     * @param offsetZ       Z position offset
     * @return true if added
     */
    boolean addRecordingToScene(String sceneName, String recordingName, int startDelay,
                                double offsetX, double offsetY, double offsetZ);

    /**
     * Remove a recording from a scene.
     */
    boolean removeRecordingFromScene(String sceneName, String recordingName);

    /**
     * Play a scene at a given location.
     *
     * @return a playback ID, or null if failed
     */
    String playScene(String sceneName, Location origin);

    /**
     * Stop a running playback.
     */
    boolean stopPlayback(String playbackId);

    /**
     * Set the playback speed for a running session.
     * <p>
     * Speed 1.0 is normal, 0.5 is half speed, 2.0 is double speed.
     * Minimum speed is 0.01, no maximum.
     *
     * @param playbackId the session ID
     * @param speed      the playback speed multiplier
     * @return true if the session exists
     */
    boolean setPlaybackSpeed(String playbackId, double speed);

    /**
     * Seek a running playback to a specific tick.
     * <p>
     * If the target tick is behind the current position, the playback restarts
     * from the beginning and fast-forwards to the target. This may take a moment
     * for long recordings.
     *
     * @param playbackId the session ID
     * @param tick       the target tick (0-based)
     * @return true if the session exists
     */
    boolean seekPlayback(String playbackId, int tick);

    /**
     * Stop all running playbacks.
     */
    void stopAllPlaybacks();

    /**
     * Get all scene names.
     */
    Collection<String> getSceneNames();

    /**
     * Delete a scene.
     */
    boolean deleteScene(String name);
}
