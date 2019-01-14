import java.awt.*;

public class Target {

    private Vector pos;
    private int size;

    Target() {
        pos = new Vector(GamePanel.WIDTH / 2, 50);
        size = 25;
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
