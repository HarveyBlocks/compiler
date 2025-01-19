package org.harvey.compiler.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @date 2025-01-08 19:52
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class EncirclePair<T> {
    T pre;
    T post;

    public boolean bothNull() {
        return pre == null && post == null;
    }
}
