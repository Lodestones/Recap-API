package gg.lode.recap.api;

import gg.lode.recap.api.recording.IRecordingManager;
import gg.lode.recap.api.scene.ISceneManager;

public interface IRecap {
    IRecordingManager getRecordingManager();
    ISceneManager getSceneManager();
}
