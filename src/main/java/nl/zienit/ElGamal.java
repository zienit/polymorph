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

        private final T A;
        private final T B;
        private final T C;

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
}
