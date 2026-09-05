# 2. Keep Swing for rendering

Date: 2026-09-05

## Status

Accepted

## Context

The simulation is drawn with Java Swing, but the original loop used it
incorrectly: a hand-rolled thread painted through `getGraphics()` instead of
`paintComponent`, and allocated a new back buffer every frame. When reviving
the project the question was whether to fix the Swing usage or port the
rendering to JavaFX or a game library.

## Decision

Keep Swing and use it correctly: a `javax.swing.Timer` drives `repaint()`, all
drawing happens in `paintComponent`, and one back buffer is reused.

## Consequences

- No new dependencies. JavaFX is no longer part of the JDK, so porting would
  have added a runtime dependency and packaging work for no functional gain.
- The rendering surface stays small and well understood, which matters more
  than visual polish for a simulation demo.
- Simulation logic is being separated from rendering regardless, so a later
  port to another toolkit would only touch the rendering classes.
