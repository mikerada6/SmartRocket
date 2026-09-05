package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceTest {

    private static Workspace workspace(Path dir, AtomicInteger layoutChanges) {
        Arguments arguments = Arguments.parse(new String[]{
                "--population", "20", "--lifespan", "30", "--width", "400", "--height", "300", "--seed", "1",
                "--log", dir.resolve("run.csv").toString()});
        World world = CourseLayout.EASY.create(400, 300);
        return new Workspace(arguments, world, "EASY", null, layoutChanges::incrementAndGet);
    }

    private static void drag(CourseEditorPanel editor, int x0, int y0, int x1, int y1) {
        var canvas = editor.canvas();
        var press = new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, 0, 0, x0, y0, 1, false, MouseEvent.BUTTON1);
        var release = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, 0, 0, x1, y1, 1, false, MouseEvent.BUTTON1);
        for (var l : canvas.getMouseListeners()) {
            l.mousePressed(press);
            l.mouseReleased(release);
        }
    }

    @Test
    void enteringEditModePausesAndLeavingWithoutChangesResumesTheSameRun(@TempDir Path dir) {
        AtomicInteger layoutChanges = new AtomicInteger();
        Workspace ws = workspace(dir, layoutChanges);
        Simulation run = ws.gamePanel().simulation();
        assertFalse(ws.isEditing());
        assertNull(ws.editor());

        ws.controls().setEditing(true);
        assertTrue(ws.isEditing());
        assertNotNull(ws.editor());
        assertTrue(ws.gamePanel().isPaused());
        assertTrue(ws.title().endsWith("editing"));
        assertEquals(1, layoutChanges.get());

        ws.controls().setEditing(false);
        assertFalse(ws.isEditing());
        assertNull(ws.editor());
        assertFalse(ws.gamePanel().isPaused());
        assertEquals(run, ws.gamePanel().simulation(), "no edits, so the same run continues");
    }

    @Test
    void leavingEditModeWithChangesRestartsOnTheEditedCourse(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        Simulation before = ws.gamePanel().simulation();
        ws.controls().setEditing(true);
        drag(ws.editor(), 10, 200, 100, 250);
        ws.controls().setEditing(false);

        assertEquals(2, ws.world().barriers().size());
        assertEquals("EASY (edited)", ws.courseName());
        assertTrue(ws.gamePanel().simulation() != before, "a new run was started");
        assertEquals(ws.world(), ws.gamePanel().simulation().world());
        assertEquals("EASY (edited)", ws.controls().coursePicker().getSelectedItem());
    }

    @Test
    void theEditorsRunButtonAppliesAndReturnsToRunMode(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        ws.controls().setEditing(true);
        drag(ws.editor(), 10, 200, 100, 250);
        for (var button : findButtons(ws.editor())) {
            if (button.getText().equals("Run")) {
                button.doClick();
            }
        }
        assertFalse(ws.isEditing());
        assertEquals(2, ws.world().barriers().size());
        assertFalse(ws.gamePanel().isPaused());
    }

    @Test
    void pauseIsRememberedAcrossEditMode(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        ws.controls().pauseButton().doClick();
        assertTrue(ws.gamePanel().isPaused());
        ws.controls().setEditing(true);
        ws.controls().setEditing(false);
        assertTrue(ws.gamePanel().isPaused(), "the user's pause survives a trip through the editor");
    }

    @Test
    void selectingABuiltInCourseRestartsOnIt(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        ws.controls().coursePicker().setSelectedItem("CLASSIC");
        assertEquals(CourseLayout.CLASSIC.create(400, 300), ws.world());
        assertEquals("CLASSIC", ws.courseName());
        assertEquals(0, ws.gamePanel().simulation().generation());
    }

    private static java.util.List<javax.swing.JButton> findButtons(java.awt.Container root) {
        java.util.List<javax.swing.JButton> found = new java.util.ArrayList<>();
        for (java.awt.Component c : root.getComponents()) {
            if (c instanceof javax.swing.JButton b) {
                found.add(b);
            } else if (c instanceof java.awt.Container inner) {
                found.addAll(findButtons(inner));
            }
        }
        return found;
    }

    @Test
    void applyingParametersRestartsWithTheNewConfiguration(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        assertFalse(ws.isParametersVisible());
        ws.controls().setParametersVisible(true);
        assertTrue(ws.isParametersVisible());

        ws.parameters().populationSpinner().setValue(35);
        ws.parameters().applyButton().doClick();

        assertEquals(35, ws.config().populationSize());
        assertEquals(35, ws.gamePanel().simulation().population().getRockets().size());
        assertEquals(400, ws.config().width(), "world size still follows the course");
    }

    @Test
    void configurationFollowsTheCourseSizeButKeepsAppliedSettings(@TempDir Path dir) {
        Workspace ws = workspace(dir, new AtomicInteger());
        ws.parameters().populationSpinner().setValue(12);
        ws.parameters().applyButton().doClick();
        ws.startOn(new World(640, 480, new Target(new Vec2(320, 30), 20), java.util.List.of()), "big", null);
        assertEquals(640, ws.config().width());
        assertEquals(12, ws.config().populationSize());
        assertEquals(640, ws.gamePanel().simulation().world().width());
    }
}
