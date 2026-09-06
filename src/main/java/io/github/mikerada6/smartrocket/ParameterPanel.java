package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Side panel of algorithm settings. Edits are staged in the spinners and take effect
 * only when Apply is pressed, which restarts the run with the new configuration.
 */
public final class ParameterPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JSpinner population;
    private final JSpinner lifespan;
    private final JSpinner mutationRate;
    private final JSpinner maxSpeed;
    private final JCheckBox limitSpeed;
    private final JSpinner elitePercent;
    private final JButton apply = new JButton("Apply and restart");
    private final JLabel error = new JLabel(" ");
    private transient SimulationConfig current;

    /** @param onApply receives the new configuration when Apply is pressed with valid values */
    public ParameterPanel(SimulationConfig initial, Consumer<SimulationConfig> onApply) {
        super(new GridBagLayout());
        this.current = initial;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        population = new JSpinner(new SpinnerNumberModel(initial.populationSize(), 1, 100_000, 100));
        lifespan = new JSpinner(new SpinnerNumberModel(initial.lifespan(), 1, 5000, 10));
        mutationRate = new JSpinner(new SpinnerNumberModel(initial.mutationRate(), 0.0, 1.0, 0.005));
        boolean limited = initial.maxSpeed() != SimulationConfig.UNLIMITED_SPEED;
        maxSpeed = new JSpinner(new SpinnerNumberModel(limited ? initial.maxSpeed() : SimulationConfig.DEFAULT_MAX_SPEED,
                0.5, 1000.0, 1.0));
        limitSpeed = new JCheckBox("Limit speed", limited);
        limitSpeed.addActionListener(e -> maxSpeed.setEnabled(limitSpeed.isSelected()));
        maxSpeed.setEnabled(limited);
        elitePercent = new JSpinner(new SpinnerNumberModel(initial.eliteFraction() * 100, 0.0, 100.0, 0.5));

        int row = 0;
        addRow(row++, "Rockets", population, "Rockets per generation");
        addRow(row++, "Lifespan", lifespan, "Frames each generation lives");
        addRow(row++, "Mutation", mutationRate, "Probability each gene is replaced when breeding");
        addRow(row++, "Elites %", elitePercent, "Share of the best rockets re-flown unchanged");
        addRow(row++, "", limitSpeed, "Cap rocket speed in pixels per frame");
        addRow(row++, "Max speed", maxSpeed, "Pixels per frame");

        apply.addActionListener(e -> {
            try {
                current = value();
                error.setText(" ");
                onApply.accept(current);
            } catch (IllegalArgumentException ex) {
                error.setText(ex.getMessage());
            }
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.insets = new Insets(12, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(apply, c);
        error.setForeground(new Color(192, 57, 43));
        c.gridy = row++;
        c.insets = new Insets(0, 0, 0, 0);
        add(error, c);
        c.gridy = row;
        c.weighty = 1;
        add(Box.createVerticalGlue(), c);
    }

    private void addRow(int row, String label, JComponent field, String tooltip) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(2, 0, 2, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        field.setToolTipText(tooltip);
        add(field, c);
    }

    /** The configuration the spinners describe, on the current world size. */
    public SimulationConfig value() {
        double speed = limitSpeed.isSelected() ? ((Number) maxSpeed.getValue()).doubleValue() : SimulationConfig.UNLIMITED_SPEED;
        return new SimulationConfig(current.width(), current.height(),
                ((Number) population.getValue()).intValue(),
                ((Number) lifespan.getValue()).intValue(),
                ((Number) mutationRate.getValue()).doubleValue(),
                speed,
                ((Number) elitePercent.getValue()).doubleValue() / 100);
    }

    /** Applied configuration; the world size in it follows the course. */
    public SimulationConfig current() {
        return current;
    }

    /** Keeps the size in step with the course; the spinners are untouched. */
    public void setWorldSize(int width, int height) {
        current = new SimulationConfig(width, height, current.populationSize(), current.lifespan(),
                current.mutationRate(), current.maxSpeed(), current.eliteFraction());
    }

    JSpinner populationSpinner() {
        return population;
    }

    JSpinner elitePercentSpinner() {
        return elitePercent;
    }

    JCheckBox limitSpeedBox() {
        return limitSpeed;
    }

    JButton applyButton() {
        return apply;
    }

    String errorText() {
        return error.getText();
    }
}
