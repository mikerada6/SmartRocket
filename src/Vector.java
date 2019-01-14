public class Vector {

    private double x;
    private double y;
    private double z;
    private boolean is2D;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
        is2D = true;
    }

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        is2D = false;
    }

    public Vector(double x, double y, double z, boolean is2D) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.is2D = is2D;
    }

    public Vector add(Vector other) {
        return new Vector(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }



    public Vector setMag(double num) {
        Vector temp = this.normalize();
        return temp.multiply(num);
    }

    public Vector multiply(double d)
    {
        return new Vector(this.x*d, this.y*d, this.z*d, is2D);
    }


    public double getMag() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector limit(double limit)
    {
        if(this.getMag()<=limit)
            return this;
        else
        {
            return this.setMag(limit);
        }
    }

    public double getAngleRadians()
    {
        return Math.tan(this.y/this.x);
    }


    public Vector normalize() {
        double mag = this.getMag();
        if (mag !=0)
            return new Vector(this.x / mag, this.y / mag, this.z / z, is2D);
        else
            return new Vector(0,0,0,is2D);
    }

    public String toString() {
        if (is2D) {
            return "(" + x + "," + y + ")";
        }
        return "(" + x + "," + y + "," + y + ")";
    }

    public String getTab()
    {
        if (is2D) {
            return  x + "\t" + y;
        }
        return x + "\t" + y + "\t" + y ;
    }



}
