package org.harvey.compiler.common.collecction;

import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 22:02
 */
public class DecorationRandomIterator<E> implements RandomlyIterator<E> {
    private final RandomlyIterator<E> origin;

    protected DecorationRandomIterator(RandomlyIterator<E> origin) {
        this.origin = origin;
    }

    @Override
    public RandomlyIterator<E> skipTo(int index) {
        return origin.skipTo(index);
    }

    @Override
    public RandomlyIterator<E> skipTo(Function<Integer, Integer> mapper) {
        return origin.skipTo(mapper);
    }

    @Override
    public void mark() {
        origin.mark();
    }

    @Override
    public void returnToMark() {
        origin.returnToMark();
    }

    @Override
    public int length() {
        return origin.length();
    }

    @Override
    public void returnToAndRemoveMark() {
        origin.returnToAndRemoveMark();
    }

    @Override
    public void removeMark() {
        origin.removeMark();
    }

    @Override
    public boolean hasNext() {
        return origin.hasNext();
    }

    @Override
    public E next() {
        return origin.next();
    }

    @Override
    public boolean hasPrevious() {
        return origin.hasPrevious();
    }

    @Override
    public E previous() {
        return origin.previous();
    }

    @Override
    public int nextIndex() {
        return origin.nextIndex();
    }

    @Override
    public int previousIndex() {
        return origin.previousIndex();
    }

    @Override
    public void remove() {
        origin.remove();
    }

    @Override
    public void set(E e) {
        origin.set(e);
    }

    @Override
    public void add(E e) {
        origin.add(e);
    }
}
