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

    public Point getOdd() {
        return a.getY().testBit(0) ? a : b;
    }

    public Point getEven() {
        return a.getY().testBit(0) ? b : a;
    }
}
