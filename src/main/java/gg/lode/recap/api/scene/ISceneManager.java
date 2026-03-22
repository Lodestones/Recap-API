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
