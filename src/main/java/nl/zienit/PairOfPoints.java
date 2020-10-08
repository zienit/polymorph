package nl.zienit;

public class PairOfPoints {

    private final Point a;
    private final Point b;

    public PairOfPoints(Point a, Point b) {
        this.a = a;
        this.b = b;
    }

    public PairOfPoints(Point a) {
        this(a, a);
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }
}
