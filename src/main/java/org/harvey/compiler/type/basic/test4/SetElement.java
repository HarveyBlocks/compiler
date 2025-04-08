package org.harvey.compiler.type.basic.test4;

import lombok.AllArgsConstructor;

import java.util.Objects;

/**
 * TODO  
 *
 * @date 2025-03-31 21:29
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@AllArgsConstructor
public class SetElement<T> {
    private final T t;

    public static <T> SetElement<T> of(T t) {
        return new SetElement<>(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SetElement)) {
            return false;
        }
        SetElement<?> that = (SetElement<?>) o;
        return t == that.t;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t);
    }
}
