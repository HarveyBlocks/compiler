package org.harvey.compiler.common.util;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 13:46
 */
public class Counter {
    private final int initial;
    private final int step;
    private int count;

    public Counter(int initial, int step) {
        this.initial = initial;
        count = initial;
        this.step = step;
    }

    public int next() {
        int old = count;
        count += step;
        return old;
    }

    public int previous() {
        return count -= step;
    }

    public int getNext() {
        return count;
    }

    public int gePrevious() {
        return count - step;
    }
}
