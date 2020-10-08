package nl.zienit;

import java.math.BigInteger;

public class Point {

    private final BigInteger x;
    private final BigInteger y;

    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Point) && ((Point) other).x.equals(x) && ((Point) other).y.equals(y);
    }

    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + ")";
    }
}
