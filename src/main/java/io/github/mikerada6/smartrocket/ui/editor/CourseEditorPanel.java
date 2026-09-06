package io.github.mikerada6.smartrocket.ui.editor;

import io.github.mikerada6.smartrocket.geometry.Vec2;
import io.github.mikerada6.smartrocket.simulation.Rocket;
import io.github.mikerada6.smartrocket.ui.SimulationRenderer;
import io.github.mikerada6.smartrocket.world.Barrier;
import io.github.mikerada6.smartrocket.world.CourseFile;
import io.github.mikerada6.smartrocket.world.Target;
import io.github.mikerada6.smartrocket.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Visual course editor. Drag on empty space to draw a barrier, drag a barrier, its edge,
 * the target or the launch point to move or resize it, click to select and press Delete
 * to remove, scroll over the target to resize it. The toolbar saves the course through
 * {@link CourseFile} and runs it.
 */
public final class CourseEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CourseEditorPanel.class.getName());
    private static final Color SELECTION = Color.YELLOW;
    private static final Color RUBBER_BAND = new Color(255, 255, 255, 120);
    private static final Color GRID_DOT = new Color(255, 255, 255, 40);

    private final transient CourseEditorModel model;
    private final transient SimulationRenderer renderer = new SimulationRenderer();
    private final transient Consumer<World> runner;
    private final Canvas canvas;
    private final JLabel status = new JLabel();
    private final JButton undoButton = new JButton("Undo");
    private final JToggleButton snapButton = new JToggleButton("Snap to grid", true);
    private transient Path file;

    /**
     * @param file   where Save writes, or null to ask on first save
     * @param runner called with the current course when the user presses Run
     */
    public CourseEditorPanel(World initial, Path file, Consumer<World> runner) {
        super(new BorderLayout());
        this.model = new CourseEditorModel(initial);
        this.file = file;
        this.runner = runner;
        // Created here rather than in a field initialiser: the canvas reads the model's size.
        this.canvas = new Canvas();
        add(buildToolbar(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        bindKeys();
        refresh();
    }

    public CourseEditorModel model() {
        return model;
    }

    /** The drawing surface; exposed so tests can send it mouse events. */
    public JComponent canvas() {
        return canvas;
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(button("Save", e -> save(false)));
        bar.add(button("Save As...", e -> save(true)));
        undoButton.addActionListener(e -> {
            model.undo();
            refresh();
        });
        bar.add(undoButton);
        bar.add(button("Clear barriers", e -> {
            model.clearBarriers();
            refresh();
        }));
        snapButton.addActionListener(e -> {
            model.setSnapToGrid(snapButton.isSelected());
            refresh();
        });
        bar.add(snapButton);
        bar.addSeparator();
        bar.add(button("Run", e -> runner.accept(model.toWorld())));
        bar.addSeparator();
        bar.add(new JLabel("  Drag: draw / move / resize  |  Delete: remove  |  Wheel over target: resize"));
        return bar;
    }

    private static JButton button(String label, java.awt.event.ActionListener action) {
        JButton b = new JButton(label);
        b.addActionListener(action);
        return b;
    }

    private void bindKeys() {
        // Cmd on macOS, Ctrl elsewhere; the toolkit cannot say without a display, as in tests.
        int menuMask = GraphicsEnvironment.isHeadless()
                ? java.awt.event.InputEvent.CTRL_DOWN_MASK
                : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        InputMap keys = canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = canvas.getActionMap();
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask), "save");
        actions.put("save", action(() -> save(false)));
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask), "undo");
        actions.put("undo", action(() -> {
            model.undo();
            refresh();
        }));
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        actions.put("delete", action(canvas::deleteSelection));
    }

    private static Action action(Runnable r) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        };
    }

    private void save(boolean askForFile) {
        if (askForFile || file == null) {
            JFileChooser chooser = new JFileChooser(Path.of("courses").toAbsolutePath().toFile());
            chooser.setDialogTitle("Save course");
            if (file != null) {
                chooser.setSelectedFile(file.toFile());
            }
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = chooser.getSelectedFile().toPath();
        }
        try {
            CourseFile.write(model.toWorld(), file);
            model.markSaved();
            refresh();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not save course to " + file, e);
            JOptionPane.showMessageDialog(this, "Could not save " + file + ":\n" + e.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        undoButton.setEnabled(model.canUndo());
        String name = file == null ? "unsaved course" : file.toString();
        status.setText("  " + name + (model.isDirty() ? " (modified)" : "") + "  |  "
                + model.barriers().size() + " barriers  |  target radius " + (int) model.target().radius()
                + "  |  launch " + (int) model.launch().x() + "," + (int) model.launch().y());
        canvas.repaint();
    }

    /** The drawing surface and all mouse handling. */
    private final class Canvas extends JComponent {

        private static final long serialVersionUID = 1L;

        private transient CourseEditorModel.Hit selection = new CourseEditorModel.NoHit();
        private Point dragStart;
        private Point dragCurrent;
        private boolean drawing;
        /** Pointer offset from the grabbed object's origin, so it does not jump to the pointer. */
        private double grabDx;
        private double grabDy;
        /** Whether this drag has already recorded an undo step; a plain click never does. */
        private boolean moveSnapshotTaken;

        Canvas() {
            setPreferredSize(new Dimension(model.width(), model.height()));
            setFocusable(true);
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    dragStart = e.getPoint();
                    dragCurrent = e.getPoint();
                    selection = model.hitTest(e.getX(), e.getY());
                    drawing = selection instanceof CourseEditorModel.NoHit;
                    moveSnapshotTaken = false;
                    grabOffsetFor(selection, e.getX(), e.getY());
                    refresh();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart == null) {
                        return;
                    }
                    moveSelectionTo(e.getPoint());
                    refresh();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragStart == null) {
                        return;
                    }
                    if (drawing) {
                        if (model.addBarrierFromDrag(dragStart.x, dragStart.y, e.getX(), e.getY())) {
                            selection = new CourseEditorModel.BarrierHit(model.barriers().size() - 1);
                        }
                    } else {
                        moveSelectionTo(e.getPoint());
                    }
                    dragStart = null;
                    dragCurrent = null;
                    drawing = false;
                    refresh();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    setCursor(cursorFor(model.hitTest(e.getX(), e.getY())));
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (model.hitTest(e.getX(), e.getY()) instanceof CourseEditorModel.TargetHit) {
                        model.resizeTarget(-e.getWheelRotation());
                        refresh();
                    }
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addMouseWheelListener(mouse);
        }

        private void grabOffsetFor(CourseEditorModel.Hit hit, int x, int y) {
            if (hit instanceof CourseEditorModel.TargetHit) {
                grabDx = x - model.target().centre().x();
                grabDy = y - model.target().centre().y();
            } else if (hit instanceof CourseEditorModel.LaunchHit) {
                grabDx = x - model.launch().x();
                grabDy = y - model.launch().y();
            } else if (hit instanceof CourseEditorModel.BarrierHit b && !b.isEdge()) {
                grabDx = x - model.barriers().get(b.index()).x();
                grabDy = y - model.barriers().get(b.index()).y();
            } else {
                grabDx = 0;
                grabDy = 0;
            }
        }

        /** Moves or resizes whatever is selected so it follows the pointer at {@code p}. */
        private void moveSelectionTo(Point p) {
            if (p.equals(dragCurrent) && moveSnapshotTaken) {
                return;
            }
            if (p.equals(dragCurrent)) {
                return;
            }
            dragCurrent = p;
            if (!moveSnapshotTaken) {
                model.snapshot();
                moveSnapshotTaken = true;
            }
            if (selection instanceof CourseEditorModel.TargetHit) {
                model.placeTarget(p.x - grabDx, p.y - grabDy);
            } else if (selection instanceof CourseEditorModel.LaunchHit) {
                model.placeLaunch(p.x - grabDx, p.y - grabDy);
            } else if (selection instanceof CourseEditorModel.BarrierHit hit) {
                if (hit.isEdge()) {
                    model.resizeBarrier(hit, p.x, p.y);
                } else {
                    model.placeBarrier(hit.index(), (int) Math.round(p.x - grabDx), (int) Math.round(p.y - grabDy));
                }
            }
        }

        private Cursor cursorFor(CourseEditorModel.Hit hit) {
            if (hit instanceof CourseEditorModel.BarrierHit b && b.isEdge()) {
                boolean horizontal = b.left() || b.right();
                boolean vertical = b.top() || b.bottom();
                if (horizontal && vertical) {
                    return Cursor.getPredefinedCursor((b.left() == b.top())
                            ? Cursor.NW_RESIZE_CURSOR : Cursor.NE_RESIZE_CURSOR);
                }
                return Cursor.getPredefinedCursor(horizontal ? Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR);
            }
            if (hit instanceof CourseEditorModel.NoHit) {
                return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            }
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        }

        void deleteSelection() {
            if (selection instanceof CourseEditorModel.BarrierHit hit && hit.index() < model.barriers().size()) {
                model.removeBarrier(hit.index());
                selection = new CourseEditorModel.NoHit();
                refresh();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            renderer.drawWorld(g2, model.toWorld());
            if (model.isSnapToGrid()) {
                g2.setColor(GRID_DOT);
                int step = CourseEditorModel.GRID * 4;
                for (int x = 0; x <= model.width(); x += step) {
                    for (int y = 0; y <= model.height(); y += step) {
                        g2.fillRect(x, y, 1, 1);
                    }
                }
            }
            if (selection instanceof CourseEditorModel.BarrierHit hit && hit.index() < model.barriers().size()) {
                Barrier b = model.barriers().get(hit.index());
                g2.setColor(SELECTION);
                g2.drawRect(b.x(), b.y(), b.width() - 1, b.height() - 1);
            } else if (selection instanceof CourseEditorModel.TargetHit) {
                Target t = model.target();
                int r = (int) Math.round(t.radius());
                g2.setColor(SELECTION);
                g2.drawOval((int) Math.round(t.centre().x()) - r, (int) Math.round(t.centre().y()) - r, 2 * r, 2 * r);
            } else if (selection instanceof CourseEditorModel.LaunchHit) {
                Vec2 l = model.launch();
                g2.setColor(SELECTION);
                g2.drawRect((int) l.x() - 2, (int) l.y() - 2, World.ROCKET_WIDTH + 4, World.ROCKET_HEIGHT + 4);
            }
            if (drawing && dragStart != null && dragCurrent != null) {
                Barrier preview = model.barrierFromDrag(dragStart.x, dragStart.y, dragCurrent.x, dragCurrent.y);
                if (preview != null) {
                    g2.setColor(RUBBER_BAND);
                    g2.fillRect(preview.x(), preview.y(), preview.width(), preview.height());
                }
            }
        }
    }
}
