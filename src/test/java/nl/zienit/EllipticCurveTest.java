package nl.zienit;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EllipticCurveTest {

    private EllipticCurve simpleEC;

    @Before
    public void before() {
        // y^2 = x^3 - 7x + 10 over integers mod 19
        simpleEC = new EllipticCurve(BigInteger.valueOf(-7), BigInteger.valueOf(10), BigInteger.valueOf(19));
    }

    @Test
    public void testSimpleECAt1() {
        final PairOfPoints pop = simpleEC.at(BigInteger.ONE);
        assertThat(pop.getEven().getY().intValue(), is(2));
        assertThat(pop.getOdd().getY().intValue(), is(17));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleECAt4() {
        simpleEC.at(BigInteger.valueOf(4));
    }

}