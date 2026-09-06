package io.github.mikerada6.smartrocket;

/** The text behind the Help button. Plain so it can be checked in tests and shown anywhere. */
public final class HelpText {

    private HelpText() {
    }

    public static final String CONTROLS = """
            Running
              Pause            stop stepping; the picture stays live
              Restart          new run on the same course (same seed if one was given)
              Speed            simulation steps per drawn frame, 1x to 100x
              Course           pick a built-in course or open a course file
              Parameters       show or hide the algorithm settings; Apply restarts
              Edit course      switch to the editor; switching back applies your changes

            Rockets
              blue to orange   far from the target to close to it
              white            reached the target
              grey             crashed into a barrier or the edge
              outlined         an elite re-flown unchanged from the last generation

            Editing
              drag on space    draw a barrier
              drag a barrier   move it; drag an edge or corner to resize
              drag the target or the launch marker to move them
              wheel on target  resize it
              Delete           remove the selected barrier
              Cmd/Ctrl-S       save        Cmd/Ctrl-Z   undo
              Snap to grid     toggle 8 px snapping
              Run              restart the simulation on the course as drawn
            """;
}
