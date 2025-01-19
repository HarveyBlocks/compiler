package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @date 2025-01-10 14:21
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class Break extends Executable {
    private final int circulateStart;
}
