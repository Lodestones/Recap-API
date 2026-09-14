package gg.lode.recap.api.scene;

import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

public interface ISceneManager {

    /** Creates an empty scene. */
    boolean createScene(String name);

    /**
     * Adds a recording to a scene, shifted in time and space.
     *
     * @param startDelay ticks to wait before this entry starts
     */
    boolean addRecordingToScene(String sceneName, String recordingName, int startDelay,
                                double offsetX, double offsetY, double offsetZ);

    boolean removeRecordingFromScene(String sceneName, String recordingName);

    /**
     * Same, but {@code playbackOnly} marks the entry as visual dressing: it's left out whenever a
     * caller asks for the non-playback-only actors.
     */
    boolean addRecordingToScene(String sceneName, String recordingName, int startDelay,
                                double offsetX, double offsetY, double offsetZ, boolean playbackOnly);

    /** Creates a scene that plays other scenes together. Children are flattened at play time. */
    boolean createCompositeScene(String name, List<String> children);

    /** Adds a child scene with no shift. */
    boolean addChildScene(String parentName, String childName);

    /**
     * Adds a child scene, or re-shifts one already there. The offset and delay apply to every entry
     * the child contributes; all zeroes behaves like the plain version.
     */
    boolean addChildScene(String parentName, String childName,
                          double offsetX, double offsetY, double offsetZ, int startDelay);

    boolean removeChildScene(String parentName, String childName);

    Collection<String> getSceneChildren(String sceneName);

    /** Copies a scene, entries and child offsets included, under a new name. */
    boolean copyScene(String sourceName, String destName);

    /** Plays a scene at a location. Returns a playback id, or null if it didn't start. */
    String playScene(String sceneName, Location origin);

    /** Plays a scene at a location, optionally looping the whole thing. */
    String playScene(String sceneName, Location origin, boolean loop);

    boolean stopPlayback(String playbackId);

    /**
     * Sets playback rate: 1.0 is real time, 0.5 half, 2.0 double. Floor is 0.01, no ceiling.
     *
     * @return true if that session exists
     */
    boolean setPlaybackSpeed(String playbackId, double speed);

    /**
     * Jumps a playback to a tick (0-based).
     *
     * <p>Going backwards means restarting and fast-forwarding to the target, which can take a
     * moment on a long recording.
     *
     * @return true if that session exists
     */
    boolean seekPlayback(String playbackId, int tick);

    void stopAllPlaybacks();

    Collection<String> getSceneNames();

    boolean deleteScene(String name);
}
