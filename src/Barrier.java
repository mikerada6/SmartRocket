import java.awt.*;

public class Barrier {

    private int x;
    private int y;
    private int width;
    private int height;

    public Barrier(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Graphics draw(Graphics g) {

        g.fillRect((x), (y), width, height);
        return g;

    }

    public int getTop() {
        return this.y;
    }

    public int getBottom() {
        return this.y + this.height;
    }

    public int getLeft() {
        return this.x;
    }

    public int getRight() {
        return this.x + this.width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
