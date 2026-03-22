package gg.lode.recap.api;

import gg.lode.recap.api.recording.IRecordingManager;
import gg.lode.recap.api.scene.ISceneManager;

public final class RecapAPI {
    private static IRecap instance;

    private RecapAPI() {}

    public static void register(IRecap recap) {
        instance = recap;
    }

    public static IRecap get() {
        if (instance == null) {
            throw new IllegalStateException("Recap has not been initialized yet.");
        }
        return instance;
    }

    public static IRecordingManager getRecordingManager() {
        return get().getRecordingManager();
    }

    public static ISceneManager getSceneManager() {
        return get().getSceneManager();
    }
}
