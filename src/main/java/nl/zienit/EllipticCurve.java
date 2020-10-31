package nl.zienit;

import java.math.BigInteger;

public class EllipticCurve implements AdditiveGroup<Point> {

    public static final Point POINT_AT_INFINITY = new Point(null, null) {
        @Override
        public String toString() {
            return "POINT_AT_INFINITY";
        }
    };

    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FOUR = BigInteger.valueOf(4);

    public final BigInteger A;
    public final BigInteger B;
    public final BigInteger p;

    public EllipticCurve(BigInteger A, BigInteger B, BigInteger p) {
        this.A = A;
        this.B = B;
        this.p = p;
        // todo test discriminant 4*A^3 + 27*B^2 mod p
        if (!p.isProbablePrime(24)) {
            throw new IllegalArgumentException("p is (probably) not prime");
        }
    }

    public static CyclicAdditiveGroup<Point> brainpoolP320r1() {
        return new EllipticCurve(
                new BigInteger("3EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB4", 16),
                new BigInteger("520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A6", 16),
                new BigInteger("D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27", 16)
        ).subgroup(
                new Point(
                        new BigInteger("43BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E20611", 16),
                        new BigInteger("14FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1", 16)
                ),
                new BigInteger("D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C59311", 16)
        );
    }

    @Override
    public Point add(Point a, Point b) {
        if (a == POINT_AT_INFINITY) {
            return b;
        }
        if (b == POINT_AT_INFINITY) {
            return a;
        }
        if (a.equals(inverse(b))) {
            return POINT_AT_INFINITY;
        }
        // calculate the 'slope' m
        BigInteger m;
        if (a.equals(b)) {
            m = THREE.multiply(a.getX().modPow(TWO, p)).add(A)
                    .multiply(TWO.multiply(a.getY()).modInverse(p))
                    .mod(p);
        } else {
            m = a.getY().subtract(b.getY())
                    .multiply(a.getX().subtract(b.getX()).modInverse(p))
                    .mod(p);
        }

        final BigInteger x = m.modPow(TWO, p).subtract(a.getX()).subtract(b.getX()).mod(p);
        final Point minusC = new Point(
                x,
                a.getY().add(m.multiply(x.subtract(a.getX()))).mod(p)
        );
        return inverse(minusC);
    }

    @Override
    public Point times(Point point, BigInteger i) {
        return i.equals(BigInteger.ZERO)
                ? POINT_AT_INFINITY
                : i.testBit(0)
                ? add(point, times(add(point, point), i.shiftRight(1)))
                : times(add(point, point), i.shiftRight(1));
    }

    @Override
    public Point inverse(Point point) {
        return point == POINT_AT_INFINITY ? POINT_AT_INFINITY : new Point(point.getX(), point.getY().negate().mod(p));
    }

    public PairOfPoints at(BigInteger x) {

        // y^2 = x^3 + Ax + B
        final BigInteger ySquare = x.modPow(THREE, p)
                .add(A.multiply(x).mod(p))
                .add(B).mod(p);

        if (ySquare.equals(BigInteger.ZERO)) {
            return new PairOfPoints(new Point(x, BigInteger.ZERO));
        }
        if (!isQuadraticResidue(ySquare)) {
            throw new IllegalArgumentException("no solution");
        }
        if (p.mod(FOUR).intValue() != 3) {
            throw new IllegalArgumentException("sqrt only implemented for p mod 4 == 3");
        }
        final BigInteger y = ySquare.modPow(p.add(BigInteger.ONE).divide(FOUR), p);
        return new PairOfPoints(new Point(x, y), new Point(x, y.negate().mod(p)));
    }

    private boolean isQuadraticResidue(BigInteger i) {
        // works only for odd primes (so not for 2, oddly the only even prime ;-)
        return i.modPow(p.subtract(BigInteger.ONE).divide(TWO), p).intValue() == 1;
    }

    public CyclicAdditiveGroup<Point> subgroup(Point G, BigInteger q) {
        return new CyclicAdditiveGroup<Point>() {

            @Override
            public Point generator() {
                return G;
            }

            @Override
            public Point add(Point a, Point b) {
                return EllipticCurve.this.add(a, b);
            }

            @Override
            public Point times(Point point, BigInteger i) {
                return EllipticCurve.this.times(point, i);
            }

            @Override
            public Point inverse(Point point) {
                return EllipticCurve.this.inverse(point);
            }

            @Override
            public BigInteger order() {
                return q;
            }
        };
    }

    public static void main(String[] args) {
        CyclicAdditiveGroup<Point> EC = brainpoolP320r1();
        for (int i = 0; i < 8; i++) {
            System.out.println(EC.times(EC.generator(), BigInteger.valueOf(i)));
        }
        System.out.println(EC.times(EC.generator(), EC.order())); // must be POINT_AT_INFINITY
    }
}
