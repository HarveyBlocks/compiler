package org.harvey.compiler.common.util;

import java.util.function.Supplier;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 21:35
 */
public class Singleton<T> {
    private volatile T t = null;

    public T instance(Supplier<T> constructor) {
        if (t != null) {
            return t;
        }
        synchronized (this) {
            if (t != null) {
                return t;
            }
            t = constructor.get();
        }
        return t;
    }
}