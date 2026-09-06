package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Everything inside the main window: run controls on top, either the simulation or the
 * course editor in the middle, live statistics on the right. Owns the current run and
 * replaces it on restart or course change; every run writes the same generation log, so
 * the log always describes the latest run. A plain panel so it can be tested headless.
 */
public final class Workspace extends JPanel implements ControlBar.Listener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Workspace.class.getName());
    private static final String RUN_CARD = "run";
    private static final String EDIT_CARD = "edit";

    private final transient Arguments arguments;
    private final transient Runnable onLayoutChanged;
    private final GamePanel gamePanel;
    private final StatsPanel statsPanel = new StatsPanel();
    private final ControlBar controls;
    private final JPanel centre = new JPanel(new CardLayout());
    private CourseEditorPanel editor;
    private boolean editing;
    private transient World world;
    private transient String courseName;
    private transient Path courseFile;
    private transient GenerationLog log;

    /**
     * @param onLayoutChanged called when the centre component or course changes, so the
     *                        window can re-pack and retitle itself
     */
    public Workspace(Arguments arguments, World world, String courseName, Path courseFile, Runnable onLayoutChanged) {
        super(new BorderLayout());
        this.arguments = arguments;
        this.world = world;
        this.courseName = courseName;
        this.courseFile = courseFile;
        this.onLayoutChanged = onLayoutChanged;
        this.gamePanel = new GamePanel(newSimulation());
        List<String> names = new ArrayList<>();
        for (CourseLayout layout : CourseLayout.values()) {
            names.add(layout.name());
        }
        this.controls = new ControlBar(this, names, courseName);
        centre.add(gamePanel, RUN_CARD);
        add(controls, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.EAST);
    }

    /** Starts a fresh run on the current world: new log, cleared charts, same seed policy as the command line. */
    private Simulation newSimulation() {
        closeLog();
        try {
            log = new GenerationLog(arguments.logPath());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not open " + arguments.logPath() + "; continuing without a log", e);
            log = null;
        }
        statsPanel.reset(arguments.config().populationSize());
        GenerationLog currentLog = log;
        return new Simulation(arguments.config(), world, arguments.newRandom(), stats -> {
            if (currentLog != null) {
                currentLog.accept(stats);
            }
            statsPanel.accept(stats);
        });
    }

    /** Releases the generation log; safe to call more than once. */
    public void closeLog() {
        if (log == null) {
            return;
        }
        try {
            log.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not close " + arguments.logPath(), e);
        }
        log = null;
    }

    public String courseName() {
        return courseName;
    }

    public boolean isEditing() {
        return editing;
    }

    /** A title describing the current course, seed and mode. */
    public String title() {
        String seed = arguments.seed().isPresent() ? "seed " + arguments.seed().getAsLong() : "random seed";
        return "Smart Rockets - " + courseName + " - " + seed + (editing ? " - editing" : "");
    }

    @Override
    public void setPaused(boolean paused) {
        gamePanel.setPaused(paused || editing);
    }

    @Override
    public void restart() {
        gamePanel.setSimulation(newSimulation());
    }

    @Override
    public void setStepsPerFrame(int steps) {
        gamePanel.setStepsPerFrame(steps);
    }

    @Override
    public void selectCourse(String name) {
        CourseLayout layout;
        try {
            layout = CourseLayout.valueOf(name);
        } catch (IllegalArgumentException e) {
            return;
        }
        SimulationConfig c = arguments.config();
        startOn(layout.create(c.width(), c.height()), name, null);
    }

    @Override
    public void openCourseFile() {
        JFileChooser chooser = new JFileChooser(Path.of("courses").toAbsolutePath().toFile());
        chooser.setDialogTitle("Open course");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            controls.showCourse(courseName);
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        try {
            startOn(CourseFile.read(file), file.getFileName().toString(), file);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, "could not open course " + file, e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot open course", JOptionPane.ERROR_MESSAGE);
            controls.showCourse(courseName);
        }
    }

    /**
     * Switches between running and editing. Entering edit mode pauses the run and opens
     * the editor on the current course; leaving it applies any changes by restarting on
     * the edited course.
     */
    @Override
    public void setEditing(boolean editing) {
        if (this.editing == editing) {
            return;
        }
        this.editing = editing;
        CardLayout cards = (CardLayout) centre.getLayout();
        if (editing) {
            editor = new CourseEditorPanel(world, courseFile, edited -> {
                applyEdit(edited);
                controls.setEditing(false);
            });
            centre.add(editor, EDIT_CARD);
            cards.show(centre, EDIT_CARD);
            gamePanel.setPaused(true);
        } else {
            if (editor != null && editor.model().isDirty()) {
                applyEdit(editor.model().toWorld());
            }
            cards.show(centre, RUN_CARD);
            if (editor != null) {
                centre.remove(editor);
                editor = null;
            }
            gamePanel.setPaused(controls.isPaused());
        }
        onLayoutChanged.run();
    }

    /** Restarts on an edited course; the name marks it as edited unless it came from a file. */
    private void applyEdit(World edited) {
        if (edited.equals(world)) {
            return;
        }
        String name = courseFile != null ? courseFile.getFileName().toString() : courseName + " (edited)";
        startOn(edited, name, courseFile);
    }

    /** Switches to another world and restarts on it. */
    public void startOn(World newWorld, String name, Path file) {
        this.world = newWorld;
        this.courseName = name;
        this.courseFile = file;
        controls.showCourse(name);
        gamePanel.setSimulation(newSimulation());
        onLayoutChanged.run();
    }

    public World world() {
        return world;
    }

    GamePanel gamePanel() {
        return gamePanel;
    }

    StatsPanel statsPanel() {
        return statsPanel;
    }

    ControlBar controls() {
        return controls;
    }

    CourseEditorPanel editor() {
        return editor;
    }
}
