package org.harvey.compiler.syntax;

import lombok.Getter;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-01 17:02
 */
@Getter
public class ReloadOperatorString implements ItemString {
    private final OperatorString origin;

    public ReloadOperatorString(OperatorString origin) {
        this.origin = origin;
    }

    public ReloadOperatorString(SourcePosition position, Operator operator) {
        this.origin = new NormalOperatorString(position, operator);
    }

    public ReloadOperatorString(SourcePosition position, Operator[] operators) {
        this.origin = new UncertainOperatorString(position, operators);
    }

    @Override
    public SourcePosition getPosition() {
        return origin.getPosition();
    }
}
