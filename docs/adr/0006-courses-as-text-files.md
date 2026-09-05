# 6. Courses are plain text files, not JSON

Date: 2026-09-05

## Status

Accepted

## Context

Courses were Java code: an enum with two hard-coded layouts. To let people
design their own courses, and later to give a visual editor something to
save, a course needs to be data. JSON is the obvious interchange format, but
the JDK ships no JSON parser and the project has no runtime dependencies,
which keeps the build to a single JDK and is worth preserving for a small
demo. A course is three kinds of statement with a handful of numbers each.

## Decision

A course file is line-based text: one statement per line, `#` starts a
comment, and the statements are `size W H`, `target X Y RADIUS` and any
number of `barrier LEFT TOP WIDTH HEIGHT`. Exactly one `size` and one
`target` are required. `CourseFile` parses and formats it, reporting the
line number in every error. `--course-file PATH` loads one; the file's size
line defines the world, so `--width` and `--height` are rejected alongside
it. The two built-in courses are also committed as files under `courses/`
and a test checks they still match the code-defined layouts.

## Consequences

- No new dependency; the parser is under a hundred lines and fully tested.
- Anyone can write or share a course with a text editor, and a future
  editor mode only needs `CourseFile.write`.
- The format has no schema versioning. If it ever grows beyond these three
  statements, add a `version` line then rather than pre-empting it now.
- Files are absolute pixels, so a course is tied to its world size, unlike
  the built-in layouts which scale. That matches how an editor would save
  what was drawn.
