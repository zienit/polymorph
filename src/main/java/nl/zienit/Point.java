package nl.zienit;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

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

    public static BigInteger encodeX(byte[] value) {
        final byte[] encoded = new byte[1 + value.length + 2];
        encoded[0] = (byte) 0x01; // assure number is always positive (two's complement sign bit = 0) and leading 0's are not lost
        System.arraycopy(value, 0, encoded, 1, value.length);
        final byte[] rnd = new byte[2];
        new Random().nextBytes(rnd);
        System.arraycopy(rnd, 0, encoded, 1 + value.length, rnd.length);
        return new BigInteger(encoded);
    }

    public byte[] decode() {
        final byte[] decoded = x.toByteArray();
        return Arrays.copyOfRange(decoded, 1, decoded.length - 2);
    }
}
