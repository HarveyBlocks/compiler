package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 一个array的init的表达式, 一个warp
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 21:10
 */
public class ArrayInitEachWarp extends DefaultComplexExpressionWrap {
    public static final String START = "{";
    public static final String END = "}";
    private final OuterEnvironment environment;
    private final int index;

    public ArrayInitEachWarp(SourcePosition position, OuterEnvironment environment, int index) {
        super(position);
        this.environment = environment;
        this.index = index;
    }

    @Override
    protected void afterSet() {
        // super.afterSet();
        // TODO 解析完毕后检查类型
        // 一定要能确定类型
        if (environment.typeDetermined()) {
            throw new AnalysisExpressionException(getPosition(), "expect determined type");
        }
    }
}
