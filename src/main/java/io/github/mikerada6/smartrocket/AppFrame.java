package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.nio.file.Path;

/** The main window: a {@link Workspace} plus the frame-level concerns of title, size and shutdown. */
public final class AppFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Workspace workspace;

    public AppFrame(Arguments arguments, World world, String courseName, Path courseFile, boolean startEditing) {
        super("Smart Rockets");
        workspace = new Workspace(arguments, world, courseName, courseFile, this::layoutChanged);
        setContentPane(workspace);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                workspace.closeLog();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(workspace::closeLog));
        if (startEditing) {
            workspace.controls().setEditing(true);
        }
        layoutChanged();
    }

    private void layoutChanged() {
        setTitle(workspace.title());
        pack();
    }
}
