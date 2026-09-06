package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Entry point: parses the command line and runs either the window or a headless batch. */
public final class SmartRockets {

    private static final Logger LOG = Logger.getLogger(SmartRockets.class.getName());

    private SmartRockets() {
    }

    public static void main(String[] args) throws IOException {
        Arguments arguments;
        try {
            arguments = Arguments.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.print(Arguments.USAGE);
            System.exit(2);
            return;
        }
        if (arguments.help()) {
            System.out.print(Arguments.USAGE);
            return;
        }
        try {
            if (arguments.headless()) {
                GenerationStats last = runHeadless(arguments);
                System.out.println(last);
                return;
            }
            if (arguments.edit()) {
                openEditor(arguments);
                return;
            }
            runWindowed(arguments);
        } catch (IllegalArgumentException e) {
            // A malformed course file is a user error, not a crash.
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Runs the configured number of generations with no window, logging each to the CSV.
     *
     * @return statistics of the final generation
     */
    public static GenerationStats runHeadless(Arguments arguments) throws IOException {
        World world = arguments.loadWorld();
        try (GenerationLog log = new GenerationLog(arguments.logPath())) {
            Simulation simulation = new Simulation(arguments.config(), world, arguments.newRandom(), log);
            int steps = arguments.headlessGenerations() * arguments.config().lifespan();
            for (int i = 0; i < steps; i++) {
                simulation.step();
            }
            return simulation.lastGeneration();
        }
    }

    private static void runWindowed(Arguments arguments) throws IOException {
        World world = arguments.loadWorld();
        SwingUtilities.invokeLater(() -> openSimulationWindow(arguments, world, JFrame.EXIT_ON_CLOSE));
    }

    private static void openEditor(Arguments arguments) throws IOException {
        World world = arguments.loadWorldForEditing();
        Path file = arguments.courseFile().orElse(null);
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Smart Rockets course editor");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Each Run opens its own simulation window; closing one leaves the editor open.
            window.setContentPane(new CourseEditorPanel(world, file,
                    edited -> openSimulationWindow(arguments, edited, JFrame.DISPOSE_ON_CLOSE)));
            window.pack();
            window.setVisible(true);
        });
    }

    /** Opens a simulation window on the event dispatch thread; the log is closed when the window closes. */
    private static void openSimulationWindow(Arguments arguments, World world, int closeOperation) {
        GenerationLog log;
        try {
            log = new GenerationLog(arguments.logPath());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "could not open " + arguments.logPath(), e);
            JOptionPane.showMessageDialog(null, "Could not open " + arguments.logPath() + ":\n" + e.getMessage(),
                    "Cannot start", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Simulation simulation = new Simulation(arguments.config(), world, arguments.newRandom(), log);
        JFrame window = new JFrame("Smart Rockets");
        window.setDefaultCloseOperation(closeOperation);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                closeQuietly(log, arguments.logPath());
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeQuietly(log, arguments.logPath())));
        window.setContentPane(new GamePanel(simulation));
        window.pack();
        window.setVisible(true);
    }

    private static void closeQuietly(GenerationLog log, Path path) {
        try {
            log.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not close " + path, e);
        }
    }
}
