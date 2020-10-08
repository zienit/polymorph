package nl.zienit;

public interface CyclicAdditiveGroup<T> extends AdditiveGroup<T> {
    T generator();
}
