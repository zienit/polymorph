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
        if (value.length > 15) {
            throw new IllegalArgumentException("value too long");
        }
        final byte[] encoded = new byte[20];
        encoded[0] = (byte) value.length;
        System.arraycopy(value, 0, encoded, 1, value.length);
        Arrays.fill(encoded, value.length + 1, 16, (byte) 0x00);
        final byte[] hash = new byte[4];
        new Random().nextBytes(hash);
        System.arraycopy(hash, 0, encoded, 16, hash.length);
        return new BigInteger(encoded);
    }

    public byte[] decode() {
        final byte[] decoded = x.toByteArray();
        final int length = decoded[0];
        return Arrays.copyOfRange(decoded, 1, 1 + length);
    }
}
