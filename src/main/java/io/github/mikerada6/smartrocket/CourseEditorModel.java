package io.github.mikerada6.smartrocket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The course being edited, with undo. Holds no Swing so every editing operation can be
 * tested headless; {@link CourseEditorPanel} only translates mouse and key events into
 * calls on this class.
 */
public final class CourseEditorModel {

    /** Smallest barrier a drag can create; anything smaller is treated as a click. */
    public static final int MIN_BARRIER_SIZE = 4;
    public static final double MIN_TARGET_RADIUS = 5;

    /** What lies under a point: the target, a barrier by index, or nothing. */
    public sealed interface Hit permits TargetHit, BarrierHit, NoHit {
    }

    public record TargetHit() implements Hit {
    }

    public record BarrierHit(int index) implements Hit {
    }

    public record NoHit() implements Hit {
    }

    private final int width;
    private final int height;
    private Target target;
    private final List<Barrier> barriers;
    private final Deque<World> undoStack = new ArrayDeque<>();
    private boolean dirty;

    public CourseEditorModel(World world) {
        this.width = world.width();
        this.height = world.height();
        this.target = world.target();
        this.barriers = new ArrayList<>(world.barriers());
    }

    public World toWorld() {
        return new World(width, height, target, barriers);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Target target() {
        return target;
    }

    public List<Barrier> barriers() {
        return List.copyOf(barriers);
    }

    /** True once anything changed since construction or the last {@link #markSaved()}. */
    public boolean isDirty() {
        return dirty;
    }

    public void markSaved() {
        dirty = false;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public Hit hitTest(double x, double y) {
        if (target.contains(new Vec2(x, y))) {
            return new TargetHit();
        }
        // Later barriers are drawn on top, so they win the hit test.
        for (int i = barriers.size() - 1; i >= 0; i--) {
            if (barriers.get(i).overlaps(x, y, 1, 1)) {
                return new BarrierHit(i);
            }
        }
        return new NoHit();
    }

    /**
     * The barrier a drag from one corner to another would create, clamped to the world,
     * or null if the drag is too small to be a barrier.
     */
    public Barrier barrierFromDrag(int x0, int y0, int x1, int y1) {
        int left = clamp(Math.min(x0, x1), 0, width);
        int top = clamp(Math.min(y0, y1), 0, height);
        int right = clamp(Math.max(x0, x1), 0, width);
        int bottom = clamp(Math.max(y0, y1), 0, height);
        if (right - left < MIN_BARRIER_SIZE || bottom - top < MIN_BARRIER_SIZE) {
            return null;
        }
        return new Barrier(left, top, right - left, bottom - top);
    }

    /** Adds the barrier a drag describes, if it is big enough. Returns whether one was added. */
    public boolean addBarrierFromDrag(int x0, int y0, int x1, int y1) {
        Barrier barrier = barrierFromDrag(x0, y0, x1, y1);
        if (barrier == null) {
            return false;
        }
        snapshot();
        barriers.add(barrier);
        return true;
    }

    public void removeBarrier(int index) {
        snapshot();
        barriers.remove(index);
    }

    public void clearBarriers() {
        if (barriers.isEmpty()) {
            return;
        }
        snapshot();
        barriers.clear();
    }

    /** Moves a barrier by an offset, keeping it inside the world. Call {@link #snapshot()} once before a drag starts. */
    public void moveBarrier(int index, int dx, int dy) {
        Barrier b = barriers.get(index);
        int x = clamp(b.x() + dx, 0, width - b.width());
        int y = clamp(b.y() + dy, 0, height - b.height());
        barriers.set(index, new Barrier(x, y, b.width(), b.height()));
        dirty = true;
    }

    /** Moves the target centre, keeping it inside the world. Call {@link #snapshot()} once before a drag starts. */
    public void moveTarget(double dx, double dy) {
        double x = clamp(target.centre().x() + dx, 0, width);
        double y = clamp(target.centre().y() + dy, 0, height);
        target = new Target(new Vec2(x, y), target.radius());
        dirty = true;
    }

    public void resizeTarget(double deltaRadius) {
        double radius = Math.max(MIN_TARGET_RADIUS, target.radius() + deltaRadius);
        if (radius == target.radius()) {
            return;
        }
        snapshot();
        target = new Target(target.centre(), radius);
    }

    /** Records the current state so the next change can be undone as one step. */
    public void snapshot() {
        undoStack.push(toWorld());
        dirty = true;
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        World previous = undoStack.pop();
        target = previous.target();
        barriers.clear();
        barriers.addAll(previous.barriers());
        dirty = true;
        return true;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
