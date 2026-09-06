package io.github.mikerada6.smartrocket;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseEditorModelTest {

    private static final World START = new World(400, 300, new Target(new Vec2(200, 40), 20),
            List.of(new Barrier(0, 150, 300, 20)));

    @Test
    void startsCleanWithTheGivenWorld() {
        CourseEditorModel model = new CourseEditorModel(START);
        assertEquals(START, model.toWorld());
        assertFalse(model.isDirty());
        assertFalse(model.canUndo());
    }

    @Test
    void dragCreatesANormalisedBarrierClampedToTheWorld() {
        CourseEditorModel model = new CourseEditorModel(START);
        assertEquals(new Barrier(10, 20, 90, 30), model.barrierFromDrag(100, 50, 10, 20));
        assertEquals(new Barrier(350, 250, 50, 50), model.barrierFromDrag(350, 250, 999, 999));
        assertNull(model.barrierFromDrag(10, 10, 12, 100), "too narrow to be a barrier");
        assertNull(model.barrierFromDrag(10, 10, 10, 10), "a click is not a barrier");
    }

    @Test
    void addingAndUndoingBarriers() {
        CourseEditorModel model = new CourseEditorModel(START);
        assertTrue(model.addBarrierFromDrag(10, 10, 60, 30));
        assertFalse(model.addBarrierFromDrag(10, 10, 11, 11));
        assertEquals(2, model.barriers().size());
        assertTrue(model.isDirty());

        assertTrue(model.undo());
        assertEquals(START.barriers(), model.barriers());
        assertFalse(model.undo());
    }

    @Test
    void hitTestPrefersTargetThenTopmostBarrier() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.addBarrierFromDrag(0, 150, 100, 170);
        assertEquals(new CourseEditorModel.TargetHit(), model.hitTest(205, 45));
        assertEquals(new CourseEditorModel.BarrierHit(1), model.hitTest(50, 160), "later barrier is on top");
        assertEquals(new CourseEditorModel.BarrierHit(0), model.hitTest(250, 160));
        assertEquals(new CourseEditorModel.NoHit(), model.hitTest(390, 290));
    }

    @Test
    void movesStayInsideTheWorld() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.snapshot();
        model.moveBarrier(0, -50, 1000);
        assertEquals(new Barrier(0, 280, 300, 20), model.barriers().get(0));
        model.moveTarget(1000, -1000);
        assertEquals(new Vec2(400, 0), model.target().centre());
        assertTrue(model.undo());
        assertEquals(START, model.toWorld());
    }

    @Test
    void targetResizeHasAFloorAndIsUndoable() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.resizeTarget(-100);
        assertEquals(CourseEditorModel.MIN_TARGET_RADIUS, model.target().radius());
        model.resizeTarget(3);
        assertEquals(CourseEditorModel.MIN_TARGET_RADIUS + 3, model.target().radius());
        model.undo();
        model.undo();
        assertEquals(20, model.target().radius());
    }

    @Test
    void removeAndClearAreUndoable() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.addBarrierFromDrag(10, 10, 60, 30);
        model.removeBarrier(0);
        assertEquals(List.of(new Barrier(10, 10, 50, 20)), model.barriers());
        model.clearBarriers();
        assertTrue(model.barriers().isEmpty());
        model.undo();
        assertEquals(1, model.barriers().size());
        model.undo();
        assertEquals(2, model.barriers().size());
    }

    @Test
    void markSavedClearsDirtyButKeepsUndoHistory() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.addBarrierFromDrag(10, 10, 60, 30);
        model.markSaved();
        assertFalse(model.isDirty());
        assertTrue(model.canUndo());
    }
}
