package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmartRockets {

    private static final Logger LOG = Logger.getLogger(SmartRockets.class.getName());
    private static final Path GENERATION_LOG = Path.of("generations.csv");

    public static void main(String[] args) throws IOException {
        SimulationConfig config = SimulationConfig.defaults();
        World world = World.defaultLayout(config.width(), config.height());
        GenerationLog log = new GenerationLog(GENERATION_LOG);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "could not close " + GENERATION_LOG, e);
            }
        }));
        Simulation simulation = new Simulation(config, world, new Random(), log);

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Smart Rockets");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new GamePanel(simulation));
            window.pack();
            window.setVisible(true);
        });
    }
}
