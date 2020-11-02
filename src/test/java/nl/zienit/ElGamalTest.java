package nl.zienit;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class ElGamalTest {

    private CyclicAdditiveGroup<Point> EC;
    private ElGamal<Point> EG;
    private BigInteger privateKey;
    private Point publicKey;
    private final String bsn = "135792468";
    private Point encodedBSN;
    private byte[] pseudonym;
    private Point encodedPseudonym;

    @Before
    public void before() {
        EC = EllipticCurve.brainpoolP320r1();
        EG = new ElGamal<>(EC);
        privateKey = EG.random();
        publicKey = EG.publicKey(privateKey);

        encodedBSN = ((EllipticCurve) EC.group()).encode(bsn.getBytes());

        try {
            pseudonym = MessageDigest.getInstance("SHA-256").digest(bsn.getBytes());
        } catch (NoSuchAlgorithmException e) {
        }

        encodedPseudonym = ((EllipticCurve) EC.group()).encode(pseudonym);
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
        final BigInteger newPrivateKey = privateKey.multiply(k);
        assertThat(EG.decrypt(rekeyed, newPrivateKey).decode(), is(bsn.getBytes()));
    }

    @Test
    public void testReShuffling() {
        final ElGamal.Cryptogram<Point> encrypted = EG.encrypt(encodedPseudonym, publicKey);

        final BigInteger s = EG.random();
        final ElGamal.Cryptogram<Point> reshuffled = EG.reShuffling(encrypted, s);
        assertThat(EG.decrypt(reshuffled, privateKey).decode(), not(is(pseudonym)));

        final ElGamal.Cryptogram<Point> reconstructed = EG.reShuffling(reshuffled, s.modInverse(EC.order()));
        assertThat(EG.decrypt(reconstructed, privateKey).decode(), is(pseudonym));
    }
}