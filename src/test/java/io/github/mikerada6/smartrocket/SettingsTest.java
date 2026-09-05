package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {

    private final Preferences node = Preferences.userRoot().node("io/github/mikerada6/smartrocket-test-" + System.nanoTime());

    @AfterEach
    void removeNode() throws BackingStoreException {
        node.removeNode();
    }

    @Test
    void defaultsWhenNothingSaved() {
        Settings settings = new Settings(node);
        assertNull(settings.windowBounds());
        assertEquals(1, settings.stepsPerFrame());
        assertFalse(settings.showParameters());
    }

    @Test
    void roundTripsWhatWasSaved() {
        Settings settings = new Settings(node);
        settings.saveWindowBounds(new Rectangle(10, 20, 1300, 900));
        settings.saveStepsPerFrame(33);
        settings.saveShowParameters(true);
        assertEquals(new Rectangle(10, 20, 1300, 900), settings.windowBounds());
        assertEquals(33, settings.stepsPerFrame());
        assertTrue(settings.showParameters());
    }

    @Test
    void speedIsClampedToTheSliderRange() {
        node.putInt("stepsPerFrame", 999);
        assertEquals(ControlBar.MAX_STEPS_PER_FRAME, new Settings(node).stepsPerFrame());
        node.putInt("stepsPerFrame", -4);
        assertEquals(1, new Settings(node).stepsPerFrame());
    }
}
