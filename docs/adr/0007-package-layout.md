# 7. Package layout and dependency direction

Date: 2026-09-06

## Status

Accepted

## Context

After the revival every class sat in one flat package, thirty-odd files
mixing geometry, the genetic algorithm, file formats, the command line and
Swing. Nothing stopped simulation code from reaching into the window, and a
few things had started to: the course library rendered thumbnails through
the Swing renderer, the world asked the rocket class for its box size, and
the genome carried an AWT colour that nothing drew any more.

## Decision

Split by responsibility, with a strict dependency direction from bottom to
top:

| package | holds | may depend on |
|---|---|---|
| `geometry` | `Vec2`, `MathUtil` | nothing |
| `world` | `World`, `Barrier`, `Target`, courses and their files | geometry |
| `simulation` | the genetic algorithm and its statistics | world, geometry |
| `report` | consumers of statistics: CSV log, history for charts | simulation |
| `cli` | `Arguments` | all of the above |
| `ui`, `ui.editor` | everything Swing | all of the above |
| root | `SmartRockets`, the entry point | everything |

Only `ui` and the entry point may import `java.awt` or `javax.swing`. The
rocket box size moved into `World` as a rule of the world, thumbnails moved
into the renderer, and the genome lost its colour. Tests live in the same
package as the class they test.

A unit test reads the import lines of every source file and fails on any
violation, so the rule is enforced without a dependency-analysis library.

## Consequences

- Headless use (tests, `--headless`, future batch experiments) is guaranteed
  by construction rather than by care: nothing below `ui` can open a window.
- Adding a class means choosing its layer; a class that needs both Swing and
  the algorithm is a sign it should be split, as `CourseEditorModel` and
  `CourseEditorPanel` already are.
- Removing the genome colour changed the random stream consumed per rocket,
  so seeded expectations were re-verified after the move.
