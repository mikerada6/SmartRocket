package io.github.mikerada6.smartrocket.ui;

import java.awt.Rectangle;
import java.util.prefs.Preferences;

/** Small per-user preferences that make the window come back the way it was left. */
public final class Settings {

    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";
    private static final String STEPS_PER_FRAME = "stepsPerFrame";
    private static final String SHOW_PARAMETERS = "showParameters";

    private final Preferences node;

    public Settings(Preferences node) {
        this.node = node;
    }

    /** The user's own settings, under a node named for this application. */
    public static Settings forUser() {
        return new Settings(Preferences.userRoot().node("io/github/mikerada6/smartrocket"));
    }

    /** Last saved window bounds, or null if none were saved. */
    public Rectangle windowBounds() {
        int w = node.getInt(WINDOW_WIDTH, -1);
        int h = node.getInt(WINDOW_HEIGHT, -1);
        if (w <= 0 || h <= 0) {
            return null;
        }
        return new Rectangle(node.getInt(WINDOW_X, 0), node.getInt(WINDOW_Y, 0), w, h);
    }

    public void saveWindowBounds(Rectangle bounds) {
        node.putInt(WINDOW_X, bounds.x);
        node.putInt(WINDOW_Y, bounds.y);
        node.putInt(WINDOW_WIDTH, bounds.width);
        node.putInt(WINDOW_HEIGHT, bounds.height);
    }

    public int stepsPerFrame() {
        return Math.max(1, Math.min(ControlBar.MAX_STEPS_PER_FRAME, node.getInt(STEPS_PER_FRAME, 1)));
    }

    public void saveStepsPerFrame(int steps) {
        node.putInt(STEPS_PER_FRAME, steps);
    }

    public boolean showParameters() {
        return node.getBoolean(SHOW_PARAMETERS, false);
    }

    public void saveShowParameters(boolean show) {
        node.putBoolean(SHOW_PARAMETERS, show);
    }
}
