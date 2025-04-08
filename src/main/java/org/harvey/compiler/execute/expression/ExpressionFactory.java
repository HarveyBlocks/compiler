package org.harvey.compiler.execute.expression;

import org.harvey.compiler.common.collecction.ListPoint;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.execute.local.LocalVariableManager;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.util.List;
import java.util.ListIterator;

/**
 * 解析表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-03 19:29
 */
public class ExpressionFactory {
    /**
     * @param localVariableManager nullable
     */
    public static Expression simplyMapToExpression(
            List<SourceString> expression,
            LocalVariableManager localVariableManager) {
        return new Expression();
    }

    /**
     * @param iterator previous is identifier
     */
    public static FullIdentifierString fullIdentifier(
            SourcePosition position, String firstName, ListIterator<SourceString> iterator) {
        List<SourceString> fullIdentifierString = DeclarableFactory.departFullIdentifier(position, firstName, iterator);
        return fullIdentifier(fullIdentifierString);
    }

    private static FullIdentifierString fullIdentifier(List<SourceString> fullIdentifierString) {
        String[] fullname = fullIdentifierString.stream().map(SourceString::getValue).toArray(String[]::new);
        SourcePosition[] positions = fullIdentifierString.stream()
                .map(SourceString::getPosition)
                .toArray(SourcePosition[]::new);
        return new FullIdentifierString(positions, fullname);
    }
}
