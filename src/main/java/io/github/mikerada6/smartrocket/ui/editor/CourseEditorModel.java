package io.github.mikerada6.smartrocket.ui.editor;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.simulation.Rocket;
import io.github.mikerada6.smartrocket.world.Barrier;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

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
    /** Grid spacing used while snapping is on. */
    public static final int GRID = 8;
    /** How close to a barrier edge a press counts as grabbing that edge for resizing. */
    public static final int EDGE_GRAB = 6;

    /** What lies under a point: the target, the launch point, a barrier (or one of its edges), or nothing. */
    public sealed interface Hit permits TargetHit, LaunchHit, BarrierHit, NoHit {
    }

    public record TargetHit() implements Hit {
    }

    public record LaunchHit() implements Hit {
    }

    /**
     * A barrier, with which of its edges the point is near. All false means the interior,
     * which moves the barrier; any true edge resizes it.
     */
    public record BarrierHit(int index, boolean left, boolean right, boolean top, boolean bottom) implements Hit {
        public BarrierHit(int index) {
            this(index, false, false, false, false);
        }

        public boolean isEdge() {
            return left || right || top || bottom;
        }
    }

    public record NoHit() implements Hit {
    }

    private final int width;
    private final int height;
    private Target target;
    private Vec2 launch;
    private final List<Barrier> barriers;
    private final Deque<World> undoStack = new ArrayDeque<>();
    private boolean dirty;
    private boolean snapToGrid = true;

    public CourseEditorModel(World world) {
        this.width = world.width();
        this.height = world.height();
        this.target = world.target();
        this.launch = world.launch();
        this.barriers = new ArrayList<>(world.barriers());
    }

    public World toWorld() {
        return new World(width, height, target, barriers, launch);
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

    public Vec2 launch() {
        return launch;
    }

    public List<Barrier> barriers() {
        return List.copyOf(barriers);
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
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
        if (x >= launch.x() - EDGE_GRAB && x <= launch.x() + World.ROCKET_WIDTH + EDGE_GRAB
                && y >= launch.y() - EDGE_GRAB && y <= launch.y() + World.ROCKET_HEIGHT + EDGE_GRAB) {
            return new LaunchHit();
        }
        // Later barriers are drawn on top, so they win the hit test.
        for (int i = barriers.size() - 1; i >= 0; i--) {
            Barrier b = barriers.get(i);
            if (b.overlaps(x - EDGE_GRAB, y - EDGE_GRAB, 2 * EDGE_GRAB + 1, 2 * EDGE_GRAB + 1)) {
                boolean inside = b.overlaps(x, y, 1, 1);
                boolean left = Math.abs(x - b.left()) <= EDGE_GRAB;
                boolean right = Math.abs(x - b.right()) <= EDGE_GRAB;
                boolean top = Math.abs(y - b.top()) <= EDGE_GRAB;
                boolean bottom = Math.abs(y - b.bottom()) <= EDGE_GRAB;
                if (inside || left || right || top || bottom) {
                    return new BarrierHit(i, left, right, top, bottom);
                }
            }
        }
        return new NoHit();
    }

    /**
     * The barrier a drag from one corner to another would create, snapped and clamped to
     * the world, or null if the drag is too small to be a barrier.
     */
    public Barrier barrierFromDrag(int x0, int y0, int x1, int y1) {
        int left = clamp(snap(Math.min(x0, x1)), 0, width);
        int top = clamp(snap(Math.min(y0, y1)), 0, height);
        int right = clamp(snap(Math.max(x0, x1)), 0, width);
        int bottom = clamp(snap(Math.max(y0, y1)), 0, height);
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

    /**
     * Moves a barrier to a new top-left, snapped and kept inside the world. Call
     * {@link #snapshot()} once before a drag starts.
     */
    public void placeBarrier(int index, int x, int y) {
        Barrier b = barriers.get(index);
        int nx = clamp(snap(x), 0, width - b.width());
        int ny = clamp(snap(y), 0, height - b.height());
        barriers.set(index, new Barrier(nx, ny, b.width(), b.height()));
        dirty = true;
    }

    /**
     * Drags the given edges of a barrier to the point, keeping the barrier at least
     * {@link #MIN_BARRIER_SIZE} and inside the world. Call {@link #snapshot()} once before
     * a drag starts.
     */
    public void resizeBarrier(BarrierHit edges, int x, int y) {
        Barrier b = barriers.get(edges.index());
        int left = b.left();
        int right = b.right();
        int top = b.top();
        int bottom = b.bottom();
        if (edges.left()) {
            left = clamp(snap(x), 0, right - MIN_BARRIER_SIZE);
        }
        if (edges.right()) {
            right = clamp(snap(x), left + MIN_BARRIER_SIZE, width);
        }
        if (edges.top()) {
            top = clamp(snap(y), 0, bottom - MIN_BARRIER_SIZE);
        }
        if (edges.bottom()) {
            bottom = clamp(snap(y), top + MIN_BARRIER_SIZE, height);
        }
        barriers.set(edges.index(), new Barrier(left, top, right - left, bottom - top));
        dirty = true;
    }

    /** Moves the target centre, keeping it inside the world. Call {@link #snapshot()} once before a drag starts. */
    public void placeTarget(double x, double y) {
        target = new Target(new Vec2(clamp(x, 0, width), clamp(y, 0, height)), target.radius());
        dirty = true;
    }

    /** Moves the launch point, keeping the rocket inside the world. Call {@link #snapshot()} once before a drag starts. */
    public void placeLaunch(double x, double y) {
        launch = new Vec2(clamp(x, 0, width - World.ROCKET_WIDTH), clamp(y, 0, height - World.ROCKET_HEIGHT));
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
        launch = previous.launch();
        barriers.clear();
        barriers.addAll(previous.barriers());
        dirty = true;
        return true;
    }

    private int snap(int v) {
        return snapToGrid ? (int) Math.round(v / (double) GRID) * GRID : v;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
