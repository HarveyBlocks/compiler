package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;

/**
 * TODO  
 *
 * @date 2025-01-08 23:37
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class WhileEnd extends BodyEnd {
    private final Expression condition;

    public WhileEnd(int start, Expression condition) {
        super(start);
        this.condition = condition;
    }
}
