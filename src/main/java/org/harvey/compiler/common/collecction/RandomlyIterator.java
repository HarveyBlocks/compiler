package org.harvey.compiler.common.collecction;

import java.util.ListIterator;
import java.util.function.Function;

/**
 * 能随机访问的iterator
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-25 23:43
 */
public interface RandomlyIterator<E> extends ListIterator<E> {
    RandomlyIterator<E> skipTo(int index);

    RandomlyIterator<E> skipTo(Function<Integer, Integer> mapper);

    void mark();

    void returnToMark();

    int length();

    void returnToAndRemoveMark();
}
