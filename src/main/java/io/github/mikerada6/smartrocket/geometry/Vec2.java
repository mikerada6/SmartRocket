package io.github.mikerada6.smartrocket.geometry;

/** Immutable 2D vector. Replaces the half-3D Vector class whose z axis was never used. */
public record Vec2(double x, double y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    /** Vector of the given length pointing at {@code radians} measured from the +x axis. */
    public static Vec2 fromAngle(double radians, double magnitude) {
        return new Vec2(Math.cos(radians) * magnitude, Math.sin(radians) * magnitude);
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 multiply(double factor) {
        return new Vec2(x * factor, y * factor);
    }

    public double mag() {
        return Math.hypot(x, y);
    }

    /** Unit vector in the same direction, or zero for the zero vector. */
    public Vec2 normalize() {
        double m = mag();
        return m == 0 ? ZERO : new Vec2(x / m, y / m);
    }

    public Vec2 setMag(double magnitude) {
        return normalize().multiply(magnitude);
    }

    /** This vector if its length is at most {@code max}, otherwise shortened to {@code max}. */
    public Vec2 limit(double max) {
        return mag() <= max ? this : setMag(max);
    }

    public double dist(Vec2 other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    /** Direction in radians from the +x axis, in (-pi, pi]. Zero for the zero vector. */
    public double heading() {
        return Math.atan2(y, x);
    }
}
