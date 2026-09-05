package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlBarTest {

    /** Records every call so a test can assert what the widgets asked for. */
    private static final class Recorder implements ControlBar.Listener {
        final List<String> calls = new ArrayList<>();

        @Override
        public void setPaused(boolean paused) {
            calls.add("paused=" + paused);
        }

        @Override
        public void restart() {
            calls.add("restart");
        }

        @Override
        public void setStepsPerFrame(int steps) {
            calls.add("steps=" + steps);
        }

        @Override
        public void selectCourse(String name) {
            calls.add("course=" + name);
        }

        @Override
        public void openCourseFile() {
            calls.add("openFile");
        }

        @Override
        public void setEditing(boolean editing) {
            calls.add("editing=" + editing);
        }

        @Override
        public void setParametersVisible(boolean visible) {
            calls.add("parameters=" + visible);
        }

        @Override
        public void showHelp() {
            calls.add("help");
        }
    }

    @Test
    void widgetsForwardToTheListener() {
        Recorder recorder = new Recorder();
        ControlBar bar = new ControlBar(recorder, List.of("EASY", "CLASSIC"), "EASY");
        assertTrue(recorder.calls.isEmpty(), "construction must not fire anything");

        bar.pauseButton().doClick();
        bar.pauseButton().doClick();
        bar.speedSlider().setValue(25);
        bar.coursePicker().setSelectedItem("CLASSIC");
        bar.coursePicker().setSelectedItem(ControlBar.OPEN_FILE);
        bar.editButton().doClick();
        bar.setEditing(false);
        bar.setEditing(false);
        bar.setParametersVisible(true);
        bar.setStepsPerFrame(40);

        assertEquals(List.of("paused=true", "paused=false", "steps=25", "course=CLASSIC", "openFile",
                "editing=true", "editing=false", "parameters=true", "steps=40"), recorder.calls);
        assertEquals(40, bar.stepsPerFrame());
    }

    @Test
    void showCourseAddsUnknownNamesWithoutFiringTheListener() {
        Recorder recorder = new Recorder();
        ControlBar bar = new ControlBar(recorder, List.of("EASY", "CLASSIC"), "EASY");
        bar.showCourse("mine.course");
        assertEquals("mine.course", bar.coursePicker().getSelectedItem());
        assertEquals(4, bar.coursePicker().getItemCount(), "EASY, CLASSIC, mine.course, open file");
        assertTrue(recorder.calls.isEmpty());
    }
}
