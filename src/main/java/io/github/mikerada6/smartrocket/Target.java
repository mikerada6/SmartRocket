package io.github.mikerada6.smartrocket;

/**
 * Circular goal. A rocket counts as arrived once its position is within {@code radius}
 * of {@code centre}; the renderer draws exactly that circle, which the original code
 * did not (it hit-tested around one point and drew the disc offset from it).
 */
public record Target(Vec2 centre, double radius) {

    public boolean contains(Vec2 point) {
        return centre.dist(point) < radius;
    }
}
