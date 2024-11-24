package org.harvey.compiler.analysis.stmt.phaser.depart;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.analysis.AnalysisException;

import java.util.LinkedList;

/**
 * 文件信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 23:38
 */
public class DepartedFileBody {
    private final LinkedList<DepartedPart> departed = new LinkedList<>();

    public LinkedList<DepartedPart> get() {
        return CollectionUtil.unmodifiableLinkedList(departed);
    }

    private void add(DepartedPart element) {
        this.departed.add(element);
    }

    public static DepartedFileBody depart(SourceTextContext context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        int bodyDepth = 0;
        SourceTextContext part = new SourceTextContext();
        DepartedFileBody dg = new DepartedFileBody();
        SourcePosition sp = null;
        for (SourceString ss : context) {
            sp = ss.getPosition();
            switch (ss.getValue()) {
                case "" + PropertyConstant.SENTENCE_END:
                    if (bodyDepth == 0 && !part.isEmpty()) {
                        dg.add(new DepartedSentencePart(part));
                        part = new SourceTextContext();
                    }
                    break;
                case "" + PropertyConstant.BODY_START:
                    if (bodyDepth < 0) {
                        throw new AnalysisException(sp, "Illegal `{}` matching");
                    }
                    part.add(ss);
                    bodyDepth++;
                    break;
                case "" + PropertyConstant.BODY_END:
                    bodyDepth--;
                    if (bodyDepth < 0) {
                        throw new AnalysisException(sp, "Illegal `{}` matching");
                    }
                    if (bodyDepth == 0) {
                        if (!part.isEmpty()) {
                            dg.add(new DepartedStatementWithBodyPart(part));
                            part = new SourceTextContext();
                        }
                    } else {
                        part.add(ss);
                    }
                    break;
                default:
                    part.add(ss);
            }
        }
        if (!part.isEmpty()) {
            throw new AnalysisException(sp, "File shouldn't be ended");
        }
        return dg;
    }
}

