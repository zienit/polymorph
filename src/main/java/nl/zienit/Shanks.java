package nl.zienit;

import java.math.BigInteger;
import java.util.Random;

// https://www.rieselprime.de/ziki/Modular_square_root#Modulus_congruent_to_1_modulo_8
public class Shanks {

    private final BigInteger m;
    private final BigInteger e;
    private final BigInteger q;
    private final BigInteger z;

    public Shanks(BigInteger m) {
        this.m = m;
        final BigInteger pMinusOne = m.subtract(BigInteger.ONE);
        BigInteger e = BigInteger.ZERO;
        BigInteger q;
        do {
            e = e.add(BigInteger.ONE);
            q = pMinusOne.multiply(BigInteger.valueOf(2).modPow(e, m).modInverse(m)).mod(m);
        } while (!q.testBit(0));
        this.e = e;
        this.q = q;

        System.out.println("e=" + e);
        System.out.println("q=" + q);

        BigInteger z;
        do {
            final BigInteger x = random();
            z = x.modPow(q, m);
        } while (z.modPow(BigInteger.valueOf(2).modPow(e.subtract(BigInteger.ONE), m), m).equals(BigInteger.ONE));
        this.z = z;
        System.out.println("z=" + z);
    }

    // todo: make reusable
    private BigInteger random() {
        final Random rnd = new Random();
        BigInteger r;
        do {
            r = new BigInteger(m.bitLength(), rnd);
        } while (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(m) >= 0);
        return r;
    }

    public BigInteger sqrt(BigInteger a) {
        final BigInteger x = a.modPow(q.subtract(BigInteger.ONE).multiply(BigInteger.valueOf(2).modInverse(m)).mod(m), m);
        BigInteger y = z;
        BigInteger r = e;
        BigInteger v = a.multiply(x).mod(m);
        BigInteger w = v.multiply(x).mod(m);

        System.out.println("y = " + y + ",r = " + r + " , x = " + x + ", v = " + v + ", w = " + w);
        while (!w.equals(BigInteger.ONE)) {
            BigInteger k = BigInteger.ONE;
            while (!w.modPow(BigInteger.valueOf(2).modPow(k, m), m).equals(BigInteger.ONE)) {
                k = k.add(BigInteger.ONE);
            }
            final BigInteger d = y.modPow(BigInteger.valueOf(2).modPow(r.subtract(k).subtract(BigInteger.ONE), m), m);
            y = d.modPow(BigInteger.valueOf(2), m);
            r = k;
            v = d.multiply(v).mod(m);
            w = w.multiply(y).mod(m);
        }
        return v;
    }

    public static void main(String[] args) {
        Shanks shanks = new Shanks(BigInteger.valueOf(113));
        BigInteger v = shanks.sqrt(BigInteger.valueOf(111));
        System.out.println("v = " + v);
    }
}
