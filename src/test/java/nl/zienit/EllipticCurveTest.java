package nl.zienit;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static nl.zienit.EllipticCurve.brainpoolP320r1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EllipticCurveTest {

    private EllipticCurve simpleEC;
    private CyclicAdditiveGroup<Point> cyclicEC;

    @Before
    public void before() {
        // y^2 = x^3 - 7x + 10 (mod 19)
        simpleEC = new EllipticCurve(BigInteger.valueOf(-7), BigInteger.valueOf(10), BigInteger.valueOf(19));

        // y^2 = x^3 + 2x + 3 (mod 97), generator = (3,6), order = 5
        cyclicEC = new EllipticCurve(BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(97))
                .subgroup(new Point(BigInteger.valueOf(3), BigInteger.valueOf(6)), BigInteger.valueOf(5));
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

    @Test
    public void testCyclicEC() {
        final Point G = cyclicEC.generator();
        assertThat(cyclicEC.times(G, BigInteger.ZERO), is(EllipticCurve.POINT_AT_INFINITY));
        assertThat(cyclicEC.times(G, BigInteger.ONE), is(G));
        assertThat(cyclicEC.times(G, BigInteger.valueOf(2)), is(new Point(BigInteger.valueOf(80), BigInteger.valueOf(10))));
        assertThat(cyclicEC.times(G, BigInteger.valueOf(3)), is(new Point(BigInteger.valueOf(80), BigInteger.valueOf(87))));
        assertThat(cyclicEC.times(G, BigInteger.valueOf(4)), is(new Point(BigInteger.valueOf(3), BigInteger.valueOf(91))));
        assertThat(cyclicEC.times(G, BigInteger.valueOf(5)), is(EllipticCurve.POINT_AT_INFINITY));
    }

    @Test
    public void testPrintABunchOfBrainpoolP320r1Points() {
        final CyclicAdditiveGroup<Point> EC = brainpoolP320r1();
        System.out.println("first 10 of " + EC.order() + " possible points of the Brainpool P320r1 Elliptic Curve:");

        for (int i = 1; i <= 10; i++) {
            System.out.println(EC.times(EC.generator(), BigInteger.valueOf(i)));
        }
    }
}