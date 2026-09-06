package io.github.mikerada6.smartrocket.ui.editor;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.world.Barrier;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

import org.junit.jupiter.api.Test;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Drives the editor's canvas with synthetic mouse events; no window is needed for that. */
class CourseEditorPanelTest {

    private static final World START = new World(400, 300, new Target(new Vec2(200, 40), 20),
            List.of(new Barrier(0, 150, 300, 20)));

    /** An editor on the sample course with snapping off, so coordinates in tests are exact. */
    private static CourseEditorPanel editor(java.util.function.Consumer<World> runner) {
        CourseEditorPanel panel = new CourseEditorPanel(START, null, runner);
        panel.model().setSnapToGrid(false);
        return panel;
    }

    private static void press(CourseEditorPanel panel, int x, int y) {
        dispatch(panel, MouseEvent.MOUSE_PRESSED, x, y);
    }

    private static void drag(CourseEditorPanel panel, int x, int y) {
        dispatch(panel, MouseEvent.MOUSE_DRAGGED, x, y);
    }

    private static void release(CourseEditorPanel panel, int x, int y) {
        dispatch(panel, MouseEvent.MOUSE_RELEASED, x, y);
    }

    private static void dispatch(CourseEditorPanel panel, int id, int x, int y) {
        MouseEvent event = new MouseEvent(panel.canvas(), id, System.currentTimeMillis(), 0, x, y, 1, false,
                MouseEvent.BUTTON1);
        for (var listener : panel.canvas().getMouseListeners()) {
            switch (id) {
                case MouseEvent.MOUSE_PRESSED -> listener.mousePressed(event);
                case MouseEvent.MOUSE_RELEASED -> listener.mouseReleased(event);
                default -> { }
            }
        }
        if (id == MouseEvent.MOUSE_DRAGGED) {
            for (var listener : panel.canvas().getMouseMotionListeners()) {
                listener.mouseDragged(event);
            }
        }
    }

    @Test
    void draggingOnEmptySpaceDrawsABarrier() {
        CourseEditorPanel panel = editor(w -> { });
        press(panel, 20, 200);
        drag(panel, 60, 230);
        release(panel, 120, 260);
        assertEquals(List.of(new Barrier(0, 150, 300, 20), new Barrier(20, 200, 100, 60)),
                panel.model().barriers());
    }

    @Test
    void draggingTheTargetMovesItAndDraggingABarrierMovesIt() {
        CourseEditorPanel panel = editor(w -> { });
        press(panel, 200, 40);
        drag(panel, 210, 50);
        release(panel, 230, 60);
        assertEquals(new Vec2(230, 60), panel.model().target().centre());

        press(panel, 100, 160);
        drag(panel, 100, 170);
        release(panel, 100, 180);
        assertEquals(new Barrier(0, 170, 300, 20), panel.model().barriers().get(0));
    }

    @Test
    void draggingAnEdgeResizesAndDraggingTheLaunchPointMovesIt() {
        CourseEditorPanel panel = editor(w -> { });
        press(panel, 300, 160);
        drag(panel, 250, 160);
        release(panel, 220, 160);
        assertEquals(new Barrier(0, 150, 220, 20), panel.model().barriers().get(0));

        Vec2 launch = panel.model().launch();
        press(panel, (int) launch.x() + 2, (int) launch.y() + 10);
        release(panel, (int) launch.x() + 52, (int) launch.y() - 90);
        assertEquals(new Vec2(launch.x() + 50, launch.y() - 100), panel.model().launch());
    }

    @Test
    void runHandsTheCurrentCourseToTheRunner() {
        AtomicReference<World> ran = new AtomicReference<>();
        CourseEditorPanel panel = editor(ran::set);
        press(panel, 20, 200);
        release(panel, 120, 260);
        for (var button : findButtons(panel)) {
            if (button.getText().equals("Run")) {
                button.doClick();
            }
        }
        assertEquals(panel.model().toWorld(), ran.get());
        assertEquals(2, ran.get().barriers().size());
    }

    private static List<javax.swing.JButton> findButtons(java.awt.Container root) {
        List<javax.swing.JButton> found = new java.util.ArrayList<>();
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
    void clickingWithoutMovingNeitherDirtiesNorAddsAnUndoStep() {
        CourseEditorPanel panel = editor(w -> { });
        press(panel, 100, 160);
        release(panel, 100, 160);
        assertFalse(panel.model().isDirty());
        assertFalse(panel.model().canUndo());
        assertEquals(START, panel.model().toWorld());

        press(panel, 100, 160);
        drag(panel, 100, 165);
        release(panel, 100, 165);
        assertTrue(panel.model().isDirty());
        assertTrue(panel.model().canUndo());
        assertTrue(panel.model().undo());
        assertEquals(START, panel.model().toWorld(), "one drag is one undo step");
    }
}
