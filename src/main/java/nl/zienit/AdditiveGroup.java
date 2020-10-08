package nl.zienit;

import java.math.BigInteger;
import java.util.NoSuchElementException;

public interface AdditiveGroup<T> {
    T add(T a,T b);
    T times(T t, BigInteger i);
    T inverse(T t);
    default BigInteger order() {
        throw new NoSuchElementException();
    };
}
