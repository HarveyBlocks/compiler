package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 类型转换的类型元素, 也就是 type CAST obj 这样的格式进行计算
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 14:37
 */
public class CastParameterizedType extends ExpressionElement implements ItemString{
    private final MemberType parameterizedType;

    public CastParameterizedType(
            SourcePosition position, MemberType parameterizedType) {
        super(position);
        this.parameterizedType = parameterizedType;
    }


}
