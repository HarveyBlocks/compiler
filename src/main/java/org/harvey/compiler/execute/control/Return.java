package org.harvey.compiler.execute.control;

import lombok.Getter;
import lombok.Setter;

/**
 * 返回的内容是由上一条Executable决定, 如果是return;, 那么在Executable中是 [EMPTY, return]
 *
 * @date 2025-01-10 20:24
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Setter
@Getter
public class Return extends SequentialExecutable {
    private boolean hasReturnValue;
}
