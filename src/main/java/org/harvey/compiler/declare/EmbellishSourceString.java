package org.harvey.compiler.declare;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 13:00
 */
@Getter
public class EmbellishSourceString {
    protected SourceString constMark = null;
    protected SourceString staticMark = null;
    protected SourceString finalMark = null;
    protected SourceString sealedMark = null;
    protected SourceString abstractMark = null;

    public boolean isNull() {
        return constMark == null &&
                staticMark == null &&
                finalMark == null &&
                sealedMark == null &&
                abstractMark == null;
    }

    /**
     * 存在即违法
     */
    public void existIsIllegal(String name) {
        if (constMark != null) {
            throw new AnalysisExpressionException(constMark.getPosition(), name + " use embellish is not allowed.");
        }
        if (staticMark != null) {
            throw new AnalysisExpressionException(staticMark.getPosition(), name + " use embellish is not allowed.");
        }
        if (finalMark != null) {
            throw new AnalysisExpressionException(finalMark.getPosition(), name + " use embellish is not allowed.");
        }
        if (sealedMark != null) {
            throw new AnalysisExpressionException(sealedMark.getPosition(), name + " use embellish is not allowed.");
        }
        if (abstractMark != null) {
            throw new AnalysisExpressionException(abstractMark.getPosition(), name + " use embellish is not allowed.");
        }

    }
}