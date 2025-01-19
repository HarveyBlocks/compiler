package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @date 2025-01-08 16:50
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class ArrayInitExpression extends ComplexExpression {
    public static final String END_MSG = "__struct__clone__end__";
    private final boolean start;//不是start, 就是end
    @Setter
    private int otherSide;

    public ArrayInitExpression(boolean start) {
        this.start = start;
    }
}
