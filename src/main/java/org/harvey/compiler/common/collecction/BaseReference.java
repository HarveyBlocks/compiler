package org.harvey.compiler.common.collecction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-29 17:27
 */
@AllArgsConstructor
@Getter
public class BaseReference {
    private final int base;
    private final int offset;

    public int getIndex() {
        return base + offset;
    }
}
