package io.github.mikerada6.smartrocket;

import java.awt.*;

public class Target {

    private final Vector pos;
    private final int size;

    public Target(Vector pos, int size) {
        this.pos = pos;
        this.size = size;
    }

    public Graphics draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillOval((int) this.pos.getX(), (int) this.pos.getY(), size, size);
        return g;
    }

    public int getSize() {
        return size;
    }

    public Vector getPos() {
        return pos;
    }
}
