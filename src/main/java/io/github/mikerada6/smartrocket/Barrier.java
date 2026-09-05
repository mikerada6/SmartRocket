package io.github.mikerada6.smartrocket;

/** Axis-aligned rectangular obstacle; {@code x, y} is the top-left corner. */
public record Barrier(int x, int y, int width, int height) {

    public int left() {
        return x;
    }

    public int top() {
        return y;
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    /** True if the rectangle with top-left {@code (px, py)} and the given size overlaps this barrier. */
    public boolean overlaps(double px, double py, double w, double h) {
        return px < right() && px + w > left() && py < bottom() && py + h > top();
    }
}
