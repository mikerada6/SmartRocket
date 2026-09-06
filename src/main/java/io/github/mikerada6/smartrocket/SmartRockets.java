package io.github.mikerada6.smartrocket;

import io.github.mikerada6.smartrocket.cli.Arguments;
import io.github.mikerada6.smartrocket.report.GenerationLog;
import io.github.mikerada6.smartrocket.simulation.GenerationStats;
import io.github.mikerada6.smartrocket.simulation.Simulation;
import io.github.mikerada6.smartrocket.ui.AppFrame;
import io.github.mikerada6.smartrocket.world.World;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

/** Entry point: parses the command line and runs either the window or a headless batch. */
public final class SmartRockets {

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
        open(arguments, arguments.loadWorld(), false);
    }

    private static void openEditor(Arguments arguments) throws IOException {
        open(arguments, arguments.loadWorldForEditing(), true);
    }

    private static void open(Arguments arguments, World world, boolean editing) {
        String name = arguments.courseFile().map(p -> p.getFileName().toString()).orElse(arguments.course().name());
        Path file = arguments.courseFile().orElse(null);
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(arguments, world, name, file, editing);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
