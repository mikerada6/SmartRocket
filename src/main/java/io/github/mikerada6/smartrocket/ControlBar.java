package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Toolbar of run controls. Holds no state of its own; every action goes to the {@link Listener}. */
public final class ControlBar extends JPanel {

    private static final long serialVersionUID = 1L;
    public static final String OPEN_FILE = "Open course file...";
    public static final int MAX_STEPS_PER_FRAME = 100;

    /** What the window does in response to the controls. */
    public interface Listener {
        void setPaused(boolean paused);

        void restart();

        void setStepsPerFrame(int steps);

        void selectCourse(String name);

        void openCourseFile();

        /** True to switch to the course editor, false to return to the run. */
        void setEditing(boolean editing);
    }

    private final transient Listener listener;
    private final JToggleButton pause = new JToggleButton("Pause");
    private final JSlider speed = new JSlider(1, MAX_STEPS_PER_FRAME, 1);
    private final JLabel speedLabel = new JLabel();
    private final JComboBox<String> courses;
    private final JToggleButton edit = new JToggleButton("Edit course");
    /** Set while the picker is being updated programmatically so the listener is not told. */
    private boolean updatingPicker;

    public ControlBar(Listener listener, List<String> courseNames, String selectedCourse) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 4));
        this.listener = listener;
        pause.addActionListener(e -> listener.setPaused(pause.isSelected()));
        add(pause);

        JButton restart = new JButton("Restart");
        restart.addActionListener(e -> listener.restart());
        add(restart);

        add(new JLabel("Speed"));
        speed.setPreferredSize(new Dimension(140, speed.getPreferredSize().height));
        speed.setToolTipText("Simulation steps per drawn frame");
        speed.addChangeListener(e -> {
            updateSpeedLabel();
            if (!speed.getValueIsAdjusting()) {
                listener.setStepsPerFrame(speed.getValue());
            }
        });
        add(speed);
        updateSpeedLabel();
        add(speedLabel);

        add(new JLabel("Course"));
        courses = new JComboBox<>(courseNames.toArray(String[]::new));
        courses.addItem(OPEN_FILE);
        showCourse(selectedCourse);
        courses.addActionListener(e -> onCoursePicked());
        add(courses);

        edit.addActionListener(e -> listener.setEditing(edit.isSelected()));
        add(edit);
    }

    /** Switches the edit toggle, telling the listener as if the user had clicked it. */
    public void setEditing(boolean editing) {
        if (edit.isSelected() != editing) {
            edit.doClick();
        }
    }

    public boolean isEditing() {
        return edit.isSelected();
    }

    private void onCoursePicked() {
        if (updatingPicker) {
            return;
        }
        String chosen = (String) courses.getSelectedItem();
        if (OPEN_FILE.equals(chosen)) {
            listener.openCourseFile();
        } else if (chosen != null) {
            listener.selectCourse(chosen);
        }
    }

    private void updateSpeedLabel() {
        speedLabel.setText(speed.getValue() + "x");
    }

    /** Shows a course name in the picker without firing the listener, e.g. after a file was opened. */
    public void showCourse(String name) {
        updatingPicker = true;
        try {
            if (((DefaultComboBoxModel<String>) courses.getModel()).getIndexOf(name) < 0) {
                courses.insertItemAt(name, courses.getItemCount() - 1);
            }
            courses.setSelectedItem(name);
        } finally {
            updatingPicker = false;
        }
    }

    public boolean isPaused() {
        return pause.isSelected();
    }

    public int stepsPerFrame() {
        return speed.getValue();
    }

    JToggleButton pauseButton() {
        return pause;
    }

    JSlider speedSlider() {
        return speed;
    }

    JComboBox<String> coursePicker() {
        return courses;
    }

    JToggleButton editButton() {
        return edit;
    }
}
