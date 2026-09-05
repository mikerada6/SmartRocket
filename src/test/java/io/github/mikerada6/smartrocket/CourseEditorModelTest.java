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

    private static CourseEditorModel unsnapped() {
        CourseEditorModel model = new CourseEditorModel(START);
        model.setSnapToGrid(false);
        return model;
    }

    @Test
    void startsCleanWithTheGivenWorld() {
        CourseEditorModel model = new CourseEditorModel(START);
        assertEquals(START, model.toWorld());
        assertEquals(World.defaultLaunch(400, 300), model.launch());
        assertFalse(model.isDirty());
        assertFalse(model.canUndo());
        assertTrue(model.isSnapToGrid(), "snapping is on by default");
    }

    @Test
    void dragCreatesANormalisedBarrierClampedToTheWorld() {
        CourseEditorModel model = unsnapped();
        assertEquals(new Barrier(10, 20, 90, 30), model.barrierFromDrag(100, 50, 10, 20));
        assertEquals(new Barrier(350, 250, 50, 50), model.barrierFromDrag(350, 250, 999, 999));
        assertNull(model.barrierFromDrag(10, 10, 12, 100), "too narrow to be a barrier");
        assertNull(model.barrierFromDrag(10, 10, 10, 10), "a click is not a barrier");
    }

    @Test
    void snappingRoundsBarrierCornersToTheGrid() {
        CourseEditorModel model = new CourseEditorModel(START);
        assertEquals(new Barrier(8, 24, 96, 32), model.barrierFromDrag(10, 22, 101, 55));
        model.snapshot();
        model.placeBarrier(0, 13, 100);
        assertEquals(new Barrier(16, 104, 300, 20), model.barriers().get(0));
    }

    @Test
    void addingAndUndoingBarriers() {
        CourseEditorModel model = unsnapped();
        assertTrue(model.addBarrierFromDrag(10, 10, 60, 30));
        assertFalse(model.addBarrierFromDrag(10, 10, 11, 11));
        assertEquals(2, model.barriers().size());
        assertTrue(model.isDirty());

        assertTrue(model.undo());
        assertEquals(START.barriers(), model.barriers());
        assertFalse(model.undo());
    }

    @Test
    void hitTestPrefersTargetThenLaunchThenTopmostBarrier() {
        CourseEditorModel model = unsnapped();
        model.addBarrierFromDrag(0, 150, 100, 170);
        assertEquals(new CourseEditorModel.TargetHit(), model.hitTest(205, 45));
        Vec2 launch = model.launch();
        assertEquals(new CourseEditorModel.LaunchHit(), model.hitTest(launch.x() + 2, launch.y() + 5));
        assertEquals(new CourseEditorModel.BarrierHit(1), model.hitTest(50, 160), "later barrier is on top");
        assertEquals(new CourseEditorModel.BarrierHit(0), model.hitTest(250, 160));
        assertEquals(new CourseEditorModel.NoHit(), model.hitTest(390, 100));
    }

    @Test
    void hitTestReportsBarrierEdgesAndCorners() {
        CourseEditorModel model = unsnapped();
        assertEquals(new CourseEditorModel.BarrierHit(0, true, false, false, false), model.hitTest(2, 160));
        assertEquals(new CourseEditorModel.BarrierHit(0, false, true, false, false), model.hitTest(303, 160));
        assertEquals(new CourseEditorModel.BarrierHit(0, false, false, true, false), model.hitTest(150, 148));
        assertEquals(new CourseEditorModel.BarrierHit(0, false, true, false, true), model.hitTest(302, 173),
                "bottom-right corner");
        assertFalse(model.hitTest(150, 160) instanceof CourseEditorModel.BarrierHit b && b.isEdge(), "interior");
    }

    @Test
    void resizingDragsOnlyTheGrabbedEdgesWithAMinimumSize() {
        CourseEditorModel model = unsnapped();
        model.snapshot();
        model.resizeBarrier(new CourseEditorModel.BarrierHit(0, false, true, false, false), 200, 999);
        assertEquals(new Barrier(0, 150, 200, 20), model.barriers().get(0));
        model.resizeBarrier(new CourseEditorModel.BarrierHit(0, false, false, true, false), 999, 100);
        assertEquals(new Barrier(0, 100, 200, 70), model.barriers().get(0));
        model.resizeBarrier(new CourseEditorModel.BarrierHit(0, true, false, false, false), 999, 0);
        assertEquals(200 - CourseEditorModel.MIN_BARRIER_SIZE, model.barriers().get(0).x(), "cannot cross the right edge");
        assertTrue(model.undo());
        assertEquals(START.barriers(), model.barriers());
    }

    @Test
    void placementsStayInsideTheWorld() {
        CourseEditorModel model = unsnapped();
        model.snapshot();
        model.placeBarrier(0, -50, 1000);
        assertEquals(new Barrier(0, 280, 300, 20), model.barriers().get(0));
        model.placeTarget(1000, -1000);
        assertEquals(new Vec2(400, 0), model.target().centre());
        model.placeLaunch(1000, 1000);
        assertEquals(new Vec2(400 - Rocket.WIDTH, 300 - Rocket.HEIGHT), model.launch());
        assertTrue(model.undo());
        assertEquals(START, model.toWorld());
    }

    @Test
    void launchPointIsPartOfTheCourse() {
        CourseEditorModel model = unsnapped();
        model.snapshot();
        model.placeLaunch(20, 30);
        assertEquals(new Vec2(20, 30), model.toWorld().launch());
        assertTrue(model.isDirty());
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
        CourseEditorModel model = unsnapped();
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
        CourseEditorModel model = unsnapped();
        model.addBarrierFromDrag(10, 10, 60, 30);
        model.markSaved();
        assertFalse(model.isDirty());
        assertTrue(model.canUndo());
    }
}
