package org.harvey.compiler.declare.context;

import lombok.Data;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 22:34
 */
@Data
public abstract class DeclaredContext {
    // -1表示无, 否则表示索引, 不是索引的, 就是未知的标识
    protected int identifierReference = -2; // 4bit

    protected AccessControl accessControl;
    protected Embellish embellish;


    protected static void assertNotNull(SourcePosition position, Object obj, String name) {
        if (obj == null) {
            throw new AnalysisExpressionException(position, name + " require not null");
        }
    }

    public static void assertNotEmpty(SourcePosition position, Collection<?> assignMap, String name) {
        if (assignMap.isEmpty()) {
            throw new AnalysisExpressionException(position, name + " require not null");
        }
    }
}
