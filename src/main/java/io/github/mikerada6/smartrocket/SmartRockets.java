package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.util.Random;

public class SmartRockets {
    public static void main(String[] args) {
        SimulationConfig config = SimulationConfig.defaults();
        World world = World.defaultLayout(config.width(), config.height());
        Simulation simulation = new Simulation(config, world, new Random());

        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Smart Rockets");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new GamePanel(simulation));
            window.pack();
            window.setVisible(true);
        });
    }
}
