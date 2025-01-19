package org.harvey.compiler.depart;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.util.LinkedList;

/**
 * 简单区分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 16:41
 */
public class SimpleDepartedBodyFactory {

    public static final String BODY_START = "" + SourceFileConstant.BODY_START;
    public static final String BODY_END = "" + SourceFileConstant.BODY_END;
    public static final String SENTENCE_END = "" + SourceFileConstant.SENTENCE_END;

    private SimpleDepartedBodyFactory() {
    }

    public static LinkedList<DepartedPart> depart(SourceTextContext context) {
        if (context == null) {
            return null;
        }
        if (context.isEmpty()) {
            return new LinkedList<>();
        }
        int bodyDepth = 0;
        SourceTextContext part = new SourceTextContext();
        LinkedList<DepartedPart> dps = new LinkedList<>();
        SourcePosition sp = null;
        for (SourceString ss : context) {
            sp = ss.getPosition();
            switch (ss.getValue()) {
                case SENTENCE_END:
                    part.add(ss);
                    if (bodyDepth == 0) {
                        dps.add(new DepartedPart(part));
                        part = new SourceTextContext();
                    }
                    break;
                case BODY_START:
                    // 要谨慎辨别是数组定义的{}还是Body的{}
                    if (bodyDepth < 0) {
                        throw new AnalysisException(sp, "Illegal `{}` matching");
                    }
                    part.add(ss);
                    bodyDepth++;
                    break;
                case BODY_END:
                    bodyDepth--;
                    if (bodyDepth < 0) {
                        throw new AnalysisException(sp, "Illegal `{}` matching");
                    }
                    part.add(ss);
                    if (bodyDepth == 0) {
                        dps.add(new DepartedPart(part));
                        part = new SourceTextContext();
                    }
                    break;
                default:
                    part.add(ss);
            }
        }
        if (!part.isEmpty()) {
            throw new AnalysisException(sp, "File shouldn't be ended");
        }
        return dps;
    }
}
