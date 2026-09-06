package io.github.mikerada6.smartrocket;

import javax.swing.*;

public class SmartRockets {
    public static void main(String[] args) {
        JFrame window = new JFrame("Smart Rockets");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setContentPane(new GamePanel());

        window.pack();
        window.setVisible(true);
    }
}