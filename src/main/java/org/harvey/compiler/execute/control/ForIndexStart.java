package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.LocalVariableDeclare;

import java.util.ArrayList;

/**
 * TODO  
 *
 * @date 2025-01-08 23:40
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class ForIndexStart extends BodyStart {
    // 和init二选一
    private final ArrayList<LocalVariableDeclare> declareType;
    // 和declareType二选一
    private final Expression init;
    // 不可null, 可empty
    private final Expression condition;
    // 不可null, 可empty
    private final Expression step;

}
