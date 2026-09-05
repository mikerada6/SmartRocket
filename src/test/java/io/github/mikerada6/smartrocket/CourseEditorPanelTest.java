package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Drives the editor's canvas with synthetic mouse events; no window is needed for that. */
class CourseEditorPanelTest {

    private static final World START = new World(400, 300, new Target(new Vec2(200, 40), 20),
            List.of(new Barrier(0, 150, 300, 20)));

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
        CourseEditorPanel panel = new CourseEditorPanel(START, null, w -> { });
        press(panel, 20, 200);
        drag(panel, 60, 230);
        release(panel, 120, 260);
        assertEquals(List.of(new Barrier(0, 150, 300, 20), new Barrier(20, 200, 100, 60)),
                panel.model().barriers());
    }

    @Test
    void draggingTheTargetMovesItAndDraggingABarrierMovesIt() {
        CourseEditorPanel panel = new CourseEditorPanel(START, null, w -> { });
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
    void runHandsTheCurrentCourseToTheRunner() {
        AtomicReference<World> ran = new AtomicReference<>();
        CourseEditorPanel panel = new CourseEditorPanel(START, null, ran::set);
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
}
