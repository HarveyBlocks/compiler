package org.harvey.compiler.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
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
