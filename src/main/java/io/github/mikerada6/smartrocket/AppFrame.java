package io.github.mikerada6.smartrocket;

import javax.swing.*;
import java.awt.Rectangle;
import java.nio.file.Path;

/**
 * The main window: a {@link Workspace} plus the frame-level concerns of title, size,
 * remembered settings and shutdown.
 */
public final class AppFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Workspace workspace;
    private final transient Settings settings;

    public AppFrame(Arguments arguments, World world, String courseName, Path courseFile, boolean startEditing) {
        this(arguments, world, courseName, courseFile, startEditing, Settings.forUser());
    }

    AppFrame(Arguments arguments, World world, String courseName, Path courseFile, boolean startEditing, Settings settings) {
        super("Smart Rockets");
        this.settings = settings;
        workspace = new Workspace(arguments, world, courseName, courseFile, this::layoutChanged);
        setContentPane(workspace);
        workspace.controls().setStepsPerFrame(settings.stepsPerFrame());
        workspace.controls().setParametersVisible(settings.showParameters());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                remember();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                workspace.closeLog();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(workspace::closeLog));
        if (startEditing) {
            workspace.controls().setEditing(true);
        }
        pack();
        Rectangle remembered = settings.windowBounds();
        if (remembered != null) {
            setBounds(remembered);
        }
        setTitle(workspace.title());
    }

    private void remember() {
        settings.saveWindowBounds(getBounds());
        settings.saveStepsPerFrame(workspace.controls().stepsPerFrame());
        settings.saveShowParameters(workspace.isParametersVisible());
    }

    /** Re-titles and re-lays out; the window keeps its size, and the view scales to fit. */
    private void layoutChanged() {
        setTitle(workspace.title());
        workspace.revalidate();
        workspace.repaint();
        if (!isVisible()) {
            pack();
        }
    }
}
