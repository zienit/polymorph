package nl.zienit;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

public class ElGamal<T> {

    private final CyclicAdditiveGroup<T> G;

    public ElGamal(CyclicAdditiveGroup<T> G) {
        this.G = G;
    }

    public static class Cryptogram<T> {

        public final T A;
        public final T B;
        public final T C;

        public Cryptogram(T a, T b, T c) {
            A = a;
            B = b;
            C = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cryptogram<?> that = (Cryptogram<?>) o;
            return Objects.equals(A, that.A) &&
                    Objects.equals(B, that.B) &&
                    Objects.equals(C, that.C);
        }

        @Override
        public int hashCode() {
            return Objects.hash(A, B, C);
        }

        @Override
        public String toString() {
            return "<" + A + "," + B + "," + C + ">";
        }
    }

    public BigInteger random() {
        final Random rnd = new Random();
        final BigInteger limit = G.order();
        BigInteger r;
        do {
            r = new BigInteger(limit.bitLength(), rnd);
        } while (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(limit) >= 0);
        return r;
    }

    public T publicKey(BigInteger privateKey) {
        return G.times(G.generator(), privateKey);
    }

    public Cryptogram<T> encrypt(T M, T Y) {
        final BigInteger t = random();
        return new Cryptogram<>(
                G.times(G.generator(), t),
                G.add(M, G.times(Y, t)),
                Y);
    }

    public T decrypt(Cryptogram<T> cryptogram, BigInteger y) {
        return G.add(cryptogram.B, G.inverse(G.times(cryptogram.A, y)));
    }

    public Cryptogram<T> reRandomize(Cryptogram<T> cryptogram) {
        final BigInteger r = random();
        return new Cryptogram<>(
                G.add(G.times(G.generator(), r), cryptogram.A),
                G.add(G.times(cryptogram.C, r), cryptogram.B),
                cryptogram.C
        );
    }

    public Cryptogram<T> reKeying(Cryptogram<T> cryptogram, BigInteger k) {
        return new Cryptogram<>(
                G.times(cryptogram.A, k.modInverse(G.order())),
                cryptogram.B,
                G.times(cryptogram.C, k)
        );
    }

    public Cryptogram<T> reShuffling(Cryptogram<T> cryptogram, BigInteger s) {
        return new Cryptogram<>(
                G.times(cryptogram.A, s),
                G.times(cryptogram.B, s),
                cryptogram.C
        );
    }

    public static void main(String[] args) {

        final CyclicAdditiveGroup<Point> G = EllipticCurve.brainpoolP320r1();
        final ElGamal<Point> EG = new ElGamal<>(G);
        final BigInteger y = EG.random();
        System.out.println("private key y: " + y);
        final Point Y = G.times(G.generator(), y);
        System.out.println("public key Y: " + Y);
        final Point M = G.times(G.generator(), EG.random());
        System.out.println("cleartext M: " + M);
        final Cryptogram<Point> cryptogram = EG.encrypt(M, Y);
        System.out.println("cryptogram: " + cryptogram);
        System.out.println("decrypted: " + EG.decrypt(cryptogram, y));

        final Cryptogram<Point> reRandomized = EG.reRandomize(cryptogram);
        System.out.println("re-randomized: " + reRandomized);
        System.out.println("decrypted: " + EG.decrypt(reRandomized, y));

        final BigInteger k = EG.random();
        final Cryptogram<Point> reKeyed = EG.reKeying(cryptogram, k);
        System.out.println("re-keyed: " + reKeyed);
        System.out.println("decrypted: " + EG.decrypt(reKeyed, y.multiply(k)));

        final BigInteger s = EG.random();
        final Cryptogram<Point> reShuffled = EG.reShuffling(cryptogram, s);
        System.out.println("re-shuffled: " + reShuffled);
        System.out.println("decrypted: " + EG.decrypt(reShuffled, y));
        System.out.println("should equal: " + G.times(M, s));
    }
}
