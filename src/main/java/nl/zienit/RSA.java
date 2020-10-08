package nl.zienit;

import java.math.BigInteger;
import java.util.Random;

public class RSA {

    public static final int BITLENGTH = 1024;

    private final BigInteger p, q, n, phi, e, d;

    public static RSA randomized() {
        // Algorithm 1.1 RSA key pair generation
        // INPUT: Security parameter l.
        // OUTPUT: RSA public key (n,e) and private key d.
        // 1. Randomly select two primes p and q of the same bitlength l/2.
        // 2. Compute n = pq and φ = (p−1)(q−1).
        // 3. Select an arbitrary integer e with 1 < e < φ and gcd(e, φ) = 1.
        // 4. Compute the integer d satisfying 1 < d < φ and ed ≡ 1 (mod φ).
        // 5. Return(n,e,d).
        final Random rnd = new Random();

        final BigInteger p = BigInteger.probablePrime(BITLENGTH / 2, rnd);
        final BigInteger q = BigInteger.probablePrime(BITLENGTH / 2, rnd);
        final BigInteger e = new BigInteger(BITLENGTH, rnd);

        return new RSA(p, q, e);
    }

    public RSA(BigInteger p, BigInteger q, BigInteger e) {
        this.p = p;
        this.q = q;
        this.e = e;
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        if (!(BigInteger.ONE.compareTo(e) < 0 && e.compareTo(phi) < 0 && e.gcd(phi).equals(BigInteger.ONE))) {
            throw new IllegalArgumentException("e and phi must be co-prime");
        }
        n = p.multiply(q);
        d = e.modInverse(phi);
    }

    // Algorithm 1.2 Basic RSA encryption
    // INPUT: RSA public key (n,e), plaintext m ∈ [0,n−1]. OUTPUT: Ciphertext c.
    // 1. Compute c = m^e mod n.
    // 2. Return(c).
    public BigInteger enc(BigInteger m) {
        return m.modPow(e, n);
    }

    // Algorithm 1.3 Basic RSA decryption
    // INPUT: RSA public key (n,e), RSA private key d, ciphertext c. OUTPUT: Plaintext m.
    // 1. Compute m = c^d mod n.
    // 2. Return(m).
    public BigInteger dec(BigInteger c) {
        return c.modPow(d, n);
    }

    public static void main(String[] args) {
        RSA rsa = randomized(); //new RSA(new BigInteger("17"), new BigInteger("59"), new BigInteger("31"));
        final BigInteger c = rsa.enc(new BigInteger("20"));
        System.out.println("c=" + c);
        final BigInteger m = rsa.dec(c);
        System.out.println("m=" + m);
    }
}
