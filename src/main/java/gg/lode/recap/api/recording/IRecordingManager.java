package gg.lode.recap.api.recording;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface IRecordingManager {

    /**
     * Start recording a player's actions.
     *
     * @param player the player to record
     * @param name   a name for this recording
     * @return true if recording started successfully
     */
    boolean startRecording(Player player, String name);

    /**
     * Stop recording a player.
     *
     * @param player the player to stop recording
     * @return true if a recording was stopped and saved
     */
    boolean stopRecording(Player player);

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
}
