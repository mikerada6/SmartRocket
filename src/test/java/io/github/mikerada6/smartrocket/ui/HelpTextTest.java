package io.github.mikerada6.smartrocket.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpTextTest {

    @Test
    void mentionsEveryControlAndShortcut() {
        for (String item : new String[]{"Pause", "Restart", "Speed", "Course", "Parameters", "Edit course",
                "Delete", "Cmd/Ctrl-S", "Cmd/Ctrl-Z", "Snap to grid", "Run", "elite", "crashed"}) {
            assertTrue(HelpText.CONTROLS.contains(item), "help lacks " + item);
        }
    }
}
