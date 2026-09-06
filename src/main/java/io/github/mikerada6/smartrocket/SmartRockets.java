package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.io.IOException;
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
        if (arguments.headless()) {
            GenerationStats last = runHeadless(arguments);
            System.out.println(last);
            return;
        }
        runWindowed(arguments);
    }

    /**
     * Runs the configured number of generations with no window, logging each to the CSV.
     *
     * @return statistics of the final generation
     */
    public static GenerationStats runHeadless(Arguments arguments) throws IOException {
        try (GenerationLog log = new GenerationLog(arguments.logPath())) {
            Simulation simulation = new Simulation(arguments.config(), arguments.world(), arguments.newRandom(), log);
            int steps = arguments.headlessGenerations() * arguments.config().lifespan();
            for (int i = 0; i < steps; i++) {
                simulation.step();
            }
            return simulation.lastGeneration();
        }
    }

    private static void runWindowed(Arguments arguments) throws IOException {
        GenerationLog log = new GenerationLog(arguments.logPath());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "could not close " + arguments.logPath(), e);
            }
        }));
        Simulation simulation = new Simulation(arguments.config(), arguments.world(), arguments.newRandom(), log);

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Smart Rockets");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new GamePanel(simulation));
            window.pack();
            window.setVisible(true);
        });
    }
}
