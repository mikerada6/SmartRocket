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
 * The main window: run controls on top, the simulation in the middle, live statistics on
 * the right. Owns the current run and replaces it on restart or course change; every run
 * writes the same generation log, so the log always describes the latest run.
 */
public final class AppFrame extends JFrame implements ControlBar.Listener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AppFrame.class.getName());

    private final transient Arguments arguments;
    private final GamePanel gamePanel;
    private final StatsPanel statsPanel = new StatsPanel();
    private final ControlBar controls;
    private transient World world;
    private transient String courseName;
    private transient Path courseFile;
    private transient GenerationLog log;

    public AppFrame(Arguments arguments, World world, String courseName, Path courseFile) {
        super("Smart Rockets");
        this.arguments = arguments;
        this.world = world;
        this.courseName = courseName;
        this.courseFile = courseFile;
        this.gamePanel = new GamePanel(newSimulation());
        List<String> names = new ArrayList<>();
        for (CourseLayout layout : CourseLayout.values()) {
            names.add(layout.name());
        }
        this.controls = new ControlBar(this, names, courseName);
        if (!names.contains(courseName)) {
            controls.showCourse(courseName);
        }
        setLayout(new BorderLayout());
        add(controls, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.EAST);
        updateTitle();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                closeLog();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeLog));
        pack();
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

    private void closeLog() {
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

    private void updateTitle() {
        String seed = arguments.seed().isPresent() ? "seed " + arguments.seed().getAsLong() : "random seed";
        setTitle("Smart Rockets - " + courseName + " - " + seed);
    }

    @Override
    public void setPaused(boolean paused) {
        gamePanel.setPaused(paused);
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

    @Override
    public void editCourse() {
        JFrame editor = new JFrame("Smart Rockets course editor - " + courseName);
        editor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editor.setContentPane(new CourseEditorPanel(world, courseFile,
                edited -> startOn(edited, courseFile == null ? "edited course" : courseFile.getFileName().toString(), courseFile)));
        editor.pack();
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
    }

    /** Switches to another world and restarts on it. */
    public void startOn(World newWorld, String name, Path file) {
        this.world = newWorld;
        this.courseName = name;
        this.courseFile = file;
        controls.showCourse(name);
        updateTitle();
        gamePanel.setSimulation(newSimulation());
        pack();
    }

    GamePanel gamePanel() {
        return gamePanel;
    }

    StatsPanel statsPanel() {
        return statsPanel;
    }
}
