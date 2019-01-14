import java.awt.*;

public class Target {

    Vector pos;

    Target()
    {
        pos= new Vector(GamePanel.WIDTH/2,50);
    }

    public Graphics draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillOval((int)this.pos.getX(),(int)this.pos.getY(),10,10);
        return g;
    }

    public Vector getPos()
    {
        return pos;
    }
}
