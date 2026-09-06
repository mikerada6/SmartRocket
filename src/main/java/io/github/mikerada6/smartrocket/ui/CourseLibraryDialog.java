package io.github.mikerada6.smartrocket.ui;

import io.github.mikerada6.smartrocket.world.CourseFile;
import io.github.mikerada6.smartrocket.world.CourseLibrary;
import io.github.mikerada6.smartrocket.world.World;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Picker for the courses in a directory, shown as thumbnails with name and size. Double
 * click or Open loads one; Browse falls back to a file chooser for a course elsewhere.
 */
public final class CourseLibraryDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final int THUMB_WIDTH = 160;
    private static final int THUMB_HEIGHT = 120;

    private final JList<CourseLibrary.Entry> list;
    private final transient BiConsumer<Path, World> onChosen;

    /**
     * @param onChosen receives the chosen file and its parsed course; a file from Browse
     *                 that fails to parse is reported in a dialog instead
     */
    public CourseLibraryDialog(Window owner, Path directory, BiConsumer<Path, World> onChosen) {
        super(owner, "Open course", ModalityType.APPLICATION_MODAL);
        this.onChosen = onChosen;
        List<CourseLibrary.Entry> entries = CourseLibrary.scan(directory);
        list = new JList<>(entries.toArray(CourseLibrary.Entry[]::new));
        list.setCellRenderer(new EntryRenderer());
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!entries.isEmpty()) {
            list.setSelectedIndex(0);
        }
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(3 * (THUMB_WIDTH + 24) + 20, 2 * (THUMB_HEIGHT + 44) + 10));

        JButton open = new JButton("Open");
        open.addActionListener(e -> openSelected());
        open.setEnabled(!entries.isEmpty());
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> browse(directory));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(browse);
        buttons.add(cancel);
        buttons.add(open);
        getRootPane().setDefaultButton(open);

        JLabel heading = new JLabel(entries.isEmpty()
                ? "  No course files in " + directory + ". Browse for one, or draw one with Edit course."
                : "  Courses in " + directory);
        setLayout(new BorderLayout());
        add(heading, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void openSelected() {
        CourseLibrary.Entry entry = list.getSelectedValue();
        if (entry == null) {
            return;
        }
        dispose();
        onChosen.accept(entry.file(), entry.world());
    }

    private void browse(Path directory) {
        JFileChooser chooser = new JFileChooser(directory.toAbsolutePath().toFile());
        chooser.setDialogTitle("Open course file");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        try {
            World world = CourseFile.read(file);
            dispose();
            onChosen.accept(file, world);
        } catch (java.io.IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot open course", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** A thumbnail with the course name and a one-line summary beneath it. */
    private static final class EntryRenderer extends JPanel implements ListCellRenderer<CourseLibrary.Entry> {

        private static final long serialVersionUID = 1L;
        private final JLabel picture = new JLabel();
        private final JLabel name = new JLabel();
        private final JLabel summary = new JLabel();

        EntryRenderer() {
            super(new BorderLayout(0, 2));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            picture.setHorizontalAlignment(SwingConstants.CENTER);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            summary.setHorizontalAlignment(SwingConstants.CENTER);
            summary.setFont(summary.getFont().deriveFont(summary.getFont().getSize2D() - 1));
            JPanel text = new JPanel(new GridLayout(2, 1));
            text.setOpaque(false);
            text.add(name);
            text.add(summary);
            add(picture, BorderLayout.CENTER);
            add(text, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CourseLibrary.Entry> l, CourseLibrary.Entry entry,
                                                      int index, boolean selected, boolean focused) {
            picture.setIcon(new ImageIcon(SimulationRenderer.thumbnail(entry.world(), THUMB_WIDTH, THUMB_HEIGHT)));
            name.setText(entry.name());
            summary.setText(entry.summary());
            setBackground(selected ? l.getSelectionBackground() : l.getBackground());
            name.setForeground(selected ? l.getSelectionForeground() : l.getForeground());
            summary.setForeground(selected ? l.getSelectionForeground() : l.getForeground());
            setOpaque(true);
            return this;
        }
    }
}
