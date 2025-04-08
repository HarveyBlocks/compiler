package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 17:20
 */
public abstract class MemberSupplier extends ExpressionElement implements ItemString {

    public MemberSupplier(SourcePosition position) {
        super(position);
    }

    public abstract MemberType getType();
}

