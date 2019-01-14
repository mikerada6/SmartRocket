import java.awt.*;

public class Target {

    Vector pos;

    Target()
    {
        pos= new Vector(GamePanel.WIDTH/2,50);
    }

    public Graphics draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int)this.pos.getX(),(int)this.pos.getY(),50,50);
        return g;
    }

    public Vector getPos()
    {
        return pos;
    }
}
