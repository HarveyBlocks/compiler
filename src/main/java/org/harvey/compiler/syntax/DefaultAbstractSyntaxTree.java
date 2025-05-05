package org.harvey.compiler.syntax;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-01 16:06
 */
@Getter
@Setter
public class DefaultAbstractSyntaxTree implements IAbstractSyntaxTree {
    private final OperatorString operator;
    private ItemString left;
    private ItemString right;

    public DefaultAbstractSyntaxTree(OperatorString operator) {
        this.operator = operator;
    }

    @Override
    public SourcePosition getPosition() {
        return operator.getPosition();
    }

    public ItemString removeRight() {
        ItemString old = right;
        right = null;
        return old;
    }

    public ItemString removeLeft() {
        ItemString old = left;
        left = null;
        return old;
    }
}
