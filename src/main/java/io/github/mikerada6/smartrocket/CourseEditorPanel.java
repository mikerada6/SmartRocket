package io.github.mikerada6.smartrocket;

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
 * Visual course editor. Drag on empty space to draw a barrier, drag a barrier or the
 * target to move it, click to select and press Delete to remove, scroll over the target
 * to resize it. The toolbar saves the course through {@link CourseFile} and runs it.
 */
public final class CourseEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CourseEditorPanel.class.getName());
    private static final Color SELECTION = Color.YELLOW;
    private static final Color RUBBER_BAND = new Color(255, 255, 255, 120);

    private final transient CourseEditorModel model;
    private final transient SimulationRenderer renderer = new SimulationRenderer();
    private final transient Consumer<World> runner;
    private final Canvas canvas;
    private final JLabel status = new JLabel();
    private final JButton undoButton = new JButton("Undo");
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
        bar.addSeparator();
        bar.add(button("Run", e -> runner.accept(model.toWorld())));
        bar.addSeparator();
        bar.add(new JLabel("  Drag: draw barrier / move  |  Delete: remove  |  Wheel over target: resize"));
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
            JFileChooser chooser = new JFileChooser();
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
                + model.barriers().size() + " barriers  |  target radius " + (int) model.target().radius());
        canvas.repaint();
    }

    /** The drawing surface and all mouse handling. */
    private final class Canvas extends JComponent {

        private static final long serialVersionUID = 1L;

        private transient CourseEditorModel.Hit selection = new CourseEditorModel.NoHit();
        private Point dragStart;
        private Point dragCurrent;
        private boolean drawing;
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

        /** Moves whatever is selected by the distance from the last drag point to {@code p}. */
        private void moveSelectionTo(Point p) {
            int dx = p.x - dragCurrent.x;
            int dy = p.y - dragCurrent.y;
            dragCurrent = p;
            if (dx == 0 && dy == 0) {
                return;
            }
            if (!moveSnapshotTaken) {
                model.snapshot();
                moveSnapshotTaken = true;
            }
            if (selection instanceof CourseEditorModel.TargetHit) {
                model.moveTarget(dx, dy);
            } else if (selection instanceof CourseEditorModel.BarrierHit hit) {
                model.moveBarrier(hit.index(), dx, dy);
            }
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
            if (selection instanceof CourseEditorModel.BarrierHit hit && hit.index() < model.barriers().size()) {
                Barrier b = model.barriers().get(hit.index());
                g2.setColor(SELECTION);
                g2.drawRect(b.x(), b.y(), b.width() - 1, b.height() - 1);
            } else if (selection instanceof CourseEditorModel.TargetHit) {
                Target t = model.target();
                int r = (int) Math.round(t.radius());
                g2.setColor(SELECTION);
                g2.drawOval((int) Math.round(t.centre().x()) - r, (int) Math.round(t.centre().y()) - r, 2 * r, 2 * r);
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
