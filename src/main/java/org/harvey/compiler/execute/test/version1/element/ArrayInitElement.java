package org.harvey.compiler.execute.test.version1.element;

import lombok.Getter;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * Array初始化的<pre>{@code
 *  Array<int> arr = {1+1,2+2,3+3};
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 21:39
 */
@Getter
public class ArrayInitElement extends ExpressionElement implements ItemString {
    private final ComplexExpressionWrap[] elementWarps;

    public ArrayInitElement(SourcePosition position, ComplexExpressionWrap[] elementWarps) {
        super(position);
        this.elementWarps = elementWarps;
    }
}
