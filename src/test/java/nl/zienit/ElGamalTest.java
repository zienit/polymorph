package nl.zienit;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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
        final ElGamal.Cryptogram<Point> pi = EG.encrypt(encodedBSN, publicKey);
        final ElGamal.Cryptogram<Point> rerandomized = EG.reRandomize(pi);
        assertThat(rerandomized, not(is(pi)));
        assertThat(EG.decrypt(rerandomized, privateKey).decode(), is(bsn.getBytes()));
    }

    @Test
    public void testReKeying() {
        final ElGamal.Cryptogram<Point> pi = EG.encrypt(encodedBSN, publicKey);
        final BigInteger k = EG.random();
        final ElGamal.Cryptogram<Point> ei = EG.reKeying(pi, k);
        assertThat(EG.decrypt(ei, privateKey).decode(), not(is(bsn.getBytes())));
        final BigInteger newPrivateKey = privateKey.multiply(k);
        assertThat(EG.decrypt(ei, newPrivateKey).decode(), is(bsn.getBytes()));
    }

    @Test
    public void testReShuffling() {
        final ElGamal.Cryptogram<Point> pp = EG.encrypt(encodedPseudonym, publicKey);

        final BigInteger s = EG.random();
        final ElGamal.Cryptogram<Point> ep = EG.reShuffling(pp, s);
        assertThat(EG.decrypt(ep, privateKey).decode(), not(is(pseudonym)));

        final ElGamal.Cryptogram<Point> unshuffled = EG.reShuffling(ep, s.modInverse(EC.order()));
        assertThat(EG.decrypt(unshuffled, privateKey).decode(), is(pseudonym));
    }

    @Test
    public void testEIDScenario() {

        // keys are derived and distributed
        final BigInteger encryptionKeySP1 = EG.random();
        final BigInteger decryptionKeySP1 = privateKey.multiply(encryptionKeySP1);
        final BigInteger encryptionKeySP2 = EG.random();
        final BigInteger decryptionKeySP2 = privateKey.multiply(encryptionKeySP2);
        final BigInteger pseudonymShuffleKeySP2 = EG.random();
        final BigInteger pseudonymClosingKeySP2 = EG.random();

        // PIP has been placed on the chip (eDL, eNIK): activate
        final ElGamal.Cryptogram<Point> pi = EG.encrypt(encodedBSN, publicKey);
        final ElGamal.Cryptogram<Point> pp = EG.encrypt(encodedPseudonym, publicKey);

        // User authenticates at IdP to access SP1 (identity) and SP2 (pseudonym).
        // The chip releases the randomized PIP after successful authentication
        final ElGamal.Cryptogram<Point> randomizedPI = EG.reRandomize(pi);
        final ElGamal.Cryptogram<Point> randomizedPP = EG.reRandomize(pp);

        // IdP transforms PI to EI@SP1
        final ElGamal.Cryptogram<Point> eiAtSP1 = EG.reKeying(pi, encryptionKeySP1);
        // IdP transforms PP to EP@SP2
        final ElGamal.Cryptogram<Point> epAtSP2 = EG.reShuffling(EG.reKeying(pp, encryptionKeySP2), pseudonymShuffleKeySP2);

        // SP1 decrypts identity
        assertThat(EG.decrypt(eiAtSP1, decryptionKeySP1).decode(), is(bsn.getBytes()));

        // SP2 reshuffles with closing key and then decrypts pseudonym
        final byte[] pAtSP2 = EG.decrypt(EG.reShuffling(epAtSP2, pseudonymClosingKeySP2), decryptionKeySP2).decode();
        System.out.println(new BigInteger(pAtSP2).toString(16));
    }
}