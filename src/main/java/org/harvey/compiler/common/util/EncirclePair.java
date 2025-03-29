package org.harvey.compiler.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 获取集合中一个元素的前一个元素和后一个元素
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 19:52
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
