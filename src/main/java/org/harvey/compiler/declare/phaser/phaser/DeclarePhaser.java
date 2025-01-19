package org.harvey.compiler.declare.phaser.phaser;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.context.DeclaredContext;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 22:27
 */
public interface DeclarePhaser<T extends DeclaredContext> {
    static boolean isOperator(SourceString ss, Operator operator) {
        return ss.getType() == SourceStringType.OPERATOR && operator.nameEquals(ss.getValue());
    }

    static void forbidden(SourceString embellish) {
        if (embellish != null) {
            throw new AnalysisExpressionException(embellish.getPosition(), embellish.getValue() + " is illegal here");
        }
    }

    T phase(Declarable declarable, int identifierIndex, Environment environment);
}
