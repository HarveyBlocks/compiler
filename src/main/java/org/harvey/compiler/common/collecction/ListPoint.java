package org.harvey.compiler.common.collecction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回element, 以及其在集合中对应位置的index
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-05 00:42
 */
@AllArgsConstructor
@Getter
public class ListPoint<E> {
    private final int index;
    private final E element;

}
