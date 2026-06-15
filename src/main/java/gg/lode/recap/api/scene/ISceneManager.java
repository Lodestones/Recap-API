package gg.lode.recap.api.scene;

import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

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
     * Add a recording to a scene with optional offsets, optionally marking it
     * playback-only (visual-only; excluded when a caller requests the
     * non-playback-only actor set).
     *
     * @return true if added
     */
    boolean addRecordingToScene(String sceneName, String recordingName, int startDelay,
                                double offsetX, double offsetY, double offsetZ, boolean playbackOnly);

    /**
     * Create a composite scene that plays the given child scenes together.
     * Child entries are flattened (recursively) at play time.
     *
     * @return true if created
     */
    boolean createCompositeScene(String name, List<String> children);

    /**
     * Add a child scene to a composite, with no offset.
     *
     * @return true if added
     */
    boolean addChildScene(String parentName, String childName);

    /**
     * Add (or re-offset) a child scene with a position/time shift applied to
     * every entry it contributes. All-zero offset/delay behaves as a plain
     * child.
     *
     * @return true if added
     */
    boolean addChildScene(String parentName, String childName,
                          double offsetX, double offsetY, double offsetZ, int startDelay);

    /**
     * Remove a child scene from a composite.
     *
     * @return true if removed
     */
    boolean removeChildScene(String parentName, String childName);

    /**
     * Get the child scene names of a composite scene.
     */
    Collection<String> getSceneChildren(String sceneName);

    /**
     * Copy a scene (entries + child offsets) to a new name.
     *
     * @return true if copied
     */
    boolean copyScene(String sourceName, String destName);

    /**
     * Play a scene at a given location.
     *
     * @return a playback ID, or null if failed
     */
    String playScene(String sceneName, Location origin);

    /**
     * Play a scene at a given location, optionally looping.
     *
     * @param loop whether the whole scene loops
     * @return a playback ID, or null if failed
     */
    String playScene(String sceneName, Location origin, boolean loop);

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
