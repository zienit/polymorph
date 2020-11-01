package nl.zienit;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class ElGamalTest {

    private CyclicAdditiveGroup<Point> EC;
    private ElGamal<Point> EG;
    private BigInteger privateKey;
    private Point publicKey;
    final String bsn = "135792468";
    private Point encodedBSN;

    @Before
    public void before() {
        EC = EllipticCurve.brainpoolP320r1();
        EG = new ElGamal<>(EC);
        privateKey = EG.random();
        publicKey = EG.publicKey(privateKey);

        for (; ; ) {
            try {
                encodedBSN = ((EllipticCurve)EC.group()).at(
                        Point.encodeX(bsn.getBytes())
                ).getEven();
                break;
            } catch (IllegalArgumentException e) { // No Solution
            }
        }
    }

    @Test
    public void testEncryptThenDecrypt() {
        final ElGamal.Cryptogram<Point> encrypted = EG.encrypt(encodedBSN, publicKey);
        assertThat(EG.decrypt(encrypted, privateKey).decode(), is(bsn.getBytes()));
    }

    @Test
    public void testReRandomize() {
        final ElGamal.Cryptogram<Point> encrypted = EG.encrypt(encodedBSN, publicKey);
        final ElGamal.Cryptogram<Point> rerandomized = EG.reRandomize(encrypted);
        assertThat(rerandomized, not(is(encrypted)));
        assertThat(EG.decrypt(rerandomized, privateKey).decode(), is(bsn.getBytes()));
    }

    @Test
    public void testReKeying() {
        final ElGamal.Cryptogram<Point> encrypted = EG.encrypt(encodedBSN, publicKey);
        final BigInteger k = EG.random();
        final ElGamal.Cryptogram<Point> rekeyed = EG.reKeying(encrypted, k);
        assertThat(EG.decrypt(rekeyed, privateKey).decode(), not(is(bsn.getBytes())));
        final BigInteger newPrivateKey = privateKey.multiply(k).mod(EC.order());
        assertThat(EG.decrypt(rekeyed, newPrivateKey).decode(), is(bsn.getBytes()));
    }
}